package com.tongmenhui.launchak47.meet;

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

import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.adapter.MeetDynamicsListAdapter;
import com.tongmenhui.launchak47.util.BaseFragment;
import com.tongmenhui.launchak47.util.HttpUtil;
import com.tongmenhui.launchak47.util.Slog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
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
 * Created by haichao.zou on 2017/11/20.
 */

public class MeetDynamicsFragment extends BaseFragment {

    private static final boolean debug = true;
    private static final String TAG = "MeetDynamicsFragment";
    private View viewContent;
    private List<MeetDynamics> meetList = new ArrayList<>();

    private MeetDynamics meetDynamics;
    //+Begin add by xuchunping for use XRecyclerView support loadmore
    //private RecyclerView recyclerView;
    private static final int PAGE_SIZE = 6;
    private int mTempSize;
    private XRecyclerView recyclerView;
    //-End add by xuchunping for use XRecyclerView support loadmore
    private DynamicsComment dynamicsComment;
    private MeetDynamicsListAdapter meetDynamicsListAdapter = new MeetDynamicsListAdapter(getContext());
    JSONObject dynamics_response;
    JSONObject commentResponse;
    JSONArray dynamics;
    JSONArray commentArray;
    JSONArray praiseArray;
    private Handler handler;
    private static final int DONE = 1;
    private static final int UPDATE = 2;

    String requstUrl = "";
    RequestBody requestBody = null;
    long timeStampSec = System.currentTimeMillis()/1000;
    String timeStamp = String.format("%010d", timeStampSec);
    SharedPreferences.Editor editor = getContext().getSharedPreferences("access_record", MODE_PRIVATE).edit();

    private static final String dynamics_url = HttpUtil.DOMAIN + "?q=meet/activity/get";
    private static final String getDynamics_update_url = HttpUtil.DOMAIN + "?q=meet/activity/update";
    String request_comment_url = HttpUtil.DOMAIN + "?q=meet/activity/interact/get";

    /*
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Slog.d(TAG, "=================onViewCreated===================");
    }
    */

    @Override
    protected int getLayoutId(){
        int layoutId = R.layout.meet_dynamics;
        return layoutId;
    }

