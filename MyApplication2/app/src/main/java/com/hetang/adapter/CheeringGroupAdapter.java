package com.hetang.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.RequestQueue;
import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.meet.MeetMemberInfo;
import com.hetang.util.HttpUtil;
import com.hetang.util.Slog;

import java.util.List;

public class CheeringGroupAdapter extends RecyclerView.Adapter<CheeringGroupAdapter.ViewHolder>{
    private static final String TAG = "CheeringGroupAdapter";
    private static Context mContext;
    RequestQueue queue;
    private List<MeetMemberInfo> mCheeringGroupList;
        private MyItemClickListener mItemClickListener;

    public CheeringGroupAdapter(Context context) {
        mContext = context;
    }
    
        public void setCheeringGroupList(List<MeetMemberInfo> cheeringGroupList) {
        mCheeringGroupList = cheeringGroupList;
    }

    @Override
    public CheeringGroupAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cheering_group_item, parent, false);
        CheeringGroupAdapter.ViewHolder holder = new CheeringGroupAdapter.ViewHolder(view, mItemClickListener);
        return holder;
    }
    
        @Override
    public void onBindViewHolder(@NonNull CheeringGroupAdapter.ViewHolder holder, int position) {
        final MeetMemberInfo cheeringGroup = mCheeringGroupList.get(position);
        holder.realName.setText(cheeringGroup.getRealname());
        //holder.profile.setText(cheeringGroup.getProfile());
        
        if(cheeringGroup.getSituation() == 0){
            Slog.d(TAG, "===============get degree: "+cheeringGroup.getDegree()+"   uid: "+cheeringGroup.getUid());
            holder.degree.setText(cheeringGroup.getDegree());
            holder.university.setText(cheeringGroup.getUniversity());
        }else {
            holder.education.setVisibility(View.GONE);
            holder.work.setVisibility(View.VISIBLE);
            holder.title.setText(cheeringGroup.getJobTitle());
            holder.company.setText(cheeringGroup.getCompany());
        }

        if (cheeringGroup.getPictureUri() != null && !"".equals(cheeringGroup.getPictureUri())) {
            Glide.with(mContext).load(HttpUtil.DOMAIN + cheeringGroup.getPictureUri()).into(holder.headUri);
            //queue = RequestQueueSingleton.instance(mContext);
            //holder.headUri.setTag(HttpUtil.DOMAIN + cheeringGroup.getPictureUri());
            //HttpUtil.loadByImageLoader(queue, holder.headUri, HttpUtil.DOMAIN + cheeringGroup.getPictureUri(), 37, 60);
        } else {
            holder.headUri.setImageDrawable(mContext.getDrawable(R.mipmap.ic_launcher));
        }

    }
    
        @Override
    public int getItemCount() {
        return mCheeringGroupList != null ? mCheeringGroupList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private MyItemClickListener mListener;
        TextView realName;
        TextView degree;
        TextView university;
        TextView title;
        TextView company;
        LinearLayout education;
        LinearLayout work;
        ImageView headUri;

        public ViewHolder(View view, MyItemClickListener myItemClickListener) {
            super(view);
            realName = view.findViewById(R.id.name);
            headUri = view.findViewById(R.id.head_uri);
            education = view.findViewById(R.id.education);
            degree = view.findViewById(R.id.degree);
            university = view.findViewById(R.id.university);
            work = view.findViewById(R.id.work);
            title = view.findViewById(R.id.title);
            company = view.findViewById(R.id.company);

            //将全局的监听赋值给接口
            this.mListener = myItemClickListener;
            itemView.setOnClickListener(this);
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
    public void setItemClickListener(CheeringGroupAdapter.MyItemClickListener myItemClickListener) {
        this.mItemClickListener = myItemClickListener;
    }
}
