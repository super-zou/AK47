package com.hetang.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.meet.UserMeetInfo;
import com.hetang.util.HttpUtil;
import com.hetang.common.MyApplication;
import com.hetang.util.ParseUtils;
import com.hetang.util.RoundImageView;

import java.util.List;

import static com.hetang.util.ParseUtils.startMeetArchiveActivity;

public class ImpressionApprovedDetailAdapter extends RecyclerView.Adapter<ImpressionApprovedDetailAdapter.ViewHolder> {
    private static final String TAG = "ImpressionApprovedDetailAdapter";

    private List<UserMeetInfo> mMemberInfoList;
    private Context mContext;

    public ImpressionApprovedDetailAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<UserMeetInfo> memberInfoList) {
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
        final UserMeetInfo memberInfo = mMemberInfoList.get(position);
        holder.name.setText(memberInfo.getName());
        String profile = memberInfo.getBaseProfile();
        holder.profile.setText(profile.replaceAll(" ", ""));
        
        String avatar = memberInfo.getAvatar();
        if (avatar != null && !"".equals(avatar)) {
           // queue = RequestQueueSingleton.instance(mContext);
           // holder.headPic.setTag(HttpUtil.DOMAIN + memberInfo.getPictureUri());
           // HttpUtil.loadByImageLoader(queue, holder.headPic, HttpUtil.DOMAIN + memberInfo.getPictureUri(), 50, 50);
            Glide.with(MyApplication.getContext()).load(HttpUtil.DOMAIN + avatar).into(holder.headPic);
        } else {
            if(memberInfo.getSex() == 0){
                holder.headPic.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.male_default_avator));
            }else {
                holder.headPic.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.female_default_avator));
            }
        }
        
        holder.headPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
ParseUtils.startMeetArchiveActivity(mContext, memberInfo.getUid());

            }
        });
    }

    @Override
    public int getItemCount() {
        return mMemberInfoList != null ? mMemberInfoList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public RoundImageView headPic;
        public TextView name;
        public TextView profile;

        public ViewHolder(View view) {
            super(view);
            headPic = view.findViewById(R.id.avatar);
            name = view.findViewById(R.id.name);
            profile = view.findViewById(R.id.profile);
        }
    }
}
