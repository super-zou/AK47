package com.tongmenhui.launchak47.meet;

//import android.app.Fragment;

import android.content.Intent;
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
import android.widget.TextView;
import android.text.TextUtils;
import android.widget.LinearLayout;
import com.tongmenhui.launchak47.util.UserProfile;
import org.json.JSONException;
import java.lang.ref.WeakReference;

import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.adapter.MeetRecommendListAdapter;
import com.tongmenhui.launchak47.util.BaseFragment;
import com.tongmenhui.launchak47.util.HttpUtil;
import com.tongmenhui.launchak47.util.ParseUtils;
import com.tongmenhui.launchak47.util.SharedPreferencesUtils;
import com.tongmenhui.launchak47.util.Slog;

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
 * Created by super-zou on 17-9-11.
 */

public class MeetRecommendFragment extends BaseFragment {
    private static final boolean debug = false;
    private static final String TAG = "MeetRecommendFragment";
    //+Begin add by xuchunping for use XRecyclerView support loadmore
    //private RecyclerView recyclerView;
    private static final int PAGE_SIZE = 6;//page size
    private static final int DONE = 1;
    private static final int UPDATE = 2;
    private static final int GET_USER_PROFILE_DONE = 3;
    private static final String GET_RECOMMEND_URL = HttpUtil.DOMAIN + "?q=meet/recommend";
    private static final String GET_USER_PROFILE_URL = HttpUtil.DOMAIN + "?q=account_manager/get_user_profile";
    private static String responseText;
    JSONObject recommend_response;
    JSONArray recommendation;
    private View viewContent;
    private int mType = 0;
    private String mTitle;
    private List<MeetMemberInfo> meetList = new ArrayList<>();
    private UserProfile userProfile;
    private XRecyclerView recyclerView;
    private int mTempSize;
    //-End add by xuchunping for use XRecyclerView support loadmore
    private MeetRecommendListAdapter meetRecommendListAdapter;
    // private String realname;
    private int uid;
    private boolean loaded = false;
    private Handler handler;
    private boolean avatarSet = false;
    private LinearLayout addMeetInfo;

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
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (debug) Slog.d(TAG, "=================onCreateView===================");
       
