package com.hetang.group;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hetang.R;
import com.hetang.adapter.SubGroupSummaryAdapter;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.common.MyApplication;
import com.hetang.meet.UserMeetInfo;
import com.hetang.talent.TalentAuthenticationDialogFragment;
import com.hetang.talent.TalentDetailsActivity;
import com.hetang.talent.TalentSummaryListActivity;
import com.hetang.util.CommonDialogFragmentInterface;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.MyLinearLayoutManager;
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
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.ActionBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.hetang.common.MyApplication.getContext;
import static com.hetang.group.GroupFragment.eden_group;
import static com.hetang.group.SingleGroupActivity.GET_MY_GROUP_DONE;
import static com.hetang.group.SingleGroupActivity.GET_TALENT_DONE;
import static com.hetang.group.SingleGroupActivity.SINGLE_GROUP_GET_MY;
import static com.hetang.group.SingleGroupActivity.getSingleGroup;
import static com.hetang.group.SingleGroupDetailsActivity.GET_SINGLE_GROUP_BY_GID;
import static com.hetang.group.SubGroupDetailsActivity.GET_SUBGROUP_BY_GID;
import static com.hetang.talent.TalentAuthenticationDialogFragment.COMMON_TALENT_AUTHENTICATION_RESULT_OK;
import static com.hetang.talent.TalentAuthenticationDialogFragment.TALENT_AUTHENTICATION_RESULT_OK;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;
import static com.hetang.util.DateUtil.timeStampToDay;

public class SubGroupActivity extends BaseAppCompatActivity implements CommonDialogFragmentInterface {
    public static final String GET_MY_UNIVERSITY_SUBGROUP = HttpUtil.DOMAIN + "?q=subgroup/get_my_university";
    public static final String GROUP_ADD_BROADCAST = "com.hetang.action.GROUP_ADD";
    public static final String TALENT_ADD_BROADCAST = "com.hetang.action.TALENT_ADD";
    public static final int ADD_NEW_SUBGROUP_DONE = 8;
    public static final int GET_SINGLE_GROUP_DONE = 9;
    public static final int ADD_NEW_TALENT_DONE = 11;
    public static final String GET_TALENTS_BY_TYPE = HttpUtil.DOMAIN + "?q=talent/get_all_by_type";
    public static final String GET_ALL_TALENTS = HttpUtil.DOMAIN + "?q=talent/get_all";
    public static final String GET_ALL_VERIFY_TALENTS = HttpUtil.DOMAIN + "?q=talent/get_all_verify";
    private static final boolean isDebug = true;
    private static final String TAG = "SubGroupActivity";
    private static final int PAGE_SIZE = 8;
    private static final String SINGLE_GROUP_GET_ALL = HttpUtil.DOMAIN + "?q=single_group/get_all";
    private static final String SUBGROUP_GET_ALL = HttpUtil.DOMAIN + "?q=subgroup/get_all";
    private static final String SUBGROUP_UPDATE = HttpUtil.DOMAIN + "?q=subgroup/update";
    private static final String ADD_SUBGROUP_VISITOR_RECORD = HttpUtil.DOMAIN + "?q=visitor_record/add_group_visit_record";
    private static final int GET_ALL_DONE = 1;
    private static final int UPDATE_ALL = 2;
    private static final int GET_ALL_END = 3;
    private static final int NO_UPDATE = 4;
    private static final int SET_AVATAR = 5;
    private static final int NO_MORE = 6;
    private static final int ADD_VISITOR_RECORD_DONE = 7;
    private static final int NO_SINGLE_GROUP_DONE = 10;
    final int itemLimit = 3;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    private int mLoadSize = 0;
    private int mUpdateSize = 0;
    private Handler handler;
    private int type = 0;
    private ViewGroup myGroupView;
    private SingleGroupReceiver mReceiver = new SingleGroupReceiver();
    private SubGroupSummaryAdapter subGroupSummaryAdapter;
    private XRecyclerView recyclerView;
    private List<SubGroup> mSubGroupList = new ArrayList<>();
    private List<Talent> mTalentList = new ArrayList<>();
    private List<SingleGroupActivity.SingleGroup> mSingleGroupList = new ArrayList<>();
    private List<SingleGroupActivity.SingleGroup> mLeadGroupList = new ArrayList<>();
    private List<SingleGroupActivity.SingleGroup> mJoinGroupList = new ArrayList<>();

