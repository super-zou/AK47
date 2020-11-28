package com.mufu.experience;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mufu.R;
import com.mufu.adapter.GridImageAdapter;
import com.mufu.common.BaseAppCompatActivity;
import com.mufu.common.OnItemClickListener;
import com.mufu.main.DynamicFragment;
import com.mufu.main.FullyGridLayoutManager;
import com.mufu.picture.GlideEngine;
import com.mufu.util.ExMultipartBody;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.ParseUtils;
import com.mufu.util.Slog;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.permissions.PermissionChecker;
import com.luck.picture.lib.style.PictureCropParameterStyle;
import com.luck.picture.lib.style.PictureParameterStyle;
import com.luck.picture.lib.style.PictureWindowAnimationStyle;
import com.luck.picture.lib.tools.PictureFileUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.mufu.common.MyApplication.getContext;
import static com.mufu.util.ParseUtils.WRITE_SHARE_EXPERIENCE;

public class WriteShareActivity extends BaseAppCompatActivity implements SelectExperienceDF.DialogFragmentCallbackInterface {

public static final String DYNAMICS_ADD_BROADCAST = "com.hetang.action.DYNAMICS_ADD";
    private static final String TAG = "WriteShareActivity";
    private int maxSelectNum = 9;
    private int themeId;
    private String mTitle;
    private int mEid = 0;
    private TextView publishBtn;
    private TextView backLeft;
    private TextView selectedExprienceTitleTV;
    private EditText editText;
    private MyHandler myHandler;
    private RecyclerView recyclerView;
    private GridImageAdapter adapter;
    private List<LocalMedia> selectList = new ArrayList<>();
        public static final int UPDATEPROGRESS = 0;
    public static final int UPDATEPROGRESSCOMPLETE = 30;
    
