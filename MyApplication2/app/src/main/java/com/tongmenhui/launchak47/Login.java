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
import okhttp3.Response;

import static java.security.AccessController.getContext;

public class Login extends AppCompatActivity {

    private EditText accountEdit;
    private EditText passwordEdit;
    private Button loginBtn;
    private ProgressDialog progressDialog;
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
                String account = accountEdit.getText().toString();
                String password = passwordEdit.getText().toString();

            }
        });



    }

    private void queryFromServer(String address, String account, String password){
        showProgress();
        HttpUtil.sendOkHttpRequest(address, account, password, new Callback(){
            @Override
            public void onResponse(Call call, Response response) throws IOException{
                String responseText = response.body().string();
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
