package com.hetang.util;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nex3z.flowlayout.FlowLayout;
import com.hetang.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hetang.archive.ArchiveFragment.REQUESTCODE;
import static com.hetang.main.MeetArchiveFragment.RESULT_OK;

public class PersonalityEditDialogFragment extends DialogFragment {
    private static final String TAG = "PersonalityEditDialogFragment";
    private static final String SET_PERSONALITY_URL = HttpUtil.DOMAIN + "?q=meet/personality/set";
    private static final String CREATE_HOBBY_URL = HttpUtil.DOMAIN + "?q=personal_archive/hobby/create";
    private Context mContext;
    private Dialog mDialog;
    private View view;
    private int totalWord = 0;
    private LayoutInflater inflater;
    
    private int type = ParseUtils.TYPE_PERSONALITY;
    private TextView title;
    private FlowLayout personalityFL;
    private EditText editText;
    private TextView save;
    private TextView remainWord;
    private TextView cancel;
    private CommonDialogFragmentInterface commonDialogFragmentInterface;
    private ProgressDialog progressDialog;
    private boolean writeDone = false;
    private MyHandler handler;

    private static final int RESPONSE_FAILED = 0;
    private static final int RESPONSE_SUCCESS = 1;
    

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        try {
            //evaluateDialogFragmentListener = (EvaluateDialogFragmentListener) context;
            commonDialogFragmentInterface = (CommonDialogFragmentInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement commonDialogFragmentInterface");
        }

        handler = new MyHandler(this);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        int uid = -1;
        Bundle bundle = getArguments();
        if (bundle != null) {
            type = bundle.getInt("type");
            uid = bundle.getInt("uid");
        }

        inflater = LayoutInflater.from(mContext);
        mDialog = new Dialog(mContext, android.R.style.Theme_Light_NoTitleBar);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        view = inflater.inflate(R.layout.personality_edit, null);
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

        initView(uid);
        return mDialog;
    }

    private void initView(final int uid) {
        title = view.findViewById(R.id.add_hobby_title);
        personalityFL = view.findViewById(R.id.personality_flow_layout);
        editText = view.findViewById(R.id.personality_edit_text);
        save = view.findViewById(R.id.save);
        remainWord = view.findViewById(R.id.word_remain);
        cancel = view.findViewById(R.id.cancel);
        if (type == ParseUtils.TYPE_HOBBY) {
            title.setText("添加兴趣爱好");
            editText.setHint("输入兴趣爱好");
        }else {
            title.setText("添加个性标签");
        }
        
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        
        addPersonalityAction(uid);
    }

