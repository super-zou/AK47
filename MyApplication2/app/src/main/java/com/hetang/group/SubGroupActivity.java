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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.adapter.SubGroupSummaryAdapter;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.common.MyApplication;
import com.hetang.meet.UserMeetInfo;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.hetang.common.MyApplication.getContext;
import static com.hetang.group.GroupFragment.fraternity_group;
import static com.hetang.group.MeetSingleGroupFragment.getSingleGroup;
import static com.hetang.group.SubGroupDetailsActivity.GET_SUBGROUP_BY_GID;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class SubGroupActivity extends BaseAppCompatActivity {
    public static final String GET_MY_UNIVERSITY_SUBGROUP = HttpUtil.DOMAIN + "?q=subgroup/get_my_university";
    public static final String GROUP_ADD_BROADCAST = "com.hetang.action.GROUP_ADD";
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
    private static final int ADD_NEW_SUBGROUP_DONE = 8;
    private static final int GET_SINGLE_GROUP_DONE = 9;
    private static final int NO_SINGLE_GROUP_DONE = 10;
    final int itemLimit = 3;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    private int mLoadSize = 0;
    private int mUpdateSize = 0;
    private Handler handler;
    private int type = 0;
    private SingleGroupReceiver mReceiver = new SingleGroupReceiver();
    private SubGroupSummaryAdapter subGroupSummaryAdapter;
    private XRecyclerView recyclerView;
    private List<SubGroup> mSubGroupList = new ArrayList<>();
    private List<MeetSingleGroupFragment.SingleGroup> mSingleGroupList = new ArrayList<>();

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
            subGroup.created = Utility.timeStampToDay(group.optInt("created"));

            subGroup.leader = new UserMeetInfo();
            if (group.optJSONObject("leader") != null) {
                ParseUtils.setBaseProfile(subGroup.leader, group.optJSONObject("leader"));
            }

            return subGroup;
        }

        return null;
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

        if (type != fraternity_group) {
            loadData();
        } else {
            getRecommendSingleGroup();
        }
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
                    if (isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        try {
                            JSONObject singleGroupResponse = new JSONObject(responseText);
                            if (singleGroupResponse != null) {
                                int groupSize = processSingleGroupResponse(singleGroupResponse);
                                if (groupSize > 0) {
                                    handler.sendEmptyMessage(GET_SINGLE_GROUP_DONE);
                                } else {
                                    handler.sendEmptyMessage(NO_SINGLE_GROUP_DONE);
                                }
                            } else {
                                handler.sendEmptyMessage(NO_SINGLE_GROUP_DONE);
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
                    if (isDebug) Slog.d(TAG, "==========response text : " + responseText);
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
                        MeetSingleGroupFragment.SingleGroup singleGroup = getSingleGroup(group, true);
                        mSingleGroupList.add(singleGroup);
                    }
                }
            }
        }

        return singleGroupSize;
    }

    private void setSingleGroupHeader() {
        View singleGroupView = LayoutInflater.from(getContext()).inflate(R.layout.recommend_group, (ViewGroup) findViewById(android.R.id.content), false);
        recyclerView.addHeaderView(singleGroupView);
        TextView title = singleGroupView.findViewById(R.id.group_recommend_title);
        title.setText(getContext().getResources().getString(R.string.matchmaker));
        LinearLayout groupWrapper = singleGroupView.findViewById(R.id.group_wrapper);
        if (groupWrapper == null) {
            return;
        }

        int size = mSingleGroupList.size();
        for (int i = 0; i < size; i++) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.single_group_item, null);
            groupWrapper.addView(view);

            if (size > 5 && i == size - 1) {
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

            final MeetSingleGroupFragment.SingleGroup singleGroup = mSingleGroupList.get(i);
            /*
            TextView groupName = view.findViewById(R.id.group_name);
            groupName.setText(singleGroup.groupName);
            */

            RoundImageView avatar = view.findViewById(R.id.avatar);
            String logo = singleGroup.leader.getAvatar();
            if (logo != null && !"".equals(logo)) {
                Glide.with(getContext()).load(HttpUtil.DOMAIN + logo).into(avatar);
            }

            TextView leaderName = view.findViewById(R.id.name);
            leaderName.setText(singleGroup.leader.getNickName());
            TextView university = view.findViewById(R.id.university);
            university.setText(singleGroup.leader.getUniversity());

            TextView memberCount = view.findViewById(R.id.group_member);
            memberCount.setText("成员 " + singleGroup.memberCount);

            TextView evaluateCount = view.findViewById(R.id.evaluate_count);
            evaluateCount.setText("评价 " + singleGroup.evaluateCount);

            view.setOnClickListener(new View.OnClickListener() {
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

    private void processNewAddResponse(JSONObject subGroupResponse) {
        JSONObject subGroupObject = null;
        if (subGroupResponse != null) {
            subGroupObject = subGroupResponse.optJSONObject("group");
        }

        if (subGroupObject != null) {
            SubGroup singleGroup = getSubGroup(subGroupObject);
            mSubGroupList.add(0, singleGroup);
            handler.sendEmptyMessage(ADD_NEW_SUBGROUP_DONE);
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
                recyclerView.refreshComplete();
                // recyclerView.loadMoreComplete();
                stopLoadProgress();
                break;
            case GET_ALL_END:
                Slog.d(TAG, "-------------->GET_ALL_END");
                subGroupSummaryAdapter.setData(mSubGroupList);
                subGroupSummaryAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();
                recyclerView.loadMoreComplete();
                recyclerView.setNoMore(true);
                stopLoadProgress();
                break;
            case NO_MORE:
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
                recyclerView.refreshComplete();
                break;
            case GET_SINGLE_GROUP_DONE:
                setSingleGroupHeader();
                loadData();
                break;
            case NO_SINGLE_GROUP_DONE:
                loadData();
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

    private void getMyNewAddedGroup(int gid) {
        RequestBody requestBody = new FormBody.Builder()
                .add("gid", String.valueOf(gid))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_SUBGROUP_BY_GID, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (isDebug) Slog.d(TAG, "==========response body : " + response.body());

                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject subGroupResponse = null;
                        try {
                            subGroupResponse = new JSONObject(responseText);
                            if (subGroupResponse != null) {
                                processNewAddResponse(subGroupResponse);
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
                        getMyNewAddedGroup(gid);
                    }
                    break;
            }

        }
    }

}
             
