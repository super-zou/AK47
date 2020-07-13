package com.hetang.experience;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.hetang.R;
import com.hetang.common.MyApplication;
import com.hetang.home.CommonContactsActivity;
import com.hetang.order.PlaceOrderDF;
import com.hetang.util.BaseDialogFragment;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.Slog;

import com.hetang.util.Utility;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateLongClickListener;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;
import static com.hetang.experience.DevelopExperienceDialogFragment.FORMATTER;
import static com.hetang.home.CommonContactsActivity.EXPERIENCE_COMPANION;
import static com.hetang.util.DateUtil.timeStampToDay;

public class CheckAppointDate extends BaseDialogFragment implements OnDateSelectedListener , OnDateLongClickListener{
    private static final boolean isDebug = true;
    private static final String TAG = "CheckAppointDate";
    private Dialog mDialog;
    private Window window;
    private static int mType = Utility.TalentType.EXPERIENCE.ordinal();
    private int sid;
    private int eid;
    private int did;
    private int price;
    private int count = 0;
    private String unit;
    private String title;
    private String dataStr;
    private Button selectBtn;
    private MyHandler myHandler;
    MaterialCalendarView widget;
    private ConstraintLayout selectWrapper;
    private TextView companionsTV;
    private JSONArray dateJSONArray;
    private static CalendarDay today;
    private static List<LocalDate> availableDateList = new ArrayList<>();
    private static List<AppointDate> appointDateList = new ArrayList<>();
    private static final int GET_AVAILABLE_APPOINTMENT_DATE_DONE = 1;
    public static final String GET_AVAILABLE_APPOINTMENT_DATE = HttpUtil.DOMAIN + "?q=travel_guide/get_available_appointment_date";
    public static final String GET_EXPERIENCE_AVAILABLE_APPOINTMENT_DATE = HttpUtil.DOMAIN + "?q=experience/get_experience_available_appointment_date";
    public static final String PLACE_ORDER = HttpUtil.DOMAIN + "?q=order_manager/place_order";
    
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
    
