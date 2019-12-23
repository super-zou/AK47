package com.hetang.meet;

//import android.app.Fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.adapter.MeetRecommendListAdapter;
import com.hetang.main.MeetArchiveActivity;
import com.hetang.util.InterActInterface;
import com.hetang.common.MyApplication;
import com.hetang.util.ParseUtils;
import com.hetang.util.RoundImageView;
import com.hetang.common.SetAvatarActivity;

import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.hetang.common.DynamicsInteractDetailsActivity;
import com.hetang.common.HandlerTemp;
import com.hetang.util.BaseFragment;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.SharedPreferencesUtils;
import com.hetang.util.Slog;
import com.hetang.util.UserProfile;

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
import static com.hetang.common.SetAvatarActivity.AVATAR_SET_ACTION_BROADCAST;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

import static com.hetang.common.AddPictureActivity.ADD_PICTURE_BROADCAST;
import static com.hetang.common.DynamicsInteractDetailsActivity.MEET_RECOMMEND_COMMENT;
import static com.hetang.common.DynamicsInteractDetailsActivity.MY_CONDITION_COMMENT;
import static com.hetang.home.HomeFragment.COMMENT_UPDATE_RESULT;
import static com.hetang.home.HomeFragment.LOVE_UPDATE_RESULT;
import static com.hetang.home.HomeFragment.MY_COMMENT_UPDATE_RESULT;
import static com.hetang.home.HomeFragment.MY_LOVE_UPDATE_RESULT;
import static com.hetang.home.HomeFragment.MY_PRAISE_UPDATE_RESULT;
import static com.hetang.home.HomeFragment.PRAISE_UPDATE_RESULT;
import static com.hetang.meet.MeetDynamicsFragment.COMMENT_COUNT_UPDATE;
import static com.hetang.meet.MeetDynamicsFragment.LOVE_UPDATE;
import static com.hetang.meet.MeetDynamicsFragment.MY_COMMENT_COUNT_UPDATE;
import static com.hetang.meet.MeetDynamicsFragment.PRAISE_UPDATE;

/**
 * Created by super-zou on 17-9-11.
 */

