package com.hetang.meet;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.hetang.R;
import com.hetang.adapter.ExploreFragmentAdapter;
import com.hetang.adapter.MeetFragmentAdapter;
import com.hetang.adapter.MeetRecommendListAdapter;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.common.HandlerTemp;
import com.hetang.common.MyApplication;
import com.hetang.common.SetAvatarActivity;
import com.hetang.contacts.ContactsApplyListActivity;
import com.hetang.dynamics.DynamicsInteractDetailsActivity;
import com.hetang.group.SubGroupActivity;
import com.hetang.main.MeetArchiveActivity;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.InterActInterface;
import com.hetang.util.ParseUtils;
import com.hetang.util.SharedPreferencesUtils;

import com.hetang.util.Slog;
import com.hetang.util.UserProfile;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.hetang.common.AddPictureActivity.ADD_PICTURE_BROADCAST;
import static com.hetang.common.MyApplication.getContext;
import static com.hetang.common.SetAvatarActivity.AVATAR_SET_ACTION_BROADCAST;
import static com.hetang.dynamics.DynamicsInteractDetailsActivity.MEET_RECOMMEND_COMMENT;
import static com.hetang.explore.ShareFragment.COMMENT_COUNT_UPDATE;
import static com.hetang.explore.ShareFragment.LOVE_UPDATE;
import static com.hetang.explore.ShareFragment.MY_COMMENT_COUNT_UPDATE;
import static com.hetang.explore.ShareFragment.PRAISE_UPDATE;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

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

