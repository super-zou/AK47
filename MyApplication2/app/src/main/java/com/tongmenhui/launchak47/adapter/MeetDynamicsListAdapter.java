package com.tongmenhui.launchak47.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.NetworkImageView;
import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.meet.DynamicsComment;
import com.tongmenhui.launchak47.meet.MeetDynamics;
import com.tongmenhui.launchak47.meet.MeetDynamicsFragment;
import com.tongmenhui.launchak47.util.CommentDialogFragment;
import com.tongmenhui.launchak47.util.CommentDialogFragmentInterface;
import com.tongmenhui.launchak47.util.FontManager;
import com.tongmenhui.launchak47.util.HttpUtil;
import com.tongmenhui.launchak47.util.RequestQueueSingleton;
import com.tongmenhui.launchak47.util.Slog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by haichao.zou on 2017/11/20.
 */

public class MeetDynamicsListAdapter extends RecyclerView.Adapter<MeetDynamicsListAdapter.ViewHolder> {

    private static final String TAG = "MeetDynamicsListAdapter";
    private static final String  domain = "http://112.126.83.127:88";
    //+ added by xuchunping
    private static final String  EYE_URL = HttpUtil.DOMAIN + "";
    private static final String  LOVED_URL = HttpUtil.DOMAIN + "?q=meet/love/add";
    private static final String  PRAISED_URL = HttpUtil.DOMAIN + "?q=meet/praise/add";
    private static final String  PRAISED_DYNAMICS_URL = HttpUtil.DOMAIN + "?q=meet/activity/interact/praise/add";

    private static final int UPDATE_LOVE_COUNT = 0;
    private static final int UPDATE_PRAISED_COUNT = 1;
    //- added by xuchunping
    private List<MeetDynamics> mMeetList;
    private String picture_url;
    private static Context mContext;
    private boolean isScrolling = false;
    RequestQueue queueMemberInfo;
    RequestQueue queueDynamics;
    //RequestQueue queueComment;
    RequestQueueSingleton requestQueueSingleton;
    static MeetDynamicsFragment meetDynamicsFragment = new MeetDynamicsFragment();
    private LocalBroadcastManager localBroadcastManager;
    CommentDialogFragmentInterface commentDialogFragmentListener;

