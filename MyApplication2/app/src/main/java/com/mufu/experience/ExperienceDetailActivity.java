package com.mufu.experience;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;

import com.bumptech.glide.Glide;
import com.mufu.R;
import com.mufu.adapter.DataBean;
import com.mufu.adapter.GuideBannerAdapter;
import com.mufu.common.BaseAppCompatActivity;
import com.mufu.common.MyApplication;
import com.mufu.consult.ConsultSummaryActivity;
import com.mufu.contacts.ChatActivity;
import com.mufu.picture.GlideEngine;
import com.mufu.util.CommonDialogFragmentInterface;
import com.mufu.util.DateUtil;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.RoundImageView;
import com.mufu.util.Slog;
import com.mufu.util.Utility;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.entity.LocalMedia;
import com.tencent.imsdk.TIMConversationType;
import com.tencent.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.willy.ratingbar.RotationRatingBar;
import com.youth.banner.Banner;
import com.youth.banner.config.IndicatorConfig;
import com.youth.banner.indicator.CircleIndicator;
import com.youth.banner.itemdecoration.MarginDecoration;
import com.youth.banner.listener.OnBannerListener;
import com.youth.banner.transformer.DepthPageTransformer;
import com.youth.banner.util.BannerUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.mufu.common.MyApplication.getContext;
import static com.mufu.util.ParseUtils.startMeetArchiveActivity;

public class ExperienceDetailActivity extends BaseAppCompatActivity implements CommonDialogFragmentInterface {
    private static final boolean isDebug = true;
    private static final String TAG = "ExperienceDetailActivity";
    public static final String GET_BANNER_PICTURES = HttpUtil.DOMAIN + "?q=experience/get_experience_pictures";
    public static final String GET_BASE_INFO = HttpUtil.DOMAIN + "?q=experience/get_experience_base_info";
    public static final String GET_ITEM_INFO = HttpUtil.DOMAIN + "?q=experience/get_item_info";
    public static final String GET_TALENT_INFO = HttpUtil.DOMAIN + "?q=experience/get_talent_info";
    public static final String GET_LIMIT_INFO = HttpUtil.DOMAIN + "?q=experience/get_experience_limit_info";
    public static final String GET_CHARGE_INFO = HttpUtil.DOMAIN + "?q=experience/get_experience_charge_info";
    public static final String GET_EVALUATE_INFO = HttpUtil.DOMAIN + "?q=experience/get_experience_evaluate_info";
    public static final int GET_BANNER_PICTURES_DONE = 1;
    public static final int GET_BASE_INFO_DONE = 2;
    public static final int GET_ITEM_INFO_DONE = 3;
    public static final int GET_TALENT_INFO_DONE = 4;
    public static final int GET_LIMIT_INFO_DONE = 5;
    public static final int GET_CHARGE_INFO_DONE = 6;
    public static final int GET_EVALUATE_INFO_DONE = 7;
    public static final int GET_DURATION_INFO_DONE = 8;
    public static final int GET_APPOINTMENT_DATE_DONE = 9;
    public static final int GET_SELF_INTRODUCTION_DONE = 10;
    public static final int GET_PACKAGE_AMOUNT_DONE = 11;
    
