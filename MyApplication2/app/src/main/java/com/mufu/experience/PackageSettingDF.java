package com.mufu.experience;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.mufu.R;
import com.mufu.common.MyApplication;
import com.mufu.util.BaseDialogFragment;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.Slog;
import com.mufu.util.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;
import static com.mufu.experience.DevelopExperienceDialogFragment.PACKAGE_REQUEST_CODE;
import static com.mufu.experience.GuideApplyDialogFragment.MODIFY_ROUTE_INFO_URL;
import static com.mufu.experience.GuideApplyDialogFragment.ROUTE_REQUEST_CODE;
import static com.mufu.experience.GuideApplyDialogFragment.SUBMIT_ROUTE_INFO_URL;
import static com.mufu.experience.GuideApplyDialogFragment.WRITE_ROUTE_INFO_SUCCESS;

public class PackageSettingDF extends BaseDialogFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "PackageSettingDF";
    private Dialog mDialog;
    private Window window;
    private Button mSaveBtn;
    private EditText mFirstPackageNameET;
    private EditText mSecondPackageNameET;
    private EditText mThirdPackageNameET;
    private EditText mFirstPackagePriceET;
    private EditText mSecondPackagePriceET;
    private EditText mThirdPackagePriceET;
    private String mFirstPackageName = "";
    private String mSecondPackageName = "";
    private String mThirdPackageName = "";
    private int mFirstPackagePrice = 0;
    private int mSecondPackagePrice = 0;
    private int mThirdPackagePrice = 0;
    private MultipartBody.Builder mMultipartBodyBuilder;
    private static final int SAVE_PACKAGE_DONE = 0;
    public static final int GET_PACKAGE_DONE = 20;
    private int mPid = 0;
    private Button modify;
    private int mType;
    private int mId;
    private int mMinPrice;
    private JSONArray mPackageResponse;
    private boolean isModified = false;
    private boolean isPackageSaved = false;
    private MyHandler myHandler;
    private ConstraintLayout mPackageSettingWrapper;
    public static final String SAVE_PACKAGE_URL = HttpUtil.DOMAIN + "?q=package/save_package";
    public static final String GET_PACKAGE_URL = HttpUtil.DOMAIN + "?q=package/get_package";
    public static final String GET_PACKAGE_AMOUNT_URL = HttpUtil.DOMAIN + "?q=package/get_package_amount";
    public static final String MODIFY_PACKAGE_URL = HttpUtil.DOMAIN + "?q=package/modify_package";
    
    public static PackageSettingDF newInstance(int id, int type, boolean isPackageSaved) {
        PackageSettingDF routeItemEditDF = new PackageSettingDF();
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        bundle.putInt("type", type);
        bundle.putBoolean("isPackageSaved", isPackageSaved);
        routeItemEditDF.setArguments(bundle);

        return routeItemEditDF;
    }

