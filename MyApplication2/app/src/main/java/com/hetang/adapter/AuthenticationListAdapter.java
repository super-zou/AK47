package com.hetang.adapter;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
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
import com.hetang.common.AuthenticationFragment;
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
import static com.hetang.common.AuthenticationActivity.VERIFIED;
import static com.hetang.common.AuthenticationActivity.unVERIFIED;
import static com.hetang.common.MyApplication.getContext;
import static com.hetang.util.Utility.getDateToString;

public class AuthenticationListAdapter extends RecyclerView.Adapter<AuthenticationListAdapter.ViewHolder> {
    private static final String TAG = "AuthenticationListAdapter";
    private List<AuthenticationFragment.Authentication> authenticationList = new ArrayList<>();
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
    private int type;
    private FragmentManager fragmentManager;
    
    public AuthenticationListAdapter(Context context) {
        mContext = context;
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
    
    public void setData(List<AuthenticationFragment.Authentication> authenticationList, int type) {
        this.type = type;
        this.authenticationList = authenticationList;
    }

    public void setScrolling(boolean isScrolling) {
        this.isScrolling = isScrolling;
    }
    
    @Override
    public AuthenticationListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.authentication_item, parent, false);
        AuthenticationListAdapter.ViewHolder holder = new AuthenticationListAdapter.ViewHolder(view);
        return holder;
    }
    
    @Override
    public void onBindViewHolder(@NonNull final AuthenticationListAdapter.ViewHolder holder, int position) {
        final AuthenticationFragment.Authentication authentication = authenticationList.get(position);

        if (authentication.getAvatar() != null && !"".equals(authentication.getAvatar())) {
            Glide.with(mContext).load(HttpUtil.DOMAIN + authentication.getAvatar()).into(holder.avatar);
        } else {
            if (authentication.getSex() == Utility.MALE) {
                holder.avatar.setImageDrawable(mContext.getDrawable(R.drawable.male_default_avator));
            } else {
                holder.avatar.setImageDrawable(mContext.getDrawable(R.drawable.female_default_avator));
            }
        }
        
        Glide.with(getContext()).load(HttpUtil.DOMAIN + authentication.authenticationPhotoUrl).into(holder.authenticationPhoto);

        holder.name.setText(authentication.getName());
        if (authentication.getSex() == 0){
            holder.sex.setText(mContext.getResources().getString(R.string.male));
            holder.sex.setTextColor(mContext.getResources().getColor(R.color.background));
        }else {
            holder.sex.setText(mContext.getResources().getString(R.string.female));
            holder.sex.setTextColor(mContext.getResources().getColor(R.color.color_red_ccfa3c55));
        }
        
        holder.major.setText(authentication.getMajor());
        holder.degree.setText(authentication.getDegreeName(authentication.getDegree()));
        holder.university.setText(authentication.getUniversity());

        String dataString = getDateToString(authentication.requestTime, "yyyy-MM-dd");
        holder.timeStamp.setText(dataString);
        
        if (type == unVERIFIED){
            holder.verify.setVisibility(View.VISIBLE);
            holder.reject.setVisibility(View.VISIBLE);

            holder.verify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    verifyAction(authentication.aid, authentication.uid);
                }
            });
            
            holder.reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    rejectAction(authentication.aid, authentication.uid);
                }
            });
        }

        holder.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUtils.startMeetArchiveActivity(mContext, authentication.getUid());
            }
        });


    }
    
    private void verifyAction(int aid, int uid) {

    }

    private void rejectAction(int aid, int uid) {

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
    
    @Override
    public int getItemCount() {
        return authenticationList != null ? authenticationList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView sex;
        TextView major;
        RoundImageView avatar;
        RoundImageView authenticationPhoto;
        TextView name;
        TextView university;
        TextView degree;
        Button verify;
        Button reject;
        TextView timeStamp;
        
        public ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            sex = view.findViewById(R.id.sex);
            avatar = view.findViewById(R.id.avatar);
            authenticationPhoto = view.findViewById(R.id.authenticationImageView);
            major = view.findViewById(R.id.major);
            degree = view.findViewById(R.id.degree);
            university = view.findViewById(R.id.university);
            verify = view.findViewById(R.id.verify);
            reject = view.findViewById(R.id.reject);
            timeStamp = view.findViewById(R.id.timestamp);
        }
    }

}
