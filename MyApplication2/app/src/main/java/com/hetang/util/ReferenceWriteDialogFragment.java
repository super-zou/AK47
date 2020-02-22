package com.hetang.util;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.fragment.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hetang.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hetang.archive.ArchiveFragment.REQUESTCODE;
import static com.hetang.main.MeetArchiveFragment.RESULT_OK;

public class ReferenceWriteDialogFragment extends DialogFragment {
    private static final String TAG = "ReferenceWriteDialogFragment";
    private static final String WRITE_REFERENCE_URL = HttpUtil.DOMAIN + "?q=meet/reference/write";
    private Context mContext;
    private Dialog mDialog;
    private View view;
    EditText editText = null;
    private LayoutInflater inflater;
    String relation = "";
    private Handler handler;
    private static final int RESPONSE_FAILED = 0;
    private static final int RESPONSE_SUCCESS = 1;
    TextView save;
    private ProgressDialog progressDialog;
    private boolean writeDone = false;
    private boolean modify = false;
    private static int TYPE_REFERENCE = 1;
    private String content;
    private int rid;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case RESPONSE_FAILED:
                        Toast.makeText(mContext, "保存失败，请稍后再试", Toast.LENGTH_LONG).show();
                        break;
                    case RESPONSE_SUCCESS:
                        save.setEnabled(false);
                        break;
                    default:
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        int uid = -1;
        String name = "";

        inflater = LayoutInflater.from(mContext);
        mDialog = new Dialog(mContext, android.R.style.Theme_Light_NoTitleBar);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        view = inflater.inflate(R.layout.reference_write_dialog, null);
        //mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(view);
        mDialog.setCanceledOnTouchOutside(true);
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        //layoutParams.alpha = 0.9f;
        layoutParams.gravity = Gravity.TOP;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        //window.setDimAmount(0.8f);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        window.setAttributes(layoutParams);

        TextView title = view.findViewById(R.id.write_reference_title);
        Bundle bundle = getArguments();
        if (bundle != null) {
            uid = bundle.getInt("uid");
            modify = bundle.getBoolean("modify", false);
            if (modify){
                title.setText(getContext().getResources().getString(R.string.modify_reference));
                rid = bundle.getInt("rid");
                relation = bundle.getString("relation");
                content = bundle.getString("content");
                setRelationShipSelected();
                setReferenceEditContent();
            }else {
                name = bundle.getString("name");
                title.setText("给" + name + "写推荐信");
            }

        }

        initView(uid);
        return mDialog;
    }

    private void initView(final int uid) {

        editText = view.findViewById(R.id.reference_edit_text);
        save = view.findViewById(R.id.save);
        TextView cancel = view.findViewById(R.id.cancel);

        getRelationShipSelection();

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //editText.setBackground(mContext.getDrawable(R.drawable.label_btn_shape));
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0 && editText.getText().length() == 0) {
                    if (save.isEnabled()) {
                        save.setEnabled(false);
                        save.setTextColor(mContext.getResources().getColor(R.color.color_disabled));
                    }
                } else {
                    if (!save.isEnabled()) {
                        save.setEnabled(true);
                        save.setTextColor(mContext.getResources().getColor(R.color.color_blue));
                    }
                }
            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (TextUtils.isEmpty(relation)) {
                    Toast.makeText(mContext, "请选择你们的关系", Toast.LENGTH_LONG).show();
                } else {
                    uploadToServer(editText.getText().toString(), uid, relation);
                    save.setEnabled(false);
                }
                return true;
            }
        });

        if (modify){
            if (!save.isEnabled()) {
                save.setEnabled(true);
                save.setTextColor(mContext.getResources().getColor(R.color.color_blue));
            }
        }

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(editText.getText())) {
                    if (TextUtils.isEmpty(relation)) {
                        Toast.makeText(mContext, "请选择你们的关系", Toast.LENGTH_LONG).show();
                    } else {
                        uploadToServer(editText.getText().toString(), uid, relation);
                    }
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });
    }

    private void getRelationShipSelection() {
        final MyRadioGroup relationGroupOne = view.findViewById(R.id.relation_radiogroup);

        relationGroupOne.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton choice = view.findViewById(i);
                relation = choice.getText().toString();
            }
        });
    }

    private void setRelationShipSelected(){
        final MyRadioGroup relationGroupOne = view.findViewById(R.id.relation_radiogroup);
        for (int i=0; i<relationGroupOne.getChildCount(); i++){
            RadioButton radioButton = (RadioButton) relationGroupOne.getChildAt(i);
            if (relation.equals(radioButton.getText().toString())){
                radioButton.setChecked(true);
            }
        }
    }

    private void setReferenceEditContent(){
        EditText editText = view.findViewById(R.id.reference_edit_text);
        editText.setText(content);
    }

    private void uploadToServer(String input, int uid, String relation) {
        showProgress(mContext);
        FormBody.Builder builder = new FormBody.Builder().add("relation", relation)
                                                         .add("content", input).add("uid", String.valueOf(uid));
        if (modify){
            builder.add("rid", String.valueOf(rid));
        }
        RequestBody requestBody = builder.build();

        HttpUtil.sendOkHttpRequest(getContext(), WRITE_REFERENCE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "================uploadToServer response:" + responseText);

                try {
                    JSONObject statusObj = new JSONObject(responseText);
                    if (statusObj.optInt("response") != 1) {
                        handler.sendEmptyMessage(RESPONSE_FAILED);
                    } else {
                        writeDone = true;
                        handler.sendEmptyMessage(RESPONSE_SUCCESS);
                        mDialog.dismiss();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //KeyboardUtils.hideSoftInput(getContext());

        if (getTargetFragment() != null) {
            Intent intent = new Intent();
            intent.putExtra("type", ParseUtils.TYPE_REFERENCE);
            intent.putExtra("status", writeDone);
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
