package com.hetang.join;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

import static com.hetang.util.Utility.isApkInDebug;

public class VerificationCodeJoin extends BaseAppCompatActivity {
    /*+Begin: added by xuchunping 2018.7.19*/
    private static final String TAG = "VerificationCodeJoin";
    private static final String GET_VERIFICATION_CODE_URL = HttpUtil.DOMAIN + "?q=chat/get_verification_code";
    private static final String ACCOUNT_CHECK_URL = HttpUtil.DOMAIN + "?q=account_manager/check_account_info";
    private static final String REGISTER_URL = HttpUtil.DOMAIN + "?q=register_guide/register";

    private EditText mAccount;
    private EditText mVerifyCode;
    private EditText mPassword;
    private EditText mConfirmPassword;
    private TextInputEditText mVerificationCodeEdit;
    private Button confirm;
    private TextView title;
    
    private TextView resend;
    private TimeCount timeCount;
    private String mInputVerificationCode;
    private TextInputLayout mVerifyCodeInputLayout;
    private String account;
    private String mResponseVerifyCode;
    /*-End: added by xuchunping 2018.7.19*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_verification_code);

        account = getIntent().getStringExtra("account");//phone number
        title = findViewById(R.id.phone_number);
        title.setText(account);
        customActionbarSet(getResources().getString(R.string.sms_verification_code));
        timeCount = new TimeCount(60000, 1000);
        timeCount.start();
        
        resend = findViewById(R.id.resend);
        confirm = findViewById(R.id.confirm);
        mVerificationCodeEdit = findViewById(R.id.verification_code_edittext);
        mVerifyCodeInputLayout = findViewById(R.id.verification_code_textInputlayout);

        //confirm.setEnabled(false);
       // confirm.setClickable(false);
        /*+Begin: added by xuchunping 2018.7.19*/
       // resend.setEnabled(false);

        requstVerificationCode();
        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeCount.start();
                requstVerificationCode();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInputVerificationCode = mVerificationCodeEdit.getText().toString();
                if(TextUtils.isEmpty(mInputVerificationCode)){
                    mVerifyCodeInputLayout.setError(getResources().getString(R.string.request_verification_code));
                }else {
                    if(verifyResult()){
                        checkRigister(account);
                    }else{
                        mVerifyCodeInputLayout.setError(getResources().getString(R.string.verify_verification_code_failed));
                    }
                }
            }
        });

    }

    private void requstVerificationCode(){
        sendVerificationCodeRequest();
    }

    private void sendVerificationCodeRequest(){
        Slog.d(TAG, "-------------------->sendVerificationCodeRequest account: "+account);
        RequestBody requestBody = new FormBody.Builder()
                .add("mobile", account)
                .build();
        HttpUtil.sendOkHttpRequest(VerificationCodeJoin.this, GET_VERIFICATION_CODE_URL, requestBody, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "------------>sendVerificationCodeRequest response : " + responseText);

                try {
                    if (!TextUtils.isEmpty(responseText)) {
                        JSONObject result = new JSONObject(responseText).optJSONObject("result");
                        mResponseVerifyCode = result.optString("obj");
                    }else {
                        //createNewUser(account);
                    }
                } catch (JSONException e) {
                    Slog.e(TAG, "sendVerificationCodeRequest onResponse e:" + e.toString());
                }

            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toast("VerificationCodeJoin failed");
                    }
                });
            }
        });
    }
    
    private boolean verifyResult(){
        if (isApkInDebug(VerificationCodeJoin.this)){
            return true;
        }else {
            return mInputVerificationCode.equals(mResponseVerifyCode);
        }
    }
    private void customActionbarSet(String titleContent){
        TextView back = findViewById(R.id.left_back);
        TextView title = findViewById(R.id.title);
        title.setText(titleContent);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.left_back), font);
    }
    
    private void checkRigister(final String account) {
        Slog.d(TAG, "======account"+account);
        RequestBody requestBody = new FormBody.Builder()
                .add("phone", account)
                .build();
        HttpUtil.sendOkHttpRequest(VerificationCodeJoin.this, ACCOUNT_CHECK_URL, requestBody, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "checkRigister response : " + responseText);

                    try {
                        if (!TextUtils.isEmpty(responseText)) {
                            JSONObject check_response = new JSONObject(responseText);
                            JSONObject accountObj = check_response.optJSONObject("account");
                            Slog.d(TAG, "checkRigister response accountObj: " + accountObj);
                            if(accountObj != null){
                               String name = "";
                                updatePassword(name);
                            }else {
                                createNewUser(account);
                            }
                        }else {
                            createNewUser(account);
                        }
                    } catch (JSONException e) {
                        Slog.e(TAG, "checkRigister onResponse e:" + e.toString());
                    }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toast("VerificationCodeJoin failed");
                    }
                });
            }
        });
    }
    
    private void updatePassword(String name){
        Intent intent = new Intent(VerificationCodeJoin.this, UpdatePassword.class);
        intent.putExtra("account", account);
        intent.putExtra("name", name);
        startActivity(intent);
        
        finish();
        
    }

    private void createNewUser(String account){
        Intent intent = new Intent(VerificationCodeJoin.this, CreateNewUser.class);
        intent.putExtra("account", account);
        startActivity(intent);
        
        finish();
        
    }

    class TimeCount extends CountDownTimer {

        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }
        
        @Override
        public void onTick(long millisUntilFinished) {
            //resend.setBackgroundColor(Color.parseColor("#B6B6D8"));
            resend.setClickable(false);
            resend.setText(getResources().getString(R.string.request_again)+millisUntilFinished / 1000);
            resend.setTextColor(getResources().getColor(R.color.color_dark_disabled));
        }

        @Override
        public void onFinish() {
            resend.setText(getResources().getString(R.string.request_again));
            resend.setClickable(true);
            resend.setTextColor(getResources().getColor(R.color.blue_dark));
        }
    }

}
