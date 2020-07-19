package com.mufu.talent;

import android.graphics.Typeface;
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
import com.mufu.adapter.TalentEvaluatorDetailsAdapter;
import com.mufu.common.BaseAppCompatActivity;
import com.mufu.common.HandlerTemp;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.Slog;
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
import static com.mufu.util.DateUtil.getDateToString;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class TalentEvaluatorDetailsActivity extends BaseAppCompatActivity {
    private static final String TAG = "EvaluatorDetailsActivity";
    private static final int PAGE_SIZE = 12;
    private static final int NO_MORE = 0;
    private static final int LOAD_EVALUATOR_DONE = 1;
    private static final int LOAD_EVALUATOR_END = 2;
    private static final String GET_IMPRESSION_DETAIL_URL = HttpUtil.DOMAIN + "?q=talent/evaluation/details";
    List<TalentEvaluatorDetails> mEvaluatorDetailsList;
    XRecyclerView mEvaluatorDetailsListRV;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    private Handler handler;
    private int gid;
    private int tid;
    private TalentEvaluatorDetailsAdapter mEvaluatorDetailsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.talent_evaluator_details);
        final int uid = getIntent().getIntExtra("uid", -1);
        final int type = getIntent().getIntExtra("type", -1);
        float scores = getIntent().getFloatExtra("scores", 0);

        tid = getIntent().getIntExtra("tid", 0);

        handler = new TalentEvaluatorDetailsActivity.MyHandler(this);
        mEvaluatorDetailsList = new ArrayList<>();

        mEvaluatorDetailsAdapter = new TalentEvaluatorDetailsAdapter(this);
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
                getEvaluatorDetails(uid, type);
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

        getEvaluatorDetails(uid, type);

        TextView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.custom_actionbar), font);
    }

    private void setScoresView(float scores) {
        View scaleRatingBarView = LayoutInflater.from(getContext()).inflate(R.layout.scale_rating_bar, (ViewGroup) findViewById(android.R.id.content), false);
        mEvaluatorDetailsListRV.addHeaderView(scaleRatingBarView);
        TextView scoresTV = scaleRatingBarView.findViewById(R.id.rating);
        scoresTV.setText(String.valueOf(scores));
        ScaleRatingBar scaleRatingBar = scaleRatingBarView.findViewById(R.id.charm_rating);
        scaleRatingBar.setRating(scores);
    }

    private void getEvaluatorDetails(int uid, int type) {
        Slog.d(TAG, "------------------------>uid: " + uid);
        int page = mEvaluatorDetailsList.size() / PAGE_SIZE;
        FormBody.Builder builder = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .add("type", String.valueOf(type))
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .add("tid", String.valueOf(tid));

        RequestBody requestBody = builder.build();
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

                            JSONArray impressionArray = evaluatorDetailsObjWraper.optJSONArray("evaluations");
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
            TalentEvaluatorDetails evaluatorDetails = new TalentEvaluatorDetails();
            JSONObject evaluatorDetailsObj = impressionArray.optJSONObject(i);
            Slog.d(TAG, "------------------------>impression object: " + evaluatorDetailsObj);
            evaluatorDetails.nickname = evaluatorDetailsObj.optString("nickname");
            evaluatorDetails.university = evaluatorDetailsObj.optString("university");
            evaluatorDetails.content = evaluatorDetailsObj.optString("content");
            evaluatorDetails.time = getDateToString(evaluatorDetailsObj.optLong("created"), "yyyy-MM-dd");
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

    private void stopLoadProgress() {
        if (progressImageView.getVisibility() == View.VISIBLE) {
            animationDrawable.stop();
            progressImageView.setVisibility(View.GONE);
        }
    }

    static class MyHandler extends HandlerTemp<TalentEvaluatorDetailsActivity> {
        public MyHandler(TalentEvaluatorDetailsActivity cls) {
            super(cls);
        }

        @Override
        public void handleMessage(Message message) {
            TalentEvaluatorDetailsActivity evaluatorDetailsActivity = ref.get();
            if (evaluatorDetailsActivity != null) {
                evaluatorDetailsActivity.handleMessage(message);
            }
        }
    }

    public class TalentEvaluatorDetails {
        public String nickname;
        public String university;
        public String content = "";
        public String time;
    }
}
