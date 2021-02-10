package com.mufu.dynamics;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.mufu.adapter.MeetDynamicsListAdapter;
import com.mufu.adapter.MeetRecommendListAdapter;
import com.mufu.common.BaseAppCompatActivity;
import com.mufu.common.MyApplication;
import com.mufu.main.DynamicFragment;
import com.mufu.meet.MeetRecommendFragment;
import com.mufu.util.CommonDialogFragmentInterface;
import com.mufu.util.CommonUserListDialogFragment;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.InterActInterface;
import com.mufu.util.ParseUtils;
import com.mufu.util.RoundImageView;
import com.mufu.util.Slog;
import com.mufu.util.Utility;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.mufu.R;
import com.mufu.adapter.DynamicsInteractDetailsAdapter;
import com.mufu.main.MeetArchiveActivity;
import com.mufu.explore.ShareFragment;
import com.mufu.meet.UserMeetInfo;

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

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;
import static com.mufu.explore.ShareFragment.DYNAMICS_PRAISED;
import static com.mufu.explore.ShareFragment.GET_DYNAMICS_WITH_ID_URL;
import static com.mufu.explore.ShareFragment.REQUEST_INTERACT_URL;

public class DynamicsInteractDetailsActivity extends BaseAppCompatActivity implements CommonDialogFragmentInterface {
    private static final String TAG = "DynamicsInteractDetails";
    public static final String COMMENT_ADD_BROADCAST = "com.tongmenhui.action.COMMENT_ADD";
    private static final boolean isDebug = true;
    private MyHandler handler;
    private XRecyclerView commentRV;
    private Context mContext;
    private int type = DYNAMIC_COMMENT;
    private UserMeetInfo userMeetInfo;
    private int newCommentCount = 0;
    private int commentCount = 0;
    public static final int DYNAMIC_COMMENT = 0;
    public static final int MEET_RECOMMEND_COMMENT = 1;
    public static final int MY_CONDITION_COMMENT = 2;
    private static final int GET_COMMENT_DONE = 0;
    private static final int ADD_COMMENT_DONE = 1;
    private static final int ADD_REPLY_DONE = 2;
    private static final int NO_MORE = 3;
    private static final int UPDATE_PRAISED_COUNT = 4;
    private static final int GET_DYNAMIC_DONE = 7;
    private static final int GET_MEET_CONDITION_DONE = 8;
    private static final int GET_INTERACT_COUNT_DONE = 9;
    private List<DynamicsComment> dynamicsComments = new ArrayList<>();
    DynamicsInteractDetailsAdapter dynamicsInteractDetailsAdapter;
    private EditText commentEdit;
    private static final int PAGE_SIZE = 10;
    private JSONArray commentArray = new JSONArray();
    private InputMethodManager inputMethodManager;
    private boolean isComment = true;//reply is false
    private boolean isActive = false;
    private String authorName = "";
    private int authorUid = -1;
    private long did;
    private int uid;
    private long cid = -1;
    private int mPosition = -1;
    private TextView publish;
    private Dynamic dynamic;
    TextView dynamicsPraise;
    TextView dynamicsPraiseCount;
    TextView lovedView;
    TextView lovedIcon;
    TextView thumbsView;
    TextView thumbsIcon;
    TextView dynamicsCommentView;
    TextView commentCountView;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    View headerView = null;

