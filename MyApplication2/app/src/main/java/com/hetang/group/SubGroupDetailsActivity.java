package com.hetang.group;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.adapter.SubGroupDetailsListAdapter;
import com.hetang.authenticate.SubmitAuthenticationDialogFragment;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.common.MyApplication;
import com.hetang.common.SetAvatarActivity;
import com.hetang.dynamics.AddDynamicsActivity;
import com.hetang.dynamics.Dynamic;
import com.hetang.dynamics.DynamicOperationDialogFragment;
import com.hetang.dynamics.DynamicsInteractDetailsActivity;
import com.hetang.home.CommonContactsActivity;
import com.hetang.home.HomeFragment;
import com.hetang.meet.MeetDynamicsFragment;
import com.hetang.meet.UserMeetInfo;
import com.hetang.util.CommonDialogFragmentInterface;
import com.hetang.util.CommonUserListDialogFragment;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.InterActInterface;
import com.hetang.util.InvitationDialogFragment;
import com.hetang.util.MyLinearLayoutManager;
import com.hetang.util.ParseUtils;
import com.hetang.util.RoundImageView;
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
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.hetang.common.MyApplication.getContext;
import static com.hetang.common.SetAvatarActivity.MODIFY_LOGO;
import static com.hetang.common.SetAvatarActivity.MODIFY_SUBGROUP_LOGO_RESULT_OK;
import static com.hetang.group.GroupFragment.fraternity_group;
import static com.hetang.home.CommonContactsActivity.GROUP_MEMBER;
import static com.hetang.home.CommonContactsActivity.GROUP_MEMBER_CATEGRORY;
import static com.hetang.home.HomeFragment.GET_MY_NEW_ADD_DONE;
import static com.hetang.home.HomeFragment.GET_MY_NEW_ADD_DYNAMICS_URL;
import static com.hetang.meet.MeetDynamicsFragment.COMMENT_COUNT_UPDATE;
import static com.hetang.meet.MeetDynamicsFragment.DYNAMICS_DELETE;
import static com.hetang.meet.MeetDynamicsFragment.DYNAMICS_PRAISED;
import static com.hetang.meet.MeetDynamicsFragment.GET_DYNAMICS_URL;
import static com.hetang.meet.MeetDynamicsFragment.HAVE_UPDATE;
import static com.hetang.meet.MeetDynamicsFragment.LOAD_DYNAMICS_DONE;
import static com.hetang.meet.MeetDynamicsFragment.NO_MORE_DYNAMICS;
import static com.hetang.meet.MeetDynamicsFragment.NO_UPDATE;
import static com.hetang.meet.MeetDynamicsFragment.PRAISE_UPDATE;
import static com.hetang.meet.MeetDynamicsFragment.UPDATE_COMMENT;
import static com.hetang.util.ParseUtils.FEMALE;
import static com.hetang.util.ParseUtils.MALE;
import static com.hetang.util.ParseUtils.startMeetArchiveActivity;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

//import android.widget.GridLayout;


