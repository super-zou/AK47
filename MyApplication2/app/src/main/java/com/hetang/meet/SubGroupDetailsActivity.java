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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.adapter.SubGroupDetailsListAdapter;
import com.hetang.adapter.SubGroupSummaryAdapter;
import com.hetang.archive.ArchiveActivity;
import com.hetang.common.AddDynamicsActivity;
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


public class SubGroupDetailsActivity extends BaseAppCompatActivity {
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
    Button joinBtn;
    Button followBtn;
    boolean followed = false;
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

        FloatingActionButton floatingActionButton = findViewById(R.id.create_activity);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (subGroup.authorStatus > 0 || subGroup.followed > 0){
                    Intent intent = new Intent(MyApplication.getContext(), AddDynamicsActivity.class);
                    intent.putExtra("type", ParseUtils.ADD_SUBGROUP_ACTIVITY_ACTION);
                    intent.putExtra("gid", gid);
                    startActivityForResult(intent, Activity.RESULT_FIRST_USER);
                }else {
                    showNoticeDialog();
                }

            }
        });

        joinBtn = mGroupHeaderView.findViewById(R.id.join);

        followBtn = mGroupHeaderView.findViewById(R.id.follow);
         
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
            subGroup.followCount = group.optInt("follow_count");
            subGroup.activityCount = group.optInt("activity_count");
            subGroup.browseCount = group.optInt("browse_count");
            subGroup.authorStatus = group.optInt("status");
            subGroup.isLeader = group.optBoolean("isLeader");
            subGroup.followed = group.optInt("followed");
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
        final TextView followAmount = mGroupHeaderView.findViewById(R.id.follow_amount);
        TextView memberAmount = mGroupHeaderView.findViewById(R.id.member_amout);
     
     if (subGroup.groupLogoUri != null && !"".equals(subGroup.groupLogoUri)) {
            Glide.with(mContext).load(HttpUtil.DOMAIN + subGroup.groupLogoUri).into(logoImage);
        }else {
            logoImage.setImageDrawable(mContext.getDrawable(R.drawable.icon));
        }

        groupName.setText(subGroup.groupName);
        groupOrg.setText(subGroup.org);
        groupRegion.setText(subGroup.region);
        leaderName.setText(subGroup.leader.getName());
        
        if (subGroup.browseCount != 0){
            browseAmount.setText(String.valueOf(subGroup.browseCount));
        }

        if (subGroup.activityCount != 0){
            activityAmount.setText(String.valueOf(subGroup.activityCount));
        }

        if (subGroup.followCount != 0){
            followAmount.setText(String.valueOf(subGroup.followCount));
        }

        memberAmount.setText(String.valueOf(subGroup.memberCount));

        String avatar = subGroup.leader.getAvatar();
        if (avatar != null && !"".equals(avatar)) {
            Glide.with(mContext).load(HttpUtil.DOMAIN + avatar).into(leaderAvatar);
        }
        
        groupDesc.setText(subGroup.groupProfile);
        if (subGroup.authorStatus != -1){
            if(subGroup.authorStatus == 0){
                joinBtn.setVisibility(View.VISIBLE);
                joinBtn.setText(getResources().getString(R.string.applying));
                joinBtn.setClickable(false);
            }else {
                joinBtn.setText(getResources().getString(R.string.invite_friend));
                joinBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        invite();
                    }
                });
            }
        }else {
            joinBtn.setVisibility(View.VISIBLE);
            joinBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    applyJoinGroup();
                }
            });
        }
        
        if (subGroup.followed == 1){
            followed = true;
            followBtn.setText("已关注");
            followBtn.setBackground(MyApplication.getContext().getDrawable(R.drawable.btn_disable));
            followBtn.setTextColor(getResources().getColor(R.color.color_dark_grey));
        }else {
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
                    if(amount != null && !TextUtils.isEmpty(amount)){
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
                
        HttpUtil.sendOkHttpRequest(this, APPLY_JOIN_SUBGROUP, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(isDebug) Slog.d(TAG, "==========response body : " + response.body());
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if(isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                    try {
                            memberJSONObject = new JSONObject(responseText).optJSONObject("member");
                        }catch (JSONException e){
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
    
    private void showNoticeDialog(final boolean isAvatarSet){
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
    
    private void invite(){
        InvitationDialogFragment invitationDialogFragment = new InvitationDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("gid", gid);
        Slog.d(TAG, "---------------------->invite gid: "+gid);
        bundle.putInt("type", ParseUtils.TYPE_SINGLE_GROUP);

        invitationDialogFragment.setArguments(bundle);
        invitationDialogFragment.show(getSupportFragmentManager(), "InvitationDialogFragment");
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
                joinBtn.setText(getResources().getString(R.string.applying));
                joinBtn.setClickable(false);
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
        
