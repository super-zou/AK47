package com.tongmenhui.launchak47.meet;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;

import com.tongmenhui.launchak47.util.Slog;

import java.util.ArrayList;

/**
 * Created by haichao.zou on 2017/11/13.
 */

public class MeetFragmentAdapter extends FragmentPagerAdapter {
    public static final String TAG = "MeetFragmentAdapter";
    private ArrayList<String> mTitles;
    private View view;

    public MeetFragmentAdapter(FragmentManager fm, ArrayList<String> titles) {
        super(fm);
        // this.view = view;
        mTitles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        //Fragment fragment;
        Slog.d(TAG, "============title: "+mTitles.get(position));
        switch (position){
            case 0:
                return new MeetRecommendFragment();
            case 1:
                return new MeetActivityFragment();

            case 2:
                return new MeetDiscoveryFragment();

            default:
                return new MeetRecommendFragment();


        }
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
