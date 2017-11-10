package com.tongmenhui.launchak47.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;

import com.tongmenhui.launchak47.R;

import java.util.Arrays;

/**
 * Created by super-zou on 17-9-11.
 */

public class MeetFragment extends Fragment{
    private View viewContent;
    private TabLayout meet_tab_layout;
    private ViewPager meet_view_pager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewContent = inflater.inflate(R.layout.fragment_meet,container,false);
        initConentView(viewContent);
        //initData();

        return viewContent;
    }

    public void initConentView(View viewContent) {
        this.meet_tab_layout = (TabLayout) viewContent.findViewById(R.id.meet_tab_layout);
        this.meet_view_pager = (ViewPager) viewContent.findViewById(R.id.meet_view_pager);
    }

    /*
    public void initData() {
        //获取标签数据
        String[] titles = getResources().getStringArray(R.array.meet_tab_items);

        //创建一个viewpager的adapter
        FragmentAdapter adapter = new FragmentAdapter(getFragmentManager(), Arrays.asList(titles));
        this.meet_view_pager.setAdapter(adapter);

        //将TabLayout和ViewPager关联起来
        this.meet_tab_layout.setupWithViewPager(this.meet_view_pager);
    }
    */
}
