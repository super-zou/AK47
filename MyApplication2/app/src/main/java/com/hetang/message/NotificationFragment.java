package com.hetang.message;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.hetang.BuildConfig;
import com.hetang.common.ReminderManager;
import com.hetang.util.BaseFragment;
import com.hetang.util.HttpUtil;
import com.hetang.common.MyApplication;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.hetang.R;
import com.hetang.adapter.NotificationListAdapter;
import com.hetang.util.FontManager;
import com.hetang.util.SharedPreferencesUtils;
import com.hetang.util.Slog;
import com.hetang.util.UserProfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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
import static com.hetang.adapter.NotificationListAdapter.UNREAD;
import static com.hetang.util.ParseUtils.APPLY_JOIN_SINGLE_GROUP_NF;
import static com.hetang.util.ParseUtils.APPROVE_IMPRESSION_ACTION;
import static com.hetang.util.ParseUtils.APPROVE_PERSONALITY_ACTION;
import static com.hetang.util.ParseUtils.COMMENT_PRAISED_NF;
import static com.hetang.util.ParseUtils.EVALUATE_ACTION;
import static com.hetang.util.ParseUtils.INVITE_SINGLE_GROUP_MEMBER_ACTION;
import static com.hetang.util.ParseUtils.JOIN_CHEERING_GROUP_ACTION;
import static com.hetang.util.ParseUtils.JOIN_SINGLE_GROUP_ACTION;
import static com.hetang.util.ParseUtils.PRAISE_DYNAMIC_ACTION;
import static com.hetang.util.ParseUtils.PRAISE_MEET_COMMENT_NF;
import static com.hetang.util.ParseUtils.REFEREE_ACTION;

import static com.hetang.util.ParseUtils.REFEREE_INVITE_NF;
import static com.hetang.util.ParseUtils.REPLY_PRAISED_NF;
import static com.hetang.util.Utility.drawable2File;
import static com.hetang.util.Utility.drawableToBitmap;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class NotificationFragment extends BaseFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "NotificationFragment";
    
    private static final int PAGE_SIZE = 6;
    private static final int LOAD_DONE = 0;
    private static final int UPDATE_DONE = 1;
    private static final int LOAD_COMPLETE_END = 2;
    private static final int LOAD_NOTHING_DONE = 3;
    private static final String GET_NOTICE_URL = HttpUtil.DOMAIN + "?q=notice/load";
    private static final String GET_UPDATE_NOTICE_URL = HttpUtil.DOMAIN + "?q=notice/get_update";
    private static final String UPDATE_NOTICE_URL = HttpUtil.DOMAIN + "?q=notice/update";
    public static final String DELETE_NOTICE_URL = HttpUtil.DOMAIN + "?q=notice/delete";
    public static final String NOTICE_PROCESS_URL = HttpUtil.DOMAIN + "?q=notice/process";
    public static final String GET_UNPROCESS_NOTICE_URL = HttpUtil.DOMAIN + "?q=notice/unprocess";

    private List<Notification> notificationList = new ArrayList<>();
    private XRecyclerView xRecyclerView;
    private int mTempSize;
    private NotificationListAdapter notificationListAdapter;
