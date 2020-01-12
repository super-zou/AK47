package com.hetang.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.meet.UserMeetInfo;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.common.MyApplication;
import com.hetang.util.RoundImageView;
import com.hetang.util.Slog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by haichao.zou on 2017/9/15.
 */

public class ConcernedRecommendAdapter extends RecyclerView.Adapter<ConcernedRecommendAdapter.ViewHolder> {
    private static final boolean isDebug = false;
    private static final String TAG = "MeetRecommendList";
    private static final String domain = "http://112.126.83.127:88";
    //+Begin by xuchunping
    private static final String LOVED_URL = HttpUtil.DOMAIN + "?q=meet/love/add";
    private static final String PRAISED_URL = HttpUtil.DOMAIN + "?q=meet/praise/add";
    private static final int UPDATE_LOVE_COUNT = 0;
    private static final int UPDATE_PRAISED_COUNT = 1;
    
    //-End by xuchunping
    private static Context mContext;
    RequestQueue queue;
    private List<UserMeetInfo> mMeetList;
    private boolean isScrolling = false;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_LOVE_COUNT:
                    notifyDataSetChanged();
                    break;
                case UPDATE_PRAISED_COUNT:
                    notifyDataSetChanged();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    
    private MyItemClickListener mItemClickListener;


    public ConcernedRecommendAdapter(Context context) {
        if (isDebug) Slog.d(TAG, "==============MeetRecommendListAdapter init=================");
        mContext = context;
        mMeetList = new ArrayList<UserMeetInfo>();
    }

    public void setScrolling(boolean isScrolling) {
        this.isScrolling = isScrolling;
    }

public void setData(List<UserMeetInfo> meetList) {
        mMeetList = meetList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Slog.d(TAG, "===========onCreateViewHolder==============");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.meet_discovery_item, parent, false);
        ViewHolder holder = new ViewHolder(view, mItemClickListener);

