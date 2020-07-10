package com.hetang.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.experience.ExperienceDetailActivity;
import com.hetang.experience.GuideDetailActivity;
import com.hetang.order.MyFragment;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.Utility;

import java.util.List;

import static com.hetang.common.MyApplication.getContext;

/**
 * Created by super-zou on 18-9-21.
 */

public class OrderSummaryAdapter extends RecyclerView.Adapter<OrderSummaryAdapter.ViewHolder> {

    private static final String TAG = "OrderSummaryAdapter";
    private static Context mContext;
    private int width;
    private List<MyFragment.Order> mOrderList;
    private boolean isScrolling = false;
    private MyItemClickListener mItemClickListener;
    private EvaluateClickListener mEvaluateClickListener;

    public OrderSummaryAdapter(Context context) {
        mContext = context;
    }
    
    public void setData(List<MyFragment.Order> orderList) {
        mOrderList = orderList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_list_item, parent, false);
        ViewHolder holder = new ViewHolder(view, mItemClickListener, mEvaluateClickListener);
        return holder;
    }
    
    @Override
    public void onBindViewHolder(@NonNull OrderSummaryAdapter.ViewHolder holder, final int position) {
        final MyFragment.Order order = mOrderList.get(position);
        setContentView(holder, order);

        holder.evaluateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEvaluateClickListener.onEvaluateClick(view, position);
            }
        });
        
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mItemClickListener != null){
                    mItemClickListener.onItemClick(view, position);
                }
            }
        });
    }
    
    public void setContentView(OrderSummaryAdapter.ViewHolder holder, final MyFragment.Order order){

        if (order.headPictureUrl != null && !"".equals(order.headPictureUrl)) {
            Glide.with(getContext()).load(HttpUtil.DOMAIN + order.headPictureUrl).into(holder.headUri);
        }

        holder.titleTV.setText(order.title);
        holder.cityTV.setText(order.city);
        holder.moneyTV.setText(String.valueOf(order.price));
        holder.totalPriceTV.setText(String.valueOf(order.totalPrice));
        if (TextUtils.isEmpty(order.unit)){
            holder.unitDividerTV.setVisibility(View.GONE);
        }else {
            holder.unitDividerTV.setVisibility(View.VISIBLE);
            holder.unitTV.setText(order.unit);
        }

        holder.amountTV.setText("x"+order.amount);
        holder.actualPaymentTV.setText(String.valueOf(order.actualPayment));
        holder.appointedDateTV.setText(order.appointmentDate);
        
        switch (order.status){
            case 0:
                holder.unsubscribeBtn.setVisibility(View.GONE);
                holder.evaluateBtn.setVisibility(View.GONE);
                holder.payBtn.setVisibility(View.VISIBLE);
                break;
            case 1:
                holder.payBtn.setVisibility(View.GONE);
                holder.unsubscribeBtn.setVisibility(View.VISIBLE);
                holder.evaluateBtn.setVisibility(View.VISIBLE);
                break;
            case 3:
                holder.unsubscribeBtn.setVisibility(View.GONE);
                holder.payBtn.setVisibility(View.GONE);
                holder.evaluateBtn.setVisibility(View.VISIBLE);
                holder.evaluateBtn.setText(getContext().getResources().getString(R.string.append_evaluation));
                break;
        }
        
        holder.unsubscribeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        
        holder.payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        holder.headUri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(order);
            }
        });
        holder.titleTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.headUri.callOnClick();
            }
        });
          
    }
    
   private void startActivity(MyFragment.Order order){
        if (order.type == Utility.TalentType.GUIDE.ordinal()){
            Intent intent = new Intent(getContext(), GuideDetailActivity.class);
            intent.putExtra("sid", order.id);
            getContext().startActivity(intent);
        }else {
            Intent intent = new Intent(getContext(), ExperienceDetailActivity.class);
            intent.putExtra("eid", order.id);
            getContext().startActivity(intent);
        }
    }
    
    @Override
    public int getItemCount() {
        return mOrderList != null ? mOrderList.size() : 0;
    }

    public void setScrolling(boolean isScrolling) {
        this.isScrolling = isScrolling;
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        MyItemClickListener mListener;
        EvaluateClickListener evaluateClickListener;
        ImageView headUri;
        TextView titleTV;
        TextView cityTV;
        TextView totalPriceTV;
        TextView actualPaymentTV;
        TextView appointedDateTV;
        TextView moneyTV;
        TextView unitDividerTV;
        TextView unitTV;
        TextView amountTV;
        Button unsubscribeBtn;
        Button payBtn;
        Button evaluateBtn;
        CardView itemLayout;
        
        public ViewHolder(View view, MyItemClickListener myItemClickListener, EvaluateClickListener evaluateClickListener) {
            super(view);
            itemLayout = view.findViewById(R.id.order_list_item);
            headUri = view.findViewById(R.id.head_picture);
            titleTV = view.findViewById(R.id.guide_title);
            totalPriceTV = view.findViewById(R.id.total_price);
            cityTV = view.findViewById(R.id.city);
            actualPaymentTV = view.findViewById(R.id.actual_payment);
            appointedDateTV = view.findViewById(R.id.appointed_date);
            moneyTV = view.findViewById(R.id.money);
            unitDividerTV = view.findViewById(R.id.unit_divider);
            amountTV = view.findViewById(R.id.amount);
            unitTV = view.findViewById(R.id.unit);
            unsubscribeBtn = view.findViewById(R.id.unsubscribe);
            payBtn = view.findViewById(R.id.pay);
            evaluateBtn = view.findViewById(R.id.evaluate);
            
            //将全局的监听赋值给接口
            this.mListener = myItemClickListener;
            this.evaluateClickListener = evaluateClickListener;
            Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.order_list_item), font);
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
    
    public interface EvaluateClickListener{
        void onEvaluateClick(View view, int position);
    }

    /**
     * 在activity里面adapter就是调用的这个方法,将点击事件监听传递过来,并赋值给全局的监听
     *
     * @param myItemClickListener
     */
    public void setItemClickListener(OrderSummaryAdapter.MyItemClickListener myItemClickListener, EvaluateClickListener evaluateClickListener) {
        this.mItemClickListener = myItemClickListener;
        this.mEvaluateClickListener = evaluateClickListener;
    }
    
}
