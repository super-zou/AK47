package com.mufu.join;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
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
import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mufu.R;
import com.mufu.common.BaseAppCompatActivity;
import com.mufu.common.HandlerTemp;
import com.mufu.common.MyApplication;
import com.mufu.util.CommonBean;
import com.mufu.util.CommonPickerView;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.Slog;

import org.angmarch.views.NiceSpinner;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.mufu.meet.SelfCondition.getDegreeIndex;
import static com.mufu.util.SharedPreferencesUtils.setAccount;
import static com.mufu.util.SharedPreferencesUtils.setAccountType;
import static com.mufu.util.SharedPreferencesUtils.setLoginedAccountSex;
import static com.mufu.util.SharedPreferencesUtils.setName;
import static com.mufu.util.SharedPreferencesUtils.setPassWord;

public class CreateNewUser extends BaseAppCompatActivity {
    private static final String TAG = "CreateNewUser";

    private static final String CREATE_USER_URL = HttpUtil.DOMAIN + "?q=account_manager/create_user";
    private static final int USER_CREATE_DONE = 0;
    private static final int MSG_LOAD_INDUSTRY_DATA = 0x0001;
    private static final int MSG_LOAD_CITY_DATA = 0x0002;
    private static final int MSG_LOAD_SUCCESS = 0x0003;
    private static final int MSG_LOAD_FAILED = 0x0004;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明定位回调监听器
    public AMapLocationListener mLocationListener;
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;
    TextView statusBarBack;
    boolean SITUATIONSELECTED = false;
    boolean isDegreeSelected = false;
    boolean isIndustrySelected = false;
    RadioGroup situationSelect;
    LinearLayout studentLayout;
    LinearLayout workLayout;
    TextInputLayout majorInputLayout;
    TextInputEditText majorEditText;
    TextInputLayout universityInputLayout;
    TextInputEditText universityEditText;
    TextInputLayout positionInputLayout;
    TextInputEditText positionEditText;
    TextInputLayout industryInputLayout;
    TextInputEditText industryEditText;
    String[] degrees;
    String degree = "";
    private String account;
    private LinearLayout createInitLayout;
    private LinearLayout createNextLayout;
    private TextInputLayout nickNameInputLayout;
    private TextInputEditText nickNameEditText;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText passwordEditText;
    private TextInputLayout repeatPasswordInputLayout;
    private TextInputEditText repeatPasswordEditText;
    private TextView mSkipTV;
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
    private String password;
    private String repeatPassword;
    private int sex = 0;
    private String living;
    private boolean permissionsAcquired = true;
    private boolean isLocated = false;
    private Handler handler;
    private int mSituation = 0;
    private Thread threadIndustry;
    private Thread threadCity;
    private ArrayList<CommonBean> industryMainItems = new ArrayList<>();
    private ArrayList<ArrayList<String>> industrySubItems = new ArrayList<>();
    private ArrayList<CommonBean> provinceItems = new ArrayList<>();
    private ArrayList<ArrayList<String>> cityItems = new ArrayList<>();

