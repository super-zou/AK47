package com.hetang.group;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
//import android.widget.GridLayout;
import android.support.v7.widget.GridLayout;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.common.RewardDialogFragment;
import com.hetang.common.TalentEvaluateDialogFragment;
import com.hetang.common.TalentEvaluatorDetailsActivity;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.meet.FillMeetInfoActivity;
import com.hetang.meet.UserMeetInfo;
import com.hetang.util.CommonDialogFragmentInterface;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.common.InvitationDialogFragment;
import com.hetang.common.MyApplication;
import com.hetang.util.ParseUtils;
import com.hetang.util.RoundImageView;
import com.hetang.common.SetAvatarActivity;
import com.hetang.util.Slog;
import com.hetang.util.UserProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hetang.common.MyApplication.getContext;
import static com.hetang.common.TalentEvaluateDialogFragment.SET_EVALUATE_RESULT_OK;
import static com.hetang.group.GroupFragment.eden_group;
import static com.hetang.group.SingleGroupActivity.getSingleGroup;
import static com.hetang.group.SubGroupDetailsActivity.AUTHENTICATING;
import static com.hetang.group.SubGroupDetailsActivity.REJECTED;
import static com.hetang.group.SubGroupDetailsActivity.SHOW_NOTICE_DIALOG;
import static com.hetang.group.SubGroupDetailsActivity.UNAUTHENTICATED;
import static com.hetang.meet.FillMeetInfoActivity.FILL_MEET_INFO_BROADCAST;

import static com.hetang.util.ParseUtils.startMeetArchiveActivity;


public class SingleGroupDetailsActivity extends BaseAppCompatActivity implements CommonDialogFragmentInterface {
    private static final String TAG = "SingleGroupDetailsActivity";
    private static final boolean isDebug = true;
    private Context mContext;
    public static final String GET_SINGLE_GROUP_BY_GID = HttpUtil.DOMAIN + "?q=single_group/get_by_gid";
    private static final String APPLY_JOIN_SINGLE_GROUP = HttpUtil.DOMAIN + "?q=single_group/apply";
    public static final String ACCEPT_JOIN_SINGLE_GROUP = HttpUtil.DOMAIN + "?q=single_group/accept";
    public static final String APPROVE_JOIN_SINGLE_GROUP = HttpUtil.DOMAIN + "?q=single_group/approve";
    public static final String ACCEPT_SUBGROUP_INVITE = HttpUtil.DOMAIN + "?q=subgroup/accept";
        private static final String GET_EVALUATION_STATISTICS_URL = HttpUtil.DOMAIN + "?q=talent/evaluation/get_statistics";
    
    SingleGroupActivity.SingleGroup singleGroup;
    private Handler handler = null;
    private static final int GET_DONE = 0;
    private static final int JOIN_DONE = 1;
    private int status;
    private static final int ACCEPT_DONE = 2;
        private static final int GET_EVALUATION_DONE = 3;
    private int uid = -1;
    private int gid = -1;
    private Bundle savedInstanceState;
    private GridLayout gridLayout;
    private JSONObject memberJSONObject = new JSONObject();
        private SubGroupDetailsActivity subGroupDetailsActivity;
    private SingleGroupReceiver mReceiver = new SingleGroupReceiver();
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
        if (subGroupDetailsActivity == null){
            subGroupDetailsActivity = new SubGroupDetailsActivity();
        }
        
        registerBroadcast();

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
        TextView join = findViewById(R.id.join);
        
