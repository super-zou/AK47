package com.tongmenhui.launchak47.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;

/**
 * Created by haichao.zou on 2017/8/22.
 */

public class Utility {

    public static boolean handleLoginResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            /*
            try {

            }catch (JSONException e){
                e.printStackTrace();
            }
            */
        }
        return false;

    }
    
    public static String timeStampToMinute(int created) {
        Date date = new Date(created * 1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return simpleDateFormat.format(date);
    }

    public static String timeStampToHour(int created) {
        Date date = new Date(created * 1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH");
        return simpleDateFormat.format(date);
    }

    public static String timeStampToDay(int created) {
        Date date = new Date(created * 1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(date);
    }

    public static float dpToPx(Context context, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}
