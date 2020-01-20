package com.hetang.adapter;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.hetang.R;
import com.hetang.authenticate.SubmitAuthenticationDialogFragment;
import com.hetang.common.MyApplication;
import com.hetang.group.SingleGroupDetailsActivity;
import com.hetang.group.SubGroupDetailsActivity;
import com.hetang.main.MeetArchiveActivity;
import com.hetang.message.NotificationFragment;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.ParseUtils;
import com.hetang.util.RoundImageView;
import com.hetang.util.Slog;
import com.hetang.util.UserProfile;
import com.hetang.util.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hetang.adapter.ContactsListAdapter.acceptContactsApply;
import static com.hetang.common.MyApplication.getContext;
import static com.hetang.util.ParseUtils.ADD_CHEERING_GROUP_MEMBER_ACTION;
import static com.hetang.util.ParseUtils.APPLY_JOIN_SINGLE_GROUP_NF;
import static com.hetang.util.ParseUtils.APPROVE_IMPRESSION_ACTION;
import static com.hetang.util.ParseUtils.APPROVE_PERSONALITY_ACTION;
import static com.hetang.util.ParseUtils.AUTHENTICATION_REJECTED_NF;
import static com.hetang.util.ParseUtils.AUTHENTICATION_VERIFIED_NF;
import static com.hetang.util.ParseUtils.EVALUATE_ACTION;
import static com.hetang.util.ParseUtils.FOLLOW_GROUP_ACTION;
import static com.hetang.util.ParseUtils.INVITE_GROUP_MEMBER_ACTION;
import static com.hetang.util.ParseUtils.INVITE_SINGLE_GROUP_MEMBER_ACTION;
import static com.hetang.util.ParseUtils.JOIN_CHEERING_GROUP_ACTION;
import static com.hetang.util.ParseUtils.JOIN_SINGLE_GROUP_ACTION;
import static com.hetang.util.ParseUtils.MODIFY_GROUP_ACTION;
import static com.hetang.util.ParseUtils.REFEREE_ACTION;
import static com.hetang.util.ParseUtils.REFEREE_INVITE_NF;
import static com.hetang.util.ParseUtils.startMeetArchiveActivity;
import static com.hetang.util.Utility.drawableToBitmap;
import static com.hetang.util.Utility.getDateToString;

public class NotificationListAdapter extends RecyclerView.Adapter<NotificationListAdapter.ViewHolder> {
    private static final String TAG = "NotificationListAdapter";
    private List<NotificationFragment.Notification> notificationList = new ArrayList<>();
    private Context mContext;
    private boolean isScrolling = false;
    private static final int READ = 0;
    public static final int UNREAD = 1;
    private static final int UNPROCESSED = 0;
    private static final int PROCESSED = 1;
    private static final int IGNORED = -1;
    private static final int UPDATE_PROCESS_BUTTON_STATUS = 0;
    private static final int MAKE_NOTIFICATION_SHOWED = 1;
    private static final int NOT_SHOWED = 0;
    private static final int SHOWED = 1;
    private Handler mHandler;
    private FragmentManager fragmentManager;

