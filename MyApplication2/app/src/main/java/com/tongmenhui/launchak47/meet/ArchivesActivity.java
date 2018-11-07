package com.tongmenhui.launchak47.meet;

import android.os.Parcel;
import android.os.Parcelable;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputEditText;
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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.support.constraint.ConstraintLayout;
import com.nex3z.flowlayout.FlowLayout;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import java.math.BigDecimal;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.NetworkImageView;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.adapter.ArchivesListAdapter;
import com.tongmenhui.launchak47.adapter.MeetImpressionStatisticsAdapter;
import com.tongmenhui.launchak47.adapter.MeetReferenceAdapter;
import com.tongmenhui.launchak47.main.BaseAppCompatActivity;
import com.tongmenhui.launchak47.util.FontManager;
import com.tongmenhui.launchak47.util.HttpUtil;
import com.tongmenhui.launchak47.util.ParseUtils;
import com.tongmenhui.launchak47.util.RequestQueueSingleton;
import com.tongmenhui.launchak47.util.Slog;
import com.tongmenhui.launchak47.util.InvitationDialogFragment;
import com.willy.ratingbar.BaseRatingBar;
import com.willy.ratingbar.ScaleRatingBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class ArchivesActivity extends BaseAppCompatActivity implements EvaluateDialogFragment.EvaluateDialogFragmentListener{
    private static final String TAG = "ArchivesActivity";
    private static final boolean isDebug = false;
    private static final String DYNAMICS_URL = HttpUtil.DOMAIN + "?q=meet/activity/get";
    private static final String COMMENT_URL = HttpUtil.DOMAIN + "?q=meet/activity/interact/get";
    private static final String LOAD_REFERENCE_URL = HttpUtil.DOMAIN + "?q=meet/reference/load";
    private static final String GET_IMPRESSION_URL = HttpUtil.DOMAIN + "?q=meet/impression/get";
    private static final String GET_IMPRESSION_STATISTICS_URL = HttpUtil.DOMAIN + "?q=meet/impression/statistics";
    private static final String GET_IMPRESSION_USERS_URL = HttpUtil.DOMAIN + "?q=meet/impression/users";

    private List<MeetDynamics> mMeetList = new ArrayList<>();
    private List<ImpressionStatistics> mImpressionStatisticsList = new ArrayList<>();
    private List<MeetReferenceInfo> mReferenceList = new ArrayList<>();
    private Handler handler;
    private static final int DONE = 1;
    private static final int UPDATE = 2;
    private static final int UPDATE_COMMENT = 3;
    private static final int LOAD_RATING_DONE = 4;
    private static final int LOAD_IMPRESSION_DONE = 5;
    private static final int LOAD_REFERENCE_DONE = 6;
    private static final int PAGE_SIZE = 6;
     private static final int REQUEST_CODE = 1;
    private int mTempSize;
    private XRecyclerView mXRecyclerView;
    private ArchivesListAdapter mArchivesListAdapter;
    private MeetReferenceAdapter mMeetReferenceAdapter;
    private MeetImpressionStatisticsAdapter mMeetImpressionStatisticsAdapter;
    private TextView mEmptyView;
    View mHeaderEvaluation;
    private JSONObject impressionObj;
    public String impression;
    public int impressionCount;
    public List<MeetMemberInfo> meetMemberList;
    private EvaluateDialogFragment evaluateDialogFragment;

    private ImageView backLeft;
    private MeetMemberInfo mMeetMember;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.archives);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.hide();
        }
        mMeetMember = (MeetMemberInfo) getIntent().getSerializableExtra("meet");
        final int uid = mMeetMember.getUid();

        initView();

        loadRatingAndImpression(uid);

        loadImpressionStatistics(uid);

        loadReferences(uid);

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
                if(evaluateDialogFragment == null) {
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
                if(isDebug) Slog.d(TAG, "==============start EvaluatorDetailsActivity");
                Intent intent = new Intent(ArchivesActivity.this, EvaluatorDetailsActivity.class);
                intent.putExtra("uid", mMeetMember.getUid());
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });

        View inviteReference = mHeaderEvaluation.findViewById(R.id.invite_reference);
        inviteReference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Toast.makeText(getApplicationContext(), "hahaha", Toast.LENGTH_SHORT).show();
                InvitationDialogFragment invitationDialogFragment = new InvitationDialogFragment();
               // invitationDialogFragment.setTargetFragment(getApplicationContext(), REQUEST_CODE);
                invitationDialogFragment.show(getSupportFragmentManager(), "InvitationDialogFragment");
            }
        });
        

    }
    
    private void initView(){
        backLeft = findViewById(R.id.left_back);
        backLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome.ttf");
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
        View headerProfile = LayoutInflater.from(this).inflate(R.layout.meet_item, (ViewGroup)findViewById(android.R.id.content),false);
        mXRecyclerView.addHeaderView(headerProfile);
        FontManager.markAsIconContainer(headerProfile.findViewById(R.id.meet_item_id), font);
        updateHeader(headerProfile);


        mHeaderEvaluation = LayoutInflater.from(this).inflate(R.layout.friends_relatives_reference, (ViewGroup)findViewById(android.R.id.content),false);
        mXRecyclerView.addHeaderView(mHeaderEvaluation);
        FontManager.markAsIconContainer(mHeaderEvaluation.findViewById(R.id.charm_rating_bar), font);
        FontManager.markAsIconContainer(mHeaderEvaluation.findViewById(R.id.rating_member_details), font);

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

        RecyclerView referenceRecyclerView = mHeaderEvaluation.findViewById(R.id.reference_list);
        referenceRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mXRecyclerView.setAdapter(mArchivesListAdapter);
    }
    
    private void setEvaluatedMode(){
        LinearLayout charmRatingBar = mHeaderEvaluation.findViewById(R.id.charm_rating_bar);
        TextView notice = mHeaderEvaluation.findViewById(R.id.notice);
        charmRatingBar.setVisibility(View.GONE);
        notice.setVisibility(View.GONE);
    }
    

    private void updateHeader(View view){
        TextView realname = (TextView) view.findViewById(R.id.name);
        TextView lives = (TextView) view.findViewById(R.id.lives);
        NetworkImageView headUri = (NetworkImageView) view.findViewById(R.id.recommend_head_uri);
        TextView selfcondition = (TextView) view.findViewById(R.id.self_condition);
        TextView requirement = (TextView) view.findViewById(R.id.partner_requirement);
        TextView illustration = (TextView) view.findViewById(R.id.illustration);
        TextView eyeView = (TextView) view.findViewById(R.id.eye_statistics);
        TextView lovedView = (TextView)view.findViewById(R.id.loved_statistics);
        TextView lovedIcon = (TextView)view.findViewById(R.id.loved_icon);
        TextView thumbsView = (TextView)view.findViewById(R.id.thumbs_up_statistics);
        TextView thumbsIcon = (TextView)view.findViewById(R.id.thumbs_up_icon);
        TextView photosView = (TextView)view.findViewById(R.id.photos_statistics);

        realname.setText(mMeetMember.getRealname());
        lives.setText(mMeetMember.getLives());

        if(!"".equals(mMeetMember.getPictureUri())){
            String picture_url = HttpUtil.DOMAIN + "/"+mMeetMember.getPictureUri();
            RequestQueue queue = RequestQueueSingleton.instance(this);

            headUri.setTag(picture_url);
            HttpUtil.loadByImageLoader(queue, headUri, picture_url, 110, 110);
        }else{
            headUri.setImageDrawable(getDrawable(R.mipmap.ic_launcher));
        }


        selfcondition.setText(mMeetMember.getSelfCondition(mMeetMember.getSituation()));
        requirement.setText(mMeetMember.getRequirement());

        eyeView.setText(String.valueOf(mMeetMember.getBrowseCount()));
        lovedView.setText(String.valueOf(mMeetMember.getLovedCount()));
        thumbsView.setText(String.valueOf(mMeetMember.getPraisedCount()));
        illustration.setText(mMeetMember.getIllustration());

    }

    private void updateEvaluationHeader(){

        RecyclerView recyclerView = mHeaderEvaluation.findViewById(R.id.reference_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mMeetReferenceAdapter = new MeetReferenceAdapter(this);
        recyclerView.setAdapter(mMeetReferenceAdapter);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome.ttf");
        FontManager.markAsIconContainer(mHeaderEvaluation.findViewById(R.id.meet_item_id), font);
        
    }
    
    private void setRatingBarView(){
        JSONArray impressionArray = impressionObj.optJSONArray("impression");
        //MeetReferenceInfo meetReferenceInfo = null;
        if(impressionArray != null && impressionArray.length() > 0){
            TextView ratingMemberCount = mHeaderEvaluation.findViewById(R.id.rating_member_count);
            ratingMemberCount.setText(impressionArray.length()+"人评价");
            float ratingCount = 0;
            for (int i=0; i<impressionArray.length(); i++){
                JSONObject impression = impressionArray.optJSONObject(i);
                if(impressionObj.optInt("visitor_uid") == impression.optInt("evaluator_uid")){
                    setEvaluatedMode();
                }
                ratingCount += impression.optDouble("rating");
            }
            float ratingAverage = ratingCount/impressionArray.length();
            float ratingAverageRoundUp = 0;
            BigDecimal  b = new BigDecimal(ratingAverage);
            ratingAverageRoundUp = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
            TextView ratingAverageTV = mHeaderEvaluation.findViewById(R.id.chram_synthesized_results);
            ratingAverageTV.setText(ratingAverageRoundUp+"分");

            ScaleRatingBar scaleRatingBarCount = mHeaderEvaluation.findViewById(R.id.charm_synthesized_rating);
            scaleRatingBarCount.setRating(ratingAverageRoundUp);

        }
    }
    
   private void loadRatingAndImpression(int uid){
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(this, GET_IMPRESSION_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.body() != null){
                    String responseText = response.body().string();
                   if(isDebug) Slog.d(TAG, "==========get impression response text : "+responseText);
                    if(responseText != null){
                        if(!TextUtils.isEmpty(responseText)){
                            try {
                                impressionObj = new JSONObject(responseText);
                                if(impressionObj != null){
                                    handler.sendEmptyMessage(LOAD_RATING_DONE);
                                }
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {}
        });
    }
    
    private void loadImpressionStatistics(final int uid){

        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(this, GET_IMPRESSION_STATISTICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.body() != null){
                    String responseText = response.body().string();
                    Slog.d(TAG, "==========loadImpressionStatistics response text : "+responseText);

                    if(responseText != null){
                        if(!TextUtils.isEmpty(responseText)){
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

    private void setImpressionStatisticsView(){
        RecyclerView impressionStatisticsWrap = mHeaderEvaluation.findViewById(R.id.impression_statistics_list);
        impressionStatisticsWrap.setLayoutManager(new LinearLayoutManager(this));
        mMeetImpressionStatisticsAdapter = new MeetImpressionStatisticsAdapter(this, getSupportFragmentManager());
        impressionStatisticsWrap.setAdapter(mMeetImpressionStatisticsAdapter);
    }

    private void parseImpressionStatistics(String response, int uid){
        JSONObject responseObj = null;
        try{
            responseObj = new JSONObject(response);
        } catch (JSONException e){
            e.printStackTrace();
        }
        if(responseObj != null){
            JSONObject impressionStatisticsObj = responseObj.optJSONObject("features_statistics");
            if(impressionStatisticsObj != null){
                Iterator iterator = impressionStatisticsObj.keys();
                int index = 0;
                while (iterator.hasNext()){
                    index++;
                    String key = (String) iterator.next();
                    int value = impressionStatisticsObj.optInt(key);
                    ImpressionStatistics impressionStatistics = new ImpressionStatistics();
                    impressionStatistics.impression = key;
                    impressionStatistics.impressionCount = value;
                    impressionStatistics.meetMemberList = getImpressionUser(key, uid);
                    mImpressionStatisticsList.add(impressionStatistics);

                    Slog.d(TAG, "==============key: "+key+"   value: "+value);
                    if(index == 5){
                        break;
                    }
                }
                Message msg = handler.obtainMessage();
                msg.what = LOAD_IMPRESSION_DONE;
                handler.sendMessage(msg);
            }
        }
    }

    public class ImpressionStatistics implements Parcelable{
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
        
        public final Parcelable.Creator<ImpressionStatistics> CREATOR = new Creator<ImpressionStatistics>() {

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
    }

    private List<MeetMemberInfo> getImpressionUser(String impression, int uid){
        List<MeetMemberInfo> memberInfoList = new ArrayList<>();
        RequestBody requestBody = new FormBody.Builder()
                .add("impression", impression)
                .add("uid", String.valueOf(uid)).build();
        Slog.d(TAG, "impression: "+impression+ " uid: "+uid);
        Response response = HttpUtil.sendOkHttpRequestSync(this, GET_IMPRESSION_USERS_URL, requestBody, null);

        try {
            String responseText = response.body().string();
            Slog.d(TAG, "==========getImpressionUser response text : "+responseText);
            try{
               JSONObject responseObj = new JSONObject(responseText);
               JSONArray responseArray = responseObj.optJSONArray("users");
               if(responseArray.length() > 0){


                   for (int i=0; i<responseArray.length(); i++){
                       MeetMemberInfo meetMemberInfo = new MeetMemberInfo();
                       JSONObject member = responseArray.optJSONObject(i);
                       meetMemberInfo.setUid(member.optInt("uid"));
                       meetMemberInfo.setSex(member.optInt("sex"));
                       meetMemberInfo.setRealname(member.optString("realname"));
                       meetMemberInfo.setPictureUri(member.optString("picture_uri"));
                       meetMemberInfo.setSituation(member.optInt("situation"));
                       if(member.optInt("situation") == 0){//student
                           meetMemberInfo.setDegree(member.optString("degree"));
                           meetMemberInfo.setMajor(member.optString("major"));
                           meetMemberInfo.setUniversity(member.optString("university"));
                       }else {
                           meetMemberInfo.setCompany(member.optString("company"));
                           meetMemberInfo.setJobTitle(member.optString("job_title"));
                           meetMemberInfo.setLives(member.optString("lives"));
                       }
                       memberInfoList.add(meetMemberInfo);
                   }
               }
            } catch (JSONException e){
                e.printStackTrace();
            }
        }catch (IOException e){
            e.printStackTrace();
        }


        return memberInfoList;
    }

    private void loadReferences(int uid){
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(this, LOAD_REFERENCE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.body() != null){
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========loadReferences response text : "+responseText);
                    if(responseText != null){
                        List<MeetReferenceInfo> meetReferenceInfoList = ParseUtils.getMeetReferenceList(responseText);
                        if(meetReferenceInfoList != null && meetReferenceInfoList.size() > 0){
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

    private void loadDynamicsData(int uid){
        handler = new ArchivesActivity.MyHandler(this);

        int page = mMeetList.size() / PAGE_SIZE;
        FormBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();
        if (isDebug) Log.d(TAG, "loadData requestBody:"+requestBody.toString()+" page:"+page+" uid:"+uid);
        HttpUtil.sendOkHttpRequest(this, DYNAMICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.body() != null){
                    String responseText = response.body().string();
                    Slog.d(TAG, "==========response text : "+responseText);
                    if(responseText != null){
                        List<MeetDynamics> tempList = parseDynamics(responseText);
                        mTempSize = 0;
                        if (null != tempList) {
                            mTempSize = tempList.size();
                            mMeetList.addAll(tempList);
                            Log.d(TAG, "getResponseText list.size:"+tempList.size());
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

    private List<MeetDynamics> parseDynamics(String responseText){
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

                    if(!dynamics.isNull("birth_year")){
                        meetDynamics.setBirthYear(dynamics.optInt("birth_year"));
                    }
                    if(!dynamics.isNull("height")){
                        meetDynamics.setHeight(dynamics.optInt("height"));
                    }
                    if(!dynamics.isNull("degree")){
                        meetDynamics.setDegree(dynamics.optString("degree"));
                    }
                    if(!dynamics.isNull("university")){
                        meetDynamics.setUniversity(dynamics.optString("university"));
                    }
                    if(!dynamics.isNull("job_title")){
                        meetDynamics.setJobTitle(dynamics.optString("job_title"));
                    }
                    if(!dynamics.isNull("lives")){
                        meetDynamics.setLives(dynamics.optString("lives"));
                    }
                    if(!dynamics.isNull("situation")){
                        meetDynamics.setSituation(dynamics.optInt("situation"));
                    }

                    //requirement
                    if(!dynamics.isNull("age_lower")){
                        meetDynamics.setAgeLower(dynamics.optInt("age_lower"));
                    }
                    if(!dynamics.isNull("age_upper")){
                        meetDynamics.setAgeUpper(dynamics.optInt("age_upper"));
                    }
                    if(!dynamics.isNull("requirement_height")){
                        meetDynamics.setRequirementHeight(dynamics.optInt("requirement_height"));
                    }
                    if(!dynamics.isNull("requirement_degree")){
                        meetDynamics.setRequirementDegree(dynamics.optString("requirement_degree"));
                    }
                    if(!dynamics.isNull("requirement_lives")){
                        meetDynamics.setRequirementLives(dynamics.optString("requirement_lives"));
                    }
                    if(!dynamics.isNull("requirement_sex")){
                        meetDynamics.setRequirementSex(dynamics.optInt("requirement_sex"));
                    }
                    if(!dynamics.isNull("illustration")){
                        meetDynamics.setIllustration(dynamics.optString("illustration"));
                    }
                    //interact count
                    meetDynamics.setBrowseCount(dynamics.optInt("browse_count"));
                    meetDynamics.setLovedCount(dynamics.optInt("loved_count"));
                    meetDynamics.setPraisedCount(dynamics.optInt("praised_count"));
                    meetDynamics.setLoved(dynamics.optInt("loved"));
                    meetDynamics.setPraised(dynamics.optInt("praised"));

                    //dynamics content
                    if(!dynamics.isNull("created")){
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
        if (isDebug) Log.d(TAG, "getDynamicsComment: aid:"+aid);
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
                Log.e(TAG, "onFailure e:"+e);
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
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private MeetDynamics getMeetDynamicsById(long aId) {
        for(int i = 0;i < mMeetList.size();i++) {
            if (aId == mMeetList.get(i).getAid()) {
                return mMeetList.get(i);
            }
        }
        return null;
    }
    
    @Override
    public void onBackFromRatingAndImpressionDialogFragment(boolean evaluated){
        if(evaluated){
            setEvaluatedMode();
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
            if(archivesActivity != null){
                archivesActivity.handleMessage(message);
            }
        }
    }

    public void handleMessage(Message message){
        switch (message.what){
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
                updateEvaluationHeader();
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
            default:
                break;
        }
    }

}
