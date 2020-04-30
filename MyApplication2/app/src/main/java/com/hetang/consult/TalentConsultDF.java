package com.hetang.consult;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hetang.R;
import com.hetang.adapter.GridImageAdapter;
import com.hetang.common.HandlerTemp;
import com.hetang.common.MyApplication;
import com.hetang.common.OnItemClickListener;
import com.hetang.dynamics.AddDynamicsActivity;
import com.hetang.experience.OrderSummaryActivity;

import com.hetang.main.FullyGridLayoutManager;
import com.hetang.picture.GlideEngine;
import com.hetang.util.BaseDialogFragment;
import com.hetang.util.CommonDialogFragmentInterface;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.Slog;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.style.PictureWindowAnimationStyle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;

public class TalentConsultDF extends BaseDialogFragment {
    private static final String TAG = "TalentConsultDF";
    private static final String WRITE_CONSULT_URL = HttpUtil.DOMAIN + "?q=consult/write_consult";
    private static final int PUBLISH_CONSULT_DONE = 0;
    final List<String> selectedFeatures = new ArrayList<>();
    private Context mContext;
    private Dialog mDialog;
    private View view;
    private int tid;
    private int cid;
    private String name;
    private int rewardIndex = -1;
    private int rewardAmount = -1;
    
    private LayoutInflater inflater;
    private Handler handler = new TalentConsultDF.MyHandler(this);
    private List<String> impressionList = new ArrayList<>();
    private ProgressDialog progressDialog;
    private OrderSummaryActivity.Order order;
    private AddDynamicsActivity addDynamicsActivity;
    private RecyclerView recyclerView;
    private GridImageAdapter adapter;
    private List<LocalMedia> selectList = new ArrayList<>();
    private List<File> selectFileList = new ArrayList<>();
    private CommonDialogFragmentInterface commonDialogFragmentInterface;
    
    public static TalentConsultDF newInstance(int tid, String name) {
        TalentConsultDF experienceEvaluateDialogFragment = new TalentConsultDF();
        Bundle bundle = new Bundle();
        bundle.putInt("tid", tid);
        bundle.putString("name", name);
        experienceEvaluateDialogFragment.setArguments(bundle);

        return experienceEvaluateDialogFragment;
    }
    
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        try {
            commonDialogFragmentInterface = (CommonDialogFragmentInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement commonDialogFragmentInterface");
        }
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            tid = bundle.getInt("tid");
            name = bundle.getString("name");
        }
        if (addDynamicsActivity == null){
            addDynamicsActivity = new AddDynamicsActivity();
        }
        
