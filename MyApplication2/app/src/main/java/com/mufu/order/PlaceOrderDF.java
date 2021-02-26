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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.mufu.R;
import com.mufu.common.MyApplication;
import com.mufu.util.BaseDialogFragment;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.OrderCodeFactory;
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
    private int mOid;
    private int mSoldAmount;
    private int mMaximumAmount;
    private MyOrdersFragmentDF.Order order;
    private String title;
    private String date;
    private String mPackageName = "";
    private int mAmountPeople = 1;
        private int mIdentityRequirement = 0;
    private Button submitOrderBtn;
    private TextView totalPriceTV;
    private TextView totalAmountTV;
    private MyHandler myHandler;
    public static final int SUBMIT_ORDER_DONE = 1;
        private LinearLayout mIdentityRequirementEditWrapper;
    private ScrollView mPlaceOrderScrollView;
    private String[] mIdentityArray;
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("EEE, d MMM yyyy");
    public static final String PLACE_ORDER = HttpUtil.DOMAIN + "?q=order_manager/place_order";
        private OrderStatusBroadcastReceiver mReceiver;
    public static final String ORDER_PAYMENT_SUCCESS_BROADCAST = "com.mufu.action.ORDER_PAYMENT_SUCCESS";
    public static final String CONSULT_PAYMENT_SUCCESS_BROADCAST = "com.mufu.action.CONSULT_PAYMENT_SUCCESS";
    public static final String ORDER_SUBMIT_BROADCAST = "com.mufu.action.ORDER_SUBMIT_BROADCAST";
        public static final String ORDER_EVALUATE_SUCCESS_BROADCAST = "com.mufu.action.ORDER_EVALUATE_SUCCESS_BROADCAST";
    
    public static PlaceOrderDF newInstance(String title, String packageName, int price, int did, String date, int id, int sold, int maximum, int type, int identityRequirement) {
        mType = Utility.TalentType.GUIDE.ordinal();
        PlaceOrderDF checkAppointDate = new PlaceOrderDF();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        if (!TextUtils.isEmpty(packageName)){
            bundle.putString("packageName", packageName);
        }
        bundle.putInt("price", price);
        bundle.putInt("did", did);
        bundle.putInt("type", type);
        bundle.putString("date", date);
        bundle.putInt("id", id);
        bundle.putInt("sold", sold);
        bundle.putInt("maximum", maximum);
                bundle.putInt("identityRequirement", identityRequirement);
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
            mSoldAmount = bundle.getInt("sold", 0);
            mMaximumAmount = bundle.getInt("maximum", 0);
            
            if (!TextUtils.isEmpty(bundle.getString("packageName", ""))){
                mPackageName = bundle.getString("packageName");
            }
                        mIdentityRequirement = bundle.getInt("identityRequirement", 0);
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
        TextView packageNameTV = mDialog.findViewById(R.id.experience_package);
        TextView dateTV = mDialog.findViewById(R.id.appointment_date);
        TextView priceTV = mDialog.findViewById(R.id.price);
        TextView remainingTV = mDialog.findViewById(R.id.remaining_amount);
        TextView maximumTV = mDialog.findViewById(R.id.maximum_amount);
                mIdentityRequirementEditWrapper = mDialog.findViewById(R.id.identity_information_edit_wrapper);
        ConstraintLayout identityRequirementWrapper = mDialog.findViewById(R.id.identity_information_wrapper);
        mPlaceOrderScrollView = mDialog.findViewById(R.id.place_order_scrollview);

        if (mIdentityRequirement > 0){
            identityRequirementWrapper.setVisibility(View.VISIBLE);
        }
        if (!TextUtils.isEmpty(mPackageName)){
            packageNameTV.setVisibility(View.VISIBLE);
            packageNameTV.setText(mPackageName);
        }
        titleTV.setText(title);
        dateTV.setText(date);
        priceTV.setText(String.valueOf(price));
        totalPriceTV.setText(String.valueOf(price));
        maximumTV.setText(String.valueOf(mMaximumAmount));
        remainingTV.setText("余位"+ (mMaximumAmount - mSoldAmount));

                submitOrderBtn = mDialog.findViewById(R.id.submit_order);
        if (price == 0){
            ConstraintLayout participantsSetting = mDialog.findViewById(R.id.participants_setting);
            participantsSetting.setVisibility(View.GONE);
            submitOrderBtn.setText("参加");
        }
        submitOrderBtn.setOnClickListener(new View.OnClickListener() {
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
        
        registerBroadcast();
        
                editIdentityInformation();

        return mDialog;
    }
    
        private void editIdentityInformation(){
        Button addIdentityInfoBtn = mDialog.findViewById(R.id.add_identity_edit);
        Handler handler = new Handler();
        addIdentityInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View itemView = LayoutInflater.from(MyApplication.getContext())
                        .inflate(R.layout.identity_information_edit_item, (ViewGroup) mDialog.findViewById(android.R.id.content), false);
                mIdentityRequirementEditWrapper.addView(itemView);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mPlaceOrderScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }
        });
    }
    
    private boolean checkIdentityEditValidation(){
        int childrenCount = mIdentityRequirementEditWrapper.getChildCount();
        if (childrenCount == 0){
            Toast.makeText(getContext(), "请添加参与者身份信息！", Toast.LENGTH_LONG).show();
            return false;
        }
        if (childrenCount < mAmountPeople){
            Toast.makeText(getContext(), "参与者身份信息不能少于"+mAmountPeople+"位！", Toast.LENGTH_LONG).show();
            return false;
        }else {
            mIdentityArray = new String[childrenCount];
            for (int i=0; i<childrenCount; i++){
                LinearLayout identityItem = (LinearLayout) mIdentityRequirementEditWrapper.getChildAt(i);
                RadioGroup sexRadioGroup = (RadioGroup) identityItem.getChildAt(0);
                RadioButton maleRB = (RadioButton)sexRadioGroup.getChildAt(0);
                int sex = Utility.MALE;
                if (!maleRB.isChecked()){
                    sex = Utility.MALE+1;
                }
                
                EditText nameET = (EditText)identityItem.getChildAt(1);
                int k = i+1;
                if (TextUtils.isEmpty(nameET.getText().toString())){
                    Toast.makeText(getContext(), "第"+k+"个用户名不能为空！", Toast.LENGTH_LONG).show();
                    return false;
                }
                EditText universityET = (EditText)identityItem.getChildAt(2);
                if (TextUtils.isEmpty(universityET.getText().toString())){
                    Toast.makeText(getContext(), "第"+k+"个用户学校不能为空！", Toast.LENGTH_LONG).show();
                    return false;
                }
                EditText identityET = (EditText)identityItem.getChildAt(3);
                if (TextUtils.isEmpty(identityET.getText().toString())){
                    Toast.makeText(getContext(), "第"+k+"个用户身份证号不能为空！", Toast.LENGTH_LONG).show();
                    return false;
                }

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("sex", String.valueOf(sex));
                    jsonObject.put("realname", nameET.getText().toString());
                    jsonObject.put("university", universityET.getText().toString());
                    jsonObject.put("identity_number", identityET.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                
                String identityString = jsonObject.toString();
                mIdentityArray[i] = identityString;
            }
        }
        return true;
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
                    
                    if (!plusBtn.isClickable()){
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
                 if (mAmountPeople < remainingAmount){
                         mAmountPeople++;
                         amountTV.setText(String.valueOf(mAmountPeople));
                         int totalPrice = mAmountPeople * price;
                         totalPriceTV.setText(String.valueOf(totalPrice));
                         totalAmountTV.setText("("+mAmountPeople+"人)");

                         if (mAmountPeople == remainingAmount){
                             plusBtn.setClickable(false);
                         }
                 }
            }
        });
    }
    
    private void startOrderPaymentDF(){
        order.oid = mOid;
        order.type = mType;
        order.title = title;
        order.price = price;
        order.id = id;
        order.packageName = mPackageName;
        order.amount = mAmountPeople;
        order.number = getOrderNumber(Utility.TalentType.EXPERIENCE.ordinal());
        order.totalPrice = mAmountPeople * price;
        OrderPaymentDF orderPaymentDF = OrderPaymentDF.newInstance(order);
        orderPaymentDF.show(getFragmentManager(), "OrderPaymentDF");
    }

    private void submitOrder(){
        showProgressDialog(getContext().getString(R.string.submitting_progress));
        if (mIdentityRequirement > 0){
            if (!checkIdentityEditValidation()){
                dismissProgressDialog();
                return;
            }
        }
        FormBody.Builder builder = new FormBody.Builder()
                .add("did", String.valueOf(did))
                .add("title", title)
                .add("number", getOrderNumber(Utility.TalentType.EXPERIENCE.ordinal()))
                .add("price", String.valueOf(price))
                .add("amount", String.valueOf(mAmountPeople))
                .add("total_price", totalPriceTV.getText().toString())
                .add("type", String.valueOf(mType))
                .add("id", String.valueOf(id))
                .add("package_name", String.valueOf(mPackageName));
        
        if (mIdentityRequirement > 0 && mIdentityArray.length> 0){
            for (int i=0; i<mIdentityArray.length; i++){
                builder.add("identity_array[]", mIdentityArray[i]);
            }
        }

        HttpUtil.sendOkHttpRequest(getContext(), PLACE_ORDER, builder.build(), new Callback() {
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
                        if (result > 0){
                            myHandler.sendEmptyMessage(SUBMIT_ORDER_DONE);
                        }
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

    public static String getOrderNumber(int type){
        if (type == Utility.TalentType.EXPERIENCE.ordinal()){
            OrderCodeFactory.setGoodsType(OrderCodeFactory.GoodsType.EXPERIENCE);
        }else if (type == Utility.TalentType.GUIDE.ordinal()){
            OrderCodeFactory.setGoodsType(OrderCodeFactory.GoodsType.GUIDE);
        }else {
            OrderCodeFactory.setGoodsType(OrderCodeFactory.GoodsType.CONSULT);
        }


        OrderCodeFactory.setPayType(OrderCodeFactory.PayType.ALIPAY);

        return OrderCodeFactory.getOrderCode();
    }
    
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case SUBMIT_ORDER_DONE:
                if (price != 0){
                    startOrderPaymentDF();
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(ORDER_SUBMIT_BROADCAST));
                }else {
                    startOrderDetailsDF();
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(ORDER_PAYMENT_SUCCESS_BROADCAST));
                }
                break;
        }
    }
    
    public void startOrderDetailsDF(){
        OrderDetailsDF orderDetailsDF = OrderDetailsDF.newInstance(mOid);
        orderDetailsDF.show(getFragmentManager(), "OrderDetailsDF");
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
