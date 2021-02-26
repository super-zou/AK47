package com.mufu.order;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mufu.R;
import com.mufu.adapter.OrdersListAdapter;
import com.mufu.common.MyApplication;
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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.mufu.common.MyApplication.getContext;
import static com.mufu.order.OrderDetailsDF.newInstance;
import static com.mufu.util.DateUtil.timeStampToDay;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class OrdersListDF extends BaseDialogFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "OrdersListDF";
    private static final int PAGE_SIZE = 8;
    private static final String GET_TODAY_ORDERS = HttpUtil.DOMAIN + "?q=order_manager/get_today_orders";
    private static final String GET_QUEUED_ORDERS = HttpUtil.DOMAIN + "?q=order_manager/get_queued_orders";
    public static final String GET_FINISHED_ORDERS = HttpUtil.DOMAIN + "?q=order_manager/get_finished_orders";
    private static final String GET_ALL_ORDERS = HttpUtil.DOMAIN + "?q=order_manager/get_all_orders";
    private static final String GET_MY_ALL_SOLD_ORDERS = HttpUtil.DOMAIN + "?q=order_manager/get_my_all_sold_orders";
    private static final String GET_CURRENT_REFUND_ORDERS = HttpUtil.DOMAIN + "?q=order_manager/get_current_refund_orders";
    private static final String UPDATE_REFUND_STATUS_URL = HttpUtil.DOMAIN + "?q=order_manager/update_refund_status";
    private static final int GET_ALL_DONE = 1;
    private static final int GET_ALL_END = 2;
    private static final int NO_MORE = 3;
    final int itemLimit = 1;
    private static final int UPDATE_REFUND_STATUS_DONE = 4;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    private int mLoadSize = 0;
    private Handler handler;
    private OrdersListAdapter ordersListAdapter;
    private XRecyclerView recyclerView;
    private List<OrderManager> mOrderManagerList = new ArrayList<>();
    private View mView;
    private Dialog mDialog;
    private Window window;
    private short mType;
    public static final String ORDER_REFUND_SUCCESS_BROADCAST = "com.mufu.action.ORDER_REFUND_SUCCESS";

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
            mType = bundle.getShort("type");
            Utility.OrderType orderType = Utility.OrderType.getOrderType(mType);
            switch (orderType){
                case QUEUED:
                    titleTV.setText(getContext().getResources().getString(R.string.waiting_for_development));
                    break;
                case FINISHED:
                    titleTV.setText(getContext().getResources().getString(R.string.finished_orders));
                    break;
                case TODAY:
                    titleTV.setText(getContext().getResources().getString(R.string.today_development));
                    break;
                case MY_ALL_SOLD:
                case ALL_SOLD:
                    titleTV.setText(getContext().getResources().getString(R.string.all_orders));
                    break;
                case REFUNDED:
                    titleTV.setText(getContext().getResources().getString(R.string.refund_orders));
                    break;
            }
            initContentView();
            requestData();
        }

        return mDialog;
    }
    
    private void initContentView() {
        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.left_back), font);

        handler = new MyHandler(this);
        recyclerView = mDialog.findViewById(R.id.order_summary_list);
        if (mType != Utility.OrderType.REFUNDED.ordinal()){
            ordersListAdapter = new OrdersListAdapter(getContext(), false);
        }else {
            ordersListAdapter = new OrdersListAdapter(getContext(), true);
        }
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
                    ordersListAdapter.setScrolling(false);
                    ordersListAdapter.notifyDataSetChanged();
                } else {
                    ordersListAdapter.setScrolling(true);
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
                requestData();
            }
        });
        
        ordersListAdapter.setItemClickListener(new OrdersListAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                OrderManager orderManager = mOrderManagerList.get(position);
                OrderDetailsDF orderDetailsDF;
                orderDetailsDF = newInstance(orderManager, mType);
                //orderDetailsDF.setTargetFragment(this, ROUTE_REQUEST_CODE);
                orderDetailsDF.show(getFragmentManager(), "OrderDetailsDF");
            }
        }, new OrdersListAdapter.RefundConfirmBtnClickListener() {
            @Override
            public void onRefundConfirmBtnClick(View view, int position) {
                updateRefundStatus(position);
            }
        }, new OrdersListAdapter.OnIdentityInfoClickListener() {
            @Override
            public void onIdentityInfo(View view, int position) {
                OrderManager orderManager = mOrderManagerList.get(position);
                ParticipantIdentityInformationDF participantIdentityInformationDF = ParticipantIdentityInformationDF.newInstance(orderManager.oid);
                participantIdentityInformationDF.show(getFragmentManager(), "ParticipantIdentityInformationDF");
            }
        });
        
        recyclerView.setAdapter(ordersListAdapter);
        
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
    
        private void updateRefundStatus(final int position){
        showProgressDialog("");
        RequestBody requestBody = new FormBody.Builder()
                .add("oid", String.valueOf(mOrderManagerList.get(position).oid))
                .build();
            
            HttpUtil.sendOkHttpRequest(getContext(), UPDATE_REFUND_STATUS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    Slog.d(TAG, "==========updateRefundStatus response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject ordersResponse = null;
                        try {
                            ordersResponse = new JSONObject(responseText);
                            if (ordersResponse != null) {
                                int result = ordersResponse.optInt("result");
                                dismissProgressDialog();
                                if (result > 0){
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("position", position);
                                    Message message = new Message();
                                    message.setData(bundle);
                                    message.what = UPDATE_REFUND_STATUS_DONE;
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

private void requestData() {

        final int page = mOrderManagerList.size() / PAGE_SIZE;
        RequestBody requestBody = new FormBody.Builder()
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();

        String url = GET_QUEUED_ORDERS;

        Utility.OrderType orderType = Utility.OrderType.getOrderType(mType);
        switch (orderType){
            case TODAY:
                url = GET_TODAY_ORDERS;
                break;
            case QUEUED:
                url = GET_QUEUED_ORDERS;
                break;
            case FINISHED:
                url = GET_FINISHED_ORDERS;
                break;
            case ALL_SOLD:
                url = GET_ALL_ORDERS;
                break;
            case MY_ALL_SOLD:
                url = GET_MY_ALL_SOLD_ORDERS;
                break;
            case REFUNDED:
                url = GET_CURRENT_REFUND_ORDERS;
                break;
        }
        
        HttpUtil.sendOkHttpRequest(getContext(), url, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========loadData response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject ordersResponse = null;
                        try {
                            ordersResponse = new JSONObject(responseText);
                            if (ordersResponse != null) {
                                mLoadSize = processOrdersResponse(ordersResponse);
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
                        OrderManager orderManager = getOrderManager(orderObject);
                        mOrderManagerList.add(orderManager);
                    }
                }
            }
        }

        return orderSize;
    }
    
    public static class OrderManager extends MyOrdersFragmentDF.Order {
        public String nickname;
        public String avatar;
        public int sex;
        public int uid;
        public String phone;
        public String appointMentDate;
        public String university;
        public String major;
        //for refund info
        public String payName;
        public String payAccount;
        public Float refundAmount;
        public String reason;
        public int refundCreated;
    }

    public OrderManager getOrderManager(JSONObject orderObject) {
        OrderManager orderManager = new OrderManager();
        if (orderObject != null) {
            orderManager.oid = orderObject.optInt("oid");
            orderManager.id = orderObject.optInt("id");
            orderManager.sex = orderObject.optInt("sex");
            orderManager.number = orderObject.optString("number");
            orderManager.city = orderObject.optString("city");
            orderManager.headPictureUrl = orderObject.optString("picture_url");
            orderManager.actualPayment = orderObject.optInt("actual_payment");
            orderManager.totalPrice = orderObject.optInt("total_price");
            orderManager.created = orderObject.optInt("created");
            orderManager.price = orderObject.optInt("price");
            orderManager.amount = orderObject.optInt("amount");
            orderManager.title = orderObject.optString("title");
            orderManager.packageName = orderObject.optString("package_name");
            orderManager.unit = orderObject.optString("unit");
            orderManager.status = orderObject.optInt("status");
            orderManager.appointmentDate = timeStampToDay(orderObject.optInt("date"));
            orderManager.paymentTime = orderObject.optInt("payment_time");
            orderManager.type = orderObject.optInt("type");
            orderManager.orderClass = orderObject.optInt("class");
            orderManager.nickname = orderObject.optString("nickname");
            orderManager.uid = orderObject.optInt("uid");
            orderManager.avatar = orderObject.optString("avatar");
            orderManager.phone = orderObject.optString("account");
            orderManager.appointMentDate = timeStampToDay(orderObject.optInt("date"));
            orderManager.university = orderObject.optString("university");
            orderManager.major = orderObject.optString("major");
            orderManager.identityRequired = orderObject.optInt("identity_required");
            if (mType == Utility.OrderType.REFUNDED.ordinal()){
                orderManager.payName = orderObject.optString("pay_name");
                orderManager.payAccount = orderObject.optString("pay_account");
                orderManager.refundAmount = (float)orderObject.optDouble("refund_amount");
                orderManager.reason = orderObject.optString("reason");
                orderManager.refundCreated = orderObject.optInt("refund_created");
            }
        }

       return orderManager;
}

public void handleMessage(Message message) {
        switch (message.what) {
            case GET_ALL_DONE:
                Slog.d(TAG, "-------------->GET_ALL_DONE");
                ordersListAdapter.setData(mOrderManagerList);
                ordersListAdapter.notifyDataSetChanged();
                recyclerView.loadMoreComplete();
                stopLoadProgress();
                break;
                case GET_ALL_END:
                Slog.d(TAG, "-------------->GET_ALL_END");
                ordersListAdapter.setData(mOrderManagerList);
                ordersListAdapter.notifyDataSetChanged();
                recyclerView.loadMoreComplete();
                recyclerView.setNoMore(true);
                stopLoadProgress();
                break;
            case NO_MORE:
                Slog.d(TAG, "-------------->NO_MORE");
                if (recyclerView != null){
                    recyclerView.setNoMore(true);
                    recyclerView.loadMoreComplete();
                }
                stopLoadProgress();
                break;
            case UPDATE_REFUND_STATUS_DONE:
                Bundle bundle = message.getData();
                int position = bundle.getInt("position");
                Toast.makeText(getContext(), "退款成功", Toast.LENGTH_SHORT).show();
                mOrderManagerList.remove(position);
                ordersListAdapter.setData(mOrderManagerList);
                ordersListAdapter.notifyItemRangeRemoved(position, 1);
                ordersListAdapter.notifyDataSetChanged();
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(ORDER_REFUND_SUCCESS_BROADCAST));
                break;
            default:
                break;
        }
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
        WeakReference<OrdersListDF> queuedFragmentDFWeakReference;

        MyHandler(OrdersListDF queuedFragmentDF) {
            queuedFragmentDFWeakReference = new WeakReference<>(queuedFragmentDF);
        }

        @Override
        public void handleMessage(Message message) {
            OrdersListDF queuedFragmentDF = queuedFragmentDFWeakReference.get();
            if (queuedFragmentDF != null) {
                queuedFragmentDF.handleMessage(message);
            }
        }
    }
}
