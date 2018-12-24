package com.tongmenhui.launchak47.meet;

import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
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
import com.tongmenhui.launchak47.adapter.MeetSingleGroupSummaryAdapter;
import com.tongmenhui.launchak47.util.BaseFragment;
import com.tongmenhui.launchak47.util.CommentDialogFragmentInterface;
import com.tongmenhui.launchak47.util.HttpUtil;
import com.tongmenhui.launchak47.util.MyApplication;
import com.tongmenhui.launchak47.util.ParseUtils;
import com.tongmenhui.launchak47.util.SharedPreferencesUtils;
import com.tongmenhui.launchak47.util.Slog;
import com.tongmenhui.launchak47.util.Utility;

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

public class MeetSingleGroupFragment extends BaseFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "MeetSingleGroupFragment";
    final int itemLimit = 5;
    private int mLoadSize = 0;
    private static final int PAGE_SIZE = 6;
    private Handler handler;
    private static final String SINGLE_GROUP_CREATE = HttpUtil.DOMAIN + "?q=single_group/create";
    private static final String SINGLE_GROUP_ADD = HttpUtil.DOMAIN + "?q=single_group/add";
    private static final String SINGLE_GROUP_APPLY = HttpUtil.DOMAIN + "?q=single_group/apply";
    private static final String SINGLE_GROUP_APPROVE = HttpUtil.DOMAIN + "?q=single_group/approve";
    private static final String SINGLE_GROUP_GET_BY_UID = HttpUtil.DOMAIN + "?q=single_group/get_by_uid";
    private static final String SINGLE_GROUP_GET_BY_ORG = HttpUtil.DOMAIN + "?q=single_group/get_by_org";
    private static final String SINGLE_GROUP_GET_MY = HttpUtil.DOMAIN + "?q=single_group/get_my";
    private static final String SINGLE_GROUP_GET_ALL = HttpUtil.DOMAIN + "?q=single_group/get_all";
    private static final String SINGLE_GROUP_UPDATE = HttpUtil.DOMAIN + "?q=single_group/update";

    private static final int GET_ALL_DONE = 1;
    private static final int UPDATE_ALL = 2;

    private MeetSingleGroupSummaryAdapter meetSingleGroupSummaryAdapter;
    private XRecyclerView  recyclerView;
    private List<SingleGroup> mSingleGroupList = new ArrayList<>();
    
        @Override
    protected void initView(View convertView) {
        recyclerView = convertView.findViewById(R.id.single_group_summary_list);
        meetSingleGroupSummaryAdapter = new MeetSingleGroupSummaryAdapter(MyApplication.getContext());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        
        recyclerView.getDefaultRefreshHeaderView().setRefreshTimeVisible(true);
        recyclerView.getDefaultFootView().setLoadingHint(getString(R.string.loading_pull_up_tip));
        recyclerView.getDefaultFootView().setNoMoreHint(getString(R.string.loading_no_more));

        // When the item number of the screen number is list.size-2,we call the onLoadMore
        recyclerView.setLimitNumberToCallLoadMore(itemLimit);
        recyclerView.setRefreshProgressStyle(ProgressStyle.BallBeat);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);
        
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    meetSingleGroupSummaryAdapter.setScrolling(false);
                    meetSingleGroupSummaryAdapter.notifyDataSetChanged();
                } else {
                    meetSingleGroupSummaryAdapter.setScrolling(true);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        
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

        recyclerView.setAdapter(meetSingleGroupSummaryAdapter);

    }
    
     @Override
    protected void loadData() {
        handler = new MeetSingleGroupFragment.MyHandler(this);

        int page = mSingleGroupList.size() / PAGE_SIZE;
        RequestBody requestBody = new FormBody.Builder()
                                               .add("step", String.valueOf(PAGE_SIZE))
                                               .add("page", String.valueOf(page))
                                               .build();
        
        HttpUtil.sendOkHttpRequest(getContext(), SINGLE_GROUP_GET_ALL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if(isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        mLoadSize = processResponse(responseText);
                        handler.sendEmptyMessage(GET_ALL_DONE);
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
    private int processResponse(String response){
        JSONObject SingleGroupResponse = null;

        try {
            SingleGroupResponse = new JSONObject(response);
        }catch (JSONException e){
            e.printStackTrace();
        }
        
        JSONArray SingleGroupArray = SingleGroupResponse.optJSONArray("single_group");
        if(SingleGroupArray != null && SingleGroupArray.length() > 0){
            for (int i=0; i<SingleGroupArray.length(); i++){
                JSONObject group = SingleGroupArray.optJSONObject(i);
                SingleGroup singleGroup = new SingleGroup();
                singleGroup.gid = group.optInt("gid");
                singleGroup.groupName = group.optString("group_name");
                singleGroup.groupProfile = group.optString("group_profile");
                singleGroup.groupMarkUri = group.optString("group_mark_uri");
                singleGroup.org = group.optString("group_org");
                singleGroup.created = Utility.timeStampToDay(group.optInt("created"));
                JSONArray memberArray = group.optJSONArray("member");
                if(memberArray.length() > 3){
                    singleGroup.memberCountRemain = memberArray.length() - 3;
                }
                
                int count = 0;
                if(memberArray.length() >= 3){
                    count = 3;
                }else {
                    count = memberArray.length();
                }
                singleGroup.headUrlList = new ArrayList<>();
                for (int n=0; n<count; n++){
                    singleGroup.headUrlList.add(memberArray.optJSONObject(n).optString("picture_uri"));
                }

                singleGroup.leader = new MeetMemberInfo();
                ParseUtils.setBaseProfile(singleGroup.leader, group.optJSONObject("leader"));

                mSingleGroupList.add(singleGroup);
            }
        }

        return SingleGroupArray!=null? SingleGroupArray.length():0;
    }
    
        public void updateData(){

    }

    @Override
    protected int getLayoutId() {
        int layoutId = R.layout.meet_single_group_summary;
        return layoutId;
    }
    
        public void handleMessage(Message message) {
        switch (message.what) {
            case GET_ALL_DONE:
                meetSingleGroupSummaryAdapter.setData(mSingleGroupList);
                meetSingleGroupSummaryAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();

                if (mLoadSize < PAGE_SIZE) {
                    //loading finished
                    recyclerView.setNoMore(true);
                    recyclerView.setLoadingMoreEnabled(false);
                }

                break;
            default:
                break;
        }
    }
    
    public static class SingleGroup {
        public int gid;
        public String groupName;
        public String groupProfile;
        public String org;
        public String groupMarkUri;
        public int memberCountRemain = 0;
        public String created;
        public MeetMemberInfo leader;
        public List<String> headUrlList;
    }
    
    static class MyHandler extends Handler {
        WeakReference<MeetSingleGroupFragment> meetSingleGroupFragmentWeakReference;

        MyHandler(MeetSingleGroupFragment meetSingleGroupFragment) {
            meetSingleGroupFragmentWeakReference = new WeakReference<MeetSingleGroupFragment>(meetSingleGroupFragment);
        }

        @Override
        public void handleMessage(Message message) {
            MeetSingleGroupFragment meetSingleGroupFragment = meetSingleGroupFragmentWeakReference.get();
            if (meetSingleGroupFragment != null) {
                meetSingleGroupFragment.handleMessage(message);
            }
        }
    }
    
 }