package com.hetang.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.common.HandlerTemp;
import com.hetang.experience.ExperienceSummaryActivity;
import com.hetang.experience.ShowAllEvaluateDF;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.RoundImageView;
import com.hetang.util.Utility;
import com.willy.ratingbar.RotationRatingBar;

import org.json.JSONArray;

import java.util.List;

import static com.hetang.common.MyApplication.getContext;
import static com.hetang.util.DateUtil.time2Comparison;

/**
 * Created by super-zou on 18-9-21.
 */
 
 public class SelectShareExperienceAdapter extends RecyclerView.Adapter<SelectShareExperienceAdapter.ViewHolder> {

    private static final String TAG = "SelectShareExperienceAdapter";
    private static Context mContext;
    private List<ExperienceSummaryActivity.Experience> mExperienceList;
    private boolean isScrolling = false;
    private ItemClickListener mItemClickListener;

    public SelectShareExperienceAdapter(Context context) {
        mContext = context;
    }
    
    public void setData(List<ExperienceSummaryActivity.Experience> experienceList) {
        mExperienceList = experienceList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.select_share_experience_item, parent, false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull SelectShareExperienceAdapter.ViewHolder holder, final int position) {
        final ExperienceSummaryActivity.Experience experience = mExperienceList.get(position);
        setContentView(holder, experience, position);
    }

    public void setContentView(SelectShareExperienceAdapter.ViewHolder holder, ExperienceSummaryActivity.Experience experience, final int position){
        if (experience.headPictureUrl != null && !"".equals(experience.headPictureUrl)) {
            Glide.with(getContext()).load(HttpUtil.DOMAIN + experience.headPictureUrl).into(holder.picture);
        }

        holder.title.setText(experience.title);

        holder.selectExperience.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mItemClickListener.onItemClick(view, position);
            }
        });

    }
    
    @Override
    public int getItemCount() {
        return mExperienceList != null ? mExperienceList.size() : 0;
    }

    public void setScrolling(boolean isScrolling) {
        this.isScrolling = isScrolling;
    }

   public static class ViewHolder extends RecyclerView.ViewHolder{
        ItemClickListener mListener;
        RoundImageView picture;
        TextView title;
        ConstraintLayout selectExperience;
        public ViewHolder(View view) {
            super(view);
            picture = view.findViewById(R.id.experience_picture);
            title = view.findViewById(R.id.experience_title);
            selectExperience = view.findViewById(R.id.select_share_experience_item);
            //将全局的监听赋值给接口
            //Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
            //FontManager.markAsIconContainer(view.findViewById(R.id.guide_list_item), font);
        }

    }

    /**
     * 创建一个回调接口
     */
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
    
    /**
     * 在activity里面adapter就是调用的这个方法,将点击事件监听传递过来,并赋值给全局的监听
     *
     * @param itemClickListener
     */
    public void setItemClickListener(SelectShareExperienceAdapter.ItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }
}
