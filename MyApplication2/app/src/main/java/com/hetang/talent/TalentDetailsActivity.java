package com.hetang.talent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.adapter.ConsultSummaryAdapter;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.common.InvitationDialogFragment;
import com.hetang.common.MyApplication;
import com.hetang.common.SetAvatarActivity;
import com.hetang.consult.ConsultSummaryActivity;
import com.hetang.consult.TalentConsultDF;
import com.hetang.contacts.ChatActivity;
import com.hetang.experience.GuideDetailActivity;
import com.hetang.group.SingleGroupActivity;
import com.hetang.group.SubGroupActivity;
import com.hetang.group.SubGroupDetailsActivity;
import com.hetang.meet.FillMeetInfoActivity;
import com.hetang.meet.UserMeetInfo;
import com.hetang.picture.GlideEngine;
import com.hetang.util.CommonDialogFragmentInterface;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.MyLinearLayoutManager;
import com.hetang.util.ParseUtils;
import com.hetang.util.RoundImageView;
import com.hetang.util.SharedPreferencesUtils;
import com.hetang.util.Slog;
import com.hetang.util.UserProfile;
import com.hetang.util.Utility;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.entity.LocalMedia;
import com.tencent.imsdk.TIMConversationType;
import com.tencent.qcloud.tim.uikit.modules.chat.base.ChatInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hetang.common.MyApplication.getContext;
import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.hetang.group.GroupFragment.eden_group;
import static com.hetang.consult.ConsultDetailActivity.GET_QUESTION_BY_CID;
import static com.hetang.consult.ConsultSummaryActivity.CONSULT_GET_BY_TID;
import static com.hetang.consult.ConsultSummaryActivity.getConsult;
import static com.hetang.group.SingleGroupActivity.getSingleGroup;
import static com.hetang.group.SubGroupActivity.getTalent;
import static com.hetang.group.SubGroupDetailsActivity.AUTHENTICATING;
import static com.hetang.group.SubGroupDetailsActivity.REJECTED;
import static com.hetang.group.SubGroupDetailsActivity.SHOW_NOTICE_DIALOG;
import static com.hetang.group.SubGroupDetailsActivity.UNAUTHENTICATED;
import static com.hetang.meet.FillMeetInfoActivity.FILL_MEET_INFO_BROADCAST;
import static com.hetang.talent.RewardDialogFragment.COMMON_TALENT_REWARD_RESULT_OK;
import static com.hetang.talent.TalentEvaluateDialogFragment.SET_EVALUATE_RESULT_OK;
import static com.hetang.talent.TalentModifyDialogFragment.TALENT_MODIFY_RESULT_OK;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class TalentDetailsActivity extends BaseAppCompatActivity implements CommonDialogFragmentInterface{
    private static final String TAG = "TalentDetailsActivity";
    private static final boolean isDebug = true;
    private static final String GET_EVALUATION_STATISTICS_URL = HttpUtil.DOMAIN + "?q=talent/evaluation/get_statistics";
    public static final String GET_TALENT_byID = HttpUtil.DOMAIN + "?q=talent/get_by_id";
    private static final int LOAD_TALENT_DONE = 1;
    private static final int GET_EVALUATION_DONE = 2;
    public final static int TALENT_CONSULT = 3;
    private static final int nonMEMBER = -1;
    private static final int INVITTED = 1;
    private static final int GET_ALL_CONSULT_DONE = 4;
    private static final int GET_ALL_CONSULT_END = 5;
    private static final int NO_CONSULT_MORE = 6;
    private static final int GET_TALENT_INFO_DONE = 7;
    private static final int GET_NEW_ADD_CONSULT_DONE = 8;
    private static final int PAGE_SIZE = 8;
    SingleGroupActivity.SingleGroup singleGroup;
    private Context mContext;
    private Handler handler = null;
    private int tid = -1;
    private SubGroupActivity.Talent talent;
    private Bundle savedInstanceState;
    private GridLayout gridLayout;
    private JSONObject memberJSONObject = new JSONObject();
    private SubGroupDetailsActivity subGroupDetailsActivity;
    private SingleGroupReceiver mReceiver = new SingleGroupReceiver();
    private int mLoadSize = 0;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    private XRecyclerView recyclerView;
    private ConsultSummaryAdapter consultSummaryAdapter;
    private ConsultSummaryActivity consultSummaryActivity;
    private List<ConsultSummaryActivity.Consult> mConsultList = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        this.savedInstanceState = savedInstanceState;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.talent_details);
        consultSummaryActivity = new ConsultSummaryActivity();
        handler = new MyHandler(this);
        tid = getIntent().getIntExtra("tid", 0);
        initView();
        getTalentDetails();
    }
    
    private void initView() {
        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.custom_actionbar), font);

        handler = new MyHandler(this);
        recyclerView = findViewById(R.id.consult_summary_list);
        consultSummaryAdapter = new ConsultSummaryAdapter(getContext());
        MyLinearLayoutManager linearLayoutManager = new MyLinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setRefreshProgressStyle(BallSpinFadeLoader);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallRotate);

        recyclerView.setPullRefreshEnabled(false);
        recyclerView.getDefaultRefreshHeaderView().setRefreshTimeVisible(true);
        recyclerView.getDefaultFootView().setLoadingHint(getString(R.string.loading_pull_up_tip));
        
        recyclerView.setRefreshProgressStyle(ProgressStyle.BallBeat);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    consultSummaryAdapter.setScrolling(false);
                    consultSummaryAdapter.notifyDataSetChanged();
                } else {
                    consultSummaryAdapter.setScrolling(true);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        
        recyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                //updateData();
            }

            @Override
            public void onLoadMore() {
                 loadTalentConsult();
            }
        });
        
        consultSummaryAdapter.setItemClickListener(new ConsultSummaryAdapter.PictureClickListener() {
            @Override
            public void onPictureClick(View view, int position, List<String> pictureUrlList, int index) {
                startPicturePreview(index, pictureUrlList);
            }
        });

        recyclerView.setAdapter(consultSummaryAdapter);
        
        TextView back = findViewById(R.id.left_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        
        //show progressImage before loading done
        progressImageView = findViewById(R.id.animal_progress);
        animationDrawable = (AnimationDrawable) progressImageView.getDrawable();
        progressImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        }, 50);
        
    }

    private void getTalentDetails(){
        RequestBody requestBody = new FormBody.Builder()
                .add("tid", String.valueOf(tid))
                .build();

        HttpUtil.sendOkHttpRequest(this, GET_TALENT_byID, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (isDebug) Slog.d(TAG, "==========response body : " + response.body());

                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject talentResponse = null;
                        try {
                            talentResponse = new JSONObject(responseText);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (talentResponse != null) {
                            JSONObject talentObject = talentResponse.optJSONObject("talent");
                            talent = getTalent(talentObject);
                            handler.sendEmptyMessage(LOAD_TALENT_DONE);
                        }

                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void setTalentDetailsView() {
        int authorUid = SharedPreferencesUtils.getSessionUid(getContext());
        Slog.d(TAG, "---------------------->authorUid: "+authorUid+" talent uid: "+talent.profile.getUid());
        if (talent.profile.getUid() == authorUid){
            TextView modify = findViewById(R.id.save);
            modify.setText(getContext().getResources().getString(R.string.edit));
            modify.setVisibility(View.VISIBLE);
            modify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("talent", talent);
                    TalentModifyDialogFragment talentModifyDialogFragment = new TalentModifyDialogFragment();
                    talentModifyDialogFragment.setArguments(bundle);
                    talentModifyDialogFragment.show(getSupportFragmentManager(), "TalentModifyDialogFragment");
                }
            });
        }

        View talentView = LayoutInflater.from(getContext()).inflate(R.layout.talent_details_header, (ViewGroup) findViewById(android.R.id.content), false);
        recyclerView.addHeaderView(talentView);

        RoundImageView leaderHead = talentView.findViewById(R.id.avatar);
        Glide.with(MyApplication.getContext()).load(HttpUtil.DOMAIN + talent.profile.getAvatar()).into(leaderHead);
        TextView name = talentView.findViewById(R.id.name);
        name.setText(talent.profile.getNickName());
        TextView university = talentView.findViewById(R.id.university);
        TextView major = talentView.findViewById(R.id.major);
        TextView degree = talentView.findViewById(R.id.degree);

        TextView titleTV = talentView.findViewById(R.id.talent_title);
        Slog.d(TAG, "---------------------->title: "+talent.title);
        titleTV.setText(talent.title);
        
        TextView subjectTV = talentView.findViewById(R.id.subject);
        subjectTV.setText(talent.subject);

        if (talent.profile.getSituation() == 0){
            university.setText(talent.profile.getUniversity());
            major.setText(" · "+talent.profile.getMajor());
            degree.setText(talent.profile.getDegreeName(talent.profile.getDegree()));
        }else {
            university.setText(talent.profile.getIndustry());
            //major.setText(talent.profile.getMajor());
            //major.setVisibility(View.GONE);
            degree.setText(talent.profile.getPosition());
        }


        TextView introduction = talentView.findViewById(R.id.introduction);
        introduction.setText(talent.introduction);
        
        TextView consultate = findViewById(R.id.consultation);
        consultate.setText("咨询");
        consultate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contactTalentDialog();
            }
        });
        
        Button evaluate = talentView.findViewById(R.id.evaluate_matchmaker);
        evaluate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TalentEvaluateDialogFragment talentEvaluateDialogFragment = new TalentEvaluateDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("uid", talent.profile.getUid());
                bundle.putInt("tid", talent.tid);
                bundle.putInt("type", talent.type);
                talentEvaluateDialogFragment.setArguments(bundle);
                talentEvaluateDialogFragment.show(getSupportFragmentManager(), "TalentEvaluateDialogFragment");
            }
        });

        getEvaluationStatistics();

        loadTalentConsult();

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.talent_details_layout), font);
    }

    private void getEvaluationStatistics() {
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(talent.profile.uid))
                .add("type", String.valueOf(talent.type))
                .add("tid", String.valueOf(talent.tid))
                .build();

        HttpUtil.sendOkHttpRequest(this, GET_EVALUATION_STATISTICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (isDebug) Slog.d(TAG, "==========response body : " + response.body());

                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        processEvaluationResponse(responseText);
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void processEvaluationResponse(String response) {
        try {
            JSONObject evaluateObject = new JSONObject(response);
            double scores = evaluateObject.optDouble("scores");
            int count = evaluateObject.optInt("count");
            float score = 0;
            if (count != 0) {
                float scoreFloat = (float) scores / count;
                score = (float) (Math.round(scoreFloat * 10)) / 10;
            }
            Slog.d(TAG, "------------------>processEvaluationResponse: scores" + scores + "  score: " + score + " count " + count);
            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putFloat("score", score);
            bundle.putInt("count", count);
            message.setData(bundle);
            message.what = GET_EVALUATION_DONE;
            handler.sendMessage(message);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setEvaluationView(final float score, int count) {
        TextView evaluateTV = findViewById(R.id.evaluate);
        String textContent = "";
        if (count == 0) {
            textContent = getResources().getString(R.string.no_evaluate);
        } else {
            textContent = score + "分" + getResources().getString(R.string.dot) + count + "条评论";
        }
        evaluateTV.setText(textContent);

        evaluateTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TalentDetailsActivity.this, TalentEvaluatorDetailsActivity.class);
                intent.putExtra("uid", talent.profile.getUid());
                intent.putExtra("type", talent.type);
                intent.putExtra("scores", score);
                intent.putExtra("tid", talent.tid);
                startActivity(intent);
            }
        });

    }

