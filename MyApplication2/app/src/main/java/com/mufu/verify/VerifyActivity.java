package com.mufu.verify;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.view.View;
import android.widget.TextView;

import com.mufu.R;
import com.mufu.verify.activity.ActivityVerifyActivity;
import com.mufu.verify.talent.TalentVerifyActivity;
import com.mufu.verify.user.UserVerifyActivity;
import com.mufu.common.BaseAppCompatActivity;
import com.mufu.common.MyApplication;
import com.mufu.main.MessageFragment;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

import static com.mufu.util.ParseUtils.startMeetArchiveActivity;

public class VerifyActivity extends BaseAppCompatActivity {

    private static final String TAG = "VerifyActivity";
    private final static boolean isDebug = false;
    public static final int REQUEST = 0;
    public static final int PASSED = 1;
    public static final int REJECTED = 2;


    private TextView userVerifyCountView;
    private TextView userVerifyNav;
    private TextView talentVerifyCountView;
    private TextView talentVerifyNav;
    private TextView activityVerifyCountView;
    private TextView activityVerifyNav;

    private int userRequestCount = 0;
    private int talentRequestCount = 0;
    private int activityRequestCount = 0;

    private static MyHandler handler;
    public static final String GET_ALL_REQUEST_COUNT_URL = HttpUtil.DOMAIN + "?q=user_extdata/get_all_request_count";
    public static final String USER_VERIFY_PASS_BROADCAST = "com.hetang.action.USER_VERIFY_PASS";
    public static final String USER_VERIFY_REJECT_BROADCAST = "com.hetang.action.USER_VERIFY_REJECT";
    public static final String TALENT_VERIFY_PASS_BROADCAST = "com.hetang.action.TALENT_VERIFY_PASS";
    public static final String TALENT_VERIFY_REJECT_BROADCAST = "com.hetang.action.TALENT_VERIFY_REJECT";
    public static final String ACTIVITY_VERIFY_PASS_BROADCAST = "com.hetang.action.ACTIVITY_VERIFY_PASS";
    public static final String ACTIVITY_VERIFY_REJECT_BROADCAST = "com.hetang.action.ACTIVITY_VERIFY_REJECT";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verify);

        init();

        getAllRequestCount();
    }

    private void init() {
        handler = new MyHandler(this);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.verify), font);

        userVerifyCountView = findViewById(R.id.user_verify_count);
        userVerifyNav = findViewById(R.id.user_verify_nav);
        talentVerifyCountView = findViewById(R.id.talent_verify_count);
        talentVerifyNav = findViewById(R.id.talent_verify_nav);
        activityVerifyCountView = findViewById(R.id.activity_verify_count);
        activityVerifyNav = findViewById(R.id.activity_verify_nav);

        userVerifyNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VerifyActivity.this, UserVerifyActivity.class);
                startActivity(intent);
            }
        });

        talentVerifyNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VerifyActivity.this, TalentVerifyActivity.class);
                startActivity(intent);
            }
        });

        activityVerifyNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VerifyActivity.this, ActivityVerifyActivity.class);
                startActivity(intent);
            }
        });
    }

    private void getAllRequestCount(){
        RequestBody requestBody = new FormBody.Builder().build();

        String url = GET_ALL_REQUEST_COUNT_URL;

        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), url, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (responseText != null) {
                        try {
                            JSONObject responseObject = new JSONObject(responseText);
                            userRequestCount = responseObject.optInt("user_request_count");
                            talentRequestCount = responseObject.optInt("talent_request_count");
                            activityRequestCount = responseObject.optInt("activity_request_count");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    userVerifyCountView.setText(String.valueOf(userRequestCount));
                                    talentVerifyCountView.setText(String.valueOf(talentRequestCount));
                                    activityVerifyCountView.setText(String.valueOf(activityRequestCount));
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
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
            case MessageFragment.HAVE_UNREAD_MESSAGE:
                break;
            default:
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    static class MyHandler extends Handler {
        WeakReference<VerifyActivity> authenticationActivityWeakReference;
        MyHandler(VerifyActivity verifyActivity) {
            authenticationActivityWeakReference = new WeakReference<>(verifyActivity);
        }

        @Override
        public void handleMessage(Message message) {
            VerifyActivity verifyActivity = authenticationActivityWeakReference.get();
            if (verifyActivity != null) {
                verifyActivity.handleMessage(message);
            }
        }
    }
}
