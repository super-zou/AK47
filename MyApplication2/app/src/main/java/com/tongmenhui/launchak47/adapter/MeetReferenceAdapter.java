package com.tongmenhui.launchak47.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.meet.MeetMemberInfo;
import com.tongmenhui.launchak47.util.FontManager;
import com.tongmenhui.launchak47.util.Slog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by super-zou on 18-9-21.
 */

public class MeetReferenceAdapter extends RecyclerView.Adapter<MeetReferenceAdapter.ReferenceViewHolder> {

    private static final String TAG = "MeetReferenceAdapter";
    private static Context mContext;
    private List<MeetMemberInfo> mMeetList;

    public MeetReferenceAdapter(Context context){
        Slog.d(TAG, "==============MeetReferenceAdapter init=================");
        mContext = context;
        mMeetList = new ArrayList<MeetMemberInfo>();
    }

    public class ReferenceViewHolder extends RecyclerView.ViewHolder{
        TextView realname;
        TextView lives;
        TextView selfcondition;
        TextView requirement;
        NetworkImageView headUri;
        TextView illustration;
        TextView eyeView;
        TextView lovedView;
        TextView lovedIcon;
        TextView thumbsView;
        TextView thumbsUpIcon;
        TextView photosView;
        TextView contentView;
        TextView createdView;
        TextView dynamicsPraiseIcon;
        TextView dynamicsPraiseCount;
        TextView dynamicsCommentCount;
        GridLayout dynamicsGrid;
        LinearLayout commentList;

        public ReferenceViewHolder(View view){
            super(view);
            view.findViewById(R.id.meet_item_id).setVisibility(View.GONE);
            realname = (TextView) view.findViewById(R.id.name);
            lives = (TextView) view.findViewById(R.id.lives);
            headUri = (NetworkImageView) view.findViewById(R.id.recommend_head_uri);
            selfcondition = (TextView) view.findViewById(R.id.self_condition);
            requirement = (TextView) view.findViewById(R.id.partner_requirement);
            illustration = (TextView) view.findViewById(R.id.illustration);
            eyeView = (TextView)view.findViewById(R.id.eye_statistics);
            lovedView = (TextView)view.findViewById(R.id.loved_statistics);
            lovedIcon = (TextView)view.findViewById(R.id.loved_icon);
            thumbsView = (TextView)view.findViewById(R.id.thumbs_up_statistics);
            thumbsUpIcon = (TextView)view.findViewById(R.id.thumbs_up_icon);
            photosView = (TextView)view.findViewById(R.id.photos_statistics);
            contentView = (TextView)view.findViewById(R.id.dynamics_content);
            dynamicsGrid = (GridLayout) view.findViewById(R.id.dynamics_picture_grid);
            createdView = (TextView) view.findViewById(R.id.dynamic_time);
            dynamicsPraiseIcon = (TextView) view.findViewById(R.id.dynamic_praise_icon);
            dynamicsPraiseCount = (TextView) view.findViewById(R.id.dynamic_praise);
            dynamicsCommentCount = (TextView) view.findViewById(R.id.dynamic_comment_count);

            commentList = (LinearLayout) view.findViewById(R.id.dynamics_comments);

            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.meet_dynamics_item), font);
        }
    }

    @Override
    public ReferenceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.evaluation_item, parent, false);
        ReferenceViewHolder holder = new ReferenceViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MeetReferenceAdapter.ReferenceViewHolder holder, int position) {

    }

    @Override
    public int getItemCount(){
        return mMeetList.size();
    }
}
