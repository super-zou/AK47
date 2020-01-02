package com.hetang.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.hetang.common.AuthenticationFragment;
import com.hetang.home.HomeFragment;
import com.hetang.main.ContactsFragment;
import com.hetang.main.MeetArchiveFragment;
import com.hetang.main.MeetFragment;
import com.hetang.main.MessageFragment;

import java.util.List;

import static com.hetang.common.AuthenticationActivity.REJECTED;
import static com.hetang.common.AuthenticationActivity.VERIFIED;
import static com.hetang.common.AuthenticationActivity.unVERIFIED;

public class AuthenticationFragmentAdapter extends FragmentStatePagerAdapter {
    public static final String TAG = "MainFragmentAdapter";
    private List<Fragment> fragmentList;
    private String[] mTitles;

    public AuthenticationFragmentAdapter(FragmentManager fm, List<Fragment> fragmentList, String[] mTitles) {
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
                fragment = AuthenticationFragment.newInstance(unVERIFIED);
                break;
            case 1:
                fragment = AuthenticationFragment.newInstance(VERIFIED);
                break;
            case 2:
                fragment = AuthenticationFragment.newInstance(REJECTED);
                break;
            default:
                fragment = AuthenticationFragment.newInstance(unVERIFIED);
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
