package com.tongmenhui.launchak47.util;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by super-zou on 17-12-9.
 */

public class RequestQueueSingleton {
    private static RequestQueue requestQueue;
    public static Context mContext;
    public void RequestQueueSingleton(){

    }
    public static RequestQueue instance(Context context){
        if(requestQueue == null){
            requestQueue = new Volley().newRequestQueue(context);
        }
        return requestQueue;
    }
}
