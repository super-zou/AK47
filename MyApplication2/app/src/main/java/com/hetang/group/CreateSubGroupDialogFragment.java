package com.hetang.group;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hetang.R;
import com.hetang.common.MyApplication;
import com.hetang.group.SubGroupActivity;
import com.hetang.util.BaseDialogFragment;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.RoundImageView;
import com.hetang.util.Slog;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import org.angmarch.views.NiceSpinner;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;
import static com.hetang.group.MeetSingleGroupFragment.GROUP_ADD_BROADCAST;
import static com.hetang.group.SubGroupDetailsActivity.GROUP_MODIFY_BROADCAST;

/**
 * Created by super-zou on 18-9-9.
 */

public class CreateSubGroupDialogFragment extends BaseDialogFragment {
    private Dialog mDialog;
    private int type = 0;
    private String defaultUniversity = "";
    private List<LocalMedia> logoSelectList = new ArrayList<>();
    private List<File> logoSelectFileList = new ArrayList<>();
    private static final boolean isDebug = true;
    private static final String TAG = "CreateSubGroupDialogFragment";
    private static final String SUBGROUP_CREATE = HttpUtil.DOMAIN + "?q=subgroup/create";
    private static final String SUBGROUP_MODIFY = HttpUtil.DOMAIN + "?q=subgroup/modify";
    private EditText nameEditText;
    private EditText profileEditText;
    private TextView regionEditText;
    private TextView logoLabel;
    private RoundImageView groupLogo;
    private String org = "";
    private boolean isModify = false;
    private SubGroupActivity.SubGroup subGroup;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_Design_BottomSheetDialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.create_subgroup);

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.create_subgroup), font);
        mDialog.setCanceledOnTouchOutside(true);
        Window window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        TextView save = mDialog.findViewById(R.id.save);
        save.setVisibility(View.VISIBLE);

        TextView title = mDialog.findViewById(R.id.title);
        title.setText(getString(R.string.create_group));

        Bundle bundle = getArguments();
        if (bundle != null) {
            type = bundle.getInt("type", 0);
            isModify = bundle.getBoolean("isModify", false);
            if (isModify){
                title.setText("修改团信息");
                subGroup = (SubGroupActivity.SubGroup)bundle.getSerializable("subgroup");
            }
        }

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSingleGroup();
            }
        });

        TextView back = mDialog.findViewById(R.id.left_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });

        nameEditText = mDialog.findViewById(R.id.editTextName);
        profileEditText = mDialog.findViewById(R.id.editTextProfile);
        regionEditText = mDialog.findViewById(R.id.editTextOrgCity);
        logoLabel = mDialog.findViewById(R.id.group_logo_label);
        groupLogo = mDialog.findViewById(R.id.group_logo);

        initView();

        return mDialog;
    }

    private void initView() {
        String[] universityArray = getResources().getStringArray(R.array.university);
        NiceSpinner niceSpinnerUniversity = mDialog.findViewById(R.id.university_spinner);
        final List<String> universityList = new LinkedList<>(Arrays.asList(universityArray));
        niceSpinnerUniversity.attachDataSource(universityList);
        defaultUniversity = universityList.get(0);
        niceSpinnerUniversity.addOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                //selfCondition.setBirthYear(Integer.parseInt(universityList.get(i)));
                org = universityList.get(i);
            }
        });

        final TextView setLogo = mDialog.findViewById(R.id.set_logo);
        setLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isModify){
                    setLogo();
                }
            }
        });

        groupLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLogo();
            }
        });

        if (isModify){
            nameEditText.setText(subGroup.groupName);
            profileEditText.setText(subGroup.groupProfile);
            for (int i=0;i<universityList.size();i++){
                if (subGroup.org.equals(universityList.get(i))){
                    niceSpinnerUniversity.setSelectedIndex(i);
                    break;
                }
            }
            org = subGroup.org;
            logoLabel.setVisibility(View.GONE);
            setLogo.setVisibility(View.GONE);
            groupLogo.setVisibility(View.GONE);
        }
    }

    private void setLogo() {
        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        final int width = dm.widthPixels;
        final int screenHeight = width;

        int themeId = R.style.picture_default_style;

        PictureSelector.create(this)
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Slog.d(TAG, "------------------------------>onActivityResult resultCode: " + resultCode + " requestCode: " + requestCode);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.SINGLE:
                    // 图片选择结果回调
                    logoSelectList = PictureSelector.obtainMultipleResult(data);
                    if (logoSelectList.size() > 0) {

                        if (logoSelectFileList.size() > 0) {
                            logoSelectFileList.clear();
                        }
                        // 例如 LocalMedia 里面返回三种path
                        // 1.media.getPath(); 为原图path
                        // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                        // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                        // 如果裁剪并压缩了，已取压缩路径为准，因为是先裁剪后压缩的
                        Slog.d(TAG, "Selected pictures: " + logoSelectList.size());
                        //activity_picture_array = new String[selectList.size()];
                        for (LocalMedia media : logoSelectList) {

                            Log.i("图片-----》", media.getPath());
                            Log.d("压缩图片------->>", media.getCompressPath());
                            Slog.d(TAG, "===========num: " + media.getNum());
                            //activity_picture_array[media.getNum() - 1] = media.getCompressPath();
                            logoSelectFileList.add(new File(media.getCompressPath()));

                            Bitmap bitmap = BitmapFactory.decodeFile(media.getCompressPath());
                            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                            groupLogo.setVisibility(View.VISIBLE);
                            groupLogo.setImageDrawable(null);
                            groupLogo.setImageDrawable(drawable);
                        }
                    }
                    break;

                default:
                    break;
            }
        }
    }

    public void saveSingleGroup() {

        String groupName = nameEditText.getText().toString();
        String groupProfile = profileEditText.getText().toString();
        String groupOrg = org;
        String groupRegion = regionEditText.getText().toString();

        if (TextUtils.isEmpty(groupName)) {
            Toast.makeText(MyApplication.getContext(), "请输入团名", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(groupProfile)) {
            Toast.makeText(MyApplication.getContext(), "请介绍一下你创建的团", Toast.LENGTH_LONG).show();
            return;
        }

        Slog.d(TAG, "---------------->groupOrg: " + groupOrg + " default university: " + defaultUniversity);
        if (TextUtils.isEmpty(groupOrg) || groupOrg.equals(defaultUniversity)) {
            Toast.makeText(MyApplication.getContext(), "请选择该团所属高校", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(groupRegion)) {
            Toast.makeText(MyApplication.getContext(), "请选择所在城市或地区", Toast.LENGTH_LONG).show();
            return;
        }

        showProgressDialog("正在保存");
        if (logoSelectFileList.size() > 0) {//with logo picture
            Map<String, String> groupInfoText = new HashMap<>();
            groupInfoText.put("type", String.valueOf(type));
            groupInfoText.put("group_name", groupName);
            groupInfoText.put("group_profile", groupProfile);
            groupInfoText.put("group_org", org);
            groupInfoText.put("region", groupRegion);
            if (isModify){
                groupInfoText.put("gid", String.valueOf(subGroup.gid));
            }

            uploadPictures(groupInfoText, "picture", logoSelectFileList);
        } else {//without logo picture
            FormBody.Builder builder = new FormBody.Builder();
            builder.add("type", String.valueOf(type))
                    .add("group_name", groupName)
                    .add("group_profile", groupProfile)
                    .add("group_org", groupOrg)
                    .add("region", groupRegion);
            String uri;
            if (isModify){
                builder.add("gid", String.valueOf(subGroup.gid));
                uri = SUBGROUP_MODIFY;
            }else {
                uri = SUBGROUP_CREATE;
            }

            final RequestBody requestBody = builder.build();
            HttpUtil.sendOkHttpRequest(getContext(), uri, requestBody, new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (isDebug) Slog.d(TAG, "==========response body : " + response.body());

                    if (response.body() != null) {
                        String responseText = response.body().string();
                        if (isDebug) Slog.d(TAG, "==========response text : " + responseText);
                        if (responseText != null && !TextUtils.isEmpty(responseText)) {
                            //if (isDebug) Slog.d(TAG, "==========response text 1: " + responseText);
                            try {
                                JSONObject responseObj = new JSONObject(responseText);
                                if (responseObj != null) {
                                    if(!isModify){
                                        int gid = responseObj.optInt("gid");
                                        if (gid > 0) {
                                            sendBroadcast(gid);
                                        }
                                    }else {
                                        int status = responseObj.optInt("status");
                                        Slog.d(TAG, "--------------------------------->status: "+status);
                                        if (status == 1){
                                            sendModifyBroadcast();
                                        }
                                    }

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            dismissProgressDialog();
                            mDialog.dismiss();
                        }
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {

                }
            });
        }
    }

    private void uploadPictures(Map<String, String> params, String picKey, List<File> files) {
        HttpUtil.uploadPictureHttpRequest(getContext(), params, picKey, files, SUBGROUP_CREATE, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    try {
                        String responseText = response.body().string();
                        Slog.d(TAG, "---------------->response: " + responseText);

                        int gid = new JSONObject(responseText).optInt("gid");
                        Slog.d(TAG, "-------------->create group gid: " + gid);

                        if (gid > 0) {
                            logoSelectFileList.clear();
                            //sendBroadcast();//send broadcast to meetdynamicsfragment notify  meet dynamics to update
                            //setCommentUpdateResult();
                            // myHandler.sendEmptyMessage(HomeFragment.DYNAMICS_UPDATE_RESULT);
                            dismissProgressDialog();
                            sendBroadcast(gid);
                            mDialog.dismiss();
                            // finish();
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
                        Toast.makeText(getContext(), "上传失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void sendBroadcast(int gid) {
        Intent intent = new Intent(GROUP_ADD_BROADCAST);
        intent.putExtra("gid", gid);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    private void sendModifyBroadcast() {
        Intent intent = new Intent(GROUP_MODIFY_BROADCAST);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

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


}
