package com.tongmenhui.launchak47.join;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.main.BaseAppCompatActivity;
import com.tongmenhui.launchak47.util.FontManager;
import com.tongmenhui.launchak47.util.HttpUtil;
import com.tongmenhui.launchak47.util.Slog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
public class VerificationCodeJoin extends BaseAppCompatActivity {
    /*+Begin: added by xuchunping 2018.7.19*/
    private static final String TAG = "VerificationCodeJoin";
    private static final String ACCOUNT_CHECK_URL = HttpUtil.DOMAIN + "?q=account_manager/check_account_info";
    private static final String REGISTER_URL = HttpUtil.DOMAIN + "?q=register_guide/register";

    private EditText mAccount;
    private EditText mVerifyCode;
    private EditText mPassword;
    private EditText mConfirmPassword;
    private TextInputEditText mVerificationCodeEdit;
    private Button confirm;
    
        private TextView resend;
    private TimeCount timeCount;
    private String mVerificationCode;
    private TextInputLayout mVerifyCodeInputLayout;
    private String account;
    /*-End: added by xuchunping 2018.7.19*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_verification_code);

        account = getIntent().getStringExtra("account");//phone number

        custom_actionbar_set(getResources().getString(R.string.sms_verification_code));
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


        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeCount.start();
                requst_verification_code();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVerificationCode = mVerificationCodeEdit.getText().toString();
                if(TextUtils.isEmpty(mVerificationCode)){
                    mVerifyCodeInputLayout.setError(getResources().getString(R.string.request_verification_code));
                }else {
                    if(verify_verification_code()){
                        checkRigister(account);
                    }else{
                        mVerifyCodeInputLayout.setError(getResources().getString(R.string.verify_verification_code_failed));
                    }
                }
            }
        });

    }

    private void requst_verification_code(){

    }
    
    private boolean verify_verification_code(){
        return true;
    }
    private void custom_actionbar_set(String titleContent){
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
        FontManager.markAsIconContainer(findViewById(R.id.custom_actionbar), font);
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
    }

    private void createNewUser(String account){
        Intent intent = new Intent(VerificationCodeJoin.this, CreateNewUser.class);
        intent.putExtra("account", account);
        startActivity(intent);
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
            resend.setTextColor(getResources().getColor(R.color.color_blue));
        }
    }

}
