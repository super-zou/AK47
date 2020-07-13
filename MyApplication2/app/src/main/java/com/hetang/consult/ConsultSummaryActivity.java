package com.hetang.consult;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hetang.R;
import com.hetang.adapter.ConsultSummaryAdapter;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.common.MyApplication;
import com.hetang.consult.ConsultDetailActivity;
import com.hetang.picture.GlideEngine;
import com.hetang.util.CommonDialogFragmentInterface;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.MyLinearLayoutManager;
import com.hetang.util.RoundImageView;
import com.hetang.util.Slog;
import com.hetang.util.Utility;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.entity.LocalMedia;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.hetang.common.MyApplication.getContext;
import static com.hetang.consult.ConsultDetailActivity.GET_QUESTION_BY_CID;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class ConsultSummaryActivity extends BaseAppCompatActivity implements CommonDialogFragmentInterface {
    private static final boolean isDebug = true;
    private static final String TAG = "ConsultSummaryActivity";
    private static final int PAGE_SIZE = 8;
    public static final String CONSULT_GET_ALL = HttpUtil.DOMAIN + "?q=consult/get_all_consults";
    public static final String CONSULT_GET_BY_TID = HttpUtil.DOMAIN + "?q=consult/get_consults_by_tid";
    public static final String GET_TALENT_BASE_INFO = HttpUtil.DOMAIN + "?q=talent/get_base_info";
    public static final String GET_ANSWERS_BY_UID = HttpUtil.DOMAIN + "?q=consult/get_answers_by_uid";
    public static final String GET_QUESTIONS_BY_UID = HttpUtil.DOMAIN + "?q=consult/get_questions_by_uid";
    private static final int GET_ALL_DONE = 1;
    private static final int GET_ALL_END = 2;
    private static final int NO_MORE = 3;
    private static final int GET_TALENT_INFO_DONE = 4;
    private static final int GET_NEW_ADD_CONSULT_DONE = 5;

    private JSONObject talentObject;
    final int itemLimit = 1;
    private int tid = 0;
    private int type = -1;
    private int uid = 0;
    private boolean isTalent = false;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    private int mLoadSize = 0;
    private Handler handler;
    private ConsultSummaryAdapter consultSummaryAdapter;
    private XRecyclerView recyclerView;
    private List<Consult> mConsultList = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consult_summary);

        initView();
        Slog.d(TAG, "---------------------->getIntent: "+getIntent());
        if (getIntent() != null){
            tid = getIntent().getIntExtra("tid", 0);
            if (tid != 0){
                isTalent = true;
                getTalentBaseInfo(tid);
                loadTalentData(tid);
            }else {
                isTalent = false;
                type = getIntent().getIntExtra("type", -1);
                if (type == -1){
                    loadData();
                }else {
                    uid = getIntent().getIntExtra("uid", -1);
                    loadDataWithUid(uid, type);
                }
            }
        }
    }
    
    private void initView() {
        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.custom_actionbar), font);

        handler = new ConsultSummaryActivity.MyHandler(this);
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
        recyclerView.getDefaultFootView().setNoMoreHint(getString(R.string.no_more));

        // When the item number of the screen number is list.size-2,we call the onLoadMore
        recyclerView.setLimitNumberToCallLoadMore(itemLimit);
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
                if (isTalent){
                    loadTalentData(tid);
                }else {
                    if (type == -1){
                        loadData();
                    }else {
                        loadDataWithUid(uid, type);
                    }
                }
            }
        });
        
        consultSummaryAdapter.setItemClickListener(new ConsultSummaryAdapter.PictureClickListener() {
            @Override
            public void onPictureClick(View view, int position, List<String> pictureUrlList, int index) {
                startPicturePreview(index, pictureUrlList);
            }
        });

        recyclerView.setAdapter(consultSummaryAdapter);

        TextView back = findViewById(R.id.back);
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
    
     private void loadData() {

        final int page = mConsultList.size() / PAGE_SIZE;
        RequestBody requestBody = new FormBody.Builder()
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), CONSULT_GET_ALL, requestBody, new Callback() {
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

                                if (mLoadSize == PAGE_SIZE) {
                                    handler.sendEmptyMessage(GET_ALL_DONE);
                                } else {
                                    if (mLoadSize != 0) {
                                        handler.sendEmptyMessage(GET_ALL_END);
                                    } else {
                                        handler.sendEmptyMessage(NO_MORE);
 }
                                }
                            } else {
                                handler.sendEmptyMessage(NO_MORE);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        } else {
                        handler.sendEmptyMessage(NO_MORE);
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) { e.printStackTrace(); }
        });
    }
    
     private void getTalentBaseInfo(int tid){
        RequestBody requestBody = new FormBody.Builder()
                .add("tid", String.valueOf(tid))
                .build();
                HttpUtil.sendOkHttpRequest(getContext(), GET_TALENT_BASE_INFO, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========addTalentHeader response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject talentResponse = null;
                        try {
                            talentResponse = new JSONObject(responseText);
                            if (talentResponse != null) {
                                talentObject = talentResponse.optJSONObject("talent");
                                handler.sendEmptyMessage(GET_TALENT_INFO_DONE);
                            }
                            
                            } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) { e.printStackTrace(); }
        });
    }
    
    private void addTalentHeaderView(){
        View talentView = LayoutInflater.from(getContext()).inflate(R.layout.talent_consult_header, (ViewGroup) findViewById(android.R.id.content), false);
        recyclerView.addHeaderView(talentView);
        RoundImageView avatarIV = talentView.findViewById(R.id.avatar);
        Glide.with(getContext()).load(HttpUtil.DOMAIN + talentObject.optString("avatar")).into(avatarIV);
        TextView nameTV = talentView.findViewById(R.id.name);
        nameTV.setText(talentObject.optString("nickname"));
        TextView subjectTV = talentView.findViewById(R.id.subject);
        subjectTV.setText(talentObject.optString("subject"));
        TextView introductionTV = talentView.findViewById(R.id.introduction);
        introductionTV.setText(talentObject.optString("introduction"));
        TextView answerCountTV = talentView.findViewById(R.id.answer_count);
        answerCountTV.setText("解答"+talentObject.optString("answer_count"));
        
        Button consultBtn = talentView.findViewById(R.id.consult_talent);
        consultBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TalentConsultDF talentConsultDF = TalentConsultDF.newInstance(talentObject.optInt("tid"), talentObject.optString("nickname"));
                talentConsultDF.show(getSupportFragmentManager(), "TalentConsultDF");
            }
        });
    }
    
     private void loadTalentData(int tid) {

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

                                if (mLoadSize == PAGE_SIZE) {
                                    handler.sendEmptyMessage(GET_ALL_DONE);
                                } else {
                                    if (mLoadSize != 0) {
                                        handler.sendEmptyMessage(GET_ALL_END);
                                    } else {
                                        handler.sendEmptyMessage(NO_MORE);
                                    }
                                }
                                } else {
                                handler.sendEmptyMessage(NO_MORE);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        handler.sendEmptyMessage(NO_MORE);
                    }
                }
            }
            
             @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
    private void loadDataWithUid(int uid, int type) {

        final int page = mConsultList.size() / PAGE_SIZE;
        RequestBody requestBody = new FormBody.Builder()
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .add("uid", String.valueOf(uid))
                .build();

        String uri = GET_ANSWERS_BY_UID;
        if (type == Utility.ConsultType.QUESTIONED.ordinal()){
            uri = GET_QUESTIONS_BY_UID;
        }
        
        HttpUtil.sendOkHttpRequest(getContext(), uri, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========loadDataWithUid response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject consultsResponse = null;
                        try {
                            consultsResponse = new JSONObject(responseText);
                            if (consultsResponse != null) {
                                mLoadSize = 0;
                                mLoadSize = processConsultsResponse(consultsResponse);
                                
                                if (mLoadSize == PAGE_SIZE) {
                                    handler.sendEmptyMessage(GET_ALL_DONE);
                                } else {
                                    if (mLoadSize != 0) {
                                        handler.sendEmptyMessage(GET_ALL_END);
                                    } else {
                                        handler.sendEmptyMessage(NO_MORE);
                                    }
                                }
                            } else {
                                handler.sendEmptyMessage(NO_MORE);
                            }
                            
                            } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        handler.sendEmptyMessage(NO_MORE);
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) { e.printStackTrace(); }
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
                        Consult consult = getConsult(consultObject);
                        mConsultList.add(consult);
                    }
                }
            }
        }

        return consultSize;
    }
    
     public static class Consult implements Parcelable {
        public int cid;
        public int uid;
        public int tid;
        public String avatar;
        public String name;
        public String question;
        public int reward;
        public int answerCount;
        public int created;
        public List<String> pictureList = new ArrayList<>();
        
        public static final Creator<Consult> CREATOR = new Creator<Consult>() {
            @Override
            public Consult createFromParcel(Parcel in) {
                return new Consult(in);
            }

            @Override
            public Consult[] newArray(int size) {
                return new Consult[size];
            }
        };

        public Consult() { }
        
        protected Consult(Parcel in) {
            cid = in.readInt();
            uid = in.readInt();
            tid = in.readInt();
            avatar = in.readString();
            name = in.readString();
            question = in.readString();
            reward = in.readInt();
            answerCount = in.readInt();
            created = in.readInt();
            pictureList = new ArrayList<>();
            in.readList(pictureList, getClass().getClassLoader());
        }

        @Override
        public int describeContents() {
            return 0;
        }
        
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(cid);
            dest.writeInt(uid);
            dest.writeInt(tid);
            dest.writeString(avatar);
            dest.writeString(name);
            dest.writeString(question);
            dest.writeInt(reward);
            dest.writeInt(answerCount);
            dest.writeInt(created);
            dest.writeList(pictureList);
        }

        @Override
        public String toString() {
            return "Route{" +'}';
        }
    }
    
    public static Consult getConsult(JSONObject consultObject) {
        Consult consult = new Consult();
        if (consultObject != null) {
            consult.cid = consultObject.optInt("cid");
            consult.uid = consultObject.optInt("uid");
            consult.tid = consultObject.optInt("tid");
            consult.avatar = consultObject.optString("avatar");
            consult.name = consultObject.optString("nickname");
            consult.question = consultObject.optString("question");
            consult.reward = consultObject.optInt("amount");
            consult.answerCount = consultObject.optInt("answer_count");
            consult.created = consultObject.optInt("created");
            JSONArray jsonArray = consultObject.optJSONArray("pictures");
            
            if (jsonArray.length() > 0){
                for (int i=0; i<jsonArray.length(); i++){
                    consult.pictureList.add(jsonArray.optString(i));
                }
            }
            //consult.pictureArray = consultObject.optJSONArray("pictures");
        }

        return consult;
    }


