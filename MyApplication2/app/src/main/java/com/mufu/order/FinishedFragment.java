package com.mufu.order;

import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mufu.R;
import com.mufu.adapter.OrderQueuedAdapter;
import com.mufu.common.MyApplication;
import com.mufu.util.BaseFragment;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.MyLinearLayoutManager;
import com.mufu.util.Slog;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

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
import static com.mufu.order.OrderDetailsDF.newInstance;
import static com.mufu.order.QueuedFragment.getOrderManager;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class FinishedFragment extends BaseFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "QueuedFragment";
    private static final int PAGE_SIZE = 8;
    private static final String GET_ALL_ORDERS = HttpUtil.DOMAIN + "?q=order_manager/get_finished_orders";
    public static final String GET_QUEUED_ORDERS_COUNT = HttpUtil.DOMAIN + "?q=order_manager/get_finished_orders_count";
    private static final int GET_ALL_DONE = 1;
    private static final int GET_ALL_END = 2;
    private static final int NO_MORE = 3;
    final int itemLimit = 1;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    private int mLoadSize = 0;
    private Handler handler;
    private OrderQueuedAdapter orderQueuedAdapter;
    private XRecyclerView recyclerView;
    private List<QueuedFragment.OrderManager> mOrderManagerList = new ArrayList<>();
    private View mView;
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.order_summary, container, false);

        mView = convertView;

        initContentView(convertView);

        requestData();

        return convertView;
    }

    private void initContentView(View view) {
        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(view.findViewById(R.id.custom_actionbar), font);
        
        handler = new MyHandler(this);
        recyclerView = view.findViewById(R.id.order_summary_list);
        orderQueuedAdapter = new OrderQueuedAdapter(getContext());
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
                    orderQueuedAdapter.setScrolling(false);
                    orderQueuedAdapter.notifyDataSetChanged();
                } else {
                    orderQueuedAdapter.setScrolling(true);
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

        orderQueuedAdapter.setItemClickListener(new OrderQueuedAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                QueuedFragment.OrderManager orderManager = mOrderManagerList.get(position);
                OrderDetailsDF orderDetailsDF;
                orderDetailsDF = newInstance(orderManager);
                //orderDetailsDF.setTargetFragment(this, ROUTE_REQUEST_CODE);
                orderDetailsDF.show(getFragmentManager(), "OrderDetailsDF");
            }
        });
        
        recyclerView.setAdapter(orderQueuedAdapter);


        //show progressImage before loading done
        progressImageView = view.findViewById(R.id.animal_progress);
        animationDrawable = (AnimationDrawable) progressImageView.getDrawable();
        progressImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        }, 50);
    }

    private void requestData() {

        final int page = mOrderManagerList.size() / PAGE_SIZE;
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
                        QueuedFragment.OrderManager orderManager = getOrderManager(orderObject);
                        mOrderManagerList.add(orderManager);
                    }
                }
            }
        }

        return orderSize;
    }
    
    public void handleMessage(Message message) {
        switch (message.what) {
            case GET_ALL_DONE:
                Slog.d(TAG, "-------------->GET_ALL_DONE");
                orderQueuedAdapter.setData(mOrderManagerList);
                orderQueuedAdapter.notifyDataSetChanged();
                recyclerView.loadMoreComplete();
                stopLoadProgress();
                break;
            case GET_ALL_END:
                Slog.d(TAG, "-------------->GET_ALL_END");
                orderQueuedAdapter.setData(mOrderManagerList);
                orderQueuedAdapter.notifyDataSetChanged();
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
    
    @Override
    protected int getLayoutId() {
        return 0;
    }

    @Override
    protected void initView(View view) {
        return;
    }

    @Override
    protected void loadData() {
        return;
    }
    
    static class MyHandler extends Handler {
        WeakReference<FinishedFragment> ordersFragmentWeakReference;

        MyHandler(FinishedFragment ordersFragment) {
            ordersFragmentWeakReference = new WeakReference<>(ordersFragment);
        }

        @Override
        public void handleMessage(Message message) {
            FinishedFragment ordersFragment = ordersFragmentWeakReference.get();
            if (ordersFragment != null) {
                ordersFragment.handleMessage(message);
            }
        }
    }
}
