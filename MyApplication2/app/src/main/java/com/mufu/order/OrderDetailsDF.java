package com.mufu.order;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.mufu.R;
import com.mufu.common.MyApplication;
import com.mufu.util.BaseDialogFragment;
import com.mufu.experience.ExperienceDetailActivity;
import com.mufu.experience.ExperienceEvaluateDialogFragment;
import com.mufu.experience.GuideDetailActivity;
import com.mufu.util.DateUtil;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.Slog;
import com.mufu.util.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;

import java.lang.ref.WeakReference;
import java.util.PrimitiveIterator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;
import static com.mufu.order.OrderPaymentDF.GET_ORDER_INFO_DONE;
import static com.mufu.order.ModifyOrderDF.ORDER_MODIFIED_BROADCAST;

public class OrderDetailsDF extends BaseDialogFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "OrderDetailsDF";
    private Dialog mDialog;
    private Window window;
    private int mOid;
    private MyHandler myHandler;
    private OrdersListDF.OrderManager mOrder;
        private Button mRefundBtn;
    public static final String GET_ORDER_BY_OID = HttpUtil.DOMAIN + "?q=order_manager/get_order_by_oid";
    public static final String GET_UNSUBSCRIBE_CONDITION = HttpUtil.DOMAIN + "?q=order_manager/check_unsubscribe_condition";
    private static final int GET_UNSUBSCRIBE_CONDITION_DONE = 1;
        public static final String APPLY_REFUND = HttpUtil.DOMAIN + "?q=order_manager/apply_refund";
    private final int REFUND_FULL = 0;
        private static final int SUBMIT_REFUND_DONE = 2;
    private final int REFUND_PARTIAL = 1;
    private int mCondition = 0;
        private int mType;
            private boolean isOwned = true;
        private OrderDetailsBroadcastReceiver mReceiver;
    
    public static OrderDetailsDF newInstance(OrdersListDF.OrderManager order, int orderType, boolean owned) {
        OrderDetailsDF orderDetailsDF = new OrderDetailsDF();
        Bundle bundle = new Bundle();
        bundle.putSerializable("order", order);
        orderDetailsDF.setArguments(bundle);
                bundle.putInt("type", orderType);
                bundle.putBoolean("owned", owned);

        return orderDetailsDF;
    }
    
    public static OrderDetailsDF newInstance(int oid) {
        OrderDetailsDF orderDetailsDF = new OrderDetailsDF();
        Bundle bundle = new Bundle();
        bundle.putInt("oid", oid);
        orderDetailsDF.setArguments(bundle);

        return orderDetailsDF;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_MaterialComponents_DialogWhenLarge);
        mDialog.setContentView(R.layout.order_details);
        
        myHandler = new MyHandler(this);
               
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
        
        Bundle bundle = getArguments();
        if (bundle != null) {
            mOrder = (OrdersListDF.OrderManager) bundle.getSerializable("order");
            mType = bundle.getInt("type");
            isOwned = bundle.getBoolean("owned", true);

            if (mOrder == null){
                mOid = bundle.getInt("oid", 0);
                getOrderByOid(mOid);
            }else {
                initView();
            }
        }
        
                TextView modifyTV = mDialog.findViewById(R.id.modify);
        modifyTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int oid = 0;
                if (mOrder != null){
                    oid = mOrder.oid;
                }else {
                    if (mOid > 0){
                        oid = mOid;
                    }
                }
                ModifyOrderDF modifyOrderDF = ModifyOrderDF.newInstance(oid, mOrder.price, mOrder.amount);
                modifyOrderDF.show(getFragmentManager(), "ModifyOrderDF");
            }
        });

        registerBroadcast();

        return mDialog;
    }

    private void initView() {
        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.order_details), font);
        
                ConstraintLayout orderProcessWrapper = mDialog.findViewById(R.id.order_process_wrapper);
        if (!isOwned){
            orderProcessWrapper.setVisibility(View.GONE);
        }
        ImageView headUri = mDialog.findViewById(R.id.head_picture);
        TextView titleTV = mDialog.findViewById(R.id.guide_title);
        TextView packageNameTV = mDialog.findViewById(R.id.package_title);
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
        mRefundBtn = mDialog.findViewById(R.id.refund);
        Button payBtn = mDialog.findViewById(R.id.pay);
        Button evaluateBtn = mDialog.findViewById(R.id.evaluate);
                TextView blockBookingTagTV = mDialog.findViewById(R.id.block_booking_tag);

        ConstraintLayout normalPriceInfoCL = mDialog.findViewById(R.id.normal_order_price_info);
        ConstraintLayout blockBookingPriceInfoCL = mDialog.findViewById(R.id.block_booking_order_price_info);
        
        if (mOrder.orderClass == Utility.OrderClass.NORMAL.ordinal()){
            normalPriceInfoCL.setVisibility(View.VISIBLE);
            blockBookingPriceInfoCL.setVisibility(View.GONE);
            moneyTV.setText(String.format("%.2f", mOrder.price));
            if (!TextUtils.isEmpty(mOrder.unit)){
                unitTV.setText(mOrder.unit);
            }else {
                unitDividerTV.setVisibility(View.GONE);
            }
            amountTV.setText("x"+mOrder.amount);
        }else {
            blockBookingTagTV.setVisibility(View.VISIBLE);
            normalPriceInfoCL.setVisibility(View.GONE);
            blockBookingPriceInfoCL.setVisibility(View.VISIBLE);
            TextView startingPriceTV = mDialog.findViewById(R.id.starting_price);
            TextView totalAmountTV = mDialog.findViewById(R.id.total_amount);
            startingPriceTV.setText("起步价："+mOrder.price+"元");
            totalAmountTV.setText("参加人数："+mOrder.amount+"人");
        }

        if (mOrder.headPictureUrl != null && !"".equals(mOrder.headPictureUrl)) {
            Glide.with(getContext()).load(HttpUtil.DOMAIN + mOrder.headPictureUrl).into(headUri);
    }

        titleTV.setText(mOrder.title);
        cityTV.setText(mOrder.city);
        if (!TextUtils.isEmpty(mOrder.packageName)){
            packageNameTV.setVisibility(View.VISIBLE);
            packageNameTV.setText(mOrder.packageName);
        }
        
        totalPriceTV.setText(String.format("%.2f", mOrder.totalPrice));
        actualPaymentTV.setText(String.format("%.2f", mOrder.actualPayment));
        appointedDateTV.setText(mOrder.appointmentDate);
        createdTV.setText("订单创建时间："+DateUtil.timeStamp2String((long)mOrder.created));
        if (mOrder.paymentTime != 0){
            paymentTime.setText("订单支付时间："+DateUtil.timeStamp2String((long)mOrder.paymentTime));
        }

        numberTV.setText("订单编号："+mOrder.number);
        
        switch (mOrder.status){
            case 0:
                mRefundBtn.setVisibility(View.GONE);
                evaluateBtn.setVisibility(View.GONE);
                break;
            case 1:
                if (mOrder.price == 0){
                    mRefundBtn.setVisibility(View.GONE);
                }
                payBtn.setVisibility(View.GONE);
                break;
            case 3:
                mRefundBtn.setVisibility(View.GONE);
                payBtn.setVisibility(View.GONE);
                evaluateBtn.setText(getContext().getResources().getString(R.string.append_evaluation));
                break;
            case 4:
            case 5:
                mRefundBtn.setVisibility(View.GONE);
                payBtn.setVisibility(View.GONE);
                evaluateBtn.setVisibility(View.GONE);
                break;
        }
        
        if (mType == Utility.OrderType.WAITING_EVALUATION.ordinal()){
            mRefundBtn.setVisibility(View.GONE);
        }

        mRefundBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkUnsubscribeCondition();
            }
        });
        
        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OrderPaymentDF orderPaymentDF = OrderPaymentDF.newInstance(mOrder);
                orderPaymentDF.show(getFragmentManager(), "OrderPaymentDF");
                mDialog.dismiss();
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
    
    private void checkUnsubscribeCondition(){
        showProgressDialog("正在申请...");
        if (mOid == 0 && mOrder != null){
            mOid = mOrder.oid;
        }
        RequestBody requestBody = new FormBody.Builder()
                .add("oid", String.valueOf(mOid))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_UNSUBSCRIBE_CONDITION, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug)
                    Slog.d(TAG, "==========checkUnsubscribeCondition response body : " + responseText);
                if (responseText != null) {
                    try {
                        dismissProgressDialog();
                        JSONObject jsonObject = new JSONObject(responseText);
                        mCondition = jsonObject.optInt("result");
                        myHandler.sendEmptyMessage(GET_UNSUBSCRIBE_CONDITION_DONE);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {}
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
    
    private void getOrderByOid(int oid){
        RequestBody requestBody = new FormBody.Builder()
                .add("oid", String.valueOf(oid))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_ORDER_BY_OID, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug)
                    Slog.d(TAG, "==========getOrderByOid response body : " + responseText);
                if (responseText != null) {
                    try {
                        dismissProgressDialog();
                        JSONObject jsonObject = new JSONObject(responseText);
                        OrdersListDF ordersListDF = new OrdersListDF();
                        mOrder = ordersListDF.getOrderManager(jsonObject.optJSONObject("order"));
                        myHandler.sendEmptyMessage(GET_ORDER_INFO_DONE);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {}
        });
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
           case GET_ORDER_INFO_DONE:
                initView();
                break;
            case GET_UNSUBSCRIBE_CONDITION_DONE:
                unsubscribeNotice();
                break;
            case SUBMIT_REFUND_DONE:
                mRefundBtn.setClickable(false);
                mRefundBtn.setText("已申请退款");
                mRefundBtn.setBackground(getContext().getResources().getDrawable(R.drawable.btn_big_radius_default));
                break;

        }
    }

    public void unsubscribeNotice(){
        String message = "";
        if (mCondition == REFUND_FULL){
            message = "请预留您的支付宝账号，申请后会在1~2日内收到退款";
        }else {
            message = "由于您申请退款时间已超过24小时且未提前5天申请，根据平台规定，只能退款80%，申请后会在2天内退款到您支付宝账户";
        }
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(getContext(), R.style.Theme_MaterialComponents_Light_Dialog_Alert);

        View editView = LayoutInflater.from(getContext()).inflate(R.layout.refund_apply_edit, null);
        EditText alipayNameET = editView.findViewById(R.id.alipay_name);
        EditText alipayAccountET = editView.findViewById(R.id.alipay_account);
        EditText reasonET = editView.findViewById(R.id.reason);
        normalDialog.setTitle("申请退款")
            .setIcon(R.mipmap.mufu_icon)
                .setView(editView)
                .setMessage(message)
                .setPositiveButton("提交申请", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String name = alipayNameET.getText().toString();
                        String account = alipayAccountET.getText().toString();
                        String reason = reasonET.getText().toString();
                        if (TextUtils.isEmpty(name)){
                            Toast.makeText(getContext(), "请输入支付宝用户名", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (TextUtils.isEmpty(account)){
                            Toast.makeText(getContext(), "请输入支付宝账户", Toast.LENGTH_SHORT).show();
                            return;
                        }

                       if (TextUtils.isEmpty(reason)){
                            Toast.makeText(getContext(), "请说明退款原因", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        submitRefundApply(name, account, reason);
                    }
                }).setNegativeButton("取消",null).show();
    }
    
    private void submitRefundApply(String name, String account, String reason){
        showProgressDialog("正在提交");
        String amount;
        if (mCondition == REFUND_FULL){
            amount = String.valueOf(mOrder.actualPayment);
        }else {
            DecimalFormat decimalFormat=new DecimalFormat(".00");
            amount = decimalFormat.format(mOrder.actualPayment * 0.8);
        }
        RequestBody requestBody = new FormBody.Builder()
                .add("oid", String.valueOf(mOid))
                .add("name", name)
                .add("account", account)
                .add("amount", amount)
                .add("reason", reason)
                .build();
        
        HttpUtil.sendOkHttpRequest(getContext(), APPLY_REFUND, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug)
                    Slog.d(TAG, "==========submitRefundApply response body : " + responseText);
                if (responseText != null) {
                    try {
                        dismissProgressDialog();
                        JSONObject jsonObject = new JSONObject(responseText);
                        int result = jsonObject.optInt("result");
                        if (result > 0){
                            myHandler.sendEmptyMessage(SUBMIT_REFUND_DONE);
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {}
        });
    }
    
        private class OrderDetailsBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ORDER_MODIFIED_BROADCAST:
                    getOrderByOid(mOrder.oid);
                    break;
            }
        }
    }

    private void registerBroadcast() {
        mReceiver = new OrderDetailsBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ORDER_MODIFIED_BROADCAST);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, intentFilter);
    }

    //unregister local broadcast
    private void unRegisterBroadcast() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
        unRegisterBroadcast();
    }


    @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
        unRegisterBroadcast();
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
