package com.hetang.experience;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.adapter.DataBean;
import com.hetang.adapter.GuideBannerAdapter;
import com.hetang.adapter.SubGroupSummaryAdapter;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.common.MyApplication;
import com.hetang.group.CreateSubGroupDialogFragment;
import com.hetang.group.SingleGroupActivity;
import com.hetang.group.SingleGroupDetailsActivity;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.ParseUtils;
import com.hetang.util.RoundImageView;
import com.hetang.util.Slog;
import com.hetang.util.UserProfile;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.youth.banner.Banner;
import com.youth.banner.config.IndicatorConfig;
import com.youth.banner.indicator.CircleIndicator;

import com.youth.banner.itemdecoration.MarginDecoration;
import com.youth.banner.listener.OnBannerListener;
import com.youth.banner.listener.OnPageChangeListener;
import com.youth.banner.transformer.DepthPageTransformer;
import com.youth.banner.transformer.ZoomOutPageTransformer;
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

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.hetang.common.MyApplication.getContext;
import static com.hetang.group.SingleGroupActivity.GET_MY_GROUP_DONE;
import static com.hetang.group.SingleGroupActivity.SINGLE_GROUP_GET_MY;
import static com.hetang.group.SingleGroupActivity.getSingleGroup;

public class GuideDetailActivity extends BaseAppCompatActivity {
    private static final boolean isDebug = true;
    private static final String TAG = "GuideDetailActivity";
    public static final String GET_BANNER_PICTURES = HttpUtil.DOMAIN + "?q=travel_guide/get_route_pictures";
    public static final String GET_BASE_INFO = HttpUtil.DOMAIN + "?q=travel_guide/get_base_info";
    public static final String GET_ROUTE_INFO = HttpUtil.DOMAIN + "?q=travel_guide/get_route_info";
    private static final String GET_GUIDE_INFO = HttpUtil.DOMAIN + "?q=travel_guide/get_guide_info";
    private static final String GET_LIMIT_INFO = HttpUtil.DOMAIN + "?q=travel_guide/get_limit_info";
    private static final String GET_CHARGE_INFO = HttpUtil.DOMAIN + "?q=travel_guide/get_charge_info";
    private static final int GET_BANNER_PICTURES_DONE = 1;
    private static final int GET_BASE_INFO_DONE = 2;
    private static final int GET_ROUTE_INFO_DONE = 3;
    private static final int GET_GUIDE_INFO_DONE = 4;
    private static final int GET_LIMIT_INFO_DONE = 5;
    private static final int GET_CHARGE_INFO_DONE = 6;
    private static final int ADD_VISITOR_RECORD_DONE = 7;
    private static final int NO_SINGLE_GROUP_DONE = 10;
    final int itemLimit = 3;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    private int mLoadSize = 0;
    private int mUpdateSize = 0;
    private Handler handler;
    private int type = 0;
    private ViewGroup myGroupView;
    private int tid;
    private int rid = 1;
    private JSONArray bannerUrlArray;
    private JSONObject guideServiceObject;
    private String additionalServiceStr;
    private JSONArray routeJsonArray;
    private JSONObject guideObject;
    private JSONObject limitationsObject;
    private JSONObject chargeObject;
    
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
            tid = getIntent().getIntExtra("tid", 0);
        }
        
        initView();
        getBannerPictures();
        getBaseInformation();
        getRouteInfo();
        getGuideIntroduction();
        getAppointmentLimit();
        getChargeInfo();
    }
    
    private void initView() {
        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.left_back), font);
        FontManager.markAsIconContainer(findViewById(R.id.cancellation_detail), font);
        FontManager.markAsIconContainer(findViewById(R.id.cny), font);
        FontManager.markAsIconContainer(findViewById(R.id.evaluate_star), font);

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
    }

    private void checkAppointDate(){
        CheckAppointDate checkAppointDate = CheckAppointDate.newInstance(tid, chargeObject.optInt("amount"), chargeObject.optString("unit"));
        checkAppointDate.show(getSupportFragmentManager(), "CheckAppointDate");
    }
    
    private void getBannerPictures(){
        RequestBody requestBody = new FormBody.Builder()
                .add("tid", String.valueOf(tid))
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
                .add("tid", String.valueOf(tid))
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
                        TextView textView = new TextView(this);
                        textView.setText(additionalServiceArray[i]);
                        additionalServiceWrapper.addView(textView);
                    }
                }
            }
        }catch (JSONException e){
            e.printStackTrace();
        }


    }
    
     private void getRouteInfo(){
        RequestBody requestBody = new FormBody.Builder()
                .add("tid", String.valueOf(tid))
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
                .add("tid", String.valueOf(tid))
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
        name.setText(guideObject.optString("realname"));
        TextView selfIntroduction = findViewById(R.id.guide_self_introduction);
        selfIntroduction.setText(guideObject.optString("self_introduction"));
    }

    private void getAppointmentLimit(){
        RequestBody requestBody = new FormBody.Builder()
                .add("tid", String.valueOf(tid))
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
                textView.setText(limitationArray[i]);
                wrapper.addView(textView);
            }
        }
    }
    
    private void getChargeInfo(){
        RequestBody requestBody = new FormBody.Builder()
                .add("tid", String.valueOf(tid))
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

       money.setText(String.valueOf(chargeObject.optInt("amount")));
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
                break;
            case GET_LIMIT_INFO_DONE:
                setLimitInfoView();
                break;
            case GET_CHARGE_INFO_DONE:
                setChargeView();
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
