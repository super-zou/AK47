package com.tongmenhui.launchak47.main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.meet.MeetActivityFragment;
import com.tongmenhui.launchak47.meet.MeetFragmentAdapter;
import com.tongmenhui.launchak47.meet.MeetRecommendFragment;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by super-zou on 17-9-11.
 */

public class MeetFragment extends Fragment{
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewContent = inflater.inflate(R.layout.fragment_meet,container,false);

        // initConentView(viewContent);
        //initData();

        return viewContent;
    }
}
