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
import com.tongmenhui.launchak47.meet.ArchivesActivity;
import com.tongmenhui.launchak47.meet.MeetMemberInfo;
import com.tongmenhui.launchak47.util.HttpUtil;
import com.tongmenhui.launchak47.util.RequestQueueSingleton;
import com.tongmenhui.launchak47.util.Slog;

import java.util.List;

public class MeetImpressionStatisticsAdapter extends RecyclerView.Adapter<MeetImpressionStatisticsAdapter.ViewHolder> {

    private static final String TAG = "MeetImpressionStatisticsAdapter";
    private static Context mContext;
    RequestQueue queue;
    private List<ArchivesActivity.ImpressionStatistics> mImpressionStatisticsList;
    //private ApprovedUsersDialogFragment approvedUsersDialogFragment;
    private android.support.v4.app.FragmentManager mFragmentManager;

    public MeetImpressionStatisticsAdapter(Context context, android.support.v4.app.FragmentManager fragmentManager) {
        Slog.d(TAG, "==============MeetImpressionStatisticsAdapter init=================");
        mContext = context;
        mFragmentManager = fragmentManager;
    }

    public void setImpressionList(List<ArchivesActivity.ImpressionStatistics> impressionStatisticsList) {
        mImpressionStatisticsList = impressionStatisticsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.impression_statistics_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MeetImpressionStatisticsAdapter.ViewHolder holder, int position) {
        Slog.d(TAG, "========================onBindViewHolder position: " + position);
        final ArchivesActivity.ImpressionStatistics impressionStatistics = mImpressionStatisticsList.get(position);
        holder.feature.setText(impressionStatistics.impression);
        holder.featureCount.setText(String.valueOf(impressionStatistics.impressionCount));

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
            holder.details.setVisibility(View.VISIBLE);
        }

        holder.details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ApprovedUsersActivity.class);
                intent.putExtra("impressionStatistics", impressionStatistics);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mImpressionStatisticsList != null ? mImpressionStatisticsList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView feature;
        TextView featureCount;
        ImageView headPicture1;
        ImageView headPicture2;
        ImageView headPicture3;
        TextView approvedUsers;
        TextView details;


        public ViewHolder(View view) {
            super(view);
            feature = view.findViewById(R.id.feature);
            featureCount = view.findViewById(R.id.feature_count);
            headPicture1 = view.findViewById(R.id.head_picure1);
            headPicture2 = view.findViewById(R.id.head_picure2);
            headPicture3 = view.findViewById(R.id.head_picure3);
            approvedUsers = view.findViewById(R.id.approved_users);
            details = view.findViewById(R.id.details);

            //Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
            //FontManager.markAsIconContainer(view.findViewById(R.id.evaluation_item), font);
        }
    }
}
