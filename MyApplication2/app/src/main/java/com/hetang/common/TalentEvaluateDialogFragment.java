package com.hetang.common;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hetang.R;
import com.hetang.util.CommonDialogFragmentInterface;
import com.hetang.util.HttpUtil;
import com.hetang.util.ParseUtils;
import com.hetang.util.Slog;
import com.hetang.util.Utility;
import com.nex3z.flowlayout.FlowLayout;
import com.willy.ratingbar.BaseRatingBar;
import com.willy.ratingbar.ScaleRatingBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.view.Gravity.CENTER;
import static com.hetang.archive.ArchiveFragment.REQUESTCODE;
import static com.hetang.archive.ArchiveFragment.SET_BLOG_RESULT_OK;
import static com.hetang.main.MeetArchiveFragment.RESULT_OK;

public class TalentEvaluateDialogFragment extends DialogFragment {
    private static final String TAG = "TalentEvaluateDialogFragment";
    private static final int IMPRESSION_PARSE_DONE = 0;
    private static final String SET_EVALUATION_URL = HttpUtil.DOMAIN + "?q=talent/evaluation/set";
    final List<String> selectedFeatures = new ArrayList<>();
    private Context mContext;
    private Dialog mDialog;
    private View view;
    private boolean mEvaluated = false;
    private LayoutInflater inflater;
    private Handler handler = new TalentEvaluateDialogFragment.MyHandler(this);
    private List<String> impressionList = new ArrayList<>();
    private ProgressDialog progressDialog;
    
    private int uid;
    private int type;
        private int gid;
    private static final int WRITE_EVALUATE_DONE = 0;
    public final static int SET_EVALUATE_RESULT_OK = 7;
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
        Bundle bundle = getArguments();
        if (bundle != null) {
            uid = bundle.getInt("uid");
            type = bundle.getInt("type");
                        gid = bundle.getInt("gid");
        }
        inflater = LayoutInflater.from(mContext);
        mDialog = new Dialog(mContext, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        view = inflater.inflate(R.layout.talent_evaluation_dialog, null);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(view);
        mDialog.setCanceledOnTouchOutside(true);
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.TOP;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setDimAmount(0.8f);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        window.setAttributes(layoutParams);

        setImpressions();

        return mDialog;
    }
    
     private void setImpressions() {
        ScaleRatingBar scaleRatingBar = view.findViewById(R.id.charm_rating_bar);
        final TextView charmRating = view.findViewById(R.id.charm_rating);

        scaleRatingBar.setOnRatingChangeListener(new BaseRatingBar.OnRatingChangeListener() {

            @Override
            public void onRatingChange(BaseRatingBar ratingBar, float rating) {
                Slog.d(TAG, "onRatingChange float: " + rating);
                charmRating.setText(String.valueOf(rating));
            }

        });
        
         Button saveEvaluate = view.findViewById(R.id.save_evaluate);
        final TextView errorNotice = view.findViewById(R.id.error_notice);
        final EditText contentET = view.findViewById(R.id.content);

        saveEvaluate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float rating = 0;

                if (charmRating.getText().toString() != null && !"".equals(charmRating.getText().toString())) {
                    rating = Float.parseFloat(charmRating.getText().toString());
                    mEvaluated = true;
                } else {
                    errorNotice.setVisibility(View.VISIBLE);
                    return;
                }

                String content = contentET.getText().toString();
                uploadToServer(content, rating);
            }
        });
    }
    
    private void uploadToServer(String content, float rating) {

        showProgress(mContext);
        FormBody.Builder builder = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .add("type", String.valueOf(type))
                .add("content", content)
                            .add("gid", String.valueOf(gid))
                .add("rating", String.valueOf(rating));

        RequestBody requestBody = builder.build();
        
        HttpUtil.sendOkHttpRequest(getContext(), SET_EVALUATION_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if(response.body() != null){
                    String responseText = response.body().string();
                    Slog.d(TAG, "==========loadReferences response text : "+responseText);
                    if(responseText != null){
                        mEvaluated = true;
                        handler.sendEmptyMessage(WRITE_EVALUATE_DONE);
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });

    }


public void handleMessage(Message message) {
        switch (message.what) {
            case WRITE_EVALUATE_DONE:
                if (commonDialogFragmentInterface != null) {//callback from ArchivesActivity class
                    commonDialogFragmentInterface.onBackFromDialog(SET_EVALUATE_RESULT_OK, 0, mEvaluated);
                }
                mDialog.dismiss();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //KeyboardUtils.hideSoftInput(getContext());

        closeProgressDialog();

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

    static class MyHandler extends HandlerTemp<TalentEvaluateDialogFragment> {
        public MyHandler(TalentEvaluateDialogFragment cls) {
            super(cls);
        }

        @Override
        public void handleMessage(Message message) {
            TalentEvaluateDialogFragment talentEvaluateDialogFragment = ref.get();
            if (talentEvaluateDialogFragment != null) {
                talentEvaluateDialogFragment.handleMessage(message);
            }
        }
    }
    
    private void showProgress(Context context) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(context.getResources().getString(R.string.saving_progress));
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
