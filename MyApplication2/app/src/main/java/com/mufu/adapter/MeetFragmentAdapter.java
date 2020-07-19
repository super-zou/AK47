package com.mufu.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import com.mufu.meet.EdenGardenFragment;
import com.mufu.meet.MeetFragment;
import com.mufu.util.BaseFragment;

public class MeetFragmentAdapter extends FragmentStatePagerAdapter {
    public static final String TAG = "ExploreFragmentAdapter";
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
        switch (position) {
            case 0:
                fragment = new MeetFragment();
                break;
            case 1:
                fragment = new EdenGardenFragment();
                break;
            default:
                fragment = new MeetFragment();
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
                baseFragment = (MeetFragment) super.instantiateItem(container, position);
                break;
            case 1:
                baseFragment = (EdenGardenFragment) super.instantiateItem(container, position);
                break;
            default:
                baseFragment = (MeetFragment) super.instantiateItem(container, position);
                break;
        }
        return baseFragment;
    }

}
