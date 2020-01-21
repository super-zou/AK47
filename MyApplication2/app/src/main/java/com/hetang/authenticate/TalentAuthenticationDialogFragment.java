package com.hetang.authenticate;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.common.MyApplication;
import com.hetang.common.SetAvatarActivity;
import com.hetang.util.BaseDialogFragment;
import com.hetang.util.CommonDialogFragmentInterface;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.RoundImageView;
import com.hetang.util.Slog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hetang.archive.ArchiveFragment.REQUESTCODE;
import static com.hetang.common.SetAvatarActivity.SUBMIT_TALENT_AUTHENTICATION_ACTION_BROADCAST;
import static com.hetang.common.SetAvatarActivity.TALENT_AUTHENTICATION_PHOTO;

public class TalentAuthenticationDialogFragment extends BaseDialogFragment {
    public final static int TALENT_AUTHENTICATION_RESULT_OK = 0;
    private static final boolean isDebug = true;
    private static final String TAG = "TalentAuthenticationDialogFragment";
    private static final String SUBMIT_URL = HttpUtil.DOMAIN + "?q=talent/become/apply";
    private int type;
    private int gid;
    private Dialog mDialog;
    private Context mContext;
    private String uri;
    private boolean isSubmit = false;
    private EditText introductionET;
    private RoundImageView rewardQRCode;
    private QRCodeSetBroadcastReceiver mReceiver;
    private CommonDialogFragmentInterface commonDialogFragmentInterface;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        try {
            commonDialogFragmentInterface = (CommonDialogFragmentInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement commonDialogFragmentInterface");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_Design_BottomSheetDialog);
        //mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.submit_talent_authentication);

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);
        mDialog.setCanceledOnTouchOutside(true);
        Window window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        Bundle bundle = getArguments();
        if (bundle != null) {
            type = bundle.getInt("type", 0);
        }
        //subGroup = (SubGroupActivity.SubGroup) bundle.getSerializable("subGroup");
        TextView leftBack = mDialog.findViewById(R.id.left_back);
        leftBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        TextView title = mDialog.findViewById(R.id.title);
        title.setText("提交达人申请");
        Button save = mDialog.findViewById(R.id.submit);
        Button cancel = mDialog.findViewById(R.id.cancel);
        save.setVisibility(View.VISIBLE);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkValid()) {
                    submit();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        introductionET = mDialog.findViewById(R.id.introduction_edittext);
        rewardQRCode = mDialog.findViewById(R.id.reward_qr_code);

        rewardQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SetAvatarActivity.class);
                intent.putExtra("type", TALENT_AUTHENTICATION_PHOTO);
                getActivity().startActivityForResult(intent, REQUESTCODE);
            }
        });

        registerBroadcast();

        return mDialog;
    }

    private void submit() {
        showProgressDialog("");
        FormBody.Builder builder = new FormBody.Builder()
                .add("introduction", introductionET.getText().toString())
                .add("uri", uri)
                .add("type", String.valueOf(type));

        RequestBody requestBody = builder.build();

        HttpUtil.sendOkHttpRequest(mContext, SUBMIT_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "saveUserInfo response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        gid = new JSONObject(responseText).optInt("gid");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    isSubmit = true;
                    dismissProgressDialog();
                    mDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });


    }

    private boolean checkValid() {
        if (TextUtils.isEmpty(introductionET.getText().toString())) {
            Toast.makeText(getContext(), "达人介绍不能为空", Toast.LENGTH_LONG).show();
            return false;
        }

        if (TextUtils.isEmpty(uri)) {
            Toast.makeText(getContext(), "请上传赞赏二维码", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }


    private JSONObject getTalentJsonObject() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("introduction", introductionET.getText().toString());
            jsonObject.put("uri", uri);
            jsonObject.put("type", type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;

    }

    //register local broadcast to receive DYNAMICS_ADD_BROADCAST
    private void registerBroadcast() {
        mReceiver = new QRCodeSetBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SUBMIT_TALENT_AUTHENTICATION_ACTION_BROADCAST);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, intentFilter);
    }

    //unregister local broadcast
    private void unRegisterBroadcast() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
        unRegisterBroadcast();
        if (commonDialogFragmentInterface != null) {//callback from ArchivesActivity class
            commonDialogFragmentInterface.onBackFromDialog(TALENT_AUTHENTICATION_RESULT_OK, gid, isSubmit);
        }
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
    }

    private class QRCodeSetBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case SUBMIT_TALENT_AUTHENTICATION_ACTION_BROADCAST:
                    uri = intent.getStringExtra("uri");
                    Glide.with(getContext()).load(HttpUtil.DOMAIN + uri).into(rewardQRCode);
                    break;
                default:
                    break;
            }
        }
    }
}
