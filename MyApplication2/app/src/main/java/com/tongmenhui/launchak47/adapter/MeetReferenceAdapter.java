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
import com.tongmenhui.launchak47.meet.MeetReferenceInfo;
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
    private List<MeetReferenceInfo> mReferenceList;

    public MeetReferenceAdapter(Context context){
        Slog.d(TAG, "==============MeetReferenceAdapter init=================");
        mContext = context;

    }

    public void setReferenceList(List<MeetReferenceInfo> referenceList){
        mReferenceList = referenceList;
    }

    public class ReferenceViewHolder extends RecyclerView.ViewHolder{
        TextView realname;
        TextView referee_profile;
        NetworkImageView referee_head_uri;
        TextView illustration;
        TextView eyeView;
        TextView lovedView;
        TextView lovedIcon;
        TextView thumbsView;
        TextView thumbsUpIcon;
        TextView referenceContent;
        TextView createdView;
        TextView commentIcon;

        public ReferenceViewHolder(View view){
            super(view);
            realname = view.findViewById(R.id.referee_name);
            referee_head_uri = view.findViewById(R.id.referee_head_uri);
            referee_profile = view.findViewById(R.id.referee_profile);
            thumbsUpIcon = view.findViewById(R.id.thumbs_up_icon);
            commentIcon = view.findViewById(R.id.comment_icon);
            createdView = view.findViewById(R.id.dynamic_time);
            referenceContent = view.findViewById(R.id.reference_content);
            //commentList = (LinearLayout) view.findViewById(R.id.dynamics_comments);

            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.evaluation_item), font);
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
        final MeetReferenceInfo referenceInfo = mReferenceList.get(position);
        holder.realname.setText(referenceInfo.getRefereeName());

    }

    @Override
    public int getItemCount(){
        return mReferenceList.size();
    }
}
