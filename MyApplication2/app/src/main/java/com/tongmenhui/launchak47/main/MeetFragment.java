package com.tongmenhui.launchak47.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;

import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.adapter.MeetFragmentAdapter;

import java.util.ArrayList;

/**
 * Created by super-zou on 17-9-11.
 */

public class MeetFragment extends Fragment{
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private MeetFragmentAdapter mFragmentAdapter;
    private TabLayout.Tab recomend_tab;
    private TabLayout.Tab dynamics_tab;
    private TabLayout.Tab discovery_tab;

    private ArrayList<String> mMeetTitleList = new ArrayList<String>(){
        {
            add("recomend");
            add("dynamics");
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
        dynamics_tab = mTabLayout.newTab().setText(mMeetTitleList.get(1));
        discovery_tab = mTabLayout.newTab().setText(mMeetTitleList.get(2));
        //添加tab
        mTabLayout.addTab(recomend_tab, 0, true);
        mTabLayout.addTab(dynamics_tab, 1, false);
        mTabLayout.addTab(discovery_tab, 2, false);

        //mTabLayout.getTabAt(0).select();


        //mRecommendFragment = new MeetRecommendFragment();
        // mActivityFragment = new MeetDynamicsFragment();
        // mDiscoveryFragment = new MeetRecommendFragment();


        // mFragmentList.add(mRecommendFragment);
        //  mFragmentList.add(mActivityFragment);
        //  mFragmentList.add(mDiscoveryFragment);

        //创建一个viewpager的adapter
        mFragmentAdapter = new MeetFragmentAdapter(getFragmentManager(), mMeetTitleList);
        mViewPager.setAdapter(mFragmentAdapter);
        //mViewPager.setCurrentItem(0);
        mViewPager.setOffscreenPageLimit(2);

        //将TabLayout和ViewPager关联起来
        mTabLayout.setupWithViewPager(mViewPager);
    }

}