private Context mContext;
    private MyHandler handler;
    Typeface font;
    int i = 0;
    private Runnable runnable;
    int page = 0;

    public static class Notification {
        public int nid;
        public int uid;
        public int tid;
        public UserProfile trigger;
        public String action = "";
        public String content = "";
        public int isNew = 0;
        public int type;
        public long timeStamp;
        public int id;
        public int processed;//default 0 unprocessed, 1 processed, -1 ignored
    }
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.notification, container, false);
        initView(convertView);
        return convertView;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.notification;
    }
    
    //@Override
    protected void initView(View view) {
        Slog.d(TAG, "================================initView");
        handler = new MyHandler(this);

        runnable = new Runnable() {
            @Override
            public void run() {
                i++;
                //Slog.d(TAG, "-------------------------------->run: "+i);
                updateData();
                //要执行的事件
                handler.postDelayed(this, 60*1000);
            }
        };
        
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 60*1000);

        mContext = MyApplication.getContext();
        notificationListAdapter = new NotificationListAdapter(getContext());

        xRecyclerView = view.findViewById(R.id.notification_list);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(linearLayoutManager);

        xRecyclerView.setRefreshProgressStyle(BallSpinFadeLoader);
        xRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallRotate);
        
        xRecyclerView.getDefaultRefreshHeaderView().setRefreshTimeVisible(false);
        xRecyclerView.setPullRefreshEnabled(false);
        xRecyclerView.getDefaultFootView().setLoadingHint(getString(R.string.loading_pull_up_tip));
        xRecyclerView.getDefaultFootView().setNoMoreHint(getString(R.string.no_more));
        int itemLimit = 1;
        // When the item number of the screen number is list.size-2,we call the onLoadMore
        xRecyclerView.setLimitNumberToCallLoadMore(itemLimit);
        xRecyclerView.setRefreshProgressStyle(ProgressStyle.BallBeat);
        xRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);

        xRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    notificationListAdapter.setScrolling(false);
                    notificationListAdapter.notifyDataSetChanged();
                } else {
                    notificationListAdapter.setScrolling(true);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        xRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                updateData();
            }

            @Override
            public void onLoadMore() {
                loadData();
            }
        });
        
        xRecyclerView.setAdapter(notificationListAdapter);

        font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(view.findViewById(R.id.notification_list), font);

        //loadData();

    }

    //@Override
    protected void loadData() {
        Slog.d(TAG, "-------------------------------->loadData");
        page = notificationList.size() / PAGE_SIZE;
        RequestBody requestBody = new FormBody.Builder()
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();
                
                HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_NOTICE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========load response text : " + responseText);
                    if (responseText != null) {
                        int itemCount = processResponseText(responseText);
                        if(itemCount > 0){
                            if (itemCount < PAGE_SIZE){
                                handler.sendEmptyMessage(LOAD_COMPLETE_END);
                            }else {
                                handler.sendEmptyMessage(LOAD_DONE);
                            }
                        }else {
                            handler.sendEmptyMessage(LOAD_NOTHING_DONE);
                        }
                        
                        }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) { }
        });
    }

    private void updateData() {
        Slog.d(TAG, "--------------------------------->updateData");
        String last = SharedPreferencesUtils.getNotificationLast(getContext());
        
        RequestBody requestBody = new FormBody.Builder()
                .add("last", last)
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(0))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_UPDATE_NOTICE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
            
            if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========updateData response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        int itemCount = processUpdateResponseText(responseText);
                        if (itemCount > 0){
                            Message message = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putInt("updateCount", itemCount);
                            message.setData(bundle);
                            message.what = UPDATE_DONE;
                            handler.sendMessage(message);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
    private int processResponseText(String responseText){
        JSONArray noticeArray;
        JSONObject responseObject;
        try {
            responseObject = new JSONObject(responseText);
            noticeArray = responseObject.optJSONArray("notice");

            if (page == 0){
                int last = responseObject.optInt("last");
                Slog.d(TAG, "---------------------->last: "+last);
                SharedPreferencesUtils.setNotificationLast(getContext(), String.valueOf(last));
            }
            
            if (isDebug) Slog.d(TAG, "------------------------------>noticeArray: "+noticeArray);
            if (noticeArray != null && noticeArray.length() > 0){
                for (int i=0; i<noticeArray.length(); i++){
                    JSONObject noticeObject = noticeArray.getJSONObject(i);
                    Notification notification = parseNotification(noticeObject);
                    notificationList.add(notification);
                }

                return noticeArray.length();
            }
        }catch (JSONException e){
            e.printStackTrace();
        }

        return 0;
    }
    
    private int processUpdateResponseText(String responseText){
        JSONArray noticeArray;
        JSONObject responseObject;
        List<Notification> updateList = new ArrayList<>();
        try {
            responseObject = new JSONObject(responseText);
            noticeArray = responseObject.optJSONArray("notice");
            int last = responseObject.optInt("last");
            SharedPreferencesUtils.setNotificationLast(getContext(), String.valueOf(last));
            if (isDebug) Slog.d(TAG, "------------------------------>noticeArray: "+noticeArray);
            JSONObject noticeObject;
            if (noticeArray != null && noticeArray.length() > 0){
                for (int i=0; i<noticeArray.length(); i++){
                    noticeObject = noticeArray.getJSONObject(i);
                    Notification notification = parseNotification(noticeObject);
                    updateList.add(notification);
                }
                notificationList.addAll(0, updateList);
                return updateList.size();
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return 0;
    }

    private Notification parseNotification(JSONObject noticeObject){
        Notification notification = new Notification();
        notification.nid = noticeObject.optInt("nid");
        notification.uid = noticeObject.optInt("uid");
        notification.tid = noticeObject.optInt("tid");
        notification.trigger = new UserProfile();
        
        notification.trigger.setName(noticeObject.optString("name"));
        notification.trigger.setAvatar(noticeObject.optString("avatar"));
        notification.trigger.setSex(noticeObject.optInt("sex"));
        notification.trigger.setSituation(noticeObject.optInt("situation"));
        if (noticeObject.optInt("situation") == 0){
            notification.trigger.setDegree(noticeObject.optString("degree"));
            notification.trigger.setUniversity(noticeObject.optString("university"));
        }else {
            notification.trigger.setPosition(noticeObject.optString("position"));
            notification.trigger.setIndustry(noticeObject.optString("industry"));
        }
        notification.trigger.setCid(noticeObject.optInt("cid"));

        notification.isNew = noticeObject.optInt("is_new");
        notification.action = noticeObject.optString("action");
        notification.type = noticeObject.optInt("type");
        
        String content = noticeObject.optString("content");
        if ( content != null && !content.equals("null")){
            notification.content = content;
        }else {
            switch (notification.type){
                case PRAISE_MEET_COMMENT_NF:
                case PRAISE_DYNAMIC_ACTION:
                case COMMENT_PRAISED_NF:
                case REPLY_PRAISED_NF:
                case INVITE_SINGLE_GROUP_MEMBER_ACTION:
                case JOIN_SINGLE_GROUP_ACTION:
                case APPLY_JOIN_SINGLE_GROUP_NF:
                    notification.content = mContext.getResources().getString(R.string.view_details);
                    break;
            }
        }
        
        notification.timeStamp = noticeObject.optInt("timestamp");
        notification.id = noticeObject.optInt("id");
        notification.processed = noticeObject.optInt("processed");

        return notification;
    }

    public void handleMessage(Message message) {
        switch (message.what){
            case LOAD_DONE:
                notificationListAdapter.setData(notificationList);
                notificationListAdapter.notifyDataSetChanged();
                xRecyclerView.refreshComplete();
                //xRecyclerView.loadMoreComplete();
                break;
                
                case LOAD_COMPLETE_END:
                notificationListAdapter.setData(notificationList);
                notificationListAdapter.notifyDataSetChanged();
                xRecyclerView.refreshComplete();
                xRecyclerView.loadMoreComplete();
                xRecyclerView.setNoMore(true);
                break;
            case LOAD_NOTHING_DONE:
                //xRecyclerView.refreshComplete();
                xRecyclerView.setNoMore(true);
                xRecyclerView.loadMoreComplete();
                break;
                
                case UPDATE_DONE:
                Bundle bundle = message.getData();
                int updateCount = 0;
                if (bundle != null){
                    updateCount = bundle.getInt("updateCount");
                }
                notificationListAdapter.setData(notificationList);
                notificationListAdapter.notifyItemRangeInserted(0, updateCount);
                notificationListAdapter.notifyDataSetChanged();
                xRecyclerView.refreshComplete();

                ReminderManager.getInstance().updateNotificationUnreadNum(updateCount);

                break;
            default:
                break;
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    static class MyHandler extends Handler {
        WeakReference<NotificationFragment> notificationFragmentWeakReference;

        MyHandler(NotificationFragment notificationFragment) {
            notificationFragmentWeakReference = new WeakReference<>(notificationFragment);
        }
        
        @Override
        public void handleMessage(Message message) {
            NotificationFragment notificationFragment = notificationFragmentWeakReference.get();
            if (notificationFragment != null) {
                notificationFragment.handleMessage(message);
            }
        }
    }
}
                
                

