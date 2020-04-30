package com.hetang.consult;

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
import androidx.appcompat.widget.AppCompatEditText;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;

import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.adapter.DataBean;
import com.hetang.adapter.GuideBannerAdapter;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.common.MyApplication;
import com.hetang.experience.CheckAppointDate;
import com.hetang.experience.ShowAllEvaluateDF;
import com.hetang.picture.GlideEngine;

import com.hetang.util.CommonDialogFragmentInterface;
import com.hetang.util.DateUtil;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.RoundImageView;
import com.hetang.util.Slog;
import com.hetang.util.Utility;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.entity.LocalMedia;
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
import static com.hetang.consult.ConsultSummaryActivity.getConsult;
import static com.hetang.util.DateUtil.time2Comparison;

public class ConsultDetailActivity extends BaseAppCompatActivity implements CommonDialogFragmentInterface {
    private static final boolean isDebug = true;
    private static final String TAG = "ConsultDetailActivity";
    public static final String GET_QUESTION_BY_CID = HttpUtil.DOMAIN + "?q=consult/get_consult_by_id";
    public static final String GET_ANSWERS_BY_CID = HttpUtil.DOMAIN + "?q=consult/get_answers_by_id";
    public static final String UPDATE_ANSWER = HttpUtil.DOMAIN + "?q=consult/update_answer";
    
