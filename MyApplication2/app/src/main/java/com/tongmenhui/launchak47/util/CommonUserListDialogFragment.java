package com.tongmenhui.launchak47.util;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.tongmenhui.launchak47.adapter.PersonalityApprovedAdapter;
import com.tongmenhui.launchak47.meet.MeetMemberInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.tongmenhui.launchak47.R;

import java.lang.ref.WeakReference;

public class CommonUserListDialogFragment extends DialogFragment {
    private static final String TAG = "CommonUserListDialogFragment";
    private Context mContext;
    private Dialog mDialog;
    private View view;
    private Button approve;
    private LayoutInflater inflater;
    private Handler handler;
    private static final int LOAD_USERS_DONE = 0;
    private static final int HIDE_APPROVE = 1;
    private static final int PERSONALITY = 0;
    private static final int FOLLOWED = 1;
    private static final int FOLLOWING = 2;
    public List<MeetMemberInfo> memberInfoList = new ArrayList<>();
    private PersonalityApprovedAdapter personalityApprovedAdapter;
    private static final String GET_APPROVED_USERS_URL = HttpUtil.DOMAIN + "?q=meet/personality/approved_users";
    private static final String APPROVE_PERSONALITY_URL = HttpUtil.DOMAIN + "?q=meet/personality/approve";
    private static final String GET_FOLLOW_USERS_URL = HttpUtil.DOMAIN + "?q=follow/get/";
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;

    }
    

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        inflater = LayoutInflater.from(mContext);
        mDialog = new Dialog(mContext, android.R.style.Theme_Light_NoTitleBar);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        view = inflater.inflate(R.layout.personality_approved_detail, null);
        //mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(view);
        mDialog.setCanceledOnTouchOutside(true);
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.TOP;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        //window.setDimAmount(0.8f);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        window.setAttributes(layoutParams);

        initView();



        TextView titleText = view.findViewById(R.id.title_text);

        handler = new CommonUserListDialogFragment.MyHandler(CommonUserListDialogFragment.this);
        int type = bundle.getInt("type", 0);
        int uid = -1;
        String title = "";
        switch (type){
            case PERSONALITY:
                int pid = -1;
                String personality = "";

                if(bundle != null){
                    personality = bundle.getString("personality");
                    pid = bundle.getInt("pid");
                }
                titleText.setText(personality);
                setApprovedUserView(pid);
                break;
            case FOLLOWED:
                uid = bundle.getInt("uid");
                title = bundle.getString("title");
                titleText.setText(title);
                setFollowUserView(type, uid);
                break;
            case FOLLOWING:
                uid = bundle.getInt("uid");
                title = bundle.getString("title");
                titleText.setText(title);
                setFollowUserView(type, uid);
                break;
        }

        return mDialog;
    }

    private void initView(){
        RecyclerView recyclerView = view.findViewById(R.id.approved_detail_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        personalityApprovedAdapter = new PersonalityApprovedAdapter(mContext);
        recyclerView.setAdapter(personalityApprovedAdapter);

        TextView cancel = view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });

    }
    
    private void setApprovedUserView(final int pid){
        approve = view.findViewById(R.id.approve);
        approve.setVisibility(View.VISIBLE);

        getUserList(PERSONALITY, -1, pid);

        approve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Slog.d(TAG, "=======================approve click");
                RequestBody requestBody = new FormBody.Builder()
                        .add("pid", String.valueOf(pid)).build();
                                HttpUtil.sendOkHttpRequest(mContext, APPROVE_PERSONALITY_URL, requestBody, new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if(response.body() != null){
                            try {
                                String responseText = response.body().string();
                                Slog.d(TAG, "==========getPersonality  response text : "+responseText);
                                boolean status = new JSONObject(responseText).optBoolean("status");
                                if(status == true){
                                    mDialog.dismiss();
                                }else {
                                    Toast.makeText(mContext, "提交失败，请稍后尝试！", Toast.LENGTH_LONG).show();
                                }
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call call, IOException e) {}
                });
            }
        });
    }

    private void setFollowUserView(int type, int uid){
        getUserList(type, uid, -1);
    }
    
    private void getUserList(final int type, int uid, int pid){
        RequestBody requestBody = null;
        String url = "";
        if(type == PERSONALITY){
            requestBody = new FormBody.Builder().add("pid", String.valueOf(pid)).build();
            url = GET_APPROVED_USERS_URL;
        }

        if(type == FOLLOWED){
            requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
            url = GET_FOLLOW_USERS_URL+"followed";
        }

        if(type == FOLLOWING){
            requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
            url = GET_FOLLOW_USERS_URL+"following";
        }

        HttpUtil.sendOkHttpRequest(mContext, url, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.body() != null){
                    try {
                        String responseText = response.body().string();
                        Slog.d(TAG, "==========getPersonality  response text : "+responseText);
                        JSONObject responseObj = new JSONObject(responseText);
                        JSONArray responseArray = responseObj.optJSONArray("users");
                        int guestUid = -1;
                        if(type == PERSONALITY){
                            guestUid = responseObj.optInt("guest_uid");
                        }

                        if(responseArray.length() > 0){
                            for (int i=0; i<responseArray.length(); i++){
                                MeetMemberInfo meetMemberInfo = new MeetMemberInfo();
                                JSONObject member = responseArray.optJSONObject(i);
                                if(type == PERSONALITY){
                                    if(guestUid == member.optInt("uid")){
                                        //approve.setVisibility(View.INVISIBLE);
                                        handler.sendEmptyMessage(HIDE_APPROVE);
                                    }
                                }
                                meetMemberInfo.setUid(member.optInt("uid"));
                                meetMemberInfo.setSex(member.optInt("sex"));
                                meetMemberInfo.setRealname(member.optString("realname"));
                                meetMemberInfo.setPictureUri(member.optString("picture_uri"));
                                meetMemberInfo.setSituation(member.optInt("situation"));
                                if(member.optInt("situation") == 0){//student
                                    meetMemberInfo.setDegree(member.optString("degree"));
                                    meetMemberInfo.setMajor(member.optString("major"));
                                    meetMemberInfo.setUniversity(member.optString("university"));
                                }else {
                                    meetMemberInfo.setCompany(member.optString("company"));
                                    meetMemberInfo.setJobTitle(member.optString("job_title"));
                                    meetMemberInfo.setLives(member.optString("lives"));
                                }
                                memberInfoList.add(meetMemberInfo);
                            }
                            handler.sendEmptyMessage(LOAD_USERS_DONE);
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                     }
            }
            @Override
            public void onFailure(Call call, IOException e) {}
        });
    }
    
    static class MyHandler extends Handler {
        WeakReference<CommonUserListDialogFragment> commonUserListDialogFragmentWeakReference;

        MyHandler(CommonUserListDialogFragment personalityDialogFragment) {
            commonUserListDialogFragmentWeakReference = new WeakReference<CommonUserListDialogFragment>(personalityDialogFragment);
        }

        @Override
        public void handleMessage(Message message) {
            CommonUserListDialogFragment personalityDialogFragment = commonUserListDialogFragmentWeakReference.get();
            if(personalityDialogFragment != null){
                personalityDialogFragment.handleMessage(message);
            }
        }
    }
    
    public void handleMessage(Message message) {
        switch (message.what) {
            case LOAD_USERS_DONE:
                    personalityApprovedAdapter.setData(memberInfoList);
                    personalityApprovedAdapter.notifyDataSetChanged();
                    break;
            case HIDE_APPROVE:
                approve.setVisibility(View.INVISIBLE);
            default:
                break;
        }
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
