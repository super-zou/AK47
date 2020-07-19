package com.mufu.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mufu.R;
import com.mufu.common.MyApplication;
import com.mufu.util.HttpUtil;
import com.mufu.util.InterActInterface;
import com.mufu.util.ParseUtils;
import com.mufu.util.RoundImageView;
import com.mufu.util.Slog;
import com.mufu.util.UserProfile;

import java.util.List;

import static com.mufu.home.CommonContactsActivity.GROUP_MEMBER;

public class CommonContactsListAdapter extends RecyclerView.Adapter<CommonContactsListAdapter.ContactsViewHolder> {
    private static final String TAG = "ContactsListAdapter";
    private static final boolean isDebug = true;
    private List<UserProfile> contactsList;
    private Context mContext;
    private boolean isScrolling = false;
    private int type = 0;
    private int gid = 0;
    boolean isLeader = false;
    static InterActInterface interActInterface;

    public CommonContactsListAdapter(Context context, int type) {
        mContext = context;
        this.type = type;
    }
    
    public CommonContactsListAdapter(Context context, int type, int gid, boolean isLeader) {
        mContext = context;
        this.type = type;
        this.gid = gid;
        this.isLeader = isLeader;
    }
    
    public void setScrolling(boolean isScrolling) {
        this.isScrolling = isScrolling;
    }

    public void setData(List<UserProfile> contactsList) {
        this.contactsList = contactsList;
    }


    @Override
    public ContactsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recommend_contacts_item, parent, false);
        ContactsViewHolder holder = new ContactsViewHolder(view);
        return holder;
    }
    
    @Override
    public void onBindViewHolder(@NonNull final ContactsViewHolder holder, final int position) {
        Slog.d(TAG, "------------------>onDefaultBindViewHolder");
        final UserProfile contacts = contactsList.get(position);

        String avatar = contacts.getAvatar();

        if (avatar != null && !"".equals(avatar)) {
            Glide.with(mContext).load(HttpUtil.DOMAIN + avatar).into(holder.avatar);
        } else {
            if(contacts.getSex() == 0){
                holder.avatar.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.male_default_avator));
            }else {
                holder.avatar.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.female_default_avator));
            }
        }
        
        holder.name.setText(contacts.getNickName());
        holder.degree.setText(contacts.getDegreeName(contacts.getDegree()));
        holder.major.setText(contacts.getMajor());
        holder.university.setText(contacts.getUniversity());

        holder.contactsItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               ParseUtils.startMeetArchiveActivity(mContext, contacts.getUid());
            }
        });

        if (type == GROUP_MEMBER){
            if (isLeader){
                holder.operation.setVisibility(View.VISIBLE);
                holder.operation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        interActInterface.onOperationClick(view, position);
                    }
                });
            }else {
                holder.operation.setVisibility(View.GONE);
            }

        }else {
            holder.operation.setVisibility(View.GONE);
        }
    }
    
    @Override
    public int getItemCount() {
        return contactsList != null ? contactsList.size() : 0;
    }

    //register the interActInterface callback, add by zouhaichao 2018/9/16
    public void setOnItemClickListener(InterActInterface commentDialogFragmentListener) {
        this.interActInterface = commentDialogFragmentListener;
    }
    
    public static class ContactsViewHolder extends RecyclerView.ViewHolder {

        public ConstraintLayout contactsItem;
        public RoundImageView avatar;
        public TextView name;
        public TextView major;
        public TextView degree;
        public TextView university;
        public TextView operation;
        
        public ContactsViewHolder(View view) {
            super(view);
            contactsItem = view.findViewById(R.id.recommend_contacts_item);
            avatar = view.findViewById(R.id.avatar);
            name = view.findViewById(R.id.name);
            major = view.findViewById(R.id.major);
            degree = view.findViewById(R.id.degree);
            university = view.findViewById(R.id.university);
            operation = view.findViewById(R.id.member_operation);
        }
    }
}
