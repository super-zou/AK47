package com.mufu.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mufu.R;
import com.mufu.experience.ExperienceDetailActivity;
import com.mufu.experience.GuideDetailActivity;
import com.mufu.main.MeetArchiveActivity;
import com.mufu.order.MyOrdersFragmentDF;
import com.mufu.order.OrdersListDF;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.RoundImageView;
import com.mufu.util.Slog;
import com.mufu.util.Utility;

import java.util.List;

import static com.mufu.common.MyApplication.getContext;
import static com.mufu.util.DateUtil.timeStampToMinute;

/**
 * Created by super-zou on 18-9-21.
 */

public class OrdersListAdapter extends RecyclerView.Adapter<OrdersListAdapter.ViewHolder> {

    private static final String TAG = "OrdersListAdapter";
    private static Context mContext;
    private int width;
    private List<OrdersListDF.OrderManager> mOrderList;
    private boolean isScrolling = false;
    private MyItemClickListener mItemClickListener;

    public OrdersListAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<OrdersListDF.OrderManager> orderList) {
        mOrderList = orderList;
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_queued_item, parent, false);
        ViewHolder holder = new ViewHolder(view, mItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull OrdersListAdapter.ViewHolder holder, final int position) {
        final OrdersListDF.OrderManager order = mOrderList.get(position);
        setContentView(holder, order);

        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                if (mItemClickListener != null){
                    mItemClickListener.onItemClick(view, position);
                }

                 */
                startActivity(order);
            }
        });
    }
    
    public void setContentView(OrdersListAdapter.ViewHolder holder, final OrdersListDF.OrderManager order){

        holder.titleTV.setText(order.title);
        if (!TextUtils.isEmpty(order.packageName)){
            holder.packageNameTV.setVisibility(View.VISIBLE);
            holder.packageNameTV.setText(order.packageName);
        }else {
            holder.packageNameTV.setVisibility(View.GONE);
        }
        
        if (order.orderClass == Utility.OrderClass.BLOCK_BOOKING.ordinal()){
            holder.blockBookingTagTV.setVisibility(View.VISIBLE);
        }else {
            holder.blockBookingTagTV.setVisibility(View.GONE);
        }
        holder.totalPriceTV.setText("支付金额："+String.format("%.2f", order.totalPrice)+"元");
        holder.totalAmountTV.setText("参加人数： "+order.amount+"人");
        
        holder.dateTV.setText("预订日期："+order.appointmentDate);
        holder.nameTV.setText(String.valueOf(order.nickname));

        holder.orderNumberTV.setText("订单编号："+order.number);
        holder.orderCreatedTV.setText("下单时间："+timeStampToMinute(order.created));
        holder.phoneTV.setText(order.phone);
        holder.universityTV.setText(order.university);
        holder.majorTV.setText(order.major);

        if (order.avatar != null && !"".equals(order.avatar)) {
            Glide.with(getContext()).load(HttpUtil.DOMAIN + order.avatar).into(holder.avatarImg);
        }
        
         holder.titleTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(order);
            }
        });

        holder.avatarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), MeetArchiveActivity.class);
                intent.putExtra("uid", order.uid);
                getContext().startActivity(intent);
            }
        });
        
        holder.nameTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.avatarImg.callOnClick();
            }
        });
        
        holder.customerWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.avatarImg.callOnClick();
            }
       });
    }
    
    private void startActivity(MyOrdersFragmentDF.Order order){
        Slog.d(TAG, "------------------>type: "+order.type+"      id: "+order.id);
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
        TextView titleTV;
        TextView packageNameTV;
        TextView dateTV;
        TextView nameTV;
        TextView orderNumberTV;
        TextView orderCreatedTV;
        RoundImageView avatarImg;
        TextView phoneTV;
        TextView universityTV;
        TextView totalPriceTV;
        TextView totalAmountTV;
        TextView blockBookingTagTV;
        ConstraintLayout blockBookingInfoCL;
        TextView majorTV;
        ConstraintLayout customerWrapper;
        ConstraintLayout itemLayout;
        
        public ViewHolder(View view, MyItemClickListener myItemClickListener) {
            super(view);
            itemLayout = view.findViewById(R.id.order_queued_item);

            titleTV = view.findViewById(R.id.title);
            packageNameTV = view.findViewById(R.id.package_title);
            dateTV = view.findViewById(R.id.date);
            nameTV = view.findViewById(R.id.name);
            orderNumberTV = view.findViewById(R.id.order_number);
            orderCreatedTV = view.findViewById(R.id.order_created);
            avatarImg = view.findViewById(R.id.avatar_image);
            phoneTV = view.findViewById(R.id.phone_number);
            universityTV = view.findViewById(R.id.university);
            majorTV = view.findViewById(R.id.major);
            totalPriceTV = view.findViewById(R.id.total_price);
            totalAmountTV = view.findViewById(R.id.total_amount);
            blockBookingTagTV = view.findViewById(R.id.block_booking_tag);
            blockBookingInfoCL = view.findViewById(R.id.block_booking_info);
            customerWrapper = view.findViewById(R.id.customer_wrapper);

            //将全局的监听赋值给接口
            this.mListener = myItemClickListener;
            Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.order_queued_item), font);
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
    public void setItemClickListener(OrdersListAdapter.MyItemClickListener myItemClickListener) {
        this.mItemClickListener = myItemClickListener;
    }
    
}
