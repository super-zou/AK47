package com.hetang.util;

import android.content.Context;
import android.content.SharedPreferences;

/*added by xuchunping 2018.7.21*/
public class SharedPreferencesUtils {
    private static final String ACCOUNT_SHARED_NAME = "account_info";
    private static final String YUNXIN_ACCOUNT_SHARED_NAME = "yunxin_account_info";
    private static final String MEET_RECOMMNED_SHARED_NAME = "meet_recommend";
    private static final String MEET_DYNAMICS_SHARED_NAME = "meet_dynamics";
    private static final String CONCERNED_DYNAMICS_SHARED_NAME = "concerned_dynamics";
    private static final String MEET_DISCOVERY_SHARED_NAME = "meet_discovery";
    private static final String SINGLE_GROUP_SHARED_NAME = "single_group";
    private static final String NOTIFICATION_SHARED_NAME = "notification";
    private static final String ACCOUNT = "account";//account is phone number or email
    private static final String CHECK_UPDATE_TIME = "check_update_time";
    private static final String SESSION = "session";
    private static final String AUTHOR_UID = "uid";
    private static final String SEX = "sex";
    private static final String PASSWORD = "password";
    private static final String PASSWORD_HASH = "password_hash";
    private static final String NAME = "name";
    private static final String TOKEN = "token";
    private static final String YUNXIN_ACCOUNT = "yunxin_account";
    private static final String ACCOUNT_TYPE = "type";
    private static final String LAST_RECOMMNED = "last_recommend";
    private static final String LAST_DYNAMICS = "last_dynamics";
    private static final String LAST_DISCOVERY = "last_discovery";
    private static final String LAST_SINGLE_GROUP = "last_dynamics";
    private static final String TIME_STAMP = "time_stamp";
    private static final String LAST_NOTIFICATION = "last_notification";

    private static SharedPreferences getSharePreferences(Context context, String type) {
        return context.getSharedPreferences(type, Context.MODE_PRIVATE);
    }
    
    //account is user phone
    public static String getAccount(Context context) {
        return getSharePreferences(context, ACCOUNT_SHARED_NAME).getString(ACCOUNT, "");
    }

    public static void setAccount(Context context, String account) {
        getSharePreferences(context, ACCOUNT_SHARED_NAME).edit().putString(ACCOUNT, account).apply();
    }

    public static String getName(Context context) {
        return getSharePreferences(context, ACCOUNT_SHARED_NAME).getString(NAME, "");
    }

    public static void setName(Context context, String name) {
        getSharePreferences(context, ACCOUNT_SHARED_NAME).edit().putString(NAME, name).apply();
    }
    
    public static int getUid(Context context) {
        return getSharePreferences(context, ACCOUNT_SHARED_NAME).getInt(AUTHOR_UID, -1);
    }

    public static void setUid(Context context, int uid) {
        getSharePreferences(context, ACCOUNT_SHARED_NAME).edit().putInt(AUTHOR_UID, uid).apply();
    }

    public static String getPassWord(Context context) {
        return getSharePreferences(context, ACCOUNT_SHARED_NAME).getString(PASSWORD, "");
    }

    public static void setPassWord(Context context, String password) {
        getSharePreferences(context, ACCOUNT_SHARED_NAME).edit().putString(PASSWORD, password).apply();
    }
    
    public static String getPassWordHash(Context context) {
        return getSharePreferences(context, ACCOUNT_SHARED_NAME).getString(PASSWORD_HASH, "");
    }

    public static void setPassWordHash(Context context, String password) {
        getSharePreferences(context, ACCOUNT_SHARED_NAME).edit().putString(PASSWORD_HASH, password).apply();
    }

    public static int getAccountType(Context context) {
        return getSharePreferences(context, ACCOUNT_SHARED_NAME).getInt(ACCOUNT_TYPE, 0);
    }

    public static void setAccountType(Context context, int type) {
        getSharePreferences(context, ACCOUNT_SHARED_NAME).edit().putInt(ACCOUNT_TYPE, type).apply();
    }

