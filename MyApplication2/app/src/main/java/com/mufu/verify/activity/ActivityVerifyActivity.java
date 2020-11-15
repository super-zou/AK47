package com.mufu.verify.activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.mufu.R;
import com.mufu.adapter.verify.VerifyFragmentAdapter;
import com.mufu.util.HttpUtil;
import com.mufu.verify.talent.TalentPassedFragment;
import com.mufu.verify.talent.TalentRejectedFragment;
import com.mufu.verify.talent.TalentRequestFragment;
import com.mufu.common.BaseAppCompatActivity;
import com.mufu.common.MyApplication;
import com.mufu.main.MessageFragment;
import com.mufu.util.BaseFragment;
import com.mufu.util.FontManager;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.mufu.verify.user.UserVerifyActivity.GET_ALL_STATUS_COUNT_DONE;

public class ActivityVerifyActivity extends BaseAppCompatActivity {

    private static final String TAG = "ActivityVerifyActivity";
    private final static boolean isDebug = true;
    TabLayout.Tab unverified_tab;
    TabLayout.Tab verified_tab;
    TabLayout.Tab rejected_tab;

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private VerifyFragmentAdapter mFragmentAdapter;
    private List<Fragment> mFragmentList = new ArrayList<>();

    private static MyHandler handler;
        TextView requestCountView;
    TextView passedCountView;
    TextView rejectedCountView;

    private int requestCount = 0;
    private int passedCount = 0;
    private int rejectedCount = 0;

    public static final int PASS_ACTION = 0;
    public static final int REJECT_ACTION = 1;

    private String[] mTitles = MyApplication.getContext().getResources().getStringArray(R.array.verify_tabs);
        public static final String GET_EXPERIENCE_VERIFY_AMOUNT_URL = HttpUtil.DOMAIN + "?q=experience/get_experience_verify_amount";
    public static final String SET_EXPERIENCE_PASSED = HttpUtil.DOMAIN + "?q=verify/set_experience_passed";
    public static final String SET_EXPERIENCE_REJECTED = HttpUtil.DOMAIN + "?q=verify/set_experience_rejected";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_user_verify);
        handler = new MyHandler(this);
        init();
    }

    private void init() {
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");

        mTabLayout = findViewById(R.id.tabs);
        mViewPager = findViewById(R.id.viewpager);

        //获取标签数据
        unverified_tab = mTabLayout.newTab();
        verified_tab = mTabLayout.newTab();
        rejected_tab = mTabLayout.newTab();
        //添加tab
        mTabLayout.addTab(unverified_tab,true);
        mTabLayout.addTab(verified_tab);
        mTabLayout.addTab(rejected_tab);
        
        Fragment unVerifiedFragment = new ActivityRequestFragment();
        mFragmentList.add(unVerifiedFragment);
        BaseFragment passedFragment = new ActivityPassedFragment();
        mFragmentList.add(passedFragment);
        Fragment rejectedFragment = new ActivityRejectedFragment();
        mFragmentList.add(rejectedFragment);

        //创建一个viewpager的adapter
        mFragmentAdapter = new VerifyFragmentAdapter(getSupportFragmentManager(), mFragmentList, mTitles);
        mViewPager.setAdapter(mFragmentAdapter);
        mViewPager.setOffscreenPageLimit(3);

        //将TabLayout和ViewPager关联起来
        mTabLayout.setupWithViewPager(mViewPager);
        //index 0 selected by default
        
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            tab.setCustomView(R.layout.tab_authentication_custom);

            TextView count = tab.getCustomView().findViewById(R.id.count);
            TextView tabText = tab.getCustomView().findViewById(R.id.tab_text);
            tabText.setText(mTitles[i]);

            if (i == 0) {
                tabText.setTextColor(getResources().getColor(R.color.blue_dark));
                count.setTextColor(getResources().getColor(R.color.blue_dark));
                tab.select();
            }

        }
        
         mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                TextView count = tab.getCustomView().findViewById(R.id.count);
                TextView tabText = tab.getCustomView().findViewById(R.id.tab_text);
                tabText.setTextColor(getResources().getColor(R.color.blue_dark));
                count.setTextColor(getResources().getColor(R.color.blue_dark));
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                TextView count = tab.getCustomView().findViewById(R.id.count);
                TextView tabText = tab.getCustomView().findViewById(R.id.tab_text);
                tabText.setTextColor(getResources().getColor(R.color.text_default));
                count.setTextColor(getResources().getColor(R.color.text_default));
            }
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        
                requestCountView = mTabLayout.getTabAt(0).getCustomView().findViewById(R.id.count);
        passedCountView = mTabLayout.getTabAt(1).getCustomView().findViewById(R.id.count);
        rejectedCountView = mTabLayout.getTabAt(2).getCustomView().findViewById(R.id.count);

        //unReadView = mTabLayout.getTabAt(2).getCustomView().findViewById(R.id.unread);
        FontManager.markAsIconContainer(findViewById(R.id.tabs), font);
        
        getAllCount();
    }
    
     private void getAllCount(){
        RequestBody requestBody = new FormBody.Builder().build();

        String url = GET_EXPERIENCE_VERIFY_AMOUNT_URL;

        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), url, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (responseText != null) {
                        try {
                            JSONObject responseObject = new JSONObject(responseText);
                            requestCount = responseObject.optInt("requesting_amount");
                            passedCount = responseObject.optInt("passed_amount");
                            rejectedCount = responseObject.optInt("rejected_amount");

                            handler.sendEmptyMessage(GET_ALL_STATUS_COUNT_DONE);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }
    
    public void handleMessage(Message message) {
        switch (message.what) {
            case GET_ALL_STATUS_COUNT_DONE:
                requestCountView.setText(String.valueOf(requestCount));
                passedCountView.setText(String.valueOf(passedCount));
                rejectedCountView.setText(String.valueOf(rejectedCount));
                break;
            default:
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    static class MyHandler extends Handler {
        WeakReference<ActivityVerifyActivity> authenticationActivityWeakReference;
        MyHandler(ActivityVerifyActivity authenticationActivity) {
            authenticationActivityWeakReference = new WeakReference<>(authenticationActivity);
        }

        @Override
        public void handleMessage(Message message) {
            ActivityVerifyActivity authenticationActivity = authenticationActivityWeakReference.get();
            if (authenticationActivity != null) {
                authenticationActivity.handleMessage(message);
            }
        }
    }
}
