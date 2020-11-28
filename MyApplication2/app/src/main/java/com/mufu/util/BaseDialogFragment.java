package com.mufu.util;

import android.app.ProgressDialog;
import androidx.fragment.app.DialogFragment;
import android.widget.Toast;

import com.mufu.common.MyApplication;

public class BaseDialogFragment extends DialogFragment {
    private ProgressDialog mProgressDialog;
        private ProgressDialog mProgressHorizontalDialog;
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
    
    public void showProgressDialogProgress(int contentLength, int currentLength) {
        Slog.d("BaseDialogFragment", "--------------------->contentLength: "+contentLength);
        if (null == mProgressHorizontalDialog) {
            mProgressHorizontalDialog = new ProgressDialog(getContext());
            mProgressHorizontalDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressHorizontalDialog.setMessage("正在上传...");
            mProgressHorizontalDialog.setCanceledOnTouchOutside(false);
            if (contentLength > 1024){
                mProgressHorizontalDialog.setProgressNumberFormat("%1d KB/%2d KB");
                int maxKB = contentLength / 1024;
                mProgressHorizontalDialog.setMax(maxKB);
            }else {
                mProgressHorizontalDialog.setMax(contentLength);
            }
        }
        
        if (currentLength > 1024){
            int currentKB = currentLength / 1024;
            mProgressHorizontalDialog.setProgress(currentKB);
        }else {
            mProgressHorizontalDialog.setProgress(currentLength);
        }

        mProgressHorizontalDialog.show();
    }
            

    public void dismissProgressDialog() {
        if (null != mProgressDialog) {
            mProgressDialog.dismiss();
        }

        if (null != mProgressHorizontalDialog){
            mProgressHorizontalDialog.dismiss();
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
