package com.hetang.talent;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.adapter.GridImageAdapter;
import com.hetang.common.MyApplication;
import com.hetang.common.OnItemClickListener;
import com.hetang.dynamics.AddDynamicsActivity;
import com.hetang.group.SubGroupActivity;
import com.hetang.main.FullyGridLayoutManager;
import com.hetang.picture.GlideEngine;
import com.hetang.util.BaseDialogFragment;
import com.hetang.util.CommonBean;
import com.hetang.util.CommonDialogFragmentInterface;
import com.hetang.util.CommonPickerView;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.RoundImageView;
import com.hetang.util.Slog;
import com.luck.picture.lib.PictureSelectionModel;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.style.PictureWindowAnimationStyle;
import com.luck.picture.lib.tools.PictureFileUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;
import static com.hetang.common.SetAvatarActivity.SUBMIT_TALENT_AUTHENTICATION_ACTION_BROADCAST;
import static com.hetang.group.GroupFragment.eden_group;
import static com.hetang.group.SubGroupActivity.TALENT_ADD_BROADCAST;
import static com.hetang.group.SubGroupActivity.getTalent;

public class TalentModifyDialogFragment extends BaseDialogFragment {
    public final static int TALENT_MODIFY_RESULT_OK = 2;
    private static final boolean isDebug = true;
    private static final String TAG = "TalentAuthenticationDialogFragment";
    private static final String MODIFY_URL = HttpUtil.DOMAIN + "?q=talent/modify";
    private int type;
    private int gid;
    private int aid;
    private SubGroupActivity.Talent talent;
    private Dialog mDialog;
    private Context mContext;
    private String uri;
    private boolean isModified = false;
    private EditText introductionET;
    private CommonDialogFragmentInterface commonDialogFragmentInterface;

    private int index = 1;
    private JSONObject authenObject;
    private GridImageAdapter adapter;
    private GridImageAdapter adapterReward;
    private List<LocalMedia> selectList = new ArrayList<>();
    private List<File> selectFileList = new ArrayList<>();
    private List<LocalMedia> selectRewardList = new ArrayList<>();
    private List<File> selectRewardFileList = new ArrayList<>();
    private AddDynamicsActivity addDynamicsActivity;
    private RecyclerView addMateriaRV;
    private RecyclerView addRewardQRRV;
    private Button selectSubject;
    private EditText chargeSetting;
    private EditText chargeIntroduction;
    private Window window;
    private int maxSelectNum = 6;
    private boolean isPicked = false;
    private Thread threadIndustry = null;
    private ArrayList<CommonBean> subjectMainItems = new ArrayList<>();
    private ArrayList<ArrayList<String>> subjectSubItems = new ArrayList<>();

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

        mDialog = new Dialog(getActivity(), R.style.Theme_MaterialComponents_DialogWhenLarge);

        Bundle bundle = getArguments();
        if (bundle != null) {
            talent = (SubGroupActivity.Talent) bundle.getSerializable("talent");
        }

        mDialog.setContentView(R.layout.talent_modify);

        mDialog.setCanceledOnTouchOutside(true);
        window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        //subGroup = (SubGroupActivity.SubGroup) bundle.getSerializable("subGroup");
        TextView leftBack = mDialog.findViewById(R.id.left_back);
        leftBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        TextView title = mDialog.findViewById(R.id.title);
        title.setText(getContext().getResources().getString(R.string.modify_talent));

