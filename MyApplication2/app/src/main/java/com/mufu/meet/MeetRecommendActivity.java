package com.mufu.meet;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.mufu.R;
import com.mufu.adapter.MeetFragmentAdapter;
import com.mufu.common.BaseAppCompatActivity;
import com.mufu.util.FontManager;

import static com.mufu.common.MyApplication.getContext;

public class MeetRecommendActivity extends BaseAppCompatActivity {
    private static final boolean isDebug = true;
    private static final String TAG = "MeetRecommendFragment";
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private MeetFragmentAdapter mFragmentAdapter;
    private TabLayout.Tab meet_tab;
    private TabLayout.Tab eden_garden_tab;
    
    private String[] mMeetTitleList = getContext().getResources().getStringArray(R.array.meet_subtabs);
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_meet);
        initConentView();
    }
    
    public void initConentView() {
        mTabLayout = (TabLayout) findViewById(R.id.meet_tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.meet_view_pager);
        //获取标签数据
        meet_tab = mTabLayout.newTab().setText(mMeetTitleList[0]);
        eden_garden_tab = mTabLayout.newTab().setText(mMeetTitleList[1]);


        //添加tab
        mTabLayout.addTab(meet_tab, 0, true);
        mTabLayout.addTab(eden_garden_tab, 1, false);
        //创建一个viewpager的adapter
        mFragmentAdapter = new MeetFragmentAdapter(getSupportFragmentManager(), mMeetTitleList);
        mViewPager.setAdapter(mFragmentAdapter);
        //mViewPager.setCurrentItem(0);
        mViewPager.setOffscreenPageLimit(2);

        //将TabLayout和ViewPager关联起来
        mTabLayout.setupWithViewPager(mViewPager);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                TextView textView = new TextView(getContext());
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
        TextView textView = new TextView(getContext());
        float selectedSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 18, getResources().getDisplayMetrics());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,selectedSize);
        textView.setText(tab.getText());
        textView.setTextColor(getResources().getColor(R.color.blue_dark));
        tab.setCustomView(textView);
        
        TextView backTV = findViewById(R.id.back);

        backTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.back), font);
    }


}

