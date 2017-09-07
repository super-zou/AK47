package com.tongmenhui.launchak47;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tongmenhui.launchak47.util.HttpUtil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.tongmenhui.launchak47.util.Slog;

import static java.security.AccessController.getContext;

public class Login extends AppCompatActivity {
    private static final String TAG = "Login";
    private EditText accountEdit;
    private EditText passwordEdit;
    private Button loginBtn;
    private ProgressDialog progressDialog;
    //Login form info
    private final String  domain = "http://www.tongmenhui.com";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        accountEdit = (EditText)findViewById(R.id.account);
        passwordEdit = (EditText)findViewById(R.id.password);
        loginBtn = (Button)findViewById(R.id.login_button);

        loginBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
               int type = 0;
               String account = accountEdit.getText().toString();
               String password = passwordEdit.getText().toString();
                Slog.d(TAG, "account: "+account+" password: "+password);
                String accountCheckUrl = domain + "?q=account_manager/check_login_user";
                RequestBody requestBody = new FormBody.Builder()
                        .add("type", "0")
                        .add("account", account)
                        .add("password", password)
                        .build();
                toLogin(accountCheckUrl, requestBody);

            }
        });



    }

    private void toLogin(String address, RequestBody requestBody){
        showProgress();
        HttpUtil.sendOkHttpRequest(address, requestBody, new Callback(){
            @Override
            public void onResponse(Call call, Response response) throws IOException{
                String responseText = response.body().string();
                Slog.d(TAG, "response : "+responseText);
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