@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_MaterialComponents_DialogWhenLarge);
        mDialog.setContentView(R.layout.activity_package_setting);
        myHandler = new MyHandler(this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mId = bundle.getInt("id");
            mType = bundle.getInt("type");
            isPackageSaved = bundle.getBoolean("isPackageSaved");
        }
        
        mDialog.setCanceledOnTouchOutside(true);
        window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        mPackageSettingWrapper = mDialog.findViewById(R.id.activity_package_setting);

        TextView leftBack = mDialog.findViewById(R.id.left_back);
        
        leftBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkFillStatus()) {
                    if (isModified) {
                        showNoticeDialog();
                    } else {
                        dismiss();
                    }
                } else {
                    dismiss();
                }
            }
        });

        //modify = mDialog.findViewById(R.id.route_modify);

        initView();

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);

        return mDialog;
    }
    
    private void initView() {
        mFirstPackageNameET = mDialog.findViewById(R.id.activity_package_first_name);
        mSecondPackageNameET = mDialog.findViewById(R.id.activity_package_second_name);
        mThirdPackageNameET = mDialog.findViewById(R.id.activity_package_third_name);
        mFirstPackagePriceET = mDialog.findViewById(R.id.activity_package_first_price);
        mSecondPackagePriceET = mDialog.findViewById(R.id.activity_package_second_price);
        mThirdPackagePriceET = mDialog.findViewById(R.id.activity_package_third_price);
        mSaveBtn = mDialog.findViewById(R.id.save_btn);
        TextView modifyTV = mDialog.findViewById(R.id.save);
        modifyTV.setText(getContext().getResources().getString(R.string.edit));
        
         mMultipartBodyBuilder = new MultipartBody.Builder();
        mMultipartBodyBuilder.setType(MultipartBody.FORM);

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validCheck()) {
                    savePackage();
                }
            }
        });
        
        if (isPackageSaved){
            int length = mPackageSettingWrapper.getChildCount();
            for (int i=0; i<length; i++){
                mPackageSettingWrapper.getChildAt(i).setEnabled(false);
            }

            modifyTV.setVisibility(View.VISIBLE);
            modifyTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isModified = true;
                    mSaveBtn.setVisibility(View.VISIBLE);
                    for (int i=0; i<length; i++){
                        mPackageSettingWrapper.getChildAt(i).setEnabled(true);
                    }
                }
            });
            mSaveBtn.setVisibility(View.GONE);
            getActivityPackages();
        }
    }
    
    private void getActivityPackages(){
        showProgressDialog("");

        RequestBody requestBody = new FormBody.Builder()
                .add("eid", String.valueOf(mId)).build();

        String uri = GET_PACKAGE_URL;

        HttpUtil.sendOkHttpRequest(getContext(), uri, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "getActivityPackages response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        mPackageResponse = new JSONObject(responseText).optJSONArray("packages");
                        if (mPackageResponse.length() > 0){
                            dismissProgressDialog();
                            myHandler.sendEmptyMessage(GET_PACKAGE_DONE);
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
    
    private void parsePackagesResponse(){
        JSONObject packageObject = mPackageResponse.optJSONObject(0);
        mFirstPackageNameET.setText(packageObject.optString("name"));
        mFirstPackagePriceET.setText(String.valueOf(packageObject.optInt("price")));

        if (mPackageResponse.length() >= 2){
            JSONObject packageObjectSecond = mPackageResponse.optJSONObject(1);
            mSecondPackageNameET.setText(packageObjectSecond.optString("name"));
            mSecondPackagePriceET.setText(String.valueOf(packageObjectSecond.optInt("price")));

            if (mPackageResponse.length() >= 3){
                JSONObject packageObjectThird = mPackageResponse.optJSONObject(2);
                mThirdPackageNameET.setText(packageObjectThird.optString("name"));
                mThirdPackagePriceET.setText(String.valueOf(packageObjectThird.optInt("price")));
            }
        }
    }
    
    private boolean validCheck() {

        if (TextUtils.isEmpty(mFirstPackageNameET.getText().toString())
                && TextUtils.isEmpty(mSecondPackageNameET.getText().toString())
                && TextUtils.isEmpty(mThirdPackageNameET.getText().toString())) {
            Toast.makeText(getContext(), getResources().getString(R.string.package_name_empty_notice), Toast.LENGTH_LONG).show();
            return false;
        }else {
            if (!TextUtils.isEmpty(mFirstPackageNameET.getText().toString())){
                mFirstPackageName = mFirstPackageNameET.getText().toString();
                if(TextUtils.isEmpty(mFirstPackagePriceET.getText().toString())){
                    Toast.makeText(getContext(), getResources().getString(R.string.package_price_empty_notice), Toast.LENGTH_LONG).show();
                    return false;
                }else {
                    mFirstPackagePrice = Integer.parseInt(mFirstPackagePriceET.getText().toString());
         }
            }

            if (!TextUtils.isEmpty(mSecondPackageNameET.getText().toString())){
                mSecondPackageName = mSecondPackageNameET.getText().toString();
                if(TextUtils.isEmpty(mSecondPackagePriceET.getText().toString())){
                    Toast.makeText(getContext(), getResources().getString(R.string.package_price_empty_notice), Toast.LENGTH_LONG).show();
                    return false;
                }else {
                    mSecondPackagePrice = Integer.parseInt(mSecondPackagePriceET.getText().toString());
                }
            }
            
            if (!TextUtils.isEmpty(mThirdPackageNameET.getText().toString())){
                mThirdPackageName = mThirdPackageNameET.getText().toString();
                if(TextUtils.isEmpty(mThirdPackagePriceET.getText().toString())){
                    Toast.makeText(getContext(), getResources().getString(R.string.package_price_empty_notice), Toast.LENGTH_LONG).show();
                    return false;
                }else {
                    mThirdPackagePrice = Integer.parseInt(mThirdPackagePriceET.getText().toString());
                }
            }
        }

        return true;
    }
    
    private void savePackage() {
        showProgressDialog(getContext().getString(R.string.saving_progress));

        mMultipartBodyBuilder.addFormDataPart("eid", String.valueOf(mId));
        mMultipartBodyBuilder.addFormDataPart("type", String.valueOf(mType));

        if (!mFirstPackageName.isEmpty()){
            mMultipartBodyBuilder.addFormDataPart("first_package_name", mFirstPackageName);
            mMultipartBodyBuilder.addFormDataPart("first_package_price", String.valueOf(mFirstPackagePrice));
            mMinPrice = mFirstPackagePrice;
        }
        
        if (!mSecondPackageName.isEmpty()){
            mMultipartBodyBuilder.addFormDataPart("second_package_name", mSecondPackageName);
            mMultipartBodyBuilder.addFormDataPart("second_package_price", String.valueOf(mSecondPackagePrice));
            if (mMinPrice > mSecondPackagePrice){
                mMinPrice = mSecondPackagePrice;
            }
        }

        if (!mThirdPackageName.isEmpty()){
            mMultipartBodyBuilder.addFormDataPart("third_package_name", mThirdPackageName);
            mMultipartBodyBuilder.addFormDataPart("third_package_price", String.valueOf(mThirdPackagePrice));
            if (mMinPrice > mThirdPackagePrice){
                mMinPrice = mThirdPackagePrice;
            }
        }
        
        RequestBody requestBody = mMultipartBodyBuilder.build();

        String url = SAVE_PACKAGE_URL;
        if (isModified){
            url = MODIFY_PACKAGE_URL;
        }

        HttpUtil.sendOkHttpRequest(getContext(), url, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "savePackage response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                try {
                        int status = new JSONObject(responseText).optInt("status");
                        if (status == 1){
                            dismissProgressDialog();
                            myHandler.sendEmptyMessage(SAVE_PACKAGE_DONE);
                        }else {

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
    
    private void callBacktoCaller() {
        if (getTargetFragment() != null) {
            Intent intent = new Intent();
            intent.putExtra("price", mMinPrice);
            intent.putExtra("isModified", isModified);
            getTargetFragment().onActivityResult(PACKAGE_REQUEST_CODE, RESULT_OK, intent);
        }
    }

    private void showNoticeDialog() {
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(getContext(), R.style.Theme_MaterialComponents_Light_Dialog_Alert);
        normalDialog.setTitle("确认放弃本次编辑吗？");
        normalDialog.setMessage("编辑尚未保存，若返回将会丢弃。");
        
        normalDialog.setPositiveButton("放弃",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDialog.dismiss();
                    }
                });

        normalDialog.setNegativeButton("保存",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //mSaveBtn.callOnClick();
                        savePackage();
                    }
                });

        normalDialog.show();
    }
    
    private boolean checkFillStatus() {

        return false;
    }


    public void handleMessage(Message msg) {
        switch (msg.what) {
            case SAVE_PACKAGE_DONE:
                mDialog.dismiss();
                callBacktoCaller();
                break;
            case GET_PACKAGE_DONE:
                parsePackagesResponse();
                break;
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
        WeakReference<PackageSettingDF> routeItemEditDFWeakReference;

        MyHandler(PackageSettingDF routeItemEditDF) {
            routeItemEditDFWeakReference = new WeakReference<PackageSettingDF>(routeItemEditDF);
        }

        @Override
        public void handleMessage(Message message) {
            PackageSettingDF routeItemEditDF = routeItemEditDFWeakReference.get();
            if (routeItemEditDF != null) {
                routeItemEditDF.handleMessage(message);
            }
        }
    }
}
