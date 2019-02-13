package com.tongmenhui.launchak47.main;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.os.Build;

import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.adapter.MainFragmentAdapter;
import com.tongmenhui.launchak47.util.BaseFragment;
import com.tongmenhui.launchak47.util.FontManager;
import com.tongmenhui.launchak47.util.Slog;

import java.util.ArrayList;
import java.util.List;

//import android.support.v4.app.FragmentTabHost;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    TabLayout.Tab home_tab;
    TabLayout.Tab meet_tab;
    TabLayout.Tab contacts_tab;
    //TabLayout.Tab activity_tab;
    TabLayout.Tab me_tab;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private MainFragmentAdapter mFragmentAdapter;
    private List<BaseFragment> mFragmentList = new ArrayList<>();

    private String[] mTitles = {"主页", "遇见", "联系人", "我"};
    private int[] mIcons = {R.string.home, R.string.meet, R.string.contacts, R.string.me};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");

        //获取标签数据
        home_tab = mTabLayout.newTab();
        meet_tab = mTabLayout.newTab();
        contacts_tab = mTabLayout.newTab();
        me_tab = mTabLayout.newTab();
        //添加tab
        mTabLayout.addTab(home_tab);
        mTabLayout.addTab(meet_tab);
        mTabLayout.addTab(contacts_tab);
        mTabLayout.addTab(me_tab);
        
                BaseFragment home = new HomeFragment();
        mFragmentList.add(home);
        BaseFragment meet = new MeetFragment();
        mFragmentList.add(meet);
        BaseFragment contacts = new ContactsFragment();
        mFragmentList.add(contacts);
        BaseFragment me = new MyArchiveFragment();
        mFragmentList.add(me);
        
        //创建一个viewpager的adapter
        mFragmentAdapter = new MainFragmentAdapter(getSupportFragmentManager(), mFragmentList);
        mViewPager.setAdapter(mFragmentAdapter);
        mViewPager.setOffscreenPageLimit(3);

        //将TabLayout和ViewPager关联起来
        mTabLayout.setupWithViewPager(mViewPager);
        //index 0 selected by default
        
        for (int i=0; i<mTabLayout.getTabCount(); i++){
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            tab.setCustomView(R.layout.tab_main_custom_item);

            TextView tabIcon = tab.getCustomView().findViewById(R.id.tab_icon);
            TextView tabText = tab.getCustomView().findViewById(R.id.tab_text);
            tabIcon.setText(mIcons[i]);
            tabText.setText(mTitles[i]);

            if(i == 0){
                tabText.setTextColor(getResources().getColor(R.color.blue_dark));
                tabIcon.setTextColor(getResources().getColor(R.color.blue_dark));
                tab.select();
            }else {
                Slog.d(TAG, "==============get color:"+Integer.toHexString(tabText.getCurrentTextColor()));
            }
        }
        
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                TextView tabIcon = tab.getCustomView().findViewById(R.id.tab_icon);
                TextView tabText = tab.getCustomView().findViewById(R.id.tab_text);
                tabText.setTextColor(getResources().getColor(R.color.blue_dark));
                tabIcon.setTextColor(getResources().getColor(R.color.blue_dark));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                TextView tabIcon = tab.getCustomView().findViewById(R.id.tab_icon);
                TextView tabText = tab.getCustomView().findViewById(R.id.tab_text);
                tabText.setTextColor(getResources().getColor(R.color.text_default));
                tabIcon.setTextColor(getResources().getColor(R.color.text_default));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        FontManager.markAsIconContainer(findViewById(R.id.tabs), font);
    
    }

    private void initView() {
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
    }
}
