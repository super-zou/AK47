package com.mufu.experience;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mufu.R;
import com.mufu.adapter.ExperienceSummaryAdapter;
import com.mufu.common.BaseAppCompatActivity;
import com.mufu.common.MyApplication;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.MyLinearLayoutManager;
import com.mufu.util.SharedPreferencesUtils;
import com.mufu.util.Slog;

import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

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
import static com.mufu.common.MyApplication.getContext;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class ExperienceSummaryActivity extends BaseAppCompatActivity {
    private static final boolean isDebug = true;
    private static final String TAG = "ExperienceSummaryActivity";
    private static final int PAGE_SIZE = 8;
    public static final String GET_ALL_EXPERIENCES = HttpUtil.DOMAIN + "?q=experience/get_all_experiences";
    
    private static final int GET_ALL_DONE = 1;
    private static final int GET_ALL_END = 2;
    private static final int NO_MORE = 3;
    final int itemLimit = 1;
    private int uid = 0;
    private boolean isSelf = false;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    private int mLoadSize = 0;
    private Handler handler;
    
    private ExperienceSummaryAdapter experienceSummaryAdapter;
    private XRecyclerView recyclerView;
    private List<Experience> mExperienceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide_summary);

        if (getIntent() != null){
            uid = getIntent().getIntExtra("uid", 0);
            int autherUid = SharedPreferencesUtils.getSessionUid(MyApplication.getContext());
            if (uid > 0 && uid == autherUid){
                isSelf = true;
            }
        }

        initView();

        loadData();
    }
    
     private void initView() {
        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.custom_actionbar), font);

        TextView pageTitle = findViewById(R.id.page_title);
        pageTitle.setText(getResources().getString(R.string.experiences));

        handler = new ExperienceSummaryActivity.MyHandler(this);
        recyclerView = findViewById(R.id.guide_summary_list);
        experienceSummaryAdapter = new ExperienceSummaryAdapter(this, isSelf);
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
                    experienceSummaryAdapter.setScrolling(false);
                    experienceSummaryAdapter.notifyDataSetChanged();
                } else {
                    experienceSummaryAdapter.setScrolling(true);
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
                loadData();
            }
        });
         
         experienceSummaryAdapter.setItemClickListener(new ExperienceSummaryAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Slog.d(TAG, "==========click : " + position);
                int eid = mExperienceList.get(position).eid;
                Intent intent = new Intent(getContext(), ExperienceDetailActivity.class);
                intent.putExtra("eid", eid);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivityForResult(intent, RESULT_FIRST_USER);
            }
        });

        recyclerView.setAdapter(experienceSummaryAdapter);
         
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
    
    private void loadData() {

        final int page = mExperienceList.size() / PAGE_SIZE;
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page));
        
         if (uid > 0){
            builder.add("uid", String.valueOf(uid));
        }
        RequestBody requestBody = builder.build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_ALL_EXPERIENCES, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========loadData response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject experiencesResponse = null;
                        try {
                            experiencesResponse = new JSONObject(responseText);
                            if (experiencesResponse != null) {
                                mLoadSize = processExperiencesResponse(experiencesResponse);

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
    
    public int processExperiencesResponse(JSONObject experiencesObject) {
        int experienceSize = 0;
        JSONArray experienceArray = null;

        if (experiencesObject != null) {
            experienceArray = experiencesObject.optJSONArray("experiences");
            Slog.d(TAG, "------------------->processExperiencesResponse: "+experienceArray);
        }
        
        if (experienceArray != null) {
            experienceSize = experienceArray.length();
            if (experienceSize > 0) {
                for (int i = 0; i < experienceArray.length(); i++) {
                    JSONObject guideObject = experienceArray.optJSONObject(i);
                    if (guideObject != null) {
                        ExperienceSummaryActivity.Experience experience = getExperience(guideObject);
                        mExperienceList.add(experience);
                    }
                }
            }
        }

        return experienceSize;
    }
    
    public static ExperienceSummaryActivity.Experience getExperience(JSONObject experienceObject) {
        ExperienceSummaryActivity.Experience experience = new ExperienceSummaryActivity.Experience();
        if (experienceObject != null) {
            experience.eid = experienceObject.optInt("eid");
            experience.city = experienceObject.optString("city");
            experience.headPictureUrl = experienceObject.optString("picture_url");
            experience.evaluateScore = experienceObject.optInt("score");
            experience.evaluateCount = experienceObject.optInt("count");
            experience.price = experienceObject.optInt("price");
            experience.title = experienceObject.optString("title");
            experience.unit = experienceObject.optString("unit");
            experience.duration = experienceObject.optInt("duration");
        }
        
        return experience;
    }

    public void handleMessage(Message message) {
        switch (message.what) {
            case GET_ALL_DONE:
                Slog.d(TAG, "-------------->GET_ALL_DONE");
                experienceSummaryAdapter.setData(mExperienceList);
                experienceSummaryAdapter.notifyDataSetChanged();
                recyclerView.loadMoreComplete();
                stopLoadProgress();
                break;
            case GET_ALL_END:
                Slog.d(TAG, "-------------->GET_ALL_END");
                experienceSummaryAdapter.setData(mExperienceList);
                experienceSummaryAdapter.notifyDataSetChanged();
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
    
    public static class Experience extends BaseExperience {
        public int eid;
        public int duration;
        public String unit;
    }

    public static class BaseExperience implements Serializable {
        public String headPictureUrl;
        public String city;
        public String title;
        public int evaluateScore;
        public int evaluateCount;
        public int price;
    }
    
    static class MyHandler extends Handler {
        WeakReference<ExperienceSummaryActivity> experienceSummaryActivityWeakReference;

        MyHandler(ExperienceSummaryActivity experienceSummaryActivity) {
            experienceSummaryActivityWeakReference = new WeakReference<>(experienceSummaryActivity);
        }

        @Override
        public void handleMessage(Message message) {
            ExperienceSummaryActivity experienceSummaryActivity = experienceSummaryActivityWeakReference.get();
            if (experienceSummaryActivity != null) {
                experienceSummaryActivity.handleMessage(message);
            }
        }
    }
}
      