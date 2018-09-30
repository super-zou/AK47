package com.tongmenhui.launchak47.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.adapter.MeetReferenceAdapter;
import com.tongmenhui.launchak47.meet.MeetMemberInfo;
import com.tongmenhui.launchak47.meet.MeetReferenceInfo;

import java.util.ArrayList;
import java.util.List;

public class SearchUserListAdapter extends RecyclerView.Adapter<SearchUserListAdapter.SearchUserViewHolder> {
    private ArrayList<MeetMemberInfo> mUnfilteredData;
    private List<MeetMemberInfo> mMemberInfoList = null;
    private Context mContext;
    RequestQueue queue;

    public SearchUserListAdapter(Context context){
        mContext = context;
    }

    public void setData(List<MeetMemberInfo> memberInfoList){
        mMemberInfoList = memberInfoList;
    }
        public class SearchUserViewHolder extends RecyclerView.ViewHolder{

        public ImageView headPic;
        public TextView name;
        public TextView profile;
        public Button invite;

        public SearchUserViewHolder(View view){
            super(view);
            headPic =  view.findViewById(R.id.headPic);
            name = (TextView) view.findViewById(R.id.name);
            profile = (TextView) view.findViewById(R.id.profile);
            invite = (Button) view.findViewById(R.id.invite);
        }
    }

    @Override
    public SearchUserListAdapter.SearchUserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.search_user_item, parent, false);
        SearchUserListAdapter.SearchUserViewHolder holder = new SearchUserListAdapter.SearchUserViewHolder(view);
        return holder;
    }
        @Override
    public void onBindViewHolder(@NonNull SearchUserListAdapter.SearchUserViewHolder holder, int position) {
        final MeetMemberInfo memberInfo = mMemberInfoList.get(position);
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
        return mMemberInfoList.size();
    }
}
