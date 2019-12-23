package com.hetang.common;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.os.Process;
import android.support.text.emoji.EmojiCompat;
import android.support.text.emoji.bundled.BundledEmojiCompatConfig;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.hetang.util.Slog;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.netease.nimlib.sdk.util.NIMUtil;
import com.hetang.R;
import com.hetang.main.MainActivity;
import com.hetang.update.OkHttpUpdateService;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xuexiang.xupdate.XUpdate;
import com.xuexiang.xupdate.entity.UpdateError;
import com.xuexiang.xupdate.listener.OnUpdateFailureListener;
import com.xuexiang.xupdate.utils.UpdateUtils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;


import static com.hetang.util.ParseUtils.startMeetArchiveActivity;
import static com.netease.nimlib.sdk.StatusCode.LOGINED;
import static com.hetang.util.SharedPreferencesUtils.getYunXinAccount;
import static com.hetang.util.SharedPreferencesUtils.getYunXinToken;
import static com.hetang.util.SharedPreferencesUtils.setYunXinAccount;
import static com.hetang.util.SharedPreferencesUtils.setYunXinToken;
import static com.xuexiang.xupdate.entity.UpdateError.ERROR.CHECK_NO_NEW_VERSION;

/**
 * Created by super-zou on 18-1-20.
 */

public class MyApplication extends Application {
    //+Begin added by xuchunping for MI push
    private static final String TAG = "MyApplication";
    public static final String APP_ID = "2882303761517916663";
    public static final String APP_KEY = "5261791687663";
    //-End added by xuchunping for MI push

    //umeng
    private static final String UMENG_APPKEY = "5de7c0350cafb2d526000a93";

    private static Context mContext;
    private Handler handler;

