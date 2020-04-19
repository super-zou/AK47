package com.hetang.experience;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.adapter.GridImageAdapter;
import com.hetang.common.HandlerTemp;
import com.hetang.common.OnItemClickListener;
import com.hetang.dynamics.AddDynamicsActivity;
import com.hetang.main.FullyGridLayoutManager;
import com.hetang.picture.GlideEngine;
import com.hetang.util.BaseDialogFragment;
import com.hetang.util.CommonDialogFragmentInterface;
import com.hetang.util.HttpUtil;
import com.hetang.util.Slog;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.style.PictureWindowAnimationStyle;
import com.willy.ratingbar.BaseRatingBar;
import com.willy.ratingbar.ScaleRatingBar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;
import static com.hetang.group.GroupFragment.eden_group;

public class ExperienceEvaluateDialogFragment extends BaseDialogFragment {
    public final static int SET_EVALUATE_RESULT_OK = 7;
    private static final String TAG = "ExperienceEvaluateDialogFragment";
    private static final String WRITE_EVALUATION_URL = HttpUtil.DOMAIN + "?q=travel_guide/write_evaluate";
    private static final int WRITE_EVALUATE_DONE = 0;
    final List<String> selectedFeatures = new ArrayList<>();
    private Context mContext;
    private Dialog mDialog;
    private View view;
    private boolean mEvaluated = false;
    private LayoutInflater inflater;
    private Handler handler = new ExperienceEvaluateDialogFragment.MyHandler(this);
    private List<String> impressionList = new ArrayList<>();
    private ProgressDialog progressDialog;
    private OrderSummaryActivity.Order order;
    private AddDynamicsActivity addDynamicsActivity;
    private RecyclerView recyclerView;
    private GridImageAdapter adapter;
    private List<LocalMedia> selectList = new ArrayList<>();
    private List<File> selectFileList = new ArrayList<>();
    
    public static ExperienceEvaluateDialogFragment newInstance(OrderSummaryActivity.Order order) {
        ExperienceEvaluateDialogFragment experienceEvaluateDialogFragment = new ExperienceEvaluateDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("order", order);
        experienceEvaluateDialogFragment.setArguments(bundle);

        return experienceEvaluateDialogFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            order = (OrderSummaryActivity.Order) bundle.getSerializable("order");
        }
        if (addDynamicsActivity == null){
            addDynamicsActivity = new AddDynamicsActivity();
        }
        inflater = LayoutInflater.from(mContext);
        mDialog = new Dialog(mContext, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        view = inflater.inflate(R.layout.experience_evaluation_dialog, null);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(view);
        mDialog.setCanceledOnTouchOutside(true);
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.TOP;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setDimAmount(0.8f);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        window.setAttributes(layoutParams);

        setImpressions();

        setEvaluationPicture();

        return mDialog;
    }
    
