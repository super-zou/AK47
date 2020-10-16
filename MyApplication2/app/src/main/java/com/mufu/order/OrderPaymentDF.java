package com.mufu.order;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.alipay.sdk.app.PayTask;
import com.mufu.R;
import com.mufu.common.MyApplication;
import com.mufu.experience.ExperienceDetailActivity;
import com.mufu.experience.GuideDetailActivity;
import com.mufu.util.BaseDialogFragment;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.Slog;
import com.mufu.util.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;
import static com.mufu.order.OrderDetailsDF.newInstance;
import static com.mufu.order.PlaceOrderDF.CONSULT_PAYMENT_SUCCESS_BROADCAST;
import static com.mufu.order.PlaceOrderDF.ORDER_PAYMENT_SUCCESS_BROADCAST;

public class OrderPaymentDF extends BaseDialogFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "OrderPaymentDF";
    private Dialog mDialog;
    private Window window;
    private MyHandler myHandler;
    private MyOrdersFragmentDF.Order mOrder;
    private String orderInfo;
    public static final int GET_ORDER_INFO_DONE = 0;
    private static final int SDK_PAY_FLAG = 1;
    private static final int UPDATE_ORDER_STATUS_DONE = 2;
    public static final String GET_PAY_ORDER_INFO = HttpUtil.DOMAIN + "?q=payment/get_order_info";
    public static final String UPDATE_ORDER_STATUS = HttpUtil.DOMAIN + "?q=order_manager/update_order_status";
    
    public static OrderPaymentDF newInstance(MyOrdersFragmentDF.Order order) {
        OrderPaymentDF orderDetailsDF = new OrderPaymentDF();
        Bundle bundle = new Bundle();
        bundle.putSerializable("order", order);
        orderDetailsDF.setArguments(bundle);

        return orderDetailsDF;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_MaterialComponents_DialogWhenLarge);
        mDialog.setContentView(R.layout.order_payment);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mOrder = (MyOrdersFragmentDF.Order) bundle.getSerializable("order");
        }
        
        mDialog.setCanceledOnTouchOutside(true);
        window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        myHandler = new MyHandler(this);

        TextView dismiss = mDialog.findViewById(R.id.dismiss);
        dismiss.setOnClickListener(new View.OnClickListener() {
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
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.order_summary), font);
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.confirm), font);
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.consult_cny), font);

        ConstraintLayout orderPaymentCL = mDialog.findViewById(R.id.order_summary);
        ConstraintLayout consultOrderPaymentCL = mDialog.findViewById(R.id.consult_order_summary);

        TextView titleTV = mDialog.findViewById(R.id.experience_title);
        TextView packageNameTV = mDialog.findViewById(R.id.experience_package_title);
        TextView priceTV = mDialog.findViewById(R.id.price);
        Button confirmBtn = mDialog.findViewById(R.id.confirm);
        
        if (mOrder.type == Utility.TalentType.EXPERIENCE.ordinal() || mOrder.type == Utility.TalentType.GUIDE.ordinal()){
            TextView totalPriceTV = mDialog.findViewById(R.id.total);
            TextView amountTV = mDialog.findViewById(R.id.amount);

            titleTV.setText(mOrder.title);
            if (!TextUtils.isEmpty(mOrder.packageName)){
                packageNameTV.setVisibility(View.VISIBLE);
                packageNameTV.setText(mOrder.packageName);
            }
            priceTV.setText(String.format("%.2f", mOrder.price));
            amountTV.setText(String.valueOf(mOrder.amount));
            totalPriceTV.setText(String.format("%.2f", mOrder.totalPrice));
            confirmBtn.setText("确认支付 "+getContext().getResources().getString(R.string.fa_cny)+String.format("%.2f", mOrder.totalPrice));

            titleTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity();
                }
            });
        }else {
            orderPaymentCL.setVisibility(View.INVISIBLE);
            consultOrderPaymentCL.setVisibility(View.VISIBLE);
            TextView paymentTitleTV = mDialog.findViewById(R.id.payment_title);
            paymentTitleTV.setText("打赏");
            TextView consultTitleTV = mDialog.findViewById(R.id.consult_title);
            TextView consultPriceTV = mDialog.findViewById(R.id.consult_price);
            consultTitleTV.setText(mOrder.title);
            consultPriceTV.setText(String.format("%.2f", mOrder.price));
            confirmBtn.setText("确认打赏 "+getContext().getResources().getString(R.string.fa_cny)+String.format("%.2f", mOrder.price));
        }
        
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPayOrderInfo();
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
    
    private JSONObject getOrderInfoJsonObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("subject", mOrder.title);
            jsonObject.put("body", mOrder.title);
            jsonObject.put("number", mOrder.number);
            jsonObject.put("amount", String.valueOf(mOrder.amount));
            jsonObject.put("price", String.valueOf(mOrder.price));
            jsonObject.put("total_price", String.valueOf(mOrder.totalPrice));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
    
    public void getPayOrderInfo(){
        showProgressDialog(getContext().getString(R.string.saving_progress));
        RequestBody requestBody = new FormBody.Builder()
                .add("order", getOrderInfoJsonObject().toString())
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_PAY_ORDER_INFO, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
            String responseText = response.body().string();
                if (isDebug)
                    Slog.d(TAG, "==========aliPay response body : " + responseText);
                if (responseText != null) {
                    try {
                        dismissProgressDialog();
                        JSONObject jsonObject = new JSONObject(responseText);
                        //int result = jsonObject.optInt("result");
                        orderInfo = jsonObject.optString("response");
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
    
    private void aliPay(){

        final Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                PayTask alipay = new PayTask(getActivity());
                Map<String, String> result = alipay.payV2(orderInfo, true);
                Slog.d(TAG, result.toString());

                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                myHandler.sendMessage(msg);
            }
        };

        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
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
                aliPay();
                break;
            case SDK_PAY_FLAG:
            @SuppressWarnings("unchecked")
                PayResult payResult = new PayResult((Map<String, String>) msg.obj);

                String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                String resultStatus = payResult.getResultStatus();
                // 判断resultStatus 为9000则代表支付成功
                if (TextUtils.equals(resultStatus, "9000")) {
                    // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                    updateOrderStatus();
                } else {
                    // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                    Toast.makeText(getContext(), "支付失败：" + resultStatus, Toast.LENGTH_LONG).show();
                }
                break;
                case UPDATE_ORDER_STATUS_DONE:
                Slog.d(TAG, "---------------->UPDATE_ORDER_STATUS_DONE");
                if (mOrder.type <= Utility.TalentType.EXPERIENCE.ordinal()){//experience or guide
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(ORDER_PAYMENT_SUCCESS_BROADCAST));
                    OrderDetailsDF orderDetailsDF = OrderDetailsDF.newInstance(mOrder.oid);
                    orderDetailsDF.show(getFragmentManager(), "OrderDetailsDF");
                }else {
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(CONSULT_PAYMENT_SUCCESS_BROADCAST));
                }
                mDialog.dismiss();
                break;
        }
    }

    private void updateOrderStatus(){
        RequestBody requestBody = new FormBody.Builder()
                .add("oid", String.valueOf(mOrder.oid))
                .add("id", String.valueOf(mOrder.id))
                .add("type", String.valueOf(mOrder.type))
                .add("actual_payment", String.valueOf(mOrder.totalPrice))
                .build();
                
                HttpUtil.sendOkHttpRequest(getContext(), UPDATE_ORDER_STATUS, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug)
                    Slog.d(TAG, "==========updateOrderStatus response body : " + responseText);
                if (responseText != null) {
                    try {
                        dismissProgressDialog();
                        JSONObject jsonObject = new JSONObject(responseText);
                        int result = jsonObject.optInt("result");
                        if (result > 0){
                            myHandler.sendEmptyMessage(UPDATE_ORDER_STATUS_DONE);
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

@Override
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
    }


    @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
    }
    
    static class MyHandler extends Handler {
        WeakReference<OrderPaymentDF> orderPaymentDFWeakReference;

        MyHandler(OrderPaymentDF orderPaymentDF) {
            orderPaymentDFWeakReference = new WeakReference<OrderPaymentDF>(orderPaymentDF);
        }

        @Override
        public void handleMessage(Message message) {
            OrderPaymentDF orderPaymentDF = orderPaymentDFWeakReference.get();
            if (orderPaymentDF != null) {
                orderPaymentDF.handleMessage(message);
            }
        }
    }
}
