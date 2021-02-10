package com.mufu.join;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import com.mufu.common.BaseAppCompatActivity;
import com.mufu.main.MeetArchiveFragment;
import com.mufu.main.MainActivity;
import com.mufu.util.HttpUtil;
import com.mufu.util.Slog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.mufu.R;

import static com.mufu.common.MyApplication.getContext;
import static com.mufu.common.SetAvatarActivity.AVATAR_SET_ACTION_BROADCAST;
import static com.mufu.explore.ShareFragment.REQUEST_CODE;
import static com.mufu.main.MainActivity.FINISH_LOGIN_SPLASH_ACTIVITY;
import static com.mufu.main.MainActivity.setTuiKitProfile;
import static com.mufu.meet.EvaluateModifyDialogFragment.EVALUATE_MODIFY_ACTION_BROADCAST;
import static com.mufu.util.SharedPreferencesUtils.setLoginStatus;
import static com.mufu.util.SharedPreferencesUtils.setUid;

public class LoginSplash extends BaseAppCompatActivity {

    private static final String TAG = "LoginSplash";

    private static final String TOKEN_URL = HttpUtil.DOMAIN + "?q=rest_services/user/token";
    private static final String GET_LOGIN_STATUS = HttpUtil.DOMAIN + "?q=account_manager/get_login_status";
    private static final String LOGIN_URL = HttpUtil.DOMAIN + "?q=rest_services/user/login";

    private String account = "";
    private String name = "";
    private String password = "";
    private String token;
    private Context mContext;

    private boolean autoLogin = true;
    private FinishBroadcastReceiver finishBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_splash);
                registerLocalBroadcast();
        mContext = this;
        
        SharedPreferences preferences = getSharedPreferences("account_info", MODE_PRIVATE);
        if(autoLogin == true && preferences != null){
            account = preferences.getString("account", "");
            //name = preferences.getString("name", "");
            password = preferences.getString("password","");

            if(!TextUtils.isEmpty(account) && !TextUtils.isEmpty(password)){
                getLoginStatus();
            }else {
                Intent intent = new Intent(LoginSplash.this, LaunchActivity.class);
                startActivity(intent);
                finish();
            }

        }else {
            Intent intent = new Intent(LoginSplash.this, LaunchActivity.class);
            startActivity(intent);
            finish();
        }
    }
    
    public void getLoginStatus(){
        RequestBody requestBody = new FormBody.Builder().build();

        HttpUtil.sendOkHttpRequest(this, GET_LOGIN_STATUS, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response != null){
                    try{
                        String responseText = response.body().string();
                        JSONObject responseObj = new JSONObject(responseText);
                        boolean isLoggedin = responseObj.optBoolean("status");
                        if(isLoggedin){
                            Slog.d(TAG, "------------>had loggedin, start main activity");
                            Intent intent = new Intent(mContext, MainActivity.class);
                            mContext.startActivity(intent);
                            setLoginStatus(getContext(), true);
                            finish();
                        }else {
                            Slog.d(TAG, "------------>start login");
                            accessToken(getApplicationContext(), account, password);
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }catch (IOException i){
                        i.printStackTrace();
                    }
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginSplash.this, "网络未连接！", Toast.LENGTH_LONG).show();
                        getLoginStatus();
                    }
                });
            }
        });

    }
    
    public void accessToken(final Context context, final String account, final String password) {
        RequestBody requestBody = new FormBody.Builder().build();
        HttpUtil.sendOkHttpRequest(context, TOKEN_URL, requestBody, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "response token: " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject responseObject = new JSONObject(responseText);
                        token = responseObject.getString("token");
                        // Slog.d(TAG, "token : "+token);
                        loginFinally(context, token, account, password);
                    } catch (JSONException e) {
                    e.printStackTrace();
                    }
                }

            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginSplash.this, R.string.login_failed, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }
    
    public void loginFinally(final Context context, String token, String account, String password) {
        Slog.d(TAG, "loginFinally account: "+account+" password: "+password+ " token: "+token);
        RequestBody requestBody = new FormBody.Builder()
                .add("username", account)
                .add("password", password)
                .build();

        HttpUtil.loginOkHttpRequest(context, token, LOGIN_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
            
            String responseText = response.body().string();
                Slog.d(TAG, "---------------->login response : " + responseText);
                if (responseText.equals("[\"Wrong username or password.\"]")){
                    Intent intent = new Intent(LoginSplash.this, LaunchActivity.class);
                    intent.putExtra("login_exception", true);
                    startActivity(intent);
                    finish();
                }else {
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject loginResponse = new JSONObject(responseText);
                        String sessionId = loginResponse.getString("sessid");
                        String session_name = loginResponse.getString("session_name");
                        JSONObject user = loginResponse.getJSONObject("user");
                        Slog.d(TAG, "sessionId: " + sessionId + "===conversation_fragment name: " + session_name);

                        SharedPreferences.Editor editor = context.getSharedPreferences("conversation_fragment", MODE_PRIVATE).edit();
                        editor.putString("sessionId", sessionId);
                        editor.putString("sessionName", session_name);
                        editor.putInt("uid", user.getInt("uid"));
                        editor.apply();

                        setUid(context, user.getInt("uid"));

                        Intent intent = new Intent(context, MainActivity.class);
                        context.startActivity(intent);
                        finish();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, R.string.login_failed, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    
        private class FinishBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case FINISH_LOGIN_SPLASH_ACTIVITY:
                    finish();
                    break;
                default:
                    break;
            }
        }
    }
    
    private void registerLocalBroadcast() {
        finishBroadcastReceiver = new FinishBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FINISH_LOGIN_SPLASH_ACTIVITY);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(finishBroadcastReceiver, intentFilter);
    }

    //unregister local broadcast
    private void unRegisterLocalBroadcast() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(finishBroadcastReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterLocalBroadcast();
    }
}
                        