        TextView save = mDialog.findViewById(R.id.save);
        save.setVisibility(View.VISIBLE);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.talent_details_layout), font);

        if (addDynamicsActivity == null){
            addDynamicsActivity = new AddDynamicsActivity();
        }

        initCommonTalent();

        return mDialog;
    }

    private void initCommonTalent(){
        introductionET = mDialog.findViewById(R.id.introduction_edit);
        introductionET.setText(talent.introduction);
        selectSubject = mDialog.findViewById(R.id.select_subject);
        selectSubject.setText(talent.subject);

        addMateriaRV = mDialog.findViewById(R.id.add_materia);
        FullyGridLayoutManager manager = new FullyGridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false);
        FullyGridLayoutManager managerReward = new FullyGridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false);
        addMateriaRV.setLayoutManager(manager);

        //for authentication materia
        adapter = new GridImageAdapter(getContext(), onAddMaterialPicClickListener);
        adapter.setList(selectList);
        adapter.setSelectMax(6);
        addMateriaRV.setAdapter(adapter);
        addClickListen(adapter);

        initSubjectJsondata();
    }

    private void submit(){
        Map<String, String> authenMap = new HashMap<>();
        if (!TextUtils.isEmpty(introductionET.getText().toString())){
            authenMap.put("introduction", introductionET.getText().toString());
        }
        if (isPicked){
            authenMap.put("subject", selectSubject.getText().toString());
        }

        if (selectRewardList.size() > 0){
            for (LocalMedia media : selectRewardList) {
                selectFileList.add(new File(media.getCompressPath()));
            }
            authenMap.put("qr_code", "modify");
        }

        if (selectList.size() > 0){
            for (LocalMedia media : selectList) {
                selectFileList.add(new File(media.getCompressPath()));
            }
            authenMap.put("material", "modify");
        }

        authenMap.put("tid", String.valueOf(talent.tid));

        uploadPictures(authenMap, "authen", selectFileList);
    }

    private void initSubjectJsondata(){

        final CommonPickerView commonPickerView = new CommonPickerView();
        if (threadIndustry == null){
            Slog.i(TAG,"行业数据开始解析");
            threadIndustry = new Thread(new Runnable() {
                @Override
                public void run() {
                    subjectMainItems = commonPickerView.getOptionsMainItem(MyApplication.getContext(), "subject.json");
                    subjectSubItems = commonPickerView.getOptionsSubItems(subjectMainItems);
                }
            });
            threadIndustry.start();
        }

        selectSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPickerView();
            }
        });
    }

    private void showPickerView() {
        //条件选择器
        OptionsPickerView pvOptions;
        pvOptions= new OptionsPickerBuilder(getContext(), new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3 ,View v) {
                //返回的分别是二个级别的选中位置
                selectSubject.setText(subjectSubItems.get(options1).get(option2));
                isPicked = true;

            }
        }).setDecorView(window.getDecorView().findViewById(R.id.talent_authentication))
                .isDialog(true).setLineSpacingMultiplier(1.2f).isCenterLabel(false).build();

        pvOptions.getDialog().getWindow().setGravity(Gravity.CENTER);
        pvOptions.setPicker(subjectMainItems, subjectSubItems);
        pvOptions.show();
    }

    private void addClickListen(GridImageAdapter imageAdapter){
        imageAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                if (selectList.size() > 0) {
                    LocalMedia media = selectList.get(position);
                    String pictureType = media.getMimeType();
                    int mediaType = PictureMimeType.getMimeType(pictureType);
                    switch (mediaType) {
                        case PictureConfig.TYPE_IMAGE:
                            //PictureSelector.create(MainActivity.this).externalPicturePreview(position, "/custom_file", selectList);
                            PictureSelector.create(TalentModifyDialogFragment.this)
                                    .themeStyle(R.style.picture_WeChat_style) // xml设置主题
                                    .setPictureStyle(addDynamicsActivity.getWeChatStyle())// 动态自定义相册主题
                                    //.setPictureWindowAnimationStyle(animationStyle)// 自定义页面启动动画
                                    .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)// 设置相册Activity方向，不设置默认使用系统
                                    .isNotPreviewDownload(true)// 预览图片长按是否可以下载
                                    //.bindCustomPlayVideoCallback(callback)// 自定义播放回调控制，用户可以使用自己的视频播放界面
                                    .loadImageEngine(GlideEngine.createGlideEngine())// 外部传入图片加载引擎，必传项
                                    .openExternalPreview(position, selectList);
                            break;
                    }
                }
            }
        });
    }

    private GridImageAdapter.onAddPicClickListener onAddPicClickListener = new GridImageAdapter.onAddPicClickListener() {
        @Override
        public void onAddPicClick() {
            maxSelectNum = 1;
            PictureSelectionModel pictureSelectionModel = PictureSelector.create(TalentModifyDialogFragment.this).openGallery(PictureMimeType.ofImage());
            pictureSelectionModel.loadImageEngine(GlideEngine.createGlideEngine())// 外部传入图片加载引擎，必传项
                    .theme(R.style.picture_WeChat_style)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style v2.3.3后 建议使用setPictureStyle()动态方式
                    .isWeChatStyle(true)// 是否开启微信图片选择风格
                    //.setLanguage(language)// 设置语言，默认中文
                    .setPictureStyle(addDynamicsActivity.getWeChatStyle())// 动态自定义相册主题
                    .setPictureCropStyle(addDynamicsActivity.getCropParameterStyle())// 动态自定义裁剪主题
                    .setPictureWindowAnimationStyle(new PictureWindowAnimationStyle())// 自定义相册启动退出动画
                    .isWithVideoImage(true)// 图片和视频是否可以同选,只在ofAll模式下有效
                    .maxSelectNum(maxSelectNum)// 最大图片选择数量
                    .minSelectNum(1)// 最小选择数量
                    .imageSpanCount(4)// 每行显示个数
                    .isReturnEmpty(false)// 未选择数据时点击按钮是否可以返回
                    //.isAndroidQTransform(false)// 是否需要处理Android Q 拷贝至应用沙盒的操作，只针对compress(false); && enableCrop(false);有效,默认处理
                    .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)// 设置相册Activity方向，不设置默认使用系统
                    .selectionMode(PictureConfig.MULTIPLE )// 多选 or 单选
                    //.isSingleDirectReturn(cb_single_back.isChecked())// 单选模式下是否直接返回，PictureConfig.SINGLE模式下有效
                    .previewImage(true)// 是否可预览图片
                    //.previewVideo(cb_preview_video.isChecked())// 是否可预览视频
                    //.querySpecifiedFormatSuffix(PictureMimeType.ofJPEG())// 查询指定后缀格式资源
                    //.enablePreviewAudio(cb_preview_audio.isChecked()) // 是否可播放音频
                    .isCamera(true)// 是否显示拍照按钮
                    //.isMultipleSkipCrop(false)// 多图裁剪时是否支持跳过，默认支持
                    //.isMultipleRecyclerAnimation(false)// 多图裁剪底部列表显示动画效果
                    .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                    .compress(true)// 是否压缩
                    .compressQuality(80)// 图片压缩后输出质量 0~ 100
                    .synOrAsy(true)//同步true或异步false 压缩 默认同步
                    .withAspectRatio(1, 1)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                    .freeStyleCropEnabled(true)// 裁剪框是否可拖拽
                    .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                    .minimumCompressSize(100);

            pictureSelectionModel.forResult(PictureConfig.SINGLE);
        }
    };

    private GridImageAdapter.onAddPicClickListener onAddMaterialPicClickListener = new GridImageAdapter.onAddPicClickListener() {
        @Override
        public void onAddPicClick() {
            maxSelectNum = 6;
            PictureSelectionModel pictureSelectionModel = PictureSelector.create(TalentModifyDialogFragment.this).openGallery(PictureMimeType.ofImage());
            pictureSelectionModel.loadImageEngine(GlideEngine.createGlideEngine())// 外部传入图片加载引擎，必传项
                    .theme(R.style.picture_WeChat_style)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style v2.3.3后 建议使用setPictureStyle()动态方式
                    .isWeChatStyle(true)// 是否开启微信图片选择风格
                    //.setLanguage(language)// 设置语言，默认中文
                    .setPictureStyle(addDynamicsActivity.getWeChatStyle())// 动态自定义相册主题
                    .setPictureCropStyle(addDynamicsActivity.getCropParameterStyle())// 动态自定义裁剪主题
                    .setPictureWindowAnimationStyle(new PictureWindowAnimationStyle())// 自定义相册启动退出动画
                    .isWithVideoImage(true)// 图片和视频是否可以同选,只在ofAll模式下有效
                    .maxSelectNum(maxSelectNum)// 最大图片选择数量
                    .minSelectNum(1)// 最小选择数量
                    .imageSpanCount(4)// 每行显示个数
                    .isReturnEmpty(false)// 未选择数据时点击按钮是否可以返回
                    //.isAndroidQTransform(false)// 是否需要处理Android Q 拷贝至应用沙盒的操作，只针对compress(false); && enableCrop(false);有效,默认处理
                    .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)// 设置相册Activity方向，不设置默认使用系统
                    .selectionMode(PictureConfig.MULTIPLE )// 多选 or 单选
                    //.isSingleDirectReturn(cb_single_back.isChecked())// 单选模式下是否直接返回，PictureConfig.SINGLE模式下有效
                    .previewImage(true)// 是否可预览图片
                    //.previewVideo(cb_preview_video.isChecked())// 是否可预览视频
                    //.querySpecifiedFormatSuffix(PictureMimeType.ofJPEG())// 查询指定后缀格式资源
                    //.enablePreviewAudio(cb_preview_audio.isChecked()) // 是否可播放音频
                    .isCamera(true)// 是否显示拍照按钮
                    //.isMultipleSkipCrop(false)// 多图裁剪时是否支持跳过，默认支持
                    //.isMultipleRecyclerAnimation(false)// 多图裁剪底部列表显示动画效果
                    .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                    .compress(true)// 是否压缩
                    .compressQuality(80)// 图片压缩后输出质量 0~ 100
                    .synOrAsy(true)//同步true或异步false 压缩 默认同步
                    .withAspectRatio(1, 1)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                    .freeStyleCropEnabled(true)// 裁剪框是否可拖拽
                    .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                    .minimumCompressSize(100);

            pictureSelectionModel.forResult(PictureConfig.CHOOSE_REQUEST);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    selectList = PictureSelector.obtainMultipleResult(data);
                    if(selectList.size() > 0){
                        adapter.setList(selectList);
                        adapter.notifyDataSetChanged();
                    }
                    break;
                case PictureConfig.SINGLE:
                    selectRewardList = PictureSelector.obtainMultipleResult(data);
                    if(selectRewardList.size() > 0){
                        adapterReward.setList(selectRewardList);
                        adapterReward.notifyDataSetChanged();
                    }
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PictureConfig.APPLY_STORAGE_PERMISSIONS_CODE:
                // 存储权限
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        PictureFileUtils.deleteCacheDirFile(getContext(), PictureMimeType.ofImage());
                    } else {
                        Toast.makeText(getContext(), getString(R.string.picture_jurisdiction), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private void uploadPictures(Map<String, String> params, String picKey, List<File> files) {
        showProgressDialog("");
        HttpUtil.uploadPictureHttpRequest(getContext(), params, picKey, files, MODIFY_URL, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    try {
                        String responseText = response.body().string();
                        Slog.d(TAG, "---------------->response: "+responseText);
                        int status = new JSONObject(responseText).optInt("status");
                        if(status == 1){
                            isModified = true;
                            dismissProgressDialog();
                            selectList.clear();
                            selectFileList.clear();
                            PictureFileUtils.deleteAllCacheDirFile(getContext());
                            startTalentDetailActivity();
                        }
                    }catch (JSONException e){
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
            }
        });

    }

    private void startTalentDetailActivity(){
        Intent intent = new Intent(getContext(), TalentDetailsActivity.class);
        intent.putExtra("tid", talent.tid);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(intent);
        mDialog.dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
        if (commonDialogFragmentInterface != null) {//callback from ArchivesActivity class
            commonDialogFragmentInterface.onBackFromDialog(TALENT_MODIFY_RESULT_OK, aid, isModified);
        }
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
    }

}