public class MeetRecommendFragment extends BaseFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "MeetRecommendFragment";
    private static final int PAGE_SIZE = 8;//page size
    private static final int GET_RECOMMEND_DONE = 1;
    private static final int GET_RECOMMEND_UPDATE = 2;
    private static final int GET_USER_PROFILE_DONE = 3;
    public static final int MY_CONDITION_SET_DONE = 30;
    public static final int MY_CONDITION_NOT_SET = 31;
    private static final int NO_MORE_RECOMMEND = 9;
    private static final int NO_UPDATE_RECOMMEND = 10;
    private static final int MY_CONDITION_LOVE_UPDATE = 11;
    private static final int MY_CONDITION_PRAISE_UPDATE = 12;
    private static final String GET_RECOMMEND_URL = HttpUtil.DOMAIN + "?q=meet/get_recommend";
    public static final String GET_MY_CONDITION_URL = HttpUtil.DOMAIN + "?q=meet/get_my_condition";
    private static final String GET_USER_PROFILE_URL = HttpUtil.DOMAIN + "?q=account_manager/get_user_profile";
    private List<UserMeetInfo> meetList = new ArrayList<>();
    private UserProfile userProfile;
    private XRecyclerView recyclerView;
    private int mResponseSize;
    private int currentPage = 0;
    private int currentPosition = -1;
    private Handler handler = new MyHandler(this);
    private boolean avatarSet = false;
    private LinearLayout addMeetInfo;
    private PictureAddBroadcastReceiver mReceiver ;
    private MeetRecommendListAdapter meetRecommendListAdapter;
    
    View mMyMeetView;
    View mView;
    int isConditionSet = -1;
    UserMeetInfo myCondition;
    TextView lovedIcon;
    TextView lovedView;
    TextView thumbsView;
    TextView thumbsIcon;
    TextView commentCountView;
    public static int student = 0;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    
    @Override
    protected int getLayoutId() {
        return R.layout.meet_recommend;
    }

    @Override
    protected void initView(View view) {
        if (isDebug) Slog.d(TAG, "=================onCreateView===================");
        mView = view;
        meetRecommendListAdapter = new MeetRecommendListAdapter(getContext());
        //viewContent = view.inflate(R.layout.meet_recommend, container, false);
        recyclerView = view.findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        
        recyclerView.setRefreshProgressStyle(BallSpinFadeLoader);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallRotate);
        //mRecyclerView.setArrowImageView(R.drawable.);

        recyclerView.getDefaultRefreshHeaderView().setRefreshTimeVisible(true);

        recyclerView.getDefaultFootView().setLoadingHint(getString(R.string.loading_pull_up_tip));
        recyclerView.getDefaultFootView().setNoMoreHint(getString(R.string.loading_no_more));
        //recyclerView.setArrowImageView(R.drawable.iconfont_downgrey);//TODO set pull down icon
        // When the item number of the screen number is list.size-2,we call the onLoadMore
        recyclerView.setLimitNumberToCallLoadMore(2);
        recyclerView.setRefreshProgressStyle(ProgressStyle.BallBeat);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);
        
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
        
        recyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                requestData(false);
            }

            @Override
            public void onLoadMore() {
                requestData(true);
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
                createCommentDetails(meetList.get(position), MEET_RECOMMEND_COMMENT);
            }
            @Override
            public void onPraiseClick(View view, int position){
            }
            @Override
            public void onDynamicPictureClick(View view, int position, String[] pictureUrlArray, int index){
            }
            
            @Override
            public void onOperationClick(View view, int position){}
        });
        
        recyclerView.setAdapter(meetRecommendListAdapter);

        getMyCondition();
        registerLoginBroadcast();

        addMeetInfo =  view.findViewById(R.id.meet_info_add);
        addMeetInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if(avatarSet == false){
                    intent = new Intent(getContext(), SetAvatarActivity.class);
                    intent.putExtra("look_friend", true);
                }else {
                    intent = new Intent(getContext(), FillMeetInfoActivity.class);
                }
                if(userProfile != null){
                    intent.putExtra("userProfile", userProfile);
                }
                startActivity(intent);
            }
        });
        
        //show progressImage before loading done
        progressImageView = view.findViewById(R.id.animal_progress);
        animationDrawable = (AnimationDrawable)progressImageView.getDrawable();
        progressImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        },50);
    }

    @Override
    protected void loadData() {
        //TODO  first load data, ths "last" not used ?  need sure
        requestData(true);
    }
    
    private void getMyCondition(){
        RequestBody requestBody = new FormBody.Builder().build();
        HttpUtil.sendOkHttpRequest(getContext(), GET_MY_CONDITION_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if(isDebug) Slog.d(TAG, "==========get my condition response text : " + responseText);
                    if (responseText != null) {
                        if (!TextUtils.isEmpty(responseText)) {
                            try {
                                JSONObject jsonObject = new JSONObject(responseText);
                                if (jsonObject != null){
                                    int isConditionSet = jsonObject.optInt("condition_set");
                                    JSONObject conditionObject = jsonObject.optJSONObject("my_condition");
                                    if (conditionObject != null){
                                        if(isDebug) Slog.d(TAG, "===========isConditionSet: "+isConditionSet+ "   conditionObject: "+conditionObject);
                                        if (isConditionSet > 0){
                                            myCondition = ParseUtils.setMeetMemberInfo(conditionObject);
                                            handler.sendEmptyMessage(MY_CONDITION_SET_DONE);
                                        }else {
                                           userProfile = ParseUtils.getUserProfileFromJSONObject(conditionObject);
                                            handler.sendEmptyMessage(MY_CONDITION_NOT_SET);
                                        }
                                    }

                                }else {
                                    handler.sendEmptyMessage(MY_CONDITION_NOT_SET);
                                }

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

    public void createCommentDetails(UserMeetInfo meetRecommend, int type) {
        Intent intent = new Intent(MyApplication.getContext(), DynamicsInteractDetailsActivity.class);
        intent.putExtra("type", type);
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
                    
                    case MY_COMMENT_UPDATE_RESULT:
                    int myCommentCount = data.getIntExtra("commentCount", 0);
                    if (isDebug) Slog.d(TAG, "==========commentCount: "+myCommentCount);
                    Message message = new Message();
                    Bundle myBundle = new Bundle();
                    myBundle.putInt("commentCount", myCommentCount);
                    message.setData(myBundle);
                    message.what = MY_COMMENT_COUNT_UPDATE;
                    handler.sendMessage(message);
                    break;
                case PRAISE_UPDATE_RESULT:
                    handler.sendEmptyMessage(PRAISE_UPDATE);
                    break;
                    
                    case MY_PRAISE_UPDATE_RESULT:
                    handler.sendEmptyMessage(MY_CONDITION_PRAISE_UPDATE);
                    break;
                case LOVE_UPDATE_RESULT:
                    handler.sendEmptyMessage(LOVE_UPDATE);
                    break;
                case MY_LOVE_UPDATE_RESULT:
                    handler.sendEmptyMessage(MY_CONDITION_LOVE_UPDATE);
                    break;
                default:
                    break;
            }
        }
    }
    
    private void setMeetHeaderView(){
        mMyMeetView = LayoutInflater.from(getContext()).inflate(R.layout.my_meet_item, (ViewGroup) mView.findViewById(android.R.id.content), false);
        recyclerView.addHeaderView(mMyMeetView);

        mMyMeetView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyApplication.getContext(), MeetArchiveActivity.class);
                intent.putExtra("meet", myCondition);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });
        
        TextView selfcondition = mMyMeetView.findViewById(R.id.self_condition);
        selfcondition.setText(myCondition.getSelfCondition(myCondition.getSituation()));
        RoundImageView avatar = mMyMeetView.findViewById(R.id.avatar);
        String avatarUrl = myCondition.getAvatar();
        if (avatarUrl != null && !"".equals(avatarUrl)) {
            Glide.with(getContext()).load(HttpUtil.DOMAIN + avatarUrl).into(avatar);
        } else {
            if(myCondition.getSex() == 0){
                avatar.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.male_default_avator));
            }else {
                avatar.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.female_default_avator));
            }
        }
        TextView visitIcon = mMyMeetView.findViewById(R.id.eye_icon);
        TextView visitRecord = mMyMeetView.findViewById(R.id.visit_record);
        
        if (myCondition.getVisitCount() > 0){
            visitRecord.setText(String.valueOf(myCondition.getVisitCount()));
            visitIcon.setVisibility(View.VISIBLE);
        }else {
            visitIcon.setVisibility(View.GONE);
        }

        TextView degreeView = mMyMeetView.findViewById(R.id.degree);
        String degree = myCondition.getDegreeName(myCondition.getDegree());
        if (!TextUtils.isEmpty(degree)){
            degreeView.setText(degree);
        }
        
        if (!TextUtils.isEmpty(myCondition.getUniversity())){
            TextView university = mMyMeetView.findViewById(R.id.university);
            university.setText(myCondition.getUniversity()+getResources().getString(R.string.dot));
        }

        TextView status = mMyMeetView.findViewById(R.id.status);
        if (myCondition.getSituation() == student){
            if (status.getVisibility() == View.GONE){
                status.setVisibility(View.VISIBLE);
            }
        }else {
            if (status.getVisibility() != View.GONE){
                status.setVisibility(View.GONE);
            }
            
            LinearLayout workInfo = mMyMeetView.findViewById(R.id.work_info);
            if (workInfo.getVisibility() == View.GONE){
                workInfo.setVisibility(View.VISIBLE);
            }
            TextView position = mMyMeetView.findViewById(R.id.position);
            String jobPosition = myCondition.getPosition();
            if (!TextUtils.isEmpty(jobPosition)){
                position.setText(jobPosition);
            }
            TextView industryView = mMyMeetView.findViewById(R.id.industry);
            String industry = myCondition.getIndustry();
            if (!TextUtils.isEmpty(industry)){
                industryView.setText(industry);
            }
        }

        
        lovedView = mMyMeetView.findViewById(R.id.loved_statistics);
        if(myCondition.getLovedCount() > 0){
            lovedView.setText(String.valueOf(myCondition.getLovedCount()));
        }
        lovedIcon = mMyMeetView.findViewById(R.id.loved_icon);
        if(myCondition.getLovedCount() > 0 ){
            if(myCondition.getLoved() == 1 ){
                lovedIcon.setText(R.string.fa_heart);
            }
        }else {
            lovedIcon.setText(R.string.fa_heart_o);
        }
        thumbsView = mMyMeetView.findViewById(R.id.thumbs_up_statistics);
        Slog.d(TAG, "--------------------->my condition getPraisedCount: "+myCondition.getPraisedCount());
        if(myCondition.getPraisedCount() > 0){
            thumbsView.setText(String.valueOf(myCondition.getPraisedCount()));
        }
        
        thumbsIcon = mMyMeetView.findViewById(R.id.thumbs_up_icon);
        if(myCondition.getPraisedCount() > 0 ){
            if(myCondition.getPraised() == 1 ){
                thumbsIcon.setText(R.string.fa_thumbs_up);
            }
        }else {
            thumbsIcon.setText(R.string.fa_thumbs_O_up);
        }

        commentCountView = mMyMeetView.findViewById(R.id.comment_count);
        int commentCount = myCondition.getCommentCount();
        if(commentCount > 0){
            commentCountView.setText(String.valueOf(commentCount));
        }

        TextView livingView = mMyMeetView.findViewById(R.id.living);
        livingView.setText(myCondition.getLiving());
        TextView homeTown = mMyMeetView.findViewById(R.id.hometown);
        homeTown.setText(myCondition.getHometown()+"人");
        
        TextView comment = mMyMeetView.findViewById(R.id.comment);
        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createCommentDetails(myCondition, MY_CONDITION_COMMENT);
            }
        });
        commentCountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createCommentDetails(myCondition, MY_CONDITION_COMMENT);
            }
        });
        
        lovedIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (1 == myCondition.getLoved()) {
                    Toast.makeText(getContext(), "You have loved it!", Toast.LENGTH_SHORT).show();
                    return;
                }
                love(myCondition);
            }
        });

        thumbsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO change UI to show parised or no
                if (1 == myCondition.getPraised()) {
                    Toast.makeText(getContext(), "You have praised it!", Toast.LENGTH_SHORT).show();
                    return;
                }
                praiseArchives(myCondition);
            }
        });
        
         Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mMyMeetView.findViewById(R.id.behavior_statistics), font);
        FontManager.markAsIconContainer(mMyMeetView.findViewById(R.id.name_info), font);
        FontManager.markAsIconContainer(mMyMeetView.findViewById(R.id.living_icon), font);
    }

    private void love(final UserMeetInfo userMeetInfo) {
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(userMeetInfo.getUid())).build();
        HttpUtil.sendOkHttpRequest(getContext(), MeetRecommendListAdapter.LOVED_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug) Log.d(TAG, "love responseText" + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject commentResponse = new JSONObject(responseText);
                        int status = commentResponse.optInt("status");
                        if (isDebug) Log.d(TAG, "love status" + status);
                        if (1 == status) {
                            //UserMeetInfo member = getMeetMemberById(userMeetInfo.getUid());
                           // userMeetInfo.setLoved(1);
                           // userMeetInfo.setLovedCount(userMeetInfo.getLovedCount() + 1);
                            sendMessage(MY_CONDITION_LOVE_UPDATE);

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }
    
    private void praiseArchives(final UserMeetInfo userMeetInfo) {
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(userMeetInfo.getUid())).build();
        HttpUtil.sendOkHttpRequest(getContext(), MeetRecommendListAdapter.PRAISED_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug) Log.d(TAG, "praiseArchives responseText:" + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject commentResponse = new JSONObject(responseText);
                        int status = commentResponse.optInt("status");
                        if (isDebug) Log.d(TAG, "praiseArchives status:" + status);
                        
                        if (1 == status) {
                            //UserMeetInfo member = getMeetMemberById(userMeetInfo.getUid());
                            //userMeetInfo.setPraised(1);
                            //userMeetInfo.setPraisedCount(userMeetInfo.getPraisedCount() + 1);
                            sendMessage(MY_CONDITION_PRAISE_UPDATE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }
    
    private class PictureAddBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case ADD_PICTURE_BROADCAST:
                    if (isDebug)  Slog.d(TAG, "==========ADD_PICTURE_BROADCAST");
                    getMyCondition();
                    meetList.clear();
                    //getRecommendContent();
                    //force update data
                    requestData(false);
                    break;
                case AVATAR_SET_ACTION_BROADCAST:
                    avatarSet = true;
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
        intentFilter.addAction(AVATAR_SET_ACTION_BROADCAST);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, intentFilter);
    }
    
    //unregister local broadcast
    private void unRegisterLoginBroadcast() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isDebug) Slog.d(TAG, "=============onResume");
        //getUserProfile();
        //updateData();
        meetRecommendListAdapter.setData(meetList);
        meetRecommendListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterLoginBroadcast();
        
        if (recyclerView != null){
            recyclerView.destroy();
            recyclerView = null;
        }
    }
    
    private void stopLoadProgress(){
        if (progressImageView.getVisibility() == View.VISIBLE){
            animationDrawable.stop();
            progressImageView.setVisibility(View.GONE);
        }
    }

    public void handleMessage(Message message) {
        switch (message.what){
            case NO_MORE_RECOMMEND:
                recyclerView.setNoMore(true);
                recyclerView.loadMoreComplete();
                stopLoadProgress();
                break;
                
                case GET_RECOMMEND_DONE:
                meetRecommendListAdapter.setData(meetList);
                meetRecommendListAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();
                if (mResponseSize < PAGE_SIZE) {
                    //loading finished
                    recyclerView.loadMoreComplete();
                    recyclerView.setNoMore(true);
                }
                stopLoadProgress();
                break;
            case NO_UPDATE_RECOMMEND:
                mResponseSize = 0;
                recyclerView.loadMoreComplete();
                recyclerView.refreshComplete();
                break;
                
                case GET_RECOMMEND_UPDATE:
                //save last update timemills
                SharedPreferencesUtils.setRecommendLast(getContext(), String.valueOf(System.currentTimeMillis() / 1000));
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
                        setMeetHeaderView();
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
                case MY_CONDITION_SET_DONE:
                if(addMeetInfo.getVisibility() == View.VISIBLE){
                    addMeetInfo.setVisibility(View.GONE);
                }
                setMeetHeaderView();
                break;
            case MY_CONDITION_NOT_SET:
                if(addMeetInfo.getVisibility() == View.GONE){
                    addMeetInfo.setVisibility(View.VISIBLE);
                }
                if (!TextUtils.isEmpty(userProfile.getAvatar())){
                    avatarSet = true;
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
            case MY_COMMENT_COUNT_UPDATE:
                Bundle myBundle = message.getData();
                int myCommentCount = myBundle.getInt("commentCount");
                myCondition.setCommentCount(myCommentCount);
                commentCountView.setText(String.valueOf(myCommentCount));
                break;
                case PRAISE_UPDATE:
                meetList.get(currentPosition).setPraisedCount(meetList.get(currentPosition).getPraisedCount()+1);
                meetList.get(currentPosition).setPraised(1);
                meetRecommendListAdapter.setData(meetList);
                meetRecommendListAdapter.notifyDataSetChanged();
                break;
            case LOVE_UPDATE:
                meetList.get(currentPosition).setLovedCount(meetList.get(currentPosition).getLovedCount()+1);
                meetList.get(currentPosition).setLoved(1);
                meetRecommendListAdapter.setData(meetList);
                meetRecommendListAdapter.notifyDataSetChanged();
                break;
                case MY_CONDITION_LOVE_UPDATE:
                myCondition.setLoved(1);
                myCondition.setLovedCount(myCondition.getLovedCount() + 1);
                lovedView.setText(String.valueOf(myCondition.getLovedCount()));
                lovedIcon.setText(R.string.fa_heart);
                break;
            case MY_CONDITION_PRAISE_UPDATE:
                myCondition.setPraised(1);
                myCondition.setPraisedCount(myCondition.getPraisedCount() + 1);
                thumbsView.setText(String.valueOf(myCondition.getPraisedCount()));
                thumbsIcon.setText(R.string.fa_thumbs_up);
                break;

            default:
                break;
        }
    }
    
    private void sendMessage(int what, Object obj) {
        Message msg = handler.obtainMessage();
        msg.what = what;
        msg.obj = obj;
        msg.sendToTarget();
    }

    private void sendMessage(int what) {
        sendMessage(what, null);
    }
    
    static class MyHandler extends HandlerTemp<MeetRecommendFragment> {

        public MyHandler(MeetRecommendFragment cls){
            super(cls);
        }

        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            MeetRecommendFragment meetRecommendFragment = ref.get();
            if (meetRecommendFragment != null) {
                meetRecommendFragment.handleMessage(message);
            }
        }
    }

    /******* added by chunping.xu for load data opt, 2019/12/13 ***********/
    private void requestData(final boolean isLoadMore) {
        RequestBody requestBody = null;
        int page = 0;
        if (isLoadMore) {
            page = meetList.size() / PAGE_SIZE;
            requestBody = new FormBody.Builder()
                    .add("step", String.valueOf(PAGE_SIZE))
                    .add("page", String.valueOf(page))
                    .build();
        } else {
            String last = SharedPreferencesUtils.getRecommendLast(getContext());
            if (isDebug) Slog.d(TAG, "=======last:" + last);
            requestBody = new FormBody.Builder()
                    .add("last", last)
                    .add("step", String.valueOf(PAGE_SIZE))
                    .add("page", String.valueOf(0))
                    .build();
        }
        if(isDebug) Log.d(TAG, "requestData page:" + page +" isLoadMore:"+ isLoadMore + " requestBody:" + requestBody.toString());
        HttpUtil.sendOkHttpRequest(getContext(), GET_RECOMMEND_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response == null || response.body() == null) {
                    return;
                }
                parseResponse(response.body().string(), isLoadMore);
            }
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure e:" + e);
            }
        });
    }
    private void parseResponse(String responseText, final boolean isLoadMore) {
        if(isDebug) Slog.d(TAG, "parseResponse responseText: " + responseText);
        if (null == responseText) {
            return;
        }
        //isLoadMore true keep last load data
        int loadSize = getResponseText(responseText, isLoadMore ? false : true);
        if (loadSize > 0){
            handler.sendEmptyMessage(isLoadMore ? GET_RECOMMEND_DONE : GET_RECOMMEND_UPDATE);
        }else {
            handler.sendEmptyMessage(isLoadMore ? NO_MORE_RECOMMEND : NO_UPDATE_RECOMMEND);
        }
    }

    private int  getResponseText(String responseText , boolean isUpdate) {
        List<UserMeetInfo> tempList = ParseUtils.getRecommendMeetList(responseText, isUpdate);
        mResponseSize = 0;
        if (null != tempList && tempList.size() != 0) {
            mResponseSize = tempList.size();
            if(isUpdate){
                meetList.clear();
            }
            meetList.addAll(tempList);
            return tempList.size();
        }
        return 0;
    }
}
