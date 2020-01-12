package com.hetang.authenticate;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.common.MyApplication;
import com.hetang.common.SetAvatarActivity;
import com.hetang.util.BaseDialogFragment;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.ParseUtils;
import com.hetang.util.RoundImageView;
import com.hetang.util.Slog;
import com.hetang.util.UserProfile;

import org.angmarch.views.NiceSpinner;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hetang.archive.ArchiveFragment.GET_USER_PROFILE_DONE;
import static com.hetang.archive.ArchiveFragment.GET_USER_PROFILE_URL;
import static com.hetang.archive.ArchiveFragment.REQUESTCODE;
import static com.hetang.common.SetAvatarActivity.AUTHENTICATION_PHOTO;
import static com.hetang.common.SetAvatarActivity.AVATAR_SET_ACTION_BROADCAST;
import static com.hetang.common.SetAvatarActivity.SUBMIT_AUTHENTICATION_ACTION_BROADCAST;


public class SubmitAuthenticationDialogFragment extends BaseDialogFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "SubmitAuthenticationDialogFragment";
    private static final String SUBMIT_URL = HttpUtil.DOMAIN + "?q=user_extdata/submit_authentication_info";
    private int sex;
    private int gid;
    private int type;
    private Dialog mDialog;
    private String before;
    private Context mContext;
    private String avatarUri;
    private String uri;
    private String realName;
    private String major;
    private String degree;
    private String university;
    private EditText realNameET;
    private EditText majorET;
    private EditText degreeET;
    private NiceSpinner degreeNS;
    private EditText universityET;
    private RoundImageView avatar;
    private RoundImageView idPhoto;
    private RadioGroup sexSelect;
    private RadioButton maleRB;
    private RadioButton femaleRB;
    private UserProfile userProfile;
    private boolean isModified = false;
    private boolean hasEmpty = false;
    private List<String> degreeList;
    private NiceSpinner niceSpinnerDegree;
    private AvatarSetBroadcastReceiver mReceiver = new AvatarSetBroadcastReceiver();
    private MyHandler handler = new MyHandler(this);


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_Design_BottomSheetDialog);
        //mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.submit_authentication);

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);
        mDialog.setCanceledOnTouchOutside(true);
        Window window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        Bundle bundle = getArguments();
        if (bundle != null){
            type = bundle.getInt("type", 0);
            if (type == 0){
                gid = bundle.getInt("gid");
            }
        }
        //subGroup = (SubGroupActivity.SubGroup) bundle.getSerializable("subGroup");
        TextView leftBack = mDialog.findViewById(R.id.left_back);
        leftBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        TextView title = mDialog.findViewById(R.id.title);
        title.setText("编辑身份认证信息");
        TextView save = mDialog.findViewById(R.id.save);
        save.setVisibility(View.VISIBLE);
        save.setText("提交");
        save.setTextSize(16);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkValid()){
                    submit();
                }
            }
        });

        avatar = mDialog.findViewById(R.id.avatar);
        realNameET = mDialog.findViewById(R.id.realname_edittext);
        sexSelect = mDialog.findViewById(R.id.sexRG);
        maleRB = mDialog.findViewById(R.id.radioMale);
        femaleRB = mDialog.findViewById(R.id.radioFemale);
        majorET = mDialog.findViewById(R.id.major_edittext);
        degreeNS = mDialog.findViewById(R.id.nice_spinner_degree);
        universityET = mDialog.findViewById(R.id.university_edittext);
        idPhoto = mDialog.findViewById(R.id.ID_photo);

        String[] degrees = getResources().getStringArray(R.array.degrees);

        niceSpinnerDegree = mDialog.findViewById(R.id.nice_spinner_degree);
        degreeList = new LinkedList<>(Arrays.asList(degrees));
        niceSpinnerDegree.attachDataSource(degreeList);
        niceSpinnerDegree.addOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //educationBackground.degree = String.valueOf(degreeList.get(i));
                degree = degreeList.get(i);
            }

        });

        TextView setAvatar = mDialog.findViewById(R.id.set_avatar);
        setAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SetAvatarActivity.class);
                getActivity().startActivityForResult(intent, REQUESTCODE);
            }
        });

        idPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SetAvatarActivity.class);
                intent.putExtra("type", AUTHENTICATION_PHOTO);
                getActivity().startActivityForResult(intent, REQUESTCODE);
            }
        });

        getUserProfile();

        registerBroadcast();

        return mDialog;
    }

    private void submit() {
        showProgressDialog("");
        FormBody.Builder builder = new FormBody.Builder()
                .add("gid", String.valueOf(gid))
                .add("authentication_info", getUserInfoJsonObject().toString())
                .add("type", String.valueOf(type));

        RequestBody requestBody = builder.build();

        HttpUtil.sendOkHttpRequest(mContext, SUBMIT_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "saveUserInfo response : "+responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    dismissProgressDialog();
                    dismiss();
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {

            }
        });


    }
    
    private boolean checkValid(){
        if (TextUtils.isEmpty(realNameET.getText().toString())){
            Toast.makeText(getContext(), "姓名不能为空", Toast.LENGTH_LONG).show();
            return false;
        }

        if (TextUtils.isEmpty(majorET.getText().toString())){
            Toast.makeText(getContext(), "专业不能为空", Toast.LENGTH_LONG).show();
            return false;
        }

        if (degree.equals("学历")){
            Toast.makeText(getContext(), "请选择学历", Toast.LENGTH_LONG).show();
            return false;
        }
        
        if (TextUtils.isEmpty(universityET.getText().toString())){
            Toast.makeText(getContext(), "学校不能为空", Toast.LENGTH_LONG).show();
            return false;
        }

        if (TextUtils.isEmpty(userProfile.getAvatar()) && TextUtils.isEmpty(avatarUri)){
            Toast.makeText(getContext(), "请设置头像", Toast.LENGTH_LONG).show();
            return false;
        }

        if (TextUtils.isEmpty(uri)){
            Toast.makeText(getContext(), "请上传证件照", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }


    private JSONObject getUserInfoJsonObject() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("realname", realNameET.getText().toString());
            jsonObject.put("sex", sex);
            jsonObject.put("university", universityET.getText().toString());
            jsonObject.put("major", majorET.getText().toString());
            jsonObject.put("degree_index", userProfile.getDegreeIndex(degree));
            jsonObject.put("uri", uri);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;

    }


    private void setIDInfo() {
        if (!"".equals(userProfile.getAvatar())) {
            String avatar_url = HttpUtil.DOMAIN + userProfile.getAvatar();
            if (getActivity() != null) {
                Glide.with(getActivity()).load(avatar_url).into(avatar);
            }
        } else {
            if (userProfile.getSex() == 0) {
                avatar.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.male_default_avator));
            } else {
                avatar.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.female_default_avator));
            }
        }

        realNameET.setText(userProfile.getRealName());
        sex = userProfile.getSex();
        if (userProfile.getSex() == 0) {
            maleRB.setChecked(true);
        } else {
            femaleRB.setChecked(true);
        }

        majorET.setText(userProfile.getMajor());
        
        degree = userProfile.getDegreeName(userProfile.getDegree());

        for (int i=0; i<degreeList.size(); i++){
            if (degree.equals(degreeList.get(i))){
                niceSpinnerDegree.setSelectedIndex(i);
                break;
            }
        }

        universityET.setText(userProfile.getUniversity());

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                before = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String after = s.toString();
                if (!after.equals(before)) {
                    isModified = true;
                }
            }
        };

        realNameET.addTextChangedListener(watcher);
        majorET.addTextChangedListener(watcher);
        universityET.addTextChangedListener(watcher);

        sexSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int id = group.getCheckedRadioButtonId();
                if(id == R.id.radioMale){
                    sex = 0;
                }else {
                    sex = 1;
                }
            }
        });

    }

    private void getUserProfile() {
        RequestBody requestBody = new FormBody.Builder().build();
        HttpUtil.sendOkHttpRequest(getContext(), GET_USER_PROFILE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();

                    if (responseText != null) {
                        if (!TextUtils.isEmpty(responseText)) {
                            try {
                                if (isDebug)
                                    Slog.d(TAG, "==============responseText: " + responseText);
                                JSONObject jsonObject = new JSONObject(responseText).optJSONObject("user");
                                userProfile = ParseUtils.getUserProfileFromJSONObject(jsonObject);
                                if (userProfile != null) {
                                    handler.sendEmptyMessage(GET_USER_PROFILE_DONE);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }

    private void handleMessage(Message message) {
        switch (message.what) {
            case GET_USER_PROFILE_DONE:
                setIDInfo();
                break;
        }
    }

    private class AvatarSetBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case AVATAR_SET_ACTION_BROADCAST:
                    avatarUri = intent.getStringExtra("avatar");
                    Glide.with(getContext()).load(HttpUtil.DOMAIN + avatarUri).into(avatar);
                    break;
                case SUBMIT_AUTHENTICATION_ACTION_BROADCAST:
                    uri = intent.getStringExtra("uri");
                    Glide.with(getContext()).load(HttpUtil.DOMAIN + uri).into(idPhoto);
                    break;
                default:
                    break;
            }
        }
    }

    //register local broadcast to receive DYNAMICS_ADD_BROADCAST
    private void registerBroadcast() {
        mReceiver = new AvatarSetBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AVATAR_SET_ACTION_BROADCAST);
        intentFilter.addAction(SUBMIT_AUTHENTICATION_ACTION_BROADCAST);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, intentFilter);
    }

    //unregister local broadcast
    private void unRegisterBroadcast() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
        unRegisterBroadcast();
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
    }

    static class MyHandler extends Handler {
        WeakReference<SubmitAuthenticationDialogFragment> submitAuthenticationDialogFragmentWeakReference;

        MyHandler(SubmitAuthenticationDialogFragment submitAuthenticationDialogFragment) {
            submitAuthenticationDialogFragmentWeakReference = new WeakReference<>(submitAuthenticationDialogFragment);
        }

        @Override
        public void handleMessage(Message message) {
            SubmitAuthenticationDialogFragment submitAuthenticationDialogFragment = submitAuthenticationDialogFragmentWeakReference.get();
            if (submitAuthenticationDialogFragment != null) {
                submitAuthenticationDialogFragment.handleMessage(message);
            }
        }
    }
}
