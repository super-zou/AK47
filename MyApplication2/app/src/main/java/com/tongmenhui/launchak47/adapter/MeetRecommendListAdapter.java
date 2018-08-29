package com.tongmenhui.launchak47.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.NetworkImageView;
import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.main.ArchivesActivity;
import com.tongmenhui.launchak47.meet.MeetMemberInfo;
import com.tongmenhui.launchak47.util.FontManager;
import com.tongmenhui.launchak47.util.HttpUtil;
import com.tongmenhui.launchak47.util.RequestQueueSingleton;
import com.tongmenhui.launchak47.util.Slog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by haichao.zou on 2017/9/15.
 */

public class MeetRecommendListAdapter extends RecyclerView.Adapter<MeetRecommendListAdapter.ViewHolder>{
    private static final String TAG = "MeetRecommendListAdapter";
    private static final String  domain = "http://112.126.83.127:88";
    private List<MeetMemberInfo> mMeetList;
    private String picture_url;
    private static Context mContext;
    private boolean isScrolling = false;

    RequestQueue queue;

    public void setScrolling(boolean isScrolling){
        this.isScrolling = isScrolling;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{
        //TextView addMeetInfo;
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

        public ViewHolder(View view){

            super(view);
            //addMeetInfo = (TextView)view.findViewById(R.id.meet_info_add);
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

            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.behavior_statistics), font);

        }
    }

    public MeetRecommendListAdapter(Context context){
        Slog.d(TAG, "==============MeetRecommendListAdapter init=================");
        mContext = context;
        mMeetList = new ArrayList<MeetMemberInfo>();
    }
    public void setData(List<MeetMemberInfo> meetList){
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
        final MeetMemberInfo meet = mMeetList.get(position);
       // Slog.d(TAG, "get name============="+meet.getRealname());
        holder.realname.setText(meet.getRealname());
        holder.lives.setText(meet.getLives());

        if(!"".equals(meet.getPictureUri()) && !isScrolling){
            picture_url = domain+"/"+meet.getPictureUri();
            queue = RequestQueueSingleton.instance(mContext);

            holder.headUri.setTag(picture_url);
            HttpUtil.loadByImageLoader(queue, holder.headUri, picture_url, 110, 110);
        }else{
            holder.headUri.setImageDrawable(mContext.getDrawable(R.mipmap.ic_launcher));
        }


        holder.selfcondition.setText(meet.getSelfCondition(meet.getSituation()));
        holder.requirement.setText(meet.getRequirement());

        holder.eyeView.setText(String.valueOf(meet.getBrowseCount()));
        holder.lovedView.setText(String.valueOf(meet.getLovedCount()));
        holder.thumbsView.setText(String.valueOf(meet.getPraisedCount()));
        holder.illustration.setText(meet.getIllustration());

        //+Begin by xuchunping
        holder.headUri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContext.startActivity(new Intent(mContext, ArchivesActivity.class));
            }
        });
        holder.photosView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ArchivesActivity.class);
                intent.putExtra("meet", meet);
                mContext.startActivity(intent);
            }
        });
        //-End by xuchunping
    }

    @Override
    public int getItemCount(){

        return mMeetList.size();
    }

}
