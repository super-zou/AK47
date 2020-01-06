package com.hetang.authenticate;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hetang.R;
import com.hetang.adapter.AuthenticateRequestListAdapter;
import com.hetang.common.MyApplication;
import com.hetang.util.BaseFragment;
import com.hetang.util.HttpUtil;
import com.hetang.util.Slog;
import com.hetang.util.UserProfile;
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

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.hetang.authenticate.AuthenticationActivity.unVERIFIED;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class RequestFragment extends BaseFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "RequestFragment";
    private static final int PAGE_SIZE = 6;
    private static final int LOAD_DONE = 0;
    private static final int UPDATE_DONE = 1;
    private static final int LOAD_COMPLETE_END = 2;
    private static final int LOAD_NOTHING_DONE = 3;
    private static final int OPERATION_DONE = 4;
    public static final String GET_ALL_AUTHENTICATION_URL = HttpUtil.DOMAIN + "?q=user_extdata/get_all_authentication_status/";
    public static final String SET_AUTHENTICATION_STATUS = HttpUtil.DOMAIN + "?q=user_extdata/set_authentication_status";
    private int mCurrentPos;
    int page = 0;
    private int type;
    private String reason = "名字与证件不一致";
    private TextView countTV;
    private int count;
    private static int PASSED = 1;
    private static int REJECTED = 2;
    private List<Authentication> authenticationList = new ArrayList<>();
    private XRecyclerView xRecyclerView;
    private AuthenticateRequestListAdapter authenticationListAdapter;
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
        authenticationListAdapter = new AuthenticateRequestListAdapter(getContext());

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
                Authentication authentication = authenticationList.get(position);
                authenticationOperation(authentication.aid, authentication.getUid(), PASSED, "");
            }

            @Override
            public void onRejectClick(View view, final int position) {
                mCurrentPos = position;
                 AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                builder.setTitle("拒绝原因");
                final String[] items=new String[]{"名字与证件不一致","头像非本人","性别与证件不一致","学历与证件不一致", "专业与证件不一致", "学校与证件不一致","上传的证件照不符合要求"};
                
                builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                          Slog.d(TAG, "----------------------> selected: "+items[which]);
                          reason = items[which];
                    }});
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Slog.d(TAG, "-------------------------->reason: "+reason);
                        Authentication authentication = authenticationList.get(position);
                        authenticationOperation(authentication.aid, authentication.getUid(), REJECTED, reason);

                    }});
                builder.create().show();
            }
        });

        xRecyclerView.setAdapter(authenticationListAdapter);

        loadData();

    }

    private void authenticationOperation(int aid, int uid, int status, String reason){
        showProgressDialog(getActivity(), "...");
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("aid", String.valueOf(aid))
                .add("uid", String.valueOf(uid))
                .add("status", String.valueOf(status));
        if (status == REJECTED){
            builder.add("reason", reason);
        }
        RequestBody requestBody = builder.build();

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

        String url = GET_ALL_AUTHENTICATION_URL + "unverified";

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

    public static Authentication parseAuthentication(JSONObject authenticationObject) {
        Authentication authentication = new Authentication();
        authentication.aid = authenticationObject.optInt("aid");
        authentication.officerUid = authenticationObject.optInt("officer_uid");
        authentication.authenticationPhotoUrl = authenticationObject.optString("uri");
        authentication.requestTime = authenticationObject.optInt("created");

        authentication.setUid(authenticationObject.optInt("uid"));
        authentication.setName(authenticationObject.optString("realname"));
        authentication.setAvatar(authenticationObject.optString("avatar"));
        authentication.setSex(authenticationObject.optInt("sex"));
        authentication.setSituation(authenticationObject.optInt("situation"));
        authentication.setMajor(authenticationObject.optString("major"));
        authentication.setDegree(authenticationObject.optString("degree"));
        authentication.setUniversity(authenticationObject.optString("university"));

        return authentication;
    }

    public void handleMessage(Message message) {
        switch (message.what) {
            case LOAD_DONE:
                authenticationListAdapter.setData(authenticationList, unVERIFIED);
                authenticationListAdapter.notifyDataSetChanged();
                xRecyclerView.refreshComplete();
                //xRecyclerView.loadMoreComplete();
                break;

            case LOAD_COMPLETE_END:
                authenticationListAdapter.setData(authenticationList, unVERIFIED);
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
                authenticationList.clear();
                loadData();
                break;
            default:
                break;
        }
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
    }

    static class MyHandler extends Handler {
        WeakReference<RequestFragment> notificationFragmentWeakReference;

        MyHandler(RequestFragment notificationFragment) {
            notificationFragmentWeakReference = new WeakReference<>(notificationFragment);
        }

        @Override
        public void handleMessage(Message message) {
            RequestFragment notificationFragment = notificationFragmentWeakReference.get();
            if (notificationFragment != null) {
                notificationFragment.handleMessage(message);
            }
        }
    }
}
