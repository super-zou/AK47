package com.hetang.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.common.MyApplication;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hetang.util.ParseUtils.TYPE_COMMON_SEARCH;

public class SearchUserListAdapter extends RecyclerView.Adapter<SearchUserListAdapter.SearchUserViewHolder> {
    private static final String TAG = "SearchUserListAdapter";
    private static final boolean isDebug = true;
    RequestQueue queue;
    private List<UserProfile> mMemberInfoList;
    private Context mContext;
    private int type;
    private int uidArray[];
    private int gid;
    private UserProfile memberInfo;
    private static final String ADD_CHEERING_GROUP_URL = HttpUtil.DOMAIN + "?q=meet/cheering_group/add";
        private static final String INVITE_SUBGROUP_MEMBER_URL = HttpUtil.DOMAIN + "?q=subgroup/invite";
    private static final String ADD_NOTICE_URL = HttpUtil.DOMAIN + "?q=notice/add";
    private static final String INVITE_REFERENCE_URL = HttpUtil.DOMAIN + "?q=meet/reference/invite";
    
    public SearchUserListAdapter(Context context) {
        mContext = context;
    } 

    
    public void setData(List<UserProfile> memberInfoList , int type, int[] uidArray) {
        mMemberInfoList = memberInfoList;
        this.type = type;
        this.uidArray = uidArray;
    }
    
    public void setData(List<UserProfile> memberInfoList , int type, int[] uidArray, int gid) {
        setData(memberInfoList, type, uidArray);
        this.gid = gid;
    }

    @Override
    public SearchUserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_user_item, parent, false);
        SearchUserViewHolder holder = new SearchUserViewHolder(view);
        return holder;
    }
    
    @Override
    public void onBindViewHolder(@NonNull final SearchUserListAdapter.SearchUserViewHolder holder, int position) {
        memberInfo = mMemberInfoList.get(position);

        holder.name.setText(memberInfo.getName());
        if (type == TYPE_COMMON_SEARCH){
            holder.invite.setVisibility(View.GONE);
        }else {
            if(type == ParseUtils.TYPE_CHEERING_GROUP){
                holder.invite.setText(R.string.add_member);
            }
        }
        
        String profile = "";
        if (memberInfo.getSituation() == 0) {//student
            profile = memberInfo.getUniversity() + "·" + memberInfo.getDegreeName(memberInfo.getDegree()) + "·" + memberInfo.getMajor();
        } else {
            profile = memberInfo.getPosition() + "·" + memberInfo.getIndustry();
        }
        holder.profile.setText(profile);
        holder.invite.setId(memberInfo.getUid());
        String avatar = memberInfo.getAvatar();
        
        if (avatar != null && !"".equals(avatar)) {
            Glide.with(MyApplication.getContext()).load(HttpUtil.DOMAIN + avatar).into(holder.avatar);
        } else {
            if(memberInfo.getSex() == 0){
                holder.avatar.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.male_default_avator));
            }else {
                holder.avatar.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.female_default_avator));
            }
        }
        
        if(!holder.invite.isEnabled()){
            holder.invite.setEnabled(true);
            holder.invite.setText(R.string.invite);
        }

        if(uidArray != null && uidArray.length > 0){
            for (int i=0; i<uidArray.length; i++){
                if (memberInfo.getUid() == uidArray[i]){
                    holder.invite.setEnabled(false);
                    holder.invite.setText(R.string.invited_member);
                    break;
                }
            }
        }
        
        holder.invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (type){
                    case ParseUtils.TYPE_REFERENCE:
                        inviteReference(v.getId());
                        break;
                    case ParseUtils.TYPE_CHEERING_GROUP:
                        addCheeringGroup(v.getId());
                        break;
                        case ParseUtils.TYPE_SINGLE_GROUP:
                        inviteSubGroupMember(v.getId(), gid);
                        break;
                        default:
                            break;

                }
                holder.invite.setEnabled(false);
                holder.invite.setText(R.string.invited_member);
            }
        });
        
         if (type == TYPE_COMMON_SEARCH){
            holder.searchItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
ParseUtils.startMeetArchiveActivity(mContext, memberInfo.getUid());
                }
            });
        }
    }
    
    private void inviteSubGroupMember(final int uid, int gid){
        Slog.d(TAG, "--------------->gid: "+gid);
        final RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .add("gid", String.valueOf(gid)).build();
        HttpUtil.sendOkHttpRequest(mContext, INVITE_SUBGROUP_MEMBER_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug)
                        Slog.d(TAG, "==========addCheeringGroup response text : " + responseText);
                    //addNotice(uid, 6, "单身团邀请", "邀请你加入单身团");
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }
    
    private void inviteReference(int uid){

        final RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(mContext, INVITE_REFERENCE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug)
                        Slog.d(TAG, "==========inviteReference response text : " + responseText);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }
    
    private void addCheeringGroup(final int uid){
        //Toast.makeText(mContext, "add cheering uid: " + uid, Toast.LENGTH_SHORT).show();
        final RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(mContext, ADD_CHEERING_GROUP_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug)
                        Slog.d(TAG, "==========addCheeringGroup response text : " + responseText);
                    //addCheeringMemberNotice(uid);
                    //addNotice(uid, 6, "亲友团邀请", "已将你设置为亲友团成员");
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }

    public void addNotice(int uid, int type, String action, String content){
        
        FormBody.Builder builder = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .add("action", action)
                .add("type", String.valueOf(type))
                .add("content", content);

        if (type == ParseUtils.TYPE_SINGLE_GROUP){
            Slog.d(TAG, "------------------>gid: "+gid);
            builder = builder.add("id", String.valueOf(gid));
        }
        
        final RequestBody requestBody = builder.build();

        HttpUtil.sendOkHttpRequest(mContext, ADD_NOTICE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug)
                        Slog.d(TAG, "==========addNotice responseText: " + responseText);
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

        public RoundImageView avatar;
        public TextView name;
        public TextView profile;
        public Button invite;
        public ConstraintLayout searchItem;

        public SearchUserViewHolder(View view) {
            super(view);
            avatar = view.findViewById(R.id.headPic);
            name = (TextView) view.findViewById(R.id.name);
            profile = (TextView) view.findViewById(R.id.profile);
            invite = (Button) view.findViewById(R.id.invite);
            searchItem = view.findViewById(R.id.search_user_item);
        }
    }
}