    public static void updateVisitorRecord(int gid) {

        RequestBody requestBody = new FormBody.Builder()
                .add("gid", String.valueOf(gid))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), ADD_SUBGROUP_VISITOR_RECORD, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (isDebug)
                    Slog.d(TAG, "==========updateVisitorRecord response body : " + response.body());
                if (response.body() != null) {
                    //todo
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    public static SubGroup getSubGroup(JSONObject group) {
        SubGroup subGroup = new SubGroup();
        if (group != null) {
            subGroup.gid = group.optInt("gid");
            subGroup.type = group.optInt("type");
            subGroup.groupName = group.optString("group_name");
            subGroup.groupProfile = group.optString("group_profile");
            subGroup.groupLogoUri = group.optString("logo_uri");
            subGroup.org = group.optString("group_org");
            subGroup.region = group.optString("region");
            subGroup.memberCount = group.optInt("member_count");
            subGroup.visitRecord = group.optInt("visit_record");
            subGroup.followCount = group.optInt("follow_count");
            subGroup.activityCount = group.optInt("activity_count");
            subGroup.created = timeStampToDay(group.optInt("created"));

            subGroup.leader = new UserMeetInfo();
            if (group.optJSONObject("leader") != null) {
                ParseUtils.setBaseProfile(subGroup.leader, group.optJSONObject("leader"));
            }

            return subGroup;
        }

        return null;
    }
    
    public static class Talent implements Serializable {
        public int tid;
        public String introduction;
        public String created;
        public UserMeetInfo profile;
        public int type;
        public String title;
        public String subject;
        public String[] materialArray;
        public float evaluateScores = 0;
        public int evaluateCount = 0;
        public int answerCount = 0;
        public int questionCount = 0;
        public int status = -1;
        public int charge;
        public String desc;
        public String reason;
    }

    public static Talent getTalent(JSONObject talentObject) {
        Talent talent = new Talent();
        if (talentObject != null) {
            talent.tid = talentObject.optInt("tid");
            talent.title = talentObject.optString("talent_title");
            talent.introduction = talentObject.optString("introduction");
            talent.status = talentObject.optInt("status");
            talent.desc = talentObject.optString("description");
            talent.subject = talentObject.optString("subject");
            talent.evaluateCount = talentObject.optInt("count");
            talent.evaluateScores = (float) talentObject.optDouble("scores");
            talent.reason = talentObject.optString("reason");
            talent.answerCount = talentObject.optInt("answer_count");
            talent.questionCount = talentObject.optInt("question_count");
            talent.profile = new UserMeetInfo();
            ParseUtils.setBaseProfile(talent.profile, talentObject);

            JSONArray materialJSONArray = talentObject.optJSONArray("materials");
            if (materialJSONArray != null && materialJSONArray.length() > 0) {
                talent.materialArray = new String[materialJSONArray.length()];
                for (int i = 0; i < materialJSONArray.length(); i++) {
                    talent.materialArray[i] = materialJSONArray.optString(i);
                }
            }
        }

        return talent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subgroup_summary);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        handler = new SubGroupActivity.MyHandler(this);

        type = getIntent().getIntExtra("type", 0);

        initView();

        if (type != eden_group) {
            //loadData();
            getTalentsByType();
        } else {
            getMySingleGroup();
            getRecommendSingleGroup();
            loadData();
            //getRecommendSingleGroup();
            //loadData();
        }
    }

