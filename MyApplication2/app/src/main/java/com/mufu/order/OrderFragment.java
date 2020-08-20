package com.mufu.order;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.mufu.R;
import com.mufu.adapter.OrderFragmentAdapter;
import com.mufu.common.MyApplication;
import com.mufu.common.ReminderManager;
import com.mufu.message.NotificationFragment;
import com.mufu.util.HttpUtil;
import com.mufu.util.SharedPreferencesUtils;
import com.mufu.util.Slog;
import com.tencent.qcloud.tim.uikit.modules.conversation.ConversationManagerKit;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.mufu.main.MeetArchiveFragment.GET_EXPERIENCE_STATISTICS_URL;
import static com.mufu.main.MeetArchiveFragment.GET_GUIDE_STATISTICS_URL;
import static com.mufu.main.MeetArchiveFragment.LOAD_MY_EXPERIENCES_DONE;
import static com.mufu.main.MeetArchiveFragment.LOAD_MY_GUIDE_COUNT_DONE;
import static com.mufu.verify.VerifyActivity.GET_ALL_REQUEST_COUNT_URL;

public class OrderFragment extends Fragment implements ReminderManager.UnreadNumChangedCallback, ConversationManagerKit.MessageUnreadWatcher {
    public static final int HAVE_UNREAD_MESSAGE = 0;
    public static final int HAVE_NO_UNREAD_MESSAGE = 1;
    public static final int HAVE_REQUEST_VERIFY_MESSAGE = 3;
    private static final String TAG = "OrderFragment";
    private static final boolean isDebug = false;
    int role = -1;
    int requestVerifyCount = 0;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private OrderFragmentAdapter mFragmentAdapter;
    private TabLayout.Tab myTab;
    private TabLayout.Tab tobeDoneTab;
    private TabLayout.Tab finishedTab;
    private TextView unReadNotification;
    private TextView unReadConversation;
    private TextView unReadVerifyRequest;
    private int myExperienceSize = 0;
    private int myGuideCount = 0;
    private int mUid = 0;
    
    private View view;
    private MyHandler handler;
    private ArrayList<String> mOrderTitleList = new ArrayList<String>() {
        {
            add("我的订单");
            add("等待开展");
            add("已完成");
        }
    };

    public static void getUnreadNotification(final Handler handler) {
        RequestBody requestBody = new FormBody.Builder().build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), NotificationFragment.GET_UNPROCESS_NOTICE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();

                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        try {
                            int unReadCount = new JSONObject(responseText).optInt("unread");

                            Message message = new Message();
                            if (unReadCount > 0) {

                                Bundle bundle = new Bundle();
                                bundle.putInt("count", unReadCount);
                                message.setData(bundle);
                                message.what = HAVE_UNREAD_MESSAGE;
                                } else {
                                message.what = HAVE_NO_UNREAD_MESSAGE;
                                //handler.sendEmptyMessage(HAVE_NO_UNREAD_MESSAGE);
                            }
                            handler.sendMessage(message);

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
    
    private void getRequestVerifyNumber(){
        RequestBody requestBody = new FormBody.Builder().build();

        String url = GET_ALL_REQUEST_COUNT_URL;

        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), url, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        try {
                            JSONObject responseObject = new JSONObject(responseText);
                            int userRequestCount = responseObject.optInt("user_request_count");
                            if (userRequestCount > 0){
                                requestVerifyCount += userRequestCount;
                            }
                            
                            int talentRequestCount = responseObject.optInt("talent_request_count");
                            if (talentRequestCount > 0){
                                requestVerifyCount += talentRequestCount;
                            }
                            int activityRequestCount = responseObject.optInt("activity_request_count");
                            if (activityRequestCount > 0){
                                requestVerifyCount += activityRequestCount;
                            }

                            if (requestVerifyCount > 0){
                                handler.sendEmptyMessage(HAVE_REQUEST_VERIFY_MESSAGE);
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_order, container, false);
        handler = new MyHandler(this);
        //getAdminRole();
        mUid = SharedPreferencesUtils.getSessionUid(MyApplication.getContext());
        loadMyExperiencesCount();
        loadMyGuidesCount();
        initConentView();
        registerMsgUnreadInfoObserver(true);
        return view;
    }
    
     public void initConentView() {
        mTabLayout = view.findViewById(R.id.order_tab_layout);
        mViewPager = view.findViewById(R.id.order_view_pager);

        //获取标签数据
        myTab = mTabLayout.newTab();

        //添加tab
        mTabLayout.addTab(myTab, 0, true);
         
         setTalentOrderManagerView();
        
        //创建一个viewpager的adapter
        mFragmentAdapter = new OrderFragmentAdapter(getFragmentManager(), mOrderTitleList);
        mViewPager.setAdapter(mFragmentAdapter);
        //mViewPager.setCurrentItem(0);
        mViewPager.setOffscreenPageLimit(1);

        //将TabLayout和ViewPager关联起来
        mTabLayout.setupWithViewPager(mViewPager);

        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            tab.setCustomView(R.layout.tab_message_custom_item);

            TextView tabText = tab.getCustomView().findViewById(R.id.tab_name);
            TextView count = tab.getCustomView().findViewById(R.id.count);
            //Slog.d(TAG, "--------------->title: "+mMessageTitleList.get(i));
            tabText.setText(mOrderTitleList.get(i));
            //count.setText("10");

            if (i == 0) {
                tabText.setTextColor(getResources().getColor(R.color.white));
                tab.select();
}
        }

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                TextView tabText = tab.getCustomView().findViewById(R.id.tab_name);
                tabText.setTextColor(getResources().getColor(R.color.white));
                //getUnreadNotification(handler);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                TextView tabText = tab.getCustomView().findViewById(R.id.tab_name);
                tabText.setTextColor(getResources().getColor(R.color.black));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        
        unReadNotification = mTabLayout.getTabAt(0).getCustomView().findViewById(R.id.count);
        unReadConversation = mTabLayout.getTabAt(1).getCustomView().findViewById(R.id.count);
        unReadVerifyRequest = mTabLayout.getTabAt(2).getCustomView().findViewById(R.id.count);
        mTabLayout.getTabAt(1).getCustomView().setVisibility(View.GONE);
        mTabLayout.getTabAt(2).getCustomView().setVisibility(View.GONE);
        //getRequestVerifyNumber();

        // 未读消息监视器
        ConversationManagerKit.getInstance().addUnreadWatcher(this);
    }

