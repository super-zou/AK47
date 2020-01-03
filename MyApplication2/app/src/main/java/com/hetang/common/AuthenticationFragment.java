package com.hetang.common;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hetang.R;
import com.hetang.adapter.AuthenticationListAdapter;
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
import static com.hetang.common.AuthenticationActivity.VERIFIED;
import static com.hetang.common.AuthenticationActivity.unVERIFIED;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class AuthenticationFragment extends BaseFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "AuthenticationFragment";

    private int type;
    private static final int PAGE_SIZE = 5;
    private static final int LOAD_DONE = 0;
    private static final int UPDATE_DONE = 1;
    private static final int LOAD_COMPLETE_END = 2;
    private static final int LOAD_NOTHING_DONE = 3;
    private static final String GET_ALL_AUTHENTICATION_URL = HttpUtil.DOMAIN + "?q=user_extdata/get_all_authentication_status/";
    private static final String GET_UPDATE_NOTICE_URL = HttpUtil.DOMAIN + "?q=notice/get_update";
    
    private List<Authentication> authenticationList = new ArrayList<>();
    private XRecyclerView xRecyclerView;
    private int mTempSize;
    private AuthenticationListAdapter authenticationListAdapter;
    private Context mContext;
    private MyHandler handler;
    Typeface font;
    int i = 0;
    private Runnable runnable;
    int page = 0;
    
    public static final AuthenticationFragment newInstance(int type) {
        AuthenticationFragment f = new AuthenticationFragment();
        Bundle bdl = new Bundle();
        bdl.putInt("type", type);
        f.setArguments(bdl);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getArguments().getInt("type");
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
        Slog.d(TAG, "================================initView");
        handler = new MyHandler(this);
        mContext = MyApplication.getContext();
        authenticationListAdapter = new AuthenticationListAdapter(getContext());

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
            public void onRefresh() {}

            @Override
            public void onLoadMore() {
                loadData();
            }
        });

        xRecyclerView.setAdapter(authenticationListAdapter);

        loadData();

    }
    
    protected void loadData() {
        Slog.d(TAG, "-------------------------------->loadData");
        page = authenticationList.size() / PAGE_SIZE;
        RequestBody requestBody = new FormBody.Builder()
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();
                
                String url = "";
        if (type == unVERIFIED){
            url = GET_ALL_AUTHENTICATION_URL+"unverified";
        }else if (type == VERIFIED){
            url = GET_ALL_AUTHENTICATION_URL+"verified";
        }else {
            url = GET_ALL_AUTHENTICATION_URL+"rejected";
        }

        Slog.d(TAG, "-------------url: "+url);
        
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
            authenticationArray = responseObject.optJSONArray("results");

            if (isDebug) Slog.d(TAG, "------------------------------>authenticationArray: " + authenticationArray);
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

    public static class Authentication extends UserProfile{
        public int aid;
        public int officerUid;
        public String authenticationPhotoUrl = "";
        public int requestTime;
    }
    
    private Authentication parseAuthentication(JSONObject authenticationObject) {
        Authentication authentication = new Authentication();
        authentication.aid = authenticationObject.optInt("aid");
        authentication.officerUid = authenticationObject.optInt("officer_uid");
        authentication.authenticationPhotoUrl = authenticationObject.optString("uri");
        authentication.requestTime = authenticationObject.optInt("created");
        
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
                authenticationListAdapter.setData(authenticationList, type);
                authenticationListAdapter.notifyDataSetChanged();
                xRecyclerView.refreshComplete();
                //xRecyclerView.loadMoreComplete();
                break;

            case LOAD_COMPLETE_END:
                authenticationListAdapter.setData(authenticationList, type);
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
            default:
                break;
     }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
    
    static class MyHandler extends Handler {
        WeakReference<AuthenticationFragment> notificationFragmentWeakReference;

        MyHandler(AuthenticationFragment notificationFragment) {
            notificationFragmentWeakReference = new WeakReference<>(notificationFragment);
        }

        @Override
        public void handleMessage(Message message) {
            AuthenticationFragment notificationFragment = notificationFragmentWeakReference.get();
            if (notificationFragment != null) {
                notificationFragment.handleMessage(message);
            }
        }
    }
}
