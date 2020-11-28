package com.mufu.common;

import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.mufu.util.Slog;
import com.umeng.analytics.MobclickAgent;

/**
 * added by xuchunping 2018.7.19
 * 处理公共操作，如全局对话框、进度条、Toast等
 */
 public class BaseAppCompatActivity extends AppCompatActivity {
    private ProgressDialog mProgressDialog;
    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //umeng
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //umeng
        MobclickAgent.onPause(this);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();
    }

    public void showProgressDialog(String msg) {
        if (null == mProgressDialog) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(msg);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }
  
      public void showProgressDialogProgress(int contentLength, int currentLength) {
        Slog.d("BaseAppCompatActivity", "--------------------->contentLength: "+contentLength);
        if (null == mProgressDialog) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setMessage("正在上传...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            if (contentLength > 1024){
                mProgressDialog.setProgressNumberFormat("%1d KB/%2d KB");
                int maxKB = contentLength / 1024;
                mProgressDialog.setMax(maxKB);
            }else {
                mProgressDialog.setMax(contentLength);
            }
        }
       
       if (currentLength > 1024){
            int currentKB = currentLength / 1024;
            mProgressDialog.setProgress(currentKB);
        }else {
            mProgressDialog.setProgress(currentLength);
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
            mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(msg);
        }
        mToast.show();
    }
}
