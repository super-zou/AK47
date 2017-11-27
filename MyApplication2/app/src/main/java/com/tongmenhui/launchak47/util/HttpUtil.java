package com.tongmenhui.launchak47.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.meet.MeetListAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
/*
    private class DownloadTask extends AsyncTask<String, Object, Bitmap> {
        private MeetListAdapter.ViewHolder mViewHolder;
        private String picture_url;

        public DownloadTask(MeetListAdapter meetListAdapter, MeetListAdapter.ViewHolder mViewHolder, String picture_url) {
            this.mViewHolder = mViewHolder;
            this.picture_url = picture_url;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            //String url = params[0];
            Bitmap bitmap = null;
            try {
                //加载一个网络图片
                InputStream is = new URL(picture_url).openStream();
                bitmap = BitmapFactory.decodeStream(is);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
           // mViewHolder.headUri.setImageBitmap(result);
            //imageView.setImageBitmap(result);
            onDownLoadPost(mViewHolder, result);
        }
    }
*/
    public static void loadByImageLoader(RequestQueue queue, ImageView imageView, String url, int width, int height){
        //创建ImageLoader对象，参数（加入请求队列，自定义缓存机制）
        ImageLoader imageLoader =new ImageLoader(queue, new BitmapCache());


        //获取图片监听器 参数（要显示的ImageView控件，默认显示的图片，加载错误显示的图片）
        ImageLoader.ImageListener listener = ImageLoader.getImageListener(imageView,
                R.drawable.main_bottom_attention_press,
                R.drawable.main_bottom_attention_press);

        imageLoader.get(url,listener,width, height);
    }


}
