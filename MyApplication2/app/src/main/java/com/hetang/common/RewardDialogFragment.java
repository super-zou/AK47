import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.util.BaseDialogFragment;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.ParseUtils;
import com.hetang.util.RoundImageView;
import com.hetang.util.Slog;
import com.hetang.util.UserProfile;

import org.angmarch.views.NiceSpinner;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hetang.archive.ArchiveFragment.GET_USER_PROFILE_DONE;
import static com.hetang.archive.ArchiveFragment.GET_USER_PROFILE_URL;
import static com.hetang.archive.ArchiveFragment.REQUESTCODE;
import static com.hetang.common.SetAvatarActivity.AUTHENTICATION_PHOTO;
import static com.hetang.common.SetAvatarActivity.AVATAR_SET_ACTION_BROADCAST;
import static com.hetang.common.SetAvatarActivity.SUBMIT_AUTHENTICATION_ACTION_BROADCAST;
import static com.hetang.util.SharedPreferencesUtils.getYunXinAccount;

public class RewardDialogFragment extends BaseDialogFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "RewardDialogFragment";
    private static final String GET_QR_CODE_URL = HttpUtil.DOMAIN + "?q=talent/get_payee_qr_code";
    private static final String WRITE_REWARD_RECORD = HttpUtil.DOMAIN + "?q=talent/write_reward_record";

    private int type;
    private Dialog mDialog;
    private Context mContext;
    private int uid;
    private String account;
    private Chat chat;
    private String qrCode;
    private static final int GET_QR_CODE_DONE = 0;

    private MyHandler handler = new MyHandler(this);
    
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
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

        Bundle bundle = getArguments();
        if (bundle != null){
            uid = bundle.getInt("uid", 0);
            account = bundle.getString("account", "");
            type = bundle.getInt("type", 0);
        }

        getRewardQRCode();
        
        TextView abandon = mDialog.findViewById(R.id.abandon);
        abandon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        TextView rewardDone = mDialog.findViewById(R.id.reward_done);
        rewardDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                  contactTalent();
            }
        });


        return mDialog;
    }
    
    private void contactTalent() {
        if (chat == null){
            chat = new Chat();
        }
        chat.processChat(getActivity(), getYunXinAccount(getActivity()), account);
        writeRewardRecord();


    }
    
    private void writeRewardRecord(){
        FormBody.Builder builder = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .add("type", String.valueOf(type));
        RequestBody requestBody = builder.build();

        HttpUtil.sendOkHttpRequest(getContext(), WRITE_REWARD_RECORD, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();

                    if (responseText != null) {
                        if (!TextUtils.isEmpty(responseText)) {
                            try {
                                boolean result = new JSONObject(responseText).optBoolean("result");
                                //handler.sendEmptyMessage(GET_QR_CODE_DONE);
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
                RoundImageView qrCodeView = mDialog.findViewById(R.id.reward_qr_code);
                Glide.with(getContext()).load(HttpUtil.DOMAIN + qrCode).into(qrCodeView);
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
