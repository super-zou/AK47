package com.mufu.experience;

import android.app.Dialog;
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
import com.mufu.adapter.ShowAllEvaluatesAdapter;
import com.mufu.common.MyApplication;
import com.mufu.picture.GlideEngine;
import com.mufu.util.BaseDialogFragment;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.MyLinearLayoutManager;

import com.mufu.util.Slog;
import com.mufu.util.Utility;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.entity.LocalMedia;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class ShowAllEvaluateDF extends BaseDialogFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "ShowAllEvaluateDF";
    private static final int PAGE_SIZE = 8;
    private static final String GET_ALL_EVALUATES = HttpUtil.DOMAIN + "?q=travel_guide/get_all_evaluates";
    private static final String GET_EXPERIENCE_ALL_EVALUATES = HttpUtil.DOMAIN + "?q=experience/get_all_experience_evaluates";
    private static final int GET_ALL_DONE = 1;
    private static final int GET_ALL_END = 2;
    private static final int NO_MORE = 3;
    final int itemLimit = 1;
    int sid;
    int eid;
    int count;
    int mType;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    private int mLoadSize = 0;
    private Handler handler;
    private MyHandler myHandler;
    private Dialog mDialog;
    private Window window;
    private ShowAllEvaluatesAdapter showAllEvaluatesAdapter;
    private XRecyclerView recyclerView;
    private List<Evaluate> mEvaluateList = new ArrayList<>();
    
    public static ShowAllEvaluateDF newInstance(int type, int id, int count) {
        ShowAllEvaluateDF showAllEvaluateDF = new ShowAllEvaluateDF();
        Bundle bundle = new Bundle();
        if (type == Utility.TalentType.GUIDE.ordinal()){
            bundle.putInt("sid", id);
        }else {
            bundle.putInt("eid", id);
        }
        bundle.putInt("count", count);
        bundle.putInt("type", type);
        showAllEvaluateDF.setArguments(bundle);

        return showAllEvaluateDF;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    
    mDialog = new Dialog(getActivity(), R.style.Theme_MaterialComponents_DialogWhenLarge);
        mDialog.setContentView(R.layout.guide_evaluate_list);
        myHandler = new ShowAllEvaluateDF.MyHandler(this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mType = bundle.getInt("type");
            if (mType == Utility.TalentType.GUIDE.ordinal()){
                sid = bundle.getInt("sid");
            }else {
                eid = bundle.getInt("eid");
            }
            count = bundle.getInt("count");
        }

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
        
        TextView countTV = mDialog.findViewById(R.id.evaluate_count);
        countTV.setText(String.valueOf(count)+"条评价");

        initView();

        loadData();

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);

        return mDialog;
    }
    
     private void initView() {
        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);

        handler = new ShowAllEvaluateDF.MyHandler(this);
        recyclerView = mDialog.findViewById(R.id.guide_evaluate_list);
        showAllEvaluatesAdapter = new ShowAllEvaluatesAdapter(getContext());
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
                    showAllEvaluatesAdapter.setScrolling(false);
                    showAllEvaluatesAdapter.notifyDataSetChanged();
                } else {
                    showAllEvaluatesAdapter.setScrolling(true);
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



        recyclerView.setAdapter(showAllEvaluatesAdapter);
        
        showAllEvaluatesAdapter.setItemClickListener(new ShowAllEvaluatesAdapter.PictureClickListener() {
            @Override
            public void onPictureClick(View view, int position, JSONArray pictureUrlArray, int index) {
                startPicturePreview(index, pictureUrlArray);
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
    
    public void startPicturePreview(int position, JSONArray pictureUrlArray){

        List<LocalMedia> localMediaList = new ArrayList<>();
        for (int i=0; i<pictureUrlArray.length(); i++){
            LocalMedia localMedia = new LocalMedia();
            localMedia.setPath(HttpUtil.getDomain()+pictureUrlArray.opt(i));
            localMediaList.add(localMedia);
        }

        PictureSelector.create(this)
                .themeStyle(R.style.picture_default_style) // xml设置主题
                .loadImageEngine(GlideEngine.createGlideEngine())// 外部传入图片加载引擎，必传项
                .isNotPreviewDownload(true)
                .openExternalPreview(position, localMediaList);

    }
    
    private void loadData() {

        String uri = GET_ALL_EVALUATES;
        final int page = mEvaluateList.size() / PAGE_SIZE;
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page));

        if (mType == Utility.TalentType.GUIDE.ordinal()){
            Slog.d(TAG, " sid: "+sid);
            builder.add("sid", String.valueOf(sid));
        }else {
            Slog.d(TAG, " eid: "+eid);
            builder.add("eid", String.valueOf(eid));
            uri = GET_EXPERIENCE_ALL_EVALUATES;
        }
        RequestBody requestBody = builder.build();
                
        HttpUtil.sendOkHttpRequest(getContext(), uri, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========loadData response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject evaluatesResponse = null;
                        try {
                            evaluatesResponse = new JSONObject(responseText);
                            if (evaluatesResponse != null) {
                                mLoadSize = processEvaluatesResponse(evaluatesResponse);
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
    
    public int processEvaluatesResponse(JSONObject evaluatesObject) {
        int evaluateSize = 0;
        JSONArray evaluateArray = null;

        if (evaluatesObject != null) {
            evaluateArray = evaluatesObject.optJSONArray("evaluates");
            Slog.d(TAG, "------------------->processGuidesResponse: "+evaluateArray);
        }
        
        if (evaluateArray != null) {
            evaluateSize = evaluateArray.length();
            if (evaluateSize > 0) {
                for (int i = 0; i < evaluateArray.length(); i++) {
                    JSONObject guideObject = evaluateArray.optJSONObject(i);
                    if (guideObject != null) {
                        Evaluate evaluate = getEvaluate(guideObject);
                        mEvaluateList.add(evaluate);
                    }
                }
            }
        }

        return evaluateSize;
    }
    
     public static class Evaluate implements Serializable {
        public int eid;
        public String avatar;
        public int created;
        public String name;
        public int rating;
        public String content;
        public JSONArray pictureArray;
    }

    public static Evaluate getEvaluate(JSONObject evaluateObject) {
        Evaluate evaluate = new Evaluate();
        if (evaluateObject != null) {
            evaluate.eid = evaluateObject.optInt("eid");
            evaluate.avatar = evaluateObject.optString("avatar");
            evaluate.created = evaluateObject.optInt("created");
            evaluate.name = evaluateObject.optString("nickname");
            evaluate.rating = evaluateObject.optInt("rating");
            evaluate.content = evaluateObject.optString("content");
            evaluate.pictureArray = evaluateObject.optJSONArray("pictures");
        }

        return evaluate;
    }

  public void handleMessage(Message message) {
        switch (message.what) {
            case GET_ALL_DONE:
                Slog.d(TAG, "-------------->GET_ALL_DONE");
                showAllEvaluatesAdapter.setData(mEvaluateList);
                showAllEvaluatesAdapter.notifyDataSetChanged();
                recyclerView.loadMoreComplete();
                stopLoadProgress();
                break;
            case GET_ALL_END:
                Slog.d(TAG, "-------------->GET_ALL_END");
                showAllEvaluatesAdapter.setData(mEvaluateList);
                showAllEvaluatesAdapter.notifyDataSetChanged();
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

    static class MyHandler extends Handler {
        WeakReference<ShowAllEvaluateDF> showAllEvaluateDFWeakReference;
        
        MyHandler(ShowAllEvaluateDF showAllEvaluateDF) {
            showAllEvaluateDFWeakReference = new WeakReference<>(showAllEvaluateDF);
        }

        @Override
        public void handleMessage(Message message) {
            ShowAllEvaluateDF showAllEvaluateDF = showAllEvaluateDFWeakReference.get();
            if (showAllEvaluateDF != null) {
                showAllEvaluateDF.handleMessage(message);
            }
        }
    }
}
            
