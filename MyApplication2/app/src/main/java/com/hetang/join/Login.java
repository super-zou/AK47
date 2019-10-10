package com.hetang.join;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hetang.main.MainActivity;
import com.hetang.util.HttpUtil;
import com.hetang.util.Slog;
import android.support.design.widget.TextInputLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.hetang.R;

public class Login extends AppCompatActivity {
    private static final String TAG = "Login";
    private static final String token_url = HttpUtil.DOMAIN + "?q=rest_services/user/token";
    private static final String get_username_url = HttpUtil.DOMAIN + "?q=account_manager/getUsernameByPhonenumber";
    private static final String login_url = HttpUtil.DOMAIN + "?q=rest_services/user/login";
    private EditText accountEdit;
    private EditText passwordEdit;
    private boolean isLogin = false;
    private Button loginBtn;
    private ProgressDialog progressDialog;
    //Login form info
    private String account;
    private String password;
    private String token;
    
    //private Button loginBtn;
    private TextInputLayout phoneInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextView retrievePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        TextView smsVerificationCode = findViewById(R.id.sms_verification_code);
        TextView loginByPassword = findViewById(R.id.login_by_password);
        loginBtn = findViewById(R.id.login_button);
        phoneInputLayout = findViewById(R.id.phoneInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        retrievePassword = findViewById(R.id.retrieve_password);
        
        smsVerificationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isLogin == true){
                    isLogin = false;
                }
                loginBtn.setText("获取验证码");
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
                loginBtn.setText("登录");
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
        login_auto();
        //loginBtn = (Button) findViewById(R.id.login_button);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isLogin){//password login
                    int type = 0;
                    account = accountEdit.getText().toString();
                    password = passwordEdit.getText().toString();

                    if(TextUtils.isEmpty(account)){
                        phoneInputLayout.setError("请输入手机号");
                        return;
                    }
                    
                    if (account.length() < 8){
                        phoneInputLayout.setError("请输入正确的手机号");
                        return;
                    }

                    if(TextUtils.isEmpty(password)){
                        passwordInputLayout.setError("请输入密码");
                        return;
                    }

                    if(password.length() < 6){
                        passwordInputLayout.setError("密码至少6位");
                        return;
                    }
                    
                    Slog.d(TAG, "account: " + account + " password: " + password);
                    RequestBody requestBody = new FormBody.Builder()
                            .add("type", "0")
                            .add("account", account)
                            .add("password", password)
                            .build();
                    showProgress();
                    get_username_by_phonenumber(get_username_url, requestBody);
                }else {//join by verification code

                }
            }
        });


    }

    private boolean login_auto() {
        SharedPreferences preferences = getSharedPreferences("account_info", MODE_PRIVATE);
        if (preferences != null) {
            accountEdit.setText(preferences.getString("account", ""));
            passwordEdit.setText(preferences.getString("password", ""));
        }
        return true;
    }

    private void get_username_by_phonenumber(String address, RequestBody requestBody) {

        HttpUtil.sendOkHttpRequest(Login.this, address, requestBody, new Callback() {
            int check_login_user = 0;
            String user_name;

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject check_response = new JSONObject(responseText);
                        check_login_user = Integer.parseInt(check_response.getString("check_login_user"));
                        user_name = check_response.getString("user_name");
                        //Slog.d(TAG, "check_login_user: "+check_response.getString("check_login_user"));
                        //Slog.d(TAG, "user_name: "+check_response.getString("user_name"));
                       if (check_login_user == 1) {//account not exist
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    closeProgressDialog();
                                    phoneInputLayout.setError("输入的手机号不存在");
                                    Toast.makeText(Login.this, "输入的手机号不存在", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }else if(check_login_user == 2){//password error
                           runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    closeProgressDialog();
                                    passwordInputLayout.setError("密码错误");
                                    Toast.makeText(Login.this, "密码错误", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }else {
                            SharedPreferences.Editor editor = getSharedPreferences("account_info", MODE_PRIVATE).edit();
                            editor.putString("account", account);
                            editor.putString("password", password);
                            editor.putInt("type", 0);
                            editor.apply();

                            goto_login(user_name);
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
                        Toast.makeText(Login.this, "登录失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void goto_login(String user_name) {
        access_token(user_name);
    }

    private void access_token(final String user_name) {
        RequestBody requestBody = new FormBody.Builder().build();
        HttpUtil.sendOkHttpRequest(Login.this, token_url, requestBody, new Callback() {
            int check_login_user = 0;

            //String username = user_name;
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "response token: " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject responseObject = new JSONObject(responseText);
                        token = responseObject.getString("token");
                        // Slog.d(TAG, "token : "+token);
                        login_finally(token, user_name);
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
                        Toast.makeText(Login.this, "登录失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    private void login_finally(String token, String user_name) {
        //Slog.d(TAG, "login_finally username: "+user_name+" password: "+password+ " token: "+token);
        RequestBody requestBody = new FormBody.Builder()
                .add("username", user_name)
                .add("password", password)
                .build();

        HttpUtil.loginOkHttpRequest(Login.this, token, login_url, requestBody, new Callback() {
            int check_login_user = 0;
            String user_name;

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

                        SharedPreferences.Editor editor = getSharedPreferences("session", MODE_PRIVATE).edit();
                        editor.putString("sessionId", sessionId);
                        editor.putString("sessionName", session_name);
                        editor.putInt("uid", user.getInt("uid"));
                        editor.apply();

                        closeProgressDialog();
                        Login.this.finish();
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        startActivity(intent);


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
                        Toast.makeText(Login.this, "登录失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showProgress() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在登录...");
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
