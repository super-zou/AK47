package com.hetang.meet;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.adapter.SubGroupDetailsListAdapter;
import com.hetang.adapter.SubGroupSummaryAdapter;
import com.hetang.archive.ArchiveActivity;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.common.MyApplication;
import com.hetang.util.CommonDialogFragmentInterface;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.InvitationDialogFragment;
import com.hetang.util.MyLinearLayoutManager;
import com.hetang.util.ParseUtils;
import com.hetang.util.RoundImageView;
import com.hetang.util.SetAvatarActivity;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.hetang.common.MyApplication.getContext;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

//import android.widget.GridLayout;


public class SubGroupDetailsActivity extends BaseAppCompatActivity implements CommonDialogFragmentInterface {
    private static final String TAG = "subGroupDetailsActivity";
    private static final boolean isDebug = true;
    private Context mContext;
    public static final String GET_SUBGROUP_BY_GID = HttpUtil.DOMAIN + "?q=subgroup/get_by_gid";
    private static final String APPLY_JOIN_SINGLE_GROUP = HttpUtil.DOMAIN + "?q=single_group/apply";
    public static final String ACCEPT_JOIN_SINGLE_GROUP = HttpUtil.DOMAIN + "?q=single_group/accept";
    public static final String APPROVE_JOIN_SINGLE_GROUP = HttpUtil.DOMAIN + "?q=single_group/approve";
    
