package com.mufu.main;

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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mufu.R;
import com.mufu.adapter.DynamicsListAdapter;
import com.mufu.dynamics.AddDynamicsActivity;
import com.mufu.dynamics.Dynamic;
import com.mufu.dynamics.DynamicOperationDialogFragment;
import com.mufu.dynamics.DynamicsInteractDetailsActivity;
import com.mufu.common.HandlerTemp;
import com.mufu.common.MyApplication;
import com.mufu.group.SubGroupActivity;
import com.mufu.explore.ShareFragment;
import com.mufu.util.BaseFragment;
import com.mufu.util.CommonUserListDialogFragment;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.InterActInterface;
import com.mufu.util.ParseUtils;
import com.mufu.util.SharedPreferencesUtils;
import com.mufu.util.Slog;
import com.mufu.util.UserProfile;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

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

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.mufu.dynamics.AddDynamicsActivity.DYNAMICS_ADD_BROADCAST;
import static com.mufu.dynamics.DynamicOperationDialogFragment.DYNAMIC_OPERATION_RESULT;
import static com.mufu.dynamics.DynamicsInteractDetailsActivity.COMMENT_ADD_BROADCAST;
import static com.mufu.dynamics.DynamicsInteractDetailsActivity.DYNAMIC_COMMENT;
import static com.mufu.group.SubGroupActivity.getSubGroup;
import static com.mufu.group.SubGroupDetailsActivity.GET_SUBGROUP_BY_GID;
import static com.mufu.explore.ShareFragment.DYNAMICS_DELETE;
import static com.mufu.explore.ShareFragment.COMMENT_COUNT_UPDATE;
import static com.mufu.explore.ShareFragment.GET_DYNAMICS_WITH_ID_URL;
import static com.mufu.explore.ShareFragment.HAVE_UPDATE;
import static com.mufu.explore.ShareFragment.LOAD_DYNAMICS_DONE;
import static com.mufu.explore.ShareFragment.REQUEST_CODE;
import static com.mufu.explore.ShareFragment.NO_MORE_DYNAMICS;
import static com.mufu.explore.ShareFragment.NO_UPDATE;
import static com.mufu.explore.ShareFragment.UPDATE_COMMENT;
import static com.mufu.util.ParseUtils.ADD_INNER_DYNAMIC_ACTION;
import static com.mufu.util.ParseUtils.CREATE_GROUP_ACTION;
import static com.mufu.util.ParseUtils.FOLLOW_GROUP_ACTION;
import static com.mufu.util.ParseUtils.JOIN_GROUP_ACTION;
import static com.mufu.util.ParseUtils.MODIFY_GROUP_ACTION;
import static com.mufu.util.ParseUtils.PRAISE_DYNAMIC_ACTION;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class DynamicFragment extends BaseFragment {
    public static final String LOAD_CONCERNED_DYNAMICS_URL = HttpUtil.DOMAIN + "?q=dynamic/load_concerned";
    public static final String LOAD_SPECIFIC_DYNAMICS_URL = HttpUtil.DOMAIN + "?q=dynamic/action/get";
    public static final String GET_UPDATE_CONCERNED_DYNAMICS_URL = HttpUtil.DOMAIN + "?q=dynamic/get_update_concerned";
    public static final String GET_MY_NEW_ADD_DYNAMICS_URL = HttpUtil.DOMAIN + "?q=dynamic/get_my_new_add";
    public static final int NO_RECOMMEND_MEMBER_DONE = 8;
    public static final int GET_MY_NEW_ADD_DONE = 15;
    public static final int COMMENT_UPDATE_RESULT = 1;
    public static final int DYNAMICS_UPDATE_RESULT = 2;
    public static final int PRAISE_UPDATE_RESULT = 3;
    public static final int LOVE_UPDATE_RESULT = 4;
    public static final int MY_COMMENT_UPDATE_RESULT = 5;
    public static final int MY_PRAISE_UPDATE_RESULT = 6;
    public static final int MY_LOVE_UPDATE_RESULT = 7;
    private static final boolean isDebug = true;
    private static final String TAG = "DynamicFragment";
    private static final int PAGE_SIZE = 6;
    private static final int GET_RECOMMEND_GROUP_DONE = 7;
    private static final int DYNAMICS_PRAISED = 7;
    int mLoadSize = 0;
    Typeface font;
    RequestBody requestBody = null;
    JSONObject dynamics_response;
    JSONObject commentResponse;
    JSONArray dynamics;
    JSONArray praiseArray;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    private XRecyclerView xRecyclerView;
    private int mTempSize;
    private DynamicsListAdapter dynamicsListAdapter;
    private Context mContext;
    private Handler handler;
    private List<Dynamic> dynamicList = new ArrayList<>();
    private ShareFragment shareFragment;
    private DynamicsAddBroadcastReceiver mReceiver = new DynamicsAddBroadcastReceiver();
    private int currentPos = 0;
    private View mView;
    private int uid = 0;
    private int authorUid = 0;
    private boolean specificUser = false;

    @Override
    protected int getLayoutId() {
        return R.layout.dynamic_page;
    }

    @Override
    protected void initView(View view) {
        mView = view;
        handler = new MyHandler(this);
        mContext = MyApplication.getContext();
        authorUid = SharedPreferencesUtils.getSessionUid(MyApplication.getContext());
        Bundle bundle = getArguments();
        if (bundle != null) {
            uid = bundle.getInt("uid");
            specificUser = bundle.getBoolean("specific", false);
        }

        dynamicsListAdapter = new DynamicsListAdapter(getContext(), specificUser);

        if (shareFragment == null) {
            shareFragment = new ShareFragment();
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
            public void onPraiseClick(View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putInt("type", DYNAMICS_PRAISED);
                bundle.putLong("did", dynamicList.get(position).getDid());
                bundle.putString("title", getContext().getResources().getString(R.string.praised_dynamic));
                CommonUserListDialogFragment commonUserListDialogFragment = new CommonUserListDialogFragment();
                commonUserListDialogFragment.setArguments(bundle);
                commonUserListDialogFragment.show(getFragmentManager(), "CommonUserListDialogFragment");
            }

            @Override
            public void onDynamicPictureClick(View view, int position, String[] pictureUrlArray, int index) {
                shareFragment.startPicturePreview(position, pictureUrlArray);
            }

            @Override
            public void onOperationClick(View view, int position) {
                                Bundle bundle = new Bundle();
                bundle.putLong("did", dynamicList.get(position).getDid());
                currentPos = position;
                DynamicOperationDialogFragment dynamicOperationDialogFragment = new DynamicOperationDialogFragment();
                dynamicOperationDialogFragment.setArguments(bundle);
                dynamicOperationDialogFragment.setTargetFragment(DynamicFragment.this, REQUEST_CODE);
                dynamicOperationDialogFragment.show(getFragmentManager(), "DynamicOperationDialogFragment");
            }

        });

        xRecyclerView.setAdapter(dynamicsListAdapter);

        if (!specificUser){
            TextView dynamicCreate = view.findViewById(R.id.dynamic_create);
            dynamicCreate.setVisibility(View.VISIBLE);
            dynamicCreate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, AddDynamicsActivity.class);
                    intent.putExtra("type", ADD_INNER_DYNAMIC_ACTION);
                    startActivityForResult(intent, Activity.RESULT_FIRST_USER);
                }
            });
        }

        progressImageView = view.findViewById(R.id.animal_progress);
        animationDrawable = (AnimationDrawable) progressImageView.getDrawable();
        progressImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        }, 50);

        //loadData();

        registerLoginBroadcast();

        font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(view.findViewById(R.id.home_page), font);

    }

    @Override
    protected void loadData() {

        final int page = dynamicList.size() / PAGE_SIZE;
        FormBody.Builder builder = new FormBody.Builder();
        String url = LOAD_CONCERNED_DYNAMICS_URL;
        if (specificUser) {
            builder.add("uid", String.valueOf(uid));
            url = LOAD_SPECIFIC_DYNAMICS_URL;
        }
        Slog.d(TAG, "--------------->url: "+url);
        RequestBody requestBody = builder
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();

        if (page == 0) {//record current time , used to check updated
            long current = System.currentTimeMillis();
            SharedPreferencesUtils.setConcernedDynamicsLast(getContext(), String.valueOf(current));
        }

        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), url, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null) {
                        List<Dynamic> tempList = getDynamicsResponse(responseText, false, handler);
                        mTempSize = 0;
                        if (null != tempList && tempList.size() > 0) {
                            mTempSize = tempList.size();
                            dynamicList.addAll(tempList);
                            Log.d(TAG, "getResponseText list.size:" + tempList.size());
                            handler.sendEmptyMessage(LOAD_DYNAMICS_DONE);
                        } else {
                            if (page == 0) {
                                xRecyclerView.getDefaultFootView().setNoMoreHint(getString(R.string.no_content));
                            }
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
            if (shareFragment == null) {
                shareFragment = new ShareFragment();
            }
            Dynamic dynamic;
            if (dynamicJSONObject != null) {
                dynamic = shareFragment.setMeetDynamics(dynamicJSONObject);
            } else {
                return null;
            }

            switch (dynamic.getType()) {
                case PRAISE_DYNAMIC_ACTION:
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
                case ParseUtils.ADD_CHEERING_GROUP_MEMBER_ACTION:
                    dynamic = getRelatedUserProfile(dynamic);
                    break;
                case CREATE_GROUP_ACTION:
                case JOIN_GROUP_ACTION:
                case FOLLOW_GROUP_ACTION:
                case MODIFY_GROUP_ACTION:
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

            shareFragment.setDynamicsInteract(dynamic, handler);
            Slog.d(TAG, "---------------------->dynamic.getUid(): " + dynamic.getUid());

/*
            if (dynamic.getUid() == authorUid){//author self
                switch (dynamic.getType()){
                    case ADD_MEET_DYNAMIC_ACTION:
                    case ADD_INNER_DYNAMIC_ACTION:
                    case ADD_SUBGROUP_ACTIVITY_ACTION:
                    case CREATE_GROUP_ACTION:
                    case JOIN_GROUP_ACTION:
                    case MODIFY_GROUP_ACTION:
                    case FOLLOW_GROUP_ACTION:
                    case ADD_CHEERING_GROUP_MEMBER_ACTION:
                        tempList.add(dynamic);
                        break;
                        default:
                            break;
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
                        } else {
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
                        if (shareFragment == null) {
                            shareFragment = new ShareFragment();
                        }
                        try {
                            dynamicJSONObject = new JSONObject(responseText).optJSONObject("dynamic");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (dynamicJSONObject != null) {
                            Dynamic dynamic = shareFragment.setMeetDynamics(dynamicJSONObject);
                            if (null != dynamic) {
                                //dynamicList.clear();
                                dynamicList.add(0, dynamic);
                                handler.sendEmptyMessage(GET_MY_NEW_ADD_DONE);
                            }
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

    public Dynamic getRelateContent(final Dynamic dynamic) {
        RequestBody requestBody = new FormBody.Builder().add("did", String.valueOf(dynamic.getRelatedId())).build();
        Response response = HttpUtil.sendOkHttpRequestSync(getContext(), GET_DYNAMICS_WITH_ID_URL, requestBody, null);
        if (response != null) {
            try {
                String responseText = response.body().string();
                if (!TextUtils.isEmpty(responseText)) {
                    JSONObject relatedContentJSONObject = new JSONObject(responseText).optJSONObject("dynamic");
                    if (relatedContentJSONObject != null) {
                        dynamic.relatedContent = new Dynamic();
                        dynamic.relatedContent = shareFragment.setMeetDynamics(relatedContentJSONObject);
                        shareFragment.setDynamicsInteract(dynamic.relatedContent);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return dynamic;

    }

    public Dynamic getRelateMeetContent(final Dynamic dynamic) {
        Slog.d(TAG, "------------------->getRelateMeetContent uid: " + dynamic.getRelatedId());
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(dynamic.getRelatedId())).build();
        Response response = HttpUtil.sendOkHttpRequestSync(getContext(), ParseUtils.GET_MEET_ARCHIVE_URL, requestBody, null);
        if (response != null) {
            try {
                String responseText = response.body().string();
                if (!TextUtils.isEmpty(responseText)) {
                    JSONObject relatedContentJSONObject = new JSONObject(responseText).optJSONObject("archive");
                    dynamic.relatedUerProfile = new UserProfile();
                    dynamic.relatedUerProfile = ParseUtils.setMeetMemberInfo(relatedContentJSONObject);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return dynamic;
    }

    public Dynamic getRelatedUserProfile(Dynamic dynamic) {
        dynamic.relatedUerProfile = new UserProfile();
        dynamic.relatedUerProfile = ParseUtils.getUserProfile(dynamic.getRelatedId());

        return dynamic;
    }

    public Dynamic getRelateSingleGroupContent(Dynamic dynamic) {

        RequestBody requestBody = new FormBody.Builder().add("gid", String.valueOf(dynamic.getRelatedId())).build();
        Response response = HttpUtil.sendOkHttpRequestSync(getContext(), GET_SUBGROUP_BY_GID, requestBody, null);
        if (response != null) {
            try {
                String responseText = response.body().string();

                if (!TextUtils.isEmpty(responseText)) {
                    /*
                    if (dynamic.getType() == ParseUtils.INVITE_SINGLE_GROUP_MEMBER_ACTION) {
                        dynamic.relatedUerProfile = new UserProfile();
                        dynamic.relatedUerProfile = ParseUtils.getUserProfile(Integer.parseInt(dynamic.getContent()));
                    }
                    */

                    JSONObject singleGroupResponse = new JSONObject(responseText);
                    JSONObject group = singleGroupResponse.optJSONObject("group");
                    if (group != null) {
                        dynamic.relatedSubGroupContent = new SubGroupActivity.SubGroup();
                        dynamic.relatedSubGroupContent = getSubGroup(group);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return dynamic;
        }
        return null;
    }

    public Dynamic getRelatedBackgroundContent(Dynamic dynamic) {
        RequestBody requestBody = null;
        FormBody.Builder builder = new FormBody.Builder();
        String url = "";
        switch (dynamic.getType()) {
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
        if (response != null) {
            try {
                String responseText = response.body().string();
                Slog.d(TAG, "------------------------>background response: " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    JSONObject backgroundResponse = new JSONObject(responseText);
                    //JSONObject group = backgroundResponse.optJSONObject("single_group");
                    dynamic.backgroundDetail = new BackgroundDetail();
                    dynamic.backgroundDetail = getBackgroundDetail(backgroundResponse, dynamic.getType());
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return dynamic;
        }
        return null;
    }

    public BackgroundDetail getBackgroundDetail(JSONObject jsonObject, int type) {
        BackgroundDetail backgroundDetail = new BackgroundDetail();
        switch (type) {
            case ParseUtils.ADD_EDUCATION_ACTION:
                JSONObject education = jsonObject.optJSONObject("education");
                if (education != null) {
                    backgroundDetail.title = education.optString("university");
                    backgroundDetail.secondaryTitle = education.optString("degree");
                    backgroundDetail.lastTitle = education.optString("major");
                    backgroundDetail.startTime = String.valueOf(education.optInt("entrance_year"));
                    backgroundDetail.endTime = String.valueOf(education.optInt("graduate_year"));
                }
                break;

            case ParseUtils.ADD_WORK_ACTION:
                JSONObject work = jsonObject.optJSONObject("work");
                if (work != null) {
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
                if (blog != null) {
                    backgroundDetail.title = blog.optString("title");
                    backgroundDetail.link = blog.optString("blog_website");
                    backgroundDetail.description = blog.optString("description");
                }
                break;
            case ParseUtils.ADD_PAPER_ACTION:
                JSONObject paper = jsonObject.optJSONObject("paper");
                if (paper != null) {
                    backgroundDetail.title = paper.optString("title");
                    backgroundDetail.link = paper.optString("website");
                    backgroundDetail.startTime = paper.optString("time");
                    backgroundDetail.description = paper.optString("description");
                }
                break;
            case ParseUtils.ADD_PRIZE_ACTION:
                JSONObject prize = jsonObject.optJSONObject("prize");
                if (prize != null) {
                    backgroundDetail.title = prize.optString("title");
                    backgroundDetail.secondaryTitle = prize.optString("institution");
                    backgroundDetail.startTime = prize.optString("time");
                    backgroundDetail.description = prize.optString("description");
                }
                break;

            case ParseUtils.ADD_VOLUNTEER_ACTION:
                JSONObject volunteer = jsonObject.optJSONObject("volunteer");
                if (volunteer != null) {
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
        super.onActivityResult(requestCode, resultCode, data);
        Slog.d(TAG, "===================onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode);
        if (requestCode == Activity.RESULT_FIRST_USER) {
            switch (resultCode) {
                case COMMENT_UPDATE_RESULT:
                    int commentCount = data.getIntExtra("commentCount", 0);
                    Slog.d(TAG, "==========commentCount: " + commentCount);
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
                                    case DYNAMIC_OPERATION_RESULT:
                    handler.sendEmptyMessage(DYNAMICS_DELETE);
                    break;
                default:
                    break;
            }
        }
    }

    public void handleMessage(Message message) {
        switch (message.what) {
            case NO_MORE_DYNAMICS:
                xRecyclerView.setNoMore(true);
                xRecyclerView.loadMoreComplete();
                // recyclerView.refreshComplete();
                if (progressImageView.getVisibility() == View.VISIBLE) {
                    animationDrawable.stop();
                    progressImageView.setVisibility(View.GONE);
                }
                break;

            case LOAD_DYNAMICS_DONE:
                if (progressImageView.getVisibility() == View.VISIBLE) {
                    animationDrawable.stop();
                    progressImageView.setVisibility(View.GONE);
                }
                if (dynamicList.size() > 0) {
                    dynamicsListAdapter.setData(dynamicList);
                    dynamicsListAdapter.notifyDataSetChanged();
                    xRecyclerView.loadMoreComplete();
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
                if (xRecyclerView != null){
                    xRecyclerView.refreshComplete();
                }
                mTempSize = 0;
                break;
            case NO_UPDATE:
                mTempSize = 0;
                                if (xRecyclerView != null){
                    xRecyclerView.refreshComplete();
                }
                break;

            case GET_MY_NEW_ADD_DONE:
                dynamicsListAdapter.setData(dynamicList);
                dynamicsListAdapter.notifyItemRangeInserted(0, 1);
                dynamicsListAdapter.notifyDataSetChanged();
                                if (xRecyclerView != null){
                    xRecyclerView.refreshComplete();
                }
                break;
            case UPDATE_COMMENT:
                dynamicsListAdapter.setData(dynamicList);
                dynamicsListAdapter.notifyDataSetChanged();
                                if (xRecyclerView != null){
                    xRecyclerView.refreshComplete();
                }
                break;

            case COMMENT_COUNT_UPDATE:
                Bundle bundle = message.getData();
                int commentCount = bundle.getInt("commentCount");
                Slog.d(TAG, "------------------>COMMENT_COUNT_UPDATE: position: " + currentPos + " commentCount: " + commentCount);
                dynamicList.get(currentPos).setCommentCount(commentCount);
                dynamicsListAdapter.setData(dynamicList);
                dynamicsListAdapter.notifyDataSetChanged();
                                if (xRecyclerView != null){
                    xRecyclerView.refreshComplete();
                }
                                break;
            case DYNAMICS_DELETE:
                dynamicList.remove(currentPos);
                dynamicsListAdapter.setData(dynamicList);
                dynamicsListAdapter.notifyItemRemoved(currentPos);
                dynamicsListAdapter.notifyDataSetChanged();
                if (dynamicList.size() < PAGE_SIZE - 1){
                    xRecyclerView.loadMoreComplete();
                }
                break;
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

        if (xRecyclerView != null) {
            xRecyclerView.destroy();
            xRecyclerView = null;
        }
    }

    public static class BackgroundDetail {
        public String title = "";
        public String link = "";
        public String secondaryTitle = "";
        public String lastTitle = "";
        public String startTime = "";
        public String endTime = "";
        public String description = "";
        public int now = 0;
    }

    static class MyHandler extends HandlerTemp<DynamicFragment> {

        public MyHandler(DynamicFragment cls) {
            super(cls);
        }

        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            DynamicFragment dynamicFragment = ref.get();
            if (dynamicFragment != null) {
                dynamicFragment.handleMessage(message);
            }
        }
    }

    private class DynamicsAddBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case DYNAMICS_ADD_BROADCAST:
                    Slog.d(TAG, "==========DYNAMICS_ADD_BROADCAST");
                    updateData();
                    break;
                case COMMENT_ADD_BROADCAST:
                    int commentCount = intent.getIntExtra("commentCount", 0);
                    Slog.d(TAG, "==========DYNAMICS_ADD_BROADCAST commentCount: " + commentCount);
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
}
