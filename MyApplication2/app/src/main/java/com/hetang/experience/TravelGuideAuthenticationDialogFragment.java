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
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter;
import com.prolificinteractive.materialcalendarview.format.MonthArrayTitleFormatter;

import org.angmarch.views.NiceSpinner;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;
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
import static com.hetang.experience.RouteItemEditDF.newInstance;
import static com.hetang.group.GroupFragment.eden_group;
import static com.hetang.group.SubGroupActivity.TALENT_ADD_BROADCAST;
import static com.hetang.group.SubGroupActivity.getTalent;
import static com.hetang.meet.MeetDynamicsFragment.REQUEST_CODE;

public class TravelGuideAuthenticationDialogFragment extends BaseDialogFragment implements CompoundButton.OnCheckedChangeListener, OnDateSelectedListener {
    public final static int TALENT_AUTHENTICATION_RESULT_OK = 0;
    public final static int ROUTE_REQUEST_CODE = 1;
    private static final boolean isDebug = true;
    private static final String TAG = "TravelGuideAuthenticationDialogFragment";
    private static final String SUBMIT_URL = HttpUtil.DOMAIN + "?q=talent/become/apply";
    private static final int MSG_LOAD_SUCCESS = 0x0003;
    private Thread threadCity;
    private int type;
    private int gid;
    private int aid;
    private TextView leftBack;
    private SubGroupActivity.Talent talent;
    private Dialog mDialog;
    private Context mContext;
    private String uri;
    private boolean isSubmit = false;
    private EditText introductionET;
    private EditText selfIntroductionET;
    private EditText headLineET;
    private GridLayout mRouteItemGL;
    private CommonDialogFragmentInterface commonDialogFragmentInterface;
    private LinearLayout authenticateWrapper;
    private ConstraintLayout navigation;
    private TextView addRouteItem;
    private Button prevBtn;
    private Button nextBtn;
    private int index = 1;
    private JSONObject authenObject;
    
    private List<LocalMedia> selectList = new ArrayList<>();
    private List<File> selectFileList = new ArrayList<>();
    private List<Route> routeList = new ArrayList<>();
    private Map<String, String> additionalServices = new HashMap<String, String>();
    private List<String> selectedDateList = new ArrayList<>();
    private AddDynamicsActivity addDynamicsActivity;
    private Button selectCityBtn;
    private EditText consultationChargeNumber;
    private EditText consultationChargeDesc;
    private EditText escortChargeSettingNumber;
    private EditText escortChargeDesc;
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
    AppCompatCheckBox followShot;
    AppCompatCheckBox travelPlan;
    AppCompatCheckBox charteredCar;
    AppCompatCheckBox ticket;
    AppCompatCheckBox ferry;
    private EditText additionalServiceET;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("EEE, d MMM yyyy");

    //@BindView(R.id.calendarView)
    MaterialCalendarView widget;
    CalendarDay today;
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

        initView();

