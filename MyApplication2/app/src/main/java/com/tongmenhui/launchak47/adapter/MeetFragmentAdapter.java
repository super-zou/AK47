package com.tongmenhui.launchak47.adapter;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.meet.MeetDiscoveryFragment;
import com.tongmenhui.launchak47.meet.MeetDynamicsFragment;
import com.tongmenhui.launchak47.meet.MeetRecommendFragment;
import com.tongmenhui.launchak47.util.BaseFragment;
import com.tongmenhui.launchak47.util.FontManager;
import com.tongmenhui.launchak47.util.Slog;

import java.util.ArrayList;

import static com.tongmenhui.launchak47.util.MyApplication.getContext;

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
        //Fragment fragment;
        Slog.d(TAG, "============title: "+mTitles.get(position));
        switch (position){
            case 0:
                return new MeetRecommendFragment();
            case 1:
                return new MeetDynamicsFragment();
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

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override @NonNull
    public Object instantiateItem(ViewGroup container, int position) {
        BaseFragment baseFragment;
        switch (position){
            case 0:
                baseFragment = (MeetRecommendFragment) super.instantiateItem(container, position);
                break;
            case 1:
                baseFragment = (MeetDynamicsFragment) super.instantiateItem(container, position);
                break;

            case 2:
                baseFragment = (MeetDiscoveryFragment) super.instantiateItem(container, position);
                break;
            default:
                baseFragment = (MeetRecommendFragment) super.instantiateItem(container, position);
                break;
        }
        return baseFragment;
    }

}
