package com.hetang.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.authenticate.AuthenticateOperationInterface;
import com.hetang.authenticate.RequestFragment;
import com.hetang.util.HttpUtil;
import com.hetang.util.ParseUtils;
import com.hetang.util.RoundImageView;
import com.hetang.util.Utility;

import java.util.ArrayList;
import java.util.List;

import static com.hetang.authenticate.AuthenticationActivity.unVERIFIED;
import static com.hetang.common.MyApplication.getContext;
import static com.hetang.util.Utility.getDateToString;

public class AuthenticatePassedtListAdapter extends RecyclerView.Adapter<AuthenticatePassedtListAdapter.ViewHolder> {
    private static final String TAG = "AuthenticateRequestListAdapter";
    private List<RequestFragment.Authentication> authenticationList = new ArrayList<>();
    private Context mContext;
    private boolean isScrolling = false;

    private static final int UPDATE_PROCESS_BUTTON_STATUS = 0;
    private static final int MAKE_NOTIFICATION_SHOWED = 1;
    private Handler mHandler;
    private int type;
    private FragmentManager fragmentManager;
    private static AuthenticateOperationInterface authenticateOperationInterface;

    public AuthenticatePassedtListAdapter(Context context) {
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

    public void setData(List<RequestFragment.Authentication> authenticationList, int type) {
        this.type = type;
        this.authenticationList = authenticationList;
    }

    public void setScrolling(boolean isScrolling) {
        this.isScrolling = isScrolling;
    }

    //register the interActInterface callback, add by zouhaichao 2018/9/16
    public void setOnItemClickListener(AuthenticateOperationInterface authenticateOperationInterface) {
        this.authenticateOperationInterface = authenticateOperationInterface;
    }

    @Override
    public AuthenticatePassedtListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.authentication_item, parent, false);
        AuthenticatePassedtListAdapter.ViewHolder holder = new AuthenticatePassedtListAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final AuthenticatePassedtListAdapter.ViewHolder holder, final int position) {
        final RequestFragment.Authentication authentication = authenticationList.get(position);

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
            holder.pass.setVisibility(View.VISIBLE);
            holder.reject.setVisibility(View.VISIBLE);

            holder.pass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //verifyAction(authentication.aid, authentication.uid);
                    authenticateOperationInterface.onPassClick(view, position);
                }
            });
            
            holder.reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    authenticateOperationInterface.onRejectClick(view, position);
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
        Button pass;
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
            pass = view.findViewById(R.id.pass);
            reject = view.findViewById(R.id.reject);
            timeStamp = view.findViewById(R.id.timestamp);
        }
    }

}
