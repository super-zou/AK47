package com.hetang.experience;

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
import com.hetang.R;
import com.hetang.adapter.DataBean;
import com.hetang.adapter.GuideBannerAdapter;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.common.MyApplication;
import com.hetang.consult.ConsultSummaryActivity;
import com.hetang.contacts.ChatActivity;
import com.hetang.picture.GlideEngine;
import com.hetang.consult.TalentConsultDF;
import com.hetang.talent.TalentDetailsActivity;
import com.hetang.util.CommonDialogFragmentInterface;
import com.hetang.util.DateUtil;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.RoundImageView;
import com.hetang.util.Slog;

import com.hetang.util.Utility;
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

import static com.hetang.common.MyApplication.getContext;

public class GuideDetailActivity extends BaseAppCompatActivity implements CommonDialogFragmentInterface{
    private static final boolean isDebug = true;
    private static final String TAG = "GuideDetailActivity";
    public static final String GET_BANNER_PICTURES = HttpUtil.DOMAIN + "?q=travel_guide/get_route_pictures";
    public static final String GET_BASE_INFO = HttpUtil.DOMAIN + "?q=travel_guide/get_base_info";
    public static final String GET_ROUTE_INFO = HttpUtil.DOMAIN + "?q=travel_guide/get_route_info";
    private static final String GET_GUIDE_INFO = HttpUtil.DOMAIN + "?q=travel_guide/get_guide_info";
    private static final String GET_LIMIT_INFO = HttpUtil.DOMAIN + "?q=travel_guide/get_limit_info";
    private static final String GET_CHARGE_INFO = HttpUtil.DOMAIN + "?q=travel_guide/get_charge_info";
        private static final String GET_EVALUATE_INFO = HttpUtil.DOMAIN + "?q=travel_guide/get_evaluate_info";
    private static final String GET_CONSULT_STATISTICS_INFO = HttpUtil.DOMAIN + "?q=consult/get_consult_statistics";
    private static final int GET_BANNER_PICTURES_DONE = 1;
    private static final int GET_BASE_INFO_DONE = 2;
    private static final int GET_ROUTE_INFO_DONE = 3;
    private static final int GET_GUIDE_INFO_DONE = 4;
    private static final int GET_LIMIT_INFO_DONE = 5;
    private static final int GET_CHARGE_INFO_DONE = 6;
    private static final int GET_EVALUATE_INFO_DONE = 7;
    private static final int GET_CONSULT_INFO_DONE = 8;
    final int itemLimit = 3;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    private int mLoadSize = 0;
    private int mUpdateSize = 0;
    private Handler handler;
    private int type = Utility.TalentType.GUIDE.ordinal();
    private ViewGroup myGroupView;
        private int sid;
    private int tid;
    private int rid = 1;
    private JSONArray bannerUrlArray;
    private JSONObject guideServiceObject;
    private String additionalServiceStr;
        private JSONArray evaluateJsonArray;
    private int evaluateCount;
    private int consultCount;
    private JSONArray routeJsonArray;
    private JSONObject guideObject;
    private JSONObject limitationsObject;
    private JSONObject chargeObject;
        private Typeface font;
    
     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide_detail);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        handler = new GuideDetailActivity.MyHandler(this);
        if (getIntent() != null){
            sid = getIntent().getIntExtra("sid", 0);
        }
        
        initView();
        getBannerPictures();
        getBaseInformation();
        getEvaluateInfo();
        getRouteInfo();
        getGuideIntroduction();
        getAppointmentLimit();
        getChargeInfo();
    }
    
    private void initView() {
        font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.left_back), font);
        FontManager.markAsIconContainer(findViewById(R.id.cancellation_detail_nav), font);
        FontManager.markAsIconContainer(findViewById(R.id.cny), font);
        FontManager.markAsIconContainer(findViewById(R.id.evaluate_star), font);
        FontManager.markAsIconContainer(findViewById(R.id.guide_head), font);

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
        CheckAppointDate checkAppointDate = CheckAppointDate.newInstance(sid, chargeObject.optInt("price"), chargeObject.optString("unit"), guideServiceObject.optString("title"));
        checkAppointDate.show(getSupportFragmentManager(), "CheckAppointDate");
    }
    
    private void getBannerPictures(){
        RequestBody requestBody = new FormBody.Builder()
                .add("sid", String.valueOf(sid))
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
                        bannerUrlArray = jsonObject.optJSONArray("url_array");
                        Slog.d(TAG, "==========getBannerPictures bannerUrlArray : " + bannerUrlArray);
                        handler.sendEmptyMessage(GET_BANNER_PICTURES_DONE);
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
        banner.setIndicator(new CircleIndicator(this));
        banner.isAutoLoop(true);
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
    }
    
     private void getBaseInformation(){
        RequestBody requestBody = new FormBody.Builder()
                .add("sid", String.valueOf(sid))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_BASE_INFO, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                    Slog.d(TAG, "==========getBaseInformation response body : " + responseText);
                if (responseText != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseText);
                        guideServiceObject = jsonObject.optJSONObject("guide_service");
                        additionalServiceStr = jsonObject.optString("additional_service");
                        handler.sendEmptyMessage(GET_BASE_INFO_DONE);
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
        TextView title = findViewById(R.id.title);
        TextView score = findViewById(R.id.score);
        TextView commentCount = findViewById(R.id.comment_count);
        TextView region = findViewById(R.id.region);
        TextView serviceContent = findViewById(R.id.service_content);
        LinearLayout additionalServiceWrapper = findViewById(R.id.additional_service);

        try {
            title.setText(guideServiceObject.getString("title"));
            region.setText(guideServiceObject.getString("city"));
            serviceContent.setText(guideServiceObject.optString("introduction"));
            if(!TextUtils.isEmpty(additionalServiceStr)){
            String[] additionalServiceArray = additionalServiceStr.split(";");
                if (additionalServiceArray.length > 0){
                    for (int i=0; i<additionalServiceArray.length; i++){
                        String content = additionalServiceArray[i];
                        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.experience_contain_item, (ViewGroup) findViewById(android.R.id.content), false);
                        additionalServiceWrapper.addView(itemView);
                        TextView itemContent = itemView.findViewById(R.id.item_content);
                        itemContent.setText(content);
                    }
                     FontManager.markAsIconContainer(findViewById(R.id.additional_service), font);
                }
            }
        }catch (JSONException e){
            e.printStackTrace();
        }


    }
    
     private void getEvaluateInfo(){
        RequestBody requestBody = new FormBody.Builder()
                .add("sid", String.valueOf(sid))
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
                        handler.sendEmptyMessage(GET_EVALUATE_INFO_DONE);
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
         evaluateWrapper.setVisibility(View.VISIBLE);
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
            TextView showAllEvaluateBtn = findViewById(R.id.show_all);
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
            
             if (evaluateCount > 3){
                showAllEvaluateBtn.setVisibility(View.VISIBLE);
                showAllEvaluateBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ShowAllEvaluateDF showAllEvaluateDF = ShowAllEvaluateDF.newInstance(type, sid, evaluateCount);
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
                //Slog.d(TAG, "-------------->picture uri: "+pictureArray.optString(i));
                //View pictureItem = LayoutInflater.from(getContext()).inflate(R.layout.route_picture_item, (ViewGroup) findViewById(android.R.id.content), false);
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
    
     private void getRouteInfo(){
        RequestBody requestBody = new FormBody.Builder()
                .add("sid", String.valueOf(sid))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_ROUTE_INFO, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "==========getRouteInfo response body : " + responseText);
                if (responseText != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseText);
                        routeJsonArray = jsonObject.optJSONArray("routes");
                        handler.sendEmptyMessage(GET_ROUTE_INFO_DONE);
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

private void setRouteInfoView(){
        LinearLayout routeWrapper = findViewById(R.id.route_wrapper);
        if (routeJsonArray != null && routeJsonArray.length() > 0){
            for (int i=0; i<routeJsonArray.length(); i++){
                JSONObject routeObject = routeJsonArray.optJSONObject(i);
                View routeItem = LayoutInflater.from(getContext()).inflate(R.layout.route_item, (ViewGroup) findViewById(android.R.id.content), false);
                routeWrapper.addView(routeItem);
                TextView routeName = routeItem.findViewById(R.id.route_name);
                TextView routeContent = routeItem.findViewById(R.id.route_content);
                routeName.setText(routeObject.optString("name"));
                routeContent.setText(routeObject.optString("introduction"));
                LinearLayout routePictureWrapper = routeItem.findViewById(R.id.route_picture_wrapper);

                setRouteItemPicture(routePictureWrapper, routeObject);
 }
        }
    }

    private void setRouteItemPicture(LinearLayout routePictureWrapper, JSONObject jsonObject){
        JSONArray pictureArray = jsonObject.optJSONArray("pictures");
        if (pictureArray != null && pictureArray.length() > 0){
            for (int i=0; i<pictureArray.length(); i++){
                //Slog.d(TAG, "-------------->picture uri: "+pictureArray.optString(i));
                View pictureItem = LayoutInflater.from(getContext()).inflate(R.layout.route_picture_item, (ViewGroup) findViewById(android.R.id.content), false);
                routePictureWrapper.addView(pictureItem);
                ImageView routePicture = pictureItem.findViewById(R.id.route_picture);
                Glide.with(getContext()).load(HttpUtil.DOMAIN+pictureArray.optString(i)).into(routePicture);
            }
        }
    }
    
    private void getGuideIntroduction(){
        RequestBody requestBody = new FormBody.Builder()
                .add("sid", String.valueOf(sid))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_GUIDE_INFO, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "==========getGuideIntroduction response body : " + responseText);
                if (responseText != null) {
                    try {
                    JSONObject jsonObject = new JSONObject(responseText);
                        //routeJsonArray = jsonObject.optJSONArray("routes");
                        guideObject = jsonObject.optJSONObject("guide");
                        tid = guideObject.optInt("tid");
                        handler.sendEmptyMessage(GET_GUIDE_INFO_DONE);
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
    
    private void setGuideIntroductionView(){
        RoundImageView avatar = findViewById(R.id.avatar);
        Glide.with(this).load(HttpUtil.DOMAIN + guideObject.optString("avatar")).into(avatar);
        TextView name = findViewById(R.id.guide_talent_name);
        name.setText(guideObject.optString("nickname"));
        TextView selfIntroduction = findViewById(R.id.guide_self_introduction);
        selfIntroduction.setText(guideObject.optString("introduction"));
        
        Button consultBtn = findViewById(R.id.consult_talent);
        consultBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TalentConsultDF talentConsultDF = TalentConsultDF.newInstance(tid, guideObject.optString("nickname"));
                talentConsultDF.show(getSupportFragmentManager(), "TalentConsultDF");
            }
        });
        
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
                startMeetArchiveActivity(getContext(), guideObject.optInt("uid"));
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
        chatInfo.setId(guideObject.optString("uid"));
        chatInfo.setChatName(guideObject.optString("realname"));

        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("CHAT_INFO", chatInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    
    private void getConsultStatisticsInfo(){
        RequestBody requestBody = new FormBody.Builder()
                .add("tid", String.valueOf(tid))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_CONSULT_STATISTICS_INFO, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "==========getConsultStatisticsInfo response body : " + responseText);
                if (responseText != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseText);
                        consultCount = jsonObject.optInt("count");
                        if (consultCount > 0){
                            handler.sendEmptyMessage(GET_CONSULT_INFO_DONE);
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

    private void setConsultInfoView(){
        TextView consultCountTV = findViewById(R.id.consultation_count);
        consultCountTV.setText("已解答"+consultCount);
        consultCountTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GuideDetailActivity.this, TalentDetailsActivity.class);
                intent.putExtra("tid", tid);
                startActivity(intent);
            }
        });
    }

    private void getAppointmentLimit(){
        RequestBody requestBody = new FormBody.Builder()
                .add("sid", String.valueOf(sid))
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
        String limitationStr = limitationsObject.optString("limitation");
        TextView groupLimitTV = findViewById(R.id.group_limit);
        groupLimitTV.setText(String.valueOf(limitationsObject.optInt("amount")));
        LinearLayout wrapper = findViewById(R.id.limit_condition_wrapper);
        if (!TextUtils.isEmpty(limitationStr)){
            String[] limitationArray = limitationStr.split(";");
            for (int i=0; i<limitationArray.length; i++){
                TextView textView = new TextView(this);
                textView.setText(getResources().getString(R.string.dot)+" "+limitationArray[i]);
                textView.setTextColor(getResources().getColor(R.color.black ));
                textView.setTextSize(16);
                wrapper.addView(textView);
            }
        }
    }
    
    private void getChargeInfo(){
        RequestBody requestBody = new FormBody.Builder()
                .add("sid", String.valueOf(sid))
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
        TextView unit = findViewById(R.id.unit);

       money.setText(String.valueOf(chargeObject.optInt("price")));
        unit.setText(chargeObject.optString("unit"));
    }
    
    public void handleMessage(Message message) {
        switch (message.what) {
            case GET_BANNER_PICTURES_DONE:
                useBanner();
                break;
            case GET_BASE_INFO_DONE:
                setBaseInfoView();
                break;
            case GET_ROUTE_INFO_DONE:
                setRouteInfoView();
                break;
            case GET_GUIDE_INFO_DONE:
                setGuideIntroductionView();
                getConsultStatisticsInfo();
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
            case GET_CONSULT_INFO_DONE:
                setConsultInfoView();
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
        Intent intent = new Intent(GuideDetailActivity.this, TalentDetailsActivity.class);
        intent.putExtra("tid", tid);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    
    static class MyHandler extends Handler {
        WeakReference<GuideDetailActivity> guideDetailActivityWeakReference;

        MyHandler(GuideDetailActivity guideDetailActivity) {
            guideDetailActivityWeakReference = new WeakReference<GuideDetailActivity>(guideDetailActivity);
        }

        @Override
        public void handleMessage(Message message) {
            GuideDetailActivity guideDetailActivity = guideDetailActivityWeakReference.get();
            if (guideDetailActivity != null) {
                guideDetailActivity.handleMessage(message);
            }
        }
    }



}
