package com.mufu.experience;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mufu.R;
import com.mufu.adapter.GridImageAdapter;
import com.mufu.common.MyApplication;
import com.mufu.common.OnItemClickListener;
import com.mufu.dynamics.AddDynamicsActivity;
import com.mufu.main.FullyGridLayoutManager;
import com.mufu.picture.GlideEngine;
import com.mufu.util.BaseDialogFragment;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.Slog;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.style.PictureWindowAnimationStyle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;
import static com.mufu.experience.GuideApplyDialogFragment.MODIFY_ROUTE_INFO_URL;
import static com.mufu.experience.GuideApplyDialogFragment.ROUTE_REQUEST_CODE;
import static com.mufu.experience.GuideApplyDialogFragment.SUBMIT_ROUTE_INFO_URL;
import static com.mufu.experience.GuideApplyDialogFragment.WRITE_ROUTE_INFO_SUCCESS;

public class RouteItemEditDF extends BaseDialogFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "RouteItemEditDF";
    private Dialog mDialog;
    private Window window;
    private Button saveBtn;
    private EditText routeNameEdit;
    private EditText routeIntroductionEdit;
    private Button modify;
    private int index;
    private int sid;
    private boolean isModified = false;
    private boolean isFilled = false;
    private AddDynamicsActivity addDynamicsActivity;
    private RecyclerView recyclerView;
    private GridImageAdapter adapter;
    private GuideApplyDialogFragment.Route route;
    private List<LocalMedia> selectList = new ArrayList<>();
    private List<File> selectFileList = new ArrayList<>();
    private MyHandler myHandler;
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            isModified = true;
        }
        @Override
        public void afterTextChanged(Editable editable) {}
    };

    public static RouteItemEditDF newInstance(int index, int sid, GuideApplyDialogFragment.Route initRoute) {
        RouteItemEditDF routeItemEditDF = new RouteItemEditDF();
        Bundle bundle = new Bundle();
        bundle.putInt("index", index);
        bundle.putInt("sid", sid);
        if (initRoute != null) {
            bundle.putParcelable("route", initRoute);
        }
        routeItemEditDF.setArguments(bundle);

        return routeItemEditDF;
    }

    public static RouteItemEditDF newInstance(int index, GuideApplyDialogFragment.Route route) {
        RouteItemEditDF routeItemEditDF = new RouteItemEditDF();
        Bundle bundle = new Bundle();
        bundle.putInt("index", index);
        if (route != null) {
            bundle.putParcelable("route", route);
        }
        routeItemEditDF.setArguments(bundle);

        return routeItemEditDF;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_MaterialComponents_DialogWhenLarge);
        mDialog.setContentView(R.layout.route_item_edit);
        myHandler = new MyHandler(this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            sid = bundle.getInt("sid");
            index = bundle.getInt("index");
            route = bundle.getParcelable("route");

            if (route != null) {
                isFilled = true;
            }
        }

        mDialog.setCanceledOnTouchOutside(true);
        window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        TextView leftBack = mDialog.findViewById(R.id.left_back);
        leftBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkFillStatus()) {
                    if (isModified) {
                        showNoticeDialog();
                    } else {
                        dismiss();
                    }
                } else {
                    dismiss();
                }
            }
        });

        modify = mDialog.findViewById(R.id.route_modify);

        initView();

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);

        return mDialog;
    }

    private void initView() {
        saveBtn = mDialog.findViewById(R.id.save_route);
        routeNameEdit = mDialog.findViewById(R.id.route_name_edittext);
        routeIntroductionEdit = mDialog.findViewById(R.id.route_introduction_edittext);
        if (addDynamicsActivity == null) {
            addDynamicsActivity = new AddDynamicsActivity();
        }

        recyclerView = mDialog.findViewById(R.id.add_route_picture);
        FullyGridLayoutManager manager = new FullyGridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);

        adapter = new GridImageAdapter(getContext(), onAddPicClickListener);
        adapter.setList(selectList);
        adapter.setSelectMax(8);
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
                            PictureSelector.create(RouteItemEditDF.this)
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

        adapter.setItemDeleteListener(new GridImageAdapter.OnPicDeleteListener() {
            @Override
            public void onPicDelete(int position) {
                Slog.d(TAG, "pic delete");
                if (route != null) {
                    isModified = true;
                }
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validCheck()) {
                    submitRoute();
                }
            }
        });

        if (isFilled) {
            routeNameEdit.setText(route.name);
            routeIntroductionEdit.setText(route.introduction);
            Slog.d(TAG, "----------------------->route.selectPicture.size: " + route.selectPicture.size());
            if (route.selectPicture.size() > 0) {
                selectList.clear();
                selectList.addAll(route.selectPicture);
                adapter.setList(selectList);
                adapter.notifyDataSetChanged();
            }
            saveBtn.setVisibility(View.GONE);
            modify.setVisibility(View.VISIBLE);
            modify.setBackground(getContext().getResources().getDrawable(R.drawable.btn_stress));
            routeNameEdit.setEnabled(false);
            routeIntroductionEdit.setEnabled(false);
            adapter.setDeleteBtnStatus(false);

            modify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    saveBtn.setVisibility(View.VISIBLE);
                    routeNameEdit.setEnabled(true);
                    routeIntroductionEdit.setEnabled(true);
                    adapter.setDeleteBtnStatus(true);
                    adapter.notifyDataSetChanged();
                }
            });
            routeNameEdit.addTextChangedListener(textWatcher);
            routeIntroductionEdit.addTextChangedListener(textWatcher);
        }

    }

    private void submitRoute() {
        showProgressDialog(getContext().getString(R.string.saving_progress));
        if (route == null) {
            route = new GuideApplyDialogFragment.Route();
        }
        route.name = routeNameEdit.getText().toString();
        route.introduction = routeIntroductionEdit.getText().toString();

        if (isModified || route.selectPicture.size() > 0) {
            route.selectPicture.clear();
        }

        route.selectPicture.addAll(selectList);

        Map<String, String> authenMap = new HashMap<>();
        if (isModified) {
            authenMap.put("rid", String.valueOf(route.getRid()));
        } else {
            authenMap.put("sid", String.valueOf(sid));
        }

        authenMap.put("name", routeNameEdit.getText().toString());
        authenMap.put("introduction", routeIntroductionEdit.getText().toString());
        if (selectList.size() > 0) {
            Slog.d(TAG, "----------------->submitRoute selectList: " + selectList);
            for (LocalMedia media : selectList) {
                selectFileList.add(new File(media.getCompressPath()));
            }
            uploadPictures(authenMap, "authen", selectFileList, isModified);
        }

    }

    private void uploadPictures(Map<String, String> params, String picKey, List<File> files, boolean isModified) {
        Slog.d(TAG, "--------------------->uploadPictures file size: " + files.size());
        String uri = SUBMIT_ROUTE_INFO_URL;

        if (isModified) {
            uri = MODIFY_ROUTE_INFO_URL;
        }

        HttpUtil.uploadPictureHttpRequest(getContext(), params, picKey, files, uri, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    try {
                        String responseText = response.body().string();
                        Slog.d(TAG, "---------------->uploadPictures response: " + responseText);
                        int result = new JSONObject(responseText).optInt("result");
                        if (!isModified) {
                            int rid = new JSONObject(responseText).optInt("rid");
                            route.setRid(rid);
                        }

                        if (result == 1) {
                            dismissProgressDialog();
                            selectList.clear();
                            selectFileList.clear();
                            //PictureFileUtils.deleteAllCacheDirFile(MyApplication.getContext());
                            myHandler.sendEmptyMessage(WRITE_ROUTE_INFO_SUCCESS);
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

    private void callBacktoCaller() {

        if (getTargetFragment() != null) {
            Intent intent = new Intent();
            intent.putExtra("route", route);
            intent.putExtra("index", index);
            intent.putExtra("isModified", isModified);
            getTargetFragment().onActivityResult(ROUTE_REQUEST_CODE, RESULT_OK, intent);
            mDialog.dismiss();
        }
    }

    private void showNoticeDialog() {
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(getContext(), R.style.Theme_MaterialComponents_Light_Dialog_Alert);
        normalDialog.setTitle("确认放弃本次编辑吗？");
        normalDialog.setMessage("编辑尚未保存，若返回将会丢弃。");

        normalDialog.setPositiveButton("放弃",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDialog.dismiss();
                    }
                });

        normalDialog.setNegativeButton("保存",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //saveBtn.callOnClick();
                        submitRoute();
                    }
                });

        normalDialog.show();
    }

    private boolean validCheck() {

        if (TextUtils.isEmpty(routeNameEdit.getText().toString())) {
            Toast.makeText(getContext(), getResources().getString(R.string.route_name_empty_notice), Toast.LENGTH_LONG).show();
            return false;
        }

        if (TextUtils.isEmpty(routeIntroductionEdit.getText().toString())) {
            Toast.makeText(getContext(), getResources().getString(R.string.route_introduction_empty_notice), Toast.LENGTH_LONG).show();
            return false;
        }

        if (selectList.size() == 0) {
            Toast.makeText(getContext(), getResources().getString(R.string.route_picture_empty_notice), Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private boolean checkFillStatus() {
        if (!TextUtils.isEmpty(routeNameEdit.getText().toString())) {
            return true;
        }

        if (!TextUtils.isEmpty(routeIntroductionEdit.getText().toString())) {
            return true;
        }

        if (selectList.size() > 0) {
            return true;
        }

        return false;
    }

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
                    if (isFilled) {
                        isModified = true;
                    }
                    break;
            }
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case WRITE_ROUTE_INFO_SUCCESS:
                callBacktoCaller();
                break;

        }
    }

    private GridImageAdapter.onAddPicClickListener onAddPicClickListener = new GridImageAdapter.onAddPicClickListener() {
        @Override
        public void onAddPicClick() {
            //boolean mode = cb_mode.isChecked();
            boolean mode = true;
            if (mode) {
                PictureSelector.create(RouteItemEditDF.this)
                        .openGallery(PictureMimeType.ofImage())
                        .loadImageEngine(GlideEngine.createGlideEngine())
                        .theme(R.style.picture_WeChat_style)
                        .isWeChatStyle(true)
                        .setPictureStyle(addDynamicsActivity.getWeChatStyle())
                        .setPictureCropStyle(addDynamicsActivity.getCropParameterStyle())
                        .setPictureWindowAnimationStyle(new PictureWindowAnimationStyle())
                        .isWithVideoImage(true)
                        .maxSelectNum(8)
                        .minSelectNum(1)
                        .maxVideoSelectNum(1)
                        .imageSpanCount(4)
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
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
    }


    @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
    }

    static class MyHandler extends Handler {
        WeakReference<RouteItemEditDF> routeItemEditDFWeakReference;

        MyHandler(RouteItemEditDF routeItemEditDF) {
            routeItemEditDFWeakReference = new WeakReference<RouteItemEditDF>(routeItemEditDF);
        }

        @Override
        public void handleMessage(Message message) {
            RouteItemEditDF routeItemEditDF = routeItemEditDFWeakReference.get();
            if (routeItemEditDF != null) {
                routeItemEditDF.handleMessage(message);
            }
        }
    }
}
