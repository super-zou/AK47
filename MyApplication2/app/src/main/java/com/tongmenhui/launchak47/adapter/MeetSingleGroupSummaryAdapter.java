package com.tongmenhui.launchak47.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
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
import com.tongmenhui.launchak47.main.ArchiveActivity;
import com.tongmenhui.launchak47.meet.SingleGroupDetailsActivity;
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
    private int width;
    RequestQueue queue;
    private List<MeetSingleGroupFragment.SingleGroup> mSingleGroupList;
    private boolean isScrolling = false;
    private MyItemClickListener mItemClickListener;

    public MeetSingleGroupSummaryAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<MeetSingleGroupFragment.SingleGroup> singleGroupList, int parentWidth) {
        mSingleGroupList = singleGroupList;
        width = parentWidth;
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_group_summary_item, parent, false);
        ViewHolder holder = new ViewHolder(view, mItemClickListener);
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
        }else {
            holder.memberCount.setVisibility(View.GONE);
        }

        holder.memberSummary.removeAllViews();
        
        if (singleGroup.headUrlList != null && singleGroup.headUrlList.size() > 0 && !isScrolling) {
            for (int i=0; i<singleGroup.headUrlList.size(); i++){
                ImageView imageView = new ImageView(mContext);
                LinearLayout.LayoutParams layoutParams;
                if(singleGroup.headUrlList.size() > 1){
                    //layoutParams= new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT,1.0f);
                    int itemWidth = width/singleGroup.headUrlList.size();
                    int itemHeight = itemWidth;
                    layoutParams= new LinearLayout.LayoutParams(itemWidth, itemHeight);

                }else {
                    int itemWidth = width/2;
                    int itemHeight = itemWidth;
                    layoutParams= new LinearLayout.LayoutParams(itemWidth, itemHeight);
                }
                layoutParams.rightMargin = 2;
                imageView.setLayoutParams(layoutParams);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                holder.memberSummary.addView(imageView);
                Glide.with(mContext).load(HttpUtil.DOMAIN + singleGroup.headUrlList.get(i)).into(imageView);
          }
        }
        holder.leaderProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Slog.d(TAG, "========click leader");
                Intent intent = new Intent(mContext, ArchiveActivity.class);
                intent.putExtra("uid", singleGroup.leader.getUid());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                mContext.startActivity(intent);
            }
        });
        
        holder.groupInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Slog.d(TAG, "=========click group");

                Intent intent = new Intent(mContext, SingleGroupDetailsActivity.class);
                intent.putExtra("gid", singleGroup.gid);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                mContext.startActivity(intent);

            }
        });
    }
    
    public void notifySetListDataChanged(List<MeetSingleGroupFragment.SingleGroup> list){
        this.mSingleGroupList = list;
        notifyDataSetChanged();
    }
    
    @Override
    public int getItemCount() {
        return mSingleGroupList != null ? mSingleGroupList.size() : 0;
    }

    public void setScrolling(boolean isScrolling) {
        this.isScrolling = isScrolling;
    }
    
   public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
       private MyItemClickListener mListener;
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
       
       ConstraintLayout leaderProfile;
        ConstraintLayout groupInfo;
        
        public ViewHolder(View view, MyItemClickListener myItemClickListener) {
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
            leaderProfile = view.findViewById(R.id.leader_profile);
            groupInfo = view.findViewById(R.id.group_info);

            //将全局的监听赋值给接口
            this.mListener = myItemClickListener;
            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.reference_item), font);
        }
       /**
         * 实现OnClickListener接口重写的方法
         * @param v
         */
        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(v, getPosition());
            }

        }
    }
    
    /**
     * 创建一个回调接口
     */
    public interface MyItemClickListener {
        void onItemClick(View view, int position);
    }

    /**
     * 在activity里面adapter就是调用的这个方法,将点击事件监听传递过来,并赋值给全局的监听
     *
     * @param myItemClickListener
     */
    public void setItemClickListener(MeetSingleGroupSummaryAdapter.MyItemClickListener myItemClickListener) {
        this.mItemClickListener = myItemClickListener;
    }
}
