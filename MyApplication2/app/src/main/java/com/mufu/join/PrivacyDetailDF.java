package com.mufu.join;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.mufu.R;
import com.mufu.common.MyApplication;
import com.mufu.util.BaseDialogFragment;
import com.mufu.util.FontManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class PrivacyDetailDF extends BaseDialogFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "PrivacyProtectionDF";
    private Dialog mDialog;
    private Window window;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_MaterialComponents_DialogWhenLarge);
        mDialog.setContentView(R.layout.privacy_protection_detail);
      
        mDialog.setCanceledOnTouchOutside(false);
        window = mDialog.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.dimAmount = 0.7f;
        layoutParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(layoutParams);
        Bundle bundle = getArguments();
        if (bundle != null){
            int type = bundle.getInt("type");
            readPrivacyDetail(type);
        }
      
      TextView backTV = mDialog.findViewById(R.id.back);
        backTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.back), font);

        return mDialog;
    }
  
  private void readPrivacyDetail(int type){
        InputStream inputStream = null;
        try {
            if (type == 0){
                inputStream = getContext().getAssets().open("agreement.txt");
            }else {
                inputStream = getContext().getAssets().open("privacy.txt");
            }

        }catch (Throwable e){
            e.printStackTrace();
        }

        TextView titleTV = mDialog.findViewById(R.id.title);
        if (type == 0){
            titleTV.setText("牧夫用户协议");
        }else {
            titleTV.setText("牧夫隐私政策");
        }
        TextView privacyTV = mDialog.findViewById(R.id.content);
        privacyTV.setText(getString(inputStream));

    }
  
  public static String getString(InputStream inputStream) {
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(inputStreamReader);
        StringBuffer sb = new StringBuffer("");
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

}
