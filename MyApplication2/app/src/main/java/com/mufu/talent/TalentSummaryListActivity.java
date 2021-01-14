package com.mufu.talent;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mufu.R;
import com.mufu.adapter.TalentSummaryListAdapter;
import com.mufu.common.BaseAppCompatActivity;
import com.mufu.common.MyApplication;
import com.mufu.group.SubGroupActivity;
import com.mufu.util.CommonDialogFragmentInterface;
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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.mufu.common.MyApplication.getContext;
import static com.mufu.group.SingleGroupDetailsActivity.GET_SINGLE_GROUP_BY_GID;
import static com.mufu.group.SubGroupActivity.ADD_NEW_TALENT_DONE;
import static com.mufu.group.SubGroupActivity.GET_ALL_TALENTS;
import static com.mufu.group.SubGroupActivity.GET_TALENTS_BY_TYPE;
import static com.mufu.group.SubGroupActivity.GROUP_ADD_BROADCAST;
import static com.mufu.group.SubGroupActivity.getTalent;
import static com.mufu.talent.TalentAuthenticationDialogFragment.TALENT_AUTHENTICATION_RESULT_OK;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class TalentSummaryListActivity extends BaseAppCompatActivity implements CommonDialogFragmentInterface {
    public static final String SINGLE_GROUP_GET_MY = HttpUtil.DOMAIN + "?q=single_group/get_my";
    public static final int GET_MY_GROUP_DONE = 7;
    public static final int GET_TALENT_DONE = 15;
    private static final boolean isDebug = true;
    private static final String TAG = "TalentSummaryListActivity";
    private static final int PAGE_SIZE = 10;
    private static final String SINGLE_GROUP_GET_ALL = HttpUtil.DOMAIN + "?q=single_group/get_all";
    private static final String SINGLE_GROUP_UPDATE = HttpUtil.DOMAIN + "?q=single_group/update";
    private static final int GET_ALL_DONE = 1;
    private static final int UPDATE_ALL = 2;
    private static final int GET_ALL_END = 3;
    private static final int NO_UPDATE = 4;
    private static final int SET_AVATAR = 5;
    private static final int NO_MORE = 6;
    final int itemLimit = 3;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    private int type;
    private int mLoadSize = 0;
    private int mUpdateSize = 0;
    private Handler handler;
    private TalentSummaryListAdapter talentSummaryListAdapter;
    private XRecyclerView recyclerView;
    private List<SubGroupActivity.Talent> mTalentList = new ArrayList<>();
    private ViewGroup myGroupView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.talent_summary_list);
        handler = new TalentSummaryListActivity.MyHandler(this);
        if (getIntent() != null){
            type = getIntent().getIntExtra("type", -1);
        }

        initView();

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

        loadData();
    }

    private void initView() {
        recyclerView = findViewById(R.id.talent_summary_list);
        talentSummaryListAdapter = new TalentSummaryListAdapter(getContext());
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
                    talentSummaryListAdapter.setScrolling(false);
                    talentSummaryListAdapter.notifyDataSetChanged();
                } else {
                    talentSummaryListAdapter.setScrolling(true);
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

        talentSummaryListAdapter.setItemClickListener(new TalentSummaryListAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Slog.d(TAG, "==========click : " + position);
                Intent intent = new Intent(getContext(), TalentDetailsActivity.class);
                //intent.putExtra("talent", mTalentList.get(position));
                intent.putExtra("tid", mTalentList.get(position).tid);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(talentSummaryListAdapter);

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.custom_actionbar), font);
    }

    protected void loadData() {

        String url = GET_TALENTS_BY_TYPE;
        final int page = mTalentList.size() / PAGE_SIZE;

        FormBody.Builder builder = new FormBody.Builder();
        builder.add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page));

        if (type != -1){
            builder.add("type", String.valueOf(type));
        }else {
            url = GET_ALL_TALENTS;
        }

        RequestBody requestBody = builder.build();

        HttpUtil.sendOkHttpRequest(getContext(), url, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject talentResponse = null;

                        try {
                            talentResponse = new JSONObject(responseText);
                            if (talentResponse != null) {
                                mLoadSize = processResponse(talentResponse);

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

                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private int processResponse(JSONObject talentResponse) {

        int singGroupSize = 0;
        JSONArray talentArray = null;

        if (talentResponse != null) {
            talentArray = talentResponse.optJSONArray("talents");
        }

        if (talentArray != null) {
            singGroupSize = talentArray.length();
            if (singGroupSize > 0) {
                for (int i = 0; i < talentArray.length(); i++) {
                    JSONObject talentObject = talentArray.optJSONObject(i);
                    if (talentObject != null) {
                        SubGroupActivity.Talent talent = getTalent(talentObject);
                        mTalentList.add(talent);
                    }
                }
            }
        }

        return singGroupSize;
    }

    private int processUpdateResponse(JSONObject talentResponse) {
        List<SubGroupActivity.Talent> mTalentUpdateList = new ArrayList<>();
        JSONArray talentArray = null;
        if (talentResponse != null) {
            talentArray = talentResponse.optJSONArray("talents");
        }

        if (talentArray != null) {
            if (talentArray.length() > 0) {
                mTalentUpdateList.clear();
                for (int i = 0; i < talentArray.length(); i++) {
                    JSONObject talentObject = talentArray.optJSONObject(i);
                    if (talentObject != null) {
                        SubGroupActivity.Talent talent = getTalent(talentObject);
                        mTalentUpdateList.add(talent);
                    }
                }
                mTalentList.addAll(0, mTalentUpdateList);
                Message message = new Message();
                message.what = UPDATE_ALL;
                Bundle bundle = new Bundle();
                bundle.putInt("update_size", mTalentUpdateList.size());
                message.setData(bundle);
                handler.sendMessage(message);
            } else {
                handler.sendEmptyMessage(GET_ALL_END);
            }
        }

        return talentArray != null ? talentArray.length() : 0;
    }

    public void updateData() {
        String last = SharedPreferencesUtils.getSingleGroupLast(getContext());
        RequestBody requestBody = new FormBody.Builder()
                .add("last", last)
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(0))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), SINGLE_GROUP_UPDATE, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject singleGroupResponse = null;

                        try {
                            singleGroupResponse = new JSONObject(responseText);
                            if (singleGroupResponse != null) {
                                int current = singleGroupResponse.optInt("current");
                                Slog.d(TAG, "----------------->current: " + current);
                                SharedPreferencesUtils.setSingleGroupLast(getContext(), String.valueOf(current));

                                mUpdateSize = processUpdateResponse(singleGroupResponse);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (mUpdateSize > 0) {
                            handler.sendEmptyMessage(UPDATE_ALL);
                        } else {
                            handler.sendEmptyMessage(NO_UPDATE);
                        }

                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {}
        });
    }

    public void handleMessage(Message message) {
        switch (message.what) {
            case GET_ALL_DONE:
                talentSummaryListAdapter.setData(mTalentList, recyclerView.getWidth());
                talentSummaryListAdapter.notifyDataSetChanged();
                //recyclerView.refreshComplete();
                recyclerView.loadMoreComplete();
                stopLoadProgress();
                break;
            case GET_ALL_END:
                talentSummaryListAdapter.setData(mTalentList, recyclerView.getWidth());
                talentSummaryListAdapter.notifyDataSetChanged();
                //recyclerView.refreshComplete();
                recyclerView.loadMoreComplete();
                recyclerView.setNoMore(true);
                stopLoadProgress();
                break;

            case NO_MORE:
                recyclerView.setNoMore(true);
                recyclerView.loadMoreComplete();
                stopLoadProgress();
                break;
            case UPDATE_ALL:
                Bundle bundle = message.getData();
                int updateSize = bundle.getInt("update_size");
                talentSummaryListAdapter.setData(mTalentList, recyclerView.getWidth());
                talentSummaryListAdapter.notifyItemRangeInserted(0, updateSize);
                talentSummaryListAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();
                break;
            case NO_UPDATE:
                recyclerView.refreshComplete();
                mUpdateSize = 0;
                break;
            case ADD_NEW_TALENT_DONE:
                talentSummaryListAdapter.setData(mTalentList, recyclerView.getWidth());
                talentSummaryListAdapter.notifyItemInserted(0);
                talentSummaryListAdapter.notifyDataSetChanged();
                if (mTalentList.size() <= PAGE_SIZE) {
                    recyclerView.loadMoreComplete();
                }
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (isDebug)
            Slog.d(TAG, "===================onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode);
        if (requestCode == Activity.RESULT_FIRST_USER) {
            switch (resultCode) {

            }
        }
    }

    @Override
    public void onBackFromDialog(int type, int result, boolean status) {
        switch (type) {
            case TALENT_AUTHENTICATION_RESULT_OK:
                if (status == true) {
                    getMyNewAddedTalent(result);
                }
                break;
            default:
                break;
        }
    }

    private void getMyNewAddedTalent(int gid) {
        RequestBody requestBody = new FormBody.Builder()
                .add("gid", String.valueOf(gid))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_SINGLE_GROUP_BY_GID, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (isDebug) Slog.d(TAG, "==========response body : " + response.body());

                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject singleGroupResponse = null;
                        try {
                            singleGroupResponse = new JSONObject(responseText);
                            if (singleGroupResponse != null) {
                                processNewAddResponse(singleGroupResponse);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void processNewAddResponse(JSONObject subGroupResponse) {
        JSONObject subGroupObject = null;
        if (subGroupResponse != null) {
            subGroupObject = subGroupResponse.optJSONObject("single_group");
        }

        if (subGroupObject != null) {
            SubGroupActivity.Talent talent = getTalent(subGroupObject);
            mTalentList.add(0, talent);
            handler.sendEmptyMessage(ADD_NEW_TALENT_DONE);
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
        WeakReference<TalentSummaryListActivity> talentSummaryListActivityWeakReference;

        MyHandler(TalentSummaryListActivity talentSummaryListActivity) {
            talentSummaryListActivityWeakReference = new WeakReference<>(talentSummaryListActivity);
        }

        @Override
        public void handleMessage(Message message) {
            TalentSummaryListActivity talentSummaryListActivity = talentSummaryListActivityWeakReference.get();
            if (talentSummaryListActivity != null) {
                talentSummaryListActivity.handleMessage(message);
            }
        }
    }

    private class SingleGroupReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case GROUP_ADD_BROADCAST:
                    Slog.d(TAG, "==========GROUP_ADD_BROADCAST");
                    int gid = intent.getIntExtra("gid", 0);
                    if (gid > 0) {
                        getMyNewAddedTalent(gid);
                    }
                    break;
            }

        }
    }

}
    
