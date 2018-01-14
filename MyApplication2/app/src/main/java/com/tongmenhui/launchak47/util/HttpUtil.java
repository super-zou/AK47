package com.tongmenhui.launchak47.util;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.tongmenhui.launchak47.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by haichao.zou on 2017/8/22.
 */

public class HttpUtil {
    public static final String TAG = "HttpUtil";

    public static void sendOkHttpRequest(String header, String address, RequestBody requestBody, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = null;

        SharedPreferences.Editor editor = getSharedPreferences("session", MODE_PRIVATE).edit();

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
            request = new Request.Builder().url(address).build();
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

    public static Bitmap getHttpBitmap(String url){
        URL myFileURL;
        Bitmap bitmap=null;
        try{
            myFileURL = new URL(url);
            //获得连接
            HttpURLConnection conn=(HttpURLConnection)myFileURL.openConnection();
            //设置超时时间为6000毫秒，conn.setConnectionTiem(0);表示没有时间限制
            conn.setConnectTimeout(0);
            //连接设置获得数据流
            conn.setDoInput(true);
            //不使用缓存
            conn.setUseCaches(false);
            //这句可有可无，没有影响
            conn.connect();
            //得到数据流
            InputStream is = conn.getInputStream();
            //解析得到图片
            bitmap = BitmapFactory.decodeStream(is);
            //关闭数据流
            is.close();
        }catch(Exception e){
            e.printStackTrace();
        }

        return bitmap;

    }
    public static void loadByImageLoader(RequestQueue queue, ImageView imageView, String url, int width, int height){

        if(url != null){
            //创建ImageLoader对象，参数（加入请求队列，自定义缓存机制）
            ImageLoader imageLoader =new ImageLoader(queue, BitmapCache.instance());
            if(url.equals(imageView.getTag())){
                if(imageView instanceof NetworkImageView){
                    //Slog.d(TAG, "================NetworkImageView  instance============");
                    ((NetworkImageView) imageView).setDefaultImageResId(R.drawable.main_bottom_attention_press);
                    ((NetworkImageView) imageView).setErrorImageResId(android.R.drawable.stat_notify_error);
                    //imageView.setLayoutParams(layoutParams);
                    ((NetworkImageView) imageView).setImageUrl(url, imageLoader);
                }else{
                    //获取图片监听器 参数（要显示的ImageView控件，默认显示的图片，加载错误显示的图片）
                    ImageLoader.ImageListener listener = ImageLoader.getImageListener(imageView,
                            R.drawable.main_bottom_attention_press,
                            R.drawable.error);

                    imageLoader.get(url,listener,width, height);
                }
            }
        }

    }


}
