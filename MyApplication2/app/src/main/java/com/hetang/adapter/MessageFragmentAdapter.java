package com.hetang.adapter;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.ViewGroup;

import com.hetang.message.RecentChatFragment;
import com.hetang.message.NotificationFragment;

import java.util.ArrayList;

/**
 * Created by haichao.zou on 2017/11/13.
 */
 public class MessageFragmentAdapter extends FragmentStatePagerAdapter {
    public static final String TAG = "MessageFragmentAdapter";
    private ArrayList<String> mTitles;

    public MessageFragmentAdapter(FragmentManager fm, ArrayList<String> titles) {
        super(fm);
        mTitles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new NotificationFragment();
                break;
            case 1:
                fragment = new RecentChatFragment();
                break;
            default:
                fragment = new NotificationFragment();
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
        Fragment baseFragment;
        switch (position) {
            case 0:
                baseFragment = (NotificationFragment) super.instantiateItem(container, position);
                break;
            case 1:
                baseFragment = (RecentChatFragment) super.instantiateItem(container, position);
                break;
            default:
                baseFragment = (NotificationFragment) super.instantiateItem(container, position);
                break;
        }
        return baseFragment;
    }

}
