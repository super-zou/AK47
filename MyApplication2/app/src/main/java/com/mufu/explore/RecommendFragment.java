package com.mufu.explore;

//import android.app.Fragment;

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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.mufu.R;
import com.mufu.adapter.DataBean;
import com.mufu.adapter.GuideBannerAdapter;
import com.mufu.adapter.MeetRecommendListAdapter;
import com.mufu.common.HandlerTemp;
import com.mufu.common.MyApplication;
import com.mufu.consult.ConsultSummaryActivity;
import com.mufu.contacts.ContactsApplyListActivity;
import com.mufu.experience.ExperienceDetailActivity;
import com.mufu.experience.ExperienceSummaryActivity;
import com.mufu.experience.GuideDetailActivity;
import com.mufu.experience.GuideSummaryActivity;
import com.mufu.group.SubGroupActivity;
import com.mufu.home.CommonContactsActivity;
import com.mufu.main.MeetArchiveActivity;
import com.mufu.meet.MeetRecommendActivity;
import com.mufu.meet.UserMeetInfo;
import com.mufu.talent.TalentDetailsActivity;
import com.mufu.talent.TalentSummaryListActivity;
import com.mufu.util.BaseFragment;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.ParseUtils;
import com.mufu.util.RoundImageView;
import com.mufu.util.Slog;
import com.mufu.util.Utility;
import com.youth.banner.Banner;
import com.youth.banner.itemdecoration.MarginDecoration;
import com.youth.banner.listener.OnBannerListener;
import com.youth.banner.transformer.DepthPageTransformer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.mufu.common.AddPictureActivity.ADD_PICTURE_BROADCAST;
import static com.mufu.common.SetAvatarActivity.AVATAR_SET_ACTION_BROADCAST;
import static com.mufu.experience.ExperienceSummaryActivity.getExperience;
import static com.mufu.experience.GuideSummaryActivity.getGuide;
import static com.mufu.group.SubGroupActivity.GET_MY_UNIVERSITY_SUBGROUP;
import static com.mufu.group.SubGroupActivity.getTalent;
import static com.mufu.main.DynamicFragment.COMMENT_UPDATE_RESULT;
import static com.mufu.main.DynamicFragment.LOVE_UPDATE_RESULT;
import static com.mufu.main.DynamicFragment.MY_COMMENT_UPDATE_RESULT;
import static com.mufu.main.DynamicFragment.MY_LOVE_UPDATE_RESULT;
import static com.mufu.main.DynamicFragment.MY_PRAISE_UPDATE_RESULT;
import static com.mufu.main.DynamicFragment.PRAISE_UPDATE_RESULT;
import static com.mufu.explore.ShareFragment.COMMENT_COUNT_UPDATE;
import static com.mufu.explore.ShareFragment.LOVE_UPDATE;
import static com.mufu.explore.ShareFragment.MY_COMMENT_COUNT_UPDATE;
import static com.mufu.explore.ShareFragment.PRAISE_UPDATE;

