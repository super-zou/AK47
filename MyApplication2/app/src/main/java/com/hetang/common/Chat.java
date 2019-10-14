package com.hetang.common;

import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.hetang.util.HttpUtil;
import com.hetang.util.SharedPreferencesUtils;
import com.hetang.util.Slog;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.hetang.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.netease.nim.uikit.common.ui.dialog.DialogMaker.showProgressDialog;
import static com.netease.nimlib.sdk.StatusCode.LOGINED;

public class Chat{
    private static final String TAG = "Chat";
    private static Runnable loginRunnable;
    private static Runnable needLoginRunnable;
    private ProgressDialog mProgressDialog;

    public void processChat(final Context context, final long authorUid, final long uid){
        //showProgressDialog(context, "正在建立会话...");
        showProgressDialog(context,"正在建立会话...");

        needLoginRunnable = new Runnable() {
        @Override
            public void run() {
                Slog.d(TAG, "----------------------->not loginned, needLoginRunnable");
                int exist  = HttpUtil.getYunXinAccountExist(MyApplication.getContext(), String.valueOf(authorUid));
                if (exist > 0){
                    Slog.d(TAG, "------------------->had exist with authorUid: "+authorUid);
                    if (HttpUtil.getYunXinAccountExist(MyApplication.getContext(), String.valueOf(uid)) > 0){
                        Slog.d(TAG, "------------------->had exist with uid: "+uid);
                        loginAndStartSession(String.valueOf(authorUid), uid);
                    }else {
                        Slog.d(TAG, "------------------->not exist with uid: "+uid);
                        Slog.d(TAG, "------------------->begin to create with uid: "+uid);
                        if(createYunXinUser(uid) > 0){
                            Slog.d(TAG, "------------------->create sucess with uid: "+uid);
                            loginAndStartSession(String.valueOf(authorUid), uid);
                        }
                    }

                }else {
                    Slog.d(TAG, "------------------->not exist: "+exist + "with authorUid: "+authorUid);
                    Slog.d(TAG, "------------------->begin to create with authorUid: " + authorUid);
                    if (createYunXinUser(authorUid) > 0){
                    Slog.d(TAG, "------------------->create sucess with authorUid: "+authorUid);
                        if (HttpUtil.getYunXinAccountExist(MyApplication.getContext(), String.valueOf(uid)) > 0){
                            Slog.d(TAG, "------------------->had exist with uid: "+uid);
                            loginAndStartSession(String.valueOf(authorUid), uid);
                        }else {
                            Slog.d(TAG, "------------------->not exist with uid: "+uid);
                            Slog.d(TAG, "------------------->begin to create with uid: "+uid);
                            if(createYunXinUser(uid) > 0){
                                Slog.d(TAG, "------------------->create sucess with uid: "+uid);
                                loginAndStartSession(String.valueOf(authorUid), uid);
                            }
                        }
                    }else {
                        Slog.e(TAG, "create YunXin user fail with authorUid: "+authorUid);
                    }
                }

                dismissProgressDialog();
            }
        };
        
        loginRunnable = new Runnable() {
            @Override
            public void run() {

                Slog.d(TAG, "----------------------->loginRunnable");
                int exist  = HttpUtil.getYunXinAccountExist(context, String.valueOf(uid));
                Slog.d(TAG, "------------->exist: "+exist+"   with uid: "+uid);
                if (exist > 0){
                    Slog.d(TAG, "------------->startP2PSession");
                    NimUIKit.startP2PSession(context, String.valueOf(uid));
                }else {
                    if (createYunXinUser(uid) > 0){
                        NimUIKit.startP2PSession(context, String.valueOf(uid));
                    }
                }

                dismissProgressDialog();
            }

        };
        
        StatusCode status = NIMClient.getStatus();
        Slog.d(TAG, "#######################login status: "+status);

        if (status == LOGINED){//had logined
            Thread loginThread = new Thread(loginRunnable);
            loginThread.start();
        }else {//not logined
            Thread needLoginThread = new Thread(needLoginRunnable);
            needLoginThread.start();
        }

    }
    
    private static void loginAndStartSession(String account, final long uid){
        Slog.d(TAG, "---------------->loginAndStartSession");
        final String token = SharedPreferencesUtils.getYunXinToken(MyApplication.getContext());
        NimUIKit.login(new LoginInfo(account, token), new RequestCallback<LoginInfo>() {
            @Override
            public void onSuccess(LoginInfo param) {
            SharedPreferencesUtils.setYunXinAccount(MyApplication.getContext(), param.getAccount());
                if(!token.equals(param.getToken())){
                    Slog.d(TAG, "-------->token error, rewrite by LoginInfo  param.getToken()");
                    SharedPreferencesUtils.setYunXinToken(MyApplication.getContext(), param.getToken());
                }

                NimUIKit.startP2PSession(MyApplication.getContext(), String.valueOf(uid));
            }

            @Override
            public void onFailed(int code) {
            
            if (code == 302 || code == 404) {
                    Toast.makeText(MyApplication.getContext(), R.string.login_failed, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MyApplication.getContext(), "云信登录失败: " + code, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onException(Throwable exception) {
                Toast.makeText(MyApplication.getContext(), "yunxin login error", Toast.LENGTH_LONG).show();
            }
        });
    }
    
    public static int createYunXinUser(final long uid){
        int result = 0;
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .build();
        Response response = HttpUtil.sendOkHttpRequestSync(MyApplication.getContext(), HttpUtil.CREATE_YUNXIN_USER, requestBody, null);
        try {
            if (response.body() != null) {
                String responseText = response.body().string();
                Slog.d(TAG, "==========createYunXinUser response text : " + responseText + "with uid: "+uid);
                if (responseText != null && !TextUtils.isEmpty(responseText)) {
                    result = new JSONObject(responseText).optInt("result");
                }
            }
        }catch (JSONException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }

        return result;
    }
    
    public void showProgressDialog(Context context, String msg) {
        if (null == mProgressDialog) {
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setMessage(msg);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    public void dismissProgressDialog() {
        if (null != mProgressDialog) {
            mProgressDialog.dismiss();
        }
    }
}
