package com.hetang.talent;

import android.app.Dialog;
import android.content.Context;
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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.common.MyApplication;
import com.hetang.contacts.ChatActivity;
import com.hetang.util.BaseDialogFragment;
import com.hetang.util.CommonDialogFragmentInterface;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.RoundImageView;
import com.hetang.util.Slog;
import com.tencent.imsdk.TIMConversationType;
import com.tencent.qcloud.tim.uikit.modules.chat.base.ChatInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hetang.group.GroupFragment.eden_group;
import static com.hetang.talent.TalentAuthenticationDialogFragment.COMMON_TALENT_AUTHENTICATION_RESULT_OK;

public class RewardDialogFragment extends BaseDialogFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "RewardDialogFragment";
    public final static int COMMON_TALENT_REWARD_RESULT_OK = 1;
    private static final String GET_QR_CODE_URL = HttpUtil.DOMAIN + "?q=talent/get_payee_qr_code";
    private static final String WRITE_REWARD_RECORD = HttpUtil.DOMAIN + "?q=talent/write_reward_record";

    private int type;
    private Dialog mDialog;
    private Context mContext;
    private int uid;
    private int gid;
    private int aid;
    private String name;
    private String qrCode;
    private static final int GET_QR_CODE_DONE = 0;
    private static final int WRITE_REWARD_DONE = 1;
    private CommonDialogFragmentInterface commonDialogFragmentInterface;

    private MyHandler handler = new MyHandler(this);
    
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
        mDialog.setContentView(R.layout.reward);

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.wechat), font);
        mDialog.setCanceledOnTouchOutside(true);
        Window window = mDialog.getWindow();
        
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        TextView title = mDialog.findViewById(R.id.reward_title);
        TextView rewardContent = mDialog.findViewById(R.id.reward_content);
        TextView rewardDone = mDialog.findViewById(R.id.reward_done);

        Bundle bundle = getArguments();
        if (bundle != null){
            uid = bundle.getInt("uid", 0);
            name = bundle.getString("name", "");
            type = bundle.getInt("type", 0);

            if (type == eden_group){
                getRewardQRCode();
                gid = bundle.getInt("gid");
            }else {
                title.setText(getContext().getResources().getString(R.string.reward_consultation));
                aid = bundle.getInt("aid");
                qrCode = bundle.getString("qr_code");
                setQRCode();
                rewardContent.setVisibility(View.GONE);
                rewardDone.setText(getContext().getResources().getString(R.string.consultation));
            }
        }

        TextView abandon = mDialog.findViewById(R.id.abandon);
        abandon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        rewardDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                  //contactTalent();
                writeRewardRecord();
            }
        });


        return mDialog;
    }
    
    private void contactTalent() {
        ChatInfo chatInfo = new ChatInfo();
        chatInfo.setType(TIMConversationType.C2C);
        chatInfo.setId(String.valueOf(uid));
        chatInfo.setChatName(name);

        //chatInfo.setId();
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("CHAT_INFO", chatInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    
    private void writeRewardRecord(){
        showProgressDialog("");
        FormBody.Builder builder = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .add("type", String.valueOf(type));
        if (type == eden_group){
            builder.add("gid", String.valueOf(gid));
        }else {
            builder.add("aid", String.valueOf(aid));
        }
        RequestBody requestBody = builder.build();

        HttpUtil.sendOkHttpRequest(getContext(), WRITE_REWARD_RECORD, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    Slog.d(TAG, "-------------------------->responseText: "+responseText);
                    if (responseText != null) {
                        if (!TextUtils.isEmpty(responseText)) {
                            try {
                                int result = new JSONObject(responseText).optInt("result");
                                if (result == 1){
                                    handler.sendEmptyMessage(WRITE_REWARD_DONE);
                                    dismissProgressDialog();
                                }
                                mDialog.dismiss();
                             } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }
    
    private void getRewardQRCode() {
        FormBody.Builder builder = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .add("type", String.valueOf(type));
        RequestBody requestBody = builder.build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_QR_CODE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    
                    if (responseText != null) {
                        if (!TextUtils.isEmpty(responseText)) {
                            try {
                                qrCode = new JSONObject(responseText).optString("payee_qr_code");
                                handler.sendEmptyMessage(GET_QR_CODE_DONE);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }
    
    private void handleMessage(Message message) {
        switch (message.what) {
            case GET_QR_CODE_DONE:
                setQRCode();
                break;
            case WRITE_REWARD_DONE:
                if (commonDialogFragmentInterface != null) {//callback from ArchivesActivity class
                    commonDialogFragmentInterface.onBackFromDialog(COMMON_TALENT_REWARD_RESULT_OK, aid, true);
                }

                contactTalent();
                break;
        }
    }

    private void setQRCode(){
        RoundImageView qrCodeView = mDialog.findViewById(R.id.reward_qr_code);
        Glide.with(getContext()).load(HttpUtil.DOMAIN + qrCode).into(qrCodeView);
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
        WeakReference<RewardDialogFragment> rewardDialogFragmentWeakReference;

        MyHandler(RewardDialogFragment rewardDialogFragment) {
            rewardDialogFragmentWeakReference = new WeakReference<>(rewardDialogFragment);
        }

        @Override
        public void handleMessage(Message message) {
            RewardDialogFragment rewardDialogFragment = rewardDialogFragmentWeakReference.get();
            if (rewardDialogFragment != null) {
                rewardDialogFragment.handleMessage(message);
            }
        }
    }
}
