package com.hetang.experience;

import android.app.AlertDialog;
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
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
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
import com.hetang.common.SetAvatarActivity;
import com.hetang.dynamics.AddDynamicsActivity;
import com.hetang.group.SubGroupActivity;
import com.hetang.main.FullyGridLayoutManager;
import com.hetang.picture.GlideEngine;
import com.hetang.talent.TalentDetailsActivity;
import com.hetang.util.BaseDialogFragment;
import com.hetang.util.CommonBean;
import com.hetang.util.CommonDialogFragmentInterface;
import com.hetang.util.CommonPickerView;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.RoundImageView;
import com.hetang.util.Slog;
import com.hetang.widget.CalendarView;
import com.luck.picture.lib.PictureSelectionModel;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.style.PictureWindowAnimationStyle;
import com.luck.picture.lib.tools.PictureFileUtils;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;
import static com.hetang.archive.ArchiveFragment.REQUESTCODE;
import static com.hetang.common.SetAvatarActivity.SUBMIT_TALENT_AUTHENTICATION_ACTION_BROADCAST;
import static com.hetang.common.SetAvatarActivity.TALENT_AUTHENTICATION_PHOTO;
import static com.hetang.group.GroupFragment.eden_group;
import static com.hetang.group.SubGroupActivity.TALENT_ADD_BROADCAST;
import static com.hetang.group.SubGroupActivity.getTalent;

public class TravelGuideAuthenticationDialogFragment extends BaseDialogFragment {
    public final static int TALENT_AUTHENTICATION_RESULT_OK = 0;
    public final static int COMMON_TALENT_AUTHENTICATION_RESULT_OK = 1;
    private static final boolean isDebug = true;
    private static final String TAG = "TravelGuideAuthenticationDialogFragment";
    private static final String SUBMIT_URL = HttpUtil.DOMAIN + "?q=talent/become/apply";
    private static final int MSG_LOAD_SUCCESS = 0x0003;
    private Thread threadCity;
    private int type;
    private int gid;
    private int aid;
    private SubGroupActivity.Talent talent;
    private Dialog mDialog;
    private Context mContext;
    private String uri;
    private boolean isSubmit = false;
    private EditText introductionET;
    private RoundImageView rewardQRCode;
    private QRCodeSetBroadcastReceiver mReceiver;
    private CommonDialogFragmentInterface commonDialogFragmentInterface;
    private GridLayout authenticateWrapper;
    private ConstraintLayout navigation;
    private Button prevBtn;
    private Button nextBtn;
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
    private int pictureSelectType = 0;//default 0 for materia
    private boolean isPicked = false;
    private Thread threadIndustry = null;
    private boolean isLocated = false;
    private ArrayList<CommonBean> provinceItems = new ArrayList<>();
    private ArrayList<ArrayList<String>> cityItems = new ArrayList<>();

    //@BindView(R.id.calendarView)
    MaterialCalendarView widget;
    CalendarDay today;

