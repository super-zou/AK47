package com.hetang.home;

//import android.app.Fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hetang.adapter.MeetRecommendListAdapter;
import com.hetang.util.InterActInterface;
import com.hetang.common.MyApplication;
import com.hetang.util.ParseUtils;
import com.hetang.util.SetAvatarActivity;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.hetang.R;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.common.DynamicsInteractDetailsActivity;
import com.hetang.common.HandlerTemp;
import com.hetang.meet.FillMeetInfoActivity;
import com.hetang.meet.MeetArchiveActivity;
import com.hetang.meet.UserMeetInfo;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.SharedPreferencesUtils;
import com.hetang.util.Slog;
import com.hetang.util.UserProfile;

import org.json.JSONArray;
import org.json.JSONException;
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
import static com.hetang.common.AddPictureActivity.ADD_PICTURE_BROADCAST;
import static com.hetang.common.DynamicsInteractDetailsActivity.MEET_RECOMMEND_COMMENT;
import static com.hetang.home.HomeFragment.COMMENT_UPDATE_RESULT;
import static com.hetang.meet.MeetDynamicsFragment.COMMENT_COUNT_UPDATE;

/**
 * Created by super-zou on 17-9-11.
 */

public class MoreRecommendActivity extends BaseAppCompatActivity {
    private static final boolean isDebug = true;
    private static final String TAG = "MoreRecommendActivity";
    //+Begin add by xuchunping for use XRecyclerView support loadmore
    //private RecyclerView recyclerView;
    private static final int PAGE_SIZE = 3;//page size
    private static final int DONE = 1;
    private static final int UPDATE = 2;
    private static final int GET_USER_PROFILE_DONE = 3;
    private static final String GET_RECOMMEND_URL = HttpUtil.DOMAIN + "?q=meet/recommend";
    private static final String GET_USER_PROFILE_URL = HttpUtil.DOMAIN + "?q=account_manager/get_user_profile";
    private static String responseText;
    JSONObject recommend_response;
    JSONArray recommendation;
    
