package com.mufu.experience;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.mufu.R;
import com.mufu.common.MyApplication;
import com.mufu.home.CommonContactsActivity;
import com.mufu.order.BlockBookingPlaceOrderDF;
import com.mufu.order.PlaceOrderDF;
import com.mufu.util.BaseDialogFragment;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.Slog;

import com.mufu.util.Utility;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateLongClickListener;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.angmarch.views.NiceSpinner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;
import static com.mufu.experience.DevelopExperienceDialogFragment.FORMATTER;
import static com.mufu.experience.DevelopExperienceDialogFragment.PACKAGE_REQUEST_CODE;
import static com.mufu.experience.PackageSettingDF.GET_PACKAGE_AMOUNT_URL;
import static com.mufu.experience.PackageSettingDF.GET_PACKAGE_DONE;
import static com.mufu.experience.PackageSettingDF.GET_PACKAGE_URL;
import static com.mufu.home.CommonContactsActivity.EXPERIENCE_COMPANION;
import static com.mufu.order.PlaceOrderDF.ORDER_PAYMENT_SUCCESS_BROADCAST;
import static com.mufu.util.DateUtil.timeStampToDay;
import static com.mufu.util.DateUtil.calendarToDate;

public class CheckAppointDate extends BaseDialogFragment implements OnDateSelectedListener, OnDateLongClickListener {
    private static final boolean isDebug = true;
    private static final String TAG = "CheckAppointDate";
    private Dialog mDialog;
    private Window window;
    private static int mType = Utility.TalentType.EXPERIENCE.ordinal();
    private int sid;
    private int eid;
    private int did;
    private int price;
    private int mSoldCount = 0;
    private static int mMaximum = 0;
    private String unit;
    private String title;
    private String dataStr;
    private Button selectBtn;
        private Button mBlockBookingBtn;
        private String mPackageName;
    private int mIdentityRequirement = 0;
    private boolean isPackageSelected = false;
    private boolean hasPackages = false;
    private TextView mPackageNameTV;
    private MyHandler myHandler;
    MaterialCalendarView widget;
    private ConstraintLayout selectWrapper;
    private TextView companionsTV;
    private TextView companionsAmountTV;
    private JSONArray dateJSONArray;
    private static CalendarDay today;
    private OrderStatusBroadcastReceiver mReceiver;
    private static List<LocalDate> availableLocalDateList = new ArrayList<>();
    private static List<AppointDate> appointDateList = new ArrayList<>();
    private static List<AppointDate> bookabledDateList = new ArrayList<>();
    private static final int GET_AVAILABLE_APPOINTMENT_DATE_DONE = 0;
    public static final String GET_AVAILABLE_APPOINTMENT_DATE = HttpUtil.DOMAIN + "?q=travel_guide/get_available_appointment_date";
    public static final String GET_EXPERIENCE_AVAILABLE_APPOINTMENT_DATE = HttpUtil.DOMAIN + "?q=experience/get_experience_available_appointment_date";

    public static CheckAppointDate newInstance(int sid, int price, String unit, String title) {
        mType = Utility.TalentType.GUIDE.ordinal();
        CheckAppointDate checkAppointDate = new CheckAppointDate();
        Bundle bundle = new Bundle();
        bundle.putInt("sid", sid);
        bundle.putInt("price", price);
        bundle.putString("unit", unit);
        bundle.putString("title", title);
        checkAppointDate.setArguments(bundle);

        return checkAppointDate;
    }