    public static Context getContext() {
        return mContext;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        NIMClient.config(mContext, loginInfo(), options());
        // 以下逻辑只在主进程初始化时执行
        if (NIMUtil.isMainProcess(this)) {
            NIMClient.initSDK();
            NimUIKit.init(mContext);
            
            final String token = getYunXinToken(MyApplication.getContext());
            String account = getYunXinAccount(MyApplication.getContext());
            
            if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(token)){
                if (NIMClient.getStatus() != LOGINED){//need to do login manually
                    NimUIKit.login(new LoginInfo(account, token), new RequestCallback<LoginInfo>() {
                        @Override
                        public void onSuccess(LoginInfo param) {
                            Slog.d(TAG, "---------->uikit login success");
                            setYunXinAccount(MyApplication.getContext(), param.getAccount());
                            if(!token.equals(param.getToken())){
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
                            Toast.makeText(MyApplication.getContext(), "yunxin login error", Toast.LENGTH_LONG).show();
                        }
                    });
                    
                    }
            }
        }

        //+Begin added by xuchunping for MI push
        if(shouldInit()) {
            MiPushClient.registerPush(this, APP_ID, APP_KEY);
        }
        
        LoggerInterface newLogger = new LoggerInterface() {
            @Override
            public void setTag(String tag) {
                // ignore
            }
            @Override
            public void log(String content, Throwable t) {
                Log.d(TAG, content, t);
            }
            @Override
            public void log(String content) {
                Log.d(TAG, content);
            }
        };
        Logger.setLogger(this, newLogger);
        //-End added by xuchunping for MI push
        
        //+Init XUpdate config
        XUpdate.get().debug(true)
                .isWifiOnly(false)                                               //默认设置只在wifi下检查版本更新
                .isGet(false)                                                    //默认设置使用get请求检查版本
                .isAutoMode(false)                                              //默认设置非自动模式，可根据具体使用配置
                .param("versionCode", UpdateUtils.getVersionCode(this))         //设置默认公共请求参数
                .param("appKey", getPackageName())
                .setOnUpdateFailureListener(new OnUpdateFailureListener() {     //设置版本更新出错的监听
                    @Override
                    public void onFailure(UpdateError error) {
                        if (error.getCode() != CHECK_NO_NEW_VERSION) {          //对不同错误进行处理
                            Slog.d(TAG, "---------------------->XUpdate update failure: "+error.toString());
                        }
                    }
                })
                .supportSilentInstall(true)
                .setIUpdateHttpService(new OkHttpUpdateService())
                .init(this);

        closeAndroidPDialog();

        EmojiCompat.Config config = new BundledEmojiCompatConfig(this);
        EmojiCompat.init(config);

        //umeng
        UMConfigure.init(this, UMENG_APPKEY, "Umeng", UMConfigure.DEVICE_TYPE_PHONE, null);
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.LEGACY_AUTO);
    }
    
    //+Begin added by xuchunping for MI push
    private boolean shouldInit() {
        ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = getPackageName();
        int myPid = Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }
    //-End added by xuchunping for MI push
    
    // 如果已经存在用户登录信息，返回LoginInfo，否则返回null即可
    private LoginInfo loginInfo() {
        String account = getYunXinAccount(MyApplication.getContext());
        String token = getYunXinToken(MyApplication.getContext());
        Slog.d(TAG, "#################account: "+account+"    token: "+token);
        Slog.d(TAG, "#########################login status: "+NIMClient.getStatus());

        if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(token)){
            if (NIMClient.getStatus() != LOGINED){
                return null;
            }else {
                return new LoginInfo(account, token);
            }
        }else {
            return null;
        }
    }
    
    // 如果返回值为 null，则全部使用默认参数。
    private SDKOptions options() {
        SDKOptions options = new SDKOptions();

        // 如果将新消息通知提醒托管给 SDK 完成，需要添加以下配置。否则无需设置。
        StatusBarNotificationConfig config = new StatusBarNotificationConfig();
        config.notificationEntrance = MainActivity.class; // 点击通知栏跳转到该Activity
        config.notificationSmallIconId = R.drawable.icon;
        // 呼吸灯配置
        config.ledARGB = Color.GREEN;
        config.ledOnMs = 1000;
        config.ledOffMs = 1500;
        // 通知铃声的uri字符串
        config.notificationSound = "android.resource://com.netease.nim.demo/raw/msg";
        options.statusBarNotificationConfig = config;
        
        // 配置保存图片，文件，log 等数据的目录
        // 如果 options 中没有设置这个值，SDK 会使用采用默认路径作为 SDK 的数据目录。
        // 该目录目前包含 log, file, image, audio, video, thumb 这6个目录。
        String sdkPath = getAppCacheDir(MyApplication.getContext()) + "/nim"; // 可以不设置，那么将采用默认路径
        // 如果第三方 APP 需要缓存清理功能， 清理这个目录下面个子目录的内容即可。
        options.sdkStorageRootPath = sdkPath;
        // 配置是否需要预下载附件缩略图，默认为 true
        options.preloadAttach = true;
        // 配置附件缩略图的尺寸大小。表示向服务器请求缩略图文件的大小
        // 该值一般应根据屏幕尺寸来确定， 默认值为 Screen.width / 2
        //options.thumbnailSize = ${Screen.width} / 2;
        
        options.appKey="b01e16469d08abf1423a74537a13ca1b";
        // 用户资料提供者, 目前主要用于提供用户资料，用于新消息通知栏中显示消息来源的头像和昵称
        options.userInfoProvider = new UserInfoProvider() {
            @Override
            public UserInfo getUserInfo(String account) {
                return null;
            }

            @Override
            public Bitmap getAvatarForMessageNotifier(SessionTypeEnum sessionTypeEnum, String account) {
                Resources res = getResources();
                Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.male_default_avator);
                return bmp;
            }
            
            @Override
            public String getDisplayNameForMessageNotifier(String account, String sessionId,
                                                           SessionTypeEnum sessionType) {
                return null;
            }
        };
        return options;
    }

    /**
     * 配置 APP 保存图片/语音/文件/log等数据的目录
     * 这里示例用SD卡的应用扩展存储目录
     */
     
     static String getAppCacheDir(Context context) {
        String storageRootPath = null;
        try {
            // SD卡应用扩展存储区(APP卸载后，该目录下被清除，用户也可以在设置界面中手动清除)，请根据APP对数据缓存的重要性及生命周期来决定是否采用此缓存目录.
            // 该存储区在API 19以上不需要写权限，即可配置 <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="18"/>
            if (context.getExternalCacheDir() != null) {
                storageRootPath = context.getExternalCacheDir().getCanonicalPath();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(storageRootPath)) {
            // SD卡应用公共存储区(APP卸载后，该目录不会被清除，下载安装APP后，缓存数据依然可以被加载。SDK默认使用此目录)，该存储区域需要写权限!
            storageRootPath = Environment.getExternalStorageDirectory() + "/" + getContext().getPackageName();
        }

        return storageRootPath;
    }
    
    private void closeAndroidPDialog(){
        try {
            Class aClass = Class.forName("android.content.pm.PackageParser$Package");
            Constructor declaredConstructor = aClass.getDeclaredConstructor(String.class);
            declaredConstructor.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            Class cls = Class.forName("android.app.ActivityThread");
            Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");
            declaredMethod.setAccessible(true);
            Object activityThread = declaredMethod.invoke(null);
            Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
            mHiddenApiWarningShown.setAccessible(true);
            mHiddenApiWarningShown.setBoolean(activityThread, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
                