    public void setScrolling(boolean isScrolling){
        this.isScrolling = isScrolling;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
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
        TextView thumbsUpIcon;
        TextView photosView;
        TextView contentView;
        TextView createdView;
        TextView dynamicsPraiseIcon;
        TextView dynamicsPraiseCount;
        TextView dynamicsCommentCount;
        TextView dynamicsCommentIcon;
        GridLayout dynamicsGrid;
        LinearLayout commentList;

        public ViewHolder(View view){

            super(view);
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
            thumbsUpIcon = (TextView)view.findViewById(R.id.thumbs_up_icon);
            photosView = (TextView)view.findViewById(R.id.photos_statistics);
            contentView = (TextView)view.findViewById(R.id.dynamics_content);
            dynamicsGrid = (GridLayout) view.findViewById(R.id.dynamics_picture_grid);
            createdView = (TextView) view.findViewById(R.id.dynamic_time);
            dynamicsPraiseIcon = (TextView) view.findViewById(R.id.dynamic_praise_icon);
            dynamicsPraiseCount = (TextView) view.findViewById(R.id.dynamic_praise);
            dynamicsCommentCount = (TextView) view.findViewById(R.id.dynamic_comment_count);
            dynamicsCommentIcon = (TextView) view.findViewById(R.id.dynamic_comment_icon);

            commentList = (LinearLayout) view.findViewById(R.id.dynamics_comments);
            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.meet_dynamics_item), font);

        }
    }

    public MeetDynamicsListAdapter(Context context){
        //Slog.d(TAG, "==============MeetRecommendListAdapter init=================");
        mContext = context;
        mMeetList = new ArrayList<MeetDynamics>();
        requestQueueSingleton = new RequestQueueSingleton();


    }

    public void setData(List<MeetDynamics> meetList){
        mMeetList = meetList;
    }

    @Override
    public MeetDynamicsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
       // Slog.d(TAG, "===========onCreateViewHolder==============");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.meet_dynamics_item, parent, false);
        MeetDynamicsListAdapter.ViewHolder holder = new MeetDynamicsListAdapter.ViewHolder(view);

        return holder;
    }

    //TODO 此处有异常，先屏蔽 by xuchunping 2018.8.5
    /*@Override
    public void onViewRecycled(MeetDynamicsListAdapter.ViewHolder holder){
        super.onViewRecycled(holder);
    }*/

    @Override
    public void onBindViewHolder(final MeetDynamicsListAdapter.ViewHolder holder, int position){

       // Slog.d(TAG, "===========onBindViewHolder==============");
        final MeetDynamics meetDynamics = mMeetList.get(position);
        holder.realname.setText(meetDynamics.getRealname());
        holder.lives.setText(meetDynamics.getLives());

        if(!"".equals(meetDynamics.getPictureUri()) && !isScrolling){
            picture_url = domain+"/"+meetDynamics.getPictureUri();
            //queueMemberInfo = new Volley().newRequestQueue(mContext);
            queueMemberInfo = requestQueueSingleton.instance(mContext);
            holder.headUri.setTag(picture_url);
            HttpUtil.loadByImageLoader(queueMemberInfo, holder.headUri, picture_url, 110, 110);
        }else{
            holder.headUri.setImageDrawable(mContext.getDrawable(R.mipmap.ic_launcher));
        }

        holder.selfcondition.setText(meetDynamics.getSelfCondition(meetDynamics.getSituation()));
        holder.requirement.setText(meetDynamics.getRequirement());
        holder.eyeView.setText(String.valueOf(meetDynamics.getBrowseCount()));
        holder.lovedView.setText(String.valueOf(meetDynamics.getLovedCount()));
        holder.thumbsView.setText(String.valueOf(meetDynamics.getPraisedCount()));
        holder.illustration.setText(meetDynamics.getIllustration());
        holder.contentView.setText(meetDynamics.getContent());

        String pictures = meetDynamics.getActivityPicture();


        if(!"".equals(pictures) && !isScrolling){
            holder.dynamicsGrid.removeAllViews();
            queueDynamics = requestQueueSingleton.instance(mContext);

            String[] picture_array = pictures.split(":");
            int length = picture_array.length;
            if(length > 0){
                if(length != 4){
                    holder.dynamicsGrid.setColumnCount(3);
                }else{
                    holder.dynamicsGrid.setColumnCount(2);
                }

                for (int i = 0; i < length; i++){

                    if(picture_array[i] != null){
                        LinearLayout linearLayout = new LinearLayout(mContext);
                        LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(200, 200);
                        layoutParams.setMargins(4, 0, 0, 4);
                        //将以上的属性赋给LinearLayout
                        linearLayout.setLayoutParams(layoutParams);

                        NetworkImageView picture = new NetworkImageView(mContext);

                        linearLayout.addView(picture);
                        holder.dynamicsGrid.addView(linearLayout);

                        picture.setImageDrawable(mContext.getDrawable(R.mipmap.ic_launcher));
                        picture.setTag(domain+"/"+picture_array[i]);
                        HttpUtil.loadByImageLoader(queueDynamics, picture, domain+"/"+picture_array[i], 200, 200);
                    }
                }

            }

        }else{
            holder.dynamicsGrid.removeAllViews();
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ImageView picture = new ImageView(mContext);
            picture.setLayoutParams(lp);
            holder.dynamicsGrid.addView(picture);
        }
        holder.createdView.setText(meetDynamics.getCreated());
        holder.dynamicsPraiseCount.setText(String.valueOf(meetDynamics.getPraisedDynamicsCount()));
        holder.dynamicsCommentCount.setText(String.valueOf(meetDynamics.getCommentCount()));
        holder.commentList.removeAllViews();
        setDynamicsCommentView(holder.commentList, meetDynamics.dynamicsCommentList);

        //added by xuchunping
        holder.lovedIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO change UI to show loved or no
                if (1 == meetDynamics.getLoved()){
                    Toast.makeText(mContext, "You have loved it!", Toast.LENGTH_SHORT).show();
                    return;
                }
                love(meetDynamics);
            }
        });
        holder.thumbsUpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO change UI to show parised or no
                if (1 == meetDynamics.getPraised()) {
                    Toast.makeText(mContext, "You have praised it!", Toast.LENGTH_SHORT).show();
                    return;
                }
                praiseArchives(meetDynamics);
            }
        });
        holder.dynamicsPraiseIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO change UI to show parised or no
                if (1 == meetDynamics.getPraisedDynamics()) {
                    Toast.makeText(mContext, "You have praised it!", Toast.LENGTH_SHORT).show();
                    return;
                }
                praiseDynamics(meetDynamics);
            }
        });

        //when comment icon touched should show comment input dialog
        holder.dynamicsCommentIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show the comment input dialog fragment
                commentDialogFragmentListener.onCommentClick();
            }
        });


        //sendDynamicsComment(holder);
    }

    public void setDynamicsCommentView(LinearLayout linearLayout, List<DynamicsComment> dynamicsCommentList){
        //Slog.d(TAG, "*************dynamicsCommentList : "+dynamicsCommentList);
        if(dynamicsCommentList.size() != 0){
            for (int i=0; i<dynamicsCommentList.size(); i++){
                View viewComment = View.inflate(mContext, R.layout.dynamics_comment_item, null);
                linearLayout.addView(viewComment);

                NetworkImageView imageView = (NetworkImageView)viewComment.findViewById(R.id.comment_picture);
                //imageView.setImageDrawable(mContext.getDrawable(R.mipmap.ic_launcher));
                RequestQueue queueComment = requestQueueSingleton.instance(mContext);
                //Slog.d(TAG, "$$$$$$$$$$$$$$$$getUrl: "+dynamicsCommentList.get(i).getPictureUrl());
                imageView.setTag(domain+"/"+dynamicsCommentList.get(i).getPictureUrl());
                HttpUtil.loadByImageLoader(queueComment, imageView, domain+"/"+dynamicsCommentList.get(i).getPictureUrl(), 50, 50);

                if(dynamicsCommentList.get(i) != null){
                    if(dynamicsCommentList.get(i).getType() == 0){//comment
                        TextView author = (TextView)viewComment.findViewById(R.id.author_name);
                        Slog.d(TAG, "$$$$$$$$$$$$$$$$$$$$$comment author: "+dynamicsCommentList.get(i).getCommenterName());
                        author.setText(dynamicsCommentList.get(i).getCommenterName());

                    }else{//reply
                        TextView commenter = (TextView)viewComment.findViewById(R.id.commenter_name);
                        commenter.setVisibility(View.VISIBLE);
                        commenter.setText(dynamicsCommentList.get(i).getCommenterName());

                        TextView replyFlag = (TextView)viewComment.findViewById(R.id.reply_flag);
                        replyFlag.setVisibility(View.VISIBLE);

                        TextView author = (TextView)viewComment.findViewById(R.id.author_name);
                        author.setText(dynamicsCommentList.get(i).getAuthorName());
                    }
                    Slog.d(TAG, "====================comment content: "+dynamicsCommentList.get(i).getContent());
                    TextView content = (TextView)viewComment.findViewById(R.id.content);
                    content.setText(dynamicsCommentList.get(i).getContent());
                }
            }
        }
    }

    @Override
    public int getItemCount(){
        return mMeetList.size();
    }

    public void addData(int position, MeetDynamics meetDynamics){
        mMeetList.add(position, meetDynamics);
        notifyItemInserted(position);
    }

    //+added by xuchunping
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

    private void praiseArchives(final MeetDynamics meetDynamics){
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(meetDynamics.getUid())).build();
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
                            updatePraisedByUid(meetDynamics.getUid(), 1);
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

    private void praiseDynamics(final MeetDynamics meetDynamics){
        RequestBody requestBody = new FormBody.Builder().add("aid", String.valueOf(meetDynamics.getAid())).build();
        HttpUtil.sendOkHttpRequest(mContext, PRAISED_DYNAMICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Log.d(TAG,"praiseDynamics responseText:"+responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject commentResponse = new JSONObject(responseText);
                        int status = commentResponse.optInt("status");
                        Log.d(TAG,"praiseDynamics status:"+status);
                        if (1 == status) {
                            MeetDynamics tempInfo = getMeetDynamicsById(meetDynamics.getAid());
                            tempInfo.setPraisedDynamics(1);
                            tempInfo.setPraisedDynamicsCount(meetDynamics.getPraisedDynamicsCount() + 1);
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

    private void love(final MeetDynamics meetDynamics){
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(meetDynamics.getUid())).build();
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
                            updateLovedByUid(meetDynamics.getUid(), 1);
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

    private MeetDynamics getMeetDynamicsById(long aId) {
        if (null == mMeetList){
            return null;
        }
        for(int i = 0;i < mMeetList.size();i++) {
            if (aId == mMeetList.get(i).getAid()) {
                return mMeetList.get(i);
            }
        }
        return null;
    }

    private void updateLovedByUid(int uid, int loved) {
        if (null == mMeetList){
            return;
        }
        MeetDynamics meet = null;
        for(int i = 0;i < mMeetList.size();i++) {
            if (uid == mMeetList.get(i).getUid()) {
                meet = mMeetList.get(i);
                meet.setLoved(loved);
                meet.setLovedCount(meet.getLovedCount() + 1);
            }
        }
    }

    private void updatePraisedByUid(int uid, int praised) {
        if (null == mMeetList){
            return;
        }
        MeetDynamics meet = null;
        for(int i = 0;i < mMeetList.size();i++) {
            if (uid == mMeetList.get(i).getUid()) {
                meet = mMeetList.get(i);
                meet.setPraised(praised);
                meet.setPraisedCount(meet.getPraisedCount() + 1);
            }
        }
    }
    //-added by xuchunping
    /*
     *Send the dynamics' comment, by zouhaichao 2018/9/7
     */
    private void sendDynamicsComment(final MeetDynamicsListAdapter.ViewHolder holder){
    }

    public void setOnCommentClickListener (CommentDialogFragmentInterface commentDialogFragmentListener) {
        this.commentDialogFragmentListener = commentDialogFragmentListener;
    }

}


