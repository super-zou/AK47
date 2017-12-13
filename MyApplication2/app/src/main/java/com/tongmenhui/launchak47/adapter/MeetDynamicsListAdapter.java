package com.tongmenhui.launchak47.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.NetworkImageView;
import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.meet.MeetDynamics;
import com.tongmenhui.launchak47.util.FontManager;
import com.tongmenhui.launchak47.util.HttpUtil;
import com.tongmenhui.launchak47.util.RequestQueueSingleton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by haichao.zou on 2017/11/20.
 */

public class MeetDynamicsListAdapter extends RecyclerView.Adapter<MeetDynamicsListAdapter.ViewHolder> {

    private static final String TAG = "MeetDynamicsListAdapter";
    private static final String  domain = "http://www.tongmenhui.com";
    private List<MeetDynamics> mMeetList;
    private String picture_url;
    private static Context mContext;
    private boolean isScrolling = false;
    RequestQueue queueMemberInfo;
    RequestQueue queueDynamics;
    RequestQueueSingleton requestQueueSingleton;



    public void setScrolling(boolean isScrolling){
        this.isScrolling = isScrolling;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView realname;
        TextView lives;
        TextView selfcondition;
        TextView requirement;
        NetworkImageView headUri;
        TextView illustration;
        TextView eyeView;
        TextView lovedView;
        TextView thumbsView;
        TextView photosView;
        TextView contentView;
        GridLayout dynamicsGrid;




        public ViewHolder(View view){

            super(view);
            realname = (TextView) view.findViewById(R.id.name);
            lives = (TextView) view.findViewById(R.id.lives);
            headUri = (NetworkImageView) view.findViewById(R.id.recommend_head_uri);
            selfcondition = (TextView) view.findViewById(R.id.self_condition);
            requirement = (TextView) view.findViewById(R.id.partner_requirement);
            illustration = (TextView) view.findViewById(R.id.illustration);
            eyeView = (TextView)view.findViewById(R.id.eye_statistics);
            lovedView = (TextView)view.findViewById(R.id.loved_statistics);
            thumbsView = (TextView)view.findViewById(R.id.thumbs_up_statistics);
            photosView = (TextView)view.findViewById(R.id.photos_statistics);
            contentView = (TextView)view.findViewById(R.id.dynamics_content);
            dynamicsGrid = (GridLayout) view.findViewById(R.id.dynamics_picture_grid);


            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.behavior_statistics), font);

        }
    }

    public MeetDynamicsListAdapter(Context context){
        //Slog.d(TAG, "==============MeetListAdapter init=================");
        mContext = context;
        mMeetList = new ArrayList<MeetDynamics>();
        requestQueueSingleton = new RequestQueueSingleton();

    }

    public void setData(List<MeetDynamics> meetList){
        mMeetList = meetList;
    }

    @Override
    public MeetDynamicsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
       // Slog.d(TAG, "===========onCreateViewHolder==============");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.meet_dynamics_item, parent, false);

        MeetDynamicsListAdapter.ViewHolder holder = new MeetDynamicsListAdapter.ViewHolder(view);

        return holder;
    }

    @Override
    public void onViewRecycled(MeetDynamicsListAdapter.ViewHolder holder){
        super.onViewRecycled(holder);
    }

    @Override
    public void onBindViewHolder(final MeetDynamicsListAdapter.ViewHolder holder, int position){

       // Slog.d(TAG, "===========onBindViewHolder==============");
        MeetDynamics meetDynamics = mMeetList.get(position);

        holder.realname.setText(meetDynamics.getRealname());
        holder.lives.setText(meetDynamics.getLives());


        if(!"".equals(meetDynamics.getPictureUri()) && !isScrolling){
            picture_url = domain+"/"+meetDynamics.getPictureUri();
            //queueMemberInfo = new Volley().newRequestQueue(mContext);
            queueMemberInfo = requestQueueSingleton.instance(mContext);
            holder.headUri.setTag(picture_url);
            HttpUtil.loadByImageLoader(queueMemberInfo, holder.headUri, picture_url, 110, 110);
        }else{
            holder.headUri.setImageDrawable(mContext.getDrawable(R.mipmap.ic_launcher));
        }

        holder.selfcondition.setText(meetDynamics.getSelfCondition(meetDynamics.getSituation()));
        holder.requirement.setText(meetDynamics.getRequirement());
        holder.eyeView.setText(String.valueOf(meetDynamics.getBrowse_count()));
        holder.lovedView.setText(String.valueOf(meetDynamics.getLoved_count()));
        holder.thumbsView.setText(String.valueOf(meetDynamics.getPraised_count()));
        holder.illustration.setText(meetDynamics.getIllustration());
        holder.contentView.setText(meetDynamics.getContent());

        String pictures = meetDynamics.getActivityPicture();

        if(!"".equals(pictures)){
            holder.dynamicsGrid.removeAllViews();
            //queueDynamics = new Volley().newRequestQueue(mContext);
            queueDynamics = requestQueueSingleton.instance(mContext);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            String[] picture_array = pictures.split(":");
            int length = picture_array.length;
            if(length > 0){



                for (int i = 0; i < length; i++){
                    if(picture_array[i] != null){
                        NetworkImageView picture = new NetworkImageView(mContext);
                        picture.setLayoutParams(lp);
                        holder.dynamicsGrid.addView(picture);
                       // holder.dynamicsContainer.setTag(R.id.tag_first, queueDynamics);
                        picture.setImageDrawable(mContext.getDrawable(R.mipmap.ic_launcher));
                        picture.setTag(domain+"/"+picture_array[i]);
                        //queueDynamics.setTag();
                        HttpUtil.loadByImageLoader(queueDynamics, picture, domain+"/"+picture_array[i], 110, 110);
                    }
                }


            }

        }else{
            holder.dynamicsGrid.removeAllViews();
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ImageView picture = new ImageView(mContext);
            picture.setLayoutParams(lp);
            holder.dynamicsGrid.addView(picture);
        }


    }

    @Override
    public int getItemCount(){

        return mMeetList.size();
    }

}
