package com.mufu.common;

import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.mufu.R;
import com.mufu.join.PrivacyDetailDF;
import com.mufu.util.BaseDialogFragment;
import com.mufu.util.FontManager;
import com.mufu.util.SharedPreferencesUtils;

public class AboutDF extends BaseDialogFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "AboutDF";
    private Dialog mDialog;
    private Window window;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_MaterialComponents_DialogWhenLarge);
        mDialog.setContentView(R.layout.about);
      
      mDialog.setCanceledOnTouchOutside(false);
      window = mDialog.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.TOP;
        //layoutParams.dimAmount = 0.7f;
        layoutParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        TextView serviceAgreeTV = mDialog.findViewById(R.id.service_agreement);
        TextView privacyPolicyTV = mDialog.findViewById(R.id.privacy_policy);
        serviceAgreeTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PrivacyDetailDF privacyDetailDF = new PrivacyDetailDF();
                Bundle bundle = new Bundle();
                bundle.putInt("type", 0);
                privacyDetailDF.setArguments(bundle);
                privacyDetailDF.show(getFragmentManager(), "PrivacyDetailDF");
            }
        });

        privacyPolicyTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PrivacyDetailDF privacyDetailDF = new PrivacyDetailDF();
                Bundle bundle = new Bundle();
                bundle.putInt("type", 1);
                privacyDetailDF.setArguments(bundle);
                privacyDetailDF.show(getFragmentManager(), "PrivacyDetailDF");
            }
        });
      
       TextView leftTV = mDialog.findViewById(R.id.left_back);
        leftTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });

        TextView titleTV = mDialog.findViewById(R.id.title);
        titleTV.setText(getContext().getResources().getString(R.string.about));

        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.left_back), font);

        return mDialog;
    }
}