        return holder;
    }
    
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final UserMeetInfo meet = mMeetList.get(position);
        holder.name.setText(meet.getNickName());

        if(meet.getActivityCount() > 0 && !isScrolling){
            holder.activityIndicator.setVisibility(View.VISIBLE);
        }else {
            holder.activityIndicator.setVisibility(View.GONE);
        }
        
        if(meet.getLiving() != null && !TextUtils.isEmpty(meet.getLiving())){
            holder.living.setText(meet.getLiving());
        }

        if (!"".equals(meet.getAvatar()) && !isScrolling) {
            Glide.with(mContext).load(HttpUtil.DOMAIN + meet.getAvatar()).into(holder.headUri);
        } else {
            if(meet.getSex() == 0){
                holder.headUri.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.male_default_avator));
            }else {
                holder.headUri.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.female_default_avator));
            }
        }
        
        holder.selfcondition.setText(meet.getSelfCondition(meet.getSituation()));
        holder.requirement.setText(meet.getRequirement());

        if(meet.getLovedCount() > 0 && !isScrolling){
            if(meet.getLoved() == 1 ){
                holder.lovedIcon.setText(R.string.fa_heart);
            }
        }else {
            holder.lovedIcon.setText(R.string.fa_heart_o);
        }
        
        if(meet.getPraisedCount() > 0 && !isScrolling){
            if(meet.getPraised() == 1 ){
                holder.thumbsIcon.setText(R.string.fa_thumbs_up);
            }
        }else {
            holder.thumbsIcon.setText(R.string.fa_thumbs_O_up);
        }

        if(meet.getLovedCount() > 0){
            holder.lovedView.setText(String.valueOf(meet.getLovedCount()));
        }
        
        if(meet.getPraisedCount() > 0){
            holder.thumbsView.setText(String.valueOf(meet.getPraisedCount()));
        }

        if(meet.getPictureCount() > 0 && !isScrolling){
            holder.thumbnailWrapper.setVisibility(View.VISIBLE);
            holder.photosView.setText(String.valueOf(meet.getPictureCount()));

            Glide.with(mContext).load(HttpUtil.DOMAIN+meet.getThumbnail()).into(holder.thumbnail);

        }else {
            if(holder.thumbnailWrapper.getVisibility() == View.VISIBLE){
                holder.thumbnailWrapper.setVisibility(View.GONE);
            }
        }
        
        if(!TextUtils.isEmpty(meet.getIllustration()) && !isScrolling){
            holder.illustration.setVisibility(View.VISIBLE);
            holder.illustration.setText("“"+meet.getIllustration()+"”");
        }else {
            holder.illustration.setVisibility(View.GONE);
        }

        if(mItemClickListener != null){
            holder.recommendLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mItemClickListener.onItemClick(view, position);
                }
            });
        }
        
        holder.lovedIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (1 == meet.getLoved()) {
                    Toast.makeText(mContext, "You have loved it!", Toast.LENGTH_SHORT).show();
                    return;
                }
                love(meet);
            }
        });
        holder.thumbsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO change UI to show parised or no
                if (1 == meet.getPraised()) {
                    Toast.makeText(mContext, "You have praised it!", Toast.LENGTH_SHORT).show();
                    return;
                }
                praiseArchives(meet);
            }
        });
        
        }

    @Override
    public int getItemCount() {

        return mMeetList != null ? mMeetList.size() : 0;
    }

    private void sendMessage(int what, Object obj) {
        Message msg = mHandler.obtainMessage();
        msg.what = what;
        msg.obj = obj;
        msg.sendToTarget();
    }

    private void sendMessage(int what) {
        sendMessage(what, null);
    }
    
    private void praiseArchives(final UserMeetInfo userMeetInfo) {
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(userMeetInfo.getUid())).build();
        HttpUtil.sendOkHttpRequest(mContext, PRAISED_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug) Log.d(TAG, "praiseArchives responseText:" + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                try {
                        JSONObject commentResponse = new JSONObject(responseText);
                        int status = commentResponse.optInt("status");
                        if (isDebug) Log.d(TAG, "praiseArchives status:" + status);
                        if (1 == status) {
                            //UserMeetInfo member = getMeetMemberById(userMeetInfo.getUid());
                            userMeetInfo.setPraised(1);
                            userMeetInfo.setPraisedCount(userMeetInfo.getPraisedCount() + 1);
                            sendMessage(UPDATE_PRAISED_COUNT);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }

    private void love(final UserMeetInfo userMeetInfo) {
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(userMeetInfo.getUid())).build();
        HttpUtil.sendOkHttpRequest(mContext, LOVED_URL, requestBody, new Callback() {
        @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug) Log.d(TAG, "love responseText" + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject commentResponse = new JSONObject(responseText);
                        int status = commentResponse.optInt("status");
                        if (isDebug) Log.d(TAG, "love status" + status);
                        if (1 == status) {
                            //UserMeetInfo member = getMeetMemberById(userMeetInfo.getUid());
                            userMeetInfo.setLoved(1);
                            userMeetInfo.setLovedCount(userMeetInfo.getLovedCount() + 1);
                            sendMessage(UPDATE_LOVE_COUNT);
                        }
                    } catch (JSONException e) {
                       e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }
    
    private UserMeetInfo getMeetMemberById(int uid) {
        if (null == mMeetList) {
            return null;
        }
        for (int i = 0; i < mMeetList.size(); i++) {
            if (uid == mMeetList.get(i).getUid()) {
                return mMeetList.get(i);
            }
        }
        return null;
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        //TextView addMeetInfo;
        TextView name;
        TextView living;
        TextView selfcondition;
        TextView requirement;
        RoundImageView headUri;
        TextView illustration;
        TextView lovedView;
        TextView lovedIcon;
        TextView thumbsView;
        TextView thumbsIcon;
        LinearLayout thumbnailWrapper;
        RoundImageView thumbnail;
        TextView photosView;
        TextView activityIndicator;
        ConstraintLayout recommendLayout;
        private MyItemClickListener mListener;
        
        public ViewHolder(View view, MyItemClickListener myItemClickListener) {

            super(view);
            //addMeetInfo = (TextView)view.findViewById(R.id.meet_info_add);
            recommendLayout = view.findViewById(R.id.meet_item_id);
            name = (TextView) view.findViewById(R.id.name);
            living = (TextView) view.findViewById(R.id.living);
            headUri =  view.findViewById(R.id.avatar);
            selfcondition = (TextView) view.findViewById(R.id.self_condition);
            requirement = (TextView) view.findViewById(R.id.partner_requirement);
            illustration = (TextView) view.findViewById(R.id.illustration);
            lovedView = (TextView) view.findViewById(R.id.loved_statistics);
            lovedIcon = (TextView) view.findViewById(R.id.loved_icon);
            thumbsView = (TextView) view.findViewById(R.id.thumbs_up_statistics);
            thumbsIcon = (TextView) view.findViewById(R.id.thumbs_up_icon);
            
            photosView = (TextView) view.findViewById(R.id.photos_statistics);
            activityIndicator = view.findViewById(R.id.activity_indicator);

            this.mListener = myItemClickListener;
            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.behavior_statistics), font);
            FontManager.markAsIconContainer(view.findViewById(R.id.activity_indicator), font);

        }
        
        /**
         * 实现OnClickListener接口重写的方法
         * @param v
         */
        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(v, getPosition());
            }

        }
    }
    //-End by xuchunping
    
    /**
     * 创建一个回调接口
     */
    public interface MyItemClickListener {
        void onItemClick(View view, int position);
    }

    /**
     * 在activity里面adapter就是调用的这个方法,将点击事件监听传递过来,并赋值给全局的监听
     *
     * @param myItemClickListener
     */
    public void setItemClickListener(MyItemClickListener myItemClickListener) {
        this.mItemClickListener = myItemClickListener;
    }
}