    private static final String GET_COMMENT_URL = HttpUtil.DOMAIN + "?q=dynamic/interact/comment/get";
    private static final String GET_MEET_COMMENT_URL = HttpUtil.DOMAIN + "?q=meet/recommend/comment/get";
    private static final String ADD_COMMENT_URL = HttpUtil.DOMAIN + "?q=dynamic/interact/comment/add";
    private static final String ADD_MEET_COMMENT_URL = HttpUtil.DOMAIN + "?q=meet/recommend/comment/add";
    private static final String ADD_REPLY_URL = HttpUtil.DOMAIN + "?q=dynamic/reply/add";
    private static final String ADD_MEET_REPLY_URL = HttpUtil.DOMAIN + "?q=meet/recommend/reply/add";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dynamics_interact_details);
        ActionBar actionBar = getSupportActionBar();
        mContext = this;

        if (actionBar != null) {
            actionBar.hide();
        }
        type = getIntent().getIntExtra("type", DYNAMIC_COMMENT);

        initView();

        loadData();

    }
    
    private void initView(){
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.custom_actionbar), font);
        handler = new MyHandler(this);
        TextView backLeft = findViewById(R.id.left_back);

        backLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final LinearLayout dynamicsInteractLayout = findViewById(R.id.dynamics_interact_details_wrapper);
        
        dynamicsInteractLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            int minSoftHeight = 0;
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                // 测量当前窗口的显示区域
                DynamicsInteractDetailsActivity.this.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
                int screenHeight =  DynamicsInteractDetailsActivity.this.getWindow().getDecorView().getRootView().getHeight();

                int softHeight = screenHeight - rect.bottom;

                if (Math.abs(softHeight) > screenHeight / 5){
                    //dynamicsInteractLayout.scrollTo(0, softHeight - minSoftHeight);
                    if(!isComment){
                        commentEdit.setHint("回复 "+authorName);
                    }
                }else {
                    if(TextUtils.isEmpty(commentEdit.getText())){
                        commentEdit.setHint("请输入评论");
                    }
                    //dynamicsInteractLayout.scrollTo(0,0);
                    //minSoftHeight = softHeight;
                }

            }
        });
        
        commentRV = findViewById(R.id.dynamics_interact_details);
        dynamicsInteractDetailsAdapter = new DynamicsInteractDetailsAdapter(mContext, type);
        //reset commentRV layout_marginBottom
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        //commentRV.setLayoutManager(linearLayoutManager);

        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        commentRV.setLayoutManager(linearLayoutManager);

        commentRV.setRefreshProgressStyle(BallSpinFadeLoader);
        commentRV.setLoadingMoreProgressStyle(ProgressStyle.BallRotate);
        //mRecyclerView.setArrowImageView(R.drawable.);

        commentRV.getDefaultRefreshHeaderView().setRefreshTimeVisible(false);
        commentRV.setPullRefreshEnabled(false);
        commentRV.getDefaultFootView().setNoMoreHint(getString(R.string.no_more));
        
        // When the item number of the screen number is list.size-2,we call the onLoadMore
        commentRV.setLimitNumberToCallLoadMore(PAGE_SIZE - 2);
        commentRV.setRefreshProgressStyle(ProgressStyle.BallBeat);
        commentRV.setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);

        commentRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    dynamicsInteractDetailsAdapter.setScrolling(false);
                    dynamicsInteractDetailsAdapter.notifyDataSetChanged();
                } else {
                    dynamicsInteractDetailsAdapter.setScrolling(true);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        
        commentRV.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                //updateData();
            }

            @Override
            public void onLoadMore() {
                loadData();
            }
        });

        final EditText commentEdit = findViewById(R.id.edit_text);
        
        dynamicsInteractDetailsAdapter.setItemClickListener(new DynamicsInteractDetailsAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ParseUtils.startMeetArchiveActivity(mContext, dynamicsComments.get(position).getAuthorUid());
            }
        }, new InterActInterface() {
        @Override
                    public void onCommentClick(View view, final int position) {
                        //commentEdit.setFocusable(true);
                        //commentEdit.setFocusableInTouchMode(true);
                        Slog.d(TAG, "----------->position: "+position);
                        isComment = false;
                        commentEdit.requestFocus();
                        authorName = dynamicsComments.get(position).getAuthorName();
                        authorUid = dynamicsComments.get(position).getAuthorUid();
                        cid = dynamicsComments.get(position).getCommentId();
                        commentEdit.setHint("回复 "+authorName);
                        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        isActive = inputMethodManager.showSoftInput(commentEdit, 0);

                        mPosition = position;
                    }
                    
                    @Override
                    public void onPraiseClick(View view, int position){}

                    @Override
                    public void onDynamicPictureClick(View view, int position, String[] pictureUrlArray, int index){}
            
                    @Override
                    public void onOperationClick(View view, int position){}

                });

        commentRV.setAdapter(dynamicsInteractDetailsAdapter);

        progressImageView = findViewById(R.id.animal_progress);
        animationDrawable = (AnimationDrawable)progressImageView.getDrawable();
        progressImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        },50);

        setHeaderView();

        processComment();
    }
    
    private void setHeaderView(){
        if (type == DYNAMIC_COMMENT){
            dynamic = (Dynamic) getIntent().getSerializableExtra("dynamic");
            Slog.d(TAG, "---------------------------->dynamic: "+dynamic);
            if (dynamic == null){
                did = getIntent().getLongExtra("did", -1);
                if (did > -1){
                    getDynamic(did);
                }
            }else {
                did = dynamic.getDid();
                headerView = LayoutInflater.from(mContext).inflate(R.layout.meet_dynamics_item, (ViewGroup) findViewById(android.R.id.content), false);
                commentRV.addHeaderView(headerView);
                setDynamicHeaderView(headerView);
                setDynamicsInteract(dynamic);
            }
            
            }else {
            userMeetInfo = (UserMeetInfo)getIntent().getSerializableExtra("meetRecommend");
            if (userMeetInfo == null){
                cid = getIntent().getIntExtra("cid", -1);
                uid = getIntent().getIntExtra("uid", -1);
                if (cid > -1){
                    getUserMeetInfo(uid);
                }
            }else {
                cid = userMeetInfo.getCid();
                headerView = LayoutInflater.from(mContext).inflate(R.layout.meet_item, (ViewGroup) findViewById(android.R.id.content), false);
                commentRV.addHeaderView(headerView);
                setMeetHeaderView(headerView);
            }
        }

    }
    public void getDynamic(long did){
        Slog.d(TAG, "---------------------->getDynamic did: "+did);
        RequestBody requestBody = new FormBody.Builder().add("did", String.valueOf(did)).build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_DYNAMICS_WITH_ID_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug) Log.d(TAG, "getDynamic responseText:" + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject relatedContentJSONObject = new JSONObject(responseText).optJSONObject("dynamic");
                        ShareFragment shareFragment = new ShareFragment();
                        if (relatedContentJSONObject != null) {
                            dynamic = shareFragment.setMeetDynamics(relatedContentJSONObject);
                        }
                        if (dynamic != null){
                            setDynamicsInteract(dynamic);
                            handler.sendEmptyMessage(GET_DYNAMIC_DONE);
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
    
    JSONObject commentResponse = null;
    JSONArray praiseArray = null;

    public void setDynamicsInteract(final Dynamic dynamic) {

        RequestBody requestBody = new FormBody.Builder().add("did", String.valueOf(dynamic.getDid())).build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), REQUEST_INTERACT_URL, requestBody, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "-------> setDynamicsInteract response: "+responseText);
                int commentCount = 0;
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        commentResponse = new JSONObject(responseText);
                        commentCount = commentResponse.getInt("comment_count");
                        praiseArray = commentResponse.getJSONArray("praise");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if(commentCount > 0){
                        dynamic.setCommentCount(commentCount);
                    }
                    dynamic.setPraisedDynamics(commentResponse.optInt("praised"));

                    if (null != praiseArray && praiseArray.length() > 0) {
                        dynamic.setPraisedDynamicsCount(praiseArray.length());
                    }
                }
                
                handler.sendEmptyMessage(GET_INTERACT_COUNT_DONE);

            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
    public void getUserMeetInfo(int uid) {
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), ParseUtils.GET_MEET_ARCHIVE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug) Log.d(TAG, "getUserMeetInfo responseText:" + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject relatedContentJSONObject = new JSONObject(responseText).optJSONObject("archive");
                        userMeetInfo = ParseUtils.setMeetMemberInfo(relatedContentJSONObject);
                        handler.sendEmptyMessage(GET_MEET_CONDITION_DONE);
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
    
    private void setDynamicHeaderView(View view){

        ConstraintLayout baseProfile = view.findViewById(R.id.base_profile);
        TextView name =  view.findViewById(R.id.name);
        name.setText(dynamic.getNickName());
        TextView living =  view.findViewById(R.id.living);
        living.setText(dynamic.getLiving());
        RoundImageView avatar =  view.findViewById(R.id.avatar);
        String avatarUrl = dynamic.getAvatar();
        
        if (avatarUrl != null && !"".equals(avatarUrl)) {
            if (mContext != null){
                Glide.with(mContext).load(HttpUtil.DOMAIN  + avatarUrl).into(avatar);
            }else {
                Glide.with(MyApplication.getContext()).load(HttpUtil.DOMAIN  + avatarUrl).into(avatar);
            }
        } else {
            if(dynamic.getSex() == 0){
                avatar.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.male_default_avator));
            }else {
                avatar.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.female_default_avator));
            }
        }
        
        TextView profile = view.findViewById(R.id.profile);
        String profileString = "";
        if (dynamic.getSituation() != -1){
            if(dynamic.getSituation() == 0){
                profileString = dynamic.getMajor()+"·"+ dynamic.getDegreeName(dynamic.getDegree())+"·"+ dynamic.getUniversity();
            }else {
                profileString = dynamic.getPosition()+"·"+ dynamic.getIndustry();
            }
            profile.setText(profileString);
        }

        TextView contentView = view.findViewById(R.id.dynamics_content);
        contentView.setText(dynamic.getContent());
        
        String pictures = dynamic.getDynamicPicture();
        if (!"".equals(pictures)) {
            setDynamicContentView(view, pictures);
        }
        //createdView = (TextView) view.findViewById(R.id.dynamic_time);

        Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(headerView.findViewById(R.id.dynamics_content_meta), font);
        FontManager.markAsIconContainer(view.findViewById(R.id.living_icon), font);

    }
    
    private void setDynamicInterActCountView(){
        dynamicsPraiseCount = headerView.findViewById(R.id.dynamic_praise_count);
        if(dynamic.getPraisedDynamicsCount() > 0){
            dynamicsPraiseCount.setText(String.valueOf(dynamic.getPraisedDynamicsCount()));
        }else {
            dynamicsPraiseCount.setText("");
        }

        dynamicsPraise = headerView.findViewById(R.id.dynamic_praise);
        if(dynamic.getPraisedDynamics() == 1){
            dynamicsPraise.setText(mContext.getResources().getString(R.string.fa_thumbs_up));
        }else {
            dynamicsPraise.setText(mContext.getResources().getString(R.string.fa_thumbs_O_up));
        }
        
        dynamicsCommentView = headerView.findViewById(R.id.dynamic_comment);
        commentCount = dynamic.getCommentCount();
        if(commentCount > 0){
            dynamicsCommentView.setText(mContext.getResources().getString(R.string.fa_comment_o)+" "+String.valueOf(commentCount));
        }else {
            dynamicsCommentView.setText("");
            dynamicsCommentView.setText(mContext.getResources().getString(R.string.fa_comment_o));
        }

        dynamicsPraise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO change UI to show parised or no
                if (1 == dynamic.getPraisedDynamics()) {
                    Toast.makeText(mContext, "You have praised it!", Toast.LENGTH_SHORT).show();
                    return;
                }
                praiseDynamics(dynamic);
            }
        });
        
        dynamicsPraiseCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putInt("type", DYNAMICS_PRAISED);
                bundle.putLong("did", dynamic.getDid());
                bundle.putString("title", "赞了该动态");
                CommonUserListDialogFragment commonUserListDialogFragment = new CommonUserListDialogFragment();
                commonUserListDialogFragment.setArguments(bundle);
                commonUserListDialogFragment.show(getSupportFragmentManager(), "CommonUserListDialogFragment");
            }
        });

    }

