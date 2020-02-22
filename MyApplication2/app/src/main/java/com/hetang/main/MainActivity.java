package com.hetang.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.hetang.R;
import com.hetang.adapter.MainFragmentAdapter;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.common.MyApplication;
import com.hetang.common.ReminderManager;
import com.hetang.home.HomeFragment;
import com.hetang.update.UpdateParser;
import com.hetang.util.BaseFragment;
import com.hetang.util.CommonDialogFragmentInterface;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.SharedPreferencesUtils;
import com.hetang.util.Slog;
import com.hetang.util.UserProfile;
import com.hetang.util.Utility;
import com.tencent.imsdk.TIMCallBack;
import com.tencent.imsdk.TIMFriendshipManager;
import com.tencent.imsdk.TIMManager;
import com.tencent.imsdk.TIMUserProfile;
import com.tencent.qcloud.tim.uikit.TUIKit;
import com.tencent.qcloud.tim.uikit.base.IUIKitCallBack;
import com.tencent.qcloud.tim.uikit.modules.conversation.ConversationManagerKit;
import com.tencent.qcloud.tim.uikit.utils.ToastUtil;
import com.xuexiang.xupdate.XUpdate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hetang.util.ParseUtils.DEFAULT_FEMALE_AVATAR_URL;
import static com.hetang.util.ParseUtils.DEFAULT_MALE_AVATAR_URL;
import static com.hetang.util.ParseUtils.getUserProfile;
import static com.hetang.util.ParseUtils.startMeetArchiveActivity;
import static com.hetang.util.SharedPreferencesUtils.getTuiKitAutoLogin;
import static com.hetang.util.SharedPreferencesUtils.getUid;
import static com.hetang.util.SharedPreferencesUtils.setTuiKitAutoLogin;

public class MainActivity extends BaseAppCompatActivity implements CommonDialogFragmentInterface, ReminderManager.UnreadNumChangedCallback , ConversationManagerKit.MessageUnreadWatcher{

    public static final int HAVA_NEW_VERSION = 2;
    private static final String TAG = "MainActivity";
    private final static boolean isDebug = true;
    private final static boolean isNimDebug = false;
    private final static boolean isUpdateDebug = false;
    private static final int START_MEET_ARCHIVE_ACTIVITY = 2;
    private static final int START_ARCHIVE_ACTIVITY = 3;
    private static final int REQUEST_CODE_INSTALL_PERMISSION = 107;
    private static MyHandler handler;
    private static String tuikitId;
    TabLayout.Tab home_tab;
    TabLayout.Tab meet_tab;
    TabLayout.Tab message_tab;
    TabLayout.Tab contacts_tab;
    TabLayout.Tab me_tab;
    TextView unReadView;
    TextView contactsAppliedNoticeView;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private MainFragmentAdapter mFragmentAdapter;
    private List<Fragment> mFragmentList = new ArrayList<>();
    private boolean hasUnreadMessage = false;
    private boolean hasUnreadSessions = false;
    private String[] mTitles = MyApplication.getContext().getResources().getStringArray(R.array.main_tabs);

    private int[] mIcons = {R.string.home, R.string.fa_magnet, R.string.message, R.string.contacts, R.string.me};