        meetRecommendListAdapter = new MeetRecommendListAdapter(getContext());
        viewContent = inflater.inflate(R.layout.meet_recommend, container, false);
        addMeetInfo = viewContent.findViewById(R.id.meet_info_add);
        addMeetInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FillMeetInfoActivity.class);
                intent.putExtra("avatarSet", avatarSet);
                startActivity(intent);
            }
        });
        recyclerView = viewContent.findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    meetRecommendListAdapter.setScrolling(false);
                    meetRecommendListAdapter.notifyDataSetChanged();
                } else {
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

        recyclerView.getDefaultRefreshHeaderView().setRefreshTimeVisible(true);

        recyclerView.getDefaultFootView().setLoadingHint(getString(R.string.loading_pull_up_tip));
        recyclerView.getDefaultFootView().setNoMoreHint(getString(R.string.loading_no_more));
        //recyclerView.setArrowImageView(R.drawable.iconfont_downgrey);//TODO set pull down icon
        final int itemLimit = 5;

        // When the item number of the screen number is list.size-2,we call the onLoadMore
//        recyclerView.setLimitNumberToCallLoadMore(2);
        recyclerView.setRefreshProgressStyle(ProgressStyle.BallBeat);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);

        recyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                updateData();
            }

            @Override
            public void onLoadMore() {
                getRecommendContent();
            }
        });
        //-End added by xuchunping

        recyclerView.setAdapter(meetRecommendListAdapter);
        getRecommendContent();
        return viewContent;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (debug) Slog.d(TAG, "=================onViewCreated===================");
        // initConentView();
    }

    public void getRecommendContent() {
        if (debug) Slog.d(TAG, "===============initConentView==============");
        
        handler = new MyHandler(this);

        getUserProfile();

        int page = meetList.size() / PAGE_SIZE;
        RequestBody requestBody = new FormBody.Builder()
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();
        Log.d(TAG, "initContentView requestBody:" + requestBody.toString() + " page:" + page);
        HttpUtil.sendOkHttpRequest(getContext(), GET_RECOMMEND_URL, requestBody, new Callback() {
            int check_login_user = 0;
            String user_name;

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                //Slog.d(TAG, "response : "+responseText);
                Log.d(TAG, "onResponse responseText:" + responseText);
                getResponseText(responseText);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure e:" + e);
            }
        });
    }
    
    private void getUserProfile(){
        RequestBody requestBody = new FormBody.Builder().build();
        HttpUtil.sendOkHttpRequest(getContext(), GET_USER_PROFILE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    Slog.d(TAG, "==========get archive response text : " + responseText);
                    if (responseText != null) {
                        if (!TextUtils.isEmpty(responseText)) {
                            try {
                                JSONObject jsonObject = new JSONObject(responseText).optJSONObject("user");
                                userProfile = ParseUtils.getUserProfileFromJSONObject(jsonObject);
                                Slog.d(TAG, "==============user profile: "+userProfile);
                                if(userProfile != null){
                                    handler.sendEmptyMessage(GET_USER_PROFILE_DONE);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            
                                                    }
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }
    


    public void getResponseText(String responseText) {

        if (debug) Slog.d(TAG, "====================getResponseText: " + responseText);
        //+Begin added by xuchunping
        List<MeetMemberInfo> tempList = ParseUtils.getRecommendMeetList(responseText);
        mTempSize = 0;
        if (null != tempList) {
            mTempSize = tempList.size();
            meetList.addAll(tempList);
            Log.d(TAG, "getResponseText list.size:" + tempList.size());
        }
        handler.sendEmptyMessage(DONE);
        //-End added by xuchunping
    }

    private void updateData() {
        String last = SharedPreferencesUtils.getRecommendLast(getContext());
        if (debug) Slog.d(TAG, "=======last:" + last);
        Log.d(TAG, "=======last:" + last);

        int page = 0;
        RequestBody requestBody = new FormBody.Builder().add("last", last)
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(0))
                .build();

        Log.d(TAG, "updateData requestBody:" + requestBody.toString());
        HttpUtil.sendOkHttpRequest(getContext(), GET_RECOMMEND_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null) {
                        List<MeetMemberInfo> tempList = ParseUtils.getRecommendMeetList(responseText);
                        if (null != tempList && tempList.size() != 0) {
                            mTempSize = tempList.size();
                            meetList.clear();
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
    
    static class MyHandler extends Handler {
        WeakReference<MeetRecommendFragment> meetRecommendFragmentWeakReference;

        MyHandler(MeetRecommendFragment meetRecommendFragment) {
            meetRecommendFragmentWeakReference = new WeakReference<MeetRecommendFragment>(meetRecommendFragment);
        }

        @Override
        public void handleMessage(Message message) {
            MeetRecommendFragment meetRecommendFragment = meetRecommendFragmentWeakReference.get();
            if (meetRecommendFragment != null) {
                meetRecommendFragment.handleMessage(message);
            }
        }
    }
    
    public void handleMessage(Message message) {
        switch (message.what){
            case DONE:
                meetRecommendListAdapter.setData(meetList);
                meetRecommendListAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();

                if (mTempSize < PAGE_SIZE) {
                    //loading finished
                    recyclerView.setNoMore(true);
                    recyclerView.setLoadingMoreEnabled(false);
                }
                break;
                case UPDATE:
                //save last update timemills
                SharedPreferencesUtils.setRecommendLast(getContext(), String.valueOf(System.currentTimeMillis() / 1000));

                meetRecommendListAdapter.setData(meetList);
                meetRecommendListAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();
                break;
                case GET_USER_PROFILE_DONE:
                Slog.d(TAG, "==============GET_USER_PROFILE_DONE cid: "+userProfile.getCid());
                if(userProfile.getCid() == 0){
                    if(addMeetInfo.getVisibility() == View.GONE){
                        addMeetInfo.setVisibility(View.VISIBLE);
                    }
                }else {
                    if(addMeetInfo.getVisibility() == View.VISIBLE){
                        addMeetInfo.setVisibility(View.GONE);
                    }
                }

                if(!TextUtils.isEmpty(userProfile.getAvatar())){
                    avatarSet = true;
                }
                break;
                default:
                    break;
        }
    }

}
