package com.tongmenhui.launchak47;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tongmenhui.launchak47.main.MainActivity;
import com.tongmenhui.launchak47.util.HttpUtil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.tongmenhui.launchak47.util.Slog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static java.security.AccessController.getContext;

public class Login extends AppCompatActivity {
    private static final String TAG = "Login";
    private EditText accountEdit;
    private EditText passwordEdit;
    private Button loginBtn;
    private ProgressDialog progressDialog;
    //Login form info
    private String account;
    private String password;
    private String token;

    private static final String  domain = "http://112.126.83.127:88";
    private static final String token_url = domain + "?q=rest_services/user/token";
    private static final String check_url = domain + "?q=account_manager/check_login_user";
    private static final String login_url = domain + "?q=rest_services/user/login";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        accountEdit = (EditText)findViewById(R.id.account);
        passwordEdit = (EditText)findViewById(R.id.password);
        login_auto();
        loginBtn = (Button)findViewById(R.id.login_button);

        loginBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
               int type = 0;
                account = accountEdit.getText().toString();
                password = passwordEdit.getText().toString();

                Slog.d(TAG, "account: "+account+" password: "+password);
                RequestBody requestBody = new FormBody.Builder()
                        .add("type", "0")
                        .add("account", account)
                        .add("password", password)
                        .build();
                showProgress();
                goto_check(check_url, requestBody);
            }
        });



    }

    private boolean login_auto(){
        SharedPreferences preferences = getSharedPreferences("account_info", MODE_PRIVATE);
        if(preferences != null){
            accountEdit.setText(preferences.getString("account", ""));
            passwordEdit.setText(preferences.getString("password", ""));
        }
        return true;
    }

    private void goto_check(String address, RequestBody requestBody){

        HttpUtil.sendOkHttpRequest(Login.this, address, requestBody, new Callback(){
            int check_login_user = 0;
            String user_name;

            @Override
            public void onResponse(Call call, Response response) throws IOException{
                String responseText = response.body().string();
                Slog.d(TAG, "response : "+responseText);
                if(!TextUtils.isEmpty(responseText)){
                    try {
                        JSONObject check_response= new JSONObject(responseText);
                        check_login_user = Integer.parseInt(check_response.getString("check_login_user"));
                        user_name = check_response.getString("user_name");
                        //Slog.d(TAG, "check_login_user: "+check_response.getString("check_login_user"));
                        //Slog.d(TAG, "user_name: "+check_response.getString("user_name"));

                        if(check_login_user != 0){
                            SharedPreferences.Editor editor = getSharedPreferences("account_info", MODE_PRIVATE).edit();
                            editor.putString("account", account);
                            editor.putString("password", password);
                            editor.putInt("type", 0);
                            editor.apply();

                            goto_login(user_name);
                        }


                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e){
                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        closeProgressDialog();
                        Toast.makeText(Login.this, "登录失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void goto_login(String user_name){
        access_token(user_name);
    }

    private void access_token(final String user_name){
        RequestBody requestBody = new FormBody.Builder().build();
        HttpUtil.sendOkHttpRequest(Login.this, token_url, requestBody, new Callback(){
            int check_login_user = 0;
            //String username = user_name;
            @Override
            public void onResponse(Call call, Response response) throws IOException{
                String responseText = response.body().string();
                Slog.d(TAG, "response token: "+responseText);
                if(!TextUtils.isEmpty(responseText)){
                    try {
                        JSONObject responseObject = new JSONObject(responseText);
                        token = responseObject.getString("token");
                        // Slog.d(TAG, "token : "+token);
                        login_finally(token, user_name);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onFailure(Call call, IOException e){
                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        closeProgressDialog();
                        Toast.makeText(Login.this, "登录失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    private void login_finally(String token, String user_name){
        //Slog.d(TAG, "login_finally username: "+user_name+" password: "+password+ " token: "+token);
        RequestBody requestBody = new FormBody.Builder()
                .add("username", user_name)
                .add("password", password)
                .build();

        HttpUtil.sendOkHttpRequest(Login.this, login_url, requestBody, new Callback(){
            int check_login_user = 0;
            String user_name;

            @Override
            public void onResponse(Call call, Response response) throws IOException{
                Slog.d(TAG, "========header :"+response.headers());
                String responseText = response.body().string();
                Slog.d(TAG, "login response : "+responseText);
                if(!TextUtils.isEmpty(responseText)){
                    try {
                        JSONObject login_response= new JSONObject(responseText);
                        String sessionId = login_response.getString("sessid");
                        String session_name = login_response.getString("session_name");
                        JSONObject user = login_response.getJSONObject("user");
                        Slog.d(TAG, "sessionId: "+sessionId+"===session name: "+session_name);

                        SharedPreferences.Editor editor = getSharedPreferences("session", MODE_PRIVATE).edit();
                        editor.putString("sessionId", sessionId);
                        editor.putString("sessionName", session_name);
                        editor.putInt("uid", user.getInt("uid"));
                        editor.apply();

                        Intent intent = new Intent(Login.this, MainActivity.class);
                        startActivity(intent);


                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e){
                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        closeProgressDialog();
                        Toast.makeText(Login.this, "登录失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showProgress(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在登录...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    private void closeProgressDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

}
