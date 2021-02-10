package com.mufu.main;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.entity.LocalMedia;
import com.mufu.R;
import com.mufu.adapter.CheeringGroupAdapter;
import com.mufu.adapter.MeetImpressionStatisticsAdapter;
import com.mufu.adapter.MeetReferenceAdapter;
import com.mufu.common.OnItemClickListener;
import com.mufu.common.ReminderManager;
import com.mufu.common.SettingsActivity;
import com.mufu.consult.ConsultSummaryActivity;
import com.mufu.contacts.ContactsActivity;
import com.mufu.contacts.ChatActivity;
import com.mufu.dynamics.Dynamic;
import com.mufu.common.HandlerTemp;
import com.mufu.common.MyApplication;
import com.mufu.common.SetAvatarActivity;
import com.mufu.experience.ExperienceSummaryActivity;
import com.mufu.experience.ExperienceTalentApplyDF;
import com.mufu.experience.GuideSummaryActivity;
import com.mufu.meet.ApprovedUsersActivity;
import com.mufu.group.MyParticipationDialogFragment;
import com.mufu.meet.EvaluateDialogFragment;
import com.mufu.meet.EvaluatorDetailsActivity;
import com.mufu.explore.ShareFragment;
import com.mufu.meet.FillMeetInfoActivity;
import com.mufu.meet.MeetConditionDialogFragment;
import com.mufu.picture.GlideEngine;
import com.mufu.talent.TalentApplyEntryDF;
import com.mufu.talent.TalentIntroductionEntryDF;
import com.mufu.meet.MeetReferenceInfo;
import com.mufu.dynamics.SpecificUserDynamicsActivity;
import com.mufu.meet.UserMeetInfo;
import com.mufu.util.BaseFragment;
import com.mufu.util.CommonDialogFragmentInterface;
import com.mufu.util.CommonUserListDialogFragment;
import com.mufu.util.FontManager;
import com.mufu.util.HtGridView;
import com.mufu.util.HttpUtil;
import com.mufu.common.InvitationDialogFragment;
import com.mufu.util.ParseUtils;
import com.mufu.util.PersonalityEditDialogFragment;
import com.mufu.util.ReferenceWriteDialogFragment;
import com.mufu.util.RoundImageView;
import com.mufu.util.SharedPreferencesUtils;
import com.mufu.util.Slog;
import com.mufu.util.UserProfile;
import com.mufu.util.Utility;
import com.nex3z.flowlayout.FlowLayout;
import com.tencent.imsdk.TIMConversationType;
import com.tencent.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.willy.ratingbar.BaseRatingBar;
import com.willy.ratingbar.ScaleRatingBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.mufu.common.MyApplication.getContext;
import static com.mufu.common.SetAvatarActivity.AVATAR_SET_ACTION_BROADCAST;
import static com.mufu.contacts.ContactsActivity.GET_APPLY_AND_REQUEST_COUNT;
import static com.mufu.contacts.ContactsActivity.HAS_REQUEST_OR_APPLY;
import static com.mufu.group.GroupFragment.GET_MY_TALENTS;
import static com.mufu.group.MyParticipationDialogFragment.MY_TALENT;
import static com.mufu.main.MainActivity.setTuiKitProfile;
import static com.mufu.meet.EvaluateModifyDialogFragment.EVALUATE_MODIFY_ACTION_BROADCAST;
import static com.mufu.meet.MeetRecommendFragment.GET_MY_CONDITION_URL;
import static com.mufu.meet.MeetRecommendFragment.MY_CONDITION_NOT_SET;
import static com.mufu.meet.MeetRecommendFragment.MY_CONDITION_SET_DONE;
import static com.mufu.util.ParseUtils.startArchiveActivity;
import static com.mufu.util.ParseUtils.startMeetArchiveActivity;
import static com.xuexiang.xupdate.utils.DrawableUtils.getDrawable;

public class MeetArchiveFragment extends BaseFragment implements CommonDialogFragmentInterface, ReminderManager.UnreadNumChangedCallback {
    private static final String TAG = "MeetArchiveFragment";
    private static final boolean isDebug = false;
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
    private static final String LOVE_CANCEL_URL = HttpUtil.DOMAIN + "?q=meet/love/cancel";
    private static final String PRAISE_ADD_URL = HttpUtil.DOMAIN + "?q=meet/praise/add";
    private static final String PRAISE_CANCEL_URL = HttpUtil.DOMAIN + "?q=meet/praise/cancel";
    private static final String GET_PROFILE_PICTURES_URL = HttpUtil.DOMAIN + "?q=meet/get_pictures_url";
    private static final String ADD_VISIT_RECORD_URL = HttpUtil.DOMAIN + "?q=visitor_record/add_visit_record";
    private static final String GET_VISIT_RECORD_URL = HttpUtil.DOMAIN + "?q=visitor_record/get_visit_record";
    public static final String GET_LOGGEDIN_ACCOUNT = HttpUtil.DOMAIN + "?q=account_manager/get_loggedin_account";
    private static final String JOIN_CHEERING_GROUP_URL = HttpUtil.DOMAIN + "?q=meet/cheering_group/join";
        public static final String GET_EXPERIENCE_STATISTICS_URL = HttpUtil.DOMAIN + "?q=experience/get_experience_statistics";
    public static final String GET_GUIDE_STATISTICS_URL = HttpUtil.DOMAIN + "?q=travel_guide/get_guide_statistics";
    public static final String GET_CONSULT_WITH_UID = HttpUtil.DOMAIN + "?q=consult/get_consult_statistics_by_uid";

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
    public static final int GET_MEET_ARCHIVE_DONE = 22;
        private static final int LOAD_MY_TALENTS_DONE = 23;
public static final int LOAD_MY_EXPERIENCES_DONE = 24;
    public static final int LOAD_MY_GUIDE_COUNT_DONE = 25;
    private static final int LOAD_MY_CONSULT_COUNT_DONE = 26;
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
    private ShareFragment shareFragment;
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
    private TextView dynamicsCount;
    private TextView setAvatarBtnTV;

    private final static int REQUESTCODE = 1;//for impression approve detail
    public final static int RESULT_OK = 2;
    private ImageSwitcher mImageSwitcher;
    private float downX;
    private float downY;
    private float lastX;
    private float lastY;
    int mWidth = 0;
    private int currentPosition = 0;
    int direction = 0;//default for left, 1 for right
    private int uid = -1;
    private static int authorUid = -1;
    private boolean isSelf = false;

    private int myTalentSize = 0;
    private int myExperienceSize = 0;
    private int myGuideCount = 0;
    private int myAnswerCount = 0;
    private int myConsultCount = 0;
    private UserProfile myProfile;
    private LinearLayout navLayout;
    private List<Drawable> drawableList = new ArrayList<>();
    private View viewContent;
    private Context mContext;
    private AvatarAddBroadcastReceiver mReceiver;
    private ChatInfo chatInfo;
    private float ratingAverageRoundUp = 0;
    private int newApplyCount = 0;
    private TextView newApplyCountView;
    private ConstraintLayout serviceWrapper;
    private String mFirstAvatarUrl;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewContent = inflater.inflate(R.layout.meet_archive, container, false);
        mContext = getContext();
        handler = new MyHandler(this);
        registerMsgUnreadInfoObserver(true);
        setView();
        getLoggedinAccount();

