package com.tongmenhui.launchak47.meet;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.util.FontManager;
import com.tongmenhui.launchak47.util.Slog;

import java.util.List;

/**
 * Created by haichao.zou on 2017/11/20.
 */

public class MeetActivityListAdapter extends RecyclerView.Adapter<MeetActivityListAdapter.ViewHolder> {

    private static final String TAG = "MeetActivityListAdapter";
    private static Context mContext;
    private List<MeetActivity> mMeetActivityList;

    static class ViewHolder extends RecyclerView.ViewHolder{
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

    public MeetActivityListAdapter(Context context, List<MeetActivity> meetActivityList){
        mContext = context;
        mMeetActivityList = meetActivityList;
    }

    @Override
    public MeetActivityListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.meet_activity_item, parent, false);
        MeetActivityListAdapter.ViewHolder holder = new MeetActivityListAdapter.ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(final MeetActivityListAdapter.ViewHolder holder, int position){

        MeetRecommend meet = mMeetActivityList.get(position);
        Slog.d(TAG, "get name============="+meet.getRealname());
        holder.realname.setText(meet.getRealname());
        holder.lives.setText(meet.getLives());

        picture_url = domain+"/"+meet.getPictureUri();
        Slog.d(TAG, "picture url==========="+picture_url);
        MeetListAdapter.DownloadTask downloadTask = new MeetListAdapter.DownloadTask(holder, picture_url);
        downloadTask.execute();

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
