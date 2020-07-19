package com.mufu.meet;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.mufu.R;
import com.mufu.common.MyApplication;
import com.mufu.util.BaseDialogFragment;
import com.mufu.util.FontManager;
import com.nex3z.flowlayout.FlowLayout;

/**
 * Created by super-zou on 18-9-9.
 */

public class MeetConditionDialogFragment extends BaseDialogFragment {
    private Dialog mDialog;
    private static final boolean isDebug = true;
    private Context mContext;
    private UserMeetInfo mMeetCondition;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_Design_BottomSheetDialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.meet_condition);

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.partner_requirement_label), font);
        mDialog.setCanceledOnTouchOutside(true);
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        final Bundle bundle = getArguments();
        mMeetCondition = (UserMeetInfo) bundle.getSerializable("meet_condition");
        setViewData();


        return mDialog;
    }

    private void setViewData(){
        FlowLayout coreInfo = mDialog.findViewById(R.id.core_info);


        TextView age = coreInfo.findViewById(R.id.age);
        TextView height = coreInfo.findViewById(R.id.height);
        TextView constellation = coreInfo.findViewById(R.id.constellation);
        TextView partnerRequirement = mDialog.findViewById(R.id.partner_requirement);

        /*
        TextView ageRequirement = mDialog.findViewById(R.id.age_require);
        TextView heightRequirement = mDialog.findViewById(R.id.height_require);
        TextView degreeRequirement = mDialog.findViewById(R.id.degree_require);
        TextView livesRequirement = mDialog.findViewById(R.id.lives_require);
        TextView sexRequirement = mDialog.findViewById(R.id.sex_require);
         */

        TextView illustration = mDialog.findViewById(R.id.illustration);

        age.setText(mMeetCondition.getAge() + "岁");
        height.setText(mMeetCondition.getHeight() + "cm");
        constellation.setText(mMeetCondition.getConstellation());

        partnerRequirement.setText(mMeetCondition.getAgeLower() + "~" + mMeetCondition.getAgeUpper() + "岁,"
                +String.valueOf(mMeetCondition.getRequirementHeight()) + "cm以上,"
        +mMeetCondition.getDegreeName(mMeetCondition.getRequirementDegree()) + "以上学历,"
                +"住在"+mMeetCondition.getRequirementLiving()+"的"+mMeetCondition.getRequirementSex());

        /*
        ageRequirement.setText(mMeetCondition.getAgeLower() + "~" + mMeetCondition.getAgeUpper() + "岁");
        heightRequirement.setText(String.valueOf(mMeetCondition.getRequirementHeight()) + "cm");
        degreeRequirement.setText(mMeetCondition.getDegreeName(mMeetCondition.getRequirementDegree()) + "学历");
        livesRequirement.setText("住在"+mMeetCondition.getRequirementLiving());
        sexRequirement.setText("的"+mMeetCondition.getRequirementSex());

         */

        if (mMeetCondition.getIllustration() != null && !TextUtils.isEmpty(mMeetCondition.getIllustration())) {
            illustration.setText("“" + mMeetCondition.getIllustration() + "”");
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
}
