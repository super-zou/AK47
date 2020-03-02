package com.hetang.verify.talent;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hetang.R;
import com.hetang.adapter.verify.TalentRejectedListAdapter;
import com.hetang.verify.VerifyOperationInterface;
import com.hetang.common.MyApplication;
import com.hetang.group.SubGroupActivity;
import com.hetang.talent.TalentDetailsActivity;
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
import static com.hetang.group.SubGroupActivity.GET_ALL_VERIFY_TALENTS;
import static com.hetang.verify.VerifyActivity.TALENT_VERIFY_PASS_BROADCAST;
import static com.hetang.verify.VerifyActivity.TALENT_VERIFY_REJECT_BROADCAST;
import static com.hetang.verify.talent.TalentRequestFragment.SET_TALENT_STATUS;
import static com.hetang.verify.talent.TalentVerifyActivity.LOAD_TALENT_DONE;
import static com.hetang.verify.user.UserRequestFragment.OPERATION_PASSED_DONE;
import static com.hetang.verify.user.UserRequestFragment.OPERATION_REJECTED_DONE;
import static com.hetang.group.SubGroupActivity.GET_ALL_TALENTS;
import static com.hetang.group.SubGroupActivity.getTalent;
import static com.hetang.talent.TalentDetailsActivity.GET_TALENT_byID;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class TalentRejectedFragment extends BaseFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "TalentRequestFragment";
    private static final int PAGE_SIZE = 6;
    private static final int LOAD_DONE = 0;
    private static final int UPDATE_DONE = 1;
    private static final int LOAD_COMPLETE_END = 2;
    private static final int LOAD_NOTHING_DONE = 3;
    private static final int OPERATION_DONE = 4;
    private static int PASSED = 1;
    private static int REJECTED = 2;
    int page = 0;
    private int mCurrentPos;
    private int type;
    private String reason = "名字与证件不一致";
    private int mLoadSize = 0;
    private List<SubGroupActivity.Talent> talentList = new ArrayList<>();
    private XRecyclerView xRecyclerView;
    private TalentRejectedListAdapter talentRejectedListAdapter;
    private MyHandler myHandler;
    private TalentRejectedBroadcastReceiver mReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.talent_verify_status, container, false);
        initView(convertView);
        registerBroadcast();
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
        myHandler = new MyHandler(this);
        talentRejectedListAdapter = new TalentRejectedListAdapter(getContext());

        xRecyclerView = view.findViewById(R.id.talent_verify_list);

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
                    talentRejectedListAdapter.setScrolling(false);
                    talentRejectedListAdapter.notifyDataSetChanged();
                } else {
                    talentRejectedListAdapter.setScrolling(true);
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

        talentRejectedListAdapter.setOnItemClickListener(new VerifyOperationInterface() {
            @Override
            public void onPassClick(View view, int position) {
                mCurrentPos = position;
                SubGroupActivity.Talent talent = talentList.get(position);
                authenticationOperation(talent.aid, PASSED, "");
            }

            @Override
            public void onRejectClick(View view, final int position) {
                mCurrentPos = position;
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("拒绝原因");
                final String[] items = new String[]{"专长和热爱不符合要求", "头像不符合要求", "主题与内容不符", "资质材料不符合要求", "收费说明不符合要求"};

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
                        SubGroupActivity.Talent talent = talentList.get(position);
                        authenticationOperation(talent.aid, REJECTED, reason);

                    }
                });
                builder.create().show();
            }
        }, new TalentRejectedListAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Slog.d(TAG, "==========click : " + position);
                Intent intent = new Intent(getContext(), TalentDetailsActivity.class);
                //intent.putExtra("talent", mTalentList.get(position));
                intent.putExtra("aid", talentList.get(position).aid);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });

        xRecyclerView.setAdapter(talentRejectedListAdapter);

        requestData();

    }

    private void authenticationOperation(int aid, int status, String reason) {
        showProgressDialog(getActivity(), "...");
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("aid", String.valueOf(aid))
                .add("status", String.valueOf(status));
        if (status == REJECTED) {
            builder.add("reason", reason);
        }
        RequestBody requestBody = builder.build();

        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), SET_TALENT_STATUS, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========load response text : " + responseText);
                    if (responseText != null) {
                        try {
                            JSONObject responseObject = new JSONObject(responseText);
                            int result = responseObject.optInt("status");
                            if (result > 0) {
                                dismissProgressDialog();
                                if (status == PASSED) {
                                    myHandler.sendEmptyMessage(OPERATION_PASSED_DONE);
                                } else {
                                    myHandler.sendEmptyMessage(OPERATION_REJECTED_DONE);
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

    protected void requestData() {
        final int page = talentList.size() / PAGE_SIZE;

        FormBody.Builder builder = new FormBody.Builder();
        builder.add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page));
        String url = GET_ALL_VERIFY_TALENTS + "/rejected";
        RequestBody requestBody = builder.build();

        HttpUtil.sendOkHttpRequest(getContext(), url, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject talentResponse = null;

                        try {
                            talentResponse = new JSONObject(responseText);
                            if (talentResponse != null) {
                                mLoadSize = processTalentsResponse(talentResponse);

                                if (mLoadSize == PAGE_SIZE) {
                                    myHandler.sendEmptyMessage(LOAD_DONE);
                                } else {
                                    if (mLoadSize != 0) {
                                        myHandler.sendEmptyMessage(LOAD_COMPLETE_END);
                                    } else {
                                        myHandler.sendEmptyMessage(LOAD_NOTHING_DONE);
                                    }
                                }
                            } else {
                                myHandler.sendEmptyMessage(LOAD_NOTHING_DONE);
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

    public int processTalentsResponse(JSONObject talentsObject) {
        int talentSize = 0;
        JSONArray talentArray = null;

        if (talentsObject != null) {
            talentArray = talentsObject.optJSONArray("talents");
        }

        if (talentArray != null) {
            talentSize = talentArray.length();
            if (talentSize > 0) {
                for (int i = 0; i < talentArray.length(); i++) {
                    JSONObject talentObject = talentArray.optJSONObject(i);
                    if (talentObject != null) {
                        SubGroupActivity.Talent talent = getTalent(talentObject);
                        talentList.add(talent);
                    }
                }
            }
        }

        return talentSize;
    }

    public static void getTalentById(int aid, Handler handler){
        RequestBody requestBody = new FormBody.Builder()
                .add("aid", String.valueOf(aid))
                .build();

        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_TALENT_byID, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (isDebug) Slog.d(TAG, "==========response body : " + response.body());

                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject talentResponse = null;
                        try {
                            talentResponse = new JSONObject(responseText);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (talentResponse != null) {
                            JSONObject talentObject = talentResponse.optJSONObject("talent");
                            SubGroupActivity.Talent talent = getTalent(talentObject);
                            Message message = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("talent", talent);
                            message.setData(bundle);
                            message.what = LOAD_TALENT_DONE;
                            handler.sendMessage(message);
                        }

                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }


    public void handleMessage(Message message) {
        switch (message.what) {
            case LOAD_DONE:
                talentRejectedListAdapter.setData(talentList);
                talentRejectedListAdapter.notifyDataSetChanged();
                xRecyclerView.loadMoreComplete();
                //xRecyclerView.loadMoreComplete();
                break;

            case LOAD_COMPLETE_END:
                talentRejectedListAdapter.setData(talentList);
                talentRejectedListAdapter.notifyDataSetChanged();
                //xRecyclerView.refreshComplete();
                xRecyclerView.loadMoreComplete();
                xRecyclerView.setNoMore(true);
                break;
            case LOAD_NOTHING_DONE:
                //xRecyclerView.refreshComplete();
                xRecyclerView.setNoMore(true);
                xRecyclerView.loadMoreComplete();
                break;
            case OPERATION_PASSED_DONE:
                sendPassBroadcast(talentList.get(mCurrentPos).aid);
                talentList.remove(mCurrentPos);
                talentRejectedListAdapter.setData(talentList);
                //userRequestListAdapter.notifyItemRemoved(mCurrentPos, 1);
                talentRejectedListAdapter.notifyItemRangeRemoved(mCurrentPos, 1);
                talentRejectedListAdapter.notifyDataSetChanged();
                if (talentList.size() < PAGE_SIZE - 1) {
                    xRecyclerView.loadMoreComplete();
                }
                break;
            case OPERATION_REJECTED_DONE:
                sendRejectBroadcast(talentList.get(mCurrentPos).aid);
                talentList.remove(mCurrentPos);
                talentRejectedListAdapter.setData(talentList);
                //userRequestListAdapter.notifyItemRemoved(mCurrentPos);
                talentRejectedListAdapter.notifyItemRangeRemoved(mCurrentPos, 1);
                talentRejectedListAdapter.notifyDataSetChanged();
                if (talentList.size() < PAGE_SIZE - 1) {
                    xRecyclerView.loadMoreComplete();
                }
                break;
            case LOAD_TALENT_DONE:
                Slog.d(TAG, "--------------------->LOAD_TALENT_DONE");
                Bundle bundle = message.getData();
                SubGroupActivity.Talent talent = (SubGroupActivity.Talent) bundle.getSerializable("talent");
                talentList.add(0, talent);
                talentRejectedListAdapter.setData(talentList);
                talentRejectedListAdapter.notifyItemInserted(0);
                talentRejectedListAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    private void sendPassBroadcast(int aid) {
        Intent intent = new Intent(TALENT_VERIFY_PASS_BROADCAST);
        intent.putExtra("aid", aid);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    private void sendRejectBroadcast(int aid) {
        Intent intent = new Intent(TALENT_VERIFY_REJECT_BROADCAST);
        intent.putExtra("aid", aid);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    private class TalentRejectedBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case TALENT_VERIFY_REJECT_BROADCAST:
                    int aid = intent.getIntExtra("aid", -1);
                    getTalentById(aid, myHandler);
                    break;
                default:
                    break;
            }
        }
    }

    private void registerBroadcast() {
        mReceiver = new TalentRejectedBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TALENT_VERIFY_REJECT_BROADCAST);
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
        WeakReference<TalentRejectedFragment> talentRequestFragmentWeakReference;

        MyHandler(TalentRejectedFragment talentRequestFragment) {
            talentRequestFragmentWeakReference = new WeakReference<>(talentRequestFragment);
        }

        @Override
        public void handleMessage(Message message) {
            TalentRejectedFragment talentRequestFragment = talentRequestFragmentWeakReference.get();
            if (talentRequestFragment != null) {
                talentRequestFragment.handleMessage(message);
            }
        }
    }
}
