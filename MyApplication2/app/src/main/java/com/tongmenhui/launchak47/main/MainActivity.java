package com.tongmenhui.launchak47.main;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.adapter.MainFragmentAdapter;

import java.util.ArrayList;

//import android.support.v4.app.FragmentTabHost;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    TabLayout.Tab main_tab;
    TabLayout.Tab meet_tab;
    TabLayout.Tab contacts_tab;
    //TabLayout.Tab activity_tab;
    TabLayout.Tab archive_tab;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private MainFragmentAdapter mFragmentAdapter;
    private ArrayList<String> mTitleList = new ArrayList<String>() {
        {
            add("主页");
            add("遇见");
            add("联系人");
            add("我");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        //获取标签数据
        main_tab = mTabLayout.newTab().setText(mTitleList.get(0));
        meet_tab = mTabLayout.newTab().setText(mTitleList.get(1));
        contacts_tab = mTabLayout.newTab().setText(mTitleList.get(2));
        archive_tab = mTabLayout.newTab().setText(mTitleList.get(3));
        //添加tab
        mTabLayout.addTab(main_tab);
        mTabLayout.addTab(meet_tab);
        mTabLayout.addTab(contacts_tab);
        mTabLayout.addTab(archive_tab);

        //创建一个viewpager的adapter
        mFragmentAdapter = new MainFragmentAdapter(getSupportFragmentManager(), mTitleList);
        mViewPager.setAdapter(mFragmentAdapter);
        mViewPager.setOffscreenPageLimit(2);

        //将TabLayout和ViewPager关联起来
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void initView() {
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
    }
}
