package com.mufu.join;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.mufu.R;
import com.mufu.common.MyApplication;
import com.mufu.order.MyOrdersFragmentDF;
import com.mufu.order.OrderDetailsDF;
import com.mufu.order.OrderPaymentDF;
import com.mufu.util.BaseDialogFragment;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.OrderCodeFactory;
import com.mufu.util.SharedPreferencesUtils;
import com.mufu.util.Slog;
import com.mufu.util.Utility;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class PrivacyProtectionDF extends BaseDialogFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "PrivacyProtectionDF";
    private Dialog mDialog;
    private Window window;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_MaterialComponents_DialogWhenLarge);
        mDialog.setContentView(R.layout.privacy_protection);
      
      mDialog.setCanceledOnTouchOutside(false);
        window = mDialog.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.dimAmount = 0.7f;
        layoutParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(layoutParams);
      
      Button acceptBtn = mDialog.findViewById(R.id.accept);
        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferencesUtils.setPrivacyStatus(getContext(), true);
                mDialog.dismiss();
            }
        });

        TextView privacyTV = mDialog.findViewById(R.id.content);
        final SpannableStringBuilder style = new SpannableStringBuilder();
        style.append(getContext().getResources().getString(R.string.privacy_protection_content));
      
      //设置部分文字点击事件
        ClickableSpan agreementClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                PrivacyDetailDF privacyDetailDF = new PrivacyDetailDF();
                Bundle bundle = new Bundle();
                bundle.putInt("type", 0);
                privacyDetailDF.setArguments(bundle);
                privacyDetailDF.show(getFragmentManager(), "PrivacyDetailDF");
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
      
      style.setSpan(agreementClickableSpan, 12, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        privacyTV.setText(style);

        //设置部分文字颜色
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(getContext().getResources().getColor(R.color.background));
        style.setSpan(foregroundColorSpan, 12, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        //设置部分文字点击事件
        ClickableSpan privacyClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                PrivacyDetailDF privacyDetailDF = new PrivacyDetailDF();
                Bundle bundle = new Bundle();
                bundle.putInt("type", 1);
                privacyDetailDF.setArguments(bundle);
                privacyDetailDF.show(getFragmentManager(), "PrivacyDetailDF");
            }
          
          @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };

        style.setSpan(privacyClickableSpan, 21, 29, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        privacyTV.setText(style);

        //设置部分文字颜色
        style.setSpan(foregroundColorSpan, 21, 29, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        //配置给TextView
        privacyTV.setMovementMethod(LinkMovementMethod.getInstance());
        privacyTV.setText(style);

        return mDialog;
    }
}
