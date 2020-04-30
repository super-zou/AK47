package com.hetang.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.gridlayout.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.common.HandlerTemp;
import com.hetang.common.MyApplication;
import com.hetang.consult.ConsultDetailActivity;
import com.hetang.consult.ConsultSummaryActivity;
import com.hetang.experience.GuideSummaryActivity;
import com.hetang.experience.ShowAllEvaluateDF;
import com.hetang.util.DateUtil;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.RoundImageView;
import com.hetang.util.Utility;


import org.json.JSONArray;

import java.util.List;

import static com.hetang.common.MyApplication.getContext;
import static com.hetang.util.DateUtil.time2Comparison;

/**
 * Created by super-zou on 18-9-21.
 */
 
 public class ConsultSummaryAdapter extends RecyclerView.Adapter<ConsultSummaryAdapter.ViewHolder> {

    private static final String TAG = "GuideSummaryAdapter";
    private static Context mContext;
    private int innerWidth;
    private List<ConsultSummaryActivity.Consult> mConsultList;
    private boolean isScrolling = false;
    private PictureClickListener mPictureClickListener;

    public ConsultSummaryAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<ConsultSummaryActivity.Consult> consultList) {
        mConsultList = consultList;
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.consult_list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);

        Resources resources = mContext.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        innerWidth = dm.widthPixels - (int) Utility.dpToPx(getContext(), 38f);

        return holder;
    }
    
    @Override
    public void onBindViewHolder(@NonNull ConsultSummaryAdapter.ViewHolder holder, final int position) {
        final ConsultSummaryActivity.Consult consult = mConsultList.get(position);
        setContentView(holder, consult, position);

        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startConsultDetailActivity(consult.cid);
            }
        });

        holder.answerTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startConsultDetailActivity(consult.cid);
            }
        });
    }
    
    public void setContentView(ConsultSummaryAdapter.ViewHolder holder, final ConsultSummaryActivity.Consult consult, int position){
        if (consult.avatar != null && !"".equals(consult.avatar)) {
            Glide.with(getContext()).load(HttpUtil.DOMAIN + consult.avatar).into(holder.avatarIV);
        }

        holder.nameTV.setText(consult.name);
        switch (consult.reward){
            case 0:
                holder.classTV.setText(getContext().getResources().getString(R.string.fa_heart));
                holder.classTV.setTextColor(getContext().getResources().getColor(R.color.color_red));
                break;
                case 2:
                holder.classTV.setText(getContext().getResources().getString(R.string.fa_coffee));
                holder.classTV.setTextColor(getContext().getResources().getColor(R.color.color_coffee));
                break;
            case 10:
                holder.classTV.setText(getContext().getResources().getString(R.string.fa_motorcycle));
                holder.classTV.setTextColor(getContext().getResources().getColor(R.color.color_green_dark));
                break;
            case 50:
                holder.classTV.setText(getContext().getResources().getString(R.string.fa_rocket));
                holder.classTV.setTextColor(getContext().getResources().getColor(R.color.color_purple));
                break;
                default:
                    break;
        }
        
        holder.contentTV.setText(consult.question);
        holder.timeTV.setText(time2Comparison((long)consult.created));
        holder.answerTV.setText("解答"+consult.answerCount);

        if (consult.pictureList != null && consult.pictureList.size() > 0) {
            if (holder.pictureGL.getTag() == null) {
                setConsultPictures(holder.pictureGL, consult, position);
            } else {
                if (!consult.equals(holder.pictureGL.getTag())) {
                    holder.pictureGL.removeAllViews();
                    setConsultPictures(holder.pictureGL, consult, position);
                }
            }
            } else {
            if (holder.pictureGL.getChildCount() > 0) {
                holder.pictureGL.removeAllViews();
            }
        }
    }
    
    private void setConsultPictures(androidx.gridlayout.widget.GridLayout gridLayout, ConsultSummaryActivity.Consult consult, final int position){
        if (consult.pictureList.size() > 0){
            for (int i=0; i<consult.pictureList.size(); i++){
                RoundImageView imageView = new RoundImageView(getContext());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(innerWidth/6, innerWidth/6);
                layoutParams.setMargins(0, 0, 3, 4);
                imageView.setLayoutParams(layoutParams);
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                gridLayout.addView(imageView);
                Glide.with(getContext()).load(HttpUtil.DOMAIN+consult.pictureList.get(i)).into(imageView);
                imageView.setId(i);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
             public void onClick(View view) {
                        mPictureClickListener.onPictureClick(view, position, consult.pictureList, imageView.getId());
                    }
                });
            }

            gridLayout.setTag(consult);
        }
    }

    private void startConsultDetailActivity(int cid){
        Intent intent = new Intent(MyApplication.getContext(), ConsultDetailActivity.class);
        intent.putExtra("cid", cid);
        getContext().startActivity(intent);
    }
    
    /**
     * 创建一个回调接口
     */
    public interface PictureClickListener {
        void onPictureClick(View view, int position, List<String> pictureUrlList, int index);
    }

    /**
     * 在activity里面adapter就是调用的这个方法,将点击事件监听传递过来,并赋值给全局的监听
     *
     * @param pictureClickListener
     */
    public void setItemClickListener(PictureClickListener pictureClickListener) {
        this.mPictureClickListener = pictureClickListener;
    }
    
    @Override
    public int getItemCount() {
        return mConsultList != null ? mConsultList.size() : 0;
    }

    public void setScrolling(boolean isScrolling) {
        this.isScrolling = isScrolling;
    }

   public static class ViewHolder extends RecyclerView.ViewHolder {
        RoundImageView avatarIV;
        TextView nameTV;
        TextView classTV;
        TextView contentTV;
        GridLayout pictureGL;
        TextView timeTV;
        TextView answerTV;
        ConstraintLayout itemLayout;
        
        public ViewHolder(View view) {
            super(view);
            itemLayout = view.findViewById(R.id.consult_list_item);
            avatarIV = view.findViewById(R.id.avatar);
            nameTV = view.findViewById(R.id.name);
            classTV = view.findViewById(R.id.reward_class);
            contentTV = view.findViewById(R.id.question);
            pictureGL = view.findViewById(R.id.picture_grid);
            timeTV = view.findViewById(R.id.time);
            answerTV = view.findViewById(R.id.answer_count);
            itemLayout = view.findViewById(R.id.consult_list_item);

            //将全局的监听赋值给接口
            Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.consult_list_item), font);
        }

    }
    
    private void handleMessage(Message message){
        //todo
    }

    static class MyHandler extends HandlerTemp<ConsultSummaryAdapter> {
        public MyHandler(ConsultSummaryAdapter cls){
            super(cls);
        }

        @Override
        public void handleMessage(Message message) {
            ConsultSummaryAdapter guideSummaryAdapter = ref.get();
            if (guideSummaryAdapter != null) {
                guideSummaryAdapter.handleMessage(message);
            }
        }
    }
}
