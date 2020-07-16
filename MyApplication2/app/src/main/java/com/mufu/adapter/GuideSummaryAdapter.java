package com.mufu.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Message;
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
import com.mufu.common.HandlerTemp;
import com.mufu.experience.GuideSummaryActivity;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;

import java.util.List;

import static com.mufu.common.MyApplication.getContext;

/**
 * Created by super-zou on 18-9-21.
 */
 
 public class GuideSummaryAdapter extends RecyclerView.Adapter<GuideSummaryAdapter.ViewHolder> {

    private static final String TAG = "GuideSummaryAdapter";
    private static Context mContext;
    private int width;
    private List<GuideSummaryActivity.Guide> mGuideList;
    private boolean isScrolling = false;
    private MyItemClickListener mItemClickListener;

    public GuideSummaryAdapter(Context context) {
        mContext = context;
    }
    
    public void setData(List<GuideSummaryActivity.Guide> guidList) {
        mGuideList = guidList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.guide_list_item, parent, false);
        ViewHolder holder = new ViewHolder(view, mItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull GuideSummaryAdapter.ViewHolder holder, final int position) {
        final GuideSummaryActivity.Guide guide = mGuideList.get(position);
        setContentView(holder, guide);

        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mItemClickListener != null){
                    mItemClickListener.onItemClick(view, position);
                }
            }
        });
    }
    
    public void setContentView(GuideSummaryAdapter.ViewHolder holder, final GuideSummaryActivity.Guide guide){
        if (guide.headPictureUrl != null && !"".equals(guide.headPictureUrl)) {
            Glide.with(getContext()).load(HttpUtil.DOMAIN + guide.headPictureUrl).into(holder.headUri);
        }

        holder.titleTV.setText(guide.title);
        holder.cityTV.setText(guide.city);
        if (guide.evaluateCount > 0){
            holder.evaluateLL.setVisibility(View.VISIBLE);
            holder.scoreTV.setText(String.valueOf(guide.evaluateScore));
            holder.countTV.setText(guide.evaluateCount+"条评价");
        }else {
            holder.evaluateLL.setVisibility(View.GONE);
        }

        holder.moneyTV.setText(String.valueOf(guide.price));
        holder.unitTV.setText(guide.unit);

    }
    
    @Override
    public int getItemCount() {
        return mGuideList != null ? mGuideList.size() : 0;
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
    public void setItemClickListener(GuideSummaryAdapter.MyItemClickListener myItemClickListener) {
        this.mItemClickListener = myItemClickListener;
    }
    
    private void handleMessage(Message message){
        //todo
    }
    
    static class MyHandler extends HandlerTemp<GuideSummaryAdapter> {
        public MyHandler(GuideSummaryAdapter cls){
            super(cls);
        }

        @Override
        public void handleMessage(Message message) {
            GuideSummaryAdapter guideSummaryAdapter = ref.get();
            if (guideSummaryAdapter != null) {
                guideSummaryAdapter.handleMessage(message);
            }
        }
    }
}
