package com.mufu.talent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.mufu.R;
import com.mufu.adapter.GridImageAdapter;
import com.mufu.common.MyApplication;
import com.mufu.dynamics.AddDynamicsActivity;
import com.mufu.group.SubGroupActivity;
import com.mufu.util.BaseDialogFragment;
import com.mufu.util.CommonBean;
import com.mufu.util.CommonDialogFragmentInterface;
import com.mufu.util.CommonPickerView;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.Slog;
import com.mufu.util.Utility;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.tools.PictureFileUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;
import static com.mufu.group.GroupFragment.eden_group;
import static com.mufu.group.SubGroupActivity.TALENT_ADD_BROADCAST;
import static com.mufu.group.SubGroupActivity.getTalent;

public class TalentAuthenticationDialogFragment extends BaseDialogFragment {
    public final static int TALENT_AUTHENTICATION_RESULT_OK = 0;
    public final static int COMMON_TALENT_AUTHENTICATION_RESULT_OK = 1;
    private static final boolean isDebug = true;
    private static final String TAG = "TalentAuthenticationDialogFragment";
    private static final String SUBMIT_URL = HttpUtil.DOMAIN + "?q=talent/become/apply";
    private Utility.TalentType type;
    private int gid;
    private int tid;
    private SubGroupActivity.Talent talent;
    private Dialog mDialog;
    private Context mContext;
    private boolean isSubmit = false;
    private EditText introductionET;
    private CommonDialogFragmentInterface commonDialogFragmentInterface;

    private GridImageAdapter adapter;
    private List<LocalMedia> selectList = new ArrayList<>();
    private List<File> selectFileList = new ArrayList<>();
    private AddDynamicsActivity addDynamicsActivity;
    
    private Button selectSubject;

    private TextView titleTV;
    private String mTitle;
    private Window window;
    private int maxSelectNum = 6;
    private int pictureSelectType = 0;//default 0 for materia
    private boolean isPicked = false;
    private Thread threadIndustry = null;
    private ArrayList<CommonBean> subjectMainItems = new ArrayList<>();
    private ArrayList<ArrayList<String>> subjectSubItems = new ArrayList<>();
    
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

        mDialog = new Dialog(getActivity(), R.style.Theme_MaterialComponents_DialogWhenLarge);

        Bundle bundle = getArguments();
        if (bundle != null) {
            type = (Utility.TalentType)bundle.getSerializable("type");
        }

        if (type == Utility.TalentType.MATCHMAKER) {
            mDialog.setContentView(R.layout.match_maker_talent_authentication);
            initMatchMakerTalent();
        } else {
            mDialog.setContentView(R.layout.talent_authentication);
            initCommonTalent();
        }
        
        titleTV = mDialog.findViewById(R.id.title);
        switch (type){
            case FOOD:
                titleTV.setText("美食达人");
                mTitle = "美食达人";
                break;
            case GROWTH:
                titleTV.setText("成长达人");
                mTitle = "成长达人";
                break;
            case TRAVEL:
                titleTV.setText("旅行达人");
                mTitle = "旅行达人";
                break;
            case INTEREST:
                titleTV.setText("兴趣达人");
                mTitle = "兴趣达人";
                break;
            case MATCHMAKER:
                titleTV.setText("牵线达人");
                mTitle = "牵线达人";
                break;
            case GUIDE:
                mTitle = "向导达人";
            case EXPERIENCE:
                mTitle = "体验达人";
                break;
                    default:
                        break;
        }
        
