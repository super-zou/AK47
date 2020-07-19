package com.mufu.order;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.mufu.R;
import com.mufu.common.MyApplication;
import com.mufu.util.BaseDialogFragment;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.OrderCodeFactory;
import com.mufu.util.SharedPreferencesUtils;
import com.mufu.util.Slog;
import com.mufu.util.Utility;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PlaceOrderDF extends BaseDialogFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "PlaceOrderDF";
    private Dialog mDialog;
    private Window window;
    private static int mType = Utility.TalentType.EXPERIENCE.ordinal();
    private int price;
    private int type;
    private int id;
    private int did;
    private int uid;
    private String title;
    private String date;
    private int mAmountPeople = 1;
    private Button payBtn;
    private TextView totalPriceTV;
    private TextView totalAmountTV;
    private MyHandler myHandler;
    private static final int GET_AVAILABLE_APPOINTMENT_DATE_DONE = 1;
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("EEE, d MMM yyyy");
    public static final String PLACE_ORDER = HttpUtil.DOMAIN + "?q=order_manager/place_order";
    
    public static PlaceOrderDF newInstance(String title, int price, int did, String date, int id, int type) {
        mType = Utility.TalentType.GUIDE.ordinal();
        PlaceOrderDF checkAppointDate = new PlaceOrderDF();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putInt("price", price);
        bundle.putInt("did", did);
        bundle.putInt("type", type);
        bundle.putString("date", date);
        bundle.putInt("id", id);
        checkAppointDate.setArguments(bundle);

        return checkAppointDate;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_MaterialComponents_DialogWhenLarge);
        mDialog.setContentView(R.layout.place_order);
        myHandler = new MyHandler(this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            price = bundle.getInt("price");
            did = bundle.getInt("did");
            mType = bundle.getInt("type");
            title = bundle.getString("title");
            date = bundle.getString("date");
            id = bundle.getInt("id", 0);
        }
        
        uid = SharedPreferencesUtils.getSessionUid(MyApplication.getContext());
        mDialog.setCanceledOnTouchOutside(false);
        window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.BOTTOM;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        TextView dismissTV = mDialog.findViewById(R.id.dismiss);
        dismissTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        
        totalPriceTV = mDialog.findViewById(R.id.total_price);
        totalAmountTV = mDialog.findViewById(R.id.total_amount);
        TextView titleTV = mDialog.findViewById(R.id.experience_title);
        TextView dateTV = mDialog.findViewById(R.id.appointment_date);
        TextView priceTV = mDialog.findViewById(R.id.price);
        titleTV.setText(title);
        dateTV.setText(date);
        priceTV.setText(String.valueOf(price));
        totalPriceTV.setText(String.valueOf(price));

        payBtn = mDialog.findViewById(R.id.pay);
        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitOrder();
            }
        });
        
        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.cny), font);
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.total_cny), font);
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.dismiss), font);

        processPeopleAmountChange();

        return mDialog;
    }
    
    private void processPeopleAmountChange(){

        Button minusBtn = mDialog.findViewById(R.id.minus);
        Button plusBtn = mDialog.findViewById(R.id.plus);
        TextView amountTV = mDialog.findViewById(R.id.amount);
        minusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAmountPeople = Integer.parseInt(amountTV.getText().toString());
                if (mAmountPeople > 1){
                    mAmountPeople--;
                    amountTV.setText(String.valueOf(mAmountPeople));
                    int totalPrice = mAmountPeople * price;
                    totalPriceTV.setText(String.valueOf(totalPrice));
                    totalAmountTV.setText("("+mAmountPeople+"人)");
                }
            }
        });

        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             mAmountPeople = Integer.parseInt(amountTV.getText().toString());
                mAmountPeople++;
                amountTV.setText(String.valueOf(mAmountPeople));
                int totalPrice = mAmountPeople * price;
                totalPriceTV.setText(String.valueOf(totalPrice));
                totalAmountTV.setText("("+mAmountPeople+"人)");
            }
        });
    }

    private void submitOrder(){
        showProgressDialog(getContext().getString(R.string.saving_progress));
        Slog.d(TAG, "--------------------->order number: "+getOrderNumber());
        RequestBody requestBody = new FormBody.Builder()
         .add("did", String.valueOf(did))
                .add("number", getOrderNumber())
                .add("price", String.valueOf(price))
                .add("amount", String.valueOf(mAmountPeople))
                .add("total_price", totalPriceTV.getText().toString())
                .add("type", String.valueOf(mType))
                .add("id", String.valueOf(id))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), PLACE_ORDER, requestBody, new Callback() {
        @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug)
                    Slog.d(TAG, "==========submitOrder response body : " + responseText);
                if (responseText != null) {
                    try {
                        dismissProgressDialog();
                        JSONObject jsonObject = new JSONObject(responseText);
                        int result = jsonObject.optInt("result");
                        //processResponse();
                        //myHandler.sendEmptyMessage(GET_AVAILABLE_APPOINTMENT_DATE_DONE);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private String getOrderNumber(){
        OrderCodeFactory.setGoodsType(OrderCodeFactory.GoodsType.EXPERIENCE);
        OrderCodeFactory.setPayType(OrderCodeFactory.PayType.WECHAT);

        return OrderCodeFactory.getOrderCode();
    }
    
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case GET_AVAILABLE_APPOINTMENT_DATE_DONE:

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
        WeakReference<PlaceOrderDF> placeOrderDFWeakReference;

        MyHandler(PlaceOrderDF placeOrderDF) {
            placeOrderDFWeakReference = new WeakReference<PlaceOrderDF>(placeOrderDF);
        }

        @Override
        public void handleMessage(Message message) {
            PlaceOrderDF checkAppointDate = placeOrderDFWeakReference.get();
            if (checkAppointDate != null) {
                checkAppointDate.handleMessage(message);
            }
        }
    }
}
