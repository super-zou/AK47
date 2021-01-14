package com.mufu.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.bumptech.glide.Glide;
import com.mufu.R;
import com.mufu.common.HandlerTemp;
import com.mufu.group.SubGroupActivity;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.RoundImageView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import static com.mufu.common.MyApplication.getContext;

/**
 * Created by super-zou on 18-9-21.
 */

public class TalentSummaryListAdapter extends RecyclerView.Adapter<TalentSummaryListAdapter.ViewHolder> {

    private static final String TAG = "TalentSummaryListAdapter";
    private static Context mContext;
    private int width;
    RequestQueue queue;
    private List<SubGroupActivity.Talent> mTalentList;
    private boolean isScrolling = false;
    private MyItemClickListener mItemClickListener;

    public TalentSummaryListAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<SubGroupActivity.Talent> mTalentList, int parentWidth) {
        this.mTalentList = mTalentList;
        width = parentWidth;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.talent_summary_item, parent, false);
        ViewHolder holder = new ViewHolder(view, mItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull TalentSummaryListAdapter.ViewHolder holder, final int position) {
         final SubGroupActivity.Talent talent = mTalentList.get(position);
        setContentView(holder, talent);

        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mItemClickListener != null){
                    mItemClickListener.onItemClick(view, position);
                }
            }
        });
    }

    public static void setContentView(TalentSummaryListAdapter.ViewHolder holder, SubGroupActivity.Talent talent){
        holder.name.setText(talent.profile.getNickName());

        if (talent.profile.getAvatar() != null && !"".equals(talent.profile.getAvatar())) {
            Glide.with(mContext).load(HttpUtil.DOMAIN + talent.profile.getAvatar()).into(holder.leaderAvatar);
        } else {
            if (talent.profile.getSex() == 0){
                holder.leaderAvatar.setImageDrawable(mContext.getDrawable(R.drawable.male_default_avator));
            }else {
                holder.leaderAvatar.setImageDrawable(mContext.getDrawable(R.drawable.female_default_avator));
            }
        }

        holder.name.setText(talent.profile.getNickName().trim());
        if (talent.profile.getSituation() == 0){
            holder.major.setText(String.valueOf(talent.profile.getMajor()));
            holder.university.setText(talent.profile.getUniversity().trim());
            holder.degree.setText(talent.profile.getDegreeName(talent.profile.getDegree()));
        }else {
            holder.university.setText(talent.profile.getIndustry().trim());
            holder.degree.setText(talent.profile.getPosition());
            holder.major.setText("");
        }

        holder.title.setText(talent.title);
        holder.subject.setText("擅长："+talent.subject);
        holder.introduction.setText(talent.introduction);
        //holder.maleCount.setText(mContext.getResources().getString(R.string.male)+" "+singleGroup.maleCount);
        //holder.femaleCount.setText(mContext.getResources().getString(R.string.female)+" "+singleGroup.femaleCount);
        if (talent.evaluateCount > 0){
            float scoreFloat = talent.evaluateScores/talent.evaluateCount;
            float score = (float)(Math.round(scoreFloat*10))/10;
            holder.evaluateCount.setVisibility(View.VISIBLE);
            //holder.evaluateCount.setText("("+talent.evaluateCount+")");
            holder.evaluateCount.setText(getContext().getResources().getString(R.string.fa_star) +" "+ score +"("+ talent.evaluateCount+")");
        }else {
            holder.evaluateCount.setVisibility(View.GONE);
            holder.evaluateCount.setText("");
        }
        
        if (talent.answerCount > 0){
            if (talent.evaluateCount > 0){
                holder.answerCount.setText(mContext.getResources().getString(R.string.dot)+"解答"+talent.answerCount);
            }else {
                holder.answerCount.setText("解答"+talent.answerCount);
            }
        }else {
            holder.answerCount.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return mTalentList != null ? mTalentList.size() : 0;
    }

    public void setScrolling(boolean isScrolling) {
        this.isScrolling = isScrolling;
    }

   public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
       private MyItemClickListener mListener;
       RoundImageView leaderAvatar;
        TextView name;
        TextView title;
        TextView university;
        TextView introduction;
        TextView degree;
        TextView major;
        TextView subject;
        //TextView star;
        TextView evaluateCount;
        TextView answerCount;
        ConstraintLayout itemLayout;

        public ViewHolder(View view, MyItemClickListener myItemClickListener) {
            super(view);
            itemLayout = view.findViewById(R.id.talent_summary_item);
            leaderAvatar = view.findViewById(R.id.leader_avatar);
            name = view.findViewById(R.id.leader_name);
            university = view.findViewById(R.id.university);
            title = view.findViewById(R.id.talent_title);
            //created = view.findViewById(R.id.created);
            introduction = view.findViewById(R.id.introduction);
            degree = view.findViewById(R.id.degree);
            major = view.findViewById(R.id.major);
            subject = view.findViewById(R.id.subject);
            //star = view.findViewById(R.id.star);
            evaluateCount = view.findViewById(R.id.evaluate_count);
            answerCount = view.findViewById(R.id.answer_count);

            //将全局的监听赋值给接口
            this.mListener = myItemClickListener;
            Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.cny), font);
            FontManager.markAsIconContainer(view.findViewById(R.id.evaluate_count), font);
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
    public void setItemClickListener(TalentSummaryListAdapter.MyItemClickListener myItemClickListener) {
        this.mItemClickListener = myItemClickListener;
    }
    
        private void handleMessage(Message message){
        //todo
    }

    static class MyHandler extends HandlerTemp<TalentSummaryListAdapter> {
        public MyHandler(TalentSummaryListAdapter cls){
            super(cls);
        }

        @Override
        public void handleMessage(Message message) {
            TalentSummaryListAdapter meetSingleGroupSummaryAdapter = ref.get();
            if (meetSingleGroupSummaryAdapter != null) {
                meetSingleGroupSummaryAdapter.handleMessage(message);
            }
        }
    }
}