    private PictureWindowAnimationStyle mWindowAnimationStyle;
    private List<File> selectFileList = new ArrayList<>();
    private Map<String, String> dynamicsText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            // 被回收
        } else {
            clearCache();
        }
        setContentView(R.layout.write_share_experience);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        
        myHandler = new MyHandler(this);
        editText = findViewById(R.id.dynamics_input);
        publishBtn = findViewById(R.id.dynamic_publish);
        backLeft = findViewById(R.id.left_back);
        selectedExprienceTitleTV = findViewById(R.id.selected_experience_title);
        themeId = R.style.picture_WeChat_style;
        recyclerView = findViewById(R.id.recycler);
        FullyGridLayoutManager manager = new FullyGridLayoutManager(WriteShareActivity.this, 3, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);
        
        adapter = new GridImageAdapter(WriteShareActivity.this, onAddPicClickListener);
        adapter.setList(selectList);
        adapter.setSelectMax(maxSelectNum);
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
                            PictureSelector.create(WriteShareActivity.this)
                                    .themeStyle(themeId) // xml设置主题
                                    .setPictureStyle(getWeChatStyle())// 动态自定义相册主题
                                    //.setPictureWindowAnimationStyle(animationStyle)// 自定义页面启动动画
                                    .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)// 设置相册Activity方向，不设置默认使用系统
                                    .isNotPreviewDownload(true)// 预览图片长按是否可以下载
                                    //.bindCustomPlayVideoCallback(callback)// 自定义播放回调控制，用户可以使用自己的视频播放界面
                                    .loadImageEngine(GlideEngine.createGlideEngine())// 外部传入图片加载引擎，必传项
                                    .openExternalPreview(position, selectList);
                            break;
                        case PictureConfig.TYPE_VIDEO:
                            PictureSelector.create(WriteShareActivity.this).externalPictureVideo(media.getPath());
                            // 预览视频
                            PictureSelector.create(WriteShareActivity.this)
                                    .themeStyle(themeId)
                                    .setPictureStyle(getWeChatStyle())// 动态自定义相册主题
                                    .externalPictureVideo(media.getPath());
                            break;
                            case PictureConfig.TYPE_AUDIO:
                            // 预览音频
                            PictureSelector.create(WriteShareActivity.this)
                                    .externalPictureAudio(
                                            media.getPath().startsWith("content://") ? media.getAndroidQToPath() : media.getPath());
                            break;
                    }
                }
            }
        });
        
        publishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mTitle)){
                    Toast.makeText(getContext(), "请选择要分享的体验",Toast.LENGTH_LONG).show();
                    return;
                }
                //showProgressDialog("正在保存...");
                String shareDesc = editText.getText().toString();
                dynamicsText = new HashMap<>();
                if (!TextUtils.isEmpty(shareDesc)) {
                    dynamicsText.put("text", shareDesc);
                }else {
                    Toast.makeText(getContext(), "请描述您的体验感受",Toast.LENGTH_LONG).show();
                    return;
                }
                dynamicsText.put("type", String.valueOf(WRITE_SHARE_EXPERIENCE));
                dynamicsText.put("eid", String.valueOf(mEid));
                dynamicsText.put("title", mTitle);
                
                if (selectList.size() > 0){
                    for (LocalMedia media : selectList) {
                        Slog.d(TAG, "===========num: " + media.getNum());
                        //activity_picture_array[media.getNum() - 1] = media.getCompressPath();
                        selectFileList.add(new File(media.getCompressPath()));
                    }
                }
                uploadPictures(dynamicsText, "picture", selectFileList);
            }
        });
        
        backLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button selectShareBtn = findViewById(R.id.select_experience);
        selectShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectExperienceDF selectExperienceDF = SelectExperienceDF.newInstance();
                selectExperienceDF.show(getSupportFragmentManager(), "SelectExperienceDF");
            }
        });
        
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.add_dynamics_statusbar), font);

    }

    private void uploadPictures(Map<String, String> params, String picKey, List<File> files) {

        HttpUtil.uploadPictureProgressHttpRequest(this, params, picKey, files, ParseUtils.DYNAMIC_ADD, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
            if (response.body() != null) {
                    try {
                        String responseText = response.body().string();
                        Slog.d(TAG, "---------------->response: "+responseText);

                        int saveStatus = new JSONObject(responseText).optInt("status");
                        Slog.d(TAG, "-------------->save status: "+saveStatus);

                        if(saveStatus == 1){
                            selectList.clear();
                            selectFileList.clear();
                            //sendBroadcast();//send broadcast to meetdynamicsfragment notify  meet dynamics to update
                            //setCommentUpdateResult();
                            myHandler.sendEmptyMessage(DynamicFragment.DYNAMICS_UPDATE_RESULT);
                            PictureFileUtils.deleteAllCacheDirFile(getContext());
                            //dismissProgressDialog();
                            // finish();
         }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WriteShareActivity.this, "Ê§°Ü", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            }, (contentLength, currentLength) -> {
           //Slog.d(TAG, "------------->onProgress contentLength: "+contentLength+" currentLength: "+currentLength);
           Message msg = new Message();
           Bundle bundle = new Bundle();
           bundle.putLong("maxLength", contentLength);
           bundle.putInt("currentLength", currentLength);
           msg.setData(bundle);
           msg.what = UPDATEPROGRESS;
           myHandler.sendMessage(msg);
       });

    }


    private GridImageAdapter.onAddPicClickListener onAddPicClickListener = new GridImageAdapter.onAddPicClickListener() {
        @Override
        public void onAddPicClick() {
            //boolean mode = cb_mode.isChecked();
            boolean mode = true;
            if (mode) {
                PictureSelector.create(WriteShareActivity.this)
                        .openGallery(PictureMimeType.ofImage())
                        .loadImageEngine(GlideEngine.createGlideEngine())// 外部传入图片加载引擎，必传项
                        .theme(themeId)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style v2.3.3后 建议使用setPictureStyle()动态方式
                        .isWeChatStyle(true)// 是否开启微信图片选择风格
                        //.isUseCustomCamera(cb_custom_camera.isChecked())// 是否使用自定义相机
                        //.setLanguage(language)// 设置语言，默认中文
                        .setPictureStyle(getWeChatStyle())// 动态自定义相册主题
                        .setPictureCropStyle(getCropParameterStyle())// 动态自定义裁剪主题
                        .setPictureWindowAnimationStyle(new PictureWindowAnimationStyle())// 自定义相册启动退出动画
                        .isWithVideoImage(true)// 图片和视频是否可以同选,只在ofAll模式下有效
                        .maxSelectNum(maxSelectNum)// 最大图片选择数量
                        .minSelectNum(1)// 最小选择数量
                        .maxVideoSelectNum(1) // 视频最大选择数量，如果没有单独设置的需求则可以不设置，同用maxSelectNum字段
                        //.minVideoSelectNum(1)// 视频最小选择数量，如果没有单独设置的需求则可以不设置，同用minSelectNum字段
                        .imageSpanCount(4)// 每行显示个数
                        .isReturnEmpty(false)// 未选择数据时点击按钮是否可以返回
                        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                         .selectionMode(PictureConfig.MULTIPLE )// 多选 or 单选
                         .previewImage(true)// 是否可预览图片
                         .isCamera(true)// 是否显示拍照按钮
                         .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                          .compress(true)// 是否压缩
                        .compressQuality(100)// 图片压缩后输出质量 0~ 100
                        .synOrAsy(true)//同步true或异步false 压缩 默认同步
                        .withAspectRatio(1, 1)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                        .freeStyleCropEnabled(true)// 裁剪框是否可拖拽
                        .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                        .minimumCompressSize(100)// 小于100kb的图片不压缩
                        .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
            }
        }

    };
    
    public PictureParameterStyle getWeChatStyle() {
        // 相册主题
        PictureParameterStyle pictureParameterStyle = new PictureParameterStyle();
        // 是否改变状态栏字体颜色(黑白切换)
        pictureParameterStyle.isChangeStatusBarFontColor = false;
        // 是否开启右下角已完成(0/9)风格
        pictureParameterStyle.isOpenCompletedNumStyle = false;
        // 是否开启类似QQ相册带数字选择风格
        pictureParameterStyle.isOpenCheckNumStyle = true;
        // 状态栏背景色
        pictureParameterStyle.pictureStatusBarColor = Color.parseColor("#393a3e");
        // 相册列表标题栏背景色
        pictureParameterStyle.pictureTitleBarBackgroundColor = Color.parseColor("#393a3e");
        // 相册父容器背景色
        pictureParameterStyle.pictureContainerBackgroundColor = ContextCompat.getColor(getContext(), R.color.color_light_grey);
        // 相册列表标题栏右侧上拉箭头
        pictureParameterStyle.pictureTitleUpResId = R.drawable.picture_icon_wechat_up;
        // 相册列表标题栏右侧下拉箭头
        pictureParameterStyle.pictureTitleDownResId = R.drawable.picture_icon_wechat_down;
        // 相册文件夹列表选中圆点
        pictureParameterStyle.pictureFolderCheckedDotStyle = R.drawable.picture_orange_oval;
        // 相册返回箭头
        pictureParameterStyle.pictureLeftBackIcon = R.drawable.picture_icon_close;
        // 标题栏字体颜色
        pictureParameterStyle.pictureTitleTextColor = ContextCompat.getColor(getContext(), R.color.picture_color_white);
        // 相册右侧按钮字体颜色  废弃 改用.pictureRightDefaultTextColor和.pictureRightDefaultTextColor
        pictureParameterStyle.pictureCancelTextColor = ContextCompat.getColor(getContext(), R.color.picture_color_53575e);
        // 相册右侧按钮字体默认颜色
        pictureParameterStyle.pictureRightDefaultTextColor = ContextCompat.getColor(getContext(), R.color.picture_color_53575e);
        // 相册右侧按可点击字体颜色,只针对isWeChatStyle 为true时有效果
        pictureParameterStyle.pictureRightSelectedTextColor = ContextCompat.getColor(getContext(), R.color.picture_color_white);
        // 相册右侧按钮背景样式,只针对isWeChatStyle 为true时有效果
        pictureParameterStyle.pictureUnCompleteBackgroundStyle = R.drawable.picture_send_button_default_bg;
        // 相册右侧按钮可点击背景样式,只针对isWeChatStyle 为true时有效果
        pictureParameterStyle.pictureCompleteBackgroundStyle = R.drawable.picture_send_button_bg;
        // 相册列表勾选图片样式
        pictureParameterStyle.pictureCheckedStyle = R.drawable.picture_wechat_num_selector;
        // 相册标题背景样式 ,只针对isWeChatStyle 为true时有效果
        pictureParameterStyle.pictureWeChatTitleBackgroundStyle = R.drawable.picture_album_bg;
        // 微信样式 预览右下角样式 ,只针对isWeChatStyle 为true时有效果
        pictureParameterStyle.pictureWeChatChooseStyle = R.drawable.picture_wechat_select_cb;
        // 相册返回箭头 ,只针对isWeChatStyle 为true时有效果
        pictureParameterStyle.pictureWeChatLeftBackStyle = R.drawable.picture_icon_back;
        // 相册列表底部背景色
        pictureParameterStyle.pictureBottomBgColor = ContextCompat.getColor(getContext(), R.color.picture_color_grey);
        // 已选数量圆点背景样式
        pictureParameterStyle.pictureCheckNumBgStyle = R.drawable.picture_num_oval;
        // 相册列表底下预览文字色值(预览按钮可点击时的色值)
        pictureParameterStyle.picturePreviewTextColor = ContextCompat.getColor(getContext(), R.color.picture_color_white);
        // 相册列表底下不可预览文字色值(预览按钮不可点击时的色值)
        pictureParameterStyle.pictureUnPreviewTextColor = ContextCompat.getColor(getContext(), R.color.picture_color_9b);
        // 相册列表已完成色值(已完成 可点击色值)
        pictureParameterStyle.pictureCompleteTextColor = ContextCompat.getColor(getContext(), R.color.picture_color_white);
        // 相册列表未完成色值(请选择 不可点击色值)
        pictureParameterStyle.pictureUnCompleteTextColor = ContextCompat.getColor(getContext(), R.color.picture_color_53575e);
        // 预览界面底部背景色
        pictureParameterStyle.picturePreviewBottomBgColor = ContextCompat.getColor(getContext(), R.color.picture_color_half_grey);
        // 外部预览界面删除按钮样式
        pictureParameterStyle.pictureExternalPreviewDeleteStyle = R.drawable.picture_icon_delete;
        // 原图按钮勾选样式  需设置.isOriginalImageControl(true); 才有效
        pictureParameterStyle.pictureOriginalControlStyle = R.drawable.picture_original_wechat_checkbox;
        // 原图文字颜色 需设置.isOriginalImageControl(true); 才有效
        pictureParameterStyle.pictureOriginalFontColor = ContextCompat.getColor(getContext(), R.color.white);
        // 外部预览界面是否显示删除按钮
        pictureParameterStyle.pictureExternalPreviewGonePreviewDelete = true;
        // 设置NavBar Color SDK Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP有效
        pictureParameterStyle.pictureNavBarColor = Color.parseColor("#393a3e");
        //getCropParameterStyle();

        return pictureParameterStyle;
    }

    public PictureCropParameterStyle getCropParameterStyle(){
        // 裁剪主题
        PictureCropParameterStyle cropParameterStyle = new PictureCropParameterStyle(
                ContextCompat.getColor(getContext(), R.color.color_light_grey),
                ContextCompat.getColor(getContext(), R.color.color_light_grey),
                Color.parseColor("#393a3e"),
                ContextCompat.getColor(getContext(), R.color.white),
                true);

        return cropParameterStyle;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    if (selectList.size() > 0){
                        selectList.addAll(PictureSelector.obtainMultipleResult(data));
                    }else {
                        selectList = PictureSelector.obtainMultipleResult(data);
                    }
                    
                     Slog.d(TAG, "Selected pictures: " + selectList.size());
                    for (LocalMedia media : selectList) {
                        Log.i(TAG, "是否压缩:" + media.isCompressed());
                        Log.i(TAG, "压缩:" + media.getCompressPath());
                        Log.i(TAG, "原图:" + media.getPath());
                        Log.i(TAG, "是否裁剪:" + media.isCut());
                        Log.i(TAG, "裁剪:" + media.getCutPath());
                        Log.i(TAG, "是否开启原图:" + media.isOriginal());
                        Log.i(TAG, "原图路径:" + media.getOriginalPath());
                        Log.i(TAG, "Android Q 特有Path:" + media.getAndroidQToPath());
                    }
                    adapter.setList(selectList);
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    }
    
    public void setResultWrapper(){
        Slog.d(TAG, "----->setResultWrapper");
        Intent intent = new Intent();
        setResult(DynamicFragment.DYNAMICS_UPDATE_RESULT, intent);
    }

    @Override
    public void onBackFromDialog(String title, int eid, boolean status){
        Slog.d(TAG, "------------->title: "+title+"   eid: "+eid);
        mEid = eid;
        mTitle = title;
        selectedExprienceTitleTV.setText(mTitle);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Slog.d(TAG, "-------->onDestroy");
        //setResultWrapper();
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
                        Toast.makeText(WriteShareActivity.this,
                                getString(R.string.picture_jurisdiction), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }
    
    /**
     * 清空缓存包括裁剪、压缩、AndroidQToPath所生成的文件，注意调用时机必须是处理完本身的业务逻辑后调用；非强制性
     */
    private void clearCache() {
        // 清空图片缓存，包括裁剪、压缩后的图片 注意:必须要在上传完成后调用 必须要获取权限
        if (PermissionChecker.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //PictureFileUtils.deleteCacheDirFile(this, PictureMimeType.ofImage());
            PictureFileUtils.deleteAllCacheDirFile(getContext());
        } else {
            PermissionChecker.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PictureConfig.APPLY_STORAGE_PERMISSIONS_CODE);
        }
    }
    
    public void handleMessage(Message msg){
        switch (msg.what){
            case DynamicFragment.DYNAMICS_UPDATE_RESULT:
                setResultWrapper();
                dismissProgressDialog();
                finish();
                break;
            case UPDATEPROGRESS:
                Bundle bundle = msg.getData();
                showProgressDialogProgress((int)bundle.getLong("maxLength"), bundle.getInt("currentLength"));
                break;
        }
    }
    
    static class MyHandler extends Handler {
        WeakReference<WriteShareActivity> addDynamicsActivityWeakReference;

        MyHandler(WriteShareActivity dynamicsInteractDetailsActivity) {
            addDynamicsActivityWeakReference = new WeakReference<WriteShareActivity>(dynamicsInteractDetailsActivity);
        }
        @Override
        public void handleMessage(Message message) {
            WriteShareActivity dynamicsInteractDetailsActivity = addDynamicsActivityWeakReference.get();
            if (dynamicsInteractDetailsActivity != null) {
                dynamicsInteractDetailsActivity.handleMessage(message);
            }
        }
    }
}
