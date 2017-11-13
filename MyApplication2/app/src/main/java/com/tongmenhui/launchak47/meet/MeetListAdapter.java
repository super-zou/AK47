package com.tongmenhui.launchak47.meet;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.util.HttpUtil;
import com.tongmenhui.launchak47.util.Slog;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * Created by haichao.zou on 2017/9/15.
 */

public class MeetListAdapter extends RecyclerView.Adapter<MeetListAdapter.ViewHolder>{
    private static final String TAG = "MeetListAdapter";
    private static final String  domain = "http://www.tongmenhui.com";
    private List<MeetRecommend> mMeetList;
    private String picture_url;
    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView realname;
        ImageView headUri;

        public ViewHolder(View view){

            super(view);
            realname = (TextView) view.findViewById(R.id.name);
            headUri = (ImageView) view.findViewById(R.id.recommend_head_uri);
        }
    }

    public MeetListAdapter(List<MeetRecommend> meetList){
        mMeetList = meetList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.meet_item, parent, false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position){

        MeetRecommend meet = mMeetList.get(position);
        Slog.d(TAG, "get name============="+meet.getRealname());
        holder.realname.setText(meet.getRealname());

        picture_url = domain+"/"+meet.getPicture_uri();
        Slog.d(TAG, "picture url==========="+picture_url);
        //Drawable drawable = LoadImageFromWebOperations(picture_url);
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = HttpUtil.getHttpBitmap(picture_url);

                holder.headUri.setImageBitmap(bitmap);
            }
        }).start();
        */
       // holder.headUri.setImageDrawable(drawable);
    }

    private Drawable LoadImageFromWebOperations(String url)
    {
        try
        {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            return d;
        }catch (Exception e) {
            System.out.println("Exc="+e);
            return null;
        }
    }

    @Override
    public int getItemCount(){

        return mMeetList.size();
    }
}
