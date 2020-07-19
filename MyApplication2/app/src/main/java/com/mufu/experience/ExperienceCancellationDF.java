package com.mufu.experience;

import android.app.Dialog;
import android.content.DialogInterface;
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

import com.mufu.R;
import com.mufu.common.MyApplication;
import com.mufu.util.BaseDialogFragment;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.Utility;

import org.threeten.bp.format.DateTimeFormatter;

import java.lang.ref.WeakReference;

public class ExperienceCancellationDF extends BaseDialogFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "PlaceOrderDF";
    private Dialog mDialog;
    private Window window;
    
     private static int mType = Utility.TalentType.EXPERIENCE.ordinal();
    private int price;
    private int type;
    private int did;
    private String title;
    private String date;
    private int mAmountPeople = 1;
    private Button payBtn;
    private TextView totalPriceTV;
    private TextView totalAmountTV;
    private MyHandler myHandler;
    private static final int GET_AVAILABLE_APPOINTMENT_DATE_DONE = 1;
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("EEE, d MMM yyyy");
    public static final String PLACE_ORDER = HttpUtil.DOMAIN + "?q=order_manager/place_order";
    
    public static ExperienceCancellationDF newInstance() {
        mType = Utility.TalentType.GUIDE.ordinal();
        ExperienceCancellationDF checkAppointDate = new ExperienceCancellationDF();
        return checkAppointDate;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_MaterialComponents_DialogWhenLarge);
        mDialog.setContentView(R.layout.experience_cancellation);
        myHandler = new MyHandler(this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            //todo
        }
        
        mDialog.setCanceledOnTouchOutside(false);
        window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.BOTTOM;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);
        
        TextView dismissTV = mDialog.findViewById(R.id.dismiss);
        dismissTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.dismiss), font);


        return mDialog;
    }
    
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case GET_AVAILABLE_APPOINTMENT_DATE_DONE:

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
        WeakReference<ExperienceCancellationDF> experienceCancellationDFWeakReference;

        MyHandler(ExperienceCancellationDF experienceCancellationDF) {
            experienceCancellationDFWeakReference = new WeakReference<>(experienceCancellationDF);
        }

        @Override
        public void handleMessage(Message message) {
            ExperienceCancellationDF experienceCancellationDF = experienceCancellationDFWeakReference.get();
            if (experienceCancellationDF != null) {
                experienceCancellationDF.handleMessage(message);
            }
        }
    }
}