    private void setImpressions() {
        ImageView headPicture = view.findViewById(R.id.head_picture);
        Glide.with(getContext()).load(HttpUtil.DOMAIN + order.headPictureUrl).into(headPicture);
        TextView titleTV = view.findViewById(R.id.guide_title);
        titleTV.setText(order.title);

        ScaleRatingBar scaleRatingBar = view.findViewById(R.id.charm_rating_bar);
        final TextView charmRating = view.findViewById(R.id.charm_rating);
        
        scaleRatingBar.setOnRatingChangeListener(new BaseRatingBar.OnRatingChangeListener() {

            @Override
            public void onRatingChange(BaseRatingBar ratingBar, float rating) {
                Slog.d(TAG, "onRatingChange float: " + rating);
                charmRating.setText(String.valueOf(rating));
            }

        });

        Button saveEvaluate = view.findViewById(R.id.save_evaluate);
        final TextView errorNotice = view.findViewById(R.id.error_notice);
        final EditText contentET = view.findViewById(R.id.content);
        
        saveEvaluate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float rating = 0;

                if (charmRating.getText().toString() != null && !"".equals(charmRating.getText().toString())) {
                    rating = Float.parseFloat(charmRating.getText().toString());
                    mEvaluated = true;
                } else {
                    errorNotice.setVisibility(View.VISIBLE);
                    return;
                }

                String content = contentET.getText().toString();
                uploadToServer(content, rating);
            }
        });
    }
    
    private void setEvaluationPicture(){
        recyclerView = mDialog.findViewById(R.id.add_evaluation_picture);
        FullyGridLayoutManager manager = new FullyGridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);

        adapter = new GridImageAdapter(getContext(), onAddPicClickListener);
        adapter.setList(selectList);
        adapter.setSelectMax(6);
        recyclerView.setAdapter(adapter);
        
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                if (selectList.size() > 0) {
                    LocalMedia media = selectList.get(position);
                    String pictureType = media.getMimeType();
                    int mediaType = PictureMimeType.getMimeType(pictureType);
                    switch (mediaType) {
                        case PictureConfig.TYPE_IMAGE:
                            //PictureSelector.create(MainActivity.this).externalPicturePreview(position, "/custom_file", selectList);
                            PictureSelector.create(ExperienceEvaluateDialogFragment.this)
                                    .themeStyle(R.style.picture_WeChat_style)
                                    .setPictureStyle(addDynamicsActivity.getWeChatStyle())
                                    //.setPictureWindowAnimationStyle(animationStyle)//
                                    .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                                    .isNotPreviewDownload(true)
                                    //.bindCustomPlayVideoCallback(callback)
                                    .loadImageEngine(GlideEngine.createGlideEngine())
                                    .openExternalPreview(position, selectList);
                            break;
                    }
                }
            }
        });
    }
    
    private void uploadToServer(String content, float rating) {
        showProgress(mContext);
        FormBody.Builder builder = new FormBody.Builder()
                .add("tid", String.valueOf(order.id))
                .add("content", content)
                .add("rating", String.valueOf(rating));


        RequestBody requestBody = builder.build();
        
        HttpUtil.sendOkHttpRequest(getContext(), WRITE_EVALUATION_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null) {
                    String responseText = response.body().string();
                    Slog.d(TAG, "==========loadReferences response text : " + responseText);
                    if (responseText != null) {
                        mEvaluated = true;
                        handler.sendEmptyMessage(WRITE_EVALUATE_DONE);
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
            case WRITE_EVALUATE_DONE:
                mDialog.dismiss();
                break;
            default:
                break;
        }
    }
    
    private GridImageAdapter.onAddPicClickListener onAddPicClickListener = new GridImageAdapter.onAddPicClickListener() {
        @Override
        public void onAddPicClick() {
            //boolean mode = cb_mode.isChecked();
            boolean mode = true;
            if (mode) {
                PictureSelector.create(ExperienceEvaluateDialogFragment.this)
                        .openGallery(PictureMimeType.ofImage())
                        .loadImageEngine(GlideEngine.createGlideEngine())
                        .theme(R.style.picture_WeChat_style)
                        .isWeChatStyle(true)
                        .setPictureStyle(addDynamicsActivity.getWeChatStyle())
                        .setPictureCropStyle(addDynamicsActivity.getCropParameterStyle())
                        .setPictureWindowAnimationStyle(new PictureWindowAnimationStyle())
                        .isWithVideoImage(true)
                        .maxSelectNum(6)
                        .minSelectNum(1)
                        .maxVideoSelectNum(1)
                        .imageSpanCount(3)
                        .isReturnEmpty(false)
                        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        .selectionMode(PictureConfig.MULTIPLE)
                        .previewImage(true)
                        .isCamera(true)
                        .isZoomAnim(true)
                        .compress(true)
                        .compressQuality(100)
                        .synOrAsy(true)
                        .withAspectRatio(1, 1)
                        .freeStyleCropEnabled(true)
                        .previewEggs(true)
                        .minimumCompressSize(100)
                        .forResult(PictureConfig.CHOOSE_REQUEST);
                        }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    if (selectList.size() > 0) {
                        selectList.addAll(PictureSelector.obtainMultipleResult(data));
                    } else {
                        selectList = PictureSelector.obtainMultipleResult(data);
                    }
                    Slog.d(TAG, "Selected pictures: " + selectList.size());
                    adapter.setList(selectList);
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        //KeyboardUtils.hideSoftInput(getContext());

        closeProgressDialog();

        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
    }
    
    @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
    }

    private void showProgress(Context context) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(context.getResources().getString(R.string.saving_progress));
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
    
    static class MyHandler extends HandlerTemp<ExperienceEvaluateDialogFragment> {
        public MyHandler(ExperienceEvaluateDialogFragment cls) {
            super(cls);
        }

        @Override
        public void handleMessage(Message message) {
            ExperienceEvaluateDialogFragment experienceEvaluateDialogFragment = ref.get();
            if (experienceEvaluateDialogFragment != null) {
                experienceEvaluateDialogFragment.handleMessage(message);
            }
        }
    }
}
