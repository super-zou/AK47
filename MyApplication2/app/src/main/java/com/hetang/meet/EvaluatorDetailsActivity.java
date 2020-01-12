package com.hetang.meet;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.hetang.adapter.EvaluatorDetailsAdapter;
import com.hetang.util.HttpUtil;
import com.hetang.util.Slog;
import com.hetang.util.UserProfile;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.hetang.R;

import com.hetang.common.BaseAppCompatActivity;
import com.hetang.common.HandlerTemp;

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

import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class EvaluatorDetailsActivity extends BaseAppCompatActivity {
    private static final String TAG = "EvaluatorDetailsActivity";
    private static final int PAGE_SIZE = 12;
    private static final int NO_MORE = 0;
    private static final int LOAD_EVALUATOR_DONE = 1;
    private static final int LOAD_EVALUATOR_END = 2;
    private static final String GET_IMPRESSION_DETAIL_URL = HttpUtil.DOMAIN + "?q=meet/impression/get_detail";
    List<EvaluatorDetails> mEvaluatorDetailsList;
    XRecyclerView mEvaluatorDetailsListRV;
    //private XRecyclerView mXRecyclerView;
    //private JSONObject impressionObj;
    private Handler handler;
    private EvaluatorDetailsAdapter mEvaluatorDetailsAdapter;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.evaluator_details);
        final int uid = getIntent().getIntExtra("uid", -1);

        handler = new EvaluatorDetailsActivity.MyHandler(this);
        mEvaluatorDetailsList = new ArrayList<>();

        mEvaluatorDetailsAdapter = new EvaluatorDetailsAdapter(this);
        mEvaluatorDetailsListRV = findViewById(R.id.evaluator_details_list);
        
        mEvaluatorDetailsListRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                mEvaluatorDetailsAdapter.notifyDataSetChanged();
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mEvaluatorDetailsListRV.setLayoutManager(linearLayoutManager);
        mEvaluatorDetailsListRV.setRefreshProgressStyle(BallSpinFadeLoader);
        mEvaluatorDetailsListRV.setLoadingMoreProgressStyle(ProgressStyle.BallRotate);
        mEvaluatorDetailsListRV.getDefaultRefreshHeaderView().setRefreshTimeVisible(true);
        mEvaluatorDetailsListRV.setPullRefreshEnabled(false);
        mEvaluatorDetailsListRV.getDefaultFootView().setLoadingHint(getString(R.string.loading_pull_up_tip));
        //mEvaluatorDetailsListRV.getDefaultFootView().setNoMoreHint(getString(R.string.loading_no_more));

        // When the item number of the screen number is list.size-2,we call the onLoadMore
        mEvaluatorDetailsListRV.setLimitNumberToCallLoadMore(2);
        mEvaluatorDetailsListRV.setRefreshProgressStyle(ProgressStyle.BallBeat);
        mEvaluatorDetailsListRV.setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);
            
            mEvaluatorDetailsListRV.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                //updateData();
            }

            @Override
            public void onLoadMore() {
                //loadDynamicsData(mMeetMember.getUid());
                getEvaluatorDetails(uid);
            }
        });

        mEvaluatorDetailsListRV.setAdapter(mEvaluatorDetailsAdapter);
        
        //show progressImage before loading done
        progressImageView = findViewById(R.id.animal_progress);
        animationDrawable = (AnimationDrawable)progressImageView.getDrawable();
        progressImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        },50);

        getEvaluatorDetails(uid);
    }
    
    private void getEvaluatorDetails(int uid) {
        Slog.d(TAG, "------------------------>uid: "+uid);
        int page = mEvaluatorDetailsList.size() / PAGE_SIZE;
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();
        
        HttpUtil.sendOkHttpRequest(this, GET_IMPRESSION_DETAIL_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (responseText != null) {
                        if (!TextUtils.isEmpty(responseText)) {
                            JSONObject evaluatorDetailsObjWraper = null;
                            try {
                                evaluatorDetailsObjWraper = new JSONObject(responseText);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            
                            JSONArray impressionArray = evaluatorDetailsObjWraper.optJSONArray("impression");
                            //Slog.d(TAG, "impressionArray: "+impressionArray);
                            if(impressionArray != null && impressionArray.length() > 0){
                                int size = setEvaluatorDetails(impressionArray);
                                if (size == PAGE_SIZE){
                                    handler.sendEmptyMessage(LOAD_EVALUATOR_DONE);
                                }else {
                                    if (size > 0){
                                        handler.sendEmptyMessage(LOAD_EVALUATOR_END);
                                    }else {
                                        handler.sendEmptyMessage(NO_MORE);
                                    }
                                }
                            }else {
                                handler.sendEmptyMessage(NO_MORE);
                            }
                            
                            Slog.d(TAG, "-----------------------impression size: "+mEvaluatorDetailsList.size());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }
    
    private int setEvaluatorDetails(JSONArray impressionArray) {
        for (int i = 0; i < impressionArray.length(); i++) {
            EvaluatorDetails evaluatorDetails = new EvaluatorDetails();
            JSONObject evaluatorDetailsObj = impressionArray.optJSONObject(i);
            Slog.d(TAG, "------------------------>impression object: "+evaluatorDetailsObj);
            evaluatorDetails.setEvaluatorUid(evaluatorDetailsObj.optInt("evaluator_uid"));
            evaluatorDetails.setRating(evaluatorDetailsObj.optDouble("rating"));
            String features = "";
            try {
                features = evaluatorDetailsObj.getString("features");
                //Slog.d(TAG, "---------------------->features: "+features);
            }catch (JSONException e){
                e.printStackTrace();
            }
            
            if(features != null && !TextUtils.isEmpty(features)){
                evaluatorDetails.setFeatures(evaluatorDetailsObj.optString("features"));
            }
            evaluatorDetails.setNickName(evaluatorDetailsObj.optString("nickname"));
            evaluatorDetails.setAvatar(evaluatorDetailsObj.optString("avatar"));
            evaluatorDetails.setSex(evaluatorDetailsObj.optInt("sex"));
            mEvaluatorDetailsList.add(evaluatorDetails);
        }

        return impressionArray.length();
    }
    
    public void handleMessage(Message message) {
        switch (message.what) {
            case LOAD_EVALUATOR_DONE:
                mEvaluatorDetailsAdapter.setData(mEvaluatorDetailsList);
                mEvaluatorDetailsAdapter.notifyDataSetChanged();
                mEvaluatorDetailsListRV.refreshComplete();
                stopLoadProgress();
                break;
            case NO_MORE:
                mEvaluatorDetailsListRV.setNoMore(true);
                mEvaluatorDetailsListRV.loadMoreComplete();
                stopLoadProgress();
                break;
            case LOAD_EVALUATOR_END:
                mEvaluatorDetailsAdapter.setData(mEvaluatorDetailsList);
                mEvaluatorDetailsAdapter.notifyDataSetChanged();
                mEvaluatorDetailsListRV.refreshComplete();
                mEvaluatorDetailsListRV.setNoMore(true);
                mEvaluatorDetailsListRV.loadMoreComplete();
                stopLoadProgress();
                break;
            default:
                break;
        }
    }

    private void stopLoadProgress(){
        if (progressImageView.getVisibility() == View.VISIBLE){
            animationDrawable.stop();
            progressImageView.setVisibility(View.GONE);
        }
    }
    
    public class EvaluatorDetails extends UserProfile {

        private int evaluatorUid;
        private double rating;
        private String features = "";

        public int getEvaluatorUid() {
            return evaluatorUid;
        }
        
        public void setEvaluatorUid(int evaluator_uid) {
            this.evaluatorUid = evaluator_uid;
        }

        public double getRating() {
            return rating;
        }

        public void setRating(double rating) {
            this.rating = rating;
        }

        public String getFeatures() {
            return features;
        }

        public void setFeatures(String features) {
            this.features = features;
        }

    }
    
    static class MyHandler extends HandlerTemp<EvaluatorDetailsActivity> {
        public MyHandler(EvaluatorDetailsActivity cls){
            super(cls);
        }

        @Override
        public void handleMessage(Message message) {
            EvaluatorDetailsActivity evaluatorDetailsActivity = ref.get();
            if (evaluatorDetailsActivity != null) {
                evaluatorDetailsActivity.handleMessage(message);
            }
        }
    }
}
