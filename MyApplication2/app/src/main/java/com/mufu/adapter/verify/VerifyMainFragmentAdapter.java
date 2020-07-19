package com.mufu.adapter.verify;

import com.mufu.verify.activity.ActivityPassedFragment;
import com.mufu.verify.activity.ActivityRejectedFragment;
import com.mufu.verify.activity.ActivityRequestFragment;
import com.mufu.verify.talent.TalentPassedFragment;
import com.mufu.verify.talent.TalentRejectedFragment;
import com.mufu.verify.talent.TalentRequestFragment;

import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class VerifyMainFragmentAdapter extends FragmentStatePagerAdapter {
    public static final String TAG = "VerifyMainFragmentAdapter";
    private List<Fragment> fragmentList;
    private String[] mTitles;
    private int type;

    public VerifyMainFragmentAdapter(FragmentManager fm, List<Fragment> fragmentList, String[] mTitles, int type) {
        super(fm);
        this.fragmentList = fragmentList;
        this.mTitles = mTitles;
        this.type = type;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    /*
    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        switch (type) {
            case MAIN:
                fragment = getMainFragment(position);
                break;
            case USER:
                fragment = getUserFragment(position);
                break;
            case TALENT:
                fragment = getTalentFragment(position);
                break;
            case ACTIVITY:
                fragment = getActivityFragment(position);
                break;
            default:
                fragment = getMainFragment(position);
                break;
        }
        return fragment;
    }
    */

    /*
    private Fragment getMainFragment(int position){
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new UserVerifyActivity();
                break;
            case 1:
                fragment = new TalentVerifyFragment();
                break;
            case 2:
                fragment = new ActivityVerifyFragment();
                break;
            default:
                fragment = new UserVerifyActivity();
                break;

        }
        return fragment;
    }
    */
/*
    private Fragment getUserFragment(int position){
        Fragment fragment;

        switch (position) {
            case 0:
                fragment = new UserRequestFragment();
                break;
            case 1:
                fragment = new UserPassedFragment();
                break;
            case 2:
                fragment = new UserRejectedFragment();
                break;
            default:
                fragment = new UserRequestFragment();
                break;
        }


        return fragment;
    }

         */

    private Fragment getTalentFragment(int position){
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new TalentRequestFragment();
                break;
            case 1:
                fragment = new TalentPassedFragment();
                break;
            case 2:
                fragment = new TalentRejectedFragment();
                break;
            default:
                fragment = new TalentRequestFragment();
                break;

        }
        return fragment;
    }

    private Fragment getActivityFragment(int position){
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new ActivityRequestFragment();
                break;
            case 1:
                fragment = new ActivityPassedFragment();
                break;
            case 2:
                fragment = new ActivityRejectedFragment();
                break;
            default:
                fragment = new ActivityRequestFragment();
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