        if(!singleGroup.isLeader){
            switch (singleGroup.authorStatus){
                case nonMEMBER:
                    join.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            checkMeetConditionSet(true);
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
                            checkMeetConditionSet(false);
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

        //TextView title = findViewById(R.id.title);
        //title.setText(singleGroup.groupName);
        
        RoundImageView leaderHead = findViewById(R.id.avatar);
        Glide.with(MyApplication.getContext()).load(HttpUtil.DOMAIN + singleGroup.leader.getAvatar()).into(leaderHead);
        TextView leaderName = findViewById(R.id.leader_name);
        leaderName.setText(singleGroup.leader.getNickName());
        TextView university = findViewById(R.id.university);
        university.setText(singleGroup.leader.getUniversity());

        TextView maleCount = findViewById(R.id.male_count);
        maleCount.setText(getResources().getString(R.string.male)+" "+singleGroup.maleCount);
        TextView femaleCount = findViewById(R.id.female_count);
        femaleCount.setText(getResources().getString(R.string.female)+" "+singleGroup.femaleCount);

        TextView introduction = findViewById(R.id.introduction);
        introduction.setText(singleGroup.introduction);

        gridLayout = findViewById(R.id.member_summary);
        
        if(singleGroup.memberCount > 0){
            for (int i=0; i<singleGroup.memberCount; i++){
                addMemberView(singleGroup.memberList.get(i), false);
            }
        }

        Button contact = findViewById(R.id.contact_matchmaker);
        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contactTalentDialog();
            }
        });
        
        Button evaluate = findViewById(R.id.evaluate_matchmaker);
        evaluate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TalentEvaluateDialogFragment talentEvaluateDialogFragment = new TalentEvaluateDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("uid", uid);
                                bundle.putInt("gid", gid);
                bundle.putInt("type", eden_group);
                talentEvaluateDialogFragment.setArguments(bundle);
                talentEvaluateDialogFragment.show(getSupportFragmentManager(), "TalentEvaluateDialogFragment");
            }
        });
        
                getEvaluationStatistics();

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.group_details_layout), font);
    }
    
    private void getEvaluationStatistics(){
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .add("type", String.valueOf(eden_group))
            .add("gid", String.valueOf(gid))
                .build();

        HttpUtil.sendOkHttpRequest(this, GET_EVALUATION_STATISTICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(isDebug) Slog.d(TAG, "==========response body : " + response.body());

                if (response.body() != null) {
                    String responseText = response.body().string();
                    if(isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        processEvaluationResponse(responseText);
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void processEvaluationResponse(String response){
        try {
            JSONObject evaluateObject = new JSONObject(response);
            double scores = evaluateObject.optDouble("scores");
            int count = evaluateObject.optInt("count");
                        float score = 0;
            if (count != 0){
                float scoreFloat = (float) scores/count;
                score = (float)(Math.round(scoreFloat*10))/10;
            }
            Slog.d(TAG, "------------------>processEvaluationResponse: scores"+scores+"  score: "+score+ " count "+count);
            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putFloat("score", score);
            bundle.putInt("count", count);
            message.setData(bundle);
            message.what = GET_EVALUATION_DONE;
            handler.sendMessage(message);

        }catch (JSONException e){
            e.printStackTrace();
        }
    }
    
    private void setEvaluationView(final float score, int count){
        TextView evaluateTV = findViewById(R.id.evaluate);
        String textContent = "";
        if (count == 0){
            textContent = getResources().getString(R.string.no_evaluate);
        }else {
            textContent = score+"分"+getResources().getString(R.string.dot)+count+"条评论";
        }
        evaluateTV.setText(textContent);

        evaluateTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SingleGroupDetailsActivity.this, TalentEvaluatorDetailsActivity.class);
                intent.putExtra("uid", uid);
                intent.putExtra("type", eden_group);
                intent.putExtra("scores", score);
                intent.putExtra("gid", gid);
                startActivity(intent);
            }
        });

    }
    
    
    private void addMemberView(final UserMeetInfo userMeetInfo, boolean isNew){
        View view = LayoutInflater.from(this).inflate(R.layout.group_member, null);
        
                RoundImageView memberHead = view.findViewById(R.id.member_head);
        Glide.with(this).load(HttpUtil.DOMAIN + userMeetInfo.getAvatar()).into(memberHead);
        
        TextView memberName = view.findViewById(R.id.name);
        memberName.setText(userMeetInfo.getNickName());

        GridLayout.Spec rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
        GridLayout.Spec columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(rowSpec, columnSpec);
        
        if (isNew){
            gridLayout.addView(view, 0, layoutParams);
        }else {
            gridLayout.addView(view, layoutParams);
        }
        
        TextView university = view.findViewById(R.id.university);
        university.setText(userMeetInfo.getUniversity());


        TextView birthYear = view.findViewById(R.id.age);
        birthYear.setText(userMeetInfo.getAge()+getResources().getString(R.string.years_old));
        
        TextView height = view.findViewById(R.id.height);
        height.setText(String.valueOf(userMeetInfo.getHeight()));
        
        TextView degree = view.findViewById(R.id.degree);
        degree.setText(userMeetInfo.getDegreeName(userMeetInfo.getDegree()));

        TextView memberLiving = view.findViewById(R.id.living);
        memberLiving.setText(userMeetInfo.getLiving());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (singleGroup.isLeader || singleGroup.authorStatus == JOINED){
                    ParseUtils.startMeetArchiveActivity(mContext, userMeetInfo.getUid());
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            needJoinNotice();
                        }
                    });
                }
            }
        });
        
        }
    
    private void processResponse(String response){
        JSONObject singleGroupResponse = null;

        try {
            singleGroupResponse = new JSONObject(response).optJSONObject("single_group");
            if (singleGroupResponse != null){
                try {
                    singleGroup = getSingleGroup(singleGroupResponse, false);
                }catch (Exception e){
                    e.printStackTrace();
                }

                if (singleGroup != null){
                    handler.sendEmptyMessage(GET_DONE);
                }
            }
        }catch (JSONException e){
            e.printStackTrace();
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
    
    private void checkMeetConditionSet(final boolean apply){
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
                                    final UserProfile userProfile = ParseUtils.getUserProfileFromJSONObject(jsonObject);
                                    
                                    if(userProfile.getCid() == 0){//meet condition not set
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                showMeetConditionSetDialog(userProfile);
                                            }
                                        });

                                    }else {
                                         if (apply){
                                            applyJoinGroup();
                                        }else {
                                            accept();
                                        }
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
    
    private void contactTalentDialog(){
        RewardDialogFragment rewardDialogFragment = new RewardDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("uid", uid);
        bundle.putString("account", singleGroup.leader.getAccount());
        bundle.putInt("type", eden_group);

        rewardDialogFragment.setArguments(bundle);
        rewardDialogFragment.show(getSupportFragmentManager(), "RewardDialogFragment");
    }
    
    private void needJoinNotice(){
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(this, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
        normalDialog.setTitle("请加入团或者联系达人");
        normalDialog.setMessage("需要先加入团才能查看用户资料，也可以直接联系达人帮你牵线");
        normalDialog.setPositiveButton("联系达人",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        contactTalentDialog();
                    }
                });
        if (singleGroup.authorStatus == nonMEMBER){
            normalDialog.setNegativeButton("加入",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkMeetConditionSet(true);
                        }
                    });
        }
        
        normalDialog.setNeutralButton("取消",
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //...To-do
            }
        });


        normalDialog.show();
    }
    
    
    
    private void showMeetConditionSetDialog(final UserProfile userProfile){
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(this, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
        normalDialog.setTitle("请设置交友信息");
        normalDialog.setMessage("需要先设置真实头像并填写交友信息");
        normalDialog.setPositiveButton("去设置",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent;
                                                if(TextUtils.isEmpty(userProfile.getAvatar())){
                            intent = new Intent(MyApplication.getContext(), SetAvatarActivity.class);
                            intent.putExtra("look_friend", true);
                        }else {
                            intent = new Intent(MyApplication.getContext(), FillMeetInfoActivity.class);
                        }
                        intent.putExtra("userProfile", userProfile);
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
                                                try {
                            memberJSONObject = new JSONObject(responseText).optJSONObject("response");
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
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
                                TextView join = findViewById(R.id.join);
                join.setText(getResources().getString(R.string.applied_wait));
                join.setClickable(false);
                break;
            case ACCEPT_DONE:
                UserMeetInfo userMeetInfo = new UserMeetInfo();
                ParseUtils.setBaseProfile(userMeetInfo, memberJSONObject);
                addMemberView(userMeetInfo, true);
                                LinearLayout joinWrapper = findViewById(R.id.join_wrap);
                joinWrapper.setVisibility(View.GONE);
                singleGroup.authorStatus = JOINED;
                break;
                case GET_EVALUATION_DONE:
                Bundle bundle = message.getData();
                setEvaluationView(bundle.getFloat("score"), bundle.getInt("count"));
                break;
            case SHOW_NOTICE_DIALOG:
                Bundle data = message.getData();
                processStatus(data.getInt("status"));
                break;
            default:
                break;
        }
    }
    
        public void processStatus(int status) {
        Slog.d(TAG, "-----------processStatus status: " + status);
        switch (status) {
            case UNAUTHENTICATED:
                subGroupDetailsActivity.showNoticeDialog("未认证", "为了保证用户隐私及安全，交友模块需要身份认证", status);
                break;
            case AUTHENTICATING:
                subGroupDetailsActivity.showNoticeDialog("认证中", "您的身份认证还在审核中，请耐心等待", status);
                break;
            case REJECTED:
                subGroupDetailsActivity.showNoticeDialog("认证未通过", "您的身份认证审核未通过，请重新认证", status);
                break;
            default:
                break;

        }
    }

    @Override
    public void onBackFromDialog(int type, int result, boolean status) {
        switch (type){
            case SET_EVALUATE_RESULT_OK://For EvaluateDialogFragment back
                if(status == true){
                    //todo
                }
                break;
            default:
                break;
        }
    }
    
    private class SingleGroupReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case FILL_MEET_INFO_BROADCAST:
if (singleGroup.authorStatus == nonMEMBER){
                        applyJoinGroup();
                        Toast.makeText(mContext, "申请已发出，待达人审核", Toast.LENGTH_LONG).show();
                    }else if (singleGroup.authorStatus == INVITTED){
                        accept();
                        Toast.makeText(mContext, "已加入牵线团", Toast.LENGTH_LONG).show();
                    }                    break;
            }

        }
    }
    
    private void registerBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FILL_MEET_INFO_BROADCAST);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, intentFilter);
    }

    //unregister local broadcast
    private void unRegisterBroadcast() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterBroadcast();
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


                
