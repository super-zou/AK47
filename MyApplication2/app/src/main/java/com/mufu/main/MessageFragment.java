package com.mufu.main;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.mufu.R;
import com.mufu.adapter.MessageFragmentAdapter;
import com.mufu.common.MyApplication;
import com.mufu.common.ReminderManager;
import com.mufu.message.NotificationFragment;
import com.mufu.util.HttpUtil;
import com.mufu.util.Slog;
import com.tencent.qcloud.tim.uikit.modules.conversation.ConversationManagerKit;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.mufu.verify.VerifyActivity.GET_ALL_REQUEST_COUNT_URL;
import static com.mufu.common.SettingsActivity.GET_ADMIN_ROLE_DOWN;
import static com.mufu.common.SettingsActivity.GET_ADMIN_ROLE_URL;

/**
 * Created by super-zou on 17-9-11.
 */

public class MessageFragment extends Fragment implements ReminderManager.UnreadNumChangedCallback, ConversationManagerKit.MessageUnreadWatcher {
    public static final int HAVE_UNREAD_MESSAGE = 0;
    public static final int HAVE_NO_UNREAD_MESSAGE = 1;
    public static final int HAVE_REQUEST_VERIFY_MESSAGE = 3;
    private static final String TAG = "MessageFragment";
    private static final boolean isDebug = false;
    int role = -1;
    int requestVerifyCount = 0;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private MessageFragmentAdapter mFragmentAdapter;
    private TabLayout.Tab notification_tab;
    private TabLayout.Tab letter_tab;
    private TabLayout.Tab verify_tab;
    private TextView unReadNotification;
    private TextView unReadConversation;
    private TextView unReadVerifyRequest;
    private View view;
    private MyHandler handler;
    private ArrayList<String> mMessageTitleList = new ArrayList<String>() {
        {
            add("通知");
            add("聊天");
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
                                //handler.sendEmptyMessage(HAVE_UNREAD_MESSAGE);
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
        view = inflater.inflate(R.layout.fragment_message, container, false);
        handler = new MyHandler(this);
        getAdminRole();
        //initConentView();
        registerMsgUnreadInfoObserver(true);
        Slog.d(TAG, "====================>onCreateView");
        return view;
    }

    public void initConentView() {
        mTabLayout = (TabLayout) view.findViewById(R.id.message_tab_layout);
        mViewPager = (ViewPager) view.findViewById(R.id.message_view_pager);

        //获取标签数据
        notification_tab = mTabLayout.newTab();
        letter_tab = mTabLayout.newTab();

        //添加tab
        mTabLayout.addTab(notification_tab, 0, true);
        mTabLayout.addTab(letter_tab, 1, false);

        Slog.d(TAG, "-------------------->role: " + role);
        if (role >= 0) {
            verify_tab = mTabLayout.newTab();
            mMessageTitleList.add("认证");
            mTabLayout.addTab(verify_tab, 2, false);
        }

        //创建一个viewpager的adapter
        mFragmentAdapter = new MessageFragmentAdapter(getFragmentManager(), mMessageTitleList);
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
            tabText.setText(mMessageTitleList.get(i));
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
        if (role >= 0){
            unReadVerifyRequest = mTabLayout.getTabAt(2).getCustomView().findViewById(R.id.count);
            getRequestVerifyNumber();
        }
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
        getUnreadNotification(handler);
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
            case GET_ADMIN_ROLE_DOWN:
                initConentView();
                break;
            case HAVE_REQUEST_VERIFY_MESSAGE:
                Slog.d(TAG, "---------------------->HAVE_REQUEST_VERIFY_MESSAGE requestVerifyCount: "+requestVerifyCount);
                unReadVerifyRequest.setText(String.valueOf(requestVerifyCount));
                if (unReadVerifyRequest.getVisibility() == View.GONE){
                    unReadVerifyRequest.setVisibility(View.VISIBLE);
                }
                break;

            default:
                break;
        }
    }

    private void getAdminRole() {
        RequestBody requestBody = new FormBody.Builder().build();
        HttpUtil.sendOkHttpRequest(getContext(), GET_ADMIN_ROLE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                try {
                    role = new JSONObject(responseText).optInt("role");
                    handler.sendEmptyMessage(GET_ADMIN_ROLE_DOWN);
                } catch (JSONException e) {
                    e.printStackTrace();
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
        WeakReference<MessageFragment> messageFragmentWeakReference;

        MyHandler(MessageFragment messageFragment) {
            messageFragmentWeakReference = new WeakReference<>(messageFragment);
        }

        @Override
        public void handleMessage(Message message) {
            MessageFragment messageFragment = messageFragmentWeakReference.get();
            if (messageFragment != null) {
                messageFragment.handleMessage(message);
            }
        }
    }

}
