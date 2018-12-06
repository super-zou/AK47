package com.tongmenhui.launchak47.util;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SearchUserListAdapter extends RecyclerView.Adapter<SearchUserListAdapter.SearchUserViewHolder> {
    private static final String TAG = "SearchUserListAdapter";
    private static final boolean isDebug = true;
    RequestQueue queue;
    private ArrayList<MeetMemberInfo> mUnfilteredData;
    private List<MeetMemberInfo> mMemberInfoList;
    private Context mContext;
    private int type;
    private static final int TYPE_INVITATION = 0;
    private static final int TYPE_CHEERING_GROUP = 2;
    private static final String ADD_CHEERING_GROUP_URL = HttpUtil.DOMAIN + "?q=meet/cheering_group/add";
    public SearchUserListAdapter(Context context) {
        mContext = context;
    } 

    
    public void setData(List<MeetMemberInfo> memberInfoList , int type) {
        mMemberInfoList = memberInfoList;
        this.type = type;
    }

    @Override
    public SearchUserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Slog.d(TAG, "=============onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_user_item, parent, false);
        SearchUserViewHolder holder = new SearchUserViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final SearchUserListAdapter.SearchUserViewHolder holder, int position) {
        final MeetMemberInfo memberInfo = mMemberInfoList.get(position);

        holder.name.setText(memberInfo.getRealname());
        if(type == TYPE_CHEERING_GROUP){
            holder.invite.setText(R.string.add_member);
        }
        holder.invite.setTag(memberInfo.getUid());
        String profile = "";
        if (memberInfo.getSituation() == 0) {//student
            profile = memberInfo.getUniversity() + "·" + memberInfo.getDegree() + "·" + memberInfo.getDegree();
        } else {
            profile = memberInfo.getJobTitle() + "·" + memberInfo.getCompany();
        }
        holder.profile.setText(profile);

        if (memberInfo.getPictureUri() != null && !"".equals(memberInfo.getPictureUri())) {
            queue = RequestQueueSingleton.instance(mContext);
            holder.headPic.setTag(HttpUtil.DOMAIN + memberInfo.getPictureUri());
            HttpUtil.loadByImageLoader(queue, holder.headPic, HttpUtil.DOMAIN + memberInfo.getPictureUri(), 50, 50);
        } else {
            holder.headPic.setImageDrawable(mContext.getDrawable(R.mipmap.ic_launcher));
        }

        holder.invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(type == TYPE_INVITATION){
                    inviteReference((int)v.getTag());
                }else {
                    addCheeringGroup((int)v.getTag());
                    holder.invite.setEnabled(false);
                    holder.invite.setText(R.string.added_member);
                    holder.invite.setBackground(mContext.getDrawable(R.drawable.btn_disable));
                }
            }
        });
    }
    
    
    private void inviteReference(int uid){
        Toast.makeText(mContext, "invite uid: " + uid, Toast.LENGTH_SHORT).show();
    }
    
    private void addCheeringGroup(int uid){
        Toast.makeText(mContext, "add cheering uid: " + uid, Toast.LENGTH_SHORT).show();
        final RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(mContext, ADD_CHEERING_GROUP_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug)
                        Slog.d(TAG, "==========praise add response text : " + responseText);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }

    @Override
    public int getItemCount() {
        if (null == mMemberInfoList) {
            return 0;
        }

        return mMemberInfoList.size();
    }

    public static class SearchUserViewHolder extends RecyclerView.ViewHolder {

        public ImageView headPic;
        public TextView name;
        public TextView profile;
        public Button invite;

        public SearchUserViewHolder(View view) {
            super(view);
            headPic = view.findViewById(R.id.headPic);
            name = (TextView) view.findViewById(R.id.name);
            profile = (TextView) view.findViewById(R.id.profile);
            invite = (Button) view.findViewById(R.id.invite);
        }
    }
}
