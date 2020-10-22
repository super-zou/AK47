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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.mufu.R;
import com.mufu.common.MyApplication;
import com.mufu.util.BaseDialogFragment;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.OrderCodeFactory;
import com.mufu.util.Slog;
import com.mufu.util.Utility;

import org.json.JSONArray;
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

import static com.mufu.experience.BlockBookingSettingDF.GET_BLOCK_BOOKING_BASE_INFO_URL;
import static com.mufu.experience.BlockBookingSettingDF.GET_BLOCK_BOOKING_DONE;
import static com.mufu.experience.PackageSettingDF.GET_BLOCK_BOOKING_PACKAGE_URL;
import static com.mufu.experience.PackageSettingDF.GET_PACKAGE_DONE;

public class BlockBookingPlaceOrderDF extends BaseDialogFragment implements RadioGroup.OnCheckedChangeListener{
    public static final int SUBMIT_ORDER_DONE = 1;
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("EEE, d MMM yyyy");
    public static final String PLACE_ORDER = HttpUtil.DOMAIN + "?q=order_manager/place_order";
    public static final String ORDER_PAYMENT_SUCCESS_BROADCAST = "com.mufu.action.ORDER_PAYMENT_SUCCESS";
    public static final String CONSULT_PAYMENT_SUCCESS_BROADCAST = "com.mufu.action.CONSULT_PAYMENT_SUCCESS";
    public static final String ORDER_SUBMIT_BROADCAST = "com.mufu.action.ORDER_SUBMIT_BROADCAST";
    public static final String ORDER_EVALUATE_SUCCESS_BROADCAST = "com.mufu.action.ORDER_EVALUATE_SUCCESS_BROADCAST";
    private static final boolean isDebug = true;
    private static final String TAG = "BlockBookingPlaceOrderDF";
    private static int mType = Utility.TalentType.EXPERIENCE.ordinal();
    private Dialog mDialog;
    private Window window;
    private int price;
    private int type;
    private int id;
    private int did;
    private int mOid;
    private int mSoldAmount;
    private int mMaximumAmount;
    private MyOrdersFragmentDF.Order order;
    private String title;
    private String date;
    private String mPackageName = "";
    private int mAmountPeople = 1;
    private Button submitOrderBtn;
    private TextView totalPriceTV;
    private TextView totalAmountTV;
    private MyHandler myHandler;
    private JSONObject mBlockBookingResponse;
    private JSONArray mPackageResponse;
    private OrderStatusBroadcastReceiver mReceiver;
    private RadioButton mFirstPackageBtn;
    private RadioButton mSecondPackageBtn;
    private RadioButton mThirdPackageBtn;
    private String mSelectedPackageName = "";
    private int mLimitedNumber = 0;
    private double mAdditionalPrice = 0;
    private double mSelectedPackagePrice = 0;
    private TextView mTotalPriceDetailTV;
    private boolean isPackageSelected = false;
    private boolean hasPackage = false;
    
    public static BlockBookingPlaceOrderDF newInstance(String title, String packageName, int did, String date, int id, int sold, int maximum, int type) {
        mType = Utility.TalentType.GUIDE.ordinal();
        BlockBookingPlaceOrderDF checkAppointDate = new BlockBookingPlaceOrderDF();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        if (!TextUtils.isEmpty(packageName)) {
            bundle.putString("packageName", packageName);
        }
        
        bundle.putInt("did", did);
        bundle.putInt("type", type);
        bundle.putString("date", date);
        bundle.putInt("id", id);
        bundle.putInt("sold", sold);
        bundle.putInt("maximum", maximum);
        checkAppointDate.setArguments(bundle);

        return checkAppointDate;
    }
    