    private static final String GET_USERSIG = HttpUtil.DOMAIN + "?q=chat/get_userSig";
    private TIMManager timManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            setIntent(new Intent());
        }

        init();

        checkUpdate();

        getUserSig(this);

        timManager = TIMManager.getInstance();

        onPreView();

    }


    //check interval 24 hours
    private void checkUpdate() {
        long last = SharedPreferencesUtils.getUpdateCheckTimeStamp(this);
        long current = System.currentTimeMillis();
        long interval = (current - last) / (1000 * 60 * 60);//get hour
        if (isUpdateDebug)
            Slog.d(TAG, "----------------------->interval: " + interval + " last: " + String.valueOf(last) + " current: " + String.valueOf(current));
        if (interval >= 24) {
            XUpdate.newBuild(this)
                    .updateUrl(HttpUtil.CHECK_VERSION_UPDATE)
                    .updateParser(new UpdateParser(this, false))
                    .supportBackgroundUpdate(true)
                    .themeColor(getResources().getColor(R.color.background))
                    .update();

            SharedPreferencesUtils.setUpdateCheckTimeStamp(this, current);
        }
    }

    private void init() {
        handler = new MyHandler(this);
        initMessage();
        initView();
    }

    private void initView() {
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");

        mTabLayout = findViewById(R.id.tabs);
        mViewPager = findViewById(R.id.viewpager);

        //获取标签数据
        home_tab = mTabLayout.newTab();
        meet_tab = mTabLayout.newTab();
        contacts_tab = mTabLayout.newTab();
        message_tab = mTabLayout.newTab();
        me_tab = mTabLayout.newTab();
        //添加tab
        mTabLayout.addTab(home_tab);
        mTabLayout.addTab(meet_tab, true);
        mTabLayout.addTab(message_tab);
        mTabLayout.addTab(contacts_tab);
        mTabLayout.addTab(me_tab);

        BaseFragment home = new HomeFragment();
        mFragmentList.add(home);
        BaseFragment meet = new MeetFragment();
        mFragmentList.add(meet);
        Fragment message = new MessageFragment();
        mFragmentList.add(message);
        Fragment contacts = new ContactsFragment();
        mFragmentList.add(contacts);
        //BaseFragment me = new ArchiveFragment();
        BaseFragment me = new MeetArchiveFragment();
        mFragmentList.add(me);

        //创建一个viewpager的adapter
        mFragmentAdapter = new MainFragmentAdapter(getSupportFragmentManager(), mFragmentList, mTitles);
        mViewPager.setAdapter(mFragmentAdapter);
        mViewPager.setOffscreenPageLimit(3);

        //将TabLayout和ViewPager关联起来
        mTabLayout.setupWithViewPager(mViewPager);
        //index 0 selected by default

        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            tab.setCustomView(R.layout.tab_main_custom_item);

            TextView tabIcon = tab.getCustomView().findViewById(R.id.tab_icon);
            TextView tabText = tab.getCustomView().findViewById(R.id.tab_text);
            tabIcon.setText(mIcons[i]);
            tabText.setText(mTitles[i]);

            if (i == 1) {
                tabText.setTextColor(getResources().getColor(R.color.blue_dark));
                tabIcon.setTextColor(getResources().getColor(R.color.blue_dark));
                tab.select();
            }

        }


        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                TextView tabIcon = tab.getCustomView().findViewById(R.id.tab_icon);
                TextView tabText = tab.getCustomView().findViewById(R.id.tab_text);
                tabText.setTextColor(getResources().getColor(R.color.blue_dark));
                tabIcon.setTextColor(getResources().getColor(R.color.blue_dark));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                TextView tabIcon = tab.getCustomView().findViewById(R.id.tab_icon);
                TextView tabText = tab.getCustomView().findViewById(R.id.tab_text);
                tabText.setTextColor(getResources().getColor(R.color.text_default));
                tabIcon.setTextColor(getResources().getColor(R.color.text_default));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // 未读消息监视器
        ConversationManagerKit.getInstance().addUnreadWatcher(this);

        unReadView = mTabLayout.getTabAt(2).getCustomView().findViewById(R.id.unread);
        contactsAppliedNoticeView = mTabLayout.getTabAt(3).getCustomView().findViewById(R.id.unread);
        FontManager.markAsIconContainer(findViewById(R.id.tabs), font);
    }

    private void initMessage() {
        initSessionMessageObserver();
        MessageFragment.getUnreadNotification(handler);
    }

    //未读消息数量观察者实现
    @Override
    public void onUnreadNumChanged(int unReadCount) {
        if (isDebug) Slog.d(TAG, "------------------------->onUnreadNumChanged: " + unReadCount);
        if (unReadCount > 0) {
            hasUnreadSessions = true;
            if (unReadView.getVisibility() == View.GONE) {
                unReadView.setVisibility(View.VISIBLE);
            }
        } else {
            hasUnreadSessions = false;
            if (hasUnreadMessage == false) {
                if (unReadView.getVisibility() != View.GONE) {
                    unReadView.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void updateUnread(int unReadCount) {
        if (isDebug) Slog.d(TAG, "------------------------->updateUnread: " + unReadCount);
        if (unReadCount > 0) {
            hasUnreadSessions = true;
            if (unReadView.getVisibility() == View.GONE) {
                unReadView.setVisibility(View.VISIBLE);
            }
        } else {
            hasUnreadSessions = false;
            if (hasUnreadMessage == false) {
                if (unReadView.getVisibility() != View.GONE) {
                    unReadView.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void onNotificationUnreadChanged(int unReadCount) {
        if (unReadCount > 0) {
            if (unReadView.getVisibility() == View.GONE) {
                unReadView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onNewContactsApplied(int appliedCount) {
        Slog.d(TAG, "------------------->onNewContactsApplied: " + appliedCount);
        if (appliedCount > 0) {
            if (contactsAppliedNoticeView.getVisibility() == View.GONE) {
                contactsAppliedNoticeView.setVisibility(View.VISIBLE);
            }
        } else {
            if (contactsAppliedNoticeView.getVisibility() == View.VISIBLE) {
                contactsAppliedNoticeView.setVisibility(View.GONE);
            }
        }
    }



    private void initSessionMessageObserver() {
        //registerMsgUnreadInfoObserver(true);
        registerMsgUnreadInfoObserver(true);
        //registerSystemMessageObservers(true);
    }

    /**
     * 注册未读消息数量观察者
     */
    private void registerMsgUnreadInfoObserver(boolean register) {
        if (register) {
            if (isNimDebug) Slog.d(TAG, "----------->register");
            ReminderManager.getInstance().registerUnreadNumChangedCallback(this);
        } else {
            if (isNimDebug) Slog.d(TAG, "----------->unregister");
            ReminderManager.getInstance().unregisterUnreadNumChangedCallback(this);
        }
    }

    @Override
    public void onBackFromDialog(int type, int result, boolean status) {
    }

    public void getUserSig(final Context context){
        final RequestBody requestBody = new FormBody.Builder().build();
        HttpUtil.sendOkHttpRequest(context, GET_USERSIG, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    Slog.d(TAG, "==========getUserSig response text : " + responseText);
                    try {
                        String sig = new JSONObject(responseText).optString("sig");
                        Slog.d(TAG, "-------------------->uid: "+getUid(context));
                        tuikitId = String.valueOf(getUid(context));
                        boolean autoLogin = getTuiKitAutoLogin(context);
                        Slog.d(TAG, "------------------>tuikit auto login: "+autoLogin);
                        if (autoLogin){
                            autoLogin(context, sig);
                        }else {
                            login(context, sig);
                        }

                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }

    private void autoLogin(final Context context, final String sig){
        timManager.autoLogin(tuikitId, new TIMCallBack() {
            @Override
            public void onError(int i, String s) {
                ToastUtil.toastLongMessage("登录失败, errCode = " + i + ", errInfo = " + s);
                login(context, sig);
            }
            @Override
            public void onSuccess() {
                Slog.d(TAG, "-------------------------->tuikit autoLogin success with account: "+tuikitId);
                String userId = timManager.getLoginUser();
                Slog.d(TAG, "---------------------->login success with user id: "+userId);
                queryProfile(userId);
            }
        });
    }

    private void login(final Context context, String sig){
        TUIKit.login(tuikitId, sig, new IUIKitCallBack() {
            @Override
            public void onError(String module, final int code, final String desc) {
                ToastUtil.toastLongMessage("登录失败, errCode = " + code + ", errInfo = " + desc);
            }

            @Override
            public void onSuccess(Object data) {
                Slog.d(TAG, "-------------------------->tuikit login success: ");
                setTuiKitAutoLogin(context, true);
                String userId = timManager.getLoginUser();
                Slog.d(TAG, "---------------------->login success with user id: "+userId);
                queryProfile(userId);
            }
        });
    }

    public static void queryProfile(String id){
        TIMUserProfile profile = TIMFriendshipManager.getInstance().queryUserProfile(id);
        if (profile != null){
            if (TextUtils.isEmpty(profile.getFaceUrl()) || TextUtils.isEmpty(profile.getNickName())){
                setTuiKitProfile();
            }
        }else {
            setTuiKitProfile();
        }
    }

    public static void setTuiKitProfile(){
        Runnable loginRunnable = new Runnable() {
            @Override
            public void run() {
                UserProfile userProfile = getUserProfile(-1);
                if (userProfile != null){
                    updateProfile(userProfile);
                }
            }
        };

        Thread loginThread = new Thread(loginRunnable);
        loginThread.start();
    }


    public static void updateProfile(UserProfile userProfile) {
        String avatarUrl;
        if (!TextUtils.isEmpty(userProfile.getAvatar())){
            avatarUrl = HttpUtil.getDomain() + userProfile.getAvatar();
        }else {
            if (userProfile.getSex() == 0){
                avatarUrl = DEFAULT_MALE_AVATAR_URL;
            }else {
                avatarUrl = DEFAULT_FEMALE_AVATAR_URL;
            }
        }

        //String mIconUrl = String.format("https://picsum.photos/id/%d/200/200", new Random().nextInt(1000));
        HashMap<String, Object> hashMap = new HashMap<>();

        // 头像
        if (!TextUtils.isEmpty(avatarUrl)) {
            hashMap.put(TIMUserProfile.TIM_PROFILE_TYPE_KEY_FACEURL, avatarUrl);
        }

        hashMap.put(TIMUserProfile.TIM_PROFILE_TYPE_KEY_NICK, userProfile.getNickName());

        TIMFriendshipManager.getInstance().modifySelfProfile(hashMap, new TIMCallBack() {
            @Override
            public void onError(int i, String s) {
                ToastUtil.toastShortMessage("Error code = " + i + ", desc = " + s);
            }
            @Override
            public void onSuccess() {
                Slog.d(TAG, "---------------->modifySelfProfile success");
            }
        });
    }

    private int getHeight(){
        //AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        return mTabLayout.getHeight();
    }

    private void onPreView() {
        getWindow().getDecorView().getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                int height = getHeight();
                setHeight(height);
                getWindow().getDecorView().getViewTreeObserver().removeOnPreDrawListener(this);
                return false;
            }
        });
    }

    private void setHeight(int height){
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams)mViewPager.getLayoutParams();
        //layoutParams.setMargins(0,0,0, (int)Utility.dpToPx(getApplicationContext(), height));
        //mViewPager.setPadding(0, 0, 0, (int)Utility.dpToPx(getApplicationContext(), height));
        mViewPager.setPadding(0, 0, 0, height);
        mViewPager.setLayoutParams(layoutParams);
    }

    public void handleMessage(Message message) {
        TextView unRead = mTabLayout.getTabAt(2).getCustomView().findViewById(R.id.unread);
        Bundle bundle = message.getData();
        int uid = 0;
        if (bundle != null) {
            uid = bundle.getInt("uid");
        }
        switch (message.what) {
            case MessageFragment.HAVE_UNREAD_MESSAGE:
                hasUnreadMessage = true;
                if (unRead.getVisibility() == View.GONE) {
                    unRead.setVisibility(View.VISIBLE);
                }
                break;
            case MessageFragment.HAVE_NO_UNREAD_MESSAGE:
                hasUnreadMessage = false;
                if (hasUnreadSessions == false && unRead.getVisibility() != View.GONE) {
                    unRead.setVisibility(View.GONE);
                }
                break;
            case START_MEET_ARCHIVE_ACTIVITY:
                startMeetArchiveActivity(this, uid);
                break;
            default:
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initMessage();
        // getUnreadNotification(handler);
        // initSessionMessageObserver();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        registerMsgUnreadInfoObserver(false);
    }

    static class MyHandler extends Handler {
        WeakReference<MainActivity> mainActivityWeakReference;

        MyHandler(MainActivity mainActivity) {
            mainActivityWeakReference = new WeakReference<>(mainActivity);
        }

        @Override
        public void handleMessage(Message message) {
            MainActivity mainActivity = mainActivityWeakReference.get();
            if (mainActivity != null) {
                mainActivity.handleMessage(message);
            }
        }
    }
}
