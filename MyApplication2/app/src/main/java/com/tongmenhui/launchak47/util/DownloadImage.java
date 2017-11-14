package com.tongmenhui.launchak47.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by super-zou on 17-11-14.
 */

public class DownloadImage extends AsyncTask<String, Object, Bitmap> {
    private RecyclerView.ViewHolder mViewHolder;

    public DownloadImage(RecyclerView.ViewHolder mViewHolder) {
        this.mViewHolder = mViewHolder;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        String url = params[0];
        Bitmap bitmap = null;
        try {
            //加载一个网络图片
            InputStream is = new URL(url).openStream();
            bitmap = BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        mViewHolder.headUri.setImageBitmap(result);
        //imageView.setImageBitmap(result);
    }
}
