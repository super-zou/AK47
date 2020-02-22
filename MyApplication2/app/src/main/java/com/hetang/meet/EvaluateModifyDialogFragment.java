package com.hetang.meet;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hetang.R;
import com.hetang.common.HandlerTemp;
import com.hetang.common.OnBackFromDialogInterFace;
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

import androidx.fragment.app.DialogFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.view.Gravity.CENTER;
import static com.hetang.archive.ArchiveFragment.REQUESTCODE;
import static com.hetang.main.MeetArchiveFragment.RESULT_OK;

public class EvaluateModifyDialogFragment extends DialogFragment {
    private static final String TAG = "EvaluateModifyDialogFragment";
    public static final int MODIFY_EVALUATE_DONE = 0;
    private static final String MODIFY_EVALUATION_URL = HttpUtil.DOMAIN + "?q=meet/evaluation/modify";
    final List<String> selectedFeatures = new ArrayList<>();
    final List<String> featureList = new ArrayList<>();
    private Context mContext;
    private Dialog mDialog;
    private View view;
    private int uid;
    private boolean mEvaluated = false;
    private LayoutInflater inflater;
    private List<String> impressionList = new ArrayList<>();
    private ProgressDialog progressDialog;
    private String mFeatures;
    private String[] featureArray;
    private float currentRating;
    private boolean featureModified = false;
    private boolean ratingModified = false;
    private EvaluatorDetailsActivity.EvaluatorDetails evaluatorDetails;
    private OnBackFromDialogInterFace onBackFromDialogInterFace;
    public static final String EVALUATE_MODIFY_ACTION_BROADCAST = "com.hetang.action.EVALUATE_MODIFY";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;

        try {
            onBackFromDialogInterFace = (OnBackFromDialogInterFace) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement commonDialogFragmentInterface");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        float rating = 0;
        Bundle bundle = getArguments();
        if (bundle != null) {
            uid = bundle.getInt("uid");
            evaluatorDetails = (EvaluatorDetailsActivity.EvaluatorDetails)bundle.getSerializable("details");
            /*
            uid = bundle.getInt("uid");
            rating = bundle.getFloat("rating");
            mFeatures = bundle.getString("features", "");
            */
            rating = (float) evaluatorDetails.getRating();
            mFeatures = evaluatorDetails.getFeatures();
            currentRating = (float) evaluatorDetails.getRating();
        }
        inflater = LayoutInflater.from(mContext);
        mDialog = new Dialog(mContext, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        view = inflater.inflate(R.layout.evaluation_modify_dialog, null);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(view);
        mDialog.setCanceledOnTouchOutside(true);
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        //layoutParams.alpha = 0.9f;
        layoutParams.gravity = Gravity.TOP;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setDimAmount(0.8f);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        window.setAttributes(layoutParams);

        setImpressions(rating);

        return mDialog;
    }

    private void setImpressions(final float rating) {
        ScaleRatingBar scaleRatingBar = view.findViewById(R.id.charm_rating_bar);
        final TextView charmRating = view.findViewById(R.id.charm_rating);
        final FlowLayout featuresFL = view.findViewById(R.id.featuresFL);
        final FlowLayout diyFeatures = view.findViewById(R.id.diy_features);

        featureArray = mFeatures.split("#");
        for (int i = 0; i < featureArray.length; i++) {
            TextView textView = new TextView(getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            textView.setText(featureArray[i]);
            textView.setGravity(CENTER);
            textView.setPadding((int)Utility.dpToPx(getContext(), 5f),
                                (int)Utility.dpToPx(getContext(), 5f),
                                (int)Utility.dpToPx(getContext(), 5f),
                               (int)Utility.dpToPx(getContext(), 5f));
            textView.setTextColor(getContext().getResources().getColor(R.color.white));
            textView.setTextSize(16f);
            textView.setBackground(getResources().getDrawable(R.drawable.btn_big_radius_primary));
            layoutParams.setMargins((int)Utility.dpToPx(getContext(), 5f), (int)Utility.dpToPx(getContext(), 5f), 0, 0);
            textView.setLayoutParams(layoutParams);
            featuresFL.addView(textView, i);

            textView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    for (int n=0; n<featuresFL.getChildCount(); n++){
                        TextView childView = (TextView)featuresFL.getChildAt(n);
                        if (childView.getText().toString().equals(textView.getText().toString())){
                            featureArray[n] = "";
                            featureModified = true;
                        }
                    }
                    textView.setVisibility(View.GONE);
                    return false;
                }
            });

        }

        scaleRatingBar.setRating(rating);
        charmRating.setText(String.valueOf(rating));

        scaleRatingBar.setOnRatingChangeListener(new BaseRatingBar.OnRatingChangeListener() {

            @Override
            public void onRatingChange(BaseRatingBar ratingBar, float rating) {
                Slog.d(TAG, "onRatingChange float: " + rating);
                charmRating.setText(String.valueOf(rating));
            }

        });

