package com.hetang.experience;

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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hetang.R;
import com.hetang.adapter.GuideSummaryAdapter;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.common.MyApplication;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.MyLinearLayoutManager;
import com.hetang.util.Slog;
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
import static com.hetang.common.MyApplication.getContext;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class GuideSummaryActivity extends BaseAppCompatActivity {
    private static final boolean isDebug = true;
    private static final String TAG = "GuideSummaryActivity";
    private static final int PAGE_SIZE = 8;
    private static final String GUIDE_GET_ALL = HttpUtil.DOMAIN + "?q=travel_guide/get_all_guides";
    private static final int GET_ALL_DONE = 1;
    private static final int GET_ALL_END = 2;
    private static final int NO_MORE = 3;
    final int itemLimit = 1;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    private int mLoadSize = 0;
    private Handler handler;
    private GuideSummaryAdapter guideSummaryAdapter;
    private XRecyclerView recyclerView;
    private List<Guide> mGuideList = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide_summary);

        initView();

        loadData();
    }
    
    private void initView() {
        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.custom_actionbar), font);

        handler = new GuideSummaryActivity.MyHandler(this);
        recyclerView = findViewById(R.id.guide_summary_list);
        guideSummaryAdapter = new GuideSummaryAdapter(getContext());
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
                    guideSummaryAdapter.setScrolling(false);
                    guideSummaryAdapter.notifyDataSetChanged();
        } else {
                    guideSummaryAdapter.setScrolling(true);
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
        
        guideSummaryAdapter.setItemClickListener(new GuideSummaryAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Slog.d(TAG, "==========click : " + position);
                int tid = mGuideList.get(position).tid;
                Intent intent = new Intent(getContext(), GuideDetailActivity.class);
                intent.putExtra("tid", tid);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivityForResult(intent, RESULT_FIRST_USER);
            }
        });

        recyclerView.setAdapter(guideSummaryAdapter);
        
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
        
        FloatingActionButton floatingActionButton = findViewById(R.id.create_activity);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void loadData() {

        final int page = mGuideList.size() / PAGE_SIZE;
        RequestBody requestBody = new FormBody.Builder()
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();
                
        HttpUtil.sendOkHttpRequest(getContext(), GUIDE_GET_ALL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========loadData response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                    JSONObject guidesResponse = null;
                        try {
                            guidesResponse = new JSONObject(responseText);
                            if (guidesResponse != null) {
                                mLoadSize = processGuidesResponse(guidesResponse);

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
    
    public int processGuidesResponse(JSONObject guidesObject) {
        int guideSize = 0;
        JSONArray guideArray = null;

        if (guidesObject != null) {
            guideArray = guidesObject.optJSONArray("guides");
            Slog.d(TAG, "------------------->processGuidesResponse: "+guideArray);
        }

        if (guideArray != null) {
            guideSize = guideArray.length();
            if (guideSize > 0) {
                for (int i = 0; i < guideArray.length(); i++) {
                    JSONObject guideObject = guideArray.optJSONObject(i);
                    if (guideObject != null) {
                        Guide guide = getGuide(guideObject);
                        mGuideList.add(guide);
                    }
                }
            }
        }

        return guideSize;
    }
    
    public static Guide getGuide(JSONObject guideObject) {
        Guide guide = new Guide();
        if (guideObject != null) {
            guide.tid = guideObject.optInt("tid");
            guide.city = guideObject.optString("city");
            guide.headPictureUrl = guideObject.optString("picture_url");
            guide.evaluateScore = guideObject.optInt("score");
            guide.evaluateCount = guideObject.optInt("count");
            guide.money = guideObject.optInt("amount");
            guide.title = guideObject.optString("title");
            guide.unit = guideObject.optString("unit");
        }

        return guide;
    }
    
    public void handleMessage(Message message) {
        switch (message.what) {
            case GET_ALL_DONE:
                Slog.d(TAG, "-------------->GET_ALL_DONE");
                guideSummaryAdapter.setData(mGuideList);
                guideSummaryAdapter.notifyDataSetChanged();
                recyclerView.loadMoreComplete();
                stopLoadProgress();
                break;
            case GET_ALL_END:
                Slog.d(TAG, "-------------->GET_ALL_END");
                guideSummaryAdapter.setData(mGuideList);
                guideSummaryAdapter.notifyDataSetChanged();
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

    public static class Guide implements Serializable {
        public int tid;
        public String headPictureUrl;
        public String city;
        public String title;
        public int evaluateScore;
        public int evaluateCount;
        public int money;
        public String unit;
    }
    
    static class MyHandler extends Handler {
        WeakReference<GuideSummaryActivity> guideSummaryActivityWeakReference;

        MyHandler(GuideSummaryActivity guideSummaryActivity) {
            guideSummaryActivityWeakReference = new WeakReference<>(guideSummaryActivity);
        }

        @Override
        public void handleMessage(Message message) {
            GuideSummaryActivity guideSummaryActivity = guideSummaryActivityWeakReference.get();
            if (guideSummaryActivity != null) {
                guideSummaryActivity.handleMessage(message);
            }
        }
    }
}
          
                    
