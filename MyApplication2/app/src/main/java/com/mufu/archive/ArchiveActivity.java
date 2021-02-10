package com.mufu.archive;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.textfield.TextInputEditText;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.mufu.R;
import com.mufu.common.BaseAppCompatActivity;
import com.mufu.common.SettingsActivity;
import com.mufu.meet.UserMeetInfo;
import com.mufu.util.CommonDialogFragmentInterface;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.ParseUtils;
import com.mufu.util.RoundImageView;
import com.mufu.common.SetAvatarActivity;
import com.mufu.util.SharedPreferencesUtils;
import com.mufu.util.Slog;
import com.mufu.util.UserProfile;
import com.nex3z.flowlayout.FlowLayout;

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
import static com.mufu.common.MyApplication.getContext;
import static com.mufu.main.MeetArchiveFragment.GET_LOGGEDIN_ACCOUNT;
import static com.mufu.main.MeetArchiveFragment.GET_MEET_ARCHIVE_DONE;
import static com.mufu.util.ParseUtils.startMeetArchiveActivity;

public class ArchiveActivity extends BaseAppCompatActivity implements CommonDialogFragmentInterface {
    private static final String TAG = "ArchiveActivity";
    private static final boolean isDebug = true;
    private Handler handler;
    UserProfile userProfile;
    JSONObject mSummary = null;
    JSONArray mEducationBackground = null;
    JSONArray mWorkExperience = null;
    JSONArray mPrize = null;
    JSONArray mPaper = null;
    JSONArray mBlog = null;
    JSONArray mVolunteer = null;

    private boolean isFollowed = false;

    private LinearLayout mEducationBackgroundListView = null;
    private LinearLayout mWorkExperienceListView = null;
    private LinearLayout mPrizeListView = null;
    private LinearLayout mPaperListView;
    private LinearLayout mBlogListView = null;
    private LinearLayout mVolunteerListView = null;
    private FlowLayout mAdditionnalInfoFL;
    private LinearLayout mEducationBaseInfo;
    private LinearLayout mWorkBaseInfo;
    RoundImageView headUri;
    int uid = 0;
    long authorUid = 0;
    Typeface font;
    private int contactStatus = -1;
    TextView mSettings;

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
    private UserMeetInfo mMeetMember;

