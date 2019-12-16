package com.hetang.common;

import android.Manifest;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hetang.adapter.GridImageAdapter;
import com.hetang.home.HomeFragment;
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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import static com.hetang.common.MyApplication.getContext;

import static com.hetang.util.ParseUtils.ADD_SUBGROUP_ACTIVITY_ACTION;

public class AddDynamicsActivity extends BaseAppCompatActivity {

    public static final String DYNAMICS_ADD_BROADCAST = "com.hetang.action.DYNAMICS_ADD";
    private static final String TAG = "AddDynamicsActivity";
    private int maxSelectNum = 9;
    private int themeId;
    private int type = 0;
    private int gid = 0;
    private TextView publishBtn;
    private TextView backLeft;
    private EditText editText;
    private MyHandler myHandler;
    private RecyclerView recyclerView;
    private GridImageAdapter adapter;
    private List<LocalMedia> selectList = new ArrayList<>();
    //private String[] activity_picture_array;
    private List<File> selectFileList = new ArrayList<>();
    private Map<String, String> dynamicsText = null;
    private GridImageAdapter.onAddPicClickListener onAddPicClickListener;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_dynamics);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        type = getIntent().getIntExtra("type", ParseUtils.ADD_MEET_DYNAMIC_ACTION);
        
        if (type == ADD_SUBGROUP_ACTIVITY_ACTION){
            gid = getIntent().getIntExtra("gid", 0);
        }
        myHandler = new MyHandler(this);
        editText = findViewById(R.id.dynamics_input);
        publishBtn = findViewById(R.id.dynamic_publish);
        backLeft = findViewById(R.id.left_back);
        themeId = R.style.picture_default_style;
        recyclerView = findViewById(R.id.recycler);
        FullyGridLayoutManager manager = new FullyGridLayoutManager(AddDynamicsActivity.this, 3, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);
        
        onAddPicClickListener = new GridImageAdapter.onAddPicClickListener() {
            @Override
            public void onAddPicClick() {
                //boolean mode = cb_mode.isChecked();
                boolean mode = true;
                if (mode) {
                    PictureSelector.create(AddDynamicsActivity.this)
                            .openGallery(PictureMimeType.ofImage())
                            .theme(themeId)
                            .maxSelectNum(maxSelectNum)
                            .minSelectNum(1)
                            .imageSpanCount(4)
                            .selectionMode(PictureConfig.MULTIPLE)
                            .previewImage(true)
                            .previewVideo(true)
                            .enablePreviewAudio(true)
                            .isCamera(true)
                            .isZoomAnim(true)
                            //.imageFormat(PictureMimeType.PNG)
                            //.setOutputCameraPath("/CustomPath")
                            //.enableCrop(true)
                            .compress(true)
                            .synOrAsy(true)
                            //.compressSavePath(getPath())
                            //.sizeMultiplier(0.5f)
                            .glideOverride(160, 160)
                            .withAspectRatio(2, 3)
                            //.hideBottomControls(cb_hide.isChecked() ? false : true)
                            .isGif(true)
                            .freeStyleCropEnabled(true)
                            //.circleDimmedLayer(cb_crop_circular.isChecked())
                            .showCropFrame(true)
                            .showCropGrid(true)
                            .openClickSound(false)
                            .selectionMedia(selectList)
                            //.isDragFrame(false)
//                        .videoMaxSecond(15)
//                        .videoMinSecond(10)
                            //.previewEggs(false)
                            //.cropCompressQuality(90)
                            .minimumCompressSize(100)
                            .forResult(PictureConfig.CHOOSE_REQUEST);
                }
            }

        };
        
        adapter = new GridImageAdapter(AddDynamicsActivity.this, onAddPicClickListener);
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
                            //PictureSelector.create(MainActivity.this).externalPicturePreview(position, "/custom_file", selectList);
                            PictureSelector.create(AddDynamicsActivity.this).externalPicturePreview(position, selectList);
                            break;
                        case 2:
                            PictureSelector.create(AddDynamicsActivity.this).externalPictureVideo(media.getPath());
                            break;
                        case 3:
                            PictureSelector.create(AddDynamicsActivity.this).externalPictureAudio(media.getPath());
                            break;
                    }
                }
            }
        });
        
        RxPermissions permissions = new RxPermissions(this);
        permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Boolean aBoolean) {
                if (aBoolean) {
                    PictureFileUtils.deleteCacheDirFile(AddDynamicsActivity.this);
                } else {
                    Toast.makeText(AddDynamicsActivity.this,
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
                showProgressDialog("正在保存...");
                String dynamics_input = editText.getText().toString();
                Slog.d(TAG, "---->dynamics_input: "+dynamics_input+" type: "+type+" gid: "+gid);
                // Toast.makeText(AddDynamicsActivity.this, editText.getText().toString(), Toast.LENGTH_SHORT).show();
                dynamicsText = new HashMap<>();
                if (dynamics_input.length() > 0) {
                    dynamicsText.put("text", dynamics_input);
                }
                dynamicsText.put("type", String.valueOf(type));
                if (type == ADD_SUBGROUP_ACTIVITY_ACTION){
                    dynamicsText.put("gid", String.valueOf(gid));
                }
                
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

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.add_dynamics_statusbar), font);

    }
    
    private void uploadPictures(Map<String, String> params, String picKey, List<File> files) {

        HttpUtil.uploadPictureHttpRequest(this, params, picKey, files, ParseUtils.DYNAMIC_ADD, new Callback() {
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
                            myHandler.sendEmptyMessage(HomeFragment.DYNAMICS_UPDATE_RESULT);
                            PictureFileUtils.deleteCacheDirFile(getContext());
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
                        Toast.makeText(AddDynamicsActivity.this, "Ê§°Ü", Toast.LENGTH_SHORT).show();
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
                    
               
                    adapter.setList(selectList);
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    }
    
     public void setResultWrapper(){
        Slog.d(TAG, "----->setResultWrapper");
        Intent intent = new Intent();
        setResult(HomeFragment.DYNAMICS_UPDATE_RESULT, intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Slog.d(TAG, "-------->onDestroy");
        //setResultWrapper();
    }
    
    public void handleMessage(Message msg){
        switch (msg.what){
            case HomeFragment.DYNAMICS_UPDATE_RESULT:
                setResultWrapper();
                dismissProgressDialog();
                finish();
                break;
        }
    }
    
    static class MyHandler extends Handler {
        WeakReference<AddDynamicsActivity> addDynamicsActivityWeakReference;

        MyHandler(AddDynamicsActivity dynamicsInteractDetailsActivity) {
            addDynamicsActivityWeakReference = new WeakReference<AddDynamicsActivity>(dynamicsInteractDetailsActivity);
        }
        @Override
        public void handleMessage(Message message) {
            AddDynamicsActivity dynamicsInteractDetailsActivity = addDynamicsActivityWeakReference.get();
            if (dynamicsInteractDetailsActivity != null) {
                dynamicsInteractDetailsActivity.handleMessage(message);
            }
        }
    }
}

                           
