package com.hetang.util;

import android.app.ProgressDialog;
import androidx.fragment.app.DialogFragment;
import android.widget.Toast;

import com.hetang.common.MyApplication;

public class BaseDialogFragment extends DialogFragment {
    private ProgressDialog mProgressDialog;
    private Toast mToast;

    @Override
    public void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();
    }
    
    
    public void showProgressDialog(String msg) {
        if (null == mProgressDialog) {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setMessage(msg);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    public void dismissProgressDialog() {
        if (null != mProgressDialog) {
            mProgressDialog.dismiss();
        }
    }
    
    public void toast(String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(MyApplication.getContext(), msg, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(msg);
        }
        mToast.show();
    }

}
