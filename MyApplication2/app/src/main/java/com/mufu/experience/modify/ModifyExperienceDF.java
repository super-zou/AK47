package com.mufu.experience.modify;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.gridlayout.widget.GridLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;

import com.mufu.R;
import com.mufu.adapter.GridImageAdapter;
import com.mufu.common.MyApplication;
import com.mufu.common.OnItemClickListener;
import com.mufu.dynamics.AddDynamicsActivity;
import com.mufu.experience.CheckAppointDate;
import com.mufu.experience.ExperienceDetailActivity;
import com.mufu.main.FullyGridLayoutManager;
import com.mufu.picture.GlideEngine;
import com.mufu.util.BaseDialogFragment;
import com.mufu.util.CommonBean;
import com.mufu.util.CommonPickerView;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;

import com.mufu.util.Slog;
import com.mufu.util.Utility;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.style.PictureWindowAnimationStyle;
import com.luck.picture.lib.tools.PictureFileUtils;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;
import static com.mufu.experience.CheckAppointDate.GET_EXPERIENCE_AVAILABLE_APPOINTMENT_DATE;
import static com.mufu.experience.ExperienceDetailActivity.GET_APPOINTMENT_DATE_DONE;
import static com.mufu.experience.ExperienceDetailActivity.GET_BANNER_PICTURES;
import static com.mufu.experience.ExperienceDetailActivity.GET_BANNER_PICTURES_DONE;
import static com.mufu.experience.ExperienceDetailActivity.GET_BASE_INFO;
import static com.mufu.experience.ExperienceDetailActivity.GET_BASE_INFO_DONE;
import static com.mufu.experience.ExperienceDetailActivity.GET_CHARGE_INFO;
import static com.mufu.experience.ExperienceDetailActivity.GET_CHARGE_INFO_DONE;
import static com.mufu.experience.ExperienceDetailActivity.GET_DURATION_INFO_DONE;
import static com.mufu.experience.ExperienceDetailActivity.GET_ITEM_INFO;
import static com.mufu.experience.ExperienceDetailActivity.GET_ITEM_INFO_DONE;
import static com.mufu.experience.ExperienceDetailActivity.GET_LIMIT_INFO;
import static com.mufu.experience.ExperienceDetailActivity.GET_LIMIT_INFO_DONE;
import static com.mufu.experience.ExperienceDetailActivity.GET_SELF_INTRODUCTION_DONE;

public class ModifyExperienceDF extends BaseDialogFragment implements OnDateSelectedListener {
    private static final boolean isDebug = true;
    private static final String TAG = "ModifyExperienceDF";
    private static final String SUBMIT_BASE_INFO_URL = HttpUtil.DOMAIN + "?q=experience/write_base_info";
    private static final String MODIFY_BASE_INFO_URL = HttpUtil.DOMAIN + "?q=experience/modify_experience_base_info";
    public static final String MODIFY_EXPERIENCE_PICTURES_URL = HttpUtil.DOMAIN + "?q=experience/modify_experience_pictures";
    public static final String MODIFY_ITEM_INFO = HttpUtil.DOMAIN + "?q=experience/modify_item_info";
    private static final String GET_DURATION_INFO = HttpUtil.DOMAIN + "?q=experience/get_duration_info";
    private static final String MODIFY_DURATION_INFO = HttpUtil.DOMAIN + "?q=experience/modify_duration";
    private static final String MODIFY_LIMITATION_URL = HttpUtil.DOMAIN + "?q=experience/modify_limitation_info";
    private static final String MODIFY_ADDRESS_URL = HttpUtil.DOMAIN + "?q=experience/modify_address_info";
    private static final String MODIFY_CHARGE_URL = HttpUtil.DOMAIN + "?q=experience/modify_charge_info";
    private static final String MODIFY_APPOINTMENT_DATE_URL = HttpUtil.DOMAIN + "?q=experience/modify_experience_available_date";
    private static final String GET_SELF_INTRODUCTION_URL = HttpUtil.DOMAIN + "?q=experience/get_self_introduction_info";
    private static final String MODIFY_SELF_INTRODUCTION_URL = HttpUtil.DOMAIN + "?q=experience/modify_self_introduction_info";
    private static final int WRITE_DONE_SUCCESS = 18;
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("EEE, d MMM yyyy");
    private boolean isBaseInfoModify = false;
    private boolean isPriceModify = false;
    private boolean isDurationModify = false;
    private boolean isGroupNumberLimitModify = false;
    private boolean isAddressModify = false;
    private boolean isSelfIntroductionModify = false;
    private static List<LocalDate> availableDateList = new ArrayList<>();
    private static List<CheckAppointDate.AppointDate> appointDateList = new ArrayList<>();
    MaterialCalendarView widget;
    
    CalendarDay today;
    private Thread threadCity;
    private boolean isExperiencePictureModified = false;
    private TextView leftBack;
    private Dialog mDialog;
    private Context mContext;
    private JSONArray bannerUrlArray;
    private String uri;
    private JSONArray itemJsonArray;
    private String mSavedItemString = "";
    private String mIntroduction;
    private int eid = 0;
    private EditText introductionET;
    private EditText selfIntroductionET;
    
