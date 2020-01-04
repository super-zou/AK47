package com.hetang.common;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import com.hetang.R;
import com.hetang.adapter.AuthenticationFragmentAdapter;
import com.hetang.adapter.MainFragmentAdapter;
import com.hetang.home.HomeFragment;
import com.hetang.main.ContactsFragment;
import com.hetang.main.MeetArchiveFragment;
import com.hetang.main.MeetFragment;
import com.hetang.main.MessageFragment;
import com.hetang.util.BaseFragment;
import com.hetang.util.FontManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.hetang.util.ParseUtils.startMeetArchiveActivity;

public class AuthenticationActivity extends BaseAppCompatActivity {

    private static final String TAG = "AuthenticationActivity";
    private final static boolean isDebug = false;
    TabLayout.Tab unverified_tab;
    TabLayout.Tab verified_tab;
    TabLayout.Tab rejected_tab;
    
    public static final int unVERIFIED = 0;
    public static final int VERIFIED = 1;
    public static final int REJECTED = 2;

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private AuthenticationFragmentAdapter mFragmentAdapter;
    private List<Fragment> mFragmentList = new ArrayList<>();

    private static MyHandler handler;

    private String[] mTitles = MyApplication.getContext().getResources().getStringArray(R.array.authentication_tabs);
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        init();
    }


    private void init() {
        handler = new MyHandler(this);

        initView();
    }
    
    private void initView() {
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
        
        BaseFragment unVerifiedFragment = AuthenticationFragment.newInstance(unVERIFIED);
        mFragmentList.add(unVerifiedFragment);
        BaseFragment verifiedFragment = AuthenticationFragment.newInstance(VERIFIED);
        mFragmentList.add(verifiedFragment);
        Fragment rejectedFragment = AuthenticationFragment.newInstance(REJECTED);
        mFragmentList.add(rejectedFragment);

        //创建一个viewpager的adapter
        mFragmentAdapter = new AuthenticationFragmentAdapter(getSupportFragmentManager(), mFragmentList, mTitles);
        mViewPager.setAdapter(mFragmentAdapter);
        //mViewPager.setOffscreenPageLimit(0);

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

        //unReadView = mTabLayout.getTabAt(2).getCustomView().findViewById(R.id.unread);
        FontManager.markAsIconContainer(findViewById(R.id.tabs), font);
    }
    
    public void handleMessage(Message message) {

        switch (message.what) {
            case MessageFragment.HAVE_UNREAD_MESSAGE:
                break;
            default:
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    static class MyHandler extends Handler {
        WeakReference<AuthenticationActivity> authenticationActivityWeakReference;
        MyHandler(AuthenticationActivity authenticationActivity) {
            authenticationActivityWeakReference = new WeakReference<>(authenticationActivity);
        }

        @Override
        public void handleMessage(Message message) {
            AuthenticationActivity authenticationActivity = authenticationActivityWeakReference.get();
            if (authenticationActivity != null) {
                authenticationActivity.handleMessage(message);
            }
        }
    }
}
