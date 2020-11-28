package com.mufu.common;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.ActionBar;
import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

import com.mufu.adapter.GridImageAdapter;
import com.mufu.dynamics.AddDynamicsActivity;
import com.mufu.picture.GlideEngine;
import com.mufu.experience.WriteShareActivity;
import com.mufu.main.DynamicFragment;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.ParseUtils;
import com.mufu.util.Slog;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.style.PictureWindowAnimationStyle;
import com.luck.picture.lib.tools.PictureFileUtils;
import com.mufu.R;
import com.mufu.main.FullyGridLayoutManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.lang.ref.WeakReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.mufu.common.MyApplication.getContext;
import static com.mufu.util.ParseUtils.startMeetArchiveActivity;
import static com.mufu.experience.WriteShareActivity.UPDATEPROGRESS;

public class AddPictureActivity extends BaseAppCompatActivity {

    private static final String TAG = "AddPictureActivity";
    private static final String UPLOAD_PICTURE_URL = HttpUtil.DOMAIN +"?q=meet/upload_picture";
    private int maxSelectNum = 9;
    private int uid;
    private int themeId;
    private TextView title;
    private TextView publishBtn;
    private TextView backLeft;
    private RecyclerView recyclerView;
    private GridImageAdapter adapter;
    private List<LocalMedia> selectList = new ArrayList<>();
    //private String[] activity_picture_array;
    private List<File> selectFileList = new ArrayList<>();
    private AddDynamicsActivity addDynamicsActivity;

    public static final String ADD_PICTURE_BROADCAST = "com.hetang.action.PICTURE_ADD";
        private MyHandler myHandler;
    //private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_picture);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
                myHandler = new MyHandler(this);

        title = findViewById(R.id.title);
        uid = getIntent().getIntExtra("uid", 0);

        if (addDynamicsActivity == null){
            addDynamicsActivity = new AddDynamicsActivity();
        }

        publishBtn = findViewById(R.id.dynamic_publish);
        backLeft = findViewById(R.id.left_back);
        themeId = R.style.picture_default_style;
        recyclerView = findViewById(R.id.recycler);
        FullyGridLayoutManager manager = new FullyGridLayoutManager(AddPictureActivity.this, 3, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);

