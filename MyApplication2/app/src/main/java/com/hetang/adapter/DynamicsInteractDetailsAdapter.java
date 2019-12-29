package com.hetang.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hetang.dynamics.DynamicsInteractDetailsActivity;
import com.hetang.R;
import com.hetang.dynamics.DynamicsComment;
import com.hetang.util.InterActInterface;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.common.MyApplication;
import com.hetang.util.RoundImageView;
import com.hetang.util.Slog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DynamicsInteractDetailsAdapter extends RecyclerView.Adapter<DynamicsInteractDetailsAdapter.MyViewHolder>{
    private static final boolean isDebug = false;
    private static final String TAG = "DynamicsInteractDetailsAdapter";

    private static Context mContext;
    private boolean isScrolling = false;
    private boolean isUpdate = false;
    int mPosition = -1;
    private static final int GET_REPLY_DONE = 0;
    private static final int ADD_COMMENT_PRAISE = 1;
    private static final int ADD_REPLY_PRAISE = 1;
    private List<DynamicsComment> dynamicsCommentList = new ArrayList<>();
    private MyItemClickListener mItemClickListener;
    private InterActInterface commentDialogFragmentListener;
    
    private static final String COMMENT_PRAISE_ADD_URL = HttpUtil.DOMAIN + "?q=dynamic/comment_praise/add";
    private static final String MEET_COMMENT_PRAISE_ADD_URL = HttpUtil.DOMAIN + "?q=meet/comment_praise/add";
    private static final String REPLY_PRAISE_ADD_URL = HttpUtil.DOMAIN + "?q=dynamic/reply_praise/add";
    private static final String MEET_REPLY_PRAISE_ADD_URL = HttpUtil.DOMAIN + "?q=meet/reply_praise/add";

    private Handler mHandler;
    private int type;

    public DynamicsInteractDetailsAdapter(Context context, int type) {
        mContext = context;
        this.type = type;
        mHandler = new Handler() {
        @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case GET_REPLY_DONE:
                        //setReplyView();
                        notifyDataSetChanged();
                        break;
                    case ADD_COMMENT_PRAISE:
                        //notifyItemChanged(mPosition);
                        notifyDataSetChanged();
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

    public void setData(List<DynamicsComment> dynamicsCommentList, boolean isUpdate) {
        this.dynamicsCommentList = dynamicsCommentList;
        this.isUpdate = isUpdate;
    }
    
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_details_item, parent, false);

        MyViewHolder holder = new MyViewHolder(view, mItemClickListener);

        return holder;
    }
    
     @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position, List<Object> payloads) {
        /*
        if(payloads.isEmpty()){
            Slog.d(TAG, "----------------> no changed");
            onBindViewHolder(holder, position);
        }else {
            Slog.d(TAG, "----------------> have changed position: "+position);
            DynamicsComment dynamicsComment = dynamicsCommentList.get(position);

            addReplyView(holder, dynamicsComment);
        }
        */
    }
    
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        if(isDebug) Slog.d(TAG, "-------->onBindViewHolder position: "+position);
        mPosition = position;

        final DynamicsComment dynamicsComment = dynamicsCommentList.get(position);
        holder.name.setText(dynamicsComment.getName());

        String avatar = dynamicsComment.getAvatar();
        if (avatar != null && !"".equals(avatar) &&  !isScrolling) {
            Glide.with(mContext).load(HttpUtil.DOMAIN  + dynamicsComment.getAvatar()).into(holder.avatar);
        } else {
        if(dynamicsComment.getSex() == 0){
                holder.avatar.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.male_default_avator));
            }else {
                holder.avatar.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.female_default_avator));
            }
        }

        String profile = "";
        if (dynamicsComment.getSituation() != -1){
            if(dynamicsComment.getSituation() == 0){
                profile = dynamicsComment.getUniversity()+"·"+dynamicsComment.getDegreeName(dynamicsComment.getDegree());
            }else {
                profile = dynamicsComment.getPosition()+"·"+dynamicsComment.getIndustry();
            }

            holder.profile.setText(profile);
        }
        
        holder.contentView.setText(dynamicsComment.getContent());

        String praiseText = mContext.getResources().getString(R.string.fa_thumbs_O_up);
        if(dynamicsComment.getPraiseCount() > 0){
            praiseText += " "+dynamicsComment.getPraiseCount();
        }
        holder.praise.setText(praiseText);

        //replyWrapper = holder.replyListWrapper;
        if(isDebug) Slog.d(TAG, "----------->replyList size: "+dynamicsComment.replyList.size());

        if(dynamicsComment.replyList.size() > 0){
        if(holder.replyListWrapper.getTag() == null){
                if (isDebug)Slog.d(TAG, "==============getTag  null");
                setReplyView(holder,dynamicsComment, position);
            }else {
                if(!holder.replyListWrapper.getTag().equals(dynamicsComment)){
                    Slog.d(TAG, "==============getTag  not equals dynamicsComment");
                    holder.replyListWrapper.removeAllViews();
                    setReplyView(holder, dynamicsComment, position);
                }else {
                    if(isUpdate){
                        holder.replyListWrapper.removeAllViews();
                        setReplyView(holder, dynamicsComment, position);
                    }else {
                        Slog.d(TAG, "======>getChildCount: "+holder.replyListWrapper.getChildCount());
                        if(holder.replyListWrapper.getChildCount() == 0){
                            setReplyView(holder, dynamicsComment, position);
         
         }
                    }

                }
            }

        }else {
            if(holder.replyListWrapper.getChildCount() > 0){
                holder.replyListWrapper.removeAllViews();
            }
        }
        if(commentDialogFragmentListener != null){
            holder.reply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    commentDialogFragmentListener.onCommentClick(view, position);
                }
            });

            holder.contentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    commentDialogFragmentListener.onCommentClick(view, position);
                   // mItemClickListener.onItemClick(view, position);
                }
            });
        }
        
        holder.praise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //interActInterface.onPraiseClick(view, position);
                addCommentPraise(dynamicsComment);
            }
        });
        
        holder.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mItemClickListener.onItemClick(view, position);
            }
        });

        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mItemClickListener.onItemClick(view, position);
            }
        });
    }
    
    private void addCommentPraise(DynamicsComment dynamicsComment){
        dynamicsComment.setPraiseCount(dynamicsComment.getPraiseCount()+1);
        mHandler.sendEmptyMessage(ADD_COMMENT_PRAISE);

        RequestBody requestBody = new FormBody.Builder()
                .add("cid", String.valueOf(dynamicsComment.getCommentId())).build();
        String address = "";
        if (type == DynamicsInteractDetailsActivity.DYNAMIC_COMMENT){
            address = COMMENT_PRAISE_ADD_URL;
        }else {
            address = MEET_COMMENT_PRAISE_ADD_URL;
        }
        
        HttpUtil.sendOkHttpRequest(mContext, address, requestBody, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "-----------addCommentPraise response: "+responseText);
            }

            @Override
            public void onFailure(Call call, IOException e) { }
        });
    }
    
    private void addReplyPraise(DynamicsComment dynamicsComment){
        dynamicsComment.setReplyPraiseCount(dynamicsComment.getReplyPraiseCount()+1);
        mHandler.sendEmptyMessage(ADD_REPLY_PRAISE);


        RequestBody requestBody = new FormBody.Builder()
                .add("rid", String.valueOf(dynamicsComment.getRid())).build();

        String address = "";
        if (type == DynamicsInteractDetailsActivity.DYNAMIC_COMMENT){
            address = REPLY_PRAISE_ADD_URL;
        }else {
            address = MEET_REPLY_PRAISE_ADD_URL;
        }
        
        HttpUtil.sendOkHttpRequest(mContext, address, requestBody, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
    private void setReplyView(MyViewHolder viewHolder, final DynamicsComment dynamicsComment, final int position){
        if(isDebug) Slog.d(TAG, "---------------->setReplyView");
        if(dynamicsComment.replyList.size() > 0){
            for (int i=0; i<dynamicsComment.replyList.size(); i++){
                final DynamicsComment dynamicsCommentReply = dynamicsComment.replyList.get(i);
                View replyView = View.inflate(mContext, R.layout.reply_item, null);
                viewHolder.replyListWrapper.addView(replyView);
                RoundImageView avatarView = replyView.findViewById(R.id.replier_avatar);
                String avatar = dynamicsCommentReply.getAvatar();
                
                if (avatar != null && !"".equals(avatar) && !isScrolling) {
                    Glide.with(mContext).load(HttpUtil.DOMAIN  + avatar).into(avatarView);
                } else {
                    if(dynamicsCommentReply.getReplierSex() == 0){
                        avatarView.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.male_default_avator));
                    }else {
                        avatarView.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.female_default_avator));
                    }
                }

                TextView replierName = replyView.findViewById(R.id.replier_name);
                TextView authorName = replyView.findViewById(R.id.author_name);
                TextView replyContent = replyView.findViewById(R.id.reply_content);
                
                replierName.setText(dynamicsCommentReply.getReplierName());
                authorName.setText(dynamicsCommentReply.getAuthorName());
                replyContent.setText(dynamicsCommentReply.getReplyContent());

                TextView reply = replyView.findViewById(R.id.reply);
                final TextView praise = replyView.findViewById(R.id.praise);

                String praiseText = mContext.getResources().getString(R.string.fa_thumbs_O_up);
                if(dynamicsCommentReply.getReplyPraiseCount() > 0){
                    praiseText += " "+dynamicsCommentReply.getReplyPraiseCount();
                }
                praise.setText(praiseText);
                
                reply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(commentDialogFragmentListener != null){
                            commentDialogFragmentListener.onCommentClick(view, position);
                        }
                    }
                });

                replyContent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(commentDialogFragmentListener != null){
                            commentDialogFragmentListener.onCommentClick(view, position);
                        }
                    }
                });
                
                praise.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String praiseText = mContext.getResources().getString(R.string.fa_thumbs_O_up);
                        praiseText += " "+(dynamicsCommentReply.getReplyPraiseCount()+1);
                        praise.setText(praiseText);

                        addReplyPraise(dynamicsCommentReply);
                    }
                });
            }
            
            viewHolder.replyListWrapper.setTag(dynamicsComment);

            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
            FontManager.markAsIconContainer(viewHolder.replyListWrapper, font);
        }
    }
    
    @Override
    public int getItemCount() {
        return dynamicsCommentList != null ? dynamicsCommentList.size() : 0;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView profile;
        RoundImageView avatar;

        TextView contentView;
        TextView praise;
        TextView reply;
        LinearLayout replyListWrapper;
        ConstraintLayout constraintLayout;
        private MyItemClickListener mListener;
        
        public MyViewHolder(View view, MyItemClickListener myItemClickListener) {
            super(view);
            name = view.findViewById(R.id.name);
            avatar =  view.findViewById(R.id.comment_avatar);
            profile = view.findViewById(R.id.profile);
            contentView = view.findViewById(R.id.reply_content);
            praise = view.findViewById(R.id.praise);
            reply = view.findViewById(R.id.reply);
            replyListWrapper = view.findViewById(R.id.reply_list);
            constraintLayout = view.findViewById(R.id.comment_details_item);
            this.mListener = myItemClickListener;

            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.comment_details_item), font);
            FontManager.markAsIconContainer(view.findViewById(R.id.reply_item), font);
        }
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
    public void setItemClickListener(MyItemClickListener myItemClickListener, InterActInterface commentDialogFragmentListener) {
        this.mItemClickListener = myItemClickListener;
        this.commentDialogFragmentListener = commentDialogFragmentListener;
    }

}
