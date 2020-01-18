package com.hetang.common;

import android.Manifest;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hetang.adapter.GridImageAdapter;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.ParseUtils;
import com.hetang.util.Slog;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.permissions.RxPermissions;
import com.luck.picture.lib.tools.PictureFileUtils;
import com.hetang.R;
import com.hetang.main.FullyGridLayoutManager;

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
import okhttp3.Response;

import static com.hetang.util.ParseUtils.startMeetArchiveActivity;

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
    private Map<String, String> dynamicsText = new HashMap<>();
    private GridImageAdapter.onAddPicClickListener onAddPicClickListener;
    
    public static final String ADD_PICTURE_BROADCAST = "com.hetang.action.PICTURE_ADD";
    //private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_picture);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        title = findViewById(R.id.title);
        uid = getIntent().getIntExtra("uid", 0);

        publishBtn = findViewById(R.id.dynamic_publish);
        backLeft = findViewById(R.id.left_back);
        themeId = R.style.picture_default_style;
        recyclerView = findViewById(R.id.recycler);
        FullyGridLayoutManager manager = new FullyGridLayoutManager(AddPictureActivity.this, 3, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);
        
        onAddPicClickListener = new GridImageAdapter.onAddPicClickListener() {
            @Override
            public void onAddPicClick() {
                //boolean mode = cb_mode.isChecked();
                boolean mode = true;
                if (mode) {
                    // ½øÈëÏà²á ÒÔÏÂÊÇÀý×Ó£º²»ÐèÒªµÄapi¿ÉÒÔ²»Ð´
                    PictureSelector.create(AddPictureActivity.this)
                            .openGallery(PictureMimeType.ofImage())// È«²¿.PictureMimeType.ofAll()¡¢Í¼Æ¬.ofImage()¡¢ÊÓÆµ.ofVideo()¡¢ÒôÆµ.ofAudio()
                            .theme(themeId)// Ö÷ÌâÑùÊ½ÉèÖÃ ¾ßÌå²Î¿¼ values/styles   ÓÃ·¨£ºR.style.picture.white.style
                            .maxSelectNum(maxSelectNum)// ×î´óÍ¼Æ¬Ñ¡ÔñÊýÁ¿
                            .minSelectNum(1)// ×îÐ¡Ñ¡ÔñÊýÁ¿
                            .imageSpanCount(4)// Ã¿ÐÐÏÔÊ¾¸öÊý
                            .selectionMode(PictureConfig.MULTIPLE)// ¶àÑ¡ or µ¥Ñ¡
                            .previewImage(true)// ÊÇ·ñ¿ÉÔ¤ÀÀÍ¼Æ¬
                            .previewVideo(true)// ÊÇ·ñ¿ÉÔ¤ÀÀÊÓÆµ
                            .enablePreviewAudio(true) // ÊÇ·ñ¿É²¥·ÅÒôÆµ
                            .isCamera(true)// ÊÇ·ñÏÔÊ¾ÅÄÕÕ°´Å¥
                            .isZoomAnim(true)// Í¼Æ¬ÁÐ±íµã»÷ Ëõ·ÅÐ§¹û Ä¬ÈÏtrue
                            //.imageFormat(PictureMimeType.PNG)// ÅÄÕÕ±£´æÍ¼Æ¬¸ñÊ½ºó×º,Ä¬ÈÏjpeg
                            //.setOutputCameraPath("/CustomPath")// ×Ô¶¨ÒåÅÄÕÕ±£´æÂ·¾¶
                            .enableCrop(true)// ÊÇ·ñ²Ã¼ô
                            .compress(true)// ÊÇ·ñÑ¹Ëõ
                            .synOrAsy(true)//Í¬²½true»òÒì²½false Ñ¹Ëõ Ä¬ÈÏÍ¬²½
                            //.compressSavePath(getPath())//Ñ¹ËõÍ¼Æ¬±£´æµØÖ·
                            //.sizeMultiplier(0.5f)// glide ¼ÓÔØÍ¼Æ¬´óÐ¡ 0~1Ö®¼ä ÈçÉèÖÃ .glideOverride()ÎÞÐ§
                            .glideOverride(160, 160)// glide ¼ÓÔØ¿í¸ß£¬Ô½Ð¡Í¼Æ¬ÁÐ±íÔ½Á÷³©£¬µ«»áÓ°ÏìÁÐ±íÍ¼Æ¬ä¯ÀÀµÄÇåÎú¶È
                            .withAspectRatio(3, 3)// ²Ã¼ô±ÈÀý Èç16:9 3:2 3:4 1:1 ¿É×Ô¶¨Òå
                            //.hideBottomControls(cb_hide.isChecked() ? false : true)// ÊÇ·ñÏÔÊ¾uCrop¹¤¾ßÀ¸£¬Ä¬ÈÏ²»ÏÔÊ¾
                            .isGif(true)// ÊÇ·ñÏÔÊ¾gifÍ¼Æ¬
                            .freeStyleCropEnabled(true)// ²Ã¼ô¿òÊÇ·ñ¿ÉÍÏ×§
                            //.circleDimmedLayer(cb_crop_circular.isChecked())// ÊÇ·ñÔ²ÐÎ²Ã¼ô
                            .showCropFrame(true)// ÊÇ·ñÏÔÊ¾²Ã¼ô¾ØÐÎ±ß¿ò Ô²ÐÎ²Ã¼ôÊ±½¨ÒéÉèÎªfalse
                            .showCropGrid(true)// ÊÇ·ñÏÔÊ¾²Ã¼ô¾ØÐÎÍø¸ñ Ô²ÐÎ²Ã¼ôÊ±½¨ÒéÉèÎªfalse
                            .openClickSound(false)// ÊÇ·ñ¿ªÆôµã»÷ÉùÒô
                            .selectionMedia(selectList)// ÊÇ·ñ´«ÈëÒÑÑ¡Í¼Æ¬
                            //.isDragFrame(false)// ÊÇ·ñ¿ÉÍÏ¶¯²Ã¼ô¿ò(¹Ì¶¨)
//                        .videoMaxSecond(15)
//                        .videoMinSecond(10)
                            //.previewEggs(false)// Ô¤ÀÀÍ¼Æ¬Ê± ÊÇ·ñÔöÇ¿×óÓÒ»¬¶¯Í¼Æ¬ÌåÑé(Í¼Æ¬»¬¶¯Ò»°ë¼´¿É¿´µ½ÉÏÒ»ÕÅÊÇ·ñÑ¡ÖÐ)
                            .cropCompressQuality(100)// ²Ã¼ôÑ¹ËõÖÊÁ¿ Ä¬ÈÏ100
                            .minimumCompressSize(100)// Ð¡ÓÚ100kbµÄÍ¼Æ¬²»Ñ¹Ëõ
                            //.cropWH()// ²Ã¼ô¿í¸ß±È£¬ÉèÖÃÈç¹û´óÓÚÍ¼Æ¬±¾Éí¿í¸ßÔòÎÞÐ§
                            //.rotateEnabled(true) // ²Ã¼ôÊÇ·ñ¿ÉÐý×ªÍ¼Æ¬
                            .scaleEnabled(true)// ²Ã¼ôÊÇ·ñ¿É·Å´óËõÐ¡Í¼Æ¬
                            //.videoQuality()// ÊÓÆµÂ¼ÖÆÖÊÁ¿ 0 or 1
                            //.videoSecond()//ÏÔÊ¾¶àÉÙÃëÒÔÄÚµÄÊÓÆµorÒôÆµÒ²¿ÉÊÊÓÃ
                            //.recordVideoSecond()//Â¼ÖÆÊÓÆµÃëÊý Ä¬ÈÏ60s
                            .forResult(PictureConfig.CHOOSE_REQUEST);//½á¹û»Øµ÷onActivityResult code
                }
            }

        };
        
        adapter = new GridImageAdapter(AddPictureActivity.this, onAddPicClickListener);
        adapter.setList(selectList);
        adapter.setSelectMax(maxSelectNum);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new GridImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                if (selectList.size() > 0) {
                    LocalMedia media = selectList.get(position);
                    String pictureType = media.getPictureType();
                    int mediaType = PictureMimeType.pictureToVideo(pictureType);
                    switch (mediaType) {
                    case 1:
                            // Ô¤ÀÀÍ¼Æ¬ ¿É×Ô¶¨³¤°´±£´æÂ·¾¶
                            //PictureSelector.create(MainActivity.this).externalPicturePreview(position, "/custom_file", selectList);
                            PictureSelector.create(AddPictureActivity.this).externalPicturePreview(position, selectList);
                            break;
                        case 2:
                            // Ô¤ÀÀÊÓÆµ
                            PictureSelector.create(AddPictureActivity.this).externalPictureVideo(media.getPath());
                            break;
                        case 3:
                            // Ô¤ÀÀÒôÆµ
                            PictureSelector.create(AddPictureActivity.this).externalPictureAudio(media.getPath());
                            break;
                    }
                }
            }
        });
        
        // Çå¿ÕÍ¼Æ¬»º´æ£¬°üÀ¨²Ã¼ô¡¢Ñ¹ËõºóµÄÍ¼Æ¬ ×¢Òâ:±ØÐëÒªÔÚÉÏ´«Íê³Éºóµ÷ÓÃ ±ØÐëÒª»ñÈ¡È¨ÏÞ
        RxPermissions permissions = new RxPermissions(this);
        permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Boolean aBoolean) {
                if (aBoolean) {
                    PictureFileUtils.deleteCacheDirFile(AddPictureActivity.this);
                } else {
                    Toast.makeText(AddPictureActivity.this,
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
        
        backLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.add_dynamics_statusbar), font);
    }
    
    private void uploadPictures(String picKey, List<File> files) {

        HttpUtil.uploadPictureHttpRequest(this, null, picKey, files, UPLOAD_PICTURE_URL, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    JSONObject statusObj;
                    try {
                        statusObj = new JSONObject(response.body().string());
                        int status = statusObj.optInt("response");
                        if(status != 0){
                            runOnUiThread(new Runnable() {
                            @Override
                                public void run() {
                                    //Toast.makeText(AddPictureActivity.this, "³É¹¦", Toast.LENGTH_SHORT).show();
                                    //sendBroadcast();//send broadcast to meetdynamicsfragment notify  meet dynamics to update
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
        });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    // Í¼Æ¬Ñ¡Ôñ½á¹û»Øµ÷
                    selectList = PictureSelector.obtainMultipleResult(data);
                    // ÀýÈç LocalMedia ÀïÃæ·µ»ØÈýÖÖpath
                    // 1.media.getPath(); ÎªÔ­Í¼path
                    // 2.media.getCutPath();Îª²Ã¼ôºópath£¬ÐèÅÐ¶Ïmedia.isCut();ÊÇ·ñÎªtrue
                    // 3.media.getCompressPath();ÎªÑ¹Ëõºópath£¬ÐèÅÐ¶Ïmedia.isCompressed();ÊÇ·ñÎªtrue
                    // Èç¹û²Ã¼ô²¢Ñ¹ËõÁË£¬ÒÑÈ¡Ñ¹ËõÂ·¾¶Îª×¼£¬ÒòÎªÊÇÏÈ²Ã¼ôºóÑ¹ËõµÄ
                    
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
    
    private void sendBroadcast() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ADD_PICTURE_BROADCAST));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Slog.d(TAG, "-------->onDestroy");
        sendBroadcast();
        ParseUtils.startMeetArchiveActivity(AddPictureActivity.this, uid);
    }

}
