package com.hetang.verify.talent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.hetang.R;
import com.hetang.adapter.verify.VerifyFragmentAdapter;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.common.MyApplication;
import com.hetang.util.BaseFragment;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hetang.verify.VerifyActivity.TALENT_VERIFY_PASS_BROADCAST;
import static com.hetang.verify.VerifyActivity.TALENT_VERIFY_REJECT_BROADCAST;
import static com.hetang.verify.user.UserVerifyActivity.GET_ALL_STATUS_COUNT_DONE;

public class TalentVerifyActivity extends BaseAppCompatActivity {

    private static final String TAG = "TalentVerifyActivity";
    private final static boolean isDebug = true;
    TabLayout.Tab unverified_tab;
    TabLayout.Tab verified_tab;
    TabLayout.Tab rejected_tab;

    private int requestCount = 0;
    private int passedCount = 0;
    private int rejectedCount = 0;

    TextView requestCountView;
    TextView passedCountView;
    TextView rejectedCountView;

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private VerifyFragmentAdapter mFragmentAdapter;
    private List<Fragment> mFragmentList = new ArrayList<>();
    public static final int LOAD_TALENT_DONE = 6;

    private static MyHandler handler;
    private TalentVerifyBroadcastReceiver mReceiver;

    private String[] mTitles = MyApplication.getContext().getResources().getStringArray(R.array.verify_tabs);
    public static final String GET_ALL_TALENT_COUNT_URL = HttpUtil.DOMAIN + "?q=talent/get_all_count";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_user_verify);
        handler = new MyHandler(this);
        init();
        registerBroadcast();
    }

    private void init() {
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");

        mTabLayout = findViewById(R.id.tabs);
        mViewPager = findViewById(R.id.viewpager);

        //获取标签数据
        unverified_tab = mTabLayout.newTab();
        verified_tab = mTabLayout.newTab();
        rejected_tab = mTabLayout.newTab();
        //添加tab
        mTabLayout.addTab(unverified_tab,true);
        mTabLayout.addTab(verified_tab);
        mTabLayout.addTab(rejected_tab);
        
        Fragment unVerifiedFragment = new TalentRequestFragment();
        mFragmentList.add(unVerifiedFragment);
        BaseFragment passedFragment = new TalentPassedFragment();
        mFragmentList.add(passedFragment);
        Fragment rejectedFragment = new TalentRejectedFragment();
        mFragmentList.add(rejectedFragment);

        //创建一个viewpager的adapter
        mFragmentAdapter = new VerifyFragmentAdapter(getSupportFragmentManager(), mFragmentList, mTitles);
        mViewPager.setAdapter(mFragmentAdapter);
        mViewPager.setOffscreenPageLimit(3);

        //将TabLayout和ViewPager关联起来
        mTabLayout.setupWithViewPager(mViewPager);
        //index 0 selected by default
        
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            tab.setCustomView(R.layout.tab_authentication_custom);

            TextView count = tab.getCustomView().findViewById(R.id.count);
            TextView tabText = tab.getCustomView().findViewById(R.id.tab_text);
            tabText.setText(mTitles[i]);

            if (i == 0) {
                tabText.setTextColor(getResources().getColor(R.color.blue_dark));
                count.setTextColor(getResources().getColor(R.color.blue_dark));
                tab.select();
            }

        }
        
         mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                TextView count = tab.getCustomView().findViewById(R.id.count);
                TextView tabText = tab.getCustomView().findViewById(R.id.tab_text);
                tabText.setTextColor(getResources().getColor(R.color.blue_dark));
                count.setTextColor(getResources().getColor(R.color.blue_dark));
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                TextView count = tab.getCustomView().findViewById(R.id.count);
                TextView tabText = tab.getCustomView().findViewById(R.id.tab_text);
                tabText.setTextColor(getResources().getColor(R.color.text_default));
                count.setTextColor(getResources().getColor(R.color.text_default));
            }
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        requestCountView = mTabLayout.getTabAt(0).getCustomView().findViewById(R.id.count);
        passedCountView = mTabLayout.getTabAt(1).getCustomView().findViewById(R.id.count);
        rejectedCountView = mTabLayout.getTabAt(2).getCustomView().findViewById(R.id.count);

        //unReadView = mTabLayout.getTabAt(2).getCustomView().findViewById(R.id.unread);
        FontManager.markAsIconContainer(findViewById(R.id.tabs), font);

        getAllCount();
    }

    private void getAllCount(){
        RequestBody requestBody = new FormBody.Builder().build();

        String url = GET_ALL_TALENT_COUNT_URL;

        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), url, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (responseText != null) {
                        try {
                            JSONObject responseObject = new JSONObject(responseText);
                            requestCount = responseObject.optInt("request_count");
                            passedCount = responseObject.optInt("passed_count");
                            rejectedCount = responseObject.optInt("rejected_count");

                            handler.sendEmptyMessage(GET_ALL_STATUS_COUNT_DONE);

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
    
    public void handleMessage(Message message) {

        switch (message.what) {
            case GET_ALL_STATUS_COUNT_DONE:
                requestCountView.setText(String.valueOf(requestCount));
                passedCountView.setText(String.valueOf(passedCount));
                rejectedCountView.setText(String.valueOf(rejectedCount));
                break;
            default:
                break;
        }
    }

    private class TalentVerifyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case TALENT_VERIFY_PASS_BROADCAST:
                    requestCount = Integer.parseInt(requestCountView.getText().toString()) - 1;
                    requestCountView.setText(String.valueOf(requestCount));
                    passedCount = Integer.parseInt(passedCountView.getText().toString()) + 1;
                    passedCountView.setText(String.valueOf(passedCount));
                    break;
                case TALENT_VERIFY_REJECT_BROADCAST:
                    requestCount = Integer.parseInt(requestCountView.getText().toString()) - 1;
                    requestCountView.setText(String.valueOf(requestCount));
                    rejectedCount = Integer.parseInt(rejectedCountView.getText().toString()) + 1;
                    rejectedCountView.setText(String.valueOf(rejectedCount));
                    break;

                default:
                    break;
            }
        }
    }

    //register local broadcast to receive DYNAMICS_ADD_BROADCAST
    private void registerBroadcast() {
        mReceiver = new TalentVerifyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TALENT_VERIFY_PASS_BROADCAST);
        intentFilter.addAction(TALENT_VERIFY_REJECT_BROADCAST);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, intentFilter);
    }

    //unregister local broadcast
    private void unRegisterBroadcast() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterBroadcast();
    }

    static class MyHandler extends Handler {
        WeakReference<TalentVerifyActivity> authenticationActivityWeakReference;
        MyHandler(TalentVerifyActivity authenticationActivity) {
            authenticationActivityWeakReference = new WeakReference<>(authenticationActivity);
        }

        @Override
        public void handleMessage(Message message) {
            TalentVerifyActivity authenticationActivity = authenticationActivityWeakReference.get();
            if (authenticationActivity != null) {
                authenticationActivity.handleMessage(message);
            }
        }
    }
}