    public static String getYunXinToken(Context context) {
        return getSharePreferences(context, ACCOUNT_SHARED_NAME).getString(TOKEN, "");
    }
    public static void setYunXinToken(Context context, String token) {
        getSharePreferences(context, ACCOUNT_SHARED_NAME).edit().putString(TOKEN, token).apply();
    }

    //Begin: for yunxin account
    public static String getYunXinAccount(Context context) {
        return getSharePreferences(context, YUNXIN_ACCOUNT_SHARED_NAME).getString(YUNXIN_ACCOUNT, "");
    }

    public static void setYunXinAccount(Context context, String accid) {//accid is phone number
        if (!accid.equals(getYunXinAccount(context))){
            getSharePreferences(context, YUNXIN_ACCOUNT_SHARED_NAME).edit().putString(YUNXIN_ACCOUNT, accid).apply();
        }
    }
    //End: for yunxin account

    public static String getRecommendLast(Context context) {
        return getSharePreferences(context, MEET_RECOMMNED_SHARED_NAME).getString(LAST_RECOMMNED, "");
    }
    
    public static void setRecommendLast(Context context, String last) {
        getSharePreferences(context, MEET_RECOMMNED_SHARED_NAME).edit().putString(LAST_RECOMMNED, last).apply();
    }

    public static String getDynamicsLast(Context context) {
        return getSharePreferences(context, MEET_DYNAMICS_SHARED_NAME).getString(LAST_DYNAMICS, "");
    }

    public static void setDynamicsLast(Context context, String last) {
        getSharePreferences(context, MEET_DYNAMICS_SHARED_NAME).edit().putString(LAST_DYNAMICS, last).apply();
    }

    public static String getNotificationLast(Context context) {
        return getSharePreferences(context, NOTIFICATION_SHARED_NAME).getString(LAST_NOTIFICATION, "");
    }

    public static void setNotificationLast(Context context, String last) {
        getSharePreferences(context, NOTIFICATION_SHARED_NAME).edit().putString(LAST_NOTIFICATION, last).apply();
    }
    
    public static String getConcernedDynamicsLast(Context context) {
        return getSharePreferences(context, CONCERNED_DYNAMICS_SHARED_NAME).getString(LAST_DYNAMICS, "");
    }

    public static void setConcernedDynamicsLast(Context context, String last) {
        getSharePreferences(context, CONCERNED_DYNAMICS_SHARED_NAME).edit().putString(LAST_DYNAMICS, last).apply();
    }

    public static String getDiscoveryLast(Context context) {
        return getSharePreferences(context, MEET_DISCOVERY_SHARED_NAME).getString(LAST_DISCOVERY, "");
    }

    public static void setDiscoveryLast(Context context, String last) {
        getSharePreferences(context, MEET_DISCOVERY_SHARED_NAME).edit().putString(LAST_DISCOVERY, last).apply();
    }

    public static String getSingleGroupLast(Context context) {
        return getSharePreferences(context, SINGLE_GROUP_SHARED_NAME).getString(LAST_SINGLE_GROUP, "");
    }
    
    public static void setSingleGroupLast(Context context, String last) {
        getSharePreferences(context, SINGLE_GROUP_SHARED_NAME).edit().putString(LAST_SINGLE_GROUP, last).apply();
    }

    public static long getUpdateCheckTimeStamp(Context context){
        return getSharePreferences(context, CHECK_UPDATE_TIME).getLong(TIME_STAMP, 0);
    }
    public static void setUpdateCheckTimeStamp(Context context, long time){
        getSharePreferences(context, CHECK_UPDATE_TIME).edit().putLong(TIME_STAMP, time).apply();
    }
    public static int getSessionUid(Context context){
        return getSharePreferences(context, SESSION).getInt("uid", -1);
    }
    public static void setLoginedAccountSex(Context context, int sex){
        getSharePreferences(context, SEX).edit().putInt(SEX, sex).apply();
    }
    public static int getLoginedAccountSex(Context context){
        return getSharePreferences(context, SEX).getInt(SEX, -1);
    }

}
