package com.hetang.archive;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputEditText;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.hetang.R;
import com.hetang.util.BaseDialogFragment;
import com.hetang.util.CommonDialogFragmentInterface;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.Slog;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hetang.archive.ArchiveFragment.SET_VOLUNTEER_RESULT_OK;

public class VolunteerEditDialogFragment extends BaseDialogFragment {
    private static final String TAG = "PrizeEditDialogFragment";
    private Context mContext;
    private Dialog mDialog;
    private View view;
    private LayoutInflater inflater;
    private TextView title;
    private TextView save;
    private TextView cancel;
    private boolean writeDone = false;
    private CommonDialogFragmentInterface commonDialogFragmentInterface;
    private static final String CREATE_VOLUNTEER_URL = HttpUtil.DOMAIN + "?q=personal_archive/volunteer/create";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        try {
            commonDialogFragmentInterface = (CommonDialogFragmentInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement commonDialogFragmentInterface");
        }
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        inflater = LayoutInflater.from(mContext);
        mDialog = new Dialog(mContext, android.R.style.Theme_Light_NoTitleBar);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        view = inflater.inflate(R.layout.volunteer_edit, null);
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

        initView();

        Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.cancel), font);

        return mDialog;
    }

    private void initView() {

        save = mDialog.findViewById(R.id.save);
        cancel = mDialog.findViewById(R.id.cancel);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });
        
        final TextInputEditText institutionEdit = mDialog.findViewById(R.id.institution_edit);
        final TextInputEditText roleEdit = mDialog.findViewById(R.id.role_edit);
        final TextInputEditText dateStart = mDialog.findViewById(R.id.date_start);
        final TextInputEditText dateFinish = mDialog.findViewById(R.id.date_finish);
        final TextInputEditText descriptionEdit = mDialog.findViewById(R.id.description_edit);

        final Volunteer volunteer = new Volunteer();


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if (!TextUtils.isEmpty(institutionEdit.getText().toString())){
                    volunteer.institution = institutionEdit.getText().toString();
                }else {
                    Toast.makeText(getContext(), "请输入组织名称", Toast.LENGTH_LONG).show();
                    return;
                }

                if(!TextUtils.isEmpty(roleEdit.getText().toString())){
                    volunteer.role = roleEdit.getText().toString();
                }else {
                    Toast.makeText(getContext(), "请输入角色", Toast.LENGTH_LONG).show();
                    return;
                }
                
                if (!TextUtils.isEmpty(dateStart.getText().toString())){
                    volunteer.start = dateStart.getText().toString();
                }else {
                    Toast.makeText(getContext(), "请输入开始时间", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!TextUtils.isEmpty(dateFinish.getText().toString())){
                    volunteer.end = dateFinish.getText().toString();
                }else {
                    Toast.makeText(getContext(), "请输入结束时间", Toast.LENGTH_LONG).show();
                    return;
                }
                
                 if (!TextUtils.isEmpty(descriptionEdit.getText().toString())){
                    volunteer.description = descriptionEdit.getText().toString();
                }

                uploadToServer(volunteer);
            }
        });
    }

    private void uploadToServer(Volunteer volunteer) {

        showProgressDialog(getString(R.string.saving_progress));
        
        RequestBody requestBody = new FormBody.Builder()
                .add("role", volunteer.role)
                .add("institution", volunteer.institution)
                .add("start", volunteer.start)
                .add("end", volunteer.end)
                .add("description", volunteer.description).build();
        HttpUtil.sendOkHttpRequest(getContext(), CREATE_VOLUNTEER_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "================uploadToServer response:" + responseText);
                if(!TextUtils.isEmpty(responseText)){
                    writeDone = true;
                    dismissProgressDialog();
                    mDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {  }
        });

    }
    
    class Volunteer{
        String institution;
        String role;
        String start;
        String end;
        String description = "";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        /*
        if (getTargetFragment() != null){
            Intent intent = new Intent();
            getTargetFragment().onActivityResult(REQUESTCODE, SET_VOLUNTEER_RESULT_OK, intent);
        }

         */

        if (commonDialogFragmentInterface != null) {//callback from ArchivesActivity class
            commonDialogFragmentInterface.onBackFromDialog(SET_VOLUNTEER_RESULT_OK, 0, writeDone);
        }

        dismissProgressDialog();

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

}
