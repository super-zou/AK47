package com.hetang.archive;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
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

import static com.hetang.archive.ArchiveFragment.SET_PAPER_RESULT_OK;

public class PaperEditDialogFragment extends BaseDialogFragment {
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
    
    private static final String CREATE_PAPER_URL = HttpUtil.DOMAIN + "?q=personal_archive/paper/create";

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
        view = inflater.inflate(R.layout.paper_edit, null);
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
        
        final TextInputEditText titleEdit = mDialog.findViewById(R.id.title_edit);
        final TextInputEditText dateEdit = mDialog.findViewById(R.id.date_edit);
        final TextInputEditText websiteEdit = mDialog.findViewById(R.id.website_edit);
        final TextInputEditText descriptionEdit = mDialog.findViewById(R.id.description_edit);

        final Paper paper = new Paper();
        
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(titleEdit.getText().toString())){
                    paper.title = titleEdit.getText().toString();
                }else {
                    Toast.makeText(getContext(), "请输入名称", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!TextUtils.isEmpty(dateEdit.getText().toString())){
                    paper.date = dateEdit.getText().toString();
                }else {
                    Toast.makeText(getContext(), "请输入日期", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!TextUtils.isEmpty(websiteEdit.getText().toString())){
                    paper.website = websiteEdit.getText().toString();
                }
                
                 if (!TextUtils.isEmpty(descriptionEdit.getText().toString())){
                    paper.description = descriptionEdit.getText().toString();
                }

                uploadToServer(paper);
            }
        });
    }

    private void uploadToServer(Paper paper) {

        showProgressDialog(getString(R.string.saving_progress));
        
        RequestBody requestBody = new FormBody.Builder()
                .add("title", paper.title)
                .add("time", paper.date)
                .add("website", paper.website)
                .add("description", paper.description).build();

        HttpUtil.sendOkHttpRequest(getContext(), CREATE_PAPER_URL, requestBody, new Callback() {
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

    class Paper{
        String title;
        String date;
        String description = "";
        String website;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();

        /*
        if (getTargetFragment() != null){
            Intent intent = new Intent();
            getTargetFragment().onActivityResult(REQUESTCODE, SET_PAPER_RESULT_OK, intent);
        }

         */

        if (commonDialogFragmentInterface != null) {//callback from ArchivesActivity class
            commonDialogFragmentInterface.onBackFromDialog(SET_PAPER_RESULT_OK, 0, writeDone);
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
