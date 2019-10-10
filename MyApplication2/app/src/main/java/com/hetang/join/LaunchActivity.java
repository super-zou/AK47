package com.hetang.join;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.WindowManager;

import com.hetang.R;
import com.hetang.main.MainActivity;
import com.hetang.util.HttpUtil;
import com.hetang.util.Slog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LaunchActivity extends AppCompatActivity {
    private static final String TAG = "LaunchActivity";
    //private static final String domain = "http://112.126.83.127:81";
    private static final String TOKEN_URL = HttpUtil.DOMAIN + "?q=rest_services/user/token";
    private static final String GET_USERNAME_URL = HttpUtil.DOMAIN + "?q=account_manager/get_username_by_phonenumber";
    private static final String LOGIN_URL = HttpUtil.DOMAIN + "?q=rest_services/user/login";
    //private static final String get_users_ext = HttpUtil.DOMAIN + "?q=user_extdata/get_user";
    private EditText accountEdit;
    private EditText passwordEdit;
    
        private boolean isLogin = false;
    private ProgressDialog progressDialog;
    //Login form info
    private String account;
    private String password;
    private String token;

    private Button loginBtn;
    //private TextInputLayout phoneInputLayout;
   // private TextInputLayout passwordInputLayout;
    private TextView retrievePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN , WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        
        TextView smsVerificationCode = findViewById(R.id.sms_verification_code);
        
        TextView loginByPassword = findViewById(R.id.login_by_password);
        loginBtn = findViewById(R.id.login_button);
        final TextInputLayout phoneInputLayout = findViewById(R.id.phoneInputLayout);
        final TextInputLayout passwordInputLayout = findViewById(R.id.passwordInputLayout);
        retrievePassword = findViewById(R.id.retrieve_password);

        smsVerificationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isLogin == true){
                    isLogin = false;
                }
                loginBtn.setText(R.string.get_verification_code);
                if(passwordInputLayout.getVisibility() != View.GONE){
                    passwordInputLayout.setVisibility(View.GONE);
                }

                if(retrievePassword.getVisibility() != View.GONE){
                    retrievePassword.setVisibility(View.GONE);
                }
                
                            }
        });

        loginByPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLogin == false){
                    isLogin = true;
                }
                loginBtn.setText(R.string.login);
                if(passwordInputLayout.getVisibility() == View.GONE){
                    passwordInputLayout.setVisibility(View.VISIBLE);
                }

                if(retrievePassword.getVisibility() == View.GONE){
                    retrievePassword.setVisibility(View.VISIBLE);
                }
            }
        });
        
        accountEdit = (EditText) findViewById(R.id.account);
        passwordEdit = (EditText) findViewById(R.id.password);

        loginAuto();
       // loginBtn = (Button) findViewById(R.id.login_button);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                account = accountEdit.getText().toString();
                if(isLogin){//password login
                    int type = 0;
                    password = passwordEdit.getText().toString();

                    if(TextUtils.isEmpty(account)){
                        phoneInputLayout.setError(getResources().getString(R.string.phone_is_empty_error));
                        return;
                    }
                    if (account.length() < 8){
                        phoneInputLayout.setError(getResources().getString(R.string.phone_is_error));
                        return;
                    }

                    if(TextUtils.isEmpty(password)){
                        passwordInputLayout.setError(getResources().getString(R.string.password_is_empty_error));
                        return;
                    }

                    if(password.length() < 6){
                        passwordInputLayout.setError(getResources().getString(R.string.password_length_error));
                        return;
                    }

                    Slog.d(TAG, "account: " + account + " password: " + password);
                    getUsernameByPhoneNumber(LaunchActivity.this, phoneInputLayout, passwordInputLayout, account, password);
                }else {//join by verification code
                if(TextUtils.isEmpty(account)){
                        phoneInputLayout.setError(getResources().getString(R.string.phone_is_empty_error));
                        return;
                    }

                    if (account.length() < 8){
                        phoneInputLayout.setError(getResources().getString(R.string.phone_is_error));
                        return;
                    }
                    Intent intent = new Intent(LaunchActivity.this, VerificationCodeJoin.class);
                    intent.putExtra("account", account);
                    startActivity(intent);
                }
            }
        });

    }
    private boolean loginAuto() {
        SharedPreferences preferences = getSharedPreferences("account_info", MODE_PRIVATE);
        if (preferences != null) {
            accountEdit.setText(preferences.getString("account", ""));
            passwordEdit.setText(preferences.getString("password", ""));
        }
        return true;
    }
    
    public void getUsernameByPhoneNumber(final Context context, final TextInputLayout phoneInputLayout, final TextInputLayout passwordInputLayout, String account, final String password) {

        showProgress(context);
        Slog.d(TAG, "====account: "+account+ "password: "+password);
        RequestBody requestBody = new FormBody.Builder()
                .add("type", "0")
                .add("account", account)
                .add("password", password)
                .build();
        HttpUtil.sendOkHttpRequest(context, GET_USERNAME_URL, requestBody, new Callback() {
            int check_login_user = 0;
            String userName;
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject check_response = new JSONObject(responseText);
                        check_login_user = Integer.parseInt(check_response.getString("check_login_user"));
                        userName = check_response.getString("user_name");
                        Slog.d(TAG, "check_login_user: "+check_response.getString("check_login_user"));
                        Slog.d(TAG, "user_name: "+check_response.getString("user_name"));

                        if (check_login_user == 1) {//account not exist
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    closeProgressDialog();
                                    phoneInputLayout.setError(context.getResources().getString(R.string.phone_is_not_exist));
                                }
                            });

                        }else if(check_login_user == 2){//password error
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    closeProgressDialog();
                                    passwordInputLayout.setError(context.getResources().getString(R.string.password_error));
                                }
                            });
                            }else {
                            gotoLogin(context, userName, password);
                        }
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
                        closeProgressDialog();
                        Toast.makeText(context, R.string.login_failed, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    
    public void gotoLogin(Context context, String userName, String password) {

        SharedPreferences.Editor editor = context.getSharedPreferences("account_info", MODE_PRIVATE).edit();
        editor.putString("account", account);
        editor.putString("password", password);
        editor.putInt("type", 0);
        editor.apply();

        accessToken(context, userName, password);
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
                        closeProgressDialog();
                        Toast.makeText(LaunchActivity.this, R.string.login_failed, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }
    
    public void loginFinally(final Context context, String token, String userName, String password) {
        Slog.d(TAG, "loginFinally username: "+userName+" password: "+password+ " token: "+token);
        RequestBody requestBody = new FormBody.Builder()
                .add("username", userName)
                .add("password", password)
                .build();

        HttpUtil.loginOkHttpRequest(context, token, LOGIN_URL, requestBody, new Callback() {
        @Override
            public void onResponse(Call call, Response response) throws IOException {
                Slog.d(TAG, "========header :" + response.headers());
                String responseText = response.body().string();
                Slog.d(TAG, "login response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject login_response = new JSONObject(responseText);
                        String sessionId = login_response.getString("sessid");
                        String session_name = login_response.getString("session_name");
                        JSONObject user = login_response.getJSONObject("user");
                        Slog.d(TAG, "sessionId: " + sessionId + "===session name: " + session_name);
                        
                        SharedPreferences.Editor editor = context.getSharedPreferences("session", MODE_PRIVATE).edit();
                        editor.putString("sessionId", sessionId);
                        editor.putString("sessionName", session_name);
                        editor.putInt("uid", user.getInt("uid"));
                        editor.apply();

                        closeProgressDialog();
                        finish();
                        Intent intent = new Intent(context, MainActivity.class);
                        context.startActivity(intent);

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
                        closeProgressDialog();
                        Toast.makeText(context, R.string.login_failed, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    
     private void showProgress(Context context) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(context.getResources().getString(R.string.logining_progress));
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

}