     private static final int GET_QUESTION_DONE = 1;
    private static final int GET_ANSWERS_DONE = 2;
    private static final int GET_ANSWER_UPDATE_DONE = 3;
    final int itemLimit = 3;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    private boolean isTalent = false;
    private Handler handler;
    private int innerWidth;
    private int innerAnswerWidth;
    private int cid;
    private JSONObject consultObject;
    private JSONObject talentAnswerObject;
    private JSONObject talentObject;
    private JSONArray answersArray;
    private JSONObject answerUpdateObject;
    private ConsultSummaryActivity.Consult consult;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consult_details);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        handler = new ConsultDetailActivity.MyHandler(this);

        if (getIntent() != null){
            cid = getIntent().getIntExtra("cid", 0);
        }

        initView();

        getQuestionInfo();

    }
    
    private void initView() {
        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.back), font);
        FontManager.markAsIconContainer(findViewById(R.id.consult_details_header), font);

        innerWidth = getResources().getDisplayMetrics().widthPixels - (int) Utility.dpToPx(getContext(), 38f);
        innerAnswerWidth = getResources().getDisplayMetrics().widthPixels - (int) Utility.dpToPx(getContext(), 92f);

        TextView leftBack = findViewById(R.id.back);
        leftBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        TextView answerEdit = findViewById(R.id.answer_question);
        answerEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AnswerEditDF answerEditDF = AnswerEditDF.newInstance(consult);
                answerEditDF.show(getSupportFragmentManager(), "AnswerEditDF");
            }
        });

    }

     private void getQuestionInfo(){
        RequestBody requestBody = new FormBody.Builder()
                .add("cid", String.valueOf(cid))
                .build();
                
                HttpUtil.sendOkHttpRequest(getContext(), GET_QUESTION_BY_CID, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                    Slog.d(TAG, "==========getQuestionInfo response body : " + responseText);
                if (responseText != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseText);
                        consultObject = jsonObject.optJSONObject("consult");
                        talentObject = jsonObject.optJSONObject("talent");
                        talentAnswerObject = jsonObject.optJSONObject("talent_answer");
                        handler.sendEmptyMessage(GET_QUESTION_DONE);
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
    
     private void setQuestionInfoView(){
         RoundImageView avatarIV = findViewById(R.id.avatar);
         TextView nameTV = findViewById(R.id.name);
         TextView classTV = findViewById(R.id.reward_class);
         TextView contentTV = findViewById(R.id.question);
         GridLayout pictureGL = findViewById(R.id.picture_grid);
         TextView timeTV = findViewById(R.id.time);
         TextView answerTV = findViewById(R.id.answer_count);
         
         consult = getConsult(consultObject);
         if (consult.avatar != null && !"".equals(consult.avatar)) {
             Glide.with(getContext()).load(HttpUtil.DOMAIN + consult.avatar).into(avatarIV);
         }

         nameTV.setText(consult.name);
         switch (consult.reward){
             case 0:
                 classTV.setText(getContext().getResources().getString(R.string.fa_heart));
                 classTV.setTextColor(getContext().getResources().getColor(R.color.color_red));
                 break;
             case 2:
                 classTV.setText(getContext().getResources().getString(R.string.fa_coffee));
                 classTV.setTextColor(getContext().getResources().getColor(R.color.color_coffee));
                 break;
             case 10:
                 classTV.setText(getContext().getResources().getString(R.string.fa_motorcycle));
                 classTV.setTextColor(getContext().getResources().getColor(R.color.color_green_dark));
                 break;
             case 50:
                 classTV.setText(getContext().getResources().getString(R.string.fa_rocket));
                 classTV.setTextColor(getContext().getResources().getColor(R.color.color_purple));
                 break;
             default:
                 break;
         }
         contentTV.setText(consult.question);
         timeTV.setText(time2Comparison((long)consult.created));
         answerTV.setText("解答"+consult.answerCount);

         if (consult.pictureList.size() > 0) {
             setConsultPictures(pictureGL, consult.pictureList, innerWidth);
         }
    }
    
    private void setConsultPictures(androidx.gridlayout.widget.GridLayout gridLayout, List<String> pictureList, int innerWidth){
        //JSONArray pictureArray = consult.pictureArray;
        Slog.d(TAG, "------------------------>setConsultPictures picture size: "+pictureList.size());
        if (pictureList.size() > 0){
            for (int i=0; i<pictureList.size(); i++){
                RoundImageView imageView = new RoundImageView(getContext());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(innerWidth/3, innerWidth/3);
                layoutParams.setMargins(0, 0, 3, 4);
                imageView.setLayoutParams(layoutParams);
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                gridLayout.addView(imageView);
                Glide.with(getContext()).load(HttpUtil.DOMAIN+pictureList.get(i)).into(imageView);
                imageView.setId(i);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //mPictureClickListener.onPictureClick(view, position, pictureArray, imageView.getId());
                        startPicturePreview(imageView.getId(), pictureList);
                    }
                });
            }
        }
    }
    
     private void setTalentInfoView(){
         RoundImageView avatarIV = findViewById(R.id.talent_avatar);
         String avatar = talentObject.optString("avatar");
        if (avatar != null && !"".equals(avatar)) {
            Glide.with(getContext()).load(HttpUtil.DOMAIN + avatar).into(avatarIV);
        }
         TextView nameTV = findViewById(R.id.talent_name);
        nameTV.setText(talentObject.optString("nickname"));
         TextView introductionTV = findViewById(R.id.talent_introduction);
        introductionTV.setText(talentObject.optString("introduction"));
        
         TextView answerTV = findViewById(R.id.answer_content);
        if (!TextUtils.isEmpty(talentAnswerObject.optString("answer"))){
            answerTV.setText(talentAnswerObject.optString("answer"));
        }else {
            answerTV.setText(getContext().getResources().getString(R.string.waiting_to_answer));
        }

        TextView answerTime = findViewById(R.id.answer_time);
        Slog.d(TAG, "----------------------->talent answer time : "+talentAnswerObject.optLong("created"));
        if (talentAnswerObject.optLong("created") > 0){
            answerTime.setText(time2Comparison(talentAnswerObject.optLong("created")));
        }
        
        JSONArray pictureJSONArray = talentAnswerObject.optJSONArray("pictures");
        GridLayout gridLayout = findViewById(R.id.answer_picture_grid);
        if (pictureJSONArray != null && pictureJSONArray.length() > 0){
            List<String> pictureList = new ArrayList<>();
            for (int i=0; i<pictureJSONArray.length(); i++){
                pictureList.add(pictureJSONArray.optString(i));
            }
            setConsultPictures(gridLayout, pictureList, innerWidth);
        }
    }
    
    private void updateTalentAnswer(int aid, int tid){
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("aid", String.valueOf(aid));
        if (isTalent){
            builder.add("tid", String.valueOf(aid));
        }
        RequestBody requestBody = builder.build();
        
        HttpUtil.sendOkHttpRequest(getContext(), UPDATE_ANSWER, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "==========updateTalentAnswer response body : " + responseText);
                if (responseText != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseText);
                        answerUpdateObject = jsonObject.optJSONObject("answer");
                        handler.sendEmptyMessage(GET_ANSWER_UPDATE_DONE);
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

    public void startPicturePreview(int position, List<String> pictureList){

        List<LocalMedia> localMediaList = new ArrayList<>();
        for (int i=0; i<pictureList.size(); i++){
            LocalMedia localMedia = new LocalMedia();
            localMedia.setPath(HttpUtil.getDomain()+pictureList.get(i));
            localMediaList.add(localMedia);
        }
        
        PictureSelector.create(this)
                .themeStyle(R.style.picture_default_style) // xml设置主题
                .loadImageEngine(GlideEngine.createGlideEngine())// 外部传入图片加载引擎，必传项
                .isNotPreviewDownload(true)
                .openExternalPreview(position, localMediaList);

    }

    private void getOtherAnswers(){
        RequestBody requestBody = new FormBody.Builder()
                .add("cid", String.valueOf(cid))
                .build();
                
                HttpUtil.sendOkHttpRequest(getContext(), GET_ANSWERS_BY_CID, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "==========getOtherAnswers response body : " + responseText);
                if (responseText != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseText);
                        answersArray = jsonObject.optJSONArray("answers");
                        if (answersArray.length() > 0){
                            handler.sendEmptyMessage(GET_ANSWERS_DONE);
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

    private void setAnswersInfoView(){
        TextView otherAnswersTitle = findViewById(R.id.other_answers_title);
        otherAnswersTitle.setVisibility(View.VISIBLE);
        LinearLayout answersWrapper = findViewById(R.id.other_answers_wrapper);
        if (answersArray != null && answersArray.length() > 0){
        for (int i=0; i<answersArray.length(); i++){
                JSONObject answerObject = answersArray.optJSONObject(i);
                View answerItem = LayoutInflater.from(getContext()).inflate(R.layout.consult_answers_item, (ViewGroup) findViewById(android.R.id.content), false);
                answersWrapper.addView(answerItem);
                RoundImageView imageView = answerItem.findViewById(R.id.avatar);
                Glide.with(getContext()).load(HttpUtil.DOMAIN+answerObject.optString("avatar")).into(imageView);
                TextView nameTV = answerItem.findViewById(R.id.name);
                nameTV.setText(answerObject.optString("nickname"));
                TextView contentTV = answerItem.findViewById(R.id.answer_content);
                contentTV.setText(answerObject.optString("answer"));
                TextView timeTV = answerItem.findViewById(R.id.answer_time);
                timeTV.setText(time2Comparison(answerObject.optLong("created")));
                GridLayout pictureWrapper = answerItem.findViewById(R.id.answer_picture_grid);
                JSONArray jsonArray = answerObject.optJSONArray("pictures");
                if (jsonArray.length() > 0){
                    List<String> pictureList = new ArrayList<>();
                    for (int j=0;j<jsonArray.length();j++){
                        pictureList.add(jsonArray.optString(j));
                    }
                    setConsultPictures(pictureWrapper, pictureList, innerAnswerWidth);
                }

            }
        }
    }
    
    private void setAnswerUpdateView(){
         if (isTalent){
             TextView answerTV = findViewById(R.id.answer_content);
             if (!TextUtils.isEmpty(answerUpdateObject.optString("answer"))){
                 answerTV.setText(answerUpdateObject.optString("answer"));
             }

             JSONArray pictureJSONArray = answerUpdateObject.optJSONArray("pictures");
             GridLayout gridLayout = findViewById(R.id.answer_picture_grid);
             if (pictureJSONArray != null && pictureJSONArray.length() > 0){
             List<String> pictureList = new ArrayList<>();
                 for (int i=0; i<pictureJSONArray.length(); i++){
                     pictureList.add(pictureJSONArray.optString(i));
                 }
                 setConsultPictures(gridLayout, pictureList, innerAnswerWidth);
             }
         }else {
             TextView otherAnswersTitle = findViewById(R.id.other_answers_title);
             otherAnswersTitle.setVisibility(View.VISIBLE);
             LinearLayout answersWrapper = findViewById(R.id.other_answers_wrapper);
             View answerItem = LayoutInflater.from(getContext()).inflate(R.layout.consult_answers_item, (ViewGroup) findViewById(android.R.id.content), false);
             answersWrapper.addView(answerItem, 0);
             RoundImageView imageView = answerItem.findViewById(R.id.avatar);
             Glide.with(getContext()).load(HttpUtil.DOMAIN+answerUpdateObject.optString("avatar")).into(imageView);
             TextView nameTV = answerItem.findViewById(R.id.name);
             nameTV.setText(answerUpdateObject.optString("nickname"));
             TextView contentTV = answerItem.findViewById(R.id.answer_content);
             contentTV.setText(answerUpdateObject.optString("answer"));
             TextView timeTV = answerItem.findViewById(R.id.answer_time);
             
              timeTV.setText(time2Comparison(answerUpdateObject.optLong("created")));
             GridLayout pictureWrapper = answerItem.findViewById(R.id.answer_picture_grid);
             JSONArray jsonArray = answerUpdateObject.optJSONArray("pictures");
             if (jsonArray.length() > 0){
                 List<String> pictureList = new ArrayList<>();
                 for (int j=0;j<jsonArray.length();j++){
                     pictureList.add(jsonArray.optString(j));
                 }
                 setConsultPictures(pictureWrapper, pictureList, innerAnswerWidth);
             }
         }
    }
    
    public void handleMessage(Message message) {
        switch (message.what) {
            case GET_QUESTION_DONE:
                setQuestionInfoView();
                setTalentInfoView();
                getOtherAnswers();
                break;
            case GET_ANSWERS_DONE:
                setAnswersInfoView();
                break;
            case GET_ANSWER_UPDATE_DONE:
                setAnswerUpdateView();
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
    public void onBackFromDialog(int type, int result, boolean status) {
         Slog.d(TAG, "-------------------onBackFromDialog aid: "+type+"  tid: "+result+"  status: "+status);
        if (status){
            if (result != 0){//for talent
                isTalent = true;
            }else {
                isTalent = false;
            }
            updateTalentAnswer(type, result);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    
    static class MyHandler extends Handler {
        WeakReference<ConsultDetailActivity> consultDetailActivityWeakReference;

        MyHandler(ConsultDetailActivity consultDetailActivity) {
            consultDetailActivityWeakReference = new WeakReference<ConsultDetailActivity>(consultDetailActivity);
        }

        @Override
        public void handleMessage(Message message) {
            ConsultDetailActivity consultDetailActivity = consultDetailActivityWeakReference.get();
            if (consultDetailActivity != null) {
                consultDetailActivity.handleMessage(message);
            }
        }
    }
}

         
