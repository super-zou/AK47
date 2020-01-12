package com.hetang.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
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
import com.hetang.contacts.ContactsApplyListActivity;
import com.hetang.R;
import com.hetang.util.HttpUtil;
import com.hetang.util.ParseUtils;
import com.hetang.util.RoundImageView;
import com.hetang.util.Slog;
import com.hetang.util.UserProfile;
import com.hetang.common.MyApplication;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hetang.main.ContactsFragment.ACCEPT_CONTACTS_APPLY_URL;
import static com.hetang.main.ContactsFragment.CONTACTS_DEFAULT;
import static com.hetang.main.ContactsFragment.CONTACTS_DISMISS_URL;
import static com.hetang.main.ContactsFragment.CONTACTS_MY_APPLY;
import static com.hetang.main.ContactsFragment.CONTACTS_NEW_APPLY;
import static com.hetang.util.ParseUtils.startMeetArchiveActivity;

public class ContactsListAdapter extends RecyclerView.Adapter<ContactsListAdapter.ContactsViewHolder> {
    private static final String TAG = "ContactsListAdapter";
    private static final boolean isDebug = true;
    private List<ContactsApplyListActivity.Contacts> contactsList;
    private Context mContext;
    private boolean isScrolling = false;
    private int type = 0;
        private Handler mHandler;
    private static final int DISMISS_ITEM = 0;

    public ContactsListAdapter(Context context, int type) {
        this.type = type;
        mContext = context;
        
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case DISMISS_ITEM:
                        notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }
    
    public void setScrolling(boolean isScrolling) {
        this.isScrolling = isScrolling;
    }

    public void setData(List<ContactsApplyListActivity.Contacts> contactsList) {
        this.contactsList = contactsList;
    }


    @Override
    public ContactsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contacts_item, parent, false);
        ContactsViewHolder holder = new ContactsViewHolder(view);
        return holder;
    }
    
    @Override
    public void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position) {
        switch (type){
            case CONTACTS_DEFAULT:
                onDefaultBindViewHolder(holder, position);
                break;
            case CONTACTS_NEW_APPLY:
            case CONTACTS_MY_APPLY:
                onApplyBindViewHolder(holder, position);
                break;
                default:
                    onDefaultBindViewHolder(holder, position);
                    break;
        }
    }
    
    public void onDefaultBindViewHolder(@NonNull final ContactsViewHolder holder, int position) {
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

        holder.contactsItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUtils.startMeetArchiveActivity(mContext, contacts.getUid());
            }
        });
    }
    
    public void onApplyBindViewHolder(@NonNull final ContactsViewHolder holder, final int position) {
        Slog.d(TAG, "------------------>onApplyBindViewHolder"+"  size: "+contactsList.size());
        final ContactsApplyListActivity.Contacts contacts = (ContactsApplyListActivity.Contacts) contactsList.get(position);

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

        holder.contactsItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUtils.startMeetArchiveActivity(mContext, contacts.getUid());
            }
        });
        
        if (type == CONTACTS_NEW_APPLY){
            String content = contacts.getContent();
            if (content != null && !TextUtils.isEmpty(content)){
                holder.applyContent.setVisibility(View.VISIBLE);
                holder.applyContent.setText("“"+content+"”");
            }else {
                holder.applyContent.setVisibility(View.GONE);
            }
            holder.accept.setVisibility(View.VISIBLE);
            holder.dismiss.setVisibility(View.VISIBLE);
        }

        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accept(holder.accept, contacts.getUid());
            }
        });
        
                    holder.dismiss.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss(contacts.getUid(), position);
                }
            });

    }
    
    private void dismiss(int uid, final int position) {
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), CONTACTS_DISMISS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null) {
                    String responseText = response.body().string();
                    //dismissProgressDialog();
                    contactsList.remove(position);
                    mHandler.sendEmptyMessage(DISMISS_ITEM);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure e:" + e);
            }
        });
    }
    
    public static void accept(Button acceptBtn, int uid){
        acceptBtn.setText(MyApplication.getContext().getResources().getString(R.string.acceptted));
        acceptBtn.setClickable(false);
        acceptBtn.setBackground(MyApplication.getContext().getDrawable(R.drawable.btn_disable));
        acceptContactsApply(uid);
    }

    public static void acceptContactsApply(int uid){
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid)).build();
                HttpUtil.sendOkHttpRequest(MyApplication.getContext(), ACCEPT_CONTACTS_APPLY_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response != null){
                    String responseText = response.body().string();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure e:" + e);
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
        Button accept;
        Button dismiss;
        TextView applyContent;

        public ContactsViewHolder(View view) {
            super(view);
            contactsItem = view.findViewById(R.id.contacts_item);
            avatar = view.findViewById(R.id.avatar);
            name = view.findViewById(R.id.name);
            dismiss = view.findViewById(R.id.dismiss);
            accept = view.findViewById(R.id.accept);
            applyContent = view.findViewById(R.id.apply_content);
        }
    }
}
