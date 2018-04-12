package com.tongmenhui.launchak47.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.TextView;
import android.widget.Toast;

import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.adapter.MeetFragmentAdapter;
import com.tongmenhui.launchak47.util.BaseFragment;
import com.tongmenhui.launchak47.util.FontManager;
import com.tongmenhui.launchak47.util.ItemsDialogFragment;

import java.util.ArrayList;

import static com.tongmenhui.launchak47.util.MyApplication.getContext;

/**
 * Created by super-zou on 17-9-11.
 */

public class MeetFragment extends BaseFragment {
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

    @Override
    protected void initView(View view){

    }

    @Override
    protected void initData(){

    }

    @Override
    protected int getLayoutId(){
       return 0;
    }

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

        //创建一个viewpager的adapter
        mFragmentAdapter = new MeetFragmentAdapter(getFragmentManager(), mMeetTitleList);
        mViewPager.setAdapter(mFragmentAdapter);
        //mViewPager.setCurrentItem(0);
        mViewPager.setOffscreenPageLimit(3);

        //将TabLayout和ViewPager关联起来
        mTabLayout.setupWithViewPager(mViewPager);

        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome.ttf");
        FontManager.markAsIconContainer(view.findViewById(R.id.activity_create), font);

        TextView ActivityCreate = (TextView) view.findViewById(R.id.activity_create);
        ActivityCreate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getContext(), AddDynamicsActivity.class);
                startActivity(intent);
            }
        });
    }
}