    private String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_WIFI_STATE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_new_user);
        setCustomActionbar();
        //requestAllPermissions();
        startRequestPermission();

        handler = new MyHandler(this);
        mContext = this;
        account = getIntent().getStringExtra("account");//phone number
        createInitLayout = findViewById(R.id.create_init);
        createNextLayout = findViewById(R.id.create_next);
        nickNameInputLayout = findViewById(R.id.nickname_input_layout);
        nickNameEditText = findViewById(R.id.nickname_edittext);

        passwordInputLayout = findViewById(R.id.password_input_layout);
        passwordEditText = findViewById(R.id.password_edittext);
        repeatPasswordInputLayout = findViewById(R.id.repeat_password_input_layout);
        repeatPasswordEditText = findViewById(R.id.repeat_password_edittext);
        prevBtn = findViewById(R.id.prev);
        actionBtn = findViewById(R.id.action_btn);
        mSkipTV = findViewById(R.id.skip);
        manualSelectBtn = findViewById(R.id.manual_select);
        sexSelect = findViewById(R.id.sexRG);
        livingTextView = findViewById(R.id.living);
        locatingProgressBar = findViewById(R.id.locating_progressbar);
        livingSelectNotice = findViewById(R.id.living_select_notice);
        studentLayout = findViewById(R.id.student);

        workLayout = findViewById(R.id.work);
        situationSelect = findViewById(R.id.situationRG);
        majorInputLayout = findViewById(R.id.major_input_layout);
        majorEditText = findViewById(R.id.major_edittext);
        universityInputLayout = findViewById(R.id.university_input_layout);
        universityEditText = findViewById(R.id.university_edittext);
        positionInputLayout = findViewById(R.id.position_input_layout);
        positionEditText = findViewById(R.id.position_edittext);
        industryInputLayout = findViewById(R.id.industry_input_layout);
        industryEditText = findViewById(R.id.industry_edittext);
        degrees = getResources().getStringArray(R.array.degrees);

        sexSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int id = group.getCheckedRadioButtonId();
                if (id == R.id.radioMale) {
                    sex = 0;
                } else {
                    sex = 1;
                }
            }
        });

        actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (false) {//not complete
                    if (firstPageInitAndCheck() == true) {
                        if (prevBtn.getVisibility() == View.INVISIBLE) {
                            prevBtn.setVisibility(View.VISIBLE);
                        }
                        mSkipTV.setVisibility(View.VISIBLE);
                        createInitLayout.setVisibility(View.GONE);
                        createNextLayout.setVisibility(View.VISIBLE);
                        actionBtn.setText(getResources().getText(R.string.done));
                        actionBtn.setBackground(getDrawable(R.drawable.btn_stress));
                        actionBtn.setTextColor(getResources().getColor(R.color.white));
                        actionDone = true;

                        NiceSpinner niceSpinnerDegree = findViewById(R.id.nice_spinner_degree);
                        final List<String> degreeList = new LinkedList<>(Arrays.asList(degrees));
                        niceSpinnerDegree.setBackgroundResource(R.drawable.border_all);
                        niceSpinnerDegree.attachDataSource(degreeList);
                        niceSpinnerDegree.addOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                //selfCondition.setDegree(String.valueOf(degreeList.get(i)));
                                degree = String.valueOf(degreeList.get(i));
                                isDegreeSelected = true;
                            }

                        });

                        handler.sendEmptyMessage(MSG_LOAD_INDUSTRY_DATA);

                        situationSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(RadioGroup group, int checkedId) {
                                int id = group.getCheckedRadioButtonId();
                                if (id == R.id.radioStudent) {
                                    mSituation = 0;
                                    if (studentLayout.getVisibility() == View.GONE) {
                                        studentLayout.setVisibility(View.VISIBLE);
                                    }
                                    if (workLayout.getVisibility() != View.GONE) {
                                        workLayout.setVisibility(View.GONE);
                                    }

                                } else {
                                    mSituation = 1;

                                    if (workLayout.getVisibility() == View.GONE) {
                                        workLayout.setVisibility(View.VISIBLE);
                                    }

                                    if (studentLayout.getVisibility() != View.GONE) {
                                        studentLayout.setVisibility(View.GONE);
                                    }

                                    handler.sendEmptyMessage(MSG_LOAD_INDUSTRY_DATA);
                                }

                                SITUATIONSELECTED = true;
                            }
                        });

                        industryEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View view, boolean b) {
                                if (b) {
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(industryEditText.getWindowToken(), 0);
                                    showPickerView(true);
                                }
                            }
                        });

                    }

                    if (statusBarBack.getVisibility() != View.INVISIBLE) {
                        statusBarBack.setVisibility(View.INVISIBLE);
                    }

                } else {
                    if (firstPageInitAndCheck()) {
                        saveUserInfo(false);
                    }
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
                actionBtn.setTextColor(getResources().getColor(R.color.background));
                actionDone = false;

                if (statusBarBack.getVisibility() == View.INVISIBLE) {
                    statusBarBack.setVisibility(View.VISIBLE);
                }

            }
        });
        
                mSkipTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInfo(true);
            }
        });

    }

    private boolean firstPageInitAndCheck() {

        nickName = nickNameEditText.getText().toString();
        //realName = realNameEditText.getText().toString();
        password = passwordEditText.getText().toString();
        repeatPassword = repeatPasswordEditText.getText().toString();

        //getLocation();

        if (TextUtils.isEmpty(nickName)) {
            nickNameInputLayout.setError(getResources().getString(R.string.nickname_is_empty));
            return false;
        }

        if (password.length() < 6) {
            passwordInputLayout.setError(getResources().getString(R.string.password_length_error));
            return false;
        }

        if (!password.equals(repeatPassword)) {
            repeatPasswordInputLayout.setError(getResources().getString(R.string.password_repeat_error));
            return false;
        }
        
        if (TextUtils.isEmpty(universityEditText.getText().toString())) {
            universityInputLayout.setError(getResources().getString(R.string.university_empty));
            return false;
        }

        return true;
    }

    private void getLocation() {
        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();

        mLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null) {
                    if (aMapLocation.getErrorCode() == 0) {
                        String city = aMapLocation.getCity();
                        living = city.substring(0, city.length() - 1);
                        livingTextView.setText(getResources().getString(R.string.living) + ":" + living);
                        isLocated = true;
                        locatingProgressBar.setVisibility(View.GONE);
                        manualSelectBtn.setText(getResources().getString(R.string.correction));

                    } else {

                        //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                        Slog.e("AmapError", "location Error, ErrCode:"
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

        handler.sendEmptyMessage(MSG_LOAD_CITY_DATA);

        manualSelectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPickerView(false);
            }
        });
    }

    private boolean checkNextInput() {

        if (SITUATIONSELECTED == true) {
            if (mSituation == 0) {
                if (TextUtils.isEmpty(universityEditText.getText().toString())) {
                    universityInputLayout.setError(getResources().getString(R.string.university_empty));
                    return false;
                }
                if (TextUtils.isEmpty(majorEditText.getText().toString())) {
                    majorInputLayout.setError(getResources().getString(R.string.major_empty));
                    return false;
                }

                if (isDegreeSelected == false) {
                    Toast.makeText(this, "请选择学历", Toast.LENGTH_LONG).show();
                    return false;
                } else {
                    if (degree.equals(getResources().getString(R.string.degree))) {
                        Toast.makeText(this, "请选择学历", Toast.LENGTH_LONG).show();
                        return false;
                    }
                }

            } else {
                if (TextUtils.isEmpty(positionEditText.getText().toString())) {
                    positionInputLayout.setError(getResources().getString(R.string.profession_empty));
                    return false;
                }

                if (TextUtils.isEmpty(industryEditText.getText().toString())) {
                    industryInputLayout.setError(getResources().getString(R.string.industry_empty));
                    return false;
                }
            }
        } else {
            Toast.makeText(this, "请选择当前状态", Toast.LENGTH_LONG).show();
            return false;
        }

        if (isLocated == false) {
            livingSelectNotice.setVisibility(View.VISIBLE);
            return false;
        }

        return true;
    }

    private void saveUserInfo(boolean skipped) {
        RequestBody requestBody = new FormBody.Builder()
                .add("user_info", getUserInfoJsonObject(skipped).toString())
                .build();

        HttpUtil.sendOkHttpRequest(mContext, CREATE_USER_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "saveUserInfo response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject createUser = new JSONObject(responseText);
                        boolean status = createUser.getBoolean("saved");

                        Slog.d(TAG, " saveUserInfo response status: " + status);
                        if (status == true) {
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

    private JSONObject getUserInfoJsonObject(boolean skipped) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("account", account);
            jsonObject.put("name", nickName);
            jsonObject.put("situation", mSituation);
            if (!skipped){
                if (mSituation == 0) {
                    jsonObject.put("university", universityEditText.getText().toString());
                    jsonObject.put("major", majorEditText.getText().toString());
                    jsonObject.put("degree", getDegreeIndex(degree));
                } else {
                    jsonObject.put("position", positionEditText.getText().toString());
                    jsonObject.put("industry", industryEditText.getText().toString());
                }
                jsonObject.put("living", living);
            }
            jsonObject.put("password", password);
            jsonObject.put("sex", sex);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void initJsondata(String jsonFile, boolean isIndustry) {
        CommonPickerView commonPickerView = new CommonPickerView();
        if (isIndustry == true) {
            industryMainItems = commonPickerView.getOptionsMainItem(this, jsonFile);
            industrySubItems = commonPickerView.getOptionsSubItems(industryMainItems);
        } else {
            provinceItems = commonPickerView.getOptionsMainItem(this, jsonFile);
            cityItems = commonPickerView.getOptionsSubItems(provinceItems);
        }

        handler.sendEmptyMessage(MSG_LOAD_SUCCESS);
    }

    private void showPickerView(boolean isIndustry) {// 弹出行业选择器

        //条件选择器
        OptionsPickerView pvOptions;
        if (isIndustry) {
            pvOptions = new OptionsPickerBuilder(this, new OnOptionsSelectListener() {

                @Override
                public void onOptionsSelect(int options1, int option2, int options3, View v) {
                    //返回的分别是二个级别的选中位置
                    String tx = industryMainItems.get(options1).getPickerViewText()
                            + industrySubItems.get(options1).get(option2);
                    industryEditText.setText(industrySubItems.get(options1).get(option2));

                    isIndustrySelected = true;

                }
            }).build();

            pvOptions.setPicker(industryMainItems, industrySubItems);
        } else {
            pvOptions = new OptionsPickerBuilder(this, new OnOptionsSelectListener() {

                @Override
                public void onOptionsSelect(int options1, int option2, int options3, View v) {
                    //返回的分别是二个级别的选中位置
                    isLocated = true;
                    String city = cityItems.get(options1).get(option2);
                    living = city;
                    livingTextView.setText(living);
                }
            }).build();

            pvOptions.setPicker(provinceItems, cityItems);
        }

        pvOptions.show();

    }

    private void handleMessage(Message message) {
        switch (message.what) {
            case USER_CREATE_DONE:
                Slog.d(TAG, "============handle message");
                setAccount(MyApplication.getContext(), account);
                setName(MyApplication.getContext(), nickName);
                setPassWord(MyApplication.getContext(), password);
                setAccountType(MyApplication.getContext(), 0);
                setLoginedAccountSex(MyApplication.getContext(), sex);
                //setYunXinAccount(MyApplication.getContext(), account);
                Intent intent = new Intent(CreateNewUser.this, LoginSplash.class);
                startActivity(intent);
                finish();
                break;
            case MSG_LOAD_INDUSTRY_DATA:
                if (threadIndustry == null) {
                    Slog.i(TAG, "行业数据开始解析");
                    threadIndustry = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            initJsondata("industry.json", true);
                        }
                    });
                    threadIndustry.start();
                }
                break;

            case MSG_LOAD_CITY_DATA:
                if (threadCity == null) {
                    Slog.i(TAG, "city数据开始解析");
                    threadCity = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            initJsondata("city.json", false);
                        }
                    });
                    threadCity.start();
                }
                break;

            case MSG_LOAD_SUCCESS:
                Slog.i(TAG, "数据获取成功");
                //isLoaded = true;
                break;

            case MSG_LOAD_FAILED:
                Log.i(TAG, "数据获取失败");
                break;
        }
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
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        //boolean b = shouldShowRequestPermissionRationale(permissions[0]);
                        permissionsAcquired = false;
                    }
                }
            }
        }
    }

    private void setCustomActionbar() {
        statusBarBack = findViewById(R.id.left_back);
        statusBarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.left_back), font);
        FontManager.markAsIconContainer(findViewById(R.id.create_next), font);
    }

    static class MyHandler extends HandlerTemp<CreateNewUser> {
        public MyHandler(CreateNewUser cls) {
            super(cls);
        }

        @Override
        public void handleMessage(Message message) {
            CreateNewUser createNewUser = ref.get();
            if (createNewUser != null) {
                createNewUser.handleMessage(message);
            }
        }
    }

}
        
