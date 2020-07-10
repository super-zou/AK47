package com.hetang.adapter;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hetang.util.HttpUtil;
import com.youth.banner.adapter.BannerAdapter;

import java.util.List;

import static com.hetang.common.MyApplication.getContext;

public class GuideBannerAdapter extends BannerAdapter<DataBean, GuideBannerAdapter.BannerViewHolder> {

    public GuideBannerAdapter(List<DataBean> mDatas) {
        //设置数据，也可以调用banner提供的方法,或者自己在adapter中实现
        super(mDatas);
    }
    
    //创建ViewHolder，可以用viewType这个字段来区分不同的ViewHolder
    @Override
    public BannerViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());
        //注意，必须设置为match_parent，这个是viewpager2强制要求的
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new BannerViewHolder(imageView);
    }

    @Override
    public void onBindView(BannerViewHolder holder, DataBean data, int position, int size) {
        if (data.imageRes != null){
            holder.imageView.setImageResource(data.imageRes);
        }else {
            if (data.imageUrl != null)
            Glide.with(getContext()).load(HttpUtil.DOMAIN+data.imageUrl).into(holder.imageView);
        }
    }
    
    class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public BannerViewHolder(@NonNull ImageView view) {
            super(view);
            this.imageView = view;
        }
    }
}
