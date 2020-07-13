package com.hetang.order;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.common.MyApplication;
import com.hetang.util.BaseDialogFragment;
import com.hetang.experience.ExperienceDetailActivity;
import com.hetang.experience.ExperienceEvaluateDialogFragment;
import com.hetang.experience.GuideDetailActivity;
import com.hetang.util.DateUtil;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.Utility;

import java.lang.ref.WeakReference;

import static android.app.Activity.RESULT_OK;
import static com.hetang.experience.GuideApplyDialogFragment.WRITE_ROUTE_INFO_SUCCESS;

public class OrderDetailsDF extends BaseDialogFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "OrderDetailsDF";
    private Dialog mDialog;
    private Window window;
    private MyFragment.Order mOrder;
    
    public static OrderDetailsDF newInstance(MyFragment.Order order) {
        OrderDetailsDF orderDetailsDF = new OrderDetailsDF();
        Bundle bundle = new Bundle();
        bundle.putSerializable("order", order);
        orderDetailsDF.setArguments(bundle);

        return orderDetailsDF;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_MaterialComponents_DialogWhenLarge);
        mDialog.setContentView(R.layout.order_details);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mOrder = (MyFragment.Order) bundle.getSerializable("order");
        }
        
         mDialog.setCanceledOnTouchOutside(true);
        window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        TextView leftBack = mDialog.findViewById(R.id.left_back);
        leftBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        
        initView();

        return mDialog;
    }

    private void initView() {
        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.order_details), font);
        
        ImageView headUri = mDialog.findViewById(R.id.head_picture);
        TextView titleTV = mDialog.findViewById(R.id.guide_title);
        TextView cityTV = mDialog.findViewById(R.id.city);
        TextView totalPriceTV = mDialog.findViewById(R.id.total_price);
        TextView actualPaymentTV = mDialog.findViewById(R.id.actual_payment);
        TextView appointedDateTV = mDialog.findViewById(R.id.appointed_date);
        TextView unitDividerTV = mDialog.findViewById(R.id.unit_divider);
        TextView amountTV = mDialog.findViewById(R.id.amount);
        TextView moneyTV = mDialog.findViewById(R.id.money);
        TextView unitTV = mDialog.findViewById(R.id.unit);
        TextView createdTV = mDialog.findViewById(R.id.order_created);
        TextView paymentTime = mDialog.findViewById(R.id.payment_time);
        TextView numberTV = mDialog.findViewById(R.id.order_number);
        Button unsubscribeBtn = mDialog.findViewById(R.id.unsubscribe);
        Button payBtn = mDialog.findViewById(R.id.pay);
        Button evaluateBtn = mDialog.findViewById(R.id.evaluate);

        if (mOrder.headPictureUrl != null && !"".equals(mOrder.headPictureUrl)) {
            Glide.with(getContext()).load(HttpUtil.DOMAIN + mOrder.headPictureUrl).into(headUri);
    }

        titleTV.setText(mOrder.title);
        cityTV.setText(mOrder.city);
        moneyTV.setText(String.valueOf(mOrder.price));
        if (!TextUtils.isEmpty(mOrder.unit)){
            unitTV.setText(mOrder.unit);
        }else {
            unitDividerTV.setVisibility(View.GONE);
        }

        amountTV.setText("x"+mOrder.amount);
        totalPriceTV.setText(String.valueOf(mOrder.totalPrice));
        actualPaymentTV.setText(String.valueOf(mOrder.actualPayment));
        appointedDateTV.setText(mOrder.appointmentDate);
        createdTV.setText("订单创建时间："+DateUtil.timeStamp2String((long)mOrder.created));
        if (mOrder.paymentTime != 0){
            paymentTime.setText("订单支付时间："+DateUtil.timeStamp2String((long)mOrder.paymentTime));
        }

        numberTV.setText("订单编号："+mOrder.number);
        
                switch (mOrder.status){
            case 0:
                unsubscribeBtn.setVisibility(View.GONE);
                evaluateBtn.setVisibility(View.GONE);
                break;
            case 1:
                payBtn.setVisibility(View.GONE);
                break;
            case 3:
                payBtn.setVisibility(View.GONE);
                evaluateBtn.setText(getContext().getResources().getString(R.string.append_evaluation));
                break;
        }

        unsubscribeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        
        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        evaluateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExperienceEvaluateDialogFragment experienceEvaluateDialogFragment;
                experienceEvaluateDialogFragment = ExperienceEvaluateDialogFragment.newInstance(mOrder);
                //orderDetailsDF.setTargetFragment(this, ROUTE_REQUEST_CODE);
                experienceEvaluateDialogFragment.show(getFragmentManager(), "RouteItemEditDF");
            }
        });
        
                headUri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity();
            }
        });
        titleTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                headUri.callOnClick();
            }
        });
    }
    
    private void startActivity(){
        if (mOrder.type == Utility.TalentType.GUIDE.ordinal()){
            Intent intent = new Intent(getContext(), GuideDetailActivity.class);
            intent.putExtra("sid", mOrder.id);
            startActivity(intent);
        }else {
            Intent intent = new Intent(getContext(), ExperienceDetailActivity.class);
            intent.putExtra("eid", mOrder.id);
            startActivity(intent);
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {

            }
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case WRITE_ROUTE_INFO_SUCCESS:

                break;

        }
    }

@Override
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
    }


    @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
    }
    
    static class MyHandler extends Handler {
        WeakReference<OrderDetailsDF> orderDetailsDFWeakReference;

        MyHandler(OrderDetailsDF orderDetailsDF) {
            orderDetailsDFWeakReference = new WeakReference<OrderDetailsDF>(orderDetailsDF);
        }

        @Override
        public void handleMessage(Message message) {
            OrderDetailsDF orderDetailsDF = orderDetailsDFWeakReference.get();
            if (orderDetailsDF != null) {
                orderDetailsDF.handleMessage(message);
            }
        }
    }
}