    public static CheckAppointDate newInstance(int eid, int price, String title) {
        mType = Utility.TalentType.EXPERIENCE.ordinal();
        CheckAppointDate checkAppointDate = new CheckAppointDate();
        Bundle bundle = new Bundle();
        bundle.putInt("eid", eid);
        bundle.putInt("price", price);
        bundle.putString("title", title);
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
            if (mType == Utility.TalentType.EXPERIENCE.ordinal()){
                eid = bundle.getInt("eid");
                unit = "人起";
            }else {
                sid = bundle.getInt("sid");
                unit = bundle.getString("unit");
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
                dismiss();
            }
        });
        
        getAvailableDate();
        
        TextView amountTV = mDialog.findViewById(R.id.amount);
        TextView unitTV = mDialog.findViewById(R.id.unit);
        amountTV.setText(String.valueOf(price));
        unitTV.setText(unit);

        selectWrapper = mDialog.findViewById(R.id.select_wrapper);
        selectBtn = mDialog.findViewById(R.id.select);
        companionsTV = mDialog.findViewById(R.id.numberOfcompanions);

        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitOrder();
            }
        });
        
        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.cny), font);

        return mDialog;
    }
    
    private void getCompanions(){
        Intent intent = new Intent(getContext(), CommonContactsActivity.class);
        intent.putExtra("did", did);
        intent.putExtra("type", EXPERIENCE_COMPANION);
        startActivity(intent);
    }
    
    private void submitOrder(){
        PlaceOrderDF placeOrderDF;
        if (mType == Utility.TalentType.EXPERIENCE.ordinal()){
            placeOrderDF = PlaceOrderDF.newInstance(title, price, did, dataStr, eid, mType);
        }else {
            placeOrderDF = PlaceOrderDF.newInstance(title, price, did, dataStr, sid, mType);
        }

        placeOrderDF.show(getFragmentManager(), "PlaceOrderDF");
    }
    

    private void setAppointDateView() {
        today = CalendarDay.today();
        for (int i=0; i<appointDateList.size(); i++){
            //JSONObject dateObject = dateJSONArray.getJSONObject(i);
            LocalDate localDate = appointDateList.get(i).getLocalDate();
            if (localDate.getDayOfMonth() >= today.getDay() || localDate.getMonthValue() > today.getMonth()){
                availableDateList.add(localDate);
            }
        }

        widget = mDialog.findViewById(R.id.calendarView);
        widget.setOnDateChangedListener(this);
        widget.setOnDateLongClickListener(this);
        final LocalDate min = LocalDate.of(today.getYear(), today.getMonth(), today.getDay());
        final LocalDate max = LocalDate.of(today.getYear(), today.getMonth()+3, today.getDay());
        widget.addDecorator(new AvailableDecorator());
        widget.addDecorator(new DayDisabledDecorator());
        widget.state().edit()
                .setMinimumDate(min)
                .setMaximumDate(max)
                .commit();
    }

    public static class AvailableDecorator implements DayViewDecorator {
        @Override
        public boolean shouldDecorate(final CalendarDay day) {
            if (availableDateList.size() > 0){
                if (availableDateList.contains(day.getDate())){
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
    
    private static class DayDisabledDecorator implements DayViewDecorator{
        @Override
        public boolean shouldDecorate(final CalendarDay day) {
            if (availableDateList.size() > 0){
                if (availableDateList.contains(day.getDate())){
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

    public static class AppointDate{
        int did;
        int sid;
        int eid;
        LocalDate localDate;
        int count = 0;

        public int getDid() {
            return did;
        }

        public void setDid(int did){
            this.did = did;
        }
        
        public int getSid(){
            return sid;
        }

        public void setSid(int sid){
            this.sid = sid;
        }

        public int getEid(){
            return eid;
        }

        public void setEid(int eid){
            this.eid = eid;
        }
        
         public LocalDate getLocalDate(){
            return localDate;
        }

        public void setLocalDate(LocalDate localDate){
            this.localDate = localDate;
        }

        public int getCount(){
            return count;
        }

        public void setCount(int count){
            this.count = count;
        }
    }
    
    
    
            
    private void getAvailableDate(){
        availableDateList.clear();
        appointDateList.clear();
        FormBody.Builder builder = new FormBody.Builder();
        String uri = GET_AVAILABLE_APPOINTMENT_DATE;
        Slog.d(TAG, "-------------------------------->type: "+mType);
        if (mType == Utility.TalentType.EXPERIENCE.ordinal()){
            builder.add("eid", String.valueOf(eid))
                    .add("type", String.valueOf(Utility.TalentType.EXPERIENCE.ordinal()));
            uri = GET_EXPERIENCE_AVAILABLE_APPOINTMENT_DATE;
        }else {
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
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {}
        });
    }
    
    private void processResponse(){
        if (dateJSONArray != null && dateJSONArray.length() > 0){
            for (int i=0; i<dateJSONArray.length(); i++){
                AppointDate appointDate = new AppointDate();
                try {
                    JSONObject dateObject = dateJSONArray.getJSONObject(i);
                    appointDate.setDid(dateObject.optInt("did"));
                    if (mType == Utility.TalentType.GUIDE.ordinal()){
                        appointDate.setSid(dateObject.optInt("sid"));
                    }else {
                        appointDate.setEid(dateObject.optInt("eid"));
                    }
                    Slog.d(TAG, "-------------------count: "+dateObject.optInt("count"));
                    appointDate.setCount(dateObject.optInt("count"));
                    appointDate.setLocalDate(LocalDate.parse(timeStampToDay(dateObject.optInt("date")), FORMATTER));
                    appointDateList.add(appointDate);
            }catch (JSONException e){
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

        if (selected){
            for (int i=0; i< appointDateList.size(); i++){
                if (appointDateList.get(i).getLocalDate().getMonthValue() == date.getMonth()){
                    if (appointDateList.get(i).getLocalDate().getDayOfMonth() == date.getDay()){
                        did = appointDateList.get(i).getDid();
                        Slog.d(TAG, "---------------onDateSelected------------>did: "+did);
                        count = appointDateList.get(i).getCount();
                        if (count > 0){
                            companionsTV.setText("同行人数"+count);
                            companionsTV.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    getCompanions();
                                }
                            });

                        }else {
                            companionsTV.setText("");
                        }
                 }
            }
        }
    }
    
        @Override
    public void onDateLongClick(@NonNull MaterialCalendarView widget,
                                @NonNull CalendarDay date){
        for (int i=0; i< appointDateList.size(); i++){
            if (appointDateList.get(i).getLocalDate().getDayOfMonth() == date.getDay()){
                int did = appointDateList.get(i).getDid();
                Slog.d(TAG, "--------------onDateLongClick------------->did: "+did);
            }
        }
    }

    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {

            }
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case GET_AVAILABLE_APPOINTMENT_DATE_DONE:
                setAppointDateView();
                break;
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
