package com.tongmenhui.launchak47.util;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;

import com.tongmenhui.launchak47.R;

public class InvitationDialogFragment extends DialogFragment implements View.OnClickListener{
    private Dialog mDialog;
    private MultiAutoCompleteTextView multiAutoCompleteTextView;
    private ArrayAdapter<String> adapter;
    private Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_Design_Light);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.invite_reference);
        mDialog.setCanceledOnTouchOutside(true);
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        //layoutParams.alpha = 0.9f;
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(layoutParams);

        multiAutoCompleteTextView = mDialog.findViewById(R.id.multiAutoCompleteTextView);
        adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_dropdown_item_1line);

        adapter.add("1");
        adapter.add("124");
        adapter.add("1234");
        adapter.add("134");
        adapter.add("2345");
        adapter.add("2356");
        adapter.add("2567");

        multiAutoCompleteTextView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        multiAutoCompleteTextView.setAdapter(adapter);

        return mDialog;
    }

    @Override
    public void onClick(View view){

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
