package com.mufu.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mufu.R;
import com.mufu.common.BaseAppCompatActivity;
import com.mufu.common.HandlerTemp;
import com.mufu.experience.ExperienceSummaryActivity;
import com.mufu.experience.modify.ModifyExperienceDF;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.Utility;

import java.util.List;

import static com.mufu.common.MyApplication.getContext;
import static com.mufu.util.ParseUtils.startMeetArchiveActivity;

/**
 * Created by super-zou on 18-9-21.
 */

public class ExperienceSummaryAdapter extends RecyclerView.Adapter<ExperienceSummaryAdapter.ViewHolder> {

    private static final String TAG = "ExperienceSummaryAdapter";
    private static Context mContext;
    private int width;
    private List<ExperienceSummaryActivity.Experience> mExperienceList;
    private boolean isScrolling = false;
    private MyItemClickListener mItemClickListener;
    private boolean isSelf;
    
    public ExperienceSummaryAdapter(Context context, boolean isSelf) {
        mContext = context;
        this.isSelf = isSelf;
    }

    public void setData(List<ExperienceSummaryActivity.Experience> experienceList) {
        mExperienceList = experienceList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.guide_list_item, parent, false);
        ViewHolder holder = new ViewHolder(view, mItemClickListener);
        return holder;
    }
    
    @Override
    public void onBindViewHolder(@NonNull ExperienceSummaryAdapter.ViewHolder holder, final int position) {
        final ExperienceSummaryActivity.Experience experience = mExperienceList.get(position);
        setContentView(holder, experience);

        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mItemClickListener != null){
                    mItemClickListener.onItemClick(view, position);
                }
            }
        });
        
         if (isSelf){
            holder.modifyTV.setVisibility(View.VISIBLE);
            holder.modifyTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ModifyExperienceDF modifyExperienceDialogFragment = new ModifyExperienceDF();
                    Bundle bundle = new Bundle();
                    bundle.putInt("eid", experience.eid);
                    bundle.putInt("type", Utility.TalentType.EXPERIENCE.ordinal());
                    modifyExperienceDialogFragment.setArguments(bundle);
                    modifyExperienceDialogFragment.show(((BaseAppCompatActivity)mContext).getSupportFragmentManager(), "ModifyExperienceDF");
                }
            });
        }else {
            holder.modifyTV.setVisibility(View.GONE);
        }
    }
    
    public void setContentView(ExperienceSummaryAdapter.ViewHolder holder, final ExperienceSummaryActivity.Experience experience){
        if (experience.headPictureUrl != null && !"".equals(experience.headPictureUrl)) {
            Glide.with(getContext()).load(HttpUtil.DOMAIN + experience.headPictureUrl).into(holder.headUri);
        }

        holder.titleTV.setText(experience.title);
        holder.cityTV.setText(experience.city);
        if (experience.evaluateCount > 0){
            holder.evaluateLL.setVisibility(View.VISIBLE);
            float average = experience.evaluateScore / experience.evaluateCount;
            float averageScore = (float) (Math.round(average * 10)) / 10;
            holder.scoreTV.setText(String.valueOf(averageScore));
            holder.countTV.setText(experience.evaluateCount+"条评价");
        }else {
            holder.evaluateLL.setVisibility(View.GONE);
        }
        
        holder.moneyTV.setText(String.valueOf(experience.price));
        if (!TextUtils.isEmpty(experience.unit)){
            holder.unitTV.setText(experience.unit);
        }else {
            holder.unitTV.setText("人起");
        }
        holder.durationTV.setVisibility(View.VISIBLE);
        holder.durationTV.setText(String.valueOf(experience.duration)+"小时");

    }

    @Override
    public int getItemCount() {
        return mExperienceList != null ? mExperienceList.size() : 0;
    }
    
    public void setScrolling(boolean isScrolling) {
        this.isScrolling = isScrolling;
    }

   public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        MyItemClickListener mListener;
        ImageView headUri;
        TextView titleTV;
        TextView cityTV;
        TextView scoreTV;
        TextView countTV;
        TextView moneyTV;
        TextView unitTV;
        TextView durationTV;
        TextView modifyTV;
        CardView itemLayout;
        LinearLayout evaluateLL;
        
        public ViewHolder(View view, MyItemClickListener myItemClickListener) {
            super(view);
            itemLayout = view.findViewById(R.id.guide_list_item);
            headUri = view.findViewById(R.id.head_picture);
            titleTV = view.findViewById(R.id.guide_title);
            cityTV = view.findViewById(R.id.city);
            scoreTV = view.findViewById(R.id.score);
            countTV = view.findViewById(R.id.evaluate_count);
            moneyTV = view.findViewById(R.id.money);
            unitTV = view.findViewById(R.id.unit);
            modifyTV = view.findViewById(R.id.modify);
            durationTV = view.findViewById(R.id.duration);
            evaluateLL = view.findViewById(R.id.evaluate_wrapper);

            //将全局的监听赋值给接口
            this.mListener = myItemClickListener;
            Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.guide_list_item), font);
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
    public void setItemClickListener(ExperienceSummaryAdapter.MyItemClickListener myItemClickListener) {
        this.mItemClickListener = myItemClickListener;
    }
    
    private void handleMessage(Message message){
        //todo
    }
    
    static class MyHandler extends HandlerTemp<ExperienceSummaryAdapter> {
        public MyHandler(ExperienceSummaryAdapter cls){
            super(cls);
        }

        @Override
        public void handleMessage(Message message) {
            ExperienceSummaryAdapter experienceSummaryAdapter = ref.get();
            if (experienceSummaryAdapter != null) {
                experienceSummaryAdapter.handleMessage(message);
            }
        }
    }
}
