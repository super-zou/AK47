package com.hetang.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.hetang.main.ContactsFragment;
import com.hetang.main.HomeFragment;
import com.hetang.main.MyArchiveFragment;
import com.hetang.main.MeetFragment;
import com.hetang.util.BaseFragment;

import java.util.List;

/**
 * Created by super-zou on 17-9-11.
 */

public class MainFragmentAdapter extends FragmentStatePagerAdapter {
    public static final String TAG = "MainFragmentAdapter";
    private List<BaseFragment> fragmentList;
    private String[] mTitles = {"主页", "遇见", "联系人", "我"};

    public MainFragmentAdapter(FragmentManager fm, List<BaseFragment> fragmentList) {
        super(fm);
        this.fragmentList = fragmentList;
    }

    @Override
    public Fragment getItem(int position) {
        //初始化Fragment数据
        // Slog.d(TAG, "===========main position: "+position);
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new HomeFragment();
                break;
            case 1:
                fragment = new MeetFragment();
                break;
            case 2:
                fragment = new ContactsFragment();
                break;
            case 3:
                fragment = new MyArchiveFragment();
                break;
            default:
                fragment = new HomeFragment();
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
