package com.tongmenhui.launchak47.util;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nex3z.flowlayout.FlowLayout;
import com.tongmenhui.launchak47.R;

import java.lang.ref.WeakReference;

public class PersonalityEditDialogFragment extends DialogFragment {
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
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        inflater = LayoutInflater.from(mContext);
        mDialog = new Dialog(mContext, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        view = inflater.inflate(R.layout.personality_edit, null);
        //mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(view);
        mDialog.setCanceledOnTouchOutside(true);
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        //layoutParams.alpha = 0.9f;
        layoutParams.gravity = Gravity.TOP;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        //window.setDimAmount(0.8f);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        window.setAttributes(layoutParams);

        initView();
        return mDialog;
    }

    private void initView(){
        final FlowLayout personalityFL = view.findViewById(R.id.personality_flow_layout);
        final EditText editText = view.findViewById(R.id.personality_edit_text);
        final TextView save = view.findViewById(R.id.save);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                editText.setBackground(mContext.getDrawable(R.drawable.label_btn_shape));
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (!TextUtils.isEmpty(v.getText())){
                    Slog.d(TAG, "=========actionId:"+actionId+" text: "+v.getText());
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins((int) Utility.dpToPx(mContext, 3),(int) Utility.dpToPx(mContext, 8),
                            (int) Utility.dpToPx(mContext, 8), (int) Utility.dpToPx(mContext, 3));
                    final TextView personality = new TextView(mContext);
                    personality.setText(v.getText()+" ×");

                    personality.setPadding((int) Utility.dpToPx(mContext, 8), (int) Utility.dpToPx(mContext, 6),
                            (int) Utility.dpToPx(mContext, 8), (int) Utility.dpToPx(mContext, 6));
                    personality.setBackground(mContext.getDrawable(R.drawable.label_btn_shape));
                    personality.setTextColor(mContext.getResources().getColor(R.color.color_blue));
                    personality.setLayoutParams(layoutParams);
                    personalityFL.addView(personality);

                    personality.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            personalityFL.removeView(personality);
                            if(personalityFL.getChildCount() == 0){
                                save.setEnabled(false);
                                save.setTextColor(mContext.getResources().getColor(R.color.color_disabled));
                            }
                        }
                    });
                    editText.setText("");
                    editText.setBackground(mContext.getDrawable(R.drawable.label_btn_shape_no_border));
                    save.setEnabled(true);
                    save.setTextColor(mContext.getResources().getColor(R.color.color_blue));
                }
                return true;
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            String personalityStr = "";
            @Override
            public void onClick(View v) {
                if(personalityFL.getChildCount() > 0){
                    for (int i=0; i<personalityFL.getChildCount(); i++){
                        final TextView personalityTV = (TextView) personalityFL.getChildAt(i);
                        String personality = personalityTV.getText().toString().replace(" ×", "#");
                        personalityStr += personality;
                    }
                }
                Slog.d(TAG, "==============personalityStr: "+personalityStr);
                save.setEnabled(false);
            }
        });
    }
    
    static class MyHandler extends Handler {
        WeakReference<PersonalityEditDialogFragment> personalityEditDialogFragmentWeakReference;

        MyHandler(PersonalityEditDialogFragment personalityDialogFragment) {
            personalityEditDialogFragmentWeakReference = new WeakReference<PersonalityEditDialogFragment>(personalityDialogFragment);
        }

        @Override
        public void handleMessage(Message message) {
            PersonalityEditDialogFragment personalityDialogFragment = personalityEditDialogFragmentWeakReference.get();
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
