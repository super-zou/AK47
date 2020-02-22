package com.hetang.message;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.Window;
import android.view.WindowManager;

import com.hetang.R;
import com.hetang.common.MyApplication;
import com.hetang.util.BaseDialogFragment;
import com.hetang.util.FontManager;

import static com.hetang.group.SubGroupDetailsActivity.EXIT_GROUP_BROADCAST;

/**
 * Created by super-zou on 18-9-9.
 */

public class NotificationDetailsDialogFragment extends BaseDialogFragment {
    private Dialog mDialog;
    private static final boolean isDebug = true;
    private static final String TAG = "NotificationDetailsDialogFragment";
    private Context mContext;
    private NotificationFragment.Notification notification;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_Design_BottomSheetDialog);
        //mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.notification_details);

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);
        mDialog.setCanceledOnTouchOutside(false);
        Window window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        //layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        final Bundle bundle = getArguments();
        notification = (NotificationFragment.Notification) bundle.getSerializable("notification");

        return mDialog;
    }

    private void sendBroadcast() {
        Intent intent = new Intent(EXIT_GROUP_BROADCAST);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
    }
}
