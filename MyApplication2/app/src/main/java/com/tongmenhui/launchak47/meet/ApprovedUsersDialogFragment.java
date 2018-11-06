package com.tongmenhui.launchak47.meet;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.adapter.ImpressionApprovedDetailAdapter;
import com.tongmenhui.launchak47.util.Slog;

public class ApprovedUsersDialogFragment extends DialogFragment {

    private static final String TAG = "ImpressionApprovedDetailsDialogFragment";
    private Context mContext;
    private Dialog mDialog;
    private View view;
    private RecyclerView mUsersDetailList;
    private ImpressionApprovedDetailAdapter approvedDetailAdapter;
    private LayoutInflater inflater;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ArchivesActivity.ImpressionStatistics impressionStatistics = null;
        Bundle bundle = getArguments();
        if(bundle != null){
            impressionStatistics  = bundle.getParcelable("impressionStatistics");
            Slog.d(TAG, "=================count: "+impressionStatistics.meetMemberList.size());
        }
        inflater = LayoutInflater.from(mContext);
        mDialog = new Dialog(mContext, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        view = inflater.inflate(R.layout.impression_approved_users_detail, null);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(view);
        mDialog.setCanceledOnTouchOutside(true);
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        //layoutParams.alpha = 0.9f;
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setDimAmount(0.8f);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        window.setAttributes(layoutParams);

        mUsersDetailList = mDialog.findViewById(R.id.users_detail);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mUsersDetailList.setLayoutManager(linearLayoutManager);
        approvedDetailAdapter = new ImpressionApprovedDetailAdapter(mContext);
        mUsersDetailList.setAdapter(approvedDetailAdapter);

        approvedDetailAdapter.setData(impressionStatistics.meetMemberList);
        approvedDetailAdapter.notifyDataSetChanged();

        return mDialog;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //KeyboardUtils.hideSoftInput(getContext());
        if(mDialog != null){
            mDialog.dismiss();
            mDialog = null;
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