    private GridImageAdapter.onAddPicClickListener onAddPicClickListener = new GridImageAdapter.onAddPicClickListener() {
        @Override
        public void onAddPicClick() {
            if (pictureSelectType == 0) {
                maxSelectNum = 6;
            } else {
                maxSelectNum = 1;
            }
            PictureSelectionModel pictureSelectionModel = PictureSelector.create(TravelGuideAuthenticationDialogFragment.this).openGallery(PictureMimeType.ofImage());
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
                    .selectionMode(PictureConfig.MULTIPLE)// 多选 or 单选
                    //.isSingleDirectReturn(cb_single_back.isChecked())// 单选模式下是否直接返回，PictureConfig.SINGLE模式下有效
                    .previewImage(true)// 是否可预览图片
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

            if (pictureSelectType == 0) {
                pictureSelectionModel.forResult(PictureConfig.CHOOSE_REQUEST);
            } else {
                pictureSelectionModel.forResult(PictureConfig.SINGLE);
            }
        }
    };

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
            type = bundle.getInt("type", 0);
        }

        authenObject = new JSONObject();

        mDialog.setContentView(R.layout.travel_guide_authentication);

        //initView();

        initCalendarView();

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

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);

        if (addDynamicsActivity == null) {
            addDynamicsActivity = new AddDynamicsActivity();
        }

        return mDialog;
    }

    private void initView() {
        addMateriaRV = mDialog.findViewById(R.id.add_materia);
        addRewardQRRV = mDialog.findViewById(R.id.add_reward_qr);
        chargeSetting = mDialog.findViewById(R.id.charge_setting);
        chargeIntroduction = mDialog.findViewById(R.id.charge_introduction);
        FullyGridLayoutManager manager = new FullyGridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false);
        FullyGridLayoutManager managerReward = new FullyGridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false);
        addMateriaRV.setLayoutManager(manager);
        addRewardQRRV.setLayoutManager(managerReward);

        //for authentication materia
        adapter = new GridImageAdapter(getContext(), onAddPicClickListener);
        adapter.setList(selectList);
        adapter.setSelectMax(6);
        addMateriaRV.setAdapter(adapter);

        //for reward qr code
        adapterReward = new GridImageAdapter(getContext(), onAddPicClickListener);
        adapterReward.setList(selectRewardList);
        adapterReward.setSelectMax(1);
        addRewardQRRV.setAdapter(adapterReward);

        selectSubject = mDialog.findViewById(R.id.select_subject);
        authenticateWrapper = mDialog.findViewById(R.id.talent_authentication_wrapper);
        navigation = mDialog.findViewById(R.id.navigation);
        prevBtn = mDialog.findViewById(R.id.prev);
        nextBtn = mDialog.findViewById(R.id.next);
        introductionET = mDialog.findViewById(R.id.introduction_edittext);

        //init city data in thread
        if (threadCity == null) {
            threadCity = new Thread(new Runnable() {

                @Override
                public void run() {
                    initCityJsondata("city.json");
                }
            });
            threadCity.start();
        }

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (index > 0) {
                    if (index == 4) {
                        nextBtn.setText(getContext().getResources().getString(R.string.next));
                    }
                    index--;
                    if (index == 1) {
                        prevBtn.setVisibility(View.GONE);
                    }
                    authenticateWrapper.getChildAt(index).setVisibility(View.VISIBLE);
                    authenticateWrapper.getChildAt(index + 1).setVisibility(View.GONE);
                    Slog.d(TAG, "-------------------->index: " + index);
                }
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validCheck(index)) {
                    if (prevBtn.getVisibility() == View.GONE) {
                        prevBtn.setVisibility(View.VISIBLE);
                    }
                    if (index < 4) {
                        index++;
                        if (index == 4) {
                            nextBtn.setText(getContext().getResources().getString(R.string.done));
                        }
                        authenticateWrapper.getChildAt(index).setVisibility(View.VISIBLE);
                        authenticateWrapper.getChildAt(index - 1).setVisibility(View.GONE);

                        processCurrent(index);

                    } else {
                        //submit();
                        submitNotice();
                    }
                }
            }
        });
    }

    private void initCalendarView() {
        //widget = mDialog.findViewById(R.id.calendarView);
        //unbinder = ButterKnife.bind(getActivity());
        today = CalendarDay.today();
        widget = mDialog.findViewById(R.id.calendarView);
        //widget.setCurrentDate(today);
        //widget.setSelectedDate(today);
        widget.addDecorator(new DisabledDecorator());

        final LocalDate min = LocalDate.of(today.getYear(), today.getMonth(), today.getDay());
        //final LocalDate max = LocalDate.of(today.getYear(), today.getMonth()+3, today.getDay());

        widget.state().edit()
                .setMinimumDate(min)
               //.setMaximumDate(max)
                .commit();
    }

    private static class DisabledDecorator implements DayViewDecorator{
        @Override public boolean shouldDecorate(final CalendarDay day) {
            if (day.getMonth() == CalendarDay.today().getMonth()){
                return true;
            }
            return false;
        }

        @Override public void decorate(final DayViewFacade view) {
            view.setDaysDisabled(true);
        }
    }

    private void initCityJsondata(String jsonFile) {
        CommonPickerView commonPickerView = new CommonPickerView();
        provinceItems = commonPickerView.getOptionsMainItem(getContext(), jsonFile);
        cityItems = commonPickerView.getOptionsSubItems(provinceItems);
    }

    private void showCityPickerView() {// 弹出地址选择器
        //条件选择器
        OptionsPickerView pvOptions;
        pvOptions= new OptionsPickerBuilder(getContext(), new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3 ,View v) {
                //返回的分别是二个级别的选中位置
                isLocated = true;
                String city = cityItems.get(options1).get(option2);
            }
        }).build();
        pvOptions.setPicker(provinceItems, cityItems);
        pvOptions.show();
    }

    private void submitNotice() {
        final AlertDialog.Builder normalDialogBuilder =
                new AlertDialog.Builder(getContext());
        normalDialogBuilder.setTitle(getResources().getString(R.string.talent_apply));
        normalDialogBuilder.setMessage(getResources().getString(R.string.talent_apply_content));
        normalDialogBuilder.setPositiveButton(getContext().getResources().getString(R.string.submit),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        submit();
                    }
                });

        AlertDialog normalDialog = normalDialogBuilder.create();
        normalDialog.show();
    }

    private void submit() {
        Map<String, String> authenMap = new HashMap<>();
        authenMap.put("introduction", introductionET.getText().toString());
        authenMap.put("subject", selectSubject.getText().toString());
        authenMap.put("charge", chargeSetting.getText().toString());
        if (!TextUtils.isEmpty(chargeIntroduction.getText().toString())) {
            authenMap.put("charge_desc", chargeIntroduction.getText().toString());
        }

        if (selectList.size() > 0) {
            for (LocalMedia media : selectList) {
                selectFileList.add(new File(media.getCompressPath()));
            }
        }
        if (selectRewardList.size() > 0) {
            for (LocalMedia media : selectRewardList) {
                selectFileList.add(new File(media.getCompressPath()));
            }
        }

        authenMap.put("type", String.valueOf(type));

        Slog.d(TAG, "--------------------->file size: " + selectFileList.size());

        uploadPictures(authenMap, "authen", selectFileList);
    }

    private boolean validCheck(int index) {
        Slog.d(TAG, "------------------------>validCheck: " + index);
        boolean valid = false;
        switch (index) {
            case 1:
                String introduction = introductionET.getText().toString();
                if (!TextUtils.isEmpty(introduction)) {
                    valid = true;
                } else {
                    introductionET.setError(getContext().getResources().getString(R.string.talent_introduction_empty));
                }
                break;
            case 2:
                valid = true;
                break;
            case 3:
                valid = isPicked;
                if (!isPicked) {
                    Toast.makeText(getContext(), getResources().getString(R.string.subject_select_notice), Toast.LENGTH_LONG).show();
                }
                break;

            case 4:
                String chargeAmount = chargeSetting.getText().toString();
                if (!TextUtils.isEmpty(chargeAmount)) {
                    valid = true;
                } else {
                    Toast.makeText(getContext(), getResources().getString(R.string.charge_setting_notice), Toast.LENGTH_LONG).show();
                    valid = false;
                    return false;
                }

                String chargeDesc = chargeIntroduction.getText().toString();
                if (!TextUtils.isEmpty(chargeDesc)) {
                    valid = true;
                } else {
                    Toast.makeText(getContext(), getResources().getString(R.string.charge_introduction_notice), Toast.LENGTH_LONG).show();
                    valid = false;
                    return valid;
                }

                if (selectRewardList.size() == 0) {
                    Toast.makeText(getContext(), getResources().getString(R.string.qr_code_notice), Toast.LENGTH_LONG).show();
                    valid = false;
                    return valid;
                } else {
                    valid = true;
                }

                break;
            default:
                valid = false;
                break;
        }

        return valid;
    }

    private void processCurrent(int index) {
        Slog.d(TAG, "------------------------>processCurrent: " + index);
        switch (index) {
            case 2:
                pictureSelectType = 0;
                addMateria(adapter);
                break;
            case 4:
                pictureSelectType = 1;
                addMateria(adapterReward);
                break;
        }
    }

    private void addMateria(GridImageAdapter imageAdapter) {
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
                            PictureSelector.create(TravelGuideAuthenticationDialogFragment.this)
                                    .themeStyle(R.style.picture_WeChat_style) // xml设置主题
                                    .setPictureStyle(addDynamicsActivity.getWeChatStyle())// 动态自定义相册主题
                                    //.setPictureWindowAnimationStyle(animationStyle)// 自定义页面启动动画
                                    .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)// 设置相册Activity方向，不设置默认使用系统
                                    .isNotPreviewDownload(true)// 预览图片长按是否可以下载
                                    //.bindCustomPlayVideoCallback(callback)// 自定义播放回调控制，用户可以使用自己的视频播放界面
                                    .loadImageEngine(GlideEngine.createGlideEngine())// 外部传入图片加载引擎，必传项
                                    .openExternalPreview(position, selectList);
                            break;
                        case PictureConfig.TYPE_VIDEO:
                            PictureSelector.create(TravelGuideAuthenticationDialogFragment.this).externalPictureVideo(media.getPath());
                            // 预览视频
                            PictureSelector.create(TravelGuideAuthenticationDialogFragment.this)
                                    .themeStyle(R.style.picture_WeChat_style)
                                    .setPictureStyle(addDynamicsActivity.getWeChatStyle())// 动态自定义相册主题
                                    .externalPictureVideo(media.getPath());
                            break;
                        case PictureConfig.TYPE_AUDIO:
                            // 预览音频
                            PictureSelector.create(TravelGuideAuthenticationDialogFragment.this)
                                    .externalPictureAudio(
                                            media.getPath().startsWith("content://") ? media.getAndroidQToPath() : media.getPath());
                            break;
                    }
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    selectList = PictureSelector.obtainMultipleResult(data);
                    if (selectList.size() > 0) {
                        adapter.setList(selectList);
                        adapter.notifyDataSetChanged();
                    }
                    break;
                case PictureConfig.SINGLE:
                    selectRewardList = PictureSelector.obtainMultipleResult(data);
                    if (selectRewardList.size() > 0) {
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

    private void initMatchMakerTalent() {
        LinearLayout explanationLL = mDialog.findViewById(R.id.explanation);
        ConstraintLayout authenticationCL = mDialog.findViewById(R.id.talent_authentication);

        Button beginBtn = mDialog.findViewById(R.id.begin_now);
        beginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                explanationLL.setVisibility(View.GONE);
                authenticationCL.setVisibility(View.VISIBLE);
                initMatchMakerAuthentication();
            }
        });
    }

    private void initMatchMakerAuthentication() {
        TextView title = mDialog.findViewById(R.id.title);
        title.setText("提交达人申请");
        Button save = mDialog.findViewById(R.id.submit);
        Button cancel = mDialog.findViewById(R.id.cancel);
        save.setVisibility(View.VISIBLE);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkValid()) {
                    submitMatchMaker();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        introductionET = mDialog.findViewById(R.id.introduction_edittext);
        rewardQRCode = mDialog.findViewById(R.id.reward_qr_code);

        rewardQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SetAvatarActivity.class);
                intent.putExtra("type", TALENT_AUTHENTICATION_PHOTO);
                getActivity().startActivityForResult(intent, REQUESTCODE);
            }
        });

        registerBroadcast();
    }

    private void submitMatchMaker() {
        showProgressDialog("");
        FormBody.Builder builder = new FormBody.Builder()
                .add("introduction", introductionET.getText().toString())
                .add("uri", uri)
                .add("type", String.valueOf(type));

        RequestBody requestBody = builder.build();

        HttpUtil.sendOkHttpRequest(mContext, SUBMIT_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "saveUserInfo response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        gid = new JSONObject(responseText).optInt("gid");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    isSubmit = true;
                    dismissProgressDialog();
                    mDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });


    }

    private boolean checkValid() {
        if (TextUtils.isEmpty(introductionET.getText().toString())) {
            Toast.makeText(getContext(), "达人介绍不能为空", Toast.LENGTH_LONG).show();
            return false;
        }

        if (TextUtils.isEmpty(uri)) {
            Toast.makeText(getContext(), "请上传赞赏二维码", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void uploadPictures(Map<String, String> params, String picKey, List<File> files) {
        showProgressDialog("");
        HttpUtil.uploadPictureHttpRequest(getContext(), params, picKey, files, SUBMIT_URL, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null) {
                    try {
                        String responseText = response.body().string();
                        Slog.d(TAG, "---------------->response: " + responseText);
                        int status = new JSONObject(responseText).optInt("status");
                        aid = new JSONObject(responseText).optInt("aid");
                        JSONObject talentobject = new JSONObject(responseText).optJSONObject("talent");
                        talent = getTalent(talentobject);
                        if (status == 1) {
                            isSubmit = true;
                            dismissProgressDialog();
                            selectList.clear();
                            selectFileList.clear();
                            selectRewardList.clear();
                            selectRewardFileList.clear();
                            PictureFileUtils.deleteAllCacheDirFile(getContext());
                            startTalentDetailActivity();
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
            }
        });

    }

    private void startTalentDetailActivity() {
        Intent intent = new Intent(getContext(), TalentDetailsActivity.class);
        //intent.putExtra("talent", talent);
        intent.putExtra("aid", talent.aid);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(intent);
        mDialog.dismiss();
    }

    //register local broadcast to receive DYNAMICS_ADD_BROADCAST
    private void registerBroadcast() {
        mReceiver = new QRCodeSetBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SUBMIT_TALENT_AUTHENTICATION_ACTION_BROADCAST);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, intentFilter);
    }

    //unregister local broadcast
    private void unRegisterBroadcast() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
        unRegisterBroadcast();
        if (commonDialogFragmentInterface != null) {//callback from ArchivesActivity class
            if (type == eden_group) {
                commonDialogFragmentInterface.onBackFromDialog(TALENT_AUTHENTICATION_RESULT_OK, gid, isSubmit);
            } else {
                //commonDialogFragmentInterface.onBackFromDialog(COMMON_TALENT_AUTHENTICATION_RESULT_OK, aid, isSubmit);
                sendTalentAddedBroadcast();
            }
        }
    }

    private void sendTalentAddedBroadcast() {
        Intent intent = new Intent(TALENT_ADD_BROADCAST);
        intent.putExtra("aid", aid);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
    }

    private class QRCodeSetBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case SUBMIT_TALENT_AUTHENTICATION_ACTION_BROADCAST:
                    uri = intent.getStringExtra("uri");
                    Glide.with(getContext()).load(HttpUtil.DOMAIN + uri).into(rewardQRCode);
                    break;
                default:
                    break;
            }
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {

        }
    }

    static class MyHandler extends Handler {
        WeakReference<TravelGuideAuthenticationDialogFragment> travelGuideAuthenticationDialogFragmentWeakReference;
        MyHandler(TravelGuideAuthenticationDialogFragment travelGuideAuthenticationDialogFragment) {
            travelGuideAuthenticationDialogFragmentWeakReference = new WeakReference<TravelGuideAuthenticationDialogFragment>(travelGuideAuthenticationDialogFragment);
        }

        @Override
        public void handleMessage(Message message) {
            TravelGuideAuthenticationDialogFragment travelGuideAuthenticationDialogFragment = travelGuideAuthenticationDialogFragmentWeakReference.get();
            if (travelGuideAuthenticationDialogFragment != null) {
                travelGuideAuthenticationDialogFragment.handleMessage(message);
            }
        }
    }
}
