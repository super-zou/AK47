package com.tongmenhui.launchak47.meet;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tongmenhui.launchak47.util.ReferenceWriteDialogFragment;

import com.android.volley.RequestQueue;
import com.bumptech.glide.Glide;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.nex3z.flowlayout.FlowLayout;
import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.adapter.ArchivesListAdapter;
import com.tongmenhui.launchak47.adapter.MeetImpressionStatisticsAdapter;
import com.tongmenhui.launchak47.adapter.MeetReferenceAdapter;
import com.tongmenhui.launchak47.main.BaseAppCompatActivity;
import com.tongmenhui.launchak47.util.CommonUserListDialogFragment;
import com.tongmenhui.launchak47.util.FontManager;
import com.tongmenhui.launchak47.util.HttpUtil;
import com.tongmenhui.launchak47.util.InvitationDialogFragment;
import com.tongmenhui.launchak47.util.ParseUtils;
import com.tongmenhui.launchak47.util.PersonalityEditDialogFragment;
import com.tongmenhui.launchak47.util.RequestQueueSingleton;
import com.tongmenhui.launchak47.util.Slog;
import com.tongmenhui.launchak47.util.Utility;
import com.willy.ratingbar.ScaleRatingBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class ArchivesActivity extends BaseAppCompatActivity implements EvaluateDialogFragment.EvaluateDialogFragmentListener {
    private static final String TAG = "ArchivesActivity";
    private static final boolean isDebug = false;
    private static final String DYNAMICS_URL = HttpUtil.DOMAIN + "?q=meet/activity/get";
    private static final String COMMENT_URL = HttpUtil.DOMAIN + "?q=meet/activity/interact/get";
    private static final String LOAD_REFERENCE_URL = HttpUtil.DOMAIN + "?q=meet/reference/load";
    private static final String GET_RATING_URL = HttpUtil.DOMAIN + "?q=meet/rating/get";
    private static final String GET_IMPRESSION_STATISTICS_URL = HttpUtil.DOMAIN + "?q=meet/impression/statistics";
    private static final String GET_IMPRESSION_USERS_URL = HttpUtil.DOMAIN + "?q=meet/impression/users";
    private static final String GET_PERSONALITY_URL = HttpUtil.DOMAIN + "?q=meet/personality/get";
    private static final String LOAD_HOBBY_URL = HttpUtil.DOMAIN + "?q=personal_archive/hobby/load";
    private static final String CONTACTS_ADD_URL = HttpUtil.DOMAIN + "?q=contacts/add_contacts";
    private static final String GET_CONTACTS_STATUS_URL = HttpUtil.DOMAIN + "?q=contacts/status";
    private static final String FOLLOW_ACTION_URL = HttpUtil.DOMAIN + "?q=follow/action/";
    private static final String GET_FOLLOW_STATISTICS_URL = HttpUtil.DOMAIN + "?q=follow/statistics";
    private static final String GET_FOLLOW_STATUS_URL = HttpUtil.DOMAIN + "?q=follow/isFollowed";
    private static final String GET_LOVE_STATISTICS_URL = HttpUtil.DOMAIN + "?q=meet/love/statistics";
    private static final String GET_PRAISE_STATISTICS_URL = HttpUtil.DOMAIN + "?q=meet/praise/statistics";
    private static final String LOVE_ADD_URL = HttpUtil.DOMAIN + "?q=meet/love/add";
    private static final String PRAISE_ADD_URL = HttpUtil.DOMAIN + "?q=meet/praise/add";
    private static final int DONE = 1;
    private static final int UPDATE = 2;
    private static final int UPDATE_COMMENT = 3;
    private static final int LOAD_RATING_DONE = 4;
    private static final int LOAD_IMPRESSION_DONE = 5;
    private static final int LOAD_REFERENCE_DONE = 6;
    private static final int LOAD_PERSONALITY_DONE = 7;
    private static final int LOAD_HOBBY_DONE = 8;
    private static final int GET_FOLLOW_DONE = 9;
    private static final int GET_CONTACTS_STATUS_DONE = 10;
    private static final int GET_FOLLOW_STATISTICS_URL_DONE = 11;
    private static final int GET_PRAISE_STATISTICS_URL_DONE = 12;
    private static final int GET_LOVE_STATISTICS_URL_DONE = 13;
    private static final int UPDATE_LOVED_COUNT = 14;
    private static final int UPDATE_PRAISED_COUNT = 15;
    private static final int FOLLOWED = 1;
    private static final int FOLLOWING = 2;
    private static final int PRAISED = 3;
    private static final int PRAISE = 4;
    private static final int LOVED = 5;
    private static final int LOVE = 6;
    private static final int PAGE_SIZE = 6;
    private static final int TYPE_HOBBY = 0;
    private static final int TYPE_PERSONALITY = 1;
    private static final int APPLIED = 0;
    private static final int ESTABLISHED = 1;
    private static final int APPROVE = 2;
    public String impression;
    public int impressionCount;
    public List<MeetMemberInfo> meetMemberList;
    View mHeaderEvaluation;
    private List<MeetDynamics> mMeetList = new ArrayList<>();
    private List<ImpressionStatistics> mImpressionStatisticsList = new ArrayList<>();
    private List<MeetReferenceInfo> mReferenceList = new ArrayList<>();
    private Handler handler;
    private boolean isLoved = false;
    private boolean isPraised = false;
    private boolean isFollowed = false;
    private int contactStatus = 0;
    private int mTempSize;
    private View mArchiveProfile;
    private XRecyclerView mXRecyclerView;
    private ArchivesListAdapter mArchivesListAdapter;
    private MeetReferenceAdapter mMeetReferenceAdapter;
    private MeetImpressionStatisticsAdapter mMeetImpressionStatisticsAdapter;
    private TextView mEmptyView;
    private JSONObject mRatingObj;
    private EvaluateDialogFragment evaluateDialogFragment;

    private ImageView backLeft;
    private MeetMemberInfo mMeetMember;
    private JSONArray personalityResponseArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.archives);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mMeetMember = (MeetMemberInfo) getIntent().getSerializableExtra("meet");
        final int uid = mMeetMember.getUid();

        initView();

        setArchiveProfile();

        loadRating(uid);

        loadImpressionStatistics(uid);

        processReferences(uid);

        processPersonality(uid);

        processHobby(uid);

        loadDynamicsData(uid);

        LinearLayout scaleRatingBar = mHeaderEvaluation.findViewById(R.id.charm_rating_bar);

        scaleRatingBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
                bundle.putInt("uid", mMeetMember.getUid());
                bundle.putInt("sex", mMeetMember.getSex());
                evaluateDialogFragment.setArguments(bundle);
                //ratingAndImpressionDialogFragment.show(ft, "EvaluateDialogFragment");
                evaluateDialogFragment.show(ft, "EvaluateDialogFragment");

            }
        });

        TextView evaluatorDetails = mHeaderEvaluation.findViewById(R.id.rating_member_details);
        evaluatorDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ArchivesActivity.this, EvaluatorDetailsActivity.class);
                intent.putExtra("uid", mMeetMember.getUid());
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });

    }

    private void initView() {
        backLeft = findViewById(R.id.left_back);
        backLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        mArchivesListAdapter = new ArchivesListAdapter(this);
        mXRecyclerView = (XRecyclerView) findViewById(R.id.recyclerview);
        mEmptyView = (TextView) findViewById(R.id.empty_text);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mXRecyclerView.setLayoutManager(linearLayoutManager);
        mXRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                mArchivesListAdapter.notifyDataSetChanged();
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mXRecyclerView.setLayoutManager(linearLayoutManager);

        mXRecyclerView.setRefreshProgressStyle(BallSpinFadeLoader);
        mXRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallRotate);
        //mRecyclerView.setArrowImageView(R.drawable.);

        mXRecyclerView.getDefaultRefreshHeaderView().setRefreshTimeVisible(true);
        mXRecyclerView.setPullRefreshEnabled(false);

        mXRecyclerView.getDefaultRefreshHeaderView().setRefreshTimeVisible(true);
        mArchiveProfile = LayoutInflater.from(this).inflate(R.layout.meet_archive_profile, (ViewGroup) findViewById(android.R.id.content), false);
        mXRecyclerView.addHeaderView(mArchiveProfile);
        FontManager.markAsIconContainer(mArchiveProfile.findViewById(R.id.meet_archive_profile), font);

        mHeaderEvaluation = LayoutInflater.from(this).inflate(R.layout.friends_relatives_reference, (ViewGroup) findViewById(android.R.id.content), false);
        mXRecyclerView.addHeaderView(mHeaderEvaluation);
        FontManager.markAsIconContainer(mHeaderEvaluation.findViewById(R.id.friends_relatives_reference), font);

        mXRecyclerView.getDefaultFootView().setLoadingHint(getString(R.string.loading_pull_up_tip));
        mXRecyclerView.getDefaultFootView().setNoMoreHint(getString(R.string.loading_no_more));
        final int itemLimit = 5;
        // When the item number of the screen number is list.size-2,we call the onLoadMore
        mXRecyclerView.setLimitNumberToCallLoadMore(4);
        mXRecyclerView.setRefreshProgressStyle(ProgressStyle.BallBeat);
        mXRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);
        mXRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