        return viewContent;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterLoginBroadcast();
        registerMsgUnreadInfoObserver(false);
        Slog.d(TAG, "------------------->onDestroy");
    }

    @Override
    public void onResume() {
        super.onResume();
        Slog.d(TAG, "------------------->onResume");
        //loadProfilePictures();
    }


    private void setView() {
        Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(viewContent.findViewById(R.id.custom_actionbar), font);

        mArchiveProfile = viewContent.findViewById(R.id.meet_archive_profile);
        FontManager.markAsIconContainer(mArchiveProfile.findViewById(R.id.meet_archive_profile), font);
        mHeaderEvaluation = viewContent.findViewById(R.id.friends_relatives_reference);
        serviceWrapper = mHeaderEvaluation.findViewById(R.id.service_wrapper);
        FontManager.markAsIconContainer(mHeaderEvaluation.findViewById(R.id.friends_relatives_reference), font);

        registerLocalBroadcast();
    }

    private void getLoggedinAccount() {
        authorUid = SharedPreferencesUtils.getSessionUid(MyApplication.getContext());
        Slog.d(TAG, "------------------------>authorUid: " + authorUid);
        if (getArguments() == null) {//get current user info

            if (authorUid > 0) {
                getMeetArchive(mContext, authorUid);
                uid = authorUid;
            } else {
                getLoggedinAccountFromServer();
            }

        } else {//get pass through user info
            mMeetMember = (UserMeetInfo) getArguments().getSerializable("user_meet_info");
            if (mMeetMember == null) {
                uid = getArguments().getInt("uid", -1);
                if (uid > 0) {
                    getMeetArchive(mContext, uid);
                }
            } else {
                uid = mMeetMember.getUid();
                processSubModules();
            }
        }
        if (isDebug) Slog.d(TAG, "------------>authorUid: " + authorUid + "    uid: " + uid);
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

    private void processSubModules() {

        if (authorUid == uid) {
            isSelf = true;
        }
        if (isDebug) Slog.d(TAG, "==========isSelf : " + isSelf);

        getApplyAndRequestCount();
        
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
        loadConsultsCount();
        loadMyTalentsCount();
        loadMyExperiencesCount();
        loadMyGuidesCount();
    }
    
    private void loadConsultsCount(){
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .build();

        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_CONSULT_WITH_UID, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========loadConsultsCount response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject consultResponse = null;
                        try {
                            consultResponse = new JSONObject(responseText);
                            if (consultResponse != null) {
                                processConsultResponse(consultResponse);
                                if (myAnswerCount > 0 || myConsultCount > 0){
                                    handler.sendEmptyMessage(LOAD_MY_CONSULT_COUNT_DONE);
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
    
    public void processConsultResponse(JSONObject consultResponse) {
        if (consultResponse != null) {
            myAnswerCount = consultResponse.optInt("answer_count");
            myConsultCount = consultResponse.optInt("consult_count");
        }
    }


    private void setMyConsultView(){

        TextView answerCountTV = mHeaderEvaluation.findViewById(R.id.answer_count);
        TextView questionCountTV = mHeaderEvaluation.findViewById(R.id.question_count);
        answerCountTV.setText(String.valueOf(myAnswerCount));
        questionCountTV.setText(String.valueOf(myConsultCount));

        answerCountTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startConsultSummaryActivity(Utility.ConsultType.ANSWERED.ordinal());
            }
        });

        questionCountTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startConsultSummaryActivity(Utility.ConsultType.QUESTIONED.ordinal());
            }
        });
    }
    
    public void startConsultSummaryActivity(int type){
        Intent intent = new Intent(getContext(), ConsultSummaryActivity.class);
        intent.putExtra("uid", uid);
        intent.putExtra("type", type);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(intent);
    }
    
    private void loadMyTalentsCount(){
                RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_MY_TALENTS, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========loadMyTalents response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject talentResponse = null;
                        try {
                            talentResponse = new JSONObject(responseText);
                            if (talentResponse != null) {
                                myTalentSize = processTalentResponse(talentResponse);
                                if (myTalentSize > 0){
                                    handler.sendEmptyMessage(LOAD_MY_TALENTS_DONE);
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
    
    private void setMyTalentSizeView(){
        serviceWrapper.setVisibility(View.VISIBLE);
                
        LinearLayout talentWrapper = mHeaderEvaluation.findViewById(R.id.common_talents);
        talentWrapper.setVisibility(View.VISIBLE);
        TextView talentCountTV = mHeaderEvaluation.findViewById(R.id.talents);
        talentCountTV.setText("达人 "+String.valueOf(myTalentSize));

        talentWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMyParticipationDF(MY_TALENT);
            }
        });
    }

    public void startMyParticipationDF(int type){
        MyParticipationDialogFragment myParticipationDialogFragment = MyParticipationDialogFragment.newInstance(type, uid);
        myParticipationDialogFragment.show(getFragmentManager(), "MyParticipationDialogFragment");
    }
    
    public int processTalentResponse(JSONObject talentResponse) {

        int talentSize = 0;
        JSONArray talentArray = null;

        if (talentResponse != null) {
            talentArray = talentResponse.optJSONArray("talents");
        }
        if (talentArray != null) {
            talentSize = talentArray.length();
        }

        return talentSize;
    }
    
    private void loadMyExperiencesCount(){
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_EXPERIENCE_STATISTICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========loadMyExperiencesCount response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject experienceResponse = null;
                        try {
                            experienceResponse = new JSONObject(responseText);
                            if (experienceResponse != null) {
                                myExperienceSize = experienceResponse.optInt("count");
                                if (myExperienceSize > 0){
                                    handler.sendEmptyMessage(LOAD_MY_EXPERIENCES_DONE);
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
    
    private void setMyExperienceSizeView(){
        serviceWrapper.setVisibility(View.VISIBLE);
        LinearLayout experienceWrapper = mHeaderEvaluation.findViewById(R.id.experience_wrapper);
        experienceWrapper.setVisibility(View.VISIBLE);
        TextView experienceCountTV = mHeaderEvaluation.findViewById(R.id.experiences);
        experienceCountTV.setText("体验 "+String.valueOf(myExperienceSize));

        experienceWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMyExperiencesActivity();
            }
        });
    }
    
    public void startMyExperiencesActivity(){
        Intent intent = new Intent(getContext(), ExperienceSummaryActivity.class);
        intent.putExtra("uid", uid);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(intent);
    }
    
    private void loadMyGuidesCount(){
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_GUIDE_STATISTICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========loadMyGuidesCount response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject experienceResponse = null;
                        try {
                            experienceResponse = new JSONObject(responseText);
                            if (experienceResponse != null) {
                                myGuideCount = experienceResponse.optInt("count");
                                if (myGuideCount > 0){
                                    handler.sendEmptyMessage(LOAD_MY_GUIDE_COUNT_DONE);
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
       
    private void setMyGuideSizeView(){
        serviceWrapper.setVisibility(View.VISIBLE);
        LinearLayout guideWrapper = mHeaderEvaluation.findViewById(R.id.guide_wrapper);
        guideWrapper.setVisibility(View.VISIBLE);
        TextView guideCountTV = mHeaderEvaluation.findViewById(R.id.guides);
        guideCountTV.setText("向导 "+String.valueOf(myGuideCount));

        guideWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMyGuidesActivity();
            }
        });
    }
    
    public void startMyGuidesActivity(){
        Intent intent = new Intent(getContext(), GuideSummaryActivity.class);
        intent.putExtra("uid", uid);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(intent);
    }

    private void loadProfilePictures() {
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(mContext, GET_PROFILE_PICTURES_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    //if (isDebug)
                    Slog.d(TAG, "==========loadProfilePictures : " + responseText);
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
            if (preIndicator != null) {
                preIndicator.setText(R.string.circle_o);
            }
        } else {//touch move right
            if (nextIndicator != null) {
                nextIndicator.setText(R.string.circle_o);
            }
        }
    }

    private void setMeetProfile() {
        TextView name = mArchiveProfile.findViewById(R.id.name);
        TextView sex = mArchiveProfile.findViewById(R.id.sex);
        Button meet = mArchiveProfile.findViewById(R.id.meet_btn);
        ConstraintLayout statusBar = mArchiveProfile.findViewById(R.id.statusbar);
        TextView settingNav = mArchiveProfile.findViewById(R.id.setting);
        TextView setAvatarTV = mArchiveProfile.findViewById(R.id.set_avatar);
        setAvatarBtnTV = mArchiveProfile.findViewById(R.id.set_avatar_btn);
        if (isSelf){
            statusBar.setVisibility(View.VISIBLE);
        }else {
            statusBar.setVisibility(View.GONE);
        }
        settingNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        setAvatarTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SetAvatarActivity.class);
                startActivity(intent);
            }
        });
        
        setAvatarBtnTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SetAvatarActivity.class);
                startActivity(intent);
            }
        });

        mImageSwitcher = mArchiveProfile.findViewById(R.id.image_switcher);
        mImageSwitcher.requestFocus();
        mImageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                return makeViewImpl();
            }
        });
        /*
        mImageSwitcher.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //startPicturePreview(pictureArray);
                return onTouchImpl(view, motionEvent);
                //return true;
            }
        });
        */

        navLayout = mArchiveProfile.findViewById(R.id.page_indicator);
        LinearLayout eduInfo = mArchiveProfile.findViewById(R.id.education_info);
        LinearLayout workInfo = mArchiveProfile.findViewById(R.id.work_info);
        //FlowLayout additionalInfo = mArchiveProfile.findViewById(R.id.additional_info);
        LinearLayout interactWrapper = mArchiveProfile.findViewById(R.id.interaction_wrap);
        LinearLayout myInteractStatistics = mArchiveProfile.findViewById(R.id.my_interact_statistics);
        if (isSelf) {
            interactWrapper.setVisibility(View.GONE);
            myInteractStatistics.setVisibility(View.VISIBLE);
        }

        LinearLayout livingWrap = mArchiveProfile.findViewById(R.id.living_wrap);
        LinearLayout illustrationWrap = mArchiveProfile.findViewById(R.id.illustration_wrap);
        TextView resumeDetail = mArchiveProfile.findViewById(R.id.resume_detail);
        TextView navResumeDetail = mArchiveProfile.findViewById(R.id.nav_resume);

        TextView living = livingWrap.findViewById(R.id.living);

        TextView degreeTV = eduInfo.findViewById(R.id.degree);
        TextView majorTV = eduInfo.findViewById(R.id.major);
        TextView university = eduInfo.findViewById(R.id.university);
        TextView position = workInfo.findViewById(R.id.position);
        //TextView industry = workInfo.findViewById(R.id.industry);
        //TextView hometown = additionalInfo.findViewById(R.id.hometown);
        //TextView nation = additionalInfo.findViewById(R.id.nation);
        //TextView religion = additionalInfo.findViewById(R.id.religion);
        TextView profile = mArchiveProfile.findViewById(R.id.profile);
        if (!TextUtils.isEmpty(mMeetMember.getIntroduction())){
            profile.setVisibility(View.VISIBLE);
            profile.setText(mMeetMember.getIntroduction());
        }

        TextView eyeView = mArchiveProfile.findViewById(R.id.visit_record);

        if (mMeetMember.getSituation() == 1) {
            workInfo.setVisibility(View.VISIBLE);
            eduInfo.setVisibility(View.GONE);
        }

        name.setText(mMeetMember.getNickName());

        if (!"".equals(mMeetMember.getAvatar())) {
            // Glide.with(mContext).load(HttpUtil.DOMAIN + mMeetMember.userProfile.getAvatar()).into(headUri);
            String url = HttpUtil.DOMAIN + mMeetMember.getAvatar();
            mFirstAvatarUrl = url;
            SimpleTarget<Drawable> simpleTarget = new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                    mImageSwitcher.setImageDrawable(resource);
                    drawableList.add(resource);
                }

            };

            Glide.with(getContext()).load(url).into(simpleTarget);
                        mImageSwitcher.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startPicturePreview();
                }
            });
            
        } else {
            if (mMeetMember.getSex() == 0) {
                mImageSwitcher.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.male_default_avator));
            } else {
                mImageSwitcher.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.female_default_avator));
            }
            
            if (isSelf){
                setAvatarBtnTV.setVisibility(View.VISIBLE);
            }
        }

        if (mMeetMember.getSex() == 0) {
            sex.setText(R.string.mars);
        } else {
            sex.setText(R.string.venus);
        }
        //sex.setText(String.valueOf(mMeetMember.userProfile.getSex()));
        if (!TextUtils.isEmpty(mMeetMember.getLiving())){
            livingWrap.setVisibility(View.VISIBLE);
            living.setText("现居" + mMeetMember.getLiving());
        }

        //degree.setText(mMeetMember.getDegreeName(mMeetMember.getDegree()));
        university.setText(mMeetMember.getUniversity());
        if (mMeetMember.getSituation() > 0) {
            position.setVisibility(View.VISIBLE);
            //industry.setVisibility(View.VISIBLE);
            position.setText(mMeetMember.getPosition());
            //industry.setText(mMeetMember.getIndustry());
        } else if (mMeetMember.getSituation() == 0){
            if (!TextUtils.isEmpty(mMeetMember.getMajor())){
                majorTV.setVisibility(View.VISIBLE);
                majorTV.setText(mMeetMember.getMajor());
            }

            if (!TextUtils.isEmpty(mMeetMember.getDegreeName(mMeetMember.getDegree()))){
                degreeTV.setVisibility(View.VISIBLE);
                degreeTV.setText(mMeetMember.getDegreeName(mMeetMember.getDegree()));
            }
        }

        if (mMeetMember.getVisitCount() > 0) {
            eyeView.setText(String.valueOf(mMeetMember.getVisitCount()));
        }

        resumeDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                ArchiveFragment archiveFragment = new ArchiveFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("uid", uid);
                archiveFragment.setArguments(bundle);
                getFragmentManager()    //
                        .beginTransaction()
                        .add(R.id.fragment_container, archiveFragment, "Archive")   // 此处的R.id.fragment_container是要盛放fragment的父容器
                        .addToBackStack(null)
                        .commit();

                 */
                Slog.d(TAG, "------------------------->uid: " + uid + "  authorUid: " + authorUid);
                startArchiveActivity(mContext, uid);
            }
        });

        navResumeDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                ArchiveFragment archiveFragment = new ArchiveFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("uid", uid);
                archiveFragment.setArguments(bundle);
                getFragmentManager()    //
                        .beginTransaction()
                        .add(R.id.fragment_container, archiveFragment, "Archive")   // 此处的R.id.fragment_container是要盛放fragment的父容器
                        .addToBackStack(null)
                        .commit();

                 */
                Slog.d(TAG, "------------------------->uid: " + uid);
                startArchiveActivity(mContext, uid);
            }
        });

        if (mMeetMember.getCid() > 0) {
            meet.setVisibility(View.VISIBLE);
            meet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkMyCondition();
                }
            });
        }
        
        ConstraintLayout contactsWrapper = mHeaderEvaluation.findViewById(R.id.contacts_wrapper);
        if (isSelf){
            contactsWrapper.setVisibility(View.VISIBLE);
        }
        contactsWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ContactsActivity.class);
                startActivity(intent);
            }
        });
        
        TextView contactsNavTV = mHeaderEvaluation.findViewById(R.id.contacts_nav);
        contactsNavTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ContactsActivity.class);
                startActivity(intent);
            }
        });
        
        ConstraintLayout talentsWrapper = mHeaderEvaluation.findViewById(R.id.talents_apply_wrapper);
        if (isSelf){
            talentsWrapper.setVisibility(View.VISIBLE);
        }
        talentsWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TalentIntroductionEntryDF talentIntroductionEntryDF = new TalentIntroductionEntryDF();
                talentIntroductionEntryDF.show(getFragmentManager(), "TalentIntroductionEntryDF");
            }
        });
        
        ConstraintLayout experiencesWrapper = mHeaderEvaluation.findViewById(R.id.experience_apply_wrapper);
        if (isSelf){
            experiencesWrapper.setVisibility(View.VISIBLE);
        }
        experiencesWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExperienceTalentApplyDF experienceTalentApplyDF = new ExperienceTalentApplyDF();
                experienceTalentApplyDF.show(getFragmentManager(), "ExperienceTalentApplyDF");
            }
        });
    }
    
    public void startPicturePreview(){
        Slog.d(TAG, "-------------------->pictureArray: "+pictureArray);
        List<LocalMedia> localMediaList = new ArrayList<>();
        LocalMedia localMedia = new LocalMedia();
        localMedia.setPath(mFirstAvatarUrl);
        localMediaList.add(localMedia);

        for (int i=0; i<pictureArray.length(); i++){
            localMedia = new LocalMedia();
            localMedia.setPath(HttpUtil.getDomain()+pictureArray.optJSONObject(i).optString("uri")+pictureArray.optJSONObject(i).optString("filename"));
            localMediaList.add(localMedia);
        }

        PictureSelector.create(this)
                .themeStyle(R.style.picture_default_style) // xml设置主题
                .loadImageEngine(GlideEngine.createGlideEngine())// 外部传入图片加载引擎，必传项
                .isNotPreviewDownload(true)
                .openExternalPreview(0, localMediaList);

    }

    private void checkMyCondition() {
        RequestBody requestBody = new FormBody.Builder().build();
        HttpUtil.sendOkHttpRequest(getContext(), GET_MY_CONDITION_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug)
                        Slog.d(TAG, "==========get my condition response text : " + responseText);
                    if (responseText != null) {
                        if (!TextUtils.isEmpty(responseText)) {
                            try {
                                JSONObject jsonObject = new JSONObject(responseText);
                                if (jsonObject != null) {
                                    int isConditionSet = jsonObject.optInt("condition_set");
                                    JSONObject conditionObject = jsonObject.optJSONObject("my_condition");
                                    if (isConditionSet > 0) {
                                        handler.sendEmptyMessage(MY_CONDITION_SET_DONE);
                                    } else {
                                        myProfile = ParseUtils.getUserProfileFromJSONObject(conditionObject);
                                        handler.sendEmptyMessage(MY_CONDITION_NOT_SET);
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

    private void needSetMeetCondition() {
        final AlertDialog.Builder normalDialogBuilder = new AlertDialog.Builder(getActivity());
        normalDialogBuilder.setTitle(getResources().getString(R.string.condition_set_request_title));
        normalDialogBuilder.setMessage(getResources().getString(R.string.condition_set_request_content));
        if (TextUtils.isEmpty(myProfile.getAvatar())) {
            normalDialogBuilder.setPositiveButton("去设置->",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(getContext(), SetAvatarActivity.class);
                            //startActivityForResult(intent, Activity.RESULT_FIRST_USER);
                            intent.putExtra("userProfile", myProfile);
                            intent.putExtra("look_friend", true);
                            startActivity(intent);
                        }
                    });
        } else {
            normalDialogBuilder.setTitle(getResources().getString(R.string.condition_set_request_title));
            normalDialogBuilder.setMessage(getResources().getString(R.string.condition_set_request_content));
            normalDialogBuilder.setPositiveButton("去设置->",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(getContext(), FillMeetInfoActivity.class);
                            //startActivityForResult(intent, Activity.RESULT_FIRST_USER);
                            intent.putExtra("userProfile", myProfile);
                            startActivity(intent);
                        }
                    });
        }


        normalDialogBuilder.setNegativeButton("关闭",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                    }
                });
        AlertDialog normalDialog = normalDialogBuilder.create();
        normalDialog.show();

        try {
            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object mAlertController = mAlert.get(normalDialog);
            Field mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
            mMessage.setAccessible(true);
            TextView mMessageView = (TextView) mMessage.get(mAlertController);
            mMessageView.setTextColor(getResources().getColor(R.color.background));
            mMessageView.setTextSize(16);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        normalDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.color_disabled));
        normalDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.color_blue));
        normalDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(18);
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

            chatBtn.setVisibility(View.VISIBLE);
            chatBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //chat.processChat(getActivity(), getYunXinAccount(context), mMeetMember.getInit());
                    if (chatInfo == null){
                        chatInfo = new ChatInfo();
                    }
                    chatInfo.setType(TIMConversationType.C2C);
                    chatInfo.setId(String.valueOf(mMeetMember.getUid()));
                    chatInfo.setChatName(mMeetMember.getNickName());

                    //chatInfo.setId();
                    Intent intent = new Intent(getContext(), ChatActivity.class);
                    intent.putExtra("CHAT_INFO", chatInfo);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
        }

        contactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View editView = LayoutInflater.from(context).inflate(R.layout.edit_dialog, null);
                final EditText editText = editView.findViewById(R.id.edit_text);
                new AlertDialog.Builder(getActivity())
                        .setTitle("添加好友申请")
                        .setView(editView)
                        .setPositiveButton("申请",//提示框的两个按钮
                                new android.content.DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        RequestBody requestBody = new FormBody.Builder()
                                                .add("uid", String.valueOf(uid))
                                                .add("content", editText.getText().toString()).build();

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

        dynamicsCount = mHeaderEvaluation.findViewById(R.id.dynamics_count);
        dynamicsCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, SpecificUserDynamicsActivity.class);
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
                    if (isDebug)
                        Slog.d(TAG, "==========getDynamicsCount response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        try {
                            int count = new JSONObject(responseText).optInt("count");
                            if (isDebug)
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
        TextView activityCount = mHeaderEvaluation.findViewById(R.id.dynamics_count);
        activityCount.setText(String.valueOf(count));
    }

    private void getConnectStatus() {
        //for contacts status
        getContactsStatus(mContext, handler, uid);
        //for follow status
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();

        HttpUtil.sendOkHttpRequest(mContext, GET_FOLLOW_STATUS_URL, requestBody, new Callback() {
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
                    if (isDebug) Slog.d(TAG, "==========getContacts Status : " + responseText);
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
       // LinearLayout followedCountWrap = mArchiveProfile.findViewById(R.id.followed_count_wrap);
        final TextView followedCount = mHeaderEvaluation.findViewById(R.id.followed_count);
        final TextView followingCount = mArchiveProfile.findViewById(R.id.following_count);
        followedCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("type", FOLLOWED);
                bundle.putInt("uid", uid);
                bundle.putString("title", "被关注 " + followedCount.getText());
                CommonUserListDialogFragment commonUserListDialogFragment = new CommonUserListDialogFragment();
                commonUserListDialogFragment.setArguments(bundle);
                commonUserListDialogFragment.show(getFragmentManager(), "CommonUserListDialogFragment");
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
                commonUserListDialogFragment.show(getFragmentManager(), "CommonUserListDialogFragment");
            }
        });


    }

    private void getFollowStatisticsCount() {
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(mContext, GET_FOLLOW_STATISTICS_URL, requestBody, new Callback() {
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
        HttpUtil.sendOkHttpRequest(mContext, GET_PRAISE_STATISTICS_URL, requestBody, new Callback() {
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
        final TextView praisedCount = mArchiveProfile.findViewById(R.id.praised_statistics);
        final TextView praiseCount = mArchiveProfile.findViewById(R.id.praise_count);

        praisedCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("type", PRAISED);
                bundle.putInt("uid", uid);
                bundle.putString("title", "被赞 " + praisedCount.getText());
                CommonUserListDialogFragment commonUserListDialogFragment = new CommonUserListDialogFragment();
                commonUserListDialogFragment.setArguments(bundle);
                commonUserListDialogFragment.show(getFragmentManager(), "CommonUserListDialogFragment");
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
                commonUserListDialogFragment.show(getFragmentManager(), "CommonUserListDialogFragment");
            }
        });
    }

    private void getLoveStatistics() {
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(mContext, GET_LOVE_STATISTICS_URL, requestBody, new Callback() {
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
        lovedCount = mArchiveProfile.findViewById(R.id.loved_statistics);
        //lovedCount = mArchiveProfile.findViewById(R.id.loved_count);
        final TextView loveCount = mArchiveProfile.findViewById(R.id.love_count);

        lovedCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("type", LOVED);
                bundle.putInt("uid", uid);
                bundle.putString("title", "被喜欢 " + lovedCount.getText());
                CommonUserListDialogFragment commonUserListDialogFragment = new CommonUserListDialogFragment();
                commonUserListDialogFragment.setArguments(bundle);
                commonUserListDialogFragment.show(getFragmentManager(), "CommonUserListDialogFragment");
            }
        });

        loveCountWrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("type", LOVE);
                bundle.putInt("uid", uid);
                bundle.putString("title", "喜欢的 " + loveCount.getText());
                CommonUserListDialogFragment commonUserListDialogFragment = new CommonUserListDialogFragment();
                commonUserListDialogFragment.setArguments(bundle);
                commonUserListDialogFragment.show(getFragmentManager(), "CommonUserListDialogFragment");
            }
        });

    }

    private void processFollowAction() {
        final Button followBtn = mArchiveProfile.findViewById(R.id.follow);
        if (isFollowed == true) {
            followBtn.setText("已关注");
            //followBtn.setBackground(getDrawable(R.drawable.btn_disable));
            followBtn.setTextColor(getResources().getColor(R.color.disabled));
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
                    //followBtn.setBackground(getDrawable(R.drawable.btn_default));

                } else {
                    isFollowed = true;
                    followUrl = FOLLOW_ACTION_URL + "add";
                    followBtn.setText("已关注");
                    //followBtn.setBackground(getDrawable(R.drawable.btn_disable));
                    followBtn.setTextColor(getResources().getColor(R.color.disabled));
                }

                HttpUtil.sendOkHttpRequest(mContext, followUrl, requestBody, new Callback() {

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
        TextView followedCount = mHeaderEvaluation.findViewById(R.id.followed_count);
        TextView followingCount = mArchiveProfile.findViewById(R.id.following_count);
        if (bundle.getInt("followed_count") > 0) {
            followedCount.setText(String.valueOf(bundle.getInt("followed_count")));
        }

        if (bundle.getInt("following_count") > 0) {
            followingCount.setText(String.valueOf(bundle.getInt("following_count")));
        }
    }

    private void setPraiseStatistics(Bundle bundle) {

        TextView praisedCount = mArchiveProfile.findViewById(R.id.praised_statistics);
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
        lovedCount = mArchiveProfile.findViewById(R.id.loved_statistics);
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
        if (isLoved) {
            lovedIcon.setText(getResources().getText(R.string.fa_heart));
        } else {
            lovedIcon.setText(getResources().getText(R.string.fa_heart_o));
        }
        final RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();

        lovedIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = LOVE_ADD_URL;
                if (isLoved){
                    url = LOVE_CANCEL_URL;
                }
                HttpUtil.sendOkHttpRequest(mContext, url, requestBody, new Callback() {
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

                if (isLoved){
                    isLoved = false;
                }else {
                    isLoved = true;
                }
                handler.sendEmptyMessage(UPDATE_LOVED_COUNT);
            }
        });
    }

    private void processPraiseAction() {
        praisedIcon = mArchiveProfile.findViewById(R.id.praised_icon);
        praisedCount = mArchiveProfile.findViewById(R.id.praised_statistics);
        if (isPraised) {
            praisedIcon.setText(getResources().getText(R.string.fa_thumbs_up));
        } else {
            praisedIcon.setText(getResources().getText(R.string.fa_thumbs_O_up));
        }

        final RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        praisedIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = PRAISE_ADD_URL;
                if (isPraised){
                    url = PRAISE_CANCEL_URL;
                }
                HttpUtil.sendOkHttpRequest(mContext, url, requestBody, new Callback() {
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

                if (isPraised){
                    isPraised = false;
                }else {
                    isPraised = true;
                }

                handler.sendEmptyMessage(UPDATE_PRAISED_COUNT);
            }
        });

        /*
        praisedCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpUtil.sendOkHttpRequest(mContext, PRAISE_ADD_URL, requestBody, new Callback() {
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
        */

    }

    private void setReferenceView(List<MeetReferenceInfo> meetReferenceInfoList) {

        RecyclerView recyclerView = mHeaderEvaluation.findViewById(R.id.reference_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mMeetReferenceAdapter = new MeetReferenceAdapter(mContext);
        mMeetReferenceAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                ReferenceWriteDialogFragment referenceWriteDialogFragment = new ReferenceWriteDialogFragment();
                referenceWriteDialogFragment.setTargetFragment(MeetArchiveFragment.this, REQUESTCODE);
                Bundle bundle = new Bundle();
                bundle.putBoolean("modify", true);
                bundle.putInt("uid", uid);
                bundle.putInt("rid", mReferenceList.get(position).getRid());
                bundle.putString("relation", mReferenceList.get(position).getRelation());
                bundle.putString("content", mReferenceList.get(position).getContent());
                referenceWriteDialogFragment.setArguments(bundle);
                referenceWriteDialogFragment.show(getFragmentManager(), "ReferenceWriteDialogFragment");
            }
        });
        recyclerView.setAdapter(mMeetReferenceAdapter);

        if (meetReferenceInfoList.size() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
        }

        Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mHeaderEvaluation.findViewById(R.id.meet_item_id), font);

    }

    private void setScoreView(float average){
        TextView ratingAverageTV = mHeaderEvaluation.findViewById(R.id.chram_synthesized_results);
        ratingAverageTV.setText(average + "分");

        ScaleRatingBar scaleRatingBarCount = mHeaderEvaluation.findViewById(R.id.charm_synthesized_rating);
        scaleRatingBarCount.setRating(average);
    }

    private void setRatingBarView() {
        JSONArray ratingArray = mRatingObj.optJSONArray("rating");
        TextView ratingMemberCount = mHeaderEvaluation.findViewById(R.id.rating_member_count);
        if (ratingArray != null && ratingArray.length() > 0) {
            evaluatorDetails.setVisibility(View.VISIBLE);
            ratingMemberCount.setText(ratingArray.length() + "人评价");
            float ratingCount = 0;
            int nonZeroCount = 0;
            for (int i = 0; i < ratingArray.length(); i++) {
                JSONObject ratingObj = ratingArray.optJSONObject(i);
                if (mRatingObj.optInt("visitor_uid") == ratingObj.optInt("evaluator_uid")) {
                    ratingBarWrapper.setVisibility(View.GONE);
                }
                if (ratingObj.optDouble("rating") > 0){
                    ratingCount += ratingObj.optDouble("rating");
                    nonZeroCount++;
                }
            }

            if (nonZeroCount != 0){
                float ratingAverage = ratingCount / nonZeroCount;
                BigDecimal b = new BigDecimal(ratingAverage);
                ratingAverageRoundUp = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
                setScoreView(ratingAverageRoundUp);
            }
        } else {
            ratingMemberCount.setText("暂无评价");
        }
    }

    private void loadRating() {
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(mContext, GET_RATING_URL, requestBody, new Callback() {
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
                if (isDebug) Slog.d(TAG, "onRatingChange float: " + rating);
                ratingScoreView.setText(String.valueOf(rating));
            }

        });

        scaleRatingBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    EvaluateDialogFragment evaluateDialogFragment = new EvaluateDialogFragment();
                    evaluateDialogFragment.setTargetFragment(MeetArchiveFragment.this, REQUESTCODE);
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
                    evaluateDialogFragment.show(getFragmentManager(), "EvaluateDialogFragment");
                }
                return false;
            }
        });

        evaluatorDetails = mHeaderEvaluation.findViewById(R.id.rating_member_details);
        evaluatorDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, EvaluatorDetailsActivity.class);
                intent.putExtra("uid", uid);
                intent.putExtra("scores", ratingAverageRoundUp);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });
    }

    private void loadImpressionStatistics() {

        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(mContext, GET_IMPRESSION_STATISTICS_URL, requestBody, new Callback() {
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
        impressionStatisticsWrap.setLayoutManager(new LinearLayoutManager(mContext));
        mMeetImpressionStatisticsAdapter = new MeetImpressionStatisticsAdapter(mContext, getFragmentManager());
        impressionStatisticsWrap.setAdapter(mMeetImpressionStatisticsAdapter);

        mMeetImpressionStatisticsAdapter.setItemClickListener(new MeetImpressionStatisticsAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(mContext, ApprovedUsersActivity.class);
                intent.putExtra("uid", uid);
                intent.putExtra("impressionStatistics", mImpressionStatisticsList.get(position));
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivityForResult(intent, REQUESTCODE);
            }
        });
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
        Response response = HttpUtil.sendOkHttpRequestSync(mContext, GET_IMPRESSION_USERS_URL, requestBody, null);

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
                        userMeetInfo.setNickName(member.optString("nickname"));
                        userMeetInfo.setAvatar(member.optString("avatar"));
                        userMeetInfo.setSituation(member.optInt("situation"));
                        if (member.optInt("situation") == 0) {//student
                            userMeetInfo.setDegree(member.optString("degree"));
                            //userMeetInfo.setMajor(member.optString("major"));
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
                    invitationDialogFragment.setTargetFragment(MeetArchiveFragment.this, REQUESTCODE);
                    Bundle bundle = new Bundle();
                    bundle.putInt("uid", uid);
                    bundle.putInt("type", ParseUtils.TYPE_REFERENCE);

                    invitationDialogFragment.setArguments(bundle);
                    invitationDialogFragment.show(getFragmentManager(), "InvitationDialogFragment");
                }
            });
        } else {
            writeReferenceWrapper.setVisibility(View.VISIBLE);

            TextView writeReference = mHeaderEvaluation.findViewById(R.id.write_reference);
            writeReference.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ReferenceWriteDialogFragment referenceWriteDialogFragment = new ReferenceWriteDialogFragment();
                    referenceWriteDialogFragment.setTargetFragment(MeetArchiveFragment.this, REQUESTCODE);
                    Bundle bundle = new Bundle();
                    bundle.putInt("uid", uid);
                    bundle.putString("name", mMeetMember.getNickName());

                    referenceWriteDialogFragment.setArguments(bundle);
                    referenceWriteDialogFragment.show(getFragmentManager(), "ReferenceWriteDialogFragment");
                }
            });

        }
    }

    private void loadReferences(int uid) {
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(mContext, LOAD_REFERENCE_URL, requestBody, new Callback() {
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
        HttpUtil.sendOkHttpRequest(mContext, GET_PERSONALITY_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug)
                    Slog.d(TAG, "================getPersonalityDetail response:" + responseText);
                if (responseText != null && !TextUtils.isEmpty(responseText)) {
                    try {
                        personalityResponseArray = new JSONObject(responseText).optJSONArray("personality_detail");
                        if (personalityResponseArray != null && personalityResponseArray.length() > 0) {
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
        layoutParams.setMargins((int) Utility.dpToPx(mContext, 3), (int) Utility.dpToPx(mContext, 8),
                (int) Utility.dpToPx(mContext, 8), (int) Utility.dpToPx(mContext, 3));
        for (int i = 0; i < personalityResponseArray.length(); i++) {
            final TextView personality = new TextView(mContext);
            personality.setPadding((int) Utility.dpToPx(mContext, 8), (int) Utility.dpToPx(mContext, 6),
                    (int) Utility.dpToPx(mContext, 8), (int) Utility.dpToPx(mContext, 6));
            personality.setBackground(getDrawable(R.drawable.btn_big_radius_primary));
            personality.setTextColor(getResources().getColor(R.color.white));
            personality.setLayoutParams(layoutParams);
            final String personalityName = personalityResponseArray.optJSONObject(i).optString("personality");
            final int count = personalityResponseArray.optJSONObject(i).optInt("count");
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
                    commonUserListDialogFragment.setTargetFragment(MeetArchiveFragment.this, REQUESTCODE);
                    Bundle bundle = new Bundle();
                    bundle.putInt("pid", pid);
                    bundle.putString("personality", personalityName);
                    bundle.putInt("count", count);
                    commonUserListDialogFragment.setArguments(bundle);
                    commonUserListDialogFragment.show(getFragmentManager(), "CommonUserListDialogFragment");
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
                personalityEditDialogFragment.setTargetFragment(MeetArchiveFragment.this, REQUESTCODE);
                Bundle bundle = new Bundle();
                bundle.putInt("uid", uid);
                bundle.putInt("type", ParseUtils.TYPE_PERSONALITY);
                personalityEditDialogFragment.setArguments(bundle);
                personalityEditDialogFragment.show(getFragmentManager(), "PersonalityEditDialogFragment");
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
        HttpUtil.sendOkHttpRequest(mContext, GET_CHEERING_GROUP_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    boolean hasJoined = false;
                    String responseText = response.body().string();
                    if (isDebug)
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
if(getActivity() != null){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                addMember.setVisibility(View.VISIBLE);
            }
        });
}

        addMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InvitationDialogFragment invitationDialogFragment = new InvitationDialogFragment();
                invitationDialogFragment.setTargetFragment(MeetArchiveFragment.this, REQUESTCODE);
                Bundle bundle = new Bundle();
                bundle.putInt("uid", uid);
                bundle.putInt("type", ParseUtils.TYPE_CHEERING_GROUP);
                invitationDialogFragment.setArguments(bundle);
                invitationDialogFragment.show(getFragmentManager(), "InvitationDialogFragment");
            }
        });
    }

    private void joinCheeringGroup() {
        final TextView join = mHeaderEvaluation.findViewById(R.id.join_cheering_group);
        
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    join.setVisibility(View.VISIBLE);
                }
            });
        }

        join.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                join.setVisibility(View.GONE);
                showProgressDialog(getActivity(), "正在加入");
                RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
                HttpUtil.sendOkHttpRequest(mContext, JOIN_CHEERING_GROUP_URL, requestBody, new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseText = response.body().string();
                        if (isDebug)
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
        mCheeringGroupAdapter = new CheeringGroupAdapter(mContext, mWidth);
        mCheeringGroupAdapter.setCheeringGroupList(mCheeringGroupMemberList);
        cheeringGroupList.setAdapter(mCheeringGroupAdapter);
        cheeringGroupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ParseUtils.startMeetArchiveActivity(mContext, mCheeringGroupMemberList.get(position).getUid());
            }
        });
    }

    private void getHobbies() {
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .build();
        HttpUtil.sendOkHttpRequest(mContext, LOAD_HOBBY_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug) Slog.d(TAG, "================get hobbies response:" + responseText);
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
                personalityEditDialogFragment.setTargetFragment(MeetArchiveFragment.this, REQUESTCODE);
                Bundle bundle = new Bundle();
                bundle.putInt("uid", uid);
                bundle.putInt("type", ParseUtils.TYPE_HOBBY);
                personalityEditDialogFragment.setArguments(bundle);
                personalityEditDialogFragment.show(getFragmentManager(), "PersonalityEditDialogFragment");

            }
        });
    }

    private void setHobbyFlow(String[] hobbyArray) {
        FlowLayout hobbyFlow = mHeaderEvaluation.findViewById(R.id.hobby_flow);
        hobbyFlow.removeAllViews();

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins((int) Utility.dpToPx(mContext, 3), (int) Utility.dpToPx(mContext, 3),
                (int) Utility.dpToPx(mContext, 3), (int) Utility.dpToPx(mContext, 3));
        for (int i = 0; i < hobbyArray.length; i++) {
            if (!hobbyArray[i].isEmpty()) {
                final TextView hobbyView = new TextView(mContext);
                hobbyView.setPadding((int) Utility.dpToPx(mContext, 8), (int) Utility.dpToPx(mContext, 6),
                        (int) Utility.dpToPx(mContext, 8), (int) Utility.dpToPx(mContext, 6));
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
        if (isDebug) Slog.d(TAG, "--------------------->onBackFromDialog type: " + type);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int type = (data == null ? -1 : data.getIntExtra("type", -1));
        boolean status = (data == null ? false : data.getBooleanExtra("status", false));
        if (isDebug)
            Slog.d(TAG, "------------------->onActivityResult type: " + type + " status: " + status);
        if (requestCode == REQUESTCODE) {
            if (resultCode == RESULT_OK) {
                boolean hasApproved = data.getBooleanExtra("approved", false);
                if (isDebug) Slog.d(TAG, "----------------->approved: " + hasApproved);
                if (hasApproved) {
                    loadImpressionStatistics();
                }
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
        }
    }

    private void updateLovedCount() {
        lovedStatistics = mArchiveProfile.findViewById(R.id.loved_statistics);
        //lovedCount = mArchiveProfile.findViewById(R.id.loved_count);
        int newLovedStatistics = 0;
        int newLovedCount = 0;

        if (isLoved){//add
            if (lovedStatistics.getText().toString().length() == 0) {
                newLovedStatistics = 1;
            } else {
                newLovedStatistics = Integer.parseInt(lovedStatistics.getText().toString()) + 1;
            }

            if (lovedCount.getText().toString().length() == 0) {
                newLovedCount = 1;
            } else {
                newLovedCount = Integer.parseInt(lovedCount.getText().toString()) + 1;
            }
            lovedCount.setText(String.valueOf(newLovedCount));
            lovedIcon.setText(getResources().getText(R.string.fa_heart));
        }else {//cancel
            newLovedStatistics = Integer.parseInt(lovedStatistics.getText().toString()) - 1;
            newLovedCount = Integer.parseInt(lovedCount.getText().toString()) - 1;
            if (newLovedCount == 0){
                lovedCount.setText("");
            }else {
                lovedCount.setText(String.valueOf(newLovedCount));
            }
            lovedIcon.setText(getResources().getText(R.string.fa_heart_o));
        }

        if (newLovedStatistics == 0){
            lovedStatistics.setText("");
        }else {
            lovedStatistics.setText(String.valueOf(newLovedStatistics));
        }

    }

    private void updatePraisedCount() {
        TextView praisedStatistics = mArchiveProfile.findViewById(R.id.praised_statistics);
        //TextView praisedCount = mArchiveProfile.findViewById(R.id.praised_count);
        int newPraisedStatistics = 0;
        int newPraisedCount = 0;
        if (isPraised){//add praise
            if (praisedStatistics.getText().toString().length() == 0) {
                newPraisedStatistics = 1;
            } else {
                newPraisedStatistics = Integer.parseInt(praisedStatistics.getText().toString()) + 1;
            }
            praisedIcon.setText(getResources().getText(R.string.fa_thumbs_up));

            if (praisedCount.getText().toString().length() == 0) {
                newPraisedCount = 1;
            } else {
                newPraisedCount = Integer.parseInt(praisedCount.getText().toString()) + 1;
            }

            praisedCount.setText(String.valueOf(newPraisedCount));

        }else {//cancel praise
            newPraisedStatistics = Integer.parseInt(praisedStatistics.getText().toString()) - 1;
            praisedIcon.setText(getResources().getText(R.string.fa_thumbs_O_up));

            newPraisedCount = Integer.parseInt(praisedCount.getText().toString()) - 1;
            if (newPraisedCount == 0){
                praisedCount.setText("");
            }else {
                praisedCount.setText(String.valueOf(newPraisedCount));
            }
        }

        if (newPraisedStatistics == 0){
            praisedStatistics.setText("");
        }else {
            praisedStatistics.setText(String.valueOf(newPraisedStatistics));
        }
    }

    public void processVisitRecord() {
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();

        //Add visit record
        HttpUtil.sendOkHttpRequest(mContext, ADD_VISIT_RECORD_URL, requestBody, new Callback() {
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
        HttpUtil.sendOkHttpRequest(mContext, GET_VISIT_RECORD_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug)
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
                    //uid = getContext().getIntent().getIntExtra("uid", -1);
                    getMeetArchive(getContext(), uid);
                } else {
                    uid = mMeetMember.getUid();
                    processSubModules();
                }
                break;

            case GET_MEET_ARCHIVE_DONE:
                processSubModules();
                break;
            case GET_PICTURES_URL_DONE:
                if (pictureArray != null && pictureArray.length() > 0) {
                    for (int i = 0; i < pictureArray.length(); i++) {
                        JSONObject pictureObj = pictureArray.optJSONObject(i);
                        final String url = HttpUtil.DOMAIN + pictureObj.optString("uri") + pictureObj.optString("filename");
                        //Slog.d(TAG, "****************url: "+url);
                        SimpleTarget<Drawable> simpleTarget = new SimpleTarget<Drawable>() {
                            @Override
                            public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                                // imageView.setImageDrawable(resource);
                                drawableList.add(resource);
                                //if (isDebug)
                                Slog.d(TAG, "****************pictureArray length: " + pictureArray.length() + " drawableList size: " + drawableList.size());
                                if (pictureArray.length() == drawableList.size() - 1) {
                                    //add indicator
                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                    for (int i = 0; i < pictureArray.length() + 1; i++) {
                                        TextView textView = new TextView(getContext());
                                        if (i != 0) {
                                            layoutParams.setMarginStart(16);
                                            textView.setText(R.string.circle_o);
                                        } else {
                                            textView.setText(R.string.circle);
                                        }
                                        textView.setTextColor(getResources().getColor(R.color.white));
                                        textView.setLayoutParams(layoutParams);
                                        textView.setTextSize(12);

                                        navLayout.addView(textView);
                                    }

                                    Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
                                    FontManager.markAsIconContainer(viewContent.findViewById(R.id.meet_archive_profile), font);
                                }
                            }
                        };

                        Glide.with(getContext()).load(url).into(simpleTarget);
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
                processContactsAction(getContext(), handler, contactStatus, contacts, follow, chat);
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
            case MY_CONDITION_SET_DONE:
                bundle.putSerializable("meet_condition", mMeetMember);
                MeetConditionDialogFragment meetConditionDialogFragment = new MeetConditionDialogFragment();
                meetConditionDialogFragment.setArguments(bundle);
                meetConditionDialogFragment.show(getFragmentManager(), "MeetConditionDialogFragment");
                break;
            case MY_CONDITION_NOT_SET:
                needSetMeetCondition();
                break;
            case LOAD_MY_TALENTS_DONE:
                setMyTalentSizeView();
                break;
            case LOAD_MY_EXPERIENCES_DONE:
                setMyExperienceSizeView();
                break;
            case LOAD_MY_GUIDE_COUNT_DONE:
                setMyGuideSizeView();
                break;
            case LOAD_MY_CONSULT_COUNT_DONE:
                setMyConsultView();
                break;
            case HAS_REQUEST_OR_APPLY:
                newApplyCountView = mHeaderEvaluation.findViewById(R.id.count);
                if (newApplyCount > 0) {
                    newApplyCountView.setText("+" + String.valueOf(newApplyCount));
                } else {
                    newApplyCountView.setText("");
                }

                ReminderManager.getInstance().updateNewContactsApplied(newApplyCount);
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

    public View makeViewImpl() {
        Resources resources = mContext.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        float density = dm.density;
        int width = dm.widthPixels;
        mWidth = width;
        int screenHeight = width;
        final RoundImageView i = new RoundImageView(getContext());
        //i.setBackgroundColor(0xff000000);
        i.setScaleType(ImageView.ScaleType.CENTER_CROP);
        //i.setLayoutParams(new ImageSwitcher.LayoutParams(width, screenHeight));
        i.setLayoutParams(new ImageSwitcher.LayoutParams(Utility.dpToPx(getContext(), 180), Utility.dpToPx(getContext(), 180)));
        return i;
    }

    public boolean onTouchImpl(View view, MotionEvent event) {
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
                    //Toast.makeText(mContext, "向" + action + "滑动", Toast.LENGTH_SHORT).show();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (direction == 0) {
                    if (currentPosition < drawableList.size() - 1) {
                        mImageSwitcher.setInAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.picture_anim_fade_in));
                        mImageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.picture_anim_fade_out));
                        currentPosition++;
                        mImageSwitcher.setImageDrawable(drawableList.get(currentPosition));
                        //Toast.makeText(getApplication(), "currentPosition: "+currentPosition, Toast.LENGTH_SHORT).show();
                        setPictureIndicator(currentPosition, direction);
                    }

                } else if (direction == 1) {
                    if (currentPosition > 0) {
                        //设置动画，这里的动画比较简单，不明白的去网上看看相关内容
                        mImageSwitcher.setInAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.picture_anim_fade_in));
                        mImageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.picture_anim_fade_out));
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

    private class AvatarAddBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Slog.d(TAG, "------------------------>AvatarAddBroadcastReceiver");
            switch (intent.getAction()) {
                case AVATAR_SET_ACTION_BROADCAST:
                    String avatar = intent.getStringExtra("avatar");
                    String url = HttpUtil.DOMAIN + avatar;
                    SimpleTarget<Drawable> simpleTarget = new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                            mImageSwitcher.setImageDrawable(resource);
                            drawableList.add(resource);
                        }
                    };

                    if (drawableList.size() > 0) {
                        //Glide.with(getContext()).load(url).into(simpleTarget);
                        mImageSwitcher.setImageDrawable(null);
                        if (drawableList.size() == 1) {// only has one avatar
                            drawableList.clear();
                            Glide.with(getContext()).load(url).into(simpleTarget);
                        } else {
                            //mImageSwitcher.removeAllViews();
                            drawableList.clear();
                            Glide.with(getContext()).load(url).into(simpleTarget);
                            loadProfilePictures();
                        }
                    } else {
                        Glide.with(getContext()).load(url).into(simpleTarget);
                    }

                    //update tuikit chat avatar
                    setTuiKitProfile();
                    setAvatarBtnTV.setVisibility(View.GONE);
                    break;
                case EVALUATE_MODIFY_ACTION_BROADCAST:
                    float score = intent.getFloatExtra("score", 0);
                    setScoreView(score);
                    ratingAverageRoundUp = score;
                    break;
                default:
                    break;
            }
        }
    }

    private void registerLocalBroadcast() {
        mReceiver = new AvatarAddBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AVATAR_SET_ACTION_BROADCAST);
        intentFilter.addAction(EVALUATE_MODIFY_ACTION_BROADCAST);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, intentFilter);
    }

    //unregister local broadcast
    private void unRegisterLoginBroadcast() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
    }
    
    private void getApplyAndRequestCount(){
        RequestBody requestBody = new FormBody.Builder().build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_APPLY_AND_REQUEST_COUNT, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null) {
                    String responseText = response.body().string();
                    try {
                        JSONObject responseObject = new JSONObject(responseText);
                        newApplyCount = responseObject.optInt("newApplyCount");

                        if (newApplyCount > 0){
                            handler.sendEmptyMessage(HAS_REQUEST_OR_APPLY);
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

    //未读消息数量观察者实现
    @Override
    public void onUnreadNumChanged(int unReadCount) {  }

    @Override
    public void onNotificationUnreadChanged(int unReadCount) { }
    
    @Override
    public void onNewContactsApplied(int appliedCount) {
        Slog.d(TAG, "------------------->onNewContactsApplied: " + appliedCount);
        if(newApplyCountView == null){
            return;
        }
        
        newApplyCountView.setText(String.valueOf(appliedCount));
        if (appliedCount > 0) {
            if (newApplyCountView.getVisibility() == View.GONE) {
                newApplyCountView.setVisibility(View.VISIBLE);
            }
        } else {
            if (newApplyCountView.getVisibility() == View.VISIBLE) {
                newApplyCountView.setVisibility(View.GONE);
            }
        }
    }

    static class MyHandler extends HandlerTemp<MeetArchiveFragment> {
        public MyHandler(MeetArchiveFragment cls) {
            super(cls);
        }

        @Override
        public void handleMessage(Message message) {
            MeetArchiveFragment meetArchiveFragment = ref.get();
            if (meetArchiveFragment != null) {
                meetArchiveFragment.handleMessage(message);
            }
        }
    }
    
    /**
     * 注册未读消息数量观察者
     */
    private void registerMsgUnreadInfoObserver(boolean register) {
        if (register) {
            ReminderManager.getInstance().registerUnreadNumChangedCallback(this);
        } else {
            ReminderManager.getInstance().unregisterUnreadNumChangedCallback(this);
        }
    }

    @Override
    protected void initView(View view) {
    }

    @Override
    protected void loadData() {
    }

    @Override
    protected int getLayoutId() {
        return 0;
    }
}
