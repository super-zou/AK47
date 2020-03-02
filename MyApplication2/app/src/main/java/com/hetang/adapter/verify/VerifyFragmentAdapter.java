package com.hetang.adapter.verify;

import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class VerifyFragmentAdapter extends FragmentStatePagerAdapter {
    public static final String TAG = "VerifyMainFragmentAdapter";
    private List<Fragment> fragmentList;
    private String[] mTitles;

    public VerifyFragmentAdapter(FragmentManager fm, List<Fragment> fragmentList, String[] mTitles) {
        super(fm);
        this.fragmentList = fragmentList;
        this.mTitles = mTitles;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
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
