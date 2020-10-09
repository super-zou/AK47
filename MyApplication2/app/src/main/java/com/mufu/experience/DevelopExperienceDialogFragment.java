package com.mufu.experience;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import com.mufu.util.Utility;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.mufu.R;
import com.mufu.adapter.GridImageAdapter;
import com.mufu.common.MyApplication;
import com.mufu.common.OnItemClickListener;
import com.mufu.dynamics.AddDynamicsActivity;
import com.mufu.group.SubGroupActivity;
import com.mufu.main.FullyGridLayoutManager;
import com.mufu.picture.GlideEngine;
import com.mufu.util.BaseDialogFragment;

import com.mufu.util.CommonBean;
import com.mufu.util.CommonDialogFragmentInterface;
import com.mufu.util.CommonPickerView;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.Slog;
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
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter;
import com.prolificinteractive.materialcalendarview.format.MonthArrayTitleFormatter;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;
import static com.mufu.experience.GuideApplyDialogFragment.ROUTE_REQUEST_CODE;
import static com.mufu.experience.RouteItemEditDF.newInstance;

public class DevelopExperienceDialogFragment extends BaseDialogFragment implements OnDateSelectedListener {
    private static final boolean isDebug = true;
    public static final String EXPERIENCE_TYPE_GUIDE = "guide";
    public static final String GUIDE_ADD_BROADCAST = "com.hetang.action.GUIDE_ADD";
    private static final String TAG = "DevelopExperienceDialogFragment";
    private static final String SUBMIT_BASE_INFO_URL = HttpUtil.DOMAIN + "?q=experience/write_base_info";
    private static final String MODIFY_BASE_INFO_URL = HttpUtil.DOMAIN + "?q=experience/modify_base_info";
    public static final String SAVE_EXPERIENCE_PICTURES_URL = HttpUtil.DOMAIN + "?q=experience/save_experience_pictures";
    public static final String MODIFY_EXPERIENCE_PICTURES_URL = HttpUtil.DOMAIN + "?q=experience/modify_experience_pictures";
    public static final String SAVE_ITEMS_URL = HttpUtil.DOMAIN + "?q=experience/save_experience_items";
    private static final String SUBMIT_CHARGE_URL = HttpUtil.DOMAIN + "?q=experience/write_charge_info";
    private static final String SUBMIT_TIME_URL = HttpUtil.DOMAIN + "?q=experience/write_time_info";
    private static final String MODIFY_TIME_URL = HttpUtil.DOMAIN + "?q=experience/modify_time_info";
    private static final String SUBMIT_LIMITATION_URL = HttpUtil.DOMAIN + "?q=experience/write_limitation_info";
    private static final String MODIFY_LIMITATION_URL = HttpUtil.DOMAIN + "?q=experience/modify_limitation_info";
    private static final String SUBMIT_ADDRESS_URL = HttpUtil.DOMAIN + "?q=experience/write_address_info";
    private static final String MODIFY_ADDRESS_URL = HttpUtil.DOMAIN + "?q=experience/modify_address_info";
    private static final String MODIFY_CHARGE_URL = HttpUtil.DOMAIN + "?q=experience/modify_charge_info";
    private static final String SUBMIT_APPOINTMENT_DATE_URL = HttpUtil.DOMAIN + "?q=experience/write_experience_appointment_date";
    private static final String MODIFY_APPOINTMENT_DATE_URL = HttpUtil.DOMAIN + "?q=experience/modify_experience_appointment_date";
    private static final String SUBMIT_SELF_INTRODUCTION_URL = HttpUtil.DOMAIN + "?q=experience/write_self_introduction_info";
    private static final String MODIFY_SELF_INTRODUCTION_URL = HttpUtil.DOMAIN + "?q=experience/modify_self_introduction_info";
        public final static int PACKAGE_REQUEST_CODE = 1;
    private static final int WRITE_BASE_INFO_SUCCESS = 1;
    public static final int SAVE_PICTURES_SUCCESS = 2;
    public static final int SAVE_ITEMS_SUCCESS = 3;
    private static final int WRITE_CHARGE_SUCCESS = 4;
    private static final int WRITE_TIME_SUCCESS = 5;
    private static final int WRITE_APPOINT_DATE_SUCCESS = 7;
    private static final int WRITE_SELF_INTRODUCTION_SUCCESS = 8;
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    MaterialCalendarView widget;
    CalendarDay today;
    private Thread threadCity;
    private int type;
    private int gid;
    private int aid;
    private TextView leftBack;
    private SubGroupActivity.Talent talent;
    private Dialog mDialog;
    private Context mContext;
    private String uri;
    private int tid;
    private int mPrice = 0;
    private int mEid = 0;
    private boolean isSaved = false;
    private boolean isItemsSaved = false;
    private boolean isPriceSaved = false;
    private boolean isTimeSaved = false;
    private boolean isGroupCountSaved = false;
    private boolean isAddressSaved = false;
    private boolean isSelfIntroductionSaved = false;
    private boolean isPackageModified = false;
    private boolean hasPackage = false;
    private EditText introductionET;
    private EditText selfIntroductionET;
    private EditText headLineET;
    private EditText groupCountET;
    private RecyclerView recyclerView;
    private GridImageAdapter adapter;
    private GridLayout mExperienceItemGL;
    private CommonDialogFragmentInterface commonDialogFragmentInterface;
    private LinearLayout authenticateWrapper;
    private ConstraintLayout navigation;
    private TextView addExperienceItem;
    private Button prevBtn;
    private Button nextBtn;
    private int index = 1;
    