public class RecommendFragment extends BaseFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "RecommendFragment";
    private static final int PAGE_SIZE = 8;//page size
    private static final int GET_ALL_EXPERIENCES_DONE = 1;
    private static final int GET_ALL_GUIDS_DONE = 2;
    private static final int GET_RECOMMEND_MEETS_DONE = 3;
    private static final int GET_RECOMMEND_TALENTS_DONE = 4;
    public static final int MY_CONDITION_SET_DONE = 30;
    public static final int MY_CONDITION_NOT_SET = 31;
    public static final int MY_UNIVERSITY_GROUP_GET_DONE = 32;
    public static final int GET_RECOMMEND_MEMBER_DONE = 33;
    public static final int HAD_NO_RECOMMEND_MEMBER = 34;
    public static final int DEFAULT_RECOMMEND_COUNT = 8;
    private static final int NO_MORE_RECOMMEND = 9;
    private static final int NO_UPDATE_RECOMMEND = 10;
    private static final int MY_CONDITION_LOVE_UPDATE = 11;
    private static final int MY_CONDITION_PRAISE_UPDATE = 12;
        //for experiences type
    private static final int GET_RECOMMEND_LANGUAGE_CULTURE_DONE = 13;
    private static final int GET_RECOMMEND_PARTY_SALON_DONE = 14;
    private static final int GET_RECOMMEND_FRIENDSHIP_DONE = 15;
    private static final int GET_RECOMMEND_NATURAL_EXPERIENCES_DONE = 16;
    private static final int GET_RECOMMEND_HUMANITY_EXPERIENCES_DONE = 17;
    private static final int GET_RECOMMEND_NGO_EXPERIENCES_DONE = 18;
    private static final int GET_RECOMMEND_THEATRE_EXPERIENCES_DONE = 19;
    private static final int GET_RECOMMEND_LEARNING_GROWTH_DONE = 20;
    private static final int GET_RECOMMEND_HOBBY_DONE = 21;
    private static final String GET_RECOMMEND_EXPERIENCES = HttpUtil.DOMAIN + "?q=experience/get_recommend_experiences";
    private static final String GET_RECOMMEND_EXPERIENCES_BY_TYPE = HttpUtil.DOMAIN + "?q=experience/get_recommend_experiences_by_type";
    private static final String GET_RECOMMEND_GUIDES = HttpUtil.DOMAIN + "?q=travel_guide/get_recommend_guides";
    private static final String GET_RECOMMEND_URL = HttpUtil.DOMAIN + "?q=meet/get_recommend";
    public static final String GET_MY_CONDITION_URL = HttpUtil.DOMAIN + "?q=meet/get_my_condition";
    public static final String GET_RECOMMEND_PERSON_URL = HttpUtil.DOMAIN + "?q=contacts/recommend_person";
    public static final String GET_RECOMMEND_TALENTS = HttpUtil.DOMAIN + "?q=talent/get_recommend";
    private List<UserMeetInfo> meetList = new ArrayList<>();
    private boolean isRecommend = true;
    private View lookFriend;
    private int currentPage = 0;
    private int currentPosition = -1;
    private static final int any = -1;
    private Handler handler = new MyHandler(this);
    private boolean avatarSet = false;
    private RelativeLayout addMeetInfo;
    private PictureAddBroadcastReceiver mReceiver;
    private MeetRecommendListAdapter meetRecommendListAdapter;
    private List<ContactsApplyListActivity.Contacts> contactsList = new ArrayList<>();
    private List<SubGroupActivity.SubGroup> subGroupList = new ArrayList<>();
    private List<SubGroupActivity.Talent> mTalentList = new ArrayList<>();

    private int mLoadExperienceSize;
    private int mLoadGuideSize;
    private int mLoadMeetSize;
    private int mLoadTalentSize;
    View mView;
    UserMeetInfo myCondition;
    TextView lovedIcon;
    TextView lovedView;
    TextView thumbsView;
    TextView thumbsIcon;
    TextView commentCountView;
    public static int student = 0;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    private ConstraintLayout mRecommendExperienceWrapper;
    private List<ExperienceSummaryActivity.Experience> mExperienceList = new ArrayList<>();
    private List<GuideSummaryActivity.Guide> mGuideList = new ArrayList<>();

    @Override
    protected int getLayoutId() {
        return R.layout.recommend_fragment;
    }

    @Override
    protected void initView(View view) {
        mView = view;
        mRecommendExperienceWrapper = mView.findViewById(R.id.experience_recommend);
        useBanner();
        //getRecommendTalent();
        getRecommendExperiences(Utility.ExperienceType.LANGUAGE_CULTURE.getType());
    }
    
    public void useBanner() {
        //--------------------------简单使用-------------------------------
        //创建（new banner()）或者布局文件中获取banner
        Banner banner = (Banner) mView.findViewById(R.id.banner);
        DataBean dataBean = new DataBean();

        //--------------------------详细使用-------------------------------
        banner.setAdapter(new GuideBannerAdapter(dataBean.getRecommendBannerData()));
        banner.isAutoLoop(true);
        banner.addItemDecoration(new MarginDecoration(0));
        banner.setPageTransformer(new DepthPageTransformer());
        banner.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(Object data, int position) {

            }
        });

        banner.start();
    }
    
    @Override
    protected void loadData() {}

    private void getRecommendExperiences(int type){
        RequestBody requestBody = new FormBody.Builder().add("recommendation", String.valueOf(isRecommend)).add("type", String.valueOf(type)).build();
        HttpUtil.sendOkHttpRequest(getContext(), GET_RECOMMEND_EXPERIENCES_BY_TYPE, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========getRecommendExperiences response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject experiencesResponse = null;
                        try {
                            experiencesResponse = new JSONObject(responseText);
                            if (experiencesResponse != null) {
                                mLoadExperienceSize = processExperiencesResponse(experiencesResponse);
                                Utility.ExperienceType experienceType = Utility.ExperienceType.getExperienceType(type);
                                switch (experienceType){
                                    case LANGUAGE_CULTURE:
                                        handler.sendEmptyMessage(GET_RECOMMEND_LANGUAGE_CULTURE_DONE);
                                        break;
                                    case PARTY_SALON:
                                        handler.sendEmptyMessage(GET_RECOMMEND_PARTY_SALON_DONE);
                                        break;
                                    case FRIENDSHIP:
                                        handler.sendEmptyMessage(GET_RECOMMEND_FRIENDSHIP_DONE);
                                        break;
                                    case NATURAL_OUTDOOR:
                                        handler.sendEmptyMessage(GET_RECOMMEND_NATURAL_EXPERIENCES_DONE);
                                        break;
                                        case HUMANITY_ART:
                                        handler.sendEmptyMessage(GET_RECOMMEND_HUMANITY_EXPERIENCES_DONE);
                                        break;
                                    case NGO_PUBLIC_GOOD:
                                        handler.sendEmptyMessage(GET_RECOMMEND_NGO_EXPERIENCES_DONE);
                                        break;
                                    case THEATRE_PERFORMANCE:
                                        handler.sendEmptyMessage(GET_RECOMMEND_THEATRE_EXPERIENCES_DONE);
                                        break;
                                    case LEARNING_GROWTH:
                                        handler.sendEmptyMessage(GET_RECOMMEND_LEARNING_GROWTH_DONE);
                                        break;
                                    case HOBBY:
                                        handler.sendEmptyMessage(GET_RECOMMEND_HOBBY_DONE);
                                        break;
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
    public int processExperiencesResponse(JSONObject experiencesObject) {
        int experienceSize = 0;
        JSONArray experienceArray = null;

        if (experiencesObject != null) {
            experienceArray = experiencesObject.optJSONArray("experiences");
            Slog.d(TAG, "------------------->processExperiencesResponse: "+experienceArray);
        }
        
        mExperienceList.clear();

        if (experienceArray != null) {
            experienceSize = experienceArray.length();
            if (experienceSize > 0) {
                for (int i = 0; i < experienceArray.length(); i++) {
                    JSONObject guideObject = experienceArray.optJSONObject(i);
                    if (guideObject != null) {
                        ExperienceSummaryActivity.Experience experience = getExperience(guideObject);
                        mExperienceList.add(experience);
                    }
                }
            }
        }

        return experienceSize;
    }
    
    private void setRecommendExperiencesView(int type){
        if (mRecommendExperienceWrapper.getVisibility() == View.GONE){
            mRecommendExperienceWrapper.setVisibility(View.VISIBLE);
        }
        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        ConstraintLayout recommendExperienceWrapper;
        LinearLayout recommendExperienceLL;
                TextView moreExperienceBtn;
        Utility.ExperienceType experienceType = Utility.ExperienceType.getExperienceType(type);
        switch (experienceType){
            case LANGUAGE_CULTURE:
                recommendExperienceWrapper = mView.findViewById(R.id.recommend_language_culture_wrapper);
                recommendExperienceLL = mView.findViewById(R.id.language_culture_cardview_wrapper);
                moreExperienceBtn = mView.findViewById(R.id.more_language_culture);
                break;
            case PARTY_SALON:
                recommendExperienceWrapper = mView.findViewById(R.id.recommend_party_salon_wrapper);
                recommendExperienceLL = mView.findViewById(R.id.party_salon_cardview_wrapper);
                moreExperienceBtn = mView.findViewById(R.id.more_party_salon);
                break;
            case FRIENDSHIP:
                recommendExperienceWrapper = mView.findViewById(R.id.recommend_friendship_wrapper);
                recommendExperienceLL = mView.findViewById(R.id.friendship_cardview_wrapper);
                moreExperienceBtn = mView.findViewById(R.id.more_friendship);
                break;
            case NATURAL_OUTDOOR:
                recommendExperienceWrapper = mView.findViewById(R.id.recommend_natural_outdoor_wrapper);
                recommendExperienceLL = mView.findViewById(R.id.natural_outdoor_cardview_wrapper);
                moreExperienceBtn = mView.findViewById(R.id.more_natural_outdoor);
                break;
            case HUMANITY_ART:
                recommendExperienceWrapper = mView.findViewById(R.id.recommend_humanity_art_wrapper);
                recommendExperienceLL = mView.findViewById(R.id.humanity_art_cardview_wrapper);
                moreExperienceBtn = mView.findViewById(R.id.more_humanity_art);
                break;
                case NGO_PUBLIC_GOOD:
                recommendExperienceWrapper = mView.findViewById(R.id.recommend_ngo_public_good_wrapper);
                recommendExperienceLL = mView.findViewById(R.id.ngo_public_good_cardview_wrapper);
                moreExperienceBtn = mView.findViewById(R.id.more_ngo_public_good);
                break;
            case THEATRE_PERFORMANCE:
                recommendExperienceWrapper = mView.findViewById(R.id.recommend_theatre_performance_wrapper);
                recommendExperienceLL = mView.findViewById(R.id.theatre_performance_cardview_wrapper);
                moreExperienceBtn = mView.findViewById(R.id.more_theatre_performance);
                break;
                case LEARNING_GROWTH:
                recommendExperienceWrapper = mView.findViewById(R.id.recommend_learning_growth_wrapper);
                recommendExperienceLL = mView.findViewById(R.id.learning_growth_cardview_wrapper);
                moreExperienceBtn = mView.findViewById(R.id.more_learning_growth);
                break;
            case HOBBY:
                recommendExperienceWrapper = mView.findViewById(R.id.recommend_hobbies_wrapper);
                recommendExperienceLL = mView.findViewById(R.id.hobbies_cardview_wrapper);
                moreExperienceBtn = mView.findViewById(R.id.more_hobbies);
                break;
                default:
                recommendExperienceWrapper = mView.findViewById(R.id.recommend_language_culture_wrapper);
                recommendExperienceLL = mView.findViewById(R.id.language_culture_cardview_wrapper);
                moreExperienceBtn = mView.findViewById(R.id.more_language_culture);
                break;
        }
        recommendExperienceWrapper.setVisibility(View.VISIBLE);
        ExperienceSummaryActivity.Experience experience;
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        int innerWidth = dm.widthPixels;
        int childrenWidth = (int)(innerWidth * 0.618);
        int innerHeight = (int)(childrenWidth);
        ConstraintLayout.LayoutParams layoutParamsPicture = new ConstraintLayout.LayoutParams(childrenWidth, innerHeight);
        LinearLayout.LayoutParams layoutParamsWrapper = new LinearLayout.LayoutParams(childrenWidth, WRAP_CONTENT);
        for (int i=0; i<mLoadExperienceSize; i++){
            View recommendExperienceView = LayoutInflater.from(getContext()).inflate(R.layout.experience_recommend_item, (ViewGroup) mView.findViewById(android.R.id.content), false);
            recommendExperienceLL.addView(recommendExperienceView);
                        recommendExperienceView.setLayoutParams(layoutParamsWrapper);
            CardView itemLayout = recommendExperienceView.findViewById(R.id.experience_recommend_item);
            ImageView headUri = recommendExperienceView.findViewById(R.id.head_picture);
            TextView titleTV = recommendExperienceView.findViewById(R.id.guide_title);
            TextView cityTV = recommendExperienceView.findViewById(R.id.city);
            TextView scoreTV = recommendExperienceView.findViewById(R.id.score);
            TextView countTV = recommendExperienceView.findViewById(R.id.evaluate_count);
            TextView moneyTV = recommendExperienceView.findViewById(R.id.money);
            TextView unitTV = recommendExperienceView.findViewById(R.id.unit);
            TextView durationTV = recommendExperienceView.findViewById(R.id.duration);
            LinearLayout evaluateLL = recommendExperienceView.findViewById(R.id.evaluate_inner_wrapper);
            TextView joinedDivider = recommendExperienceView.findViewById(R.id.joined_divider);
            TextView freeTV = recommendExperienceView.findViewById(R.id.free);
            LinearLayout priceWrapper = recommendExperienceView.findViewById(R.id.price_wrapper);
            TextView joinedAmountTV = recommendExperienceView.findViewById(R.id.joined_amount);
            
            experience = mExperienceList.get(i);

            if (experience.headPictureUrl != null && !"".equals(experience.headPictureUrl)) {
                Glide.with(getContext()).load(HttpUtil.DOMAIN + experience.headPictureUrl).into(headUri);
            }
            
            headUri.setLayoutParams(layoutParamsPicture);
            headUri.setAdjustViewBounds(true);

            titleTV.setText(experience.title);
            cityTV.setText(experience.city);
            if (experience.evaluateCount > 0){
                evaluateLL.setVisibility(View.VISIBLE);
                joinedDivider.setVisibility(View.VISIBLE);
                float average = experience.evaluateScore / experience.evaluateCount;
                float averageScore = (float) (Math.round(average * 10)) / 10;
                scoreTV.setText(String.valueOf(averageScore));
                countTV.setText("("+experience.evaluateCount+")");
            }else {
                evaluateLL.setVisibility(View.GONE);
            }
            
            if (experience.price != 0){
                moneyTV.setText(String.valueOf(experience.price));
                unitTV.setText("人起");
            }else {
                priceWrapper.setVisibility(View.GONE);
                freeTV.setVisibility(View.VISIBLE);
            }

            if (experience.joinedAmount == 0){
                joinedAmountTV.setText("新上线活动");
            }else {
                joinedAmountTV.setText(String.valueOf(experience.joinedAmount)+"人参加过");
            }
            
            durationTV.setVisibility(View.VISIBLE);
            durationTV.setText(String.valueOf(experience.duration)+"小时");

            FontManager.markAsIconContainer(recommendExperienceView.findViewById(R.id.experience_recommend_item), font);

            itemLayout.setTag(experience.eid);
            itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), ExperienceDetailActivity.class);
                    intent.putExtra("eid", (int)itemLayout.getTag());
                    startActivity(intent);
                }
            });
        }
                
        if(mLoadExperienceSize > 2){
            moreExperienceBtn.setVisibility(View.VISIBLE);
        }
        
        moreExperienceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ExperienceSummaryActivity.class);
                intent.putExtra("type", type);
                startActivity(intent);
            }
        });
    }
    
    private void getRecommendGuides(){
        RequestBody requestBody = new FormBody.Builder().add("recommend", String.valueOf(isRecommend)).build();
        HttpUtil.sendOkHttpRequest(getContext(), GET_RECOMMEND_GUIDES, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========getRecommendGuides response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                    JSONObject experiencesResponse = null;
                        try {
                            experiencesResponse = new JSONObject(responseText);
                            if (experiencesResponse != null) {
                                mLoadGuideSize = processGuidesResponse(experiencesResponse);

                                handler.sendEmptyMessage(GET_ALL_GUIDS_DONE);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            
             @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    public int processGuidesResponse(JSONObject guidesObject) {
        int guideSize = 0;
        JSONArray guideArray = null;

        if (guidesObject != null) {
            guideArray = guidesObject.optJSONArray("guides");
            Slog.d(TAG, "------------------->processGuidesResponse: "+guideArray);
        }
        
        if (guideArray != null) {
            guideSize = guideArray.length();
            if (guideSize > 0) {
                for (int i = 0; i < guideArray.length(); i++) {
                    JSONObject guideObject = guideArray.optJSONObject(i);
                    if (guideObject != null) {
                        GuideSummaryActivity.Guide guide = getGuide(guideObject);
                        mGuideList.add(guide);
                    }
                }
            }
        }

        return guideSize;
    }

    private void setRecommendGuidesView(){
        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        ConstraintLayout recommendGuideWrapper = mView.findViewById(R.id.guide_recommend);
        recommendGuideWrapper.setVisibility(View.VISIBLE);
        LinearLayout recommendGuideLL = mView.findViewById(R.id.guide_recommend_wrapper);
        GuideSummaryActivity.Guide guide;
        for (int i=0; i<mLoadGuideSize; i++){
            View recommendExperienceView = LayoutInflater.from(getContext()).inflate(R.layout.guide_list_item, (ViewGroup) mView.findViewById(android.R.id.content), false);
            recommendGuideLL.addView(recommendExperienceView);
            
            CardView itemLayout = recommendExperienceView.findViewById(R.id.guide_list_item);
            ImageView headUri = recommendExperienceView.findViewById(R.id.head_picture);
            TextView titleTV = recommendExperienceView.findViewById(R.id.guide_title);
            TextView cityTV = recommendExperienceView.findViewById(R.id.city);
            TextView scoreTV = recommendExperienceView.findViewById(R.id.score);
            TextView countTV = recommendExperienceView.findViewById(R.id.evaluate_count);
            TextView moneyTV = recommendExperienceView.findViewById(R.id.money);
            TextView unitTV = recommendExperienceView.findViewById(R.id.unit);
            LinearLayout evaluateLL = recommendExperienceView.findViewById(R.id.evaluate_wrapper);
            
            guide = mGuideList.get(i);

            if (guide.headPictureUrl != null && !"".equals(guide.headPictureUrl)) {
                Glide.with(getContext()).load(HttpUtil.DOMAIN + guide.headPictureUrl).into(headUri);
            }

            titleTV.setText(guide.title);
            cityTV.setText(guide.city);
            if (guide.evaluateCount > 0){
                evaluateLL.setVisibility(View.VISIBLE);
                float average = guide.evaluateScore / guide.evaluateCount;
                float averageScore = (float) (Math.round(average * 10)) / 10;
                scoreTV.setText(String.valueOf(averageScore));
                countTV.setText("("+guide.evaluateCount+"条评价)");
            }else {
                evaluateLL.setVisibility(View.GONE);
            }
            
            moneyTV.setText(String.valueOf(guide.price));
            unitTV.setText("人起");

            FontManager.markAsIconContainer(recommendExperienceView.findViewById(R.id.guide_list_item), font);

            itemLayout.setTag(guide.sid);
            itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), GuideDetailActivity.class);
                    intent.putExtra("sid", (int)itemLayout.getTag());
                    startActivity(intent);
                }
            });
        }
        
        Button moreGuideBtn = mView.findViewById(R.id.more_guide);
        moreGuideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), GuideSummaryActivity.class);
                startActivity(intent);
            }
        });
    }

    private void getRecommendMeet() {
        RequestBody requestBody = null;

        requestBody = new FormBody.Builder()
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(0))
                .add("recommend", String.valueOf(isRecommend))
                .build();
                
                HttpUtil.sendOkHttpRequest(getContext(), GET_RECOMMEND_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null){
                    String responseText = response.body().string();
                    Slog.d(TAG, "--------------------->getRecommendMeet response: "+responseText);
                    if (!TextUtils.isEmpty(responseText)){
                        mLoadMeetSize = getResponseText(responseText);
                        Slog.d(TAG, "--------------------->getRecommendMeet mLoadMeetSize: "+mLoadMeetSize);
                        handler.sendEmptyMessage(GET_RECOMMEND_MEETS_DONE);
                    }
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure e:" + e);
            }
        });
    }


    private int getResponseText(String responseText) {
        List<UserMeetInfo> tempList = ParseUtils.getRecommendMeetList(responseText, false);
        if (null != tempList && tempList.size() != 0) {
            meetList.addAll(tempList);
            return tempList.size();
        }
        return 0;
    }
    
    private void setRecommendMeetsView(){
        ConstraintLayout meetRecommendWrapper = mView.findViewById(R.id.meet_wrapper);
        meetRecommendWrapper.setVisibility(View.VISIBLE);
        LinearLayout meetWrapper = mView.findViewById(R.id.meet_recommend_wrapper);
        for (int i=0; i<meetList.size(); i++){
            View recommendMeetView = LayoutInflater.from(getContext()).inflate(R.layout.meet_discovery_item, (ViewGroup) mView.findViewById(android.R.id.content), false);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            layoutParams.setMargins(0,0,0, 8);
            recommendMeetView.setLayoutParams(layoutParams);
            meetWrapper.addView(recommendMeetView);
            setMeetRecommendContent(meetList.get(i), recommendMeetView);
            
            recommendMeetView.setTag(meetList.get(i).uid);
            recommendMeetView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), MeetArchiveActivity.class);
                    intent.putExtra("uid", (int)recommendMeetView.getTag());
                    startActivity(intent);
                }
            });
        }
        
        Button moreRecommendMeetBtn = mView.findViewById(R.id.more_meet);
        moreRecommendMeetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), MeetRecommendActivity.class);
                startActivity(intent);
            }
        });
    }

