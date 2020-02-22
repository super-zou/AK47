package com.hetang.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.hetang.authenticate.PassedFragment;
import com.hetang.authenticate.RejectedFragment;
import com.hetang.authenticate.RequestFragment;

import java.util.List;

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
                fragment = new RequestFragment();
                break;
            case 1:
                fragment = new PassedFragment();
                break;
            case 2:
                fragment = new RejectedFragment();
                break;
            default:
                fragment = new RequestFragment();
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