    public static final String GET_USER_PROFILE_URL = HttpUtil.DOMAIN + "?q=account_manager/get_user_profile";
    public static final String SET_USER_PROFILE_URL = HttpUtil.DOMAIN + "?q=account_manager/set_user_profile";
    public static final String GET_EDUCATION_BACKGROUND_URL = HttpUtil.DOMAIN + "?q=personal_archive/education_background/load";
    public static final String GET_WORK_EXPERIENCE_URL = HttpUtil.DOMAIN + "?q=personal_archive/work_experience/load";
    public static final String GET_PRIZE_URL = HttpUtil.DOMAIN + "?q=personal_archive/prize/load";
    public static final String GET_PAPER_URL = HttpUtil.DOMAIN + "?q=personal_archive/paper/load";
    public static final String GET_BLOG_URL = HttpUtil.DOMAIN + "?q=personal_archive/blog/load";
    public static final String GET_VOLUNTEER_URL = HttpUtil.DOMAIN + "?q=personal_archive/volunteer/load";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.archive);

        handler = new MyHandler(this);
        authorUid = SharedPreferencesUtils.getSessionUid(getContext());

        Slog.d(TAG, "------------------authorUid: " + authorUid);

        mSettings = findViewById(R.id.settings);

        mEducationBackgroundListView = findViewById(R.id.education_background_list);
        mWorkExperienceListView = findViewById(R.id.work_experience_list);
        mPrizeListView = findViewById(R.id.prize_list);
        mPaperListView = findViewById(R.id.paper_list);
        mBlogListView = findViewById(R.id.blog_list);
        mVolunteerListView = findViewById(R.id.volunteer_list);
        mEducationBaseInfo = findViewById(R.id.education_info);
        mWorkBaseInfo = findViewById(R.id.work_info);

        if (getIntent() != null) {
            uid = getIntent().getIntExtra("uid", -1);
            loadArchiveData();
            getMeetArchive();
        } else {
            getCurrentUid();
        }

        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        TextView back = findViewById(R.id.left_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.archive), font);
    }

    private void getCurrentUid() {
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
                            getMeetArchive();
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
    
        public void getMeetArchive() {
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(ArchiveActivity.this, ParseUtils.GET_MEET_ARCHIVE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug)
                        Slog.d(TAG, "==========get archive response text : " + responseText);
                    if (responseText != null) {
                        if (!TextUtils.isEmpty(responseText)) {
                            try {

                                JSONObject jsonObject = new JSONObject(responseText).optJSONObject("archive");
                                if (jsonObject != null) {
                                    mMeetMember = ParseUtils.setMeetMemberInfo(jsonObject);
                                    handler.sendEmptyMessage(GET_MEET_ARCHIVE_DONE);
                                } else {
                                    startMeetArchiveActivity(getContext(), uid);
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

    private void loadArchiveData() {
        if (authorUid == uid) {
            mSettings.setVisibility(View.VISIBLE);
        } else {
            mSettings.setVisibility(View.GONE);
        }

        getUserProfile(uid);
        //getEducationBackground();
    }

    private void getUserProfile(int uid) {
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(getContext(), GET_USER_PROFILE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (responseText != null) {
                        if (!TextUtils.isEmpty(responseText)) {
                            try {
                                if (isDebug)
                                    Slog.d(TAG, "==============responseText: " + responseText);
                                JSONObject jsonObject = new JSONObject(responseText).optJSONObject("user");
                                userProfile = ParseUtils.getUserProfileFromJSONObject(jsonObject);
                                if (userProfile != null) {
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
    
    private void setAdditionalView(){
        TextView degree = findViewById(R.id.degree);
        TextView major = findViewById(R.id.major);
        TextView university = findViewById(R.id.university);
        TextView position = findViewById(R.id.position);
        TextView industry = findViewById(R.id.industry);
        
        if (mMeetMember.getSituation() != -1){
            if (mMeetMember.getSituation() == 1) {
                mWorkBaseInfo.setVisibility(View.VISIBLE);
                mEducationBaseInfo.setVisibility(View.GONE);
                position.setVisibility(View.VISIBLE);
                industry.setVisibility(View.VISIBLE);
                position.setText(mMeetMember.getPosition());
                industry.setText(mMeetMember.getIndustry());
            }else {
                major.setText(mMeetMember.getMajor());
                degree.setText(mMeetMember.getDegreeName(mMeetMember.getDegree()));
                university.setText(mMeetMember.getUniversity());
            }
        }else {
            mEducationBaseInfo.setVisibility(View.GONE);
        }
    }

    private void setProfileView() {

        TextView setAvatar = findViewById(R.id.set_avatar);
        TextView name = findViewById(R.id.name);
        TextView sex = findViewById(R.id.sex);
        TextView summary = findViewById(R.id.summary);
        headUri = findViewById(R.id.head_uri);
        final LinearLayout experienceList = findViewById(R.id.experience_list);

        //for introduction
        final TextView introduction = findViewById(R.id.introduction);
        final TextView addIntroduction = findViewById(R.id.add_introduction);
        final TextInputEditText introductionEdit = findViewById(R.id.introduction_edit);
        final TextView saveIntroduction = findViewById(R.id.save_introduction);
        //for education background
        TextView addEducation = findViewById(R.id.add_education);
        //for work experience
        TextView addWorkExperience = findViewById(R.id.add_work);
        final TextView experienceExtend = findViewById(R.id.experience_extend);
        final ScrollView scrollView = findViewById(R.id.archive_scroll_view);
        LinearLayout experienceWrapper = findViewById(R.id.experience_wrap);
        TextView extend = findViewById(R.id.experience_extend);

        if (userProfile.getAuthorSelf() == true) {
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
            if (getContext() != null) {
                Glide.with(getContext()).load(picture_url).into(headUri);
            }
        } else {
            if (userProfile.getSex() == 0) {
                headUri.setImageDrawable(getContext().getDrawable(R.drawable.male_default_avator));
            } else {
                headUri.setImageDrawable(getContext().getDrawable(R.drawable.female_default_avator));
            }
        }

        name.setText(userProfile.getNickName());

        if (userProfile.getSex() == 0) {
            sex.setText(R.string.mars);
        } else {
            sex.setText(R.string.venus);
        }

        if (!"".equals(userProfile.getSummary()) && !"".equals(userProfile.getSummary())) {
            summary.setText(userProfile.getSummary());
        }

        if (!TextUtils.isEmpty(userProfile.getIntroduction()) && !"null".equals(userProfile.getIntroduction())) {
            introduction.setVisibility(View.VISIBLE);
            introduction.setText(userProfile.getIntroduction());

            if (userProfile.getAuthorSelf() == true) {
                addIntroduction.setText(getString(R.string.edit_introduction));
            }
        }

        final LinearLayout introductionEditWrapper = findViewById(R.id.introduction_edit_wrapper);
        addIntroduction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (introductionEditWrapper.getVisibility() == View.GONE) {
                    introductionEditWrapper.setVisibility(View.VISIBLE);
                }
                if (!"".equals(introduction.getText())) {
                    introductionEdit.setText(introduction.getText());
                }
            }
        });


        saveIntroduction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(introductionEdit.getText())) {
                    saveIntroduction(introductionEdit.getText().toString(), introduction);
                    addIntroduction.setText(getString(R.string.edit_introduction));
                }
            }
        });
        experienceWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (experienceList.getVisibility() == View.VISIBLE) {
                    experienceList.setVisibility(View.GONE);
                    experienceExtend.setText(getString(R.string.fa_chevron_down));
                } else {

                    experienceList.setVisibility(View.VISIBLE);
                    experienceExtend.setText(getString(R.string.fa_chevron_up));

                    ConstraintLayout constraintLayout = findViewById(R.id.activity_archive_wrapper);
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

    private void getBackground(){
        getEducationBackground();
        getWorkExperience();
        getPrize();
        getPaper();
        getBlog();
        getVolunteer();
    }


    private void moreQualificationAction() {

        RelativeLayout prizeWrapper = findViewById(R.id.prize_add_wrapper);
        RelativeLayout paperWrapper = findViewById(R.id.paper_add_wrapper);
        RelativeLayout blogWrapper = findViewById(R.id.blog_add_wrapper);
        RelativeLayout volunteerWrapper = findViewById(R.id.volunteer_add_wrapper);


        prizeWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PrizeEditDialogFragment prizeEditDialogFragment = new PrizeEditDialogFragment();
                //prizeEditDialogFragment.setTargetFragment(ArchiveFragment.this, REQUESTCODE);
                prizeEditDialogFragment.show(getSupportFragmentManager(), "PrizeEditDialogFragment");
            }
        });

        paperWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PaperEditDialogFragment paperEditDialogFragment = new PaperEditDialogFragment();
                //paperEditDialogFragment.setTargetFragment(ArchiveFragment.this, REQUESTCODE);
                paperEditDialogFragment.show(getSupportFragmentManager(), "PaperEditDialogFragment");
            }
        });

        blogWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BlogEditDialogFragment blogEditDialogFragment = new BlogEditDialogFragment();
                //blogEditDialogFragment.setTargetFragment(ArchiveFragment.this, REQUESTCODE);
                blogEditDialogFragment.show(getSupportFragmentManager(), "BlogEditDialogFragment");
            }
        });

        volunteerWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VolunteerEditDialogFragment volunteerEditDialogFragment = new VolunteerEditDialogFragment();
                //volunteerEditDialogFragment.setTargetFragment(ArchiveFragment.this, REQUESTCODE);
                volunteerEditDialogFragment.show(getSupportFragmentManager(), "VolunteerEditDialogFragment");
            }
        });

    }

    private void saveIntroduction(String introduction, TextView introductionView) {
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(userProfile.getUid()))
                .add("introduction", introduction).build();

        if (introductionView.getVisibility() == View.GONE) {
            introductionView.setVisibility(View.VISIBLE);
        }
        introductionView.setText(introduction);
        LinearLayout introductionEditWrapper = findViewById(R.id.introduction_edit_wrapper);
        if (introductionEditWrapper.getVisibility() == View.VISIBLE) {
            introductionEditWrapper.setVisibility(View.GONE);
        }

        HttpUtil.sendOkHttpRequest(getContext(), SET_USER_PROFILE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }

    private void addEducationBackground(TextView addEducation) {
        addEducation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EducationEditDialogFragment educationEditDialogFragment = new EducationEditDialogFragment();
                //educationEditDialogFragment.setTargetFragment(ArchiveFragment.this, REQUESTCODE);
                educationEditDialogFragment.show(getSupportFragmentManager(), "EducationEditDialogFragment");
            }
        });
    }

    private void addWorkExperience(TextView addWorkExperience) {
        addWorkExperience.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), WorkEditActivity.class);
                startActivityForResult(intent, REQUESTCODE);
            }
        });
    }

    public void getEducationBackground() {
        Slog.d(TAG, "-------------------->getEducationBackground");
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_EDUCATION_BACKGROUND_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug)
                    Slog.d(TAG, "================getEducationBackground response:" + responseText);
                if (responseText != null && !TextUtils.isEmpty(responseText)) {
                    try {
                        mEducationBackground = new JSONObject(responseText).optJSONArray("education");
                        if (isDebug)
                            Slog.d(TAG, "================getEducationBackground education:" + mEducationBackground);
                        if (mEducationBackground != null) {
                            handler.sendEmptyMessage(GET_EDUCATION_BACKGROUND_DONE);
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

    private void setEducationBackgroundView() {
        if (mEducationBackgroundListView.getChildCount() > 0) {
            mEducationBackgroundListView.removeAllViews();
        }
        for (int i = 0; i < mEducationBackground.length(); i++) {
            try {
                JSONObject education = mEducationBackground.getJSONObject(i);
                View view = LayoutInflater.from(getContext()).inflate(R.layout.achieve_base_background, null);
                mEducationBackgroundListView.addView(view, i);
                TextView university = view.findViewById(R.id.title);
                TextView degree = view.findViewById(R.id.secondary_title);
                TextView major = view.findViewById(R.id.last_title);
                major.setVisibility(View.VISIBLE);

                TextView start = view.findViewById(R.id.start_time);
                TextView end = view.findViewById(R.id.end_time);

                university.setText(education.optString("university"));
                degree.setText(education.optString("degree"));
                major.setText(", " + education.optString("major"));

                start.setText(education.optString("entrance_year") + "年" + education.optString("entrance_month"));
                end.setText("~  " + education.optString("graduate_year") + "年" + education.optString("graduate_month"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public void getWorkExperience() {

        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid)).build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_WORK_EXPERIENCE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug)
                    Slog.d(TAG, "================getWorkExperience response:" + responseText);
                if (responseText != null && !TextUtils.isEmpty(responseText)) {
                    try {
                        mWorkExperience = new JSONObject(responseText).optJSONArray("work");
                        if (isDebug)
                            Slog.d(TAG, "================getWorkExperience work:" + mWorkExperience);
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

    public void getPrize() {

        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(getContext(), GET_PRIZE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug) Slog.d(TAG, "================getPrize response:" + responseText);
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

    public void getPaper() {

        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(getContext(), GET_PAPER_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug) Slog.d(TAG, "================getPaper response:" + responseText);
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

    public void getBlog() {

        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(getContext(), GET_BLOG_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug) Slog.d(TAG, "================getPaper response:" + responseText);
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

    public void getVolunteer() {

        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(getContext(), GET_VOLUNTEER_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug) Slog.d(TAG, "================getVolunteer response:" + responseText);
                if (responseText != null && !TextUtils.isEmpty(responseText)) {
                    try {
                        mVolunteer = new JSONObject(responseText).optJSONArray("volunteers");
                        if (isDebug)
                            Slog.d(TAG, "================getVolunteer volunteers:" + mVolunteer);
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

    private void setWorkExperienceView() {
        if (mWorkExperienceListView.getChildCount() > 0) {
            mWorkExperienceListView.removeAllViews();
        }

        for (int i = 0; i < mWorkExperience.length(); i++) {
            try {
                JSONObject workExperience = mWorkExperience.getJSONObject(i);
                View view = LayoutInflater.from(getContext()).inflate(R.layout.achieve_base_background, null);
                mWorkExperienceListView.addView(view, i);
                TextView jobTitle = view.findViewById(R.id.title);
                TextView company = view.findViewById(R.id.secondary_title);
                TextView industry = view.findViewById(R.id.last_title);
                TextView start = view.findViewById(R.id.start_time);
                TextView end = view.findViewById(R.id.end_time);

                jobTitle.setText(workExperience.optString("position"));
                company.setText(workExperience.optString("company"));
                industry.setText(", " + workExperience.optString("industry"));

                start.setText(workExperience.optInt("entrance_year") + "年");
                if (workExperience.optInt("now") == 1 && workExperience.optInt("leave_year") == 0) {
                    end.setText("~  " + "至今");
                } else {
                    end.setText("~  " + workExperience.optString("leave_year") + "年");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void setPrizeView() {

        ConstraintLayout prizeWrapper = findViewById(R.id.prize_wrapper);
        prizeWrapper.setVisibility(View.VISIBLE);

        TextView addPrize = findViewById(R.id.add_new_prize);
        RelativeLayout addPrizeWrapper = findViewById(R.id.prize_add_wrapper);
        addPrizeWrapper.setVisibility(View.GONE);
        if (authorUid == uid) {
            addPrize.setVisibility(View.VISIBLE);
            addPrize.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PrizeEditDialogFragment prizeEditDialogFragment = new PrizeEditDialogFragment();
                    //prizeEditDialogFragment.setTargetFragment(ArchiveFragment.this, REQUESTCODE);
                    prizeEditDialogFragment.show(getSupportFragmentManager(), "PrizeEditDialogFragment");
                }
            });
        }

        if (mPrizeListView.getChildCount() > 0) {
            mPrizeListView.removeAllViews();
        }
        for (int i = 0; i < mPrize.length(); i++) {
            try {
                JSONObject prize = mPrize.getJSONObject(i);
                View view = LayoutInflater.from(getContext()).inflate(R.layout.achieve_base_background, null);
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


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void setPaperView() {

        ConstraintLayout paperWrapper = findViewById(R.id.paper_wrapper);
        paperWrapper.setVisibility(View.VISIBLE);

        TextView addPaper = findViewById(R.id.add_new_paper);
        RelativeLayout addPaperWrapper = findViewById(R.id.paper_add_wrapper);
        addPaperWrapper.setVisibility(View.GONE);
        addPaper.setVisibility(View.VISIBLE);
        addPaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PaperEditDialogFragment paperEditDialogFragment = new PaperEditDialogFragment();
                //paperEditDialogFragment.setTargetFragment(ArchiveFragment.this, REQUESTCODE);
                paperEditDialogFragment.show(getSupportFragmentManager(), "PaperEditDialogFragment");
            }
        });

        if (mPaperListView != null && mPaperListView.getChildCount() > 0) {
            mPaperListView.removeAllViews();
        }

        for (int i = 0; i < mPaper.length(); i++) {
            try {
                JSONObject paper = mPaper.getJSONObject(i);
                View view = LayoutInflater.from(getContext()).inflate(R.layout.achieve_base_background, null);
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

                if (!TextUtils.isEmpty(paper.optString("website"))) {
                    website.setVisibility(View.VISIBLE);
                    website.setTag(paper.optString("website"));
                    title.setTextColor(getResources().getColor(R.color.color_blue));

                    titleWrapper.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Uri uri;
                            String uriString = website.getTag().toString();
                            if (!uriString.startsWith("http") && !uriString.startsWith("https")) {
                                uriString = "http://" + uriString;
                            }
                            uri = Uri.parse(uriString);
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        }
                    });
                }

                start.setText(paper.optString("time"));
                description.setText(paper.optString("description"));
                FontManager.markAsIconContainer(findViewById(R.id.link), font);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        FontManager.markAsIconContainer(findViewById(R.id.paper_list), font);

    }

    private void setBlogView() {

        ConstraintLayout blogWrapper = findViewById(R.id.blog_wrapper);
        blogWrapper.setVisibility(View.VISIBLE);

        TextView addBlog = findViewById(R.id.add_new_blog);
        RelativeLayout addBlogWrapper = findViewById(R.id.blog_add_wrapper);
        addBlogWrapper.setVisibility(View.GONE);
        if (authorUid == uid) {
            addBlog.setVisibility(View.VISIBLE);
            addBlog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BlogEditDialogFragment blogEditDialogFragment = new BlogEditDialogFragment();
                    //blogEditDialogFragment.setTargetFragment(ArchiveActivity.this, REQUESTCODE);
                    blogEditDialogFragment.show(getSupportFragmentManager(), "BlogEditDialogFragment");
                }
            });
        }

        if (mBlogListView.getChildCount() > 0) {
            mBlogListView.removeAllViews();
        }

        for (int i = 0; i < mBlog.length(); i++) {
            try {
                JSONObject blog = mBlog.getJSONObject(i);
                View view = LayoutInflater.from(getContext()).inflate(R.layout.achieve_base_background, null);
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

                if (!TextUtils.isEmpty(blog.optString("blog_website"))) {
                    website.setVisibility(View.VISIBLE);
                    website.setTag(blog.optString("blog_website"));
                    title.setTextColor(getResources().getColor(R.color.color_blue));

                    titleWrapper.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Uri uri;
                            String uriString = website.getTag().toString();
                            if (!uriString.startsWith("http") && !uriString.startsWith("https")) {
                                uriString = "http://" + uriString;
                            }
                            uri = Uri.parse(uriString);
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        }
                    });
                }

                title.setText(blog.optString("title"));
                description.setText(blog.optString("description"));


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        FontManager.markAsIconContainer(findViewById(R.id.blog_list), font);

    }

    private void setVolunteerView() {

        ConstraintLayout volunteerWrapper = findViewById(R.id.volunteer_wrapper);
        volunteerWrapper.setVisibility(View.VISIBLE);

        TextView addVolunteer = findViewById(R.id.add_new_volunteer);
        RelativeLayout addVolunteerWrapper = findViewById(R.id.volunteer_add_wrapper);
        addVolunteerWrapper.setVisibility(View.GONE);

        if (authorUid == uid) {
            addVolunteer.setVisibility(View.VISIBLE);
            addVolunteer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    VolunteerEditDialogFragment volunteerEditDialogFragment = new VolunteerEditDialogFragment();
                    //volunteerEditDialogFragment.setTargetFragment(ArchiveActivity, REQUESTCODE);
                    volunteerEditDialogFragment.show(getSupportFragmentManager(), "VolunteerEditDialogFragment");
                }
            });
        }

        if (mVolunteerListView.getChildCount() > 0) {
            mVolunteerListView.removeAllViews();
        }

        for (int i = 0; i < mVolunteer.length(); i++) {
            try {
                JSONObject volunteer = mVolunteer.getJSONObject(i);
                View view = LayoutInflater.from(getContext()).inflate(R.layout.achieve_base_background, null);
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
                end.setText("-  " + volunteer.optString("end"));
                description.setText(volunteer.optString("description"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleMessage(Message message) {
        Bundle bundle = message.getData();
        switch (message.what) {
            case GET_USER_PROFILE_DONE:
                setProfileView();
                getBackground();
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
            case GET_MEET_ARCHIVE_DONE:
                setAdditionalView();
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (isDebug)
            Slog.d(TAG, "--------------->onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode);
        if (requestCode == REQUESTCODE) {
            switch (resultCode) {
                case SET_AVATAR_RESULT_OK:
                    String avatar = data.getStringExtra("avatar");
                    Glide.with(this).load(HttpUtil.DOMAIN + avatar).into(headUri);
                    break;
                case SET_WORK_RESULT_OK:
                    getWorkExperience();
                    break;
                default:
                    break;
            }
        }
    }

    static class MyHandler extends Handler {
        WeakReference<ArchiveActivity> archiveActivityWeakReference;
        MyHandler(ArchiveActivity archiveActivity) {
            archiveActivityWeakReference = new WeakReference<>(archiveActivity);
        }

        @Override
        public void handleMessage(Message message) {
            ArchiveActivity archiveActivity = archiveActivityWeakReference.get();
            if (archiveActivity != null) {
                archiveActivity.handleMessage(message);
            }
        }
    }

    @Override
    public void onBackFromDialog(int type, int result, boolean status) {
        if (status){
            switch (type){
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
            }
        }
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    private void exit() {
        finish();
    }
}