private void setDynamicContentView(View view, String pictures){
        int width;
        int height;
        Resources resources = mContext.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int innerWidth = dm.widthPixels - (int) Utility.dpToPx(mContext, 32f);

        GridLayout dynamicsGrid = view.findViewById(R.id.dynamics_picture_grid);
        final String[] picture_array = pictures.split(":");
        final int length = picture_array.length;
        
        if (length > 0) {
            if (length != 4) {
                if(length < 4){
                    dynamicsGrid.setColumnCount(length);
                    if(length == 1){
                        width = innerWidth/2;
                        height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    }else {
                        width = (innerWidth - (int) Utility.dpToPx(mContext, 2f)*(length-1))/length;
                        height = width;
                    }

                }else {
                    dynamicsGrid.setColumnCount(3);
                    width = (innerWidth - (int)Utility.dpToPx(mContext, 2f)*2)/3;
                    height = width;
                }
            } else {
                dynamicsGrid.setColumnCount(2);
                width = (innerWidth)/2;
                height = width;
            }
            
            final RequestOptions requestOptions = new RequestOptions()
                    //.placeholder(mContext.getDrawable(R.mipmap.hetang_icon))
                    .placeholder(mContext.getResources().getDrawable(R.drawable.icon))
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE);
            final List<Drawable> drawableList = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                if (picture_array[i] != null) {
                    //LinearLayout linearLayout = new LinearLayout(mContext);
                    final RoundImageView picture = new RoundImageView(mContext);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
                    layoutParams.setMargins(0, 0, 2, 4);
                    
                    //将以上的属性赋给LinearLayout
                    picture.setLayoutParams(layoutParams);
                    picture.setAdjustViewBounds(true);
                    if(length == 1){
                        picture.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    }else {
                        picture.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }
                    picture.setMaxHeight(2 * width);

                    dynamicsGrid.addView(picture);
                    Glide.with(mContext).load(HttpUtil.DOMAIN + picture_array[i]).apply(requestOptions).into(picture);
                    
                    picture.setId(i);
                    picture.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //interActInterface.onDynamicPictureClick(view, position, picture_array, picture.getId());
                        }
                    });
                }
            }
        }
    }
    
    private void praiseDynamics(final Dynamic dynamic) {

        RequestBody requestBody = new FormBody.Builder().add("did", String.valueOf(dynamic.getDid())).build();
        HttpUtil.sendOkHttpRequest(getApplicationContext(), MeetDynamicsListAdapter.PRAISED_DYNAMICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                
                if (isDebug) Log.d(TAG, "praiseDynamics responseText:" + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject commentResponse = new JSONObject(responseText);
                        int status = commentResponse.optInt("status");
                        if (isDebug) Log.d(TAG, "praiseDynamics status:" + status);
                        if (1 == status) {
                            dynamic.setPraisedDynamics(1);
                            sendMessage(UPDATE_PRAISED_COUNT);
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

    private void setMeetHeaderView(View view){

        view.setOnClickListener(new View.OnClickListener() {
        
        @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyApplication.getContext(), MeetArchiveActivity.class);
                intent.putExtra("meet", userMeetInfo);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });

        TextView name = view.findViewById(R.id.name);
        name.setText(userMeetInfo.getNickName());

        TextView selfcondition = view.findViewById(R.id.self_condition);
        selfcondition.setText(userMeetInfo.getSelfCondition());
        
        RoundImageView avatar = view.findViewById(R.id.avatar);
        String avatarUrl = userMeetInfo.getAvatar();
        if (avatarUrl != null && !"".equals(avatarUrl)) {
            Glide.with(mContext).load(HttpUtil.DOMAIN + avatarUrl).into(avatar);
        } else {
            if(userMeetInfo.getSex() == 0){
                avatar.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.male_default_avator));
            }else {
                avatar.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.female_default_avator));
            }
        }

        TextView visitIcon = view.findViewById(R.id.eye_icon);
        TextView visitRecord = view.findViewById(R.id.visit_record);
        if (userMeetInfo.getVisitCount() > 0){
            visitRecord.setText(String.valueOf(userMeetInfo.getVisitCount()));
            visitIcon.setVisibility(View.VISIBLE);
        }else {
            visitIcon.setVisibility(View.GONE);
        }
        
        TextView illustration = view.findViewById(R.id.illustration);
        if(!TextUtils.isEmpty(userMeetInfo.getIllustration())){
            illustration.setVisibility(View.VISIBLE);
            illustration.setText("“"+userMeetInfo.getIllustration()+"”");
        }else {
            illustration.setVisibility(View.GONE);
        }

        TextView degreeView = view.findViewById(R.id.degree);
        String degree = userMeetInfo.getDegreeName(userMeetInfo.getDegree());
        if (!TextUtils.isEmpty(degree)){
            degreeView.setText(degree);
        }

        if (!TextUtils.isEmpty(userMeetInfo.getUniversity())){
            TextView university = view.findViewById(R.id.university);
            university.setText(userMeetInfo.getUniversity()+getResources().getString(R.string.dot));
        }
        
