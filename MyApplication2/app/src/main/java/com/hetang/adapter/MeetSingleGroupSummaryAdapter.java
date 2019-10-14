package com.hetang.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.annotation.NonNull;
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
import com.hetang.meet.MeetSingleGroupFragment;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.RoundImageView;

import java.util.List;

import static com.hetang.util.Utility.dpToPx;

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
    public void onBindViewHolder(@NonNull MeetSingleGroupSummaryAdapter.ViewHolder holder, final int position) {
         final MeetSingleGroupFragment.SingleGroup singleGroup = mSingleGroupList.get(position);
        setContentView(holder, singleGroup);

        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mItemClickListener != null){
                    mItemClickListener.onItemClick(view, position);
                }
            }
        });
    }
    
    public static void setContentView(MeetSingleGroupSummaryAdapter.ViewHolder holder, MeetSingleGroupFragment.SingleGroup singleGroup){
        holder.name.setText(singleGroup.leader.getName());

        if (singleGroup.leader.getAvatar() != null && !"".equals(singleGroup.leader.getAvatar())) {
            Glide.with(mContext).load(HttpUtil.DOMAIN + singleGroup.leader.getAvatar()).into(holder.leaderHeadUri);
        } else {
            if (singleGroup.leader.getSex() == 0){
                holder.leaderHeadUri.setImageDrawable(mContext.getDrawable(R.drawable.male_default_avator));
            }else {
                holder.leaderHeadUri.setImageDrawable(mContext.getDrawable(R.drawable.female_default_avator));
            }
        }
        holder.groupName.setText(singleGroup.groupName.trim());
        holder.groupProfile.setText(singleGroup.groupProfile.trim());
        if (!TextUtils.isEmpty(singleGroup.org)){
            holder.org.setText("来自 "+singleGroup.org.trim());
        }

        if (singleGroup.headUrlList != null && singleGroup.headUrlList.size() > 0 ) {
            holder.divider.setVisibility(View.VISIBLE);
            holder.memberSummary.setVisibility(View.VISIBLE);
            //if(!isScrolling){
            if(holder.memberSummary.getTag() == null){
                setMemberAvatarView(singleGroup, holder);
            }else {
                if (!singleGroup.equals(holder.memberSummary.getTag())){
                    holder.memberSummary.removeAllViews();
                    setMemberAvatarView(singleGroup, holder);
                }
            }

            // }
        }else {
            holder.divider.setVisibility(View.GONE);
            holder.memberSummary.setVisibility(View.GONE);
            if(holder.memberSummary.getChildCount() > 0){
                holder.memberSummary.removeAllViews();
            }
        }
        if (singleGroup.memberCountRemain > 0){
            holder.membeRemainsCount.setVisibility(View.VISIBLE);
            holder.membeRemainsCount.setText("+"+singleGroup.memberCountRemain);
        }else {
            holder.membeRemainsCount.setVisibility(View.GONE);
        }
    }
    
    public static void setMemberAvatarView(MeetSingleGroupFragment.SingleGroup singleGroup, ViewHolder holder){
        int avatarCount = singleGroup.headUrlList.size();
        for (int i=0; i<avatarCount; i++){
            RoundImageView imageView = new RoundImageView(mContext);
            LinearLayout.LayoutParams layoutParams;
            float itemWidth = dpToPx(mContext, 100);
            float itemHeight = itemWidth;
            layoutParams = new LinearLayout.LayoutParams((int)itemWidth, (int)itemHeight);
            layoutParams.rightMargin = 2;
            imageView.setLayoutParams(layoutParams);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            holder.memberSummary.addView(imageView, i);
            String avatar = singleGroup.headUrlList.get(i);

            final RequestOptions requestOptions = new RequestOptions()
                    .placeholder(mContext.getDrawable(R.mipmap.hetang_icon))
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE);
             if (avatar != null && !"null".equals(avatar)){
                Glide.with(mContext).load(HttpUtil.DOMAIN + avatar).apply(requestOptions).into(imageView);
            }
        }

        holder.memberSummary.setTag(singleGroup);
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
    
   public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
       private MyItemClickListener mListener;
                RoundImageView leaderHeadUri;
        TextView name;
        //TextView baseProfle;
        TextView groupName;
        TextView org;
        //TextView created;
        TextView groupProfile;
        View divider;
        LinearLayout memberSummary;
        TextView membeRemainsCount;
       
        LinearLayout leaderProfile;
        RelativeLayout groupInfo;
        ConstraintLayout itemLayout;
        
        public ViewHolder(View view, MyItemClickListener myItemClickListener) {
            super(view);
            itemLayout = view.findViewById(R.id.single_group_summary_item);
            leaderHeadUri = view.findViewById(R.id.leader_head_uri);
            name = view.findViewById(R.id.leader_name);
            //baseProfle = view.findViewById(R.id.base_profile);
            groupName = view.findViewById(R.id.group_name);
            org = view.findViewById(R.id.org);
            //created = view.findViewById(R.id.created);
            groupProfile = view.findViewById(R.id.profile);
            divider = view.findViewById(R.id.divider);
            memberSummary = view.findViewById(R.id.member_summary);
            membeRemainsCount = view.findViewById(R.id.remains);
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
    
        private void handleMessage(Message message){
        //todo
    }

    static class MyHandler extends HandlerTemp<MeetSingleGroupSummaryAdapter> {
        public MyHandler(MeetSingleGroupSummaryAdapter cls){
            super(cls);
        }

        @Override
        public void handleMessage(Message message) {
            MeetSingleGroupSummaryAdapter meetSingleGroupSummaryAdapter = ref.get();
            if (meetSingleGroupSummaryAdapter != null) {
                meetSingleGroupSummaryAdapter.handleMessage(message);
            }
        }
    }
}