        mDialog.setCanceledOnTouchOutside(true);
        window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        //subGroup = (SubGroupActivity.SubGroup) bundle.getSerializable("subGroup");
        TextView leftBack = mDialog.findViewById(R.id.left_back);
        leftBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (commonDialogFragmentInterface != null) {//callback from ArchivesActivity class
                    if (type.ordinal() == eden_group) {
                        commonDialogFragmentInterface.onBackFromDialog(TALENT_AUTHENTICATION_RESULT_OK, gid, isSubmit);
                    } else {
                        //commonDialogFragmentInterface.onBackFromDialog(COMMON_TALENT_AUTHENTICATION_RESULT_OK, aid, isSubmit);
                        sendTalentAddedBroadcast();
                    }
                }
                dismiss();
            }
        });
        
        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);

        if (addDynamicsActivity == null) {
            addDynamicsActivity = new AddDynamicsActivity();
        }

        return mDialog;
    }
    
    private void initCommonTalent() {
        selectSubject = mDialog.findViewById(R.id.select_subject);
        introductionET = mDialog.findViewById(R.id.introduction_edittext);

        initSubjectJsondata();

        Button submitBtn = mDialog.findViewById(R.id.submit);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validCheck()){
                    submitNotice();
                }
            }
        });

    }

    private void submitNotice() {
        final AlertDialog.Builder normalDialogBuilder =
                new AlertDialog.Builder(getContext());
        normalDialogBuilder.setTitle(getResources().getString(R.string.talent_apply));
        normalDialogBuilder.setMessage(getResources().getString(R.string.talent_apply_content));
        normalDialogBuilder.setPositiveButton(getContext().getResources().getString(R.string.submit),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        submit();
                    }
                });

        AlertDialog normalDialog = normalDialogBuilder.create();
        normalDialog.show();
    }
    
    private void submit() {
        showProgressDialog("正在提交");
        Map<String, String> authenMap = new HashMap<>();
        authenMap.put("introduction", introductionET.getText().toString());
        authenMap.put("subject", selectSubject.getText().toString());

        if (selectList.size() > 0) {
            for (LocalMedia media : selectList) {
                selectFileList.add(new File(media.getCompressPath()));
            }
        }

        authenMap.put("type", String.valueOf(type.ordinal()));
        authenMap.put("title", String.valueOf(mTitle));

        Slog.d(TAG, "--------------------->type: " + type.ordinal());

        uploadPictures(authenMap, "authen", selectFileList);
    }
    
    private boolean validCheck() {
        if (!isPicked){
            Toast.makeText(getContext(), getResources().getString(R.string.subject_select_notice), Toast.LENGTH_LONG).show();
            return false;
        }
        
        String introduction = introductionET.getText().toString();
        if (TextUtils.isEmpty(introduction)) {
            introductionET.setError(getContext().getResources().getString(R.string.talent_introduction_empty));
            return false;
        }

        return true;
    }
    
    private void initSubjectJsondata() {

        final CommonPickerView commonPickerView = new CommonPickerView();
        if (threadIndustry == null) {
            Slog.i(TAG, "行业数据开始解析");
            threadIndustry = new Thread(new Runnable() {
                @Override
                public void run() {
                    subjectMainItems = commonPickerView.getOptionsMainItem(MyApplication.getContext(), "subject.json");
                    subjectSubItems = commonPickerView.getOptionsSubItems(subjectMainItems);
                }
            });
            threadIndustry.start();
        }

        selectSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPickerView();
            }
        });
    }
    
    
    private void showPickerView() {
        //条件选择器
        OptionsPickerView pvOptions;
        pvOptions = new OptionsPickerBuilder(getContext(), new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                //返回的分别是二个级别的选中位置
                selectSubject.setText(subjectSubItems.get(options1).get(option2));
                isPicked = true;

            }
        }).setDecorView(window.getDecorView().findViewById(R.id.talent_authentication))
                .isDialog(true).setLineSpacingMultiplier(1.2f).isCenterLabel(false).build();

        pvOptions.getDialog().getWindow().setGravity(Gravity.CENTER);
        pvOptions.setPicker(subjectMainItems, subjectSubItems);
        pvOptions.show();
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    selectList = PictureSelector.obtainMultipleResult(data);
                    if (selectList.size() > 0) {
                        adapter.setList(selectList);
                        adapter.notifyDataSetChanged();
                    }
                    break;
                case PictureConfig.SINGLE:
                    break;
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PictureConfig.APPLY_STORAGE_PERMISSIONS_CODE:
                // 存储权限
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        PictureFileUtils.deleteCacheDirFile(getContext(), PictureMimeType.ofImage());
                    } else {
                        Toast.makeText(getContext(), getString(R.string.picture_jurisdiction), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }
    
    private void initMatchMakerTalent() {
        LinearLayout explanationLL = mDialog.findViewById(R.id.explanation);
        ConstraintLayout authenticationCL = mDialog.findViewById(R.id.talent_authentication);

        Button beginBtn = mDialog.findViewById(R.id.begin_now);
        beginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                explanationLL.setVisibility(View.GONE);
                authenticationCL.setVisibility(View.VISIBLE);
                initMatchMakerAuthentication();
            }
        });
    }
    
    private void initMatchMakerAuthentication() {
        TextView title = mDialog.findViewById(R.id.title);
        title.setText("提交达人申请");
        Button save = mDialog.findViewById(R.id.submit);
        Button cancel = mDialog.findViewById(R.id.cancel);
        save.setVisibility(View.VISIBLE);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkValid()) {
                    submitMatchMaker();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        introductionET = mDialog.findViewById(R.id.introduction_edittext);

    }
    
    private void submitMatchMaker() {
        showProgressDialog("");
        FormBody.Builder builder = new FormBody.Builder()
                .add("introduction", introductionET.getText().toString())
                .add("type", String.valueOf(type));

        RequestBody requestBody = builder.build();

        HttpUtil.sendOkHttpRequest(mContext, SUBMIT_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "saveUserInfo response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        gid = new JSONObject(responseText).optInt("gid");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    isSubmit = true;
                    dismissProgressDialog();
                    mDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private boolean checkValid() {
        if (TextUtils.isEmpty(introductionET.getText().toString())) {
            Toast.makeText(getContext(), "达人介绍不能为空", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }
    
    private void uploadPictures(Map<String, String> params, String picKey, List<File> files) {
        HttpUtil.uploadPictureHttpRequest(getContext(), params, picKey, files, SUBMIT_URL, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    try {
                        String responseText = response.body().string();
                        Slog.d(TAG, "---------------->response: " + responseText);
                        int status = new JSONObject(responseText).optInt("status");
                        tid = new JSONObject(responseText).optInt("tid");
                        JSONObject talentobject = new JSONObject(responseText).optJSONObject("talent");
                        talent = getTalent(talentobject);
                        if (status == 1) {
                            isSubmit = true;
                            dismissProgressDialog();
                            selectList.clear();
                            selectFileList.clear();
                            PictureFileUtils.deleteAllCacheDirFile(getContext());
                            startTalentDetailActivity();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.submit_error), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }
    
     private void startTalentDetailActivity() {
        Intent intent = new Intent(getContext(), TalentDetailsActivity.class);
        //intent.putExtra("talent", talent);
        intent.putExtra("tid", talent.tid);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(intent);
        mDialog.dismiss();
    }
    
    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
    }
    
    private void sendTalentAddedBroadcast() {
        if (talent != null){
            Intent intent = new Intent(TALENT_ADD_BROADCAST);
            intent.putExtra("tid", talent.tid);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
        }
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
    }

}
