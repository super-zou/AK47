package com.hetang.meet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.hetang.R;
import com.hetang.adapter.MeetDynamicsListAdapter;
import com.hetang.main.AddDynamicsActivity;
import com.hetang.util.BaseFragment;
import com.hetang.util.CommentDialogFragment;
import com.hetang.util.CommentDialogFragmentInterface;
import com.hetang.util.HttpUtil;
import com.hetang.util.SharedPreferencesUtils;
import com.hetang.util.Slog;

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

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

/**
 * Created by haichao.zou on 2017/11/20.
 */

public class MeetDynamicsFragment extends BaseFragment {

    public static final int REQUEST_CODE = 1;
    private static final boolean debug = true;
    private static final String TAG = "MeetDynamicsFragment";
    //private MeetDynamicsListAdapter.ViewHolder viewHolder;
    //+Begin add by xuchunping for use XRecyclerView support loadmore
    //private RecyclerView recyclerView;
    private static final int PAGE_SIZE = 6;
    private static final int DONE = 1;
    private static final int UPDATE = 2;
    private static final int UPDATE_COMMENT = 3;
    private static final int ADD_COMMENT = 4;
    private static final String dynamics_url = HttpUtil.DOMAIN + "?q=meet/activity/get";
    private static final String getDynamics_update_url = HttpUtil.DOMAIN + "?q=meet/activity/update";
    JSONObject dynamics_response;
    JSONObject commentResponse;
    JSONArray dynamics;
    JSONArray commentArray;
    JSONArray praiseArray;
    String requstUrl = "";
    RequestBody requestBody = null;
    String request_comment_url = HttpUtil.DOMAIN + "?q=meet/activity/interact/get";
    String request_comment_add = HttpUtil.DOMAIN + "?q=meet/activity/interact/comment/add";
    private View viewContent;
    private List<MeetDynamics> meetList = new ArrayList<>();
    private MeetDynamics mMeetDynamics;
    private DynamicsComment mDynamicsComment;
    private int mTempSize;
    private XRecyclerView recyclerView;
    //-End add by xuchunping for use XRecyclerView support loadmore
    private DynamicsComment dynamicsComment;
    private MeetDynamicsListAdapter meetDynamicsListAdapter = new MeetDynamicsListAdapter(getContext());
    private Handler handler;
    private DynamicsAddBroadcastReceiver mReceiver = new DynamicsAddBroadcastReceiver();


    @Override
    protected int getLayoutId() {
        int layoutId = R.layout.meet_dynamics;
        return layoutId;
    }


    @Override
    protected void initView(View convertView) {

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

        recyclerView.getDefaultRefreshHeaderView().setRefreshTimeVisible(true);

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

        //callback from meetDynamicsListAdapter, when comment icon touched, will show comment input dialog
        meetDynamicsListAdapter.setOnCommentClickListener(new CommentDialogFragmentInterface() {
            @Override
            public void onCommentClick(MeetDynamics meetDynamics, DynamicsComment dynamicsComment) {
                //viewHolder = dynamicsViewHolder;
                mMeetDynamics = meetDynamics;
                mDynamicsComment = dynamicsComment;
                getCommentInputDialogFragment();
            }
        });

        recyclerView.setAdapter(meetDynamicsListAdapter);

        registerLoginBroadcast();
    }

