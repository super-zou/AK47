package com.hetang.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.common.MyApplication;
import com.hetang.contacts.ContactsApplyListActivity;
import com.hetang.util.HttpUtil;
import com.hetang.util.ParseUtils;
import com.hetang.util.RoundImageView;
import com.hetang.util.Slog;
import com.hetang.util.UserProfile;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hetang.main.ContactsFragment.ACCEPT_CONTACTS_APPLY_URL;
import static com.hetang.main.ContactsFragment.CONTACTS_DEFAULT;
import static com.hetang.main.ContactsFragment.CONTACTS_MY_APPLY;
import static com.hetang.main.ContactsFragment.CONTACTS_NEW_APPLY;

public class RecommendContactsListAdapter extends RecyclerView.Adapter<RecommendContactsListAdapter.ContactsViewHolder> {
    private static final String TAG = "ContactsListAdapter";
    private static final boolean isDebug = true;
    private List<UserProfile> contactsList;
    private Context mContext;
    private boolean isScrolling = false;
    private int type = 0;

    public RecommendContactsListAdapter(Context context) {
        mContext = context;
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
    public void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position) {
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
        
        holder.name.setText(contacts.getName());
        holder.degree.setText(contacts.getDegreeName(contacts.getDegree()));
        holder.major.setText(contacts.getMajor());
        holder.university.setText(contacts.getUniversity());

        holder.contactsItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (contacts.getCid() > 0){
                    ParseUtils.startMeetArchiveActivity(mContext, contacts.getUid());
                }else {
                    ParseUtils.startArchiveActivity(mContext, contacts.getUid());
                }

            }
        });
    }
    
    @Override
    public int getItemCount() {
        return contactsList != null ? contactsList.size() : 0;
    }
    
    public static class ContactsViewHolder extends RecyclerView.ViewHolder {

        public ConstraintLayout contactsItem;
        public RoundImageView avatar;
        public TextView name;
        public TextView major;
        public TextView degree;
        public TextView university;
        
        public ContactsViewHolder(View view) {
            super(view);
            contactsItem = view.findViewById(R.id.recommend_contacts_item);
            avatar = view.findViewById(R.id.avatar);
            name = view.findViewById(R.id.name);
            major = view.findViewById(R.id.major);
            degree = view.findViewById(R.id.degree);
            university = view.findViewById(R.id.university);
        }
    }
}
