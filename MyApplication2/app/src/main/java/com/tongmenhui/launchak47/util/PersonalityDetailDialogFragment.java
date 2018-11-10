package com.tongmenhui.launchak47.util;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.tongmenhui.launchak47.R;

import java.lang.ref.WeakReference;

public class PersonalityDetailDialogFragment extends DialogFragment {
    private static final String TAG = "PersonalityDialogFragment";
    private Context mContext;
    private Dialog mDialog;
    private View view;
    private LayoutInflater inflater;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;

    }
    
        @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
       // getDialog().getWindow().setBackgroundDrawableResource(R.drawable.bg);
        getDialog().getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.personality_dialog_title);
    }
    
        @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        inflater = LayoutInflater.from(mContext);
        mDialog = new Dialog(mContext, android.R.style.Theme_Translucent);
        getDialog().requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

        view = inflater.inflate(R.layout.personality_approved_detail, null);
        //mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(view);
        mDialog.setCanceledOnTouchOutside(true);
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
                layoutParams.gravity = Gravity.BOTTOM;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setDimAmount(0.8f);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        window.setAttributes(layoutParams);

        return mDialog;
    }
    
        static class MyHandler extends Handler {
        WeakReference<PersonalityDetailDialogFragment> personalityDialogFragmentWeakReference;

        MyHandler(PersonalityDetailDialogFragment personalityDialogFragment) {
            personalityDialogFragmentWeakReference = new WeakReference<PersonalityDetailDialogFragment>(personalityDialogFragment);
        }

        @Override
        public void handleMessage(Message message) {
            PersonalityDetailDialogFragment personalityDialogFragment = personalityDialogFragmentWeakReference.get();
            if(personalityDialogFragment != null){
                personalityDialogFragment.handleMessage(message);
            }
        }
    }
    
        public void handleMessage(Message message) {
        switch (message.what) {
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //KeyboardUtils.hideSoftInput(getContext());
        if(mDialog != null){
            mDialog.dismiss();
            mDialog = null;
        }
    }
    
        @Override
    public void onDismiss(DialogInterface dialogInterface){
        super.onDismiss(dialogInterface);
    }

    @Override
    public void onCancel(DialogInterface dialogInterface){
        super.onCancel(dialogInterface);
    }
}
