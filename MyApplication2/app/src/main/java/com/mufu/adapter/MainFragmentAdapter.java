package com.mufu.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.mufu.main.DynamicFragment;
import com.mufu.main.ExploreFragment;
import com.mufu.main.MessageFragment;
import com.mufu.main.MeetArchiveFragment;
import com.mufu.order.OrderFragment;

import java.util.List;

/**
 * Created by super-zou on 17-9-11.
 */

public class MainFragmentAdapter extends FragmentStatePagerAdapter {
    public static final String TAG = "MainFragmentAdapter";
    private List<Fragment> fragmentList;
    private String[] mTitles;

    public MainFragmentAdapter(FragmentManager fm, List<Fragment> fragmentList, String[] mTitles) {
        super(fm);
        this.fragmentList = fragmentList;
        this.mTitles = mTitles;
    }

    @Override
    public Fragment getItem(int position) {
        //初始化Fragment数据
        // Slog.d(TAG, "===========main position: "+position);
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new ExploreFragment();
                break;
            case 1:
                fragment = new DynamicFragment();
                break;
            case 2:
                fragment = new MessageFragment();
                break;
            case 3:
                fragment = new OrderFragment();
                break;
            case 4:
                fragment = new MeetArchiveFragment();
                break;
            default:
                fragment = new ExploreFragment();
                break;

        }
        return fragment;
    }

    @Override
    public int getCount() {
        return fragmentList != null ? fragmentList.size() : 0;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }
}