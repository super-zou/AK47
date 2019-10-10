package com.hetang.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

import com.android.volley.RequestQueue;
import com.hetang.launchak47.R;
import com.hetang.meet.MeetReferenceInfo;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.Slog;

import java.util.List;

/**
 * Created by super-zou on 18-9-21.
 */

public class MeetReferenceAdapter extends RecyclerView.Adapter<MeetReferenceAdapter.ReferenceViewHolder> {

    private static final String TAG = "MeetReferenceAdapter";
    private static Context mContext;
    RequestQueue queue;
    private List<MeetReferenceInfo> mReferenceList;

    public MeetReferenceAdapter(Context context) {
        Slog.d(TAG, "==============MeetReferenceAdapter init=================");
        mContext = context;

    }

    public void setReferenceList(List<MeetReferenceInfo> referenceList) {
        mReferenceList = referenceList;
    }

    @Override
    public ReferenceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reference_item, parent, false);
        ReferenceViewHolder holder = new ReferenceViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MeetReferenceAdapter.ReferenceViewHolder holder, int position) {
        final MeetReferenceInfo referenceInfo = mReferenceList.get(position);
        holder.realName.setText(referenceInfo.getRefereeName());
        holder.relation.setText(referenceInfo.getRelation());
        holder.refereeProfile.setText(referenceInfo.getRefereeProfile());
        holder.referenceContent.setText(referenceInfo.getReferenceContent());
        //holder.createdView.setText(referenceInfo.getCreated().toString());

        if (referenceInfo.getHeadUri() != null && !"".equals(referenceInfo.getHeadUri())) {
            Glide.with(mContext).load(HttpUtil.DOMAIN + referenceInfo.getHeadUri()).into(holder.refereeHeadUri);
        } else {
            holder.refereeHeadUri.setImageDrawable(mContext.getDrawable(R.mipmap.ic_launcher));
        }

    }

    @Override
    public int getItemCount() {
        return mReferenceList != null ? mReferenceList.size() : 0;
    }

    public class ReferenceViewHolder extends RecyclerView.ViewHolder {
        TextView realName;
        TextView relation;
        TextView refereeProfile;
        ImageView refereeHeadUri;
        TextView illustration;
        TextView eyeView;
        TextView lovedView;
        TextView lovedIcon;
        TextView thumbsView;
        TextView thumbsUpIcon;
        TextView referenceContent;
        TextView createdView;
        TextView commentIcon;

        public ReferenceViewHolder(View view) {
            super(view);
            realName = view.findViewById(R.id.referee_name);
            relation = view.findViewById(R.id.relation);
            refereeHeadUri = view.findViewById(R.id.referee_head_uri);
            refereeProfile = view.findViewById(R.id.referee_profile);
            thumbsUpIcon = view.findViewById(R.id.thumbs_up_icon);
            commentIcon = view.findViewById(R.id.comment_icon);
            createdView = view.findViewById(R.id.dynamic_time);
            referenceContent = view.findViewById(R.id.reference_content);
            //commentList = (LinearLayout) view.findViewById(R.id.dynamics_comments);

            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.reference_item), font);
        }
    }
}
