package com.hetang.experience;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hetang.R;
import com.hetang.adapter.GridImageAdapter;
import com.hetang.common.MyApplication;
import com.hetang.common.OnItemClickListener;
import com.hetang.dynamics.AddDynamicsActivity;
import com.hetang.main.FullyGridLayoutManager;
import com.hetang.picture.GlideEngine;
import com.hetang.util.BaseDialogFragment;
import com.hetang.util.FontManager;
import com.hetang.util.Slog;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.style.PictureWindowAnimationStyle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.hetang.experience.TravelGuideAuthenticationDialogFragment.ROUTE_REQUEST_CODE;

public class RouteItemEditDF extends BaseDialogFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "ExperienceTalentAuthentication";
    private Dialog mDialog;
    private Window window;
    private Button saveBtn;
    private EditText routeNameEdit;
    private EditText routeIntroductionEdit;
    private TextView modify;
    private int index;
    private boolean isModify = false;
    private boolean isFilled = false;
    private AddDynamicsActivity addDynamicsActivity;
    private RecyclerView recyclerView;
    private GridImageAdapter adapter;
    private TravelGuideAuthenticationDialogFragment.Route route;
    private List<LocalMedia> selectList = new ArrayList<>();
    private List<File> selectFileList = new ArrayList<>();
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_MaterialComponents_DialogWhenLarge);
        mDialog.setContentView(R.layout.route_item_edit);

        Bundle bundle = getArguments();
        if (bundle != null){
            index = bundle.getInt("index");
            route = bundle.getParcelable("route");
        }
        initView();
        
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
                if (checkFillStatus()){
                    if (!isFilled || isModify){
                        showNoticeDialog();
                    }else {
                        dismiss();
                    }
                }else {
                    dismiss();
                }
            }
        });
        
        TextView modify = mDialog.findViewById(R.id.save);
        modify.setText(getContext().getResources().getString(R.string.modify_route));

        if (route != null){
            isFilled = true;
            saveBtn.setVisibility(View.GONE);
            modify.setVisibility(View.VISIBLE);
            routeNameEdit.setEnabled(false);
            routeIntroductionEdit.setEnabled(false);
            modify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isModify = true;
                    saveBtn.setVisibility(View.VISIBLE);
                    routeNameEdit.setEnabled(true);
                    routeIntroductionEdit.setEnabled(true);
                }
            });
        }
        
        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);

        return mDialog;
    }

    public static RouteItemEditDF newInstance(int index, TravelGuideAuthenticationDialogFragment.Route route){
        RouteItemEditDF routeItemEditDF = new RouteItemEditDF();
        Bundle bundle = new Bundle();
        bundle.putInt("index", index);
        if (route != null){
            bundle.putParcelable("route", route);
        }
        routeItemEditDF.setArguments(bundle);

        return routeItemEditDF;
    }
    
    private void initView() {
        saveBtn = mDialog.findViewById(R.id.save_route);
        routeNameEdit = mDialog.findViewById(R.id.route_name_edittext);
        routeIntroductionEdit = mDialog.findViewById(R.id.route_introduction_edittext);
        if (addDynamicsActivity == null){
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
        
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validCheck()){
                    route = new TravelGuideAuthenticationDialogFragment.Route(routeNameEdit.getText().toString(),
                            routeIntroductionEdit.getText().toString(), selectList);
                    route.name = routeNameEdit.getText().toString();
                    route.introduction = routeIntroductionEdit.getText().toString();
                    route.selectPicture = selectList;
                    
                    if (getTargetFragment() != null){
                        Intent intent = new Intent();
                        intent.putExtra("route", route);
                        intent.putExtra("index", index);
                        intent.putExtra("isModify", isModify);
                        getTargetFragment().onActivityResult(ROUTE_REQUEST_CODE, RESULT_OK, intent);
                        mDialog.dismiss();
                    }
                }
            }
        });
        
        if (route != null){
            routeNameEdit.setText(route.name);
            routeIntroductionEdit.setText(route.introduction);
            if (route.selectPicture.size() > 0){
                selectList = route.selectPicture;
                adapter.setList(selectList);
                adapter.notifyDataSetChanged();
            }
        }
    }
    
    private void showNoticeDialog() {
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(getContext(), R.style.Theme_MaterialComponents_Light_Dialog_Alert);
        normalDialog.setTitle("确认放弃本次编辑吗？");
        normalDialog.setMessage("编辑尚未保存，若返回将会丢弃。");

        normalDialog.setPositiveButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                
                 normalDialog.setNegativeButton("确认",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //saveBtn.callOnClick();
                        mDialog.dismiss();
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

        if (selectList.size() == 0){
            Toast.makeText(getContext(), getResources().getString(R.string.route_picture_empty_notice), Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }
    
    private boolean checkFillStatus(){
        if (!TextUtils.isEmpty(routeNameEdit.getText().toString())) {
            return true;
        }

        if (!TextUtils.isEmpty(routeIntroductionEdit.getText().toString())) {
            return true;
        }

        if (selectList.size() > 0){
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
                    if (selectList.size() > 0){
                        selectList.addAll(PictureSelector.obtainMultipleResult(data));
                    }else {
                        selectList = PictureSelector.obtainMultipleResult(data);
                    }
                    Slog.d(TAG, "Selected pictures: " + selectList.size());
                    adapter.setList(selectList);
                    adapter.notifyDataSetChanged();
                    break;
            }
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
                        .loadImageEngine(GlideEngine.createGlideEngine())// �ⲿ����ͼƬ�������棬�ش���
                        .theme(R.style.picture_WeChat_style)// ������ʽ���� ����ο� values/styles   �÷���R.style.picture.white.style v2.3.3�� ����ʹ��setPictureStyle()��̬��ʽ
                        .isWeChatStyle(true)// �Ƿ���΢��ͼƬѡ����
                        .setPictureStyle(addDynamicsActivity.getWeChatStyle())// ��̬�Զ����������
                        .setPictureCropStyle(addDynamicsActivity.getCropParameterStyle())// ��̬�Զ���ü�����
                        .setPictureWindowAnimationStyle(new PictureWindowAnimationStyle())// �Զ�����������˳�����
                        .isWithVideoImage(true)// ͼƬ����Ƶ�Ƿ����ͬѡ,ֻ��ofAllģʽ����Ч
                        .maxSelectNum(8)// ���ͼƬѡ������
                        .minSelectNum(1)// ��Сѡ������
                        .maxVideoSelectNum(1)
                        .imageSpanCount(4)// ÿ����ʾ����
                        .isReturnEmpty(false)// δѡ������ʱ�����ť�Ƿ���Է���
                        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)// �������Activity���򣬲�����Ĭ��ʹ��ϵͳ
                        .selectionMode(PictureConfig.MULTIPLE )// ��ѡ or ��ѡ
                        .previewImage(true)// �Ƿ��Ԥ��ͼƬ
                        .isCamera(true)// �Ƿ���ʾ���հ�ť
                        .isZoomAnim(true)// ͼƬ�б��� ����Ч�� Ĭ��true
                        .compress(true)// �Ƿ�ѹ��
                        .compressQuality(100)// ͼƬѹ����������� 0~ 100
                        .synOrAsy(true)//ͬ��true���첽false ѹ�� Ĭ��ͬ��
                        .withAspectRatio(1, 1)// �ü����� ��16:9 3:2 3:4 1:1 ���Զ���
                        .freeStyleCropEnabled(true)// �ü����Ƿ����ק
                        .previewEggs(true)// Ԥ��ͼƬʱ �Ƿ���ǿ���һ���ͼƬ����(ͼƬ����һ�뼴�ɿ�����һ���Ƿ�ѡ��)
                        .minimumCompressSize(100)// С��100kb��ͼƬ��ѹ��
                        .forResult(PictureConfig.CHOOSE_REQUEST);//����ص�onActivityResult code
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
}
