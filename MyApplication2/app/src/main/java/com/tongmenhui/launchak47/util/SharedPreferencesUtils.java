package com.tongmenhui.launchak47.util;

import android.content.Context;
import android.content.SharedPreferences;

/*added by xuchunping 2018.7.21*/
public class SharedPreferencesUtils {
    private static final String SHARED_NAME = "account_info";
    private static final String ACCOUNT = "account";
    private static final String ACCOUNT_TYPE = "type";

    private static SharedPreferences getSharePreferences(Context context){
        return context.getSharedPreferences(SHARED_NAME,  Context.MODE_PRIVATE);
    }

    public static String getAccount(Context context){
        return getSharePreferences(context).getString(ACCOUNT, "");
    }

    public static void setAccount(Context context, String account) {
        getSharePreferences(context).edit().putString(ACCOUNT, account).commit();
    }

    public static int getAccountType(Context context){
        return getSharePreferences(context).getInt(ACCOUNT_TYPE, 0);
    }

    public static void setAccountType(Context context, int type) {
        getSharePreferences(context).edit().putInt(ACCOUNT_TYPE, type).commit();
    }
}