    private void getTalentsByType() {
        RequestBody requestBody = new FormBody.Builder()
                .add("step", String.valueOf(8))
                .add("page", String.valueOf(0))
                .add("type", String.valueOf(type)).build();
        HttpUtil.sendOkHttpRequest(getContext(), GET_TALENTS_BY_TYPE, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug)
                        Slog.d(TAG, "==========getTalentsByType response text : " + responseText);
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

                loadData();
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    public int processTalentsResponse(JSONObject talentsObject) {
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
                        Talent talent = getTalent(talentObject);
                        mTalentList.add(talent);
                    }
                }
            }
        }

        return talentSize;
    }

    private void getMySingleGroup() {
        RequestBody requestBody = new FormBody.Builder().build();

        HttpUtil.sendOkHttpRequest(getContext(), SINGLE_GROUP_GET_MY, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug)
                        Slog.d(TAG, "==========getMySingleGroup response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject SingleGroupResponse = null;
                        try {
                            SingleGroupResponse = new JSONObject(responseText);
                            if (SingleGroupResponse != null) {
                                int loadSize = processMyGroupResponse(SingleGroupResponse);
                                if (loadSize > 0) {
                                    handler.sendEmptyMessage(GET_MY_GROUP_DONE);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private int processMyGroupResponse(JSONObject SingleGroupResponse) {

        int singGroupSize = 0;
        JSONArray leadGroupArray = null;
        JSONArray joinGroupArray = null;

        if (SingleGroupResponse != null) {
            leadGroupArray = SingleGroupResponse.optJSONArray("lead_groups");
            joinGroupArray = SingleGroupResponse.optJSONArray("join_groups");
        }

        if (leadGroupArray != null) {
            singGroupSize = leadGroupArray.length();
            if (singGroupSize > 0) {
                for (int i = 0; i < leadGroupArray.length(); i++) {
                    JSONObject group = leadGroupArray.optJSONObject(i);
                    if (group != null) {
                        SingleGroupActivity.SingleGroup singleGroup = getSingleGroup(group, false);
                        mLeadGroupList.add(singleGroup);
                    }
                }
            }
        }

        if (joinGroupArray != null) {
            singGroupSize = joinGroupArray.length();
            if (singGroupSize > 0) {
                for (int i = 0; i < joinGroupArray.length(); i++) {
                    JSONObject group = joinGroupArray.optJSONObject(i);
                    if (group != null) {
                        SingleGroupActivity.SingleGroup singleGroup = getSingleGroup(group, false);
                        mJoinGroupList.add(singleGroup);
                    }
                }
            }
        }

        return singGroupSize;
    }

    private void setMyGroupView() {
        if (myGroupView == null) {
            Slog.d(TAG, "------------------------->myGroupView null");
            myGroupView = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.my_single_group, (ViewGroup) findViewById(android.R.id.content), false);
            recyclerView.addHeaderView(myGroupView);
        }
        android.widget.LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, 3);

        if (mLeadGroupList.size() > 0) {
            for (int i = 0; i < mLeadGroupList.size(); i++) {
                View leadGroupItemView = LayoutInflater.from(getContext()).inflate(R.layout.single_group_summary_item, (ViewGroup) findViewById(android.R.id.content), false);
                leadGroupItemView.setLayoutParams(layoutParams);
                myGroupView.addView(leadGroupItemView);
                setGroupView(leadGroupItemView, mLeadGroupList.get(i));
                final SingleGroupActivity.SingleGroup singleGroup = mLeadGroupList.get(i);
                leadGroupItemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), SingleGroupDetailsActivity.class);
                        intent.putExtra("gid", singleGroup.gid);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        startActivity(intent);
                    }
                });
            }
        }
        if (mJoinGroupList.size() > 0) {
            for (int i = 0; i < mJoinGroupList.size(); i++) {
                View joinGroupItemView = LayoutInflater.from(getContext()).inflate(R.layout.single_group_summary_item, (ViewGroup) findViewById(android.R.id.content), false);
                joinGroupItemView.setLayoutParams(layoutParams);
                myGroupView.addView(joinGroupItemView);
                setGroupView(joinGroupItemView, mJoinGroupList.get(i));
                final SingleGroupActivity.SingleGroup singleGroup = mJoinGroupList.get(i);
                joinGroupItemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), SingleGroupDetailsActivity.class);
                        intent.putExtra("gid", singleGroup.gid);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        startActivity(intent);
                    }
                });
            }
        }

    }

    private void setGroupView(View view, SingleGroupActivity.SingleGroup singleGroup) {
        TextView nameTV = view.findViewById(R.id.leader_name);
        nameTV.setText(singleGroup.leader.getNickName());
        RoundImageView avatar = view.findViewById(R.id.leader_avatar);
        Glide.with(this).load(HttpUtil.DOMAIN + singleGroup.leader.getAvatar()).into(avatar);
        TextView universityTV = view.findViewById(R.id.university);
        universityTV.setText(singleGroup.leader.getUniversity());
        TextView introductionTV = view.findViewById(R.id.introduction);

        introductionTV.setText(singleGroup.introduction);
        TextView maleCountTV = view.findViewById(R.id.male_member_count);
        maleCountTV.setText(getResources().getString(R.string.male) + " " + singleGroup.maleCount);
        TextView femaleCountTV = view.findViewById(R.id.female_member_count);
        femaleCountTV.setText(getResources().getString(R.string.female) + " " + singleGroup.femaleCount);
        if (singleGroup.evaluateCount > 0) {
            TextView evaluateCountTV = view.findViewById(R.id.evaluate_count);
            float scoreFloat = singleGroup.evaluateScores / singleGroup.evaluateCount;
            float score = (float) (Math.round(scoreFloat * 10)) / 10;
            evaluateCountTV.setText(getResources().getString(R.string.fa_star) + " " + score + getResources().getString(R.string.dot) + singleGroup.evaluateCount);
        }

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(view.findViewById(R.id.evaluate_count), font);
    }

    private void initView() {
        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.custom_actionbar), font);

        handler = new SubGroupActivity.MyHandler(this);
        recyclerView = findViewById(R.id.sub_group_summary_list);
        subGroupSummaryAdapter = new SubGroupSummaryAdapter(getContext());
        MyLinearLayoutManager linearLayoutManager = new MyLinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setRefreshProgressStyle(BallSpinFadeLoader);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallRotate);

        recyclerView.setPullRefreshEnabled(false);
        recyclerView.getDefaultRefreshHeaderView().setRefreshTimeVisible(true);
        recyclerView.getDefaultFootView().setLoadingHint(getString(R.string.loading_pull_up_tip));
        recyclerView.getDefaultFootView().setNoMoreHint(getString(R.string.no_more));

        // When the item number of the screen number is list.size-2,we call the onLoadMore
        recyclerView.setLimitNumberToCallLoadMore(itemLimit);
        recyclerView.setRefreshProgressStyle(ProgressStyle.BallBeat);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    subGroupSummaryAdapter.setScrolling(false);
                    subGroupSummaryAdapter.notifyDataSetChanged();
                } else {
                    subGroupSummaryAdapter.setScrolling(true);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        recyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                //updateData();
            }

            @Override
            public void onLoadMore() {
                loadData();
            }
        });

        subGroupSummaryAdapter.setItemClickListener(new SubGroupSummaryAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Slog.d(TAG, "==========click : " + position);
                int gid = mSubGroupList.get(position).gid;
                updateVisitorRecord(gid);
                Intent intent = new Intent(getContext(), SubGroupDetailsActivity.class);
                intent.putExtra("gid", gid);
                intent.putExtra("type", type);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivityForResult(intent, RESULT_FIRST_USER);
            }
        });

        recyclerView.setAdapter(subGroupSummaryAdapter);

        FloatingActionButton floatingActionButton = findViewById(R.id.create_single_group);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //checkAvatarSet();
                showSubGroupDialog();
            }
        });

        registerLoginBroadcast();

        TextView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button becomeTalentBtn = findViewById(R.id.become_talent);
        //if (type == eden_group) {
        becomeTalentBtn.setVisibility(View.VISIBLE);
        becomeTalentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                becomeTalent(type);
            }
        });
        //}


        //show progressImage before loading done
        progressImageView = findViewById(R.id.animal_progress);
        animationDrawable = (AnimationDrawable) progressImageView.getDrawable();
        progressImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        }, 50);
    }

    private void getRecommendSingleGroup() {
        RequestBody requestBody = new FormBody.Builder()
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(0))
                .add("type", String.valueOf(type))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), SINGLE_GROUP_GET_ALL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug)
                        Slog.d(TAG, "==========getRecommendSingleGroup response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        try {
                            JSONObject singleGroupResponse = new JSONObject(responseText);
                            if (singleGroupResponse != null) {
                                int groupSize = processSingleGroupResponse(singleGroupResponse);
                                if (groupSize > 0) {
                                    handler.sendEmptyMessage(GET_SINGLE_GROUP_DONE);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }

            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void becomeTalent(int type) {
        TalentAuthenticationDialogFragment talentAuthenticationDialogFragment = new TalentAuthenticationDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        talentAuthenticationDialogFragment.setArguments(bundle);
        //createSingleGroupDialogFragment.setTargetFragment(SingleGroupActivity.this, REQUEST_CODE);
        talentAuthenticationDialogFragment.show(getSupportFragmentManager(), "TalentAuthenticationDialogFragment");
    }

    @Override
    public void onBackFromDialog(int type, int result, boolean status) {
        switch (type) {
            case TALENT_AUTHENTICATION_RESULT_OK://For EvaluateDialogFragment back
                if (status == true) {
                    getMyNewAdded(result, true);
                }
                break;
            case COMMON_TALENT_AUTHENTICATION_RESULT_OK:
                if (status == true) {

                }
                break;
            default:
                break;
        }
    }

    private void loadData() {

        final int page = mSubGroupList.size() / PAGE_SIZE;
        RequestBody requestBody = new FormBody.Builder()
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .add("type", String.valueOf(type))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), SUBGROUP_GET_ALL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========loadData response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject subGroupResponse = null;
                        try {
                            subGroupResponse = new JSONObject(responseText);
                            if (subGroupResponse != null) {
                                mLoadSize = processResponse(subGroupResponse);

                                if (mLoadSize == PAGE_SIZE) {
                                    handler.sendEmptyMessage(GET_ALL_DONE);
                                } else {
                                    if (mLoadSize != 0) {
                                        handler.sendEmptyMessage(GET_ALL_END);
                                    } else {
                                        handler.sendEmptyMessage(NO_MORE);
                                    }
                                }
                            } else {
                                handler.sendEmptyMessage(NO_MORE);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        handler.sendEmptyMessage(NO_MORE);
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private int processResponse(JSONObject subGroupResponse) {

        int subGroupSize = 0;
        JSONArray subGroupArray = null;

        if (subGroupResponse != null) {
            subGroupArray = subGroupResponse.optJSONArray("subgroup");
        }

        if (subGroupArray != null) {
            subGroupSize = subGroupArray.length();
            if (subGroupSize > 0) {
                for (int i = 0; i < subGroupArray.length(); i++) {
                    JSONObject group = subGroupArray.optJSONObject(i);
                    if (group != null) {
                        SubGroup subGroup = getSubGroup(group);
                        mSubGroupList.add(subGroup);
                    }
                }
            }
        }

        return subGroupSize;
    }

    private int processSingleGroupResponse(JSONObject singleGroupResponse) {

        int singleGroupSize = 0;
        JSONArray singleGroupArray = null;

        if (singleGroupResponse != null) {
            singleGroupArray = singleGroupResponse.optJSONArray("single_group");
        }

        if (singleGroupArray != null) {
            singleGroupSize = singleGroupArray.length();
            if (singleGroupSize > 0) {
                for (int i = 0; i < singleGroupArray.length(); i++) {
                    JSONObject group = singleGroupArray.optJSONObject(i);
                    if (group != null) {
                        SingleGroupActivity.SingleGroup singleGroup = getSingleGroup(group, true);
                        mSingleGroupList.add(singleGroup);
                    }
                }
            }
        }

        return singleGroupSize;
    }

    private void setRecommendTalentsHeader() {
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        int innerWidth = dm.widthPixels - (int) Utility.dpToPx(getContext(), 32f);
        int height = innerWidth;
        int wrapperWidth = innerWidth / 2;
        int avatarWidth = wrapperWidth - (int) Utility.dpToPx(getContext(), 12f);

        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(wrapperWidth, WRAP_CONTENT);

        View talentView = LayoutInflater.from(getContext()).inflate(R.layout.recommend_talent, (ViewGroup) findViewById(android.R.id.content), false);
        recyclerView.addHeaderView(talentView);
        TextView moreTalent = talentView.findViewById(R.id.more);
        moreTalent.setText(getResources().getString(R.string.more_talent));
        moreTalent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SubGroupActivity.this, TalentSummaryListActivity.class);
                intent.putExtra("type", type);
                startActivity(intent);
            }
        });

        GridLayout talentWrapper = talentView.findViewById(R.id.talent_wrapper);
        if (talentWrapper == null) {
            return;
        }

        int size = mTalentList.size();

        if (size > 6) {
            size = 6;
            moreTalent.setVisibility(View.VISIBLE);
        }

        for (int i = 0; i < size; i++) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.recommend_talent_item, null);
            talentWrapper.addView(view, params);
            final Talent talent = mTalentList.get(i);
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

            if (talent.profile.getSituation() == 0) {
                degree.setText(talent.profile.getDegreeName(talent.profile.getDegree()));
                university.setText(talent.profile.getUniversity());
            } else {
                degree.setText(talent.profile.getPosition());
                university.setText(talent.profile.getIndustry());
            }


            TextView introduction = view.findViewById(R.id.introduction);
            introduction.setText(talent.introduction);

            TextView charge = view.findViewById(R.id.charge);
            charge.setText(talent.charge + "元");

            TextView subject = view.findViewById(R.id.subject);
            subject.setText(talent.subject);

            if (talent.evaluateCount > 0) {
                TextView evaluateCountTV = view.findViewById(R.id.evaluate_count);
                float scoreFloat = talent.evaluateScores / talent.evaluateCount;
                float score = (float) (Math.round(scoreFloat * 10)) / 10;
                evaluateCountTV.setText(getResources().getString(R.string.fa_star) + " " + score + getResources().getString(R.string.dot) + talent.evaluateCount);
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

    private void setSingleGroupHeader() {
        Slog.d(TAG, "-------------------->setSingleGroupHeader");
        View singleGroupView = LayoutInflater.from(getContext()).inflate(R.layout.recommend_group, (ViewGroup) findViewById(android.R.id.content), false);
        recyclerView.addHeaderView(singleGroupView);
        TextView title = singleGroupView.findViewById(R.id.group_recommend_title);
        title.setText(getContext().getResources().getString(R.string.matchmaker));
        LinearLayout groupWrapper = singleGroupView.findViewById(R.id.group_wrapper);
        if (groupWrapper == null) {
            return;
        }

        TextView moreTalent = singleGroupView.findViewById(R.id.more);
        moreTalent.setText(getResources().getString(R.string.more_talent));
        moreTalent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SubGroupActivity.this, SingleGroupActivity.class);
                startActivity(intent);
            }
        });


        int size = mSingleGroupList.size();
        if (size > 10) {
            size = 10;
        }

        for (int i = 0; i < size; i++) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.single_group_item, null);
            groupWrapper.addView(view);

            final SingleGroupActivity.SingleGroup singleGroup = mSingleGroupList.get(i);

            RoundImageView avatarRV = view.findViewById(R.id.avatar);
            String avatar = singleGroup.leader.getAvatar();
            if (avatar != null && !"".equals(avatar)) {
                Glide.with(getContext()).load(HttpUtil.DOMAIN + avatar).into(avatarRV);
            }

            TextView leaderName = view.findViewById(R.id.name);
            leaderName.setText(singleGroup.leader.getNickName());
            TextView university = view.findViewById(R.id.university);
            university.setText(singleGroup.leader.getUniversity());

            TextView introduction = view.findViewById(R.id.introduction);
            introduction.setText(singleGroup.introduction);

            TextView memberCount = view.findViewById(R.id.group_member);
            memberCount.setText("成员 " + singleGroup.memberCount);

            if (singleGroup.evaluateCount > 0) {
                TextView evaluateCountTV = view.findViewById(R.id.evaluate_count);
                float scoreFloat = singleGroup.evaluateScores / singleGroup.evaluateCount;
                float score = (float) (Math.round(scoreFloat * 10)) / 10;
                evaluateCountTV.setText(getResources().getString(R.string.fa_star) + " " + score + getResources().getString(R.string.dot) + singleGroup.evaluateCount);
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), SingleGroupDetailsActivity.class);
                    intent.putExtra("gid", singleGroup.gid);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(intent);
                }
            });

            Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.evaluate_count), font);
        }
    }

    private int processUpdateResponse(JSONObject SingleGroupResponse) {
        List<SubGroup> mSubGroupUpdateList = new ArrayList<>();
        JSONArray SingleGroupArray = null;
        if (SingleGroupResponse != null) {
            SingleGroupArray = SingleGroupResponse.optJSONArray("single_group");
        }

        if (SingleGroupArray != null) {
            if (SingleGroupArray.length() > 0) {
                mSubGroupUpdateList.clear();
                for (int i = 0; i < SingleGroupArray.length(); i++) {
                    JSONObject group = SingleGroupArray.optJSONObject(i);
                    if (group != null) {
                        SubGroup singleGroup = getSubGroup(group);
                        mSubGroupUpdateList.add(singleGroup);
                    }
                }
                mSubGroupList.addAll(0, mSubGroupUpdateList);
                Message message = new Message();
                message.what = UPDATE_ALL;
                Bundle bundle = new Bundle();
                bundle.putInt("update_size", mSubGroupUpdateList.size());
                message.setData(bundle);
                handler.sendMessage(message);
            } else {
                handler.sendEmptyMessage(GET_ALL_END);
            }
        }

        return SingleGroupArray != null ? SingleGroupArray.length() : 0;
    }

    private void processNewAddResponse(JSONObject subGroupResponse, boolean isTalent) {
        JSONObject subGroupObject = null;
        if (subGroupResponse != null) {
            if (isTalent) {
                subGroupObject = subGroupResponse.optJSONObject("single_group");
            } else {
                subGroupObject = subGroupResponse.optJSONObject("group");
            }
        }

        if (subGroupObject != null) {
            if (isTalent) {
                SingleGroupActivity.SingleGroup singleGroup = getSingleGroup(subGroupObject, true);
                mLeadGroupList.add(0, singleGroup);
                handler.sendEmptyMessage(ADD_NEW_TALENT_DONE);
            } else {
                SubGroup singleGroup = getSubGroup(subGroupObject);
                mSubGroupList.add(0, singleGroup);
                handler.sendEmptyMessage(ADD_NEW_SUBGROUP_DONE);
            }
        }
    }

    private void checkAvatarSet() {
        RequestBody requestBody = new FormBody.Builder().build();
        HttpUtil.sendOkHttpRequest(getContext(), ParseUtils.GET_USER_PROFILE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug)
                        Slog.d(TAG, "==========get archive response text : " + responseText);
                    if (responseText != null) {
                        if (!TextUtils.isEmpty(responseText)) {
                            try {
                                JSONObject jsonObject = new JSONObject(responseText).optJSONObject("user");
                                if (isDebug)
                                    Slog.d(TAG, "==============user profile object: " + jsonObject);
                                if (jsonObject != null) {
                                    UserProfile userProfile = ParseUtils.getUserProfileFromJSONObject(jsonObject);
                                    final boolean isAvatarSet;
                                    if (!TextUtils.isEmpty(userProfile.getAvatar())) {
                                        //avatar is set up
                                        showSubGroupDialog();
                                    } else {
                                        //avatar is not set up
                                        handler.sendEmptyMessage(SET_AVATAR);
                                    }

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

    private void addMyNewTalent() {
        View leadGroupItemView = LayoutInflater.from(getContext()).inflate(R.layout.single_group_summary_item, (ViewGroup) findViewById(android.R.id.content), false);
        if (myGroupView == null) {
            myGroupView = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.my_single_group, (ViewGroup) findViewById(android.R.id.content), false);
            recyclerView.addHeaderView(myGroupView);
        }
        myGroupView.addView(leadGroupItemView, 1);
        setGroupView(leadGroupItemView, mLeadGroupList.get(0));

        leadGroupItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SingleGroupDetailsActivity.class);
                intent.putExtra("gid", mLeadGroupList.get(0).gid);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });
    }

    private void showSubGroupDialog() {
        CreateSubGroupDialogFragment createSingleGroupDialogFragment = new CreateSubGroupDialogFragment();
        //createSingleGroupDialogFragment.setTargetFragment(MeetSingleGroupFragment.this, REQUEST_CODE);
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        createSingleGroupDialogFragment.setArguments(bundle);
        createSingleGroupDialogFragment.show(getSupportFragmentManager(), "CreateSubGroupDialogFragment");
    }

    public void updateData() {
        String last = SharedPreferencesUtils.getSingleGroupLast(getContext());
        RequestBody requestBody = new FormBody.Builder()
                .add("last", last)
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(0))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), SUBGROUP_UPDATE, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject singleGroupResponse = null;
                        try {
                            singleGroupResponse = new JSONObject(responseText);
                            if (singleGroupResponse != null) {
                                int current = singleGroupResponse.optInt("current");
                                Slog.d(TAG, "----------------->current: " + current);
                                SharedPreferencesUtils.setSingleGroupLast(getContext(), String.valueOf(current));

                                mUpdateSize = processUpdateResponse(singleGroupResponse);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (mUpdateSize > 0) {
                            handler.sendEmptyMessage(UPDATE_ALL);
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
    }

    public void handleMessage(Message message) {
        switch (message.what) {
            case GET_ALL_DONE:
                Slog.d(TAG, "-------------->GET_ALL_DONE");
                subGroupSummaryAdapter.setData(mSubGroupList);
                subGroupSummaryAdapter.notifyDataSetChanged();
                //recyclerView.refreshComplete();
                // recyclerView.loadMoreComplete();
                stopLoadProgress();
                break;
            case GET_ALL_END:
                Slog.d(TAG, "-------------->GET_ALL_END");
                subGroupSummaryAdapter.setData(mSubGroupList);
                subGroupSummaryAdapter.notifyDataSetChanged();
                //recyclerView.refreshComplete();
                recyclerView.loadMoreComplete();
                recyclerView.setNoMore(true);
                stopLoadProgress();
                break;
            case NO_MORE:
                Slog.d(TAG, "-------------->NO_MORE");
                recyclerView.setNoMore(true);
                recyclerView.loadMoreComplete();
                stopLoadProgress();
                break;
            case UPDATE_ALL:
                Bundle bundle = message.getData();
                int updateSize = bundle.getInt("update_size");
                subGroupSummaryAdapter.setData(mSubGroupList);
                subGroupSummaryAdapter.notifyItemRangeInserted(0, updateSize);
                subGroupSummaryAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();
                break;
            case NO_UPDATE:
                recyclerView.refreshComplete();
                mUpdateSize = 0;
                break;
            case ADD_NEW_SUBGROUP_DONE:
                subGroupSummaryAdapter.setData(mSubGroupList);
                subGroupSummaryAdapter.notifyItemRangeInserted(0, 1);
                subGroupSummaryAdapter.notifyDataSetChanged();
                if (mSingleGroupList.size() <= PAGE_SIZE) {
                    recyclerView.loadMoreComplete();
                }
                break;
            case ADD_NEW_TALENT_DONE:
                addMyNewTalent();
                subGroupSummaryAdapter.notifyDataSetChanged();
                break;
            case GET_SINGLE_GROUP_DONE:
                setSingleGroupHeader();
                break;
            case GET_MY_GROUP_DONE:
                setMyGroupView();
                break;
            case GET_TALENT_DONE:
                setRecommendTalentsHeader();
                break;
            default:
                break;
        }
    }

    private void stopLoadProgress() {
        if (progressImageView.getVisibility() == View.VISIBLE) {
            animationDrawable.stop();
            progressImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (isDebug)
            Slog.d(TAG, "===================onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode);
        if (requestCode == Activity.RESULT_FIRST_USER) {
            switch (resultCode) {
                case RESULT_OK:

                    break;
                default:
                    break;
            }
        }
    }

    private void registerLoginBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GROUP_ADD_BROADCAST);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, intentFilter);
    }

    //unregister local broadcast
    private void unRegisterLoginBroadcast() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
    }

    private void getMyNewAdded(int gid, final boolean isTalent) {
        RequestBody requestBody = new FormBody.Builder()
                .add("gid", String.valueOf(gid))
                .build();

        String uri = GET_SUBGROUP_BY_GID;
        if (isTalent) {
            uri = GET_SINGLE_GROUP_BY_GID;
        }

        HttpUtil.sendOkHttpRequest(getContext(), uri, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (isDebug) Slog.d(TAG, "==========response body : " + response.body());

                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug)
                        Slog.d(TAG, "==========getMyNewAdded response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject subGroupResponse = null;
                        try {
                            subGroupResponse = new JSONObject(responseText);
                            if (subGroupResponse != null) {
                                processNewAddResponse(subGroupResponse, isTalent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterLoginBroadcast();

        if (recyclerView != null) {
            recyclerView.destroy();
            recyclerView = null;
        }
    }

    public static class SubGroup implements Serializable {
        public int gid;
        public int type;
        public String groupName;
        public String groupProfile;
        public String org;
        public String region;
        public String groupLogoUri;
        public int memberCount = 0;
        public int followCount = 0;
        public int visitRecord = 0;
        public int activityCount = 0;
        public int maleCount = 0;
        public int femaleCount = 0;
        public List<UserProfile> maleList = new ArrayList<>();
        public List<UserProfile> femaleList = new ArrayList<>();
        public String created;
        public UserMeetInfo leader;

        //public List<String> headUrlList;
        public int authorStatus = -1;
        public int followed = -1;
        public boolean isLeader = false;
        //public List<UserMeetInfo> memberInfoList;
    }

    static class MyHandler extends Handler {
        WeakReference<SubGroupActivity> subGroupActivityWeakReference;

        MyHandler(SubGroupActivity subGroupActivity) {
            subGroupActivityWeakReference = new WeakReference<SubGroupActivity>(subGroupActivity);
        }

        @Override
        public void handleMessage(Message message) {
            SubGroupActivity subGroupActivity = subGroupActivityWeakReference.get();
            if (subGroupActivity != null) {
                subGroupActivity.handleMessage(message);
            }
        }
    }

    private class SingleGroupReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case GROUP_ADD_BROADCAST:
                    Slog.d(TAG, "==========GROUP_ADD_BROADCAST");
                    int gid = intent.getIntExtra("gid", 0);
                    if (gid > 0) {
                        getMyNewAdded(gid, false);
                    }
                    break;
            }

        }
    }

}
             