//        TextView status = view.findViewById(R.id.status);
        if (userMeetInfo.getSituation() == MeetRecommendFragment.student){
            /*
            if (status.getVisibility() == View.GONE){
                status.setVisibility(View.VISIBLE);
            }

             */
        }else {
            /*
            if (status.getVisibility() != View.GONE){
                status.setVisibility(View.GONE);
            }

             */
            LinearLayout workInfo = view.findViewById(R.id.work_info);
            if (workInfo.getVisibility() == View.GONE){
                workInfo.setVisibility(View.VISIBLE);
            }
            TextView position = view.findViewById(R.id.position);
            String jobPosition = userMeetInfo.getPosition();
            if (!TextUtils.isEmpty(jobPosition)){
                position.setText(jobPosition);
            }
            TextView industryView = view.findViewById(R.id.industry);
            String industry = userMeetInfo.getIndustry();
            if (!TextUtils.isEmpty(industry)){
                industryView.setText(industry);
            }
        }

        TextView living = view.findViewById(R.id.living);
        living.setText(userMeetInfo.getLiving());
        TextView homeTown = view.findViewById(R.id.hometown);
        if (!TextUtils.isEmpty(userMeetInfo.getHometown())){
            homeTown.setText(mContext.getResources().getString(R.string.dot)+userMeetInfo.getHometown()+"人");
        }

        lovedView = view.findViewById(R.id.loved_statistics);
        if(userMeetInfo.getLovedCount() > 0){
            lovedView.setText(String.valueOf(userMeetInfo.getLovedCount()));
        }
        
        lovedIcon = view.findViewById(R.id.loved_icon);
        if(userMeetInfo.getLovedCount() > 0 ){
            if(userMeetInfo.getLoved() == 1 ){
                lovedIcon.setText(R.string.fa_heart);
            }
        }else {
            lovedIcon.setText(R.string.fa_heart_o);
        }
        thumbsView = view.findViewById(R.id.thumbs_up_statistics);
        if(userMeetInfo.getPraisedCount() > 0){
            thumbsView.setText(String.valueOf(userMeetInfo.getPraisedCount()));
        }
        
        thumbsIcon = view.findViewById(R.id.thumbs_up_icon);
        if(userMeetInfo.getPraisedCount() > 0 ){
            if(userMeetInfo.getPraised() == 1 ){
                thumbsIcon.setText(R.string.fa_thumbs_up);
            }
        }else {
            thumbsIcon.setText(R.string.fa_thumbs_O_up);
        }


        TextView activityIndicator = view.findViewById(R.id.activity_indicator);
        if(userMeetInfo.getActivityCount() > 0){
            activityIndicator.setVisibility(View.VISIBLE);
        }else {
            activityIndicator.setVisibility(View.GONE);
        }
        
        commentCountView = view.findViewById(R.id.comment_count);
        commentCount = userMeetInfo.getCommentCount();
        Slog.d(TAG, "----------------------->commentCount: "+commentCount);
        if(commentCount > 0){
            commentCountView.setText(String.valueOf(commentCount));
        }

        lovedIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (1 == userMeetInfo.getLoved()) {
                    Toast.makeText(mContext, "You have loved it!", Toast.LENGTH_SHORT).show();
                    return;
                }
                love(userMeetInfo);
            }
        });
        
        thumbsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO change UI to show parised or no
                if (1 == userMeetInfo.getPraised()) {
                    Toast.makeText(mContext, "You have praised it!", Toast.LENGTH_SHORT).show();
                    return;
                }
                praiseArchives(userMeetInfo);
            }
        });
        
        Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(view.findViewById(R.id.behavior_statistics), font);
        FontManager.markAsIconContainer(view.findViewById(R.id.living_icon), font);
        FontManager.markAsIconContainer(view.findViewById(R.id.activity_indicator), font);
    }
    
    private void love(final UserMeetInfo userMeetInfo) {
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(userMeetInfo.getUid())).build();
        HttpUtil.sendOkHttpRequest(mContext, MeetRecommendListAdapter.LOVED_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug) Log.d(TAG, "love responseText" + responseText);
                
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject commentResponse = new JSONObject(responseText);
                        int status = commentResponse.optInt("status");
                        if (isDebug) Log.d(TAG, "love status" + status);
                        if (1 == status) {
                            //UserMeetInfo member = getMeetMemberById(userMeetInfo.getUid());
                            userMeetInfo.setLoved(1);
                            userMeetInfo.setLovedCount(userMeetInfo.getLovedCount() + 1);
                            sendMessage(MeetRecommendListAdapter.UPDATE_LOVE_COUNT);
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

    private void praiseArchives(final UserMeetInfo userMeetInfo) {
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(userMeetInfo.getUid())).build();
        HttpUtil.sendOkHttpRequest(mContext, MeetRecommendListAdapter.PRAISED_URL, requestBody, new Callback() {
        
        @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug) Log.d(TAG, "praiseArchives responseText:" + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject commentResponse = new JSONObject(responseText);
                        int status = commentResponse.optInt("status");
                        if (isDebug) Log.d(TAG, "praiseArchives status:" + status);
                        if (1 == status) {
                            //UserMeetInfo member = getMeetMemberById(userMeetInfo.getUid());
                            userMeetInfo.setPraised(1);
                            userMeetInfo.setPraisedCount(userMeetInfo.getPraisedCount() + 1);
                            sendMessage(MeetRecommendListAdapter.UPDATE_RECOMMEND_PRAISED_COUNT);
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
    
    private void processComment(){
        publish = findViewById(R.id.publish);

        commentEdit = findViewById(R.id.edit_text);
        TextWatcher textWatcher = new TextWatcher() {

            private CharSequence temp;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                temp = s;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {   }
            @Override
            public void afterTextChanged(Editable s) {
                if (temp.length() > 0) {
                    publish.setEnabled(true);
                    publish.setClickable(true);
                    publish.setBackgroundColor(ContextCompat.getColor(MyApplication.getContext(), R.color.color_blue));

                } else {
                    publish.setEnabled(false);
                    publish.setClickable(false);
                    publish.setBackgroundColor(ContextCompat.getColor(MyApplication.getContext(), R.color.color_disabled));

                }
            }
        };
        
        commentEdit.addTextChangedListener(textWatcher);

        commentEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!TextUtils.isEmpty(commentEdit.getText())){
                    if(!isComment){
                        commentEdit.setText("");
                        commentEdit.setHint("请输入评论");
                    }
                }

                isComment = true;

            }
        });
        
        publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressDialog("正在发布");
                if(isComment){
                    addComment(commentEdit.getText().toString());
                }else {
                    Slog.d(TAG, "----------------reply position: "+mPosition);
                    addReply();

                }

            }
        });
    }
    
    private void addComment(String comment){
        FormBody.Builder builder = new FormBody.Builder();
        String address = "";
        if (type == DYNAMIC_COMMENT){
            builder.add("did", String.valueOf(did));
            address = ADD_COMMENT_URL;
        }else {
            builder.add("condition_id", String.valueOf(cid));
            address = ADD_MEET_COMMENT_URL;
        }
        
        RequestBody requestBody = builder.add("content", comment).build();
        HttpUtil.sendOkHttpRequest(mContext, address, requestBody, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if(isDebug) Slog.d(TAG, "----------------->add comment response: "+responseText);

                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseText).optJSONObject("comment");
                        DynamicsComment dynamicsComment = new DynamicsComment();
                        setComment(jsonObject, dynamicsComment);
                        dynamicsComments.add(0, dynamicsComment);
                        handler.sendEmptyMessage(ADD_COMMENT_DONE);
                        dismissProgressDialog();

                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void addReply(){
        Slog.d(TAG, "---------------->addReply cid: "+cid+" position: "+mPosition+" uid: "+authorUid+" content: "+commentEdit.getText().toString());
        String address = "";
        FormBody.Builder builder = new FormBody.Builder();
        if (type == DYNAMIC_COMMENT){
            address = ADD_REPLY_URL;
        }else {
            address = ADD_MEET_REPLY_URL;
        }
        
        RequestBody requestBody = new FormBody.Builder()
                .add("cid", String.valueOf(cid))
                .add("uid", String.valueOf(authorUid))
                .add("content", commentEdit.getText().toString()).build();

        HttpUtil.sendOkHttpRequest(mContext, address, requestBody, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                
                if(isDebug) Slog.d(TAG, "----------------->add reply response: "+responseText);

                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject replyObj = new JSONObject(responseText).optJSONObject("reply");
                        DynamicsComment dynamicsComment = new DynamicsComment();
                        setReply(replyObj, dynamicsComment);
                        dynamicsComments.get(mPosition).replyList.add(dynamicsComment);

                        handler.sendEmptyMessage(ADD_REPLY_DONE);
                        dismissProgressDialog();

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

    private void loadData(){

        int page = dynamicsComments.size() / PAGE_SIZE;
        Slog.d(TAG, "-------------------->loadData did: "+did);
        String address = "";
        FormBody.Builder builder = new FormBody.Builder();
        
        switch (type){
            case DYNAMIC_COMMENT:
                builder.add("did", String.valueOf(did));
                address = GET_COMMENT_URL;
                break;
            case MEET_RECOMMEND_COMMENT:
            case MY_CONDITION_COMMENT:
                builder.add("condition_id", String.valueOf(cid));
                address = GET_MEET_COMMENT_URL;
                break;
                default:
                    break;
        }

        RequestBody requestBody = builder.add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();
                
                HttpUtil.sendOkHttpRequest(mContext, address, requestBody, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();

                if(isDebug) Slog.d(TAG, "----------------->comment response: "+responseText);

                if (!TextUtils.isEmpty(responseText)) {
                try {
                        commentArray = new JSONObject(responseText).optJSONArray("comment");
                        Slog.d(TAG, "-------------------->loadData commentArray length: "+commentArray.length());
                        if(commentArray.length() > 0){
                            setCommentResponse();

                            Message msg = new Message();
                            msg.what = GET_COMMENT_DONE;
                            handler.sendMessage(msg);

                        }else {
                            handler.sendEmptyMessage(NO_MORE);
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

    private void setCommentResponse(){
        for (int i=0; i<commentArray.length(); i++){

            try {
                JSONObject commentObj = commentArray.getJSONObject(i);
                DynamicsComment dynamicsComment = new DynamicsComment();
                setComment(commentObj, dynamicsComment);
                dynamicsComments.add(dynamicsComment);
            }catch (JSONException e){
                e.printStackTrace();
            }

        }
    }
    
    private void setComment(JSONObject commentObj, DynamicsComment dynamicsComment){
        dynamicsComment.setAvatar(commentObj.optString("avatar"));
        dynamicsComment.setNickName(commentObj.optString("nickname"));
        dynamicsComment.setAuthorName(commentObj.optString("nickname"));
        dynamicsComment.setAuthorUid(commentObj.optInt("uid"));
        dynamicsComment.setSex(commentObj.optInt("sex"));
        dynamicsComment.setSituation(commentObj.optInt("situation"));
        if(commentObj.optInt("situation") != -1){
            if(commentObj.optInt("situation") == 0){
                dynamicsComment.setDegree(commentObj.optString("degree"));
                dynamicsComment.setUniversity(commentObj.optString("university"));
            }else {
                dynamicsComment.setPosition(commentObj.optString("position"));
                dynamicsComment.setIndustry(commentObj.optString("industry"));
            }
        }
        
        dynamicsComment.setContent(commentObj.optString("content"));
        dynamicsComment.setPraiseCount(commentObj.optInt("praiseCount"));
        dynamicsComment.setCommentId(commentObj.optInt("cid"));

        JSONArray replyArray = commentObj.optJSONArray("replies");
        if(replyArray != null && replyArray.length() > 0){
            for (int i=0; i<replyArray.length(); i++){
                try {
                    JSONObject replyObj = replyArray.getJSONObject(i);
                    DynamicsComment dynamicsCommentReply = new DynamicsComment();
                    setReply(replyObj, dynamicsCommentReply);
                    dynamicsComment.replyList.add(dynamicsCommentReply);
                }catch (JSONException e){
                    e.printStackTrace();
                }

            }
        }
    }
     private void setReply(JSONObject replyObj, DynamicsComment dynamicsComment){
        dynamicsComment.setAvatar(replyObj.optString("avatar"));
        dynamicsComment.setAuthorName(replyObj.optString("author_name"));
        dynamicsComment.setAuthorUid(replyObj.optInt("author_uid"));
        dynamicsComment.setReplierName(replyObj.optString("replier_name"));
        dynamicsComment.setReplierUid(replyObj.optInt("replier_uid"));
        dynamicsComment.setReplierSex(replyObj.optInt("replier_sex"));
        Slog.d(TAG, "-------------------->replier_sex: "+replyObj.optInt("replier_sex"));
        dynamicsComment.setRid(replyObj.optLong("rid"));
        dynamicsComment.setCommentId(replyObj.optLong("cid"));

        dynamicsComment.setReplyContent(replyObj.optString("content"));
        dynamicsComment.setReplyPraiseCount(replyObj.optInt("praiseCount"));
    }
    
    public void handleMessage(Message msg){
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isActive();
        switch (msg.what){
            case GET_DYNAMIC_DONE:
                headerView = LayoutInflater.from(mContext).inflate(R.layout.meet_dynamics_item, (ViewGroup) findViewById(android.R.id.content), false);
                commentRV.addHeaderView(headerView);
                setDynamicHeaderView(headerView);
                break;
                
                case GET_INTERACT_COUNT_DONE:
                if (type == DYNAMIC_COMMENT){
                    setDynamicInterActCountView();
                    if(dynamic.getCommentCount() == 0){
                        dynamicsInteractDetailsAdapter.setData(dynamicsComments, false);
                        dynamicsInteractDetailsAdapter.notifyDataSetChanged();
                    }
                }else {

                }
                break;
            case GET_MEET_CONDITION_DONE:
                View meetHeaderView = LayoutInflater.from(mContext).inflate(R.layout.meet_discovery_item, (ViewGroup) findViewById(android.R.id.content), false);
                commentRV.addHeaderView(meetHeaderView);
                setMeetHeaderView(meetHeaderView);
                break;
                
                case NO_MORE:
                commentRV.setNoMore(true);
                commentRV.setLoadingMoreEnabled(false);
                if (progressImageView.getVisibility() == View.VISIBLE){
                    animationDrawable.stop();
                    progressImageView.setVisibility(View.GONE);
                }
                break;
            case GET_COMMENT_DONE:
                if (progressImageView.getVisibility() == View.VISIBLE){
                    animationDrawable.stop();
                    progressImageView.setVisibility(View.GONE);
                }
                
                if(dynamicsComments.size() > 0){
                    dynamicsInteractDetailsAdapter.setData(dynamicsComments, false);
                    dynamicsInteractDetailsAdapter.notifyDataSetChanged();
                    commentRV.refreshComplete();

                    if(commentArray.length() < PAGE_SIZE){
                        commentRV.setLoadingMoreEnabled(false);
                    }
                }else {
                    commentRV.setNoMore(true);
                }

                break;
                
                case ADD_COMMENT_DONE:

                newCommentCount++;
                commentEdit.clearFocus();
                commentEdit.setText("");

                if(isOpen){
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                }

                dynamicsInteractDetailsAdapter.setData(dynamicsComments, false);

                dynamicsInteractDetailsAdapter.notifyItemRangeInserted(0, 1);
                dynamicsInteractDetailsAdapter.notifyDataSetChanged();
                commentRV.scrollToPosition(0);

                if (type == DYNAMIC_COMMENT){
                    dynamicsCommentView.setText(mContext.getResources().getString(R.string.fa_comment_o)+" "+String.valueOf(commentCount+newCommentCount));
                }else {
                    commentCountView.setText(String.valueOf(commentCount+newCommentCount));
                }
                setCommentUpdateResult();

                break;
                
                case ADD_REPLY_DONE:
                newCommentCount++;
                commentEdit.clearFocus();
                commentEdit.setText("");
                if(isOpen){
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                }
                dynamicsInteractDetailsAdapter.setData(dynamicsComments, true);
                dynamicsInteractDetailsAdapter.notifyDataSetChanged();
                if (type == DYNAMIC_COMMENT){
                    dynamicsCommentView.setText(mContext.getResources().getString(R.string.fa_comment_o)+" "+String.valueOf(commentCount+newCommentCount));
                }else {
                    commentCountView.setText(String.valueOf(commentCount+newCommentCount));
                }
                setCommentUpdateResult();
                break;
                
                case UPDATE_PRAISED_COUNT:
                dynamicsPraiseCount.setText(String.valueOf(dynamic.getPraisedDynamicsCount()+1));
                dynamicsPraise.setText(R.string.fa_thumbs_up);
                setPraiseUpdateResult();
                break;
            case MeetRecommendListAdapter.UPDATE_LOVE_COUNT:
                lovedView.setText(String.valueOf(userMeetInfo.getLovedCount()));
                lovedIcon.setText(R.string.fa_heart);
                setLoveUpdateResult();
                break;
            case MeetRecommendListAdapter.UPDATE_RECOMMEND_PRAISED_COUNT:
                thumbsView.setText(String.valueOf(userMeetInfo.getPraisedCount()));
                thumbsIcon.setText(R.string.fa_thumbs_up);
                setPraiseUpdateResult();
                break;
                default:
                    break;

        }

    }
    
    public void setCommentUpdateResult(){
        Slog.d(TAG, "----->setCommentUpdateResult comment count: "+commentCount+newCommentCount);
        Intent intent = new Intent();
        intent.putExtra("commentCount", commentCount+newCommentCount);
        if (type == MY_CONDITION_COMMENT){
            setResult(DynamicFragment.MY_COMMENT_UPDATE_RESULT, intent);
        }else {
            setResult(DynamicFragment.COMMENT_UPDATE_RESULT, intent);
        }
    }

    public void setPraiseUpdateResult(){
        Slog.d(TAG, "----->setPraiseUpdateResult");
        Intent intent = new Intent();
        if (type == MY_CONDITION_COMMENT){
            setResult(DynamicFragment.MY_PRAISE_UPDATE_RESULT, intent);
        }else {
            setResult(DynamicFragment.PRAISE_UPDATE_RESULT, intent);
        }
    }
    
    public void setLoveUpdateResult(){
        Slog.d(TAG, "----->setPraiseUpdateResult");
        Intent intent = new Intent();
        if (type == MY_CONDITION_COMMENT){
            setResult(DynamicFragment.MY_LOVE_UPDATE_RESULT, intent);
        }else {
            setResult(DynamicFragment.LOVE_UPDATE_RESULT, intent);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        /*
        if(newCommentCount > 0){
           // sendBroadcast();
            int Count = commentCount+newCommentCount;
            Slog.d(TAG, "----->onDestroy setCommentUpdateResult comment count: "+Count);
            Intent intent = new Intent();
            intent.putExtra("commentCount", commentCount+newCommentCount);
            setResult(RESULT_OK, intent);
        }
        */

    }

    private void sendMessage(int what, Object obj) {
        Message msg = handler.obtainMessage();
        msg.what = what;
        msg.obj = obj;
        msg.sendToTarget();
    }
    
    private void sendMessage(int what) {
        sendMessage(what, null);
    }

    @Override
    public void onBackFromDialog(int type, int result, boolean status) { }

    static class MyHandler extends Handler {
        WeakReference<DynamicsInteractDetailsActivity> dynamicsInteractDetailsActivityWeakReference;
        MyHandler(DynamicsInteractDetailsActivity dynamicsInteractDetailsActivity) {
            dynamicsInteractDetailsActivityWeakReference = new WeakReference<DynamicsInteractDetailsActivity>(dynamicsInteractDetailsActivity);
        }

        @Override
        public void handleMessage(Message message) {
            DynamicsInteractDetailsActivity dynamicsInteractDetailsActivity = dynamicsInteractDetailsActivityWeakReference.get();
            if (dynamicsInteractDetailsActivity != null) {
                dynamicsInteractDetailsActivity.handleMessage(message);
            }
        }
    }
}
            
        
        
