package com.mufu.order;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.mufu.R;
import com.mufu.common.MyApplication;
import com.mufu.experience.PackageSettingDF;
import com.mufu.util.BaseDialogFragment;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.Slog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;
import static com.mufu.experience.DevelopExperienceDialogFragment.BLOCK_BOOKING_REQUEST_CODE;
import static com.mufu.experience.DevelopExperienceDialogFragment.PACKAGE_REQUEST_CODE;
import static com.mufu.experience.PackageSettingDF.GET_BLOCK_BOOKING_PACKAGE_AMOUNT_URL;
import static com.mufu.experience.PackageSettingDF.GET_PACKAGE_DONE;

public class ParticipantIdentityInformationDF extends BaseDialogFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "BlockBookingSettingDF";
    private Dialog mDialog;
    private Window window;
    private JSONArray mIdentityInfoArray;
    private MyHandler myHandler;
    public static final int GET_PARTICIPANT_IDENTITY_DONE = 0;

    public static final String GET_PARTICIPANT_IDENTITY_INFORMATION = HttpUtil.DOMAIN + "?q=order_manager/get_participant_identity_information";
  
      public static ParticipantIdentityInformationDF newInstance(int oid) {
        ParticipantIdentityInformationDF blockBookingSettingDF = new ParticipantIdentityInformationDF();
        Bundle bundle = new Bundle();
        bundle.putInt("oid", oid);
        blockBookingSettingDF.setArguments(bundle);

        return blockBookingSettingDF;
    }
  
  @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        int oid = 0;
        mDialog = new Dialog(getActivity(), R.style.Theme_MaterialComponents_DialogWhenLarge);
        mDialog.setContentView(R.layout.participant_identity_information);
        myHandler = new MyHandler(this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            oid = bundle.getInt("oid");
        }
      
      mDialog.setCanceledOnTouchOutside(true);
        window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        TextView leftBack = mDialog.findViewById(R.id.dismiss);
      
       leftBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        getParticipantIdentityInformation(oid);

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.dismiss), font);

        return mDialog;
    }
  
  private void getParticipantIdentityInformation(int oid){
        showProgressDialog("");

        RequestBody requestBody = new FormBody.Builder()
                .add("oid", String.valueOf(oid)).build();

        String uri = GET_PARTICIPANT_IDENTITY_INFORMATION;
    
    HttpUtil.sendOkHttpRequest(getContext(), uri, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                dismissProgressDialog();
                String responseText = response.body().string();
                Slog.d(TAG, "getParticipantIdentityInformation response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        mIdentityInfoArray = new JSONObject(responseText).optJSONArray("result");
                        if (mIdentityInfoArray.length() > 0){
                            myHandler.sendEmptyMessage(GET_PARTICIPANT_IDENTITY_DONE);
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
            case GET_PARTICIPANT_IDENTITY_DONE:
                setView();
                break;
        }
    }
  
  private void setView(){
        LinearLayout wrapper = mDialog.findViewById(R.id.identity_information_wrapper);
        for (int i=0; i<mIdentityInfoArray.length(); i++){
            View itemView = LayoutInflater.from(MyApplication.getContext())
                    .inflate(R.layout.participant_identity_information_item, (ViewGroup) mDialog.findViewById(android.R.id.content), false);
            wrapper.addView(itemView);
            JSONObject identityInfoObj = mIdentityInfoArray.optJSONObject(i);
            TextView nameTV = itemView.findViewById(R.id.name);
            TextView sexTV = itemView.findViewById(R.id.sex);
            TextView universityTV = itemView.findViewById(R.id.university);
            TextView numberTV = itemView.findViewById(R.id.number);
            nameTV.setText(identityInfoObj.optString("real_name"));
           if (identityInfoObj.optInt("sex") == 0){
                sexTV.setText("男");
            }else {
                sexTV.setText("女");
            }
            universityTV.setText(identityInfoObj.optString("university"));
            numberTV.setText(identityInfoObj.optString("number"));
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

    static class MyHandler extends Handler {
        WeakReference<ParticipantIdentityInformationDF> routeItemEditDFWeakReference;

        MyHandler(ParticipantIdentityInformationDF routeItemEditDF) {
            routeItemEditDFWeakReference = new WeakReference<ParticipantIdentityInformationDF>(routeItemEditDF);
        }
        
        @Override
        public void handleMessage(Message message) {
            ParticipantIdentityInformationDF routeItemEditDF = routeItemEditDFWeakReference.get();
            if (routeItemEditDF != null) {
                routeItemEditDF.handleMessage(message);
            }
        }
    }
}