    private JSONObject authenObject;
    private List<LocalMedia> selectList = new ArrayList<>();
    private List<File> selectFileList = new ArrayList<>();
    private List<GuideApplyDialogFragment.Route> routeList = new ArrayList<>();
    private Map<String, String> additionalServices = new HashMap<String, String>();
    private List<String> selectedDateList = new ArrayList<>();
    private AddDynamicsActivity addDynamicsActivity;
    private Button selectCityBtn;
    private EditText consultationChargeNumber;
    private EditText consultationChargeDesc;
    private EditText mChargeAmount;
        private Button mPackageSettingBtn;
    private EditText addressET;
    private EditText durationET;
    private EditText limitationET;
    private String consultationUnit;
    private String escortUnit = "天";
    
     private String limitations;
    private RadioGroup sexSelect;
    private boolean isModified = false;
    private AppCompatCheckBox understandCancellation;
    private Window window;
    private int maxSelectNum = 6;
    private int pictureSelectType = 0;//default 0 for materia
    private boolean isCityPicked = false;
    private Thread threadIndustry = null;
    private boolean isLocated = false;
    private Typeface font;
    
    private ArrayList<CommonBean> provinceItems = new ArrayList<>();
    private ArrayList<ArrayList<String>> cityItems = new ArrayList<>();
    private EditText additionalServiceET;
    private MyHandler myHandler;
    private int developConsultation = -1;
    private String mBaseInfoString = "";
    private String mChargeAndLimitString = "";
    private List<String> mSelectedDateList = new ArrayList<>();
    private List<String> mItemList = new ArrayList<>();
    
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
        myHandler = new MyHandler(this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            type = bundle.getInt("type", 0);
        }

        authenObject = new JSONObject();

        mDialog.setContentView(R.layout.develop_experience);

        initView();
        
