package com.tongmenhui.launchak47.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.bumptech.glide.Glide;
import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.meet.ApprovedUsersActivity;
import com.tongmenhui.launchak47.meet.MeetArchivesActivity;
import com.tongmenhui.launchak47.meet.MeetMemberInfo;
import com.tongmenhui.launchak47.util.HttpUtil;
import com.tongmenhui.launchak47.util.RequestQueueSingleton;
import com.tongmenhui.launchak47.util.Slog;

import java.util.List;

public class MeetImpressionStatisticsAdapter extends RecyclerView.Adapter<MeetImpressionStatisticsAdapter.ViewHolder> {

    private static final String TAG = "MeetImpressionStatisticsAdapter";
    private static Context mContext;
    RequestQueue queue;
    private MyItemClickListener mItemClickListener;
    private List<MeetArchivesActivity.ImpressionStatistics> mImpressionStatisticsList;
    //private ApprovedUsersDialogFragment approvedUsersDialogFragment;
    private android.support.v4.app.FragmentManager mFragmentManager;

    public MeetImpressionStatisticsAdapter(Context context, android.support.v4.app.FragmentManager fragmentManager) {

        mContext = context;
        mFragmentManager = fragmentManager;
    }

    public void setImpressionList(List<MeetArchivesActivity.ImpressionStatistics> impressionStatisticsList) {
        mImpressionStatisticsList = impressionStatisticsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.impression_statistics_item, parent, false);
        ViewHolder holder = new ViewHolder(view, mItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MeetImpressionStatisticsAdapter.ViewHolder holder, int position) {

        final MeetArchivesActivity.ImpressionStatistics impressionStatistics = mImpressionStatisticsList.get(position);
        holder.feature.setText(impressionStatistics.impression + " · " + String.valueOf(impressionStatistics.impressionCount));

        if(impressionStatistics.meetMemberList.size() > 0){
            MeetMemberInfo meetMemberInfo = impressionStatistics.meetMemberList.get(0);
            if (meetMemberInfo.getPictureUri() != null && !"".equals(meetMemberInfo.getPictureUri())) {
                queue = RequestQueueSingleton.instance(mContext);
                /*+Begin: added by xuchunping for Use glide loader image, 2018/11/28*/
                //holder.headPicture1.setTag(HttpUtil.DOMAIN + meetMemberInfo.getPictureUri());
                //HttpUtil.loadByImageLoader(queue, holder.headPicture1, HttpUtil.DOMAIN + meetMemberInfo.getPictureUri(), 50, 50);
                Glide.with(mContext).load(HttpUtil.DOMAIN + meetMemberInfo.getPictureUri()).into(holder.headPicture1);
                /*-End: added by xuchunping for Use glide loader image*, 2018/11/28*/
            } else {
                holder.headPicture1.setImageDrawable(mContext.getDrawable(R.mipmap.ic_launcher));
            }

            if (impressionStatistics.meetMemberList.size() > 1) {
                holder.headPicture2.setVisibility(View.VISIBLE);
                meetMemberInfo = impressionStatistics.meetMemberList.get(1);
                if (meetMemberInfo.getPictureUri() != null && !"".equals(meetMemberInfo.getPictureUri())) {
                    queue = RequestQueueSingleton.instance(mContext);
                    /*+Begin: added by xuchunping for Use glide loader image, 2018/11/28*/
                    //holder.headPicture2.setTag(HttpUtil.DOMAIN + meetMemberInfo.getPictureUri());
                    //HttpUtil.loadByImageLoader(queue, holder.headPicture2, HttpUtil.DOMAIN + meetMemberInfo.getPictureUri(), 50, 50);
                    Glide.with(mContext).load(HttpUtil.DOMAIN + meetMemberInfo.getPictureUri()).into(holder.headPicture2);
                    /*-End: added by xuchunping for Use glide loader image*, 2018/11/28*/
                } else {
                    holder.headPicture2.setImageDrawable(mContext.getDrawable(R.mipmap.ic_launcher));
                }
                if (impressionStatistics.meetMemberList.size() > 2) {
                    holder.headPicture3.setVisibility(View.VISIBLE);
                    meetMemberInfo = impressionStatistics.meetMemberList.get(2);
                    if (meetMemberInfo.getPictureUri() != null && !"".equals(meetMemberInfo.getPictureUri())) {
                        queue = RequestQueueSingleton.instance(mContext);
                        /*+Begin: added by xuchunping for Use glide loader image, 2018/11/28*/
                        //holder.headPicture3.setTag(HttpUtil.DOMAIN + meetMemberInfo.getPictureUri());
                        //HttpUtil.loadByImageLoader(queue, holder.headPicture3, HttpUtil.DOMAIN + meetMemberInfo.getPictureUri(), 50, 50);
                        Glide.with(mContext).load(HttpUtil.DOMAIN + meetMemberInfo.getPictureUri()).into(holder.headPicture3);
                        /*-End: added by xuchunping for Use glide loader image*, 2018/11/28*/
                    } else {
                        holder.headPicture3.setImageDrawable(mContext.getDrawable(R.mipmap.ic_launcher));
                    }
                }
            }

            if (impressionStatistics.meetMemberList.size() <= 3) {
                holder.approvedUsers.setText("认可");
            } else {
                holder.approvedUsers.setText("等" + impressionStatistics.meetMemberList.size() + "人认可");

            }
        }
    }

    @Override
    public int getItemCount() {
        return mImpressionStatisticsList != null ? mImpressionStatisticsList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private MyItemClickListener mListener;
        TextView feature;
        ImageView headPicture1;
        ImageView headPicture2;
        ImageView headPicture3;
        TextView approvedUsers;


        public ViewHolder(View view, MyItemClickListener myItemClickListener) {
            super(view);
            feature = view.findViewById(R.id.feature);

            headPicture1 = view.findViewById(R.id.head_picure1);
            headPicture2 = view.findViewById(R.id.head_picure2);
            headPicture3 = view.findViewById(R.id.head_picure3);
            approvedUsers = view.findViewById(R.id.approved_users);

                        //将全局的监听赋值给接口
            this.mListener = myItemClickListener;
            itemView.setOnClickListener(this);

            //Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
            //FontManager.markAsIconContainer(view.findViewById(R.id.evaluation_item), font);
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
    public void setItemClickListener(MyItemClickListener myItemClickListener) {
        this.mItemClickListener = myItemClickListener;
    }
}
