package com.mufu.verify.user;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mufu.R;
import com.mufu.adapter.verify.UserRequestListAdapter;
import com.mufu.verify.VerifyOperationInterface;
import com.mufu.common.MyApplication;
import com.mufu.util.BaseFragment;
import com.mufu.util.HttpUtil;
import com.mufu.util.Slog;
import com.mufu.util.UserProfile;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.mufu.verify.VerifyActivity.REQUEST;
import static com.mufu.verify.VerifyActivity.USER_VERIFY_PASS_BROADCAST;
import static com.mufu.verify.VerifyActivity.USER_VERIFY_REJECT_BROADCAST;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class UserRequestFragment extends BaseFragment {
    public static final String GET_ALL_AUTHENTICATION_URL = HttpUtil.DOMAIN + "?q=user_extdata/get_all_authentication_status/";
    public static final String SET_AUTHENTICATION_STATUS = HttpUtil.DOMAIN + "?q=user_extdata/set_authentication_status";
    public static final String GET_AUTHENTICATION_BY_ID = HttpUtil.DOMAIN + "?q=user_extdata/get_authenticate_by_id";
    private static final boolean isDebug = true;
    private static final String TAG = "UserRequestFragment";
    private static final int PAGE_SIZE = 6;
    private static final int LOAD_DONE = 0;
    private static final int UPDATE_DONE = 1;
    private static final int LOAD_COMPLETE_END = 2;
    private static final int LOAD_NOTHING_DONE = 3;
    public static final int OPERATION_PASSED_DONE = 4;
    public static final int OPERATION_REJECTED_DONE = 5;
    private static int PASSED = 1;
    private static int REJECTED = 2;
    int page = 0;
    private int mCurrentPos;
    private int type;
    private String reason = "名字与证件不一致";
    private int count;
    private List<UserRequestFragment.Authentication> authenticationList = new ArrayList<>();
    private XRecyclerView xRecyclerView;
    //private UserRequestListAdapter userRequestListAdapter;
    private UserRequestListAdapter userRequestListAdapter;
    private MyHandler handler;

    public static Authentication parseAuthentication(JSONObject authenticationObject) {
        Authentication authentication = new Authentication();
        authentication.aid = authenticationObject.optInt("aid");
        authentication.officerUid = authenticationObject.optInt("officer_uid");
        authentication.authenticationPhotoUrl = authenticationObject.optString("uri");
        authentication.requestTime = authenticationObject.optInt("created");
        authentication.reason = authenticationObject.optString("reason");

        authentication.setUid(authenticationObject.optInt("uid"));
        authentication.setNickName(authenticationObject.optString("nickname"));
        authentication.setRealName(authenticationObject.optString("realname"));
        authentication.setAvatar(authenticationObject.optString("avatar"));
        authentication.setSex(authenticationObject.optInt("sex"));
        authentication.setSituation(authenticationObject.optInt("situation"));
        authentication.setMajor(authenticationObject.optString("major"));
        authentication.setDegree(authenticationObject.optString("degree"));
        authentication.setUniversity(authenticationObject.optString("university"));

        return authentication;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.user_verify_status, container, false);
        initView(convertView);
        return convertView;
    }

    @Override
    protected int getLayoutId() {
        return 0;
    }

    @Override
    protected void loadData() {
    }

    protected void initView(View view) {
        handler = new MyHandler(this);
        //userRequestListAdapter = new UserRequestListAdapter(getContext());
        userRequestListAdapter = new UserRequestListAdapter(getContext(), this);

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
                    userRequestListAdapter.setScrolling(false);
                    userRequestListAdapter.notifyDataSetChanged();
                } else {
                    userRequestListAdapter.setScrolling(true);
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

        userRequestListAdapter.setOnItemClickListener(new VerifyOperationInterface() {
            @Override
            public void onPassClick(View view, int position) {
                mCurrentPos = position;
                Authentication authentication = authenticationList.get(position);
                authenticationOperation(authentication.aid, authentication.getUid(), PASSED, "");
            }

            @Override
            public void onRejectClick(View view, final int position) {
                mCurrentPos = position;
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("拒绝原因");
                final String[] items = new String[]{"名字与证件不一致", "头像非本人", "性别与证件不一致", "学历与证件不一致", "专业与证件不一致", "学校与证件不一致", "上传的证件照不符合要求"};

                builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Slog.d(TAG, "----------------------> selected: " + items[which]);
                        reason = items[which];
                    }
                });
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Slog.d(TAG, "-------------------------->reason: " + reason);
                        Authentication authentication = authenticationList.get(position);
                        authenticationOperation(authentication.aid, authentication.getUid(), REJECTED, reason);

                    }
                });
                builder.create().show();
            }
        });

        xRecyclerView.setAdapter(userRequestListAdapter);

        requestData();

    }

    private void authenticationOperation(int aid, int uid, int status, String reason) {
        showProgressDialog(getActivity(), "...");
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("aid", String.valueOf(aid))
                .add("uid", String.valueOf(uid))
                .add("status", String.valueOf(status));
        if (status == REJECTED) {
            builder.add("reason", reason);
        }
        RequestBody requestBody = builder.build();

        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), SET_AUTHENTICATION_STATUS, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug)
                        Slog.d(TAG, "==========authenticationOperation load response text : " + responseText);
                    if (responseText != null) {
                        try {
                            JSONObject responseObject = new JSONObject(responseText);
                            int result = responseObject.optInt("status");
                            if (result > 0) {
                                dismissProgressDialog();
                                if (status == PASSED){
                                    handler.sendEmptyMessage(OPERATION_PASSED_DONE);
                                }else {
                                    handler.sendEmptyMessage(OPERATION_REJECTED_DONE);
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
        Slog.d(TAG, "------------------>requestData");
        page = authenticationList.size() / PAGE_SIZE;
        RequestBody requestBody = new FormBody.Builder()
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();

        String url = GET_ALL_AUTHENTICATION_URL + "unverified";

        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), url, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========requestData load response text : ");
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

            if (authenticationArray != null && authenticationArray.length() > 0) {
                for (int i = 0; i < authenticationArray.length(); i++) {
                    JSONObject authenticationObject = authenticationArray.getJSONObject(i);
                    Authentication authentication = parseAuthentication(authenticationObject);
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
                userRequestListAdapter.setData(authenticationList, REQUEST);
                userRequestListAdapter.notifyDataSetChanged();
                xRecyclerView.refreshComplete();
                //xRecyclerView.loadMoreComplete();
                break;
            case LOAD_COMPLETE_END:
                userRequestListAdapter.setData(authenticationList, REQUEST);
                userRequestListAdapter.notifyDataSetChanged();
                xRecyclerView.refreshComplete();
                xRecyclerView.loadMoreComplete();
                xRecyclerView.setNoMore(true);
                break;
            case LOAD_NOTHING_DONE:
                //xRecyclerView.refreshComplete();
                xRecyclerView.setNoMore(true);
                xRecyclerView.loadMoreComplete();
                break;
            case OPERATION_PASSED_DONE:
                sendPassBroadcast(authenticationList.get(mCurrentPos).aid);
                authenticationList.remove(mCurrentPos);
                userRequestListAdapter.setData(authenticationList, REQUEST);
                //userRequestListAdapter.notifyItemRemoved(mCurrentPos, 1);
                userRequestListAdapter.notifyItemRangeRemoved(mCurrentPos, 1);
                userRequestListAdapter.notifyDataSetChanged();
                if (authenticationList.size() < PAGE_SIZE - 1) {
                    xRecyclerView.loadMoreComplete();
                }
                break;
            case OPERATION_REJECTED_DONE:
                sendRejectBroadcast(authenticationList.get(mCurrentPos).aid);
                authenticationList.remove(mCurrentPos);
                userRequestListAdapter.setData(authenticationList, REQUEST);
                //userRequestListAdapter.notifyItemRemoved(mCurrentPos);
                userRequestListAdapter.notifyItemRangeRemoved(mCurrentPos, 1);
                userRequestListAdapter.notifyDataSetChanged();
                if (authenticationList.size() < PAGE_SIZE - 1) {
                    xRecyclerView.loadMoreComplete();
                }
                break;
            default:
                break;
        }
    }

    private void sendPassBroadcast(int aid) {
        Intent intent = new Intent(USER_VERIFY_PASS_BROADCAST);
        intent.putExtra("aid", aid);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    private void sendRejectBroadcast(int aid) {
        Intent intent = new Intent(USER_VERIFY_REJECT_BROADCAST);
        intent.putExtra("aid", aid);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static class Authentication extends UserProfile {
        public int aid;
        public int officerUid;
        public String authenticationPhotoUrl = "";
        public int requestTime;
        public String reason = "";
    }

    static class MyHandler extends Handler {
        WeakReference<UserRequestFragment> notificationFragmentWeakReference;

        MyHandler(UserRequestFragment notificationFragment) {
            notificationFragmentWeakReference = new WeakReference<>(notificationFragment);
        }

        @Override
        public void handleMessage(Message message) {
            UserRequestFragment notificationFragment = notificationFragmentWeakReference.get();
            if (notificationFragment != null) {
                notificationFragment.handleMessage(message);
            }
        }
    }
}
