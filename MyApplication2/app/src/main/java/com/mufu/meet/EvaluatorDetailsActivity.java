package com.mufu.meet;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mufu.R;
import com.mufu.adapter.EvaluatorDetailsAdapter;
import com.mufu.common.BaseAppCompatActivity;
import com.mufu.common.HandlerTemp;
import com.mufu.common.OnBackFromDialogInterFace;
import com.mufu.common.OnItemClickListener;
import com.mufu.util.HttpUtil;
import com.mufu.util.Slog;
import com.mufu.util.UserProfile;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.willy.ratingbar.ScaleRatingBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.mufu.common.MyApplication.getContext;
import static com.mufu.meet.EvaluateModifyDialogFragment.MODIFY_EVALUATE_DONE;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class EvaluatorDetailsActivity extends BaseAppCompatActivity implements OnBackFromDialogInterFace {
    private static final String TAG = "EvaluatorDetailsActivity";
    private static final int PAGE_SIZE = 12;
    private static final int NO_MORE = 0;
    private static final int LOAD_EVALUATOR_DONE = 1;
    private static final int LOAD_EVALUATOR_END = 2;
    private static final int MODIFY_EVALUATOR_DONE = 3;
    private static final String GET_IMPRESSION_DETAIL_URL = HttpUtil.DOMAIN + "?q=meet/impression/get_detail";
    List<EvaluatorDetails> mEvaluatorDetailsList;
    XRecyclerView mEvaluatorDetailsListRV;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    float scores = 0;
    private TextView scoresTV;
    private ScaleRatingBar scaleRatingBar;
    private int mPosition;
    private Handler handler;
    private EvaluatorDetailsAdapter mEvaluatorDetailsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.evaluator_details);
        final int uid = getIntent().getIntExtra("uid", -1);
        scores = getIntent().getFloatExtra("scores", 0);

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

        mEvaluatorDetailsAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                mPosition = position;
                EvaluateModifyDialogFragment evaluateModifyDialogFragment = new EvaluateModifyDialogFragment();
                Bundle bundle = new Bundle();
                /*
                bundle.putInt("uid", uid);
                float rating = (float) mEvaluatorDetailsList.get(position).getRating();
                if (rating > 0){
                    bundle.putFloat("rating", rating);
                }
                String features = mEvaluatorDetailsList.get(position).getFeatures();
                if (!TextUtils.isEmpty(features)){
                    bundle.putString("features", features);
                }
                */
                EvaluatorDetails evaluatorDetails = mEvaluatorDetailsList.get(position);
                bundle.putInt("uid", uid);
                bundle.putSerializable("details", evaluatorDetails);
                evaluateModifyDialogFragment.setArguments(bundle);
                evaluateModifyDialogFragment.show(getSupportFragmentManager(), "EvaluateModifyDialogFragment");
            }
        });

        mEvaluatorDetailsListRV.setAdapter(mEvaluatorDetailsAdapter);

        //show progressImage before loading done
        progressImageView = findViewById(R.id.animal_progress);
        animationDrawable = (AnimationDrawable) progressImageView.getDrawable();
        progressImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        }, 50);

        setScoresView(scores);

        getEvaluatorDetails(uid);
    }

    private void setScoresView(float scores) {
        View scaleRatingBarView = LayoutInflater.from(getContext()).inflate(R.layout.scale_rating_bar, (ViewGroup) findViewById(android.R.id.content), false);
        mEvaluatorDetailsListRV.addHeaderView(scaleRatingBarView);
        scoresTV = scaleRatingBarView.findViewById(R.id.rating);
        scoresTV.setText(String.valueOf(scores));
        scaleRatingBar = scaleRatingBarView.findViewById(R.id.charm_rating);
        scaleRatingBar.setRating(scores);
    }

    private void getEvaluatorDetails(int uid) {
        Slog.d(TAG, "------------------------>uid: " + uid);
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
                            if (impressionArray != null && impressionArray.length() > 0) {
                                int size = setEvaluatorDetails(impressionArray);
                                if (size == PAGE_SIZE) {
                                    handler.sendEmptyMessage(LOAD_EVALUATOR_DONE);
                                } else {
                                    if (size > 0) {
                                        handler.sendEmptyMessage(LOAD_EVALUATOR_END);
                                    } else {
                                        handler.sendEmptyMessage(NO_MORE);
                                    }
                                }
                            } else {
                                handler.sendEmptyMessage(NO_MORE);
                            }

                            Slog.d(TAG, "-----------------------impression size: " + mEvaluatorDetailsList.size());
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
            Slog.d(TAG, "------------------------>impression object: " + evaluatorDetailsObj);
            evaluatorDetails.setRid(evaluatorDetailsObj.optInt("rid"));
            evaluatorDetails.setIid(evaluatorDetailsObj.optInt("iid"));
            evaluatorDetails.setEvaluatorUid(evaluatorDetailsObj.optInt("evaluator_uid"));
            evaluatorDetails.setRating(evaluatorDetailsObj.optDouble("rating"));
            String features = "";
            try {
                features = evaluatorDetailsObj.getString("features");
                //Slog.d(TAG, "---------------------->features: "+features);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (features != null && !TextUtils.isEmpty(features)) {
                evaluatorDetails.setFeatures(evaluatorDetailsObj.optString("features"));
                evaluatorDetails.setNickName(evaluatorDetailsObj.optString("nickname"));
                evaluatorDetails.setAvatar(evaluatorDetailsObj.optString("avatar"));
                evaluatorDetails.setSex(evaluatorDetailsObj.optInt("sex"));
                mEvaluatorDetailsList.add(evaluatorDetails);
            }
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
            case MODIFY_EVALUATOR_DONE:
                Bundle bundle = message.getData();
                float average = bundle.getFloat("rating", 0);
                if (average > 0){
                    scoresTV.setText(String.valueOf(average));
                    scaleRatingBar.setRating(average);
                }
                mEvaluatorDetailsAdapter.setData(mEvaluatorDetailsList);
                mEvaluatorDetailsAdapter.notifyDataSetChanged();
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
    public void onBackFromDialog(int type, Bundle bundle){
        switch (type){
            case MODIFY_EVALUATE_DONE:
                boolean status = bundle.getBoolean("status");
                if (status){
                    float rating = bundle.getFloat("rating", 0);
                    Message message = new Message();
                    Bundle evaluate = new Bundle();
                    evaluate.putFloat("rating", rating);
                    message.setData(evaluate);
                    message.what = MODIFY_EVALUATOR_DONE;

                    String features = bundle.getString("features", "");
                    if (!TextUtils.isEmpty(features)){
                        mEvaluatorDetailsList.get(mPosition).setFeatures(features);
                    }

                    handler.sendMessage(message);
                }
                break;
        }
    }

    static class MyHandler extends HandlerTemp<EvaluatorDetailsActivity> {
        public MyHandler(EvaluatorDetailsActivity cls) {
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

    public class EvaluatorDetails extends UserProfile {

        private int rid = -1;
        private int iid = -1;
        private int evaluatorUid;
        private double rating;
        private String features = "";

        public int getRid(){
            return rid;
        }

        public void setRid(int rid){
            this.rid = rid;
        }

        public int getIid(){
            return iid;
        }

        public void setIid(int iid){
            this.iid = iid;
        }

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
}
