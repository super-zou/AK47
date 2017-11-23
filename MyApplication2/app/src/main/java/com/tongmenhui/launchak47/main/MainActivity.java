package com.tongmenhui.launchak47.main;

import android.content.SharedPreferences;
import android.graphics.Typeface;
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
import com.tongmenhui.launchak47.util.FontManager;
import com.tongmenhui.launchak47.util.Slog;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    //private List<TabLayout.Tab> mTableItemList;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private MainFragmentAdapter mMainFragmentAdapter;
    private TabLayout.Tab home_tab;
    private TabLayout.Tab meet_tab;
    private TabLayout.Tab archive_tab;
    private ArrayList<String> titleList = new ArrayList<String>(){
        {
            add("home");
            add("meet");
            add("archive");
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences preferences = getSharedPreferences("session", MODE_PRIVATE);
       // Slog.d(TAG, "=========================session id: "+preferences.getString("sessionId",""));
       // Slog.d(TAG, "=========================session: "+preferences.getString("session_name",""));
       // Slog.d(TAG, "=========================uid: "+preferences.getInt("uid", -1));
        initTab();
        //Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        //FontManager.markAsIconContainer(findViewById(R.id.behavior_statistics), iconFont);
        //Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome.ttf");

    }

    //初始化Tab
    private void initTab() {
        Slog.d(TAG, "==================initTab");
        mTabLayout = (TabLayout)findViewById(R.id.tabLayout);
        //mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        home_tab = mTabLayout.newTab().setText(titleList.get(0));
        meet_tab = mTabLayout.newTab().setText(titleList.get(1));
        archive_tab = mTabLayout.newTab().setText(titleList.get(2));

        //添加tab
        mTabLayout.addTab(home_tab, 0);
        mTabLayout.addTab(meet_tab, 1);
        mTabLayout.addTab(archive_tab, 2);

        mViewPager = (ViewPager)findViewById(R.id.viewPager);
        mMainFragmentAdapter = new MainFragmentAdapter(getSupportFragmentManager(), titleList);
        mViewPager.setAdapter(mMainFragmentAdapter);
        mViewPager.setCurrentItem(0);
       // mViewPager.setOffscreenPageLimit(0);

        mTabLayout.setupWithViewPager(mViewPager);
    }

}
