package com.mufu.group;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.mufu.R;
import com.mufu.common.MyApplication;
import com.mufu.util.BaseDialogFragment;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.Slog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PreparationDialogFragment extends BaseDialogFragment {
    private Dialog mDialog;
    private static final boolean isDebug = true;
    int type = 0;
    Button follow;
    TextView followCountTV;
    TextView visit;
    int visitRecord = 0;
    int followCount = 0;
    int isFollowed = 0;
    private MyHandler myHandler;
    private static final String TAG = "PreparationDialogFragment";
    private static final String ROOT_GROUP_FOLLOW_ADD = HttpUtil.DOMAIN + "?q=follow/root_group_action/add";
    private static final String ROOT_GROUP_FOLLOW_GET = HttpUtil.DOMAIN + "?q=follow/root_group_action/get";
    private static final String ROOT_GROUP_VISIT_ADD = HttpUtil.DOMAIN + "?q=visitor_record/add_root_group_visit_record";
    private static final String ROOT_GROUP_VISIT_GET = HttpUtil.DOMAIN + "?q=visitor_record/get_root_group_visit_record";

    private static final int GET_FOLLOW_DONE = 0;
    private static final int GET_VISIT_DONE = 1;
    private static final int ADD_FOLLOW_DONE = 2;



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_Design_BottomSheetDialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.preparation);
        
         Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.create_subgroup), font);
        mDialog.setCanceledOnTouchOutside(true);
        Window window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        myHandler = new MyHandler(this);

        follow = mDialog.findViewById(R.id.followBtn);
        followCountTV = mDialog.findViewById(R.id.follow_count);
        visit = mDialog.findViewById(R.id.visit_record);

        Bundle bundle = getArguments();
        if (bundle != null){
            type = bundle.getInt("type");
        }

        getFollow();
        getVisit();
        
         follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFollow();
            }
        });

        TextView back = mDialog.findViewById(R.id.left_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });

        return mDialog;
    }
    
    private void getFollow(){
        final RequestBody requestBody = new FormBody.Builder().add("type", String.valueOf(type)).build();
        HttpUtil.sendOkHttpRequest(getContext(), ROOT_GROUP_FOLLOW_GET, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (isDebug) Slog.d(TAG, "==========response body : " + response.body());
if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        //if (isDebug) Slog.d(TAG, "==========response text 1: " + responseText);
                        try {
                            JSONObject responseObj = new JSONObject(responseText);
                            if (responseObj != null) {
                                followCount = responseObj.optInt("followCount");
                                isFollowed = responseObj.optInt("isFollowed");
                                myHandler.sendEmptyMessage(GET_FOLLOW_DONE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }
       }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    private void addFollow(){
        final RequestBody requestBody = new FormBody.Builder().add("type", String.valueOf(type)).build();
        HttpUtil.sendOkHttpRequest(getContext(), ROOT_GROUP_FOLLOW_ADD, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (isDebug) Slog.d(TAG, "==========response body : " + response.body());

                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                    try {
                            JSONObject responseObj = new JSONObject(responseText);
                            if (responseObj != null) {
                                followCount = responseObj.optInt("followCount");
                                myHandler.sendEmptyMessage(ADD_FOLLOW_DONE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    private void getVisit(){
        final RequestBody requestBody = new FormBody.Builder().add("type", String.valueOf(type)).build();
        HttpUtil.sendOkHttpRequest(getContext(), ROOT_GROUP_VISIT_GET, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (isDebug) Slog.d(TAG, "==========response body : " + response.body());
                
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        //if (isDebug) Slog.d(TAG, "==========response text 1: " + responseText);
                        try {
                            JSONObject responseObj = new JSONObject(responseText);
                            if (responseObj != null) {
                                visitRecord = responseObj.optInt("visitRecord");
                                myHandler.sendEmptyMessage(GET_VISIT_DONE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    private void addVisit(){
        final RequestBody requestBody = new FormBody.Builder().add("type", String.valueOf(type)).build();
        HttpUtil.sendOkHttpRequest(getContext(), ROOT_GROUP_VISIT_ADD, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (isDebug) Slog.d(TAG, "==========response body : " + response.body());
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        //if (isDebug) Slog.d(TAG, "==========response text 1: " + responseText);
                        try {
                            JSONObject responseObj = new JSONObject(responseText);
                            if (responseObj != null) {

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //dismissProgressDialog();
                        //mDialog.dismiss();
                    }
                }
            }
            
             @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void handleMessage(Message message){
        switch (message.what){
            case GET_FOLLOW_DONE:
                if (isFollowed == 1){
                    follow.setText("已关注");
                    follow.setClickable(false);
                }
                followCountTV.setText("关注 "+followCount);
                break;
                case GET_VISIT_DONE:
                visit.setText("访问 "+visitRecord);
                addVisit();
                break;
            case ADD_FOLLOW_DONE:
                follow.setText("已关注");
                follow.setClickable(false);
                followCountTV.setText("关注 "+followCount);
                break;
                default:
                    break;
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
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
        WeakReference<PreparationDialogFragment> preparationDialogFragmentWeakReference;

        MyHandler(PreparationDialogFragment preparationDialogFragment) {
            preparationDialogFragmentWeakReference = new WeakReference<>(preparationDialogFragment);
        }

        @Override
        public void handleMessage(Message message) {
            PreparationDialogFragment preparationDialogFragment = preparationDialogFragmentWeakReference.get();
            if (preparationDialogFragment != null) {
                preparationDialogFragment.handleMessage(message);
            }
        }
    }


}