    public NotificationListAdapter(Context context, FragmentManager fragmentManager) {
        mContext = context;
        this.fragmentManager = fragmentManager;
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UPDATE_PROCESS_BUTTON_STATUS:
                        case MAKE_NOTIFICATION_SHOWED:
                        notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    public void setData(List<NotificationFragment.Notification> notificationList) {
        this.notificationList = notificationList;
    }

    public void setScrolling(boolean isScrolling) {
        this.isScrolling = isScrolling;
    }

    @Override
    public NotificationListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_item, parent, false);
        NotificationListAdapter.ViewHolder holder = new NotificationListAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final NotificationListAdapter.ViewHolder holder, int position) {
        final NotificationFragment.Notification notification = notificationList.get(position);

        if (notification.isNew == UNREAD) {
            holder.isNew.setVisibility(View.VISIBLE);
            holder.isNew.bringToFront();
            switch (notification.type) {
                case EVALUATE_ACTION:
                case REFEREE_ACTION:
                case REFEREE_INVITE_NF:
                case INVITE_GROUP_MEMBER_ACTION:
                case APPROVE_IMPRESSION_ACTION:
                case APPROVE_PERSONALITY_ACTION:
                case JOIN_CHEERING_GROUP_ACTION:
                    if (notification.showed == NOT_SHOWED) {
                        showNotification(notification);
                    }
                    break;
            }
        } else {
            holder.isNew.setVisibility(View.GONE);
        }

        holder.action.setText(notification.action);
        final UserProfile trigger = notification.trigger;//the trigger who produce this notice
        if (trigger.getAvatar() != null && !"".equals(trigger.getAvatar())) {
            Glide.with(mContext).load(HttpUtil.DOMAIN + trigger.getAvatar()).into(holder.avatar);
        } else {
            if (trigger.getSex() == Utility.MALE) {
                holder.avatar.setImageDrawable(mContext.getDrawable(R.drawable.male_default_avator));
            } else {
                holder.avatar.setImageDrawable(mContext.getDrawable(R.drawable.female_default_avator));
            }
        }
        holder.name.setText(trigger.getNickName());
        /*
        String profile = "";
        if (trigger.getSituation() != -1){
            if(trigger.getSituation() == 0){
                profile = trigger.getDegreeName(trigger.getDegree())+"·"+ trigger.getUniversity();
            }else {
                profile = trigger.getPosition()+"·"+ trigger.getIndustry();
            }
            holder.profile.setText(profile);
        }
        */

        if (!TextUtils.isEmpty(notification.content)) {
            holder.content.setText(notification.content);
        } else {
            holder.content.setText("查看详情");
        }

        String dataString = getDateToString(notification.timeStamp, "yyyy-MM-dd");
        holder.timeStamp.setText(dataString);

        
        switch (notification.type){
            //case ParseUtils.APPLY_CONTACTS_NF:
            case ParseUtils.APPLY_JOIN_SINGLE_GROUP_NF:
            case ParseUtils.INVITE_GROUP_MEMBER_ACTION:
                holder.acceptBtn.setVisibility(View.VISIBLE);
                if (notification.processed == UNPROCESSED){
                holder.acceptBtn.setClickable(true);
                    holder.acceptBtn.setText(MyApplication.getContext().getResources().getString(R.string.accept));
                    holder.acceptBtn.setBackground(MyApplication.getContext().getDrawable(R.drawable.btn_stress));
                    holder.acceptBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            approveAction(notification);
                            markNotificationProcessed(holder.isNew, notification, PROCESSED);
                            //deleteNotification(notification.nid);
                        }
                    });
                }else {
                    holder.acceptBtn.setText(MyApplication.getContext().getResources().getString(R.string.acceptted));
                    holder.acceptBtn.setClickable(false);
                    holder.acceptBtn.setBackground(MyApplication.getContext().getDrawable(R.drawable.btn_disable));
                }

                break;
                default:
                    holder.acceptBtn.setVisibility(View.GONE);
                    break;
        }
                
        holder.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
ParseUtils.startMeetArchiveActivity(mContext, notification.tid);
                markNotificationProcessed(holder.isNew, notification);
            }
        });

        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
