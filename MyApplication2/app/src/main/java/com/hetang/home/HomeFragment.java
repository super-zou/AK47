package com.hetang.home;

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

import com.bumptech.glide.Glide;
import com.hetang.util.BaseFragment;
import com.hetang.util.HttpUtil;
import com.hetang.util.InterActInterface;
import com.hetang.common.MyApplication;
import com.hetang.util.ParseUtils;
import com.hetang.util.PictureReviewDialogFragment;
import com.hetang.util.RoundImageView;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.hetang.R;
import com.hetang.adapter.DynamicsListAdapter;
import com.hetang.common.AddDynamicsActivity;
import com.hetang.common.Dynamic;
import com.hetang.common.HandlerTemp;
import com.hetang.meet.MeetDynamicsFragment;
import com.hetang.meet.MeetSingleGroupFragment;
import com.hetang.meet.SingleGroupDetailsActivity;
import com.hetang.meet.UserMeetInfo;
import com.hetang.util.CommonUserListDialogFragment;
import com.hetang.common.DynamicsInteractDetailsActivity;
import com.hetang.util.FontManager;
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
import static com.hetang.common.AddDynamicsActivity.DYNAMICS_ADD_BROADCAST;
import static com.hetang.common.DynamicsInteractDetailsActivity.DYNAMIC_COMMENT;
import static com.hetang.meet.MeetDynamicsFragment.COMMENT_COUNT_UPDATE;
import static com.hetang.meet.MeetDynamicsFragment.GET_DYNAMICS_WITH_ID_URL;
import static com.hetang.meet.MeetDynamicsFragment.HAVE_UPDATE;
import static com.hetang.meet.MeetDynamicsFragment.LOAD_DYNAMICS_DONE;
import static com.hetang.meet.MeetDynamicsFragment.NO_MORE_DYNAMICS;
import static com.hetang.meet.MeetDynamicsFragment.NO_UPDATE;
import static com.hetang.meet.MeetDynamicsFragment.UPDATE_COMMENT;
import static com.hetang.meet.MeetSingleGroupFragment.getSingleGroup;
import static com.hetang.common.DynamicsInteractDetailsActivity.COMMENT_ADD_BROADCAST;
import static com.hetang.meet.SingleGroupDetailsActivity.GET_SINGLE_GROUP_BY_GID;
import static com.hetang.util.ParseUtils.startMeetArchiveActivity;
import static com.hetang.util.SharedPreferencesUtils.getSessionUid;

