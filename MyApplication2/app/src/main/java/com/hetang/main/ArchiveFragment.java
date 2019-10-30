package com.hetang.main;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hetang.archive.BlogEditDialogFragment;
import com.hetang.archive.EducationEditDialogFragment;
import com.hetang.archive.PaperEditDialogFragment;
import com.hetang.archive.PrizeEditDialogFragment;
import com.hetang.archive.VolunteerEditDialogFragment;
import com.hetang.archive.WorkEditActivity;
import com.hetang.common.Chat;
import com.hetang.common.SettingsActivity;
import com.hetang.R;
import com.hetang.meet.SpecificUserDynamicsActivity;
import com.hetang.util.BaseFragment;
import com.hetang.util.CommonUserListDialogFragment;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.common.MyApplication;
import com.hetang.util.ParseUtils;
import com.hetang.util.RoundImageView;
import com.hetang.util.SetAvatarActivity;
import com.hetang.util.Slog;
import com.hetang.util.UserProfile;
import com.hetang.util.SharedPreferencesUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;


import static com.hetang.meet.MeetArchiveActivity.APPLIED;
import static com.hetang.meet.MeetArchiveActivity.CONTACTS_ADD_URL;
import static com.hetang.meet.MeetArchiveActivity.ESTABLISHED;
import static com.hetang.meet.MeetArchiveActivity.FOLLOWED;
import static com.hetang.meet.MeetArchiveActivity.FOLLOW_ACTION_URL;
import static com.hetang.meet.MeetArchiveActivity.GET_CONTACTS_STATUS_DONE;
import static com.hetang.meet.MeetArchiveActivity.GET_CONTACTS_STATUS_URL;
import static com.hetang.meet.MeetArchiveActivity.GET_FOLLOW_DONE;
import static com.hetang.meet.MeetArchiveActivity.GET_FOLLOW_STATUS_URL;
import static com.hetang.meet.MeetArchiveActivity.GET_LOGGEDIN_ACCOUNT;
import static com.hetang.meet.MeetArchiveActivity.GET_PRAISE_STATISTICS_URL;
import static com.hetang.meet.MeetArchiveActivity.GET_PRAISE_STATISTICS_URL_DONE;
import static com.hetang.meet.MeetArchiveActivity.PRAISED;
import static com.hetang.util.ParseUtils.startMeetArchiveActivity;
import static com.hetang.util.SharedPreferencesUtils.getYunXinAccount;
import static com.hetang.meet.MeetDynamicsFragment.REQUEST_CODE;

/**
 * Created by super-zou on 17-9-11.
 */

public class ArchiveFragment extends BaseFragment {
    private static final String TAG = "ArchiveFragment";
    private static final boolean isDebug = false;
    private Handler handler;
    UserProfile userProfile;
    JSONObject mSummary = null;
    JSONArray mEducationBackground = null;
    JSONArray mWorkExperience = null;
    JSONArray mPrize = null;
    JSONArray mPaper = null;
    JSONArray mBlog = null;
    JSONArray mVolunteer = null;
    
    public static final String GET_ACTIVITIES_COUNT_BY_UID = HttpUtil.DOMAIN + "?q=dynamic/get_count_by_uid";
    public static final String GET_USER_PROFILE_URL = HttpUtil.DOMAIN + "?q=account_manager/get_user_profile";
    public static final String SET_USER_PROFILE_URL = HttpUtil.DOMAIN + "?q=account_manager/set_user_profile";
    public static final String GET_EDUCATION_BACKGROUND_URL = HttpUtil.DOMAIN + "?q=personal_archive/education_background/load";
    public static final String GET_WORK_EXPERIENCE_URL = HttpUtil.DOMAIN + "?q=personal_archive/work_experience/load";
    public static final String GET_PRIZE_URL = HttpUtil.DOMAIN + "?q=personal_archive/prize/load";
    public static final String GET_PAPER_URL = HttpUtil.DOMAIN + "?q=personal_archive/paper/load";
    public static final String GET_BLOG_URL = HttpUtil.DOMAIN + "?q=personal_archive/blog/load";
    public static final String GET_VOLUNTEER_URL = HttpUtil.DOMAIN + "?q=personal_archive/volunteer/load";
    public static final String GET_FOLLOW_STATISTICS_URL = HttpUtil.DOMAIN + "?q=follow/statistics";
    public static final int GET_USER_PROFILE_DONE = 0;
    public static final int GET_EDUCATION_BACKGROUND_DONE = 1;
    public static final int GET_WORK_EXPERIENCE_DONE = 2;
    public static final int GET_ACTIVITIES_COUNT_DONE = 3;
    public static final int GET_FOLLOW_STATISTICS_URL_DONE = 4;
    public static final int GET_PRIZE_DONE = 5;
    public static final int GET_PAPER_DONE = 6;
    public static final int GET_BLOG_DONE = 7;
    public static final int GET_VOLUNTEER_DONE = 8;
    
    public final static int REQUESTCODE = 1;
    public final static int SET_AVATAR_RESULT_OK = 2;
    public final static int SET_WORK_RESULT_OK = 3;
    public final static int SET_EDUCATION_RESULT_OK = 4;
    public final static int SET_PRIZE_RESULT_OK = 5;
    public final static int SET_PAPER_RESULT_OK = 6;
    public final static int SET_BLOG_RESULT_OK = 7;
    public final static int SET_VOLUNTEER_RESULT_OK = 8;

    private boolean isFollowed = false;
    
