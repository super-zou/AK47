package com.hetang.verify.user;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hetang.R;
import com.hetang.adapter.verify.UserRejectedListAdapter;
import com.hetang.verify.VerifyOperationInterface;
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
import static com.hetang.verify.user.UserPassedFragment.LOAD_AUTHENTICATE_DONE;
import static com.hetang.verify.user.UserPassedFragment.getAuthenticateById;
import static com.hetang.verify.user.UserRequestFragment.GET_ALL_AUTHENTICATION_URL;
import static com.hetang.verify.user.UserRequestFragment.SET_AUTHENTICATION_STATUS;
import static com.hetang.verify.user.UserRequestFragment.parseAuthentication;
import static com.hetang.verify.VerifyActivity.USER_VERIFY_REJECT_BROADCAST;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class UserRejectedFragment extends BaseFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "UserRejectedFragment";
    private static final int PAGE_SIZE = 5;
    private static final int LOAD_DONE = 0;
    private static final int UPDATE_DONE = 1;
    private static final int LOAD_COMPLETE_END = 2;
    private static final int LOAD_NOTHING_DONE = 3;
    private static final int OPERATION_DONE = 4;
    private int mCurrentPos;
    int page = 0;
    private int type;
    private int count;
    private static int PASSED = 1;
    private static int REJECTED = 2;
    private List<UserRequestFragment.Authentication> authenticationList = new ArrayList<>();
    private XRecyclerView xRecyclerView;
    private UserRejectedListAdapter authenticationListAdapter;
    private MyHandler handler;
    private UserRejectedBroadcastReceiver mReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.user_verify_status, container, false);
        initView(convertView);
        registerBroadcast();
        return convertView;
    }


    @Override
    protected int getLayoutId() {
        return 0;
    }

    @Override
    protected void loadData() { }

    protected void initView(View view) {
        handler = new MyHandler(this);
        authenticationListAdapter = new UserRejectedListAdapter(getContext(), this);

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
                requestData();
            }
        });

        authenticationListAdapter.setOnItemClickListener(new VerifyOperationInterface() {
            @Override
            public void onPassClick(View view, int position) {
                mCurrentPos = position;
                UserRequestFragment.Authentication authentication = authenticationList.get(position);
                authenticationOperation(authentication.aid, authentication.getUid(), PASSED);
            }

            @Override
            public void onRejectClick(View view, int position) {
                mCurrentPos = position;
                UserRequestFragment.Authentication authentication = authenticationList.get(position);
                authenticationOperation(authentication.aid, authentication.getUid(), REJECTED);
            }
        });

        xRecyclerView.setAdapter(authenticationListAdapter);

        requestData();

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

    protected void requestData() {
        page = authenticationList.size() / PAGE_SIZE;
        RequestBody requestBody = new FormBody.Builder()
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();

        String url = GET_ALL_AUTHENTICATION_URL + "rejected";

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

            authenticationArray = responseObject.optJSONArray("results");

            if (isDebug)
                Slog.d(TAG, "------------------------------>authenticationArray: " + authenticationArray);
            if (authenticationArray != null && authenticationArray.length() > 0) {
                for (int i = 0; i < authenticationArray.length(); i++) {
                    JSONObject authenticationObject = authenticationArray.getJSONObject(i);
                    UserRequestFragment.Authentication authentication = parseAuthentication(authenticationObject);
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
                authenticationListAdapter.setData(authenticationList, REJECTED);
                authenticationListAdapter.notifyDataSetChanged();
                xRecyclerView.refreshComplete();
                //xRecyclerView.loadMoreComplete();
                break;
            case LOAD_COMPLETE_END:
                authenticationListAdapter.setData(authenticationList, REJECTED);
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
                authenticationList.remove(mCurrentPos);
                authenticationListAdapter.setData(authenticationList, REJECTED);
                authenticationListAdapter.notifyItemRemoved(mCurrentPos);
                authenticationListAdapter.notifyDataSetChanged();
                break;
            case LOAD_AUTHENTICATE_DONE:
                Slog.d(TAG, "--------------------->LOAD_AUTHENTICATE_DONE");
                Bundle bundle = message.getData();
                UserRequestFragment.Authentication authentication = (UserRequestFragment.Authentication)bundle.getSerializable("authentication");
                authenticationList.add(0, authentication);
                authenticationListAdapter.setData(authenticationList, REJECTED);
                authenticationListAdapter.notifyItemInserted(0);
                authenticationListAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    private class UserRejectedBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case USER_VERIFY_REJECT_BROADCAST:
                    Slog.d(TAG, "-------------------->USER_VERIFY_REJECT_BROADCAST");
                    int aid = intent.getIntExtra("aid", -1);
                    getAuthenticateById(aid, handler);
                    break;
                default:
                    break;
            }
        }
    }

    private void registerBroadcast() {
        mReceiver = new UserRejectedBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(USER_VERIFY_REJECT_BROADCAST);
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
        WeakReference<UserRejectedFragment> userRejectedFragmentWeakReference;
        MyHandler(UserRejectedFragment userRejectedFragment) {
            userRejectedFragmentWeakReference = new WeakReference<>(userRejectedFragment);
        }

        @Override
        public void handleMessage(Message message) {
            UserRejectedFragment userRejectedFragment = userRejectedFragmentWeakReference.get();
            if (userRejectedFragment != null) {
                userRejectedFragment.handleMessage(message);
            }
        }
    }
}