    public static String getOrderNumber(int type) {
        if (type == Utility.TalentType.EXPERIENCE.ordinal()) {
            OrderCodeFactory.setGoodsType(OrderCodeFactory.GoodsType.EXPERIENCE);
        } else if (type == Utility.TalentType.GUIDE.ordinal()) {
            OrderCodeFactory.setGoodsType(OrderCodeFactory.GoodsType.GUIDE);
        } else {
            OrderCodeFactory.setGoodsType(OrderCodeFactory.GoodsType.CONSULT);
        }


        OrderCodeFactory.setPayType(OrderCodeFactory.PayType.ALIPAY);

        return OrderCodeFactory.getOrderCode();
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_MaterialComponents_DialogWhenLarge);
        mDialog.setContentView(R.layout.block_booking_place_order);
        myHandler = new MyHandler(this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            did = bundle.getInt("did");
            mType = bundle.getInt("type");
            title = bundle.getString("title");
            date = bundle.getString("date");
            id = bundle.getInt("id", 0);
            mSoldAmount = bundle.getInt("sold", 0);
            mMaximumAmount = bundle.getInt("maximum", 0);
            
            if (!TextUtils.isEmpty(bundle.getString("packageName", ""))) {
                mPackageName = bundle.getString("packageName");
            }
        }

        order = new MyOrdersFragmentDF.Order();
        mReceiver = new OrderStatusBroadcastReceiver();

