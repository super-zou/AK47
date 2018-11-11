package com.tongmenhui.launchak47.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;

import org.json.JSONException;

/**
 * Created by haichao.zou on 2017/8/22.
 */

public class Utility {

    public static boolean handleLoginResponse(String response){
        if(!TextUtils.isEmpty(response)){
            /*
            try {

            }catch (JSONException e){
                e.printStackTrace();
            }
            */
        }
        return false;

    }
    public static float dpToPx(Context context, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}
