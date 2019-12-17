package com.hetang.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.hetang.R;
import com.previewlibrary.loader.IZoomMediaLoader;
import com.previewlibrary.loader.MySimpleTarget;

public class PictureLoader implements IZoomMediaLoader {

    RequestOptions options;

    {
        options = new RequestOptions()
                .centerCrop()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .placeholder(R.drawable.icon)
                .error(R.drawable.ic_placeholder)
                .priority(Priority.HIGH);
    }
    
    @Override
    public void displayImage(@NonNull Fragment context, @NonNull String path, ImageView imageView,@NonNull MySimpleTarget  simpleTarget){
        Glide.with(context).load(path).apply(options).into(imageView);
    }

    @Override
    public void displayGifImage(@NonNull Fragment context, @NonNull String path, ImageView imageView, @NonNull final MySimpleTarget simpleTarget) {
        Glide.with(context).load(path)
                //可以解决gif比较几种时 ，加载过慢  //DiskCacheStrategy.NONE
                .apply(options)
                .into(imageView);
    }
    
    @Override
    public void onStop(@NonNull Fragment context) {
        Glide.with(context).onStop();
    }
    @Override
    public void clearMemory(@NonNull Context c) {
        Glide.get(c).clearMemory();
    }
}