        adapter = new GridImageAdapter(AddPictureActivity.this, onAddPicClickListener);
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
                            PictureSelector.create(AddPictureActivity.this)
                                    .themeStyle(themeId) // xml设置主题
                                    .setPictureStyle(addDynamicsActivity.getWeChatStyle())// 动态自定义相册主题
                                    //.setPictureWindowAnimationStyle(animationStyle)// 自定义页面启动动画
                                    .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)// 设置相册Activity方向，不设置默认使用系统
                                    .isNotPreviewDownload(true)// 预览图片长按是否可以下载
                                    //.bindCustomPlayVideoCallback(callback)// 自定义播放回调控制，用户可以使用自己的视频播放界面
                                    .loadImageEngine(GlideEngine.createGlideEngine())// 外部传入图片加载引擎，必传项
                                    .openExternalPreview(position, selectList);
                            break;
                        case PictureConfig.TYPE_VIDEO:
                            PictureSelector.create(AddPictureActivity.this).externalPictureVideo(media.getPath());
                            // 预览视频
                            PictureSelector.create(AddPictureActivity.this)
                                    .themeStyle(themeId)
                                    .setPictureStyle(addDynamicsActivity.getWeChatStyle())// 动态自定义相册主题
                                    .externalPictureVideo(media.getPath());
                            break;
                        case PictureConfig.TYPE_AUDIO:
                            // 预览音频
                            PictureSelector.create(AddPictureActivity.this)
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
                //showProgress(AddPictureActivity.this);
                showProgressDialog(getResources().getString(R.string.saving_progress));
                if (selectList.size() > 0){
                    for (LocalMedia media : selectList) {
                        //activity_picture_array[media.getNum() - 1] = media.getCompressPath();
                        selectFileList.add(new File(media.getCompressPath()));
                        Slog.d(TAG, "===========selectFileList: "+selectFileList.size());
                    }
                    
                    uploadPictures("picture", selectFileList);
                }else{
                   finish(); 
                }
            }
        });

        /*
        backLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
         */

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.add_dynamics_statusbar), font);
    }

    private GridImageAdapter.onAddPicClickListener onAddPicClickListener = new GridImageAdapter.onAddPicClickListener() {
        @Override
        public void onAddPicClick() {
            //boolean mode = cb_mode.isChecked();
            boolean mode = true;
            if (mode) {
                PictureSelector.create(AddPictureActivity.this)
                        .openGallery(PictureMimeType.ofImage())
                        .loadImageEngine(GlideEngine.createGlideEngine())// 外部传入图片加载引擎，必传项
                        .theme(R.style.picture_WeChat_style)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style v2.3.3后 建议使用setPictureStyle()动态方式
                        .isWeChatStyle(true)// 是否开启微信图片选择风格
                        //.isUseCustomCamera(cb_custom_camera.isChecked())// 是否使用自定义相机
                        //.setLanguage(language)// 设置语言，默认中文
                        .setPictureStyle(addDynamicsActivity.getWeChatStyle())// 动态自定义相册主题
                        .setPictureCropStyle(addDynamicsActivity.getCropParameterStyle())// 动态自定义裁剪主题
                        .setPictureWindowAnimationStyle(new PictureWindowAnimationStyle())// 自定义相册启动退出动画
                        .isWithVideoImage(true)// 图片和视频是否可以同选,只在ofAll模式下有效
                        .maxSelectNum(maxSelectNum)// 最大图片选择数量
                        .minSelectNum(1)// 最小选择数量
                        .maxVideoSelectNum(1) // 视频最大选择数量，如果没有单独设置的需求则可以不设置，同用maxSelectNum字段
                        //.minVideoSelectNum(1)// 视频最小选择数量，如果没有单独设置的需求则可以不设置，同用minSelectNum字段
                        .imageSpanCount(4)// 每行显示个数
                        .isReturnEmpty(false)// 未选择数据时点击按钮是否可以返回
                        //.isAndroidQTransform(false)// 是否需要处理Android Q 拷贝至应用沙盒的操作，只针对compress(false); && enableCrop(false);有效,默认处理
                        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)// 设置相册Activity方向，不设置默认使用系统
                        //.isOriginalImageControl(cb_original.isChecked())// 是否显示原图控制按钮，如果设置为true则用户可以自由选择是否使用原图，压缩、裁剪功能将会失效
                        //.bindCustomPlayVideoCallback(callback)// 自定义视频播放回调控制，用户可以使用自己的视频播放界面
                        //.cameraFileName(System.currentTimeMillis() +".jpg")    // 重命名拍照文件名、如果是相册拍照则内部会自动拼上当前时间戳防止重复，注意这个只在使用相机时可以使用，如果使用相机又开启了压缩或裁剪 需要配合压缩和裁剪文件名api
                        //.renameCompressFile(System.currentTimeMillis() +".jpg")// 重命名压缩文件名、 注意这个不要重复，只适用于单张图压缩使用
                        //.renameCropFileName(System.currentTimeMillis() + ".jpg")// 重命名裁剪文件名、 注意这个不要重复，只适用于单张图裁剪使用
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
                        //.imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
                        //.enableCrop(true)// 是否裁剪
                        //.basicUCropConfig()//对外提供所有UCropOptions参数配制，但如果PictureSelector原本支持设置的还是会使用原有的设置
                        .compress(true)// 是否压缩
                        .compressQuality(80)// 图片压缩后输出质量 0~ 100
                        .synOrAsy(true)//同步true或异步false 压缩 默认同步
                        //.queryMaxFileSize(10)// 只查多少M以内的图片、视频、音频  单位M
                        //.compressSavePath(getPath())//压缩图片保存地址
                        //.sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效 注：已废弃
                        //.glideOverride(160, 160)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度 注：已废弃
                        .withAspectRatio(1, 1)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                        //.hideBottomControls(cb_hide.isChecked() ? false : true)// 是否显示uCrop工具栏，默认不显示
                        //.isGif(cb_isGif.isChecked())// 是否显示gif图片
                        .freeStyleCropEnabled(true)// 裁剪框是否可拖拽
                        //.circleDimmedLayer(cb_crop_circular.isChecked())// 是否圆形裁剪
                        //.setCircleDimmedColor(ContextCompat.getColor(getContext(), R.color.app_color_white))// 设置圆形裁剪背景色值
                        //.setCircleDimmedBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.app_color_white))// 设置圆形裁剪边框色值
                        //.setCircleStrokeWidth(3)// 设置圆形裁剪边框粗细
                        //.showCropFrame(true)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
                        //.showCropGrid(true)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
                        //.openClickSound(cb_voice.isChecked())// 是否开启点击声音
                        //.selectionMedia(mAdapter.getData())// 是否传入已选图片
                        //.isDragFrame(false)// 是否可拖动裁剪框(固定)
                        //.videoMinSecond(10)
                        //.videoMaxSecond(15)
                        //.recordVideoSecond(10)//录制视频秒数 默认60s
                        .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                        //.cropCompressQuality(90)// 注：已废弃 改用cutOutQuality()
                        //.cutOutQuality(90)// 裁剪输出质量 默认100
                        .minimumCompressSize(100)// 小于100kb的图片不压缩
                        //.cropWH()// 裁剪宽高比，设置如果大于图片本身宽高则无效
                        //.cropImageWideHigh()// 裁剪宽高比，设置如果大于图片本身宽高则无效
                        //.rotateEnabled(true) // 裁剪是否可旋转图片
                        //.scaleEnabled(true)// 裁剪是否可放大缩小图片
                        .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
            }
        }

    };
    
    private void uploadPictures(String picKey, List<File> files) {

        HttpUtil.uploadPictureProgressHttpRequest(this, null, picKey, files, UPLOAD_PICTURE_URL, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    JSONObject statusObj;
                    try {
                        statusObj = new JSONObject(response.body().string());
                        int status = statusObj.optInt("response");
                        Slog.d(TAG, "--------------------->status : "+status);
                        if(status != 0){
                            runOnUiThread(new Runnable() {
                            @Override
                                public void run() {
                                    dismissProgressDialog();
                                    finish();
                                }
                            });
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
                        dismissProgressDialog();
                        Toast.makeText(AddPictureActivity.this, "±£´æÊ§°Ü", Toast.LENGTH_SHORT).show();
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
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    if (selectList.size() == 0){
                        selectList = PictureSelector.obtainMultipleResult(data);
                    }else {
                        selectList.addAll(PictureSelector.obtainMultipleResult(data));
                    }

                    Slog.d(TAG, "Selected pictures: " + selectList.size());
                    //activity_picture_array = new String[selectList.size()];
                    if(selectList.size() > 0){
                        adapter.setList(selectList);
                        adapter.notifyDataSetChanged();
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
                        Toast.makeText(AddPictureActivity.this,
                                getString(R.string.picture_jurisdiction), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }
    
        public void handleMessage(Message msg){
        switch (msg.what){
            case UPDATEPROGRESS:
                Bundle bundle = msg.getData();
                showProgressDialogProgress((int)bundle.getLong("maxLength"), bundle.getInt("currentLength"));
                break;
        }
    }
    
    private void sendBroadcast() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ADD_PICTURE_BROADCAST));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sendBroadcast();
        if (selectFileList.size() > 0){
            ParseUtils.startMeetArchiveActivity(AddPictureActivity.this, uid);
        }
    }
    
    static class MyHandler extends Handler {
        WeakReference<AddPictureActivity> addPictureActivityWeakReference;

        MyHandler(AddPictureActivity addPictureActivity) {
            addPictureActivityWeakReference = new WeakReference<AddPictureActivity>(addPictureActivity);
        }
        @Override
        public void handleMessage(Message message) {
            AddPictureActivity addPictureActivity = addPictureActivityWeakReference.get();
            if (addPictureActivity != null) {
                addPictureActivity.handleMessage(message);
            }
        }
    }

}
