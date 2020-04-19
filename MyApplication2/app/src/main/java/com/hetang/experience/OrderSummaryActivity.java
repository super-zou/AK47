package com.hetang.experience;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hetang.R;
import com.hetang.adapter.GuideSummaryAdapter;
import com.hetang.adapter.OrderSummaryAdapter;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.common.MyApplication;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.MyLinearLayoutManager;
import com.hetang.util.Slog;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

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
import static com.hetang.common.MyApplication.getContext;
import static com.hetang.experience.OrderDetailsDF.newInstance;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class OrderSummaryActivity extends BaseAppCompatActivity {
    private static final boolean isDebug = true;
    private static final String TAG = "OrderSummaryActivity";
    private static final int PAGE_SIZE = 8;
    private static final String GET_ALL_ORDERS = HttpUtil.DOMAIN + "?q=order_manager/get_all_orders";
    public static final String GET_MY_ORDERS_COUNT = HttpUtil.DOMAIN + "?q=order_manager/get_my_orders_count";
    private static final int GET_ALL_DONE = 1;
    private static final int GET_ALL_END = 2;
    private static final int NO_MORE = 3;
    final int itemLimit = 1;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    private int mLoadSize = 0;
    private Handler handler;
    private OrderSummaryAdapter orderSummaryAdapter;
    private XRecyclerView recyclerView;
    private List<Order> mOrderList = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_summary);

        initView();

        loadData();
    }

    private void initView() {
        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.custom_actionbar), font);

        handler = new OrderSummaryActivity.MyHandler(this);
        recyclerView = findViewById(R.id.order_summary_list);
        orderSummaryAdapter = new OrderSummaryAdapter(getContext());
        MyLinearLayoutManager linearLayoutManager = new MyLinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        
        recyclerView.setRefreshProgressStyle(BallSpinFadeLoader);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallRotate);

        recyclerView.setPullRefreshEnabled(false);
        recyclerView.getDefaultRefreshHeaderView().setRefreshTimeVisible(true);
        recyclerView.getDefaultFootView().setLoadingHint(getString(R.string.loading_pull_up_tip));
        recyclerView.getDefaultFootView().setNoMoreHint(getString(R.string.no_more_order));
        
        recyclerView.setLimitNumberToCallLoadMore(itemLimit);
        recyclerView.setRefreshProgressStyle(ProgressStyle.BallBeat);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    orderSummaryAdapter.setScrolling(false);
                    orderSummaryAdapter.notifyDataSetChanged();
                } else {
                    orderSummaryAdapter.setScrolling(true);
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
                loadData();
            }
        });
        
        orderSummaryAdapter.setItemClickListener(new OrderSummaryAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Order order = mOrderList.get(position);
                OrderDetailsDF orderDetailsDF;
                orderDetailsDF = newInstance(order);
                //orderDetailsDF.setTargetFragment(this, ROUTE_REQUEST_CODE);
                orderDetailsDF.show(getSupportFragmentManager(), "RouteItemEditDF");
            }
        });

        recyclerView.setAdapter(orderSummaryAdapter);
        
        TextView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //show progressImage before loading done
        progressImageView = findViewById(R.id.animal_progress);
        animationDrawable = (AnimationDrawable) progressImageView.getDrawable();
        progressImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        }, 50);
        
        FloatingActionButton floatingActionButton = findViewById(R.id.create_activity);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void loadData() {

        final int page = mOrderList.size() / PAGE_SIZE;
        RequestBody requestBody = new FormBody.Builder()
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();
                
       HttpUtil.sendOkHttpRequest(getContext(), GET_ALL_ORDERS, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========loadData response text : " + responseText);
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
                        Order order = getOrder(orderObject);
                        mOrderList.add(order);
                    }
                }
            }
        }

        return orderSize;
    }
    
    public static Order getOrder(JSONObject orderObject) {
        Order order = new Order();
        if (orderObject != null) {
            order.oid = orderObject.optInt("oid");
            order.id = orderObject.optInt("id");
            order.city = orderObject.optString("city");
            order.headPictureUrl = orderObject.optString("picture_url");
            order.actualPayment = orderObject.optInt("payment");
            order.created = orderObject.optInt("created");
            order.money = orderObject.optInt("amount");
            order.title = orderObject.optString("title");
            order.unit = orderObject.optString("unit");
            order.status = orderObject.optInt("status");
            order.appointmentDate = orderObject.optString("date_string");
            order.paymentTime = orderObject.optInt("payment_time");
        }

        return order;
    }
    
    public static class Order implements Serializable {
        public int oid;
        public int id;
        public String headPictureUrl;
        public int status;
        public String title;
        public String city;
        public int created;
        public int paymentTime;
        public int money;
        public String unit;
        public int actualPayment;
        public String appointmentDate;
    }
    
     public void handleMessage(Message message) {
        switch (message.what) {
            case GET_ALL_DONE:
                Slog.d(TAG, "-------------->GET_ALL_DONE");
                orderSummaryAdapter.setData(mOrderList);
                orderSummaryAdapter.notifyDataSetChanged();
                recyclerView.loadMoreComplete();
                stopLoadProgress();
                break;
            case GET_ALL_END:
                Slog.d(TAG, "-------------->GET_ALL_END");
                orderSummaryAdapter.setData(mOrderList);
                orderSummaryAdapter.notifyDataSetChanged();
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
    
    static class MyHandler extends Handler {
        WeakReference<OrderSummaryActivity> guideSummaryActivityWeakReference;

        MyHandler(OrderSummaryActivity guideSummaryActivity) {
            guideSummaryActivityWeakReference = new WeakReference<>(guideSummaryActivity);
        }

        @Override
        public void handleMessage(Message message) {
            OrderSummaryActivity guideSummaryActivity = guideSummaryActivityWeakReference.get();
            if (guideSummaryActivity != null) {
                guideSummaryActivity.handleMessage(message);
            }
        }
    }
}
        