        //uid = SharedPreferencesUtils.getSessionUid(MyApplication.getContext());
        mDialog.setCanceledOnTouchOutside(false);
        window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.BOTTOM;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);
        
        RadioGroup radioGroup = mDialog.findViewById(R.id.package_radio_group);
        radioGroup.setOnCheckedChangeListener(this);

        TextView dismissTV = mDialog.findViewById(R.id.dismiss);
        dismissTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        
        totalPriceTV = mDialog.findViewById(R.id.total_price);
        mTotalPriceDetailTV = mDialog.findViewById(R.id.price_detail);
        TextView titleTV = mDialog.findViewById(R.id.experience_title);
        TextView packageNameTV = mDialog.findViewById(R.id.experience_package);
        TextView dateTV = mDialog.findViewById(R.id.appointment_date);
        TextView priceTV = mDialog.findViewById(R.id.starting_price);
        TextView remainingTV = mDialog.findViewById(R.id.remaining_amount);
        TextView maximumTV = mDialog.findViewById(R.id.maximum_amount);
        mFirstPackageBtn = mDialog.findViewById(R.id.first_package_selector_btn);
        mSecondPackageBtn = mDialog.findViewById(R.id.second_package_selector_btn);
        mThirdPackageBtn = mDialog.findViewById(R.id.third_package_selector_btn);
        
        if (!TextUtils.isEmpty(mPackageName)) {
            packageNameTV.setVisibility(View.VISIBLE);
            packageNameTV.setText(mPackageName);
        }
        titleTV.setText(title);
        dateTV.setText(date);
        //priceTV.setText(String.valueOf(price));
        //totalPriceTV.setText(String.valueOf(price));
        maximumTV.setText(String.valueOf(mMaximumAmount));
        remainingTV.setText("余位" + (mMaximumAmount - mSoldAmount));

        submitOrderBtn = mDialog.findViewById(R.id.submit_order);
        submitOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasPackage){
                    if (isPackageSelected){
                        submitOrder();
                    }else {
                        Toast.makeText(getContext(), "请选择套餐！", Toast.LENGTH_LONG).show();
                    }
                }

            }
        });

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.cny), font);
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.total_cny), font);
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.dismiss), font);

        getBlockBookingBaseInfo();
        getBlockBookingPackages();

        processPeopleAmountChange();

        registerBroadcast();

        return mDialog;
    }
    
    private void getBlockBookingBaseInfo(){
        showProgressDialog("");

        RequestBody requestBody = new FormBody.Builder()
                .add("eid", String.valueOf(id)).build();

        String uri = GET_BLOCK_BOOKING_BASE_INFO_URL;
        
        HttpUtil.sendOkHttpRequest(getContext(), uri, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "getBlockBookingBaseInfo response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        mBlockBookingResponse = new JSONObject(responseText).optJSONObject("block_booking");
                        if (mBlockBookingResponse != null){
                            dismissProgressDialog();
                            myHandler.sendEmptyMessage(GET_BLOCK_BOOKING_DONE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            
             @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void getBlockBookingPackages(){
        RequestBody requestBody;
        FormBody.Builder builder = new FormBody.Builder();
        String uri = "";
        requestBody = builder.add("eid", String.valueOf(id)).build();
        uri = GET_BLOCK_BOOKING_PACKAGE_URL;
        HttpUtil.sendOkHttpRequest(getContext(), uri, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "getBlockBookingPackages response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        mPackageResponse = new JSONObject(responseText).optJSONArray("packages");
                        if (mPackageResponse.length() > 0){
                            myHandler.sendEmptyMessage(GET_PACKAGE_DONE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void processPeopleAmountChange() {

        Button minusBtn = mDialog.findViewById(R.id.minus);
        Button plusBtn = mDialog.findViewById(R.id.plus);
        TextView amountTV = mDialog.findViewById(R.id.amount);
        minusBtn.setOnClickListener(new View.OnClickListener() {
        @Override
            public void onClick(View view) {
                mAmountPeople = Integer.parseInt(amountTV.getText().toString());
                if (mAmountPeople > 1) {
                    mAmountPeople--;
                    amountTV.setText(String.valueOf(mAmountPeople));
                    if (mAmountPeople <= mLimitedNumber){
                        totalPriceTV.setText(String.valueOf(mSelectedPackagePrice));
                    }else {
                        int additionalNumber = mAmountPeople - mLimitedNumber;
                        double totalPrice = mSelectedPackagePrice + additionalNumber*mAdditionalPrice;
                        totalPriceTV.setText(String.valueOf(totalPrice));
                        mTotalPriceDetailTV.setText("="+mSelectedPackagePrice+"+"+additionalNumber+"人×"+mAdditionalPrice);
                    }
                    
                    if (!plusBtn.isClickable()) {
                        plusBtn.setClickable(true);
                    }
                }
            }
        });

        int remainingAmount = mMaximumAmount - mSoldAmount;

        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            mAmountPeople = Integer.parseInt(amountTV.getText().toString());
                if (mAmountPeople < remainingAmount) {
                    mAmountPeople++;
                    amountTV.setText(String.valueOf(mAmountPeople));
                    if (mAmountPeople <= mLimitedNumber){
                        totalPriceTV.setText(String.valueOf(mSelectedPackagePrice));
                    }else {
                        int additionalNumber = mAmountPeople - mLimitedNumber;
                        double totalPrice = mSelectedPackagePrice + additionalNumber*mAdditionalPrice;
                        totalPriceTV.setText(String.valueOf(totalPrice));
                        mTotalPriceDetailTV.setText("="+mSelectedPackagePrice+"+"+additionalNumber+"人×"+mAdditionalPrice);
                    }

                    if (mAmountPeople == remainingAmount) {
                        plusBtn.setClickable(false);
                    }
                }
            }
        });
   }

    private void startOrderPaymentDF() {
        order.oid = mOid;
        order.type = mType;
        order.orderClass = Utility.OrderClass.BLOCK_BOOKING.ordinal();
        order.title = title;
        order.price = price;
        order.id = id;
        order.packageName = mSelectedPackageName;
        order.amount = mAmountPeople;
        order.number = getOrderNumber(Utility.TalentType.EXPERIENCE.ordinal());
        order.totalPrice = Float.parseFloat(totalPriceTV.getText().toString());
        OrderPaymentDF orderPaymentDF = OrderPaymentDF.newInstance(order);
        orderPaymentDF.show(getFragmentManager(), "OrderPaymentDF");
    }
    
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int index = 0;
        switch (checkedId){
            case R.id.first_package_selector_btn:
                index = 0;
                break;
            case R.id.second_package_selector_btn:
                index = 1;
                break;
            case R.id.third_package_selector_btn:
                index = 2;
                break;
        }
        isPackageSelected = true;
        Slog.d(TAG, "----------------index: "+index);
        mSelectedPackageName = mPackageResponse.optJSONObject(index).optString("name");
        mSelectedPackagePrice = mPackageResponse.optJSONObject(index).optInt("price");
        if (mAmountPeople <= mLimitedNumber){
            totalPriceTV.setText(String.valueOf(mSelectedPackagePrice));
        }else {
            int additionalNumber = mAmountPeople - mLimitedNumber;
            double totalPrice = mSelectedPackagePrice + additionalNumber*mAdditionalPrice;
            totalPriceTV.setText(String.valueOf(totalPrice));
            mTotalPriceDetailTV.setText("="+mSelectedPackagePrice+"+"+additionalNumber+"人×"+mAdditionalPrice);
        }
        //order.packageName = mSelectedPackageName;
    }
    
    private void submitOrder() {
        showProgressDialog(getContext().getString(R.string.submitting_progress));
        RequestBody requestBody = new FormBody.Builder()
                .add("did", String.valueOf(did))
                .add("number", getOrderNumber(Utility.TalentType.EXPERIENCE.ordinal()))
                .add("price", String.valueOf(mSelectedPackagePrice))
                .add("amount", String.valueOf(mAmountPeople))
                .add("total_price", totalPriceTV.getText().toString())
                .add("type", String.valueOf(mType))
                .add("id", String.valueOf(id))
                .add("class", String.valueOf(Utility.OrderClass.BLOCK_BOOKING.ordinal()))
                .add("package_name", mSelectedPackageName)
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
                        mOid = jsonObject.optInt("oid");
                        if (result > 0) {
                            myHandler.sendEmptyMessage(SUBMIT_ORDER_DONE);
                        }
                        
                        } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
    private void parseBlockBookingBaseInfo(){
        TextView startingLimitedNumberTV = mDialog.findViewById(R.id.starting_limited_number);
        TextView startingPriceTV = mDialog.findViewById(R.id.starting_price);
        TextView additionalPriceTV = mDialog.findViewById(R.id.additional_price_desc);
        mLimitedNumber = mBlockBookingResponse.optInt("limited_number");
        startingLimitedNumberTV.setText("限定人数："+mLimitedNumber);
        mSelectedPackagePrice = mBlockBookingResponse.optDouble("starting_price");
        startingPriceTV.setText(String.valueOf(mSelectedPackagePrice));
        mAdditionalPrice = mBlockBookingResponse.optDouble("additional_price");
        additionalPriceTV.setText("超员加收："+mAdditionalPrice+"元/人");
        totalPriceTV.setText(String.valueOf(mBlockBookingResponse.optDouble("starting_price")));
    }
    
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case SUBMIT_ORDER_DONE:
                startOrderPaymentDF();
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(ORDER_SUBMIT_BROADCAST));
                break;
            case GET_BLOCK_BOOKING_DONE:
                parseBlockBookingBaseInfo();
                break;
            case GET_PACKAGE_DONE:
                hasPackage = true;
                LinearLayout packageWrapperLL = mDialog.findViewById(R.id.package_wrapper);
                packageWrapperLL.setVisibility(View.VISIBLE);
                parsePackagesResponse();
                break;
        }
    }
    
    private void parsePackagesResponse(){
        JSONObject packageObject = mPackageResponse.optJSONObject(0);
        mFirstPackageBtn.setVisibility(View.VISIBLE);
        mFirstPackageBtn.setText(packageObject.optString("name")+"\n"+packageObject.optInt("price")+"元起");

        if (mPackageResponse.length() >= 2){
            mSecondPackageBtn.setVisibility(View.VISIBLE);
            JSONObject packageObjectSecond = mPackageResponse.optJSONObject(1);
            mSecondPackageBtn.setText(packageObjectSecond.optString("name")+"\n"+packageObjectSecond.optInt("price")+"元起");

            if (mPackageResponse.length() >= 3){
                mThirdPackageBtn.setVisibility(View.VISIBLE);
                JSONObject packageObjectThird = mPackageResponse.optJSONObject(2);
                mThirdPackageBtn.setText(packageObjectThird.optString("name")+"\n"+packageObjectThird.optInt("price")+"元起");
            }
        }
    }
    
    private void registerBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ORDER_PAYMENT_SUCCESS_BROADCAST);
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
        WeakReference<BlockBookingPlaceOrderDF> placeOrderDFWeakReference;

        MyHandler(BlockBookingPlaceOrderDF placeOrderDF) {
            placeOrderDFWeakReference = new WeakReference<BlockBookingPlaceOrderDF>(placeOrderDF);
        }

        @Override
        public void handleMessage(Message message) {
            BlockBookingPlaceOrderDF checkAppointDate = placeOrderDFWeakReference.get();
            if (checkAppointDate != null) {
                checkAppointDate.handleMessage(message);
            }
        }
    }
    
    private class OrderStatusBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Slog.d(TAG, "-------------->OrderStatusBroadcastReceiver");
            switch (intent.getAction()) {
                case ORDER_PAYMENT_SUCCESS_BROADCAST:
                    mDialog.dismiss();
                    break;
            }
        }
    }
}
