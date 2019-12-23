package com.hetang.common;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hetang.R;
import com.hetang.meet.FillMeetInfoActivity;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.Slog;
import com.hetang.util.UserProfile;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.permissions.RxPermissions;
import com.luck.picture.lib.tools.PictureFileUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Response;

import static com.hetang.meet.SubGroupDetailsActivity.MODIFY_LOGO;

public class SetAvatarActivity extends BaseAppCompatActivity {
    private static final String TAG = "SetAvatarActivity";
    private static final String UPLOAD_PICTURE_URL = HttpUtil.DOMAIN + "?q=meet/upload_picture";
    private static final String MODIFY_SUBGROUP_LOGO_URL = HttpUtil.DOMAIN + "?q=subgroup/modify_logo";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private Button done;
    private List<LocalMedia> avatarSelectList = new ArrayList<>();
    private List<File> avatarSelectFileList = new ArrayList<>();
    private Map<String, String> params = new HashMap<>();
    private int themeId;
    private Context mContext;
    Button addAvatar;
    private boolean isLookFriend = false;
    private UserProfile userProfile;
    private TextView leftBack;
    private TextView title;
    private static final int SET_AVATAR_RESULT_OK = 2;
    public static final int MODIFY_SUBGROUP_LOGO_RESULT_OK = 5;
    private int gid;
    private int type = 0;

    public static final String AVATAR_SET_ACTION_BROADCAST = "com.hetang.action.AVATAR_SET";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_avatar);
        mContext = this;

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
                    showProgressDialog("正在保存");
                    if (type == MODIFY_LOGO){
                        params.put("type", "group_logo");
                        params.put("gid", String.valueOf(gid));
                    }else {
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
        RxPermissions permissions = new RxPermissions(this);
        permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Boolean aBoolean) {
                if (aBoolean) {
                    PictureFileUtils.deleteCacheDirFile(mContext);
                } else {
                    Toast.makeText(mContext,
                            getString(R.string.picture_jurisdiction), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });

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
                        .openGallery(PictureMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                        .theme(themeId)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style
                        .maxSelectNum(1)// 最大图片选择数量
                        .minSelectNum(1)// 最小选择数量
                        .selectionMode(PictureConfig.SINGLE)// 多选 or 单选
                        .previewImage(true)// 是否可预览图片
                        .isCamera(true)// 是否显示拍照按钮
                        .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                        //.imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
                        //.setOutputCameraPath("/CustomPath")// 自定义拍照保存路径
                        .enableCrop(true)// 是否裁剪
                        .compress(true)// 是否压缩
                        .synOrAsy(true)//同步true或异步false 压缩 默认同步
                        .glideOverride(width, screenHeight)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
                        .withAspectRatio(1, 1)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                        //.hideBottomControls(cb_hide.isChecked() ? false : true)// 是否显示uCrop工具栏，默认不显示
                        .isGif(true)// 是否显示gif图片
                        .freeStyleCropEnabled(false)// 裁剪框是否可拖拽
                        .showCropFrame(false)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
                        .showCropGrid(true)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
                        .openClickSound(true)// 是否开启点击声音
                        .cropCompressQuality(100)// 裁剪压缩质量 默认100
                        .minimumCompressSize(100)// 小于100kb的图片不压缩
                        .cropWH(width, screenHeight)// 裁剪宽高比，设置如果大于图片本身宽高则无效
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
        if (type == MODIFY_LOGO){
            uri = MODIFY_SUBGROUP_LOGO_URL;
        }else {
            uri = UPLOAD_PICTURE_URL;
        }
        HttpUtil.uploadPictureHttpRequest(this, params, picKey, files, uri, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (!TextUtils.isEmpty(responseText)) {
                        try {
                            Slog.d(TAG, "----------------------->responseText: " + responseText);
                            if (type == MODIFY_LOGO){
                                String logoUri = new JSONObject(responseText).optString("logo_uri");
                                Intent intent = getIntent();
                                intent.putExtra("logoUri", logoUri);
                                setResult(MODIFY_SUBGROUP_LOGO_RESULT_OK, intent);
                                finish();
                            }else {
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
        });

    }

    private void sendBroadcast(String url) {
        Intent intent = new Intent(AVATAR_SET_ACTION_BROADCAST);
        intent.putExtra("avatar", url);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
