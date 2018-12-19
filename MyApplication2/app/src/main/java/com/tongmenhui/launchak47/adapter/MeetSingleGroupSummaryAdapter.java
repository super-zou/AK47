package com.tongmenhui.launchak47.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.bumptech.glide.Glide;
import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.meet.MeetReferenceInfo;
import com.tongmenhui.launchak47.meet.MeetSingleGroupFragment;
import com.tongmenhui.launchak47.util.FontManager;
import com.tongmenhui.launchak47.util.HttpUtil;
import com.tongmenhui.launchak47.util.Slog;
import com.tongmenhui.launchak47.util.Utility;

import java.util.List;

/**
 * Created by super-zou on 18-9-21.
 */

public class MeetSingleGroupSummaryAdapter extends RecyclerView.Adapter<MeetSingleGroupSummaryAdapter.ViewHolder> {

    private static final String TAG = "MeetSingleGroupSummaryAdapter";
    private static Context mContext;
    RequestQueue queue;
    private List<MeetSingleGroupFragment.SingleGroup> mSingleGroupList;
    private boolean isScrolling = false;

    public MeetSingleGroupSummaryAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<MeetSingleGroupFragment.SingleGroup> singleGroupList) {
        mSingleGroupList = singleGroupList;
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_group_summary_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }
    
    @Override
    public void onBindViewHolder(@NonNull MeetSingleGroupSummaryAdapter.ViewHolder holder, int position) {
        final MeetSingleGroupFragment.SingleGroup singleGroup = mSingleGroupList.get(position);

        holder.name.setText(singleGroup.leader.getRealname());

        if (singleGroup.leader.getPictureUri() != null && !"".equals(singleGroup.leader.getPictureUri())) {
            Glide.with(mContext).load(HttpUtil.DOMAIN + singleGroup.leader.getPictureUri()).into(holder.leaderHeadUri);
        } else {
            holder.leaderHeadUri.setImageDrawable(mContext.getDrawable(R.mipmap.ic_launcher));
        }
        
        if(singleGroup.leader.getSituation() == 0){
            holder.major.setText(singleGroup.leader.getMajor());
            holder.degree.setText(singleGroup.leader.getDegree());
            holder.university.setText(singleGroup.leader.getUniversity());
        }else {
            holder.educationBackGround.setVisibility(View.GONE);
            holder.workInfo.setVisibility(View.VISIBLE);

            holder.title.setText(singleGroup.leader.getJobTitle());
            //holder.company.setText(singleGroup.leader.getCompany());
            holder.living.setText(singleGroup.leader.getLives().trim());
        }
        
        holder.groupName.setText(singleGroup.groupName);
        holder.groupProfile.setText(singleGroup.groupProfile);
        holder.org.setText("来自"+singleGroup.org);
        //holder.created.setText(singleGroup.created);
        if(singleGroup.memberCountRemain > 0){
            holder.memberCount.setVisibility(View.VISIBLE);
            holder.memberCount.setText("+"+String.valueOf(singleGroup.memberCountRemain));
        }
        
        if (singleGroup.headUrlList != null && singleGroup.headUrlList.size() > 0 && !isScrolling) {
            if(holder.memberSummary.getChildCount() > 0){
                holder.memberSummary.removeAllViews();
            }
            for (int i=0; i<singleGroup.headUrlList.size(); i++){
                ImageView imageView = new ImageView(mContext);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT,1.0f);
                layoutParams.rightMargin = 8;
                imageView.setLayoutParams(layoutParams);
                holder.memberSummary.addView(imageView);
                Glide.with(mContext).load(HttpUtil.DOMAIN + singleGroup.leader.getPictureUri()).into(imageView);
            }
        }
    }
    
    @Override
    public int getItemCount() {
        return mSingleGroupList != null ? mSingleGroupList.size() : 0;
    }

    public void setScrolling(boolean isScrolling) {
        this.isScrolling = isScrolling;
    }
    
        public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView leaderHeadUri;
        TextView name;
        RelativeLayout educationBackGround;
        LinearLayout workInfo;
        TextView major;
        TextView degree;
        TextView university;
        TextView title;
        //TextView company;
        TextView living;
        TextView groupName;
        TextView org;
        //TextView created;
        TextView groupProfile;

        LinearLayout memberSummary;
        TextView memberCount;
        
        public ViewHolder(View view) {
            super(view);
            leaderHeadUri = view.findViewById(R.id.leader_head_uri);
            name = view.findViewById(R.id.leader_name);
            educationBackGround = view.findViewById(R.id.education_background);
            workInfo = view.findViewById(R.id.work_info);
            major = view.findViewById(R.id.major);
            degree = view.findViewById(R.id.degree);
            university = view.findViewById(R.id.university);
            title = view.findViewById(R.id.title);
            //company = view.findViewById(R.id.company);
            living = view.findViewById(R.id.living);
            groupName = view.findViewById(R.id.group_name);
            org = view.findViewById(R.id.org);
            //created = view.findViewById(R.id.created);
            groupProfile = view.findViewById(R.id.profile);

            memberSummary = view.findViewById(R.id.member_summary);
            memberCount = view.findViewById(R.id.member_count);

            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.reference_item), font);
        }
    }
}
