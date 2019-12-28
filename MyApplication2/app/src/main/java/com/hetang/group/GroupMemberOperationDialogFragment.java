package com.hetang.group;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.hetang.R;
import com.hetang.common.MyApplication;
import com.hetang.util.BaseDialogFragment;
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

import static com.hetang.home.CommonContactsActivity.BLOCK_MEMBER_BROADCAST;
import static com.hetang.home.CommonContactsActivity.DELETE_MEMBER_BROADCAST;
import static com.hetang.home.CommonContactsActivity.UNBLOCK_MEMBER_BROADCAST;

/**
 * Created by super-zou on 18-9-9.
 */
 
 public class GroupMemberOperationDialogFragment extends BaseDialogFragment {
    private Dialog mDialog;
    private static final boolean isDebug = true;
    private static final String TAG = "GroupMemberOperationDialogFragment";
    private static final String GROUP_MEMBER_OPERATION = HttpUtil.DOMAIN + "?q=subgroup/member/operation/";
    private Context mContext;
    private int gid;
    private int uid;
    private int status;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_Design_BottomSheetDialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.group_member_operation);

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);
        mDialog.setCanceledOnTouchOutside(true);
        Window window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);
        
        final Bundle bundle = getArguments();
        gid = bundle.getInt("gid", 0);
        uid = bundle.getInt("uid", 0);
        status = bundle.getInt("status", -1);

        TextView block = mDialog.findViewById(R.id.block);
        if (status == 2){
            block.setText("解禁");
        }
        
        block.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (status != 2){
                    new AlertDialog.Builder(mContext)
                            .setTitle(mContext.getResources().getString(R.string.block))
                            .setMessage(mContext.getResources().getString(R.string.block_group_member_query_text))
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    operationGroupMember("block");
                                }
                            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mDialog.dismiss();
                        }
                    }).create().show();
                }else {
                    operationGroupMember("unblock");
                }
            }
        });


        TextView delete = mDialog.findViewById(R.id.delete);
        
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(mContext)
                        .setTitle(mContext.getResources().getString(R.string.delete))
                        .setMessage(mContext.getResources().getString(R.string.delete_group_member_query_text))
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                operationGroupMember("delete");
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDialog.dismiss();
                    }
                }).create().show();
        }
        });


        return mDialog;
    }

    public void operationGroupMember(final String operation) {

        showProgressDialog("");
        final RequestBody requestBody = new FormBody.Builder()
                .add("gid", String.valueOf(gid))
                .add("uid", String.valueOf(uid))
                .build();
                
                String uri;
        if (operation.equals("block")){
            uri = GROUP_MEMBER_OPERATION+"block";
        }else if (operation.equals("delete")){
            uri = GROUP_MEMBER_OPERATION+"delete";
        }else {
            uri = GROUP_MEMBER_OPERATION+"unblock";
        }

        HttpUtil.sendOkHttpRequest(getContext(), uri, requestBody, new Callback() {
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
                                int result = responseObj.optInt("result");
                                if (result > 0) {
                                    sendBroadcast(operation);
                                    dismissProgressDialog();
                                }
                            }
                        } catch (JSONException e) {
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

    private void sendBroadcast(String operation) {
        Intent intent;
        if (operation.equals("block")){
            intent = new Intent(BLOCK_MEMBER_BROADCAST);
            }else if (operation.equals("delete")){
            intent = new Intent(DELETE_MEMBER_BROADCAST);
        }else {
            intent = new Intent(UNBLOCK_MEMBER_BROADCAST);
        }
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
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
