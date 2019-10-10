package com.hetang.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.hetang.R;
import com.hetang.meet.MeetMemberInfo;
import com.hetang.util.HttpUtil;
import com.hetang.util.RequestQueueSingleton;

import java.util.ArrayList;
import java.util.List;

import static com.hetang.util.ParseUtils.getMeetArchive;

public class ImpressionApprovedDetailAdapter extends RecyclerView.Adapter<ImpressionApprovedDetailAdapter.ViewHolder> {
    private static final String TAG = "ImpressionApprovedDetailAdapter";
    RequestQueue queue;
    private ArrayList<MeetMemberInfo> mUnfilteredData;
    private List<MeetMemberInfo> mMemberInfoList;
    private Context mContext;

    public ImpressionApprovedDetailAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<MeetMemberInfo> memberInfoList) {
        mMemberInfoList = memberInfoList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.approved_user_info, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final MeetMemberInfo memberInfo = mMemberInfoList.get(position);
        holder.name.setText(memberInfo.getRealname());
        String profile = "";
        if (memberInfo.getSituation() == 0) {//student
            profile = memberInfo.getUniversity() + "." + memberInfo.getDegree() + "." + memberInfo.getDegree();
        } else {
            profile = memberInfo.getJobTitle() + "." + memberInfo.getCompany();
            if (!"".equals(memberInfo.getLives())) {
                profile += "." + memberInfo.getLives();
            }
        }
        holder.profile.setText(profile.replaceAll(" ", ""));

        if (memberInfo.getPictureUri() != null && !"".equals(memberInfo.getPictureUri())) {
            queue = RequestQueueSingleton.instance(mContext);
            holder.headPic.setTag(HttpUtil.DOMAIN + memberInfo.getPictureUri());
            HttpUtil.loadByImageLoader(queue, holder.headPic, HttpUtil.DOMAIN + memberInfo.getPictureUri(), 50, 50);
        } else {
            holder.headPic.setImageDrawable(mContext.getDrawable(R.mipmap.ic_launcher));
        }

        holder.headPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getMeetArchive(mContext, memberInfo.getUid());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMemberInfoList != null ? mMemberInfoList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView headPic;
        public TextView name;
        public TextView profile;

        public ViewHolder(View view) {
            super(view);
            headPic = view.findViewById(R.id.networkImageView);
            name = view.findViewById(R.id.name);
            profile = view.findViewById(R.id.profile);
        }
    }
}
