package com.mufu.talent;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.mufu.R;
import com.mufu.common.MyApplication;
import com.mufu.experience.ExperienceTalentApplyDF;
import com.mufu.experience.GuideTalentApplyDF;
import com.mufu.util.BaseDialogFragment;
import com.mufu.util.FontManager;
import com.mufu.util.Utility;

public class TalentApplyEntryDF extends BaseDialogFragment {

    private static final boolean isDebug = true;
    private static final String TAG = "TalentApplyEntryDF";
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
        mDialog.setContentView(R.layout.talent_apply_entry);

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

        processTalentApply();

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.talent_apply_entry), font);

        return mDialog;
    }
    
    private void processTalentApply(){
        ConstraintLayout experienceTalentApply = mDialog.findViewById(R.id.experience_talent_wrapper);
        experienceTalentApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExperienceTalentApplyDF experienceTalentApplyDF = new ExperienceTalentApplyDF();
                experienceTalentApplyDF.show(getFragmentManager(), "ExperienceTalentApplyDF");
            }
        });

        ConstraintLayout guideTalentApply = mDialog.findViewById(R.id.guide_talent_wrapper);
        guideTalentApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GuideTalentApplyDF guideTalentApplyDF = new GuideTalentApplyDF();
                guideTalentApplyDF.show(getFragmentManager(), "GuideTalentApplyDF");
            }
        });
        
        ConstraintLayout travelTalentApply = mDialog.findViewById(R.id.travel_talent_wrapper);
        travelTalentApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                becomeTalent(Utility.TalentType.TRAVEL);
            }
        });

        ConstraintLayout interestTalentApply = mDialog.findViewById(R.id.interest_talent_wrapper);
        interestTalentApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                becomeTalent(Utility.TalentType.INTEREST);
            }
        });
        
        ConstraintLayout growthTalentApply = mDialog.findViewById(R.id.growth_talent_wrapper);
        growthTalentApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                becomeTalent(Utility.TalentType.GROWTH);
            }
        });

        ConstraintLayout foodTalentApply = mDialog.findViewById(R.id.food_talent_wrapper);
        foodTalentApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                becomeTalent(Utility.TalentType.FOOD);
            }
        });
        
        ConstraintLayout matchMakerTalentApply = mDialog.findViewById(R.id.match_maker_talent_wrapper);
        matchMakerTalentApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                becomeTalent(Utility.TalentType.MATCHMAKER);
            }
        });
    }

    private void becomeTalent(Utility.TalentType type) {
        TalentAuthenticationDialogFragment talentAuthenticationDialogFragment = new TalentAuthenticationDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("type", type);
        talentAuthenticationDialogFragment.setArguments(bundle);
        //createSingleGroupDialogFragment.setTargetFragment(SingleGroupActivity.this, REQUEST_CODE);
        talentAuthenticationDialogFragment.show(getFragmentManager(), "TalentAuthenticationDialogFragment");
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