public class HomeFragment extends BaseFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "HomeFragment";
    private static final int PAGE_SIZE = 6;
    private static final String GET_HOME_RECOMMEND_PERSON_URL = HttpUtil.DOMAIN + "?q=contacts/home_recommend_person";
    private static final String GET_RECOMMEND_SINGLE_GROUP_URL = HttpUtil.DOMAIN + "?q=single_group/get_all";
    public static final String LOAD_CONCERNED_DYNAMICS_URL = HttpUtil.DOMAIN + "?q=dynamic/load_concerned";
    public static final String GET_UPDATE_CONCERNED_DYNAMICS_URL = HttpUtil.DOMAIN + "?q=dynamic/get_update_concerned";
    public static final String GET_MY_NEW_ADD_DYNAMICS_URL = HttpUtil.DOMAIN + "?q=dynamic/get_my_new_add";
    
    private XRecyclerView xRecyclerView;
    View mHomeRecommendView;
    View mRecommendSingleGroupView;
    private int mTempSize;
    private DynamicsListAdapter dynamicsListAdapter;
    private Context mContext;
    private Handler handler;
    int mLoadSize = 0;
    Typeface font;
    private List<UserProfile> contactsList = new ArrayList<>();
    private List<MeetSingleGroupFragment.SingleGroup> mSingleGroupList = new ArrayList<>();
    
    private static final int GET_RECOMMEND_MEMBER_DONE = 6;
    private static final int GET_RECOMMEND_SINGLE_GROUP_DONE = 7;
    private static final int NO_RECOMMEND_MEMBER_DONE = 8;
    public static final int GET_MY_NEW_ADD_DONE = 11;

    RequestBody requestBody = null;
    private List<Dynamic> dynamicList = new ArrayList<>();
    private MeetDynamicsFragment meetDynamicsFragment;
    
    private DynamicsAddBroadcastReceiver mReceiver = new DynamicsAddBroadcastReceiver();
    private static final int DYNAMICS_PRAISED = 7;
    private int currentPos = 0;
    private View mView;
    JSONObject dynamics_response;
    JSONObject commentResponse;
    JSONArray dynamics;
    JSONArray praiseArray;
    public static final int COMMENT_UPDATE_RESULT = 1;
    public static final int DYNAMICS_UPDATE_RESULT = 2;
    public static final int PRAISE_UPDATE_RESULT = 3;
    public static final int LOVE_UPDATE_RESULT = 4;
    public static final int MY_COMMENT_UPDATE_RESULT = 5;
    public static final int MY_PRAISE_UPDATE_RESULT = 6;
    public static final int MY_LOVE_UPDATE_RESULT = 7;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    
    @Override
    protected int getLayoutId() {
        return R.layout.home_page;
    }

    @Override
    protected void initView(View view) {
        mView = view;
        handler = new MyHandler(this);
        mContext = MyApplication.getContext();
        dynamicsListAdapter = new DynamicsListAdapter(getContext(), false);
        if (meetDynamicsFragment == null){
            meetDynamicsFragment = new MeetDynamicsFragment();
        }
        
        //concernedRecommendAdapter = new ConcernedRecommendAdapter(getContext());
        xRecyclerView = view.findViewById(R.id.home_page_recycler_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(linearLayoutManager);

        xRecyclerView.setRefreshProgressStyle(BallSpinFadeLoader);
        xRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallRotate);

        xRecyclerView.getDefaultRefreshHeaderView().setRefreshTimeVisible(false);

        xRecyclerView.getDefaultFootView().setLoadingHint(getString(R.string.loading_pull_up_tip));
        xRecyclerView.getDefaultFootView().setNoMoreHint(getString(R.string.loading_no_more));
        final int itemLimit = 5;
        
        // When the item number of the screen number is list.size-2,we call the onLoadMore
        xRecyclerView.setLimitNumberToCallLoadMore(4);
        xRecyclerView.setRefreshProgressStyle(ProgressStyle.BallBeat);
        xRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);

        xRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        
        xRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                updateData();
            }

            @Override
            public void onLoadMore() {
                loadData();
            }
        });
        
        dynamicsListAdapter.setOnCommentClickListener(new InterActInterface() {
            @Override
            public void onCommentClick(View view, int position) {
                //createCommentDetails(aid, count);
                currentPos = position;
                createCommentDetails(dynamicList.get(position));

            }
            @Override
            public void onPraiseClick(View view, int position){
            
            Bundle bundle = new Bundle();
                bundle.putInt("type", DYNAMICS_PRAISED);
                bundle.putLong("did", dynamicList.get(position).getDid());
                bundle.putString("title", getContext().getResources().getString(R.string.praised_dynamic));
                CommonUserListDialogFragment commonUserListDialogFragment = new CommonUserListDialogFragment();
                commonUserListDialogFragment.setArguments(bundle);
                commonUserListDialogFragment.show(getFragmentManager(), "CommonUserListDialogFragment");

            }
            @Override
            public void onDynamicPictureClick(View view, int position, String[] pictureUrlArray, int index){
            Bundle bundle = new Bundle();
                bundle.putInt("index", index);
                bundle.putStringArray("pictureUrlArray", pictureUrlArray);

                PictureReviewDialogFragment pictureReviewDialogFragment = new PictureReviewDialogFragment();
                pictureReviewDialogFragment.setArguments(bundle);
                pictureReviewDialogFragment.show(getFragmentManager(), "PictureReviewDialogFragment");
            }

        });

        xRecyclerView.setAdapter(dynamicsListAdapter);
        
        TextView dynamicCreate = view.findViewById(R.id.dynamic_create);
        dynamicCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, AddDynamicsActivity.class);
                intent.putExtra("type", ParseUtils.ADD_INNER_DYNAMIC_ACTION);
                startActivityForResult(intent, Activity.RESULT_FIRST_USER);
            }
        });

        progressImageView = view.findViewById(R.id.animal_progress);
        animationDrawable = (AnimationDrawable)progressImageView.getDrawable();
        progressImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        },50);
        
        //getRecommendInfo();

        registerLoginBroadcast();

        font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(view.findViewById(R.id.home_page), font);

    }
    
    private void getRecommendInfo(){
        RequestBody requestBody = new FormBody.Builder().build();
        HttpUtil.sendOkHttpRequest(getContext(), GET_HOME_RECOMMEND_PERSON_URL, requestBody, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug) Slog.d(TAG, "GET RECOMMEND PERSON response : " + responseText);
                if (!TextUtils.isEmpty(responseText)){
                    processResponseText(responseText);
                }
                if(contactsList.size() > 0){
                    handler.sendEmptyMessage(GET_RECOMMEND_MEMBER_DONE);
                }else {
                    handler.sendEmptyMessage(NO_RECOMMEND_MEMBER_DONE);
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }

    private void getRecommendSingleGroup(){
        int page = mSingleGroupList.size() / 16;
        requestBody = new FormBody.Builder()
                .add("step", String.valueOf(16))
                .add("page", String.valueOf(page))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_RECOMMEND_SINGLE_GROUP_URL, requestBody, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();

                if(isDebug) Slog.d(TAG, "==========get recommend single group response : " + responseText);
                if (responseText != null && !TextUtils.isEmpty(responseText)) {
                    JSONObject SingleGroupResponse = null;
                    try {
                        SingleGroupResponse = new JSONObject(responseText);
                        if(SingleGroupResponse != null){
                            mLoadSize = processSingleGroupResponse(SingleGroupResponse);
                        }

                    }catch (JSONException e){
                        e.printStackTrace();
                    }

                    if(mLoadSize > 0){
                        handler.sendEmptyMessage(GET_RECOMMEND_SINGLE_GROUP_DONE);
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) { }
        });
    }

