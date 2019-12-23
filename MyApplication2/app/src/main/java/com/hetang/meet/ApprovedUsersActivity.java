package com.hetang.meet;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.hetang.adapter.ImpressionApprovedDetailAdapter;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.common.HandlerTemp;
import com.hetang.main.MeetArchiveFragment;
import com.hetang.util.FontManager;

import com.hetang.R;
import com.hetang.util.HttpUtil;
import com.hetang.common.MyApplication;
import com.hetang.util.Slog;
import com.hetang.util.SharedPreferencesUtils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApprovedUsersActivity extends BaseAppCompatActivity {

    private static final String TAG = "ApprovedUsersActivity";
    MeetArchiveFragment.ImpressionStatistics impressionStatistics = null;
    private Context mContext;
    private final static int APPROVE_DONE = 1;
    private final static int RESULT_OK = 2;
    private boolean approved = false;
    private RecyclerView mUsersDetailList;
    private ImpressionApprovedDetailAdapter approvedDetailAdapter;
    private LayoutInflater inflater;
    private TextView approve;
    private TextView title;
    private MyHandler myHandler = new MyHandler(this);
    private static final String IMPRESSION_APPROVE_URL = HttpUtil.DOMAIN + "?q=meet/impression/approve";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.impression_approved_users_detail);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        
        mUsersDetailList = findViewById(R.id.users_detail);
        
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mUsersDetailList.setLayoutManager(linearLayoutManager);
        approvedDetailAdapter = new ImpressionApprovedDetailAdapter(mContext);
        mUsersDetailList.setAdapter(approvedDetailAdapter);

        final int uid = getIntent().getIntExtra("uid", -1);
        impressionStatistics = getIntent().getParcelableExtra("impressionStatistics");

        approvedDetailAdapter.setData(impressionStatistics.meetMemberList);
        approvedDetailAdapter.notifyDataSetChanged();
        
        TextView back = findViewById(R.id.left);
        approve = findViewById(R.id.approve);
        title = findViewById(R.id.title);
        title.setText(impressionStatistics.impression+" · "+impressionStatistics.impressionCount);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exit();
            }
        });
        int authorUid = SharedPreferencesUtils.getSessionUid(MyApplication.getContext());
        for (UserMeetInfo s:impressionStatistics.meetMemberList) {
            if (authorUid == s.getUid()){
                approve.setText(getResources().getString(R.string.feature_approvied));
                approve.setBackgroundColor(mContext.getResources().getColor(R.color.color_blue));
                approve.setTextColor(mContext.getResources().getColor(R.color.white));
                approve.setClickable(false);
                approve.setEnabled(false);
            }
        }
        
        approve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setImpressionApprove(uid);
            }
        });

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.custom_actionbar), font);
    }
    
    private void setImpressionApprove(int uid){
        showProgressDialog("");
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .add("features", impressionStatistics.impression+"#").build();
        HttpUtil.sendOkHttpRequest(ApprovedUsersActivity.this, IMPRESSION_APPROVE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                
                if(response.body() != null){
                    String responseText = response.body().string();
                    Slog.d(TAG, "==========setImpressionApprove response text : "+responseText);
                    if(responseText != null){
                        myHandler.sendEmptyMessage(APPROVE_DONE);
                        approved = true;
                        /*
                        Intent intent = new Intent();
                        intent.putExtra("approved", true);
                        setResult(RESULT_OK, intent);
                        finish();
                        */
                    }
                }

            }
            
            @Override
            public void onFailure(Call call, IOException e) {

            }
        });

    }

    @Override
    public void onBackPressed(){
        exit();
    }
    
    private void exit(){
        if (approved == true){
            Intent intent = new Intent();
            intent.putExtra("approved", true);
            setResult(RESULT_OK, intent);
        }
        finish();
    }
    
    private void handleMessage(Message message){
        switch (message.what){
            case APPROVE_DONE:
                dismissProgressDialog();
                approve.setText(getResources().getString(R.string.feature_approvied));
                approve.setClickable(false);
                approve.setEnabled(false);
                int impressionCount = impressionStatistics.impressionCount+1;
                title.setText(impressionStatistics.impression+" · "+impressionCount);
                break;
        }
    }
    
    static class MyHandler extends HandlerTemp<ApprovedUsersActivity> {
        public MyHandler(ApprovedUsersActivity cls){
            super(cls);
        }

        @Override
        public void handleMessage(Message message) {
            ApprovedUsersActivity approvedUsersActivity = ref.get();
            if (approvedUsersActivity != null) {
                approvedUsersActivity.handleMessage(message);
            }
        }
    }
}
