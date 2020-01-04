package com.hetang.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.main.LoginSyncDataStatusObserver;
import com.netease.nim.uikit.api.model.session.SessionEventListener;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.xuexiang.xupdate.XUpdate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hetang.common.Chat.createYunXinUser;
import static com.hetang.util.HttpUtil.GET_PASSWORD_HASH;
import static com.hetang.util.HttpUtil.GET_USERINFO_WITH_ACCOUNT;
import static com.hetang.util.HttpUtil.getYunXinAccountExist;
import static com.hetang.util.ParseUtils.startMeetArchiveActivity;
import static com.hetang.util.SharedPreferencesUtils.getYunXinAccount;
import static com.hetang.util.SharedPreferencesUtils.getYunXinToken;
import static com.hetang.util.SharedPreferencesUtils.setYunXinAccount;
import static com.hetang.util.SharedPreferencesUtils.setYunXinToken;

public class MainActivity extends BaseAppCompatActivity implements CommonDialogFragmentInterface, ReminderManager.UnreadNumChangedCallback {

    public static final int HAVA_NEW_VERSION = 2;
    private static final String TAG = "MainActivity";
    private final static boolean isDebug = false;
    private final static boolean isNimDebug = false;
    private final static boolean isUpdateDebug = false;
    private static final int START_MEET_ARCHIVE_ACTIVITY = 2;
    private static final int START_ARCHIVE_ACTIVITY = 3;
    private static final int REQUEST_CODE_INSTALL_PERMISSION = 107;
    private static MyHandler handler;
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

