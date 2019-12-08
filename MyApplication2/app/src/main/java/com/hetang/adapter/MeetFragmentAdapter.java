package com.hetang.adapter;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.hetang.meet.GroupFragment;
import com.hetang.meet.MeetSingleGroupFragment;
import com.hetang.meet.MeetDiscoveryFragment;
import com.hetang.meet.MeetDynamicsFragment;
import com.hetang.meet.MeetRecommendFragment;
import com.hetang.util.BaseFragment;


/**
 * Created by haichao.zou on 2017/11/13.
 */

public class MeetFragmentAdapter extends FragmentStatePagerAdapter {
    public static final String TAG = "MeetFragmentAdapter";
    private String[] mTitles;
    private View view;

    public MeetFragmentAdapter(FragmentManager fm, String[] titles) {
        super(fm);
        // this.view = view;
        mTitles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        BaseFragment fragment;
        //Slog.d(TAG, "============title: "+mTitles.get(position));
        switch (position) {
            case 0:
                fragment = new MeetRecommendFragment();
                break;
            case 1:
                fragment = new GroupFragment();
                break;
            case 2:
                 fragment = new MeetDynamicsFragment();
                break;
            case 3:
                fragment = new MeetDiscoveryFragment();
                break;
            default:
                fragment = new MeetRecommendFragment();
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return mTitles != null ? mTitles.length : 0;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        return mTitles[position];
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    @NonNull
    public Object instantiateItem(ViewGroup container, int position) {
        BaseFragment baseFragment;
        switch (position) {
            case 0:
                baseFragment = (MeetRecommendFragment) super.instantiateItem(container, position);
                break;
            case 1:
                baseFragment = (GroupFragment) super.instantiateItem(container, position);
                break;

            case 2:
                baseFragment = (MeetDynamicsFragment) super.instantiateItem(container, position);
                break;
            case 3:
                baseFragment = (MeetDiscoveryFragment) super.instantiateItem(container, position);
                break;
            default:
                baseFragment = (MeetRecommendFragment) super.instantiateItem(container, position);
                break;
        }
        return baseFragment;
    }

}
