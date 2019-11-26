package com.hetang.meet;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hetang.adapter.DynamicsListAdapter;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.common.Dynamic;
import com.hetang.common.DynamicsInteractDetailsActivity;
import com.hetang.common.HandlerTemp;
import com.hetang.home.HomeFragment;
import com.hetang.util.CommonDialogFragmentInterface;
import com.hetang.util.CommonUserListDialogFragment;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.InterActInterface;
import com.hetang.common.MyApplication;
import com.hetang.util.PictureReviewDialogFragment;
import com.hetang.util.Slog;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.hetang.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class SpecificUserDynamicsActivity extends BaseAppCompatActivity implements CommonDialogFragmentInterface {
    private static final String TAG = "SpecificUserDynamics";
    private static final boolean isDebug = true;
    private static final int PAGE_SIZE = 6;
    private static final int NO_MORE_DYNAMICS = 0;
    private static final int LOAD_DYNAMICS_DONE = 1;
    private static final int UPDATE_COMMENT = 4;
    private static final int COMMENT_COUNT_UPDATE = 5;
    private static final String GET_DYNAMIC_URL = HttpUtil.DOMAIN + "?q=dynamic/action/get";
    private static final String REQUEST_INTERACT_URL = HttpUtil.DOMAIN + "?q=dynamic/interact/get";

    JSONObject dynamics_response;
    JSONObject commentResponse;
    JSONArray dynamics;
    JSONArray praiseArray;
    String requstUrl = "";
    RequestBody requestBody = null;
    private View viewContent;
    private List<Dynamic> meetList = new ArrayList<>();
    private int mTempSize;
    private XRecyclerView recyclerView;
    private DynamicsListAdapter dynamicsListAdapter;
    private Handler handler;
    private static final int DYNAMICS_PRAISED = 7;
    private int currentPos = 0;
    int uid = 0;
    MeetDynamicsFragment meetDynamicsFragment;
    HomeFragment homeFragment;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.specific_user_dynamics_activity);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        uid = getIntent().getIntExtra("uid", -1);
        handler = new MyHandler(this);
        if (homeFragment == null){
            homeFragment = new HomeFragment();
        }
        initView();

    }
    
    private void initView(){
        TextView backLeft = findViewById(R.id.left_back);
        backLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.specific_dynamics);
        dynamicsListAdapter = new DynamicsListAdapter(MyApplication.getContext(), true);

        meetDynamicsFragment = new MeetDynamicsFragment();
        
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MyApplication.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    dynamicsListAdapter.setScrolling(false);
                    dynamicsListAdapter.notifyDataSetChanged();
                } else {
                    dynamicsListAdapter.setScrolling(true);
                }

                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        
        //+Begin added by xuchunping
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setRefreshProgressStyle(BallSpinFadeLoader);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallRotate);
        //mRecyclerView.setArrowImageView(R.drawable.);

        recyclerView.getDefaultRefreshHeaderView().setRefreshTimeVisible(false);
        recyclerView.setPullRefreshEnabled(false);

       // recyclerView.getDefaultFootView().setLoadingHint(getString(R.string.loading_pull_up_tip));
        recyclerView.getDefaultFootView().setNoMoreHint(getString(R.string.loading_no_more_no_update));
        final int itemLimit = 5;
        
        // When the item number of the screen number is list.size-2,we call the onLoadMore
        recyclerView.setLimitNumberToCallLoadMore(PAGE_SIZE - 2);
        recyclerView.setRefreshProgressStyle(ProgressStyle.BallBeat);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);

        recyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {

            @Override
            public void onRefresh() { }

            @Override
            public void onLoadMore() {
                loadData();
            }
        });
        
        recyclerView.scrollToPosition(0);

        //callback from dynamicsListAdapter, when comment icon touched, will show comment input dialog
        dynamicsListAdapter.setOnCommentClickListener(new InterActInterface() {
            @Override
            public void onCommentClick(View view, int position) {
                //createCommentDetails(aid, count);
                currentPos = position;
                createCommentDetails(meetList.get(position));

            }
            @Override
            public void onPraiseClick(View view, int position){
            
            Bundle bundle = new Bundle();
                bundle.putInt("type", DYNAMICS_PRAISED);
                bundle.putLong("aid", meetList.get(position).getDid());
                bundle.putString("title", MyApplication.getContext().getResources().getString(R.string.praised_dynamic));
                CommonUserListDialogFragment commonUserListDialogFragment = new CommonUserListDialogFragment();
                commonUserListDialogFragment.setArguments(bundle);
                commonUserListDialogFragment.show(getSupportFragmentManager(), "CommonUserListDialogFragment");

            }
            @Override
            public void onDynamicPictureClick(View view, int position, String[] pictureUrlArray, int index){
                Bundle bundle = new Bundle();
                bundle.putInt("index", index);
                bundle.putStringArray("pictureUrlArray", pictureUrlArray);

                PictureReviewDialogFragment pictureReviewDialogFragment = new PictureReviewDialogFragment();
                pictureReviewDialogFragment.setArguments(bundle);
                pictureReviewDialogFragment.show(getSupportFragmentManager(), "PictureReviewDialogFragment");
            }
            
            @Override
            public void onOperationClick(View view, int position){}

        });

        recyclerView.setAdapter(dynamicsListAdapter);

        progressImageView = findViewById(R.id.animal_progress);
        animationDrawable = (AnimationDrawable)progressImageView.getDrawable();
        
        progressImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        },50);

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.custom_actionbar), font);
        font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.specific_dynamics), font);

        loadData();
    }
    
    private void loadData() {
        Slog.d(TAG, "------------------->loadData");
        int page = meetList.size() / PAGE_SIZE;
        requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_DYNAMIC_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
            
            if (response.body() != null) {
                    String responseText = response.body().string();
                    //Slog.d(TAG, "==========response : "+response.body());
                    Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null) {
                        List<Dynamic> tempList = homeFragment.getDynamicsResponse(responseText, false, handler);
                        mTempSize = 0;
                        if (null != tempList && tempList.size() > 0) {
                            // meetList.clear();
                            mTempSize = tempList.size();
                            meetList.addAll(tempList);
                            Log.d(TAG, "getResponseText list.size:" + tempList.size());
                            handler.sendEmptyMessage(LOAD_DYNAMICS_DONE);
                        }else {
                            handler.sendEmptyMessage(NO_MORE_DYNAMICS);
                        }
                   }
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
    public void handleMessage(Message message) {
        switch (message.what){
            case NO_MORE_DYNAMICS:
                recyclerView.setNoMore(true);
                if (progressImageView.getVisibility() == View.VISIBLE){
                    animationDrawable.stop();
                    progressImageView.setVisibility(View.GONE);
                }
                break;
                
                case LOAD_DYNAMICS_DONE:
                if (progressImageView.getVisibility() == View.VISIBLE){
                    animationDrawable.stop();
                    progressImageView.setVisibility(View.GONE);
                }
                if(meetList.size() > 0){
                    dynamicsListAdapter.setData(meetList);
                    dynamicsListAdapter.notifyDataSetChanged();
                    recyclerView.loadMoreComplete();
                }

                if (mTempSize < PAGE_SIZE) {
                    //loading finished
                    recyclerView.setNoMore(true);
                    recyclerView.loadMoreComplete();
                    recyclerView.setLoadingMoreEnabled(false);
                }
                break;
                
                case UPDATE_COMMENT:
                dynamicsListAdapter.setData(meetList);
                dynamicsListAdapter.notifyDataSetChanged();
                break;
            case COMMENT_COUNT_UPDATE:
                Bundle bundle = message.getData();
                int commentCount = bundle.getInt("commentCount");
                Slog.d(TAG, "------------------>COMMENT_COUNT_UPDATE: position: "+currentPos+ " commentCount: "+commentCount);
                meetList.get(currentPos).setCommentCount(commentCount);
                dynamicsListAdapter.setData(meetList);
                dynamicsListAdapter.notifyDataSetChanged();
            default:
                break;
        }
    }
    
    public void createCommentDetails(Dynamic dynamic) {
        Intent intent = new Intent(MyApplication.getContext(), DynamicsInteractDetailsActivity.class);
        intent.putExtra("type", DynamicsInteractDetailsActivity.DYNAMIC_COMMENT);
        intent.putExtra("dynamic", dynamic);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivityForResult(intent, Activity.RESULT_FIRST_USER);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Slog.d(TAG, "===================onActivityResult requestCode: "+requestCode+" resultCode: "+resultCode);
        if (requestCode == Activity.RESULT_FIRST_USER){
            if (resultCode == HomeFragment.COMMENT_UPDATE_RESULT){
                int commentCount = data.getIntExtra("commentCount", 0);
                Slog.d(TAG, "==========commentCount: "+commentCount);
                Message msg = new Message();
                Bundle bundle = new Bundle();
                bundle.putInt("commentCount", commentCount);
                msg.setData(bundle);
                msg.what = COMMENT_COUNT_UPDATE;
                handler.sendMessage(msg);
            }
        }
    }
    
    @Override
    public void onBackFromDialog(int type, int result, boolean status) { }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    
    static class MyHandler extends HandlerTemp<SpecificUserDynamicsActivity> {
        public MyHandler(SpecificUserDynamicsActivity cls){
            super(cls);
        }


        @Override
        public void handleMessage(Message message) {
            SpecificUserDynamicsActivity specificUserDynamicsActivity = ref.get();
            if (specificUserDynamicsActivity != null) {
                specificUserDynamicsActivity.handleMessage(message);
            }
        }
    }
}

                
