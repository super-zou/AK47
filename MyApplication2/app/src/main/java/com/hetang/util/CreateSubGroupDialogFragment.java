package com.hetang.util;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.hetang.R;
import com.hetang.common.MyApplication;
import com.hetang.meet.SingleGroupDetailsActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hetang.meet.MeetSingleGroupFragment.GROUP_ADD_BROADCAST;
/**
 * Created by super-zou on 18-9-9.
 */
 
 public class CreateSubGroupDialogFragment extends BaseDialogFragment {
    private Dialog mDialog;
    private static final boolean isDebug = true;
    private static final String TAG = "CreateSubGroupDialogFragment";
    private static final String SUBGROUP_CREATE = HttpUtil.DOMAIN + "?q=subgroup/create";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_Design_BottomSheetDialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.create_subgroup);

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);
        mDialog.setCanceledOnTouchOutside(true);
        Window window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        TextView save = mDialog.findViewById(R.id.save);
        save.setVisibility(View.VISIBLE);

        TextView title = mDialog.findViewById(R.id.title);
        title.setText(getString(R.string.create_group));
        
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSingleGroup();
            }
        });

        TextView back = mDialog.findViewById(R.id.left_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });

        return mDialog;
    }
    
    public void saveSingleGroup(){
        EditText name = mDialog.findViewById(R.id.editTextName);
        EditText profile = mDialog.findViewById(R.id.editTextProfile);
        EditText org = mDialog.findViewById(R.id.editTextOrg);
        EditText city = mDialog.findViewById(R.id.editTextOrgCity);

        String groupName = name.getText().toString();
        String groupProfile = profile.getText().toString();
        String groupOrg = org.getText().toString();
        String groupCity = city.getText().toString();

        if(TextUtils.isEmpty(groupName)){
            return;
        }
        showProgressDialog("正在保存");
        final RequestBody requestBody = new FormBody.Builder()
                .add("group_name", groupName)
                .add("group_profile", groupProfile)
                .add("group_org", groupOrg)
                .add("group_city", groupCity)
                .build();
                
        HttpUtil.sendOkHttpRequest(getContext(), SUBGROUP_CREATE, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(isDebug) Slog.d(TAG, "==========response body : " + response.body());

                if (response.body() != null) {
                    String responseText = response.body().string();
                    if(isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        if(isDebug) Slog.d(TAG, "==========response text 1: " + responseText);
                     try {
                            JSONObject responseObj = new JSONObject(responseText);
                            if (responseObj != null){
                                int gid = responseObj.optInt("gid");
                                if (gid > 0){
                                    sendBroadcast(gid);
                                }
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                        dismissProgressDialog();
                        mDialog.dismiss();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void sendBroadcast(int gid) {
      Intent intent = new Intent(GROUP_ADD_BROADCAST);
      intent.putExtra("gid", gid);
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
