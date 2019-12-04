package com.hetang.meet;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
//import android.widget.GridLayout;
import android.support.v7.widget.GridLayout;

import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hetang.archive.ArchiveActivity;
import com.hetang.R;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.util.CommonDialogFragmentInterface;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.InvitationDialogFragment;
import com.hetang.common.MyApplication;
import com.hetang.util.ParseUtils;
import com.hetang.util.RoundImageView;
import com.hetang.util.SetAvatarActivity;
import com.hetang.util.Slog;
import com.hetang.util.UserProfile;
import com.hetang.util.Utility;

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

import static com.hetang.util.ParseUtils.startMeetArchiveActivity;


public class SingleGroupDetailsActivity extends BaseAppCompatActivity implements CommonDialogFragmentInterface {
    private static final String TAG = "SingleGroupDetailsActivity";
    private static final boolean isDebug = true;
    private Context mContext;
    public static final String GET_SINGLE_GROUP_BY_GID = HttpUtil.DOMAIN + "?q=single_group/get_by_gid";
    private static final String APPLY_JOIN_SINGLE_GROUP = HttpUtil.DOMAIN + "?q=single_group/apply";
    public static final String ACCEPT_JOIN_SINGLE_GROUP = HttpUtil.DOMAIN + "?q=single_group/accept";
    public static final String APPROVE_JOIN_SINGLE_GROUP = HttpUtil.DOMAIN + "?q=single_group/approve";
    
