package com.tongmenhui.launchak47.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by haichao.zou on 2017/8/22.
 */

public class HttpUtil {
    public static void sendOkHttpRequest(String address, String account, String password, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
