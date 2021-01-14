package com.mufu.verify.talent;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.mufu.R;
import com.mufu.adapter.verify.TalentRequestListAdapter;
import com.mufu.verify.VerifyOperationInterface;
import com.mufu.common.MyApplication;
import com.mufu.group.SubGroupActivity;
import com.mufu.talent.TalentDetailsActivity;
import com.mufu.util.BaseFragment;
import com.mufu.util.HttpUtil;
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
import static com.mufu.group.SubGroupActivity.GET_ALL_VERIFY_TALENTS;
import static com.mufu.verify.VerifyActivity.TALENT_VERIFY_PASS_BROADCAST;
import static com.mufu.verify.VerifyActivity.TALENT_VERIFY_REJECT_BROADCAST;
import static com.mufu.verify.user.UserRequestFragment.OPERATION_PASSED_DONE;
import static com.mufu.verify.user.UserRequestFragment.OPERATION_REJECTED_DONE;
import static com.mufu.group.SubGroupActivity.getTalent;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class TalentRequestFragment extends BaseFragment {
    public static final String GET_ALL_AUTHENTICATION_URL = HttpUtil.DOMAIN + "?q=user_extdata/get_all_authentication_status/";
    public static final String SET_TALENT_STATUS = HttpUtil.DOMAIN + "?q=talent/set_status";
    private static final boolean isDebug = true;
    private static final String TAG = "TalentRequestFragment";
    private static final int PAGE_SIZE = 11;
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
    private TalentRequestListAdapter talentRequestListAdapter;
    private MyHandler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.talent_verify_status, container, false);
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
        talentRequestListAdapter = new TalentRequestListAdapter(getContext());

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
                    talentRequestListAdapter.setScrolling(false);
                    talentRequestListAdapter.notifyDataSetChanged();
                } else {
                    talentRequestListAdapter.setScrolling(true);
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

        talentRequestListAdapter.setOnItemClickListener(new VerifyOperationInterface() {
            @Override
            public void onPassClick(View view, int position) {
                mCurrentPos = position;
                SubGroupActivity.Talent talent = talentList.get(position);
                authenticationOperation(talent.tid, PASSED, "");
            }

            @Override
            public void onRejectClick(View view, final int position) {
                mCurrentPos = position;
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                EditText et = new EditText(getContext());
                et.setHint(getContext().getResources().getString(R.string.reject_reason));
                builder.setTitle("拒绝原因");
                final String[] items = new String[]{"专长和热爱不符合要求", "头像不符合要求", "主题与内容不符", "资质材料不符合要求", "收费说明不符合要求"};

                builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Slog.d(TAG, "----------------------> selected: " + items[which]);
                        reason = items[which];
                    }
                });
                builder.setView(et);

                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Slog.d(TAG, "-------------------------->reason: " + reason);
                        SubGroupActivity.Talent talent = talentList.get(position);
                        if (!TextUtils.isEmpty(et.getText().toString())){
                            reason = et.getText().toString();
                        }
                        authenticationOperation(talent.tid, REJECTED, reason);

                    }
                });
                builder.create().show();
            }
        }, new TalentRequestListAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Slog.d(TAG, "==========click : " + position);
                Intent intent = new Intent(getContext(), TalentDetailsActivity.class);
                //intent.putExtra("talent", mTalentList.get(position));
                intent.putExtra("tid", talentList.get(position).tid);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });

        xRecyclerView.setAdapter(talentRequestListAdapter);

        requestData();

    }

    private void authenticationOperation(int tid, int status, String reason) {
        showProgressDialog(getActivity(), "...");
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("tid", String.valueOf(tid))
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
                            int result = responseObject.optInt("result");
                            if (result > 0) {
                                dismissProgressDialog();
                                if (status == PASSED) {
                                    handler.sendEmptyMessage(OPERATION_PASSED_DONE);
                                } else {
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

    protected void requestData() {
        final int page = talentList.size() / PAGE_SIZE;

        FormBody.Builder builder = new FormBody.Builder();
        builder.add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page));
        String url = GET_ALL_VERIFY_TALENTS + "/request";
        RequestBody requestBody = builder.build();

        HttpUtil.sendOkHttpRequest(getContext(), url, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    //if (isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject talentResponse = null;

                        try {
                            talentResponse = new JSONObject(responseText);
                            if (talentResponse != null) {
                                mLoadSize = processTalentsResponse(talentResponse);

                                if (mLoadSize == PAGE_SIZE) {
                                    handler.sendEmptyMessage(LOAD_DONE);
                                } else {
                                    if (mLoadSize != 0) {
                                        handler.sendEmptyMessage(LOAD_COMPLETE_END);
                                    } else {
                                        handler.sendEmptyMessage(LOAD_NOTHING_DONE);
                                    }
                                }
                            } else {
                                handler.sendEmptyMessage(LOAD_NOTHING_DONE);
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


    public void handleMessage(Message message) {
        switch (message.what) {
            case LOAD_DONE:
                talentRequestListAdapter.setData(talentList);
                talentRequestListAdapter.notifyDataSetChanged();
                xRecyclerView.loadMoreComplete();
                //xRecyclerView.loadMoreComplete();
                break;

            case LOAD_COMPLETE_END:
                talentRequestListAdapter.setData(talentList);
                talentRequestListAdapter.notifyDataSetChanged();
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
                sendPassBroadcast(talentList.get(mCurrentPos).tid);
                talentList.remove(mCurrentPos);
                talentRequestListAdapter.setData(talentList);
                //userRequestListAdapter.notifyItemRemoved(mCurrentPos, 1);
                talentRequestListAdapter.notifyItemRangeRemoved(mCurrentPos, 1);
                talentRequestListAdapter.notifyDataSetChanged();
                if (talentList.size() < PAGE_SIZE - 1) {
                    xRecyclerView.loadMoreComplete();
                }
                break;
            case OPERATION_REJECTED_DONE:
                sendRejectBroadcast(talentList.get(mCurrentPos).tid);
                talentList.remove(mCurrentPos);
                talentRequestListAdapter.setData(talentList);
                //userRequestListAdapter.notifyItemRemoved(mCurrentPos);
                talentRequestListAdapter.notifyItemRangeRemoved(mCurrentPos, 1);
                talentRequestListAdapter.notifyDataSetChanged();
                if (talentList.size() < PAGE_SIZE - 1) {
                    xRecyclerView.loadMoreComplete();
                }
                break;
            default:
                break;
        }
    }

    private void sendPassBroadcast(int tid) {
        Intent intent = new Intent(TALENT_VERIFY_PASS_BROADCAST);
        intent.putExtra("tid", tid);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    private void sendRejectBroadcast(int aid) {
        Intent intent = new Intent(TALENT_VERIFY_REJECT_BROADCAST);
        intent.putExtra("aid", aid);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    static class MyHandler extends Handler {
        WeakReference<TalentRequestFragment> talentRequestFragmentWeakReference;

        MyHandler(TalentRequestFragment talentRequestFragment) {
            talentRequestFragmentWeakReference = new WeakReference<>(talentRequestFragment);
        }

        @Override
        public void handleMessage(Message message) {
            TalentRequestFragment talentRequestFragment = talentRequestFragmentWeakReference.get();
            if (talentRequestFragment != null) {
                talentRequestFragment.handleMessage(message);
            }
        }
    }
}