    private int[] mIcons = {R.string.home, R.string.meet, R.string.message, R.string.contacts, R.string.me};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            setIntent(new Intent());
        }

        init();
        //avoid activity to be killed, recall processIntent()， also call it in onNewIntent
        processIntent();
        //isFirstIn = true;
        checkUpdate();

        loginYunXinServer();

        if (isNimDebug) Slog.d(TAG, "#####################login status: " + NIMClient.getStatus());

    }

    private void processIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(NimIntent.EXTRA_NOTIFY_CONTENT)) {
                parseNotifyIntent(intent);
                //finish();
            }
        }
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

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent != null) {
            setIntent(intent);
            processIntent();
        }
    }


    private void parseNotifyIntent(Intent intent) {

        ArrayList<IMMessage> messages = (ArrayList<IMMessage>) intent.getSerializableExtra(NimIntent.EXTRA_NOTIFY_CONTENT);
        if (messages == null || messages.size() > 1) {
            if (isNimDebug) Slog.d(TAG, "######################parseNotifyIntent initView");
            initView();
        } else {
            //showMainActivity(new Intent().putExtra(NimIntent.EXTRA_NOTIFY_CONTENT, messages.get(0)));
            if (isNimDebug) Slog.d(TAG, "######################parseNotifyIntent startP2PSession");
            IMMessage imMessage = messages.get(0);
            if (imMessage.getSessionType() == SessionTypeEnum.P2P) {
                NimUIKit.startP2PSession(MyApplication.getContext(), imMessage.getSessionId());
            }
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

        unReadView = mTabLayout.getTabAt(2).getCustomView().findViewById(R.id.unread);
        contactsAppliedNoticeView = mTabLayout.getTabAt(3).getCustomView().findViewById(R.id.unread);
        FontManager.markAsIconContainer(findViewById(R.id.tabs), font);
    }

    private void initMessage() {
        SessionEventListener listener = new SessionEventListener() {
            @Override
            public void onAvatarClicked(Context context, IMMessage message) {
                // 一般用于打开用户资料页面
                startArchiveActivityWithAccount(MyApplication.getContext(), message.getFromAccount());
            }

            @Override
            public void onAvatarLongClicked(Context context, IMMessage message) {
                // 一般用于群组@功能，或者弹出菜单，做拉黑，加好友等功能
            }

            @Override
            public void onAckMsgClicked(Context context, IMMessage message) {
            }
        };

        NimUIKit.setSessionListener(listener);

        initSessionMessageObserver();
        MessageFragment.getUnreadNotification(handler);
    }

    //the account is user's phone number
    private void startArchiveActivityWithAccount(final Context context, String account) {
        showProgressDialog("");

        final RequestBody requestBody = new FormBody.Builder().add("account", String.valueOf(account)).build();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Response response = HttpUtil.sendOkHttpRequestSync(context, GET_USERINFO_WITH_ACCOUNT, requestBody, null);
                if (response.body() != null) {
                    try {
                        String responseText = response.body().string();
                        int uid = new JSONObject(responseText).optInt("uid");
                        int cid = new JSONObject(responseText).optInt("cid");
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putInt("uid", uid);
                        message.what = START_MEET_ARCHIVE_ACTIVITY;
                        message.setData(bundle);
                        handler.sendMessage(message);
                        dismissProgressDialog();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException i) {
                        i.printStackTrace();
                    }
                }

            }
        };

        Thread startArchiveThread = new Thread(runnable);
        startArchiveThread.start();
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

    private void loginYunXinServer() {
        // final int authorUid = SharedPreferencesUtils.getSessionUid(MyApplication.getContext());
        Runnable loginRunnable = new Runnable() {
            @Override
            public void run() {
                //1.check account registered? only registered account will execute login, not registered will do register when establish chatting
                final String accid = getYunXinAccount(MyApplication.getContext());
                final String token = getYunXinToken(MyApplication.getContext());
                Slog.d(TAG, "------------------------------------>loginYunXinServer with accid: " + accid + "   token: " + token);

                if (!TextUtils.isEmpty(accid) && !TextUtils.isEmpty(token)) {
                    loginYunXin(accid, token);
                } else {
                    int exist = getYunXinAccountExist(MyApplication.getContext(), accid);
                    if (exist > 0) {//yunxin account exist
                        //loginYunXin(account, token);
                        getPassWordHashToLogin(accid);
                    } else {//not existed, need create here
                        if (createYunXinUser(accid) > 0) {
                            getPassWordHashToLogin(accid);
                        }
                    }
                }
            }
        };

        Thread loginThread = new Thread(loginRunnable);
        loginThread.start();
    }

    public void loginYunXin(String account, final String token) {
        NimUIKit.login(new LoginInfo(account, token), new RequestCallback<LoginInfo>() {
            @Override
            public void onSuccess(LoginInfo param) {
                Slog.d(TAG, "---------->uikit login success");
                setYunXinAccount(MyApplication.getContext(), param.getAccount());
                if (!token.equals(param.getToken())) {
                    Slog.d(TAG, "-------->token error, rewrite by LoginInfo  param.getToken()");
                    setYunXinToken(MyApplication.getContext(), param.getToken());
                }
            }

            @Override
            public void onFailed(int code) {

                if (code == 302 || code == 404) {
                    Toast.makeText(MyApplication.getContext(), R.string.login_failed, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MyApplication.getContext(), "云信登录失败: " + code, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onException(Throwable exception) {
                Toast.makeText(MyApplication.getContext(), "云信登录异常", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getPassWordHashToLogin(final String accid) {
        RequestBody requestBody = new FormBody.Builder().build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_PASSWORD_HASH, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "---------------->getPassWordHash response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject loginResponse = new JSONObject(responseText);
                        String passwordHash = loginResponse.getString("password_hash");
                        Slog.d(TAG, "------------------------------------------->passwordHash: " + passwordHash);
                        if (!TextUtils.isEmpty(passwordHash)) {
                            loginYunXin(accid, passwordHash);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }

    private void observerSyncDataComplete() {
        boolean syncCompleted = LoginSyncDataStatusObserver.getInstance().observeSyncDataCompletedEvent(new Observer<Void>() {
            @Override
            public void onEvent(Void v) {
                DialogMaker.dismissProgressDialog();
            }
        });

        //如果数据没有同步完成，弹个进度Dialog
        if (!syncCompleted) {
            //DialogMaker.showProgressDialog(MainActivity.this, getString(R.string.prepare_data)).setCanceledOnTouchOutside(false);
        }
    }

    private void initSessionMessageObserver() {
        //registerMsgUnreadInfoObserver(true);
        observerSyncDataComplete();
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
