package com.hetang.meet;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.hetang.launchak47.R;
import com.hetang.adapter.MeetRecommendListAdapter;
import com.hetang.util.BaseFragment;
import com.hetang.util.HttpUtil;
import com.hetang.util.ParseUtils;
import com.hetang.util.SharedPreferencesUtils;
import com.hetang.util.Slog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

/**
 * Created by haichao.zou on 2017/11/23.
 */

public class MeetDiscoveryFragment extends BaseFragment {

    private static final boolean debug = false;
    private static final String TAG = "MeetDiscoveryFragment";
    //+Begin add by xuchunping for use XRecyclerView support loadmore
    //private RecyclerView recyclerView;
    private static final int PAGE_SIZE = 6;
    private static final int DONE = 1;
    private static final int UPDATE = 2;
    private static final String domain = "http://112.126.83.127:88/";
    private static final String get_discovery_url = HttpUtil.DOMAIN + "?q=meet/discovery/get";
    private static String responseText;
    JSONObject discovery_response;
    JSONArray discovery;
    private View viewContent;
    private int mType = 0;
    private String mTitle;
    private List<MeetMemberInfo> meetMemberList = new ArrayList<>();
    private MeetMemberInfo meetMemberInfo;
    private XRecyclerView recyclerView;
    private int mTempSize;
    //-End add by xuchunping for use XRecyclerView support loadmore
    private MeetRecommendListAdapter meetListAdapter;
    // private String realname;
    private int uid;
    private Boolean loaded = false;
    private Context mContext;
    private Handler handler;

    @Override
    protected void initView(View view) {

    }

    @Override
    protected void loadData() {

    }

    @Override
    protected int getLayoutId() {
        return 0;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (debug) Slog.d(TAG, "=================onCreateView===================");
        mContext = getActivity().getApplicationContext();
        initContentView();
        meetListAdapter = new MeetRecommendListAdapter(getContext());
        viewContent = inflater.inflate(R.layout.meet_discovery, container, false);
        recyclerView = (XRecyclerView) viewContent.findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    meetListAdapter.setScrolling(false);
                    meetListAdapter.notifyDataSetChanged();
                } else {
                    meetListAdapter.setScrolling(true);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        //+Begin added by xuchunping
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setRefreshProgressStyle(BallSpinFadeLoader);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallRotate);
//        mRecyclerView.setArrowImageView(R.drawable.);

        recyclerView
                .getDefaultRefreshHeaderView()
                .setRefreshTimeVisible(true);

        recyclerView.getDefaultFootView().setLoadingHint(getString(R.string.loading_pull_up_tip));
        recyclerView.getDefaultFootView().setNoMoreHint(getString(R.string.loading_no_more));
        final int itemLimit = 5;

        // When the item number of the screen number is list.size-2,we call the onLoadMore
        recyclerView.setLimitNumberToCallLoadMore(4);
        recyclerView.setRefreshProgressStyle(ProgressStyle.BallBeat);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);

        recyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                updateData();
            }

            @Override
            public void onLoadMore() {
                initContentView();
            }
        });
        //-End added by xuchunping
        recyclerView.setAdapter(meetListAdapter);
        return viewContent;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (debug) Slog.d(TAG, "=================onViewCreated===================");
        // initConentView();
    }

    public void initContentView() {
        if (debug) Slog.d(TAG, "===============initConentView==============");

        int page = meetMemberList.size() / PAGE_SIZE;
        RequestBody requestBody = new FormBody.Builder()
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();
        Log.d(TAG, "initContentView requestBody:" + requestBody.toString() + " page:" + page);
        HttpUtil.sendOkHttpRequest(getContext(), get_discovery_url, requestBody, new Callback() {
            int check_login_user = 0;
            String user_name;

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (debug) Slog.d(TAG, "response : " + responseText);
                getResponseText(responseText);
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });

        handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if (message.what == DONE) {
                    meetListAdapter.setData(meetMemberList);
                    meetListAdapter.notifyDataSetChanged();
                    recyclerView.refreshComplete();

                    if (mTempSize < PAGE_SIZE) {
                        //loading finished
                        recyclerView.setNoMore(true);
                        recyclerView.setLoadingMoreEnabled(false);
                    }
                } else if (message.what == UPDATE) {
                    //save last update timemills
                    SharedPreferencesUtils.setDiscoveryLast(getContext(), String.valueOf(System.currentTimeMillis() / 1000));

                    meetListAdapter.setData(meetMemberList);
                    meetListAdapter.notifyDataSetChanged();
                    recyclerView.refreshComplete();
                }
            }
        };
    }

    public void getResponseText(String responseText) {
        if (debug) Slog.d(TAG, "====================getResponseText====================");
        //+Begin added by xuchunping
        List<MeetMemberInfo> tempList = ParseUtils.getMeetDiscoveryList(responseText);
        mTempSize = 0;
        if (null != tempList) {
            mTempSize = tempList.size();
            meetMemberList.addAll(tempList);
            Log.d(TAG, "getResponseText list.size:" + tempList.size());
        }
        handler.sendEmptyMessage(DONE);
    }

    private void updateData() {
        String last = SharedPreferencesUtils.getDiscoveryLast(getContext());
        RequestBody requestBody = new FormBody.Builder().add("last", last)
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(0))
                .build();
        Log.d(TAG, "updateData requestBody:" + requestBody.toString() + " last:" + last);
        HttpUtil.sendOkHttpRequest(getContext(), get_discovery_url, requestBody, new Callback() {
            int check_login_user = 0;
            String user_name;

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (debug) Slog.d(TAG, "response : " + responseText);
                Log.d(TAG, "response : " + responseText);
                if (responseText != null) {
                    List<MeetMemberInfo> tempList = ParseUtils.getMeetDiscoveryList(responseText);
                    if (null != tempList && tempList.size() != 0) {
                        mTempSize = tempList.size();
                        meetMemberList.clear();
                        meetMemberList.addAll(tempList);
                        Log.d(TAG, "getResponseText list.size:" + tempList.size());
                    }
                    handler.sendEmptyMessage(UPDATE);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }
    //-End added by xuchunping
}
