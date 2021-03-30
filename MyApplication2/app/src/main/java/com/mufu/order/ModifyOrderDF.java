package com.mufu.order;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.mufu.R;
import com.mufu.common.MyApplication;
import com.mufu.util.BaseDialogFragment;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.OrderCodeFactory;
import com.mufu.util.Slog;
import com.mufu.util.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ModifyOrderDF extends BaseDialogFragment {
    public static final int MODIFY_ORDER_DONE = 1;
    public static final String MODIFY_ORDER = HttpUtil.DOMAIN + "?q=order_manager/modify_order";
    public static final String ORDER_MODIFIED_BROADCAST = "com.mufu.action.ORDER_MODIFIED_BROADCAST";
    private static final boolean isDebug = true;
    private static final String TAG = "ModifyOrderDF";
    private static int mType = Utility.TalentType.EXPERIENCE.ordinal();
    private Dialog mDialog;
    private Window window;
    private int mOid;
    private float mOriginalPrice;
    private String mPackageName = "";
    private int mAmountPeople = 1;
    private Button submitOrderBtn;
    private TextView mOriginalPriceTV;
    private EditText modifiedPriceET;
    private MyHandler myHandler;
  
  public static ModifyOrderDF newInstance(int oid, float originalPrice, int amount) {
        mType = Utility.TalentType.GUIDE.ordinal();
        ModifyOrderDF checkAppointDate = new ModifyOrderDF();
        Bundle bundle = new Bundle();
        bundle.putInt("oid", oid);
        bundle.putFloat("original_price", originalPrice);
        bundle.putInt("amount", amount);
        checkAppointDate.setArguments(bundle);

        return checkAppointDate;
    }
  
  @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_MaterialComponents_DialogWhenLarge);
        mDialog.setContentView(R.layout.modify_order);
        myHandler = new MyHandler(this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mOid = bundle.getInt("oid");
            mOriginalPrice = bundle.getFloat("original_price");
            mAmountPeople = bundle.getInt("amount");
        }
      
      mDialog.setCanceledOnTouchOutside(false);
        window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.BOTTOM;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        TextView dismissTV = mDialog.findViewById(R.id.left_back);
        dismissTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
      
      mOriginalPriceTV = mDialog.findViewById(R.id.original_price);
        mOriginalPriceTV.setText(String.valueOf(mOriginalPrice));
        modifiedPriceET = mDialog.findViewById(R.id.modify_price_edit);
        TextView titleTV = mDialog.findViewById(R.id.title);
        titleTV.setText("修改订单");

        submitOrderBtn = mDialog.findViewById(R.id.save_btn);
        submitOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modifyOrder();
            }
        });

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);
        return mDialog;
    }
  
  private void modifyOrder() {
        String modifiedPrice = modifiedPriceET.getText().toString();
        if (TextUtils.isEmpty(modifiedPrice)){
            Toast.makeText(getContext(), "请输入修改后的价格！", Toast.LENGTH_LONG).show();
            return;
        }

        showProgressDialog(getContext().getString(R.string.submitting_progress));
        int totalPrice = mAmountPeople * Integer.parseInt(modifiedPrice);
        RequestBody requestBody = new FormBody.Builder()
                .add("oid", String.valueOf(mOid))
                .add("price", modifiedPrice)
                .add("total_price", String.valueOf(totalPrice))
                .build();
    
    HttpUtil.sendOkHttpRequest(getContext(), MODIFY_ORDER, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug)
                    Slog.d(TAG, "==========submitOrder response body : " + responseText);
                if (responseText != null) {
                    try {
                        dismissProgressDialog();
                        JSONObject jsonObject = new JSONObject(responseText);
                        int result = jsonObject.optInt("result");
                        if (result > 0) {
                            myHandler.sendEmptyMessage(MODIFY_ORDER_DONE);
                        }
                      } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MODIFY_ORDER_DONE:
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(ORDER_MODIFIED_BROADCAST));
                mDialog.dismiss();
                break;
        }
    }
  
  static class MyHandler extends Handler {
        WeakReference<ModifyOrderDF> placeOrderDFWeakReference;

        MyHandler(ModifyOrderDF placeOrderDF) {
            placeOrderDFWeakReference = new WeakReference<>(placeOrderDF);
        }

        @Override
        public void handleMessage(Message message) {
            ModifyOrderDF modifyOrderDF = placeOrderDFWeakReference.get();
            if (modifyOrderDF != null) {
                modifyOrderDF.handleMessage(message);
            }
        }
    }
}