    //MeetsubGroupFragment.subGroup subGroup;
    SubGroupActivity.SubGroup subGroup;
    private Handler handler = null;
    private static final int GET_DONE = 0;
    private static final int JOIN_DONE = 1;
    private static final int ACCEPT_DONE = 2;
    private static final int GET_GROUP_HEADER_DONE = 3;
    private int uid = -1;
    private int gid = -1;
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
        handler = new MyHandler(this);
        //getSubGroupByGid();
        initView();

    }
    
    private void initView() {

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
                //loadData();
            }
        });
        
        /*
        subGroupDetailsListAdapter.setItemClickListener(new SubGroupSummaryAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Slog.d(TAG, "==========click : " + position);
                Intent intent = new Intent(getContext(), SubGroupDetailsActivity.class);
                intent.putExtra("gid", mSubGroupList.get(position).gid);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });
        
 */
 mGroupHeaderView = LayoutInflater.from(mContext).inflate(R.layout.subgroup_details_header, (ViewGroup) findViewById(android.R.id.content), false);
        recyclerView.addHeaderView(mGroupHeaderView);

        recyclerView.setAdapter(subGroupDetailsListAdapter);

        /*
        FloatingActionButton floatingActionButton = findViewById(R.id.create_single_group);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //checkAvatarSet();
                //showsubGroupDialog();
            }
        });

         */
         
         // registerLoginBroadcast();
        TextView back = mGroupHeaderView.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mGroupHeaderView.findViewById(R.id.custom_actionbar), font);
        FontManager.markAsIconContainer(mGroupHeaderView.findViewById(R.id.subgroup_details_header), font);
        
        //show progressImage before loading done
        progressImageView = findViewById(R.id.animal_progress);
        animationDrawable = (AnimationDrawable)progressImageView.getDrawable();
        progressImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        },50);

        getSubGroupByGid();
    }
    
    private void getSubGroupByGid(){
        RequestBody requestBody = new FormBody.Builder()
                .add("gid", String.valueOf(gid))
                .build();

        HttpUtil.sendOkHttpRequest(this, GET_SUBGROUP_BY_GID, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(isDebug) Slog.d(TAG, "==========response body : " + response.body());

                if (response.body() != null) {
                    String responseText = response.body().string();
                    if(isDebug) Slog.d(TAG, "==========response text : " + responseText);
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

    private void processResponse(String response){
        JSONObject subGroupResponse = null;

        try {
            subGroupResponse = new JSONObject(response);
        }catch (JSONException e){
            e.printStackTrace();
        }
        
        subGroup = new SubGroupActivity.SubGroup();

        if(subGroupResponse != null){
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

            subGroup.leader = new UserMeetInfo();
            if (group.optJSONObject("leader") != null){
                ParseUtils.setBaseProfile(subGroup.leader, group.optJSONObject("leader"));
            }

            handler.sendEmptyMessage(GET_GROUP_HEADER_DONE);
        }

    }

    private void setSubGroupHeaderView(){
        Slog.d(TAG, "-------------------------->setSubGroupHeaderView");
        RoundImageView logoImage = mGroupHeaderView.findViewById(R.id.logo);
        TextView groupName = mGroupHeaderView.findViewById(R.id.group_name);
        TextView groupOrg = mGroupHeaderView.findViewById(R.id.org);
        TextView groupRegion = mGroupHeaderView.findViewById(R.id.region);
        TextView leaderName = mGroupHeaderView.findViewById(R.id.leader_name);
        RoundImageView leaderAvatar = mGroupHeaderView.findViewById(R.id.leader_avatar);
        TextView groupDesc = mGroupHeaderView.findViewById(R.id.group_desc);
        TextView browseAmount = mGroupHeaderView.findViewById(R.id.browse_amount);
        TextView activityAmount = mGroupHeaderView.findViewById(R.id.activity_amount);
        TextView followAmount = mGroupHeaderView.findViewById(R.id.follow_amount);
     
     if (subGroup.groupLogoUri != null && !"".equals(subGroup.groupLogoUri)) {
            Glide.with(mContext).load(HttpUtil.DOMAIN + subGroup.groupLogoUri).into(logoImage);
        }else {
            logoImage.setImageDrawable(mContext.getDrawable(R.drawable.icon));
        }

        groupName.setText(subGroup.groupName);
        groupOrg.setText(subGroup.org);
        groupRegion.setText(subGroup.region);
        leaderName.setText(subGroup.leader.getName());

        String avatar = subGroup.leader.getAvatar();
        if (avatar != null && !"".equals(avatar)) {
            Glide.with(mContext).load(HttpUtil.DOMAIN + avatar).into(leaderAvatar);
        }
        
         groupDesc.setText(subGroup.groupProfile);
        memberAmout.setText(String.valueOf(subGroup.memberCount));
        /*
        TextView back = mGroupHeaderView.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

         */

    }
    
    private void setSubGroupView(){

        uid = subGroup.leader.getUid();
        TextView leaderProfileDetails = findViewById(R.id.leader_profile_detail);
        leaderProfileDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SubGroupDetailsActivity.this, ArchiveActivity.class);
                intent.putExtra("uid", uid);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });
        
        LinearLayout joinWrap = findViewById(R.id.join_wrap);
        TextView join = findViewById(R.id.join);
        
        if(!subGroup.isLeader){
            switch (subGroup.authorStatus){
                case nonMEMBER:
                    join.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            checkMeetConditionSet();
                        }
                    });
                    break;
                    case APPLYING:
                    join.setText(getResources().getString(R.string.applied_wait));
                    join.setClickable(false);
                    break;
                    case INVITTED:
                    join.setText(getResources().getString(R.string.approvied));
                    join.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            accept();
                        }
                    });
                    break;
                    
                    case JOINED:
                    join.setText(getResources().getString(R.string.invite_friend));
                    join.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            invite();
                        }
                    });

                    break;
                    default:
                        break;
            }
            }else {
            join.setText(getResources().getString(R.string.invite_member));
            join.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    invite();
                }
            });
        }
        
        TextView title = findViewById(R.id.title);
        title.setText(subGroup.groupName);
        
        RoundImageView leaderHead = findViewById(R.id.head_uri);
        Glide.with(getContext()).load(HttpUtil.DOMAIN + subGroup.leader.getAvatar()).into(leaderHead);
        TextView leaderName = findViewById(R.id.leader_name);
        leaderName.setText(subGroup.leader.getName());
        TextView leaderProfile = findViewById(R.id.leader_profile);
        TextView leaderBaseProfile = findViewById(R.id.base_profile);
        TextView living = findViewById(R.id.living);

        if(subGroup.leader.getProfile() != null){
            leaderProfile.setText(subGroup.leader.getProfile());
        }
        
        if(subGroup.leader.getBaseProfile() != null){
            leaderBaseProfile.setText(subGroup.leader.getBaseProfile());
        }

        living.setText(subGroup.leader.getLiving().toString().trim());

        //TextView groupName = findViewById(R.id.group_name);
        //groupName.setText(subGroup.groupName);
        
        TextView memberCountView = findViewById(R.id.member_count);

        TextView groupOrg = findViewById(R.id.org);
        groupOrg.setText(subGroup.org);
        
        TextView groupProfile = findViewById(R.id.profile_detail);
        groupProfile.setText(subGroup.groupProfile);


        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.group_details_layout), font);
    }
    
    private void addMemberView(final UserMeetInfo userMeetInfo, boolean isNew){
        View view = LayoutInflater.from(this).inflate(R.layout.group_member, null);
        TextView memberName = view.findViewById(R.id.name);
        memberName.setText(userMeetInfo.getName());
        RoundImageView memberHead = view.findViewById(R.id.member_head);
        
        GridLayout.Spec rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
        GridLayout.Spec columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(rowSpec, columnSpec);
        
        if (isNew){
            gridLayout.addView(view, 0, layoutParams);
        }else {
            gridLayout.addView(view, layoutParams);
        }


        Glide.with(this).load(HttpUtil.DOMAIN + userMeetInfo.getAvatar()).into(memberHead);
        TextView birthYear = view.findViewById(R.id.age);
        birthYear.setText(String.valueOf(userMeetInfo.getAge()));
        
        TextView height = view.findViewById(R.id.height);
        height.setText(String.valueOf(userMeetInfo.getHeight()));
        
        TextView degree = view.findViewById(R.id.degree);
        degree.setText(userMeetInfo.getDegreeName(userMeetInfo.getDegree()));

        TextView memberLiving = view.findViewById(R.id.living);
        memberLiving.setText(userMeetInfo.getLiving());
        
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userMeetInfo.getCid() > 0){
                    ParseUtils.startMeetArchiveActivity(mContext, userMeetInfo.getUid());
                }else {
                    ParseUtils.startArchiveActivity(mContext, userMeetInfo.getUid());
                }
               // ParseUtils.startMeetArchiveActivity(subGroupDetailsActivity.this, userMeetInfo.getUid());
            }
        });
        
        }
        
        private void applyJoinGroup(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgressDialog("");
            }
        });

        RequestBody requestBody = new FormBody.Builder()
                .add("gid", String.valueOf(gid))
                .build();
                
        HttpUtil.sendOkHttpRequest(this, APPLY_JOIN_SINGLE_GROUP, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(isDebug) Slog.d(TAG, "==========response body : " + response.body());
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if(isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                    try {
                            memberJSONObject = new JSONObject(responseText).optJSONObject("response");
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                        
                        dismissProgressDialog();
                        handler.sendEmptyMessage(JOIN_DONE);
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
    private void checkMeetConditionSet(){
        RequestBody requestBody = new FormBody.Builder().build();
        HttpUtil.sendOkHttpRequest(this, ParseUtils.GET_USER_PROFILE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if(isDebug) Slog.d(TAG, "==========get archive response text : " + responseText);
                    if (responseText != null) {
                        if (!TextUtils.isEmpty(responseText)) {
                        try {
                                JSONObject jsonObject = new JSONObject(responseText).optJSONObject("user");
                                if(isDebug) Slog.d(TAG, "==============user profile object: "+jsonObject);
                                if(jsonObject != null){
                                    UserProfile userProfile = ParseUtils.getUserProfileFromJSONObject(jsonObject);
                                    final boolean isAvatarSet;
                                    if(!TextUtils.isEmpty(userProfile.getAvatar())){
                                        isAvatarSet = true;
                                    }else {
                                        isAvatarSet = false;
                                    }
                                    if(userProfile.getCid() == 0){//meet condition not set
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                showMeetConditionSetDialog(isAvatarSet);
                                            }
                                        });

                                    }else {
                                        applyJoinGroup();
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
    
    private void showMeetConditionSetDialog(final boolean isAvatarSet){
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(this, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
        normalDialog.setTitle("请设置交友信息");
        normalDialog.setMessage("需要先设置真实头像和交友信息");
        normalDialog.setPositiveButton("去设置",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    Intent intent;
                        if(isAvatarSet == false){
                            intent = new Intent(getContext(), SetAvatarActivity.class);
                            intent.putExtra("look_friend", true);
                        }else {
                            intent = new Intent(getContext(), FillMeetInfoActivity.class);
                        }
                        startActivity(intent);
                    }
                });
        normalDialog.setNegativeButton("关闭",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                    }
                });
        
        normalDialog.show();
    }
    
    private void invite(){
        InvitationDialogFragment invitationDialogFragment = new InvitationDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("gid", gid);
        Slog.d(TAG, "---------------------->invite gid: "+gid);
        bundle.putInt("type", ParseUtils.TYPE_SINGLE_GROUP);

        invitationDialogFragment.setArguments(bundle);
        invitationDialogFragment.show(getSupportFragmentManager(), "InvitationDialogFragment");
    }
    
    private void accept(){
        Slog.d(TAG, "=============accept");
        RequestBody requestBody = new FormBody.Builder()
                .add("gid", String.valueOf(gid))
                .build();
        HttpUtil.sendOkHttpRequest(this, ACCEPT_JOIN_SINGLE_GROUP, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
            if(isDebug) Slog.d(TAG, "==========response body : " + response.body());
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if(isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        handler.sendEmptyMessage(ACCEPT_DONE);
                        //refresh();
                    }
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
    
     private void approve(){
        RequestBody requestBody = new FormBody.Builder()
                .add("gid", String.valueOf(gid))
                .build();
    }
    
     public void handleMessage(Message message) {
        switch (message.what) {
            case GET_GROUP_HEADER_DONE:
                setSubGroupHeaderView();
                break;
                case GET_DONE:
                setSubGroupView();
                break;
            case JOIN_DONE:
            case ACCEPT_DONE:
                UserMeetInfo userMeetInfo = new UserMeetInfo();
                ParseUtils.setBaseProfile(userMeetInfo, memberJSONObject);
                addMemberView(userMeetInfo, true);
                TextView memberCountView = findViewById(R.id.member_count);
                TextView join = findViewById(R.id.join);
                join.setText(getResources().getString(R.string.applied_wait));
                join.setClickable(false);

                break;
            default:
                break;
        }
    }
    
    @Override
    public void onBackFromDialog(int type, int result, boolean status) {
        switch (type){
            case ParseUtils.TYPE_SINGLE_GROUP://For EvaluateDialogFragment back
                if(status == true){
                    //todo
                }
                break;
            default:
                break;
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
}
        
