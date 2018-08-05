package com.tongmenhui.launchak47.meet;

//import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.adapter.MeetRecommendListAdapter;
import com.tongmenhui.launchak47.util.BaseFragment;
import com.tongmenhui.launchak47.util.HttpUtil;
import com.tongmenhui.launchak47.util.ParseUtils;
import com.tongmenhui.launchak47.util.Slog;

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

import static android.content.Context.MODE_PRIVATE;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

/**
 * Created by super-zou on 17-9-11.
 */

public class MeetRecommendFragment extends BaseFragment {
    private static final boolean debug = false;
    private static final String TAG = "MeetRecommendFragment";
    private View viewContent;
    private int mType = 0;
    private String mTitle;
    private List<MeetMemberInfo> meetList = new ArrayList<>();
    private MeetMemberInfo meetMemberInfo;
    //+Begin add by xuchunping for use XRecyclerView support loadmore
    //private RecyclerView recyclerView;
    private static final int PAGE_SIZE = 6;//每页获取6条
    private XRecyclerView recyclerView;
    //-End add by xuchunping for use XRecyclerView support loadmore
    private MeetRecommendListAdapter meetRecommendListAdapter;
   // private String realname;
    private int uid;
    private static String responseText;
    JSONObject recommend_response;
    JSONArray recommendation;
    private Boolean loaded = false;
    private Handler handler;
    private static final int DONE = 1;

    private static final String get_recommend_url = HttpUtil.DOMAIN + "?q=meet/recommend";

    @Override
    protected void initView(View view){

    }

    @Override
    protected void loadData(boolean firstInit){

    }

    @Override
    protected int getLayoutId(){
        return 0;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(debug) Slog.d(TAG, "=================onCreateView===================");
        initContentView();
        meetRecommendListAdapter = new MeetRecommendListAdapter(getContext());
        viewContent = inflater.inflate(R.layout.meet_recommend, container, false);
        TextView addMeetInfo = (TextView)viewContent.findViewById(R.id.meet_info_add);
        addMeetInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FillMeetInfoActivity.class);
                startActivity(intent);
            }
        });
        recyclerView = (XRecyclerView) viewContent.findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState){
                if(newState == SCROLL_STATE_IDLE){
                    meetRecommendListAdapter.setScrolling(false);
                    meetRecommendListAdapter.notifyDataSetChanged();
                }else{
                    meetRecommendListAdapter.setScrolling(true);
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

        recyclerView.getDefaultFootView().setLoadingHint("上拉查看更多");
        recyclerView.getDefaultFootView().setNoMoreHint("全部加载完成");
        //recyclerView.setArrowImageView(R.drawable.iconfont_downgrey);//TODO 可设置下拉刷新图标
        final int itemLimit = 5;

        // When the item number of the screen number is list.size-2,we call the onLoadMore
        recyclerView.setLimitNumberToCallLoadMore(4);
        recyclerView.setRefreshProgressStyle(ProgressStyle.BallBeat);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);

        recyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                initContentView();
                new Handler().postDelayed(new Runnable(){
                    public void run() {
                        meetRecommendListAdapter.notifyDataSetChanged();
                        if(recyclerView != null)
                            recyclerView.refreshComplete();
                    }
                }, 2000);            //refresh data here
            }

            @Override
            public void onLoadMore() {
                    new Handler().postDelayed(new Runnable(){
                        public void run() {
                            //TODO test
                            if(recyclerView != null) {
                                recyclerView.loadMoreComplete();
                                meetRecommendListAdapter.notifyDataSetChanged();
                            }
                        }
                    }, 2000);
            }
        });
        //-End added by xuchunping

        recyclerView.setAdapter(meetRecommendListAdapter);
        return viewContent;

    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(debug) Slog.d(TAG, "=================onViewCreated===================");
       // initConentView();
    }

    public void initContentView(){
        if(debug) Slog.d(TAG, "===============initConentView==============");

        int page = meetList.size() / PAGE_SIZE + 1;
        RequestBody requestBody = new FormBody.Builder()
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();
        Log.d(TAG, "initContentView requestBody:"+requestBody.toString()+" page:"+page);
        HttpUtil.sendOkHttpRequest(getContext(), get_recommend_url, requestBody, new Callback(){
            int check_login_user = 0;
            String user_name;

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                //Slog.d(TAG, "response : "+responseText);
                Log.d(TAG, "onResponse responseText:"+responseText);
                getResponseText(responseText);
            }

            @Override
            public void onFailure(Call call, IOException e){
                Log.e(TAG, "onFailure e:"+e);
            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(Message message){
                if(message.what == DONE){
                    meetRecommendListAdapter.setData(meetList);
                    meetRecommendListAdapter.notifyDataSetChanged();
                }
            }
        };

       // getResponseText(responseText);

    }

    public void getResponseText(String responseText){

        if(debug) Slog.d(TAG, "====================getResponseText: "+responseText);
        //+Begin added by xuchunping
        List<MeetMemberInfo> tempList = ParseUtils.getMeetList(responseText);
        if (null != tempList) {
            meetList.addAll(tempList);
            Log.d(TAG, "getResponseText list.size:"+tempList.size());
            if (tempList.size() < PAGE_SIZE) {
                //数据全部加载完成，没有更多数据了
                recyclerView.setLoadingMoreEnabled(false);//禁用上拉刷新
            }
        }
        handler.sendEmptyMessage(DONE);
        //-End added by xuchunping
    }
}
