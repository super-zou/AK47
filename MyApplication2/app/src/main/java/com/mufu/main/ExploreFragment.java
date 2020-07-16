package com.mufu.main;

import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mufu.adapter.ExploreFragmentAdapter;
import com.mufu.R;
import com.mufu.util.BaseFragment;
import com.mufu.util.FontManager;
import com.mufu.common.InvitationDialogFragment;
import com.mufu.util.ParseUtils;

public class ExploreFragment extends BaseFragment {
    private static final String TAG = "ExploreFragment";
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ExploreFragmentAdapter mFragmentAdapter;
    private TabLayout.Tab recomend_tab;
    private TabLayout.Tab share_tab;
    //private TabLayout.Tab rootGroup_tab;
    private TabLayout.Tab discovery_tab;


    private String[] mExploreTitleList = getResources().getStringArray(R.array.explore_subtabs);
    
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
        View viewContent = inflater.inflate(R.layout.fragment_explore, container, false);
        initConentView(viewContent);
        return viewContent;
    }
    public void initConentView(View view) {
        mTabLayout = (TabLayout) view.findViewById(R.id.meet_tab_layout);
        mViewPager = (ViewPager) view.findViewById(R.id.meet_view_pager);
        //获取标签数据
        recomend_tab = mTabLayout.newTab().setText(mExploreTitleList[0]);
        //rootGroup_tab = mTabLayout.newTab().setText(mExploreTitleList[1]);
        share_tab = mTabLayout.newTab().setText(mExploreTitleList[1]);
        discovery_tab = mTabLayout.newTab().setText(mExploreTitleList[2]);
        
        //添加tab
        mTabLayout.addTab(recomend_tab, 0, true);
        //mTabLayout.addTab(rootGroup_tab, 1, false);
        mTabLayout.addTab(share_tab, 1, false);
        mTabLayout.addTab(discovery_tab, 2, false);
        
        //创建一个viewpager的adapter
        mFragmentAdapter = new ExploreFragmentAdapter(getFragmentManager(), mExploreTitleList);
        mViewPager.setAdapter(mFragmentAdapter);
        //mViewPager.setCurrentItem(0);
        mViewPager.setOffscreenPageLimit(2);

        //将TabLayout和ViewPager关联起来
        mTabLayout.setupWithViewPager(mViewPager);
        
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                TextView textView = new TextView(getActivity());
                float selectedSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 18, getResources().getDisplayMetrics());
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,selectedSize);
                textView.setText(tab.getText());
                textView.setTextColor(getResources().getColor(R.color.blue_dark));
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
        textView.setTextColor(getResources().getColor(R.color.blue_dark));
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