    @Override
    protected void initView(View convertView){
        recyclerView = (XRecyclerView) convertView.findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    meetDynamicsListAdapter.setScrolling(false);
                    meetDynamicsListAdapter.notifyDataSetChanged();
                } else {
                    meetDynamicsListAdapter.setScrolling(true);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        //+Begin added by xuchunping
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setRefreshProgressStyle(BallSpinFadeLoader);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallRotate);
        //mRecyclerView.setArrowImageView(R.drawable.);

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
                loadData();
            }
        });
        //-End added by xuchunping
        recyclerView.setAdapter(meetDynamicsListAdapter);

    }


    @Override
    protected void loadData(){
        if(debug) Slog.d(TAG, "===============initData==============");
        handler = new MyHandler(this);

        editor.putString("last", timeStamp);
        editor.apply();
        requstUrl = dynamics_url;
        int page = meetList.size() / PAGE_SIZE + 1;
        requestBody = new FormBody.Builder()
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();
        Log.d(TAG, "loadData requestBody:"+requestBody.toString()+" page:"+page);
        HttpUtil.sendOkHttpRequest(getContext(), requstUrl, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.body() != null){
                    String responseText = response.body().string();
                    //Slog.d(TAG, "==========response : "+response.body());
                    Slog.d(TAG, "==========response text : "+responseText);
                    if(responseText != null){
                        List<MeetDynamics> tempList = getResponseText(responseText);
                        mTempSize = 0;
                        if (null != tempList) {
                            mTempSize = tempList.size();
                            meetList.addAll(tempList);
                            Log.d(TAG, "getResponseText list.size:"+tempList.size());
                        }
                        handler.sendEmptyMessage(DONE);
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    public void updateData(){
        handler = new MyHandler(this);

        SharedPreferences preferences = getContext().getSharedPreferences("access_record", MODE_PRIVATE);
        String last = preferences.getString("last", "");
        if(debug) Slog.d(TAG, "=======last:"+last);

        requstUrl = getDynamics_update_url;
        int page = meetList.size() / PAGE_SIZE + 1;
        requestBody = new FormBody.Builder().add("last", last)
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();

        editor.putString("last", timeStamp);
        editor.apply();
        Log.d(TAG, "updateData requestBody:"+requestBody.toString()+" page="+page);
        HttpUtil.sendOkHttpRequest(getContext(), requstUrl, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.body() != null){
                    String responseText = response.body().string();
                    Slog.d(TAG, "==========response : "+response.body());
                    Slog.d(TAG, "==========response text : "+responseText);
                    if(responseText != null){
                        List<MeetDynamics> tempList = getResponseText(responseText);
                        if (null != tempList) {
                            mTempSize = 0;
                            meetList.clear();
                            mTempSize = tempList.size();
                            meetList.addAll(tempList);
                            Log.d(TAG, "getResponseText list.size:"+tempList.size());
                        }
                        handler.sendEmptyMessage(UPDATE);
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    public List<MeetDynamics> getResponseText(String responseText) {

        //Slog.d(TAG, "====================getResponseText====================");

        if (!TextUtils.isEmpty(responseText)) {
            try {
                dynamics_response = new JSONObject(responseText);
                dynamics = dynamics_response.getJSONArray("activity");
                if (dynamics.length() > 0) {
                    return set_meet_member_info(dynamics);
                }else{
                    if(debug) Slog.d(TAG, "=============response content empty==============");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            if(debug) Slog.d(TAG, "=============response empty==============");
        }
        return null;
    }

    public List<MeetDynamics> set_meet_member_info(JSONArray dynamicsArray) {
        List<MeetDynamics> tempList = new ArrayList<MeetDynamics>();
        int length = dynamicsArray.length();
        Slog.d(TAG, "==========set_meet_member_info==========dynamics length: "+length);
        try {
            for (int i = 0; i < length; i++) {
                JSONObject dynamics = dynamicsArray.getJSONObject(i);
                //Slog.d(TAG, "==========dynamicsArray.getJSONObject: " + dynamics);
                meetDynamics = new MeetDynamics();

                meetDynamics.setRealname(dynamics.getString("realname"));
                meetDynamics.setUid(dynamics.getInt("uid"));
                meetDynamics.setPictureUri(dynamics.getString("picture_uri"));

                if(!dynamics.isNull("birth_year")){
                    meetDynamics.setBirthYear(dynamics.getInt("birth_year"));
                }
                if(!dynamics.isNull("height")){
                    meetDynamics.setHeight(dynamics.getInt("height"));
                }
                if(!dynamics.isNull("degree")){
                    meetDynamics.setDegree(dynamics.getString("degree"));
                }
                if(!dynamics.isNull("university")){
                    meetDynamics.setUniversity(dynamics.getString("university"));
                }
                if(!dynamics.isNull("job_title")){
                    meetDynamics.setJobTitle(dynamics.getString("job_title"));
                }
                if(!dynamics.isNull("lives")){
                    meetDynamics.setLives(dynamics.getString("lives"));
                }
                if(!dynamics.isNull("situation")){
                    meetDynamics.setSituation(dynamics.getInt("situation"));
                }

                //requirement
                if(!dynamics.isNull("age_lower")){
                    meetDynamics.setAgeLower(dynamics.getInt("age_lower"));
                }
                if(!dynamics.isNull("age_upper")){
                    meetDynamics.setAgeUpper(dynamics.getInt("age_upper"));
                }
                if(!dynamics.isNull("requirement_height")){
                    meetDynamics.setRequirementHeight(dynamics.getInt("requirement_height"));
                }
                if(!dynamics.isNull("requirement_degree")){
                    meetDynamics.setRequirementDegree(dynamics.getString("requirement_degree"));
                }
                if(!dynamics.isNull("requirement_lives")){
                    meetDynamics.setRequirementLives(dynamics.getString("requirement_lives"));
                }
                if(!dynamics.isNull("requirement_sex")){
                    meetDynamics.setRequirementSex(dynamics.getInt("requirement_sex"));
                }
                if(!dynamics.isNull("illustration")){
                    meetDynamics.setIllustration(dynamics.getString("illustration"));
                }
                //interact count
                meetDynamics.setBrowseCount(dynamics.getInt("browse_count"));
                meetDynamics.setLovedCount(dynamics.getInt("loved_count"));
                meetDynamics.setPraisedCount(dynamics.getInt("praised_count"));

                //dynamics content
                meetDynamics.setCreated(dynamics.getLong("created"));
                String content = dynamics.getString("content");
                if (content != null && content.length() != 0) {
                    meetDynamics.setContent(content);
                }
                if (!dynamics.isNull("activity_picture")) {
                    String dynamics_pictures = dynamics.getString("activity_picture");
                    if (!"".equals(dynamics_pictures)) {
                        meetDynamics.setActivityPicture(dynamics_pictures);
                    }
                }

                meetDynamics.setAid(dynamics.getLong("aid"));

                getDynamicsComment(meetDynamics, dynamics.getLong("aid"));

                tempList.add(meetDynamics);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tempList;

    }

    public void getDynamicsComment(final MeetDynamics meetDynamics, Long aid) {

        RequestBody requestBody = new FormBody.Builder().add("aid", aid.toString()).build();

        HttpUtil.sendOkHttpRequest(getContext(), request_comment_url, requestBody, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                //Slog.d(TAG, "######################comment: "+responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        commentResponse = new JSONObject(responseText);
                        commentArray = commentResponse.getJSONArray("comment");
                        praiseArray = commentResponse.getJSONArray("praise");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (commentArray.length() > 0) {
                        setDynamicsComment(meetDynamics, commentArray, praiseArray);
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });

    }

    public void setDynamicsComment(MeetDynamics meetDynamics, JSONArray commentArray, JSONArray praiseArray) {
        JSONObject comment;
        JSONObject praise;
        meetDynamics.setPraiseCount(praiseArray.length());
        meetDynamics.setCommentCount(commentArray.length());
        //Slog.d(TAG, "==========comment:  " + commentArray);
        try {
            for (int i = 0; i < commentArray.length(); i++) {
                comment = commentArray.getJSONObject(i);
                dynamicsComment = new DynamicsComment();
                dynamicsComment.setType(comment.getInt("type"));
                dynamicsComment.setCid(comment.getInt("cid"));
                dynamicsComment.setAid(comment.getInt("aid"));
                dynamicsComment.setPictureUrl(comment.getString("picture_uri"));
                if (!comment.isNull("author_uid")) {
                    dynamicsComment.setAuthorUid(comment.getLong("author_uid"));
                }
                if (!comment.isNull("author_name")) {
                    dynamicsComment.setAuthorName(comment.getString("author_name"));
                }
                dynamicsComment.setCommenterName(comment.getString("commenter_name"));
                dynamicsComment.setCommenterUid(comment.getLong("commenter_uid"));
                dynamicsComment.setContent(comment.getString("content"));
                meetDynamics.addComment(dynamicsComment);
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
        //Slog.d(TAG, "*********************dynamics comment size: "+meetDynamics.getComment().size());
    }

    @Override
    public void onResume(){
        super.onResume();
       // initConentView();
       // initData();
        Slog.d(TAG, "=============onResume");
       // loadDynamicsData(false);

    }

    static class MyHandler extends Handler {
        WeakReference<MeetDynamicsFragment> meetDynamicsFragmentWeakReference;

        MyHandler(MeetDynamicsFragment meetDynamicsFragment) {
            meetDynamicsFragmentWeakReference = new WeakReference<MeetDynamicsFragment>(meetDynamicsFragment);
        }

        @Override
        public void handleMessage(Message message) {
            MeetDynamicsFragment mMeetDynamicsFragment = meetDynamicsFragmentWeakReference.get();
            if(mMeetDynamicsFragment != null){
                mMeetDynamicsFragment.handleMessage(message);
            }
        }
    }

    public void handleMessage(Message message){
        switch (message.what){
            case DONE:
                meetDynamicsListAdapter.setData(meetList);
                meetDynamicsListAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();

                if (mTempSize < PAGE_SIZE) {
                    //loading finished
                    recyclerView.setNoMore(true);
                    recyclerView.setLoadingMoreEnabled(false);
                }
                break;
            case UPDATE:
                meetDynamicsListAdapter.setData(meetList);
                meetDynamicsListAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();
                break;
        }
    }

}
