package com.tongmenhui.launchak47.util;

import android.content.Context;
import android.content.SharedPreferences;

/*added by xuchunping 2018.7.21*/
public class SharedPreferencesUtils {
    private static final String SHARED_NAME = "account_info";
    private static final String ACCOUNT = "account";
    private static final String ACCOUNT_TYPE = "type";
    private static final String LAST_RECOMMNED = "last_recommend";
    private static final String LAST_DYNAMICS = "last_dynamics";
    private static final String LAST_DISCOVERY = "last_discovery";

    private static SharedPreferences getSharePreferences(Context context) {
        return context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
    }

    public static String getAccount(Context context) {
        return getSharePreferences(context).getString(ACCOUNT, "");
    }

    public static void setAccount(Context context, String account) {
        getSharePreferences(context).edit().putString(ACCOUNT, account).commit();
    }

    public static int getAccountType(Context context) {
        return getSharePreferences(context).getInt(ACCOUNT_TYPE, 0);
    }

    public static void setAccountType(Context context, int type) {
        getSharePreferences(context).edit().putInt(ACCOUNT_TYPE, type).commit();
    }

    public static String getRecommendLast(Context context) {
        return getSharePreferences(context).getString(LAST_RECOMMNED, "");
    }

    public static void setRecommendLast(Context context, String last) {
        getSharePreferences(context).edit().putString(LAST_RECOMMNED, last).commit();
    }

    public static String getDynamicsLast(Context context) {
        return getSharePreferences(context).getString(LAST_DYNAMICS, "");
    }

    public static void setDynamicsLast(Context context, String last) {
        getSharePreferences(context).edit().putString(LAST_DYNAMICS, last).commit();
    }

    public static String getDiscoveryLast(Context context) {
        return getSharePreferences(context).getString(LAST_DISCOVERY, "");
    }

    public static void setDiscoveryLast(Context context, String last) {
        getSharePreferences(context).edit().putString(LAST_DISCOVERY, last).commit();
    }
}
