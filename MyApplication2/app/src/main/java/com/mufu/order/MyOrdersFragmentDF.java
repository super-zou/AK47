package com.mufu.order;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mufu.R;
import com.mufu.adapter.MyOrderSummaryAdapter;
import com.mufu.common.MyApplication;
import com.mufu.experience.ExperienceEvaluateDialogFragment;
import com.mufu.util.BaseDialogFragment;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.MyLinearLayoutManager;
import com.mufu.util.Slog;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.mufu.util.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.mufu.order.OrderDetailsDF.newInstance;
import static com.mufu.order.PlaceOrderDF.ORDER_EVALUATE_SUCCESS_BROADCAST;
import static com.mufu.order.PlaceOrderDF.ORDER_PAYMENT_SUCCESS_BROADCAST;
import static com.mufu.order.PlaceOrderDF.ORDER_SUBMIT_BROADCAST;
import static com.mufu.util.DateUtil.timeStampToDay;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class MyOrdersFragmentDF extends BaseDialogFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "MyOrdersFragmentDF";
    private static final int PAGE_SIZE = 8;
    private static final String GET_MY_ALL_ORDERS = HttpUtil.DOMAIN + "?q=order_manager/get_my_all_orders";
    private static final String GET_MY_UNPAID_ORDERS = HttpUtil.DOMAIN + "?q=order_manager/get_my_unpaid_orders";
    private static final String GET_MY_BOOKED_ORDERS = HttpUtil.DOMAIN + "?q=order_manager/get_my_booked_orders";
    private static final String GET_MY_REFUND_ORDERS = HttpUtil.DOMAIN + "?q=order_manager/get_my_refund_orders";
    private static final String GET_WAITING_FOR_MY_EVALUATION_ORDERS = HttpUtil.DOMAIN + "?q=order_manager/get_waiting_for_my_evaluation_orders";
    private static final int GET_ALL_DONE = 1;
    private static final int GET_ALL_END = 2;
    private static final int NO_MORE = 3;
    final int itemLimit = 1;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    private int mLoadSize = 0;
    private Handler handler;
    private MyOrderSummaryAdapter myOrderSummaryAdapter;
    private XRecyclerView recyclerView;
    private List<OrdersListDF.OrderManager> mOrderList = new ArrayList<>();
    private View mView;
    private OrderStatusBroadcastReceiver mReceiver;
        private Dialog mDialog;
    private Window window;
    private short mOrderType;
    private int mItemPosition;
    
        @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_MaterialComponents_DialogWhenLarge);
        mDialog.setContentView(R.layout.order_summary);

        mDialog.setCanceledOnTouchOutside(true);
        window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);
        
        TextView leftBackTV = mDialog.findViewById(R.id.left_back);
        leftBackTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        TextView titleTV = mDialog.findViewById(R.id.title);

        Bundle bundle = getArguments();
        
        if (bundle != null){
            short type = bundle.getShort("type");
            Slog.d(TAG, "--------------type: "+type);
            mOrderType = type;
            Utility.OrderType orderType = Utility.OrderType.getOrderType(type);
            switch (orderType){
                case UNPAYMENT:
                    titleTV.setText(getContext().getResources().getString(R.string.unpaid));
                    break;
                    case BOOKED:
                    titleTV.setText(getContext().getResources().getString(R.string.booked_orders));
                    break;
                case WAITING_EVALUATION:
                    titleTV.setText(getContext().getResources().getString(R.string.waiting_for_evaluation_orders));
                    break;
                case APPLYING_REFUND:
                case REFUNDED:
                    titleTV.setText(getContext().getResources().getString(R.string.refund_orders));
                    break;
                case MY_ALL:
                    titleTV.setText(getContext().getResources().getString(R.string.all_orders));
                    break;
            }
            initContentView(type);
            requestData(type);
        }

        mReceiver = new OrderStatusBroadcastReceiver();

        registerBroadcast();

        return mDialog;
    }
    
    private void initContentView(short type) {
        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);
        handler = new MyHandler(this);
        recyclerView = mDialog.findViewById(R.id.order_summary_list);
        myOrderSummaryAdapter = new MyOrderSummaryAdapter(getContext());
        MyLinearLayoutManager linearLayoutManager = new MyLinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setRefreshProgressStyle(BallSpinFadeLoader);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallRotate);

        recyclerView.setPullRefreshEnabled(false);
        recyclerView.getDefaultRefreshHeaderView().setRefreshTimeVisible(true);
        recyclerView.getDefaultFootView().setLoadingHint(getString(R.string.loading_pull_up_tip));
        recyclerView.getDefaultFootView().setNoMoreHint(getString(R.string.no_more_order));
        
        // When the item number of the screen number is list.size-2,we call the onLoadMore
        recyclerView.setLimitNumberToCallLoadMore(itemLimit);
        recyclerView.setRefreshProgressStyle(ProgressStyle.BallBeat);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    myOrderSummaryAdapter.setScrolling(false);
                    myOrderSummaryAdapter.notifyDataSetChanged();
                } else {
                    myOrderSummaryAdapter.setScrolling(true);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        
        recyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                //updateData();
            }

            @Override
            public void onLoadMore() {
                requestData(type);
            }
        });
        
        myOrderSummaryAdapter.setItemClickListener(new MyOrderSummaryAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                OrdersListDF.OrderManager order = mOrderList.get(position);
                OrderDetailsDF orderDetailsDF;
                orderDetailsDF = newInstance(order, mOrderType);
                //orderDetailsDF.setTargetFragment(this, ROUTE_REQUEST_CODE);
                orderDetailsDF.show(getFragmentManager(), "RouteItemEditDF");
            }
        }, new MyOrderSummaryAdapter.EvaluateClickListener() {
            @Override
            public void onEvaluateClick(View view, int position) {
                mItemPosition = position;
                Order order = mOrderList.get(position);
                ExperienceEvaluateDialogFragment experienceEvaluateDialogFragment;
                experienceEvaluateDialogFragment = ExperienceEvaluateDialogFragment.newInstance(order);
                //orderDetailsDF.setTargetFragment(this, ROUTE_REQUEST_CODE);
             experienceEvaluateDialogFragment.show(getFragmentManager(), "ExperienceEvaluateDialogFragment");
            }
        }, new MyOrderSummaryAdapter.PayClickListener() {
            @Override
            public void onPayClick(View view, int position) {
                mItemPosition = position;
                Order order = mOrderList.get(position);
                OrderPaymentDF orderPaymentDF = OrderPaymentDF.newInstance(order);
                orderPaymentDF.show(getFragmentManager(), "OrderPaymentDF");
            }
        });

        recyclerView.setAdapter(myOrderSummaryAdapter);


        //show progressImage before loading done
        progressImageView = mDialog.findViewById(R.id.animal_progress);
        animationDrawable = (AnimationDrawable) progressImageView.getDrawable();
        progressImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        }, 50);
    }
    
    private void requestData(short type) {

        final int page = mOrderList.size() / PAGE_SIZE;
        RequestBody requestBody = new FormBody.Builder()
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();
        
        String url = "";
        Utility.OrderType orderType = Utility.OrderType.getOrderType(type);
        switch (orderType){
            case BOOKED:
                url = GET_MY_BOOKED_ORDERS;
                break;
            case UNPAYMENT:
                url = GET_MY_UNPAID_ORDERS;
                break;
            case WAITING_EVALUATION:
                url = GET_WAITING_FOR_MY_EVALUATION_ORDERS;
                break;
            case APPLYING_REFUND:
            case REFUNDED:
                url = GET_MY_REFUND_ORDERS;
                break;
            case MY_ALL:
                url = GET_MY_ALL_ORDERS;
                break;
        }

        HttpUtil.sendOkHttpRequest(getContext(), url, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
            if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========requestData response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject guidesResponse = null;
                        try {
                            guidesResponse = new JSONObject(responseText);
                            if (guidesResponse != null) {
                                mLoadSize = processOrdersResponse(guidesResponse);

                                if (mLoadSize == PAGE_SIZE) {
                                    handler.sendEmptyMessage(GET_ALL_DONE);
                                    } else {
                                    if (mLoadSize != 0) {
                                        handler.sendEmptyMessage(GET_ALL_END);
                                    } else {
                                        handler.sendEmptyMessage(NO_MORE);
                                    }
                                }
                            } else {
                                handler.sendEmptyMessage(NO_MORE);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        
                        } else {
                        handler.sendEmptyMessage(NO_MORE);
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
    public int processOrdersResponse(JSONObject ordersObject) {
        int orderSize = 0;
        JSONArray orderArray = null;

        if (ordersObject != null) {
            orderArray = ordersObject.optJSONArray("orders");
            Slog.d(TAG, "------------------->processOrdersResponse: "+orderArray);
        }

        if (orderArray != null) {
            orderSize = orderArray.length();
            if (orderSize > 0) {
                for (int i = 0; i < orderArray.length(); i++) {
                    JSONObject orderObject = orderArray.optJSONObject(i);
                    if (orderObject != null) {
                        OrdersListDF.OrderManager order = getOrder(orderObject);
                        mOrderList.add(order);
                    }
                }
            }
        }

        return orderSize;
    }
    
     public static OrdersListDF.OrderManager getOrder(JSONObject orderObject) {
        OrdersListDF.OrderManager order = new OrdersListDF.OrderManager();
        if (orderObject != null) {
            order.oid = orderObject.optInt("oid");
            order.id = orderObject.optInt("id");
            order.number = orderObject.optString("number");
            order.city = orderObject.optString("city");
            order.headPictureUrl = orderObject.optString("picture_url");
            order.actualPayment = orderObject.optInt("actual_payment");
            order.totalPrice = orderObject.optInt("total_price");
            order.created = orderObject.optInt("created");
            order.price = orderObject.optInt("price");
            order.amount = orderObject.optInt("amount");
            order.title = orderObject.optString("title");
            order.packageName = orderObject.optString("package_name");
            order.unit = orderObject.optString("unit");
            order.status = orderObject.optInt("status");
            order.appointmentDate = timeStampToDay(orderObject.optInt("date"));
            order.paymentTime = orderObject.optInt("payment_time");
            order.type = orderObject.optInt("type");
            order.orderClass = orderObject.optInt("class");
        }

        return order;
    }

    public static class Order implements Serializable {
        public int oid;
        public int id;
        public String number;
        public String headPictureUrl;
        public int status;
        public String title;
        public String packageName;
        public String city;
        public int created;
        public int paymentTime;
        public float price;
        public int amount = 1;
        public String unit;
        public float actualPayment;
        public float totalPrice = 0;
        public String appointmentDate;
        public int type;
        public int orderClass = Utility.OrderClass.NORMAL.ordinal();
    }
    
    public void handleMessage(Message message) {
        switch (message.what) {
            case GET_ALL_DONE:
                Slog.d(TAG, "-------------->GET_ALL_DONE");
                myOrderSummaryAdapter.setData(mOrderList);
                myOrderSummaryAdapter.notifyDataSetChanged();
                recyclerView.loadMoreComplete();
                stopLoadProgress();
                break;
            case GET_ALL_END:
                Slog.d(TAG, "-------------->GET_ALL_END");
                myOrderSummaryAdapter.setData(mOrderList);
                myOrderSummaryAdapter.notifyDataSetChanged();
                recyclerView.loadMoreComplete();
                recyclerView.setNoMore(true);
                stopLoadProgress();
                break;
            case NO_MORE:
                Slog.d(TAG, "-------------->NO_MORE");
                recyclerView.setNoMore(true);
                recyclerView.loadMoreComplete();
                stopLoadProgress();
             break;
            default:
                break;
        }
    }
    
    private class OrderStatusBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ORDER_PAYMENT_SUCCESS_BROADCAST:
                    mOrderList.get(mItemPosition).status = 1;
                    myOrderSummaryAdapter.notifyDataSetChanged();
                    break;
                case ORDER_SUBMIT_BROADCAST:
                    //mOrderList.clear();
                    //recyclerView.reset();
                    //requestData();
                    break;
                case ORDER_EVALUATE_SUCCESS_BROADCAST:
                    mOrderList.get(mItemPosition).status = 3;
                    myOrderSummaryAdapter.notifyDataSetChanged();
                    break;
            }
        }
    }
    
    private void registerBroadcast() {
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

    private void stopLoadProgress() {
        if (progressImageView.getVisibility() == View.VISIBLE) {
            animationDrawable.stop();
            progressImageView.setVisibility(View.GONE);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (recyclerView != null) {
            recyclerView.destroy();
            recyclerView = null;
        }
        
        unRegisterBroadcast();
    }
       
    static class MyHandler extends Handler {
        WeakReference<MyOrdersFragmentDF> myOrdersFragmentDFWeakReference;

        MyHandler(MyOrdersFragmentDF myOrdersFragmentDF) {
            myOrdersFragmentDFWeakReference = new WeakReference<>(myOrdersFragmentDF);
        }

        @Override
        public void handleMessage(Message message) {
            MyOrdersFragmentDF myOrdersFragmentDF = myOrdersFragmentDFWeakReference.get();
            if (myOrdersFragmentDF != null) {
                myOrdersFragmentDF.handleMessage(message);
            }
        }
    }
}
