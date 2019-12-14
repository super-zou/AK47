package com.hetang.meet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.hetang.R;
import com.hetang.adapter.CheeringGroupAdapter;
import com.hetang.adapter.MeetImpressionStatisticsAdapter;
import com.hetang.adapter.MeetReferenceAdapter;
import com.hetang.archive.ArchiveActivity;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.common.Chat;
import com.hetang.common.Dynamic;
import com.hetang.common.HandlerTemp;
import com.hetang.common.MyApplication;
import com.hetang.main.ArchiveFragment;
import com.hetang.util.CommonDialogFragmentInterface;
import com.hetang.util.CommonUserListDialogFragment;
import com.hetang.util.FontManager;
import com.hetang.util.HtGridView;
import com.hetang.util.HttpUtil;
import com.hetang.util.InvitationDialogFragment;
import com.hetang.util.ParseUtils;
import com.hetang.util.PersonalityEditDialogFragment;
import com.hetang.util.ReferenceWriteDialogFragment;
import com.hetang.util.SharedPreferencesUtils;
import com.hetang.util.Slog;
import com.hetang.util.UserProfile;
import com.hetang.util.Utility;
import com.nex3z.flowlayout.FlowLayout;
import com.willy.ratingbar.BaseRatingBar;
import com.willy.ratingbar.ScaleRatingBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hetang.util.SharedPreferencesUtils.getYunXinAccount;

public class MeetArchiveActivity extends BaseAppCompatActivity implements CommonDialogFragmentInterface, ViewSwitcher.ViewFactory, View.OnTouchListener {
    private static final String TAG = "MeetArchiveActivity";
    private static final boolean isDebug = true;
    private static final String GET_ACTIVITIES_COUNT_BY_UID = HttpUtil.DOMAIN + "?q=dynamic/get_count_by_uid";
    private static final String COMMENT_URL = HttpUtil.DOMAIN + "?q=dynamic/interact/get";
    private static final String LOAD_REFERENCE_URL = HttpUtil.DOMAIN + "?q=meet/reference/load";
    private static final String GET_RATING_URL = HttpUtil.DOMAIN + "?q=meet/rating/get";
    private static final String GET_IMPRESSION_STATISTICS_URL = HttpUtil.DOMAIN + "?q=meet/impression/statistics";
    private static final String GET_IMPRESSION_USERS_URL = HttpUtil.DOMAIN + "?q=meet/impression/users";
    private static final String GET_PERSONALITY_URL = HttpUtil.DOMAIN + "?q=meet/personality/get";
    private static final String GET_CHEERING_GROUP_URL = HttpUtil.DOMAIN + "?q=meet/cheering_group/get";
    private static final String LOAD_HOBBY_URL = HttpUtil.DOMAIN + "?q=personal_archive/hobby/load";
    public static final String CONTACTS_ADD_URL = HttpUtil.DOMAIN + "?q=contacts/add_contacts";
    public static final String GET_CONTACTS_STATUS_URL = HttpUtil.DOMAIN + "?q=contacts/status";
    public static final String FOLLOW_ACTION_URL = HttpUtil.DOMAIN + "?q=follow/action/";
    private static final String GET_FOLLOW_STATISTICS_URL = HttpUtil.DOMAIN + "?q=follow/statistics";
    public static final String GET_FOLLOW_STATUS_URL = HttpUtil.DOMAIN + "?q=follow/isFollowed";
    private static final String GET_LOVE_STATISTICS_URL = HttpUtil.DOMAIN + "?q=meet/love/statistics";
    public static final String GET_PRAISE_STATISTICS_URL = HttpUtil.DOMAIN + "?q=meet/praise/statistics";

    private static final String LOVE_ADD_URL = HttpUtil.DOMAIN + "?q=meet/love/add";
    private static final String PRAISE_ADD_URL = HttpUtil.DOMAIN + "?q=meet/praise/add";
    private static final String GET_PROFILE_PICTURES_URL = HttpUtil.DOMAIN + "?q=meet/get_pictures_url";
    private static final String ADD_VISIT_RECORD_URL = HttpUtil.DOMAIN + "?q=visitor_record/add_visit_record";
    private static final String GET_VISIT_RECORD_URL = HttpUtil.DOMAIN + "?q=visitor_record/get_visit_record";
    public static final String GET_LOGGEDIN_ACCOUNT = HttpUtil.DOMAIN + "?q=account_manager/get_loggedin_account";
    public static final String CHECK_YUNXIN_USER = HttpUtil.DOMAIN + "?q=chat/check_yunxin_user";
    private static final String JOIN_CHEERING_GROUP_URL = HttpUtil.DOMAIN + "?q=meet/cheering_group/join";

    private static final int DONE = 1;
    private static final int UPDATE = 2;
    private static final int UPDATE_COMMENT = 3;
    private static final int LOAD_RATING_DONE = 4;
    private static final int LOAD_IMPRESSION_DONE = 5;
    private static final int LOAD_REFERENCE_DONE = 6;
    private static final int LOAD_PERSONALITY_DONE = 7;
    private static final int LOAD_HOBBY_DONE = 8;
    public static final int GET_FOLLOW_DONE = 9;
    public static final int GET_CONTACTS_STATUS_DONE = 10;
    private static final int GET_FOLLOW_STATISTICS_URL_DONE = 11;
    public static final int GET_PRAISE_STATISTICS_URL_DONE = 12;
    private static final int GET_LOVE_STATISTICS_URL_DONE = 13;

    private static final int UPDATE_LOVED_COUNT = 14;
    private static final int UPDATE_PRAISED_COUNT = 15;
    private static final int GET_CHEERING_GROUP_DONE = 16;
    private static final int GET_PICTURES_URL_DONE = 17;
    private static final int GET_VISIT_RECORD_DONE = 18;
    private static final int GET_LOGGEDIN_UID_DONE = 19;
    private static final int JOIN_CHEERING_GROUP_DONE = 20;
    private static final int GET_ACTIVITIES_COUNT_DONE = 21;
    private static final int GET_MEET_ARCHIVE_DONE = 22;
    public static final int FOLLOWED = 1;
    private static final int FOLLOWING = 2;
    public static final int PRAISED = 3;
    private static final int PRAISE = 4;
    private static final int LOVED = 5;
    private static final int LOVE = 6;
    private static final int PAGE_SIZE = 6;
    public static final int APPLIED = 0;
    public static final int ESTABLISHED = 1;

    public List<UserProfile> mCheeringGroupMemberList = new ArrayList<>();
    View mHeaderEvaluation;
    private List<Dynamic> mMeetList = new ArrayList<>();
    private List<ImpressionStatistics> mImpressionStatisticsList = new ArrayList<>();
    private List<MeetReferenceInfo> mReferenceList = new ArrayList<>();
    private Handler handler;
    private boolean isLoved = false;
    private boolean isPraised = false;
    private boolean isFollowed = false;
    private int contactStatus = -1;
    private View mArchiveProfile;

    private MeetReferenceAdapter mMeetReferenceAdapter;
    private MeetImpressionStatisticsAdapter mMeetImpressionStatisticsAdapter;
    private CheeringGroupAdapter mCheeringGroupAdapter;
    private JSONObject mRatingObj;
    private JSONArray pictureArray = new JSONArray();
    private EvaluateDialogFragment evaluateDialogFragment;
    private MeetDynamicsFragment meetDynamicsFragment;

    private TextView backLeft;
    private UserMeetInfo mMeetMember;
    private JSONArray personalityResponseArray;

    private TextView praisedIcon;
    private TextView praisedCount;
    private TextView lovedStatistics;
    private TextView lovedCount;
    private TextView lovedIcon;
    private TextView evaluatorDetails;
    private RelativeLayout ratingBarWrapper;
    private ScaleRatingBar scaleRatingBar;
    private ConstraintLayout dynamicsCount;

    private final static int REQUESTCODE = 1;//for impression approve detail
    private final static int RESULT_OK = 2;
    private ImageSwitcher mImageSwitcher;
    private float downX;
    private float downY;
    private float lastX;
    private float lastY;
    int mWidth = 0;
    private int currentPosition = 0;
    int direction = 0;//default for left, 1 for right
    private static int uid = -1;
    private static int authorUid = -1;
    private boolean isSelf = false;

    int mTempSize = 0;
    Chat chat;

