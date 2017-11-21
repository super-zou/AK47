package com.tongmenhui.launchak47.meet;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by haichao.zou on 2017/11/13.
 */

public class MeetFragmentAdapter extends FragmentStatePagerAdapter {
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
        Fragment fragment;
        switch (position){
            case 0:
                fragment = new MeetRecommendFragment();
                break;
            case 1:
                fragment = new MeetActivityFragment();
                break;
            case 2:
                fragment = new MeetRecommendFragment();
                break;
            default:
                fragment = new MeetRecommendFragment();
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
