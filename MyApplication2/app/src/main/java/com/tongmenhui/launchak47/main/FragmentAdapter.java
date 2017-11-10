package com.tongmenhui.launchak47.main;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;

import java.util.List;

/**
 * Created by super-zou on 17-9-11.
 */

public class FragmentAdapter extends FragmentStatePagerAdapter{
    public static final String TAB_TAG = "@dream@";
    private List<TabLayout.Tab> mTitles;
    private View view;

    public FragmentAdapter(FragmentManager fm, List<TabLayout.Tab> titles) {
        super(fm);
       // this.view = view;
        mTitles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        //初始化Fragment数据
        ContentFragment fragment = new ContentFragment();
        //String[] title = mTitles.get(position).getTitleString();
       // fragment.setType(Integer.parseInt(title[1]));
        fragment.setTitle(mTitles.get(position).toString());
        return fragment;
    }

    @Override
    public int getCount() {
        return mTitles.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position).getText();
    }
}