    private LinearLayout navLayout;
    private List<Drawable> drawableList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meet_archive);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        handler = new MeetArchiveActivity.MyHandler(this);
        mMeetMember = (UserMeetInfo) getIntent().getSerializableExtra("meet");
        //initView();

        setView();

        getLoggedinAccount();

    }

    private void setView() {
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.custom_actionbar), font);
        backLeft = findViewById(R.id.left_back);
        backLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });

        ScrollView meetArchiveWrapper = findViewById(R.id.meet_archive_wrapper);
        mArchiveProfile = findViewById(R.id.meet_archive_profile);
        FontManager.markAsIconContainer(mArchiveProfile.findViewById(R.id.meet_archive_profile), font);
        mHeaderEvaluation = findViewById(R.id.friends_relatives_reference);
        FontManager.markAsIconContainer(mHeaderEvaluation.findViewById(R.id.friends_relatives_reference), font);
    }

    private void getLoggedinAccount() {

        authorUid = SharedPreferencesUtils.getSessionUid(MyApplication.getContext());

        if (authorUid > 0) {
            if (mMeetMember == null) {
                uid = getIntent().getIntExtra("uid", -1);
                getMeetArchive(this, uid);
            } else {
                uid = mMeetMember.getUid();
                processSubModules();
            }
        } else {
            getLoggedinAccountFromServer();
        }

        Slog.d(TAG, "------------>authorUid: " + authorUid + "    uid: " + uid);
    }

    public void getLoggedinAccountFromServer() {
        RequestBody requestBody = new FormBody.Builder().build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_LOGGEDIN_ACCOUNT, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug)
                        Slog.d(TAG, "==========getLoggedinAccountFromServer : " + responseText);
                    try {
                        //String responseText = response.body().string();
                        if (responseText != null && !TextUtils.isEmpty(responseText)) {
                            JSONObject account = new JSONObject(responseText);
                            authorUid = account.optInt("uid");
                            handler.sendEmptyMessage(GET_LOGGEDIN_UID_DONE);
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

    @Override
    public void onBackPressed() {
        exit();
    }

    private void exit() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("Archive");

        if (fragment != null && fragment.isVisible()) {
            getSupportFragmentManager().popBackStack();
        } else {
            finish();
        }
    }

    private void processSubModules() {

        if (authorUid == uid) {
            isSelf = true;
        }
        if (isDebug) Slog.d(TAG, "==========isSelf : " + isSelf);

        setArchiveProfile();

        loadRating();

        loadImpressionStatistics();

        processReferences();

        processPersonality();

        processHobby();

        processCheeringGroup();

        processVisitRecord();
    }

    private void setArchiveProfile() {

        loadProfilePictures();
        setMeetProfile();
        getDynamicsCount();
        getConnectStatus();
        getFollowStatistics();
        getPraiseStatistics();
        getLoveStatistics();

    }

    private void loadProfilePictures() {
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(MeetArchiveActivity.this, GET_PROFILE_PICTURES_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========loadProfilePictures : " + responseText);
                    try {
                        pictureArray = new JSONObject(responseText).optJSONArray("pictures");
                        handler.sendEmptyMessage(GET_PICTURES_URL_DONE);
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

    private void setPictureIndicator(int pos, int direction) {
        TextView preIndicator = null;
        TextView nextIndicator = null;
        if (pos >= 1) {
            preIndicator = (TextView) navLayout.getChildAt(pos - 1);
        }

        if (pos <= pictureArray.length()) {
            nextIndicator = (TextView) navLayout.getChildAt(pos + 1);
        }

        TextView curIndicator = (TextView) navLayout.getChildAt(pos);
        if (curIndicator != null) {
            curIndicator.setText(R.string.circle);
        }
        if (direction == 0) {//touch move left
            preIndicator.setText(R.string.circle_o);
        } else {//touch move right
            nextIndicator.setText(R.string.circle_o);
        }
    }

    private void setMeetProfile() {
        TextView name = mArchiveProfile.findViewById(R.id.name);
        TextView sex = mArchiveProfile.findViewById(R.id.sex);

        mImageSwitcher = mArchiveProfile.findViewById(R.id.image_switcher);
        mImageSwitcher.requestFocus();
        mImageSwitcher.setFactory(this);
        mImageSwitcher.setOnTouchListener(this);

        navLayout = mArchiveProfile.findViewById(R.id.page_indicator);
        FlowLayout baseProfile = mArchiveProfile.findViewById(R.id.meet_profile);
        LinearLayout interactWrapper = mArchiveProfile.findViewById(R.id.interaction_wrap);
        if (isSelf) {
            interactWrapper.setVisibility(View.GONE);
        }

        LinearLayout livingWrap = mArchiveProfile.findViewById(R.id.living_wrap);
        LinearLayout illustrationWrap = mArchiveProfile.findViewById(R.id.illustration_wrap);
        TextView profileDetail = mArchiveProfile.findViewById(R.id.profile_detail);

        TextView age = baseProfile.findViewById(R.id.age);
        TextView height = baseProfile.findViewById(R.id.height);
        TextView constellation = baseProfile.findViewById(R.id.constellation);
        TextView hometown = baseProfile.findViewById(R.id.hometown);
        TextView nation = baseProfile.findViewById(R.id.nation);
        TextView religion = baseProfile.findViewById(R.id.religion);
        TextView living = livingWrap.findViewById(R.id.living);

        TextView degree = baseProfile.findViewById(R.id.degree);
        TextView major = baseProfile.findViewById(R.id.major);
        TextView university = baseProfile.findViewById(R.id.university);
        TextView position = baseProfile.findViewById(R.id.position);
        TextView industry = baseProfile.findViewById(R.id.industry);

        TextView ageRequirement = mArchiveProfile.findViewById(R.id.age_require);
        TextView heightRequirement = mArchiveProfile.findViewById(R.id.height_require);
        TextView degreeRequirement = mArchiveProfile.findViewById(R.id.degree_require);
        TextView livesRequirement = mArchiveProfile.findViewById(R.id.lives_require);
        TextView sexRequirement = mArchiveProfile.findViewById(R.id.sex_require);

        TextView illustration = mArchiveProfile.findViewById(R.id.illustration);
        TextView eyeView = mArchiveProfile.findViewById(R.id.visit_record);

        name.setText(mMeetMember.getName());

        if (!"".equals(mMeetMember.getAvatar())) {
            // Glide.with(this).load(HttpUtil.DOMAIN + mMeetMember.userProfile.getAvatar()).into(headUri);
            String url = HttpUtil.DOMAIN + mMeetMember.getAvatar();
            SimpleTarget<Drawable> simpleTarget = new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                    mImageSwitcher.setImageDrawable(resource);
                    drawableList.add(resource);
                }

            };

            Glide.with(getApplicationContext()).load(url).into(simpleTarget);

        } else {
            // headUri.setImageDrawable(getDrawable(R.mipmap.ic_launcher));
        }

        age.setText(String.valueOf(mMeetMember.getAge()) + "岁");
        height.setText(String.valueOf(mMeetMember.getHeight()) + "cm");
        constellation.setText(mMeetMember.getConstellation());
        hometown.setText(getResources().getString(R.string.hometown) + ":" + mMeetMember.getHometown());
        nation.setText(mMeetMember.getNation());

        if (!"无".equals(mMeetMember.getReligion())) {
            religion.setText(mMeetMember.getReligion());
        } else {
            religion.setVisibility(View.GONE);
        }

        if (mMeetMember.getSex() == 0) {
            sex.setText(R.string.mars);
        } else {
            sex.setText(R.string.venus);
        }
        //sex.setText(String.valueOf(mMeetMember.userProfile.getSex()));
        living.setText("现居" + mMeetMember.getLiving());

        degree.setText(mMeetMember.getDegreeName(mMeetMember.getDegree()));
        university.setText(mMeetMember.getUniversity());
        if (mMeetMember.getSituation() == 0) {
            major.setVisibility(View.VISIBLE);
            major.setText(mMeetMember.getMajor());
        } else {
            position.setVisibility(View.VISIBLE);
            industry.setVisibility(View.VISIBLE);
            position.setText(mMeetMember.getPosition());
            industry.setText(mMeetMember.getIndustry());
        }

        ageRequirement.setText(mMeetMember.getAgeLower() + "~" + mMeetMember.getAgeUpper() + "岁");
        heightRequirement.setText(String.valueOf(mMeetMember.getRequirementHeight()) + "cm");
        degreeRequirement.setText(mMeetMember.getDegreeName(mMeetMember.getRequirementDegree()) + "学历");
        livesRequirement.setText(mMeetMember.getRequirementLiving());
        sexRequirement.setText(mMeetMember.getRequirementSex());

        if (mMeetMember.getIllustration() != null && !TextUtils.isEmpty(mMeetMember.getIllustration())) {
            illustrationWrap.setVisibility(View.VISIBLE);
            illustration.setText("“" + mMeetMember.getIllustration() + "”");
        }
        if (mMeetMember.getVisitCount() > 0) {
            eyeView.setText(String.valueOf(mMeetMember.getVisitCount()));
        }

        profileDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArchiveFragment archiveFragment = new ArchiveFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("uid", uid);
                archiveFragment.setArguments(bundle);
                getSupportFragmentManager()    //
                        .beginTransaction()
                        .add(R.id.fragment_container, archiveFragment, "Archive")   // 此处的R.id.fragment_container是要盛放fragment的父容器
                        .addToBackStack(null)
                        .commit();
            }
        });

    }

    public void processContactsAction(final Context context, final Handler handler, int contactStatus,
                                      final Button contactBtn, final Button followBtn, Button chatBtn) {
        if (contactStatus == APPLIED) {
            contactBtn.setEnabled(false);
            contactBtn.setText("已申请添加好友");
            followBtn.setVisibility(View.GONE);
        } else if (contactStatus == ESTABLISHED) {
            contactBtn.setVisibility(View.GONE);
            followBtn.setVisibility(View.GONE);

            if (null == chat) {
                chat = new Chat();
            }
            chatBtn.setVisibility(View.VISIBLE);
            chatBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    chat.processChat(context, getYunXinAccount(context), mMeetMember.getInit());
                }
            });
        }

        contactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View editView = LayoutInflater.from(context).inflate(R.layout.edit_dialog, null);
                final TextInputEditText editText = editView.findViewById(R.id.edit_text);
                new AlertDialog.Builder(context)
                        .setTitle("添加好友申请")
                        .setView(editView)
                        .setPositiveButton("确定",//提示框的两个按钮
                                new android.content.DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        RequestBody requestBody = new FormBody.Builder()
                                                .add("uid", String.valueOf(uid))
                                                .add("content", editText.getText().toString()).build();

                                        Slog.d(TAG, "------------------>edit edit edit text: " + editText.getText().toString());
                                        HttpUtil.sendOkHttpRequest(context, CONTACTS_ADD_URL, requestBody, new Callback() {
                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {
                                                if (response.body() != null) {
                                                    String responseText = response.body().string();
                                                    if (isDebug)
                                                        Slog.d(TAG, "==========processContactsAction : " + responseText);

                                                    try {
                                                        JSONObject status = new JSONObject(responseText);
                                                        boolean addContacts = status.optBoolean("add_contacts");
                                                        if (addContacts == true) {
                                                            getContactsStatus(context, handler, uid);
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

                                        contactBtn.setText("已申请添加好友");
                                        contactBtn.setEnabled(false);
                                        followBtn.setVisibility(View.GONE);
                                    }
                                })
                        .setNegativeButton("取消", null).create().show();
            }
        });


    }

    private void getDynamicsCount() {
        dynamicsCount = mArchiveProfile.findViewById(R.id.dynamics_count);
        dynamicsCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MeetArchiveActivity.this, SpecificUserDynamicsActivity.class);
                intent.putExtra("uid", uid);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });

        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .build();

        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_ACTIVITIES_COUNT_BY_UID, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {

                    String responseText = response.body().string();
                    //Slog.d(TAG, "==========response : "+response.body());
                    Slog.d(TAG, "==========getDynamicsCount response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        try {
                            int count = new JSONObject(responseText).optInt("count");
                            Slog.d(TAG, "==========getDynamicsCount response count : " + count);
                            if (count > 0) {
                                Message msg = new Message();
                                Bundle bundle = new Bundle();
                                bundle.putInt("count", count);
                                msg.setData(bundle);
                                msg.what = GET_ACTIVITIES_COUNT_DONE;
                                handler.sendMessage(msg);
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

    private void setActivitiesCountView(int count) {
        Slog.d(TAG, "====================setActivitiesCountView count: " + count);
        dynamicsCount.setVisibility(View.VISIBLE);
        TextView activityCount = mArchiveProfile.findViewById(R.id.dynamics_count_text);
        activityCount.setText(String.valueOf(count));
    }

    private void getConnectStatus() {
        //for contacts status
        getContactsStatus(MeetArchiveActivity.this, handler, uid);
        //for follow status
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();

        HttpUtil.sendOkHttpRequest(MeetArchiveActivity.this, GET_FOLLOW_STATUS_URL, requestBody, new Callback() {
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

    public static void getContactsStatus(Context context, final Handler handler, int uid) {
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        //for contacts
        HttpUtil.sendOkHttpRequest(context, GET_CONTACTS_STATUS_URL, requestBody, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    // if(isDebug)
                    Slog.d(TAG, "==========getContacts Status : " + responseText);
                    try {
                        JSONObject status = new JSONObject(responseText);
                        int contactStatus = status.optInt("status");
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putInt("status", contactStatus);

                        message.setData(bundle);
                        message.what = GET_CONTACTS_STATUS_DONE;
                        //handler.sendEmptyMessage(GET_CONTACTS_STATUS_DONE);
                        handler.sendMessage(message);
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

    private void getFollowStatistics() {

        //for follow
        getFollowStatisticsCount();

        LinearLayout followingCountWrap = mArchiveProfile.findViewById(R.id.following_count_wrap);
        LinearLayout followedCountWrap = mArchiveProfile.findViewById(R.id.followed_count_wrap);
        final TextView followedCount = mArchiveProfile.findViewById(R.id.followed_count);
        final TextView followingCount = mArchiveProfile.findViewById(R.id.following_count);
        followedCountWrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("type", FOLLOWED);
                bundle.putInt("uid", uid);
                bundle.putString("title", "被关注 " + followedCount.getText());
                CommonUserListDialogFragment commonUserListDialogFragment = new CommonUserListDialogFragment();
                commonUserListDialogFragment.setArguments(bundle);
                commonUserListDialogFragment.show(getSupportFragmentManager(), "CommonUserListDialogFragment");
            }
        });

        followingCountWrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("type", FOLLOWING);
                bundle.putInt("uid", uid);
                bundle.putString("title", "关注的 " + followingCount.getText());
                CommonUserListDialogFragment commonUserListDialogFragment = new CommonUserListDialogFragment();
                commonUserListDialogFragment.setArguments(bundle);
                commonUserListDialogFragment.show(getSupportFragmentManager(), "CommonUserListDialogFragment");
            }
        });


    }

    private void getFollowStatisticsCount() {
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(MeetArchiveActivity.this, GET_FOLLOW_STATISTICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug)
                        Slog.d(TAG, "==========getFollow statistics : " + responseText);
                    try {
                        JSONObject followObject = new JSONObject(responseText);

                        int following_count = followObject.optInt("following_count");
                        int followed_count = followObject.optInt("followed_count");
                        if (following_count != 0 || followed_count != 0) {
                            Bundle bundle = new Bundle();
                            bundle.putInt("following_count", following_count);
                            bundle.putInt("followed_count", followed_count);
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

    private void getPraiseStatistics() {
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(MeetArchiveActivity.this, GET_PRAISE_STATISTICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();

                    if (isDebug)
                        Slog.d(TAG, "==========getPraiseStatistics statistics : " + responseText);
                    try {
                        JSONObject praiseObject = new JSONObject(responseText);
                        int praise_count = praiseObject.optInt("praise_count");
                        int praised_count = praiseObject.optInt("praised_count");

                        Bundle bundle = new Bundle();
                        bundle.putInt("praise_count", praise_count);
                        bundle.putInt("praised_count", praised_count);
                        isPraised = praiseObject.optBoolean("isPraised");
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

        LinearLayout praiseCountWrap = mArchiveProfile.findViewById(R.id.praise_count_wrap);
        LinearLayout praisedCountWrap = mArchiveProfile.findViewById(R.id.praised_count_wrap);
        final TextView praisedCount = mArchiveProfile.findViewById(R.id.praised_count);
        final TextView praiseCount = mArchiveProfile.findViewById(R.id.praise_count);

        praisedCountWrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("type", PRAISED);
                bundle.putInt("uid", uid);
                bundle.putString("title", "被赞 " + praisedCount.getText());
                CommonUserListDialogFragment commonUserListDialogFragment = new CommonUserListDialogFragment();
                commonUserListDialogFragment.setArguments(bundle);
                commonUserListDialogFragment.show(getSupportFragmentManager(), "CommonUserListDialogFragment");
            }
        });

        praiseCountWrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("type", PRAISE);
                bundle.putInt("uid", uid);
                bundle.putString("title", "赞过的 " + praiseCount.getText());
                CommonUserListDialogFragment commonUserListDialogFragment = new CommonUserListDialogFragment();
                commonUserListDialogFragment.setArguments(bundle);
                commonUserListDialogFragment.show(getSupportFragmentManager(), "CommonUserListDialogFragment");
            }
        });
    }

    private void getLoveStatistics() {
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(MeetArchiveActivity.this, GET_LOVE_STATISTICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug)
                        Slog.d(TAG, "==========getLoveStatistics statistics : " + responseText);
                    try {

                        JSONObject loveObject = new JSONObject(responseText);
                        int loveCount = loveObject.optInt("love_count");
                        int lovedCount = loveObject.optInt("loved_count");
                        isLoved = loveObject.optBoolean("isLoved");

                        Bundle bundle = new Bundle();
                        bundle.putInt("love_count", loveCount);
                        bundle.putInt("loved_count", lovedCount);
                        Message msg = new Message();
                        msg.setData(bundle);
                        msg.what = GET_LOVE_STATISTICS_URL_DONE;
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

        LinearLayout loveCountWrap = mArchiveProfile.findViewById(R.id.love_count_wrap);
        LinearLayout lovedCountWrap = mArchiveProfile.findViewById(R.id.loved_count_wrap);
        TextView loveDivider = mArchiveProfile.findViewById(R.id.love_divider);
        TextView lovedTitle = mArchiveProfile.findViewById(R.id.loved_count_title);
        lovedCount = mArchiveProfile.findViewById(R.id.loved_count);
        final TextView loveCount = mArchiveProfile.findViewById(R.id.love_count);

        lovedCountWrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("type", LOVED);
                bundle.putInt("uid", uid);
                bundle.putString("title", "被喜欢 " + lovedCount.getText());
                CommonUserListDialogFragment commonUserListDialogFragment = new CommonUserListDialogFragment();
                commonUserListDialogFragment.setArguments(bundle);
                commonUserListDialogFragment.show(getSupportFragmentManager(), "CommonUserListDialogFragment");
            }
        });

        if (authorUid == uid) {//only self can see love statistics
            loveCountWrap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("type", LOVE);
                    bundle.putInt("uid", uid);
                    bundle.putString("title", "喜欢的 " + loveCount.getText());
                    CommonUserListDialogFragment commonUserListDialogFragment = new CommonUserListDialogFragment();
                    commonUserListDialogFragment.setArguments(bundle);
                    commonUserListDialogFragment.show(getSupportFragmentManager(), "CommonUserListDialogFragment");
                }
            });

        } else {
            lovedTitle.setText(getResources().getString(R.string.loved));
            loveDivider.setVisibility(View.GONE);
            loveCountWrap.setVisibility(View.GONE);
        }
    }

    private void processFollowAction() {
        final Button followBtn = mArchiveProfile.findViewById(R.id.follow);
        if (isFollowed == true) {
            followBtn.setText("已关注");
            followBtn.setBackground(getDrawable(R.drawable.btn_disable));
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
                    followBtn.setBackground(getDrawable(R.drawable.btn_default));

                } else {
                    isFollowed = true;
                    followUrl = FOLLOW_ACTION_URL + "add";
                    followBtn.setText("已关注");
                    followBtn.setBackground(getDrawable(R.drawable.btn_disable));
                    followBtn.setTextColor(getResources().getColor(R.color.color_dark_grey));
                }

                HttpUtil.sendOkHttpRequest(MeetArchiveActivity.this, followUrl, requestBody, new Callback() {

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.body() != null) {
                            String responseText = response.body().string();
                            getFollowStatisticsCount();
                            if (isDebug)
                                Slog.d(TAG, "==========get impression response text : " + responseText);
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                    }
                });
            }
        });
    }

    private void setFollowStatistics(Bundle bundle) {
        TextView followedCount = mArchiveProfile.findViewById(R.id.followed_count);
        TextView followingCount = mArchiveProfile.findViewById(R.id.following_count);
        if (bundle.getInt("followed_count") > 0) {
            followedCount.setText(String.valueOf(bundle.getInt("followed_count")));
        }

        if (bundle.getInt("following_count") > 0) {
            followingCount.setText(String.valueOf(bundle.getInt("following_count")));
        }
    }

    private void setPraiseStatistics(Bundle bundle) {

        TextView praisedCount = mArchiveProfile.findViewById(R.id.praised_count);
        TextView praiseCount = mArchiveProfile.findViewById(R.id.praise_count);
        TextView praisedStatistics = mArchiveProfile.findViewById(R.id.praised_statistics);

        if (bundle.getInt("praised_count") > 0) {
            praisedCount.setText(String.valueOf(bundle.getInt("praised_count")));
            praisedStatistics.setText(String.valueOf(bundle.getInt("praised_count")));
        }

        if (bundle.getInt("praise_count") > 0) {
            praiseCount.setText(String.valueOf(bundle.getInt("praise_count")));
        }

        processPraiseAction();
    }

    private void setLoveStatistics(Bundle bundle) {
        lovedCount = mArchiveProfile.findViewById(R.id.loved_count);
        TextView loveCount = mArchiveProfile.findViewById(R.id.love_count);
        lovedStatistics = mArchiveProfile.findViewById(R.id.loved_statistics);
        if (bundle != null) {
            if (bundle.getInt("loved_count") > 0) {
                lovedCount.setText(String.valueOf(bundle.getInt("loved_count")));
                lovedStatistics.setText(String.valueOf(bundle.getInt("loved_count")));
            }

            if (bundle.getInt("love_count") > 0) {
                loveCount.setText(String.valueOf(bundle.getInt("love_count")));
            }
        }

        processLoveAction();//process love or praise action();
    }

    private void processLoveAction() {

        lovedIcon = mArchiveProfile.findViewById(R.id.loved_icon);
        final TextView lovedCount = mArchiveProfile.findViewById(R.id.loved_statistics);
        if (isLoved) {
            lovedIcon.setEnabled(false);
            lovedCount.setEnabled(false);
            lovedIcon.setText(getResources().getText(R.string.fa_heart));
        } else {
            lovedIcon.setEnabled(true);
            lovedCount.setEnabled(true);
            lovedIcon.setText(getResources().getText(R.string.fa_heart_o));
        }
        final RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();

        lovedIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpUtil.sendOkHttpRequest(MeetArchiveActivity.this, LOVE_ADD_URL, requestBody, new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.body() != null) {
                            String responseText = response.body().string();
                            if (isDebug)
                                Slog.d(TAG, "==========love add response text : " + responseText);
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                    }
                });
                isLoved = true;
                lovedIcon.setEnabled(false);
                lovedCount.setEnabled(false);
                handler.sendEmptyMessage(UPDATE_LOVED_COUNT);
            }
        });

        lovedCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                HttpUtil.sendOkHttpRequest(MeetArchiveActivity.this, LOVE_ADD_URL, requestBody, new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.body() != null) {
                            String responseText = response.body().string();
                            if (isDebug)
                                Slog.d(TAG, "==========love add response text : " + responseText);
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                    }
                });
                isLoved = true;
                lovedIcon.setEnabled(false);
                lovedCount.setEnabled(false);
                handler.sendEmptyMessage(UPDATE_LOVED_COUNT);
            }
        });

    }

    private void processPraiseAction() {
        praisedIcon = mArchiveProfile.findViewById(R.id.praised_icon);
        praisedCount = mArchiveProfile.findViewById(R.id.praised_statistics);
        if (isPraised) {
            praisedCount.setEnabled(false);
            praisedIcon.setEnabled(false);
            praisedIcon.setText(getResources().getText(R.string.fa_thumbs_up));
        } else {
            praisedCount.setEnabled(true);
            praisedIcon.setEnabled(true);
            praisedIcon.setText(getResources().getText(R.string.fa_thumbs_O_up));
        }

        final RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        praisedIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpUtil.sendOkHttpRequest(MeetArchiveActivity.this, PRAISE_ADD_URL, requestBody, new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.body() != null) {
                            String responseText = response.body().string();
                            if (isDebug)
                                Slog.d(TAG, "==========praise add response text : " + responseText);
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                    }
                });
                isPraised = true;
                praisedIcon.setEnabled(false);
                praisedCount.setEnabled(false);
                handler.sendEmptyMessage(UPDATE_PRAISED_COUNT);
            }
        });

        praisedCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpUtil.sendOkHttpRequest(MeetArchiveActivity.this, PRAISE_ADD_URL, requestBody, new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.body() != null) {
                            String responseText = response.body().string();
                            if (isDebug)
                                Slog.d(TAG, "==========praise add response text : " + responseText);
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                    }
                });
                isPraised = true;
                praisedIcon.setEnabled(false);
                praisedCount.setEnabled(false);
                handler.sendEmptyMessage(UPDATE_PRAISED_COUNT);
            }
        });

    }

    private void setReferenceView(List<MeetReferenceInfo> meetReferenceInfoList) {

        RecyclerView recyclerView = mHeaderEvaluation.findViewById(R.id.reference_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mMeetReferenceAdapter = new MeetReferenceAdapter(this);
        recyclerView.setAdapter(mMeetReferenceAdapter);

        if (meetReferenceInfoList.size() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
        }

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mHeaderEvaluation.findViewById(R.id.meet_item_id), font);

    }

    private void setRatingBarView() {
        JSONArray ratingArray = mRatingObj.optJSONArray("rating");
        TextView ratingMemberCount = mHeaderEvaluation.findViewById(R.id.rating_member_count);
        if (ratingArray != null && ratingArray.length() > 0) {
            evaluatorDetails.setVisibility(View.VISIBLE);
            ratingMemberCount.setText(ratingArray.length() + "人评价");
            float ratingCount = 0;
            for (int i = 0; i < ratingArray.length(); i++) {
                JSONObject ratingObj = ratingArray.optJSONObject(i);
                if (mRatingObj.optInt("visitor_uid") == ratingObj.optInt("evaluator_uid")) {
                    ratingBarWrapper.setVisibility(View.GONE);
                }
                ratingCount += ratingObj.optDouble("rating");
            }

            float ratingAverage = ratingCount / ratingArray.length();
            float ratingAverageRoundUp = 0;
            BigDecimal b = new BigDecimal(ratingAverage);
            ratingAverageRoundUp = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
            TextView ratingAverageTV = mHeaderEvaluation.findViewById(R.id.chram_synthesized_results);
            ratingAverageTV.setText(ratingAverageRoundUp + "分");

            ScaleRatingBar scaleRatingBarCount = mHeaderEvaluation.findViewById(R.id.charm_synthesized_rating);
            scaleRatingBarCount.setRating(ratingAverageRoundUp);

        } else {
            ratingMemberCount.setText("暂无评价");
        }
    }

    private void loadRating() {
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(this, GET_RATING_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug)
                        Slog.d(TAG, "==========get rating response text : " + responseText);
                    if (responseText != null) {
                        if (!TextUtils.isEmpty(responseText)) {
                            try {

                                mRatingObj = new JSONObject(responseText);
                                if (mRatingObj != null) {
                                    handler.sendEmptyMessage(LOAD_RATING_DONE);
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

        ratingBarWrapper = mHeaderEvaluation.findViewById(R.id.rating_bar_wrapper);

        scaleRatingBar = mHeaderEvaluation.findViewById(R.id.charm_rating_bar);
        final TextView ratingScoreView = mHeaderEvaluation.findViewById(R.id.rating_score);

        scaleRatingBar.setOnRatingChangeListener(new BaseRatingBar.OnRatingChangeListener() {

            @Override
            public void onRatingChange(BaseRatingBar ratingBar, float rating) {
                Slog.d(TAG, "onRatingChange float: " + rating);
                ratingScoreView.setText(String.valueOf(rating));
            }

        });

        scaleRatingBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    Fragment prev = getSupportFragmentManager().findFragmentByTag("EvaluateDialogFragment");
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);
                    if (evaluateDialogFragment == null) {
                        evaluateDialogFragment = new EvaluateDialogFragment();
                    }

                    Bundle bundle = new Bundle();
                    bundle.putInt("uid", uid);
                    bundle.putInt("sex", mMeetMember.getSex());
                    String rating = ratingScoreView.getText().toString();
                    if (!"".equals(rating)) {
                        try {
                            bundle.putFloat("rating", Float.parseFloat(ratingScoreView.getText().toString()));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                    evaluateDialogFragment.setArguments(bundle);

                    //ratingAndImpressionDialogFragment.show(ft, "EvaluateDialogFragment");
                    evaluateDialogFragment.show(ft, "EvaluateDialogFragment");
                }
                return false;
            }
        });

        evaluatorDetails = mHeaderEvaluation.findViewById(R.id.rating_member_details);
        evaluatorDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MeetArchiveActivity.this, EvaluatorDetailsActivity.class);
                intent.putExtra("uid", uid);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });
    }

    private void loadImpressionStatistics() {

        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(this, GET_IMPRESSION_STATISTICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug)
                        Slog.d(TAG, "==========loadImpressionStatistics response text : " + responseText);

                    if (responseText != null) {
                        if (!TextUtils.isEmpty(responseText)) {
                            parseImpressionStatistics(responseText, uid);
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void setImpressionStatisticsView() {
        RecyclerView impressionStatisticsWrap = mHeaderEvaluation.findViewById(R.id.impression_statistics_list);
        impressionStatisticsWrap.setLayoutManager(new LinearLayoutManager(this));
        mMeetImpressionStatisticsAdapter = new MeetImpressionStatisticsAdapter(this, getSupportFragmentManager());
        impressionStatisticsWrap.setAdapter(mMeetImpressionStatisticsAdapter);

        mMeetImpressionStatisticsAdapter.setItemClickListener(new MeetImpressionStatisticsAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(MeetArchiveActivity.this, ApprovedUsersActivity.class);
                intent.putExtra("uid", uid);
                intent.putExtra("impressionStatistics", mImpressionStatisticsList.get(position));
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivityForResult(intent, REQUESTCODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUESTCODE) {
            if (resultCode == RESULT_OK) {
                boolean hasApproved = data.getBooleanExtra("approved", false);
                if (isDebug) Slog.d(TAG, "----------------->approved: " + hasApproved);
                if (hasApproved) {
                    loadImpressionStatistics();
                }
            }
        }
    }


    private void parseImpressionStatistics(String response, int uid) {
        JSONObject responseObj = null;
        try {
            responseObj = new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (responseObj != null) {
            JSONObject impressionStatisticsObj = responseObj.optJSONObject("features_statistics");
            if (impressionStatisticsObj != null) {
                mImpressionStatisticsList.clear();
                Iterator iterator = impressionStatisticsObj.keys();
                int index = 0;
                while (iterator.hasNext()) {
                    index++;
                    String key = (String) iterator.next();
                    int value = impressionStatisticsObj.optInt(key);
                    ImpressionStatistics impressionStatistics = new ImpressionStatistics();
                    impressionStatistics.impression = key;
                    impressionStatistics.impressionCount = value;
                    impressionStatistics.meetMemberList = getImpressionUser(key, uid);
                    mImpressionStatisticsList.add(impressionStatistics);

                    if (index == 5) {
                        break;
                    }
                }
                Message msg = handler.obtainMessage();
                msg.what = LOAD_IMPRESSION_DONE;
                handler.sendMessage(msg);
            }
        }
    }

    private List<UserMeetInfo> getImpressionUser(String impression, int uid) {
        List<UserMeetInfo> memberInfoList = new ArrayList<>();
        RequestBody requestBody = new FormBody.Builder()
                .add("impression", impression)
                .add("uid", String.valueOf(uid)).build();
        if (isDebug) Slog.d(TAG, "impression: " + impression + " uid: " + uid);
        Response response = HttpUtil.sendOkHttpRequestSync(this, GET_IMPRESSION_USERS_URL, requestBody, null);

        if (response != null) {
            try {
                String responseText = response.body().string();
                JSONObject responseObj = new JSONObject(responseText);
                JSONArray responseArray = responseObj.optJSONArray("users");
                if (responseArray != null && responseArray.length() > 0) {

                    for (int i = 0; i < responseArray.length(); i++) {
                        UserMeetInfo userMeetInfo = new UserMeetInfo();
                        JSONObject member = responseArray.optJSONObject(i);
                        userMeetInfo.setUid(member.optInt("uid"));
                        userMeetInfo.setSex(member.optInt("sex"));
                        userMeetInfo.setName(member.optString("name"));
                        userMeetInfo.setAvatar(member.optString("avatar"));
                        userMeetInfo.setSituation(member.optInt("situation"));
                        if (member.optInt("situation") == 0) {//student
                            userMeetInfo.setDegree(member.optString("degree"));
                            userMeetInfo.setMajor(member.optString("major"));
                            userMeetInfo.setUniversity(member.optString("university"));
                        } else {
                            userMeetInfo.setPosition(member.optString("position"));
                            userMeetInfo.setLiving(member.optString("living"));
                        }
                        userMeetInfo.setCid(member.optInt("cid"));
                        memberInfoList.add(userMeetInfo);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return memberInfoList;
    }

    private void processReferences() {
        loadReferences(uid);

        View inviteReference = mHeaderEvaluation.findViewById(R.id.invite_reference);
        LinearLayout writeReferenceWrapper = mHeaderEvaluation.findViewById(R.id.write_reference_wrapper);
        if (isSelf) {
            inviteReference.setVisibility(View.VISIBLE);
            inviteReference.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    InvitationDialogFragment invitationDialogFragment = new InvitationDialogFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt("uid", uid);
                    bundle.putInt("type", ParseUtils.TYPE_REFERENCE);

                    invitationDialogFragment.setArguments(bundle);
                    invitationDialogFragment.show(getSupportFragmentManager(), "InvitationDialogFragment");
                }
            });
        } else {
            writeReferenceWrapper.setVisibility(View.VISIBLE);

            TextView writeReference = mHeaderEvaluation.findViewById(R.id.write_reference);
            writeReference.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ReferenceWriteDialogFragment referenceWriteDialogFragment = new ReferenceWriteDialogFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt("uid", uid);
                    bundle.putString("name", mMeetMember.getName());

                    referenceWriteDialogFragment.setArguments(bundle);
                    referenceWriteDialogFragment.show(getSupportFragmentManager(), "ReferenceWriteDialogFragment");
                }
            });

        }
    }

    private void loadReferences(int uid) {
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(this, LOAD_REFERENCE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug)
                        Slog.d(TAG, "==========loadReferences response text : " + responseText);
                    if (responseText != null) {
                        mReferenceList.clear();
                        List<MeetReferenceInfo> meetReferenceInfoList = ParseUtils.getMeetReferenceList(responseText);
                        if (meetReferenceInfoList != null && meetReferenceInfoList.size() > 0) {
                            mReferenceList.addAll(meetReferenceInfoList);
                            handler.sendEmptyMessage(LOAD_REFERENCE_DONE);
                        }

                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void processPersonality() {
        getPersonality();
        if (isSelf) {
            addPersonality();
        }
    }


    private void getPersonality() {
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .build();
        HttpUtil.sendOkHttpRequest(this, GET_PERSONALITY_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                //if(isDebug)
                Slog.d(TAG, "================getPersonalityDetail response:" + responseText);
                if (responseText != null && !TextUtils.isEmpty(responseText)) {
                    try {
                        personalityResponseArray = new JSONObject(responseText).optJSONArray("personality_detail");
                        if (personalityResponseArray.length() > 0) {
                            sortPersonalityWithCount(personalityResponseArray);
                            if (isDebug)
                                Slog.d(TAG, "====================after sort: " + personalityResponseArray);
                            //Message msg = new Message();
                            handler.sendEmptyMessage(LOAD_PERSONALITY_DONE);
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

    public static void sortPersonalityWithCount(JSONArray jsonArray) {
        JSONObject temp = null;
        int size = jsonArray.length();
        try {
            for (int i = 0; i < size - 1; i++) {
                for (int j = 0; j < size - 1 - i; j++) {
                    if (jsonArray.getJSONObject(j).optInt("count") < jsonArray.getJSONObject(j + 1).optInt("count"))  //交换两数位置
                    {
                        temp = jsonArray.getJSONObject(j);
                        jsonArray.put(j, jsonArray.getJSONObject(j + 1));
                        jsonArray.put(j + 1, temp);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void setPersonalityFlow() {
        FlowLayout personalityFlow = mHeaderEvaluation.findViewById(R.id.personality_flow);
        personalityFlow.removeAllViews();

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins((int) Utility.dpToPx(this, 3), (int) Utility.dpToPx(this, 8),
                (int) Utility.dpToPx(this, 8), (int) Utility.dpToPx(this, 3));
        for (int i = 0; i < personalityResponseArray.length(); i++) {
            final TextView personality = new TextView(this);
            personality.setPadding((int) Utility.dpToPx(this, 8), (int) Utility.dpToPx(this, 6),
                    (int) Utility.dpToPx(this, 8), (int) Utility.dpToPx(this, 6));
            personality.setBackground(getDrawable(R.drawable.label_btn_shape));
            personality.setTextColor(getResources().getColor(R.color.color_blue));
            personality.setLayoutParams(layoutParams);
            String personalityName = personalityResponseArray.optJSONObject(i).optString("personality");
            int count = personalityResponseArray.optJSONObject(i).optInt("count");
            if (count != 0) {
                personality.setText(personalityName + " · " + count);
            } else {
                personality.setText(personalityName);
            }
            personalityFlow.addView(personality);
            final int pid = personalityResponseArray.optJSONObject(i).optInt("pid");
            personality.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final CommonUserListDialogFragment commonUserListDialogFragment = new CommonUserListDialogFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt("pid", pid);
                    bundle.putString("personality", personality.getText().toString());
                    commonUserListDialogFragment.setArguments(bundle);
                    commonUserListDialogFragment.show(getSupportFragmentManager(), "CommonUserListDialogFragment");
                }
            });
        }
    }

    private void addPersonality() {
        TextView addPersonality = mHeaderEvaluation.findViewById(R.id.add_personality);
        addPersonality.setVisibility(View.VISIBLE);
        addPersonality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PersonalityEditDialogFragment personalityEditDialogFragment = new PersonalityEditDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("uid", uid);
                bundle.putInt("type", ParseUtils.TYPE_PERSONALITY);
                personalityEditDialogFragment.setArguments(bundle);
                personalityEditDialogFragment.show(getSupportFragmentManager(), "PersonalityEditDialogFragment");
            }
        });
    }

    private void processHobby() {
        getHobbies();
        if (isSelf) {
            addHobbies();
        }
    }

    private void processCheeringGroup() {
        getCheeringGroup();
        if (isSelf) {
            addCheeringGroupMember();
        }
    }

    private void getCheeringGroup() {

        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(this, GET_CHEERING_GROUP_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    boolean hasJoined = false;
                    String responseText = response.body().string();
                    //if (isDebug)
                    Slog.d(TAG, "==========getCheeringGroup response text : " + responseText);
                    if (responseText != null) {
                        try {
                            JSONArray jsonArray = new JSONObject(responseText).getJSONArray("response");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                mCheeringGroupMemberList.clear();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    UserProfile userProfile = ParseUtils.getUserProfileFromJSONObject(jsonArray.getJSONObject(i));
                                    if (userProfile.getUid() == authorUid) {
                                        hasJoined = true;
                                    }
                                    mCheeringGroupMemberList.add(userProfile);
                                }

                                if (!hasJoined & !isSelf) {
                                    joinCheeringGroup();
                                }

                                handler.sendEmptyMessage(GET_CHEERING_GROUP_DONE);
                            } else {
                                if (!isSelf) {
                                    joinCheeringGroup();
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (!isSelf) {
                            joinCheeringGroup();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void addCheeringGroupMember() {
        final TextView addMember = mHeaderEvaluation.findViewById(R.id.add_cheering_group);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                addMember.setVisibility(View.VISIBLE);
            }
        });

        addMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InvitationDialogFragment invitationDialogFragment = new InvitationDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("uid", uid);
                bundle.putInt("type", ParseUtils.TYPE_CHEERING_GROUP);
                invitationDialogFragment.setArguments(bundle);
                invitationDialogFragment.show(getSupportFragmentManager(), "InvitationDialogFragment");
            }
        });
    }

    private void joinCheeringGroup() {
        final TextView join = mHeaderEvaluation.findViewById(R.id.join_cheering_group);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                join.setVisibility(View.VISIBLE);
            }
        });

        join.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                join.setVisibility(View.GONE);
                showProgressDialog("正在加入");
                RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
                HttpUtil.sendOkHttpRequest(MeetArchiveActivity.this, JOIN_CHEERING_GROUP_URL, requestBody, new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseText = response.body().string();
                        Slog.d(TAG, "================joinCheeringGroup response:" + responseText);
                        if (responseText != null && !TextUtils.isEmpty(responseText)) {
                            dismissProgressDialog();
                            handler.sendEmptyMessage(JOIN_CHEERING_GROUP_DONE);
                        }

                    }

                    @Override
                    public void onFailure(Call call, IOException e) {

                    }
                });
            }
        });
    }

    private void setCheeringGroupView() {
        HtGridView cheeringGroupList = mHeaderEvaluation.findViewById(R.id.cheering_group_list);

        mCheeringGroupAdapter = new CheeringGroupAdapter(this, mWidth);
        mCheeringGroupAdapter.setCheeringGroupList(mCheeringGroupMemberList);
        cheeringGroupList.setAdapter(mCheeringGroupAdapter);
        cheeringGroupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent intent = new Intent(MeetArchiveActivity.this, ArchiveActivity.class);
                intent.putExtra("uid", mCheeringGroupMemberList.get(position).getUid());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });

    }

    private void getHobbies() {
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .build();
        HttpUtil.sendOkHttpRequest(this, LOAD_HOBBY_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "================get hobbies response:" + responseText);
                if (responseText != null && !TextUtils.isEmpty(responseText)) {
                    try {

                        JSONArray hobbyJSONArray = new JSONObject(responseText).optJSONArray("hobby");
                        String[] hobbyArray = new String[hobbyJSONArray.length()];
                        for (int i = 0; i < hobbyJSONArray.length(); i++) {
                            hobbyArray[i] = hobbyJSONArray.optJSONObject(i).optString("hobby");
                        }
                        if (hobbyArray.length > 0) {
                            Message msg = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putStringArray("hobby", hobbyArray);
                            msg.setData(bundle);
                            msg.what = LOAD_HOBBY_DONE;
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

    private void addHobbies() {
        TextView add = mHeaderEvaluation.findViewById(R.id.add_hobby);
        add.setVisibility(View.VISIBLE);
        add.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                PersonalityEditDialogFragment personalityEditDialogFragment = new PersonalityEditDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("uid", uid);
                bundle.putInt("type", ParseUtils.TYPE_HOBBY);
                personalityEditDialogFragment.setArguments(bundle);
                personalityEditDialogFragment.show(getSupportFragmentManager(), "PersonalityEditDialogFragment");

            }
        });
    }

    private void setHobbyFlow(String[] hobbyArray) {
        FlowLayout hobbyFlow = mHeaderEvaluation.findViewById(R.id.hobby_flow);
        hobbyFlow.removeAllViews();

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins((int) Utility.dpToPx(this, 3), (int) Utility.dpToPx(this, 3),
                (int) Utility.dpToPx(this, 3), (int) Utility.dpToPx(this, 3));
        for (int i = 0; i < hobbyArray.length; i++) {
            if (!hobbyArray[i].isEmpty()) {
                final TextView hobbyView = new TextView(this);
                hobbyView.setPadding((int) Utility.dpToPx(this, 8), (int) Utility.dpToPx(this, 6),
                        (int) Utility.dpToPx(this, 8), (int) Utility.dpToPx(this, 6));
                //hobbyView.setBackground(getDrawable(R.drawable.label_btn_shape));
                hobbyView.setTextColor(getResources().getColor(R.color.color_blue));
                hobbyView.setLayoutParams(layoutParams);
                hobbyView.setText(hobbyArray[i]);
                hobbyFlow.addView(hobbyView);
            }
        }
    }

    @Override
    public void onBackFromDialog(int type, int result, boolean status) {
        switch (type) {
            case ParseUtils.TYPE_EVALUATE://For EvaluateDialogFragment back
                if (status == true) {
                    loadRating();
                    loadImpressionStatistics();
                }
                break;
            case ParseUtils.TYPE_REFERENCE://For ReferenceWriteDialogFrament back
                if (status == true) {
                    processReferences();
                }
                break;
            case ParseUtils.TYPE_PERSONALITY://For PersonalityEditDialogFrament back
                if (status == true) {
                    processPersonality();
                }
                break;
            case ParseUtils.TYPE_HOBBY://For PersonalityEditDialogFrament back
                if (status == true) {
                    processHobby();
                }
                break;
            case ParseUtils.TYPE_CHEERING_GROUP://For PersonalityEditDialogFrament back
                if (status == true) {
                    processCheeringGroup();
                }
                break;
            default:
                break;
        }
    }


    private void updateLovedCount() {
        lovedStatistics = mArchiveProfile.findViewById(R.id.loved_statistics);
        lovedCount = mArchiveProfile.findViewById(R.id.loved_count);
        int newLovedStatistics = 0;
        int newLovedCount = 0;
        if (lovedStatistics.getText().toString().length() == 0) {
            newLovedStatistics = 1;
        } else {
            newLovedStatistics = Integer.parseInt(lovedStatistics.getText().toString()) + 1;
        }

        lovedStatistics.setText(String.valueOf(newLovedStatistics));

        if (lovedCount.getText().toString().length() == 0) {
            newLovedCount = 1;
        } else {
            newLovedCount = Integer.parseInt(lovedCount.getText().toString()) + 1;
        }
        lovedCount.setText(String.valueOf(newLovedCount));
        lovedIcon.setText(getResources().getText(R.string.fa_heart));
    }

    private void updatePraisedCount() {
        TextView praisedStatistics = mArchiveProfile.findViewById(R.id.praised_statistics);
        //TextView praisedCount = mArchiveProfile.findViewById(R.id.praised_count);
        int newPraisedStatistics = 0;
        int newPraisedCount = 0;
        if (praisedStatistics.getText().toString().length() == 0) {
            newPraisedStatistics = 1;
        } else {
            newPraisedStatistics = Integer.parseInt(praisedStatistics.getText().toString()) + 1;
        }
        praisedStatistics.setText(String.valueOf(newPraisedStatistics));

        if (praisedCount.getText().toString().length() == 0) {
            newPraisedCount = 1;
        } else {
            newPraisedCount = Integer.parseInt(praisedCount.getText().toString());
        }
        praisedCount.setText(String.valueOf(newPraisedCount));
        praisedIcon.setText(getResources().getText(R.string.fa_thumbs_up));
    }

    public void processVisitRecord() {
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();

        //Add visit record
        HttpUtil.sendOkHttpRequest(this, ADD_VISIT_RECORD_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    //TODO
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });

        //Get visit record
        HttpUtil.sendOkHttpRequest(this, GET_VISIT_RECORD_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    //if (isDebug)
                    Slog.d(TAG, "==========get visit record response text : " + responseText);
                    if (responseText != null) {
                        try {
                            int visitRecord = new JSONObject(responseText).optInt("visit_record");
                            if (visitRecord > 0) {
                                Message msg = new Message();
                                Bundle bundle = new Bundle();
                                bundle.putInt("visitRecord", visitRecord);
                                msg.setData(bundle);
                                msg.what = GET_VISIT_RECORD_DONE;
                                handler.sendMessage(msg);
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

    public void setVisitRecordView(int recordCount) {
        TextView visitCountView = mArchiveProfile.findViewById(R.id.visit_record);
        visitCountView.setText(String.valueOf(recordCount));
    }

    public static class ImpressionStatistics implements Parcelable {
        public static final Parcelable.Creator<ImpressionStatistics> CREATOR = new Creator<ImpressionStatistics>() {

            @Override
            public ImpressionStatistics createFromParcel(Parcel source) {
                ImpressionStatistics impressionStatistics = new ImpressionStatistics();
                impressionStatistics.impression = source.readString();
                impressionStatistics.impressionCount = source.readInt();
                //impressionStatistics.meetMemberList = new ArrayList<UserMeetInfo>();
                impressionStatistics.meetMemberList = source.readArrayList(getClass().getClassLoader());

                return impressionStatistics;
            }

            @Override
            public ImpressionStatistics[] newArray(int size) {
                return new ImpressionStatistics[size];
            }
        };

        public String impression;
        public int impressionCount;
        public List<UserMeetInfo> meetMemberList = new ArrayList<>();

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            //序列化过程：必须按成员变量声明的顺序进行封装
            dest.writeString(impression);
            dest.writeInt(impressionCount);
            dest.writeList(meetMemberList);
        }
    }

    public void handleMessage(Message message) {
        Bundle bundle = message.getData();
        switch (message.what) {
            case GET_LOGGEDIN_UID_DONE:
                if (mMeetMember == null) {
                    uid = getIntent().getIntExtra("uid", -1);
                    getMeetArchive(this, uid);
                } else {
                    uid = mMeetMember.getUid();
                    processSubModules();
                }
                break;

            case GET_MEET_ARCHIVE_DONE:
                processSubModules();
                break;
            case GET_PICTURES_URL_DONE:
                if (pictureArray.length() > 0) {
                    for (int i = 0; i < pictureArray.length(); i++) {
                        JSONObject pictureObj = pictureArray.optJSONObject(i);
                        final String url = HttpUtil.DOMAIN + pictureObj.optString("uri") + pictureObj.optString("filename");
                        //Slog.d(TAG, "****************url: "+url);
                        SimpleTarget<Drawable> simpleTarget = new SimpleTarget<Drawable>() {
                            @Override
                            public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                                // imageView.setImageDrawable(resource);
                                drawableList.add(resource);
                                if (isDebug)
                                    Slog.d(TAG, "****************pictureArray length: " + pictureArray.length() + " drawableList size: " + drawableList.size());
                                if (pictureArray.length() == drawableList.size() - 1) {
                                    //add indicator
                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                    for (int i = 0; i < pictureArray.length() + 1; i++) {
                                        TextView textView = new TextView(getApplicationContext());
                                        if (i != 0) {
                                            layoutParams.setMarginStart(16);
                                            textView.setText(R.string.circle_o);
                                        } else {
                                            textView.setText(R.string.circle);
                                        }
                                        textView.setTextColor(getResources().getColor(R.color.white));
                                        textView.setLayoutParams(layoutParams);

                                        navLayout.addView(textView);
                                    }

                                    Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
                                    FontManager.markAsIconContainer(findViewById(R.id.meet_archive_profile), font);
                                }
                            }
                        };

                        Glide.with(getApplicationContext()).load(url).into(simpleTarget);
                    }
                }
                break;
            case LOAD_REFERENCE_DONE:
                setReferenceView(mReferenceList);
                mMeetReferenceAdapter.setReferenceList(mReferenceList);
                mMeetReferenceAdapter.notifyDataSetChanged();
                break;

            case LOAD_RATING_DONE:
                setRatingBarView();
                break;
            case LOAD_IMPRESSION_DONE:
                setImpressionStatisticsView();
                mMeetImpressionStatisticsAdapter.setImpressionList(mImpressionStatisticsList);
                mMeetImpressionStatisticsAdapter.notifyDataSetChanged();
                break;
            case LOAD_PERSONALITY_DONE:
                setPersonalityFlow();
                break;
            case LOAD_HOBBY_DONE:
                String[] hobby = bundle.getStringArray("hobby");
                setHobbyFlow(hobby);
                break;
            case GET_CONTACTS_STATUS_DONE:
                int contactStatus = bundle.getInt("status");
                final Button contacts = mArchiveProfile.findViewById(R.id.contacts);
                final Button follow = mArchiveProfile.findViewById(R.id.follow);
                Button chat = mArchiveProfile.findViewById(R.id.chat);
                processContactsAction(MeetArchiveActivity.this, handler, contactStatus, contacts, follow, chat);
                break;
            case GET_FOLLOW_DONE:
                processFollowAction();
                break;
            case GET_FOLLOW_STATISTICS_URL_DONE:
                setFollowStatistics(bundle);
                break;
            case GET_PRAISE_STATISTICS_URL_DONE:
                setPraiseStatistics(bundle);
                break;

            case GET_LOVE_STATISTICS_URL_DONE:
                setLoveStatistics(bundle);
                break;
            case UPDATE_LOVED_COUNT:
                updateLovedCount();
                break;
            case UPDATE_PRAISED_COUNT:
                updatePraisedCount();
                break;
            case GET_CHEERING_GROUP_DONE:
                setCheeringGroupView();
                break;

            case JOIN_CHEERING_GROUP_DONE:
                processCheeringGroup();
                break;
            case GET_VISIT_RECORD_DONE:
                int recordCount = bundle.getInt("visitRecord");
                setVisitRecordView(recordCount);
                break;
            case GET_ACTIVITIES_COUNT_DONE:
                int count = bundle.getInt("count");
                setActivitiesCountView(count);
                break;
            default:
                break;
        }
    }

    public void getMeetArchive(final Context context, final int uid) {
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(context, ParseUtils.GET_MEET_ARCHIVE_URL, requestBody, new Callback() {
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
                                    ParseUtils.startArchiveActivity(MeetArchiveActivity.this, uid);
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

    @Override
    public View makeView() {
        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        float density = dm.density;
        int width = dm.widthPixels;
        mWidth = width;
        int screenHeight = width;
        final ImageView i = new ImageView(this);
        //i.setBackgroundColor(0xff000000);
        i.setScaleType(ImageView.ScaleType.CENTER_CROP);
        i.setLayoutParams(new ImageSwitcher.LayoutParams(width, screenHeight));
        return i;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        //在触发时回去到起始坐标
        String action = "";
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                direction = 0;
                view.getParent().requestDisallowInterceptTouchEvent(true);
                //downX = event.getX();
                //downY = event.getY();
                downX = x;
                downY = y;

                break;

            case MotionEvent.ACTION_MOVE:
                //获取到距离差
                float dx = x - downX;
                float dy = y - downY;
                //防止是按下也判断
                if (Math.abs(dx) > 10 && Math.abs(dy) > 10) {
                    //通过距离差判断方向
                    int orientation = getOrientation(dx, dy);
                    switch (orientation) {
                        case 'r':
                            action = "右";
                            direction = 1;

                            break;
                        case 'l':
                            action = "左";
                            direction = 0;

                            break;

                        case 't':
                        case 'b':
                            action = "上下";
                            direction = -1;
                            view.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                    //Toast.makeText(this, "向" + action + "滑动", Toast.LENGTH_SHORT).show();
                }

                break;

            case MotionEvent.ACTION_UP:
                if (direction == 0) {
                    if (currentPosition < drawableList.size() - 1) {
                        mImageSwitcher.setInAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.fade_in));
                        mImageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.fade_out));
                        currentPosition++;
                        mImageSwitcher.setImageDrawable(drawableList.get(currentPosition));
                        //Toast.makeText(getApplication(), "currentPosition: "+currentPosition, Toast.LENGTH_SHORT).show();
                        setPictureIndicator(currentPosition, direction);
                    }

                } else if (direction == 1) {
                    if (currentPosition > 0) {
                        //设置动画，这里的动画比较简单，不明白的去网上看看相关内容
                        mImageSwitcher.setInAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.fade_in));
                        mImageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.fade_out));
                        currentPosition--;
                        mImageSwitcher.setImageDrawable(drawableList.get(currentPosition));
                        //Toast.makeText(getApplication(), "currentPosition: "+currentPosition, Toast.LENGTH_SHORT).show();
                        setPictureIndicator(currentPosition, direction);
                    }
                }
                break;

        }
        return true;
    }

    /**
     * 根据距离差判断 滑动方向
     *
     * @param dx X轴的距离差
     * @param dy Y轴的距离差
     * @return 滑动的方向
     */
    private int getOrientation(float dx, float dy) {

        if (Math.abs(dx) >= Math.abs(dy) - 10) {
            //X轴移动
            return dx > 0 ? 'r' : 'l';
        } else {
            //Y轴移动
            return dy > 0 ? 'b' : 't';
        }
    }

    static class MyHandler extends HandlerTemp<MeetArchiveActivity> {
        public MyHandler(MeetArchiveActivity cls) {
            super(cls);
        }

        @Override
        public void handleMessage(Message message) {
            MeetArchiveActivity meetArchivesActivity = ref.get();
            if (meetArchivesActivity != null) {
                meetArchivesActivity.handleMessage(message);
            }
        }
    }

}


     


        