    //private View viewContent;
    private int mType = 0;
    private String mTitle;
    private List<UserMeetInfo> meetList = new ArrayList<>();
    private UserProfile userProfile;
    private XRecyclerView recyclerView;
    private int mTempSize;
    //-End add by xuchunping for use XRecyclerView support loadmore
    private MeetRecommendListAdapter meetRecommendListAdapter;
    // private String realname;
    private int uid;
    private boolean loaded = false;
    private Handler handler;
    private boolean avatarSet = false;
    private LinearLayout addMeetInfo;
    Typeface font;
    private PictureAddBroadcastReceiver mReceiver ;
    int currentPosition = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.more_recommend);
        font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.custom_actionbar), font);
        registerLoginBroadcast();
        initView();
    }

    protected void initView() {
        handler = new MyHandler(this);

        TextView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        
        meetRecommendListAdapter = new MeetRecommendListAdapter(MyApplication.getContext());
        //viewContent = view.inflate(R.layout.meet_recommend, container, false);

        recyclerView = findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MyApplication.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    meetRecommendListAdapter.setScrolling(false);
                    meetRecommendListAdapter.notifyDataSetChanged();
                } else {
                    meetRecommendListAdapter.setScrolling(true);
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
        
        recyclerView.getDefaultFootView().setLoadingHint(getString(R.string.loading_pull_up_tip));
        recyclerView.getDefaultFootView().setNoMoreHint(getString(R.string.loading_no_more));
        //recyclerView.setArrowImageView(R.drawable.iconfont_downgrey);//TODO set pull down icon
        final int itemLimit = 6;

        // When the item number of the screen number is list.size-2,we call the onLoadMore
       // recyclerView.setLimitNumberToCallLoadMore(itemLimit);
        recyclerView.setRefreshProgressStyle(ProgressStyle.BallBeat);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);
        
        recyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {}

            @Override
            public void onLoadMore() {
                //getRecommendContent();
                loadData();
            }
        });
        //-End added by xuchunping
        
        meetRecommendListAdapter.setItemClickListener(new MeetRecommendListAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(MyApplication.getContext(), MeetArchiveActivity.class);
                intent.putExtra("meet", meetList.get(position));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        }, new InterActInterface(){
        
        @Override
            public void onCommentClick(View view, int position) {
                //createCommentDetails(meetList.get(position).getDid(), meetList.get(position).getCommentCount());
                currentPosition = position;
                createCommentDetails(meetList.get(position));
            }
            @Override
            public void onPraiseClick(View view, int position){
            }
            @Override
            public void onDynamicPictureClick(View view, int position, String[] pictureUrlArray, int index){
            }
        });
        
        recyclerView.setAdapter(meetRecommendListAdapter);

        //getRecommendContent();

        addMeetInfo =  findViewById(R.id.meet_info_add);
        addMeetInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if(avatarSet == false){
                    intent = new Intent(MyApplication.getContext(), SetAvatarActivity.class);
                    intent.putExtra("look_friend", true);
                }else {
                    intent = new Intent(MyApplication.getContext(), FillMeetInfoActivity.class);
                }
                
                if(userProfile != null){
                    intent.putExtra("userProfile", userProfile);
                    //intent.putExtra("sex", userProfile.getSex());
                    //intent.putExtra("hometown", userProfile.getHometown());
                }
                startActivity(intent);
            }
        });

        getRecommendContent();
    }
    
    public void createCommentDetails(UserMeetInfo meetRecommend) {
        Intent intent = new Intent(MyApplication.getContext(), DynamicsInteractDetailsActivity.class);
        intent.putExtra("type", MEET_RECOMMEND_COMMENT);
        intent.putExtra("meetRecommend", meetRecommend);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivityForResult(intent, Activity.RESULT_FIRST_USER);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (isDebug) Slog.d(TAG, "===================onActivityResult requestCode: "+requestCode+" resultCode: "+resultCode);
        if (requestCode == Activity.RESULT_FIRST_USER){
            switch (resultCode){
                case COMMENT_UPDATE_RESULT:
                    int commentCount = data.getIntExtra("commentCount", 0);
                    if (isDebug) Slog.d(TAG, "==========commentCount: "+commentCount);
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putInt("commentCount", commentCount);
                    msg.setData(bundle);
                    msg.what = COMMENT_COUNT_UPDATE;
                    handler.sendMessage(msg);
                    break;
                default:
                    break;
            }
        }
    }
    
    protected void loadData() {
        getRecommendContent();
    }

    public void getRecommendContent(){

        getUserProfile();

        int page = meetList.size() / PAGE_SIZE;
        RequestBody requestBody = new FormBody.Builder()
                                            .add("step", String.valueOf(PAGE_SIZE))
                                            .add("page", String.valueOf(page))
                                            .build();
                                            
                                            if(isDebug) Log.d(TAG, "initContentView requestBody:" + requestBody.toString() + " page:" + page);
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_RECOMMEND_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response != null){
                    String responseText = response.body().string();
                    if(isDebug) Slog.d(TAG, "getRecommendContent response : "+responseText);
                    getResponseText(responseText, false);
                    handler.sendEmptyMessage(DONE);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure e:" + e);
            }
        });
    }
    
    public void getResponseText(String responseText , boolean isUpdate) {

        if (isDebug) Slog.d(TAG, "====================getResponseText: " + responseText);
        //+Begin added by xuchunping
        List<UserMeetInfo> tempList = ParseUtils.getRecommendMeetList(responseText, isUpdate);
        mTempSize = 0;
        if (null != tempList && tempList.size() != 0) {
            mTempSize = tempList.size();
            if(isUpdate){
                meetList.clear();
            }
            meetList.addAll(tempList);
            if(isDebug) Log.d(TAG, "getResponseText list.size:" + tempList.size());
        }
        //-End added by xuchunping
    }
    
    private void updateData() {
        String last = SharedPreferencesUtils.getRecommendLast(MyApplication.getContext());
        if (isDebug) Slog.d(TAG, "=======last:" + last);

        int page = 0;
        RequestBody requestBody = new FormBody.Builder()
                .add("last", last)
                .add("step", String.valueOf(PAGE_SIZE+1))
                .add("page", String.valueOf(0))
                .build();
                
                HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_RECOMMEND_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null && response.body() != null) {
                    String responseText = response.body().string();
                    if(isDebug) Slog.d(TAG, "========== updateData response text : " + responseText);
                    if (responseText != null) {
                        getResponseText(responseText, true);
                        handler.sendEmptyMessage(UPDATE);
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) { }
        });
    }
    
    private void getUserProfile(){
        RequestBody requestBody = new FormBody.Builder().build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_USER_PROFILE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if(isDebug) Slog.d(TAG, "==========get archive response text : " + responseText);
                    if (responseText != null) {
                    if (!TextUtils.isEmpty(responseText)) {
                            try {
                                JSONObject jsonObject = new JSONObject(responseText).optJSONObject("user");
                                if(isDebug) Slog.d(TAG, "==============user profile object: "+jsonObject);
                                if(jsonObject != null){
                                    userProfile = ParseUtils.getUserProfileFromJSONObject(jsonObject);
                                }

                                handler.sendEmptyMessage(GET_USER_PROFILE_DONE);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
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
            case DONE:
                meetRecommendListAdapter.setData(meetList);
                meetRecommendListAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();

                if (mTempSize < PAGE_SIZE) {
                    //loading finished
                    recyclerView.setNoMore(true);
                    //recyclerView.setLoadingMoreEnabled(false);
                }
                break;
                
                case UPDATE:
                //save last update timemills
                SharedPreferencesUtils.setRecommendLast(MyApplication.getContext(), String.valueOf(System.currentTimeMillis() / 1000));
                meetRecommendListAdapter.setScrolling(false);
                meetRecommendListAdapter.setData(meetList);
                meetRecommendListAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();
                break;
                
                case GET_USER_PROFILE_DONE:
                if(userProfile != null){
                    if(isDebug) Slog.d(TAG, "==============GET_USER_PROFILE_DONE cid: "+userProfile.getCid());
                    if(userProfile.getCid() == 0){
                        if(addMeetInfo.getVisibility() == View.GONE){
                            addMeetInfo.setVisibility(View.VISIBLE);
                        }
                    }else {
                        if(addMeetInfo.getVisibility() == View.VISIBLE){
                            addMeetInfo.setVisibility(View.GONE);
                        }
                    }

                    if(!TextUtils.isEmpty(userProfile.getAvatar())){
                        avatarSet = true;
                    }
                }else {
                    if(addMeetInfo.getVisibility() == View.GONE){
                        addMeetInfo.setVisibility(View.VISIBLE);
                    }
                }
                break;
                
                case COMMENT_COUNT_UPDATE:
                Bundle bundle = message.getData();
                int commentCount = bundle.getInt("commentCount");
                if (isDebug) Slog.d(TAG, "------------------>COMMENT_COUNT_UPDATE: position: "+currentPosition+ " commentCount: "+commentCount);
                meetList.get(currentPosition).setCommentCount(commentCount);
                meetRecommendListAdapter.setData(meetList);
                meetRecommendListAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if(isDebug) Slog.d(TAG, "=============onResume");
        getUserProfile();
        //updateData();
    }
    
    private class PictureAddBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case ADD_PICTURE_BROADCAST:
                    if (isDebug)  Slog.d(TAG, "==========DYNAMICS_ADD_BROADCAST");
                    updateData();
                    break;
                default:
                    break;
            }
        }
    }
    
    //register local broadcast to receive DYNAMICS_ADD_BROADCAST
    private void registerLoginBroadcast() {
        mReceiver = new PictureAddBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ADD_PICTURE_BROADCAST);
        LocalBroadcastManager.getInstance(MyApplication.getContext()).registerReceiver(mReceiver, intentFilter);
    }

    //unregister local broadcast
    private void unRegisterLoginBroadcast() {
        LocalBroadcastManager.getInstance(MyApplication.getContext()).unregisterReceiver(mReceiver);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterLoginBroadcast();
    }

    static class MyHandler extends HandlerTemp<MoreRecommendActivity> {
        public MyHandler(MoreRecommendActivity cls){
            super(cls);
        }

        @Override
        public void handleMessage(Message message) {
            MoreRecommendActivity moreRecommendActivity = ref.get();
            if (moreRecommendActivity != null) {
                moreRecommendActivity.handleMessage(message);
            }
        }
    }

}