    private LinearLayout mEducationBackgroundListView = null;
    private LinearLayout mWorkExperienceListView = null;
    private LinearLayout mPrizeListView = null;
    private LinearLayout mPaperListView ;
    private LinearLayout mBlogListView = null;
    private LinearLayout mVolunteerListView = null;
    RoundImageView headUri;
    int uid = 0;
    long authorUid = 0;
    Typeface font;
    private int contactStatus = -1;
    View mView;
    Chat chat;
    TextView mSettings;
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.archive, container, false);
        mView = view;
        handler = new MyHandler(this);
        authorUid = SharedPreferencesUtils.getSessionUid(MyApplication.getContext());

        Slog.d(TAG, "------------------authorUid: "+authorUid);

        mSettings = view.findViewById(R.id.settings);
        
        mEducationBackgroundListView = view.findViewById(R.id.education_background_list);
        mWorkExperienceListView = view.findViewById(R.id.work_experience_list);
        mPrizeListView = view.findViewById(R.id.prize_list);
        mPaperListView = view.findViewById(R.id.paper_list);
        mBlogListView = view.findViewById(R.id.blog_list);
        mVolunteerListView = view.findViewById(R.id.volunteer_list);

        if (getArguments() != null){
            uid = getArguments().getInt("uid");
            loadArchiveData();
        }else {
            getCurrentUid();
        }
        
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MyApplication.getContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");

        FontManager.markAsIconContainer(mView.findViewById(R.id.archive), font);

        return view;
    }
    
    private void getCurrentUid(){
        RequestBody requestBody = new FormBody.Builder().build();
        HttpUtil.sendOkHttpRequest(getContext(), GET_LOGGEDIN_ACCOUNT, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (responseText != null) {
                        try {
                            //String responseText = response.body().string();
                            uid = new JSONObject(responseText).optInt("uid");
                            loadArchiveData();
                            }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {}
        });
    }

    @Override
    protected void initView(View view) { }
    
    @Override
    protected void loadData() { }

    private void loadArchiveData(){
        if (authorUid == uid){
            mSettings.setVisibility(View.VISIBLE);
        }else {
            mSettings.setVisibility(View.GONE);
        }

        getUserProfile(uid);
        getFollowStatus(uid);
        getDynamicsCount(uid);
        getFollowedCount(uid);
        getContactsStatus();
        getPraisedCount(uid);
        getEducationBackground();

    }
    
    private void getUserProfile(int uid){
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(getContext(), GET_USER_PROFILE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();

                    if (responseText != null) {
                    if (!TextUtils.isEmpty(responseText)) {
                            try {
                                if (isDebug) Slog.d(TAG, "==============responseText: "+responseText);
                                JSONObject jsonObject = new JSONObject(responseText).optJSONObject("user");
                                userProfile = ParseUtils.getUserProfileFromJSONObject(jsonObject);
                                if(userProfile != null){
                                    handler.sendEmptyMessage(GET_USER_PROFILE_DONE);
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

    private void setProfileView(){

        TextView setAvatar = mView.findViewById(R.id.set_avatar);
        TextView name = mView.findViewById(R.id.name);
        TextView sex = mView.findViewById(R.id.sex);
        TextView summary = mView.findViewById(R.id.summary);
        LinearLayout educationWrapper = mView.findViewById(R.id.education);
        TextView living = mView.findViewById(R.id.living);
        headUri = mView.findViewById(R.id.head_uri);
        TextView degree = mView.findViewById(R.id.degree);
        TextView university = mView.findViewById(R.id.university);
        LinearLayout majorWrap = mView.findViewById(R.id.major_wrap);
        LinearLayout work = mView.findViewById(R.id.work);
        TextView major = mView.findViewById(R.id.major);
        TextView position = mView.findViewById(R.id.position);
        TextView industry = mView.findViewById(R.id.industry);
        TextView hometown = mView.findViewById(R.id.hometown);
        final LinearLayout experienceList = mView.findViewById(R.id.experience_list);
        LinearLayout interactWrapper = mView.findViewById(R.id.interaction_wrap);

        //for introduction
        final TextView introduction = mView.findViewById(R.id.introduction);
        final TextView addIntroduction = mView.findViewById(R.id.add_introduction);
        final TextInputEditText introductionEdit = mView.findViewById(R.id.introduction_edit);
         final TextView saveIntroduction = mView.findViewById(R.id.save_introduction);
        //for education background
        TextView addEducation = mView.findViewById(R.id.add_education);
        //for work experience
        TextView addWorkExperience = mView.findViewById(R.id.add_work);
        final TextView experienceExtend  = mView.findViewById(R.id.experience_extend);
        final ScrollView scrollView = mView.findViewById(R.id.archive_scroll_view);
        LinearLayout experienceWrapper = mView.findViewById(R.id.experience_wrap);
        TextView extend = mView.findViewById(R.id.experience_extend);
        
        RelativeLayout paperWrapper = mView.findViewById(R.id.paper_add_wrapper);
        RelativeLayout blogWrapper = mView.findViewById(R.id.blog_add_wrapper);
        RelativeLayout volunteerWrapper = mView.findViewById(R.id.volunteer_add_wrapper);

        if(userProfile.getAuthorSelf() == true){
            interactWrapper.setVisibility(View.GONE);
            setAvatar.setVisibility(View.VISIBLE);
            addIntroduction.setVisibility(View.VISIBLE);
            addEducation.setVisibility(View.VISIBLE);
            addWorkExperience.setVisibility(View.VISIBLE);
            addEducationBackground(addEducation);
            addWorkExperience(addWorkExperience);
            experienceWrapper.setVisibility(View.VISIBLE);
            extend.setVisibility(View.VISIBLE);
            moreQualificationAction();
        }
        
        setAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SetAvatarActivity.class);
                startActivityForResult(intent, REQUESTCODE);
            }
        });
        
        if (!"".equals(userProfile.getAvatar())) {
            String picture_url = HttpUtil.DOMAIN + userProfile.getAvatar();
            if (getActivity() != null){
                Glide.with(getActivity()).load(picture_url).into(headUri);
            }
        } else {
            if(userProfile.getSex() == 0){
                headUri.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.male_default_avator));
            }else {
                headUri.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.female_default_avator));
            }
        }
        
        name.setText(userProfile.getName());

        if(userProfile.getSex() == 0){
            sex.setText(R.string.mars);
        }else {
            sex.setText(R.string.venus);
        }

        if(userProfile.getLiving() != null && !"".equals(userProfile.getLiving())){
            living.setText("现居"+userProfile.getLiving().trim());
        }

        if(!"".equals(userProfile.getSummary()) && !"".equals(userProfile.getSummary())){
            summary.setText(userProfile.getSummary());
        }
        
        if (userProfile.getSituation() != -1){
            educationWrapper.setVisibility(View.VISIBLE);
            degree.setText(userProfile.getDegreeName(userProfile.getDegree()));
            university.setText(userProfile.getUniversity());

            if(userProfile.getSituation() == 0){
                majorWrap.setVisibility(View.VISIBLE);
                major.setText(userProfile.getMajor());
            }else {
                if(work.getVisibility() == View.GONE){
                    work.setVisibility(View.VISIBLE);
                }
                position.setText(userProfile.getPosition());
                industry.setText(userProfile.getIndustry());
            }
        }
        
        if(!"".equals(userProfile.getHometown())){
            hometown.setText(getResources().getText(R.string.hometown)+":"+userProfile.getHometown());
        }

        if(!TextUtils.isEmpty(userProfile.getIntroduction()) && !"null".equals(userProfile.getIntroduction())){
            introduction.setVisibility(View.VISIBLE);
            introduction.setText(userProfile.getIntroduction());

            if(userProfile.getAuthorSelf() == true){
                addIntroduction.setText(getString(R.string.edit_introduction));
            }
        }

        final LinearLayout introductionEditWrapper = mView.findViewById(R.id.introduction_edit_wrapper);
        addIntroduction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(introductionEditWrapper.getVisibility() == View.GONE){
                    introductionEditWrapper.setVisibility(View.VISIBLE);
                }
                if(!"".equals(introduction.getText())) {
                    introductionEdit.setText(introduction.getText());
                }
            }
        });


        saveIntroduction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(introductionEdit.getText())){
                    saveIntroduction(introductionEdit.getText().toString(), introduction);
                    addIntroduction.setText(getString(R.string.edit_introduction));
                }
            }
        });
        
        LinearLayout meetArchiveNav = mView.findViewById(R.id.meet_archive);
        if(userProfile.getCid() != 0){
            meetArchiveNav.setVisibility(View.VISIBLE);
        }
        meetArchiveNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUtils.startMeetArchiveActivity(MyApplication.getContext(), userProfile.getUid());
            }
        });

        getWorkExperience();        
        getPrize();
        getPaper();
        getBlog();
        getVolunteer();

        experienceWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(experienceList.getVisibility() == View.VISIBLE){
                    experienceList.setVisibility(View.GONE);
                    experienceExtend.setText(getString(R.string.fa_chevron_down));
                }else {
                
                experienceList.setVisibility(View.VISIBLE);
                    experienceExtend.setText(getString(R.string.fa_chevron_up));

                    ConstraintLayout constraintLayout = mView.findViewById(R.id.activity_archive_wrapper);
                    constraintLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            int height = experienceList.getHeight();
                            int scrollY = scrollView.getScrollY();
                            scrollView.scrollTo(0, scrollY + height);
                        }
                    });
                }
            }
        });

    }
    
    private void getContactsStatus(){
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();

        //for contacts
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_CONTACTS_STATUS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    // if(isDebug)
                    if (isDebug) Slog.d(TAG, "==========getContacts Status : " + responseText);
                    try {
                    JSONObject status = new JSONObject(responseText);
                        contactStatus = status.optInt("status");
                        handler.sendEmptyMessage(GET_CONTACTS_STATUS_DONE);
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
    
    private void moreQualificationAction(){

        RelativeLayout prizeWrapper = mView.findViewById(R.id.prize_add_wrapper);
        RelativeLayout paperWrapper = mView.findViewById(R.id.paper_add_wrapper);
        RelativeLayout blogWrapper = mView.findViewById(R.id.blog_add_wrapper);
        RelativeLayout volunteerWrapper = mView.findViewById(R.id.volunteer_add_wrapper);


        prizeWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PrizeEditDialogFragment prizeEditDialogFragment = new PrizeEditDialogFragment();
                prizeEditDialogFragment.setTargetFragment(ArchiveFragment.this, REQUESTCODE);
                prizeEditDialogFragment.show(getFragmentManager(), "PrizeEditDialogFragment");
            }
        });
        
        paperWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PaperEditDialogFragment paperEditDialogFragment = new PaperEditDialogFragment();
                paperEditDialogFragment.setTargetFragment(ArchiveFragment.this, REQUESTCODE);
                paperEditDialogFragment.show(getFragmentManager(), "PaperEditDialogFragment");
            }
        });

        blogWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BlogEditDialogFragment blogEditDialogFragment = new BlogEditDialogFragment();
                blogEditDialogFragment.setTargetFragment(ArchiveFragment.this, REQUESTCODE);
                blogEditDialogFragment.show(getFragmentManager(), "BlogEditDialogFragment");
            }
        });
        
        volunteerWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VolunteerEditDialogFragment volunteerEditDialogFragment = new VolunteerEditDialogFragment();
                volunteerEditDialogFragment.setTargetFragment(ArchiveFragment.this, REQUESTCODE);
                volunteerEditDialogFragment.show(getFragmentManager(), "VolunteerEditDialogFragment");
            }
        });

    }

    private void saveIntroduction(String introduction, TextView introductionView){
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(userProfile.getUid()))
                .add("introduction", introduction).build();
                
                if(introductionView.getVisibility() == View.GONE){
            introductionView.setVisibility(View.VISIBLE);
        }
        introductionView.setText(introduction);
        LinearLayout introductionEditWrapper = mView.findViewById(R.id.introduction_edit_wrapper);
        if (introductionEditWrapper.getVisibility() == View.VISIBLE){
            introductionEditWrapper.setVisibility(View.GONE);
        }

        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), SET_USER_PROFILE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {}

            @Override
            public void onFailure(Call call, IOException e) {}
        });
    }
    
     private void addEducationBackground(TextView addEducation){
        addEducation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EducationEditDialogFragment educationEditDialogFragment = new EducationEditDialogFragment();
                educationEditDialogFragment.setTargetFragment(ArchiveFragment.this, REQUESTCODE);
                educationEditDialogFragment.show(getFragmentManager(), "EducationEditDialogFragment");
            }
        });
    }

    private void addWorkExperience(TextView addWorkExperience){
        addWorkExperience.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), WorkEditActivity.class);
                startActivityForResult(intent, REQUESTCODE);
            }
        });
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (isDebug) Slog.d(TAG, "--------------->onActivityResult requestCode: "+requestCode+" resultCode: "+resultCode);
        if(requestCode == REQUESTCODE){
            switch (resultCode){
                case SET_AVATAR_RESULT_OK:
                    String avatar = data.getStringExtra("avatar");
                    Glide.with(this).load(HttpUtil.DOMAIN + avatar).into(headUri);
                    break;
                case SET_WORK_RESULT_OK:
                    getWorkExperience();
                    break;
                    case SET_EDUCATION_RESULT_OK:
                    getEducationBackground();
                    break;
                case SET_PRIZE_RESULT_OK:
                    getPrize();
                    break;
                case SET_PAPER_RESULT_OK:
                    getPaper();
                    break;
                case SET_BLOG_RESULT_OK:
                    getBlog();
                    break;
                case SET_VOLUNTEER_RESULT_OK:
                    getVolunteer();
                    break;
                default:
                    break;
            }
        }
    }
    
    private void getDynamicsCount(final int uid){

        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .build();

        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_ACTIVITIES_COUNT_BY_UID, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========getDynamicsCount response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        try {
                            int count = new JSONObject(responseText).optInt("count");
                            if (isDebug) Slog.d(TAG, "==========getDynamicsCount response count : " + count);
                            if(count > 0){
                                Message msg = new Message();
                                Bundle bundle = new Bundle();
                                bundle.putInt("count", count);
                                msg.setData(bundle);
                                msg.what = GET_ACTIVITIES_COUNT_DONE;
                                handler.sendMessage(msg);
                            }

                        }catch (JSONException e){
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
    
     private void getFollowedCount(int uid){
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_FOLLOW_STATISTICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if(isDebug)
                        Slog.d(TAG, "==========getFollow statistics : " + responseText);
                    try {
                    JSONObject followObject = new JSONObject(responseText);
                        int followed_count = followObject.optInt("followed_count");
                        if (followed_count != 0) {
                            Bundle bundle = new Bundle();
                            bundle.putInt("count", followed_count);
                            Message msg = new Message();
                            msg.setData(bundle);
                            msg.what = GET_FOLLOW_STATISTICS_URL_DONE;
                            handler.sendMessage(msg);
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

    private void getFollowStatus(int uid){
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_FOLLOW_STATUS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
            if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========getFollowStatus : " + responseText);
                    try {
                        JSONObject status = new JSONObject(responseText);
                        isFollowed = status.optBoolean("isFollowed");
                        handler.sendEmptyMessage(GET_FOLLOW_DONE);
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
    
    private void getPraisedCount(int uid){
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_PRAISE_STATISTICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();

                    try {
                        JSONObject praiseObject = new JSONObject(responseText);
                        int praised_count = praiseObject.optInt("praised_count");
                        Bundle bundle = new Bundle();
                        bundle.putInt("praised_count", praised_count);
                        Message msg = new Message();
                        msg.setData(bundle);
                        msg.what = GET_PRAISE_STATISTICS_URL_DONE;
                        handler.sendMessage(msg);

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

    public void getEducationBackground(){
        Slog.d(TAG,"-------------------->getEducationBackground");
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_EDUCATION_BACKGROUND_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if(isDebug) Slog.d(TAG, "================getEducationBackground response:" + responseText);
                if (responseText != null && !TextUtils.isEmpty(responseText)) {
                    try {
                        mEducationBackground = new JSONObject(responseText).optJSONArray("education");
                        if (isDebug) Slog.d(TAG, "================getEducationBackground education:" + mEducationBackground);
                        if (mEducationBackground != null) {
                            handler.sendEmptyMessage(GET_EDUCATION_BACKGROUND_DONE);
                        }
                        } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) { }
        });
    }
    
    private void setEducationBackgroundView(){
        if(mEducationBackgroundListView.getChildCount() > 0){
            mEducationBackgroundListView.removeAllViews();
        }
        for (int i=0; i<mEducationBackground.length(); i++){
            try {
                JSONObject education = mEducationBackground.getJSONObject(i);
                View view = LayoutInflater.from(MyApplication.getContext()).inflate(R.layout.achieve_base_background, null);
                mEducationBackgroundListView.addView(view, i);
                TextView university = view.findViewById(R.id.title);
                TextView degree = view.findViewById(R.id.secondary_title);
                TextView major = view.findViewById(R.id.last_title);
                major.setVisibility(View.VISIBLE);
                
                TextView start = view.findViewById(R.id.start_time);
                TextView end = view.findViewById(R.id.end_time);

                university.setText(education.optString("university"));
                degree.setText(education.optString("degree"));
                major.setText(", "+education.optString("major"));

                start.setText(education.optString("entrance_year")+"年"+education.optString("entrance_month"));
                end.setText("~  "+education.optString("graduate_year")+"年"+education.optString("graduate_month"));
            }catch (JSONException e){
                e.printStackTrace();
            }

        }
    }
    
    public void getWorkExperience(){
        
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid)).build();
                
                HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_WORK_EXPERIENCE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if(isDebug) Slog.d(TAG, "================getWorkExperience response:" + responseText);
                if (responseText != null && !TextUtils.isEmpty(responseText)) {
                    try {
                        mWorkExperience = new JSONObject(responseText).optJSONArray("work");
                        if (isDebug) Slog.d(TAG, "================getWorkExperience work:" + mWorkExperience);
                        if (mWorkExperience != null) {
                            handler.sendEmptyMessage(GET_WORK_EXPERIENCE_DONE);
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

    public void getPrize(){
    
    RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_PRIZE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if(isDebug) Slog.d(TAG, "================getPrize response:" + responseText);
                if (responseText != null && !TextUtils.isEmpty(responseText)) {
                    try {
                        mPrize = new JSONObject(responseText).optJSONArray("prizes");
                        if (isDebug) Slog.d(TAG, "================getPrize prizes:" + mPrize);
                        if (mPrize != null && mPrize.length() > 0) {
                            handler.sendEmptyMessage(GET_PRIZE_DONE);
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
    
    public void getPaper(){

        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_PAPER_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if(isDebug) Slog.d(TAG, "================getPaper response:" + responseText);
                if (responseText != null && !TextUtils.isEmpty(responseText)) {
                    try {
                        mPaper = new JSONObject(responseText).optJSONArray("papers");
                        if (isDebug) Slog.d(TAG, "================getPaper papers:" + mPaper);
                        if (mPaper != null && mPaper.length() > 0) {
                            handler.sendEmptyMessage(GET_PAPER_DONE);
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
    
    public void getBlog(){

        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_BLOG_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if(isDebug) Slog.d(TAG, "================getPaper response:" + responseText);
                if (responseText != null && !TextUtils.isEmpty(responseText)) {
                try {
                        mBlog = new JSONObject(responseText).optJSONArray("blogs");
                        if (isDebug) Slog.d(TAG, "================getBlog blogs:" + mBlog);
                        if (mBlog != null && mBlog.length() > 0) {
                            handler.sendEmptyMessage(GET_BLOG_DONE);
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
    
    public void getVolunteer(){

        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_VOLUNTEER_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if(isDebug) Slog.d(TAG, "================getVolunteer response:" + responseText);
                if (responseText != null && !TextUtils.isEmpty(responseText)) {
                    try {
                        mVolunteer = new JSONObject(responseText).optJSONArray("volunteers");
                        if (isDebug) Slog.d(TAG, "================getVolunteer volunteers:" + mVolunteer);
                        if (mVolunteer != null && mVolunteer.length() > 0) {
                            handler.sendEmptyMessage(GET_VOLUNTEER_DONE);
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

    private void setWorkExperienceView(){
        if(mWorkExperienceListView.getChildCount() > 0){
            mWorkExperienceListView.removeAllViews();
        }
        
        for (int i=0; i<mWorkExperience.length(); i++){
            try {
                JSONObject workExperience = mWorkExperience.getJSONObject(i);
                View view = LayoutInflater.from(MyApplication.getContext()).inflate(R.layout.achieve_base_background, null);
                mWorkExperienceListView.addView(view, i);
                TextView jobTitle = view.findViewById(R.id.title);
                TextView company = view.findViewById(R.id.secondary_title);
                TextView industry = view.findViewById(R.id.last_title);
                TextView start = view.findViewById(R.id.start_time);
                TextView end = view.findViewById(R.id.end_time);
                
                jobTitle.setText(workExperience.optString("position"));
                company.setText(workExperience.optString("company"));
                industry.setText(", "+workExperience.optString("industry"));

                start.setText(workExperience.optInt("entrance_year")+"年");
                if(workExperience.optInt("now") == 1 && workExperience.optInt("leave_year") == 0){
                    end.setText("~  "+"至今");
                }else {
                    end.setText("~  "+workExperience.optString("leave_year")+"年");
                }

            }catch (JSONException e){
                e.printStackTrace();
            }

        }
    }
    
    private void setPrizeView(){

        ConstraintLayout prizeWrapper = mView.findViewById(R.id.prize_wrapper);
        prizeWrapper.setVisibility(View.VISIBLE);

        TextView addPrize = mView.findViewById(R.id.add_new_prize);
        RelativeLayout addPrizeWrapper = mView.findViewById(R.id.prize_add_wrapper);
        addPrizeWrapper.setVisibility(View.GONE);
        if (authorUid == uid){
            addPrize.setVisibility(View.VISIBLE);
            addPrize.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PrizeEditDialogFragment prizeEditDialogFragment = new PrizeEditDialogFragment();
                    prizeEditDialogFragment.setTargetFragment(ArchiveFragment.this, REQUESTCODE);
                    prizeEditDialogFragment.show(getFragmentManager(), "PrizeEditDialogFragment");
                }
            });
        }
        
        if(mPrizeListView.getChildCount() > 0){
            mPrizeListView.removeAllViews();
        }
        for (int i=0; i<mPrize.length(); i++){
            try {
                JSONObject prize = mPrize.getJSONObject(i);
                View view = LayoutInflater.from(MyApplication.getContext()).inflate(R.layout.achieve_base_background, null);
                mPrizeListView.addView(view, i);
                TextView title = view.findViewById(R.id.title);
                TextView institution = view.findViewById(R.id.secondary_title);
                TextView subTitle2 = view.findViewById(R.id.last_title);
                subTitle2.setVisibility(View.GONE);
                TextView start = view.findViewById(R.id.start_time);
                TextView end = view.findViewById(R.id.end_time);
                end.setVisibility(View.GONE);
                TextView description = view.findViewById(R.id.description);
                
                title.setText(prize.optString("title"));
                institution.setText(prize.optString("institution"));
                start.setText(prize.optString("time"));
                description.setText(prize.optString("description"));


            }catch (JSONException e){
                e.printStackTrace();
            }

        }
    }
    
    private void setPaperView(){

        ConstraintLayout paperWrapper = mView.findViewById(R.id.paper_wrapper);
        paperWrapper.setVisibility(View.VISIBLE);

        TextView addPaper = mView.findViewById(R.id.add_new_paper);
        RelativeLayout addPaperWrapper = mView.findViewById(R.id.paper_add_wrapper);
        addPaperWrapper.setVisibility(View.GONE);
        addPaper.setVisibility(View.VISIBLE);
        addPaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PaperEditDialogFragment paperEditDialogFragment = new PaperEditDialogFragment();
                paperEditDialogFragment.setTargetFragment(ArchiveFragment.this, REQUESTCODE);
                paperEditDialogFragment.show(getFragmentManager(), "PaperEditDialogFragment");
            }
        });

        if(mPaperListView != null && mPaperListView.getChildCount() > 0){
            mPaperListView.removeAllViews();
        }
        
        for (int i=0; i<mPaper.length(); i++){
            try {
                JSONObject paper = mPaper.getJSONObject(i);
                View view = LayoutInflater.from(MyApplication.getContext()).inflate(R.layout.achieve_base_background, null);
                mPaperListView.addView(view, i);
                LinearLayout titleWrapper = view.findViewById(R.id.title_wrapper);
                TextView title = view.findViewById(R.id.title);
                final TextView website = view.findViewById(R.id.link);
                TextView subtitle1 = view.findViewById(R.id.secondary_title);
                subtitle1.setVisibility(View.GONE);
                TextView subTitle2 = view.findViewById(R.id.last_title);
                subTitle2.setVisibility(View.GONE);
                TextView start = view.findViewById(R.id.start_time);
                TextView end = view.findViewById(R.id.end_time);
                end.setVisibility(View.GONE);
                TextView description = view.findViewById(R.id.description);

                title.setText(paper.optString("title"));

                if(!TextUtils.isEmpty(paper.optString("website"))){
                    website.setVisibility(View.VISIBLE);
                    website.setTag(paper.optString("website"));
                    title.setTextColor(getResources().getColor(R.color.color_blue));

                    titleWrapper.setOnClickListener(new View.OnClickListener() {
                    @Override
                        public void onClick(View view) {
                            Uri uri;
                            String uriString = website.getTag().toString();
                            if(!uriString.startsWith("http") && !uriString.startsWith("https")){
                                uriString = "http://"+uriString;
                            }
                            uri = Uri.parse(uriString);
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        }
                    });
                }

                start.setText(paper.optString("time"));
                description.setText(paper.optString("description"));
                FontManager.markAsIconContainer(mView.findViewById(R.id.link), font);
                
                }catch (JSONException e){
                e.printStackTrace();
            }
        }

        FontManager.markAsIconContainer(mView.findViewById(R.id.paper_list), font);

    }

    private void setBlogView(){
    
    ConstraintLayout blogWrapper = mView.findViewById(R.id.blog_wrapper);
        blogWrapper.setVisibility(View.VISIBLE);

        TextView addBlog = mView.findViewById(R.id.add_new_blog);
        RelativeLayout addBlogWrapper = mView.findViewById(R.id.blog_add_wrapper);
        addBlogWrapper.setVisibility(View.GONE);
        if (authorUid == uid){
            addBlog.setVisibility(View.VISIBLE);
            addBlog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BlogEditDialogFragment blogEditDialogFragment = new BlogEditDialogFragment();
                    blogEditDialogFragment.setTargetFragment(ArchiveFragment.this, REQUESTCODE);
                    blogEditDialogFragment.show(getFragmentManager(), "BlogEditDialogFragment");
                }
            });
        }
        
        if(mBlogListView.getChildCount() > 0){
            mBlogListView.removeAllViews();
        }

        for (int i=0; i<mBlog.length(); i++){
            try {
                JSONObject blog = mBlog.getJSONObject(i);
                View view = LayoutInflater.from(MyApplication.getContext()).inflate(R.layout.achieve_base_background, null);
                mBlogListView.addView(view, i);
                LinearLayout titleWrapper = view.findViewById(R.id.title_wrapper);
                TextView title = view.findViewById(R.id.title);
                final TextView website = view.findViewById(R.id.link);
                TextView subTitle1 = view.findViewById(R.id.secondary_title);
                TextView subTitle2 = view.findViewById(R.id.last_title);
                
                subTitle1.setVisibility(View.GONE);
                subTitle2.setVisibility(View.GONE);
                TextView start = view.findViewById(R.id.start_time);
                start.setVisibility(View.GONE);
                TextView end = view.findViewById(R.id.end_time);
                end.setVisibility(View.GONE);
                TextView description = view.findViewById(R.id.description);

                if(!TextUtils.isEmpty(blog.optString("blog_website"))){
                    website.setVisibility(View.VISIBLE);
                    website.setTag(blog.optString("blog_website"));
                    title.setTextColor(getResources().getColor(R.color.color_blue));
                    
                    titleWrapper.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Uri uri;
                            String uriString = website.getTag().toString();
                            if(!uriString.startsWith("http") && !uriString.startsWith("https")){
                                uriString = "http://"+uriString;
                            }
                            uri = Uri.parse(uriString);
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        }
                    });
                }
                
                title.setText(blog.optString("title"));
                description.setText(blog.optString("description"));


            }catch (JSONException e){
                e.printStackTrace();
            }
        }

        FontManager.markAsIconContainer(mView.findViewById(R.id.blog_list), font);

    }
    
    private void setVolunteerView(){

        ConstraintLayout volunteerWrapper = mView.findViewById(R.id.volunteer_wrapper);
        volunteerWrapper.setVisibility(View.VISIBLE);

        TextView addVolunteer = mView.findViewById(R.id.add_new_volunteer);
        RelativeLayout addVolunteerWrapper = mView.findViewById(R.id.volunteer_add_wrapper);
        addVolunteerWrapper.setVisibility(View.GONE);
        
        if (authorUid == uid){
            addVolunteer.setVisibility(View.VISIBLE);
            addVolunteer.setOnClickListener(new View.OnClickListener() {
            @Override
                public void onClick(View view) {
                    VolunteerEditDialogFragment volunteerEditDialogFragment = new VolunteerEditDialogFragment();
                    volunteerEditDialogFragment.setTargetFragment(ArchiveFragment.this, REQUESTCODE);
                    volunteerEditDialogFragment.show(getFragmentManager(), "VolunteerEditDialogFragment");
                }
            });
        }

        if(mVolunteerListView.getChildCount() > 0){
            mVolunteerListView.removeAllViews();
        }
        
        for (int i=0; i<mVolunteer.length(); i++){
            try {
                JSONObject volunteer = mVolunteer.getJSONObject(i);
                View view = LayoutInflater.from(MyApplication.getContext()).inflate(R.layout.achieve_base_background, null);
                mVolunteerListView.addView(view, i);
                TextView institution = view.findViewById(R.id.title);
                TextView role = view.findViewById(R.id.secondary_title);
                TextView website = view.findViewById(R.id.last_title);
                TextView start = view.findViewById(R.id.start_time);
                TextView end = view.findViewById(R.id.end_time);
                TextView description = view.findViewById(R.id.description);
                
                institution.setText(volunteer.optString("institution"));
                role.setText(volunteer.optString("role"));
                website.setText(volunteer.optString("website"));
                start.setText(volunteer.optString("start"));
                end.setText("-  "+volunteer.optString("end"));
                description.setText(volunteer.optString("description"));

            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }
    
    private void setDynamicsCountView(int count){
        LinearLayout dynamicsCountWrapper = mView.findViewById(R.id.dynamics_count_wrapper);
        dynamicsCountWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyApplication.getContext(), SpecificUserDynamicsActivity.class);
                intent.putExtra("uid", userProfile.getUid());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });
        
        TextView dynamicsCount = mView.findViewById(R.id.dynamics_count);
        dynamicsCount.setText(String.valueOf(count));
    }

    private void setFollowedStatistics(final int count){
        LinearLayout followedCountWrapper = mView.findViewById(R.id.followed_count_wrap);
        TextView followedCount = mView.findViewById(R.id.followed_count);
        followedCountWrapper.setOnClickListener(new View.OnClickListener() {
        
        @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putInt("type", FOLLOWED);
                bundle.putInt("uid", userProfile.getUid());
                bundle.putString("title", "被关注 " + count);
                CommonUserListDialogFragment commonUserListDialogFragment = new CommonUserListDialogFragment();
                commonUserListDialogFragment.setArguments(bundle);
                commonUserListDialogFragment.show(getFragmentManager(), "CommonUserListDialogFragment");
            }
        });

        followedCount.setText(String.valueOf(count));
    }
    
    private void setPraisedStatistics(final int count){
        LinearLayout praisedCountWrapper = mView.findViewById(R.id.praised_count_wrap);
        TextView praisedCount = mView.findViewById(R.id.praised_count);
        praisedCount.setText(String.valueOf(count));

        praisedCountWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putInt("type", PRAISED);
                bundle.putInt("uid", userProfile.getUid());
                
                bundle.putString("title", "被赞 " + count);
                CommonUserListDialogFragment commonUserListDialogFragment = new CommonUserListDialogFragment();
                commonUserListDialogFragment.setArguments(bundle);
                commonUserListDialogFragment.show(getFragmentManager(), "CommonUserListDialogFragment");
            }
        });
    }

    private void processFollowAction() {
        final Button followBtn = mView.findViewById(R.id.follow);
        if (isFollowed == true) {
            followBtn.setText("已关注");
            followBtn.setBackground(MyApplication.getContext().getDrawable(R.drawable.btn_disable));
            followBtn.setTextColor(getResources().getColor(R.color.color_dark_grey));
        }
        
        followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
                String followUrl = "";
                if (isFollowed == true) {
                    isFollowed = false;
                    followUrl = FOLLOW_ACTION_URL + "cancel";
                    followBtn.setText("+关注");
                    followBtn.setTextColor(getResources().getColor(R.color.color_blue));
                    followBtn.setBackground(MyApplication.getContext().getDrawable(R.drawable.btn_default));
                } else {
                    isFollowed = true;
                    followUrl = FOLLOW_ACTION_URL + "add";
                    followBtn.setText("已关注");
                    followBtn.setBackground(MyApplication.getContext().getDrawable(R.drawable.btn_disable));
                    followBtn.setTextColor(getResources().getColor(R.color.color_dark_grey));
                }

                HttpUtil.sendOkHttpRequest(MyApplication.getContext(), followUrl, requestBody, new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.body() != null) {
                            String responseText = response.body().string();
                            getFollowStatisticsCount();
                            if (isDebug)
                                Slog.d(TAG, "==========get follow response text : " + responseText);
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                    }
                    });
            }
        });
    }

    private void getFollowStatisticsCount(){
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_FOLLOW_STATISTICS_URL, requestBody, new Callback() {
        @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if(isDebug)
                        Slog.d(TAG, "==========getFollow statistics : " + responseText);
                    try {
                        JSONObject followObject = new JSONObject(responseText);
                        int following_count = followObject.optInt("following_count");
                        int followed_count = followObject.optInt("followed_count");
                        Bundle bundle = new Bundle();
                        //bundle.putInt("following_count", following_count);
                        bundle.putInt("followed_count", followed_count);
                        Message msg = new Message();
                        msg.setData(bundle);
                        msg.what = GET_FOLLOW_STATISTICS_URL_DONE;
                        handler.sendMessage(msg);

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

    private void processContactsAction() {
        final Button contacts = mView.findViewById(R.id.contacts);
        final Button follow = mView.findViewById(R.id.follow);
        Button chatBtn = mView.findViewById(R.id.chat);
        
        if (contactStatus == APPLIED) {
            contacts.setEnabled(false);
            contacts.setText("已申请");
            follow.setVisibility(View.GONE);
        } else if (contactStatus == ESTABLISHED) {
            contacts.setVisibility(View.GONE);
            follow.setVisibility(View.GONE);

            chatBtn.setVisibility(View.VISIBLE);
            if (null == chat){
                chat = new Chat();
            }
            chatBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    chat.processChat(getActivity(), getYunXinAccount(getActivity()), userProfile.getInit());
                }
            });
        }
        
        contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
                HttpUtil.sendOkHttpRequest(MyApplication.getContext(), CONTACTS_ADD_URL, requestBody, new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                    if (response.body() != null) {
                            String responseText = response.body().string();
                            if(isDebug)
                                Slog.d(TAG, "==========processContactsAction : " + responseText);
                            try {
                                JSONObject status = new JSONObject(responseText);
                                boolean addContacts = status.optBoolean("add_contacts");
                                if(addContacts == true){
                                    getContactsStatus();
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

                contacts.setText("已申请");
                contacts.setEnabled(false);
            }
        });
    }

    @Override
    protected int getLayoutId() {   return 0; }
    
    public void handleMessage(Message message) {
        Bundle bundle = message.getData();
        switch (message.what){
            case GET_USER_PROFILE_DONE:
                setProfileView();
                break;
            case GET_CONTACTS_STATUS_DONE:
                processContactsAction();
                break;
            case GET_FOLLOW_DONE:
                processFollowAction();
                break;
            case GET_ACTIVITIES_COUNT_DONE:
                int dynamicsCount = bundle.getInt("count");
                setDynamicsCountView(dynamicsCount);
                break;
                case GET_FOLLOW_STATISTICS_URL_DONE:
                int followedCount = bundle.getInt("followed_count");
                setFollowedStatistics(followedCount);
                break;
            case GET_PRAISE_STATISTICS_URL_DONE:
                int praisedCount = bundle.getInt("praised_count");
                setPraisedStatistics(praisedCount);
                break;
            case GET_EDUCATION_BACKGROUND_DONE:
                setEducationBackgroundView();
                break;
            case GET_WORK_EXPERIENCE_DONE:
                setWorkExperienceView();
                break;
            case GET_PRIZE_DONE:
                setPrizeView();
                break;
                case GET_PAPER_DONE:
                setPaperView();
                break;
            case GET_BLOG_DONE:
                setBlogView();
                break;
            case GET_VOLUNTEER_DONE:
                setVolunteerView();
                break;
            default:
                break;
        }
    }

    static class MyHandler extends Handler {
        WeakReference<ArchiveFragment> archiveFragmentWeakReference;
        
        MyHandler(ArchiveFragment archiveFragment) {
            archiveFragmentWeakReference = new WeakReference<>(archiveFragment);
        }

        @Override
        public void handleMessage(Message message) {
            ArchiveFragment archiveFragment = archiveFragmentWeakReference.get();
            if (archiveFragment != null) {
                archiveFragment.handleMessage(message);
            }
        }
    }
}