public class SubGroupDetailsActivity extends BaseAppCompatActivity implements CommonDialogFragmentInterface {
    public static final String GET_SUBGROUP_BY_GID = HttpUtil.DOMAIN + "?q=subgroup/get_by_gid";
    public static final String GET_AUTHENTICATION_STATUS = HttpUtil.DOMAIN + "?q=user_extdata/get_authentication_status";
    public static final String GROUP_MODIFY_BROADCAST = "com.hetang.action.GROUP_MODIFY";
    public static final String EXIT_GROUP_BROADCAST = "com.hetang.action.EXIT_GROUP";
    public static final String JOIN_GROUP_BROADCAST = "com.hetang.action.JOIN_GROUP";
    public static final String FOLLOW_GROUP_ACTION_URL = HttpUtil.DOMAIN + "?q=follow/group_action/";
    private static final String TAG = "subGroupDetailsActivity";
    private static final boolean isDebug = true;
    private static final String APPLY_JOIN_SUBGROUP = HttpUtil.DOMAIN + "?q=subgroup/apply";
    private static final int JOIN_DONE = 10;
    private static final int ACCEPT_DONE = 11;
    private static final int GET_GROUP_HEADER_DONE = 12;
    private static final int SHOW_NOTICE_DIALOG = 13;
    private static final int PAGE_SIZE = 6;
    private static final int UNAUTHENTICATED = -1;
    private static final int AUTHENTICATING = 0;
    private static final int VERIFIED = 1;
    private static final int REJECTED = 2;
    private static Handler handler = null;
    //MeetsubGroupFragment.subGroup subGroup;
    SubGroupActivity.SubGroup subGroup;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    View mGroupHeaderView;
    Button joinBtn;
    Button followBtn;
    boolean followed = false;
    MeetDynamicsFragment dynamicsFragment;
    TextView visitRecordTV;
    TextView activityAmount;
    TextView followAmount;
    TextView memberAmount;
    RoundImageView logoImage;
    SubGroupDetailsReceiver subGroupDetailsReceiver = new SubGroupDetailsReceiver();
    private Context mContext;
    private int uid = -1;
    private int gid = -1;
    private int type;
    private int currentPos = 0;
    private Bundle savedInstanceState;
    private GridLayout gridLayout;
    private JSONObject memberJSONObject = new JSONObject();
    private XRecyclerView recyclerView;
    private int mTempSize;
    private List<Dynamic> dynamicList = new ArrayList<>();
    private SubGroupDetailsListAdapter subGroupDetailsListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        this.savedInstanceState = savedInstanceState;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subgroup_details);

        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.group_details_layout), font);

        gid = getIntent().getIntExtra("gid", -1);
        type = getIntent().getIntExtra("type", -1);
        handler = new MyHandler(this);
        if (dynamicsFragment == null) {
            dynamicsFragment = new MeetDynamicsFragment();
        }
        //getSubGroupByGid();
        initView();

    }

    private void initView() {

        TextView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exit();
            }
        });

        TextView operation = findViewById(R.id.group_operation);
        operation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("subGroup", subGroup);
                SubGroupOperationDialogFragment subGroupOperationDialogFragment = new SubGroupOperationDialogFragment();
                subGroupOperationDialogFragment.setArguments(bundle);
                subGroupOperationDialogFragment.show(getSupportFragmentManager(), "SubGroupOperationDialogFragment");
            }
        });

        recyclerView = findViewById(R.id.subgroup_activity_list);
        subGroupDetailsListAdapter = new SubGroupDetailsListAdapter(getContext());
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
        recyclerView.setLimitNumberToCallLoadMore(2);
        recyclerView.setRefreshProgressStyle(ProgressStyle.BallBeat);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    subGroupDetailsListAdapter.setScrolling(false);
                    subGroupDetailsListAdapter.notifyDataSetChanged();
                } else {
                    subGroupDetailsListAdapter.setScrolling(true);
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

        mGroupHeaderView = LayoutInflater.from(mContext).inflate(R.layout.subgroup_details_header, (ViewGroup) findViewById(android.R.id.content), false);
        recyclerView.addHeaderView(mGroupHeaderView);

        //callback from meetDynamicsListAdapter, when comment icon touched, will show comment input dialog
        subGroupDetailsListAdapter.setOnCommentClickListener(new InterActInterface() {
            @Override
            public void onCommentClick(View view, int position) {
                currentPos = position;
                //createCommentDetails(meetList.get(position).getDid(), meetList.get(position).getCommentCount());
                createCommentDetails(dynamicList.get(position));

            }

            @Override
            public void onPraiseClick(View view, int position) {

                Bundle bundle = new Bundle();
                bundle.putInt("type", DYNAMICS_PRAISED);
                bundle.putLong("did", dynamicList.get(position).getDid());
                bundle.putString("title", "赞了该动态");
                CommonUserListDialogFragment commonUserListDialogFragment = new CommonUserListDialogFragment();
                commonUserListDialogFragment.setArguments(bundle);
                commonUserListDialogFragment.show(getSupportFragmentManager(), "CommonUserListDialogFragment");

            }

            @Override
            public void onDynamicPictureClick(View view, int position, String[] pictureUrlArray, int index) {
                dynamicsFragment.startPicturePreview(index, pictureUrlArray);
            }

            @Override
            public void onOperationClick(View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putLong("did", dynamicList.get(position).getDid());
                currentPos = position;
                DynamicOperationDialogFragment dynamicOperationDialogFragment = new DynamicOperationDialogFragment();
                dynamicOperationDialogFragment.setArguments(bundle);
                dynamicOperationDialogFragment.show(getSupportFragmentManager(), "DynamicOperationDialogFragment");
            }

        });

        recyclerView.setAdapter(subGroupDetailsListAdapter);

        FloatingActionButton floatingActionButton = findViewById(R.id.create_activity);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (type != fraternity_group) {
                            if (subGroup.authorStatus > 0 || subGroup.followed > 0) {
                                Intent intent = new Intent(MyApplication.getContext(), AddDynamicsActivity.class);
                                intent.putExtra("type", ParseUtils.ADD_SUBGROUP_ACTIVITY_ACTION);
                                intent.putExtra("gid", gid);
                                startActivityForResult(intent, Activity.RESULT_FIRST_USER);
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showNoticeDialog();
                                    }
                                });
                            }
                        } else {
                            int status = getAuthenticationStatus();
                            Slog.d(TAG, "--------------getAuthenticationStatus: " + status);
                            if (status == 1) {
                                if (subGroup.authorStatus > 0 || subGroup.followed > 0) {
                                    Intent intent = new Intent(MyApplication.getContext(), AddDynamicsActivity.class);
                                    intent.putExtra("type", ParseUtils.ADD_SUBGROUP_ACTIVITY_ACTION);
                                    intent.putExtra("gid", gid);
                                    startActivityForResult(intent, Activity.RESULT_FIRST_USER);
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showNoticeDialog();
                                        }
                                    });
                                }
                            } else {
                                Message message = Message.obtain();
                                Bundle bundle = new Bundle();
                                bundle.putInt("status", status);
                                message.setData(bundle);
                                message.what = SHOW_NOTICE_DIALOG;
                                handler.sendMessage(message);
                            }
                        }
                    }
                }).start();

            }
        });

        joinBtn = mGroupHeaderView.findViewById(R.id.join);

        followBtn = mGroupHeaderView.findViewById(R.id.follow);

        registerLoginBroadcast();
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.custom_actionbar), font);
        FontManager.markAsIconContainer(mGroupHeaderView.findViewById(R.id.subgroup_details_header), font);

        //show progressImage before loading done
        progressImageView = findViewById(R.id.animal_progress);
        animationDrawable = (AnimationDrawable) progressImageView.getDrawable();
        progressImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        }, 50);

        getSubGroupByGid();

        loadData();
    }

    public int getAuthenticationStatus() {
        int status = -1;
        RequestBody requestBody = new FormBody.Builder().build();
        Response response = HttpUtil.sendOkHttpRequestSync(getContext(), GET_AUTHENTICATION_STATUS, requestBody, null);
        if (response != null) {
            try {
                String responseText = response.body().string();
                if (isDebug) Slog.d(TAG, "==========response text : " + responseText);
                if (responseText != null && !TextUtils.isEmpty(responseText)) {
                    JSONObject statusResponse = new JSONObject(responseText);
                    status = statusResponse.optInt("status");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return status;
    }

    public void processStatus(int status) {
        Slog.d(TAG, "-----------processStatus status: " + status);
        switch (status) {
            case UNAUTHENTICATED:
                showNoticeDialog("未认证", "为了保证用户隐私及安全，交友模块需要身份认证", status);
                break;
            case AUTHENTICATING:
                showNoticeDialog("认证中", "您的身份认证还在审核中，请耐心等待", status);
                break;
            case REJECTED:
                showNoticeDialog("认证未通过", "您的身份认证审核未通过，请重新认证", status);
                break;
            default:
                break;

        }
    }


    public void createCommentDetails(Dynamic dynamic) {
        Intent intent = new Intent(MyApplication.getContext(), DynamicsInteractDetailsActivity.class);
        intent.putExtra("type", DynamicsInteractDetailsActivity.DYNAMIC_COMMENT);
        intent.putExtra("dynamic", dynamic);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivityForResult(intent, Activity.RESULT_FIRST_USER);
    }

    private void getSubGroupByGid() {
        RequestBody requestBody = new FormBody.Builder()
                .add("gid", String.valueOf(gid))
                .add("type", String.valueOf(type))
                .build();

        HttpUtil.sendOkHttpRequest(this, GET_SUBGROUP_BY_GID, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (isDebug) Slog.d(TAG, "==========response body : " + response.body());

                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        processResponse(responseText);
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void processResponse(String response) {
        JSONObject subGroupResponse = null;

        try {
            subGroupResponse = new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        subGroup = new SubGroupActivity.SubGroup();

        if (subGroupResponse != null) {
            JSONObject group = subGroupResponse.optJSONObject("group");
            subGroup = getSubGroup(group);
            handler.sendEmptyMessage(GET_GROUP_HEADER_DONE);
        }
    }

    private SubGroupActivity.SubGroup getSubGroup(JSONObject group) {
        if (group != null) {
            SubGroupActivity.SubGroup subGroup = new SubGroupActivity.SubGroup();
            subGroup.gid = group.optInt("gid");
            subGroup.type = group.optInt("type");
            subGroup.groupName = group.optString("group_name");
            subGroup.groupProfile = group.optString("group_profile");
            subGroup.groupLogoUri = group.optString("logo_uri");
            subGroup.org = group.optString("group_org");
            subGroup.region = group.optString("region");
            subGroup.memberCount = group.optInt("member_count");
            subGroup.created = Utility.timeStampToDay(group.optInt("created"));
            subGroup.followCount = group.optInt("follow_count");
            subGroup.activityCount = group.optInt("activity_count");
            subGroup.visitRecord = group.optInt("visit_record");
            subGroup.authorStatus = group.optInt("status");
            subGroup.isLeader = group.optBoolean("isLeader");
            subGroup.followed = group.optInt("followed");
            subGroup.leader = new UserMeetInfo();

            if (type == fraternity_group) {
                subGroup.maleCount = group.optInt("male_count");
                subGroup.femaleCount = group.optInt("female_count");
                try {
                    //for male
                    JSONArray maleArr = group.optJSONArray("males");
                    for (int i = 0; i < maleArr.length(); i++) {
                        UserProfile userProfile = new UserProfile();
                        JSONObject maleObject = maleArr.getJSONObject(i);
                        userProfile.setUid(maleObject.optInt("uid"));
                        userProfile.setAvatar(maleObject.optString("avatar"));
                        subGroup.maleList.add(userProfile);
                    }
                    //for female
                    JSONArray femaleArr = group.optJSONArray("females");
                    for (int i = 0; i < femaleArr.length(); i++) {
                        UserProfile userProfile = new UserProfile();
                        JSONObject maleObject = femaleArr.getJSONObject(i);
                        userProfile.setUid(maleObject.optInt("uid"));
                        userProfile.setAvatar(maleObject.optString("avatar"));
                        subGroup.femaleList.add(userProfile);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (group.optJSONObject("leader") != null) {
                ParseUtils.setBaseProfile(subGroup.leader, group.optJSONObject("leader"));
            }

            return subGroup;
        }
        return null;
    }

    private void setSubGroupHeaderView() {
        Slog.d(TAG, "-------------------------->setSubGroupHeaderView");
        logoImage = mGroupHeaderView.findViewById(R.id.logo);
        TextView groupName = mGroupHeaderView.findViewById(R.id.group_name);
        TextView groupOrg = mGroupHeaderView.findViewById(R.id.org);
        TextView groupRegion = mGroupHeaderView.findViewById(R.id.region);
        TextView leaderName = mGroupHeaderView.findViewById(R.id.leader_name);
        RoundImageView leaderAvatar = mGroupHeaderView.findViewById(R.id.leader_avatar);
        TextView groupDesc = mGroupHeaderView.findViewById(R.id.group_desc);
        TextView created = mGroupHeaderView.findViewById(R.id.created);
        HorizontalScrollView maleHorizontalScrollView = mGroupHeaderView.findViewById(R.id.maleHorizontalScrollView);
        LinearLayout maleWrapper = mGroupHeaderView.findViewById(R.id.male_wrapper);
        HorizontalScrollView femaleHorizontalScrollView = mGroupHeaderView.findViewById(R.id.femaleHorizontalScrollView);
        LinearLayout femaleWrapper = mGroupHeaderView.findViewById(R.id.female_wrapper);
        visitRecordTV = mGroupHeaderView.findViewById(R.id.visit_record);
        activityAmount = mGroupHeaderView.findViewById(R.id.activity_count);
        followAmount = mGroupHeaderView.findViewById(R.id.follow_count);
        memberAmount = mGroupHeaderView.findViewById(R.id.member_count);

        if (subGroup.groupLogoUri != null && !"".equals(subGroup.groupLogoUri)) {
            Glide.with(mContext).load(HttpUtil.DOMAIN + subGroup.groupLogoUri).into(logoImage);
        } else {
            logoImage.setImageDrawable(mContext.getDrawable(R.drawable.icon));
        }

        groupName.setText(subGroup.groupName);
        groupOrg.setText(subGroup.org);
        groupRegion.setText(subGroup.region);
        leaderName.setText(subGroup.leader.getNickName());

        created.setText("创建于 " + subGroup.created);

        visitRecordTV.setText(mContext.getResources().getString(R.string.visit) + " " + subGroup.visitRecord);
        activityAmount.setText(mContext.getResources().getString(R.string.dynamics) + " " + subGroup.activityCount);
        followAmount.setText(mContext.getResources().getString(R.string.follow) + " " + subGroup.followCount);
        memberAmount.setText(mContext.getResources().getString(R.string.member) + " " + subGroup.memberCount);

        String avatar = subGroup.leader.getAvatar();
        if (avatar != null && !"".equals(avatar)) {
            Glide.with(mContext).load(HttpUtil.DOMAIN + avatar).into(leaderAvatar);
        } else {
            if (subGroup.leader.getSex() == 0) {
                leaderAvatar.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.male_default_avator));
            } else {
                leaderAvatar.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.female_default_avator));
            }
        }

        groupDesc.setText(subGroup.groupProfile);

        if (type == fraternity_group) {
            if (subGroup.maleCount > 0) {
                maleHorizontalScrollView.setVisibility(View.VISIBLE);
                int size = subGroup.maleList.size();
                for (int i = 0; i < size; i++) {
                    View view = LayoutInflater.from(getContext()).inflate(R.layout.group_member_item, null);
                    maleWrapper.addView(view);
                    final RoundImageView avatarView = view.findViewById(R.id.avatar);
                    String avatarUri = subGroup.maleList.get(i).getAvatar();
                    if (avatarUri != "") {
                        Glide.with(getContext()).load(HttpUtil.DOMAIN + avatarUri).into(avatarView);
                    }
                    avatarView.setTag(subGroup.maleList.get(i).getUid());
                    if (subGroup.maleCount > 6 && i == size - 1) {
                        LinearLayout findMore = view.findViewById(R.id.find_more);
                        findMore.setVisibility(View.VISIBLE);
                        findMore.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        int status = getAuthenticationStatus();
                                        if (status == 1) {
                                            Intent intent = new Intent(MyApplication.getContext(), CommonContactsActivity.class);
                                            intent.putExtra("type", GROUP_MEMBER_CATEGRORY);
                                            intent.putExtra("sex", MALE);
                                            intent.putExtra("gid", gid);
                                            startActivity(intent);
                                        } else {
                                            Message message = Message.obtain();
                                            Bundle bundle = new Bundle();
                                            bundle.putInt("status", status);
                                            message.setData(bundle);
                                            message.what = SHOW_NOTICE_DIALOG;
                                            handler.sendMessage(message);
                                        }
                                    }
                                }).start();
                            }
                        });
                    }
                    avatarView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    int status = getAuthenticationStatus();
                                    if (status == 1) {
                                        startMeetArchiveActivity(SubGroupDetailsActivity.this, (int) avatarView.getTag());
                                    } else {
                                        Message message = Message.obtain();
                                        Bundle bundle = new Bundle();
                                        bundle.putInt("status", status);
                                        message.setData(bundle);
                                        message.what = SHOW_NOTICE_DIALOG;
                                        handler.sendMessage(message);
                                    }
                                }
                            }).start();
                        }
                    });

                }
            }

            if (subGroup.femaleCount > 0) {
                femaleHorizontalScrollView.setVisibility(View.VISIBLE);
                int size = subGroup.femaleList.size();
                for (int i = 0; i < size; i++) {
                    View view = LayoutInflater.from(getContext()).inflate(R.layout.group_member_item, null);
                    femaleWrapper.addView(view);
                    final RoundImageView avatarView = view.findViewById(R.id.avatar);
                    String avatarUri = subGroup.femaleList.get(i).getAvatar();
                    if (avatarUri != "") {
                        Glide.with(getContext()).load(HttpUtil.DOMAIN + avatarUri).into(avatarView);
                    }
                    avatarView.setTag(subGroup.femaleList.get(i).getUid());
                    if (subGroup.femaleCount > 6 && i == size - 1) {
                        LinearLayout findMore = view.findViewById(R.id.find_more);
                        findMore.setVisibility(View.VISIBLE);
                        findMore.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(MyApplication.getContext(), CommonContactsActivity.class);
                                intent.putExtra("type", GROUP_MEMBER_CATEGRORY);
                                intent.putExtra("sex", FEMALE);
                                intent.putExtra("gid", gid);
                                startActivity(intent);
                            }
                        });
                    }

                    avatarView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    int status = getAuthenticationStatus();
                                    if (status == 1) {
                                        startMeetArchiveActivity(SubGroupDetailsActivity.this, (int) avatarView.getTag());
                                    } else {
                                        Message message = Message.obtain();
                                        Bundle bundle = new Bundle();
                                        bundle.putInt("status", status);
                                        message.setData(bundle);
                                        message.what = SHOW_NOTICE_DIALOG;
                                        handler.sendMessage(message);
                                    }
                                }
                            }).start();
                        }
                    });
                }
            }
        }


        if (subGroup.authorStatus != -1) {
            if (subGroup.authorStatus == 0) {
                joinBtn.setVisibility(View.VISIBLE);
                joinBtn.setText(getResources().getString(R.string.approvied));
                joinBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        acceptSingleGroupInvite();
                    }
                });
            } else {
                joinBtn.setText(getResources().getString(R.string.invite_friend));
                joinBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        invite();
                    }
                });
            }
            followBtn.setVisibility(View.INVISIBLE);
        } else {
            joinBtn.setVisibility(View.VISIBLE);
            joinBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (type != fraternity_group){
                        applyJoinGroup();
                    }else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int status = getAuthenticationStatus();
                                if (status == 1) {
                                    applyJoinGroup();
                                } else {
                                    Message message = Message.obtain();
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("status", status);
                                    message.setData(bundle);
                                    message.what = SHOW_NOTICE_DIALOG;
                                    handler.sendMessage(message);
                                }
                            }
                        }).start();
                    }
                }
            });
        }
        if (subGroup.authorStatus == -1) {

            if (subGroup.followed == 1) {
                followed = true;
                followBtn.setText("已关注");
                followBtn.setBackground(MyApplication.getContext().getDrawable(R.drawable.btn_disable));
                followBtn.setTextColor(getResources().getColor(R.color.color_dark_grey));
            } else {
                followed = false;
                followBtn.setText("+关注");
                followBtn.setTextColor(getResources().getColor(R.color.blue_dark));
                followBtn.setBackground(MyApplication.getContext().getDrawable(R.drawable.btn_default));
            }
        }

        followBtn.setOnClickListener(new View.OnClickListener() {
            RequestBody requestBody = new FormBody.Builder()
                    .add("gid", String.valueOf(gid))
                    .add("uid", String.valueOf(uid))
                    .build();

            @Override
            public void onClick(View view) {
                String followUrl = "";
                String[] followSplit = followAmount.getText().toString().split(" ");
                int currentCount = Integer.parseInt(followSplit[1]);
                if (followed == true) {
                    followed = false;
                    followUrl = FOLLOW_GROUP_ACTION_URL + "cancel";
                    followBtn.setText("+关注");
                    followBtn.setTextColor(getResources().getColor(R.color.blue_dark));
                    followBtn.setBackground(MyApplication.getContext().getDrawable(R.drawable.btn_default));
                    followAmount.setText("关注 " + String.valueOf(currentCount - 1));
                    subGroup.followed = 0;
                } else {
                    followed = true;
                    followUrl = FOLLOW_GROUP_ACTION_URL + "add";
                    followBtn.setText("已关注");
                    followBtn.setBackground(MyApplication.getContext().getDrawable(R.drawable.btn_disable));
                    followBtn.setTextColor(getResources().getColor(R.color.color_dark_grey));
                    //String amount = followAmount.getText().toString();
                    //int currentCount = 0;
                    if (followSplit[1] != null && !TextUtils.isEmpty(followSplit[1])) {
                        currentCount = Integer.parseInt(followSplit[1]);
                    }
                    followAmount.setText("关注 " + String.valueOf(currentCount + 1));
                    subGroup.followed = 1;
                }

                HttpUtil.sendOkHttpRequest(MyApplication.getContext(), followUrl, requestBody, new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.body() != null) {
                            String responseText = response.body().string();
                            if (isDebug)
                                Slog.d(TAG, "==========get follow response text : " + responseText);
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                    }
                });
            }
        });

        memberAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (type != fraternity_group){
                            Intent intent = new Intent(getContext(), CommonContactsActivity.class);
                            intent.putExtra("type", GROUP_MEMBER);
                            intent.putExtra("gid", gid);
                            intent.putExtra("isLeader", subGroup.isLeader);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                            startActivityForResult(intent, RESULT_FIRST_USER);
                        }else {
                            int status = getAuthenticationStatus();
                            Slog.d(TAG, "--------------getAuthenticationStatus: " + status);
                            if (status == 1) {
                                Intent intent = new Intent(getContext(), CommonContactsActivity.class);
                                intent.putExtra("type", GROUP_MEMBER);
                                intent.putExtra("gid", gid);
                                intent.putExtra("isLeader", subGroup.isLeader);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                startActivityForResult(intent, RESULT_FIRST_USER);
                            } else {
                                Message message = Message.obtain();
                                Bundle bundle = new Bundle();
                                bundle.putInt("status", status);
                                message.setData(bundle);
                                message.what = SHOW_NOTICE_DIALOG;
                                handler.sendMessage(message);
                            }
                        }

                    }
                }).start();
            }
        });

        logoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (subGroup.isLeader) {
                    Intent intent = new Intent(getContext(), SetAvatarActivity.class);
                    intent.putExtra("type", MODIFY_LOGO);
                    intent.putExtra("gid", subGroup.gid);
                    startActivityForResult(intent, RESULT_FIRST_USER);
                }
            }
        });

        leaderName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMeetArchiveActivity(mContext, subGroup.leader.getUid());
            }
        });

        leaderAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMeetArchiveActivity(mContext, subGroup.leader.getUid());
            }
        });
    }

    private void applyJoinGroup() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgressDialog("");
            }
        });

        RequestBody requestBody = new FormBody.Builder()
                .add("gid", String.valueOf(gid))
                .build();

        HttpUtil.sendOkHttpRequest(this, APPLY_JOIN_SUBGROUP, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (isDebug) Slog.d(TAG, "==========response body : " + response.body());
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        try {
                            memberJSONObject = new JSONObject(responseText).optJSONObject("member");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        dismissProgressDialog();
                        handler.sendEmptyMessage(JOIN_DONE);
                        subGroup.authorStatus = 1;
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void acceptSingleGroupInvite() {
        Slog.d(TAG, "=============accept");
        RequestBody requestBody = new FormBody.Builder()
                .add("gid", String.valueOf(gid))
                .add("uid", String.valueOf(uid))
                .build();
        HttpUtil.sendOkHttpRequest(mContext, SingleGroupDetailsActivity.ACCEPT_SUBGROUP_INVITE, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Slog.d(TAG, "==========response body : " + response.body());
                if (response.body() != null) {
                    String responseText = response.body().string();
                    Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        //refresh();
                        handler.sendEmptyMessage(ACCEPT_DONE);
                        subGroup.authorStatus = 1;
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }


    private void showNoticeDialog() {
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(this, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
        normalDialog.setTitle("请先加入");
        normalDialog.setMessage("加入后才能发布信息");
        /*
        normalDialog.setPositiveButton("去设置",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                */
        normalDialog.setNegativeButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                    }
                });

        normalDialog.show();
    }

    private void invite() {
        InvitationDialogFragment invitationDialogFragment = new InvitationDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("gid", gid);
        Slog.d(TAG, "---------------------->invite gid: " + gid);
        bundle.putInt("type", ParseUtils.TYPE_SINGLE_GROUP);

        invitationDialogFragment.setArguments(bundle);
        invitationDialogFragment.show(getSupportFragmentManager(), "InvitationDialogFragment");
    }

    private void showNoticeDialog(String title, String content, final int status) {
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(this, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
        normalDialog.setTitle(title);
        normalDialog.setMessage(content);
        DialogInterface.OnClickListener listener = null;
        String positiveButtonText = "";
        if (status < 0) {
            positiveButtonText = "去认证";
            listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("type", 0);
                    bundle.putInt("gid", gid);
                    SubmitAuthenticationDialogFragment submitAuthenticationDialogFragment = new SubmitAuthenticationDialogFragment();
                    submitAuthenticationDialogFragment.setArguments(bundle);
                    submitAuthenticationDialogFragment.show(getSupportFragmentManager(), "SubmitAuthenticationDialogFragment");
                }
            };
        } else if (status == 0) {
            positiveButtonText = "确定";
        } else {
            positiveButtonText = "重新认证";
            listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("type", 1);
                    bundle.putInt("gid", gid);
                    SubmitAuthenticationDialogFragment submitAuthenticationDialogFragment = new SubmitAuthenticationDialogFragment();
                    submitAuthenticationDialogFragment.setArguments(bundle);
                    submitAuthenticationDialogFragment.show(getSupportFragmentManager(), "SubmitAuthenticationDialogFragment");
                }
            };
        }

        normalDialog.setPositiveButton(positiveButtonText, listener);

        normalDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                    }
                });

        normalDialog.show();
    }


    private void loadData() {

        final int page = dynamicList.size() / PAGE_SIZE;
        RequestBody requestBody = new FormBody.Builder()
                .add("type", String.valueOf(ParseUtils.ADD_SUBGROUP_ACTIVITY_ACTION))
                .add("gid", String.valueOf(gid))
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();

        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_DYNAMICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    //Slog.d(TAG, "==========response : "+response.body());
                    if (isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null) {
                        List<Dynamic> tempList = dynamicsFragment.getDynamicsResponse(responseText, false, handler);

                        mTempSize = 0;
                        if (null != tempList && tempList.size() > 0) {
                            // meetList.clear();
                            mTempSize = tempList.size();
                            if (page == 0) {
                                //load first page,so remove cache data
                                dynamicList.clear();
                                dynamicList.addAll(tempList);
                            } else {
                                dynamicList.addAll(tempList);
                            }
                            Log.d(TAG, "getResponseText list.size:" + tempList.size());
                            handler.sendEmptyMessage(LOAD_DYNAMICS_DONE);
                        } else {
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


    public void handleMessage(Message message) {
        switch (message.what) {
            case GET_GROUP_HEADER_DONE:
                setSubGroupHeaderView();
                break;
            case JOIN_DONE:
            case ACCEPT_DONE:
                joinBtn.setText(getResources().getString(R.string.joined));
                joinBtn.setClickable(false);
                followBtn.setVisibility(View.INVISIBLE);
                sendJoinGroupBroadcast();
                break;
            case NO_MORE_DYNAMICS:
                recyclerView.setNoMore(true);
                recyclerView.loadMoreComplete();
                stopLoadProgress();
                break;
            case LOAD_DYNAMICS_DONE:
                subGroupDetailsListAdapter.setData(dynamicList);
                subGroupDetailsListAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();
                if (mTempSize < PAGE_SIZE) {
                    //loading finished
                    recyclerView.loadMoreComplete();
                    recyclerView.setNoMore(true);
                }
                stopLoadProgress();
                break;
            case HAVE_UPDATE:
                //meetDynamicsListAdapter.setScrolling(false);
                subGroupDetailsListAdapter.setData(dynamicList);
                subGroupDetailsListAdapter.notifyItemRangeInserted(0, mTempSize);
                subGroupDetailsListAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();
                mTempSize = 0;
                break;
            case NO_UPDATE:
                mTempSize = 0;
                recyclerView.refreshComplete();
                break;
            case UPDATE_COMMENT:
                subGroupDetailsListAdapter.setData(dynamicList);
                subGroupDetailsListAdapter.notifyDataSetChanged();
                break;
            case COMMENT_COUNT_UPDATE:
                Bundle bundle = message.getData();
                int commentCount = bundle.getInt("commentCount");
                if (isDebug)
                    Slog.d(TAG, "------------------>COMMENT_COUNT_UPDATE: position: " + currentPos + " commentCount: " + commentCount);
                dynamicList.get(currentPos).setCommentCount(commentCount);
                subGroupDetailsListAdapter.setData(dynamicList);
                subGroupDetailsListAdapter.notifyDataSetChanged();
                break;
            case PRAISE_UPDATE:
                dynamicList.get(currentPos).setPraisedDynamicsCount(dynamicList.get(currentPos).getPraisedDynamicsCount() + 1);
                dynamicList.get(currentPos).setPraisedDynamics(1);
                subGroupDetailsListAdapter.setData(dynamicList);
                subGroupDetailsListAdapter.notifyDataSetChanged();
                break;
            case GET_MY_NEW_ADD_DONE:
                subGroupDetailsListAdapter.setData(dynamicList);
                subGroupDetailsListAdapter.notifyItemRangeInserted(0, 1);
                subGroupDetailsListAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();
                //update activity amount
                subGroup.activityCount += 1;
                activityAmount.setText(mContext.getResources().getString(R.string.dynamics) + " " + subGroup.activityCount);
                break;
            case DYNAMICS_DELETE:
                dynamicList.remove(currentPos);
                subGroupDetailsListAdapter.setData(dynamicList);
                subGroupDetailsListAdapter.notifyItemRemoved(currentPos);
                subGroupDetailsListAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();
                break;
            case SHOW_NOTICE_DIALOG:
                Bundle data = message.getData();
                processStatus(data.getInt("status"));
                break;
            default:
                break;
        }
    }

    private void sendJoinGroupBroadcast() {
        Intent intent = new Intent(JOIN_GROUP_BROADCAST);
        intent.putExtra("gid", gid);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    private void stopLoadProgress() {
        if (progressImageView.getVisibility() == View.VISIBLE) {
            animationDrawable.stop();
            progressImageView.setVisibility(View.GONE);

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Activity.RESULT_FIRST_USER) {
            switch (resultCode) {
                case HomeFragment.COMMENT_UPDATE_RESULT:
                    int commentCount = data.getIntExtra("commentCount", 0);
                    if (isDebug) Slog.d(TAG, "==========commentCount: " + commentCount);
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putInt("commentCount", commentCount);
                    msg.setData(bundle);
                    msg.what = COMMENT_COUNT_UPDATE;
                    handler.sendMessage(msg);
                    break;

                case HomeFragment.PRAISE_UPDATE_RESULT:
                    handler.sendEmptyMessage(PRAISE_UPDATE);
                    break;
                case HomeFragment.DYNAMICS_UPDATE_RESULT:
                    getMyNewActivity();
                    break;
                case MODIFY_SUBGROUP_LOGO_RESULT_OK:
                    String logoUri = data.getStringExtra("logoUri");
                    if (!TextUtils.isEmpty(logoUri)) {
                        Glide.with(this).load(HttpUtil.DOMAIN + logoUri).into(logoImage);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onBackFromDialog(int type, int result, boolean status) {
    }

    private void getMyNewActivity() {
        RequestBody requestBody = new FormBody.Builder()
                .add("type", String.valueOf(ParseUtils.ADD_SUBGROUP_ACTIVITY_ACTION))
                .add("pid", String.valueOf(gid))
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
                        try {
                            dynamicJSONObject = new JSONObject(responseText).optJSONObject("dynamic");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (dynamicJSONObject != null) {
                            Dynamic dynamic = dynamicsFragment.setMeetDynamics(dynamicJSONObject);
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

        recyclerView.scrollToPosition(0);
    }

    private void registerLoginBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GROUP_MODIFY_BROADCAST);
        intentFilter.addAction(EXIT_GROUP_BROADCAST);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(subGroupDetailsReceiver, intentFilter);
    }

    //unregister local broadcast
    private void unRegisterLoginBroadcast() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(subGroupDetailsReceiver);
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    private void exit() {
        Intent intent = new Intent();
        intent.putExtra("approved", true);
        setResult(RESULT_OK, intent);
        finish();
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

    static class MyHandler extends Handler {
        WeakReference<SubGroupDetailsActivity> subGroupDetailsActivityWeakReference;

        MyHandler(SubGroupDetailsActivity subGroupDetailsActivity) {
            subGroupDetailsActivityWeakReference = new WeakReference<>(subGroupDetailsActivity);
        }

        @Override
        public void handleMessage(Message message) {
            SubGroupDetailsActivity subGroupDetailsActivity = subGroupDetailsActivityWeakReference.get();
            if (subGroupDetailsActivity != null) {
                subGroupDetailsActivity.handleMessage(message);
            }
        }
    }

    private class SubGroupDetailsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case GROUP_MODIFY_BROADCAST:
                    getSubGroupByGid();
                    break;
                case EXIT_GROUP_BROADCAST:
                    getSubGroupByGid();
                    Toast.makeText(mContext, "成功退出", Toast.LENGTH_LONG).show();
                    break;
            }

        }
    }
}
        