        inflater = LayoutInflater.from(mContext);
        mDialog = new Dialog(mContext, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        view = inflater.inflate(R.layout.talent_consult_dialog, null);
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
        
        TextView back = mDialog.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });

        TextView title = mDialog.findViewById(R.id.title);
        title.setText("咨询"+name);

        setConsultPictureWidget();
        
        initView();

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.reward_value_wrapper), font);

        return mDialog;
    }
    
    private void setConsultPictureWidget(){
        recyclerView = mDialog.findViewById(R.id.question_picture);
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
                            PictureSelector.create(TalentConsultDF.this)
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
    
    private void initView() {
        LinearLayout rewardWrapper = mDialog.findViewById(R.id.reward_value_wrapper);
        ConstraintLayout freeReward = mDialog.findViewById(R.id.free_reward);
        freeReward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                freeReward.setBackgroundColor(getContext().getResources().getColor(R.color.color_light_grey));
                if (rewardIndex != 0 && rewardIndex > -1){
                    rewardWrapper.getChildAt(rewardIndex).setBackgroundColor(getContext().getResources().getColor(R.color.white));
                }
                rewardIndex = 0;
                rewardAmount = 0;
            }
        });
        
        ConstraintLayout lowReward = mDialog.findViewById(R.id.low_reward);
        lowReward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lowReward.setBackgroundColor(getContext().getResources().getColor(R.color.color_light_grey));
                if (rewardIndex != 1 && rewardIndex > -1){
                    rewardWrapper.getChildAt(rewardIndex).setBackgroundColor(getContext().getResources().getColor(R.color.white));
                }
                rewardIndex = 1;
                rewardAmount = 2;
            }
        });
        
        ConstraintLayout mediumReward = mDialog.findViewById(R.id.medium_reward);
        mediumReward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediumReward.setBackgroundColor(getContext().getResources().getColor(R.color.color_light_grey));
                if (rewardIndex != 2 && rewardIndex > -1){
                    rewardWrapper.getChildAt(rewardIndex).setBackgroundColor(getContext().getResources().getColor(R.color.white));
                }
                rewardIndex = 2;
                rewardAmount = 10;
            }
        });
        
        ConstraintLayout highReward = mDialog.findViewById(R.id.high_reward);
        highReward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                highReward.setBackgroundColor(getContext().getResources().getColor(R.color.color_light_grey));
                if (rewardIndex != 3 && rewardIndex > -1){
                    rewardWrapper.getChildAt(rewardIndex).setBackgroundColor(getContext().getResources().getColor(R.color.white));
                }
                rewardIndex = 3;
                rewardAmount = 50;
            }
        });
        
        Button publishBtn = mDialog.findViewById(R.id.publish_consult);
        publishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitConsult();
            }
        });
    }
    
    private void submitConsult() {
        AppCompatEditText questionET = mDialog.findViewById(R.id.question_content);
        String question = questionET.getText().toString();
        if (TextUtils.isEmpty(question)){
            Toast.makeText(getContext(), "请输入您要咨询的问题", Toast.LENGTH_LONG).show();
            return;
        }

        if (rewardIndex == -1){
            Toast.makeText(getContext(), "请设置打赏金额", Toast.LENGTH_LONG).show();
            return;
        }
        
        showProgressDialog(getContext().getString(R.string.saving_progress));

        Map<String, String> consultMap = new HashMap<>();

        consultMap.put("tid", String.valueOf(tid));
        consultMap.put("question", String.valueOf(question));
        consultMap.put("amount", String.valueOf(rewardAmount));

        if (selectList.size() > 0) {
            Slog.d(TAG, "----------------->submitConsult selectList: " + selectList);
            for (LocalMedia media : selectList) {
                selectFileList.add(new File(media.getCompressPath()));
            }
        }
        
        uploadPictures(consultMap, "consult", selectFileList);

    }

    private void uploadPictures(Map<String, String> params, String picKey, List<File> files) {
        Slog.d(TAG, "--------------------->uploadPictures file size: " + files.size());
        String uri = WRITE_CONSULT_URL;

        HttpUtil.uploadPictureHttpRequest(getContext(), params, picKey, files, uri, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                try {
                        String responseText = response.body().string();
                        Slog.d(TAG, "---------------->uploadPictures response: " + responseText);
                        int result = new JSONObject(responseText).optInt("result");
                        cid = new JSONObject(responseText).optInt("cid");
                        if (result == 1) {
                            dismissProgressDialog();
                            selectList.clear();
                            selectFileList.clear();
                            //PictureFileUtils.deleteAllCacheDirFile(MyApplication.getContext());
                            handler.sendEmptyMessage(PUBLISH_CONSULT_DONE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.submit_error), Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();

            }
        });

    }
    
    public void handleMessage(Message message) {
        switch (message.what) {
            case PUBLISH_CONSULT_DONE:
                commonDialogFragmentInterface.onBackFromDialog(cid, tid, true);
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
                PictureSelector.create(TalentConsultDF.this)
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

    static class MyHandler extends HandlerTemp<TalentConsultDF> {
        public MyHandler(TalentConsultDF cls) {
            super(cls);
        }
        
         @Override
        public void handleMessage(Message message) {
            TalentConsultDF experienceEvaluateDialogFragment = ref.get();
            if (experienceEvaluateDialogFragment != null) {
                experienceEvaluateDialogFragment.handleMessage(message);
            }
        }
    }
}
