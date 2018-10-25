package com.tongmenhui.launchak47.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.meet.MeetDynamicsFragment;

/**
 * Created by super-zou on 18-9-9.
 */

public class CommentDialogFragment extends DialogFragment implements View.OnClickListener {
    private Dialog mDialog;
    private EditText commentEditText;
    private Button sendBtn;
    private InputMethodManager inputMethodManager;
    //private DialogFragmentDataCallback dataCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_Design_BottomSheetDialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.comment_input_dialog);
        mDialog.setCanceledOnTouchOutside(true);
        getDialog().getWindow().setDimAmount(0.8f);
        Window window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.BOTTOM;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        commentEditText = mDialog.findViewById(R.id.comment_content);
        sendBtn = mDialog.findViewById(R.id.comment_send);

        fillEditText();
        setSoftKeyboard();

        commentEditText.addTextChangedListener(mTextWatcher);
        sendBtn.setOnClickListener(this);

        return mDialog;
    }

    private void fillEditText() {

    }

    private void setSoftKeyboard() {
        commentEditText.setFocusable(true);
        commentEditText.setFocusableInTouchMode(true);
        commentEditText.requestFocus();

        commentEditText.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager.showSoftInput(commentEditText, 0)) {
                    commentEditText.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
    }

    private TextWatcher mTextWatcher = new TextWatcher() {

        private CharSequence temp;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            temp = s;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (temp.length() > 0) {
                sendBtn.setEnabled(true);
                sendBtn.setClickable(true);
                sendBtn.setTextColor(ContextCompat.getColor(getActivity(), R.color.blue));
            } else {
                sendBtn.setEnabled(true);
                sendBtn.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.comment_send:
                //Toast.makeText(getActivity(), commentEditText.getText().toString(), Toast.LENGTH_LONG).show();
                Intent intent= new Intent();
                intent.putExtra("comment_text", commentEditText.getText().toString());
                getTargetFragment().onActivityResult(MeetDynamicsFragment.REQUEST_CODE, Activity.RESULT_OK, intent);
                commentEditText.setText("");
                dismiss();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface){
        super.onDismiss(dialogInterface);
    }

    @Override
    public void onCancel(DialogInterface dialogInterface){
        super.onCancel(dialogInterface);
    }


}