    private EditText headLineET;
    private EditText groupCountET;
    private JSONArray dateJSONArray;
    private RecyclerView recyclerView;
    private GridImageAdapter adapter;
    private GridLayout mExperienceItemGL;
    private LinearLayout authenticateWrapper;
    private TextView addExperienceItem;
    private Button prevBtn;
    private Button nextBtn;
    private int index = 1;
    
    private List<LocalMedia> selectList = new ArrayList<>();
    private List<LocalMedia> newAddList = new ArrayList<>();
    private List<File> selectFileList = new ArrayList<>();
    private List<String> selectedDateList = new ArrayList<>();
    private List<String> cancelledDateList = new ArrayList<>();
    private AddDynamicsActivity addDynamicsActivity;
    private Button selectCityBtn;
    private EditText mChargeAmount;
    private EditText addressET;
    private EditText durationET;
    private Window window;
    
    private boolean isCityPicked = false;
    private Typeface font;
    private ArrayList<CommonBean> provinceItems = new ArrayList<>();
    private ArrayList<ArrayList<String>> cityItems = new ArrayList<>();
    private MyHandler myHandler;
    private String mBaseInfoString = "";
    private String mDeletedPictureUrl = "";

    private JSONObject experienceObject;
    private JSONObject chargeObject;
    private int duration;
    private int groupNumberLimit;
    
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_MaterialComponents_DialogWhenLarge);
        myHandler = new MyHandler(this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            eid = bundle.getInt("eid", 0);
        }
        
        mDialog.setContentView(R.layout.develop_experience);

        initView();

        mDialog.setCanceledOnTouchOutside(true);
        window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        leftBack = mDialog.findViewById(R.id.left_back);
        leftBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        
        font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.charge_duration_wrapper), font);

        if (addDynamicsActivity == null) {
            addDynamicsActivity = new AddDynamicsActivity();
        }

        return mDialog;
    }
    
    private void initView() {
        authenticateWrapper = mDialog.findViewById(R.id.experience_apply_form_wrapper);
        selectCityBtn = mDialog.findViewById(R.id.select_city);
        headLineET = mDialog.findViewById(R.id.create_headline_edit);
        introductionET = mDialog.findViewById(R.id.introduction_edittext);
        selfIntroductionET = mDialog.findViewById(R.id.self_introduction);
        mExperienceItemGL = mDialog.findViewById(R.id.contain_items_gridlayout);
        addExperienceItem = mDialog.findViewById(R.id.add_new_item);
        mChargeAmount = mDialog.findViewById(R.id.price_setting_edit);
        durationET = mDialog.findViewById(R.id.duration_edit);
        groupCountET = mDialog.findViewById(R.id.group_count_limit_edit);
        addressET = mDialog.findViewById(R.id.address_edit);
        widget = mDialog.findViewById(R.id.calendarView);
        widget.setOnDateChangedListener(this);
        
        prevBtn = mDialog.findViewById(R.id.prevBtn);
        nextBtn = mDialog.findViewById(R.id.nextBtn);

        selectCity();
        initPictureSelectWidget();
        initExperienceItem();
        navigationProcess();
        getExperienceData();
    }
    
    private void getExperienceData(){
        getBaseInformation();
        getExperiencePictures();
        getExperienceItems();
        getChargeInfo();
        getDurationInfo();
        getGroupNumberLimit();
        getAvailableDate();
        getSelfIntroduction();
    }
    
    private void getBaseInformation(){
        RequestBody requestBody = new FormBody.Builder()
                .add("eid", String.valueOf(eid))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_BASE_INFO, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
            String responseText = response.body().string();
                Slog.d(TAG, "==========getBaseInformation response body : " + responseText);
                if (responseText != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseText);
                        if (jsonObject != null){
                            experienceObject = jsonObject.optJSONObject("experience");
                            Slog.d(TAG, "==========getBaseInformation experienceObject : " + experienceObject);
                            if (experienceObject != null){
                                myHandler.sendEmptyMessage(GET_BASE_INFO_DONE);
                            }
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
 });
    }

    private void setBaseInfo(){
        selectCityBtn.setText(experienceObject.optString("city"));
        headLineET.setText(experienceObject.optString("title"));
        headLineET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0){
                    isBaseInfoModify = true;
                }
            }
        });
        
        introductionET.setText(experienceObject.optString("introduction"));

        introductionET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0){
                    isBaseInfoModify = true;
                }
            }
        });
        
        addressET.setText(experienceObject.optString("address"));
        addressET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0){
                    isAddressModify = true;
                }
            }
        });
    }
    
    private void getExperiencePictures(){
        RequestBody requestBody = new FormBody.Builder()
                .add("eid", String.valueOf(eid))
                .build();
                
        HttpUtil.sendOkHttpRequest(getContext(), GET_BANNER_PICTURES, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "==========getExperiencePictures response body : " + responseText);
                if (responseText != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseText);
                        bannerUrlArray = jsonObject.optJSONArray("uri_array");
                        Slog.d(TAG, "==========getExperiencePictures bannerUrlArray : " + bannerUrlArray);
                        if (bannerUrlArray != null && bannerUrlArray.length() > 0){
                            myHandler.sendEmptyMessage(GET_BANNER_PICTURES_DONE);
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
        }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
    private void setExperiencePictures(){
        List<LocalMedia> localMediaList = new ArrayList<>();
        for (int i=0; i<bannerUrlArray.length(); i++){
            LocalMedia localMedia = new LocalMedia();
            localMedia.setPath(HttpUtil.getDomain()+bannerUrlArray.opt(i));
            localMediaList.add(localMedia);
        }

        selectList.clear();
        selectList.addAll(localMediaList);
        adapter.setList(selectList);
        adapter.notifyDataSetChanged();
    }
    
    private void getExperienceItems(){
        RequestBody requestBody = new FormBody.Builder()
                .add("eid", String.valueOf(eid))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_ITEM_INFO, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "==========getContainItemInfo response body : " + responseText);
                if (responseText != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseText);
                        itemJsonArray = jsonObject.optJSONArray("item_array");
                        if (itemJsonArray != null && itemJsonArray.length() > 0){
                            myHandler.sendEmptyMessage(GET_ITEM_INFO_DONE);
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
    private void setItemInfoView(){

        if (itemJsonArray != null && itemJsonArray.length() > 0){
            ((EditText)mExperienceItemGL.getChildAt(0)).setText(itemJsonArray.optString(0));
            int itemLength = itemJsonArray.length();
            if (itemLength > 1){
                for (int i=1; i<itemLength; i++){
                    String content = itemJsonArray.optString(i);
                    View view = LayoutInflater.from(MyApplication.getContext())
                            .inflate(R.layout.experience_contain_item_edit, (ViewGroup) mDialog.findViewById(android.R.id.content), false);
                    mExperienceItemGL.addView(view);
                    EditText itemContent = view.findViewById(R.id.contain_item_introduction_edit);
                    itemContent.setText(content);
                }
            }

            for (int i=0; i<itemLength; i++){
                mSavedItemString += itemJsonArray.optString(i);
                if (i < itemLength-1){
                    mSavedItemString += ';';
                }
            }
        }

        FontManager.markAsIconContainer(mDialog.findViewById(R.id.item_wrapper), font);
    }
    
    private void getChargeInfo(){
        RequestBody requestBody = new FormBody.Builder()
                .add("eid", String.valueOf(eid))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_CHARGE_INFO, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "==========getChargeInfo response body : " + responseText);
                if (responseText != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseText);
                        chargeObject = jsonObject.optJSONObject("charge");
                        if (chargeObject != null){
                            myHandler.sendEmptyMessage(GET_CHARGE_INFO_DONE);
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
    private void setChargeEdit(){
        mChargeAmount.setText(String.valueOf(chargeObject.optInt("price")));
        mChargeAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                isPriceModify = true;
            }
        });
    }
    
    private void getDurationInfo(){
        RequestBody requestBody = new FormBody.Builder()
                .add("eid", String.valueOf(eid))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_DURATION_INFO, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "==========getDurationInfo response body : " + responseText);
                if (responseText != null) {
                try {
                        JSONObject jsonObject = new JSONObject(responseText);
                        duration = jsonObject.optInt("duration");
                        myHandler.sendEmptyMessage(GET_DURATION_INFO_DONE);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
    private void setDurationEdit(){
        durationET.setText(String.valueOf(duration));
        durationET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                isDurationModify = true;
            }
        });
    }
    
    private void getGroupNumberLimit(){
        RequestBody requestBody = new FormBody.Builder()
                .add("eid", String.valueOf(eid))
                .build();
                
        HttpUtil.sendOkHttpRequest(getContext(), GET_LIMIT_INFO, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "==========getGroupNumberLimit response body : " + responseText);
                if (responseText != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseText);
                        groupNumberLimit = jsonObject.optInt("amount");
                        myHandler.sendEmptyMessage(GET_LIMIT_INFO_DONE);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void setGroupNumberLimitEdit(){
        groupCountET.setText(String.valueOf(groupNumberLimit));
        groupCountET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                isGroupNumberLimitModify = true;
            }
        });
    }

    private void getAvailableDate(){
        appointDateList.clear();
        availableDateList.clear();
        selectedDateList.clear();
        cancelledDateList.clear();
        
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("eid", String.valueOf(eid))
                .add("type", String.valueOf(Utility.TalentType.EXPERIENCE.ordinal()));
        uri = GET_EXPERIENCE_AVAILABLE_APPOINTMENT_DATE;
        HttpUtil.sendOkHttpRequest(getContext(), uri, builder.build(), new Callback() {
            @Override
        public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug)
                    Slog.d(TAG, "==========getAvailableDate response body : " + responseText);
                if (responseText != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseText);
                        dateJSONArray = jsonObject.optJSONArray("dates");
                        processResponse();
                        myHandler.sendEmptyMessage(GET_APPOINTMENT_DATE_DONE);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
    private void processResponse(){
        if (dateJSONArray != null && dateJSONArray.length() > 0){
            for (int i=0; i<dateJSONArray.length(); i++){
                CheckAppointDate.AppointDate appointDate = new CheckAppointDate.AppointDate();
                try {
                    JSONObject dateObject = dateJSONArray.getJSONObject(i);
                    //appointDate.setDid(dateObject.optInt("did"));
                    appointDate.setEid(dateObject.optInt("eid"));
                    //appointDate.setCount(dateObject.optInt("count"));
                    appointDate.setLocalDate(LocalDate.parse(dateObject.optString("date_string"), FORMATTER));
                    appointDateList.add(appointDate);
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }
    }
    
    private void setAppointDateView() {
        today = CalendarDay.today();
        Slog.d(TAG, "-------------------->appointDateList size: "+appointDateList.size());
        for (int i=0; i<appointDateList.size(); i++){
            //JSONObject dateObject = dateJSONArray.getJSONObject(i);
            LocalDate localDate = appointDateList.get(i).getLocalDate();
            if (localDate.getDayOfMonth() >= today.getDay() || localDate.getMonthValue() > today.getMonth()){
                availableDateList.add(localDate);
                CalendarDay calendarDay = CalendarDay.from(localDate);
                widget.setDateSelected(calendarDay, true);
            }
        }
        
        //widget.setOnDateLongClickListener(this);
        final LocalDate min = LocalDate.of(today.getYear(), today.getMonth(), today.getDay());
        //final LocalDate max = LocalDate.of(today.getYear(), today.getMonth()+6, today.getDay());
       // widget.addDecorator(new AvailableDecorator());
        //widget.addDecorator(new CheckAppointDate.DayDisabledDecorator());

        widget.state().edit()
                .setMinimumDate(min)
                //.setMaximumDate(max)
                .commit();
    }
    
    private void getSelfIntroduction(){
        RequestBody requestBody = new FormBody.Builder()
                .add("eid", String.valueOf(eid))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_SELF_INTRODUCTION_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
            String responseText = response.body().string();
                Slog.d(TAG, "==========getSelfIntroduction response body : " + responseText);
                if (responseText != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseText);
                        //itemJsonArray = jsonObject.optJSONArray("routes");
                        mIntroduction = jsonObject.optString("introduction");
                        myHandler.sendEmptyMessage(GET_SELF_INTRODUCTION_DONE);

                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
    private void setSelfIntroductionEdit(){
        selfIntroductionET.setText(String.valueOf(mIntroduction));
        selfIntroductionET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                isSelfIntroductionModify = true;
            }
        });
    }
    
    @Override
    public void onDateSelected(
            @NonNull MaterialCalendarView widget,
            @NonNull CalendarDay date,
            boolean selected) {

        String dateStr;
        dateStr = FORMATTER.format(date.getDate());
        Slog.d(TAG, "----------------------------------->selected: " + selected + "   data: " + dateStr);
        if (selected) {
            if (cancelledDateList.contains(dateStr)){
                cancelledDateList.remove(dateStr);
            }else {
                selectedDateList.add(dateStr);
            }
        } else {
            if (selectedDateList.contains(dateStr)){
                selectedDateList.remove(dateStr);
            }

            if (availableDateList.contains(date.getDate())){
                Slog.d(TAG, "------------------------>cancel the date: "+dateStr);
                cancelledDateList.add(dateStr);
            }
        }
    }

private void saveExperiencePictures(){
        Map<String, String> authenMap = new HashMap<>();

        authenMap.put("eid", String.valueOf(eid));

        if (!TextUtils.isEmpty(mDeletedPictureUrl)){
            Slog.d(TAG, "----------------->saveExperiencePictures delete url: " + mDeletedPictureUrl);
            authenMap.put("delete_pictures", mDeletedPictureUrl);
        }

        if (newAddList.size() > 0) {
            Slog.d(TAG, "----------------->saveExperiencePictures newAddList size: " + newAddList.size());
            for (LocalMedia media : newAddList) {
                selectFileList.add(new File(media.getPath()));
            }
        }

        uploadPictures(authenMap, "authen", selectFileList);
    }
    
    private void uploadPictures(Map<String, String> params, String picKey, List<File> files) {
        Slog.d(TAG, "--------------------->uploadPictures file size: " + files.size());
        showProgressDialog("正在保存");
        uri = MODIFY_EXPERIENCE_PICTURES_URL;

        HttpUtil.uploadPictureHttpRequest(getContext(), params, picKey, files, uri, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
            if (response.body() != null) {
                    try {
                        String responseText = response.body().string();
                        Slog.d(TAG, "---------------->uploadPictures response: " + responseText);
                        int result = new JSONObject(responseText).optInt("result");

                        if (result == 1) {
                            dismissProgressDialog();
                           // isPictureSaved = true;
                            isExperiencePictureModified = false;
                            myHandler.sendEmptyMessage(WRITE_DONE_SUCCESS);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.submit_error), Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();

            }
        });

    }
    
    private void initExperienceItem() {
        addExperienceItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addExperienceItem();
            }
        });
    }

    private void addExperienceItem() {
        View view = LayoutInflater.from(MyApplication.getContext())
                .inflate(R.layout.experience_contain_item_edit, (ViewGroup) mDialog.findViewById(android.R.id.content), false);
        mExperienceItemGL.addView(view);
    }
    
    private void selectCity() {
        //init city data in thread
        if (threadCity == null) {
            threadCity = new Thread(new Runnable() {

                @Override
                public void run() {
                    initCityJsondata("city.json");
                }
            });
            threadCity.start();
        }
        selectCityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCityPickerView();
            }
        });
    }
    
    private void initPictureSelectWidget() {
        recyclerView = mDialog.findViewById(R.id.add_experience_pictures);
        FullyGridLayoutManager manager = new FullyGridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);

        adapter = new GridImageAdapter(getContext(), onAddPicClickListener);
        adapter.setList(selectList);
        adapter.setSelectMax(9);
        recyclerView.setAdapter(adapter);
        
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                if (selectList.size() > 0) {
                    LocalMedia media = selectList.get(position);
                    String pictureType = media.getMimeType();
                    int mediaType = PictureMimeType.getMimeType(pictureType);
                    switch (mediaType) {
                        case PictureConfig.TYPE_IMAGE:
                            //PictureSelector.create(MainActivity.this).externalPicturePreview(position, "/custom_file", selectList);
                            PictureSelector.create(ModifyExperienceDF.this)
                                    .themeStyle(R.style.picture_WeChat_style)
                                    .setPictureStyle(addDynamicsActivity.getWeChatStyle())
                                    //.setPictureWindowAnimationStyle(animationStyle)//
                                    .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                                    .isNotPreviewDownload(true)
                                    //.bindCustomPlayVideoCallback(callback)
                                    .loadImageEngine(GlideEngine.createGlideEngine())
                                    .openExternalPreview(position, selectList);
                            break;
}
                }
            }
        });

        adapter.setItemDeleteListener(new GridImageAdapter.OnPicDeleteListener() {
            @Override
            public void onPicDelete(int position) {
                Slog.d(TAG, "pic delete");
                isExperiencePictureModified = true;
                mDeletedPictureUrl += bannerUrlArray.opt(position)+";";
            }
        });
    }
    
    private void initCityJsondata(String jsonFile) {
        CommonPickerView commonPickerView = new CommonPickerView();
        provinceItems = commonPickerView.getOptionsMainItem(getContext(), jsonFile);
        cityItems = commonPickerView.getOptionsSubItems(provinceItems);
    }
    
    private void showCityPickerView() {// 弹出地址选择器
        //条件选择器
        OptionsPickerView pvOptions;
        pvOptions = new OptionsPickerBuilder(getContext(), new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                //返回的分别是二个级别的选中位置
                isCityPicked = true;
                isBaseInfoModify = true;
                String city = cityItems.get(options1).get(option2);
                selectCityBtn.setText(city);
            }
            }).setDecorView(window.getDecorView().findViewById(R.id.develop_experience))
                .isDialog(true).setLineSpacingMultiplier(1.2f).isCenterLabel(false).build();
        pvOptions.getDialog().getWindow().setGravity(Gravity.CENTER);
        pvOptions.setPicker(provinceItems, cityItems);
        pvOptions.show();
    }
    
    private void submitBaseInfo(boolean modified) {
        Slog.d(TAG, "------------------>submitBaseInfo modified: " + modified);
        mBaseInfoString = getBaseInfoJsonObject().toString();
        showProgressDialog(getContext().getString(R.string.saving_progress));
        FormBody.Builder builder;
        String uri = "";
        
        if (modified) {
            builder = new FormBody.Builder()
                    .add("eid", String.valueOf(eid))
                    .add("base_info", mBaseInfoString);
            uri = MODIFY_BASE_INFO_URL;
        } else {
            builder = new FormBody.Builder()
                    .add("base_info", mBaseInfoString);
            uri = SUBMIT_BASE_INFO_URL;
        }
        
        RequestBody requestBody = builder.build();

        HttpUtil.sendOkHttpRequest(mContext, uri, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "submitBaseInfo response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        eid = new JSONObject(responseText).optInt("eid");
                        dismissProgressDialog();
                        isBaseInfoModify = false;
                        myHandler.sendEmptyMessage(WRITE_DONE_SUCCESS);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void saveItemsInfo(String itemString) {
        showProgressDialog(getContext().getString(R.string.saving_progress));
        FormBody.Builder builder;
        String uri = "";
        builder = new FormBody.Builder()
                .add("eid", String.valueOf(eid))
                .add("item_string", itemString);
        uri = MODIFY_ITEM_INFO;
        
        RequestBody requestBody = builder.build();

        HttpUtil.sendOkHttpRequest(mContext, uri, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "saveItemsInfo response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        int result = new JSONObject(responseText).optInt("result");
                        if (result == 1){
                            mSavedItemString = itemString;
                            dismissProgressDialog();
                            myHandler.sendEmptyMessage(WRITE_DONE_SUCCESS);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void submitPrice() {
        showProgressDialog(getContext().getString(R.string.saving_progress));
        FormBody.Builder builder = new FormBody.Builder()
                .add("eid", String.valueOf(eid))
                .add("price", mChargeAmount.getText().toString());
                
                RequestBody requestBody = builder.build();
        HttpUtil.sendOkHttpRequest(mContext, MODIFY_CHARGE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "submitPrice response : " + responseText);
                
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        int result = new JSONObject(responseText).optInt("result");

                        if (result == 1) {
                            dismissProgressDialog();
                            isPriceModify = false;
                            myHandler.sendEmptyMessage(WRITE_DONE_SUCCESS);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void submitTime() {
        showProgressDialog(getContext().getString(R.string.saving_progress));
        FormBody.Builder builder = new FormBody.Builder()
                .add("eid", String.valueOf(eid))
                .add("duration", durationET.getText().toString());
                
                RequestBody requestBody = builder.build();
        HttpUtil.sendOkHttpRequest(mContext, MODIFY_DURATION_INFO, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "submitTime response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        int result = new JSONObject(responseText).optInt("result");
                        
                        if (result == 1) {
                            isDurationModify = false;
                            dismissProgressDialog();
                            myHandler.sendEmptyMessage(WRITE_DONE_SUCCESS);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
     private void submitLimitation() {
        showProgressDialog(getContext().getString(R.string.saving_progress));
        FormBody.Builder builder = new FormBody.Builder()
                .add("eid", String.valueOf(eid))
                .add("amount", groupCountET.getText().toString());

        RequestBody requestBody = builder.build();
        HttpUtil.sendOkHttpRequest(mContext, MODIFY_LIMITATION_URL, requestBody, new Callback() {
        
         @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "submitLimitation response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        int result = new JSONObject(responseText).optInt("result");

                        if (result == 1) {
                            isGroupNumberLimitModify = false;
                            dismissProgressDialog();
                            myHandler.sendEmptyMessage(WRITE_DONE_SUCCESS);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void submitAddress() {
        showProgressDialog(getContext().getString(R.string.saving_progress));
        FormBody.Builder builder = new FormBody.Builder()
                .add("eid", String.valueOf(eid))
                .add("address", addressET.getText().toString());
                
                RequestBody requestBody = builder.build();
        HttpUtil.sendOkHttpRequest(mContext, MODIFY_ADDRESS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "submitAddress response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        int result = new JSONObject(responseText).optInt("result");

                        if (result == 1) {
                            isAddressModify = false;
                            dismissProgressDialog();
                            myHandler.sendEmptyMessage(WRITE_DONE_SUCCESS);
                        }
                        
                         } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

private void submitAppointDate() {
        showProgressDialog(getContext().getString(R.string.saving_progress));
        String dateString = "";
        String cancelledDateString = "";

        FormBody.Builder builder = new FormBody.Builder()
                .add("eid", String.valueOf(eid))
                .add("type", String.valueOf(Utility.TalentType.EXPERIENCE.ordinal()));
                
                if (selectedDateList.size() > 0){
            for (int i = 0; i < selectedDateList.size(); i++) {
                if (i == selectedDateList.size() - 1) {
                    dateString += selectedDateList.get(i);
                } else {
                    dateString += selectedDateList.get(i) + ";";
                }
            }

            builder.add("new_date_string", dateString);
        }
        
         if (cancelledDateList.size() > 0){
            for (int i = 0; i < cancelledDateList.size(); i++) {
                if (i == cancelledDateList.size() - 1) {
                    cancelledDateString += cancelledDateList.get(i);
                } else {
                    cancelledDateString += cancelledDateList.get(i) + ";";
                }
            }

            builder.add("cancelled_date_string", cancelledDateString);
        }
        
        RequestBody requestBody = builder.build();

        HttpUtil.sendOkHttpRequest(mContext, MODIFY_APPOINTMENT_DATE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "submitAppointDate response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        int result = new JSONObject(responseText).optInt("result");
                         if (result == 1) {
                            cancelledDateList.clear();
                            selectedDateList.clear();
                            myHandler.sendEmptyMessage(WRITE_DONE_SUCCESS);
                            dismissProgressDialog();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

private void submitSelfIntroduction() {
        showProgressDialog(getContext().getString(R.string.saving_progress));
        FormBody.Builder builder = new FormBody.Builder()
                .add("eid", String.valueOf(eid))
                .add("introduction", selfIntroductionET.getText().toString());
                
                RequestBody requestBody = builder.build();
        HttpUtil.sendOkHttpRequest(mContext, MODIFY_SELF_INTRODUCTION_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "submitSelfIntroduction response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        int result = new JSONObject(responseText).optInt("result");

                        if (result == 1) {
                            isSelfIntroductionModify = false;
                            dismissProgressDialog();
                            myHandler.sendEmptyMessage(WRITE_DONE_SUCCESS);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void navigationProcess() {
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (index > 0) {
                 nextBtn.setText(getContext().getResources().getString(R.string.next));
                    index--;
                    if (index == 1) {
                        prevBtn.setVisibility(View.GONE);
                        leftBack.setVisibility(View.VISIBLE);
                    }
                    authenticateWrapper.getChildAt(index - 1).setVisibility(View.VISIBLE);
                    authenticateWrapper.getChildAt(index).setVisibility(View.GONE);
                }
            }
        });
        
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validCheck(index)) {
                    switch (index) {
                        case 3:
                            if (isBaseInfoModify) {
                                submitBaseInfo(true);
                            } else {
                                if (!TextUtils.isEmpty(mBaseInfoString) && !mBaseInfoString.equals(getBaseInfoJsonObject().toString())) {
                                    submitBaseInfo(true);
                                } else {
                                    processNextBtn();
                                }
                            }
                            break;
                            
                            case 4:
                            if (isExperiencePictureModified){
                                saveExperiencePictures();
                            }else {
                                processNextBtn();
                            }
                            break;
                        case 5:
                            String itemString = "";
                            if(mExperienceItemGL.getRowCount() > 0){
                                for (int i=0; i<mExperienceItemGL.getRowCount(); i++){
                                    EditText editText = (EditText) mExperienceItemGL.getChildAt(i);
                                    if (!TextUtils.isEmpty(editText.getText())){
                                        itemString += editText.getText().toString();
                                        if (i < mExperienceItemGL.getRowCount() - 1){
                                            itemString += ";";
                                        }
       }
                                }
                            }

                            if (!mSavedItemString.equals(itemString)){
                                saveItemsInfo(itemString);
                            }else {
                                processNextBtn();
                            }

                            break;
                            
                            case 6:
                            if (isPriceModify) {
                                submitPrice();
                            } else {
                                processNextBtn();
                            }
                            break;
                        case 7:
                            if (isDurationModify) {
                                submitTime();
                            } else {
                                processNextBtn();
                            }
                            break;
                            
                            case 8://number of people
                            if (isGroupNumberLimitModify) {
                                submitLimitation();
                            } else {
                                processNextBtn();
                            }
                            break;
                        case 9://number of people
                            if (isAddressModify) {
                                submitAddress();
                            } else {
                                processNextBtn();
                            }
                            break;
                            case 10:
                            if (selectedDateList.size() > 0 || cancelledDateList.size() >0) {
                                submitAppointDate();
                            } else {
                                processNextBtn();
                            }
                            break;
                        case 11:
                            if(isSelfIntroductionModify){
                                submitSelfIntroduction();
                            }else {
                                processNextBtn();
                            }
                            break;
                            default:
                            processNextBtn();
                            break;
                    }
                }
            }
        });
    }

    private void processNextBtn() {
        if (leftBack.getVisibility() == View.VISIBLE) {
            leftBack.setVisibility(View.GONE);
        }
        
        if (prevBtn.getVisibility() == View.GONE) {
            prevBtn.setVisibility(View.VISIBLE);
        }
        if (index < authenticateWrapper.getChildCount() - 2) {
            if (index == authenticateWrapper.getChildCount() - 3) {
                nextBtn.setText(getContext().getResources().getString(R.string.done));
            }
            authenticateWrapper.getChildAt(index).setVisibility(View.VISIBLE);
            authenticateWrapper.getChildAt(index - 1).setVisibility(View.GONE);
            index++;
        } else {
            startExperienceDetailActivity();
        }
    }
    
    private boolean validCheck(int index) {
        Slog.d(TAG, "------------------------>validCheck: " + index);
        boolean valid = false;
        switch (index) {
            case 1://select city
                valid = true;
                Slog.d(TAG, "---------------->city picked: "+isCityPicked);
                break;
            case 2://add headline
                if (!TextUtils.isEmpty(headLineET.getText().toString())) {
                    valid = true;
                } else {
                    valid = false;
                    Toast.makeText(getContext(), getResources().getString(R.string.headline_empty_notice), Toast.LENGTH_LONG).show();
                }
                break;
                case 3://experience introduction
                if (!TextUtils.isEmpty(introductionET.getText().toString())) {
                    valid = true;
                } else {
                    valid = false;
                    Toast.makeText(getContext(), getResources().getString(R.string.experience_introduction_notice), Toast.LENGTH_LONG).show();
                }
                break;
            case 4://experience pictures
                if (selectList.size() == 0){
                    valid = false;
                    Toast.makeText(getContext(), getResources().getString(R.string.experience_picture_notice), Toast.LENGTH_LONG).show();
                }else {
                    valid = true;
                }
                break;
                case 6:
                if (!TextUtils.isEmpty(mChargeAmount.getText())) {
                    valid = true;
                } else {
                    valid = false;
                    Toast.makeText(getContext(), getResources().getString(R.string.charge_empty_notice), Toast.LENGTH_LONG).show();
                    break;
                }

                break;
            case 7:
                if (!TextUtils.isEmpty(durationET.getText())){
                    valid = true;
                }else {
                    valid = false;
                    Toast.makeText(getContext(), getResources().getString(R.string.duration_empty_notice), Toast.LENGTH_LONG).show();
                }

                break;
                case 8:
                if (!TextUtils.isEmpty(groupCountET.getText())) {
                    valid = true;
                } else {
                    valid = false;
                    Toast.makeText(getContext(), getResources().getString(R.string.group_amount_empty_notice), Toast.LENGTH_LONG).show();
                }

                break;
            case 9:
                if (!TextUtils.isEmpty(addressET.getText())) {
                    valid = true;
                } else {
                    valid = false;
                    Toast.makeText(getContext(), getResources().getString(R.string.address_empty_notice), Toast.LENGTH_LONG).show();
                }

                break;
                case 10:
                if (cancelledDateList.size() == availableDateList.size() && selectedDateList.size() == 0) {
                    valid = false;
                    Toast.makeText(getContext(), getResources().getString(R.string.select_date_empty_notice), Toast.LENGTH_LONG).show();
                } else {
                    valid = true;
                }
                break;
            case 11://self introduction
                if (!TextUtils.isEmpty(selfIntroductionET.getText().toString())) {
                    valid = true;
                } else {
                    valid = false;
                    Toast.makeText(getContext(), getResources().getString(R.string.self_introduction_empty_notice), Toast.LENGTH_LONG).show();
                }
                break;
                
                default:
                valid = true;
                break;
        }

        return valid;
    }

    private JSONObject getBaseInfoJsonObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("city", selectCityBtn.getText().toString());
            jsonObject.put("title", headLineET.getText().toString());
            jsonObject.put("introduction", introductionET.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
    
    public void startExperienceDetailActivity() {
        Intent intent = new Intent(getContext(), ExperienceDetailActivity.class);
        intent.putExtra("eid", eid);
        startActivity(intent);
        mDialog.dismiss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
        switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    if (selectList.size() > 0) {
                        selectList.addAll(PictureSelector.obtainMultipleResult(data));
                    } else {
                        selectList = PictureSelector.obtainMultipleResult(data);
                    }

                    newAddList = PictureSelector.obtainMultipleResult(data);
                    Slog.d(TAG, "new Add pictures: " + newAddList.size());
                    adapter.setList(selectList);
                    adapter.notifyDataSetChanged();
                    isExperiencePictureModified = true;
                    break;
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PictureConfig.APPLY_STORAGE_PERMISSIONS_CODE:
                // 存储权限
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        PictureFileUtils.deleteCacheDirFile(getContext(), PictureMimeType.ofImage());
                    } else {
                        Toast.makeText(getContext(), getString(R.string.picture_jurisdiction), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }
    
    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
    }
    
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case GET_BASE_INFO_DONE:
                setBaseInfo();
                break;
            case GET_BANNER_PICTURES_DONE:
                setExperiencePictures();
                break;
            case GET_ITEM_INFO_DONE:
                setItemInfoView();
                break;
            case GET_CHARGE_INFO_DONE:
                setChargeEdit();
                break;
                case GET_DURATION_INFO_DONE:
                setDurationEdit();
                break;
            case GET_LIMIT_INFO_DONE:
                setGroupNumberLimitEdit();
                break;
            case GET_APPOINTMENT_DATE_DONE:
                setAppointDateView();
                break;
            case GET_SELF_INTRODUCTION_DONE:
                setSelfIntroductionEdit();
                break;
            case WRITE_DONE_SUCCESS:
                processNextBtn();
                break;
            default:
                break;
        }
    }
    
    private GridImageAdapter.onAddPicClickListener onAddPicClickListener = new GridImageAdapter.onAddPicClickListener() {
        @Override
        public void onAddPicClick() {
            //boolean mode = cb_mode.isChecked();
            boolean mode = true;
            if (mode) {
                PictureSelector.create(ModifyExperienceDF.this)
                        .openGallery(PictureMimeType.ofImage())
                        .loadImageEngine(GlideEngine.createGlideEngine())
                        .theme(R.style.picture_WeChat_style)
                        .isWeChatStyle(true)
                        .setPictureStyle(addDynamicsActivity.getWeChatStyle())
                        .setPictureCropStyle(addDynamicsActivity.getCropParameterStyle())
                        .setPictureWindowAnimationStyle(new PictureWindowAnimationStyle())
                        .isWithVideoImage(true)
                        .maxSelectNum(8)
                        .minSelectNum(1)
                        .maxVideoSelectNum(1)
                        .imageSpanCount(4)
                        .isReturnEmpty(false)
                        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        .selectionMode(PictureConfig.MULTIPLE)
                        .previewImage(true)
                        .isCamera(true)
                        .isZoomAnim(true)
                        .compress(true)
                        .compressQuality(100)
                        .synOrAsy(true)
                        .withAspectRatio(1, 1)
                        .freeStyleCropEnabled(true)
                        .previewEggs(true)
                        .minimumCompressSize(100)
                        .forResult(PictureConfig.CHOOSE_REQUEST);
            }
        }
    };
    
    static class MyHandler extends Handler {
        WeakReference<ModifyExperienceDF> modifyExperienceDialogFragmentWeakReference;

        MyHandler(ModifyExperienceDF modifyExperienceDialogFragment) {
            modifyExperienceDialogFragmentWeakReference = new WeakReference<ModifyExperienceDF>(modifyExperienceDialogFragment);
        }

        @Override
        public void handleMessage(Message message) {
            ModifyExperienceDF modifyExperienceDialogFragment = modifyExperienceDialogFragmentWeakReference.get();
            if (modifyExperienceDialogFragment != null) {
                modifyExperienceDialogFragment.handleMessage(message);
            }
        }
    }

}