        mDialog.setCanceledOnTouchOutside(true);
        window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        leftBack = mDialog.findViewById(R.id.left_back);
        leftBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        
         font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.charge_duration_wrapper), font);

        if (addDynamicsActivity == null) {
            addDynamicsActivity = new AddDynamicsActivity();
        }

        return mDialog;
    }
    
    private void initView() {
        authenticateWrapper = mDialog.findViewById(R.id.experience_apply_form_wrapper);
        selectCityBtn = mDialog.findViewById(R.id.select_city);
        headLineET = mDialog.findViewById(R.id.create_headline_edit);
        introductionET = mDialog.findViewById(R.id.introduction_edittext);
        selfIntroductionET = mDialog.findViewById(R.id.self_introduction);
        
        mExperienceItemGL = mDialog.findViewById(R.id.contain_items_gridlayout);
        addExperienceItem = mDialog.findViewById(R.id.add_new_item);
        mChargeAmount = mDialog.findViewById(R.id.price_setting_edit);
                mPackageSettingBtn = mDialog.findViewById(R.id.package_setting_btn);
        durationET = mDialog.findViewById(R.id.duration_edit);
        groupCountET = mDialog.findViewById(R.id.group_count_limit_edit);
        limitationET = mDialog.findViewById(R.id.condition_edit);
        addressET = mDialog.findViewById(R.id.address_edit);
        understandCancellation = mDialog.findViewById(R.id.understand_cancellation);
        prevBtn = mDialog.findViewById(R.id.prevBtn);
        nextBtn = mDialog.findViewById(R.id.nextBtn);
        
        mPackageSettingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if (mEid > 0){
                    startPackageSettingDF();
                //}
            }
        });
        
        selectCity();
        initPictureSelectWidget();
        initExperienceItem();
        initCalendarView();
        navigationProcess();
    }
    
    private void startPackageSettingDF(){
        PackageSettingDF packageSettingDF = PackageSettingDF.newInstance(mEid, type, mPrice==0 ? false:true);
        packageSettingDF.setTargetFragment(this, PACKAGE_REQUEST_CODE);
        packageSettingDF.show(getFragmentManager(), "PackageSettingDF");
    }
    
     private void navigationProcess() {
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (index > 0) {
                    nextBtn.setText(getContext().getResources().getString(R.string.next));
                    index--;
                    if (index == 1) {
                        prevBtn.setVisibility(View.GONE);
                        leftBack.setVisibility(View.VISIBLE);
                    }
                    authenticateWrapper.getChildAt(index - 1).setVisibility(View.VISIBLE);
                    authenticateWrapper.getChildAt(index).setVisibility(View.GONE);
                }
            }
        });
        
         nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validCheck(index)) {
                    switch (index) {
                        case 3:
                            if (TextUtils.isEmpty(mBaseInfoString)) {
                                submitBaseInfo(false);
                            } else {
                                if (!mBaseInfoString.equals(getBaseInfoJsonObject().toString())) {
                                    submitBaseInfo(true);
                                } else {
                                    processNextBtn();
                                }
                            }
                            break;
                        case 4:
                            if (!isSaved && selectList.size() > 0){
                                saveExperiencePictures();
                            }else {
                                processNextBtn();
                            }
                            break;
                       case 5:
                            String itemString = "";
                            if(mExperienceItemGL.getRowCount() > 0){
                                for (int i=0; i<mExperienceItemGL.getRowCount(); i++){
                                    EditText editText = (EditText) mExperienceItemGL.getChildAt(i);
                                    if (!TextUtils.isEmpty(editText.getText())){
                                        itemString += editText.getText().toString();
                                        if (i < mExperienceItemGL.getRowCount() - 1){
                                            itemString += ";";
                                        }
                                    }
                                }
                            }
                            
                            if (!"".equals(itemString) && !isItemsSaved){
                                saveItemsInfo(itemString, false);
                            }else {
                                processNextBtn();
                            }

                            break;
                        case 6:
                            if (!isPriceSaved) {
                                submitPrice(false);
                            } else {
                                if (hasPackage){
                                    if(isPackageModified){
                                        submitPrice(true);
                                        isPackageModified = false;
                                    }else {
                                        processNextBtn();
                                    }
                                }else {
                                    if (mPrice != Integer.parseInt(mChargeAmount.getText().toString())){
                                        submitPrice(true);
                                    }else {
                                        processNextBtn();
                                    }
                                }
                            }
                            break;
                       case 7:
                            if (!isTimeSaved && !TextUtils.isEmpty(durationET.getText().toString())) {
                                submitTime(false);
                            } else {
                                processNextBtn();
                            }
                            break;
                        case 8://number of people
                            if (!isGroupCountSaved && !TextUtils.isEmpty(groupCountET.getText())) {
                                submitLimitation(false);
                            } else {
                                processNextBtn();
                            }
                            break;
                       case 9://number of people
                            if (!isAddressSaved && !TextUtils.isEmpty(addressET.getText())) {
                                submitAddress(false);
                            } else {
                                processNextBtn();
                            }
                            break;
                            case 10:
                            if (mSelectedDateList.size() == 0) {
                                submitAppointDate(false);
                            } else {
                                if (!mSelectedDateList.equals(selectedDateList)) {
                                    submitAppointDate(true);
                                } else {
                                    processNextBtn();
                                }
                            }
                            break;
                            case 11:
                            if(!isSelfIntroductionSaved && !TextUtils.isEmpty(selfIntroductionET.getText())){
                                submitSelfIntroduction(false);
                            }else {
                                processNextBtn();
                            }
                            break;
                        default:
                            processNextBtn();
                            break;
                    }
                }
            }
        });
    }
    
    private void processNextBtn() {
        if (leftBack.getVisibility() == View.VISIBLE) {
            leftBack.setVisibility(View.GONE);
        }
        if (prevBtn.getVisibility() == View.GONE) {
            prevBtn.setVisibility(View.VISIBLE);
        }
        if (index < authenticateWrapper.getChildCount() - 1) {
            if (index == authenticateWrapper.getChildCount() - 2) {
                nextBtn.setText(getContext().getResources().getString(R.string.done));
            }
            authenticateWrapper.getChildAt(index).setVisibility(View.VISIBLE);
            authenticateWrapper.getChildAt(index - 1).setVisibility(View.GONE);
            index++;
        } else {
            submitNotice();
        }
    }
    
        private void saveExperiencePictures(){
        Map<String, String> authenMap = new HashMap<>();

        if (isModified) {
            //authenMap.put("rid", String.valueOf(route.getRid()));
        } else {
            authenMap.put("eid", String.valueOf(mEid));
        }

        if (selectList.size() > 0) {
            Slog.d(TAG, "----------------->saveExperiencePictures selectList: " + selectList);
            for (LocalMedia media : selectList) {
                selectFileList.add(new File(media.getCompressPath()));
            }
            uploadPictures(authenMap, "authen", selectFileList, isModified);
        }

    }
    
    private void uploadPictures(Map<String, String> params, String picKey, List<File> files, boolean isModified) {
        Slog.d(TAG, "--------------------->uploadPictures file size: " + files.size());
        showProgressDialog("正在保存");
        String uri = SAVE_EXPERIENCE_PICTURES_URL;

        if (isModified) {
            uri = MODIFY_EXPERIENCE_PICTURES_URL;
        }
        
        HttpUtil.uploadPictureHttpRequest(getContext(), params, picKey, files, uri, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    try {
                        String responseText = response.body().string();
                        Slog.d(TAG, "---------------->uploadPictures response: " + responseText);
                        int result = new JSONObject(responseText).optInt("result");
                        
                        if (result == 1) {
                            dismissProgressDialog();
                            isSaved = true;
                            //selectList.clear();
                           // selectFileList.clear();
                            //PictureFileUtils.deleteAllCacheDirFile(MyApplication.getContext());
                            myHandler.sendEmptyMessage(SAVE_PICTURES_SUCCESS);
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
    
    
    
    private void initExperienceItem() {
        addExperienceItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addExperienceItem();
            }
        });
    }

    private void addExperienceItem() {
        View view = LayoutInflater.from(MyApplication.getContext())
                .inflate(R.layout.experience_contain_item_edit, (ViewGroup) mDialog.findViewById(android.R.id.content), false);
        mExperienceItemGL.addView(view);
    }
    
    private void selectCity() {
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
        selectCityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCityPickerView();
            }
        });
    }
    
    private void initPictureSelectWidget() {
        recyclerView = mDialog.findViewById(R.id.add_experience_pictures);
        FullyGridLayoutManager manager = new FullyGridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);

        adapter = new GridImageAdapter(getContext(), onAddPicClickListener);
        adapter.setList(selectList);
        adapter.setSelectMax(9);
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
                            PictureSelector.create(DevelopExperienceDialogFragment.this)
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
            }
        });
    }
    
    private void initCalendarView() {

        today = CalendarDay.today();
        widget = mDialog.findViewById(R.id.calendarView);
        widget.setOnDateChangedListener(this);
        //widget.setCurrentDate(today);
        //widget.setSelectedDate(today);
        widget.setTitleFormatter(new MonthArrayTitleFormatter(getResources().getTextArray(R.array.custom_months)));
        widget.setWeekDayFormatter(new ArrayWeekDayFormatter(getResources().getTextArray(R.array.custom_weekdays)));
        widget.addDecorator(new DisabledDecorator());

        final LocalDate min = LocalDate.of(today.getYear(), today.getMonth(), today.getDay());
        //final LocalDate max = LocalDate.of(today.getYear(), today.getMonth()+3, today.getDay());

        widget.state().edit()
                .setMinimumDate(min)
                //.setMaximumDate(max)
                .commit();
    }
    
     @Override
    public void onDateSelected(
            @NonNull MaterialCalendarView widget,
            @NonNull CalendarDay date,
            boolean selected) {

        String dataStr;
        dataStr = FORMATTER.format(date.getDate());
        Slog.d(TAG, "----------------------------------->selected: " + selected + "   data: " + dataStr);
        if (selected) {
            selectedDateList.add(dataStr);
        } else {
            selectedDateList.remove(dataStr);
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
        pvOptions = new OptionsPickerBuilder(getContext(), new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                //返回的分别是二个级别的选中位置
                isCityPicked = true;
                String city = cityItems.get(options1).get(option2);
                selectCityBtn.setText(city);
            }
        }).setDecorView(window.getDecorView().findViewById(R.id.develop_experience))
                .isDialog(true).setLineSpacingMultiplier(1.2f).isCenterLabel(false).build();
        pvOptions.getDialog().getWindow().setGravity(Gravity.CENTER);
        pvOptions.setPicker(provinceItems, cityItems);
        pvOptions.show();
    }
    
    private void submitNotice() {
        final AlertDialog.Builder normalDialogBuilder =
                new AlertDialog.Builder(getContext());
        normalDialogBuilder.setTitle(getResources().getString(R.string.experience_talent_apply));
        normalDialogBuilder.setMessage(getResources().getString(R.string.talent_apply_content));
        normalDialogBuilder.setPositiveButton(getContext().getResources().getString(R.string.submit),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDialog.dismiss();
                        startExperienceDetailActivity();
                    }
                });

        AlertDialog normalDialog = normalDialogBuilder.create();
        normalDialog.show();
    }
    
    private void submitBaseInfo(boolean modified) {
        Slog.d(TAG, "------------------>submitBaseInfo modified: " + modified);
        mBaseInfoString = getBaseInfoJsonObject().toString();
        showProgressDialog(getContext().getString(R.string.saving_progress));
        FormBody.Builder builder;
        String uri = "";
        if (modified) {
            builder = new FormBody.Builder()
                    .add("tid", String.valueOf(tid))
                    .add("base_info", mBaseInfoString);
            uri = MODIFY_BASE_INFO_URL;
        } else {
            builder = new FormBody.Builder()
                    .add("base_info", mBaseInfoString);
            uri = SUBMIT_BASE_INFO_URL;
        }
        
        RequestBody requestBody = builder.build();

        HttpUtil.sendOkHttpRequest(mContext, uri, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "submitBaseInfo response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        if (!modified) {
                           mEid = new JSONObject(responseText).optInt("eid");
                        }
                        dismissProgressDialog();
                        myHandler.sendEmptyMessage(WRITE_BASE_INFO_SUCCESS);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void saveItemsInfo(String itemString, boolean modified) {
        Slog.d(TAG, "------------------>saveItemsInfo modified: " + modified);
        showProgressDialog(getContext().getString(R.string.saving_progress));
        FormBody.Builder builder;
        String uri = "";
        if (modified) {
            builder = new FormBody.Builder()
                    .add("tid", String.valueOf(tid))
                    .add("item_string", itemString);
            uri = MODIFY_BASE_INFO_URL;
        } else {
            builder = new FormBody.Builder()
                    .add("eid", String.valueOf(mEid))
                    .add("item_string", itemString);
            uri = SAVE_ITEMS_URL;
        }
        
        RequestBody requestBody = builder.build();

        HttpUtil.sendOkHttpRequest(mContext, uri, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "submitBaseInfo response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        int result = new JSONObject(responseText).optInt("result");
                        if (result == 1){
                            isItemsSaved = true;
                            dismissProgressDialog();
                            myHandler.sendEmptyMessage(SAVE_ITEMS_SUCCESS);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
    private void submitPrice(boolean isModify) {
        int price = 0;
        showProgressDialog(getContext().getString(R.string.saving_progress));
        if (hasPackage){
            price = mPrice;
        }else {
            if (!TextUtils.isEmpty(mChargeAmount.getText().toString())){
                price = Integer.parseInt(mChargeAmount.getText().toString());
                mPrice = price;
            }
        }
        FormBody.Builder builder = new FormBody.Builder()
                .add("eid", String.valueOf(mEid))
                .add("price", String.valueOf(price));
        String uri = SUBMIT_CHARGE_URL;
        if (isModify) {
            uri = MODIFY_CHARGE_URL;
        }
        
        RequestBody requestBody = builder.build();
        HttpUtil.sendOkHttpRequest(mContext, uri, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "submitPrice response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        int result = new JSONObject(responseText).optInt("result");

                        if (result == 1) {
                            dismissProgressDialog();
                            isPriceSaved = true;
                            myHandler.sendEmptyMessage(WRITE_CHARGE_SUCCESS);
                        }
                        
                        } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
    private void submitTime(boolean isModify) {
        showProgressDialog(getContext().getString(R.string.saving_progress));
        FormBody.Builder builder = new FormBody.Builder()
                .add("eid", String.valueOf(mEid))
                .add("duration", durationET.getText().toString());
        String uri = SUBMIT_TIME_URL;
        if (isModify) {
            uri = MODIFY_TIME_URL;
        }

        RequestBody requestBody = builder.build();
        HttpUtil.sendOkHttpRequest(mContext, uri, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "submitTime response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        int result = new JSONObject(responseText).optInt("result");

                        if (result == 1) {
                            isTimeSaved = true;
                            dismissProgressDialog();
                            myHandler.sendEmptyMessage(WRITE_TIME_SUCCESS);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void submitLimitation(boolean isModify) {
        showProgressDialog(getContext().getString(R.string.saving_progress));
        FormBody.Builder builder = new FormBody.Builder()
                .add("eid", String.valueOf(mEid))
                .add("amount", groupCountET.getText().toString());
        String uri = SUBMIT_LIMITATION_URL;
        if (isModify) {
            uri = MODIFY_LIMITATION_URL;
        }
        
        RequestBody requestBody = builder.build();
        HttpUtil.sendOkHttpRequest(mContext, uri, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "submitLimitation response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        int result = new JSONObject(responseText).optInt("result");
                        if (result == 1) {
                            isGroupCountSaved = true;
                            dismissProgressDialog();
                            myHandler.sendEmptyMessage(WRITE_TIME_SUCCESS);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
    private void submitAddress(boolean isModify) {
        showProgressDialog(getContext().getString(R.string.saving_progress));
        FormBody.Builder builder = new FormBody.Builder()
                .add("eid", String.valueOf(mEid))
                .add("address", addressET.getText().toString());
        String uri = SUBMIT_ADDRESS_URL;
        if (isModify) {
            uri = MODIFY_ADDRESS_URL;
        }
        
        RequestBody requestBody = builder.build();
        HttpUtil.sendOkHttpRequest(mContext, uri, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "submitAddress response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        int result = new JSONObject(responseText).optInt("result");
                        
                        if (result == 1) {
                            isGroupCountSaved = true;
                            dismissProgressDialog();
                            myHandler.sendEmptyMessage(WRITE_TIME_SUCCESS);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
    
    private void submitAppointDate(boolean isModify) {
        showProgressDialog(getContext().getString(R.string.saving_progress));
        String dateString = "";

        for (int i = 0; i < selectedDateList.size(); i++) {
            if (i == selectedDateList.size() - 1) {
                dateString += selectedDateList.get(i);
            } else {
                dateString += selectedDateList.get(i) + ";";
            }
        }
        
        mSelectedDateList = new ArrayList<>(selectedDateList);

        FormBody.Builder builder = new FormBody.Builder()
                .add("eid", String.valueOf(mEid))
                .add("type", String.valueOf(Utility.TalentType.EXPERIENCE.ordinal()))
                .add("date_string", dateString);

        RequestBody requestBody = builder.build();

        String uri = SUBMIT_APPOINTMENT_DATE_URL;
        if (isModify) {
            uri = MODIFY_APPOINTMENT_DATE_URL;
        }
        
        HttpUtil.sendOkHttpRequest(mContext, uri, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "submitAppointDate response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        int result = new JSONObject(responseText).optInt("result");
                        if (result == 1) {
                            dismissProgressDialog();
                            myHandler.sendEmptyMessage(WRITE_APPOINT_DATE_SUCCESS);
                        }
                        } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
    private void submitSelfIntroduction(boolean isModify) {
        showProgressDialog(getContext().getString(R.string.saving_progress));
        FormBody.Builder builder = new FormBody.Builder()
                .add("eid", String.valueOf(mEid))
                .add("introduction", selfIntroductionET.getText().toString());
        String uri = SUBMIT_SELF_INTRODUCTION_URL;
        if (isModify) {
            uri = MODIFY_SELF_INTRODUCTION_URL;
        }

        RequestBody requestBody = builder.build();
        
        HttpUtil.sendOkHttpRequest(mContext, uri, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "submitSelfIntroduction response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        int result = new JSONObject(responseText).optInt("result");
                        if (result == 1) {
                            isSelfIntroductionSaved = true;
                            dismissProgressDialog();
                            myHandler.sendEmptyMessage(WRITE_SELF_INTRODUCTION_SUCCESS);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }


private boolean validCheck(int index) {
        Slog.d(TAG, "------------------------>validCheck: " + index);
        boolean valid = false;
        switch (index) {
            case 1:
                valid = isCityPicked;
                if (!isCityPicked) {
                    Toast.makeText(getContext(), getResources().getString(R.string.city_select_notice), Toast.LENGTH_LONG).show();
                }
                break;
                case 2:
                if (!TextUtils.isEmpty(headLineET.getText().toString())) {
                    valid = true;
                } else {
                    valid = false;
                    Toast.makeText(getContext(), getResources().getString(R.string.headline_empty_notice), Toast.LENGTH_LONG).show();
                }
                break;
            case 3:
                if (!TextUtils.isEmpty(introductionET.getText().toString())) {
                    valid = true;
                } else {
                    valid = false;
                    Toast.makeText(getContext(), getResources().getString(R.string.service_introduction_notice), Toast.LENGTH_LONG).show();
                }
                break;
            case 4://experience pictures
                if (selectList.size() == 0){
                    valid = false;
                    Toast.makeText(getContext(), getResources().getString(R.string.experience_picture_notice), Toast.LENGTH_LONG).show();
                }else {
                    valid = true;
                }
                break;
            case 6:
                if (!TextUtils.isEmpty(mChargeAmount.getText()) || mPrice != 0) {
                    valid = true;
                } else {
                    valid = false;
                    Toast.makeText(getContext(), getResources().getString(R.string.charge_empty_notice), Toast.LENGTH_LONG).show();
                    break;
                }
                break;
                case 7:
                if (!TextUtils.isEmpty(durationET.getText())){
                    valid = true;
                }else {
                    valid = false;
                    Toast.makeText(getContext(), getResources().getString(R.string.duration_empty_notice), Toast.LENGTH_LONG).show();
                }
                break;
                case 8:
                if (!TextUtils.isEmpty(groupCountET.getText())) {
                    valid = true;
                } else {
                    valid = false;
                    Toast.makeText(getContext(), getResources().getString(R.string.group_amount_empty_notice), Toast.LENGTH_LONG).show();
                }

                break;
            case 9:
                if (!TextUtils.isEmpty(addressET.getText())) {
                    valid = true;
                } else {
                    valid = false;
                    Toast.makeText(getContext(), getResources().getString(R.string.address_empty_notice), Toast.LENGTH_LONG).show();
                }

                break;
            case 10:
                if (selectedDateList.size() > 0) {
                    valid = true;
                } else {
                    valid = false;
                    Toast.makeText(getContext(), getResources().getString(R.string.select_date_empty_notice), Toast.LENGTH_LONG).show();
                }
                break;
            case 11://self introduction
                if (!TextUtils.isEmpty(selfIntroductionET.getText().toString())) {
                    valid = true;
                } else {
                    valid = false;
                    Toast.makeText(getContext(), getResources().getString(R.string.self_introduction_empty_notice), Toast.LENGTH_LONG).show();
                }
                break;
            case 12:
                if (understandCancellation.isChecked()) {
                    valid = true;
                } else {
                    valid = false;
                    Toast.makeText(getContext(), getResources().getString(R.string.understand_cancellation_notice), Toast.LENGTH_LONG).show();
                }
                break;
            default:
                valid = true;
                break;
                }

        return valid;
    }
    
     private JSONObject getBaseInfoJsonObject() {
        String additionalServiceStr = "";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("city", selectCityBtn.getText().toString());
            jsonObject.put("title", headLineET.getText().toString());
            jsonObject.put("introduction", introductionET.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
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
                    break;
                case PACKAGE_REQUEST_CODE:
                    hasPackage = true;
                    mPrice = data.getIntExtra("price", 0);
                    isPackageModified = data.getBooleanExtra("isPackageModified", false);
                    mPackageSettingBtn.setText(getContext().getResources().getString(R.string.examine_package_setting));
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
    
    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
    }

    public void startExperienceDetailActivity() {
        Intent intent = new Intent(getContext(), ExperienceDetailActivity.class);
        intent.putExtra("eid", mEid);
        startActivity(intent);
    }

    private void sendTalentAddedBroadcast() {
        Intent intent = new Intent(GUIDE_ADD_BROADCAST);
        intent.putExtra("tid", tid);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }
    
     @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case WRITE_BASE_INFO_SUCCESS:
                processNextBtn();
                break;
            case SAVE_PICTURES_SUCCESS:
                processNextBtn();
                break;
            case SAVE_ITEMS_SUCCESS:
                processNextBtn();
                break;
            case WRITE_CHARGE_SUCCESS:
                processNextBtn();
                break;
            case WRITE_TIME_SUCCESS:
                processNextBtn();
                break;
            case WRITE_APPOINT_DATE_SUCCESS:
                processNextBtn();
                break;
            case WRITE_SELF_INTRODUCTION_SUCCESS:
                processNextBtn();
                break;
            default:
                break;
        }
    }
    
    private static class DisabledDecorator implements DayViewDecorator {
        @Override
        public boolean shouldDecorate(final CalendarDay day) {
            return false;
        }

        @Override
        public void decorate(final DayViewFacade view) {
            view.setDaysDisabled(true);
        }
    }
    
    private GridImageAdapter.onAddPicClickListener onAddPicClickListener = new GridImageAdapter.onAddPicClickListener() {
        @Override
        public void onAddPicClick() {
            //boolean mode = cb_mode.isChecked();
            boolean mode = true;
            if (mode) {
                PictureSelector.create(DevelopExperienceDialogFragment.this)
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


    static class MyHandler extends Handler {
        WeakReference<DevelopExperienceDialogFragment> travelGuideAuthenticationDialogFragmentWeakReference;

        MyHandler(DevelopExperienceDialogFragment guideApplyDialogFragment) {
            travelGuideAuthenticationDialogFragmentWeakReference = new WeakReference<DevelopExperienceDialogFragment>(guideApplyDialogFragment);
        }
        
        @Override
        public void handleMessage(Message message) {
            DevelopExperienceDialogFragment guideApplyDialogFragment = travelGuideAuthenticationDialogFragmentWeakReference.get();
            if (guideApplyDialogFragment != null) {
                guideApplyDialogFragment.handleMessage(message);
            }
        }
    }

}
