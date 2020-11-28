package com.mufu.common;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mufu.R;
import com.mufu.dynamics.AddDynamicsActivity;
import com.mufu.meet.FillMeetInfoActivity;
import com.mufu.picture.GlideEngine;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.Slog;
import com.mufu.util.UserProfile;
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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Response;

import static com.mufu.common.MyApplication.getContext;
import static com.mufu.experience.WriteShareActivity.UPDATEPROGRESS;

public class SetAvatarActivity extends BaseAppCompatActivity {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final int MODIFY_SUBGROUP_LOGO_RESULT_OK = 5;
    public static final int MODIFY_LOGO = 3;
    public static final int AUTHENTICATION_PHOTO = 4;
    public static final int TALENT_AUTHENTICATION_PHOTO = 5;
    public static final String AVATAR_SET_ACTION_BROADCAST = "com.hetang.action.AVATAR_SET";
    public static final String SUBMIT_AUTHENTICATION_ACTION_BROADCAST = "com.hetang.action.SUBMIT_AUTHEN";
    public static final String SUBMIT_TALENT_AUTHENTICATION_ACTION_BROADCAST = "com.hetang.action.SUBMIT_TALENT_AUTHEN";
    private static final String TAG = "SetAvatarActivity";
    private static final String UPLOAD_PICTURE_URL = HttpUtil.DOMAIN + "?q=meet/upload_picture";
    private static final String MODIFY_SUBGROUP_LOGO_URL = HttpUtil.DOMAIN + "?q=subgroup/modify_logo";
    private static final String SUBMIT_AUTHENTICATION_PHOTO_URL = HttpUtil.DOMAIN + "?q=user_extdata/submit_authentication_photo";
    private static final int SET_AVATAR_RESULT_OK = 2;
    Button addAvatar;
    private Button done;
    private List<LocalMedia> avatarSelectList = new ArrayList<>();
    private List<File> avatarSelectFileList = new ArrayList<>();
    private Map<String, String> params = new HashMap<>();
    private int themeId;
    private Context mContext;
    private boolean isLookFriend = false;
    private UserProfile userProfile;
    private TextView leftBack;
    private TextView title;
    private int gid;
    private int type = 0;
    private AddDynamicsActivity addDynamicsActivity;
        private MyHandler myHandler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_avatar);
        mContext = this;

        if (addDynamicsActivity == null){
            addDynamicsActivity = new AddDynamicsActivity();
        }

        isLookFriend = getIntent().getBooleanExtra("look_friend", false);
        userProfile = (UserProfile) getIntent().getSerializableExtra("userProfile");

        done = findViewById(R.id.done);
        addAvatar = findViewById(R.id.add_avatar);
        leftBack = findViewById(R.id.left_back);
        title = findViewById(R.id.title);
        title.setText(getResources().getString(R.string.set_avatar));

        type = getIntent().getIntExtra("type", 0);
        if (type == MODIFY_LOGO) {
            gid = getIntent().getIntExtra("gid", 0);
            title.setText("修改团标");
        } else if (type == AUTHENTICATION_PHOTO) {
            title.setText("上传证件照");
        } else if (type == TALENT_AUTHENTICATION_PHOTO) {
            title.setText("上传赞赏二维码");
        }

        leftBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        selectPicture();

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (avatarSelectFileList.size() > 0) {
                    //showProgressDialog("正在保存");
                    if (type == MODIFY_LOGO) {
                        params.put("type", "group_logo");
                        params.put("gid", String.valueOf(gid));
                    } else if (type == AUTHENTICATION_PHOTO || type == TALENT_AUTHENTICATION_PHOTO) {
                        params.put("type", "authentication");
                    } else {
                        params.put("type", "avatar");
                    }

                    params.put("domain", HttpUtil.DOMAIN);
                    uploadPictures(params, "picture", avatarSelectFileList);
                } else {
                    Toast.makeText(getApplicationContext(), "请设置头像", Toast.LENGTH_LONG).show();
                }
            }
        });

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.set_avatar_wrapper), font);
    }

    private void selectPicture() {

        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        float density = dm.density;
        final int width = dm.widthPixels;
        final int screenHeight = width;

        themeId = R.style.picture_default_style;

        addAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PictureSelector.create(SetAvatarActivity.this)
                        .openGallery(PictureMimeType.ofImage())
                        .loadImageEngine(GlideEngine.createGlideEngine())// 外部传入图片加载引擎，必传项
                        .theme(R.style.picture_WeChat_style)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style v2.3.3后 建议使用setPictureStyle()动态方式
                        .isWeChatStyle(true)// 是否开启微信图片选择风格
                        //.isUseCustomCamera(cb_custom_camera.isChecked())// 是否使用自定义相机
                        //.setLanguage(language)// 设置语言，默认中文
                        .setPictureStyle(addDynamicsActivity.getWeChatStyle())// 动态自定义相册主题
                        .setPictureCropStyle(addDynamicsActivity.getCropParameterStyle())// 动态自定义裁剪主题
                        .setPictureWindowAnimationStyle(new PictureWindowAnimationStyle())// 自定义相册启动退出动画
                        //.isWithVideoImage(true)// 图片和视频是否可以同选,只在ofAll模式下有效
                        .maxSelectNum(1)// 最大图片选择数量
                        .minSelectNum(1)// 最小选择数量
                        .isReturnEmpty(false)// 未选择数据时点击按钮是否可以返回
                        //.isAndroidQTransform(false)// 是否需要处理Android Q 拷贝至应用沙盒的操作，只针对compress(false); && enableCrop(false);有效,默认处理
                        .selectionMode(PictureConfig.SINGLE )// 多选 or 单选
                        //.isSingleDirectReturn(cb_single_back.isChecked())// 单选模式下是否直接返回，PictureConfig.SINGLE模式下有效
                        .previewImage(true)// 是否可预览图片
                        .isCamera(true)// 是否显示拍照按钮
                        .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                        //.imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
                        .enableCrop(true)// 是否裁剪
                        //.basicUCropConfig()//对外提供所有UCropOptions参数配制，但如果PictureSelector原本支持设置的还是会使用原有的设置
                        .compress(true)// 是否压缩
                        .compressQuality(100)// 图片压缩后输出质量 0~ 100
                        .synOrAsy(true)//同步true或异步false 压缩 默认同步
                        //.queryMaxFileSize(10)// 只查多少M以内的图片、视频、音频  单位M
                        //.compressSavePath(getPath())//压缩图片保存地址
                        .withAspectRatio(1, 1)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                        //.hideBottomControls(cb_hide.isChecked() ? false : true)// 是否显示uCrop工具栏，默认不显示
                        //.isGif(cb_isGif.isChecked())// 是否显示gif图片
                        //.freeStyleCropEnabled(true)// 裁剪框是否可拖拽
                        .showCropFrame(true)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
                        .showCropGrid(true)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
                        //.openClickSound(cb_voice.isChecked())// 是否开启点击声音
                        //.selectionMedia(mAdapter.getData())// 是否传入已选图片
                        .isDragFrame(true)// 是否可拖动裁剪框(固定)
                        //.previewEggs(false)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                        //.cropCompressQuality(90)// 注：已废弃 改用cutOutQuality()
                        //.cutOutQuality(90)// 裁剪输出质量 默认100
                        .minimumCompressSize(100)// 小于100kb的图片不压缩
                        .cropWH(width, screenHeight)// 裁剪宽高比，设置如果大于图片本身宽高则无效
                        //.cropImageWideHigh(1, 1)// 裁剪宽高比，设置如果大于图片本身宽高则无效
                        .rotateEnabled(true) // 裁剪是否可旋转图片
                        .scaleEnabled(true)// 裁剪是否可放大缩小图片
                        .forResult(PictureConfig.SINGLE);//结果回调onActivityResult code
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.SINGLE:
                    // 图片选择结果回调
                    avatarSelectList = PictureSelector.obtainMultipleResult(data);
                    if (avatarSelectList.size() > 0) {

                        if (avatarSelectFileList.size() > 0) {
                            avatarSelectFileList.clear();
                        }
                        // 例如 LocalMedia 里面返回三种path
                        // 1.media.getPath(); 为原图path
                        // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                        // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                        // 如果裁剪并压缩了，已取压缩路径为准，因为是先裁剪后压缩的
                        Slog.d(TAG, "Selected pictures: " + avatarSelectList.size());
                        //activity_picture_array = new String[selectList.size()];
                        for (LocalMedia media : avatarSelectList) {

                            Log.i("图片-----》", media.getPath());
                            Log.d("压缩图片------->>", media.getCompressPath());
                            Slog.d(TAG, "===========num: " + media.getNum());
                            //activity_picture_array[media.getNum() - 1] = media.getCompressPath();
                            avatarSelectFileList.add(new File(media.getCompressPath()));

                            Bitmap bitmap = BitmapFactory.decodeFile(media.getCompressPath());
                            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                            addAvatar.setBackground(drawable);
                        }
                    }
                    break;

                default:
                    break;
            }
        }
    }

    private void uploadPictures(Map<String, String> params, String picKey, List<File> files) {
        String uri;
        if (type == MODIFY_LOGO) {
            uri = MODIFY_SUBGROUP_LOGO_URL;
        } else if (type == AUTHENTICATION_PHOTO || type == TALENT_AUTHENTICATION_PHOTO) {
            uri = SUBMIT_AUTHENTICATION_PHOTO_URL;
        } else {
            uri = UPLOAD_PICTURE_URL;
        }
        HttpUtil.uploadPictureProgressHttpRequest(this, params, picKey, files, uri, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (!TextUtils.isEmpty(responseText)) {
                        try {
                            Slog.d(TAG, "----------------------->responseText: " + responseText);
                            if (type == MODIFY_LOGO) {
                                String logoUri = new JSONObject(responseText).optString("logo_uri");
                                Intent intent = getIntent();
                                intent.putExtra("logoUri", logoUri);
                                setResult(MODIFY_SUBGROUP_LOGO_RESULT_OK, intent);
                                finish();
                            } else if (type == AUTHENTICATION_PHOTO || type == TALENT_AUTHENTICATION_PHOTO) {
                                int status = new JSONObject(responseText).optInt("response");
                                if (status == 1) {
                                    String uri = new JSONObject(responseText).optString("uri");
                                    sendBroadcast(uri);
                                    finish();
                                }
                            } else {
                                int status = new JSONObject(responseText).optInt("response");
                                if (status == 1) {
                                    String avatarUri = new JSONObject(responseText).optString("avatar");
                                    if (isLookFriend) {
                                        Intent intent = new Intent(getApplicationContext(), FillMeetInfoActivity.class);
                                        intent.putExtra("userProfile", userProfile);
                                        startActivity(intent);
                                    } else {
                                        Slog.d(TAG, "------------------------>avatar: " + avatarUri);
                                        Intent intent = getIntent();
                                        intent.putExtra("avatar", avatarUri);
                                        setResult(SET_AVATAR_RESULT_OK, intent);
                                    }
                                    sendBroadcast(avatarUri);
                                    finish();
                                }
                            }
                            dismissProgressDialog();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SetAvatarActivity.this, "失败", Toast.LENGTH_SHORT).show();
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

    private void sendBroadcast(String uri) {
        Intent intent;
        if (type == AUTHENTICATION_PHOTO) {
            intent = new Intent(SUBMIT_AUTHENTICATION_ACTION_BROADCAST);
            intent.putExtra("uri", uri);
        } else if (type == TALENT_AUTHENTICATION_PHOTO) {
            intent = new Intent(SUBMIT_TALENT_AUTHENTICATION_ACTION_BROADCAST);
            intent.putExtra("uri", uri);
        } else {
            intent = new Intent(AVATAR_SET_ACTION_BROADCAST);
            intent.putExtra("avatar", uri);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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
                        Toast.makeText(SetAvatarActivity.this,
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

    static class MyHandler extends Handler {
        WeakReference<SetAvatarActivity> setAvatarActivityWeakReference;

        MyHandler(SetAvatarActivity setAvatarActivity) {
            setAvatarActivityWeakReference = new WeakReference<SetAvatarActivity>(setAvatarActivity);
        }
        @Override
        public void handleMessage(Message message) {
            SetAvatarActivity setAvatarActivity = setAvatarActivityWeakReference.get();
            if (setAvatarActivity != null) {
                setAvatarActivity.handleMessage(message);
            }
        }
    }
}
