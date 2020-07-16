package com.mufu.meet;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mufu.R;
import com.mufu.adapter.MeetRecommendListAdapter;
import com.mufu.common.HandlerTemp;
import com.mufu.common.MyApplication;
import com.mufu.common.SetAvatarActivity;
import com.mufu.contacts.ContactsApplyListActivity;
import com.mufu.dynamics.DynamicsInteractDetailsActivity;
import com.mufu.group.SubGroupActivity;
import com.mufu.main.MeetArchiveActivity;
import com.mufu.util.BaseFragment;

import com.mufu.util.HttpUtil;
import com.mufu.util.InterActInterface;
import com.mufu.util.ParseUtils;
import com.mufu.util.SharedPreferencesUtils;
import com.mufu.util.Slog;
import com.mufu.util.UserProfile;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

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
import static com.mufu.common.AddPictureActivity.ADD_PICTURE_BROADCAST;
import static com.mufu.common.SetAvatarActivity.AVATAR_SET_ACTION_BROADCAST;
import static com.mufu.dynamics.DynamicsInteractDetailsActivity.MEET_RECOMMEND_COMMENT;
import static com.mufu.explore.ShareFragment.COMMENT_COUNT_UPDATE;
import static com.mufu.explore.ShareFragment.LOVE_UPDATE;
import static com.mufu.explore.ShareFragment.MY_COMMENT_COUNT_UPDATE;
import static com.mufu.explore.ShareFragment.PRAISE_UPDATE;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class MeetFragment extends BaseFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "MeetRecommendFragment";
    private static final int PAGE_SIZE = 8;//page size
    private static final int GET_RECOMMEND_DONE = 1;
    private static final int GET_RECOMMEND_UPDATE = 2;
    public static final int MY_CONDITION_SET_DONE = 30;
    public static final int MY_CONDITION_NOT_SET = 31;
    public static final int MY_UNIVERSITY_GROUP_GET_DONE = 32;
    public static final int GET_RECOMMEND_MEMBER_DONE = 33;
    public static final int HAD_NO_RECOMMEND_MEMBER = 34;
    public static final int DEFAULT_RECOMMEND_COUNT = 8;
    private static final int NO_MORE_RECOMMEND = 9;
    private static final int NO_UPDATE_RECOMMEND = 10;
    private static final int MY_CONDITION_LOVE_UPDATE = 11;
    private static final int MY_CONDITION_PRAISE_UPDATE = 12;
    
    private static final String GET_RECOMMEND_URL = HttpUtil.DOMAIN + "?q=meet/get_recommend";
    public static final String GET_MY_CONDITION_URL = HttpUtil.DOMAIN + "?q=meet/get_my_condition";
    public static final String GET_RECOMMEND_PERSON_URL = HttpUtil.DOMAIN + "?q=contacts/recommend_person";
    private List<UserMeetInfo> meetList = new ArrayList<>();
    private UserProfile userProfile;
    private XRecyclerView recyclerView;
    private int mResponseSize;
    private View lookFriend;
    private int currentPage = 0;
    private int currentPosition = -1;
    private static final int any = -1;
    private Handler handler = new MyHandler(this);
    
    private boolean avatarSet = false;
    private RelativeLayout addMeetInfo;
    private PictureAddBroadcastReceiver mReceiver;
    private MeetRecommendListAdapter meetRecommendListAdapter;
    private List<ContactsApplyListActivity.Contacts> contactsList = new ArrayList<>();
    private List<SubGroupActivity.SubGroup> subGroupList = new ArrayList<>();
    private List<SubGroupActivity.Talent> mTalentList = new ArrayList<>();
    
    View mView;
    View mRecommendGroupView;
    View mRecommendContactsView;
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
        return 0;
    }
    
    @Override
    protected void initView(View convertView) {
    }

    @Override
    protected void loadData() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewContent = inflater.inflate(R.layout.meet_recommend, container, false);
        initConentView(viewContent);
        mView = viewContent;
        return viewContent;
    }
    
    protected void initConentView(View view) {

        if (isDebug) Slog.d(TAG, "=================onCreateView===================");
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
                Slog.d(TAG, "----------------------->onLoadMore");
                requestData(true);
            }
        });
        
        meetRecommendListAdapter.setItemClickListener(new MeetRecommendListAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(MyApplication.getContext(), MeetArchiveActivity.class);
                intent.putExtra("meet", meetList.get(position));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        }, new InterActInterface() {
        @Override
            public void onCommentClick(View view, int position) {
             //createCommentDetails(meetList.get(position).getDid(), meetList.get(position).getCommentCount());
                currentPosition = position;
                createCommentDetails(meetList.get(position), MEET_RECOMMEND_COMMENT);
            }

            @Override
            public void onPraiseClick(View view, int position) {
            }

            @Override
            public void onDynamicPictureClick(View view, int position, String[] pictureUrlArray, int index) {
            }
            
             @Override
            public void onOperationClick(View view, int position) {
            }
        });
        
        recyclerView.setAdapter(meetRecommendListAdapter);

        getMyCondition();
        registerLoginBroadcast();

        //show progressImage before loading done
        progressImageView = view.findViewById(R.id.animal_progress);
        animationDrawable = (AnimationDrawable) progressImageView.getDrawable();
        progressImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        }, 50);
        
        requestData(true);
    }
    
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
        
        if (isDebug)
            Log.d(TAG, "requestData page:" + page + " isLoadMore:" + isLoadMore + " requestBody:" + requestBody.toString());
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
        //if (isDebug) Slog.d(TAG, "parseResponse responseText: " + responseText);
        if (null == responseText) {
            return;
        }
        //isLoadMore true keep last load data
        int loadSize = getResponseText(responseText, isLoadMore ? false : true);
        if (loadSize > 0) {
            handler.sendEmptyMessage(isLoadMore ? GET_RECOMMEND_DONE : GET_RECOMMEND_UPDATE);
        } else {
            handler.sendEmptyMessage(isLoadMore ? NO_MORE_RECOMMEND : NO_UPDATE_RECOMMEND);
        }
    }
    
    private int getResponseText(String responseText, boolean isUpdate) {
        List<UserMeetInfo> tempList = ParseUtils.getRecommendMeetList(responseText, isUpdate);
        mResponseSize = 0;
        if (null != tempList && tempList.size() != 0) {
            mResponseSize = tempList.size();
            if (isUpdate) {
                meetList.clear();
            }
            meetList.addAll(tempList);
            return tempList.size();
        }
        return 0;
    }
    
    private void getMyCondition() {
        RequestBody requestBody = new FormBody.Builder().build();
        HttpUtil.sendOkHttpRequest(getContext(), GET_MY_CONDITION_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    //if (isDebug)
                    //Slog.d(TAG, "==========get my condition response text : " + responseText);
                    if (responseText != null) {
                        if (!TextUtils.isEmpty(responseText)) {
                        try {
                                JSONObject jsonObject = new JSONObject(responseText);
                                if (jsonObject != null) {
                                    int isConditionSet = jsonObject.optInt("condition_set");
                                    JSONObject conditionObject = jsonObject.optJSONObject("my_condition");
                                    if (conditionObject != null) {
                                        if (isDebug)
                                            Slog.d(TAG, "===========isConditionSet: " + isConditionSet);
                                        if (isConditionSet > 0) {
                                            myCondition = ParseUtils.setMeetMemberInfo(conditionObject);
                                            handler.sendEmptyMessage(MY_CONDITION_SET_DONE);
                                        } else {
                                            userProfile = ParseUtils.getUserProfileFromJSONObject(conditionObject);
                                            handler.sendEmptyMessage(MY_CONDITION_NOT_SET);
                                        }
                                    }

                                } else {
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
    
    private void stopLoadProgress() {
        if (progressImageView.getVisibility() == View.VISIBLE) {
            animationDrawable.stop();
            progressImageView.setVisibility(View.GONE);
        }
    }
    
    private class PictureAddBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ADD_PICTURE_BROADCAST:
                    if (isDebug) Slog.d(TAG, "==========ADD_PICTURE_BROADCAST");
                    meetList.clear();
                    recyclerView.removeAllHeaderView();
                    recyclerView.reset();
                    getMyCondition();
                    requestData(true);
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
    
    private void setMeetHeaderView() {
        lookFriend = LayoutInflater.from(getContext()).inflate(R.layout.look_friend, (ViewGroup) mView.findViewById(android.R.id.content), false);
        recyclerView.addHeaderView(lookFriend);

        addMeetInfo = lookFriend.findViewById(R.id.meet_info_add);
        addMeetInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (avatarSet == false) {
                    intent = new Intent(getContext(), SetAvatarActivity.class);
                    intent.putExtra("look_friend", true);
                } else {
                    intent = new Intent(getContext(), FillMeetInfoActivity.class);
                }
                if (userProfile != null) {
                    intent.putExtra("userProfile", userProfile);
                }
                startActivity(intent);
            }
        });
    }
    
    public void handleMessage(Message message) {
        switch (message.what) {
            case NO_MORE_RECOMMEND:
                stopLoadProgress();
                recyclerView.setNoMore(true);
                recyclerView.loadMoreComplete();
                recyclerView.refreshComplete();
                break;

            case GET_RECOMMEND_DONE:
                meetRecommendListAdapter.setData(meetList);
                meetRecommendListAdapter.notifyDataSetChanged();
                recyclerView.loadMoreComplete();
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

            case MY_CONDITION_NOT_SET:
                setMeetHeaderView();
                if (!TextUtils.isEmpty(userProfile.getAvatar())) {
                    avatarSet = true;
                }
                break;
                case COMMENT_COUNT_UPDATE:
                Bundle bundle = message.getData();
                int commentCount = bundle.getInt("commentCount");
                if (isDebug)
                    Slog.d(TAG, "------------------>COMMENT_COUNT_UPDATE: position: " + currentPosition + " commentCount: " + commentCount);
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
                meetList.get(currentPosition).setPraisedCount(meetList.get(currentPosition).getPraisedCount() + 1);
                meetList.get(currentPosition).setPraised(1);
                meetRecommendListAdapter.setData(meetList);
                meetRecommendListAdapter.notifyDataSetChanged();
                break;
            case LOVE_UPDATE:
                meetList.get(currentPosition).setLovedCount(meetList.get(currentPosition).getLovedCount() + 1);
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
    
     static class MyHandler extends HandlerTemp<MeetFragment> {

        public MyHandler(MeetFragment cls) {
            super(cls);
        }

        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            MeetFragment meetRecommendActivity = ref.get();
            if (meetRecommendActivity != null) {
                meetRecommendActivity.handleMessage(message);
            }
        }
    }

}
