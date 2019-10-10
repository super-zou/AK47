package com.hetang.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hetang.launchak47.R;
import com.hetang.meet.MeetMemberInfo;

import java.util.List;

public class HomePageAdapter extends RecyclerView.Adapter<HomePageAdapter.ViewHolder>{
    private static final String TAG = "PersonalityApprovedAdapter";
    private List<MeetMemberInfo> mMemberInfoList;
    private Context mContext;

    public HomePageAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<MeetMemberInfo> memberInfoList) {
        mMemberInfoList = memberInfoList;
    }
    
        @Override
    public HomePageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.approved_user_info, parent, false);
        HomePageAdapter.ViewHolder holder = new HomePageAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull HomePageAdapter.ViewHolder holder, int position) {

    }
    
     @Override
    public int getItemCount() {
        return mMemberInfoList != null ? mMemberInfoList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {


        public ViewHolder(View view) {
            super(view);

        }
    }

}
