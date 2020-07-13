package com.hetang.talent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.hetang.R;
import com.hetang.adapter.GridImageAdapter;
import com.hetang.common.MyApplication;
import com.hetang.common.OnItemClickListener;
import com.hetang.dynamics.AddDynamicsActivity;
import com.hetang.experience.ExperienceTalentApplyDF;
import com.hetang.experience.GuideTalentApplyDF;
import com.hetang.group.SubGroupActivity;
import com.hetang.main.FullyGridLayoutManager;
import com.hetang.picture.GlideEngine;
import com.hetang.util.BaseDialogFragment;
import com.hetang.util.CommonBean;
import com.hetang.util.CommonDialogFragmentInterface;
import com.hetang.util.CommonPickerView;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.Slog;
import com.hetang.util.Utility;
import com.luck.picture.lib.PictureSelectionModel;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.style.PictureWindowAnimationStyle;
import com.luck.picture.lib.tools.PictureFileUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
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
import static com.hetang.group.GroupFragment.eden_group;
import static com.hetang.group.SubGroupActivity.TALENT_ADD_BROADCAST;
import static com.hetang.group.SubGroupActivity.getTalent;

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
