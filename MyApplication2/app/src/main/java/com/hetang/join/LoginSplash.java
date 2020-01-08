package com.hetang.join;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.hetang.common.BaseAppCompatActivity;
import com.hetang.main.MainActivity;
import com.hetang.util.HttpUtil;
import com.hetang.util.Slog;
import com.hetang.common.MyApplication;
import com.hetang.util.SharedPreferencesUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.hetang.R;

public class LoginSplash extends BaseAppCompatActivity {

    private static final String TAG = "LoginSplash";

    private static final String TOKEN_URL = HttpUtil.DOMAIN + "?q=rest_services/user/token";
    private static final String GET_LOGIN_STATUS = HttpUtil.DOMAIN + "?q=account_manager/get_login_status";
    private static final String LOGIN_URL = HttpUtil.DOMAIN + "?q=rest_services/user/login";
    
    private String name = "";
    private String password = "";
    private String token;

    private boolean autoLogin = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_splash);
        
        SharedPreferences preferences = getSharedPreferences("account_info", MODE_PRIVATE);
        if(autoLogin == true && preferences != null){
            name = preferences.getString("name", "");
            password = preferences.getString("password","");

            if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(password)){
                getLoginStatus(getApplicationContext());
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
    
    public void getLoginStatus(final  Context context){
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
                            Intent intent = new Intent(context, MainActivity.class);
                            context.startActivity(intent);
                            finish();
                        }else {
                            Slog.d(TAG, "------------>start login");
                            accessToken(getApplicationContext(), name, password);
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
                        getLoginStatus(getApplicationContext());
                    }
                });
            }
        });

    }
    
    public void accessToken(final Context context, final String userName, final String password) {
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
                        loginFinally(context, token, userName, password);
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
    
    public void loginFinally(final Context context, String token, String userName, String password) {
        //Slog.d(TAG, "loginFinally username: "+userName+" password: "+password+ " token: "+token);
        RequestBody requestBody = new FormBody.Builder()
                .add("username", userName)
                .add("password", password)
                .build();

        HttpUtil.loginOkHttpRequest(context, token, LOGIN_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
            
            String responseText = response.body().string();
                Slog.d(TAG, "---------------->login response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject loginResponse = new JSONObject(responseText);
                        String sessionId = loginResponse.getString("sessid");
                        String session_name = loginResponse.getString("session_name");
                        JSONObject user = loginResponse.getJSONObject("user");
                        Slog.d(TAG, "sessionId: " + sessionId + "===session name: " + session_name);

                        SharedPreferences.Editor editor = context.getSharedPreferences("session", MODE_PRIVATE).edit();
                        editor.putString("sessionId", sessionId);
                        editor.putString("sessionName", session_name);
                        editor.putInt("uid", user.getInt("uid"));
                        
                        editor.apply();

                        Intent intent = new Intent(context, MainActivity.class);
                        context.startActivity(intent);
                        finish();

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
                        Toast.makeText(context, R.string.login_failed, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
                        
