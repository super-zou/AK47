package com.mufu.experience;

import android.app.Dialog;
import android.content.DialogInterface;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import static com.mufu.experience.DevelopExperienceDialogFragment.PACKAGE_REQUEST_CODE;

public class PackageSelectorDF extends BaseDialogFragment implements RadioGroup.OnCheckedChangeListener {
    private static final boolean isDebug = true;
    private static final String TAG = "PackageSelectorDF";
    private Dialog mDialog;
    private Window window;
    private Button mSaveBtn;

    private int mType;
    private int mId;
    private int mSelectedId;
    private String mSelectedPackageName;
    private int mSelectedPackagePrice;
    private JSONArray mPackageResponse;
    private MyHandler myHandler;
    public static final int GET_PACKAGE_DONE = 1;
    private RadioButton mFirstPackageBtn;
    private RadioButton mSecondPackageBtn;
    private RadioButton mThirdPackageBtn;
    public static final String GET_PACKAGE_URL = HttpUtil.DOMAIN + "?q=package/get_package";
    
    public static PackageSelectorDF newInstance(int id, int type) {
        PackageSelectorDF routeItemEditDF = new PackageSelectorDF();
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        bundle.putInt("type", type);
        routeItemEditDF.setArguments(bundle);

        return routeItemEditDF;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_MaterialComponents_DialogWhenLarge);
        mDialog.setContentView(R.layout.activity_package_selector);
        myHandler = new MyHandler(this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mId = bundle.getInt("id");
        }
        
        mDialog.setCanceledOnTouchOutside(true);
        window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(layoutParams);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        mFirstPackageBtn = mDialog.findViewById(R.id.first_package_selector_btn);
        mSecondPackageBtn = mDialog.findViewById(R.id.second_package_selector_btn);
        mThirdPackageBtn = mDialog.findViewById(R.id.third_package_selector_btn);
        
        TextView dismissDialog = mDialog.findViewById(R.id.dialog_dismiss);
        dismissDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        initView();

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.dialog_dismiss), font);

        return mDialog;
    }
    
    private void initView() {
        mSaveBtn = mDialog.findViewById(R.id.save_btn);
        RadioGroup radioGroup = mDialog.findViewById(R.id.package_radio_group);
        radioGroup.setOnCheckedChangeListener(this);
        getActivityPackages();
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(mSelectedPackageName)){
                    callBacktoCaller();
                    mDialog.dismiss();
                }else {
                    Toast.makeText(getContext(), "请选择套餐", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int index = 0;
        switch (checkedId){
            case R.id.first_package_selector_btn:
                index = 0;
                break;
            case R.id.second_package_selector_btn:
                index = 1;
                break;
            case R.id.third_package_selector_btn:
                index = 2;
                break;
        }
        //mSelectedId = checkedId;
        //splitPackageArray = content.split("\n\n");
        Slog.d(TAG, "----------------index: "+index);
        mSelectedPackageName = mPackageResponse.optJSONObject(index).optString("name");
        mSelectedPackagePrice = mPackageResponse.optJSONObject(index).optInt("price");
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
        mFirstPackageBtn.setVisibility(View.VISIBLE);
        mFirstPackageBtn.setText(packageObject.optString("name")+"\n\n"+packageObject.optInt("price")+"元/人");

        if (mPackageResponse.length() >= 2){
            mSecondPackageBtn.setVisibility(View.VISIBLE);
            JSONObject packageObjectSecond = mPackageResponse.optJSONObject(1);
            mSecondPackageBtn.setText(packageObjectSecond.optString("name")+"\n\n"+packageObjectSecond.optInt("price")+"元/人");

            if (mPackageResponse.length() >= 3){
                mThirdPackageBtn.setVisibility(View.VISIBLE);
                JSONObject packageObjectThird = mPackageResponse.optJSONObject(2);
                mThirdPackageBtn.setText(packageObjectThird.optString("name")+"\n\n"+packageObjectThird.optInt("price")+"元/人");
            }
        }
    }
    
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case GET_PACKAGE_DONE:
                parsePackagesResponse();
                break;
        }
    }

    private void callBacktoCaller() {
        if (getTargetFragment() != null) {
            Intent intent = new Intent();
            intent.putExtra("name", mSelectedPackageName);
            intent.putExtra("price", mSelectedPackagePrice);
            getTargetFragment().onActivityResult(PACKAGE_REQUEST_CODE, RESULT_OK, intent);
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
        WeakReference<PackageSelectorDF> packageSelectorDFWeakReference;

        MyHandler(PackageSelectorDF packageSelectorDF) {
            packageSelectorDFWeakReference = new WeakReference<PackageSelectorDF>(packageSelectorDF);
        }

        @Override
        public void handleMessage(Message message) {
            PackageSelectorDF packageSelectorDF = packageSelectorDFWeakReference.get();
            if (packageSelectorDF != null) {
                packageSelectorDF.handleMessage(message);
            }
        }
    }
}
