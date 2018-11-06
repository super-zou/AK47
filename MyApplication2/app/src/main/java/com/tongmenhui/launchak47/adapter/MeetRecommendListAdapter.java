package com.tongmenhui.launchak47.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.NetworkImageView;
import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.meet.ArchivesActivity;
import com.tongmenhui.launchak47.meet.MeetMemberInfo;
import com.tongmenhui.launchak47.util.FontManager;
import com.tongmenhui.launchak47.util.HttpUtil;
import com.tongmenhui.launchak47.util.RequestQueueSingleton;
import com.tongmenhui.launchak47.util.Slog;

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

public class MeetRecommendListAdapter extends RecyclerView.Adapter<MeetRecommendListAdapter.ViewHolder>{
    private static final String TAG = "MeetRecommendList";
    private static final String  domain = "http://112.126.83.127:88";
    //+Begin by xuchunping
    private static final String  LOVED_URL = HttpUtil.DOMAIN + "?q=meet/love/add";
    private static final String  PRAISED_URL = HttpUtil.DOMAIN + "?q=meet/praise/add";
    private static final int UPDATE_LOVE_COUNT = 0;
    private static final int UPDATE_PRAISED_COUNT = 1;
    //-End by xuchunping

    private List<MeetMemberInfo> mMeetList;
    private String picture_url;
    private static Context mContext;
    private boolean isScrolling = false;

    RequestQueue queue;

    public void setScrolling(boolean isScrolling){
        this.isScrolling = isScrolling;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{
        //TextView addMeetInfo;
        TextView realname;
        TextView lives;
        TextView selfcondition;
        TextView requirement;
        NetworkImageView headUri;
        TextView illustration;
        TextView eyeView;
        TextView lovedView;
        TextView lovedIcon;
        TextView thumbsView;
        TextView thumbsIcon;
        TextView photosView;

        public ViewHolder(View view){

            super(view);
            //addMeetInfo = (TextView)view.findViewById(R.id.meet_info_add);
            realname = (TextView) view.findViewById(R.id.name);
            lives = (TextView) view.findViewById(R.id.lives);
            headUri = (NetworkImageView) view.findViewById(R.id.recommend_head_uri);
            selfcondition = (TextView) view.findViewById(R.id.self_condition);
            requirement = (TextView) view.findViewById(R.id.partner_requirement);
            illustration = (TextView) view.findViewById(R.id.illustration);
            eyeView = (TextView)view.findViewById(R.id.eye_statistics);
            lovedView = (TextView)view.findViewById(R.id.loved_statistics);
            lovedIcon = (TextView)view.findViewById(R.id.loved_icon);
            thumbsView = (TextView)view.findViewById(R.id.thumbs_up_statistics);
            thumbsIcon = (TextView)view.findViewById(R.id.thumbs_up_icon);
            photosView = (TextView)view.findViewById(R.id.photos_statistics);

            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.behavior_statistics), font);

        }
    }

    public MeetRecommendListAdapter(Context context){
        Slog.d(TAG, "==============MeetRecommendListAdapter init=================");
        mContext = context;
        mMeetList = new ArrayList<MeetMemberInfo>();
    }
    public void setData(List<MeetMemberInfo> meetList){
        mMeetList = meetList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        //Slog.d(TAG, "===========onCreateViewHolder==============");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.meet_item, parent, false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position){

       // Slog.d(TAG, "===========onBindViewHolder==============");
        final MeetMemberInfo meet = mMeetList.get(position);
       // Slog.d(TAG, "get name============="+meet.getRealname());
        holder.realname.setText(meet.getRealname());
        holder.lives.setText(meet.getLives());

        if(!"".equals(meet.getPictureUri()) && !isScrolling){
            picture_url = HttpUtil.DOMAIN+meet.getPictureUri();
            queue = RequestQueueSingleton.instance(mContext);

            holder.headUri.setTag(picture_url);
            HttpUtil.loadByImageLoader(queue, holder.headUri, picture_url, 110, 110);
        }else{
            holder.headUri.setImageDrawable(mContext.getDrawable(R.mipmap.ic_launcher));
        }


        holder.selfcondition.setText(meet.getSelfCondition(meet.getSituation()));
        holder.requirement.setText(meet.getRequirement());

        holder.eyeView.setText(String.valueOf(meet.getBrowseCount()));
        holder.lovedView.setText(String.valueOf(meet.getLovedCount()));
        holder.thumbsView.setText(String.valueOf(meet.getPraisedCount()));
        holder.illustration.setText(meet.getIllustration());

        //+Begin by xuchunping
        holder.lovedIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (1 == meet.getLoved()){
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
        holder.headUri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ArchivesActivity.class);
                Log.d(TAG, "meet:"+meet+" uid:"+meet.getUid());
                intent.putExtra("meet", meet);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                mContext.startActivity(intent);
            }
        });
        holder.photosView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ArchivesActivity.class);
                intent.putExtra("meet", meet);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount(){

        return mMeetList != null ? mMeetList.size():0;
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
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

    private void sendMessage(int what, Object obj){
        Message msg = mHandler.obtainMessage();
        msg.what = what;
        msg.obj = obj;
        msg.sendToTarget();
    }

    private void sendMessage(int what){
        sendMessage(what, null);
    }

    private void praiseArchives(final MeetMemberInfo meetMemberInfo){
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(meetMemberInfo.getUid())).build();
        HttpUtil.sendOkHttpRequest(mContext, PRAISED_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Log.d(TAG,"praiseArchives responseText:"+responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject commentResponse = new JSONObject(responseText);
                        int status = commentResponse.optInt("status");
                        Log.d(TAG,"praiseArchives status:"+status);
                        if (1 == status) {
                            MeetMemberInfo member = getMeetMemberById(meetMemberInfo.getUid());
                            member.setPraised(1);
                            member.setPraisedCount(meetMemberInfo.getPraisedCount() + 1);
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

    private void love(final MeetMemberInfo meetMemberInfo){
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(meetMemberInfo.getUid())).build();
        HttpUtil.sendOkHttpRequest(mContext, LOVED_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Log.d(TAG,"love responseText"+responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject commentResponse = new JSONObject(responseText);
                        int status = commentResponse.optInt("status");
                        Log.d(TAG,"love status"+status);
                        if (1 == status) {
                            MeetMemberInfo member = getMeetMemberById(meetMemberInfo.getUid());
                            member.setLoved(1);
                            member.setLovedCount(meetMemberInfo.getLovedCount() + 1);
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

    private MeetMemberInfo getMeetMemberById(int uid) {
        if (null == mMeetList){
            return null;
        }
        for(int i = 0;i < mMeetList.size();i++) {
            if (uid == mMeetList.get(i).getUid()) {
                return mMeetList.get(i);
            }
        }
        return null;
    }
    //-End by xuchunping
}
