package com.mufu.group;

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

import com.mufu.R;
import com.mufu.common.MyApplication;
import com.mufu.util.BaseDialogFragment;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.Slog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.mufu.group.SubGroupDetailsActivity.EXIT_GROUP_BROADCAST;

/**
 * Created by super-zou on 18-9-9.
 */

public class SubGroupOperationDialogFragment extends BaseDialogFragment {
    private Dialog mDialog;
    private static final boolean isDebug = true;
    private static final String TAG = "DynamicOperationDialogFragment";
    private static final String QUIT_GROUP = HttpUtil.DOMAIN + "?q=subgroup/quit";
    private Context mContext;
    private SubGroupActivity.SubGroup subGroup;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_Design_BottomSheetDialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.subgroup_operation);

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
        subGroup = (SubGroupActivity.SubGroup) bundle.getSerializable("subGroup");

        TextView setting = mDialog.findViewById(R.id.setting);
        if (subGroup.isLeader){
            setting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundleModify = new Bundle();
                    bundleModify.putSerializable("subgroup", subGroup);
                    bundleModify.putBoolean("isModify", true);
                    CreateSubGroupDialogFragment createSubGroupDialogFragment = new CreateSubGroupDialogFragment();
                    createSubGroupDialogFragment.setArguments(bundleModify);
                    createSubGroupDialogFragment.show(getFragmentManager(), "CreateSubGroupDialogFragment");
                    mDialog.dismiss();
                }
            });
        }else {
            setting.setClickable(false);
            setting.setTextColor(mContext.getResources().getColor(R.color.color_disabled));
        }

        TextView exit = mDialog.findViewById(R.id.exit);
        if (subGroup.authorStatus == 1){
            exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(mContext)
                            .setMessage(mContext.getResources().getString(R.string.exit_group_query_text))
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    exitGroup(subGroup.gid);
                                }
                            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mDialog.dismiss();
                        }
                    }).create().show();
                }
            });
        }else {
            exit.setClickable(false);
            exit.setTextColor(mContext.getResources().getColor(R.color.color_disabled));
        }

        return mDialog;
    }

    public void exitGroup(int gid) {

        showProgressDialog("正在退出");
        final RequestBody requestBody = new FormBody.Builder()
                .add("gid", String.valueOf(gid))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), QUIT_GROUP, requestBody, new Callback() {
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
                                    sendBroadcast();
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

    private void sendBroadcast() {
        Intent intent = new Intent(EXIT_GROUP_BROADCAST);
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