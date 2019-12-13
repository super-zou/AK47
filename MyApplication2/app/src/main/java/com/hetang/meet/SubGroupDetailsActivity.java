package com.hetang.meet;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.adapter.SubGroupDetailsListAdapter;
import com.hetang.common.AddDynamicsActivity;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.common.Dynamic;
import com.hetang.common.DynamicsInteractDetailsActivity;
import com.hetang.common.MyApplication;
import com.hetang.home.HomeFragment;
import com.hetang.util.CommonDialogFragmentInterface;
import com.hetang.util.CommonUserListDialogFragment;
import com.hetang.util.DynamicOperationDialogFragment;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.InterActInterface;
import com.hetang.util.InvitationDialogFragment;
import com.hetang.util.MyLinearLayoutManager;
import com.hetang.util.ParseUtils;
import com.hetang.util.PictureReviewDialogFragment;
import com.hetang.util.RoundImageView;
import com.hetang.util.Slog;
import com.hetang.util.SubGroupOperationDialogFragment;
import com.hetang.util.Utility;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

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
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

//import android.widget.GridLayout;


public class SubGroupDetailsActivity extends BaseAppCompatActivity implements CommonDialogFragmentInterface{
    private static final String TAG = "subGroupDetailsActivity";
    private static final boolean isDebug = true;
    private Context mContext;
    public static final String GET_SUBGROUP_BY_GID = HttpUtil.DOMAIN + "?q=subgroup/get_by_gid";
    private static final String APPLY_JOIN_SUBGROUP = HttpUtil.DOMAIN + "?q=subgroup/apply";
    public static final String ACCEPT_JOIN_SUBGROUP = HttpUtil.DOMAIN + "?q=subgroup/accept";
    public static final String APPROVE_JOIN_SUBGROUP = HttpUtil.DOMAIN + "?q=subgroup/approve";

