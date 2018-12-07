package com.tongmenhui.launchak47.main;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.bumptech.glide.Glide;
import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.meet.MeetArchivesActivity;
import com.tongmenhui.launchak47.util.FontManager;
import com.tongmenhui.launchak47.util.HttpUtil;
import com.tongmenhui.launchak47.util.RequestQueueSingleton;
import com.tongmenhui.launchak47.util.Slog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ArchiveActivity extends AppCompatActivity {
    private static final String TAG = "ArchiveActivity";
    private static final boolean isDebug = true;
    private Handler handler;
    JSONObject summary = null;
    private static final String GET_ARCHIVE_SUMMARY_URL = HttpUtil.DOMAIN + "?q=personal_archive/summary";
    private static final int GET_ARCHIVE_SUMMARY_DONE = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        
        handler = new MyHandler(this);

        initView();
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.activity_archive), font);
    }
    
        private void initView(){
        TextView back = findViewById(R.id.left_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        int uid = getIntent().getIntExtra("uid", -1);

        getSummary(uid);

        getEducationBackground(uid);

        getWorkExperience(uid);
    }
    
        private void getSummary(int uid){
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .build();
        HttpUtil.sendOkHttpRequest(this, GET_ARCHIVE_SUMMARY_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if(isDebug) Slog.d(TAG, "================getSummary response:" + responseText);
                if (responseText != null && !TextUtils.isEmpty(responseText)) {
                    try {
                        summary = new JSONObject(responseText).optJSONObject("response");
                        Slog.d(TAG, "================getSummary summary:" + summary);
                        if (summary != null) {
                            handler.sendEmptyMessage(GET_ARCHIVE_SUMMARY_DONE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
        private void setSummaryView(){
        Slog.d(TAG, "==================setSummaryView name: "+summary.optString("name"));
        TextView name = findViewById(R.id.name);
        TextView profile = findViewById(R.id.profile);
        ImageView headUri = findViewById(R.id.head_uri);
        name.setText(summary.optString("name"));
        profile.setText(summary.optString("content"));

        if (!"".equals(summary.optString("picture_uri"))) {
            String picture_url = HttpUtil.DOMAIN + "/" + summary.optString("picture_uri");
            Glide.with(this).load(picture_url).into(headUri);
        } else {
            headUri.setImageDrawable(getDrawable(R.mipmap.ic_launcher));
        }
    }
    
        private void getEducationBackground(int uid){

    }

    private void getWorkExperience(int uid){

    }
    
        public void handleMessage(Message message) {
        switch (message.what){
            case GET_ARCHIVE_SUMMARY_DONE:
                setSummaryView();
                break;
            default:
                break;
        }
    }
    
        static class MyHandler extends Handler {
        WeakReference<ArchiveActivity> archiveActivityWeakReference;

        MyHandler(ArchiveActivity archiveActivity) {
            archiveActivityWeakReference = new WeakReference<ArchiveActivity>(archiveActivity);
        }

        @Override
        public void handleMessage(Message message) {
            ArchiveActivity archiveActivity = archiveActivityWeakReference.get();
            if (archiveActivity != null) {
                archiveActivity.handleMessage(message);
            }
        }
    }
}
