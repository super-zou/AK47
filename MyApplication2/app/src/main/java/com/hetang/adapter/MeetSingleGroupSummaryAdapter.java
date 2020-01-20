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
import com.hetang.group.SingleGroupActivity;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.RoundImageView;
import com.hetang.util.UserProfile;

import java.util.List;

import static com.hetang.common.MyApplication.getContext;
import static com.hetang.util.Utility.dpToPx;

/**
 * Created by super-zou on 18-9-21.
 */

public class MeetSingleGroupSummaryAdapter extends RecyclerView.Adapter<MeetSingleGroupSummaryAdapter.ViewHolder> {

    private static final String TAG = "MeetSingleGroupSummaryAdapter";
    private static Context mContext;
    private int width;
    RequestQueue queue;
    private List<SingleGroupActivity.SingleGroup> mSingleGroupList;
    private boolean isScrolling = false;
    private MyItemClickListener mItemClickListener;

    public MeetSingleGroupSummaryAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<SingleGroupActivity.SingleGroup> singleGroupList, int parentWidth) {
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
         final SingleGroupActivity.SingleGroup singleGroup = mSingleGroupList.get(position);
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
    
    public static void setContentView(MeetSingleGroupSummaryAdapter.ViewHolder holder, SingleGroupActivity.SingleGroup singleGroup){
        holder.name.setText(singleGroup.leader.getNickName());

        if (singleGroup.leader.getAvatar() != null && !"".equals(singleGroup.leader.getAvatar())) {
            Glide.with(mContext).load(HttpUtil.DOMAIN + singleGroup.leader.getAvatar()).into(holder.leaderAvatar);
        } else {
            if (singleGroup.leader.getSex() == 0){
                holder.leaderAvatar.setImageDrawable(mContext.getDrawable(R.drawable.male_default_avator));
            }else {
                holder.leaderAvatar.setImageDrawable(mContext.getDrawable(R.drawable.female_default_avator));
            }
        }
        /*
        holder.groupName.setText(singleGroup.groupName.trim());
        holder.groupProfile.setText(singleGroup.groupProfile.trim());
        if (!TextUtils.isEmpty(singleGroup.org)){
            holder.org.setText("来自 "+singleGroup.org.trim());
        }
        */

        holder.name.setText(singleGroup.leader.getNickName().trim());
        holder.university.setText(singleGroup.leader.getUniversity().trim());

        holder.introduction.setText(singleGroup.introduction);
        holder.maleCount.setText(mContext.getResources().getString(R.string.male)+" "+singleGroup.maleCount);
        holder.femaleCount.setText(mContext.getResources().getString(R.string.female)+" "+singleGroup.femaleCount);
        if (singleGroup.evaluateCount > 0){
            float scoreFloat = singleGroup.evaluateScores/singleGroup.evaluateCount;
            float score = (float)(Math.round(scoreFloat*10))/10;
            holder.evaluateCount.setText(score+mContext.getResources().getString(R.string.dot)+singleGroup.evaluateCount);
        }else {
            holder.evaluateCount.setText("");
        }
        
        if (singleGroup.memberList != null && singleGroup.memberList.size() > 0) {
             holder.memberWrapper.setVisibility(View.VISIBLE);
             if (holder.memberWrapper.getTag() == null){
                 setMemberView(singleGroup, holder);
             }else {
                 if (!singleGroup.equals(holder.memberWrapper.getTag())){
                     holder.memberWrapper.removeAllViews();
                     setMemberView(singleGroup, holder);
                 }
             }
        }else {
            holder.memberWrapper.setVisibility(View.GONE);
            /*
             if (holder.memberWrapper.getChildCount() > 0){
                 holder.memberWrapper.removeAllViews();
             }

             */
        }
        
    }
    
    public static void setMemberView(SingleGroupActivity.SingleGroup singleGroup, ViewHolder holder){
        for (int i=0; i<singleGroup.memberList.size(); i++){
            View view = LayoutInflater.from(getContext()).inflate(R.layout.single_group_member_item, null);
            holder.memberWrapper.addView(view);
            UserProfile member = singleGroup.memberList.get(i);
            RoundImageView memberAvatar = view.findViewById(R.id.avatar);
            String avatar = member.getAvatar();
            if (avatar != null && !"".equals(avatar)) {
                Glide.with(getContext()).load(HttpUtil.DOMAIN + avatar).into(memberAvatar);
            }

            TextView degree = view.findViewById(R.id.degree);
            degree.setText(member.getDegreeName(member.getDegree()));
            TextView major = view.findViewById(R.id.major);
            major.setText(member.getMajor());
            TextView university = view.findViewById(R.id.university);
            university.setText(member.getUniversity());
        }

        holder.memberWrapper.setTag(singleGroup);
    }
        
    
    public void notifySetListDataChanged(List<SingleGroupActivity.SingleGroup> list){
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
       RoundImageView leaderAvatar;
        TextView name;
        TextView university;
        TextView introduction;
        TextView maleCount;
        TextView femaleCount;
        TextView evaluateCount;
        LinearLayout memberWrapper;
        ConstraintLayout itemLayout;         
        
        public ViewHolder(View view, MyItemClickListener myItemClickListener) {
            super(view);
            itemLayout = view.findViewById(R.id.single_group_summary_item);
            leaderAvatar = view.findViewById(R.id.leader_avatar);
            name = view.findViewById(R.id.leader_name);
            university = view.findViewById(R.id.university);
            //created = view.findViewById(R.id.created);
            introduction = view.findViewById(R.id.introduction);
            maleCount = view.findViewById(R.id.male_member_count);
            femaleCount = view.findViewById(R.id.female_member_count);
            evaluateCount = view.findViewById(R.id.evaluate_count);
            memberWrapper = view.findViewById(R.id.member_wrapper);
            
            //将全局的监听赋值给接口
            this.mListener = myItemClickListener;
            Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
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
