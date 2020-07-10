package com.hetang.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import com.hetang.order.FinishedFragment;
import com.hetang.order.MyFragment;
import com.hetang.order.QueuedFragment;

import java.util.ArrayList;

/**
 * Created by haichao.zou on 2017/11/13.
 */
 public class OrderFragmentAdapter extends FragmentStatePagerAdapter {
    public static final String TAG = "MessageFragmentAdapter";
    private ArrayList<String> mTitles;

    public OrderFragmentAdapter(FragmentManager fm, ArrayList<String> titles) {
        super(fm);
        mTitles = titles;
    }
    
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new MyFragment();
                break;
            case 1:
                fragment = new QueuedFragment();
                break;
            case 2:
                fragment = new FinishedFragment();
                break;
        }
        return fragment;
    }
    
    @Override
    public int getCount() {
        return mTitles != null ? mTitles.size() : 0;
    }
    
    @Override
    public CharSequence getPageTitle(int position) {

        return mTitles.get(position);
    }


    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }
    
    @Override
    @NonNull
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment baseFragment = null;
        switch (position) {
            case 0:
                baseFragment = (MyFragment) super.instantiateItem(container, position);
                break;
            case 1:
                baseFragment = (QueuedFragment) super.instantiateItem(container, position);
                break;
            case 2:
                baseFragment = (FinishedFragment) super.instantiateItem(container, position);
                break;
        }
        return baseFragment;
    }

}
