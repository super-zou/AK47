package com.tongmenhui.launchak47.util;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by haichao.zou on 2017/8/22.
 */

public class HttpUtil {
    public static void sendOkHttpRequest(String header, String address, RequestBody requestBody, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = null;

        if(requestBody != null){
            if(header != null){
                request = new Request.Builder().url(address).header("X-CSRF-Token", header).post(requestBody).build();
            }else{
                request = new Request.Builder().url(address).post(requestBody).build();
            }

        }else{
            if(header != null){
                request = new Request.Builder().url(address).header("X-CSRF-Token", header).build();
            }else{
                request = new Request.Builder().url(address).build();
            }

        }

        client.newCall(request).enqueue(callback);
    }

    public static void getOkHttpRequestSync(String address, String session){
        OkHttpClient client = new OkHttpClient();
        Request request = null;

        if(session != null){
            request = new Request.Builder().addHeader("cookie", session).url(address).build();
        }
        try {
            client.newCall(request).execute();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void getOkHttpRequestAsync(String address, String session, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = null;

        if(session != null){
            request = new Request.Builder().addHeader("cookie", session).url(address).build();
        }

        client.newCall(request).enqueue(callback);

    }


}
