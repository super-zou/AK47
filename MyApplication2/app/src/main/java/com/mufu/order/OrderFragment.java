package com.mufu.order;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.mufu.R;
import com.mufu.common.MyApplication;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.SharedPreferencesUtils;
import com.mufu.util.Slog;
import com.mufu.util.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.mufu.common.SettingsActivity.GET_ADMIN_ROLE_DOWN;
import static com.mufu.common.SettingsActivity.GET_ADMIN_ROLE_URL;
import static com.mufu.main.MeetArchiveFragment.GET_EXPERIENCE_STATISTICS_URL;
import static com.mufu.main.MeetArchiveFragment.GET_GUIDE_STATISTICS_URL;
import static com.mufu.main.MeetArchiveFragment.LOAD_MY_EXPERIENCES_DONE;
import static com.mufu.main.MeetArchiveFragment.LOAD_MY_GUIDE_COUNT_DONE;
import static com.mufu.order.PlaceOrderDF.ORDER_EVALUATE_SUCCESS_BROADCAST;
import static com.mufu.order.PlaceOrderDF.ORDER_PAYMENT_SUCCESS_BROADCAST;
import static com.mufu.order.PlaceOrderDF.ORDER_SUBMIT_BROADCAST;

public class OrderFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = "OrderFragment";
    private static final boolean isDebug = true;
    
    private int myExperienceSize = 0;
    private int myGuideCount = 0;
    private int mUid = 0;
    private int mRole = 0;
    private static final int GET_TODAY_ORDERS_AMOUNT_DONE = 0;
    private static final int GET_QUEUED_ORDERS_AMOUNT_DONE = 1;
    private static final int GET_FINISHED_ORDERS_AMOUNT_DONE = 2;
    private static final int GET_MY_UNPAID_ORDERS_AMOUNT_DONE = 3;
    private static final int GET_MY_BOOKED_ORDERS_AMOUNT_DONE = 4;
    private static final int GET_WAITING_FOR_MY_EVALUATION_ORDERS_AMOUNT_DONE = 5;
    private static final int GET_MY_REVENUE_DONE = 6;
    public static final String GET_TODAY_ORDERS_AMOUNT = HttpUtil.DOMAIN + "?q=order_manager/get_today_orders_amount";
    public static final String GET_QUEUED_ORDERS_AMOUNT = HttpUtil.DOMAIN + "?q=order_manager/get_queued_orders_amount";
    public static final String GET_FINISHED_ORDERS_AMOUNT = HttpUtil.DOMAIN + "?q=order_manager/get_finished_orders_amount";
    private static final String GET_MY_UNPAID_ORDERS_AMOUNT = HttpUtil.DOMAIN + "?q=order_manager/get_my_unpaid_orders_amount";
    private static final String GET_MY_BOOKED_ORDERS_AMOUNT = HttpUtil.DOMAIN + "?q=order_manager/get_my_booked_orders_amount";
    private static final String GET_WAITING_FOR_MY_EVALUATION_ORDERS_AMOUNT = HttpUtil.DOMAIN + "?q=order_manager/get_waiting_for_my_evaluation_orders_amount";
    public static final String GET_MY_REVENUE = HttpUtil.DOMAIN + "?q=order_manager/get_my_revenue";
    
    private View view;
    private MyHandler handler;
    private TextView mMyAllOrdersTV;
    private TextView mMyAllOrdersNavTV;
    private TextView mMyAllSoldOrdersNavTV;
    private TextView mTodayOrdersAmountTV;
    private TextView mQueuedOrdersAmountTV;
    private TextView mFinishedOrdersAmountTV;
    private TextView mMyAllSoldOrdersTV;
    private ConstraintLayout mSoldOrdersWrapper;
    private TextView mUnPaidOrdersAmountTV;
    private TextView mBookedOrdersAmountTV;
    private TextView mWaitingForMyEvaluationOrdersAmountTV;
    private OrderStatusBroadcastReceiver mReceiver;
    private ConstraintLayout revenueWrapper;
    private Button mGetAllOrdersBtn;
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_order_manager, container, false);
        handler = new MyHandler(this);
        //getAdminRole();
        mUid = SharedPreferencesUtils.getSessionUid(MyApplication.getContext());
        initConentView();
        return view;
    }
    
    public void initConentView() {
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(view.findViewById(R.id.all_orders_nav), font);
        FontManager.markAsIconContainer(view.findViewById(R.id.my_all_sold_orders_nav), font);
        mSoldOrdersWrapper = view.findViewById(R.id.sold_orders_wrapper);
        mTodayOrdersAmountTV = view.findViewById(R.id.today_orders_amount);
        mQueuedOrdersAmountTV = view.findViewById(R.id.queued_orders_amount);
        mFinishedOrdersAmountTV = view.findViewById(R.id.finished_orders_amount);
        mMyAllSoldOrdersTV = view.findViewById(R.id.my_all_sold_orders);
        mMyAllSoldOrdersNavTV = view.findViewById(R.id.my_all_sold_orders_nav);
        mUnPaidOrdersAmountTV = view.findViewById(R.id.waiting_to_pay_amount);
        mBookedOrdersAmountTV = view.findViewById(R.id.booked_amount);
        mMyAllOrdersTV = view.findViewById(R.id.all_orders);
        mMyAllOrdersNavTV = view.findViewById(R.id.all_orders_nav);
        mWaitingForMyEvaluationOrdersAmountTV = view.findViewById(R.id.waiting_for_evaluation_amount);
        mGetAllOrdersBtn = view.findViewById(R.id.get_all_orders_btn);

        mMyAllSoldOrdersTV.setOnClickListener(this);
        mMyAllSoldOrdersNavTV.setOnClickListener(this);
        mMyAllOrdersTV.setOnClickListener(this);
        mMyAllOrdersNavTV.setOnClickListener(this);
        mGetAllOrdersBtn.setOnClickListener(this);


        getMyOrdersAmount();
        loadMyExperiencesCount();
        loadMyGuidesCount();
        processMyRevenue();
        getAdminRole();

        registerBroadcast();
    }
    
    private void getMyOrdersAmount(){
        getMyUnpaidOrdersAmount();
        getMyBookedOrdersAmount();
        getWaitingForMyEvaluationOrdersAmount();
    }

    private void processMyRevenue(){
        revenueWrapper = view.findViewById(R.id.revenue_wrapper);
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(revenueWrapper, font);
        //FontManager.markAsIconContainer(mDialog.findViewById(R.id.revenue_nav), font);

        getMyRevenue();
    }
    
    private void getMyUnpaidOrdersAmount(){
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_MY_UNPAID_ORDERS_AMOUNT, new FormBody.Builder().build(), new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========loadTodayOrdersAmount response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject responseObject = null;
                        try {
                            responseObject = new JSONObject(responseText);
                            if (responseObject != null) {
                                int amount = responseObject.optInt("amount");
                                Message message = new Message();
                                Bundle bundle = new Bundle();
                                bundle.putInt("amount", amount);
                                message.what = GET_MY_UNPAID_ORDERS_AMOUNT_DONE;
                                message.setData(bundle);
                                handler.sendMessage(message);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }
    
     private void getMyBookedOrdersAmount(){
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_MY_BOOKED_ORDERS_AMOUNT, new FormBody.Builder().build(), new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========loadTodayOrdersAmount response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject responseObject = null;
                        try {
                            responseObject = new JSONObject(responseText);
                            if (responseObject != null) {
                                 int amount = responseObject.optInt("amount");
                                if (amount > 0){
                                    Message message = new Message();
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("amount", amount);
                                    message.what = GET_MY_BOOKED_ORDERS_AMOUNT_DONE;
                                    message.setData(bundle);
                                    handler.sendMessage(message);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }

    private void getWaitingForMyEvaluationOrdersAmount(){
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_WAITING_FOR_MY_EVALUATION_ORDERS_AMOUNT, new FormBody.Builder().build(), new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========getWaitingForMyEvaluationOrdersAmount response text : " + responseText);
if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject responseObject = null;
                        try {
                            responseObject = new JSONObject(responseText);
                            if (responseObject != null) {
                                int amount = responseObject.optInt("amount");
                                if (amount > 0){
                                    Message message = new Message();
                                    
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("amount", amount);
                                    message.what = GET_WAITING_FOR_MY_EVALUATION_ORDERS_AMOUNT_DONE;
                                    message.setData(bundle);
                                    handler.sendMessage(message);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }
    
    private void loadMyExperiencesCount(){
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(mUid))
                .build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_EXPERIENCE_STATISTICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========loadMyExperiencesCount response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject experienceResponse = null;
                        try {
                            experienceResponse = new JSONObject(responseText);
                            if (experienceResponse != null) {
                                myExperienceSize = experienceResponse.optInt("count");
                                if (myExperienceSize > 0){
                                    handler.sendEmptyMessage(LOAD_MY_EXPERIENCES_DONE);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }
    
    private void loadMyGuidesCount(){
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(mUid))
                .build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_GUIDE_STATISTICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========loadMyGuidesCount response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject experienceResponse = null;
                         try {
                            experienceResponse = new JSONObject(responseText);
                            if (experienceResponse != null) {
                                myGuideCount = experienceResponse.optInt("count");
                                if (myGuideCount > 0){
                                    handler.sendEmptyMessage(LOAD_MY_GUIDE_COUNT_DONE);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }
    
    private void loadTodayOrdersAmount(){
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(mUid))
                .build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_TODAY_ORDERS_AMOUNT, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========loadTodayOrdersAmount response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                         JSONObject responseObject = null;
                        try {
                            responseObject = new JSONObject(responseText);
                            if (responseObject != null) {
                                int amount = responseObject.optInt("amount");
                                if (amount > 0){
                                    Message message = new Message();
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("amount", amount);
                                    message.what = GET_TODAY_ORDERS_AMOUNT_DONE;
                                    message.setData(bundle);
                                    handler.sendMessage(message);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }

    private void loadQueuedOrdersAmount(){
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(mUid))
                .build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_QUEUED_ORDERS_AMOUNT, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========loadQueuedOrdersAmount response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject responseObject = null;
                        try {
                            responseObject = new JSONObject(responseText);
                            if (responseObject != null) {
                                int amount = responseObject.optInt("amount");
                                if (amount > 0){
                                    Message message = new Message();
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("amount", amount);
                                    message.what = GET_QUEUED_ORDERS_AMOUNT_DONE;
                                    message.setData(bundle);
                                    handler.sendMessage(message);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }
    
    private void loadfinishedOrdersAmount(){
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(mUid))
                .build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_FINISHED_ORDERS_AMOUNT, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========loadfinishedOrdersAmount response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject responseObject = null;
                        try {
                            responseObject = new JSONObject(responseText);
                            if (responseObject != null) {
                                int amount = responseObject.optInt("amount");
                                if (amount > 0){
                                    Message message = new Message();
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("amount", amount);
                                    message.what = GET_FINISHED_ORDERS_AMOUNT_DONE;
                                    message.setData(bundle);
                                    handler.sendMessage(message);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

@Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }

    private void getMyRevenue(){
        HttpUtil.sendOkHttpRequest(getContext(), GET_MY_REVENUE, new FormBody.Builder().build(), new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug)
                        Slog.d(TAG, "==========getMyRevenue response text : " + responseText);
                    if (responseText != null) {
                        if (!TextUtils.isEmpty(responseText)) {
                            try {

                                int revenue = new JSONObject(responseText).optInt("revenue");
                                if (revenue > 0) {
                                    Message message = new Message();
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("revenue", revenue);
                                    message.setData(bundle);
                                    message.what = GET_MY_REVENUE_DONE;
                                    handler.sendMessage(message);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }

    private void setMyRevenueView(int revenue){
        TextView revenueValueTV = view.findViewById(R.id.revenue_amount);
        revenueValueTV.setText(revenue+"元");

        TextView revenueNavTV = view.findViewById(R.id.revenue_nav);
        revenueNavTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRevenueDialog();
            }
        });
    }
    
    private void showRevenueDialog() {
        final AlertDialog.Builder normalDialogBuilder = new AlertDialog.Builder(getActivity());
        normalDialogBuilder.setTitle(getResources().getString(R.string.withdrawal_title));
        normalDialogBuilder.setMessage(getResources().getString(R.string.withdrawal_message));

        AlertDialog normalDialog = normalDialogBuilder.create();
        normalDialog.show();
        
        try {
            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object mAlertController = mAlert.get(normalDialog);
            Field mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
            mMessage.setAccessible(true);
            TextView mMessageView = (TextView) mMessage.get(mAlertController);
            mMessageView.setTextColor(getResources().getColor(R.color.background));
            mMessageView.setTextSize(16);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        
        normalDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.color_disabled));
        normalDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.color_blue));
        normalDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(18);
    }
    
    private void getAdminRole() {
        RequestBody requestBody = new FormBody.Builder().build();
        HttpUtil.sendOkHttpRequest(getContext(), GET_ADMIN_ROLE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                try {
                    mRole = new JSONObject(responseText).optInt("role");
                    if (mRole >= 0){
                        handler.sendEmptyMessage(GET_ADMIN_ROLE_DOWN);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }

    public void handleMessage(Message message) {
        Bundle bundle = message.getData();
        switch (message.what) {
            case LOAD_MY_GUIDE_COUNT_DONE:
            case LOAD_MY_EXPERIENCES_DONE:
                if (mSoldOrdersWrapper.getVisibility() == View.GONE){
                    mSoldOrdersWrapper.setVisibility(View.VISIBLE);
                }
                loadTodayOrdersAmount();
                loadQueuedOrdersAmount();
                loadfinishedOrdersAmount();
                break;
            case GET_TODAY_ORDERS_AMOUNT_DONE:
                mTodayOrdersAmountTV.setText(String.valueOf(bundle.getInt("amount")));
                mTodayOrdersAmountTV.setOnClickListener(this);
                break;
            case GET_QUEUED_ORDERS_AMOUNT_DONE:
                mQueuedOrdersAmountTV.setText(String.valueOf(bundle.getInt("amount")));
                mQueuedOrdersAmountTV.setOnClickListener(this);
                break;
                case GET_FINISHED_ORDERS_AMOUNT_DONE:
                mFinishedOrdersAmountTV.setText(String.valueOf(bundle.getInt("amount")));
                mFinishedOrdersAmountTV.setOnClickListener(this);
                break;
            case GET_MY_UNPAID_ORDERS_AMOUNT_DONE:
                mUnPaidOrdersAmountTV.setText(String.valueOf(bundle.getInt("amount", 0)));
                mUnPaidOrdersAmountTV.setOnClickListener(this);
                break;
            case GET_MY_BOOKED_ORDERS_AMOUNT_DONE:
                mBookedOrdersAmountTV.setText(String.valueOf(bundle.getInt("amount")));
                mBookedOrdersAmountTV.setOnClickListener(this);
                break;
            case GET_WAITING_FOR_MY_EVALUATION_ORDERS_AMOUNT_DONE:
                mWaitingForMyEvaluationOrdersAmountTV.setText(String.valueOf(bundle.getInt("amount")));
                mWaitingForMyEvaluationOrdersAmountTV.setOnClickListener(this);
                break;
                case GET_MY_REVENUE_DONE:
                revenueWrapper.setVisibility(View.VISIBLE);
                int revenue = bundle.getInt("revenue");
                setMyRevenueView(revenue);
                break;
            case GET_ADMIN_ROLE_DOWN:
                mGetAllOrdersBtn.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View view){
        Bundle bundle = new Bundle();
        OrdersListDF soldFragmentDF = new OrdersListDF();
        MyOrdersFragmentDF myOrdersFragmentDF = new MyOrdersFragmentDF();
        switch (view.getId()){
            case R.id.today_orders_amount:
                bundle.putShort("type", (short)Utility.OrderType.TODAY.getType());
                soldFragmentDF.setArguments(bundle);
                soldFragmentDF.show(getFragmentManager(), "OrdersListDF");
                break;
            case R.id.queued_orders_amount:
                bundle.putShort("type", (short)Utility.OrderType.QUEUED.getType());
                soldFragmentDF.setArguments(bundle);
                soldFragmentDF.show(getFragmentManager(), "OrdersListDF");
                break;
            case R.id.finished_orders_amount:
                bundle.putShort("type", (short)Utility.OrderType.FINISHED.getType());
                soldFragmentDF.setArguments(bundle);
                soldFragmentDF.show(getFragmentManager(), "OrdersListDF");
                break;
                case R.id.my_all_sold_orders:
            case R.id.my_all_sold_orders_nav:
                bundle.putShort("type", (short)Utility.OrderType.MY_ALL_SOLD.getType());
                soldFragmentDF.setArguments(bundle);
                soldFragmentDF.show(getFragmentManager(), "MyOrdersFragmentDF");
                break;
                case R.id.waiting_to_pay_amount:
                bundle.putShort("type", (short)Utility.OrderType.UNPAYMENT.getType());
                myOrdersFragmentDF.setArguments(bundle);
                myOrdersFragmentDF.show(getFragmentManager(), "MyOrdersFragmentDF");
                break;
            case R.id.booked_amount:
                bundle.putShort("type", (short)Utility.OrderType.BOOKED.getType());
                myOrdersFragmentDF.setArguments(bundle);
                myOrdersFragmentDF.show(getFragmentManager(), "MyOrdersFragmentDF");
                break;
            case R.id.waiting_for_evaluation_amount:
                bundle.putShort("type", (short)Utility.OrderType.WAITING_EVALUATION.getType());
                myOrdersFragmentDF.setArguments(bundle);
                myOrdersFragmentDF.show(getFragmentManager(), "MyOrdersFragmentDF");
                break;
            case R.id.all_orders:
            case R.id.all_orders_nav:
                bundle.putShort("type", (short)Utility.OrderType.MY_ALL.getType());
                myOrdersFragmentDF.setArguments(bundle);
                myOrdersFragmentDF.show(getFragmentManager(), "MyOrdersFragmentDF");
                break;
            case R.id.get_all_orders_btn:
                bundle.putShort("type", (short)Utility.OrderType.ALL_SOLD.getType());
                soldFragmentDF.setArguments(bundle);
                soldFragmentDF.show(getFragmentManager(), "OrdersListDF");
                break;
        }

    }

    private class OrderStatusBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ORDER_PAYMENT_SUCCESS_BROADCAST:
                case ORDER_SUBMIT_BROADCAST:
                case ORDER_EVALUATE_SUCCESS_BROADCAST:
                    getMyOrdersAmount();
                    break;
            }
        }
    }
    
    private void registerBroadcast() {
        mReceiver = new OrderStatusBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ORDER_PAYMENT_SUCCESS_BROADCAST);
        intentFilter.addAction(ORDER_SUBMIT_BROADCAST);
        intentFilter.addAction(ORDER_EVALUATE_SUCCESS_BROADCAST);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, intentFilter);
    }

    //unregister local broadcast
    private void unRegisterBroadcast() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterBroadcast();
    }

    static class MyHandler extends Handler {
        WeakReference<OrderFragment> orderFragmentWeakReference;

        MyHandler(OrderFragment orderFragment) {
            orderFragmentWeakReference = new WeakReference<>(orderFragment);
        }

        @Override
        public void handleMessage(Message message) {
            OrderFragment orderFragment = orderFragmentWeakReference.get();
            if (orderFragment != null) {
                orderFragment.handleMessage(message);
            }
        }
    }

}
