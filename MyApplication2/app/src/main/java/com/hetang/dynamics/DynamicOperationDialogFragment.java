package com.hetang.dynamics;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.hetang.R;
import com.hetang.common.MyApplication;
import com.hetang.util.BaseDialogFragment;
import com.hetang.util.CommonDialogFragmentInterface;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.Slog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hetang.explore.ShareFragment.DYNAMICS_DELETE_BROADCAST;
import static com.hetang.explore.ShareFragment.REQUEST_CODE;

/**
 * Created by super-zou on 18-9-9.
 */
 
 public class DynamicOperationDialogFragment extends BaseDialogFragment {
    private Dialog mDialog;
    private static final boolean isDebug = true;
    private static final String TAG = "DynamicOperationDialogFragment";
    private static final String DELETE_DYNAMIC = HttpUtil.DOMAIN + "?q=dynamic/action/delete";
    private Context mContext;
      private CommonDialogFragmentInterface commonDialogFragmentInterface;
    public static final int DYNAMIC_OPERATION_RESULT = 4;
    private boolean isDeleted = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
             commonDialogFragmentInterface = (CommonDialogFragmentInterface) context;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_Design_BottomSheetDialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.dynamic_operation);

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);
        mDialog.setCanceledOnTouchOutside(true);
        Window window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);
        
        TextView delete = mDialog.findViewById(R.id.delete);

        final Bundle bundle = getArguments();

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(mContext)
                        .setMessage(mContext.getResources().getString(R.string.delete_query_text))
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteDynamic(bundle.getLong("did"));
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mDialog.dismiss();
                            }
                        })
                        .create().show();
            }
        });

        return mDialog;
    }
    
    public void deleteDynamic(final long did){

        showProgressDialog("正在删除");
        final RequestBody requestBody = new FormBody.Builder()
                .add("did", String.valueOf(did))
                .build();
                
        HttpUtil.sendOkHttpRequest(getContext(), DELETE_DYNAMIC, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(isDebug) Slog.d(TAG, "==========response body : " + response.body());
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if(isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                     try {
                         JSONObject responseObj = new JSONObject(responseText);
                         if (responseObj != null){
                             int result = responseObj.optInt("result");
                             if (result > 0){
                                 //sendBroadcast();
                                                               isDeleted = true;
                                 back2Caller((int)did);
                                 dismissProgressDialog();
                             }
                         }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                        mDialog.dismiss();
                    }
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {

            }
        });

    }
    
    private void sendBroadcast() {
        Intent intent = new Intent(DYNAMICS_DELETE_BROADCAST);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }
  
      private void back2Caller(int result){
        if (getTargetFragment() != null){
            getTargetFragment().onActivityResult(REQUEST_CODE, DYNAMIC_OPERATION_RESULT, null);
        }else {
            if (commonDialogFragmentInterface != null) {//callback from ArchivesActivity class
                commonDialogFragmentInterface.onBackFromDialog(DYNAMIC_OPERATION_RESULT, result, isDeleted);
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
}