    private void addPersonalityAction(final int uid) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                editText.setBackground(mContext.getDrawable(R.drawable.label_btn_shape));
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int remain = 64 - totalWord - s.length();
                if (remain >= 0) {
                    remainWord.setText(remain + "/64");
                } else {
                    Toast.makeText(mContext, "已经超出64个文字限制", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0 && personalityFL.getChildCount() == 0) {
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
                if (!TextUtils.isEmpty(v.getText())) {
                    Slog.d(TAG, "=========actionId:" + actionId + " text: " + v.getText());
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins((int) Utility.dpToPx(mContext, 3), (int) Utility.dpToPx(mContext, 8),
                            (int) Utility.dpToPx(mContext, 8), (int) Utility.dpToPx(mContext, 3));
                    final TextView personality = new TextView(mContext);
                    personality.setText(v.getText() + " ×");
                    totalWord += v.getText().length();

                    personality.setPadding((int) Utility.dpToPx(mContext, 8), (int) Utility.dpToPx(mContext, 6),
                            (int) Utility.dpToPx(mContext, 8), (int) Utility.dpToPx(mContext, 6));
                    personality.setBackground(mContext.getDrawable(R.drawable.label_btn_shape));
                    personality.setTextColor(mContext.getResources().getColor(R.color.color_blue));
                    personality.setLayoutParams(layoutParams);
                    personalityFL.addView(personality);

                    personality.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            personalityFL.removeView(personality);
                            if (personalityFL.getChildCount() == 0) {
                                save.setEnabled(false);
                                save.setTextColor(mContext.getResources().getColor(R.color.color_disabled));
                            }
                            totalWord -= personality.getText().length() - 2;
                            remainWord.setText((64 - totalWord) + "/64");
                        }
                    });
                    editText.setText("");
                    editText.setBackground(mContext.getDrawable(R.drawable.label_btn_shape_no_border));
                    save.setEnabled(true);
                    save.setTextColor(mContext.getResources().getColor(R.color.color_blue));
                }
                return true;
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            String personalityStr = "";

            @Override
            public void onClick(View v) {
                if (personalityFL.getChildCount() > 0) {
                    if (totalWord < 64) {
                        for (int i = 0; i < personalityFL.getChildCount(); i++) {
                            final TextView personalityTV = (TextView) personalityFL.getChildAt(i);
                            String personality = personalityTV.getText().toString().replace(" ×", "#");
                            personalityStr += personality;
                        }
                        uploadToServer(personalityStr, uid);
                        Slog.d(TAG, "==============personalityStr: " + personalityStr);
                        save.setEnabled(false);
                    } else {
                        Toast.makeText(mContext, "已经超出64个文字限制，请减少到64个文字!", Toast.LENGTH_LONG).show();
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

    private void uploadToServer(String input, int uid) {
        showProgress(mContext);
        if (type == ParseUtils.TYPE_PERSONALITY) {//for personality
            RequestBody requestBody = new FormBody.Builder()
                    .add("uid", String.valueOf(uid))
                    .add("personality", input).build();
            HttpUtil.sendOkHttpRequest(getContext(), SET_PERSONALITY_URL, requestBody, new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    Slog.d(TAG, "================uploadToServer response:" + responseText);
                    try {
                        JSONObject statusObj = new JSONObject(responseText);
                        if (statusObj.optBoolean("status") != true) {
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
        } else {//for hobby
            RequestBody requestBody = new FormBody.Builder()
                    .add("uid", String.valueOf(uid))
                    .add("hobby", input).build();
            HttpUtil.sendOkHttpRequest(getContext(), CREATE_HOBBY_URL, requestBody, new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    try {
                        JSONObject statusObj = new JSONObject(responseText);
                        if (statusObj.optBoolean("status") != true) {
                            Toast.makeText(mContext, "保存失败，请稍后再试", Toast.LENGTH_LONG).show();
                        } else {
                            writeDone = true;
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

    }

    public void handleMessage(Message message) {
        switch (message.what) {
            case RESPONSE_FAILED:
                Toast.makeText(mContext, "保存失败，请稍后再试", Toast.LENGTH_LONG).show();
                break;
            case RESPONSE_SUCCESS:
                save.setEnabled(false);
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (getTargetFragment() != null){
            Intent intent = new Intent();
            if (type == ParseUtils.TYPE_PERSONALITY){
                intent.putExtra("type", ParseUtils.TYPE_PERSONALITY);
                intent.putExtra("status", writeDone);
            }

            if (type == ParseUtils.TYPE_HOBBY){
                intent.putExtra("type", ParseUtils.TYPE_HOBBY);
                intent.putExtra("status", writeDone);
            }

            getTargetFragment().onActivityResult(REQUESTCODE, RESULT_OK, intent);
        }

        closeProgressDialog();
        
        //KeyboardUtils.hideSoftInput(getContext());
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

    static class MyHandler extends Handler {
        WeakReference<PersonalityEditDialogFragment> personalityEditDialogFragmentWeakReference;

        MyHandler(PersonalityEditDialogFragment personalityDialogFragment) {
            personalityEditDialogFragmentWeakReference = new WeakReference<PersonalityEditDialogFragment>(personalityDialogFragment);
        }

        @Override
        public void handleMessage(Message message) {
            PersonalityEditDialogFragment personalityDialogFragment = personalityEditDialogFragmentWeakReference.get();
            if (personalityDialogFragment != null) {
                personalityDialogFragment.handleMessage(message);
            }
        }
    }
}
