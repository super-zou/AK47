package com.tongmenhui.launchak47.main;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import org.json.JSONArray;

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

import static com.tongmenhui.launchak47.util.ParseUtils.getMeetArchive;

public class ArchiveActivity extends AppCompatActivity {
    private static final String TAG = "ArchiveActivity";
    private static final boolean isDebug = true;
    private Handler handler;
    JSONObject mSummary = null;
    JSONArray mEducationBackground = null;
    JSONArray mWorkExperience = null;
    private static final String GET_ARCHIVE_SUMMARY_URL = HttpUtil.DOMAIN + "?q=personal_archive/summary";
    private static final String GET_EDUCATION_BACKGROUND_URL = HttpUtil.DOMAIN + "?q=personal_archive/education_background/load";
    private static final String GET_WORK_EXPERIENCE_URL = HttpUtil.DOMAIN + "?q=personal_archive/work_experience/load";
    private static final int GET_ARCHIVE_SUMMARY_DONE = 0;
    private static final int GET_EDUCATION_BACKGROUND_DONE = 1;
    private static final int GET_WORK_EXPERIENCE_DONE = 2;

    private LinearLayout mEducationBackgroundListView = null;
    private LinearLayout mWorkExperienceListView = null;
    
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
            
        mEducationBackgroundListView = findViewById(R.id.education_background_list);
        mWorkExperienceListView = findViewById(R.id.work_experience_list);

        final int uid = getIntent().getIntExtra("uid", -1);

        getSummary(uid);

        getEducationBackground(uid);

        getWorkExperience(uid);
            
       TextView meetArchive = findViewById(R.id.meet_archive);
        meetArchive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getMeetArchive(ArchiveActivity.this, uid);
            }
        });
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
                         mSummary = new JSONObject(responseText).optJSONObject("response");
                        if(isDebug) Slog.d(TAG, "================getSummary summary:" + mSummary);
                        if (mSummary != null) {
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
        TextView name = findViewById(R.id.name);
        TextView sex = findViewById(R.id.sex);
        TextView profile = findViewById(R.id.profile);
        TextView living = findViewById(R.id.living);
        ImageView headUri = findViewById(R.id.head_uri);
        name.setText(mSummary.optString("name"));

        if(mSummary.optInt("sex") == 0){
            sex.setText(R.string.mars);
        }else {
            sex.setText(R.string.venus);
        }

        if(mSummary.optString("lives") != null){
            living.setText("现居"+mSummary.optString("lives").trim());
        }
        
                if(mSummary.optString("content") != null){
            profile.setText(mSummary.optString("content"));
        }

        if (!"".equals(mSummary.optString("picture_uri"))) {
            String picture_url = HttpUtil.DOMAIN + "/" + mSummary.optString("picture_uri");
            Glide.with(this).load(picture_url).into(headUri);
        } else {
            headUri.setImageDrawable(getDrawable(R.mipmap.ic_launcher));
        }
    }
    
    private void getEducationBackground(int uid){
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(this, GET_EDUCATION_BACKGROUND_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if(isDebug) Slog.d(TAG, "================getEducationBackground response:" + responseText);
                if (responseText != null && !TextUtils.isEmpty(responseText)) {
                    try {
                        mEducationBackground = new JSONObject(responseText).optJSONArray("education");
                        Slog.d(TAG, "================getEducationBackground education:" + mEducationBackground);
                        if (mEducationBackground != null) {
                            handler.sendEmptyMessage(GET_EDUCATION_BACKGROUND_DONE);
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
    
        private void setEducationBackgroundView(){
        for (int i=0; i<mEducationBackground.length(); i++){
            try {
                JSONObject education = mEducationBackground.getJSONObject(i);
                View view = LayoutInflater.from(this).inflate(R.layout.achive_base_background, null);
                mEducationBackgroundListView.addView(view, i);
                TextView university = view.findViewById(R.id.title);
                TextView degree = view.findViewById(R.id.subtitle1);
                TextView major = view.findViewById(R.id.subtitle2);
                major.setVisibility(View.VISIBLE);

                TextView start = view.findViewById(R.id.start_time);
                TextView end = view.findViewById(R.id.end_time);

                university.setText(education.optString("university"));
                degree.setText(education.optString("degree"));
                major.setText(", "+education.optString("major"));

                 start.setText(education.optString("entrance_year")+"年"+education.optString("entrance_month"));
                end.setText("~  "+education.optString("graduate_year")+"年"+education.optString("graduate_month"));
            }catch (JSONException e){
                e.printStackTrace();
            }

        }
    }

    private void getWorkExperience(int uid){
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(this, GET_WORK_EXPERIENCE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if(isDebug) Slog.d(TAG, "================getWorkExperience response:" + responseText);
                if (responseText != null && !TextUtils.isEmpty(responseText)) {
                    try {
                        mWorkExperience = new JSONObject(responseText).optJSONArray("work");
                        Slog.d(TAG, "================getWorkExperience work:" + mWorkExperience);
                        if (mWorkExperience != null) {
                            handler.sendEmptyMessage(GET_WORK_EXPERIENCE_DONE);
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
    
    private void setWorkExperienceView(){
        for (int i=0; i<mWorkExperience.length(); i++){
            try {
                JSONObject workExperience = mWorkExperience.getJSONObject(i);
                View view = LayoutInflater.from(this).inflate(R.layout.achive_base_background, null);
                mWorkExperienceListView.addView(view, i);
                TextView jobTitle = view.findViewById(R.id.title);
                TextView company = view.findViewById(R.id.subtitle1);
                 TextView start = view.findViewById(R.id.start_time);
                TextView end = view.findViewById(R.id.end_time);

                jobTitle.setText(workExperience.optString("job_title"));
                company.setText(workExperience.optString("company"));

                start.setText(workExperience.optString("entrance_year")+"年"+workExperience.optString("entrance_month"));
                end.setText("~  "+workExperience.optString("leave_year")+"年"+workExperience.optString("leave_month"));
            }catch (JSONException e){
                e.printStackTrace();
            }

        }
    }
    
        public void handleMessage(Message message) {
        switch (message.what){
            case GET_ARCHIVE_SUMMARY_DONE:
                setSummaryView();
                break;
            case GET_EDUCATION_BACKGROUND_DONE:
                setEducationBackgroundView();
                break;
            case GET_WORK_EXPERIENCE_DONE:
                setWorkExperienceView();
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