@Override
    public void onBackFromDialog(int cid, int tid, boolean status) {
        Slog.d(TAG, "-------------------onBackFromDialog cid: "+cid+"  tid: "+tid+"  status: "+status);
        if (status){
            getNewAddConsult(cid);
        }
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
                        Consult consult = getConsult(consultObject);
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
            case GET_ALL_DONE:
                Slog.d(TAG, "-------------->GET_ALL_DONE");
                consultSummaryAdapter.setData(mConsultList);
                consultSummaryAdapter.notifyDataSetChanged();
                recyclerView.loadMoreComplete();
                stopLoadProgress();
                break;
                case GET_ALL_END:
                Slog.d(TAG, "-------------->GET_ALL_END");
                consultSummaryAdapter.setData(mConsultList);
                consultSummaryAdapter.notifyDataSetChanged();
                recyclerView.loadMoreComplete();
                recyclerView.setNoMore(true);
                stopLoadProgress();
                break;
                case NO_MORE:
                Slog.d(TAG, "-------------->NO_MORE");
                recyclerView.setNoMore(true);
                recyclerView.loadMoreComplete();
                stopLoadProgress();
                break;
            case GET_TALENT_INFO_DONE:
                addTalentHeaderView();
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

    private void stopLoadProgress() {
        if (progressImageView.getVisibility() == View.VISIBLE) {
            animationDrawable.stop();
            progressImageView.setVisibility(View.GONE);
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (recyclerView != null) {
            recyclerView.destroy();
            recyclerView = null;
        }
    }

    static class MyHandler extends Handler {
        WeakReference<ConsultSummaryActivity> consultSummaryActivityWeakReference;
        MyHandler(ConsultSummaryActivity consultSummaryActivity) {
            consultSummaryActivityWeakReference = new WeakReference<>(consultSummaryActivity);
        }

        @Override
        public void handleMessage(Message message) {
            ConsultSummaryActivity consultSummaryActivity = consultSummaryActivityWeakReference.get();
            if (consultSummaryActivity != null) {
                consultSummaryActivity.handleMessage(message);
            }
        }
    }
}
      