    //MeetsubGroupFragment.subGroup subGroup;
    SubGroupActivity.SubGroup subGroup;
    private Handler handler = null;
    private static final int JOIN_DONE = 10;
    private static final int ACCEPT_DONE = 11;
    private static final int GET_GROUP_HEADER_DONE = 12;
    private int uid = -1;
    private int gid = -1;
    private static final int PAGE_SIZE = 6;
    private int currentPos = 0;
    private Bundle savedInstanceState;
    private GridLayout gridLayout;
    private JSONObject memberJSONObject = new JSONObject();
    private static final int nonMEMBER = -1;
    private static final int APPLYING = 0;
    private static final int INVITTED = 1;
    private static final int JOINED = 2;
    private XRecyclerView recyclerView;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    View mGroupHeaderView;
    Button joinBtn;
    Button followBtn;
    boolean followed = false;
    private int mTempSize;
    MeetDynamicsFragment dynamicsFragment;
    TextView visitRecordTV;
    TextView activityAmount;
    TextView followAmount;
    TextView memberAmount;
    private List<Dynamic> dynamicList = new ArrayList<>();
    private SubGroupDetailsListAdapter subGroupDetailsListAdapter;
    public static final String FOLLOW_GROUP_ACTION_URL = HttpUtil.DOMAIN + "?q=follow/group_action/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        this.savedInstanceState = savedInstanceState;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subgroup_details);

        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.group_details_layout), font);

        gid = getIntent().getIntExtra("gid", -1);
        handler = new MyHandler(this);
        if (dynamicsFragment == null){
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
            public void onPraiseClick(View view, int position){

                Bundle bundle = new Bundle();
                bundle.putInt("type", DYNAMICS_PRAISED);
                bundle.putLong("did", dynamicList.get(position).getDid());
                bundle.putString("title", "赞了该动态");
                CommonUserListDialogFragment commonUserListDialogFragment = new CommonUserListDialogFragment();
                commonUserListDialogFragment.setArguments(bundle);
                commonUserListDialogFragment.show(getSupportFragmentManager(), "CommonUserListDialogFragment");

            }
            @Override
            public void onDynamicPictureClick(View view, int position, String[] pictureUrlArray, int index){
                Bundle bundle = new Bundle();
                bundle.putInt("index", index);
                bundle.putStringArray("pictureUrlArray", pictureUrlArray);

                PictureReviewDialogFragment pictureReviewDialogFragment = new PictureReviewDialogFragment();
                pictureReviewDialogFragment.setArguments(bundle);
                pictureReviewDialogFragment.show(getSupportFragmentManager(), "PictureReviewDialogFragment");
            }

            @Override
            public void onOperationClick(View view, int position){
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
                if (subGroup.authorStatus > 0 || subGroup.followed > 0) {
                    Intent intent = new Intent(MyApplication.getContext(), AddDynamicsActivity.class);
                    intent.putExtra("type", ParseUtils.ADD_SUBGROUP_ACTIVITY_ACTION);
                    intent.putExtra("gid", gid);
                    startActivityForResult(intent, Activity.RESULT_FIRST_USER);
                } else {
                    showNoticeDialog();
                }

            }
        });

        joinBtn = mGroupHeaderView.findViewById(R.id.join);

        followBtn = mGroupHeaderView.findViewById(R.id.follow);

        // registerLoginBroadcast();
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
            if (group.optJSONObject("leader") != null) {
                ParseUtils.setBaseProfile(subGroup.leader, group.optJSONObject("leader"));
            }

            handler.sendEmptyMessage(GET_GROUP_HEADER_DONE);
        }

    }

    private void setSubGroupHeaderView() {
        Slog.d(TAG, "-------------------------->setSubGroupHeaderView");
        RoundImageView logoImage = mGroupHeaderView.findViewById(R.id.logo);
        TextView groupName = mGroupHeaderView.findViewById(R.id.group_name);
        TextView groupOrg = mGroupHeaderView.findViewById(R.id.org);
        TextView groupRegion = mGroupHeaderView.findViewById(R.id.region);
        TextView leaderName = mGroupHeaderView.findViewById(R.id.leader_name);
        RoundImageView leaderAvatar = mGroupHeaderView.findViewById(R.id.leader_avatar);
        TextView groupDesc = mGroupHeaderView.findViewById(R.id.group_desc);
        visitRecordTV = mGroupHeaderView.findViewById(R.id.visit_record);
        activityAmount = mGroupHeaderView.findViewById(R.id.activity_amount);
        followAmount = mGroupHeaderView.findViewById(R.id.follow_amount);
        memberAmount = mGroupHeaderView.findViewById(R.id.member_amout);

        if (subGroup.groupLogoUri != null && !"".equals(subGroup.groupLogoUri)) {
            Glide.with(mContext).load(HttpUtil.DOMAIN + subGroup.groupLogoUri).into(logoImage);
        } else {
            logoImage.setImageDrawable(mContext.getDrawable(R.drawable.icon));
        }

        groupName.setText(subGroup.groupName);
        groupOrg.setText(subGroup.org);
        groupRegion.setText(subGroup.region);
        leaderName.setText(subGroup.leader.getName());

        if (subGroup.visitRecord != 0) {
            visitRecordTV.setText(String.valueOf(subGroup.visitRecord));
        }

        if (subGroup.activityCount != 0) {
            activityAmount.setText(String.valueOf(subGroup.activityCount));
        }

        if (subGroup.followCount != 0) {
            followAmount.setText(String.valueOf(subGroup.followCount));
        }

        memberAmount.setText(String.valueOf(subGroup.memberCount));

        String avatar = subGroup.leader.getAvatar();
        if (avatar != null && !"".equals(avatar)) {
            Glide.with(mContext).load(HttpUtil.DOMAIN + avatar).into(leaderAvatar);
        }

        groupDesc.setText(subGroup.groupProfile);
        if (subGroup.authorStatus != -1) {
            if (subGroup.authorStatus == 0) {
                joinBtn.setVisibility(View.VISIBLE);
                joinBtn.setText(getResources().getString(R.string.applying));
                joinBtn.setClickable(false);
            } else {
                joinBtn.setText(getResources().getString(R.string.invite_friend));
                joinBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        invite();
                    }
                });
            }
        } else {
            joinBtn.setVisibility(View.VISIBLE);
            joinBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    applyJoinGroup();
                }
            });
        }

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

        followBtn.setOnClickListener(new View.OnClickListener() {
            RequestBody requestBody = new FormBody.Builder()
                    .add("gid", String.valueOf(gid))
                    .add("uid", String.valueOf(uid))
                    .build();

            @Override
            public void onClick(View view) {
                String followUrl = "";
                if (followed == true) {
                    followed = false;
                    followUrl = FOLLOW_GROUP_ACTION_URL + "cancel";
                    followBtn.setText("+关注");
                    followBtn.setTextColor(getResources().getColor(R.color.blue_dark));
                    followBtn.setBackground(MyApplication.getContext().getDrawable(R.drawable.btn_default));
                    int currentCount = Integer.parseInt(followAmount.getText().toString());
                    followAmount.setText(String.valueOf(currentCount - 1));
                    subGroup.followed = 0;
                } else {
                    followed = true;
                    followUrl = FOLLOW_GROUP_ACTION_URL + "add";
                    followBtn.setText("已关注");
                    followBtn.setBackground(MyApplication.getContext().getDrawable(R.drawable.btn_disable));
                    followBtn.setTextColor(getResources().getColor(R.color.color_dark_grey));
                    String amount = followAmount.getText().toString();
                    int currentCount = 0;
                    if (amount != null && !TextUtils.isEmpty(amount)) {
                        currentCount = Integer.parseInt(amount);
                    }
                    followAmount.setText(String.valueOf(currentCount + 1));
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
                        subGroup.authorStatus = 0;
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
        normalDialog.setTitle("请先关注或加入");
        normalDialog.setMessage("需要先关注或加入团，才能发布信息");
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
                        }else {
                            handler.sendEmptyMessage(NO_MORE_DYNAMICS);
                        }

                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {}
        });
    }


    public void handleMessage(Message message) {
        switch (message.what) {
            case GET_GROUP_HEADER_DONE:
                setSubGroupHeaderView();
                break;
            case JOIN_DONE:
            case ACCEPT_DONE:
                joinBtn.setText(getResources().getString(R.string.applying));
                joinBtn.setClickable(false);
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
                String amount = activityAmount.getText().toString();
                int currentCount = 0;
                if (amount != null && !TextUtils.isEmpty(amount)) {
                    currentCount = Integer.parseInt(amount);
                }
                activityAmount.setText(String.valueOf(currentCount + 1));
                break;
            case DYNAMICS_DELETE:
                dynamicList.remove(currentPos);
                subGroupDetailsListAdapter.setData(dynamicList);
                subGroupDetailsListAdapter.notifyItemRemoved(currentPos);
                subGroupDetailsListAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();
                break;
            default:
                break;
        }
    }

    private void stopLoadProgress(){
        if (progressImageView.getVisibility() == View.VISIBLE){
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
                default:
                    break;
            }
        }
    }
    
    @Override
    public void onBackFromDialog(int type, int result, boolean status) { }

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
    
    @Override
    public void onBackPressed(){
        exit();
    }

    private void exit(){
        Intent intent = new Intent();
        intent.putExtra("approved", true);
        setResult(RESULT_OK, intent);
        finish();
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
}
        
