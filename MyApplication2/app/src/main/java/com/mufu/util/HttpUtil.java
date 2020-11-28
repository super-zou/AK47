package com.mufu.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.mufu.common.MyApplication;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.mufu.util.Utility.isApkInDebug;

/**
 * Created by haichao.zou on 2017/8/22.
 */
public class HttpUtil {
    public static final String TAG = "HttpUtil";
    /*+Begin: added by xuchunping 2018.7.19*/
    public static final String DOMAIN = getDomain();
    /*-End: added by xuchunping 2018.7.19*/
    public static final String CHECK_YUNXIN_ACCOUNT_EXIST = HttpUtil.DOMAIN + "?q=chat/check_yunxin_account";
    public static final String CREATE_YUNXIN_USER = HttpUtil.DOMAIN + "?q=chat/create_user";
    public static final String CHECK_VERSION_UPDATE = HttpUtil.DOMAIN + "?q=version_update/check";
    public static final String GET_DOWNLOAD_QR = HttpUtil.DOMAIN + "?q=version_update/get_download_qr";
    public static final String GET_PASSWORD_HASH = HttpUtil.DOMAIN + "?q=account_manager/get_password_hash";
    public static final String GET_USERINFO_WITH_ACCOUNT = HttpUtil.DOMAIN + "?q=account_manager/get_userinfo_with_account";
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/jpeg");
    private static String mCookie;
    static Request request;
    
    public static OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(300, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS);
    static OkHttpClient client = builder.build();
    
    public static String getDomain(){
        String domain = "";
        if (isApkInDebug(MyApplication.getContext()) == true){
            domain = "http://112.126.83.127:81/";
        }else {
            domain = "http://112.126.83.127:80/";
        }

        return domain;
    }

    public static String getCookie(Context context) {
        String cookie = "";
        String sessionName;
        String sessionId;
        SharedPreferences preferences = context.getSharedPreferences("conversation_fragment", MODE_PRIVATE);
        sessionName = preferences.getString("sessionName", "");
        sessionId = preferences.getString("sessionId", "");

        if(!TextUtils.isEmpty(sessionName) && !TextUtils.isEmpty(sessionId)){
            cookie = sessionName + "=" + sessionId;
        }

        return cookie;
    }
    
    public static void sendOkHttpRequest(Context context, String address, RequestBody requestBody, okhttp3.Callback callback) {

        mCookie = getCookie(context);
        if (requestBody != null) {
            if (mCookie != null && !TextUtils.isEmpty(mCookie)) {
                request = new Request.Builder().url(address).addHeader("cookie", mCookie).post(requestBody).build();
            } else {
                request = new Request.Builder().url(address).post(requestBody).build();
            }
            } else {
            if (mCookie != null && !TextUtils.isEmpty(mCookie)) {
                request = new Request.Builder().url(address).addHeader("cookie", mCookie).build();
            } else {
                request = new Request.Builder().url(address).build();
            }

        }

        client.newCall(request).enqueue(callback);
    }
    
    public static Response sendOkHttpRequestSync(Context context, String address, RequestBody requestBody, okhttp3.Callback callback) {
        Response response = null;
        mCookie = getCookie(context);

        if (requestBody != null) {
            if (mCookie != null && !TextUtils.isEmpty(mCookie)) {
                request = new Request.Builder().url(address).addHeader("cookie", mCookie).post(requestBody).build();
            } else {
                request = new Request.Builder().url(address).post(requestBody).build();
            }
            
            } else {
            if (mCookie != null && !TextUtils.isEmpty(mCookie)) {
                request = new Request.Builder().url(address).addHeader("cookie", mCookie).build();
            } else {
                request = new Request.Builder().url(address).build();
            }

        }

        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }
    
     public static void loginOkHttpRequest(Context context, String token, String address, RequestBody requestBody, okhttp3.Callback callback) {
        //OkHttpClient client = new OkHttpClient();
        if (requestBody != null) {
            request = new Request.Builder().url(address).addHeader("X-CSRF-Token", token).post(requestBody).build();
        } else {
            request = new Request.Builder().url(address).addHeader("X-CSRF-Token", token).build();
        }

        client.newCall(request).enqueue(callback);
    }
    
    public static void uploadPictureHttpRequest(Context context, Map<String, String> params, String picKey, List<File> files, String address, okhttp3.Callback callback) {
        //OkHttpClient client = new OkHttpClient();
        mCookie = getCookie(context);

        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
        multipartBodyBuilder.setType(MultipartBody.FORM);
        //遍历map中所有参数到builder
        if (params != null) {
            for (String key : params.keySet()) {
                multipartBodyBuilder.addFormDataPart(key, params.get(key));
            }
        }
        
        if (files != null && files.size() > 0) {
            //遍历paths中所有图片绝对路径到builder，并约定key如“upload”作为后台接受多张图片的key
            int i = 0;
            for (File file : files) {
                Slog.d(TAG, "file name: " + file.getName());
                multipartBodyBuilder.addFormDataPart(picKey + i, file.getName(), RequestBody.create(MEDIA_TYPE_PNG, file));
                i++;
            }
        }

        RequestBody requestBody = multipartBodyBuilder.build();
        
         if (mCookie != null && !TextUtils.isEmpty(mCookie)) {
            request = new Request.Builder().url(address).addHeader("cookie", mCookie).post(requestBody).build();
        } else {
            request = new Request.Builder().url(address).post(requestBody).build();
        }

        client.newCall(request).enqueue(callback);
    }
    
    public static void uploadPictureProgressHttpRequest(Context context, Map<String, String> params, String picKey, List<File> files,
                                                        String address, okhttp3.Callback callback, ExMultipartBody.UploadProgressListener uploadProgressListener) {
        //OkHttpClient client = new OkHttpClient();
        mCookie = getCookie(context);

        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
        multipartBodyBuilder.setType(MultipartBody.FORM);
        //遍历map中所有参数到builder
        if (params != null) {
            for (String key : params.keySet()) {
                multipartBodyBuilder.addFormDataPart(key, params.get(key));
            }
        }
        
        if (files != null && files.size() > 0) {
            //遍历paths中所有图片绝对路径到builder，并约定key如“upload”作为后台接受多张图片的key
            int i = 0;
            for (File file : files) {
                Slog.d(TAG, "file name: " + file.getName());
                multipartBodyBuilder.addFormDataPart(picKey + i, file.getName(), RequestBody.create(MEDIA_TYPE_PNG, file));
                i++;
            }
        }

        ExMultipartBody exMultipartBody = new ExMultipartBody(multipartBodyBuilder.build(), uploadProgressListener);
        
        //RequestBody requestBody = multipartBodyBuilder.build();

        if (mCookie != null && !TextUtils.isEmpty(mCookie)) {
            request = new Request.Builder().url(address).addHeader("cookie", mCookie).post(exMultipartBody).build();
        } else {
            request = new Request.Builder().url(address).post(exMultipartBody).build();
        }

        client.newCall(request).enqueue(callback);
    }

} 
