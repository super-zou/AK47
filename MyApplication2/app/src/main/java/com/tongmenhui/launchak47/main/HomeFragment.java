package com.tongmenhui.launchak47.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tongmenhui.launchak47.R;

import java.util.Arrays;
import android.support.v4.app.Fragment;

/**
 * Created by super-zou on 17-9-11.
 */

public class HomeFragment extends Fragment{
    private View viewContent;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewContent = inflater.inflate(R.layout.fragment_home,container,false);
        return viewContent;
    }
}
