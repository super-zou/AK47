package com.hetang.group;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hetang.R;
import com.hetang.adapter.MeetSingleGroupSummaryAdapter;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.common.MyApplication;
import com.hetang.common.SetAvatarActivity;
import com.hetang.meet.UserMeetInfo;
import com.hetang.talent.TalentAuthenticationDialogFragment;
import com.hetang.util.CommonDialogFragmentInterface;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.MyLinearLayoutManager;
import com.hetang.util.ParseUtils;
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
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.hetang.archive.ArchiveFragment.SET_AVATAR_RESULT_OK;
import static com.hetang.common.MyApplication.getContext;
import static com.hetang.group.GroupFragment.eden_group;
import static com.hetang.group.SingleGroupDetailsActivity.GET_SINGLE_GROUP_BY_GID;
import static com.hetang.group.SubGroupActivity.ADD_NEW_TALENT_DONE;
import static com.hetang.group.SubGroupActivity.GROUP_ADD_BROADCAST;
import static com.hetang.talent.TalentAuthenticationDialogFragment.COMMON_TALENT_AUTHENTICATION_RESULT_OK;
import static com.hetang.talent.TalentAuthenticationDialogFragment.TALENT_AUTHENTICATION_RESULT_OK;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;
import static com.hetang.util.DateUtil.timeStampToDay;

