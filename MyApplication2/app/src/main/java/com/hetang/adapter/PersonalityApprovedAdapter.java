package com.hetang.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.bumptech.glide.Glide;
import com.hetang.R;

import com.hetang.util.HttpUtil;
import com.hetang.common.MyApplication;
import com.hetang.util.ParseUtils;
import com.hetang.util.RequestQueueSingleton;
import com.hetang.util.RoundImageView;
import com.hetang.util.Slog;
import com.hetang.util.UserProfile;

import java.util.List;

import static com.hetang.util.ParseUtils.startMeetArchiveActivity;

public class PersonalityApprovedAdapter extends RecyclerView.Adapter<PersonalityApprovedAdapter.ViewHolder> {
    private static final String TAG = "PersonalityApprovedAdapter";
    RequestQueue queue;
    private List<UserProfile> mMemberInfoList;
    private Context mContext;

    public PersonalityApprovedAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<UserProfile> memberInfoList) {
        mMemberInfoList = memberInfoList;
    }
    @Override
    public PersonalityApprovedAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.approved_user_info, parent, false);
        PersonalityApprovedAdapter.ViewHolder holder = new PersonalityApprovedAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PersonalityApprovedAdapter.ViewHolder holder, int position) {
        final UserProfile userProfile = mMemberInfoList.get(position);
        holder.name.setText(userProfile.getName());
        String profile = "";
        if (userProfile.getSituation() == 0) {//student
            profile = userProfile.getUniversity() + "·" + userProfile.getDegreeName(userProfile.getDegree()) + "·" + userProfile.getMajor();
        } else {
            profile = userProfile.getPosition() + "·" + userProfile.getIndustry();
            if (!"".equals(userProfile.getLiving())) {
                profile += "·" + userProfile.getLiving();
            }
        }

        holder.profile.setText(profile.replaceAll(" ", ""));

        if (userProfile.getAvatar() != null && !"".equals(userProfile.getAvatar())) {
            queue = RequestQueueSingleton.instance(mContext);
            //holder.headPic.setTag(HttpUtil.DOMAIN + memberInfo.userProfile.getAvatar());
            Slog.d(TAG, "---------------------->avatar: "+userProfile.getAvatar());
            Glide.with(mContext).load(HttpUtil.DOMAIN + userProfile.getAvatar()).into(holder.avatar);
            //HttpUtil.loadByImageLoader(queue, holder.headPic, HttpUtil.DOMAIN + memberInfo.getPictureUri(), 50, 50);
        } else {
            if(userProfile.getSex() == 0){
                holder.avatar.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.male_default_avator));
            }else {
                holder.avatar.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.female_default_avator));
            }
        }
        
        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUtils.startArchiveActivity(mContext, userProfile.getUid());
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return mMemberInfoList != null ? mMemberInfoList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ConstraintLayout constraintLayout;
        public RoundImageView avatar;
        public TextView name;
        public TextView profile;
        
        public ViewHolder(View view) {
            super(view);
            constraintLayout = view.findViewById(R.id.user_info);
            avatar = view.findViewById(R.id.avatar);
            name = view.findViewById(R.id.name);
            profile = view.findViewById(R.id.profile);
        }
    }
}
