package com.hetang.main;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hetang.adapter.MeetFragmentAdapter;
import com.hetang.R;
import com.hetang.util.BaseFragment;
import com.hetang.util.FontManager;
import com.hetang.util.InvitationDialogFragment;
import com.hetang.util.ParseUtils;
import com.hetang.util.Slog;

/**
 * Created by super-zou on 17-9-11.
 */
public class MeetFragment extends BaseFragment {
    private static final String TAG = "MeetFragment";
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private MeetFragmentAdapter mFragmentAdapter;
    private TabLayout.Tab recomend_tab;
    private TabLayout.Tab dynamics_tab;
    private TabLayout.Tab singleGroup_tab;
    private TabLayout.Tab discovery_tab;


    private String[] mMeetTitleList = getResources().getStringArray(R.array.meet_tabs);
    
    @Override
    protected void initView(View view) {

    }

    @Override
    protected void loadData() {

    }

    @Override
    protected int getLayoutId() {
        return 0;
    }
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewContent = inflater.inflate(R.layout.fragment_meet, container, false);
        initConentView(viewContent);
        return viewContent;
    }

    public void initConentView(View view) {
        mTabLayout = (TabLayout) view.findViewById(R.id.meet_tab_layout);
        mViewPager = (ViewPager) view.findViewById(R.id.meet_view_pager);
        //获取标签数据
        recomend_tab = mTabLayout.newTab().setText(mMeetTitleList[0]);
        singleGroup_tab = mTabLayout.newTab().setText(mMeetTitleList[1]);
        dynamics_tab = mTabLayout.newTab().setText(mMeetTitleList[2]);
        //discovery_tab = mTabLayout.newTab().setText(mMeetTitleList[3]);
        
        //添加tab
        mTabLayout.addTab(recomend_tab, 0, true);
        mTabLayout.addTab(singleGroup_tab, 1, false);
        mTabLayout.addTab(dynamics_tab, 2, false);
        //mTabLayout.addTab(discovery_tab, 3, false);

        //创建一个viewpager的adapter
        mFragmentAdapter = new MeetFragmentAdapter(getFragmentManager(), mMeetTitleList);
        mViewPager.setAdapter(mFragmentAdapter);
        //mViewPager.setCurrentItem(0);
        mViewPager.setOffscreenPageLimit(4);

        //将TabLayout和ViewPager关联起来
        mTabLayout.setupWithViewPager(mViewPager);
        
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                TextView textView = new TextView(getActivity());
                float selectedSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 18, getResources().getDisplayMetrics());
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,selectedSize);
                textView.setText(tab.getText());
                textView.setTextColor(getResources().getColor(R.color.white));
                tab.setCustomView(textView);
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.setCustomView(null);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        
        TabLayout.Tab tab = mTabLayout.getTabAt(0);
        TextView textView = new TextView(getActivity());
        float selectedSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 18, getResources().getDisplayMetrics());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,selectedSize);
        textView.setText(tab.getText());
        textView.setTextColor(getResources().getColor(R.color.white));
        tab.setCustomView(textView);

        TextView search = view.findViewById(R.id.search);
        
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSearchUserDialog();
            }
        });

        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(view.findViewById(R.id.search), font);
    }
    
    private void startSearchUserDialog(){
        InvitationDialogFragment invitationDialogFragment = new InvitationDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("type", ParseUtils.TYPE_COMMON_SEARCH);
        invitationDialogFragment.setArguments(bundle);
        invitationDialogFragment.show(getFragmentManager(), "InvitationDialogFragment");
    }

}
