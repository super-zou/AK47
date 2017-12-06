package com.tongmenhui.launchak47.meet;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.util.FontManager;
import com.tongmenhui.launchak47.util.HttpUtil;
import com.tongmenhui.launchak47.util.Slog;

import java.lang.reflect.Array;
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
        TextView contentView;
        LinearLayout dynamicsContainer;

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
            contentView = (TextView)view.findViewById(R.id.dynamics_content);
            dynamicsContainer = (LinearLayout) view.findViewById(R.id.dynamics_containers);

            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.behavior_statistics), font);

        }
    }

    public MeetDynamicsListAdapter(Context context){
        Slog.d(TAG, "==============MeetListAdapter init=================");
        mContext = context;
        mMeetList = new ArrayList<MeetDynamics>();
    }

    public void setData(List<MeetDynamics> meetList){
        mMeetList = meetList;
    }

    @Override
    public MeetDynamicsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        Slog.d(TAG, "===========onCreateViewHolder==============");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.meet_dynamics_item, parent, false);
        MeetDynamicsListAdapter.ViewHolder holder = new MeetDynamicsListAdapter.ViewHolder(view);

        return holder;
    }

    @Override
    public void onViewRecycled(MeetDynamicsListAdapter.ViewHolder holder){
        super.onViewRecycled(holder);
        Object tag = holder.dynamicsContainer.getTag(R.id.tag_first);
        queue.cancelAll(tag);
    }

    @Override
    public void onBindViewHolder(final MeetDynamicsListAdapter.ViewHolder holder, int position){

        Slog.d(TAG, "===========onBindViewHolder==============");
        MeetDynamics meetDynamics = mMeetList.get(position);
        // Slog.d(TAG, "get name============="+meet.getRealname());
        holder.realname.setText(meetDynamics.getRealname());
        holder.lives.setText(meetDynamics.getLives());

        picture_url = domain+"/"+meetDynamics.getPictureUri();
        // Slog.d(TAG, "picture url==========="+picture_url);
        //DownloadTask downloadTask = new DownloadTask(holder, picture_url);
        //downloadTask.execute();
        queue = new Volley().newRequestQueue(mContext);


        HttpUtil.loadByImageLoader(queue, holder.headUri, picture_url, 110, 110);

        holder.selfcondition.setText(meetDynamics.getSelfCondition(meetDynamics.getSituation()));
        holder.requirement.setText(meetDynamics.getRequirement());

        holder.eyeView.setText(String.valueOf(meetDynamics.getBrowse_count()));
        holder.lovedView.setText(String.valueOf(meetDynamics.getLoved_count()));
        holder.thumbsView.setText(String.valueOf(meetDynamics.getPraised_count()));
        holder.illustration.setText(meetDynamics.getIllustration());

        holder.contentView.setText(meetDynamics.getContent());

        String pictures = meetDynamics.getActivityPicture();
        Slog.d(TAG, "=============pictures: "+pictures);
        if(!"".equals(pictures)){
            holder.dynamicsContainer.removeAllViews();

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            String[] picture_array = pictures.split(":");
            int length = picture_array.length;
            if(length > 0){
                for (int i = 0; i < length; i++){
                    Slog.d(TAG, "==========picture_array:"+picture_array[i]);
                    if(picture_array[i] != null){
                        ImageView picture = new ImageView(mContext);
                        picture.setLayoutParams(lp);
                        //picture.setTag(1, picture_array[i]);
                        // picture.setTag(picture_array[i]);
                        //picture.setBackgroundColor(Color.parseColor("#f34649"));
                        holder.dynamicsContainer.addView(picture);
                        holder.dynamicsContainer.setTag(R.id.tag_first, queue);
                        HttpUtil.loadByImageLoader(queue, picture, domain+"/"+picture_array[i], 110, 110);
                    }
                }
            }

        }else{
            Slog.d(TAG, "=========wowo null");
        }


    }

    @Override
    public int getItemCount(){

        return mMeetList.size();
    }
}
