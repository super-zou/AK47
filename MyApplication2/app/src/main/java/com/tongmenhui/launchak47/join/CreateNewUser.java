package com.tongmenhui.launchak47.join;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.util.FontManager;
import com.tongmenhui.launchak47.util.HttpUtil;
import com.tongmenhui.launchak47.util.Slog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateNewUser extends AppCompatActivity {
    private static final String TAG = "UpdatePassword";

    private static final String CREATE_USER_URL = HttpUtil.DOMAIN + "?q=account_manager/create_user";
    private String account;

    TextView statusBarBack;
    private LinearLayout createInitLayout;
    private LinearLayout createNextLayout;
    private TextInputLayout nickNameInputLayout;
    private TextInputEditText nickNameEditText;
    private TextInputLayout realNameInputLayout;
    private TextInputEditText realNameEditText;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText passwordEditText;
    private TextInputLayout repeatPasswordInputLayout;
    private TextInputEditText repeatPasswordEditText;
    
        private Button prevBtn;
    private Button actionBtn;
    private Button manualSelectBtn;
    private RadioGroup sexSelect;
    private TextView livingTextView;
    private ProgressBar locatingProgressBar;
    private TextView livingSelectNotice;
    private Context mContext;

    private boolean actionDone = false;
    private String nickName;
    private String realName;
    private String password;
    private String repeatPassword;
    private int sex = 0;
    private String living;
    private boolean permissionsAcquired = true;
    private boolean isLocated = false;
    private Handler handler;
    private LaunchActivity launchActivity;
    private static final int USER_CREATE_DONE = 0;
    
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明定位回调监听器
    public AMapLocationListener mLocationListener;
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;

    private String[] permissions = { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_WIFI_STATE};
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_new_user);
        custom_actionbar_set();
        requestAllPermissions();

        mContext = this;
        account = getIntent().getStringExtra("account");//phone number
        
        createInitLayout = findViewById(R.id.create_init);
        createNextLayout = findViewById(R.id.create_next);
        nickNameInputLayout = findViewById(R.id.nickname_input_layout);
        nickNameEditText = findViewById(R.id.nickname_edittext);
        realNameInputLayout = findViewById(R.id.realname_input_layout);
        realNameEditText = findViewById(R.id.realname_edittext);
        passwordInputLayout = findViewById(R.id.password_input_layout);
        passwordEditText = findViewById(R.id.password_edittext);
        repeatPasswordInputLayout = findViewById(R.id.repeat_password_input_layout);
        repeatPasswordEditText = findViewById(R.id.repeat_password_edittext);
        prevBtn = findViewById(R.id.prev);
        actionBtn = findViewById(R.id.action_btn);
        manualSelectBtn = findViewById(R.id.manual_select);
        sexSelect = findViewById(R.id.sexRG);
        livingTextView = findViewById(R.id.living);
       locatingProgressBar = findViewById(R.id.locating_progressbar);
        livingSelectNotice = findViewById(R.id.living_select_notice);

        actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionDone == false){

                    if(initCreate() == true){

                        if(prevBtn.getVisibility() == View.INVISIBLE){
                            prevBtn.setVisibility(View.VISIBLE);
                        }
                        createInitLayout.setVisibility(View.GONE);
                        createNextLayout.setVisibility(View.VISIBLE);
                        actionBtn.setText(getResources().getText(R.string.done));
                        actionBtn.setBackground(getDrawable(R.drawable.btn_stress));
                        actionBtn.setTextColor(R.color.white);
                        actionDone = true;

                        sexSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(RadioGroup group, int checkedId) {
                                int id = group.getCheckedRadioButtonId();
                                if(id == R.id.radioMale){
                                    sex = 0;
                                }else {
                                    sex = 1;
                                }
                            }
                        });
                        
                        getLocation();
                    }

                    if(statusBarBack.getVisibility() != View.INVISIBLE){
                        statusBarBack.setVisibility(View.INVISIBLE);
                    }

                }else {
                    completeCreate();
                }

            }
        });
        
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevBtn.setVisibility(View.INVISIBLE);
                createInitLayout.setVisibility(View.VISIBLE);
                createNextLayout.setVisibility(View.GONE);
                actionBtn.setText(getResources().getText(R.string.next));
                actionBtn.setBackground(getDrawable(R.drawable.btn_default));
                actionBtn.setTextColor(R.color.background);
                actionDone = false;

                if(statusBarBack.getVisibility() == View.INVISIBLE){
                    statusBarBack.setVisibility(View.VISIBLE);
                }

            }
        });
        
        launchActivity = new LaunchActivity();
        handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if (message.what == USER_CREATE_DONE) {
                    Slog.d(TAG, "============handle message");
                    launchActivity.getUsernameByPhoneNumber(mContext,null, null, account, password);
                    //finish();
                }
            }
        };

    }
    
    private boolean initCreate(){

        nickName = nickNameEditText.getText().toString();
        realName = realNameEditText.getText().toString();
        password = passwordEditText.getText().toString();
        repeatPassword = repeatPasswordEditText.getText().toString();

        if(TextUtils.isEmpty(nickName)){
            nickNameInputLayout.setError(getResources().getString(R.string.nickname_is_empty));
            return false;
        }
        
        if(password.length() < 6){
            passwordInputLayout.setError(getResources().getString(R.string.password_length_error));
            return false;
        }

        if(!password.equals(repeatPassword)){
            repeatPasswordInputLayout.setError(getResources().getString(R.string.password_repeat_error));
            return false;
        }

        return true;
    }
    
    private void getLocation(){
        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();

        mLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null) {
                    if (aMapLocation.getErrorCode() == 0) {
                        Slog.d(TAG, "LocationType: "+aMapLocation.getLocationType()+"Province: "+aMapLocation.getProvince()+" city: "+aMapLocation.getCity()+ " district: "+aMapLocation.getDistrict());
                        living = aMapLocation.getProvince()+getResources().getString(R.string.dot)+aMapLocation.getCity()+getResources().getString(R.string.dot)+aMapLocation.getDistrict();
                        livingTextView.setText(living);
                        isLocated = true;
                        locatingProgressBar.setVisibility(View.GONE);
                        manualSelectBtn.setText(getResources().getString(R.string.correction));

                    }else {
                        //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                        Slog.e("AmapError","location Error, ErrCode:"
                                + aMapLocation.getErrorCode() + ", errInfo:"
                                + aMapLocation.getErrorInfo());
                        livingTextView.setText(getResources().getString(R.string.location_error));
                    }

                    mLocationClient.stopLocation();//停止定位后，本地定位服务并不会被销毁
                    mLocationClient.onDestroy();//销毁定位客户端，同时销毁本地定位服务。
                }
            }
        };
        
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);

        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //获取最近3s内精度最高的一次定位结果：
        mLocationOption.setOnceLocationLatest(true);
        //单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
        mLocationOption.setHttpTimeOut(80000);

        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }
    
     private void completeCreate(){
        if(isLocated == false){
            livingSelectNotice.setVisibility(View.VISIBLE);
        }else {
            saveUserInfo();
        }

    }
    
    private void saveUserInfo(){
        RequestBody requestBody = new FormBody.Builder()
                .add("user_info", getUserInfoJsonObject().toString())
                .build();

        HttpUtil.sendOkHttpRequest(mContext, CREATE_USER_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "saveUserInfo response : "+responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject createUser = new JSONObject(responseText);
                        boolean status = createUser.getBoolean("saved");
                        Slog.d(TAG, " saveUserInfo response status: " +status);
                        if(status == true){
                            Slog.d(TAG, "=====send message");
                            handler.sendEmptyMessage(USER_CREATE_DONE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                finish();
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
    private JSONObject getUserInfoJsonObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("account", account);
            jsonObject.put("name", nickName);
            if(!TextUtils.isEmpty(realName)){
                jsonObject.put("realname", realName);
            }
            jsonObject.put("password", password);
            jsonObject.put("sex", sex);
            jsonObject.put("living", living);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
    
    private void requestAllPermissions(){
        // 版本判断。当手机系统大于 23 时，才有必要去判断权限是否获取
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // 检查该权限是否已经获取
            for (int i=0; i<permissions.length; i++){
                int checkResult = ContextCompat.checkSelfPermission(this, permissions[i]);
                // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝

                if (checkResult != PackageManager.PERMISSION_GRANTED) {
                    // 如果没有授予该权限，就去提示用户请求
                    showDialogTipUserRequestPermission();
                }
            }

        }
    }
    
    // 提示用户该请求权限的弹出框
    private void showDialogTipUserRequestPermission() {
         new AlertDialog.Builder(this)
                 .setTitle(R.string.location_service_not_available)
                 .setMessage(R.string.location_permission_request)
                 .setPositiveButton(R.string.open_now, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                      startRequestPermission();
              }
         }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                  finish();
              }
         }).setCancelable(false).show();
    }
    
     // 开始提交请求权限
    private void startRequestPermission() {
        ActivityCompat.requestPermissions(this, permissions, 001);
    }

    // 用户权限申请的回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
         super.onRequestPermissionsResult(requestCode, permissions, grantResults);

         if (requestCode == 001) {
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                     for (int i=0; i<permissions.length; i++){
                         if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                             //boolean b = shouldShowRequestPermissionRationale(permissions[0]);
                             Toast.makeText(this, "权限"+permissions[i]+"获取失败", Toast.LENGTH_SHORT).show();
                             permissionsAcquired = false;
                         }
                     }
             }
         }
    }
    
     private void custom_actionbar_set(){
        statusBarBack = findViewById(R.id.left_back);
        statusBarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.custom_actionbar), font);
        FontManager.markAsIconContainer(findViewById(R.id.create_next), font);
    }
}
