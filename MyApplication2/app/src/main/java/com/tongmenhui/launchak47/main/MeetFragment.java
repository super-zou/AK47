package com.tongmenhui.launchak47.main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.meet.MeetActivityFragment;
import com.tongmenhui.launchak47.meet.MeetFragmentAdapter;
import com.tongmenhui.launchak47.meet.MeetRecommendFragment;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by super-zou on 17-9-11.
 */

public class MeetFragment extends Fragment{
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private MeetFragmentAdapter mFragmentAdapter;
    private TabLayout.Tab recomend_tab;
    private TabLayout.Tab dynamic_event_tab;
    private TabLayout.Tab discovery_tab;

    private ArrayList<String> mMeetTitleList = new ArrayList<String>(){
        {
            add("recomend");
            add("activity");
            add("discovery");
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewContent = inflater.inflate(R.layout.fragment_meet,container,false);

        initConentView(viewContent);
        return viewContent;
    }

    public void initConentView(View view){
        mTabLayout = (TabLayout) view.findViewById(R.id.meet_tab_layout);
        mViewPager = (ViewPager) view.findViewById(R.id.meet_view_pager);
        //获取标签数据
        recomend_tab = mTabLayout.newTab().setText(mMeetTitleList.get(0));
        dynamic_event_tab = mTabLayout.newTab().setText(mMeetTitleList.get(1));
        discovery_tab = mTabLayout.newTab().setText(mMeetTitleList.get(2));
        //添加tab
        mTabLayout.addTab(recomend_tab, 0, true);
        mTabLayout.addTab(dynamic_event_tab, 1, false);
        mTabLayout.addTab(discovery_tab, 2, false);

        //mTabLayout.getTabAt(0).select();


        //mRecommendFragment = new MeetRecommendFragment();
        // mActivityFragment = new MeetActivityFragment();
        // mDiscoveryFragment = new MeetRecommendFragment();


        // mFragmentList.add(mRecommendFragment);
        //  mFragmentList.add(mActivityFragment);
        //  mFragmentList.add(mDiscoveryFragment);

        //创建一个viewpager的adapter
        mFragmentAdapter = new MeetFragmentAdapter(getChildFragmentManager(), mMeetTitleList);
        mViewPager.setAdapter(mFragmentAdapter);
        //mViewPager.setCurrentItem(0);

        //将TabLayout和ViewPager关联起来
        mTabLayout.setupWithViewPager(mViewPager);
    }
}
