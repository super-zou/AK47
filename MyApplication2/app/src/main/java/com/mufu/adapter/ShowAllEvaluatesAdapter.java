package com.mufu.adapter;

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
import androidx.gridlayout.widget.GridLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mufu.R;
import com.mufu.common.HandlerTemp;
import com.mufu.experience.ShowAllEvaluateDF;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.RoundImageView;
import com.mufu.util.Utility;
import com.willy.ratingbar.RotationRatingBar;

import org.json.JSONArray;

import java.util.List;

import static com.mufu.common.MyApplication.getContext;
import static com.mufu.util.DateUtil.time2Comparison;

/**
 * Created by super-zou on 18-9-21.
 */
 
 public class ShowAllEvaluatesAdapter extends RecyclerView.Adapter<ShowAllEvaluatesAdapter.ViewHolder> {

    private static final String TAG = "ShowAllEvaluatesAdapter";
    private static Context mContext;
    private int innerWidth;
    private List<ShowAllEvaluateDF.Evaluate> mEvaluateList;
    private boolean isScrolling = false;
    private PictureClickListener mPictureClickListener;

    public ShowAllEvaluatesAdapter(Context context) {
        mContext = context;
    }
    
    public void setData(List<ShowAllEvaluateDF.Evaluate> evaluateList) {
        mEvaluateList = evaluateList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.guide_evaluate_item, parent, false);
        ViewHolder holder = new ViewHolder(view);

        Resources resources = mContext.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        innerWidth = dm.widthPixels - (int) Utility.dpToPx(getContext(), 38f);

        return holder;
    }
    
    @Override
    public void onBindViewHolder(@NonNull ShowAllEvaluatesAdapter.ViewHolder holder, final int position) {
        final ShowAllEvaluateDF.Evaluate evaluate = mEvaluateList.get(position);
        setContentView(holder, evaluate, position);
    }

    public void setContentView(ShowAllEvaluatesAdapter.ViewHolder holder, final ShowAllEvaluateDF.Evaluate evaluate, final int position){
        if (evaluate.avatar != null && !"".equals(evaluate.avatar)) {
            Glide.with(getContext()).load(HttpUtil.DOMAIN + evaluate.avatar).into(holder.avatar);
        }
        
        holder.nameTV.setText(evaluate.name);
        //holder.timeTV.setText(DateUtil.timeStamp2String((long)evaluate.created));
        holder.timeTV.setText(time2Comparison((long)evaluate.created));
        holder.countTV.setText(evaluate.content);
        holder.ratingBar.setRating(evaluate.rating);

        if (evaluate.pictureArray != null && evaluate.pictureArray.length() > 0) {
            if (holder.gridLayout.getTag() == null) {
                setEvaluatePictures(holder.gridLayout, evaluate, position);
            } else {
                if (!evaluate.equals(holder.gridLayout.getTag())) {
                    holder.gridLayout.removeAllViews();
                    setEvaluatePictures(holder.gridLayout, evaluate, position);
                }
            }
            } else {
            if (holder.gridLayout.getChildCount() > 0) {
                holder.gridLayout.removeAllViews();
            }
        }
    }
    
    private void setEvaluatePictures(GridLayout gridLayout, ShowAllEvaluateDF.Evaluate evaluate, final int position){
        JSONArray pictureArray = evaluate.pictureArray;
        if (pictureArray != null && pictureArray.length() > 0){
            for (int i=0; i<pictureArray.length(); i++){
                RoundImageView imageView = new RoundImageView(getContext());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(innerWidth/3, innerWidth/3);
                layoutParams.setMargins(0, 0, 3, 4);
                imageView.setLayoutParams(layoutParams);
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                gridLayout.addView(imageView);
                Glide.with(getContext()).load(HttpUtil.DOMAIN+pictureArray.optString(i)).into(imageView);
                imageView.setId(i);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPictureClickListener.onPictureClick(view, position, pictureArray, imageView.getId());
                    }
                });
            }

            gridLayout.setTag(evaluate);
        }
    }
    
    @Override
    public int getItemCount() {
        return mEvaluateList != null ? mEvaluateList.size() : 0;
    }

    public void setScrolling(boolean isScrolling) {
        this.isScrolling = isScrolling;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        PictureClickListener mListener;
        RoundImageView avatar;
        TextView nameTV;
        TextView timeTV;
        RotationRatingBar ratingBar;
        TextView countTV;
        GridLayout gridLayout;
        
        public ViewHolder(View view) {
            super(view);
            avatar = view.findViewById(R.id.evaluate_avatar);
            nameTV = view.findViewById(R.id.evaluate_name);
            timeTV = view.findViewById(R.id.evaluate_time);
            ratingBar = view.findViewById(R.id.rating_bar);
            countTV = view.findViewById(R.id.evaluate_content);
            gridLayout = view.findViewById(R.id.evaluate_picture_grid);

            //将全局的监听赋值给接口
            Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.guide_list_item), font);
        }

    }
    
    /**
     * 创建一个回调接口
     */
    public interface PictureClickListener {
        void onPictureClick(View view, int position, JSONArray pictureUrlArray, int index);
    }
    
    /**
     * 在activity里面adapter就是调用的这个方法,将点击事件监听传递过来,并赋值给全局的监听
     *
     * @param pictureClickListener
     */
    public void setItemClickListener(ShowAllEvaluatesAdapter.PictureClickListener pictureClickListener) {
        this.mPictureClickListener = pictureClickListener;
    }

    private void handleMessage(Message message){
        //todo
    }
    
    static class MyHandler extends HandlerTemp<ShowAllEvaluatesAdapter> {
        public MyHandler(ShowAllEvaluatesAdapter cls){
            super(cls);
        }

        @Override
        public void handleMessage(Message message) {
            ShowAllEvaluatesAdapter guideSummaryAdapter = ref.get();
            if (guideSummaryAdapter != null) {
                guideSummaryAdapter.handleMessage(message);
            }
        }
    }
}
