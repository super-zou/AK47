package com.tongmenhui.launchak47.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.NetworkImageView;
import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.meet.MeetReferenceInfo;
import com.tongmenhui.launchak47.util.FontManager;
import com.tongmenhui.launchak47.util.HttpUtil;
import com.tongmenhui.launchak47.util.RequestQueueSingleton;
import com.tongmenhui.launchak47.util.Slog;

import java.util.List;

public class CheeringGroupAdapter extends RecyclerView.Adapter<CheeringGroupAdapter.ViewHolder>{
    private static final String TAG = "CheeringGroupAdapter";
    private static Context mContext;
    RequestQueue queue;
    private List<MeetReferenceInfo> mCheeringGroupList;

    public CheeringGroupAdapter(Context context) {
        mContext = context;
    }
    
        public void setCheeringGroupList(List<MeetReferenceInfo> cheeringGroupList) {
        mCheeringGroupList = cheeringGroupList;
    }

    @Override
    public CheeringGroupAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cheering_group_item, parent, false);
        CheeringGroupAdapter.ViewHolder holder = new CheeringGroupAdapter.ViewHolder(view);
        return holder;
    }
    
        @Override
    public void onBindViewHolder(@NonNull CheeringGroupAdapter.ViewHolder holder, int position) {
        final MeetReferenceInfo cheeringGroup = mCheeringGroupList.get(position);
        holder.realName.setText(cheeringGroup.getRefereeName());
        holder.relation.setText(cheeringGroup.getRelation());
        holder.profile.setText(cheeringGroup.getRefereeProfile());

        if (cheeringGroup.getHeadUri() != null && !"".equals(cheeringGroup.getHeadUri())) {
            queue = RequestQueueSingleton.instance(mContext);
            holder.headUri.setTag(HttpUtil.DOMAIN + cheeringGroup.getHeadUri());
            HttpUtil.loadByImageLoader(queue, holder.headUri, HttpUtil.DOMAIN + cheeringGroup.getHeadUri(), 37, 60);
        } else {
            holder.headUri.setImageDrawable(mContext.getDrawable(R.mipmap.ic_launcher));
        }

    }
    
        @Override
    public int getItemCount() {
        return mCheeringGroupList != null ? mCheeringGroupList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView realName;
        TextView relation;
        TextView profile;
        NetworkImageView headUri;

        public ViewHolder(View view) {
            super(view);
            realName = view.findViewById(R.id.name);
            headUri = view.findViewById(R.id.head_uri);
            profile = view.findViewById(R.id.profile);
            relation = view.findViewById(R.id.relation);
        }
    }
}
