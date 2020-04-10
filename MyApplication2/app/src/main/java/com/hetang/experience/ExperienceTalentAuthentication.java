package com.hetang.experience;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.hetang.R;
import com.hetang.common.MyApplication;
import com.hetang.util.BaseDialogFragment;
import com.hetang.util.FontManager;

public class ExperienceTalentAuthentication extends BaseDialogFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "ExperienceTalentAuthentication";
    private Dialog mDialog;
    private Window window;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_MaterialComponents_DialogWhenLarge);
        mDialog.setContentView(R.layout.experience_talent_authentication);
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
                dismiss();
            }
        });

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);

        return mDialog;
    }

    private void initView() {
        Button applyGuideBtn = mDialog.findViewById(R.id.apply_guide);
        applyGuideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               startGuideDetailActivity();
                mDialog.dismiss();
            }
        });

        Button developExperienceBtn = mDialog.findViewById(R.id.develop_experience);
        developExperienceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

   public void startGuideDetailActivity(){
        Intent intent = new Intent(getContext(), GuideDetailActivity.class);
        //intent.putExtra("tid", tid);
        startActivity(intent);
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
    }


    @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
    }
}