public class SingleGroupActivity extends BaseAppCompatActivity implements CommonDialogFragmentInterface {
    private static final boolean isDebug = true;
    private static final String TAG = "SingleGroupActivity";
    private static final int PAGE_SIZE = 10;
    public static final String SINGLE_GROUP_GET_MY = HttpUtil.DOMAIN + "?q=single_group/get_my";
    private static final String SINGLE_GROUP_GET_ALL = HttpUtil.DOMAIN + "?q=single_group/get_all";
    private static final String SINGLE_GROUP_UPDATE = HttpUtil.DOMAIN + "?q=single_group/update";
    private static final int GET_ALL_DONE = 1;
    private static final int UPDATE_ALL = 2;
    private static final int GET_ALL_END = 3;
    private static final int NO_UPDATE = 4;
    private static final int SET_AVATAR = 5;
    private static final int NO_MORE = 6;
    public static final int GET_MY_GROUP_DONE = 7;
    public static final int GET_TALENT_DONE = 15;
    final int itemLimit = 3;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    View mMyGroupView;
    private int mLoadSize = 0;
    private int mUpdateSize = 0;
    private Handler handler;
    private MeetSingleGroupSummaryAdapter meetSingleGroupSummaryAdapter;
    private XRecyclerView recyclerView;
    private List<SingleGroup> mSingleGroupList = new ArrayList<>();
    private List<SingleGroup> mLeadGroupList = new ArrayList<>();
    private List<SingleGroup> mJoinGroupList = new ArrayList<>();
    public static int groupType = eden_group;
    private ViewGroup myGroupView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meet_single_group_summary);
        handler = new SingleGroupActivity.MyHandler(this);

        initView();

        Button floatingActionButton = findViewById(R.id.become_talent);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                becomeTalent();
            }
        });

        TextView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //registerLoginBroadcast();

        //show progressImage before loading done
        progressImageView = findViewById(R.id.animal_progress);
        animationDrawable = (AnimationDrawable) progressImageView.getDrawable();
        progressImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        }, 50);

        loadData();
        //getMySingleGroup();
    }

    private void initView() {
        recyclerView = findViewById(R.id.single_group_summary_list);
        meetSingleGroupSummaryAdapter = new MeetSingleGroupSummaryAdapter(getContext());
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
                    meetSingleGroupSummaryAdapter.setScrolling(false);
                    meetSingleGroupSummaryAdapter.notifyDataSetChanged();
                } else {
                    meetSingleGroupSummaryAdapter.setScrolling(true);
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

        meetSingleGroupSummaryAdapter.setItemClickListener(new MeetSingleGroupSummaryAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Slog.d(TAG, "==========click : " + position);
                Intent intent = new Intent(getContext(), SingleGroupDetailsActivity.class);
                intent.putExtra("gid", mSingleGroupList.get(position).gid);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(meetSingleGroupSummaryAdapter);

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.custom_actionbar), font);
    }

    protected void loadData() {

        final int page = mSingleGroupList.size() / PAGE_SIZE;
        RequestBody requestBody = new FormBody.Builder()
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), SINGLE_GROUP_GET_ALL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject SingleGroupResponse = null;

                        try {
                            SingleGroupResponse = new JSONObject(responseText);
                            if (SingleGroupResponse != null) {
                                mLoadSize = processResponse(SingleGroupResponse);

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

    
    private int processResponse(JSONObject SingleGroupResponse) {

        int singGroupSize = 0;
        JSONArray SingleGroupArray = null;

        if (SingleGroupResponse != null) {
            SingleGroupArray = SingleGroupResponse.optJSONArray("single_group");
        }

        if (SingleGroupArray != null) {
            singGroupSize = SingleGroupArray.length();
            if (singGroupSize > 0) {
                for (int i = 0; i < SingleGroupArray.length(); i++) {
                    JSONObject group = SingleGroupArray.optJSONObject(i);
                    if (group != null) {
                        SingleGroup singleGroup = getSingleGroup(group, false);
                        mSingleGroupList.add(singleGroup);
                    }
                }
            }
        }

        return singGroupSize;
    }

    private int processUpdateResponse(JSONObject SingleGroupResponse) {
        List<SingleGroup> mSingleGroupUpdateList = new ArrayList<>();
        JSONArray SingleGroupArray = null;
        if (SingleGroupResponse != null) {
            SingleGroupArray = SingleGroupResponse.optJSONArray("single_group");
        }

        if (SingleGroupArray != null) {
            if (SingleGroupArray.length() > 0) {
                mSingleGroupUpdateList.clear();
                for (int i = 0; i < SingleGroupArray.length(); i++) {
                    JSONObject group = SingleGroupArray.optJSONObject(i);
                    if (group != null) {
                        SingleGroup singleGroup = getSingleGroup(group, false);
                        mSingleGroupUpdateList.add(singleGroup);
                    }
                }
                mSingleGroupList.addAll(0, mSingleGroupUpdateList);
                Message message = new Message();
                message.what = UPDATE_ALL;
                Bundle bundle = new Bundle();
                bundle.putInt("update_size", mSingleGroupUpdateList.size());
                message.setData(bundle);
                handler.sendMessage(message);
            } else {
                handler.sendEmptyMessage(GET_ALL_END);
            }
        }

        return SingleGroupArray != null ? SingleGroupArray.length() : 0;
    }

    public static SingleGroup getSingleGroup(JSONObject group, boolean isSummary) {
        SingleGroup singleGroup = new SingleGroup();
        if (group != null) {
            singleGroup.gid = group.optInt("gid");
            singleGroup.introduction = group.optString("introduction");
            singleGroup.created = timeStampToDay(group.optInt("created"));
            singleGroup.memberCount = group.optInt("member_count");
            singleGroup.maleCount = group.optInt("male_count");
            singleGroup.femaleCount = group.optInt("female_count");
            singleGroup.authorStatus = group.optInt("author_status");
            singleGroup.isLeader = group.optBoolean("isLeader");
            singleGroup.evaluateScores = (float) group.optDouble("scores");
            singleGroup.evaluateCount = group.optInt("count");
            JSONArray memberArray = group.optJSONArray("members");


            if (memberArray != null) {
                int count = memberArray.length();
                if (count > 0) {
                    singleGroup.memberList = new ArrayList<>();
                    for (int n = 0; n < count; n++) {
                        UserMeetInfo userMeetInfo = new UserMeetInfo();
                        userMeetInfo.setUid(memberArray.optJSONObject(n).optInt("uid"));
                        userMeetInfo.setNickName(memberArray.optJSONObject(n).optString("nickname"));
                        userMeetInfo.setAvatar(memberArray.optJSONObject(n).optString("avatar"));
                        userMeetInfo.setMajor(memberArray.optJSONObject(n).optString("major"));
                        userMeetInfo.setDegree(memberArray.optJSONObject(n).optString("degree"));
                        userMeetInfo.setUniversity(memberArray.optJSONObject(n).optString("university"));
                        userMeetInfo.setSex(memberArray.optJSONObject(n).optInt("sex"));
                        if (!isSummary) {
                            userMeetInfo.setBirthYear(memberArray.optJSONObject(n).optInt("birth_year"));
                            userMeetInfo.setHeight(memberArray.optJSONObject(n).optInt("height"));
                            userMeetInfo.setLiving(memberArray.optJSONObject(n).optString("living"));
                        }
                        singleGroup.memberList.add(userMeetInfo);
                    }
                }
            }
            singleGroup.leader = new UserMeetInfo();
            ParseUtils.setBaseProfile(singleGroup.leader, group);

            return singleGroup;
        }
        return null;
    }


    private void becomeTalent() {
        TalentAuthenticationDialogFragment talentAuthenticationDialogFragment = new TalentAuthenticationDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("type", groupType);
        talentAuthenticationDialogFragment.setArguments(bundle);
        //createSingleGroupDialogFragment.setTargetFragment(SingleGroupActivity.this, REQUEST_CODE);
        talentAuthenticationDialogFragment.show(getSupportFragmentManager(), "TalentAuthenticationDialogFragment");
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
                                        showSingleGroupDialog();
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

    private void showSingleGroupDialog() {
        CreateSubGroupDialogFragment createSingleGroupDialogFragment = new CreateSubGroupDialogFragment();
        //createSingleGroupDialogFragment.setTargetFragment(SingleGroupActivity.this, REQUEST_CODE);
        createSingleGroupDialogFragment.show(getSupportFragmentManager(), "CreateSubGroupDialogFragment");
    }

    public void updateData() {
        String last = SharedPreferencesUtils.getSingleGroupLast(getContext());
        RequestBody requestBody = new FormBody.Builder()
                .add("last", last)
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(0))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), SINGLE_GROUP_UPDATE, requestBody, new Callback() {
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
                meetSingleGroupSummaryAdapter.setData(mSingleGroupList, recyclerView.getWidth());
                meetSingleGroupSummaryAdapter.notifyDataSetChanged();
                //recyclerView.refreshComplete();
                // recyclerView.loadMoreComplete();
                stopLoadProgress();
                break;
            case GET_ALL_END:
                meetSingleGroupSummaryAdapter.setData(mSingleGroupList, recyclerView.getWidth());
                meetSingleGroupSummaryAdapter.notifyDataSetChanged();
                //recyclerView.refreshComplete();
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
                meetSingleGroupSummaryAdapter.setData(mSingleGroupList, recyclerView.getWidth());
                meetSingleGroupSummaryAdapter.notifyItemRangeInserted(0, updateSize);
                meetSingleGroupSummaryAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();
                break;
            case NO_UPDATE:
                recyclerView.refreshComplete();
                mUpdateSize = 0;
                break;
            case SET_AVATAR:
                startAvatarSetActivity();
                break;
                        case ADD_NEW_TALENT_DONE:
                meetSingleGroupSummaryAdapter.setData(mSingleGroupList, recyclerView.getWidth());
                meetSingleGroupSummaryAdapter.notifyItemInserted(0);
                meetSingleGroupSummaryAdapter.notifyDataSetChanged();
                if (mSingleGroupList.size() <= PAGE_SIZE){
                    recyclerView.loadMoreComplete();
                }
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

    private void startAvatarSetActivity() {
        final AlertDialog.Builder normalDialogBuilder =
                new AlertDialog.Builder(this);
        normalDialogBuilder.setTitle(getResources().getString(R.string.avatar_set_request_title));
        normalDialogBuilder.setMessage(getResources().getString(R.string.avatar_set_request_content));
        normalDialogBuilder.setPositiveButton("去设置->",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getContext(), SetAvatarActivity.class);
                        startActivityForResult(intent, Activity.RESULT_FIRST_USER);
                    }
                });

        normalDialogBuilder.setNegativeButton("关闭",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                    }
                });
        AlertDialog normalDialog = normalDialogBuilder.create();
        normalDialog.show();

        try {
            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object mAlertController = mAlert.get(normalDialog);
            Field mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
            mMessage.setAccessible(true);
            TextView mMessageView = (TextView) mMessage.get(mAlertController);
            mMessageView.setTextColor(getResources().getColor(R.color.background));
            mMessageView.setTextSize(16);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        normalDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.color_disabled));
        normalDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.color_blue));
        normalDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(18);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (isDebug)
            Slog.d(TAG, "===================onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode);
        if (requestCode == Activity.RESULT_FIRST_USER) {
            switch (resultCode) {
                case SET_AVATAR_RESULT_OK:
                    showSingleGroupDialog();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onBackFromDialog(int type, int result, boolean status) {
        switch (type) {
            case TALENT_AUTHENTICATION_RESULT_OK:
                if (status == true) {
                    getMyNewAddedTalent(result);
                }
                break;
            default:
                break;
        }
    }

    private void getMyNewAddedTalent(int gid) {
        RequestBody requestBody = new FormBody.Builder()
                .add("gid", String.valueOf(gid))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_SINGLE_GROUP_BY_GID, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (isDebug) Slog.d(TAG, "==========response body : " + response.body());

                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject singleGroupResponse = null;
                        try {
                            singleGroupResponse = new JSONObject(responseText);
                            if (singleGroupResponse != null) {
                                processNewAddResponse(singleGroupResponse);
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
    
        private void processNewAddResponse(JSONObject subGroupResponse) {
        JSONObject subGroupObject = null;
        if (subGroupResponse != null) {
            subGroupObject = subGroupResponse.optJSONObject("single_group");
        }

        if (subGroupObject != null) {
            SingleGroupActivity.SingleGroup singleGroup = getSingleGroup(subGroupObject, true);
            mSingleGroupList.add(0, singleGroup);
            handler.sendEmptyMessage(ADD_NEW_TALENT_DONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (recyclerView != null) {
            recyclerView.destroy();
            recyclerView = null;
        }
    }

    public static class SingleGroup {
        public int gid;
        public String introduction;
        public String created;
        public UserMeetInfo leader;
        public List<UserMeetInfo> memberList;
        public float evaluateScores = 0;
        public int evaluateCount = 0;
        public int memberCount = 0;
        public int maleCount = 0;
        public int femaleCount = 0;
        public int authorStatus = -1;
        public boolean isLeader;
    }

    static class MyHandler extends Handler {
        WeakReference<SingleGroupActivity> meetSingleGroupFragmentWeakReference;

        MyHandler(SingleGroupActivity singleGroupActivity) {
            meetSingleGroupFragmentWeakReference = new WeakReference<SingleGroupActivity>(singleGroupActivity);
        }

        @Override
        public void handleMessage(Message message) {
            SingleGroupActivity singleGroupActivity = meetSingleGroupFragmentWeakReference.get();
            if (singleGroupActivity != null) {
                singleGroupActivity.handleMessage(message);
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
                        getMyNewAddedTalent(gid);
                    }
                    break;
            }

        }
    }

}
    
