package com.mufu.adapter;

import android.content.Context;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.bumptech.glide.Glide;
import com.mufu.R;
import com.mufu.main.MeetArchiveFragment;
import com.mufu.meet.UserMeetInfo;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.RoundImageView;

import java.util.List;

public class MeetImpressionStatisticsAdapter extends RecyclerView.Adapter<MeetImpressionStatisticsAdapter.ViewHolder> {

    private static final String TAG = "MeetImpressionStatisticsAdapter";
    private static Context mContext;
    RequestQueue queue;
    private MyItemClickListener mItemClickListener;
    private List<MeetArchiveFragment.ImpressionStatistics> mImpressionStatisticsList;
    //private ApprovedUsersDialogFragment approvedUsersDialogFragment;
    private FragmentManager mFragmentManager;

    public MeetImpressionStatisticsAdapter(Context context, FragmentManager fragmentManager) {

        mContext = context;
        mFragmentManager = fragmentManager;
    }

    public void setImpressionList(List<MeetArchiveFragment.ImpressionStatistics> impressionStatisticsList) {
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
        final MeetArchiveFragment.ImpressionStatistics impressionStatistics = mImpressionStatisticsList.get(position);
        if (impressionStatistics.impression != null && impressionStatistics.impression.length() > 0) {
            String impression;
            if (impressionStatistics.impression.length() < 12) {
                impression = impressionStatistics.impression;
            } else {
                impression = impressionStatistics.impression.substring(0, 12) + "...";
            }
            holder.feature.setText(impression + " · " + String.valueOf(impressionStatistics.impressionCount));
        }

        int memberSize = impressionStatistics.meetMemberList.size();
        if (memberSize > 0) {
            int size = 3;
            if (memberSize < 3) {
                size = memberSize;
            }
            for (int i = 0; i < size; i++) {
                RoundImageView avatarView = new RoundImageView(mContext);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(80, 80);
                lp.setMarginStart(3);
                UserMeetInfo userMeetInfo = impressionStatistics.meetMemberList.get(i);
                String avatarURL = userMeetInfo.getAvatar();
                if (avatarURL != null && !"".equals(avatarURL)) {
                    Glide.with(mContext).load(HttpUtil.DOMAIN + avatarURL).into(avatarView);
                }
                holder.avatarAbstractLL.addView(avatarView, lp);
            }
            if (memberSize > 5) {
                TextView textView = new TextView(mContext);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                textView.setText("等");
                holder.avatarAbstractLL.addView(textView, lp);
            }

        }
    }

    @Override
    public int getItemCount() {
        return mImpressionStatisticsList != null ? mImpressionStatisticsList.size() : 0;
    }

    /**
     * 在activity里面adapter就是调用的这个方法,将点击事件监听传递过来,并赋值给全局的监听
     *
     * @param myItemClickListener
     */
    public void setItemClickListener(MyItemClickListener myItemClickListener) {
        this.mItemClickListener = myItemClickListener;
    }

    /**
     * 创建一个回调接口
     */
    public interface MyItemClickListener {
        void onItemClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView feature;
        LinearLayout avatarAbstractLL;
        TextView approvedUsers;
        private MyItemClickListener mListener;


        public ViewHolder(View view, MyItemClickListener myItemClickListener) {
            super(view);
            feature = view.findViewById(R.id.feature);

            avatarAbstractLL = view.findViewById(R.id.avatar_abstract);
            approvedUsers = view.findViewById(R.id.approved_users);
            //将全局的监听赋值给接口
            this.mListener = myItemClickListener;
            itemView.setOnClickListener(this);

            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.approved_users), font);
        }

        /**
         * 实现OnClickListener接口重写的方法
         *
         * @param v
         */
        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(v, getPosition());
            }

        }


    }
}
