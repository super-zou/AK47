package com.hetang.meet;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.nex3z.flowlayout.FlowLayout;
import com.hetang.launchak47.R;
import com.hetang.util.HttpUtil;
import com.hetang.util.Slog;
import com.hetang.util.Utility;
import com.willy.ratingbar.BaseRatingBar;
import com.willy.ratingbar.ScaleRatingBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EvaluateDialogFragment extends DialogFragment {
    private static final String TAG = "EvaluateDialogFragment";
    private static final int IMPRESSION_PARSE_DONE = 0;
   // private static final String SET_IMPRESSION_URL = HttpUtil.DOMAIN + "?q=meet/impression/set";
    private static final String SET_EVALUATION_URL = HttpUtil.DOMAIN + "?q=meet/evaluation/set";
    private static final String GET_IMPRESSION_STATISTICS_URL = HttpUtil.DOMAIN + "?q=meet/impression/statistics";
    final List<String> selectedFeatures = new ArrayList<>();
    private Context mContext;
    private Dialog mDialog;
    private View view;
    private boolean mEvaluated = false;
    private EvaluateDialogFragmentListener evaluateDialogFragmentListener;
    private LayoutInflater inflater;
    private Handler handler = new EvaluateDialogFragment.MyHandler(this);
    private List<String> impressionList = new ArrayList<>();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        try {
            evaluateDialogFragmentListener = (EvaluateDialogFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement evaluateDialogFragmentListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int uid = -1;
        int sex = 0;
        Bundle bundle = getArguments();
        if (bundle != null) {
            uid = bundle.getInt("uid");
            sex = bundle.getInt("sex");
        }
        inflater = LayoutInflater.from(mContext);
        mDialog = new Dialog(mContext, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        view = inflater.inflate(R.layout.evaluation_dialog, null);
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

        setImpressions(uid, sex);

        return mDialog;
    }

    private void setImpressions(final int uid, final int sex) {
        ScaleRatingBar scaleRatingBar = view.findViewById(R.id.charm_rating_bar);
        final TextView charmRating = view.findViewById(R.id.charm_rating);
        final FlowLayout maleFeatures = view.findViewById(R.id.male_features);
        final FlowLayout femaleFeatures = view.findViewById(R.id.female_features);
        final FlowLayout diyFeatures = view.findViewById(R.id.diy_features);

        loadApprovedImpressions(uid);

        if (sex == 0) {//display male features
            maleFeatures.setVisibility(View.VISIBLE);
        } else {//display female features
            femaleFeatures.setVisibility(View.VISIBLE);
        }

        scaleRatingBar.setOnRatingChangeListener(new BaseRatingBar.OnRatingChangeListener() {

            @Override
            public void onRatingChange(BaseRatingBar ratingBar, float rating) {
                Slog.d(TAG, "onRatingChange float: " + rating);
                charmRating.setText(String.valueOf(rating));
            }

        });

        if (sex == 0) {
            selectFeatures(maleFeatures);
        } else {
            selectFeatures(femaleFeatures);
        }

        Button addDiyFeature = view.findViewById(R.id.add_feature);
        Button saveFeatures = view.findViewById(R.id.save_features);
        final TextView errorNotice = view.findViewById(R.id.error_notice);
        final EditText featureInput = view.findViewById(R.id.feature_input);
        addDiyFeature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!"".equals(featureInput.getText())) {
                    TextView diyTextView = new TextView(getContext());
                    //FlowLayout.LayoutParams layoutParams = new FlowLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    diyTextView.setPadding((int) Utility.dpToPx(mContext, 8), (int) Utility.dpToPx(mContext, 8),
                            (int) Utility.dpToPx(mContext, 8), (int) Utility.dpToPx(mContext, 8));
                    diyTextView.setText(featureInput.getText());
                    diyTextView.setGravity(Gravity.CENTER);
                    diyTextView.setBackground(getContext().getDrawable(R.drawable.label_selected_bg));
                    diyFeatures.addView(diyTextView);
                    diyFeatures.setVisibility(View.VISIBLE);
                    selectedFeatures.add(featureInput.getText().toString());
                    featureInput.setText("");
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
                    Slog.d(TAG, "selected features: " + features);
                    mEvaluated = true;
                }
                float rating = 0;
                if (charmRating.getText().toString() != null && !"".equals(charmRating.getText().toString())) {
                    rating = Float.parseFloat(charmRating.getText().toString());
                    mEvaluated = true;
                } else {
                    errorNotice.setVisibility(View.VISIBLE);
                    return;
                }
                dismiss();
                uploadToServer(features, rating, uid);
            }
        });
    }

    private void uploadToServer(String features, float rating, int uid) {

        FormBody.Builder builder = new FormBody.Builder()
                                                .add("uid", String.valueOf(uid))
                                                .add("rating", String.valueOf(rating));
        if(features != null && features.length() > 0){
            builder.add("features", features);
        }
        /*
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .add("rating", String.valueOf(rating))
                .add("features", features).build();
                */
        RequestBody requestBody = builder.build();

        HttpUtil.sendOkHttpRequest(getContext(), SET_EVALUATION_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                /*
                if(response.body() != null){
                    String responseText = response.body().string();
                    Slog.d(TAG, "==========loadReferences response text : "+responseText);
                    if(responseText != null){
                        List<MeetReferenceInfo> meetReferenceInfoList = ParseUtils.getMeetReferenceList(responseText);
                        if(meetReferenceInfoList != null && meetReferenceInfoList.size() > 0){
                            mReferenceList.addAll(meetReferenceInfoList);
                        }
                        handler.sendEmptyMessage(LOAD_REFERENCE_DONE);
                    }
                }
                */
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });

    }

    private void loadApprovedImpressions(final int uid) {

        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(mContext, GET_IMPRESSION_STATISTICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    Slog.d(TAG, "==========loadApprovedImpressions response: " + responseText);

                    if (responseText != null) {
                        if (!TextUtils.isEmpty(responseText)) {
                            parseImpressions(responseText);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    public void parseImpressions(String responseText) {

        JSONObject responseObj = null;
        try {
            responseObj = new JSONObject(responseText);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (responseObj != null) {
            JSONObject impressionStatisticsObj = responseObj.optJSONObject("features_statistics");
            if (impressionStatisticsObj != null) {
                Iterator iterator = impressionStatisticsObj.keys();
                int index = 0;
                while (iterator.hasNext()) {
                    index++;
                    String key = (String) iterator.next();
                    Slog.d(TAG, "==============key: " + key);
                    impressionList.add(key);
                    if (index == 7) {
                        break;
                    }
                }
                Message msg = handler.obtainMessage();
                msg.what = IMPRESSION_PARSE_DONE;
                handler.sendMessage(msg);
            }
        }
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
        selectFeatures(approvedFeatures);

    }

    private void selectFeatures(FlowLayout featuresLayout) {
        for (int i = 0; i < featuresLayout.getChildCount(); i++) {
            final TextView feature = (TextView) featuresLayout.getChildAt(i);
            feature.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Slog.d(TAG, "===========get text: " + feature.getText() + " tag: " + feature.getTag());
                    if (null != feature.getTag() && feature.getTag().equals("selected")) {
                        feature.setBackground(getContext().getDrawable(R.drawable.label_bg));
                        feature.setTag(null);
                        selectedFeatures.remove(feature.getText().toString());
                    } else {
                        feature.setBackground(getContext().getDrawable(R.drawable.label_selected_bg));
                        feature.setTag("selected");
                        selectedFeatures.add(feature.getText().toString());
                    }
                }
            });
        }
    }

    public void handleMessage(Message message) {
        switch (message.what) {
            case IMPRESSION_PARSE_DONE:
                setImpressionsView();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //KeyboardUtils.hideSoftInput(getContext());
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
        if (evaluateDialogFragmentListener != null) {//callback from ArchivesActivity class
            evaluateDialogFragmentListener.onBackFromRatingAndImpressionDialogFragment(mEvaluated);
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

    //When the dialog destried  the function will be called to transmit data to ArchivesActivity
    public interface EvaluateDialogFragmentListener {
        //evaluated set true if user rating or set impression, or else set false
        void onBackFromRatingAndImpressionDialogFragment(boolean evaluated);
    }

    static class MyHandler extends Handler {
        WeakReference<EvaluateDialogFragment> evaluateDialogFragmentWeakReference;

        MyHandler(EvaluateDialogFragment evaluateDialogFragment) {
            evaluateDialogFragmentWeakReference = new WeakReference<EvaluateDialogFragment>(evaluateDialogFragment);
        }

        @Override
        public void handleMessage(Message message) {
            EvaluateDialogFragment evaluateDialogFragment = evaluateDialogFragmentWeakReference.get();
            if (evaluateDialogFragment != null) {
                evaluateDialogFragment.handleMessage(message);
            }
        }
    }

}
