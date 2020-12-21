package com.mufu.verify.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.mufu.R;
import com.mufu.adapter.ExperienceSummaryAdapter;

import com.mufu.experience.ExperienceDetailActivity;
import com.mufu.experience.ExperienceSummaryActivity;
import com.mufu.group.SubGroupActivity;
import com.mufu.util.BaseFragment;
import com.mufu.util.HttpUtil;
import com.mufu.util.MyLinearLayoutManager;
import com.mufu.util.Slog;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.mufu.verify.VerifyOperationInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_FIRST_USER;
import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;
import static com.mufu.verify.activity.ActivityVerifyActivity.PASS_ACTION;
import static com.mufu.verify.activity.ActivityVerifyActivity.REJECT_ACTION;
import static com.mufu.verify.activity.ActivityVerifyActivity.SET_EXPERIENCE_PASSED;
import static com.mufu.verify.activity.ActivityVerifyActivity.SET_EXPERIENCE_REJECTED;

public class ActivityRequestFragment extends BaseFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "ActivityRequestFragment";
    private static final int PAGE_SIZE = 8;
    public static final String GET_REQUESTING_EXPERIENCES = HttpUtil.DOMAIN + "?q=experience/get_requesting_experiences";

    private static final int GET_ALL_DONE = 1;
    private static final int GET_ALL_END = 2;
    private static final int NO_MORE = 3;
    private static final int SET_VERIFY_STATUS_DONE = 4;
    
    final int itemLimit = 1;
    private int uid = 0;
    private boolean isSelf = false;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    private int mLoadSize = 0;
    private Handler handler;
    private int mPosition;

    private ExperienceSummaryAdapter experienceRequestListAdapter;
    private XRecyclerView recyclerView;
    private List<ExperienceSummaryActivity.Experience> mExperienceList = new ArrayList<>();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.experience_verify_summary, container, false);
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
        recyclerView = view.findViewById(R.id.experience_verify_list);
        experienceRequestListAdapter = new ExperienceSummaryAdapter(getContext(), isSelf, true);
        MyLinearLayoutManager linearLayoutManager = new MyLinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setRefreshProgressStyle(BallSpinFadeLoader);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallRotate);

        recyclerView.setPullRefreshEnabled(false);
        recyclerView.getDefaultRefreshHeaderView().setRefreshTimeVisible(true);
        recyclerView.getDefaultFootView().setLoadingHint(getString(R.string.loading_pull_up_tip));
        recyclerView.getDefaultFootView().setNoMoreHint(getString(R.string.no_more));
        
        // When the item number of the screen number is list.size-2,we call the onLoadMore
        recyclerView.setLimitNumberToCallLoadMore(itemLimit);
        recyclerView.setRefreshProgressStyle(ProgressStyle.BallBeat);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    experienceRequestListAdapter.setScrolling(false);
                    experienceRequestListAdapter.notifyDataSetChanged();
                } else {
                    experienceRequestListAdapter.setScrolling(true);
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
        
        experienceRequestListAdapter.setItemClickListener(new ExperienceSummaryAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Slog.d(TAG, "==========click : " + position);
                int eid = mExperienceList.get(position).eid;
                Intent intent = new Intent(getContext(), ExperienceDetailActivity.class);
                intent.putExtra("eid", eid);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivityForResult(intent, RESULT_FIRST_USER);
            }

            @Override
            public void onPassClick(View view, int position) {
                Slog.d(TAG, "==================onPassClick");
                mPosition = position;
                int eid = mExperienceList.get(position).eid;
                setVerifyStatus(PASS_ACTION, eid, "");
            }
            
            @Override
            public void onRejectClick(View view, int position) {
                Slog.d(TAG, "==================onRejectClick");
                mPosition = position;
                int eid = mExperienceList.get(position).eid;
                rejectNotice(eid);
            }
            
            @Override
            public void onItemDeleteClick(View view, int position){}
        });

        recyclerView.setAdapter(experienceRequestListAdapter);


        //show progressImage before loading done
        progressImageView = view.findViewById(R.id.animal_progress);
        animationDrawable = (AnimationDrawable) progressImageView.getDrawable();
        progressImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        }, 50);

        requestData();
    }
    
    private void requestData() {

        final int page = mExperienceList.size() / PAGE_SIZE;
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page));

        RequestBody requestBody = builder.build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_REQUESTING_EXPERIENCES, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========loadData response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject experiencesResponse = null;
                        try {
                            experiencesResponse = new JSONObject(responseText);
                            if (experiencesResponse != null) {
                                mLoadSize = processExperiencesResponse(experiencesResponse);

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

    public int processExperiencesResponse(JSONObject experiencesObject) {
        int experienceSize = 0;
        JSONArray experienceArray = null;

        if (experiencesObject != null) {
            experienceArray = experiencesObject.optJSONArray("experiences");
            Slog.d(TAG, "------------------->processExperiencesResponse: "+experienceArray);
        }
        
        if (experienceArray != null) {
            experienceSize = experienceArray.length();
            if (experienceSize > 0) {
                for (int i = 0; i < experienceArray.length(); i++) {
                    JSONObject guideObject = experienceArray.optJSONObject(i);
                    if (guideObject != null) {
                        ExperienceSummaryActivity.Experience experience = getExperience(guideObject);
                        mExperienceList.add(experience);
                    }
                }
            }
        }

        return experienceSize;
    }
    
    public static ExperienceSummaryActivity.Experience getExperience(JSONObject experienceObject) {
        ExperienceSummaryActivity.Experience experience = new ExperienceSummaryActivity.Experience();
        if (experienceObject != null) {
            experience.eid = experienceObject.optInt("eid");
            experience.city = experienceObject.optString("city");
            experience.headPictureUrl = experienceObject.optString("picture_url");
            experience.evaluateScore = experienceObject.optInt("score");
            experience.evaluateCount = experienceObject.optInt("count");
            experience.price = experienceObject.optInt("price");
            experience.title = experienceObject.optString("title");
            experience.unit = experienceObject.optString("unit");
            experience.duration = experienceObject.optInt("duration");
        }

        return experience;
    }
    
    
    public void setVerifyStatus(int type, int eid, String reason){
        Slog.d(TAG, "----------------------->setVerifyStatus");
        showProgressDialog(getActivity(),"");
        String url;
        if (type == PASS_ACTION){
            url = SET_EXPERIENCE_PASSED;
        }else {
            url = SET_EXPERIENCE_REJECTED;
        }

        FormBody.Builder builder = new FormBody.Builder();
        builder.add("eid", String.valueOf(eid));
        if (type == REJECT_ACTION){
            builder.add("reason", reason);
        }
        RequestBody requestBody = builder.build();
        
        HttpUtil.sendOkHttpRequest(getContext(), url, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========loadData response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject experiencesResponse = null;
                        try {
                            experiencesResponse = new JSONObject(responseText);
                            if (experiencesResponse != null) {
                                int result = experiencesResponse.optInt("result");
                                if (result > 0){
                                    dismissProgressDialog();
                                    handler.sendEmptyMessage(SET_VERIFY_STATUS_DONE);
                                }
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

    public void rejectNotice(int eid){
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(getActivity(), R.style.Theme_MaterialComponents_Light_Dialog_Alert);

        EditText reasonET = new EditText(getContext());
        normalDialog.setTitle("说明拒绝原因")
                .setIcon(R.mipmap.mufu_icon)
            .setView(reasonET)
                .setPositiveButton("拒绝", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String reason = reasonET.getText().toString();
                        if (TextUtils.isEmpty(reason)){
                            Toast.makeText(getContext(), "请说明拒绝原因", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        setVerifyStatus(REJECT_ACTION, eid, reason);
                    }
                }).setNegativeButton("取消",null).show();
        
         }

    public void handleMessage(Message message) {
        switch (message.what) {
            case GET_ALL_DONE:
                Slog.d(TAG, "-------------->GET_ALL_DONE");
                experienceRequestListAdapter.setData(mExperienceList);
                experienceRequestListAdapter.notifyDataSetChanged();
                recyclerView.loadMoreComplete();
                stopLoadProgress();
                break;
            case GET_ALL_END:
                Slog.d(TAG, "-------------->GET_ALL_END");
                experienceRequestListAdapter.setData(mExperienceList);
                experienceRequestListAdapter.notifyDataSetChanged();
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
            case SET_VERIFY_STATUS_DONE:
                mExperienceList.remove(mPosition);
                experienceRequestListAdapter.setData(mExperienceList);
                experienceRequestListAdapter.notifyItemRangeRemoved(mPosition, 1);
                experienceRequestListAdapter.notifyDataSetChanged();
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
    
    public static class Experience extends ExperienceSummaryActivity.BaseExperience {
        public int eid;
        public int duration;
        public String unit;
    }

    public static class BaseExperience implements Serializable {
        public String headPictureUrl;
        public String city;
        public String title;
        public int evaluateScore;
        public int evaluateCount;
        public int price;
    }

    static class MyHandler extends Handler {
        WeakReference<ActivityRequestFragment> activityRequestFragmentWeakReference;

        MyHandler(ActivityRequestFragment activityRequestFragment) {
            activityRequestFragmentWeakReference = new WeakReference<>(activityRequestFragment);
        }

        @Override
        public void handleMessage(Message message) {
            ActivityRequestFragment experienceSummaryActivity = activityRequestFragmentWeakReference.get();
            if (experienceSummaryActivity != null) {
                experienceSummaryActivity.handleMessage(message);
            }
        }
    }
}


