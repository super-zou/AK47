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
    private View viewContent;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private MeetFragmentAdapter mFragmentAdapter;
    private TabLayout.Tab recomend_tab;
    private TabLayout.Tab dynamic_event_tab;
    private TabLayout.Tab discovery_tab;

    private Fragment mRecommendFragment;
    private Fragment mActivityFragment;
    private Fragment mDiscoveryFragment;


    private ArrayList<String> mMeetTitleList = new ArrayList<String>(){
        {
            add("recomend");
            add("activity");
            add("discovery");
        }
    };

    private ArrayList<Fragment> mFragmentList = new ArrayList<Fragment>();

    public MeetFragment() {

    }

/*
    public static MeetFragment newInstance(String text){
        Bundle bundle = new Bundle();
        bundle.putString("text",text);
        MeetFragment meetFragment = new MeetFragment();
        meetFragment.setArguments(bundle);
        return  meetFragment;
    }
*/
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewContent = inflater.inflate(R.layout.fragment_meet,container,false);
       // initConentView(viewContent);
        return viewContent;
    }
    @Override
    public void onResume(){
        super.onResume();
        initConentView(viewContent);
    }

    public void initConentView(View viewContent) {
        mTabLayout = (TabLayout) viewContent.findViewById(R.id.meet_tab_layout);
        mViewPager = (ViewPager) viewContent.findViewById(R.id.meet_view_pager);
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


        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                Toast.makeText(getContext(), "选中的" + tab.getText(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

                Toast.makeText(getContext(), "未选中的" + tab.getText(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

                Toast.makeText(getContext(), "复选的" + tab.getText(), Toast.LENGTH_SHORT).show();

            }
        });
    }


}
