package com.tongmenhui.launchak47.main;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;

import com.tongmenhui.launchak47.util.Slog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by super-zou on 17-9-11.
 */

public class MainFragmentAdapter extends FragmentStatePagerAdapter{
    public static final String TAG = "FragmentAdapter";
    private ArrayList<String> mTitles;
    private View view;

    public MainFragmentAdapter(FragmentManager fm, ArrayList<String> titles) {
        super(fm);
       // this.view = view;
        mTitles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        //初始化Fragment数据
        //Slog.d(TAG, "===========position: "+position);
        Fragment fragment;
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
