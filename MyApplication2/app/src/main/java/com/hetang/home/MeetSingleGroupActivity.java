package com.hetang.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hetang.adapter.MeetSingleGroupSummaryAdapter;
import com.hetang.util.CreateSingleGroupDialogFragment;
import com.hetang.util.HttpUtil;
import com.hetang.common.MyApplication;
import com.hetang.util.MyLinearLayoutManager;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.hetang.R;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.common.HandlerTemp;
import com.hetang.meet.MeetSingleGroupFragment;
import com.hetang.meet.SingleGroupDetailsActivity;
import com.hetang.util.FontManager;
import com.hetang.util.SharedPreferencesUtils;
import com.hetang.util.Slog;

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
import static com.hetang.meet.MeetSingleGroupFragment.getSingleGroup;

public class MeetSingleGroupActivity extends BaseAppCompatActivity {
    private static final boolean isDebug = true;
    private static final String TAG = "MeetSingleGroupActivity";
    final int itemLimit = 3;
    private int mLoadSize = 0;
    private int mUpdateSize = 0;
    private static final int PAGE_SIZE = 5;
    private Handler handler;
    private static final String SINGLE_GROUP_GET_ALL = HttpUtil.DOMAIN + "?q=single_group/get_all";
    private static final String SINGLE_GROUP_UPDATE = HttpUtil.DOMAIN + "?q=single_group/update";

    private static final int GET_ALL_DONE = 1;
    private static final int UPDATE_ALL = 2;
    private static final int GET_ALL_END = 3;
    private static final int NO_UPDATE = 4;
    
    public static final String GROUP_ADD_BROADCAST = "com.hetang.action.GROUP_ADD";
    private SingleGroupReceiver mReceiver = new SingleGroupReceiver();

