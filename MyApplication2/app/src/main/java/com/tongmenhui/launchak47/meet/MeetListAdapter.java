package com.tongmenhui.launchak47.meet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.res.AssetManager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.util.FontManager;
import com.tongmenhui.launchak47.util.HttpUtil;
import com.tongmenhui.launchak47.util.Slog;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;

/**
 * Created by haichao.zou on 2017/9/15.
 */

public class MeetListAdapter extends RecyclerView.Adapter<MeetListAdapter.ViewHolder>{
    private static final String TAG = "MeetListAdapter";
    private static final String  domain = "http://www.tongmenhui.com";
    private List<MeetRecommend> mMeetList;
    private String picture_url;
    private static Context mContext;

    RequestQueue queue;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView realname;
        TextView lives;
        TextView selfcondition;
        TextView requirement;
        ImageView headUri;
        TextView illustration;
        TextView eyeView;
        TextView lovedView;
        TextView thumbsView;
        TextView photosView;

        public ViewHolder(View view){

            super(view);
            realname = (TextView) view.findViewById(R.id.name);
            lives = (TextView) view.findViewById(R.id.lives);
            headUri = (ImageView) view.findViewById(R.id.recommend_head_uri);
            selfcondition = (TextView) view.findViewById(R.id.self_condition);
            requirement = (TextView) view.findViewById(R.id.partner_requirement);
            illustration = (TextView) view.findViewById(R.id.illustration);
            eyeView = (TextView)view.findViewById(R.id.eye_statistics);
            lovedView = (TextView)view.findViewById(R.id.loved_statistics);
            thumbsView = (TextView)view.findViewById(R.id.thumbs_up_statistics);
            photosView = (TextView)view.findViewById(R.id.photos_statistics);
            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.behavior_statistics), font);

        }
    }

    public MeetListAdapter(Context context){
        Slog.d(TAG, "==============MeetListAdapter init=================");
        mContext = context;
        mMeetList = new ArrayList<MeetRecommend>();
    }
    public void setData(List<MeetRecommend> meetList){
        mMeetList = meetList;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        Slog.d(TAG, "===========onCreateViewHolder==============");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.meet_item, parent, false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position){

        Slog.d(TAG, "===========onBindViewHolder==============");
        MeetRecommend meet = mMeetList.get(position);
       // Slog.d(TAG, "get name============="+meet.getRealname());
        holder.realname.setText(meet.getRealname());
        holder.lives.setText(meet.getLives());

        picture_url = domain+"/"+meet.getPictureUri();
       // Slog.d(TAG, "picture url==========="+picture_url);
        //DownloadTask downloadTask = new DownloadTask(holder, picture_url);
        //downloadTask.execute();
        queue = new Volley().newRequestQueue(mContext);


        HttpUtil.loadByImageLoader(queue, holder.headUri, picture_url, 110, 110);

        holder.selfcondition.setText(meet.getSelfCondition(meet.getSituation()));
        holder.requirement.setText(meet.getRequirement());

        holder.eyeView.setText(String.valueOf(meet.getBrowse_count()));
        holder.lovedView.setText(String.valueOf(meet.getLoved_count()));
        holder.thumbsView.setText(String.valueOf(meet.getPraised_count()));
        holder.illustration.setText(meet.getIllustration());


    }

    @Override
    public int getItemCount(){

        return mMeetList.size();
    }

}
