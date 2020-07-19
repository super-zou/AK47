package com.mufu.util;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mufu.R;

public class DialogUtil {
    private static AlertDialog dialog;

    /**
     * * @param activity Context
     * * @param iconRes 提示图标
     * * @param title 提示标题
     * * @param msg 提示内容
     * * @param positiveText 确认
     * * @param negativeText 取消
     * * @param cancelableTouchOut 点击外部是否隐藏提示框
     * * @param alertDialogBtnClickListener 点击监听
     * */
     
     public static void showAlertDialog(Context context, String title, String msg, String positiveText, String negativeText,
                                   boolean cancelableTouchOut, final AlertDialogBtnClickListener alertDialogBtnClickListener){
        View view = LayoutInflater.from(context).inflate(R.layout.update_notice_dialog, null);
        TextView mTitle = view.findViewById(R.id.title);
        TextView mMessage = view.findViewById(R.id.content);
        Button positiveButton = view.findViewById(R.id.positive);
        ImageView negativeButton = view.findViewById(R.id.close);
        mTitle.setText(title);
        mMessage.setText(msg);
        positiveButton.setText(positiveText);
        //negativeButton.setText(negativeText);
        
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialogBtnClickListener.clickPositive();
                dialog.dismiss();
            }
        });


        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialogBtnClickListener.clickNegative();
                dialog.dismiss();
            }
        });
        
         AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);


        builder.setCancelable(true); //返回键dismiss
        // 创建对话框
        dialog = builder.create();
        //dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        //去掉圆角背景背后的棱角
        dialog.setCanceledOnTouchOutside(cancelableTouchOut); //失去焦点dismiss
        dialog.show();
    }
    
    public interface AlertDialogBtnClickListener {
        void clickPositive();
        void clickNegative();
    }

}