    @Override
    public void updateUnread(int unReadCount) {
        Slog.d(TAG, "------------------>updateUnread: " + unReadCount);
        //TextView unRead = mTabLayout.getTabAt(1).getCustomView().findViewById(R.id.count);
        if (unReadCount > 0) {
            if (unReadConversation.getVisibility() == View.GONE) {
                unReadConversation.setVisibility(View.VISIBLE);
            }
            unReadConversation.setText(String.valueOf(unReadCount));
        } else {
            if (unReadConversation.getVisibility() == View.VISIBLE) {
              unReadConversation.setVisibility(View.GONE);
            }
            unReadConversation.setText("");
        }
    }

    @Override
    public void onUnreadNumChanged(int unReadCount) {
        //TextView unRead = mTabLayout.getTabAt(1).getCustomView().findViewById(R.id.count);
        if (unReadCount > 0) {
            if (unReadConversation.getVisibility() == View.GONE) {
                unReadConversation.setVisibility(View.VISIBLE);
            }
            unReadConversation.setText(String.valueOf(unReadCount));
        } else {
            if (unReadConversation.getVisibility() == View.VISIBLE) {
                unReadConversation.setVisibility(View.GONE);
            }
            unReadConversation.setText("");
        }
    }
    
    @Override
    public void onNotificationUnreadChanged(int unReadCount) {
        getUnreadNotification(handler);
    }

    @Override
    public void onNewContactsApplied(int appliedCount) {
    }

    /**
     * 注册未读消息数量观察者
     */
    private void registerMsgUnreadInfoObserver(boolean register) {
        if (register) {
            ReminderManager.getInstance().registerUnreadNumChangedCallback(this);
        } else {
            ReminderManager.getInstance().unregisterUnreadNumChangedCallback(this);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        //getUnreadNotification(handler);
    }

    public void handleMessage(Message message) {
        switch (message.what) {
            case HAVE_UNREAD_MESSAGE:
                if (unReadNotification != null){
                    if (unReadNotification.getVisibility() == View.GONE) {
                        unReadNotification.setVisibility(View.VISIBLE);
                    }
                    Bundle bundle = message.getData();
                    unReadNotification.setText(String.valueOf(bundle.getInt("count")));
                }
                break;
                case HAVE_NO_UNREAD_MESSAGE:
                if (unReadConversation != null){
                    if (unReadNotification.getVisibility() != View.GONE) {
                        unReadNotification.setVisibility(View.GONE);
                    }
                }
                break;
            case HAVE_REQUEST_VERIFY_MESSAGE:
                Slog.d(TAG, "---------------------->HAVE_REQUEST_VERIFY_MESSAGE requestVerifyCount: "+requestVerifyCount);
                unReadVerifyRequest.setText(String.valueOf(requestVerifyCount));
                if (unReadVerifyRequest.getVisibility() == View.GONE){
                    unReadVerifyRequest.setVisibility(View.VISIBLE);
                }
                break;
            case LOAD_MY_GUIDE_COUNT_DONE:
            case LOAD_MY_EXPERIENCES_DONE:
                mTabLayout.getTabAt(1).getCustomView().setVisibility(View.VISIBLE);
                mTabLayout.getTabAt(2).getCustomView().setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }
    
    private void setTalentOrderManagerView(){
        tobeDoneTab = mTabLayout.newTab();
        finishedTab = mTabLayout.newTab();

        mTabLayout.addTab(tobeDoneTab, 1, false);
        mTabLayout.addTab(finishedTab, 1, false);

    }
    
    private void loadMyExperiencesCount(){
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(mUid))
                .build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_EXPERIENCE_STATISTICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========loadMyExperiencesCount response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject experienceResponse = null;
                        try {
                            experienceResponse = new JSONObject(responseText);
                            if (experienceResponse != null) {
                                myExperienceSize = experienceResponse.optInt("count");
                                if (myExperienceSize > 0){
                                    handler.sendEmptyMessage(LOAD_MY_EXPERIENCES_DONE);
                                }
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
    
    private void loadMyGuidesCount(){
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(mUid))
                .build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_GUIDE_STATISTICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========loadMyGuidesCount response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject experienceResponse = null;
                        try {
                            experienceResponse = new JSONObject(responseText);
                            if (experienceResponse != null) {
                                myGuideCount = experienceResponse.optInt("count");
                                if (myGuideCount > 0){
                                    handler.sendEmptyMessage(LOAD_MY_GUIDE_COUNT_DONE);
                                }
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
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        registerMsgUnreadInfoObserver(false);
    }

    static class MyHandler extends Handler {
        WeakReference<OrderFragment> orderFragmentWeakReference;

        MyHandler(OrderFragment orderFragment) {
            orderFragmentWeakReference = new WeakReference<>(orderFragment);
        }

        @Override
        public void handleMessage(Message message) {
            OrderFragment orderFragment = orderFragmentWeakReference.get();
            if (orderFragment != null) {
                orderFragment.handleMessage(message);
            }
        }
    }

}
