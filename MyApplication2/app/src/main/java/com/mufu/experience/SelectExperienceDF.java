package com.mufu.experience;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mufu.R;
import com.mufu.adapter.SelectShareExperienceAdapter;
import com.mufu.common.MyApplication;
import com.mufu.util.BaseDialogFragment;
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
import static com.mufu.experience.ExperienceSummaryActivity.GET_ALL_EXPERIENCES;
import static com.mufu.experience.ExperienceSummaryActivity.getExperience;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class SelectExperienceDF extends BaseDialogFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "SelectExperienceDF";
    private static final int PAGE_SIZE = 8;
    private static final int GET_ALL_DONE = 1;
    private static final int GET_ALL_END = 2;
    private static final int NO_MORE = 3;
    final int itemLimit = 1;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    private int mLoadSize = 0;
    private Handler handler;
    private MyHandler myHandler;
    private Dialog mDialog;
    private Window window;
    private SelectShareExperienceAdapter selectShareExperienceAdapter;
    private DialogFragmentCallbackInterface dialogFragmentCallbackInterface;
    public static final int EXPERIENCE_SELECT_RESULT = 0;
    private XRecyclerView recyclerView;
    private List<ExperienceSummaryActivity.Experience> mExperienceList = new ArrayList<>();
    
    public static SelectExperienceDF newInstance() {
        SelectExperienceDF selectExperienceDF = new SelectExperienceDF();
        return selectExperienceDF;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dialogFragmentCallbackInterface = (DialogFragmentCallbackInterface) context;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_MaterialComponents_DialogWhenLarge);
        mDialog.setContentView(R.layout.select_share_experience_list);
        myHandler = new SelectExperienceDF.MyHandler(this);

        mDialog.setCanceledOnTouchOutside(true);
        window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);
        
        TextView leftBack = mDialog.findViewById(R.id.left_back);
        leftBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });


        initView();

        loadData();

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);

        return mDialog;
    }
    
    private void initView() {
        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);

        handler = new SelectExperienceDF.MyHandler(this);
        recyclerView = mDialog.findViewById(R.id.select_experience_list);
        selectShareExperienceAdapter = new SelectShareExperienceAdapter(getContext());
        MyLinearLayoutManager linearLayoutManager = new MyLinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        
        recyclerView.setRefreshProgressStyle(BallSpinFadeLoader);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallRotate);

        recyclerView.setPullRefreshEnabled(false);
        recyclerView.getDefaultRefreshHeaderView().setRefreshTimeVisible(true);

        // When the item number of the screen number is list.size-2,we call the onLoadMore
        recyclerView.setLimitNumberToCallLoadMore(itemLimit);
        recyclerView.setRefreshProgressStyle(ProgressStyle.BallBeat);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);
        
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    selectShareExperienceAdapter.setScrolling(false);
                    selectShareExperienceAdapter.notifyDataSetChanged();
                } else {
                    selectShareExperienceAdapter.setScrolling(true);
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
        
        recyclerView.setAdapter(selectShareExperienceAdapter);
        selectShareExperienceAdapter.setItemClickListener(new SelectShareExperienceAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (dialogFragmentCallbackInterface != null) {//callback from ArchivesActivity class
                    dialogFragmentCallbackInterface.onBackFromDialog(mExperienceList.get(position).title, mExperienceList.get(position).eid, true);
                }
                mDialog.dismiss();
            }
        });
        
        //show progressImage before loading done
        progressImageView = mDialog.findViewById(R.id.animal_progress);
        animationDrawable = (AnimationDrawable) progressImageView.getDrawable();
        progressImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        }, 50);
    }
    
    private void loadData() {

        String uri = GET_ALL_EXPERIENCES;
        final int page = mExperienceList.size() / PAGE_SIZE;
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page));

        RequestBody requestBody = builder.build();
        
        HttpUtil.sendOkHttpRequest(getContext(), uri, requestBody, new Callback() {
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
    
    public void handleMessage(Message message) {
        switch (message.what) {
            case GET_ALL_DONE:
                Slog.d(TAG, "-------------->GET_ALL_DONE");
                selectShareExperienceAdapter.setData(mExperienceList);
                selectShareExperienceAdapter.notifyDataSetChanged();
                recyclerView.loadMoreComplete();
                stopLoadProgress();
                break;
            case GET_ALL_END:
                Slog.d(TAG, "-------------->GET_ALL_END");
                selectShareExperienceAdapter.setData(mExperienceList);
                selectShareExperienceAdapter.notifyDataSetChanged();
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

    public interface DialogFragmentCallbackInterface {
        void onBackFromDialog(String title, int eid, boolean status);
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
        WeakReference<SelectExperienceDF> selectExperienceDFWeakReference;

        MyHandler(SelectExperienceDF selectExperienceDF) {
            selectExperienceDFWeakReference = new WeakReference<>(selectExperienceDF);
        }

        @Override
        public void handleMessage(Message message) {
            SelectExperienceDF selectExperienceDF = selectExperienceDFWeakReference.get();
            if (selectExperienceDF != null) {
                selectExperienceDF.handleMessage(message);
            }
        }
    }
}
