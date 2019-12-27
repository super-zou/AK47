package com.hetang.main;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hetang.adapter.MessageFragmentAdapter;
import com.hetang.common.ReminderManager;
import com.hetang.R;
import com.hetang.util.HttpUtil;
import com.hetang.common.MyApplication;
import com.hetang.message.NotificationFragment;
import com.hetang.util.Slog;

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

/**
 * Created by super-zou on 17-9-11.
 */

public class MessageFragment extends Fragment implements ReminderManager.UnreadNumChangedCallback {
    private static final String TAG = "MessageFragment";
    private static final boolean isDebug = false;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private MessageFragmentAdapter mFragmentAdapter;
    private TabLayout.Tab notification_tab;
    private TabLayout.Tab letter_tab;
    
    private MyHandler handler;
    public static final int HAVE_UNREAD_MESSAGE = 0;
    public static final int HAVE_NO_UNREAD_MESSAGE = 1;

    private ArrayList<String> mMessageTitleList = new ArrayList<String>() {
        {
            add("通知");
            add("聊天");
        }
    };
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewContent = inflater.inflate(R.layout.fragment_message, container, false);
        handler = new MyHandler(this);
        initConentView(viewContent);
        registerMsgUnreadInfoObserver(true);
        Slog.d(TAG, "====================>onCreateView");
        return viewContent;
    }

    public void initConentView(View view) {
        mTabLayout = (TabLayout) view.findViewById(R.id.message_tab_layout);
        mViewPager = (ViewPager) view.findViewById(R.id.message_view_pager);
        
        //获取标签数据
        //notification_tab = mTabLayout.newTab().setText(mMessageTitleList.get(0));
        //letter_tab = mTabLayout.newTab().setText(mMessageTitleList.get(1));
        notification_tab = mTabLayout.newTab();
        letter_tab = mTabLayout.newTab();

        //添加tab
        mTabLayout.addTab(notification_tab, 0, true);
        mTabLayout.addTab(letter_tab, 1, false);

        //创建一个viewpager的adapter
        mFragmentAdapter = new MessageFragmentAdapter(getFragmentManager(), mMessageTitleList);
        mViewPager.setAdapter(mFragmentAdapter);
        //mViewPager.setCurrentItem(0);
        mViewPager.setOffscreenPageLimit(1);

        //将TabLayout和ViewPager关联起来
        mTabLayout.setupWithViewPager(mViewPager);
        
        for (int i=0; i<mTabLayout.getTabCount(); i++){
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            tab.setCustomView(R.layout.tab_message_custom_item);

            TextView tabText = tab.getCustomView().findViewById(R.id.tab_name);
            TextView count = tab.getCustomView().findViewById(R.id.count);
            //Slog.d(TAG, "--------------->title: "+mMessageTitleList.get(i));
            tabText.setText(mMessageTitleList.get(i));
            //count.setText("10");

            if(i == 0){
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
    }
    
    @Override
    public void onUnreadNumChanged(int unReadCount) {
        TextView unRead = mTabLayout.getTabAt(1).getCustomView().findViewById(R.id.count);
        if (unReadCount > 0){
            if (unRead.getVisibility() == View.GONE) {
                unRead.setVisibility(View.VISIBLE);
            }
            unRead.setText(String.valueOf(unReadCount));
        }else {
            if (unRead.getVisibility() == View.VISIBLE) {
                unRead.setVisibility(View.GONE);
            }
            unRead.setText("");
        }
    }
    
    @Override
    public void onNotificationUnreadChanged(int unReadCount) {
        getUnreadNotification(handler);
    }
    
        @Override
    public void onNewContactsApplied(int appliedCount){}

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
    public void onResume(){
        super.onResume();
        getUnreadNotification(handler);
    }

    public static void getUnreadNotification(final Handler handler){
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
                            if (unReadCount > 0){

                                Bundle bundle = new Bundle();
                                bundle.putInt("count", unReadCount);
                                message.setData(bundle);
                                message.what = HAVE_UNREAD_MESSAGE;
                                //handler.sendEmptyMessage(HAVE_UNREAD_MESSAGE);
                                }else {
                                message.what = HAVE_NO_UNREAD_MESSAGE;
                                //handler.sendEmptyMessage(HAVE_NO_UNREAD_MESSAGE);
                            }
                            handler.sendMessage(message);

                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) { }
        });
    }
    
    public void handleMessage(Message message) {
        TextView unRead = mTabLayout.getTabAt(0).getCustomView().findViewById(R.id.count);
        switch (message.what){
            case HAVE_UNREAD_MESSAGE:
                if (unRead.getVisibility() == View.GONE) {
                    unRead.setVisibility(View.VISIBLE);
                }
                Bundle bundle = message.getData();
                unRead.setText(String.valueOf(bundle.getInt("count")));

                break;
            case HAVE_NO_UNREAD_MESSAGE:
                if (unRead.getVisibility() != View.GONE){
                    unRead.setVisibility(View.GONE);
                }
                break;
            default:
                break;
        }
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