        Button addDiyFeature = view.findViewById(R.id.add_feature);
        Button saveFeatures = view.findViewById(R.id.save_features);
        final TextView errorNotice = view.findViewById(R.id.error_notice);
        final EditText featureInput = view.findViewById(R.id.feature_input);
        addDiyFeature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(featureInput.getText().toString())) {
                    TextView diyTextView = new TextView(getContext());
                    //FlowLayout.LayoutParams layoutParams = new FlowLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    diyTextView.setPadding((int) Utility.dpToPx(mContext, 8), (int) Utility.dpToPx(mContext, 8),
                            (int) Utility.dpToPx(mContext, 8), (int) Utility.dpToPx(mContext, 8));
                    diyTextView.setText(featureInput.getText());
                    diyTextView.setGravity(CENTER);
                    diyTextView.setBackground(getContext().getDrawable(R.drawable.label_selected_bg));
                    diyFeatures.addView(diyTextView);
                    diyFeatures.setVisibility(View.VISIBLE);
                    selectedFeatures.add(featureInput.getText().toString());
                    featureInput.setText("");

                    featureModified = true;
                }
            }
        });

        saveFeatures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String features = "";
                Slog.d(TAG, "selected features size: " + selectedFeatures.size());
                if (selectedFeatures.size() > 0) {
                    for (String feature : selectedFeatures) {
                        features += feature + "#";
                    }
                    //Slog.d(TAG, "selected features: " + features);
                    mEvaluated = true;
                }

                if (featureArray != null && featureArray.length > 0){
                    for (int i=0; i<featureArray.length; i++){
                        if (!TextUtils.isEmpty(featureArray[i])){
                            features += featureArray[i] + "#";
                        }
                    }
                }
                float rating = 0;

                if (charmRating.getText().toString() != null) {
                    rating = Float.parseFloat(charmRating.getText().toString());
                }

                uploadToServer(features, rating, uid);
            }
        });
    }

    private void uploadToServer(String features, float rating, int uid) {

        showProgress(mContext);
        FormBody.Builder builder = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .add("rid", String.valueOf(evaluatorDetails.getRid()));

        if (Math.abs(rating - currentRating) > 0){
            ratingModified = true;
            builder.add("rating", String.valueOf(rating));
        }

        if (featureModified){
            if (features != null && features.length() > 0) {
                builder.add("features", features);
            }
        }

        RequestBody requestBody = builder.build();

        HttpUtil.sendOkHttpRequest(getContext(), MODIFY_EVALUATION_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if(response.body() != null){
                    String responseText = response.body().string();
                    Slog.d(TAG, "------------------------->responseText: "+responseText);
                    if(responseText != null){
                        try {
                            int result = new JSONObject(responseText).optInt("result");
                            float average = (float) new JSONObject(responseText).optDouble("average");
                            if (result == 1){
                                //handler.sendEmptyMessage(LOAD_REFERENCE_DONE);
                                if (onBackFromDialogInterFace != null) {//callback from ArchivesActivity class
                                    Bundle bundle = new Bundle();
                                    bundle.putBoolean("status", true);
                                    if (ratingModified && average > 0){
                                        bundle.putFloat("rating", average);
                                        sendBroadCast(average);
                                    }

                                    if (featureModified){
                                        bundle.putString("features", features);
                                    }
                                    onBackFromDialogInterFace.onBackFromDialog(MODIFY_EVALUATE_DONE, bundle);
                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                dismiss();
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });

    }

    private void sendBroadCast(float score){
        Intent intent = new Intent(EVALUATE_MODIFY_ACTION_BROADCAST);
        intent.putExtra("score", score);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }


    public void setImpressionsView() {
        LinearLayout approvedFeaturesLabel = view.findViewById(R.id.approved_features_label);
        FlowLayout approvedFeatures = view.findViewById(R.id.features_approved);
        approvedFeaturesLabel.setVisibility(View.VISIBLE);
        approvedFeatures.setVisibility(View.VISIBLE);
        for (int i = 0; i < impressionList.size(); i++) {
            TextView approvedTextView = new TextView(getContext());
            approvedTextView.setPadding((int) Utility.dpToPx(mContext, 8), (int) Utility.dpToPx(mContext, 8),
                    (int) Utility.dpToPx(mContext, 8), (int) Utility.dpToPx(mContext, 8));
            approvedTextView.setText(impressionList.get(i));
            approvedTextView.setGravity(Gravity.CENTER);
            approvedTextView.setBackground(getContext().getDrawable(R.drawable.label_bg));
            approvedFeatures.addView(approvedTextView);
        }
        //selectFeatures(approvedFeatures);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //KeyboardUtils.hideSoftInput(getContext());

        if (getTargetFragment() != null){
            Intent intent = new Intent();
            intent.putExtra("type", ParseUtils.TYPE_EVALUATE);
            intent.putExtra("status", mEvaluated);
            getTargetFragment().onActivityResult(REQUESTCODE, RESULT_OK, intent);
        }

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