@Override
    protected void loadData() {

        final int page = dynamicList.size() / PAGE_SIZE;
        RequestBody requestBody = new FormBody.Builder()
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();

        if (page == 0){//record current time , used to check updated
            long current = System.currentTimeMillis();
            SharedPreferencesUtils.setConcernedDynamicsLast(getContext(), String.valueOf(current));
        }
        
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), LOAD_CONCERNED_DYNAMICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    //Slog.d(TAG, "==========response : "+response.body());
                    Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null) {
                        List<Dynamic> tempList = getDynamicsResponse(responseText, false, handler);

                        mTempSize = 0;
                        if (null != tempList && tempList.size() > 0) {
                            // dynamicList.clear();
                            mTempSize = tempList.size();
                            dynamicList.addAll(tempList);
                            Log.d(TAG, "getResponseText list.size:" + tempList.size());
                            handler.sendEmptyMessage(LOAD_DYNAMICS_DONE);
                        }else {
                            if (page == 0){
                                xRecyclerView.getDefaultFootView().setNoMoreHint(getString(R.string.no_content));
                            }
                            handler.sendEmptyMessage(NO_MORE_DYNAMICS);
                        }

                    }
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) { }
        });
    }

    public List<Dynamic> getDynamicsResponse(String responseText, boolean isUpdate, Handler handler) {

        if (!TextUtils.isEmpty(responseText)) {
            try {
                dynamics_response = new JSONObject(responseText);
                dynamics = dynamics_response.optJSONArray("dynamic");
                if (dynamics != null && dynamics.length() > 0) {
                    return setDynamicInfo(dynamics, handler);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    public List<Dynamic> setDynamicInfo(JSONArray dynamicsArray, Handler handler) {
        List<Dynamic> tempList = new ArrayList<Dynamic>();
        int length = dynamicsArray.length();
        if (isDebug) Slog.d(TAG, "==========setDynamicInfo==========dynamics length: " + length);

        for (int i = 0; i < length; i++) {

            JSONObject dynamicJSONObject = dynamicsArray.optJSONObject(i);
            if (meetDynamicsFragment == null){
                meetDynamicsFragment = new MeetDynamicsFragment();
            }
            
            Dynamic dynamic = meetDynamicsFragment.setMeetDynamics(dynamicJSONObject);

            switch (dynamic.getType()){
                case ParseUtils.PRAISE_DYNAMIC_ACTION:
                    dynamic = getRelateContent(dynamic);
                    break;
                case ParseUtils.PRAISE_MEET_CONDITION_ACTION:
                case ParseUtils.PUBLISH_MEET_CONDITION_ACTION:
                case ParseUtils.APPROVE_IMPRESSION_ACTION:
                case ParseUtils.APPROVE_PERSONALITY_ACTION:
                case ParseUtils.ADD_PERSONALITY_ACTION:
                case ParseUtils.JOIN_CHEERING_GROUP_ACTION:
                case ParseUtils.ADD_HOBBY_ACTION:
                case ParseUtils.EVALUATE_ACTION:
                case ParseUtils.REFEREE_ACTION:
                
                dynamic = getRelateMeetContent(dynamic);
                    break;
                case ParseUtils.ADD_CHEERING_GROUP_MEMBER_ACTION:
                    dynamic = getRelatedUserProfile(dynamic);
                    break;
                case ParseUtils.CREATE_SINGLE_GROUP_ACTION:
                case ParseUtils.JOIN_SINGLE_GROUP_ACTION:
                case ParseUtils.INVITE_SINGLE_GROUP_MEMBER_ACTION:
                    dynamic = getRelateSingleGroupContent(dynamic);
                    break;
                    
                    case ParseUtils.ADD_EDUCATION_ACTION:
                case ParseUtils.ADD_WORK_ACTION:
                case ParseUtils.ADD_BLOG_ACTION:
                case ParseUtils.ADD_PAPER_ACTION:
                case ParseUtils.ADD_PRIZE_ACTION:
                case ParseUtils.ADD_VOLUNTEER_ACTION:
                    dynamic = getRelatedBackgroundContent(dynamic);
                    break;
                    default:
                        break;
            }
            
            meetDynamicsFragment.setDynamicsInteract(dynamic, handler);
            int authorUid = getSessionUid(MyApplication.getContext());
            Slog.d(TAG, "---------------------->dynamic.getUid(): "+dynamic.getUid());
            /*
            if (dynamic.getUid() == authorUid){//author self
                if (dynamic.getType() < PRAISE_DYNAMIC_ACTION){//only show meet or common dynamics to author self
                    tempList.add(dynamic);
                }
            }else {
                tempList.add(dynamic);
            }
            */
            tempList.add(dynamic);
        }

        return tempList;

    }
    
    private void updateData() {
        String last = SharedPreferencesUtils.getConcernedDynamicsLast(getContext());
        Slog.d(TAG, "=======last:" + last);

        requestBody = new FormBody.Builder().add("last", last)
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(0))
                .build();
                
                //record current time , used to check updated
        long current = System.currentTimeMillis();
        SharedPreferencesUtils.setConcernedDynamicsLast(getContext(), String.valueOf(current));
        HttpUtil.sendOkHttpRequest(getContext(), GET_UPDATE_CONCERNED_DYNAMICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    
                    Slog.d(TAG, "==========updateData response text : " + responseText);
                    if (responseText != null) {
                        //save last update timemills
                        List<Dynamic> tempList = getDynamicsResponse(responseText, true, handler);
                        if (null != tempList && tempList.size() > 0) {
                            mTempSize = 0;
                            //dynamicList.clear();
                            mTempSize = tempList.size();
                            dynamicList.addAll(0, tempList);
                            //dynamicList.addAll(tempList);
                            Log.d(TAG, "========updateData getResponseText list.size:" + tempList.size());
                            handler.sendEmptyMessage(HAVE_UPDATE);
                        }else {
                            handler.sendEmptyMessage(NO_UPDATE);
                        }

                    }
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {

            }
        });

        xRecyclerView.scrollToPosition(0);
    }

    private void getMyNewAddDynamics() {
        requestBody = new FormBody.Builder()
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(0))
                .build();
                
                HttpUtil.sendOkHttpRequest(getContext(), GET_MY_NEW_ADD_DYNAMICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    JSONObject dynamicJSONObject = null;
                    Slog.d(TAG, "==========updateData response text : " + responseText);
                    if (responseText != null) {
                    
                    //save last update timemills
                        if (meetDynamicsFragment == null){
                            meetDynamicsFragment = new MeetDynamicsFragment();
                        }
                        try {
                            dynamicJSONObject = new JSONObject(responseText).optJSONObject("dynamic");
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                        Dynamic dynamic = meetDynamicsFragment.setMeetDynamics(dynamicJSONObject);
                        if (null != dynamic) {
                            //dynamicList.clear();
                            dynamicList.add(0, dynamic);
                            handler.sendEmptyMessage(GET_MY_NEW_ADD_DONE);
                        }
                    }
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {

            }
        });

        xRecyclerView.scrollToPosition(0);
    }

    private void processResponseText(String responseText){
        try {
        JSONArray contactsArray = new JSONObject(responseText).optJSONArray("recommend_users");
            JSONObject contactsObject;
            for (int i=0; i<contactsArray.length(); i++){
                UserProfile contacts = new UserProfile();
                contactsObject = contactsArray.getJSONObject(i);
                contacts.setAvatar(contactsObject.optString("avatar"));
                contacts.setUid(contactsObject.optInt("uid"));
                contacts.setName(contactsObject.optString("name"));
                contacts.setSex(contactsObject.optInt("sex"));
                contacts.setSituation(contactsObject.optInt("situation"));
                contacts.setUniversity(contactsObject.optString("university"));
                
                if(contactsObject.optInt("situation") == 0){
                    contacts.setMajor(contactsObject.optString("major"));
                    contacts.setDegree(contactsObject.optString("degree"));
                }else {
                    contacts.setIndustry(contactsObject.optString("industry"));
                    contacts.setPosition(contactsObject.optString("position"));
                }
                contactsList.add(contacts);
            }

        }catch (JSONException e){
            e.printStackTrace();
        }
    }
    
    private int processSingleGroupResponse(JSONObject SingleGroupResponse){

        JSONArray SingleGroupArray = null;
        if(SingleGroupResponse != null){
            SingleGroupArray = SingleGroupResponse.optJSONArray("single_group");
        }

        if(SingleGroupArray != null){
            if(SingleGroupArray.length() > 0){
                int count = 0;
                if(SingleGroupArray.length() > 16){
                
                count = 16;
                }else {
                    count = SingleGroupArray.length();
                }
                for (int i=0; i<count; i++){
                    JSONObject group = SingleGroupArray.optJSONObject(i);
                    if (group == null){
                        return 0;
                    }
                    MeetSingleGroupFragment.SingleGroup singleGroup = getSingleGroup(group);
                    if(singleGroup.headUrlList != null && singleGroup.headUrlList.size() > 0){
                        mSingleGroupList.add(singleGroup);
                    }
                }
            }
        }

        return mSingleGroupList.size();
    }
    
    private void setRecommendContactsView(){
        mHomeRecommendView = LayoutInflater.from(mContext).inflate(R.layout.home_page_recommend, (ViewGroup) mView.findViewById(android.R.id.content), false);
        xRecyclerView.addHeaderView(mHomeRecommendView);
        LinearLayout contactsWrapper = mHomeRecommendView.findViewById(R.id.contacts_wrapper);
        int size = 0;
        if (contactsList.size() > 8){
            size = 8;
        }else {
            size = contactsList.size();
        }
        
        for (int i=0; i<size; i++){
            View view = LayoutInflater.from(mContext).inflate(R.layout.home_recommend_item, null);
            if (contactsWrapper == null){
                Slog.d(TAG, "---------------->contactsWrapper: "+contactsWrapper);
                return;
            }
            
            contactsWrapper.addView(view);
            if (size > 3 && i == size-1){
                LinearLayout findMore = view.findViewById(R.id.find_more);
                findMore.setVisibility(View.VISIBLE);
                findMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MyApplication.getContext(), MoreRecommendActivity.class);
                        startActivity(intent);
                    }
                });
            }
            
             RoundImageView avatarView = view.findViewById(R.id.head_uri);
            final UserProfile userProfile = contactsList.get(i);
            String avatar = userProfile.getAvatar();
            if (avatar != null && !"".equals(avatar)) {
                Glide.with(mContext).load(HttpUtil.DOMAIN + avatar).into(avatarView);
            } else {
                if(userProfile.getSex() == 0){
                    avatarView.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.male_default_avator));
                }else {
                    avatarView.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.female_default_avator));
                }
            }
            
            TextView name = view.findViewById(R.id.name);
            name.setText(userProfile.getName());

            LinearLayout education = view.findViewById(R.id.education);
            LinearLayout work = view.findViewById(R.id.work);
            if (userProfile.getSituation() == 0){
                TextView degree = view.findViewById(R.id.degree);
                TextView university = view.findViewById(R.id.university);

                degree.setText(userProfile.getDegreeName(userProfile.getDegree()));
                university.setText(userProfile.getUniversity());
                }else {
                if (education.getVisibility() == View.VISIBLE){
                    education.setVisibility(View.GONE);
                }

                if (work.getVisibility() == View.GONE){
                    work.setVisibility(View.VISIBLE);
                }
            }
            
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //ParseUtils.startMeetArchiveActivity(getContext(), userProfile.getUid());
                    if (userProfile.getCid() > 0){
                        ParseUtils.startMeetArchiveActivity(mContext, userProfile.getUid());
                    }else {
                        ParseUtils.startArchiveActivity(mContext, userProfile.getUid());
                    }
                }
            });
            
            }
    }

    private void setRecommendSingleGroupView(){
        mRecommendSingleGroupView = LayoutInflater.from(mContext).inflate(R.layout.recommend_single_group, (ViewGroup) mView.findViewById(android.R.id.content), false);
        xRecyclerView.addHeaderView(mRecommendSingleGroupView);
        LinearLayout singleGroupWrapper = mRecommendSingleGroupView.findViewById(R.id.single_group_wrapper);
        
        if (singleGroupWrapper == null){
            return;
        }
        int size = mSingleGroupList.size();
        Slog.d(TAG, "----------------------->mSingleGroupList size: "+size);
        for (int i=0; i<mSingleGroupList.size(); i++){
            View view = LayoutInflater.from(mContext).inflate(R.layout.recommend_single_group_item, null);
            singleGroupWrapper.addView(view);
            if (i > 2 && i == size - 1){
                LinearLayout findMore = view.findViewById(R.id.find_more);
                findMore.setVisibility(View.VISIBLE);
                
                findMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), MeetSingleGroupActivity.class);
                        startActivity(intent);
                    }
                });
            }
            final MeetSingleGroupFragment.SingleGroup singleGroup = mSingleGroupList.get(i);
            TextView name = view.findViewById(R.id.name);
            name.setText(singleGroup.leader.getName());
            
            RoundImageView avatar = view.findViewById(R.id.avatar);
            if (singleGroup.leader.getAvatar() != null && !"".equals(singleGroup.leader.getAvatar())) {
                if (mContext != null){
                    Glide.with(mContext).load(HttpUtil.DOMAIN + singleGroup.leader.getAvatar()).into(avatar);
                }

            } else {
                if(singleGroup.leader.getSex() == 0){
                    avatar.setImageDrawable(mContext.getDrawable(R.drawable.male_default_avator));
                }else {
                    avatar.setImageDrawable(mContext.getDrawable(R.drawable.female_default_avator));
                }
            }
            
            LinearLayout memberAvatarList = view.findViewById(R.id.member_avatar_list);
            if (singleGroup.headUrlList != null && singleGroup.headUrlList.size() > 0 ) {
                setMemberAvatarView(singleGroup, memberAvatarList);
            }

            TextView memberRemainCount = view.findViewById(R.id.member_remain_count);

            if(singleGroup.memberCountRemain > 0){
                memberRemainCount.setVisibility(View.VISIBLE);
                memberRemainCount.setText("+"+String.valueOf(singleGroup.memberCountRemain));
            }else {
                memberRemainCount.setVisibility(View.GONE);
            }
            
            TextView groupName = view.findViewById(R.id.group_name);
            groupName.setText(singleGroup.groupName);

            TextView org = view.findViewById(R.id.org);
            org.setText(mContext.getResources().getString(R.string.from)+singleGroup.org);

            TextView groupProfile = view.findViewById(R.id.group_profile);
            groupProfile.setText(singleGroup.groupProfile);
            
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MyApplication.getContext(), SingleGroupDetailsActivity.class);
                    intent.putExtra("gid", singleGroup.gid);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(intent);
                }
            });
        }
    }
    
    private void setMemberAvatarView(MeetSingleGroupFragment.SingleGroup singleGroup, LinearLayout memberAvatarList){
        for (int i=0; i<singleGroup.headUrlList.size(); i++){
            RoundImageView imageView = new RoundImageView(mContext);
            LinearLayout.LayoutParams layoutParams;
            layoutParams= new LinearLayout.LayoutParams(150, 150);
            layoutParams.rightMargin = 5;
            imageView.setLayoutParams(layoutParams);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            memberAvatarList.addView(imageView);
            Glide.with(mContext).load(HttpUtil.DOMAIN + singleGroup.headUrlList.get(i)).into(imageView);
        }
    }
    
    public Dynamic getRelateContent(final Dynamic dynamic){
        RequestBody requestBody = new FormBody.Builder().add("did", String.valueOf(dynamic.getRelatedId())).build();
        Response response = HttpUtil.sendOkHttpRequestSync(getContext(), GET_DYNAMICS_WITH_ID_URL, requestBody, null);
        if (response != null){
            try {
                String responseText = response.body().string();
                if (!TextUtils.isEmpty(responseText)) {
                    JSONObject relatedContentJSONObject = new JSONObject(responseText).optJSONObject("dynamic");
                    dynamic.relatedContent = new Dynamic();
                    dynamic.relatedContent = meetDynamicsFragment.setMeetDynamics(relatedContentJSONObject);
                    meetDynamicsFragment.setDynamicsInteract(dynamic.relatedContent);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        return dynamic;

    }
    
    public Dynamic getRelateMeetContent(final Dynamic dynamic){
        Slog.d(TAG, "------------------->getRelateMeetContent uid: "+dynamic.getRelatedId());
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(dynamic.getRelatedId())).build();
        Response response = HttpUtil.sendOkHttpRequestSync(getContext(), ParseUtils.GET_MEET_ARCHIVE_URL, requestBody, null);
        if (response != null){
            try {
                String responseText = response.body().string();
                if (!TextUtils.isEmpty(responseText)) {
                    JSONObject relatedContentJSONObject = new JSONObject(responseText).optJSONObject("archive");
                    dynamic.relatedMeetContent = new UserMeetInfo();
                    dynamic.relatedMeetContent = ParseUtils.setMeetMemberInfo(relatedContentJSONObject);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }catch (IOException e){
 e.printStackTrace();
            }
        }

        return dynamic;
    }

    public Dynamic getRelatedUserProfile(Dynamic dynamic){
        dynamic.relatedUerProfile = new UserProfile();
        dynamic.relatedUerProfile = ParseUtils.getUserProfile(dynamic.getRelatedId());

        return dynamic;
    }
    
    public Dynamic getRelateSingleGroupContent(Dynamic dynamic){

        RequestBody requestBody = new FormBody.Builder().add("gid", String.valueOf(dynamic.getRelatedId())).build();
        Response response = HttpUtil.sendOkHttpRequestSync(getContext(), GET_SINGLE_GROUP_BY_GID, requestBody, null);
        if (response != null){
            try {
                String responseText = response.body().string();
                
                if (!TextUtils.isEmpty(responseText)) {
                    if (dynamic.getType() == ParseUtils.INVITE_SINGLE_GROUP_MEMBER_ACTION){
                        dynamic.relatedUerProfile = new UserProfile();
                        dynamic.relatedUerProfile = ParseUtils.getUserProfile(Integer.parseInt(dynamic.getContent()));
                    }

                    JSONObject singleGroupResponse = new JSONObject(responseText);
                    JSONObject group = singleGroupResponse.optJSONObject("single_group");
                    if (group != null){
                        dynamic.relatedSingleGroupContent = new MeetSingleGroupFragment.SingleGroup();
                        dynamic.relatedSingleGroupContent = getSingleGroup(group);
                    }
                }
                
                } catch (JSONException e) {
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
            return dynamic;
        }
        return null;
    }
    
    public Dynamic getRelatedBackgroundContent(Dynamic dynamic){
        RequestBody requestBody = null;
        FormBody.Builder builder = new FormBody.Builder();
        String url = "";
        switch (dynamic.getType()){
            case ParseUtils.ADD_EDUCATION_ACTION:
                requestBody = builder.add("ebid", String.valueOf(dynamic.getRelatedId())).build();
                url = ParseUtils.GET_SPECIFIC_EDUCATION_BACKGROUND_URL;
                break;
                case ParseUtils.ADD_WORK_ACTION:
                requestBody = builder.add("weid", String.valueOf(dynamic.getRelatedId())).build();
                url = ParseUtils.GET_SPECIFIC_WORK_EXPERIENCE_URL;
                break;
            case ParseUtils.ADD_BLOG_ACTION:
                requestBody = builder.add("bid", String.valueOf(dynamic.getRelatedId())).build();
                url = ParseUtils.GET_SPECIFIC_BLOG_URL;
                break;
            case ParseUtils.ADD_PAPER_ACTION:
                requestBody = builder.add("pid", String.valueOf(dynamic.getRelatedId())).build();
                url = ParseUtils.GET_SPECIFIC_PAPER_URL;
                break;
                
                case ParseUtils.ADD_PRIZE_ACTION:
                requestBody = builder.add("pid", String.valueOf(dynamic.getRelatedId())).build();
                url = ParseUtils.GET_SPECIFIC_PRIZE_URL;
                break;
            case ParseUtils.ADD_VOLUNTEER_ACTION:
                requestBody = builder.add("vid", String.valueOf(dynamic.getRelatedId())).build();
                url = ParseUtils.GET_SPECIFIC_VOLUNTEER_URL;
                break;
                default:
                    break;
        }
        
        Response response = HttpUtil.sendOkHttpRequestSync(getContext(), url, requestBody, null);
        if (response != null){
            try {
                String responseText = response.body().string();
                Slog.d(TAG, "------------------------>background response: "+responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    JSONObject backgroundResponse = new JSONObject(responseText);
                    //JSONObject group = backgroundResponse.optJSONObject("single_group");
                    dynamic.backgroundDetail = new BackgroundDetail();
                    dynamic.backgroundDetail = getBackgroundDetail(backgroundResponse, dynamic.getType());
                }
                
                } catch (JSONException e) {
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
            return dynamic;
        }
        return null;
    }
    
     public static class BackgroundDetail{
        public String title = "";
        public String link = "";
        public String secondaryTitle = "";
        public String lastTitle = "";
        public String startTime = "";
        public String endTime = "";
        public String description = "";
        public int now = 0;
    }
    
    public BackgroundDetail getBackgroundDetail(JSONObject jsonObject, int type){
        BackgroundDetail backgroundDetail = new BackgroundDetail();
        switch (type){
            case ParseUtils.ADD_EDUCATION_ACTION:
                JSONObject education = jsonObject.optJSONObject("education");
                if (education != null){
                    backgroundDetail.title = education.optString("university");
                    backgroundDetail.secondaryTitle = education.optString("degree");
                    backgroundDetail.lastTitle = education.optString("major");
                    backgroundDetail.startTime = String.valueOf(education.optInt("entrance_year"));
                    backgroundDetail.endTime = String.valueOf(education.optInt("graduate_year"));
                }
                break;
                
                case ParseUtils.ADD_WORK_ACTION:
                JSONObject work = jsonObject.optJSONObject("work");
                if (work != null){
                    backgroundDetail.title = work.optString("position");
                    backgroundDetail.secondaryTitle = work.optString("company");
                    backgroundDetail.lastTitle = work.optString("industry");
                    backgroundDetail.startTime = String.valueOf(work.optInt("entrance_year"));
                    backgroundDetail.endTime = String.valueOf(work.optInt("leave_year"));
                    backgroundDetail.now = work.optInt("now");
                }
                break;
            case ParseUtils.ADD_BLOG_ACTION:
                JSONObject blog = jsonObject.optJSONObject("blog");
                if (blog != null){
                    backgroundDetail.title = blog.optString("title");
                    backgroundDetail.link = blog.optString("blog_website");
                    backgroundDetail.description = blog.optString("description");
                }
                break;
            case ParseUtils.ADD_PAPER_ACTION:
                JSONObject paper = jsonObject.optJSONObject("paper");
                if (paper != null){
                    backgroundDetail.title = paper.optString("title");
                    backgroundDetail.link = paper.optString("website");
                    backgroundDetail.startTime = paper.optString("time");
                    backgroundDetail.description = paper.optString("description");
                }
                break;
                case ParseUtils.ADD_PRIZE_ACTION:
                JSONObject prize = jsonObject.optJSONObject("prize");
                if (prize != null){
                    backgroundDetail.title = prize.optString("title");
                    backgroundDetail.secondaryTitle = prize.optString("institution");
                    backgroundDetail.startTime = prize.optString("time");
                    backgroundDetail.description = prize.optString("description");
                }
                break;
                
                case ParseUtils.ADD_VOLUNTEER_ACTION:
                JSONObject volunteer = jsonObject.optJSONObject("volunteer");
                if (volunteer != null){
                    backgroundDetail.title = volunteer.optString("institution");
                    backgroundDetail.secondaryTitle = volunteer.optString("role");
                    backgroundDetail.lastTitle = volunteer.optString("website");
                    backgroundDetail.startTime = volunteer.optString("start");
                    backgroundDetail.endTime = volunteer.optString("end");
                    backgroundDetail.description = volunteer.optString("description");
                }
                break;
            default:
                break;
                
                }
        return backgroundDetail;
    }

    public void createCommentDetails(Dynamic dynamic) {
        Intent intent = new Intent(MyApplication.getContext(), DynamicsInteractDetailsActivity.class);
        intent.putExtra("type", DYNAMIC_COMMENT);
        intent.putExtra("dynamic", dynamic);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivityForResult(intent, Activity.RESULT_FIRST_USER);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Slog.d(TAG, "===================onActivityResult requestCode: "+requestCode+" resultCode: "+resultCode);
        if (requestCode == Activity.RESULT_FIRST_USER){
            switch (resultCode){
                case COMMENT_UPDATE_RESULT:
                    int commentCount = data.getIntExtra("commentCount", 0);
                    Slog.d(TAG, "==========commentCount: "+commentCount);
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putInt("commentCount", commentCount);
                    msg.setData(bundle);
                    msg.what = COMMENT_COUNT_UPDATE;
                    handler.sendMessage(msg);
                    break;
                case DYNAMICS_UPDATE_RESULT:
                    getMyNewAddDynamics();
                    break;
                default:
                    break;
            }
        }
    }

    private class DynamicsAddBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case DYNAMICS_ADD_BROADCAST:
                    Slog.d(TAG, "==========DYNAMICS_ADD_BROADCAST");
                    updateData();
                    break;
                    case COMMENT_ADD_BROADCAST:
                    int commentCount = intent.getIntExtra("commentCount", 0);
                    Slog.d(TAG, "==========DYNAMICS_ADD_BROADCAST commentCount: "+commentCount);
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putInt("commentCount", commentCount);
                    msg.setData(bundle);
                    msg.what = COMMENT_COUNT_UPDATE;
                    handler.sendMessage(msg);
                    break;
            }

        }
    }
    
    public void handleMessage(Message message) {
        switch (message.what){
            case GET_RECOMMEND_MEMBER_DONE:
                setRecommendContactsView();
                getRecommendSingleGroup();
                break;
           case NO_RECOMMEND_MEMBER_DONE:
                getRecommendSingleGroup();
                break;
            case GET_RECOMMEND_SINGLE_GROUP_DONE:
                setRecommendSingleGroupView();
                break;
            case NO_MORE_DYNAMICS:
                xRecyclerView.setNoMore(true);
                xRecyclerView.loadMoreComplete();
                // recyclerView.refreshComplete();
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
                if(dynamicList.size() > 0){
                    dynamicsListAdapter.setData(dynamicList);
                    dynamicsListAdapter.notifyDataSetChanged();
                    xRecyclerView.refreshComplete();
                }

                if (mTempSize < PAGE_SIZE) {
                    //loading finished
                    xRecyclerView.setNoMore(true);
                    xRecyclerView.loadMoreComplete();
                    // recyclerView.refreshComplete();
                    xRecyclerView.setLoadingMoreEnabled(false);
                }
                break;
                
                case HAVE_UPDATE:
                //meetDynamicsListAdapter.setScrolling(false);
                dynamicsListAdapter.setData(dynamicList);
                dynamicsListAdapter.notifyItemRangeInserted(0, mTempSize);
                dynamicsListAdapter.notifyDataSetChanged();
                xRecyclerView.refreshComplete();
                mTempSize = 0;
                break;
            case NO_UPDATE:
                mTempSize = 0;
                xRecyclerView.refreshComplete();
                break;
                
                case GET_MY_NEW_ADD_DONE:
                dynamicsListAdapter.setData(dynamicList);
                dynamicsListAdapter.notifyItemRangeInserted(0, 1);
                dynamicsListAdapter.notifyDataSetChanged();
                xRecyclerView.refreshComplete();
                break;
            case UPDATE_COMMENT:
                dynamicsListAdapter.setData(dynamicList);
                dynamicsListAdapter.notifyDataSetChanged();
                xRecyclerView.refreshComplete();
                break;
                
                case COMMENT_COUNT_UPDATE:
                Bundle bundle = message.getData();
                int commentCount = bundle.getInt("commentCount");
                Slog.d(TAG, "------------------>COMMENT_COUNT_UPDATE: position: "+currentPos+ " commentCount: "+commentCount);
                dynamicList.get(currentPos).setCommentCount(commentCount);
                dynamicsListAdapter.setData(dynamicList);
                dynamicsListAdapter.notifyDataSetChanged();
                xRecyclerView.refreshComplete();
            default:
                break;
        }
    }
    
    private void registerLoginBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, intentFilter);
    }

    //unregister local broadcast
    private void unRegisterLoginBroadcast() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterLoginBroadcast();
        
        if(xRecyclerView != null){
            xRecyclerView.destroy();
            xRecyclerView = null;
        }
    }
    
    static class MyHandler extends HandlerTemp<HomeFragment> {

        public MyHandler(HomeFragment cls){
            super(cls);
        }

        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            HomeFragment homeFragment = ref.get();
            if (homeFragment != null) {
                homeFragment.handleMessage(message);
            }
        }
    }
}
