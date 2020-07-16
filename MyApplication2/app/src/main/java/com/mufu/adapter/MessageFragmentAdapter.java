package com.mufu.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import android.view.ViewGroup;

import com.mufu.message.ConversationFragment;
import com.mufu.message.NotificationFragment;
import com.mufu.message.VerifyFragment;

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
                fragment = new ConversationFragment();
                break;
            case 2:
                fragment = new VerifyFragment();
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
                baseFragment = (ConversationFragment) super.instantiateItem(container, position);
                break;
            case 2:
                baseFragment = (VerifyFragment) super.instantiateItem(container, position);
                break;
            default:
                baseFragment = (NotificationFragment) super.instantiateItem(container, position);
                break;
        }
        return baseFragment;
    }

}
