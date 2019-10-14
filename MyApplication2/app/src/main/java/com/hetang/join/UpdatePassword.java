package com.hetang.join;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hetang.R;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.util.FontManager;
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

public class UpdatePassword extends BaseAppCompatActivity {
    private static final String TAG = "UpdatePassword";
    private TextInputLayout enterPassword;
    private TextInputEditText enterPasswordEdit;
    private Button passwordLogin;
    private Button requestNewPasswordBtn;
    private LinearLayout newPasswordFields;
    private TextInputLayout requestNewPassword;
    private TextInputEditText requestNewPasswordEdit;
    private TextInputLayout newPasswordRepeat;
    
    private TextInputEditText newPasswordRepeatEdit;
    private Button resetLogin;
    private String account;
    private String password;
    private String repeatPassword;
    private LaunchActivity launchActivity;
    private Handler handler;
    private Context mContext;

    private static final int PASSWORD_RESET_DONE = 0;
    private static final String UPDATE_PASSWORD = HttpUtil.DOMAIN + "?q=account_manager/update_password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_password);
        mContext = this;
        account = getIntent().getStringExtra("account");//phone number
        customActionbarSet();
        enterPassword = findViewById(R.id.enter_password);
        enterPasswordEdit = findViewById(R.id.enter_password_edittext);
        passwordLogin = findViewById(R.id.login_password_button);
        requestNewPasswordBtn = findViewById(R.id.request_new_password_btn);
        newPasswordFields = findViewById(R.id.login_by_new_password);
        requestNewPassword = findViewById(R.id.request_new_password);
        requestNewPasswordEdit = findViewById(R.id.request_new_password_edittext);
        newPasswordRepeat = findViewById(R.id.request_repeat_password);
        newPasswordRepeatEdit = findViewById(R.id.request_repeat_password_edittext);
        resetLogin = findViewById(R.id.login_reset_password_button);

        passwordLogin();
        newPasswordLogin();

        handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if (message.what == PASSWORD_RESET_DONE) {
                    Slog.d(TAG, "============handle message");
                    launchActivity.getUsernameByPhoneNumber(mContext,null, null, account, password);
                    finish();
       }
            }
        };

    }

    private void passwordLogin(){
        launchActivity = new LaunchActivity();
        passwordLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                password = enterPasswordEdit.getText().toString();
                Slog.d(TAG, "================password: "+password+ " account: "+account);
                if(TextUtils.isEmpty(password)){
                    enterPassword.setError(getResources().getString(R.string.password_is_empty_error));
                    return;
                }
                
                if(password.length() < 6){
                    enterPassword.setError(getResources().getString(R.string.password_length_error));
                    return;
                }

                launchActivity.getUsernameByPhoneNumber(mContext,null, enterPassword, account, password);
                
                finish();
                
            }
        });
    }

    private void newPasswordLogin(){
        requestNewPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newPasswordFields.setVisibility(View.VISIBLE);
            }
        });
        
        resetLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                password = requestNewPasswordEdit.getText().toString();
                repeatPassword = newPasswordRepeatEdit.getText().toString();
                Slog.d(TAG, "================password: "+password+ " repeatPassword: "+repeatPassword);
                if(TextUtils.isEmpty(password)){
                    requestNewPasswordEdit.setError(getResources().getString(R.string.password_is_empty_error));
                    return;
                }

                if(password.length() < 6){
                    requestNewPasswordEdit.setError(getResources().getString(R.string.password_length_error));
                    return;
                }

                if(!password.equals(repeatPassword)){
                    newPasswordRepeat.setError(getResources().getString(R.string.password_repeat_error));
                    return;
       }

                //launchActivity.getUsernameByPhoneNumber(UpdatePassword.this,null, null, account, password);
                resetPassword(account, password);
            }
        });
    }

    private void resetPassword(final String account, final String password){
        RequestBody requestBody = new FormBody.Builder()
                .add("account", account)
                .add("password", password)
                .build();
        HttpUtil.sendOkHttpRequest(mContext, UPDATE_PASSWORD, requestBody, new Callback() {
        @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "resetPassword response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject checkResponse = new JSONObject(responseText);
                        boolean status = checkResponse.getBoolean("status");

                        Slog.d(TAG, "resetPassword checkResponse : " + checkResponse + " status: "+status);
                        if(status == true){
                            Slog.d(TAG, "=====send message");
                            handler.sendEmptyMessage(PASSWORD_RESET_DONE);
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
                        Toast.makeText(mContext, R.string.login_failed, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void customActionbarSet(){
        TextView back = findViewById(R.id.left_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.custom_actionbar), font);
    }

}
