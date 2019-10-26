package com.hetang.meet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hetang.common.DynamicsInteractDetailsActivity;
import com.hetang.common.HandlerTemp;
import com.hetang.home.HomeFragment;
import com.hetang.util.BaseFragment;
import com.hetang.util.HttpUtil;
import com.hetang.util.InterActInterface;
import com.hetang.common.MyApplication;
import com.hetang.util.ParseUtils;
import com.hetang.util.SharedPreferencesUtils;
import com.hetang.util.Slog;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.hetang.R;
import com.hetang.adapter.MeetDiscoveryListAdapter;

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

/**
 * Created by haichao.zou on 2017/11/23.
 */

public class MeetDiscoveryFragment extends BaseFragment {

    private static final boolean debug = false;
    private static final String TAG = "MeetDiscoveryFragment";
    //+Begin add by xuchunping for use XRecyclerView support loadmore
    //private RecyclerView recyclerView;
    private static final int PAGE_SIZE = 6;
    //private static final int DONE = 1;
    private static final int UPDATE_DONE = 3;
    private static final int LOAD_MORE_DONE = 1;
    private static final int NO_MORE = 0;
    private static final int LOAD_MORE_END = 2;
    
    private static final String GET_DISCOVERY_URL = HttpUtil.DOMAIN + "?q=meet/discovery/get";
    private static String responseText;
    JSONObject discovery_response;
    JSONArray discovery;
    private View viewContent;
    private int mType = 0;
    private String mTitle;
    private List<UserMeetInfo> meetMemberList = new ArrayList<>();
    private UserMeetInfo userMeetInfo;
    private XRecyclerView recyclerView;
    private int mTempSize;
    //-End add by xuchunping for use XRecyclerView support loadmore
    private MeetDiscoveryListAdapter meetDiscoveryListAdapter;
    // private String realname;
    private int uid;
    private Boolean loaded = false;
    private Context mContext;
    private Handler handler = new MyHandler(this);
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    
    @Override
    protected void initView(View view) {}

    @Override
    protected void loadData() {}

