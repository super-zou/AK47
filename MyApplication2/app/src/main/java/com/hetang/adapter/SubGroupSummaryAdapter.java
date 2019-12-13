package com.hetang.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.hetang.R;
import com.hetang.common.HandlerTemp;
import com.hetang.meet.SubGroupActivity;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.RoundImageView;

import java.util.List;

import static com.hetang.util.Utility.dpToPx;

/**
 * Created by super-zou on 18-9-21.
 */

public class SubGroupSummaryAdapter extends RecyclerView.Adapter<SubGroupSummaryAdapter.ViewHolder> {

    private static final String TAG = "MeetsubGroupSummaryAdapter";
    private static Context mContext;
    private int width;
    RequestQueue queue;
    private List<SubGroupActivity.SubGroup> mSubGroupList;
    private boolean isScrolling = false;
    private MyItemClickListener mItemClickListener;

    public SubGroupSummaryAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<SubGroupActivity.SubGroup> subGroupList) {
        mSubGroupList = subGroupList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.subgroup_summary_item, parent, false);
        ViewHolder holder = new ViewHolder(view, mItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull SubGroupSummaryAdapter.ViewHolder holder, final int position) {
        final SubGroupActivity.SubGroup subGroup = mSubGroupList.get(position);
        setContentView(holder, subGroup);

        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mItemClickListener != null){
                    mItemClickListener.onItemClick(view, position);
                }
            }
        });
    }

    public static void setContentView(SubGroupSummaryAdapter.ViewHolder holder, SubGroupActivity.SubGroup subGroup){
        holder.name.setText(subGroup.leader.getName());

        if (subGroup.leader.getAvatar() != null && !"".equals(subGroup.leader.getAvatar())) {
            Glide.with(mContext).load(HttpUtil.DOMAIN + subGroup.leader.getAvatar()).into(holder.leaderHeadUri);
        } else {
            if (subGroup.leader.getSex() == 0){
                holder.leaderHeadUri.setImageDrawable(mContext.getDrawable(R.drawable.male_default_avator));
            }else {
                holder.leaderHeadUri.setImageDrawable(mContext.getDrawable(R.drawable.female_default_avator));
            }
        }
        holder.groupName.setText(subGroup.groupName.trim());
        holder.groupProfile.setText(subGroup.groupProfile.trim());
        if (!TextUtils.isEmpty(subGroup.org)){
            holder.org.setText(subGroup.org.trim());
        }
        if (!TextUtils.isEmpty(subGroup.region)){
            holder.region.setText(subGroup.region.trim());
        }

        /*
        if (!TextUtils.isEmpty(subGroup.created)){
            holder.created.setText(subGroup.created);
        }
        */

        holder.visitRecord.setText(mContext.getResources().getString(R.string.visit)+" "+subGroup.visitRecord);
        holder.followCount.setText(mContext.getResources().getString(R.string.follow)+" "+subGroup.followCount);
        holder.activityCount.setText(mContext.getResources().getString(R.string.dynamics)+" "+subGroup.activityCount);
        holder.memberCount.setText(mContext.getResources().getString(R.string.member)+" "+subGroup.memberCount);

        if (subGroup.groupLogoUri != null && !"".equals(subGroup.groupLogoUri)) {
            Glide.with(mContext).load(HttpUtil.DOMAIN + subGroup.groupLogoUri).into(holder.logo);
        }else {
            holder.logo.setImageDrawable(mContext.getDrawable(R.drawable.icon));
        }

    }

    public void notifySetListDataChanged(List<SubGroupActivity.SubGroup> list){
        this.mSubGroupList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mSubGroupList != null ? mSubGroupList.size() : 0;
    }

    public void setScrolling(boolean isScrolling) {
        this.isScrolling = isScrolling;
    }

   public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
       private MyItemClickListener mListener;
       RoundImageView leaderHeadUri;
        TextView name;
        //TextView baseProfle;
        TextView groupName;
        TextView org;
        TextView created;
        TextView groupProfile;
        RoundImageView logo;
        TextView region;
        TextView memberCount;
        TextView visitRecord;
        TextView activityCount;
        TextView followCount;


        LinearLayout leaderProfile;
        RelativeLayout groupInfo;
        ConstraintLayout itemLayout;

        public ViewHolder(View view, MyItemClickListener myItemClickListener) {
            super(view);
            itemLayout = view.findViewById(R.id.subgroup_summary_item);
            leaderHeadUri = view.findViewById(R.id.leader_head_uri);
            name = view.findViewById(R.id.leader_name);
            //baseProfle = view.findViewById(R.id.base_profile);
            groupName = view.findViewById(R.id.group_name);
            org = view.findViewById(R.id.org);
            //created = view.findViewById(R.id.created);
            groupProfile = view.findViewById(R.id.profile);
            leaderProfile = view.findViewById(R.id.leader_profile);
            logo = view.findViewById(R.id.logo);
            region = view.findViewById(R.id.region);
            memberCount = view.findViewById(R.id.member_count);
            visitRecord = view.findViewById(R.id.visit_record);
            activityCount = view.findViewById(R.id.activity_count);
            followCount = view.findViewById(R.id.follow_count);
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
    public void setItemClickListener(SubGroupSummaryAdapter.MyItemClickListener myItemClickListener) {
        this.mItemClickListener = myItemClickListener;
    }
    
        private void handleMessage(Message message){
        //todo
    }

    static class MyHandler extends HandlerTemp<SubGroupSummaryAdapter> {
        public MyHandler(SubGroupSummaryAdapter cls){
            super(cls);
        }

        @Override
        public void handleMessage(Message message) {
            SubGroupSummaryAdapter meetsubGroupSummaryAdapter = ref.get();
            if (meetsubGroupSummaryAdapter != null) {
                meetsubGroupSummaryAdapter.handleMessage(message);
            }
        }
    }
}