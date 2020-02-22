package com.hetang.authenticate;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hetang.R;
import com.hetang.adapter.AuthenticatePassedtListAdapter;
import com.hetang.common.MyApplication;
import com.hetang.util.BaseFragment;
import com.hetang.util.HttpUtil;
import com.hetang.util.Slog;
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
import static com.hetang.authenticate.RequestFragment.GET_ALL_AUTHENTICATION_URL;
import static com.hetang.authenticate.RequestFragment.SET_AUTHENTICATION_STATUS;
import static com.hetang.authenticate.RequestFragment.parseAuthentication;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class PassedFragment extends BaseFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "RequestFragment";
    private static final int PAGE_SIZE = 5;
    private static final int LOAD_DONE = 0;
    private static final int UPDATE_DONE = 1;
    private static final int LOAD_COMPLETE_END = 2;
    private static final int LOAD_NOTHING_DONE = 3;
    private static final int OPERATION_DONE = 4;
    private int mCurrentPos;
    int page = 0;
    private int type;
    private TextView countTV;
    private int count;
    private static int PASSED = 1;
    private static int REJECTED = 2;
    private List<RequestFragment.Authentication> authenticationList = new ArrayList<>();
    private XRecyclerView xRecyclerView;
    private AuthenticatePassedtListAdapter authenticationListAdapter;
    private MyHandler handler;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.authentication, container, false);
        initView(convertView);
        return convertView;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.authentication;
    }

    protected void initView(View view) {
        handler = new MyHandler(this);
        authenticationListAdapter = new AuthenticatePassedtListAdapter(getContext());

        countTV = view.findViewById(R.id.count);
        xRecyclerView = view.findViewById(R.id.authentication_list);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(linearLayoutManager);

        xRecyclerView.setRefreshProgressStyle(BallSpinFadeLoader);
        xRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallRotate);

        xRecyclerView.getDefaultRefreshHeaderView().setRefreshTimeVisible(false);
        xRecyclerView.setPullRefreshEnabled(false);

        xRecyclerView.getDefaultFootView().setNoMoreHint(getString(R.string.no_more));
        int itemLimit = 1;
        // When the item number of the screen number is list.size-2,we call the onLoadMore
        xRecyclerView.setLimitNumberToCallLoadMore(itemLimit);
        xRecyclerView.setRefreshProgressStyle(ProgressStyle.BallBeat);
        xRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);

        xRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    authenticationListAdapter.setScrolling(false);
                    authenticationListAdapter.notifyDataSetChanged();
                } else {
                    authenticationListAdapter.setScrolling(true);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        xRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
            }

            @Override
            public void onLoadMore() {
                loadData();
            }
        });

        authenticationListAdapter.setOnItemClickListener(new AuthenticateOperationInterface() {
            @Override
            public void onPassClick(View view, int position) {
                mCurrentPos = position;
                RequestFragment.Authentication authentication = authenticationList.get(position);
                authenticationOperation(authentication.aid, authentication.getUid(), PASSED);
            }

            @Override
            public void onRejectClick(View view, int position) {
                mCurrentPos = position;
                RequestFragment.Authentication authentication = authenticationList.get(position);
                authenticationOperation(authentication.aid, authentication.getUid(), REJECTED);
            }
        });

        xRecyclerView.setAdapter(authenticationListAdapter);

        loadData();

    }

    private void authenticationOperation(int aid, int uid, int status){
        showProgressDialog(getActivity(), "...");
        RequestBody requestBody = new FormBody.Builder()
                .add("aid", String.valueOf(aid))
                .add("uid", String.valueOf(uid))
                .add("status", String.valueOf(status))
                .build();

        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), SET_AUTHENTICATION_STATUS, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========load response text : " + responseText);
                    if (responseText != null) {
                        try {
                            JSONObject responseObject = new JSONObject(responseText);
                            int status = responseObject.optInt("status");
                            if (status > 0){
                                dismissProgressDialog();
                                handler.sendEmptyMessage(OPERATION_DONE);
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

    protected void loadData() {
        page = authenticationList.size() / PAGE_SIZE;
        RequestBody requestBody = new FormBody.Builder()
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();

        String url = GET_ALL_AUTHENTICATION_URL + "passed";

        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), url, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========load response text : " + responseText);
                    if (responseText != null) {
                        int itemCount = processResponseText(responseText);
                        if (itemCount > 0) {
                            if (itemCount < PAGE_SIZE) {

                                handler.sendEmptyMessage(LOAD_COMPLETE_END);
                            } else {
                                handler.sendEmptyMessage(LOAD_DONE);
                            }
                        } else {
                            handler.sendEmptyMessage(LOAD_NOTHING_DONE);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }

    private int processResponseText(String responseText) {
        JSONArray authenticationArray;
        JSONObject responseObject;
        try {
            responseObject = new JSONObject(responseText);
            count = responseObject.optInt("count");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    countTV.setText(String.valueOf(count));
                }
            });
            authenticationArray = responseObject.optJSONArray("results");

            if (isDebug)
                Slog.d(TAG, "------------------------------>authenticationArray: " + authenticationArray);
            if (authenticationArray != null && authenticationArray.length() > 0) {
                for (int i = 0; i < authenticationArray.length(); i++) {
                    JSONObject authenticationObject = authenticationArray.getJSONObject(i);
                    RequestFragment.Authentication authentication = parseAuthentication(authenticationObject);
                    authenticationList.add(authentication);
                }

                return authenticationArray.length();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void handleMessage(Message message) {
        switch (message.what) {
            case LOAD_DONE:
                authenticationListAdapter.setData(authenticationList, PASSED);
                authenticationListAdapter.notifyDataSetChanged();
                xRecyclerView.refreshComplete();
                //xRecyclerView.loadMoreComplete();
                break;

            case LOAD_COMPLETE_END:
                authenticationListAdapter.setData(authenticationList, PASSED);
                authenticationListAdapter.notifyDataSetChanged();
                xRecyclerView.refreshComplete();
                xRecyclerView.loadMoreComplete();
                xRecyclerView.setNoMore(true);
                break;
            case LOAD_NOTHING_DONE:
                //xRecyclerView.refreshComplete();
                xRecyclerView.setNoMore(true);
                xRecyclerView.loadMoreComplete();
                break;
            case OPERATION_DONE:
                countTV.setText(String.valueOf(count - 1));
                authenticationList.remove(mCurrentPos);
                authenticationListAdapter.setData(authenticationList, PASSED);
                authenticationListAdapter.notifyItemRemoved(mCurrentPos);
                authenticationListAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    static class MyHandler extends Handler {
        WeakReference<PassedFragment> notificationFragmentWeakReference;

        MyHandler(PassedFragment notificationFragment) {
            notificationFragmentWeakReference = new WeakReference<>(notificationFragment);
        }

        @Override
        public void handleMessage(Message message) {
            PassedFragment notificationFragment = notificationFragmentWeakReference.get();
            if (notificationFragment != null) {
                notificationFragment.handleMessage(message);
            }
        }
    }
}