    @Override
    protected int getLayoutId() {
        return 0;
    }
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (debug) Slog.d(TAG, "=================onCreateView===================");
        mContext = getContext();
        initContentView();
        meetDiscoveryListAdapter = new MeetDiscoveryListAdapter(getContext());
        viewContent = inflater.inflate(R.layout.meet_discovery, container, false);
        recyclerView = (XRecyclerView) viewContent.findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    meetDiscoveryListAdapter.setScrolling(false);
                    meetDiscoveryListAdapter.notifyDataSetChanged();
                } else {
                    meetDiscoveryListAdapter.setScrolling(true);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        
        recyclerView.setRefreshProgressStyle(BallSpinFadeLoader);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallRotate);
        recyclerView.getDefaultRefreshHeaderView().setRefreshTimeVisible(true);
        recyclerView.getDefaultFootView().setLoadingHint(getString(R.string.loading_pull_up_tip));
        recyclerView.getDefaultFootView().setNoMoreHint(getString(R.string.loading_no_more));
        final int itemLimit = 5;

        // When the item number of the screen number is list.size-2,we call the onLoadMore
        recyclerView.setLimitNumberToCallLoadMore(4);
        recyclerView.setRefreshProgressStyle(ProgressStyle.BallBeat);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);
        
        recyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                updateData();
            }

            @Override
            public void onLoadMore() {
                initContentView();
            }
        });
        recyclerView.setAdapter(meetDiscoveryListAdapter);
        
        meetDiscoveryListAdapter.setItemClickListener(new MeetDiscoveryListAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(MyApplication.getContext(), MeetArchiveActivity.class);
                intent.putExtra("meet", meetMemberList.get(position));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        }, new InterActInterface(){
            @Override
            public void onCommentClick(View view, int position) {
                //createCommentDetails(meetList.get(position).getDid(), meetList.get(position).getCommentCount());
                //currentPosition = position;
                createCommentDetails(mContext, meetMemberList.get(position), DynamicsInteractDetailsActivity.MEET_RECOMMEND_COMMENT);
            }
            @Override
            public void onPraiseClick(View view, int position){
            }
            @Override
            public void onDynamicPictureClick(View view, int position, String[] pictureUrlArray, int index){
            }
        });
        
        //show progressImage before loading done
        progressImageView = viewContent.findViewById(R.id.animal_progress);
        animationDrawable = (AnimationDrawable)progressImageView.getDrawable();
        progressImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        },50);

        return viewContent;

    }
    
    public void createCommentDetails(Context context, UserMeetInfo meetRecommend, int type) {
        Intent intent = new Intent(context, DynamicsInteractDetailsActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("meetRecommend", meetRecommend);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivityForResult(intent, Activity.RESULT_FIRST_USER);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (debug) Slog.d(TAG, "=================onViewCreated===================");
        // initConentView();
    }
    
    public void initContentView() {
        if (debug) Slog.d(TAG, "===============initConentView==============");

        int page = meetMemberList.size() / PAGE_SIZE;
        RequestBody requestBody = new FormBody.Builder()
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();
        Log.d(TAG, "initContentView requestBody:" + requestBody.toString() + " page:" + page);
        HttpUtil.sendOkHttpRequest(getContext(), GET_DISCOVERY_URL, requestBody, new Callback() {
            int check_login_user = 0;
            String user_name;
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (debug) Slog.d(TAG, "response : " + responseText);
                getResponseText(responseText);
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });

    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Slog.d(TAG, "===================onActivityResult requestCode: "+requestCode+" resultCode: "+resultCode);
        if (requestCode == Activity.RESULT_FIRST_USER){
            switch (resultCode){
                case HomeFragment.COMMENT_UPDATE_RESULT:
                    int commentCount = data.getIntExtra("commentCount", 0);
                    Slog.d(TAG, "==========commentCount: "+commentCount);
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putInt("commentCount", commentCount);
                    msg.setData(bundle);
                    msg.what = MeetDynamicsFragment.COMMENT_COUNT_UPDATE;
                    handler.sendMessage(msg);
                    break;
                    
                    case HomeFragment.PRAISE_UPDATE_RESULT:
                    handler.sendEmptyMessage(MeetDynamicsFragment.PRAISE_UPDATE);
                    break;
                case HomeFragment.LOVE_UPDATE_RESULT:
                    handler.sendEmptyMessage(MeetDynamicsFragment.LOVE_UPDATE);
                    break;
                default:
                    break;
            }
        }
    }
    
    public void getResponseText(String responseText) {
        if (debug) Slog.d(TAG, "====================getResponseText====================");
        //+Begin added by xuchunping
        List<UserMeetInfo> tempList = ParseUtils.getMeetDiscoveryList(responseText);
        
        if (null != tempList) {
            mTempSize = tempList.size();
            if(mTempSize > 0){
                meetMemberList.addAll(tempList);
                Log.d(TAG, "getResponseText list.size:" + tempList.size());
                if (mTempSize < PAGE_SIZE){
                    handler.sendEmptyMessage(LOAD_MORE_END);
                }else {
                    handler.sendEmptyMessage(LOAD_MORE_DONE);
                }
            }
        }else {
            handler.sendEmptyMessage(NO_MORE);
        }
    }
    
    private void updateData() {
        String last = SharedPreferencesUtils.getDiscoveryLast(getContext());
        RequestBody requestBody = new FormBody.Builder().add("last", last)
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(0))
                .build();
        Log.d(TAG, "updateData requestBody:" + requestBody.toString() + " last:" + last);
        HttpUtil.sendOkHttpRequest(getContext(), GET_DISCOVERY_URL, requestBody, new Callback() {
            int check_login_user = 0;
            String user_name;
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (debug) Slog.d(TAG, "response : " + responseText);
                Log.d(TAG, "response : " + responseText);
                if (responseText != null) {
                    List<UserMeetInfo> tempList = ParseUtils.getMeetDiscoveryList(responseText);
                    if (null != tempList && tempList.size() != 0) {
                        mTempSize = tempList.size();
                        meetMemberList.clear();
                        meetMemberList.addAll(tempList);
                        Log.d(TAG, "getResponseText list.size:" + tempList.size());
                    }
                    handler.sendEmptyMessage(UPDATE_DONE);
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }

    private void stopLoadProgress(){
        if (progressImageView.getVisibility() == View.VISIBLE){
            animationDrawable.stop();
            progressImageView.setVisibility(View.GONE);
        }
    }
    
    private void handleMessage(Message message){
        switch (message.what){
            case LOAD_MORE_DONE:
                meetDiscoveryListAdapter.setData(meetMemberList);
                meetDiscoveryListAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();
               // recyclerView.loadMoreComplete();
                stopLoadProgress();

                break;
                case NO_MORE:
                recyclerView.setNoMore(true);
                recyclerView.loadMoreComplete();
                //recyclerView.setLoadingMoreEnabled(false);
                stopLoadProgress();
                break;
            case LOAD_MORE_END:
                meetDiscoveryListAdapter.setData(meetMemberList);
                meetDiscoveryListAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();
                recyclerView.loadMoreComplete();
                recyclerView.setNoMore(true);
               // recyclerView.setLoadingMoreEnabled(false);
                stopLoadProgress();
                break;
                case UPDATE_DONE:
                SharedPreferencesUtils.setDiscoveryLast(getContext(), String.valueOf(System.currentTimeMillis() / 1000));
                meetDiscoveryListAdapter.setData(meetMemberList);
                meetDiscoveryListAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();
                break;
                default:
                    break;
        }
    }
    
    static class MyHandler extends HandlerTemp<MeetDiscoveryFragment> {

        public MyHandler(MeetDiscoveryFragment cls){
            super(cls);
        }

        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            MeetDiscoveryFragment meetDiscoveryFragment = ref.get();
            if (meetDiscoveryFragment != null) {
                meetDiscoveryFragment.handleMessage(message);
            }
        }
    }
    
   @Override
    public void onDestroy() {
        super.onDestroy();
        
        if (recyclerView != null){
            recyclerView.destroy();
            recyclerView = null;
        }
    }
}
