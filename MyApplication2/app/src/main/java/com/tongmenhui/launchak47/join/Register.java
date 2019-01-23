package com.tongmenhui.launchak47;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.tongmenhui.launchak47.main.BaseAppCompatActivity;
import com.tongmenhui.launchak47.util.CommonUtils;
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

public class Register extends BaseAppCompatActivity {
    /*+Begin: added by xuchunping 2018.7.19*/
    private static final String TAG = "Register";
    private static final String REGISTER_CHECK_URL = HttpUtil.DOMAIN + "?q=register_guide/register_account_validate";
    private static final String REGISTER_URL = HttpUtil.DOMAIN + "?q=register_guide/register";

    private EditText mAccount;
    private EditText mVerifyCode;
    private EditText mPassword;
    private EditText mConfirmPassword;
    /*-End: added by xuchunping 2018.7.19*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        final Button next = (Button) findViewById(R.id.next);
        /*+Begin: added by xuchunping 2018.7.19*/
        next.setEnabled(false);
        mAccount = (EditText) findViewById(R.id.phone_number);
        mVerifyCode = (EditText) findViewById(R.id.verify_code);
        //TODO test
        mVerifyCode.setText("8888");
        mPassword = (EditText) findViewById(R.id.password);
        mConfirmPassword = (EditText) findViewById(R.id.confirm_password);
        mConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (checkUserInput()) {
                    next.setEnabled(true);
                } else {
                    next.setEnabled(false);
                }
            }
        });
        /*-End: added by xuchunping 2018.7.19*/
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*+Begin: added by xuchunping 2018.7.19*/
                final String account = mAccount.getText().toString();
                boolean isEmail = CommonUtils.isEmail(account);
                boolean isMobile = CommonUtils.isMobile(account);
                if (!isEmail && !isMobile) {
                    toast(getString(R.string.register_account_check_error));
                    return;
                }
                Slog.d(TAG, "number: " + account);
                RequestBody requestBody = new FormBody.Builder()
                        .add("type", isEmail ? "0" : "1")//0 for mail
                        .add("account", account)
                        .build();
                checkRigister(REGISTER_CHECK_URL, requestBody);
                /*-End: added by xuchunping 2018.7.19*/
            }
        });
    }

    /*+Begin: added by xuchunping 2018.7.19*/
    private boolean checkUserInput() {
        String account = mAccount.getText().toString();
        String verifyCode = mVerifyCode.getText().toString();
        String password = mPassword.getText().toString();
        String confirmPass = mConfirmPassword.getText().toString();
        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(verifyCode) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPass)) {
            return false;
        }
        if (!CommonUtils.isMobile(account) && !CommonUtils.isEmail(account)) {
            return false;
        }
        if (!password.equals(confirmPass)) {
            return false;
        }
        if (password.length() < 8) {
            return false;
        }
        return true;
    }

    private void checkRigister(String address, RequestBody requestBody) {
        HttpUtil.sendOkHttpRequest(Register.this, address, requestBody, new Callback() {
            boolean exist = false;
            String user_name;

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "checkRigister response : " + responseText + " " + TextUtils.isEmpty(responseText));
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject check_response = new JSONObject(responseText);
                        exist = check_response.optBoolean("exist");
                        user_name = check_response.optString("user_name");
                        Slog.d(TAG, "checkRigister isExist: " + exist);
                        Slog.d(TAG, "checkRigister user_name: " + user_name);

                        if (!exist) {
                            //TODO unregister
                            register();
                        }
                    } catch (JSONException e) {
                        Slog.e(TAG, "checkRigister onResponse e:" + e.toString());
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toast("Register failed");
                    }
                });
            }
        });
    }

    private void register() {
        final String account = mAccount.getText().toString();
        boolean isEmail = CommonUtils.isEmail(account);
        JSONObject obj = new JSONObject();
        try {
            if (isEmail) {
                obj.put("mail", account);
                obj.put("account_type", "0");
            } else {
                obj.put("phone", account);
                obj.put("account_type", "1");
            }
            //
            obj.put("pass", mConfirmPassword.getText().toString());
        } catch (JSONException e) {
            Slog.e(TAG, "register JSONException:" + e.toString());
        }
        RequestBody requestBody = new FormBody.Builder()
                .add("user_info", obj.toString())//0 for mail
                .add("account", account)
                .add("type", isEmail ? "0" : "1")
                .build();
        HttpUtil.sendOkHttpRequest(Register.this, REGISTER_URL, requestBody, new Callback() {
            boolean exist = false;
            String user_name;

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "register response : " + responseText + " " + TextUtils.isEmpty(responseText));
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject check_response = new JSONObject(responseText);
                        exist = check_response.optBoolean("exist");
                        user_name = check_response.optString("user_name");
                        Slog.d(TAG, "register isExist: " + exist);
                        Slog.d(TAG, "register user_name: " + user_name);

                        if (exist) {
                            //TODO unregistered
                        }
                    } catch (JSONException e) {
                        Slog.e(TAG, "onResponse e:" + e.toString());
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        });
    }
    /*-End: added by xuchunping 2018.7.19*/
}
