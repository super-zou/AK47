package com.tongmenhui.launchak47.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;

import com.tongmenhui.launchak47.util.Slog;

import java.util.ArrayList;

/**
 * Created by super-zou on 17-9-11.
 */

public class MainFragmentAdapter extends FragmentStatePagerAdapter{
    public static final String TAG = "MainFragmentAdapter";
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
        Slog.d(TAG, "===========main position: "+position);
        Fragment fragment;
        switch (position){
            case 0:
                fragment = new MeetFragment();

                break;
            case 1:
                fragment = new ActivityFragment();

                break;
            case 2:
                fragment = new ArchiveFragment();
                break;

            default:
                fragment = new MeetFragment();
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
