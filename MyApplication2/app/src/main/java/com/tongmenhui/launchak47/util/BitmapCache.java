package com.tongmenhui.launchak47.util;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader;

/**
 * Created by haichao.zou on 2017/11/21.
 */

public class BitmapCache implements ImageLoader.ImageCache{
    private LruCache<String, Bitmap> mCache;
    private static BitmapCache bitmapCache;

    public BitmapCache() {
        //int maxSize = 10 * 1024 * 1024;
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory/10;
        mCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight();
            }
        };
    }

    public static BitmapCache instance(){
        if(bitmapCache == null){
            bitmapCache = new BitmapCache();
        }
        return bitmapCache;
    }

    @Override
    public Bitmap getBitmap(String url) {
        return mCache.get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        mCache.put(url, bitmap);
    }
}
