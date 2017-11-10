package com.tongmenhui.launchak47.main;

import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentTabHost;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.util.Slog;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private List<TabLayout.Tab> mTableItemList;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private FragmentAdapter mFragmentAdapter;
    private TabLayout.Tab home_tab;
    private TabLayout.Tab meet_tab;
    private TabLayout.Tab archive_tab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences preferences = getSharedPreferences("session", MODE_PRIVATE);
        Slog.d(TAG, "=========================session id: "+preferences.getString("sessionId",""));
        Slog.d(TAG, "=========================session: "+preferences.getString("session_name",""));
        Slog.d(TAG, "=========================uid: "+preferences.getInt("uid", -1));
        initTab();
    }

    //初始化Tab
    private void initTab() {
        mTabLayout = (TabLayout)findViewById(R.id.tabLayout);
        home_tab = mTabLayout.getTabAt(0);
        meet_tab = mTabLayout.getTabAt(1);
        archive_tab = mTabLayout.getTabAt(2);
        //添加tab
        mTableItemList.add(home_tab);
        mTableItemList.add(meet_tab);
        mTableItemList.add(archive_tab);

        mViewPager = (ViewPager)findViewById(R.id.viewPager);
        mFragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), mTableItemList);
        mViewPager.setAdapter(mFragmentAdapter);

        mTabLayout.setupWithViewPager(mViewPager);
    }

}