ParseUtils.startMeetArchiveActivity(mContext, notification.tid);
                markNotificationProcessed(holder.isNew, notification);
            }
        });
        
        holder.content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (notification.type) {
                    case ParseUtils.MEET_COMMENT_REPLY_NF:
                    case ParseUtils.PRAISE_MEET_COMMENT_NF:
                    case ParseUtils.MEET_COMMENT_NF:
                        ParseUtils.startMeetConditionDetails(getContext(), notification.uid, notification.id, null);
                        markNotificationProcessed(holder.isNew, notification);
                        break;

                    case ParseUtils.LOVED_NF:
                    case ParseUtils.REFEREE_ACTION:
                    case ParseUtils.PRAISE_MEET_CONDITION_ACTION:
                    case ParseUtils.EVALUATE_ACTION:
                    case ParseUtils.APPROVE_IMPRESSION_ACTION:
                    case ParseUtils.APPROVE_PERSONALITY_ACTION:
                    case ParseUtils.JOIN_CHEERING_GROUP_ACTION:
                        ParseUtils.startMeetArchiveActivity(getContext(), notification.uid);
                        markNotificationProcessed(holder.isNew, notification);
                        break;
                    case ParseUtils.REFEREE_INVITE_NF:
                    case ParseUtils.ADD_CHEERING_GROUP_MEMBER_ACTION:
                        ParseUtils.startMeetArchiveActivity(getContext(), notification.tid);
                        markNotificationProcessed(holder.isNew, notification);
                        break;
                    case ParseUtils.COMMENT_REPLY_NF:
                    case ParseUtils.COMMENT_PRAISED_NF:
                    case ParseUtils.DYNAMIC_COMMENT_NF:
                    case ParseUtils.PRAISE_DYNAMIC_ACTION:
                        ParseUtils.startDynamicDetails(getContext(), notification.id, null);
                        markNotificationProcessed(holder.isNew, notification);
                        break;

                    case ParseUtils.JOIN_GROUP_ACTION:
                    case ParseUtils.APPLY_JOIN_GROUP_NF:
                    case ParseUtils.INVITE_GROUP_MEMBER_ACTION:
                    case FOLLOW_GROUP_ACTION:
                    case MODIFY_GROUP_ACTION:
                    case AUTHENTICATION_VERIFIED_NF:
                        startSubGroupDetails(getContext(), notification.id);
                        markNotificationProcessed(holder.isNew, notification);
                        break;
                    case AUTHENTICATION_REJECTED_NF:
                        Bundle bundle = new Bundle();
                        bundle.putInt("type", 1);
                        SubmitAuthenticationDialogFragment submitAuthenticationDialogFragment = new SubmitAuthenticationDialogFragment();
                        submitAuthenticationDialogFragment.setArguments(bundle);
                        submitAuthenticationDialogFragment.show(fragmentManager, "SubmitAuthenticationDialogFragment");
                        markNotificationProcessed(holder.isNew, notification);
                        break;
                    case INVITE_SINGLE_GROUP_MEMBER_ACTION:
                    case APPLY_JOIN_SINGLE_GROUP_NF:
                    case JOIN_SINGLE_GROUP_ACTION:
                        startSingleGroupDetails(getContext(), notification.id);
                        markNotificationProcessed(holder.isNew, notification);
                        break;
                    default:
                        startMeetArchiveActivity(getContext(),notification.tid);
                        markNotificationProcessed(holder.isNew, notification);
                        break;
                }
            }
        });


    }

    private void showNotification(NotificationFragment.Notification NF) {
        Intent clickIntent = new Intent();
        final NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        switch (NF.type) {
            case EVALUATE_ACTION:
            case REFEREE_ACTION:
            case APPROVE_IMPRESSION_ACTION:
            case APPROVE_PERSONALITY_ACTION:
            case JOIN_CHEERING_GROUP_ACTION:
                clickIntent = new Intent(mContext, MeetArchiveActivity.class);
                clickIntent.putExtra("uid", NF.uid);
                break;
            case REFEREE_INVITE_NF:
            case ADD_CHEERING_GROUP_MEMBER_ACTION:
                clickIntent = new Intent(mContext, MeetArchiveActivity.class);
                clickIntent.putExtra("uid", NF.tid);
                break;
            case INVITE_GROUP_MEMBER_ACTION:
                clickIntent = new Intent(mContext, SingleGroupDetailsActivity.class);
                clickIntent.putExtra("gid", NF.id);
                break;
        }

        PendingIntent clickPI = PendingIntent.getActivity(mContext, 1, clickIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String id = "channel_1";
            String description = "荷塘重要消息";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(id, description, importance);
            channel.enableLights(true);
            if (channel.canShowBadge() == true) {
                channel.setShowBadge(true);
            }
            channel.setBypassDnd(true);
            channel.enableVibration(true);

            notificationManager.createNotificationChannel(channel);
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, id);
            builder.setCategory(android.app.Notification.CATEGORY_MESSAGE)
                    .setSmallIcon(R.drawable.icon)
                    .setContentTitle(NF.trigger.getNickName() + " " + NF.action)
                    .setContentText(NF.content)
                    .setContentIntent(clickPI)
                    .setAutoCancel(true);

            SimpleTarget<Drawable> simpleTarget = new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                    builder.setLargeIcon(drawableToBitmap(resource));
                    android.app.Notification notification = builder.build();
                    notificationManager.notify(1, notification);
                }
            };

            Glide.with(mContext).load(HttpUtil.DOMAIN + NF.trigger.getAvatar()).into(simpleTarget);
        } else {

            android.app.Notification notification = new NotificationCompat.Builder(mContext)
                    .setContentTitle(NF.trigger.getNickName() + " " + NF.action)
                    .setContentText(NF.content)
                    .setContentIntent(clickPI)
                    .setSmallIcon(R.drawable.icon)
                    .build();
            notificationManager.notify(1, notification);
        }

        markNotificationShowed(NF);
    }

    private void markNotificationShowed(final NotificationFragment.Notification notification) {
        RequestBody requestBody = new FormBody.Builder()
                .add("nid", String.valueOf(notification.nid))
                .add("showed", String.valueOf(SHOWED))
                .build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), NotificationFragment.NOTICE_PROCESS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null) {
                    String responseText = response.body().string();
                    notification.showed = SHOWED;
                    mHandler.sendEmptyMessage(MAKE_NOTIFICATION_SHOWED);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Slog.e(TAG, "onFailure e:" + e);
            }
        });
    }

    private void approveAction(NotificationFragment.Notification notification) {
        if (notification.type == ParseUtils.APPLY_CONTACTS_NF) {
            acceptContactsApply(notification.tid);
        } else if (notification.type == ParseUtils.APPLY_JOIN_GROUP_NF) {
            approveSingleGroupApply(notification.id, notification.tid);
        }else if (notification.type == ParseUtils.APPLY_JOIN_SINGLE_GROUP_NF){
            approveSingleGroupApply(notification.id, notification.tid); 
        }else {
            acceptSingleGroupInvite(notification.id, notification.tid);
        }
    }

    private void markNotificationProcessed(TextView isNew, NotificationFragment.Notification notification) {
        markNotificationProcessed(isNew, notification, UNPROCESSED);
    }

    private void markNotificationProcessed(TextView isNew, final NotificationFragment.Notification notification, final int processed) {
        Slog.d(TAG, "------------------>markNotificationProcessed nid: " + notification);
        isNew.setVisibility(View.GONE);
        notification.isNew = READ;
        RequestBody requestBody = new FormBody.Builder()
                .add("nid", String.valueOf(notification.nid))
                .add("processed", String.valueOf(processed)).build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), NotificationFragment.NOTICE_PROCESS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null) {
                    String responseText = response.body().string();
                    if (processed == PROCESSED) {
                        notification.processed = PROCESSED;
                        mHandler.sendEmptyMessage(UPDATE_PROCESS_BUTTON_STATUS);
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Slog.e(TAG, "onFailure e:" + e);
            }
        });
    }

    private void acceptSingleGroupInvite(int gid, int uid) {
        Slog.d(TAG, "=============accept");
        RequestBody requestBody = new FormBody.Builder()
                .add("gid", String.valueOf(gid))
                .add("uid", String.valueOf(uid))
                .build();
        HttpUtil.sendOkHttpRequest(mContext, SingleGroupDetailsActivity.ACCEPT_SUBGROUP_INVITE, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Slog.d(TAG, "==========response body : " + response.body());
                if (response.body() != null) {
                    String responseText = response.body().string();
                    Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        //refresh();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void approveSingleGroupApply(int gid, int uid) {
        Slog.d(TAG, "=============approveSingleGroupApply gid: " + gid);
        RequestBody requestBody = new FormBody.Builder()
                .add("gid", String.valueOf(gid))
                .add("uid", String.valueOf(uid))
                .build();
        HttpUtil.sendOkHttpRequest(mContext, SingleGroupDetailsActivity.APPROVE_JOIN_SINGLE_GROUP, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Slog.d(TAG, "==========response body : " + response.body());
                if (response.body() != null) {
                    String responseText = response.body().string();
                    Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        //refresh();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void startSubGroupDetails(Context context, int gid) {
        Intent intent = new Intent(context, SubGroupDetailsActivity.class);
        intent.putExtra("gid", gid);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        mContext.startActivity(intent);
    }
    
    private void startSingleGroupDetails(Context context, int gid) {
        Intent intent = new Intent(context, SingleGroupDetailsActivity.class);
        intent.putExtra("gid", gid);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        mContext.startActivity(intent);
    }


    @Override
    public int getItemCount() {
        return notificationList != null ? notificationList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView isNew;
        TextView action;
        RoundImageView avatar;
        TextView name;
        //TextView profile;
        TextView content;
        Button acceptBtn;
        TextView timeStamp;
        ConstraintLayout item;

        public ViewHolder(View view) {

            super(view);
            isNew = view.findViewById(R.id.is_new);
            action = view.findViewById(R.id.action);
            avatar = view.findViewById(R.id.avatar);
            name = view.findViewById(R.id.name);
            //profile = view.findViewById(R.id.profile);
            content = view.findViewById(R.id.content);
            acceptBtn = view.findViewById(R.id.accept);
            timeStamp = view.findViewById(R.id.timestamp);
            item = view.findViewById(R.id.notification_item);

            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.is_new), font);
        }
    }

}
                    