    final int itemLimit = 3;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    private int mLoadSize = 0;
    private int mUpdateSize = 0;
    private Handler handler;
    private int mType = Utility.TalentType.EXPERIENCE.ordinal();
    private ViewGroup myGroupView;
    private int eid;
    private int rid = 1;
    private JSONArray bannerUrlArray;
    private JSONObject experienceObject;
    private String additionalServiceStr;
    private JSONArray evaluateJsonArray;
    private int evaluateCount;
    private int consultCount;
    private JSONArray itemJsonArray;
    private JSONObject talentObject;
    private JSONObject limitationsObject;
    private JSONObject chargeObject;
    private String address;
    private Typeface font;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.experience_detail);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        handler = new ExperienceDetailActivity.MyHandler(this);
        if (getIntent() != null){
            eid = getIntent().getIntExtra("eid", 0);
        }
        
        initView();

        getBannerPictures();
        getBaseInformation();
        getContainItemInfo();
        getTalentIntroduction();
        getChargeInfo();
        getEvaluateInfo();
    }
    
    private void initView() {
        font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.left_back), font);
        FontManager.markAsIconContainer(findViewById(R.id.cancellation_detail_nav), font);
        FontManager.markAsIconContainer(findViewById(R.id.cny), font);
        FontManager.markAsIconContainer(findViewById(R.id.evaluate_star), font);
        FontManager.markAsIconContainer(findViewById(R.id.experience_head), font);
        FontManager.markAsIconContainer(findViewById(R.id.cancellation_detail_nav), font);

        TextView leftBack = findViewById(R.id.left_back);
        leftBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        
        Button checkAppointDateBtn = findViewById(R.id.check_appointment_date);
        checkAppointDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAppointDate();
            }
        });

        TextView cancellationDetailNavTV = findViewById(R.id.cancellation_detail_nav);
        cancellationDetailNavTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCancellationDialog();
            }
        });

    }
    
    private void showCancellationDialog(){
        ExperienceCancellationDF experienceCancellationDF = ExperienceCancellationDF.newInstance();
        experienceCancellationDF.show(getSupportFragmentManager(), "ExperienceCancellationDF");
    }

    private void checkAppointDate(){
        CheckAppointDate checkAppointDate = CheckAppointDate.newInstance(eid, chargeObject.optInt("price"),
                experienceObject.optString("title"), experienceObject.optInt("amount"));
        checkAppointDate.show(getSupportFragmentManager(), "CheckAppointDate");
    }
    
    private void getBannerPictures(){
        RequestBody requestBody = new FormBody.Builder()
                .add("eid", String.valueOf(eid))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_BANNER_PICTURES, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug)
                    Slog.d(TAG, "==========getBannerPictures response body : " + responseText);
                    if (responseText != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseText);
                        bannerUrlArray = jsonObject.optJSONArray("uri_array");
                        Slog.d(TAG, "==========getBannerPictures bannerUrlArray : " + bannerUrlArray);
                        if (bannerUrlArray != null && bannerUrlArray.length() > 0){
                            handler.sendEmptyMessage(GET_BANNER_PICTURES_DONE);
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
    
    public void useBanner() {
        //--------------------------简单使用-------------------------------
        //创建（new banner()）或者布局文件中获取banner
        Banner banner = (Banner) findViewById(R.id.banner);
        DataBean dataBean = new DataBean();
        if(bannerUrlArray != null){
            try {
                for (int i=0; i<bannerUrlArray.length(); i++){
                    dataBean.setBannerData((String) bannerUrlArray.get(i));
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        
        //--------------------------详细使用-------------------------------
        banner.setAdapter(new GuideBannerAdapter(dataBean.getBannerData()));
        banner.isAutoLoop(true);
        banner.setIndicator(new CircleIndicator(this));
        banner.setIndicatorSelectedColorRes(R.color.background);
        banner.setIndicatorNormalColorRes(R.color.white);
        banner.setIndicatorGravity(IndicatorConfig.Direction.CENTER);
        banner.setIndicatorSpace((int)BannerUtils.dp2px(20));
        banner.setIndicatorMargins(new IndicatorConfig.Margins((int) BannerUtils.dp2px(10)));
        banner.setIndicatorWidth(25,35);
        banner.addItemDecoration(new MarginDecoration(0));
        banner.setPageTransformer(new DepthPageTransformer());
        banner.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(Object data, int position) {

            }
        });
        banner.start();
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
                                handler.sendEmptyMessage(GET_BASE_INFO_DONE);
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
    
    private void setBaseInfoView(){
        TextView titleTV = findViewById(R.id.title);
        TextView regionTV = findViewById(R.id.region);
        TextView durationTV = findViewById(R.id.duration);
        TextView groupLimitTV = findViewById(R.id.group_limit);
        TextView serviceContent = findViewById(R.id.service_content);
        TextView addressTV = findViewById(R.id.address_info);

        titleTV.setText(experienceObject.optString("title"));
        regionTV.setText(experienceObject.optString("city"));
        durationTV.setText(experienceObject.optString("duration")+"小时");
        groupLimitTV.setText("最多"+experienceObject.optString("amount")+"人");
        serviceContent.setText(experienceObject.optString("introduction"));
        addressTV.setText(experienceObject.optString("address"));
    }
    
    private void getEvaluateInfo(){
        RequestBody requestBody = new FormBody.Builder()
                .add("eid", String.valueOf(eid))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_EVALUATE_INFO, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "==========getEvaluateInfo response body : " + responseText);
                if (responseText != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseText);
                        evaluateJsonArray = jsonObject.optJSONArray("evaluates");
                        evaluateCount = jsonObject.optInt("count");
                        if (evaluateCount != 0){
                            handler.sendEmptyMessage(GET_EVALUATE_INFO_DONE);
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
    
    private void setEvaluateInfoView(){
        LinearLayout evaluateWrapper = findViewById(R.id.evaluate_wrapper);
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int innerWidth = dm.widthPixels - (int) Utility.dpToPx(getContext(), 38f);
        float ratingSum = 0;
        if (evaluateJsonArray != null && evaluateJsonArray.length() > 0){
            for (int i=0; i<evaluateJsonArray.length(); i++){
            JSONObject evaluateObject = evaluateJsonArray.optJSONObject(i);
                View evaluateItem = LayoutInflater.from(getContext()).inflate(R.layout.guide_evaluate_item, (ViewGroup) findViewById(android.R.id.content), false);
                evaluateWrapper.addView(evaluateItem);
                ImageView evaluateAvatar = evaluateItem.findViewById(R.id.evaluate_avatar);
                Glide.with(getContext()).load(HttpUtil.DOMAIN+evaluateObject.optString("avatar")).into(evaluateAvatar);
                TextView evaluateName = evaluateItem.findViewById(R.id.evaluate_name);
                evaluateName.setText(evaluateObject.optString("nickname"));
                TextView createdTV = evaluateItem.findViewById(R.id.evaluate_time);
                createdTV.setText(DateUtil.timeStamp2String(evaluateObject.optLong("created")));
                TextView evaluateContent = evaluateItem.findViewById(R.id.evaluate_content);
                evaluateContent.setText(evaluateObject.optString("content"));
                RotationRatingBar ratingBar = evaluateItem.findViewById(R.id.rating_bar);
                ratingBar.setRating((float) evaluateObject.optDouble("rating"));

                ratingSum += evaluateObject.optInt("rating");
                GridLayout evaluatePictureWrapper = evaluateItem.findViewById(R.id.evaluate_picture_grid);

                setEvaluateItemPicture(evaluatePictureWrapper, evaluateObject, innerWidth);
            }

            LinearLayout evaluateLL = findViewById(R.id.evaluate_summary);
            ConstraintLayout avaluateCL = findViewById(R.id.evaluate_statistics);
            
            if (evaluateCount > 0){
                evaluateLL.setVisibility(View.VISIBLE);
                avaluateCL.setVisibility(View.VISIBLE);
            }
            TextView headRatingTV = findViewById(R.id.rating);
            TextView ratingTV = findViewById(R.id.evaluate_rating);
            float average = ratingSum / evaluateJsonArray.length();
            float averageScore = (float) (Math.round(average * 10)) / 10;
            ratingTV.setText(String.valueOf(averageScore));
            headRatingTV.setText(String.valueOf(averageScore));
            
            TextView headEvaluateCountTV = findViewById(R.id.count);
            TextView evaluateCountTV = findViewById(R.id.evaluate_count);
            evaluateCountTV.setText("("+evaluateCount+")");
            headEvaluateCountTV.setText("("+evaluateCount+")");

            TextView showAllEvaluateBtn = findViewById(R.id.show_all);
            if (evaluateCount > 3){
                showAllEvaluateBtn.setVisibility(View.VISIBLE);
                showAllEvaluateBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ShowAllEvaluateDF showAllEvaluateDF = ShowAllEvaluateDF.newInstance(mType, eid, evaluateCount);
                        showAllEvaluateDF.show(getSupportFragmentManager(), "ShowAllEvaluateDF");
                    }
                });
              }else {
                showAllEvaluateBtn.setVisibility(View.GONE);
            }
        }
    }

    private void setEvaluateItemPicture(GridLayout evaluatePictureWrapper, JSONObject jsonObject, int innerWidth){
        JSONArray pictureArray = jsonObject.optJSONArray("pictures");
        if (pictureArray != null && pictureArray.length() > 0){
            for (int i=0; i<pictureArray.length(); i++){
            RoundImageView imageView = new RoundImageView(this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(innerWidth/3, innerWidth/3);
                layoutParams.setMargins(0, 0, 3, 4);
                //将以上的属性赋给LinearLayout
                imageView.setLayoutParams(layoutParams);
                imageView.setAdjustViewBounds(true);
                //imageView.setLayoutParams(new ViewGroup.LayoutParams(innerWidth/3, innerWidth/3));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                evaluatePictureWrapper.addView(imageView);
                Glide.with(getContext()).load(HttpUtil.DOMAIN+pictureArray.optString(i)).into(imageView);
                imageView.setId(i);;
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startPicturePreview(imageView.getId(), pictureArray);
                    }
                });
            }
        }
    }
    
    public void startPicturePreview(int position, JSONArray pictureUrlArray){

        List<LocalMedia> localMediaList = new ArrayList<>();
        for (int i=0; i<pictureUrlArray.length(); i++){
            LocalMedia localMedia = new LocalMedia();
            localMedia.setPath(HttpUtil.getDomain()+pictureUrlArray.opt(i));
            localMediaList.add(localMedia);
        }

        PictureSelector.create(this)
                .themeStyle(R.style.picture_default_style) // xml设置主题
                .loadImageEngine(GlideEngine.createGlideEngine())// 外部传入图片加载引擎，必传项
                .isNotPreviewDownload(true)
                .openExternalPreview(position, localMediaList);

    }
    
     private void getContainItemInfo(){
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
                            handler.sendEmptyMessage(GET_ITEM_INFO_DONE);
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
        LinearLayout routeWrapper = findViewById(R.id.item_wrapper);
        if (itemJsonArray != null && itemJsonArray.length() > 0){
            for (int i=0; i<itemJsonArray.length(); i++){
                String content = itemJsonArray.optString(i);
                View itemView = LayoutInflater.from(getContext()).inflate(R.layout.experience_contain_item, (ViewGroup) findViewById(android.R.id.content), false);
                routeWrapper.addView(itemView);
                TextView itemContent = itemView.findViewById(R.id.item_content);
                itemContent.setText(content);
            }
        }

        FontManager.markAsIconContainer(findViewById(R.id.item_wrapper), font);
    }
    
    private void getTalentIntroduction(){
        RequestBody requestBody = new FormBody.Builder()
                .add("eid", String.valueOf(eid))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_TALENT_INFO, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "==========getTalentIntroduction response body : " + responseText);
                if (responseText != null) {
                    try {
                    JSONObject jsonObject = new JSONObject(responseText);
                        //itemJsonArray = jsonObject.optJSONArray("routes");
                        talentObject = jsonObject.optJSONObject("talent");
                        if (talentObject != null){
                            handler.sendEmptyMessage(GET_TALENT_INFO_DONE);
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
    
    private void setTalentIntroductionView(){
        RoundImageView avatar = findViewById(R.id.avatar);
        Glide.with(getContext()).load(HttpUtil.DOMAIN + talentObject.optString("avatar")).into(avatar);
        TextView name = findViewById(R.id.guide_talent_name);
        name.setText(talentObject.optString("nickname"));
        TextView selfIntroduction = findViewById(R.id.guide_self_introduction);
        selfIntroduction.setText(talentObject.optString("introduction"));

        Button contactBtn = findViewById(R.id.contact_talent);
        contactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contactTalent();
            }
        });
        
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMeetArchiveActivity(getContext(), talentObject.optInt("uid"));
            }
        });

        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                avatar.callOnClick();
            }
        });
    }
    
    private void contactTalent() {
        ChatInfo chatInfo = new ChatInfo();
        chatInfo.setType(TIMConversationType.C2C);
        chatInfo.setId(talentObject.optString("uid"));
        chatInfo.setChatName(talentObject.optString("nickname"));

        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("CHAT_INFO", chatInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void getAppointmentLimit(){
        RequestBody requestBody = new FormBody.Builder()
                .add("eid", String.valueOf(eid))
                .build();
                
                HttpUtil.sendOkHttpRequest(getContext(), GET_LIMIT_INFO, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "==========getAppointmentLimit response body : " + responseText);
                if (responseText != null) {
                try {
                        JSONObject jsonObject = new JSONObject(responseText);
                        limitationsObject = jsonObject.optJSONObject("limitations");
                        if (limitationsObject != null){
                            handler.sendEmptyMessage(GET_LIMIT_INFO_DONE);
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
    
    private void setLimitInfoView(){
        String limitationStr = limitationsObject.optString("condition");
        LinearLayout wrapper = findViewById(R.id.limit_condition_wrapper);
        if (!TextUtils.isEmpty(limitationStr)){
            String[] limitationArray = limitationStr.split("；");
            for (int i=0; i<limitationArray.length; i++){
                TextView textView = new TextView(this);
                //textView.setTextAppearance(R.style.TextSourceHanSansAppearance);
                textView.setText(getResources().getString(R.string.dot)+" "+limitationArray[i]);
                wrapper.addView(textView);
                textView.setTextColor(getResources().getColor(R.color.black ));
                textView.setTextSize(16);
            }
        }
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
                            handler.sendEmptyMessage(GET_CHARGE_INFO_DONE);
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
    
    private void setChargeView(){
        TextView money = findViewById(R.id.money);
        money.setText(String.valueOf(chargeObject.optInt("price")));
    }
    
    public void handleMessage(Message message) {
        switch (message.what) {
            case GET_BANNER_PICTURES_DONE:
                useBanner();
                break;
            case GET_BASE_INFO_DONE:
                setBaseInfoView();
                break;
            case GET_ITEM_INFO_DONE:
                setItemInfoView();
                break;
            case GET_TALENT_INFO_DONE:
                setTalentIntroductionView();
                break;
            case GET_LIMIT_INFO_DONE:
                setLimitInfoView();
                break;
            case GET_CHARGE_INFO_DONE:
                setChargeView();
                break;
            case GET_EVALUATE_INFO_DONE:
                setEvaluateInfoView();
                break;
            default:
                break;
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Activity.RESULT_FIRST_USER) {
            switch (resultCode) {
                case RESULT_OK:

                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onBackFromDialog(int cid, int tid, boolean status) {
        Slog.d(TAG, "-------------------onBackFromDialog cid: "+cid+"  tid: "+tid+"  status: "+status);
        Intent intent = new Intent(ExperienceDetailActivity.this, ConsultSummaryActivity.class);
        intent.putExtra("tid", tid);
        startActivity(intent);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    
    static class MyHandler extends Handler {
        WeakReference<ExperienceDetailActivity> experienceDetailActivityWeakReference;

        MyHandler(ExperienceDetailActivity experienceDetailActivity) {
            experienceDetailActivityWeakReference = new WeakReference<ExperienceDetailActivity>(experienceDetailActivity);
        }

        @Override
        public void handleMessage(Message message) {
            ExperienceDetailActivity experienceDetailActivity = experienceDetailActivityWeakReference.get();
            if (experienceDetailActivity != null) {
                experienceDetailActivity.handleMessage(message);
            }
        }
    }
}

    
                
