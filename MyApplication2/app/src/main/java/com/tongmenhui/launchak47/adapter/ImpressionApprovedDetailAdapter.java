package com.tongmenhui.launchak47.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.meet.MeetMemberInfo;
import com.tongmenhui.launchak47.util.HttpUtil;
import com.tongmenhui.launchak47.util.RequestQueueSingleton;
import com.tongmenhui.launchak47.util.SearchUserListAdapter;
import com.tongmenhui.launchak47.util.Slog;

import java.util.ArrayList;
import java.util.List;

public class ImpressionApprovedDetailAdapter extends RecyclerView.Adapter<ImpressionApprovedDetailAdapter.ViewHolder> {
    private static final String TAG = "ImpressionApprovedDetailAdapter";
    private ArrayList<MeetMemberInfo> mUnfilteredData;
    private List<MeetMemberInfo> mMemberInfoList;
    private Context mContext;
    RequestQueue queue;

    public ImpressionApprovedDetailAdapter(Context context){
        mContext = context;
    }

    public void setData(List<MeetMemberInfo> memberInfoList){
        mMemberInfoList = memberInfoList;
    }
    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView headPic;
        public TextView name;
        public TextView profile;

        public ViewHolder(View view){
            super(view);
            headPic =  view.findViewById(R.id.networkImageView);
            name = view.findViewById(R.id.name);
            profile = view.findViewById(R.id.profile);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.impression_approved_user_info, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final MeetMemberInfo memberInfo = mMemberInfoList.get(position);
        Slog.d(TAG, "=============get real name: "+memberInfo.getRealname());
        holder.name.setText(memberInfo.getRealname());
        String profile = "";
        if(memberInfo.getSituation() == 0){//student
            profile = memberInfo.getUniversity()+"."+memberInfo.getDegree()+"."+memberInfo.getDegree();
        }else{
            profile = memberInfo.getJobTitle()+"."+memberInfo.getCompany();
        }
        holder.profile.setText(profile);

        if(memberInfo.getPictureUri() != null && !"".equals(memberInfo.getPictureUri())){
            queue = RequestQueueSingleton.instance(mContext);
            holder.headPic.setTag(HttpUtil.DOMAIN+memberInfo.getPictureUri());
            HttpUtil.loadByImageLoader(queue, holder.headPic, HttpUtil.DOMAIN + memberInfo.getPictureUri(), 50, 50);
        }else{
            holder.headPic.setImageDrawable(mContext.getDrawable(R.mipmap.ic_launcher));
        }
    }

    @Override
    public int getItemCount(){
          return mMemberInfoList != null ? mMemberInfoList.size():0;
    }
}