    public static CheckAppointDate newInstance(int eid, int price, String title, int maximum, int identityRequirement) {
        mType = Utility.TalentType.EXPERIENCE.ordinal();
        CheckAppointDate checkAppointDate = new CheckAppointDate();
        Bundle bundle = new Bundle();
        bundle.putInt("eid", eid);
        bundle.putInt("price", price);
        bundle.putString("title", title);
        bundle.putInt("maximum", maximum);
        bundle.putInt("identityRequirement", identityRequirement);
        checkAppointDate.setArguments(bundle);

        return checkAppointDate;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_MaterialComponents_DialogWhenLarge);
        mDialog.setContentView(R.layout.check_appoint_date);
        myHandler = new MyHandler(this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            price = bundle.getInt("price");
            title = bundle.getString("title");
            if (mType == Utility.TalentType.EXPERIENCE.ordinal()) {
                eid = bundle.getInt("eid");
                unit = "人起";
            } else {
                sid = bundle.getInt("sid");
                unit = bundle.getString("unit");
            }
            
            mMaximum = bundle.getInt("maximum");
            mIdentityRequirement = bundle.getInt("identityRequirement", 0);
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
                dismiss();
            }
        });
        
        TextView statusbarTitle = mDialog.findViewById(R.id.title);
        statusbarTitle.setText(getContext().getResources().getString(R.string.sliping_to_check));

        getAvailableDate();
        getActivityPackageAmount();

        TextView priceTV = mDialog.findViewById(R.id.price);
        TextView unitTV = mDialog.findViewById(R.id.unit);
        priceTV.setText(String.valueOf(price));
        unitTV.setText(unit);

        selectWrapper = mDialog.findViewById(R.id.select_wrapper);
        selectBtn = mDialog.findViewById(R.id.select);
        mBlockBookingBtn = mDialog.findViewById(R.id.block_booking_selector_btn);
        companionsTV = mDialog.findViewById(R.id.companions);
        companionsAmountTV = mDialog.findViewById(R.id.numberOfcompanions);
        mPackageNameTV = mDialog.findViewById(R.id.package_content);

        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitOrder();
            }
        });
        
        mBlockBookingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startBlockBookingPlaceOrderDF();
            }
        });

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.cny), font);
        
        mReceiver = new OrderStatusBroadcastReceiver();
        registerBroadcast();

        return mDialog;
    }

    private void startPackageSelectorDF(){
        PackageSelectorDF packageSelectorDF = PackageSelectorDF.newInstance(eid, mType);
        packageSelectorDF.setTargetFragment(this, PACKAGE_REQUEST_CODE);
        packageSelectorDF.show(getFragmentManager(), "PackageSelectorDF");
    }
    
    private void startBlockBookingPlaceOrderDF(){
        BlockBookingPlaceOrderDF blockBookingPlaceOrderDF = BlockBookingPlaceOrderDF.newInstance(title, "", did, dataStr, eid, mSoldCount, mMaximum, mType);
        blockBookingPlaceOrderDF.setTargetFragment(this, PACKAGE_REQUEST_CODE);
        blockBookingPlaceOrderDF.show(getFragmentManager(), "BlockBookingPlaceOrderDF");
    }
    
    private void getCompanions() {
        Intent intent = new Intent(getContext(), CommonContactsActivity.class);
        intent.putExtra("did", did);
        intent.putExtra("type", EXPERIENCE_COMPANION);
        startActivity(intent);
    }

    private void submitOrder() {
        PlaceOrderDF placeOrderDF = null;
        if (hasPackages){
            if (isPackageSelected){
                if (mType == Utility.TalentType.EXPERIENCE.ordinal()) {
                    placeOrderDF = PlaceOrderDF.newInstance(title, mPackageName, price, did, dataStr, eid, mSoldCount, mMaximum, mType, mIdentityRequirement);
                } else {
                    placeOrderDF = PlaceOrderDF.newInstance(title, mPackageName, price, did, dataStr, sid, mSoldCount, mMaximum, mType, mIdentityRequirement);
                }
            }else {
                Toast.makeText(getContext(), "请选择套餐", Toast.LENGTH_LONG).show();
                return;
            }
        }else {
             if (mType == Utility.TalentType.EXPERIENCE.ordinal()) {
                placeOrderDF = PlaceOrderDF.newInstance(title, "", price, did, dataStr, eid, mSoldCount, mMaximum, mType, mIdentityRequirement);
            } else {
                placeOrderDF = PlaceOrderDF.newInstance(title, "", price, did, dataStr, sid, mSoldCount, mMaximum, mType, mIdentityRequirement);
            }
        }
        placeOrderDF.show(getFragmentManager(), "PlaceOrderDF");
    }


    private void setAppointDateView() {
        today = CalendarDay.today();
        for (int i = 0; i < appointDateList.size(); i++) {
            //JSONObject dateObject = dateJSONArray.getJSONObject(i);
            LocalDate localDate = appointDateList.get(i).getLocalDate();
            if (localDate.getDayOfMonth() >= today.getDay() || localDate.getMonthValue() > today.getMonth() || localDate.getYear() > today.getYear()) {
                availableLocalDateList.add(localDate);
                bookabledDateList.add(appointDateList.get(i));
            }
        }

        widget = mDialog.findViewById(R.id.calendarView);
        widget.setOnDateChangedListener(this);
        widget.setOnDateLongClickListener(this);
        final LocalDate min = LocalDate.of(today.getYear(), today.getMonth(), today.getDay());
        
        int month = today.getMonth() + 2;
        int year = today.getYear();
        if (month > 12){
            year += 1;
            month = month - 12;
        }

        final LocalDate max = LocalDate.of(year, month, today.getDay());
        widget.addDecorator(new AvailableDecorator());
        widget.addDecorator(new DayDisabledDecorator());
        widget.addDecorator(new DaySoldOutDecorator());
        widget.state().edit()
                .setMinimumDate(min)
                .setMaximumDate(max)
                .commit();
    }

    public static class AvailableDecorator implements DayViewDecorator {
        @Override
        public boolean shouldDecorate(final CalendarDay day) {
            if (availableLocalDateList.size() > 0) {
                if (availableLocalDateList.contains(day.getDate())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void decorate(final DayViewFacade view) {
            //view.setDaysDisabled(true);
            //view.addSpan(new DotSpan(5, R.color.background));
            view.setBackgroundDrawable(MyApplication.getContext().getResources().getDrawable(R.drawable.hollow_circle));
        }
    }

    private static class DayDisabledDecorator implements DayViewDecorator {
        @Override
        public boolean shouldDecorate(final CalendarDay day) {
            if (availableLocalDateList.size() > 0) {
                if (availableLocalDateList.contains(day.getDate())) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void decorate(final DayViewFacade view) {
            view.setDaysDisabled(true);
        }
    }
    
        private static class DaySoldOutDecorator implements DayViewDecorator {
        @Override
        public boolean shouldDecorate(final CalendarDay day) {
            if (bookabledDateList.size() > 0) {
                if (availableLocalDateList.contains(day.getDate())) {
                    int index = availableLocalDateList.indexOf(day.getDate());
                    Slog.d(TAG, "---------->index: "+index+"  count:"+bookabledDateList.get(index).getCount());
                    if (bookabledDateList.get(index).getCount() == mMaximum){
                        Slog.d(TAG, "---------->bookable count is maximum: ");
                        return true;
                    }else {
                        if (bookabledDateList.get(index).getAppointClass() == Utility.OrderClass.BLOCK_BOOKING.ordinal()){
                            return true;
                        }
                    }
                }
            }

            return false;
        }

        @Override
        public void decorate(final DayViewFacade view) {
            view.setDaysDisabled(true);
            view.setBackgroundDrawable(MyApplication.getContext().getResources().getDrawable(R.drawable.sold_out));
        }
    }

    public static class AppointDate {
        int did;
        int sid;
        int eid;
        LocalDate localDate;
        int count = 0;
        int appointClass = Utility.OrderClass.NORMAL.ordinal();
        boolean hasBlockBookingFeature = false;//default can not be block booked

        public int getDid() {
            return did;
        }

        public void setDid(int did) {
            this.did = did;
        }

        public int getSid() {
            return sid;
        }

        public void setSid(int sid) {
            this.sid = sid;
        }

        public int getEid() {
            return eid;
        }

        public void setEid(int eid) {
            this.eid = eid;
        }

        public LocalDate getLocalDate() {
            return localDate;
        }

        public void setLocalDate(LocalDate localDate) {
            this.localDate = localDate;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
        
        public int getAppointClass() { return appointClass; }

        public void setAppointClass(int appointClass) { this.appointClass = appointClass; }

        public boolean getHasBookingFeature() { return hasBlockBookingFeature; }

        public void setHasBlockBookingFeature(boolean hasBlockBookingFeature){ this.hasBlockBookingFeature = hasBlockBookingFeature; }
    }


    private void getAvailableDate() {
        clearAllList();
        
        FormBody.Builder builder = new FormBody.Builder();
        String uri = GET_AVAILABLE_APPOINTMENT_DATE;
        Slog.d(TAG, "-------------------------------->type: " + mType);
        if (mType == Utility.TalentType.EXPERIENCE.ordinal()) {
            builder.add("eid", String.valueOf(eid))
                    .add("type", String.valueOf(Utility.TalentType.EXPERIENCE.ordinal()));
            uri = GET_EXPERIENCE_AVAILABLE_APPOINTMENT_DATE;
        } else {
            builder.add("sid", String.valueOf(sid))
                    .add("type", String.valueOf(Utility.TalentType.GUIDE.ordinal()));
        }
        RequestBody requestBody = builder.build();

        HttpUtil.sendOkHttpRequest(getContext(), uri, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug)
                    Slog.d(TAG, "==========getAvailableDate response body : " + responseText);
                if (responseText != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseText);
                        dateJSONArray = jsonObject.optJSONArray("dates");
                        processResponse();
                        myHandler.sendEmptyMessage(GET_AVAILABLE_APPOINTMENT_DATE_DONE);
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
    
    private void clearAllList(){
        availableLocalDateList.clear();
        appointDateList.clear();
        bookabledDateList.clear();
    }

    private void processResponse() {
        if (dateJSONArray != null && dateJSONArray.length() > 0) {
            for (int i = 0; i < dateJSONArray.length(); i++) {
                AppointDate appointDate = new AppointDate();
                try {
                    JSONObject dateObject = dateJSONArray.getJSONObject(i);
                    appointDate.setDid(dateObject.optInt("did"));
                    if (mType == Utility.TalentType.GUIDE.ordinal()) {
                        appointDate.setSid(dateObject.optInt("id"));
                    } else {
                        appointDate.setEid(dateObject.optInt("id"));
                    }

                    appointDate.setCount(dateObject.optInt("count"));
                    appointDate.setAppointClass(dateObject.optInt("class"));
                    if (dateObject.optInt("block_booking") > 0){
                        appointDate.setHasBlockBookingFeature(true);
                    }
                    appointDate.setLocalDate(LocalDate.parse(timeStampToDay(dateObject.optInt("date")), FORMATTER));
                    appointDateList.add(appointDate);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDateSelected(
            @NonNull MaterialCalendarView widget,
            @NonNull CalendarDay date,
            boolean selected) {
        dataStr = FORMATTER.format(date.getDate());
        selectWrapper.setVisibility(View.VISIBLE);

        if (selected) {
            for (int i = 0; i < appointDateList.size(); i++) {
                if (appointDateList.get(i).getLocalDate().getMonthValue() == date.getMonth()) {
                    if (appointDateList.get(i).getLocalDate().getDayOfMonth() == date.getDay()) {
                        did = appointDateList.get(i).getDid();
                        Slog.d(TAG, "---------------onDateSelected------------>did: " + did);
                        mSoldCount = appointDateList.get(i).getCount();
                        if (mSoldCount > 0) {
                            companionsAmountTV.setText(String.valueOf(mSoldCount));
                            companionsAmountTV.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    getCompanions();
                                }
                            });
                            companionsTV.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    companionsAmountTV.callOnClick();
                            }
                                });

                        } else {
                            companionsAmountTV.setText("0");
                        }
                        
                        if (appointDateList.get(i).getHasBookingFeature() && mSoldCount == 0){
                            mBlockBookingBtn.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDateLongClick(@NonNull MaterialCalendarView widget,
                                @NonNull CalendarDay date) {
        for (int i = 0; i < appointDateList.size(); i++) {
            if (appointDateList.get(i).getLocalDate().getDayOfMonth() == date.getDay()) {
                int did = appointDateList.get(i).getDid();
                Slog.d(TAG, "--------------onDateLongClick------------->did: " + did);
            }
        }
    }
    
        private void getActivityPackageAmount(){
        showProgressDialog("");

        RequestBody requestBody = new FormBody.Builder()
                .add("eid", String.valueOf(eid)).build();

        String uri = GET_PACKAGE_AMOUNT_URL;

        HttpUtil.sendOkHttpRequest(getContext(), uri, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                dismissProgressDialog();
                String responseText = response.body().string();
                Slog.d(TAG, "getActivityPackages response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        int amount = new JSONObject(responseText).optInt("amount");
                        if (amount > 0){
                            myHandler.sendEmptyMessage(GET_PACKAGE_DONE);
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
    
    
    private class OrderStatusBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ORDER_PAYMENT_SUCCESS_BROADCAST:
                    mDialog.dismiss();
                    break;
            }
        }
    }
    
    private void registerBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ORDER_PAYMENT_SUCCESS_BROADCAST);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, intentFilter);
    }

    //unregister local broadcast
    private void unRegisterBroadcast() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                                case PACKAGE_REQUEST_CODE:
                    Slog.d(TAG, "------------>onActivityResult");
                    mPackageName = data.getStringExtra("name");
                    price = data.getIntExtra("price", 0);
                    mPackageNameTV.setVisibility(View.VISIBLE);
                    mPackageNameTV.setText(mPackageName);
                    ((TextView)mDialog.findViewById(R.id.price)).setText(String.valueOf(price));
                    isPackageSelected = true;
                    break;
            }
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case GET_AVAILABLE_APPOINTMENT_DATE_DONE:
                setAppointDateView();
                break;
                       case GET_PACKAGE_DONE:
                hasPackages = true;
                Button packageSelectorBtn = mDialog.findViewById(R.id.package_selector_btn);
                packageSelectorBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startPackageSelectorDF();
                    }
                });
                packageSelectorBtn.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
        unRegisterBroadcast();
    }


    @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
        unRegisterBroadcast();
    }

    static class MyHandler extends Handler {
        WeakReference<CheckAppointDate> checkAppointDateWeakReference;

        MyHandler(CheckAppointDate checkAppointDate) {
            checkAppointDateWeakReference = new WeakReference<CheckAppointDate>(checkAppointDate);
        }

        @Override
        public void handleMessage(Message message) {
            CheckAppointDate checkAppointDate = checkAppointDateWeakReference.get();
            if (checkAppointDate != null) {
                checkAppointDate.handleMessage(message);
            }
        }
    }
}