        initCalendarView();
        
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
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.add_travel_guide_items), font);
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.charge_wrapper), font);

        if (addDynamicsActivity == null) {
            addDynamicsActivity = new AddDynamicsActivity();
        }

        return mDialog;
    }
    
    private void initView() {
        selectCityBtn = mDialog.findViewById(R.id.select_city);
        authenticateWrapper = mDialog.findViewById(R.id.travel_guide_wrapper);
        introductionET = mDialog.findViewById(R.id.introduction_edittext);
        selfIntroductionET = mDialog.findViewById(R.id.self_introduction);
        headLineET = mDialog.findViewById(R.id.create_headline_edit);
        mRouteItemGL = mDialog.findViewById(R.id.add_travel_guide_items);
        addRouteItem = mDialog.findViewById(R.id.add_route_item);
        understandCancellation = mDialog.findViewById(R.id.understand_cancellation);
        prevBtn = mDialog.findViewById(R.id.prevBtn);
        nextBtn = mDialog.findViewById(R.id.nextBtn);

        selectCity();
        initRouteItem();
        setAdditionalServices();
        initChargeSetting();
        navigationProcess();

    }
    
    private void navigationProcess(){
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
                    Slog.d(TAG, "-------------------->index: " + index);
                }
            }
        });
        
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validCheck(index)) {
                    if (leftBack.getVisibility() == View.VISIBLE){
                        leftBack.setVisibility(View.GONE);
                    }
                    if (prevBtn.getVisibility() == View.GONE) {
                        prevBtn.setVisibility(View.VISIBLE);
                    }
                    if (index < authenticateWrapper.getChildCount() - 1) {
                        if (index == authenticateWrapper.getChildCount() - 2) {
                            nextBtn.setText(getContext().getResources().getString(R.string.done));
                        }
                        Slog.d(TAG, "---------------------current index: "+index);
                        authenticateWrapper.getChildAt(index).setVisibility(View.VISIBLE);
                        authenticateWrapper.getChildAt(index - 1).setVisibility(View.GONE);

                        index++;

                        processCurrent(index);

                    } else {
                        submitNotice();
                    }
                }
            }
        });
    }
    
    private void addRouteItem(){
        View view = LayoutInflater.from(MyApplication.getContext())
                .inflate(R.layout.route_item_index, (ViewGroup) mDialog.findViewById(android.R.id.content), false);
        mRouteItemGL.addView(view);
    }

    private void initRouteItem(){
        addRouteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRouteItem();
                initRouteItem();
            }
        });
        
        int size = mRouteItemGL.getChildCount();
        Slog.d(TAG, "--------------------->size: "+size);
        for (int i=0; i<size; i++){
            final int index = i;
            mRouteItemGL.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startRouteItemEditDF(index);
                }
            });
        }

        FontManager.markAsIconContainer(mDialog.findViewById(R.id.add_travel_guide_items), font);
    }
    
    private void selectCity(){
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

    
    private void setAdditionalServices(){
        followShot = mDialog.findViewById(R.id.follow_shot);
        travelPlan = mDialog.findViewById(R.id.travel_plan);
        charteredCar = mDialog.findViewById(R.id.chartered_car);
        ticket = mDialog.findViewById(R.id.ticket);
        ferry = mDialog.findViewById(R.id.ferry);
        additionalServiceET = mDialog.findViewById(R.id.additional_service_edit);

        followShot.setOnCheckedChangeListener(this);
        travelPlan.setOnCheckedChangeListener(this);
        charteredCar.setOnCheckedChangeListener(this);
        ticket.setOnCheckedChangeListener(this);
        ferry.setOnCheckedChangeListener(this);

    }
    
    @Override
    public void onCheckedChanged(CompoundButton checkBox, boolean checked) {
        Slog.d(TAG, "--------------checkbox text: "+checkBox.getText().toString());
        switch (checkBox.getId()){
            case R.id.follow_shot:
                if (checked){
                    additionalServices.put("1", checkBox.getText().toString());
                }else {
                    additionalServices.remove("1");
                }
                break;
            case R.id.travel_plan:
                if (checked){
                    additionalServices.put("2", checkBox.getText().toString());
                }else {
                    additionalServices.remove("2");
                }
                break;
                case R.id.chartered_car:
                if (checked){
                    additionalServices.put("3", checkBox.getText().toString());
                }else {
                    additionalServices.remove("3");
                }
                break;
            case R.id.ticket:
                if (checked){
                    additionalServices.put("4", checkBox.getText().toString());
                }else {
                    additionalServices.remove("4");
                }
                break;
                case R.id.ferry:
                if (checked){
                    additionalServices.put("5", checkBox.getText().toString());
                }else {
                    additionalServices.remove("5");
                }
                break;
                default:
                    break;
        }
    }
    
    private void initChargeSetting(){
        Button consultationServiceBtn = mDialog.findViewById(R.id.consultation_service);
        Button escortServiceBtn = mDialog.findViewById(R.id.escort_service);
        RelativeLayout consultationServiceSetting = mDialog.findViewById(R.id.consultation_charge_setting);
        RelativeLayout escortServiceSetting = mDialog.findViewById(R.id.escort_charge_setting);
        consultationChargeNumber = mDialog.findViewById(R.id.consultation_charge_setting_edit);
        consultationChargeDesc = mDialog.findViewById(R.id.consultation_charge_supplement_edit);
        escortChargeSettingNumber = mDialog.findViewById(R.id.charge_setting_edit);
        escortChargeDesc = mDialog.findViewById(R.id.charge_supplement_edit);

        consultationServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                consultationServiceBtn.setBackground(getContext().getResources().getDrawable(R.drawable.btn_primary));
                consultationServiceBtn.setTextColor(getContext().getResources().getColor(R.color.white));
                consultationServiceSetting.setVisibility(View.VISIBLE);
            }
        });

        escortServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                escortServiceBtn.setBackground(getContext().getResources().getDrawable(R.drawable.btn_primary));
                escortServiceBtn.setTextColor(getContext().getResources().getColor(R.color.white));
                escortServiceSetting.setVisibility(View.VISIBLE);
            }
        });
        
        String[] units = getResources().getStringArray(R.array.duration);
        NiceSpinner consultationUnitSpinner = (NiceSpinner) mDialog.findViewById(R.id.consultation_unit);
        NiceSpinner escortUnitSpinner = (NiceSpinner) mDialog.findViewById(R.id.escort_unit);
        final List<String> unitList = new LinkedList<>(Arrays.asList(units));
        consultationUnitSpinner.attachDataSource(unitList);
        escortUnitSpinner.attachDataSource(unitList);
        
        consultationUnitSpinner.addOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }

        });

        escortUnitSpinner.addOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }

        });
    }
    
    private void initCalendarView() {
        //widget = mDialog.findViewById(R.id.calendarView);
        //unbinder = ButterKnife.bind(getActivity());

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
        Slog.d(TAG, "----------------------------------->selected: "+selected+"   data: "+dataStr);
        if (selected){
            selectedDateList.add(dataStr);
        }else {
            selectedDateList.remove(dataStr);
        }
    }
    
    private void startRouteItemEditDF(int index){
        Slog.d(TAG, "--------------------->index: "+index+" size: "+routeList.size());
        RouteItemEditDF routeItemEditDF;
        if (routeList.size() > index){
            routeItemEditDF = newInstance(index, routeList.get(index));
        }else {
            routeItemEditDF = newInstance(index, null);
        }

        routeItemEditDF.setTargetFragment(this, ROUTE_REQUEST_CODE);
        routeItemEditDF.show(getFragmentManager(), "RouteItemEditDF");
    }
    
    private static class DisabledDecorator implements DayViewDecorator{
        @Override public boolean shouldDecorate(final CalendarDay day) {
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
                isCityPicked = true;
                String city = cityItems.get(options1).get(option2);
                selectCityBtn.setText(city);
            }
        }).setDecorView(window.getDecorView().findViewById(R.id.travel_guide_authentication))
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
                        submit();
                    }
                });

        AlertDialog normalDialog = normalDialogBuilder.create();
        normalDialog.show();
    }
    
    private void submit() {
        Toast.makeText(getContext(), "submit", Toast.LENGTH_LONG).show();
        /*
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

        authenMap.put("type", String.valueOf(type));

        Slog.d(TAG, "--------------------->file size: " + selectFileList.size());

        uploadPictures(authenMap, "authen", selectFileList);
        */
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
                if (!TextUtils.isEmpty(introductionET.getText().toString())){
                    valid = true;
                }else {
                    valid = false;
                    Toast.makeText(getContext(), getResources().getString(R.string.service_introduction_notice), Toast.LENGTH_LONG).show();
                }
                break;
                case 3:
                if (routeList.size() == 0){
                    valid = false;
                    Toast.makeText(getContext(), getResources().getString(R.string.route_introduction_empty_notice), Toast.LENGTH_LONG).show();
                }else {
                    valid = true;
                }
                break;

            case 5:
                if (!TextUtils.isEmpty(selfIntroductionET.getText().toString())){
                    valid = true;
                }else {
                    valid = false;
                    Toast.makeText(getContext(), getResources().getString(R.string.self_introduction_empty_notice), Toast.LENGTH_LONG).show();
                }

                break;
                case 6:
                if (!TextUtils.isEmpty(headLineET.getText().toString())){
                    valid = true;
                }else {
                    valid = false;
                    Toast.makeText(getContext(), getResources().getString(R.string.headline_empty_notice), Toast.LENGTH_LONG).show();
                }

                break;
            case 7:
                if(!TextUtils.isEmpty(consultationChargeNumber.getText()) || !TextUtils.isEmpty(escortChargeSettingNumber.getText())){
                    valid = true;
                }else {
                    valid = false;
                    Toast.makeText(getContext(), getResources().getString(R.string.charge_empty_notice), Toast.LENGTH_LONG).show();
                }
                break;
                case 8:
                if (selectedDateList.size() > 0){
                    valid = true;
                }else {
                    valid = false;
                    Toast.makeText(getContext(), getResources().getString(R.string.select_date_empty_notice), Toast.LENGTH_LONG).show();
                }
                break;
            case 10:
                if (understandCancellation.isChecked()){
                    valid = true;
                }else {
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
    
    private void processCurrent(int index) {
        Slog.d(TAG, "------------------------>processCurrent: " + index);
        switch (index) {
            case 5:
                if (!TextUtils.isEmpty(additionalServiceET.getText())){
                    additionalServices.put("addition", additionalServiceET.getText().toString());
                }
                StringBuilder sb = new StringBuilder();
                for (String key : additionalServices.keySet()) {
                    sb.append(additionalServices.get(key) + "\t");
                }
                Slog.d(TAG, "---------------------------->additionalServices: "+sb);
                break;
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ROUTE_REQUEST_CODE:
                    Bundle bundle = data.getExtras();
                    int index = bundle.getInt("index");
                    Route route = bundle.getParcelable("route");
                    if(bundle.getBoolean("isModify")){
                        routeList.remove(index);
                    }

                    routeList.add(index, route);
                    setRouteName(index, route.name);
                    break;
            }
        }
    }
    
    private void setRouteName(int index, String name){
        ConstraintLayout routeItem = (ConstraintLayout)mRouteItemGL.getChildAt(index);
        TextView routeName = (TextView)routeItem.getChildAt(0);
        routeName.setText(name);
    }

    public static class Route implements Parcelable {
        public String name;
        public String introduction;
        public List<LocalMedia> selectPicture = new ArrayList<>();

        public Route(String name, String introduction, List<LocalMedia> selectPicture) {
            this.name = name;
            this.introduction = introduction;
            this.selectPicture = selectPicture;
        }
         protected Route(Parcel in) {
            name = in.readString();
            introduction = in.readString();
            selectPicture = new ArrayList<>();
            in.readList(selectPicture, getClass().getClassLoader());
        }

        public static final Creator<Route> CREATOR = new Creator<Route>() {
            @Override
            public Route createFromParcel(Parcel in) {
                return new Route(in);
            }

            @Override
            public Route[] newArray(int size) {
                return new Route[size];
            }
        };
        
        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeString(introduction);
            dest.writeList(selectPicture);
        }

        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }

        public String getIntroduction() {
            return introduction;
        }

        public void setIntroduction(String introduction) {
            this.introduction = introduction;
        }

        @Override
        public String toString() {
            return "Route{" +
                    "name='" + name + '\'' +
                    ", introduction=" + introduction +
                    ", selectPicture=" + selectPicture +
                    '}';
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
        /*
        mReceiver = new QRCodeSetBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SUBMIT_TALENT_AUTHENTICATION_ACTION_BROADCAST);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, intentFilter);

         */
    }
    
     //unregister local broadcast
    private void unRegisterBroadcast() {
        //LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
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
