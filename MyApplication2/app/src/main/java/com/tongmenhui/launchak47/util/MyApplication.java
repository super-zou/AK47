package com.tongmenhui.launchak47.util;

import android.app.Application;
import android.content.Context;

/**
 * Created by super-zou on 18-1-20.
 */

public class MyApplication extends Application {
    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        mContext = getApplicationContext();
    }
}
