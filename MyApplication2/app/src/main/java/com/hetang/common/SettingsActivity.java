package com.hetang.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.archive.ArchiveActivity;
import com.hetang.update.UpdateParser;
import com.hetang.util.FontManager;
import com.hetang.util.RoundImageView;
import com.hetang.util.ShareDialogFragment;
import com.hetang.util.SharedPreferencesUtils;
import com.hetang.util.Slog;
import com.hetang.util.Utility;
import com.hetang.util.HttpUtil;
import com.xuexiang.xupdate.XUpdate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hetang.util.HttpUtil.GET_DOWNLOAD_QR;
import static com.hetang.common.MyApplication.getContext;

public class SettingsActivity extends BaseAppCompatActivity {

private static final String TAG = "SettingsActivity";
    private MyHandler myHandler;
    private String uri;
    private CheckVersionBroadcastReceiver checkVersionBroadcastReceiver = new CheckVersionBroadcastReceiver();
    public static final String NO_NEW_VERSION_BROADCAST = "com.tongmenhui.action.NO_NEW_VERSION";
    public static final String HAD_NEW_VERSION_BROADCAST = "com.tongmenhui.action.HAD_NEW_VERSION";
    public static final int GET_DOWNLOAD_URI_DOWN = 0;
        public static final int GET_ADMIN_ROLE_DOWN = 1;
    public static final String GET_ADMIN_ROLE_URL = HttpUtil.DOMAIN + "?q=user_extdata/get_admin_role";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        TextView back = findViewById(R.id.left_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        
        myHandler = new MyHandler(this);

        init();

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.settings_layout), font);

    }
    
    private void init(){
        TextView share = findViewById(R.id.share_icon);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareDialogFragment shareDialogFragment = new ShareDialogFragment();
                shareDialogFragment.show(getSupportFragmentManager(), "shareDialogFragment");
            }
        });

        getDownLoadQR();
        
        RelativeLayout checkUpdate = findViewById(R.id.check_update_wrapper);
        TextView currentVersioin = findViewById(R.id.current_version);
        currentVersioin.setText(Utility.getVersionName(this));
        checkUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkUpdate();
            }
        });

        registerLoginBroadcast();
        
                getAdminRole();
    }
    
    private void getAdminRole(){
        RequestBody requestBody = new FormBody.Builder().build();
        HttpUtil.sendOkHttpRequest(getContext(), GET_ADMIN_ROLE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                try {
                    int role = new JSONObject(responseText).optInt("role");
                    if (role >= 0){
                        myHandler.sendEmptyMessage(GET_ADMIN_ROLE_DOWN);
                    }
                    
                    }catch (JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }
    
    private void setAuthenticationView(){
        LinearLayout authenticationWrapper = findViewById(R.id.authentication_wrapper);
        TextView authentication = findViewById(R.id.authentication);
        authenticationWrapper.setVisibility(View.VISIBLE);
        authenticationWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, AuthenticationActivity.class);
                startActivity(intent);
            }
        });
    }
    
    private void getDownLoadQR(){
        RequestBody requestBody = new FormBody.Builder().build();
        HttpUtil.sendOkHttpRequest(getContext(), GET_DOWNLOAD_QR, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                try {
                    uri = new JSONObject(responseText).optString("uri");
                    myHandler.sendEmptyMessage(GET_DOWNLOAD_URI_DOWN);
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }
    
     private void checkUpdate(){
        showProgressDialog(getResources().getString(R.string.checking));
        XUpdate.newBuild(this)
                .updateUrl(HttpUtil.CHECK_VERSION_UPDATE)
                .updateParser(new UpdateParser(this, true))
                .supportBackgroundUpdate(true)
                .themeColor(getResources().getColor(R.color.background))
                .update();
    }

    private void registerLoginBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NO_NEW_VERSION_BROADCAST);
        intentFilter.addAction(HAD_NEW_VERSION_BROADCAST);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(checkVersionBroadcastReceiver, intentFilter);
    }
    
    //unregister local broadcast
    private void unRegisterLoginBroadcast() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(checkVersionBroadcastReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Slog.d(TAG, "-------->onDestroy");
        unRegisterLoginBroadcast();
        //setResultWrapper();
    }
    
    private class CheckVersionBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case NO_NEW_VERSION_BROADCAST:
                    dismissProgressDialog();
                    Toast.makeText(context, getResources().getString(R.string.no_new_version), Toast.LENGTH_LONG).show();
                    break;
                case HAD_NEW_VERSION_BROADCAST:
                    dismissProgressDialog();
                    break;
                default:
                    break;
            }
        }
    }
    
    public void handleMessage(Message msg){
        switch (msg.what){
            case GET_DOWNLOAD_URI_DOWN:
                RoundImageView downLoadQR = findViewById(R.id.download_qr_code);
                Glide.with(this).load(HttpUtil.DOMAIN + uri).into(downLoadQR);
                break;
                            case GET_ADMIN_ROLE_DOWN:
                setAuthenticationView();
                break;
                default:
                    break;
        }
    }
    
    static class MyHandler extends Handler {
        WeakReference<SettingsActivity> settingsActivityWeakReference;

        MyHandler(SettingsActivity settingsActivity) {
            settingsActivityWeakReference = new WeakReference<>(settingsActivity);
        }

        @Override
        public void handleMessage(Message message) {
            SettingsActivity settingsActivity = settingsActivityWeakReference.get();
            if (settingsActivity != null) {
                settingsActivity.handleMessage(message);
            }
        }
    }
}
