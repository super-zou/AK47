package com.tongmenhui.launchak47.meet;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;

import com.tongmenhui.launchak47.main.ArchiveFragment;
import com.tongmenhui.launchak47.main.ContentFragment;
import com.tongmenhui.launchak47.main.HomeFragment;
import com.tongmenhui.launchak47.main.MeetFragment;

import java.util.ArrayList;

/**
 * Created by haichao.zou on 2017/11/13.
 */

public class MeetFragmentAdapter extends FragmentStatePagerAdapter {
    public static final String TAB_TAG = "MeetFragmentAdapter";
    private ArrayList<String> mTitles;
    private View view;

    public MeetFragmentAdapter(FragmentManager fm, ArrayList<String> titles) {
        super(fm);
        // this.view = view;
        mTitles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        //初始化Fragment数据
        Fragment fragment = new ContentFragment();
        /*
        switch (position){
            case 0:
                fragment = new HomeFragment();
                break;
            case 1:
                // fragment = new ContentFragment();
                //String[] title = mTitles.get(position).getTitleString();
                // fragment.setType(Integer.parseInt(title[1]));
                //fragment.setTitle(mTitles.get(position));
                fragment = new MeetFragment();
                break;
            case 2:
                fragment = new ArchiveFragment();
                break;

            default:
                fragment = new HomeFragment();
                break;

        }
        */
        return fragment;
    }

    @Override
    public int getCount() {
        return mTitles.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }
}