private void loadTalentConsult() {

        final int page = mConsultList.size() / PAGE_SIZE;
        RequestBody requestBody = new FormBody.Builder()
                .add("tid", String.valueOf(tid))
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), CONSULT_GET_BY_TID, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========loadData response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject consultsResponse = null;
                        try {
                            consultsResponse = new JSONObject(responseText);
                            if (consultsResponse != null) {
                                mLoadSize = processConsultsResponse(consultsResponse);
                                if (isDebug) Slog.d(TAG, "==========loadTalentConsult response size : " + mLoadSize);
                                if (mLoadSize == PAGE_SIZE) {
                                    handler.sendEmptyMessage(GET_ALL_CONSULT_DONE);
                                } else {
                                    if (mLoadSize != 0) {
                                        handler.sendEmptyMessage(GET_ALL_CONSULT_END);
                                    } else {
                                        handler.sendEmptyMessage(NO_CONSULT_MORE);
                                    }
                                }
                                } else {
                                handler.sendEmptyMessage(NO_CONSULT_MORE);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        handler.sendEmptyMessage(NO_CONSULT_MORE);
                    }
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    public int processConsultsResponse(JSONObject consultsObject) {
        int consultSize = 0;
        JSONArray consultArray = null;

        if (consultsObject != null) {
            consultArray = consultsObject.optJSONArray("consults");
            Slog.d(TAG, "------------------->processconsultsResponse: "+consultArray);
        }
        
        if (consultArray != null) {
            consultSize = consultArray.length();
            if (consultSize > 0) {
                for (int i = 0; i < consultArray.length(); i++) {
                    JSONObject consultObject = consultArray.optJSONObject(i);
                    if (consultObject != null) {
                        ConsultSummaryActivity.Consult consult = getConsult(consultObject);
                        mConsultList.add(consult);
                    }
                }
            }
        }

        return consultSize;
    }
    
    private void contactTalentDialog() {
        TalentConsultDF talentConsultDF = TalentConsultDF.newInstance(TALENT_CONSULT, talent.tid, talent.profile.getNickName());
        talentConsultDF.show(getSupportFragmentManager(), "TalentConsultDF");
    }

    private void contactTalent() {
        ChatInfo chatInfo = new ChatInfo();
        chatInfo.setType(TIMConversationType.C2C);
        chatInfo.setId(String.valueOf(talent.profile.uid));
        chatInfo.setChatName(talent.profile.getNickName());

        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("CHAT_INFO", chatInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onBackFromDialog(int type, int cid, boolean status) {

        switch (type) {
            case TALENT_MODIFY_RESULT_OK:
                if (status == true){
                    getTalentDetails();
                }
                break;
            case TALENT_CONSULT:
                if (status){
                    getNewAddConsult(cid);
                }
                break;
            default:
                break;
        }
    }
    
    public void startPicturePreview(int position, List<String> pictureUrlList){
        List<LocalMedia> localMediaList = new ArrayList<>();
        for (int i=0; i<pictureUrlList.size(); i++){
            LocalMedia localMedia = new LocalMedia();
            localMedia.setPath(HttpUtil.getDomain()+pictureUrlList.get(i));
            localMediaList.add(localMedia);
        }

        PictureSelector.create(this)
                .themeStyle(R.style.picture_default_style) // xml设置主题
                .loadImageEngine(GlideEngine.createGlideEngine())// 外部传入图片加载引擎，必传项
                .isNotPreviewDownload(true)
                .openExternalPreview(position, localMediaList);

    }
    
    private void getNewAddConsult(int cid){
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
                        JSONObject consultObject = jsonObject.optJSONObject("consult");
                        ConsultSummaryActivity.Consult consult = getConsult(consultObject);
                        mConsultList.add(0, consult);
                        handler.sendEmptyMessage(GET_NEW_ADD_CONSULT_DONE);
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
    
    public void handleMessage(Message message) {
        switch (message.what) {
            case GET_EVALUATION_DONE:
                Bundle bundle = message.getData();
                setEvaluationView(bundle.getFloat("score"), bundle.getInt("count"));
                break;
            case LOAD_TALENT_DONE:
                setTalentDetailsView();
                break;
            case GET_ALL_CONSULT_DONE:
                Slog.d(TAG, "-------------->GET_ALL_DONE");
                consultSummaryAdapter.setData(mConsultList);
                consultSummaryAdapter.notifyDataSetChanged();
                recyclerView.loadMoreComplete();
                stopLoadProgress();
                break;
            case GET_ALL_CONSULT_END:
                Slog.d(TAG, "-------------->GET_ALL_END");
                consultSummaryAdapter.setData(mConsultList);
                consultSummaryAdapter.notifyDataSetChanged();
                recyclerView.loadMoreComplete();
                recyclerView.setNoMore(true);
                stopLoadProgress();
                break;
           case NO_CONSULT_MORE:
                Slog.d(TAG, "-------------->NO_MORE");
                recyclerView.setNoMore(true);
                recyclerView.loadMoreComplete();
                stopLoadProgress();
                break;
            case GET_NEW_ADD_CONSULT_DONE:
                consultSummaryAdapter.setData(mConsultList);
                consultSummaryAdapter.notifyItemInserted(0);
                consultSummaryAdapter.notifyDataSetChanged();
                recyclerView.loadMoreComplete();
                break;
            default:
                break;
        }
    }

    private void registerBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FILL_MEET_INFO_BROADCAST);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, intentFilter);
    }

    //unregister local broadcast
    private void unRegisterBroadcast() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterBroadcast();
    }
    
    private void stopLoadProgress() {
        if (progressImageView.getVisibility() == View.VISIBLE) {
            animationDrawable.stop();
            progressImageView.setVisibility(View.GONE);
        }
    }

    static class MyHandler extends Handler {
        WeakReference<TalentDetailsActivity> talentDetailsActivityWeakReference;
        MyHandler(TalentDetailsActivity talentDetailsActivity) {
            talentDetailsActivityWeakReference = new WeakReference<TalentDetailsActivity>(talentDetailsActivity);
        }

        @Override
        public void handleMessage(Message message) {
            TalentDetailsActivity talentDetailsActivity = talentDetailsActivityWeakReference.get();
            if (talentDetailsActivity != null) {
                talentDetailsActivity.handleMessage(message);
            }
        }
    }

    private class SingleGroupReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case FILL_MEET_INFO_BROADCAST:
                    if (singleGroup.authorStatus == nonMEMBER) {
                        Toast.makeText(mContext, "申请已发出，待达人审核", Toast.LENGTH_LONG).show();
                    } else if (singleGroup.authorStatus == INVITTED) {
                        Toast.makeText(mContext, "已加入牵线团", Toast.LENGTH_LONG).show();
                    }
                    break;
            }

        }
    }
}


                
