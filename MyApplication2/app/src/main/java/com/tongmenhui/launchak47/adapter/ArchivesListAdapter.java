package com.tongmenhui.launchak47.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.tongmenhui.launchak47.util.FontManager;
import com.tongmenhui.launchak47.util.HttpUtil;
import com.tongmenhui.launchak47.util.RequestQueueSingleton;
import com.tongmenhui.launchak47.util.Slog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ArchivesListAdapter  extends RecyclerView.Adapter<ArchivesListAdapter.ViewHolder> {

    private static final String TAG = "ArchivesListAdapter";
    private static final int UPDATE_PRAISED_COUNT = 1;
    private static final String  PRAISED_DYNAMICS_URL = HttpUtil.DOMAIN + "?q=meet/activity/interact/praise/add";

    private List<MeetDynamics> mMeetList;
    private static Context mContext;
    private boolean isScrolling = false;
    RequestQueueSingleton requestQueueSingleton;
    RequestQueue queueMemberInfo;
    RequestQueue queueDynamics;

    public ArchivesListAdapter(Context context){
        this.mContext = context;
        requestQueueSingleton = new RequestQueueSingleton();
    }

    public void setData(List<MeetDynamics> meetList){
        mMeetList = meetList;
    }

    @Override
    public ArchivesListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.meet_dynamics_item, parent, false);
        ArchivesListAdapter.ViewHolder holder = new ArchivesListAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final MeetDynamics meetDynamics = mMeetList.get(position);
        holder.realname.setText(meetDynamics.getRealname());
        holder.lives.setText(meetDynamics.getLives());

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
                        //LinearLayout
                        linearLayout.setLayoutParams(layoutParams);

                        NetworkImageView picture = new NetworkImageView(mContext);

                        linearLayout.addView(picture);
                        holder.dynamicsGrid.addView(linearLayout);

                        picture.setImageDrawable(mContext.getDrawable(R.mipmap.ic_launcher));
                        picture.setTag(HttpUtil.DOMAIN+"/"+picture_array[i]);
                        HttpUtil.loadByImageLoader(queueDynamics, picture, HttpUtil.DOMAIN+"/"+picture_array[i], 200, 200);
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
    }

    @Override
    public int getItemCount() {
        return null != mMeetList ? mMeetList.size() : 0;
    }

    private void setDynamicsCommentView(LinearLayout linearLayout, List<DynamicsComment> dynamicsCommentList) {
        if (dynamicsCommentList.size() != 0) {
            for (int i = 0; i < dynamicsCommentList.size(); i++) {
                View viewComment = View.inflate(mContext, R.layout.dynamics_comment_item, null);
                linearLayout.addView(viewComment);

                NetworkImageView imageView = (NetworkImageView) viewComment.findViewById(R.id.comment_picture);
                //imageView.setImageDrawable(mContext.getDrawable(R.mipmap.ic_launcher));
                RequestQueue queueComment = requestQueueSingleton.instance(mContext);
                imageView.setTag(HttpUtil.DOMAIN + "/" + dynamicsCommentList.get(i).getPictureUrl());
                HttpUtil.loadByImageLoader(queueComment, imageView, HttpUtil.DOMAIN + "/" + dynamicsCommentList.get(i).getPictureUrl(), 50, 50);

                if (dynamicsCommentList.get(i) != null) {
                    if (dynamicsCommentList.get(i).getType() == 0) {//comment
                        TextView author = (TextView) viewComment.findViewById(R.id.author_name);
                        author.setText(dynamicsCommentList.get(i).getCommenterName());

                    } else {//reply
                        TextView commenter = (TextView) viewComment.findViewById(R.id.commenter_name);
                        commenter.setVisibility(View.VISIBLE);
                        commenter.setText(dynamicsCommentList.get(i).getCommenterName());

                        TextView replyFlag = (TextView) viewComment.findViewById(R.id.reply_flag);
                        replyFlag.setVisibility(View.VISIBLE);

                        TextView author = (TextView) viewComment.findViewById(R.id.author_name);
                        author.setText(dynamicsCommentList.get(i).getAuthorName());
                    }
                    //Slog.d(TAG, "====================comment content: " + dynamicsCommentList.get(i).getContent());
                    TextView content = (TextView) viewComment.findViewById(R.id.content);
                    content.setText(dynamicsCommentList.get(i).getContent());
                }
            }
        }
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
        GridLayout dynamicsGrid;
        LinearLayout commentList;

        public ViewHolder(View view){
            super(view);
            view.findViewById(R.id.meet_item_id).setVisibility(View.GONE);
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

            commentList = (LinearLayout) view.findViewById(R.id.dynamics_comments);


            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.meet_dynamics_item), font);
        }
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
                        //Log.d(TAG,"praiseDynamics status:"+status);
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

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
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
}