public void setMeetRecommendContent(UserMeetInfo meet, View view){
        ConstraintLayout recommendLayout = view.findViewById(R.id.meet_item_id);
        TextView name = (TextView) view.findViewById(R.id.name);
        TextView living = (TextView) view.findViewById(R.id.living);
        TextView hometown = view.findViewById(R.id.hometown);
        RoundImageView avatar =  view.findViewById(R.id.avatar);
        TextView university = view.findViewById(R.id.university);
        TextView degree = view.findViewById(R.id.degree);
        TextView selfConditionTV = view.findViewById(R.id.self_condition);
        LinearLayout educationBackground = view.findViewById(R.id.education_background);
        LinearLayout workInfo = view.findViewById(R.id.work_info);
        TextView position = view.findViewById(R.id.position);
        TextView industry = view.findViewById(R.id.industry);
        
        TextView visitIcon = view.findViewById(R.id.eye_icon);
        TextView visitRecord = view.findViewById(R.id.visit_record);
        TextView lovedView = view.findViewById(R.id.loved_statistics);
        TextView lovedIcon =  view.findViewById(R.id.loved_icon);
        TextView thumbsView =  view.findViewById(R.id.thumbs_up_statistics);
        TextView thumbsIcon =  view.findViewById(R.id.thumbs_up_icon);
        TextView comment = view.findViewById(R.id.comment);
        TextView commentCount = view.findViewById(R.id.comment_count);

        name.setText(meet.getNickName());
        
        if (!TextUtils.isEmpty(meet.getSelfCondition())){
            selfConditionTV.setText(meet.getSelfCondition());
        }else {
            selfConditionTV.setVisibility(View.GONE);
        }

        /*
        if(meet.getLiving() != null && !TextUtils.isEmpty(meet.getLiving())){
            living.setText(meet.getLiving());
        }

        if(meet.getHometown() != null && !TextUtils.isEmpty(meet.getHometown())){
            hometown.setText(meet.getHometown()+"人");
        }

         */

        if (meet.getAvatar() != null && !"".equals(meet.getAvatar())) {
            Glide.with(getContext()).load(HttpUtil.DOMAIN + meet.getAvatar()).into(avatar);
            } else {
            if(meet.getSex() == 0){
                avatar.setImageDrawable(getContext().getDrawable(R.drawable.male_default_avator));
            }else {
                avatar.setImageDrawable(getContext().getDrawable(R.drawable.female_default_avator));
            }
        }

        if (!TextUtils.isEmpty(meet.getUniversity())){
            university.setText(meet.getUniversity());
        }
        
        if (meet.getSituation() == student){
            if (workInfo.getVisibility() == View.VISIBLE){
                workInfo.setVisibility(View.GONE);
            }
        }else {
        if (workInfo.getVisibility() == View.GONE){
                workInfo.setVisibility(View.VISIBLE);
            }
            String jobPosition = meet.getPosition();
            if (!TextUtils.isEmpty(jobPosition)){
                position.setText(jobPosition);
            }

            String industryStr = meet.getIndustry();
            if (!TextUtils.isEmpty(industryStr)){
                industry.setText(industryStr);
            }
        }
        
        if(meet.getLovedCount() > 0){
            if(meet.getLoved() == 1 ){
                lovedIcon.setText(R.string.fa_heart);
            }
        }else {
            lovedIcon.setText(R.string.fa_heart_o);
        }

        if(meet.getLovedCount() > 0){
            lovedView.setText(String.valueOf(meet.getLovedCount()));
        }
        
        if(meet.getPraisedCount() > 0){
            if(meet.getPraised() == 1 ){
                thumbsIcon.setText(R.string.fa_thumbs_up);
            }
        }else {
            thumbsIcon.setText(R.string.fa_thumbs_O_up);
        }

        if(meet.getPraisedCount() > 0){
            thumbsView.setText(String.valueOf(meet.getPraisedCount()));
        }else {
            thumbsView.setText("");
        }
        
        if (meet.getVisitCount() > 0){
            visitRecord.setText(String.valueOf(meet.getVisitCount()));
            visitIcon.setVisibility(View.VISIBLE);
            visitRecord.setVisibility(View.VISIBLE);
        }else {
            visitIcon.setVisibility(View.GONE);
            visitRecord.setVisibility(View.GONE);
        }

        if (meet.getCommentCount() > 0){
            commentCount.setText(String.valueOf(meet.getCommentCount()));
        }
        
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(view.findViewById(R.id.behavior_statistics), font);
        FontManager.markAsIconContainer(view.findViewById(R.id.activity_indicator), font);
        FontManager.markAsIconContainer(view.findViewById(R.id.eye_icon), font);
        FontManager.markAsIconContainer(view.findViewById(R.id.living_icon), font);
    }


    private void getRecommendTalent() {
        RequestBody requestBody = new FormBody.Builder()
                .add("step", String.valueOf(6))
                .add("page", String.valueOf(0))
                .add("recommend", String.valueOf(isRecommend))
                .build();
                
                HttpUtil.sendOkHttpRequest(getContext(), GET_RECOMMEND_TALENTS, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========getRecommendTalent response text: "+responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject talentsResponse = null;
                        
                        try {
                            talentsResponse = new JSONObject(responseText);
                            if (talentsResponse != null) {
                                mLoadTalentSize = processTalentsResponse(talentsResponse);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    handler.sendEmptyMessage(GET_RECOMMEND_TALENTS_DONE);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
     public int processTalentsResponse(JSONObject talentsObject){
        int talentSize = 0;
        JSONArray talentArray = null;

        if (talentsObject != null) {
            talentArray = talentsObject.optJSONArray("talents");
        }

        if (talentArray != null) {
            talentSize = talentArray.length();
            if (talentSize > 0) {
                for (int i = 0; i < talentArray.length(); i++) {
                    JSONObject talentObject = talentArray.optJSONObject(i);
                    if (talentObject != null) {
                        SubGroupActivity.Talent talent = getTalent(talentObject);
                        mTalentList.add(talent);
                    }
                }
            }
        }

        return talentSize;
    }
    
    private void getMyUniversityGroup() {
        HttpUtil.sendOkHttpRequest(getContext(), GET_MY_UNIVERSITY_SUBGROUP, new FormBody.Builder().build(), new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug)
                        Slog.d(TAG, "==========getMyUniversityGroup response text : " + responseText);
                    if (responseText != null) {
                        if (!TextUtils.isEmpty(responseText)) {
                            try {
                                JSONObject jsonObject = new JSONObject(responseText);
                                if (jsonObject != null) {
                                    JSONArray groupArray = jsonObject.optJSONArray("groups");
                                    if (groupArray != null && groupArray.length() > 0) {
                                        for (int i = 0; i < groupArray.length(); i++) {
                                            JSONObject groupObject = groupArray.getJSONObject(i);
                                            SubGroupActivity.SubGroup subGroup = new SubGroupActivity.SubGroup();
                                            subGroup.gid = groupObject.optInt("gid");
                                            subGroup.groupName = groupObject.optString("group_name");
                                            subGroup.groupLogoUri = groupObject.optString("logo_uri");
                                            subGroup.visitRecord = groupObject.optInt("visit_record");
                                            subGroup.activityCount = groupObject.optInt("activity_count");
                                            subGroupList.add(subGroup);
                                        }
                     handler.sendEmptyMessage(MY_UNIVERSITY_GROUP_GET_DONE);
                                    }

                                } else {
                                    handler.sendEmptyMessage(MY_CONDITION_NOT_SET);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }

    private void getRecommendContacts() {
        RequestBody requestBody = new FormBody.Builder()
                .add("step", String.valueOf(DEFAULT_RECOMMEND_COUNT))
                .add("page", String.valueOf(0)).build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_RECOMMEND_PERSON_URL, requestBody, new Callback() {
        @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug) Slog.d(TAG, "getRecommendContacts response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    processResponseText(responseText);
                }
                if (contactsList.size() > 0) {
                    handler.sendEmptyMessage(GET_RECOMMEND_MEMBER_DONE);
                } else {
                    handler.sendEmptyMessage(HAD_NO_RECOMMEND_MEMBER);
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }

    private void processResponseText(String responseText) {
        try {
            JSONArray contactsArray = new JSONObject(responseText).optJSONArray("results");
            JSONObject contactsObject;
            if (contactsArray != null){
                for (int i = 0; i < contactsArray.length(); i++) {
                    ContactsApplyListActivity.Contacts contacts = new ContactsApplyListActivity.Contacts();
                    contactsObject = contactsArray.getJSONObject(i);
                    contacts.setAvatar(contactsObject.optString("avatar"));
                    contacts.setUid(contactsObject.optInt("uid"));
                    contacts.setNickName(contactsObject.optString("nickname"));
                    contacts.setSex(contactsObject.optInt("sex"));
                    contacts.setSituation(contactsObject.optInt("situation"));
                    contacts.setUniversity(contactsObject.optString("university"));

                    if (contactsObject.optInt("situation") == 0) {
                        contacts.setMajor(contactsObject.optString("major"));
                        contacts.setDegree(contactsObject.optString("degree"));
                    } else {
                        contacts.setIndustry(contactsObject.optString("industry"));
                        contacts.setPosition(contactsObject.optString("position"));
                    }
                    contactsList.add(contacts);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setRecommendContactsView() {
        ConstraintLayout recommendContactsWrapper = mView.findViewById(R.id.schoolmate_wrapper);
        recommendContactsWrapper.setVisibility(View.VISIBLE);
        LinearLayout contactsWrapper = mView.findViewById(R.id.contacts_wrapper);
        int size = 0;
        if (contactsList.size() > 8) {
            size = 8;
        } else {
            size = contactsList.size();
        }
        
        for (int i = 0; i < size; i++) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.contacts_recommend_item, null);
            if (contactsWrapper == null) {
                return;
            }

            contactsWrapper.addView(view);
            RoundImageView avatarView = view.findViewById(R.id.head_uri);
            final ContactsApplyListActivity.Contacts userProfile = contactsList.get(i);
            String avatar = userProfile.getAvatar();
            if (avatar != null && !"".equals(avatar)) {
                Glide.with(getContext()).load(HttpUtil.DOMAIN + avatar).into(avatarView);
            } else {
                if (userProfile.getSex() == 0) {
                    avatarView.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.male_default_avator));
                } else {
                    avatarView.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.female_default_avator));
                }
            }

            TextView name = view.findViewById(R.id.name);
            name.setText(userProfile.getNickName());
            LinearLayout education = view.findViewById(R.id.education);
            LinearLayout work = view.findViewById(R.id.work);
            if (userProfile.getSituation() == 0) {
                TextView degree = view.findViewById(R.id.degree);
                TextView major = view.findViewById(R.id.major);
                TextView university = view.findViewById(R.id.university);

                degree.setText(userProfile.getDegreeName(userProfile.getDegree()));
                major.setText(userProfile.getMajor());
                university.setText(userProfile.getUniversity());
            } else {
                if (education.getVisibility() == View.VISIBLE) {
                    education.setVisibility(View.GONE);
                }
                 if (work.getVisibility() == View.GONE) {
                    work.setVisibility(View.VISIBLE);
                }
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ParseUtils.startMeetArchiveActivity(getContext(), userProfile.getUid());
                }
            });

        }
        
         Button moreContactsBtn = mView.findViewById(R.id.more_schoolmate);
        moreContactsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyApplication.getContext(), CommonContactsActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (isDebug)
            Slog.d(TAG, "===================onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode);
        if (requestCode == Activity.RESULT_FIRST_USER) {
            switch (resultCode) {
                case COMMENT_UPDATE_RESULT:
                    int commentCount = data.getIntExtra("commentCount", 0);
                    if (isDebug) Slog.d(TAG, "==========commentCount: " + commentCount);
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putInt("commentCount", commentCount);
                    msg.setData(bundle);
                    msg.what = COMMENT_COUNT_UPDATE;
                    handler.sendMessage(msg);
                    break;
                    case MY_COMMENT_UPDATE_RESULT:
                    int myCommentCount = data.getIntExtra("commentCount", 0);
                    if (isDebug) Slog.d(TAG, "==========commentCount: " + myCommentCount);
                    Message message = new Message();
                    Bundle myBundle = new Bundle();
                    myBundle.putInt("commentCount", myCommentCount);
                    message.setData(myBundle);
                    message.what = MY_COMMENT_COUNT_UPDATE;
                    handler.sendMessage(message);
                    break;
                case PRAISE_UPDATE_RESULT:
                    handler.sendEmptyMessage(PRAISE_UPDATE);
                    break;
                    
                    case MY_PRAISE_UPDATE_RESULT:
                    handler.sendEmptyMessage(MY_CONDITION_PRAISE_UPDATE);
                    break;
                case LOVE_UPDATE_RESULT:
                    handler.sendEmptyMessage(LOVE_UPDATE);
                    break;
                case MY_LOVE_UPDATE_RESULT:
                    handler.sendEmptyMessage(MY_CONDITION_LOVE_UPDATE);
                    break;
                default:
                    break;
            }
        }
    }
    
    private void setRecommendTalentsHeader(){
        ConstraintLayout talentsWrapperCL = mView.findViewById(R.id.talent_wrapper);
        talentsWrapperCL.setVisibility(View.VISIBLE);
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        int innerWidth = dm.widthPixels - (int) Utility.dpToPx(getContext(), 32f);
        int height = innerWidth;
        int wrapperWidth = innerWidth/2;
        int avatarWidth = wrapperWidth - (int) Utility.dpToPx(getContext(), 12f);

        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(wrapperWidth, WRAP_CONTENT);

        GridLayout talentWrapper = mView.findViewById(R.id.talent_recommend_wrapper);
        if (talentWrapper == null){
            return;
        }
        
        int size = mTalentList.size();
        if (size > 4){
            size = 4;
        }
        for (int i = 0; i < size; i++) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.recommend_talent_item, null);
            talentWrapper.addView(view, params);
            final SubGroupActivity.Talent talent = mTalentList.get(i);
            RoundImageView avatarRV = view.findViewById(R.id.avatar);
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(avatarWidth, avatarWidth);
            layoutParams.setMargins(0, 0, 2, 2);
            avatarRV.setLayoutParams(layoutParams);
            String avatar = talent.profile.getAvatar();
            if (avatar != null && !"".equals(avatar)) {
                Glide.with(getContext()).load(HttpUtil.DOMAIN + avatar).into(avatarRV);
            }else {
                if (talent.profile.getSex() == 0) {
                    avatarRV.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.male_default_avator));
                } else {
                    avatarRV.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.female_default_avator));
                }
            }
            
            TextView nickname = view.findViewById(R.id.name);
            nickname.setText(talent.profile.getNickName());

            TextView degree = view.findViewById(R.id.degree);
            TextView university = view.findViewById(R.id.university);

            if (talent.profile.getSituation() == student){
                degree.setText(talent.profile.getDegreeName(talent.profile.getDegree()));
                university.setText(talent.profile.getUniversity());
            }else {
                degree.setText(talent.profile.getPosition());
                university.setText(talent.profile.getIndustry());
            }
            
            TextView introduction = view.findViewById(R.id.introduction);
            introduction.setText(talent.introduction);

            TextView titleTV = view.findViewById(R.id.talent_title);
            titleTV.setText(talent.title);

            TextView subject = view.findViewById(R.id.subject);
            subject.setText("擅长："+talent.subject);
            
            if (talent.evaluateCount > 0) {
                TextView evaluateCountTV = view.findViewById(R.id.evaluate_count);
                float scoreFloat = talent.evaluateScores / talent.evaluateCount;
                float score = (float) (Math.round(scoreFloat * 10)) / 10;
                evaluateCountTV.setText(getResources().getString(R.string.fa_star) +" "+ score +"("+ talent.evaluateCount+")");
            }
            
            if (talent.answerCount > 0){
                TextView answerCountTV = view.findViewById(R.id.answer_count);
                if(talent.evaluateCount > 0){
                    answerCountTV.setText(getResources().getString(R.string.dot) + "解答"+talent.answerCount);
                }else {
                    answerCountTV.setText("解答"+talent.answerCount);
                }
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), TalentDetailsActivity.class);
                    //intent.putExtra("talent", talent);
                    intent.putExtra("tid", talent.tid);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(intent);
                }
            });
            
            Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.evaluate_count), font);
        }

        Button moreTalentsBtn = mView.findViewById(R.id.more_talent);
        if (mLoadTalentSize > 4){
            moreTalentsBtn.setVisibility(View.VISIBLE);
        }
        moreTalentsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), TalentSummaryListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });
        
        TextView talentConsultsTV = mView.findViewById(R.id.talent_consult);
        talentConsultsTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ConsultSummaryActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });

    }
    
    private class PictureAddBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ADD_PICTURE_BROADCAST:
                    if (isDebug) Slog.d(TAG, "==========ADD_PICTURE_BROADCAST");
                    lookFriend.setVisibility(View.GONE);
                    meetList.clear();
                    //force update data
                    loadData();
                    break;
                case AVATAR_SET_ACTION_BROADCAST:
                    avatarSet = true;
                    break;
                default:
                    break;
            }
        }
    }
    
    //register local broadcast to receive DYNAMICS_ADD_BROADCAST
    private void registerLoginBroadcast() {
        mReceiver = new PictureAddBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ADD_PICTURE_BROADCAST);
        intentFilter.addAction(AVATAR_SET_ACTION_BROADCAST);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, intentFilter);
    }

    //unregister local broadcast
    private void unRegisterLoginBroadcast() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (isDebug) Slog.d(TAG, "=============onResume");
        //getUserProfile();
        //updateData();
        //meetRecommendListAdapter.setData(meetList);
        //meetRecommendListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterLoginBroadcast();

        /*
        if (recyclerView != null) {
            recyclerView.destroy();
            recyclerView = null;
        }
         */
    }
        
    public void handleMessage(Message message) {
        switch (message.what) {
             case GET_RECOMMEND_LANGUAGE_CULTURE_DONE:
                if (mLoadExperienceSize > 0){
                    setRecommendExperiencesView(Utility.ExperienceType.LANGUAGE_CULTURE.getType());
                }
                getRecommendExperiences(Utility.ExperienceType.PARTY_SALON.getType());
                break;
            case GET_RECOMMEND_PARTY_SALON_DONE:
                if (mLoadExperienceSize > 0){
                    setRecommendExperiencesView(Utility.ExperienceType.PARTY_SALON.getType());
                }
                getRecommendExperiences(Utility.ExperienceType.FRIENDSHIP.getType());
                break;
            case GET_RECOMMEND_FRIENDSHIP_DONE:
                if (mLoadExperienceSize > 0){
                    setRecommendExperiencesView(Utility.ExperienceType.FRIENDSHIP.getType());
                }
                getRecommendExperiences(Utility.ExperienceType.NATURAL_OUTDOOR.getType());
                break;
             case GET_RECOMMEND_NATURAL_EXPERIENCES_DONE:
                if (mLoadExperienceSize > 0){
                    setRecommendExperiencesView(Utility.ExperienceType.NATURAL_OUTDOOR.getType());
                }
                getRecommendExperiences(Utility.ExperienceType.HUMANITY_ART.getType());
                break;
            case GET_RECOMMEND_HUMANITY_EXPERIENCES_DONE:
                if (mLoadExperienceSize > 0){
                    setRecommendExperiencesView(Utility.ExperienceType.HUMANITY_ART.getType());
                }
                getRecommendExperiences(Utility.ExperienceType.NGO_PUBLIC_GOOD.getType());
                break;
            case GET_RECOMMEND_NGO_EXPERIENCES_DONE:
                if (mLoadExperienceSize > 0){
                    setRecommendExperiencesView(Utility.ExperienceType.NGO_PUBLIC_GOOD.getType());
                }
                getRecommendExperiences(Utility.ExperienceType.THEATRE_PERFORMANCE.getType());
                break;
            case GET_RECOMMEND_THEATRE_EXPERIENCES_DONE:
                if (mLoadExperienceSize > 0){
                    setRecommendExperiencesView(Utility.ExperienceType.THEATRE_PERFORMANCE.getType());
                }
                getRecommendExperiences(Utility.ExperienceType.LEARNING_GROWTH.getType());
                break;
            case GET_RECOMMEND_LEARNING_GROWTH_DONE:
                if (mLoadExperienceSize > 0){
                    setRecommendExperiencesView(Utility.ExperienceType.LEARNING_GROWTH.getType());
                }
                getRecommendExperiences(Utility.ExperienceType.HOBBY.getType());
                break;
            case GET_RECOMMEND_HOBBY_DONE:
                if (mLoadExperienceSize > 0){
                    setRecommendExperiencesView(Utility.ExperienceType.HOBBY.getType());
                }
                getRecommendMeet();
                break;
            case GET_ALL_GUIDS_DONE:
                if (mLoadGuideSize > 0){
                    setRecommendGuidesView();
                }
                break;
            case GET_RECOMMEND_MEETS_DONE:
                if (mLoadMeetSize > 0){
                    setRecommendMeetsView();
                }
                getRecommendTalent();
                break;
                case GET_RECOMMEND_TALENTS_DONE:
                if (mLoadTalentSize > 0){
                    setRecommendTalentsHeader();
                }
                //getRecommendMeet();
                getRecommendContacts();
                break;
            case GET_RECOMMEND_MEMBER_DONE:
                setRecommendContactsView();
                break;
                case COMMENT_COUNT_UPDATE:
                Bundle bundle = message.getData();
                int commentCount = bundle.getInt("commentCount");
                if (isDebug)
                    Slog.d(TAG, "------------------>COMMENT_COUNT_UPDATE: position: " + currentPosition + " commentCount: " + commentCount);
                meetList.get(currentPosition).setCommentCount(commentCount);
                meetRecommendListAdapter.setData(meetList);
                meetRecommendListAdapter.notifyDataSetChanged();
                break;
            case MY_COMMENT_COUNT_UPDATE:
                Bundle myBundle = message.getData();
                int myCommentCount = myBundle.getInt("commentCount");
                myCondition.setCommentCount(myCommentCount);
                commentCountView.setText(String.valueOf(myCommentCount));
                break;
                case PRAISE_UPDATE:
                meetList.get(currentPosition).setPraisedCount(meetList.get(currentPosition).getPraisedCount() + 1);
                meetList.get(currentPosition).setPraised(1);
                meetRecommendListAdapter.setData(meetList);
                meetRecommendListAdapter.notifyDataSetChanged();
                break;
            case LOVE_UPDATE:
                meetList.get(currentPosition).setLovedCount(meetList.get(currentPosition).getLovedCount() + 1);
                meetList.get(currentPosition).setLoved(1);
                meetRecommendListAdapter.setData(meetList);
                meetRecommendListAdapter.notifyDataSetChanged();
                break;
                case MY_CONDITION_LOVE_UPDATE:
                myCondition.setLoved(1);
                myCondition.setLovedCount(myCondition.getLovedCount() + 1);
                lovedView.setText(String.valueOf(myCondition.getLovedCount()));
                lovedIcon.setText(R.string.fa_heart);
                break;
            case MY_CONDITION_PRAISE_UPDATE:
                myCondition.setPraised(1);
                myCondition.setPraisedCount(myCondition.getPraisedCount() + 1);
                thumbsView.setText(String.valueOf(myCondition.getPraisedCount()));
                thumbsIcon.setText(R.string.fa_thumbs_up);
                break;
            default:
                break;
        }
    }
    
    static class MyHandler extends HandlerTemp<RecommendFragment> {

        public MyHandler(RecommendFragment cls) {
            super(cls);
        }

        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            RecommendFragment meetRecommendFragment = ref.get();
            if (meetRecommendFragment != null) {
                meetRecommendFragment.handleMessage(message);
            }
        }
    }

}