//                updateData();
            }

            @Override
            public void onLoadMore() {
                loadDynamicsData(mMeetMember.getUid());
            }
        });

        mXRecyclerView.setAdapter(mArchivesListAdapter);
    }

    private void setEvaluatedMode() {
        LinearLayout charmRatingBar = mHeaderEvaluation.findViewById(R.id.charm_rating_bar);
        TextView notice = mHeaderEvaluation.findViewById(R.id.notice);
        charmRatingBar.setVisibility(View.GONE);
        notice.setVisibility(View.GONE);
    }

    private void setArchiveProfile() {

        setMeetProfile();
        getConnectStatus();
        getFollowStatistics();
        getPraiseStatistics();
        getLoveStatistics();

        processChatAction(mMeetMember.getUid());

    }

    private void setMeetProfile() {
        TextView realname = mArchiveProfile.findViewById(R.id.name);
        ImageView headUri = (ImageView) mArchiveProfile.findViewById(R.id.recommend_head_uri);
        LinearLayout baseProfile = mArchiveProfile.findViewById(R.id.base_profile);
        LinearLayout education = mArchiveProfile.findViewById(R.id.education);
        LinearLayout work = mArchiveProfile.findViewById(R.id.work);
        TextView age = baseProfile.findViewById(R.id.age);
        TextView height = baseProfile.findViewById(R.id.height);
        TextView sex = baseProfile.findViewById(R.id.sex);
        TextView lives = baseProfile.findViewById(R.id.lives);

        TextView degree = education.findViewById(R.id.degree);
        TextView major = education.findViewById(R.id.major);
        TextView university = education.findViewById(R.id.university);
        TextView job = work.findViewById(R.id.job);
        TextView company = work.findViewById(R.id.company);

        TextView ageRequirement = mArchiveProfile.findViewById(R.id.age_require);
        TextView heightRequirement = mArchiveProfile.findViewById(R.id.height_require);
        TextView degreeRequirement = mArchiveProfile.findViewById(R.id.degree_require);
        TextView livesRequirement = mArchiveProfile.findViewById(R.id.lives_require);
        TextView sexRequirement = mArchiveProfile.findViewById(R.id.sex_require);

        TextView illustration = mArchiveProfile.findViewById(R.id.illustration);
        TextView eyeView = mArchiveProfile.findViewById(R.id.eye_statistics);
        // TextView lovedView = mArchiveProfile.findViewById(R.id.loved_statistics);
        //TextView lovedIcon = mArchiveProfile.findViewById(R.id.loved_icon);
        // TextView thumbsView = mArchiveProfile.findViewById(R.id.thumbs_up_statistics);
        // TextView thumbsIcon = mArchiveProfile.findViewById(R.id.thumbs_up_icon);
        TextView photosView = mArchiveProfile.findViewById(R.id.photos_statistics);

        realname.setText(mMeetMember.getRealname());

        if (!"".equals(mMeetMember.getPictureUri())) {
            String picture_url = HttpUtil.DOMAIN + "/" + mMeetMember.getPictureUri();
            RequestQueue queue = RequestQueueSingleton.instance(this);

            /*+Begin: added by xuchunping for Use glide loader image, 2018/11/28*/
            //headUri.setTag(picture_url);
            //HttpUtil.loadByImageLoader(queue, headUri, picture_url, 110, 110);
            Glide.with(this).load(picture_url).into(headUri);
            /*-End: added by xuchunping for Use glide loader image, 2018/11/28*/
        } else {
            headUri.setImageDrawable(getDrawable(R.mipmap.ic_launcher));
        }


        age.setText(String.valueOf(mMeetMember.getAge()) + "岁");
        height.setText(String.valueOf(mMeetMember.getHeight()) + "CM");
        sex.setText(String.valueOf(mMeetMember.getSelfSex()));
        lives.setText(mMeetMember.getLives());
        if (mMeetMember.getSituation() == 0) {
            major.setText(mMeetMember.getMajor());
            degree.setText(mMeetMember.getDegreeName(mMeetMember.getDegree()));
            university.setText(mMeetMember.getUniversity());
        } else {
            job.setText(mMeetMember.getJobTitle());
            company.setText(mMeetMember.getCompany());
        }
        ageRequirement.setText(mMeetMember.getAgeLower() + "~" + mMeetMember.getAgeUpper() + "岁");
        heightRequirement.setText(String.valueOf(mMeetMember.getRequirementHeight()) + "CM");
        degreeRequirement.setText(mMeetMember.getDegreeName(mMeetMember.getRequirementDegree()) + "学历");
        livesRequirement.setText(mMeetMember.getRequirementLives());
        sexRequirement.setText(mMeetMember.getRequirementSex());

        illustration.setText(mMeetMember.getIllustration());
        eyeView.setText(String.valueOf(mMeetMember.getBrowseCount()));
        //lovedView.setText(String.valueOf(mMeetMember.getLovedCount()));
        //thumbsView.setText(String.valueOf(mMeetMember.getPraisedCount()));
    }

    private void processContactsAction() {
        final Button contacts = mArchiveProfile.findViewById(R.id.contacts);

        if (contactStatus == APPLIED) {
            contacts.setEnabled(false);
            contacts.setText("已申请");
        } else if (contactStatus == ESTABLISHED) {
            contacts.setVisibility(View.GONE);
        } else if (contactStatus == APPROVE) {
            contacts.setText("同意请求");
            contacts.setTag("approve");
        }

        contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contacts.getTag().equals("approve")) {
                    if(isDebug) Slog.d(TAG, "=====================同意请求");
                } else {
                    RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(mMeetMember.getUid())).build();
                    HttpUtil.sendOkHttpRequest(ArchivesActivity.this, CONTACTS_ADD_URL, requestBody, new Callback() {
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.body() != null) {
                                String responseText = response.body().string();
                                if(isDebug)
                                Slog.d(TAG, "==========processContactsAction : " + responseText);
                                /*
                                try {
                                    JSONObject status = new JSONObject(responseText);
                                    isFollowed = status.optBoolean("isFollowed");
                                    handler.sendEmptyMessage(GET_FOLLOW_DONE);
                                }catch (JSONException e){
                                    e.printStackTrace();
                                }
                                */
                            }
                        }

                        @Override
                        public void onFailure(Call call, IOException e) {
                        }
                    });

                    contacts.setText("已申请");
                    contacts.setEnabled(false);
                }
            }
        });
    }

    private void getConnectStatus() {

        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(mMeetMember.getUid())).build();
        HttpUtil.sendOkHttpRequest(ArchivesActivity.this, GET_FOLLOW_STATUS_URL, requestBody, new Callback() {
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

    private void getFollowStatistics() {
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(mMeetMember.getUid())).build();

        //for contacts
        HttpUtil.sendOkHttpRequest(ArchivesActivity.this, GET_CONTACTS_STATUS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if(isDebug)
                    Slog.d(TAG, "==========getContacts Status : " + responseText);
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

        //for follow
        HttpUtil.sendOkHttpRequest(ArchivesActivity.this, GET_FOLLOW_STATISTICS_URL, requestBody, new Callback() {
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

        LinearLayout followingCountWrap = mArchiveProfile.findViewById(R.id.following_count_wrap);
        LinearLayout followedCountWrap = mArchiveProfile.findViewById(R.id.followed_count_wrap);
        final TextView followedCount = mArchiveProfile.findViewById(R.id.followed_count);
        final TextView followingCount = mArchiveProfile.findViewById(R.id.following_count);
        followedCountWrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("type", FOLLOWED);
                bundle.putInt("uid", mMeetMember.getUid());
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
                bundle.putInt("uid", mMeetMember.getUid());
                bundle.putString("title", "关注的 " + followingCount.getText());
                CommonUserListDialogFragment commonUserListDialogFragment = new CommonUserListDialogFragment();
                commonUserListDialogFragment.setArguments(bundle);
                commonUserListDialogFragment.show(getSupportFragmentManager(), "CommonUserListDialogFragment");
            }
        });


    }

    private void getPraiseStatistics() {
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(mMeetMember.getUid())).build();
        HttpUtil.sendOkHttpRequest(ArchivesActivity.this, GET_PRAISE_STATISTICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if(isDebug)
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
                bundle.putInt("uid", mMeetMember.getUid());
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
                bundle.putInt("uid", mMeetMember.getUid());
                bundle.putString("title", "赞过的 " + praiseCount.getText());
                CommonUserListDialogFragment commonUserListDialogFragment = new CommonUserListDialogFragment();
                commonUserListDialogFragment.setArguments(bundle);
                commonUserListDialogFragment.show(getSupportFragmentManager(), "CommonUserListDialogFragment");
            }
        });
    }

    private void getLoveStatistics() {
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(mMeetMember.getUid())).build();
        HttpUtil.sendOkHttpRequest(ArchivesActivity.this, GET_LOVE_STATISTICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if(isDebug)
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
        final TextView lovedCount = mArchiveProfile.findViewById(R.id.loved_count);
        final TextView loveCount = mArchiveProfile.findViewById(R.id.love_count);
        lovedCountWrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("type", LOVED);
                bundle.putInt("uid", mMeetMember.getUid());
                bundle.putString("title", "被喜欢 " + lovedCount.getText());
                CommonUserListDialogFragment commonUserListDialogFragment = new CommonUserListDialogFragment();
                commonUserListDialogFragment.setArguments(bundle);
                commonUserListDialogFragment.show(getSupportFragmentManager(), "CommonUserListDialogFragment");
            }
        });
        loveCountWrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("type", LOVE);
                bundle.putInt("uid", mMeetMember.getUid());
                bundle.putString("title", "喜欢的 " + loveCount.getText());
                CommonUserListDialogFragment commonUserListDialogFragment = new CommonUserListDialogFragment();
                commonUserListDialogFragment.setArguments(bundle);
                commonUserListDialogFragment.show(getSupportFragmentManager(), "CommonUserListDialogFragment");
            }
        });
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
                RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(mMeetMember.getUid())).build();
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

                HttpUtil.sendOkHttpRequest(ArchivesActivity.this, followUrl, requestBody, new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.body() != null) {
                            String responseText = response.body().string();
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
        TextView lovedCount = mArchiveProfile.findViewById(R.id.loved_count);
        TextView loveCount = mArchiveProfile.findViewById(R.id.love_count);
        TextView lovedStatistics = mArchiveProfile.findViewById(R.id.loved_statistics);
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

    private void processChatAction(int uid) {

    }

    private void processLoveAction() {

        final TextView lovedIcon = mArchiveProfile.findViewById(R.id.loved_icon);
        final TextView lovedCount = mArchiveProfile.findViewById(R.id.loved_statistics);
        if (isLoved) {
            lovedIcon.setEnabled(false);
            lovedCount.setEnabled(false);
        } else {
            lovedIcon.setEnabled(true);
            lovedCount.setEnabled(true);
        }
        final RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(mMeetMember.getUid())).build();

        lovedIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpUtil.sendOkHttpRequest(ArchivesActivity.this, LOVE_ADD_URL, requestBody, new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.body() != null) {
                            String responseText = response.body().string();
                            if(isDebug)
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
                HttpUtil.sendOkHttpRequest(ArchivesActivity.this, LOVE_ADD_URL, requestBody, new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.body() != null) {
                            String responseText = response.body().string();
                            if(isDebug)
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
        final TextView praisedIcon = mArchiveProfile.findViewById(R.id.praised_icon);
        final TextView praisedCount = mArchiveProfile.findViewById(R.id.praised_statistics);
        if (isPraised) {
            praisedCount.setEnabled(false);
            praisedIcon.setEnabled(false);
        } else {
            praisedCount.setEnabled(true);
            praisedIcon.setEnabled(true);
        }

        final RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(mMeetMember.getUid())).build();
        praisedIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpUtil.sendOkHttpRequest(ArchivesActivity.this, PRAISE_ADD_URL, requestBody, new Callback() {
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
                HttpUtil.sendOkHttpRequest(ArchivesActivity.this, PRAISE_ADD_URL, requestBody, new Callback() {
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
        
        if(meetReferenceInfoList.size() > 0){
            recyclerView.setVisibility(View.VISIBLE);
        }
        
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mHeaderEvaluation.findViewById(R.id.meet_item_id), font);

    }

    private void setRatingBarView() {
        JSONArray ratingArray = mRatingObj.optJSONArray("rating");
        //MeetReferenceInfo meetReferenceInfo = null;
        if (ratingArray != null && ratingArray.length() > 0) {
            TextView ratingMemberCount = mHeaderEvaluation.findViewById(R.id.rating_member_count);
            ratingMemberCount.setText(ratingArray.length() + "人评价");
            float ratingCount = 0;
            for (int i = 0; i < ratingArray.length(); i++) {
                JSONObject ratingObj = ratingArray.optJSONObject(i);
                if (mRatingObj.optInt("visitor_uid") == ratingObj.optInt("evaluator_uid")) {
                    setEvaluatedMode();
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

        }
    }

    private void loadRating(int uid) {
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
    }

    private void loadImpressionStatistics(final int uid) {

        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(this, GET_IMPRESSION_STATISTICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if(isDebug) Slog.d(TAG, "==========loadImpressionStatistics response text : " + responseText);

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
                Intent intent = new Intent(ArchivesActivity.this, ApprovedUsersActivity.class);
                intent.putExtra("uid", mMeetMember.getUid());
                intent.putExtra("impressionStatistics", mImpressionStatisticsList.get(position));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
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

    private List<MeetMemberInfo> getImpressionUser(String impression, int uid) {
        List<MeetMemberInfo> memberInfoList = new ArrayList<>();
        RequestBody requestBody = new FormBody.Builder()
                .add("impression", impression)
                .add("uid", String.valueOf(uid)).build();
        Slog.d(TAG, "impression: " + impression + " uid: " + uid);
        Response response = HttpUtil.sendOkHttpRequestSync(this, GET_IMPRESSION_USERS_URL, requestBody, null);

        try {
            String responseText = response.body().string();
            try {
                JSONObject responseObj = new JSONObject(responseText);
                JSONArray responseArray = responseObj.optJSONArray("users");
                if (responseArray.length() > 0) {


                    for (int i = 0; i < responseArray.length(); i++) {
                        MeetMemberInfo meetMemberInfo = new MeetMemberInfo();
                        JSONObject member = responseArray.optJSONObject(i);
                        meetMemberInfo.setUid(member.optInt("uid"));
                        meetMemberInfo.setSex(member.optInt("sex"));
                        meetMemberInfo.setRealname(member.optString("realname"));
                        meetMemberInfo.setPictureUri(member.optString("picture_uri"));
                        meetMemberInfo.setSituation(member.optInt("situation"));
                        if (member.optInt("situation") == 0) {//student
                            meetMemberInfo.setDegree(member.optString("degree"));
                            meetMemberInfo.setMajor(member.optString("major"));
                            meetMemberInfo.setUniversity(member.optString("university"));
                        } else {
                            meetMemberInfo.setCompany(member.optString("company"));
                            meetMemberInfo.setJobTitle(member.optString("job_title"));
                            meetMemberInfo.setLives(member.optString("lives"));
                        }
                        memberInfoList.add(meetMemberInfo);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return memberInfoList;
    }
    
    private void processReferences(int uid){
        loadReferences(uid);

        View inviteReference = mHeaderEvaluation.findViewById(R.id.invite_reference);
        inviteReference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Toast.makeText(getApplicationContext(), "hahaha", Toast.LENGTH_SHORT).show();
                InvitationDialogFragment invitationDialogFragment = new InvitationDialogFragment();
                // invitationDialogFragment.setTargetFragment(getApplicationContext(), REQUEST_CODE);
                Bundle bundle = new Bundle();
                bundle.putInt("uid", mMeetMember.getUid());
                invitationDialogFragment.setArguments(bundle);
                invitationDialogFragment.show(getSupportFragmentManager(), "InvitationDialogFragment");
            }
        });
        
        TextView writeReference = mHeaderEvaluation.findViewById(R.id.write_reference);
        writeReference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReferenceWriteDialogFragment referenceWriteDialogFragment = new ReferenceWriteDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("uid", mMeetMember.getUid());
                bundle.putString("name", mMeetMember.getRealname());

                referenceWriteDialogFragment.setArguments(bundle);
                referenceWriteDialogFragment.show(getSupportFragmentManager(), "ReferenceWriteDialogFragment");
            }
        });

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
                        List<MeetReferenceInfo> meetReferenceInfoList = ParseUtils.getMeetReferenceList(responseText);
                        if (meetReferenceInfoList != null && meetReferenceInfoList.size() > 0) {
                            mReferenceList.addAll(meetReferenceInfoList);
                        }
                        handler.sendEmptyMessage(LOAD_REFERENCE_DONE);
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void processPersonality(final int uid) {
        getPersonality(uid);
        addPersonality(uid);
    }


    private void getPersonality(int uid) {
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .build();
        HttpUtil.sendOkHttpRequest(this, GET_PERSONALITY_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if(isDebug) Slog.d(TAG, "================getPersonalityDetail response:" + responseText);
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

    private void addPersonality(final int uid) {
        Button addPersonality = mHeaderEvaluation.findViewById(R.id.add_personality);
        addPersonality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PersonalityEditDialogFragment personalityEditDialogFragment = new PersonalityEditDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("uid", uid);
                personalityEditDialogFragment.setArguments(bundle);
                personalityEditDialogFragment.show(getSupportFragmentManager(), "PersonalityEditDialogFragment");
            }
        });
    }

    private void processHobby(int uid) {
        getHobbies(uid);
        addHobbies(uid);
    }

    private void getHobbies(int uid) {
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

    private void addHobbies(final int uid) {
        Button add = mHeaderEvaluation.findViewById(R.id.add_hobby);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PersonalityEditDialogFragment personalityEditDialogFragment = new PersonalityEditDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("uid", uid);
                bundle.putInt("type", TYPE_HOBBY);
                personalityEditDialogFragment.setArguments(bundle);
                personalityEditDialogFragment.show(getSupportFragmentManager(), "PersonalityEditDialogFragment");

            }
        });
    }

    private void setHobbyFlow(String[] hobbyArray) {
        FlowLayout hobbyFlow = mHeaderEvaluation.findViewById(R.id.hobby_flow);

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

    private void loadDynamicsData(int uid) {
        handler = new ArchivesActivity.MyHandler(this);

        int page = mMeetList.size() / PAGE_SIZE;
        FormBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();
        if (isDebug)
            Log.d(TAG, "loadData requestBody:" + requestBody.toString() + " page:" + page + " uid:" + uid);
        HttpUtil.sendOkHttpRequest(this, DYNAMICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if(isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null) {
                        List<MeetDynamics> tempList = parseDynamics(responseText);
                        mTempSize = 0;
                        if (null != tempList) {
                            mTempSize = tempList.size();
                            mMeetList.addAll(tempList);
                            Log.d(TAG, "getResponseText list.size:" + tempList.size());
                        }
                        handler.sendEmptyMessage(DONE);
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private List<MeetDynamics> parseDynamics(String responseText) {
        List<MeetDynamics> tempList = new ArrayList<MeetDynamics>();
        if (!TextUtils.isEmpty(responseText)) {
            try {
                JSONObject dynamics_response = new JSONObject(responseText);
                JSONArray dynamicsArray = dynamics_response.getJSONArray("activity");
                if (dynamicsArray.length() < 0) {
                    return null;
                }

                int length = dynamicsArray.length();
                MeetDynamics meetDynamics = null;
                for (int i = 0; i < length; i++) {
                    JSONObject dynamics = dynamicsArray.getJSONObject(i);
                    meetDynamics = new MeetDynamics();

                    meetDynamics.setRealname(dynamics.optString("realname"));
                    meetDynamics.setUid(dynamics.optInt("uid"));
                    meetDynamics.setSex(dynamics.optInt("sex"));
                    meetDynamics.setPictureUri(dynamics.optString("picture_uri"));

                    if (!dynamics.isNull("birth_year")) {
                        meetDynamics.setBirthYear(dynamics.optInt("birth_year"));
                    }
                    if (!dynamics.isNull("height")) {
                        meetDynamics.setHeight(dynamics.optInt("height"));
                    }
                    if (!dynamics.isNull("degree")) {
                        meetDynamics.setDegree(dynamics.optString("degree"));
                    }
                    if (!dynamics.isNull("university")) {
                        meetDynamics.setUniversity(dynamics.optString("university"));
                    }
                    if (!dynamics.isNull("job_title")) {
                        meetDynamics.setJobTitle(dynamics.optString("job_title"));
                    }
                    if (!dynamics.isNull("lives")) {
                        meetDynamics.setLives(dynamics.optString("lives"));
                    }
                    if (!dynamics.isNull("situation")) {
                        meetDynamics.setSituation(dynamics.optInt("situation"));
                    }

                    //requirement
                    if (!dynamics.isNull("age_lower")) {
                        meetDynamics.setAgeLower(dynamics.optInt("age_lower"));
                    }
                    if (!dynamics.isNull("age_upper")) {
                        meetDynamics.setAgeUpper(dynamics.optInt("age_upper"));
                    }
                    if (!dynamics.isNull("requirement_height")) {
                        meetDynamics.setRequirementHeight(dynamics.optInt("requirement_height"));
                    }
                    if (!dynamics.isNull("requirement_degree")) {
                        meetDynamics.setRequirementDegree(dynamics.optString("requirement_degree"));
                    }
                    if (!dynamics.isNull("requirement_lives")) {
                        meetDynamics.setRequirementLives(dynamics.optString("requirement_lives"));
                    }
                    if (!dynamics.isNull("requirement_sex")) {
                        meetDynamics.setRequirementSex(dynamics.optInt("requirement_sex"));
                    }
                    if (!dynamics.isNull("illustration")) {
                        meetDynamics.setIllustration(dynamics.optString("illustration"));
                    }
                    //interact count
                    meetDynamics.setBrowseCount(dynamics.optInt("browse_count"));
                    meetDynamics.setLovedCount(dynamics.optInt("loved_count"));
                    meetDynamics.setPraisedCount(dynamics.optInt("praised_count"));
                    meetDynamics.setLoved(dynamics.optInt("loved"));
                    meetDynamics.setPraised(dynamics.optInt("praised"));

                    //dynamics content
                    if (!dynamics.isNull("created")) {
                        meetDynamics.setCreated(dynamics.optLong("created"));
                    }
                    String content = dynamics.optString("content");
                    if (content != null && content.length() != 0) {
                        meetDynamics.setContent(content);
                    }
                    if (!dynamics.isNull("activity_picture")) {
                        String dynamics_pictures = dynamics.optString("activity_picture");
                        if (!"".equals(dynamics_pictures)) {
                            meetDynamics.setActivityPicture(dynamics_pictures);
                        }
                    }

                    meetDynamics.setAid(dynamics.optLong("aid"));
                    getDynamicsComment(dynamics.optLong("aid"));
                    tempList.add(meetDynamics);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return tempList;
    }

    private void getDynamicsComment(final Long aid) {
        if (isDebug) Log.d(TAG, "getDynamicsComment: aid:" + aid);
        RequestBody requestBody = new FormBody.Builder().add("aid", aid.toString()).build();
        HttpUtil.sendOkHttpRequest(this, COMMENT_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                //Log.d(TAG, "getDynamicsComment: "+responseText);
                JSONObject commentResponse = null;
                JSONArray commentArray = null;
                JSONArray praiseArray = null;
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        commentResponse = new JSONObject(responseText);
                        commentArray = commentResponse.getJSONArray("comment");
                        praiseArray = commentResponse.getJSONArray("praise");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    MeetDynamics meetDynamics = getMeetDynamicsById(aid);
                    meetDynamics.setPraisedDynamics(commentResponse.optInt("praised"));
                    if (commentArray.length() > 0) {
                        setDynamicsComment(meetDynamics, commentArray, praiseArray);
                    }
                    if (null != praiseArray) {


                        meetDynamics.setPraisedDynamicsCount(praiseArray.length());
                    }
                    handler.sendEmptyMessage(UPDATE_COMMENT);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure e:" + e);
            }
        });

    }

    private void setDynamicsComment(MeetDynamics meetDynamics, JSONArray commentArray, JSONArray praiseArray) {
        JSONObject comment;
        JSONObject praise;
        meetDynamics.setCommentCount(commentArray.length());
        DynamicsComment dynamicsComment = null;
        try {
            for (int i = 0; i < commentArray.length(); i++) {
                comment = commentArray.getJSONObject(i);
                dynamicsComment = new DynamicsComment();
                dynamicsComment.setType(comment.optInt("type"));
                dynamicsComment.setCid(comment.optInt("cid"));
                dynamicsComment.setAid(comment.optInt("aid"));
                dynamicsComment.setPictureUrl(comment.optString("picture_uri"));
                if (!comment.isNull("author_uid")) {
                    dynamicsComment.setAuthorUid(comment.optLong("author_uid"));
                }
                if (!comment.isNull("author_name")) {
                    dynamicsComment.setAuthorName(comment.optString("author_name"));
                }
                dynamicsComment.setCommenterName(comment.optString("commenter_name"));
                dynamicsComment.setCommenterUid(comment.optLong("commenter_uid"));
                dynamicsComment.setContent(comment.optString("content"));
                meetDynamics.addComment(dynamicsComment);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private MeetDynamics getMeetDynamicsById(long aId) {
        for (int i = 0; i < mMeetList.size(); i++) {
            if (aId == mMeetList.get(i).getAid()) {
                return mMeetList.get(i);
            }
        }
        return null;
    }

    @Override
    public void onBackFromRatingAndImpressionDialogFragment(boolean evaluated) {
        if (evaluated) {
            setEvaluatedMode();
        }
    }

    private void updateLovedCount() {
        TextView lovedStatistics = mArchiveProfile.findViewById(R.id.loved_statistics);
        TextView lovedCount = mArchiveProfile.findViewById(R.id.loved_count);
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
    }

    private void updatePraisedCount() {
        TextView praisedStatistics = mArchiveProfile.findViewById(R.id.praised_statistics);
        TextView praisedCount = mArchiveProfile.findViewById(R.id.praised_count);
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
            newPraisedCount = Integer.parseInt(praisedCount.getText().toString()) + 1;
        }
        praisedCount.setText(String.valueOf(newPraisedCount));
    }

    public void handleMessage(Message message) {
        Bundle bundle = new Bundle();
        bundle = message.getData();
        switch (message.what) {
            case DONE:
                mArchivesListAdapter.setData(mMeetList);
                mArchivesListAdapter.notifyDataSetChanged();
                mXRecyclerView.refreshComplete();

                if (mTempSize < PAGE_SIZE) {
                    //loading finished
                    mXRecyclerView.setNoMore(true);
                    mXRecyclerView.setLoadingMoreEnabled(false);
                }
                /*if (mMeetList.size() > 0) {
                    mEmptyView.setVisibility(View.GONE);
                    mXRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    mEmptyView.setVisibility(View.VISIBLE);
                    mXRecyclerView.setVisibility(View.GONE);
                }*/
                break;
            case UPDATE:
                break;
            case UPDATE_COMMENT:
                mArchivesListAdapter.setData(mMeetList);
                mArchivesListAdapter.notifyDataSetChanged();
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
                processContactsAction();
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
            default:
                break;
        }
    }

    public static class ImpressionStatistics implements Parcelable {
        public static final Parcelable.Creator<ImpressionStatistics> CREATOR = new Creator<ImpressionStatistics>() {

            @Override
            public ImpressionStatistics createFromParcel(Parcel source) {
                ImpressionStatistics impressionStatistics = new ImpressionStatistics();
                impressionStatistics.impression = source.readString();
                impressionStatistics.impressionCount = source.readInt();
                //impressionStatistics.meetMemberList = new ArrayList<MeetMemberInfo>();
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
        public List<MeetMemberInfo> meetMemberList = new ArrayList<>();

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

    static class MyHandler extends Handler {
        WeakReference<ArchivesActivity> meetDynamicsFragmentWeakReference;

        MyHandler(ArchivesActivity archivesActivity) {
            meetDynamicsFragmentWeakReference = new WeakReference<ArchivesActivity>(archivesActivity);
        }

        @Override
        public void handleMessage(Message message) {
            ArchivesActivity archivesActivity = meetDynamicsFragmentWeakReference.get();
            if (archivesActivity != null) {
                archivesActivity.handleMessage(message);
            }
        }
    }

}
