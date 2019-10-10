package com.hetang.meet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
//import android.widget.GridLayout;
import android.support.v7.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hetang.launchak47.R;
import com.hetang.main.ArchiveActivity;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.ParseUtils;
import com.hetang.util.Slog;
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

import static com.hetang.util.ParseUtils.getMeetArchive;

public class SingleGroupDetailsActivity extends Activity {
    private static final String TAG = "SingleGroupDetailsActivity";
    private static final boolean isDebug = true;
    private Context mContext;
    private static final String GET_SINGLE_GROUP_BY_GID = HttpUtil.DOMAIN + "?q=single_group/get_by_gid";
    private static final String APPLY_JOIN_SINGLE_GROUP = HttpUtil.DOMAIN + "?q=single_group/apply";
    private static final String ACCEPT_JOIN_SINGLE_GROUP = HttpUtil.DOMAIN + "?q=single_group/accept";
    MeetSingleGroupFragment.SingleGroup singleGroup;
    private Handler handler = null;
    private static final int GET_DONE = 0;
    private static final int JOIN_DONE = 1;
    private static final int ACCEPT_DONE = 2;
    private int uid = -1;
    private int gid = -1;
    private Bundle savedInstanceState;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        this.savedInstanceState = savedInstanceState;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_group_details);

        /*
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
*/
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
            if(singleGroup.authorStatus == -1){
                joinWrap.setVisibility(View.VISIBLE);
                join.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        joinGroup();
                    }
                });
                }else if(singleGroup.authorStatus == 0){
                joinWrap.setVisibility(View.VISIBLE);
                join.setText(getResources().getString(R.string.applied_wait));
                join.setClickable(false);
            }else if(singleGroup.authorStatus == 1){
                joinWrap.setVisibility(View.VISIBLE);
                join.setText(getResources().getString(R.string.approvied));
                join.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        accept();
                    }
                });
            }
        }
        TextView title = findViewById(R.id.title);
        title.setText(singleGroup.groupName);

        ImageView leaderHead = findViewById(R.id.head_uri);
        Glide.with(mContext).load(HttpUtil.DOMAIN + singleGroup.leader.getPictureUri()).into(leaderHead);
        TextView leaderName = findViewById(R.id.leader_name);
        leaderName.setText(singleGroup.leader.getRealname());
        TextView leaderProfile = findViewById(R.id.leader_profile);
        TextView living = findViewById(R.id.living);

        if(singleGroup.leader.getProfile() != null){
            leaderProfile.setText(singleGroup.leader.getProfile());
        }
        
        if(singleGroup.leader.getSituation() == 0){
            TextView major = findViewById(R.id.major);
            TextView degree = findViewById(R.id.degree);
            TextView university = findViewById(R.id.university);

            major.setText(singleGroup.leader.getMajor());
            degree.setText(singleGroup.leader.getDegree());
            university.setText(singleGroup.leader.getUniversity());
        }else {
            LinearLayout education = findViewById(R.id.education);
            education.setVisibility(View.GONE);

            LinearLayout work = findViewById(R.id.work);
            work.setVisibility(View.VISIBLE);

            TextView job = findViewById(R.id.job);
            job.setText(singleGroup.leader.getJobTitle());
            TextView company = findViewById(R.id.company);
            company.setText(singleGroup.leader.getCompany());
        }
        
        living.setText(singleGroup.leader.getLives().toString().trim());

        TextView groupName = findViewById(R.id.group_name);
        groupName.setText(singleGroup.groupName);

        TextView memberCountView = findViewById(R.id.member_count);
        memberCountView.setText(String.valueOf(singleGroup.memberInfoList.size())+R.string.member_count_suffix);

        TextView groupOrg = findViewById(R.id.org);
        groupOrg.setText(singleGroup.org);

        TextView groupProfile = findViewById(R.id.profile_detail);
        groupProfile.setText(singleGroup.groupProfile);
        int memberCount = singleGroup.memberInfoList.size();
        GridLayout gridLayout = findViewById(R.id.member_summary);
        if(memberCount > 0){
            for (int i=0; i<memberCount; i++){
                View view = LayoutInflater.from(this).inflate(R.layout.group_member, null);
                TextView memberName = view.findViewById(R.id.name);
                memberName.setText(singleGroup.memberInfoList.get(i).getRealname());
                ImageView memberHead = view.findViewById(R.id.member_head);

                GridLayout.Spec rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
                GridLayout.Spec columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
                GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(rowSpec, columnSpec);

                gridLayout.addView(view, layoutParams);
                Glide.with(this).load(HttpUtil.DOMAIN + singleGroup.memberInfoList.get(i).getPictureUri()).into(memberHead);
                TextView birthYear = view.findViewById(R.id.age);
                birthYear.setText(String.valueOf(singleGroup.memberInfoList.get(i).getAge())+ R.string.age);

                TextView height = view.findViewById(R.id.height);
                height.setText(String.valueOf(singleGroup.memberInfoList.get(i).getHeight())+R.string.height);

                TextView degree = view.findViewById(R.id.degree);
                degree.setText(singleGroup.memberInfoList.get(i).getDegreeName(singleGroup.memberInfoList.get(i).getDegree()));

                TextView memberLiving = view.findViewById(R.id.living);
                memberLiving.setText(singleGroup.memberInfoList.get(i).getLives());
                
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getMeetArchive(SingleGroupDetailsActivity.this, uid);
                    }
                });

            }
        }

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.group_details_layout), font);
    }
    
    private void processResponse(String response){
        JSONObject SingleGroupResponse = null;

        try {
            SingleGroupResponse = new JSONObject(response);
        }catch (JSONException e){
            e.printStackTrace();
        }
        singleGroup = new MeetSingleGroupFragment.SingleGroup();
        JSONObject group = SingleGroupResponse.optJSONObject("single_group");
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
        
        singleGroup.leader = new MeetMemberInfo();
        ParseUtils.setBaseProfile(singleGroup.leader, group.optJSONObject("leader"));

        singleGroup.memberInfoList = ParseUtils.getMeetInfoListFromJsonArray(memberArray);

        handler.sendEmptyMessage(GET_DONE);
    }
    
    private void joinGroup(){
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
                        handler.sendEmptyMessage(JOIN_DONE);
                        //refresh();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
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
                refresh();
                break;
            default:
                break;
        }
    }
    
    private void refresh(){
        onCreate(savedInstanceState);
        //finish();
        //Intent intent=new Intent(this, SingleGroupDetailsActivity.class);
       // startActivity(intent);
        //overridePendingTransition(0, 0);
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

