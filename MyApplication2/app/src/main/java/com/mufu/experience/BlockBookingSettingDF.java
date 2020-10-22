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
import static com.mufu.experience.DevelopExperienceDialogFragment.BLOCK_BOOKING_REQUEST_CODE;
import static com.mufu.experience.DevelopExperienceDialogFragment.PACKAGE_REQUEST_CODE;
import static com.mufu.experience.PackageSettingDF.GET_BLOCK_BOOKING_PACKAGE_AMOUNT_URL;
import static com.mufu.experience.PackageSettingDF.GET_PACKAGE_AMOUNT_URL;
import static com.mufu.experience.PackageSettingDF.GET_PACKAGE_DONE;

public class BlockBookingSettingDF extends BaseDialogFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "BlockBookingSettingDF";
    private Dialog mDialog;
    private Window window;
    private Button mSaveBtn;
    private EditText mStartingPriceET;
    private EditText mLimitedNumberET;
    private EditText mAdditionalPriceET;
    private Button mBlockBookingPackageBtn;
    private MultipartBody.Builder mMultipartBodyBuilder;
    private static final int SAVE_BLOCK_BOOKING_DONE = 0;
    public static final int GET_BLOCK_BOOKING_DONE = 21;
    private int mBid = 0;
    private Button modify;
    private int mType;
    private int mId;
    private int mPrice = 0;
    private JSONArray mPackageResponse;
    private JSONObject mBlockBookingResponse;
    private boolean isModified = false;
    private boolean isBlockBookingSaved = false;
    private boolean hasPackage = false;
    private boolean isPackageModified = false;
    private MyHandler myHandler;
    public static final String SAVE_BLOCK_BOOKING_BASE_INFO_URL = HttpUtil.DOMAIN + "?q=block_booking/save_block_booking_base_info";
    public static final String GET_BLOCK_BOOKING_BASE_INFO_URL = HttpUtil.DOMAIN + "?q=block_booking/get_block_booking_base_info";
    public static final String MODIFY_BLOCK_BOOKING_BASE_INFO_URL = HttpUtil.DOMAIN + "?q=block_booking/modify_block_booking_base_info";
    
    public static BlockBookingSettingDF newInstance(int id, int type, boolean isBlockBookingSaved) {
        BlockBookingSettingDF blockBookingSettingDF = new BlockBookingSettingDF();
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        bundle.putInt("type", type);
        bundle.putBoolean("isBlockBookingSaved", isBlockBookingSaved);
        blockBookingSettingDF.setArguments(bundle);

        return blockBookingSettingDF;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_MaterialComponents_DialogWhenLarge);
        mDialog.setContentView(R.layout.block_booking_setting);
        myHandler = new MyHandler(this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mId = bundle.getInt("id");
            mType = bundle.getInt("type");
            isBlockBookingSaved = bundle.getBoolean("isBlockBookingSaved");
        }
        
        mDialog.setCanceledOnTouchOutside(true);
        window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);
        
        TextView titleTV = mDialog.findViewById(R.id.title);
        titleTV.setText(getContext().getResources().getString(R.string.block_booking_setting_title));

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
        
        initView();

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);

        return mDialog;
    }
    
    private void initView() {
        mStartingPriceET = mDialog.findViewById(R.id.starting_price_edit);
        mLimitedNumberET = mDialog.findViewById(R.id.limited_number_edit);
        mAdditionalPriceET = mDialog.findViewById(R.id.additional_price_edit);
        mBlockBookingPackageBtn = mDialog.findViewById(R.id.block_booking_package_setting_btn);
        mSaveBtn = mDialog.findViewById(R.id.save_btn);
        TextView modifyTV = mDialog.findViewById(R.id.save);
        modifyTV.setText(getContext().getResources().getString(R.string.edit));
        
        mMultipartBodyBuilder = new MultipartBody.Builder();
        mMultipartBodyBuilder.setType(MultipartBody.FORM);

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validCheck()) {
                    saveBlockBookingBaseInfo();
                }
            }
        });
        
        if (isBlockBookingSaved){
            getBlockBookingBaseInfo();
        }
        
        mBlockBookingPackageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPackageSettingDF();
            }
        });
    }

    private void getBlockBookingPackageAmount(){
        showProgressDialog("");

        RequestBody requestBody = new FormBody.Builder()
                .add("eid", String.valueOf(mId)).build();

        String uri = GET_BLOCK_BOOKING_PACKAGE_AMOUNT_URL;
        
        HttpUtil.sendOkHttpRequest(getContext(), uri, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                dismissProgressDialog();
                String responseText = response.body().string();
                Slog.d(TAG, "getBlockBookingPackageAmount response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        int amount = new JSONObject(responseText).optInt("amount");
                        if (amount > 0){
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

    private void startPackageSettingDF(){
        PackageSettingDF packageSettingDF = PackageSettingDF.newInstance(mId, mType, true, hasPackage);
        packageSettingDF.setTargetFragment(this, PACKAGE_REQUEST_CODE);
        packageSettingDF.show(getFragmentManager(), "PackageSettingDF");
    }
    
    private void getBlockBookingBaseInfo(){
        showProgressDialog("");

        RequestBody requestBody = new FormBody.Builder()
                .add("eid", String.valueOf(mId)).build();

        String uri = GET_BLOCK_BOOKING_BASE_INFO_URL;

        HttpUtil.sendOkHttpRequest(getContext(), uri, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "getBlockBookingBaseInfo response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                try {
                        mBlockBookingResponse = new JSONObject(responseText).optJSONObject("block_booking");
                        if (mBlockBookingResponse != null){
                            dismissProgressDialog();
                            myHandler.sendEmptyMessage(GET_BLOCK_BOOKING_DONE);
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
    
    private void parseBlockBookingResponse(){
        mStartingPriceET.setText(String.valueOf(mBlockBookingResponse.optDouble("starting_price")));
        mLimitedNumberET.setText(String.valueOf(mBlockBookingResponse.optInt("limited_number")));
        mAdditionalPriceET.setText(String.valueOf(mBlockBookingResponse.optDouble("additional_price")));
        mBid = mBlockBookingResponse.optInt("bid");
        getBlockBookingPackageAmount();
    }
    
    private boolean validCheck() {

        if (TextUtils.isEmpty(mStartingPriceET.getText().toString())) {
            Toast.makeText(getContext(), getResources().getString(R.string.starting_price_empty_notice), Toast.LENGTH_LONG).show();
            return false;
        }

        if (TextUtils.isEmpty(mLimitedNumberET.getText().toString())) {
            Toast.makeText(getContext(), getResources().getString(R.string.limited_number_empty_notice), Toast.LENGTH_LONG).show();
            return false;
        }
        
        if (TextUtils.isEmpty(mAdditionalPriceET.getText().toString())) {
            Toast.makeText(getContext(), getResources().getString(R.string.additional_price_empty_notice), Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }
    
    private void saveBlockBookingBaseInfo() {
        showProgressDialog(getContext().getString(R.string.saving_progress));

        FormBody.Builder builder;
        builder = new FormBody.Builder()
                .add("eid", String.valueOf(mId))
                .add("type", String.valueOf(mType))
                .add("starting_price", mStartingPriceET.getText().toString())
                .add("limited_number", mLimitedNumberET.getText().toString())
                .add("additional_price", mAdditionalPriceET.getText().toString());
                
                RequestBody requestBody = builder.build();

        Slog.d(TAG, "eid: "+mId+" starting_price: "+mStartingPriceET.getText().toString()+" limited_number: "+mLimitedNumberET.getText().toString()+" isBlockBookingSaved: "+isBlockBookingSaved);

        String url = SAVE_BLOCK_BOOKING_BASE_INFO_URL;
        if (isBlockBookingSaved){
            url = MODIFY_BLOCK_BOOKING_BASE_INFO_URL;
        }
        
        HttpUtil.sendOkHttpRequest(getContext(), url, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "savePackage response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                try {
                        mBid = new JSONObject(responseText).optInt("bid");
                        if (mBid > 0){
                            dismissProgressDialog();
                            myHandler.sendEmptyMessage(SAVE_BLOCK_BOOKING_DONE);
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
            getTargetFragment().onActivityResult(BLOCK_BOOKING_REQUEST_CODE, RESULT_OK, intent);
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
                        saveBlockBookingBaseInfo();
                    }
                });

        normalDialog.show();
    }
    
    private boolean checkFillStatus() {

        return false;
    }
    
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case SAVE_BLOCK_BOOKING_DONE:
                mDialog.dismiss();
                callBacktoCaller();
                break;
            case GET_BLOCK_BOOKING_DONE:
                parseBlockBookingResponse();
                break;
            case GET_PACKAGE_DONE:
                hasPackage = true;
                mBlockBookingPackageBtn.setText(getContext().getResources().getString(R.string.examine_block_booking_package_setting));
                break;
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PACKAGE_REQUEST_CODE:
                    hasPackage = true;
                    mPrice = data.getIntExtra("price", 0);
                    mBlockBookingPackageBtn.setText(getContext().getResources().getString(R.string.examine_block_booking_package_setting));
                    Slog.d(TAG, "--------------------------->PACKAGE_REQUEST_CODE");
                    break;
            }
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
        WeakReference<BlockBookingSettingDF> routeItemEditDFWeakReference;

        MyHandler(BlockBookingSettingDF routeItemEditDF) {
            routeItemEditDFWeakReference = new WeakReference<BlockBookingSettingDF>(routeItemEditDF);
        }
        
        @Override
        public void handleMessage(Message message) {
            BlockBookingSettingDF routeItemEditDF = routeItemEditDFWeakReference.get();
            if (routeItemEditDF != null) {
                routeItemEditDF.handleMessage(message);
            }
        }
    }
}

                
