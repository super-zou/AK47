package com.hetang.talent;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.hetang.R;
import com.hetang.common.MyApplication;
import com.hetang.experience.ExperienceTalentApplyDF;
import com.hetang.experience.GuideTalentApplyDF;
import com.hetang.util.BaseDialogFragment;
import com.hetang.util.FontManager;
import com.hetang.util.Utility;

public class TalentIntroductionEntryDF extends BaseDialogFragment {
private static final boolean isDebug = true;
    private static final String TAG = "TalentIntroductionEntryDF";
    private Dialog mDialog;
    private Context mContext;

    private Window window;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_MaterialComponents_DialogWhenLarge);
        mDialog.setContentView(R.layout.talent_introduction_entry);

        mDialog.setCanceledOnTouchOutside(true);
        window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.CENTER;
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

        startTalentApply();

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.talent_introduction_entry), font);

        return mDialog;
    }
    
    private void startTalentApply(){
        Button beginBtn = mDialog.findViewById(R.id.begin_now);
        beginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TalentApplyEntryDF talentApplyEntryDF = new TalentApplyEntryDF();
                talentApplyEntryDF.show(getFragmentManager(), "TalentApplyEntryDF");
                mDialog.dismiss();
            }
        });
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
