package com.hetang.experience;

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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.hetang.util.HttpUtil;
import com.hetang.util.Slog;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.style.PictureWindowAnimationStyle;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

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
import static com.hetang.common.MyApplication.getContext;
import static com.hetang.experience.GuideAuthenticationDialogFragment.MODIFY_ROUTE_INFO_URL;
import static com.hetang.experience.GuideAuthenticationDialogFragment.ROUTE_REQUEST_CODE;
import static com.hetang.experience.GuideAuthenticationDialogFragment.SUBMIT_ROUTE_INFO_URL;
import static com.hetang.experience.GuideAuthenticationDialogFragment.WRITE_ROUTE_INFO_SUCCESS;

public class CheckAppointDate extends BaseDialogFragment implements OnDateSelectedListener {
    private static final boolean isDebug = true;
    private static final String TAG = "CheckAppointDate";
    private Dialog mDialog;
    private Window window;
    private int tid;
    private MyHandler myHandler;
    MaterialCalendarView widget;
    private static final int GET_AVAILABLE_APPOINTMENT_DATE_DONE = 1;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("EEE, d MMM yyyy");
    public static final String GET_AVAILABLE_APPOINTMENT_DATE = HttpUtil.DOMAIN + "?q=travel_guide/get_available_appointment_date";
    
    public static CheckAppointDate newInstance(int tid) {
        CheckAppointDate checkAppointDate = new CheckAppointDate();
        Bundle bundle = new Bundle();
        bundle.putInt("tid", tid);
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
            tid = bundle.getInt("tid");
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
        
        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);

        return mDialog;
    }

    private void setAppointDateView() {
        CalendarDay today = CalendarDay.today();
        widget = mDialog.findViewById(R.id.calendarView);
        final LocalDate min = LocalDate.of(today.getYear(), today.getMonth(), today.getDay());
        final LocalDate max = LocalDate.of(today.getYear(), today.getMonth()+3, today.getDay());
        widget.state().edit()
                .setMinimumDate(min)
                .setMaximumDate(max)
                .commit();
    }
    
    private void getAvailableDate(){
        RequestBody requestBody = new FormBody.Builder()
                .add("tid", String.valueOf(tid))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_AVAILABLE_APPOINTMENT_DATE, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug)
                    Slog.d(TAG, "==========getBannerPictures response body : " + responseText);
                if (responseText != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseText);
                        myHandler.sendEmptyMessage(GET_AVAILABLE_APPOINTMENT_DATE_DONE);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
    @Override
    public void onDateSelected(
            @NonNull MaterialCalendarView widget,
            @NonNull CalendarDay date,
            boolean selected) {

        String dataStr;
        dataStr = FORMATTER.format(date.getDate());

    }

    private void submitRoute() {
        showProgressDialog(getContext().getString(R.string.saving_progress));
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