    MeetSingleGroupFragment.SingleGroup singleGroup;
    private Handler handler = null;
    private static final int GET_DONE = 0;
    private static final int JOIN_DONE = 1;
    private static final int ACCEPT_DONE = 2;
    private int uid = -1;
    private int gid = -1;
    private Bundle savedInstanceState;
    private GridLayout gridLayout;
    private JSONObject memberJSONObject = new JSONObject();
    private static final int nonMEMBER = -1;
    private static final int APPLYING = 0;
    private static final int INVITTED = 1;
    private static final int JOINED = 2;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        this.savedInstanceState = savedInstanceState;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_group_details);

        gid = getIntent().getIntExtra("gid", -1);
        handler = new MyHandler(this);
        getSingleGroupByGid();

    }
    
     private void getSingleGroupByGid(){
        RequestBody requestBody = new FormBody.Builder()
                .add("gid", String.valueOf(gid))
                .build();

        HttpUtil.sendOkHttpRequest(this, GET_SINGLE_GROUP_BY_GID, requestBody, new Callback() {
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
    
    private void setSingleGroupView(){

        TextView back = findViewById(R.id.left_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        
        uid = singleGroup.leader.getUid();
        TextView leaderProfileDetails = findViewById(R.id.leader_profile_detail);
        leaderProfileDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SingleGroupDetailsActivity.this, ArchiveActivity.class);
                intent.putExtra("uid", uid);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });
        
        LinearLayout joinWrap = findViewById(R.id.join_wrap);
        TextView join = findViewById(R.id.join);
        
        if(!singleGroup.isLeader){
            switch (singleGroup.authorStatus){
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
        title.setText(singleGroup.groupName);
        
        RoundImageView leaderHead = findViewById(R.id.head_uri);
        Glide.with(MyApplication.getContext()).load(HttpUtil.DOMAIN + singleGroup.leader.getAvatar()).into(leaderHead);
        TextView leaderName = findViewById(R.id.leader_name);
        leaderName.setText(singleGroup.leader.getName());
        TextView leaderProfile = findViewById(R.id.leader_profile);
        TextView leaderBaseProfile = findViewById(R.id.base_profile);
        TextView living = findViewById(R.id.living);

        if(singleGroup.leader.getProfile() != null){
            leaderProfile.setText(singleGroup.leader.getProfile());
        }
        
        if(singleGroup.leader.getBaseProfile() != null){
            leaderBaseProfile.setText(singleGroup.leader.getBaseProfile());
        }

        living.setText(singleGroup.leader.getLiving().toString().trim());

        //TextView groupName = findViewById(R.id.group_name);
        //groupName.setText(singleGroup.groupName);
        
        TextView memberCountView = findViewById(R.id.member_count);

        int memberCount = singleGroup.memberInfoList.size();
        if(memberCount > 0){
            memberCountView.setText(memberCount+getString(R.string.member_count_suffix));
        }

        TextView groupOrg = findViewById(R.id.org);
        groupOrg.setText(singleGroup.org);

        TextView groupProfile = findViewById(R.id.profile_detail);
        groupProfile.setText(singleGroup.groupProfile);

        gridLayout = findViewById(R.id.member_summary);
        
        if(memberCount > 0){
            for (int i=0; i<memberCount; i++){
                addMemberView(singleGroup.memberInfoList.get(i), false);
            }
        }

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
               // ParseUtils.startMeetArchiveActivity(SingleGroupDetailsActivity.this, userMeetInfo.getUid());
            }
        });
        
        }
    
    private void processResponse(String response){
        JSONObject singleGroupResponse = null;

        try {
            singleGroupResponse = new JSONObject(response);
        }catch (JSONException e){
            e.printStackTrace();
        }
        singleGroup = new MeetSingleGroupFragment.SingleGroup();

        if(singleGroupResponse != null){
            JSONObject group = singleGroupResponse.optJSONObject("single_group");
            singleGroup.gid = group.optInt("gid");
            singleGroup.groupName = group.optString("group_name");
            singleGroup.groupProfile = group.optString("group_profile");
            singleGroup.groupMarkUri = group.optString("group_mark_uri");
            singleGroup.org = group.optString("group_org");
            singleGroup.created = Utility.timeStampToDay(group.optInt("created"));
            singleGroup.authorStatus = group.optInt("author_status");
            singleGroup.isLeader = group.optBoolean("isLeader");
            if(isDebug) Slog.d(TAG, "=========================authorStatus: "+group.optInt("author_status")+
                    "   isLeader: "+group.optBoolean("isLeader"));
            JSONArray memberArray = group.optJSONArray("members");

            //singleGroup.leader = ParseUtils.getUserProfileFromJSONObject(group.optJSONObject("leader"));
            singleGroup.leader = new UserMeetInfo();
            JSONObject leaderObj = group.optJSONObject("leader");
            if (leaderObj != null){
                ParseUtils.setBaseProfile(singleGroup.leader, leaderObj);
            }

            singleGroup.memberInfoList = ParseUtils.getMeetInfoListFromJsonArray(memberArray);

            handler.sendEmptyMessage(GET_DONE);
        }

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
                            intent = new Intent(MyApplication.getContext(), SetAvatarActivity.class);
                            intent.putExtra("look_friend", true);
                        }else {
                            intent = new Intent(MyApplication.getContext(), FillMeetInfoActivity.class);
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
            case GET_DONE:
                setSingleGroupView();
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

                int memberCount = singleGroup.memberInfoList.size()+1;

                memberCountView.setText(memberCount+getString(R.string.member_count_suffix));

                Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
                FontManager.markAsIconContainer(findViewById(R.id.group_details_layout), font);
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
        WeakReference<SingleGroupDetailsActivity> singleGroupDetailsActivityWeakReference;

        MyHandler(SingleGroupDetailsActivity singleGroupDetailsActivity) {
            singleGroupDetailsActivityWeakReference = new WeakReference<SingleGroupDetailsActivity>(singleGroupDetailsActivity);
        }

        @Override
        public void handleMessage(Message message) {
            SingleGroupDetailsActivity singleGroupDetailsActivity = singleGroupDetailsActivityWeakReference.get();
            if (singleGroupDetailsActivity != null) {
                singleGroupDetailsActivity.handleMessage(message);
            }
        }
    }
}


                