    @Override
    protected void loadData() {
        if (debug) Slog.d(TAG, "===============initData==============");
        handler = new MyHandler(this);

        requstUrl = dynamics_url;
        int page = meetList.size() / PAGE_SIZE;
        requestBody = new FormBody.Builder()
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();
        Log.d(TAG, "loadData requestBody:" + requestBody.toString() + " page:" + page);
        HttpUtil.sendOkHttpRequest(getContext(), requstUrl, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    //Slog.d(TAG, "==========response : "+response.body());
                    Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null) {
                        List<MeetDynamics> tempList = getResponseText(responseText);
                        mTempSize = 0;
                        if (null != tempList) {
                            mTempSize = tempList.size();
                            meetList.addAll(tempList);
                            Log.d(TAG, "getResponseText list.size:" + tempList.size());
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

    private void updateData() {
        handler = new MyHandler(this);

        String last = SharedPreferencesUtils.getDynamicsLast(getContext());
        if (debug) Slog.d(TAG, "=======last:" + last);

        requstUrl = getDynamics_update_url;
        int page = 0;//meetList.size() / PAGE_SIZE + 1;
        requestBody = new FormBody.Builder().add("last", last)
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();

        //Log.d(TAG, "updateData requestBody:"+requestBody.toString()+" page="+page);
        HttpUtil.sendOkHttpRequest(getContext(), requstUrl, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    Slog.d(TAG, "==========response : " + response.body());
                    Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null) {
                        List<MeetDynamics> tempList = getResponseText(responseText);
                        if (null != tempList) {
                            mTempSize = 0;
                            meetList.clear();
                            mTempSize = tempList.size();
                            meetList.addAll(tempList);
                            Log.d(TAG, "getResponseText list.size:" + tempList.size());
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
                //dynamics = dynamics_response.getJSONArray("activity");
                dynamics = dynamics_response.optJSONArray("activity");
                if (dynamics != null && dynamics.length() > 0) {
                    return set_meet_member_info(dynamics);
                } else {
                    if (debug) Slog.d(TAG, "=============response content empty==============");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            if (debug) Slog.d(TAG, "=============response empty==============");
        }
        return null;
    }

    public List<MeetDynamics> set_meet_member_info(JSONArray dynamicsArray) {
        List<MeetDynamics> tempList = new ArrayList<MeetDynamics>();
        int length = dynamicsArray.length();
        Slog.d(TAG, "==========set_meet_member_info==========dynamics length: " + length);
        try {
            for (int i = 0; i < length; i++) {
                JSONObject dynamics = dynamicsArray.getJSONObject(i);
                //Slog.d(TAG, "==========dynamicsArray.getJSONObject: " + dynamics);
                MeetDynamics meetDynamics = new MeetDynamics();

                meetDynamics.setRealname(dynamics.getString("realname"));
                meetDynamics.setUid(dynamics.getInt("uid"));
                meetDynamics.setPictureUri(dynamics.getString("picture_uri"));

                if (!dynamics.isNull("birth_year")) {
                    meetDynamics.setBirthYear(dynamics.getInt("birth_year"));
                }
                if (!dynamics.isNull("height")) {
                    meetDynamics.setHeight(dynamics.getInt("height"));
                }
                if (!dynamics.isNull("degree")) {
                    meetDynamics.setDegree(dynamics.getString("degree"));
                }
                if (!dynamics.isNull("university")) {
                    meetDynamics.setUniversity(dynamics.getString("university"));
                }
                if (!dynamics.isNull("job_title")) {
                    meetDynamics.setJobTitle(dynamics.getString("job_title"));
                }
                if (!dynamics.isNull("lives")) {
                    meetDynamics.setLives(dynamics.getString("lives"));
                }
                if (!dynamics.isNull("situation")) {
                    meetDynamics.setSituation(dynamics.getInt("situation"));
                }

                //requirement
                if (!dynamics.isNull("age_lower")) {
                    meetDynamics.setAgeLower(dynamics.getInt("age_lower"));
                }
                if (!dynamics.isNull("age_upper")) {
                    meetDynamics.setAgeUpper(dynamics.getInt("age_upper"));
                }
                if (!dynamics.isNull("requirement_height")) {
                    meetDynamics.setRequirementHeight(dynamics.getInt("requirement_height"));
                }
                if (!dynamics.isNull("requirement_degree")) {
                    meetDynamics.setRequirementDegree(dynamics.getString("requirement_degree"));
                }
                if (!dynamics.isNull("requirement_lives")) {
                    meetDynamics.setRequirementLives(dynamics.getString("requirement_lives"));
                }
                if (!dynamics.isNull("requirement_sex")) {
                    meetDynamics.setRequirementSex(dynamics.getInt("requirement_sex"));
                }
                if (!dynamics.isNull("illustration")) {
                    meetDynamics.setIllustration(dynamics.getString("illustration"));
                }
                //interact count
                meetDynamics.setBrowseCount(dynamics.getInt("browse_count"));
                meetDynamics.setLovedCount(dynamics.getInt("loved_count"));
                meetDynamics.setPraisedCount(dynamics.getInt("praised_count"));
                meetDynamics.setLoved(dynamics.optInt("loved"));
                meetDynamics.setPraised(dynamics.optInt("praised"));

                //dynamics content
                if (!dynamics.isNull("created")) {
                    meetDynamics.setCreated(dynamics.getLong("created"));
                }
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

                getDynamicsComment(dynamics.getLong("aid"));

                tempList.add(meetDynamics);

            }
        } catch (JSONException e) {
            Log.d(TAG, "set_meet_member_info e==================" + e.toString());
        }
        return tempList;

    }

    public void getDynamicsComment(final Long aid) {
        //Log.d(TAG, "getDynamicsComment: aid:"+aid);
        RequestBody requestBody = new FormBody.Builder().add("aid", aid.toString()).build();

        HttpUtil.sendOkHttpRequest(getContext(), request_comment_url, requestBody, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                //Slog.d(TAG, "######################comment: "+responseText);
                Log.d(TAG, "getDynamicsComment: " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        commentResponse = new JSONObject(responseText);
                        commentArray = commentResponse.getJSONArray("comment");
                        praiseArray = commentResponse.getJSONArray("praise");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    MeetDynamics meetDynamics = getMeetDynamicsById(aid);
                    meetDynamics.setPraisedDynamics(commentResponse.optInt("praised"));
                    if (commentArray.length() > 0) {
                        setDynamicsComment(meetDynamics, commentArray, praiseArray);
                    }
                    if (null != praiseArray) {


                        meetDynamics.setPraisedDynamicsCount(praiseArray.length());
                        Log.d(TAG, "getDynamicsComment +++++++++++++++ praiseArray.length(): " + praiseArray.length());
                    }
                    handler.sendEmptyMessage(UPDATE_COMMENT);
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
        //meetDynamics.setPraiseCount(praiseArray.length());
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Slog.d(TAG, "*********************dynamics comment size: "+meetDynamics.getComment().size());
    }

    @Override
    public void onResume() {
        super.onResume();
        Slog.d(TAG, "=============onResume");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterLoginBroadcast();
    }

    public void handleMessage(Message message) {
        switch (message.what) {
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
                //save last update timemills
                SharedPreferencesUtils.setDynamicsLast(getContext(), String.valueOf(System.currentTimeMillis() / 1000));

                meetDynamicsListAdapter.setData(meetList);
                meetDynamicsListAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();
                break;
            case UPDATE_COMMENT:
                meetDynamicsListAdapter.setData(meetList);
                meetDynamicsListAdapter.notifyDataSetChanged();
                break;
            case ADD_COMMENT:
                meetDynamicsListAdapter.notifyItemChanged(meetDynamicsListAdapter.getDynamicsItemPosition());
                break;
            default:
                break;
        }
    }

    private MeetDynamics getMeetDynamicsById(long aId) {
        for (int i = 0; i < meetList.size(); i++) {
            if (aId == meetList.get(i).getAid()) {
                return meetList.get(i);
            }
        }
        return null;
    }

    public void getCommentInputDialogFragment() {
        CommentDialogFragment commentDialogFragment = new CommentDialogFragment();
        commentDialogFragment.setTargetFragment(MeetDynamicsFragment.this, REQUEST_CODE);
        commentDialogFragment.show(getFragmentManager(), "CommentDialogFragment");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            final String commentText = data.getStringExtra("comment_text");
            Long aid = mMeetDynamics.getAid();
            Toast.makeText(getContext(), "onActivityResult: " + commentText + " aid: " + aid.toString(), Toast.LENGTH_LONG).show();
            //mDynamicsComment.setContent(commentText);
            FormBody.Builder builder = new FormBody.Builder().add("aid", aid.toString())
                    .add("type", String.valueOf(mDynamicsComment.getType()))
                    .add("content", commentText);

            if (mDynamicsComment.getType() == 1) {
                builder.add("name", mDynamicsComment.getAuthorName()).add("uid", String.valueOf(mDynamicsComment.getAuthorUid()));
            }

            RequestBody requestBody = builder.build();

            HttpUtil.sendOkHttpRequest(getContext(), request_comment_add, requestBody, new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    Slog.d(TAG, "######################comment: " + responseText);

                    if (!TextUtils.isEmpty(responseText)) {
                        try {
                            JSONObject commentResponseObj = new JSONObject(responseText);
                            JSONObject comment = commentResponseObj.getJSONObject("comment");
                            mDynamicsComment.setPictureUrl(comment.getString("picture_uri"));
                            mDynamicsComment.setContent(comment.getString("content"));
                            mDynamicsComment.setCommenterName(comment.getString("commenter_name"));
                            mDynamicsComment.setCommenterUid(comment.getLong("commenter_uid"));
                            if (mDynamicsComment.getType() == 1) {
                                mDynamicsComment.setAuthorName(comment.getString("author_name"));
                                mDynamicsComment.setAuthorUid(comment.getLong("author_uid"));
                            }
                            mDynamicsComment.setTimeStamp(comment.getInt("created"));
                            mMeetDynamics.addComment(mDynamicsComment);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    handler.sendEmptyMessage(ADD_COMMENT);
                }

                @Override
                public void onFailure(Call call, IOException e) {

                }
            });
        }
    }

    //register local broadcast to receive DYNAMICS_ADD_BROADCAST
    private void registerLoginBroadcast() {
        IntentFilter intentFilter = new IntentFilter(AddDynamicsActivity.DYNAMICS_ADD_BROADCAST);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, intentFilter);
    }

    //unregister local broadcast
    private void unRegisterLoginBroadcast() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
    }

    static class MyHandler extends Handler {
        WeakReference<MeetDynamicsFragment> meetDynamicsFragmentWeakReference;

        MyHandler(MeetDynamicsFragment meetDynamicsFragment) {
            meetDynamicsFragmentWeakReference = new WeakReference<MeetDynamicsFragment>(meetDynamicsFragment);
        }

        @Override
        public void handleMessage(Message message) {
            MeetDynamicsFragment mMeetDynamicsFragment = meetDynamicsFragmentWeakReference.get();
            if (mMeetDynamicsFragment != null) {
                mMeetDynamicsFragment.handleMessage(message);
            }
        }
    }

    private class DynamicsAddBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Slog.d(TAG, "==========DYNAMICS_ADD_BROADCAST");
            updateData();
        }
    }

}