    private MeetSingleGroupSummaryAdapter meetSingleGroupSummaryAdapter = new MeetSingleGroupSummaryAdapter(MyApplication.getContext());
    private XRecyclerView recyclerView;
    private List<MeetSingleGroupFragment.SingleGroup> mSingleGroupList = new ArrayList<>();
    private List<MeetSingleGroupFragment.SingleGroup> mSingleGroupUpdateList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meet_single_group_summary);
        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.custom_actionbar), font);
        initView();
    }
    
    private void initView(){
        LinearLayout customActionBar = findViewById(R.id.custom_actionbar);

        if (customActionBar.getVisibility() == View.GONE){
            customActionBar.setVisibility(View.VISIBLE);
        }

        TextView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        
        handler = new MyHandler(this);
        recyclerView = findViewById(R.id.single_group_summary_list);
        MyLinearLayoutManager linearLayoutManager = new MyLinearLayoutManager(MyApplication.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setPadding(0, 0, 0, 188);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setRefreshProgressStyle(BallSpinFadeLoader);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallRotate);

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
        
        meetSingleGroupSummaryAdapter.setItemClickListener(new MeetSingleGroupSummaryAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Slog.d(TAG, "==========click : " + position);
                Intent intent = new Intent(MyApplication.getContext(), SingleGroupDetailsActivity.class);
                intent.putExtra("gid", mSingleGroupList.get(position).gid);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(meetSingleGroupSummaryAdapter);
        
        FloatingActionButton floatingActionButton = findViewById(R.id.create_single_group);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateSingleGroupDialogFragment createSingleGroupDialogFragment = new CreateSingleGroupDialogFragment();
                //createSingleGroupDialogFragment.setTargetFragment(MeetSingleGroupFragment.this, REQUEST_CODE);
                createSingleGroupDialogFragment.show(getSupportFragmentManager(), "CreateSingleGroupDialogFragment");
            }
        });

        loadData();

        registerLoginBroadcast();
    }
    
    protected void loadData() {

        int page = mSingleGroupList.size() / PAGE_SIZE;
        RequestBody requestBody = new FormBody.Builder()
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();

        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), SINGLE_GROUP_GET_ALL, requestBody, new Callback() {
        @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if(isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject SingleGroupResponse = null;
                        try {
                            SingleGroupResponse = new JSONObject(responseText);
                            if(SingleGroupResponse != null){
                                mLoadSize = processResponse(SingleGroupResponse);
                            }
                            }catch (JSONException e){
                            e.printStackTrace();
                        }


                        if(mLoadSize > 0){
                            handler.sendEmptyMessage(GET_ALL_DONE);
                        }
                    }
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private int processResponse(JSONObject SingleGroupResponse){
    
    JSONArray SingleGroupArray = null;
        if(SingleGroupResponse != null){
            SingleGroupArray = SingleGroupResponse.optJSONArray("single_group");
        }

        if(SingleGroupArray != null){
            if(SingleGroupArray.length() > 0){
                for (int i=0; i<SingleGroupArray.length(); i++){
                    JSONObject group = SingleGroupArray.optJSONObject(i);
                    if (group != null){
                        MeetSingleGroupFragment.SingleGroup singleGroup = getSingleGroup(group);
                        mSingleGroupList.add(singleGroup);
                    }
                }
            }else {
                handler.sendEmptyMessage(GET_ALL_END);
            }
        }

        return SingleGroupArray != null ?  SingleGroupArray.length():0;
    }
    
    private int processUpdateResponse(JSONObject SingleGroupResponse){

        JSONArray SingleGroupArray = null;
        if(SingleGroupResponse != null){
            SingleGroupArray = SingleGroupResponse.optJSONArray("single_group");
        }

        if(SingleGroupArray != null){
            if(SingleGroupArray.length() > 0){
                mSingleGroupUpdateList.clear();
                for (int i=0; i<SingleGroupArray.length(); i++){
                    JSONObject group = SingleGroupArray.optJSONObject(i);
                    if (group != null){
                        MeetSingleGroupFragment.SingleGroup singleGroup = getSingleGroup(group);
                        mSingleGroupUpdateList.add(singleGroup);
                    }
                }

                mSingleGroupList.addAll(0, mSingleGroupUpdateList);
            }else {
                handler.sendEmptyMessage(GET_ALL_END);
            }
        }

        return SingleGroupArray != null ?  SingleGroupArray.length():0;
    }

    public void updateData(){
        String last = SharedPreferencesUtils.getSingleGroupLast(MyApplication.getContext());
        RequestBody requestBody = new FormBody.Builder()
                .add("last", last)
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(0))
                .build();
                
                HttpUtil.sendOkHttpRequest(MyApplication.getContext(), SINGLE_GROUP_UPDATE, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if(isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject singleGroupResponse = null;
                        
                        try {
                            singleGroupResponse = new JSONObject(responseText);
                            if(singleGroupResponse != null){
                                int current = singleGroupResponse.optInt("current");
                                Slog.d(TAG, "----------------->current: "+current);
                                SharedPreferencesUtils.setSingleGroupLast(MyApplication.getContext(), String.valueOf(current));

                                mUpdateSize = processUpdateResponse(singleGroupResponse);
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                        
                        if(mUpdateSize > 0){
                            handler.sendEmptyMessage(UPDATE_ALL);
                        }else {
                            handler.sendEmptyMessage(NO_UPDATE);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
    private void registerLoginBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GROUP_ADD_BROADCAST);
        LocalBroadcastManager.getInstance(MyApplication.getContext()).registerReceiver(mReceiver, intentFilter);
    }

    //unregister local broadcast
    private void unRegisterLoginBroadcast() {
        LocalBroadcastManager.getInstance(MyApplication.getContext()).unregisterReceiver(mReceiver);
    }
    
    private class SingleGroupReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case GROUP_ADD_BROADCAST:
                    Slog.d(TAG, "==========GROUP_ADD_BROADCAST");
                    updateData();
                    break;
            }

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterLoginBroadcast();
    }
    
    public void handleMessage(Message message) {
        switch (message.what) {
            case GET_ALL_DONE:
                if (mLoadSize < PAGE_SIZE) {
                    //loading finished
                    recyclerView.setNoMore(true);
                    recyclerView.setLoadingMoreEnabled(false);
                }

                meetSingleGroupSummaryAdapter.setData(mSingleGroupList, recyclerView.getWidth());
                meetSingleGroupSummaryAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();

                break;
                
                case GET_ALL_END:
                Slog.d(TAG, "=============GET_ALL_END");
                recyclerView.setNoMore(true);
                recyclerView.setLoadingMoreEnabled(false);
                meetSingleGroupSummaryAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();
                break;
            case UPDATE_ALL:
                //meetDynamicsListAdapter.setScrolling(false);
                meetSingleGroupSummaryAdapter.setData(mSingleGroupList, recyclerView.getWidth());
                meetSingleGroupSummaryAdapter.notifyItemRangeInserted(0, mUpdateSize);
                meetSingleGroupSummaryAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();

                mUpdateSize = 0;

                break;
                
                case NO_UPDATE:
                recyclerView.refreshComplete();
                mUpdateSize = 0;
                break;
            default:
                break;
        }
    }

    static class MyHandler extends HandlerTemp<MeetSingleGroupActivity> {
        public MyHandler(MeetSingleGroupActivity cls){
            super(cls);
        }
        @Override
        public void handleMessage(Message message) {
            MeetSingleGroupActivity meetSingleGroupActivity = ref.get();
            if (meetSingleGroupActivity != null) {
                meetSingleGroupActivity.handleMessage(message);
            }
        }
    }
}
