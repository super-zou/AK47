package com.hetang.explore;

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
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.adapter.MeetRecommendListAdapter;
import com.hetang.common.HandlerTemp;
import com.hetang.common.MyApplication;
import com.hetang.common.SetAvatarActivity;
import com.hetang.contacts.ContactsApplyListActivity;
import com.hetang.dynamics.DynamicsInteractDetailsActivity;
import com.hetang.group.GroupActivity;
import com.hetang.group.SubGroupActivity;

import com.hetang.group.SubGroupDetailsActivity;
import com.hetang.home.CommonContactsActivity;
import com.hetang.main.MeetArchiveActivity;
import com.hetang.meet.FillMeetInfoActivity;
import com.hetang.meet.UserMeetInfo;
import com.hetang.talent.TalentDetailsActivity;
import com.hetang.talent.TalentSummaryListActivity;
import com.hetang.util.BaseFragment;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.InterActInterface;
import com.hetang.util.ParseUtils;
import com.hetang.util.RoundImageView;
import com.hetang.util.SharedPreferencesUtils;
import com.hetang.util.Slog;
import com.hetang.util.UserProfile;
import com.hetang.util.Utility;
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

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.hetang.common.AddPictureActivity.ADD_PICTURE_BROADCAST;
import static com.hetang.common.SetAvatarActivity.AVATAR_SET_ACTION_BROADCAST;
import static com.hetang.dynamics.DynamicsInteractDetailsActivity.MEET_RECOMMEND_COMMENT;
import static com.hetang.group.SingleGroupActivity.GET_TALENT_DONE;
import static com.hetang.group.SubGroupActivity.GET_ALL_TALENTS;
import static com.hetang.group.SubGroupActivity.GET_MY_UNIVERSITY_SUBGROUP;
import static com.hetang.group.SubGroupActivity.getTalent;
import static com.hetang.main.DynamicFragment.COMMENT_UPDATE_RESULT;
import static com.hetang.main.DynamicFragment.LOVE_UPDATE_RESULT;
import static com.hetang.main.DynamicFragment.MY_COMMENT_UPDATE_RESULT;
import static com.hetang.main.DynamicFragment.MY_LOVE_UPDATE_RESULT;
import static com.hetang.main.DynamicFragment.MY_PRAISE_UPDATE_RESULT;
import static com.hetang.main.DynamicFragment.PRAISE_UPDATE_RESULT;
import static com.hetang.explore.ShareFragment.COMMENT_COUNT_UPDATE;
import static com.hetang.explore.ShareFragment.LOVE_UPDATE;
import static com.hetang.explore.ShareFragment.MY_COMMENT_COUNT_UPDATE;
import static com.hetang.explore.ShareFragment.PRAISE_UPDATE;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class ExploreFragment extends BaseFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "ExploreFragment";
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

    View mMyMeetView;
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
    }
    
    @Override
    protected void loadData() {
        Slog.d(TAG, "------------------------->loadData");
        //requestData(true);
    }

    private void getMyCondition() {
        RequestBody requestBody = new FormBody.Builder().build();
        HttpUtil.sendOkHttpRequest(getContext(), GET_MY_CONDITION_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
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

    private void getRecommendTalent() {
        RequestBody requestBody = new FormBody.Builder()
                .add("step", String.valueOf(8))
                .add("page", String.valueOf(0))
                .build();
                
                HttpUtil.sendOkHttpRequest(getContext(), GET_ALL_TALENTS, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========getRecommendTalent response text: "+responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                    JSONObject talentsResponse = null;
                        try {
                            talentsResponse = new JSONObject(responseText);
                            if (talentsResponse != null) {
                                int loadSize = processTalentsResponse(talentsResponse);
                                if (loadSize > 0) {
                                    handler.sendEmptyMessage(GET_TALENT_DONE);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                requestData(true);
            }
            
            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    public int processTalentsResponse(JSONObject talentsObject){
        int talentSize = 0;
        JSONArray talentArray = null;

        if (talentsObject != null) {
            talentArray = talentsObject.optJSONArray("talents");
        }
        
        if (talentArray != null) {
            talentSize = talentArray.length();
            if (talentSize > 0) {
                for (int i = 0; i < talentArray.length(); i++) {
                    JSONObject talentObject = talentArray.optJSONObject(i);
                    if (talentObject != null) {
                        SubGroupActivity.Talent talent = getTalent(talentObject);
                        mTalentList.add(talent);
                    }
                }
            }
        }

        return talentSize;
    }
    
    private void getMyUniversityGroup() {
        HttpUtil.sendOkHttpRequest(getContext(), GET_MY_UNIVERSITY_SUBGROUP, new FormBody.Builder().build(), new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug)
                        Slog.d(TAG, "==========getMyUniversityGroup response text : " + responseText);
                    if (responseText != null) {
                        if (!TextUtils.isEmpty(responseText)) {
                            try {
                                JSONObject jsonObject = new JSONObject(responseText);
                                if (jsonObject != null) {
                                 JSONArray groupArray = jsonObject.optJSONArray("groups");
                                    if (groupArray != null && groupArray.length() > 0) {
                                        for (int i = 0; i < groupArray.length(); i++) {
                                            JSONObject groupObject = groupArray.getJSONObject(i);
                                            SubGroupActivity.SubGroup subGroup = new SubGroupActivity.SubGroup();
                                            subGroup.gid = groupObject.optInt("gid");
                                            subGroup.groupName = groupObject.optString("group_name");
                                            subGroup.groupLogoUri = groupObject.optString("logo_uri");
                                            subGroup.visitRecord = groupObject.optInt("visit_record");
                                            subGroup.activityCount = groupObject.optInt("activity_count");
                                         subGroupList.add(subGroup);
                                        }
                                        handler.sendEmptyMessage(MY_UNIVERSITY_GROUP_GET_DONE);
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

    private void getRecommendContacts() {
        RequestBody requestBody = new FormBody.Builder()
                .add("step", String.valueOf(DEFAULT_RECOMMEND_COUNT))
                .add("page", String.valueOf(0)).build();
        HttpUtil.sendOkHttpRequest(getContext(), GET_RECOMMEND_PERSON_URL, requestBody, new Callback() {
        @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug) Slog.d(TAG, "getRecommendContacts response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    processResponseText(responseText);
                }
                if (contactsList.size() > 0) {
                    handler.sendEmptyMessage(GET_RECOMMEND_MEMBER_DONE);
                } else {
                    handler.sendEmptyMessage(HAD_NO_RECOMMEND_MEMBER);
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }

    private void processResponseText(String responseText) {
        try {
            JSONArray contactsArray = new JSONObject(responseText).optJSONArray("results");
            JSONObject contactsObject;
            if (contactsArray != null){
                for (int i = 0; i < contactsArray.length(); i++) {
                    ContactsApplyListActivity.Contacts contacts = new ContactsApplyListActivity.Contacts();
                    contactsObject = contactsArray.getJSONObject(i);
                    contacts.setAvatar(contactsObject.optString("avatar"));
                    contacts.setUid(contactsObject.optInt("uid"));
                    contacts.setNickName(contactsObject.optString("nickname"));
                    contacts.setSex(contactsObject.optInt("sex"));
                    contacts.setSituation(contactsObject.optInt("situation"));
                    contacts.setUniversity(contactsObject.optString("university"));
                    
                    if (contactsObject.optInt("situation") == 0) {
                        contacts.setMajor(contactsObject.optString("major"));
                        contacts.setDegree(contactsObject.optString("degree"));
                    } else {
                        contacts.setIndustry(contactsObject.optString("industry"));
                        contacts.setPosition(contactsObject.optString("position"));
                    }
                    contactsList.add(contacts);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
        if (isDebug)
            Slog.d(TAG, "===================onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode);
        if (requestCode == Activity.RESULT_FIRST_USER) {
            switch (resultCode) {
                case COMMENT_UPDATE_RESULT:
                    int commentCount = data.getIntExtra("commentCount", 0);
                    if (isDebug) Slog.d(TAG, "==========commentCount: " + commentCount);
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putInt("commentCount", commentCount);
                    msg.setData(bundle);
                    msg.what = COMMENT_COUNT_UPDATE;
                    handler.sendMessage(msg);
                    break;
                    case MY_COMMENT_UPDATE_RESULT:
                    int myCommentCount = data.getIntExtra("commentCount", 0);
                    if (isDebug) Slog.d(TAG, "==========commentCount: " + myCommentCount);
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
    
     private void setRecommendContactsView() {
        mRecommendContactsView = LayoutInflater.from(getContext()).inflate(R.layout.contacts_recommend, (ViewGroup) mView.findViewById(android.R.id.content), false);
        recyclerView.addHeaderView(mRecommendContactsView);
        LinearLayout contactsWrapper = mRecommendContactsView.findViewById(R.id.contacts_wrapper);
        int size = 0;
        if (contactsList.size() > 8) {
            size = 8;
        } else {
            size = contactsList.size();
        }
        
        for (int i = 0; i < size; i++) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.contacts_recommend_item, null);
            if (contactsWrapper == null) {
                return;
            }

            contactsWrapper.addView(view);
            if (size > 3 && i == size - 1) {
                LinearLayout findMore = view.findViewById(R.id.find_more);
                findMore.setVisibility(View.VISIBLE);
                findMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MyApplication.getContext(), CommonContactsActivity.class);
                        startActivity(intent);
                    }
                });
            }
            
            RoundImageView avatarView = view.findViewById(R.id.head_uri);
            final ContactsApplyListActivity.Contacts userProfile = contactsList.get(i);
            String avatar = userProfile.getAvatar();
            if (avatar != null && !"".equals(avatar)) {
                Glide.with(getContext()).load(HttpUtil.DOMAIN + avatar).into(avatarView);
            } else {
                if (userProfile.getSex() == 0) {
                    avatarView.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.male_default_avator));
                } else {
                    avatarView.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.female_default_avator));
                }
            }
            
            TextView name = view.findViewById(R.id.name);
            name.setText(userProfile.getNickName());

            LinearLayout education = view.findViewById(R.id.education);
            LinearLayout work = view.findViewById(R.id.work);
            if (userProfile.getSituation() == 0) {
                TextView degree = view.findViewById(R.id.degree);
                TextView major = view.findViewById(R.id.major);
                TextView university = view.findViewById(R.id.university);

                degree.setText(userProfile.getDegreeName(userProfile.getDegree()));
                major.setText(userProfile.getMajor());
                university.setText(userProfile.getUniversity());
                
                } else {
                if (education.getVisibility() == View.VISIBLE) {
                    education.setVisibility(View.GONE);
                }

                if (work.getVisibility() == View.GONE) {
                    work.setVisibility(View.VISIBLE);
                }
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ParseUtils.startMeetArchiveActivity(getContext(), userProfile.getUid());
                }
            });

        }
    }
    
    private void setRecommendTalentsHeader(){
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        int innerWidth = dm.widthPixels - (int) Utility.dpToPx(getContext(), 32f);
        int height = innerWidth;
        int wrapperWidth = innerWidth/2;
        int avatarWidth = wrapperWidth - (int) Utility.dpToPx(getContext(), 12f);

        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(wrapperWidth, WRAP_CONTENT);

        View talentView = LayoutInflater.from(getContext()).inflate(R.layout.recommend_talent, (ViewGroup) mView.findViewById(android.R.id.content), false);
        recyclerView.addHeaderView(talentView);
        TextView moreTalent = talentView.findViewById(R.id.more);
        moreTalent.setText(getResources().getString(R.string.more_talent));
        moreTalent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), TalentSummaryListActivity.class);
                //intent.putExtra("type", type);
                startActivity(intent);
            }
        });

        GridLayout talentWrapper = talentView.findViewById(R.id.talent_wrapper);
        if (talentWrapper == null){
            return;
        }
        
        int size = mTalentList.size();
        if (size > 6){
            size = 6;
            moreTalent.setVisibility(View.VISIBLE);
        }

        for (int i = 0; i < size; i++) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.recommend_talent_item, null);
            talentWrapper.addView(view, params);
            final SubGroupActivity.Talent talent = mTalentList.get(i);
            RoundImageView avatarRV = view.findViewById(R.id.avatar);
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(avatarWidth, avatarWidth);
            layoutParams.setMargins(0, 0, 2, 2);
            avatarRV.setLayoutParams(layoutParams);
            String avatar = talent.profile.getAvatar();
            if (avatar != null && !"".equals(avatar)) {
                Glide.with(getContext()).load(HttpUtil.DOMAIN + avatar).into(avatarRV);
            }
            TextView nickname = view.findViewById(R.id.name);
            nickname.setText(talent.profile.getNickName());
            
            TextView degree = view.findViewById(R.id.degree);
            TextView university = view.findViewById(R.id.university);

            if (talent.profile.getSituation() == student){
                degree.setText(talent.profile.getDegreeName(talent.profile.getDegree()));
                university.setText(talent.profile.getUniversity());
            }else {
                degree.setText(talent.profile.getPosition());
                university.setText(talent.profile.getIndustry());
            }
            
            TextView introduction = view.findViewById(R.id.introduction);
            introduction.setText(talent.introduction);

            TextView charge = view.findViewById(R.id.charge);
            charge.setText(talent.charge+"元");

            TextView subject = view.findViewById(R.id.subject);
            subject.setText(talent.subject);

            if (talent.evaluateCount > 0) {
                TextView evaluateCountTV = view.findViewById(R.id.evaluate_count);
                float scoreFloat = talent.evaluateScores / talent.evaluateCount;
                float score = (float) (Math.round(scoreFloat * 10)) / 10;
                evaluateCountTV.setText(getResources().getString(R.string.fa_star) +" "+ score + getResources().getString(R.string.dot) + talent.evaluateCount);
            }
            
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), TalentDetailsActivity.class);
                    //intent.putExtra("talent", talent);
                    intent.putExtra("tid", talent.tid);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(intent);
                }
            });

            Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.evaluate_count), font);
        }

    }
    
    private void setMyUniversityGroupRecommendView() {
        mRecommendGroupView = LayoutInflater.from(getContext()).inflate(R.layout.recommend_group, (ViewGroup) mView.findViewById(android.R.id.content), false);
        recyclerView.addHeaderView(mRecommendGroupView);
        LinearLayout groupWrapper = mRecommendGroupView.findViewById(R.id.group_wrapper);
        if (groupWrapper == null) {
            return;
        }
        TextView more = mRecommendGroupView.findViewById(R.id.more);
        more.setText(getContext().getResources().getString(R.string.more));
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), GroupActivity.class);
                startActivity(intent);
            }
        });
        int size = subGroupList.size();
        if (size > 3){
            more.setVisibility(View.VISIBLE);
        }
        
        for (int i = 0; i < size; i++) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.recommend_group_item, null);
            groupWrapper.addView(view);

            if (size > 2 && i == size - 1) {
                LinearLayout findMore = view.findViewById(R.id.find_more);
                findMore.setVisibility(View.VISIBLE);

                findMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), GroupActivity.class);
                        startActivity(intent);
                    }
                });
            }
            
            final SubGroupActivity.SubGroup subGroup = subGroupList.get(i);
            TextView groupName = view.findViewById(R.id.group_name);
            groupName.setText(subGroup.groupName);

            RoundImageView groupLogo = view.findViewById(R.id.group_logo);
            String logo = subGroup.groupLogoUri;
            if (logo != null && !"".equals(logo)) {
                Glide.with(getContext()).load(HttpUtil.DOMAIN + logo).into(groupLogo);
            } else {
                groupLogo.setImageDrawable(getContext().getDrawable(R.drawable.icon));
            }
            
            TextView visitRecord = view.findViewById(R.id.visit_record);
            visitRecord.setText("访问 " + subGroup.visitRecord);

            TextView activityCount = view.findViewById(R.id.activity_count);
            activityCount.setText("动态 " + subGroup.activityCount);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), SubGroupDetailsActivity.class);
                    intent.putExtra("gid", subGroup.gid);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(intent);
                }
            });
        }
    }
    
    private class PictureAddBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ADD_PICTURE_BROADCAST:
                    if (isDebug) Slog.d(TAG, "==========ADD_PICTURE_BROADCAST");
                    lookFriend.setVisibility(View.GONE);
                    meetList.clear();
                    //force update data
                    loadData();
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
        if (isDebug) Slog.d(TAG, "=============onResume");
        //getUserProfile();
        //updateData();
        //meetRecommendListAdapter.setData(meetList);
        //meetRecommendListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterLoginBroadcast();

        /*
        if (recyclerView != null) {
            recyclerView.destroy();
            recyclerView = null;
        }
         */
    }
    
    private void stopLoadProgress() {
        if (progressImageView.getVisibility() == View.VISIBLE) {
            animationDrawable.stop();
            progressImageView.setVisibility(View.GONE);
        }
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
            case MY_CONDITION_SET_DONE:
                getRecommendContacts();
                break;
                case MY_CONDITION_NOT_SET:
                setMeetHeaderView();
                if (!TextUtils.isEmpty(userProfile.getAvatar())) {
                    avatarSet = true;
                }
                getRecommendContacts();
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
            case MY_UNIVERSITY_GROUP_GET_DONE:
                setMyUniversityGroupRecommendView();
                break;
                 case GET_RECOMMEND_MEMBER_DONE:
                setRecommendContactsView();
                getRecommendTalent();
                getMyUniversityGroup();
                break;
            case HAD_NO_RECOMMEND_MEMBER:
                getRecommendTalent();
                getMyUniversityGroup();
                break;
            case GET_TALENT_DONE:
                setRecommendTalentsHeader();
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
    
    static class MyHandler extends HandlerTemp<ExploreFragment> {

        public MyHandler(ExploreFragment cls) {
            super(cls);
        }

        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            ExploreFragment meetRecommendFragment = ref.get();
            if (meetRecommendFragment != null) {
                meetRecommendFragment.handleMessage(message);
            }
        }
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
}
        
