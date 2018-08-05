package com.tongmenhui.launchak47.meet;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.adapter.MeetRecommendListAdapter;
import com.tongmenhui.launchak47.util.BaseFragment;
import com.tongmenhui.launchak47.util.HttpUtil;
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

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

/**
 * Created by haichao.zou on 2017/11/23.
 */

public class MeetDiscoveryFragment extends BaseFragment {

    private static final boolean debug = false;
    private static final String TAG = "MeetDiscoveryFragment";
    private View viewContent;
    private int mType = 0;
    private String mTitle;
    private List<MeetMemberInfo> meetMemberList = new ArrayList<>();
    private MeetMemberInfo meetMemberInfo;
    //+Begin add by xuchunping for use XRecyclerView support loadmore
    //private RecyclerView recyclerView;
    private static final int PAGE_SIZE = 6;//每页获取6条
    private XRecyclerView recyclerView;
    //-End add by xuchunping for use XRecyclerView support loadmore
    private MeetRecommendListAdapter meetListAdapter;
    // private String realname;
    private int uid;
    private static String responseText;
    JSONObject discovery_response;
    JSONArray discovery;
    private Boolean loaded = false;
    private Context mContext;
    private Handler handler;
    private static final int DONE = 1;

    private static final String  domain = "http://112.126.83.127:88/";
    private static final String get_discovery_url = HttpUtil.DOMAIN + "?q=meet/discovery/get";

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
        mContext = getActivity().getApplicationContext();
        initConentView();
        meetListAdapter = new MeetRecommendListAdapter(getContext());
        viewContent = inflater.inflate(R.layout.meet_discovery, container, false);
        recyclerView = (XRecyclerView) viewContent.findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState){
                if(newState == SCROLL_STATE_IDLE){
                    meetListAdapter.setScrolling(false);
                    meetListAdapter.notifyDataSetChanged();
                }else{
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
                new Handler().postDelayed(new Runnable(){
                    public void run() {
                        meetListAdapter.notifyDataSetChanged();
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
                            meetListAdapter.notifyDataSetChanged();
                        }
                    }
                }, 2000);
            }
        });
        //-End added by xuchunping
        recyclerView.setAdapter(meetListAdapter);
        return viewContent;

    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(debug) Slog.d(TAG, "=================onViewCreated===================");
        // initConentView();
    }

    public void initConentView(){
        if(debug) Slog.d(TAG, "===============initConentView==============");

        RequestBody requestBody = new FormBody.Builder().build();
        HttpUtil.sendOkHttpRequest(getContext(), get_discovery_url, requestBody, new Callback(){
            int check_login_user = 0;
            String user_name;

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if(debug) Slog.d(TAG, "response : "+responseText);
                getResponseText(responseText);
            }

            @Override
            public void onFailure(Call call, IOException e){

            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(Message message){
                if(message.what == DONE){
                    meetListAdapter.setData(meetMemberList);
                    meetListAdapter.notifyDataSetChanged();
                }
            }
        };
    }

    public void getResponseText(String responseText){

        if(debug) Slog.d(TAG, "====================getResponseText====================");

        if(!TextUtils.isEmpty(responseText)){
            try {
                discovery_response= new JSONObject(responseText);
                discovery = discovery_response.getJSONArray("discovery");
                set_meet_member_info(discovery);
                loaded = true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    public void set_meet_member_info(JSONArray discovery){
        int length = discovery.length();
        if(debug) Slog.d(TAG, "==========set_meet_member_info==========discovery length: "+length);
        try{
            for (int i=0; i< length; i++){
                JSONObject recommender = discovery.getJSONObject(i);
                meetMemberInfo = new MeetMemberInfo();

                meetMemberInfo.setRealname(recommender.getString("realname"));
                meetMemberInfo.setUid(recommender.getInt("uid"));
                meetMemberInfo.setPictureUri(recommender.getString("picture_uri"));
                meetMemberInfo.setBirthYear(recommender.getInt("birth_year"));
                meetMemberInfo.setHeight(recommender.getInt("height"));
                meetMemberInfo.setUniversity(recommender.getString("university"));
                meetMemberInfo.setDegree(recommender.getString("degree"));
                meetMemberInfo.setJobTitle(recommender.getString("job_title"));
                meetMemberInfo.setLives(recommender.getString("lives"));
                meetMemberInfo.setSituation(recommender.getInt("situation"));

                //requirement
                meetMemberInfo.setAgeLower(recommender.getInt("age_lower"));
                meetMemberInfo.setAgeUpper(recommender.getInt("age_upper"));
                meetMemberInfo.setRequirementHeight(recommender.getInt("requirement_height"));
                meetMemberInfo.setRequirementDegree(recommender.getString("requirement_degree"));
                meetMemberInfo.setRequirementLives(recommender.getString("requirement_lives"));
                meetMemberInfo.setRequirementSex(recommender.getInt("requirement_sex"));
                meetMemberInfo.setIllustration(recommender.getString("illustration"));


                // meetMemberInfo.setSelf(recommender.getInt("self"));
                meetMemberInfo.setBrowseCount(recommender.getInt("browse_count"));
                meetMemberInfo.setLovedCount(recommender.getInt("loved_count"));
                // meetMemberInfo.setLoved(recommender.getInt("loved"));
                // meetMemberInfo.setPraised(recommender.getInt("praised"));
                meetMemberInfo.setPraisedCount(recommender.getInt("praised_count"));
                //  meetMemberInfo.setPictureChain(recommender.getString("pictureChain"));
                // meetMemberInfo.setRequirementSet(recommender.getInt("requirementSet"));
                meetMemberList.add(meetMemberInfo);
            }

            handler.sendEmptyMessage(DONE);

        }catch (JSONException e){

        }

    }

}
