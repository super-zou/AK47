package com.mufu.adapter;

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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mufu.R;
import com.mufu.experience.ExperienceDetailActivity;
import com.mufu.experience.GuideDetailActivity;
import com.mufu.order.MyOrdersFragmentDF;
import com.mufu.order.OrdersListDF;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.Utility;

import java.util.List;

import static com.mufu.common.MyApplication.getContext;

/**
 * Created by super-zou on 18-9-21.
 */

public class MyOrderSummaryAdapter extends RecyclerView.Adapter<MyOrderSummaryAdapter.ViewHolder> {

    private static final String TAG = "MyOrderSummaryAdapter";
    private static Context mContext;
    private int width;
    private List<OrdersListDF.OrderManager> mOrderList;
    private boolean isScrolling = false;
    private MyItemClickListener mItemClickListener;
    private EvaluateClickListener mEvaluateClickListener;
    private PayClickListener mPayClickListener;
    private static final int UNPAID = 0;
    private static final int PAID = 1;
    private static final int EVALUATION = 3;
    private static final int APPLYING_REFUND = 4;
    private static final int REFUNDED = 5;

    public MyOrderSummaryAdapter(Context context) {
        mContext = context;
    }
    
    public void setData(List<OrdersListDF.OrderManager> orderList) {
        mOrderList = orderList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_list_item, parent, false);
        ViewHolder holder = new ViewHolder(view, mItemClickListener, mEvaluateClickListener, mPayClickListener);
        return holder;
    }
    
    @Override
    public void onBindViewHolder(@NonNull MyOrderSummaryAdapter.ViewHolder holder, final int position) {
        final MyOrdersFragmentDF.Order order = mOrderList.get(position);
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
        
        holder.payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPayClickListener != null){
                    mPayClickListener.onPayClick(view, position);
                }
            }
        });
    }
    
    public void setContentView(MyOrderSummaryAdapter.ViewHolder holder, final MyOrdersFragmentDF.Order order){

        if (order.headPictureUrl != null && !"".equals(order.headPictureUrl)) {
            Glide.with(getContext()).load(HttpUtil.DOMAIN + order.headPictureUrl).into(holder.headUri);
        }

        holder.titleTV.setText(order.title);
        if (!TextUtils.isEmpty(order.packageName)){
            holder.packageNameTV.setVisibility(View.VISIBLE);
            holder.packageNameTV.setText(order.packageName);
        }else {
            holder.packageNameTV.setVisibility(View.GONE);
        }
        holder.cityTV.setText(order.city);
        if (order.orderClass == Utility.OrderClass.NORMAL.ordinal()){
            holder.blockBookingTagTV.setVisibility(View.GONE);
            holder.blockBookingPriceInfoCL.setVisibility(View.GONE);
            holder.normalPriceInfoCL.setVisibility(View.VISIBLE);
            holder.moneyTV.setText(String.format("%.2f", order.price));
            if (TextUtils.isEmpty(order.unit)){
                holder.unitDividerTV.setVisibility(View.GONE);
            }else {
                holder.unitDividerTV.setVisibility(View.VISIBLE);
                holder.unitTV.setText(order.unit);
            }
            holder.amountTV.setText("x"+order.amount+"人");
        }else {
            holder.blockBookingTagTV.setVisibility(View.VISIBLE);
            holder.blockBookingPriceInfoCL.setVisibility(View.VISIBLE);
            holder.normalPriceInfoCL.setVisibility(View.GONE);
            holder.startingPriceTV.setText("起步价："+order.price+"元");
            holder.totalAmountTV.setText("参加人数："+order.amount+"人");
        }

        holder.totalPriceTV.setText(String.format("%.2f", order.totalPrice));
        
        holder.actualPaymentTV.setText(String.format("%.2f",order.actualPayment));
        holder.appointedDateTV.setText(order.appointmentDate);
        holder.refundTagTV.setVisibility(View.GONE);
        
        switch (order.status){
            case UNPAID:
                holder.evaluateBtn.setVisibility(View.GONE);
                holder.payBtn.setVisibility(View.VISIBLE);
                break;
            case PAID:
                holder.payBtn.setVisibility(View.GONE);
                holder.evaluateBtn.setVisibility(View.VISIBLE);
                holder.evaluateBtn.setText(getContext().getResources().getString(R.string.evaluation));
                break;
            case EVALUATION:
                holder.payBtn.setVisibility(View.GONE);
                holder.evaluateBtn.setVisibility(View.VISIBLE);
                holder.evaluateBtn.setText(getContext().getResources().getString(R.string.append_evaluation));
                break;
           case APPLYING_REFUND:
                holder.refundTagTV.setVisibility(View.VISIBLE);
                holder.refundTagTV.setText("已申请退款");
                holder.payBtn.setVisibility(View.GONE);
                holder.evaluateBtn.setVisibility(View.GONE);
                break;
            case REFUNDED:
                holder.refundTagTV.setVisibility(View.VISIBLE);
                holder.refundTagTV.setText("已退款");
                holder.payBtn.setVisibility(View.GONE);
                holder.evaluateBtn.setVisibility(View.GONE);
                break;
        }
               
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
    
   private void startActivity(MyOrdersFragmentDF.Order order){
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
        PayClickListener payClickListener;
        ImageView headUri;
        TextView titleTV;
        TextView packageNameTV;
        TextView cityTV;
        TextView totalPriceTV;
        TextView actualPaymentTV;
        TextView appointedDateTV;
        TextView moneyTV;
        TextView unitDividerTV;
        TextView unitTV;
        TextView amountTV;
        Button payBtn;
        Button evaluateBtn;
        CardView itemLayout;
        TextView blockBookingTagTV;
        TextView startingPriceTV;
        TextView totalAmountTV;
        TextView refundTagTV;
        ConstraintLayout normalPriceInfoCL;
        ConstraintLayout blockBookingPriceInfoCL;
        
        public ViewHolder(View view, MyItemClickListener myItemClickListener, EvaluateClickListener evaluateClickListener, PayClickListener payClickListener) {
            super(view);
            itemLayout = view.findViewById(R.id.order_list_item);
            headUri = view.findViewById(R.id.head_picture);
            titleTV = view.findViewById(R.id.guide_title);
            packageNameTV = view.findViewById(R.id.package_title);
            totalPriceTV = view.findViewById(R.id.total_price);
            cityTV = view.findViewById(R.id.city);
            actualPaymentTV = view.findViewById(R.id.actual_payment);
            appointedDateTV = view.findViewById(R.id.appointed_date);
            moneyTV = view.findViewById(R.id.money);
            unitDividerTV = view.findViewById(R.id.unit_divider);
            amountTV = view.findViewById(R.id.amount);
            unitTV = view.findViewById(R.id.unit);
            payBtn = view.findViewById(R.id.pay);
            evaluateBtn = view.findViewById(R.id.evaluate);
                        startingPriceTV = view.findViewById(R.id.starting_price);
            totalAmountTV = view.findViewById(R.id.total_amount);
            blockBookingTagTV = view.findViewById(R.id.block_booking_tag);
            refundTagTV = view.findViewById(R.id.refund_tag);
            normalPriceInfoCL = view.findViewById(R.id.normal_order_price_info);
            blockBookingPriceInfoCL = view.findViewById(R.id.block_booking_order_price_info);
            
            //将全局的监听赋值给接口
            this.mListener = myItemClickListener;
            this.evaluateClickListener = evaluateClickListener;
            this.payClickListener = payClickListener;
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
    
    public interface PayClickListener{
        void onPayClick(View view, int position);
    }

    /**
     * 在activity里面adapter就是调用的这个方法,将点击事件监听传递过来,并赋值给全局的监听
     *
     * @param myItemClickListener
     */
    public void setItemClickListener(MyOrderSummaryAdapter.MyItemClickListener myItemClickListener, EvaluateClickListener evaluateClickListener, PayClickListener payClickListener) {
        this.mItemClickListener = myItemClickListener;
        this.mEvaluateClickListener = evaluateClickListener;
        this.mPayClickListener = payClickListener;
    }
    
}
