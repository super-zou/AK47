package com.hetang.util;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hetang.R;
import com.hetang.adapter.PersonalityApprovedAdapter;
//import com.hetang.meet.MeetMemberInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hetang.archive.ArchiveFragment.REQUESTCODE;
import static com.hetang.main.MeetArchiveFragment.RESULT_OK;

public class CommonUserListDialogFragment extends DialogFragment {
    private static final String TAG = "CommonUserListDialogFragment";
    private static final int LOAD_USERS_DONE = 0;
    private static final int APPROVED = 1;
    private static final int APPROVE_DONE = 2;
    private static final int PERSONALITY = 0;
    private static final int FOLLOWED = 1;
    private static final int FOLLOWING = 2;
    private static final int PRAISED = 3;
    private static final int PRAISE = 4;
    private static final int LOVED = 5;
    private static final int LOVE = 6;
    private static final int DYNAMICS_PRAISED = 7;
    private static final String GET_APPROVED_USERS_URL = HttpUtil.DOMAIN + "?q=meet/personality/approved_users";
    private static final String APPROVE_PERSONALITY_URL = HttpUtil.DOMAIN + "?q=meet/personality/approve";
    private static final String GET_FOLLOW_USERS_URL = HttpUtil.DOMAIN + "?q=follow/get/";
    private static final String GET_LOVE_USERS_URL = HttpUtil.DOMAIN + "?q=meet/love_detail/";
    private static final String GET_PRAISE_USERS_URL = HttpUtil.DOMAIN + "?q=meet/praise_detail/";
    private static final String GET_DYNAMICS_PRAISED_DETAIL_URL = HttpUtil.DOMAIN + "?q=dynamic/praised_detail";
    public List<UserProfile> memberInfoList = new ArrayList<>();
    private Context mContext;
    private Dialog mDialog;
    private View view;
    private Button approve;
    private LayoutInflater inflater;
    private Handler handler;
    private PersonalityApprovedAdapter personalityApprovedAdapter;
    private ProgressDialog progressDialog;
    private boolean writeDone = false;
    private int type;
    int count = 0;
    TextView titleText;
    String personalityName = "";
    ImageView progressImageView;
    AnimationDrawable animationDrawable;

    private static final int RESPONSE_FAILED = 0;
    private static final int RESPONSE_SUCCESS = 1;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        handler = new CommonUserListDialogFragment.MyHandler(CommonUserListDialogFragment.this);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        int uid;
        int did = 0;
        String title = "";
        Bundle bundle = getArguments();

        initView();

        titleText = view.findViewById(R.id.title_text);

        handler = new CommonUserListDialogFragment.MyHandler(CommonUserListDialogFragment.this);
        type = bundle.getInt("type", 0);

        switch (type) {
            case PERSONALITY:
                int pid = -1;
                if (bundle != null) {
                    personalityName = bundle.getString("personality");
                    pid = bundle.getInt("pid");
                    count = bundle.getInt("count");
                }
                titleText.setText(personalityName + " · " + count);
                setApprovedUserView(pid);
                break;
            case FOLLOWED:
                uid = bundle.getInt("uid");
                title = bundle.getString("title");
                titleText.setText(title);
                setUserView(type, uid);
                break;
            case FOLLOWING:
                uid = bundle.getInt("uid");
                title = bundle.getString("title");
                titleText.setText(title);
                setUserView(type, uid);
                break;
            case PRAISED:
                uid = bundle.getInt("uid");
                title = bundle.getString("title");
                titleText.setText(title);
                setUserView(type, uid);
                break;
            case PRAISE:
                uid = bundle.getInt("uid");
                title = bundle.getString("title");
                titleText.setText(title);
                setUserView(type, uid);
                break;
            case LOVED:
                uid = bundle.getInt("uid");
                title = bundle.getString("title");
                titleText.setText(title);
                setUserView(type, uid);
                break;
            case LOVE:
                uid = bundle.getInt("uid");
                title = bundle.getString("title");
                titleText.setText(title);
                setUserView(type, uid);
                break;
            case DYNAMICS_PRAISED:
                did = (int)bundle.getLong("did");
                title = bundle.getString("title");
                titleText.setText(title);
                setUserView(type, did);
                break;
                default:
                    break;
        }

        return mDialog;
    }

    private void initView() {
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
        
        progressImageView = view.findViewById(R.id.animal_progress);
        animationDrawable = (AnimationDrawable)progressImageView.getDrawable();
        progressImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        },50);

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

    private void setApprovedUserView(final int pid) {
        approve = view.findViewById(R.id.approve);
        approve.setVisibility(View.VISIBLE);

        getUserList(PERSONALITY, -1, pid);

        approve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Slog.d(TAG, "=======================approve click");
                showProgress(getContext().getResources().getString(R.string.saving_progress));
                RequestBody requestBody = new FormBody.Builder()
                        .add("pid", String.valueOf(pid)).build();
                HttpUtil.sendOkHttpRequest(mContext, APPROVE_PERSONALITY_URL, requestBody, new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.body() != null) {
                            try {
                                String responseText = response.body().string();
                                Slog.d(TAG, "==========getPersonality  response text : " + responseText);
                                boolean status = new JSONObject(responseText).optBoolean("status");
                                if (status == true) {
                                    writeDone = true;
                                    handler.sendEmptyMessage(APPROVE_DONE);
                                    //mDialog.dismiss();
                                    closeProgressDialog();
                                } else {
                                    Toast.makeText(mContext, "提交失败，请稍后尝试！", Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                    }
                });
            }
        });
    }

    private void setUserView(int type, int id) {
        getUserList(type, id, -1);
    }

    private void getUserList(final int type, int id, int pid) {
        RequestBody requestBody = null;
        String url = "";
        switch (type) {
            case PERSONALITY:
                requestBody = new FormBody.Builder().add("pid", String.valueOf(pid)).build();
                url = GET_APPROVED_USERS_URL;
                break;
            case FOLLOWED:
                requestBody = new FormBody.Builder().add("uid", String.valueOf(id)).build();
                url = GET_FOLLOW_USERS_URL + "followed";
                break;
            case FOLLOWING:
                requestBody = new FormBody.Builder().add("uid", String.valueOf(id)).build();
                url = GET_FOLLOW_USERS_URL + "following";
                break;
            case PRAISED:
                requestBody = new FormBody.Builder().add("uid", String.valueOf(id)).build();
                url = GET_PRAISE_USERS_URL + "praised";
                break;
            case PRAISE:
                requestBody = new FormBody.Builder().add("uid", String.valueOf(id)).build();
                url = GET_PRAISE_USERS_URL + "praise";
                break;
            case LOVED:
                requestBody = new FormBody.Builder().add("uid", String.valueOf(id)).build();
                url = GET_LOVE_USERS_URL + "loved";
                break;
            case LOVE:
                requestBody = new FormBody.Builder().add("uid", String.valueOf(id)).build();
                url = GET_LOVE_USERS_URL + "love";
                break;
            case DYNAMICS_PRAISED:
                requestBody = new FormBody.Builder().add("did", String.valueOf(id)).build();
                url = GET_DYNAMICS_PRAISED_DETAIL_URL;
                break;
            default:
                break;
        }
        HttpUtil.sendOkHttpRequest(mContext, url, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    try {
                        String responseText = response.body().string();
                        Slog.d(TAG, "==========getUserList  response text : " + responseText);
                        JSONObject responseObj = new JSONObject(responseText);
                        JSONArray responseArray = responseObj.optJSONArray("users");
                        int guestUid = -1;
                        if (type == PERSONALITY) {
                            guestUid = responseObj.optInt("guest_uid");
                        }

                        if (responseArray != null && responseArray.length() > 0) {
                            for (int i = 0; i < responseArray.length(); i++) {
                                UserProfile userProfile = new UserProfile();
                                JSONObject member = responseArray.optJSONObject(i);
                                if (type == PERSONALITY) {
                                    if (guestUid == member.optInt("uid")) {
                                        //approve.setVisibility(View.INVISIBLE);
                                        handler.sendEmptyMessage(APPROVED);
                                    }
                                }

                                userProfile = ParseUtils.getUserProfileFromJSONObject(member);
                                memberInfoList.add(userProfile);
                            }
                        }

                        handler.sendEmptyMessage(LOAD_USERS_DONE);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }

    public void handleMessage(Message message) {
        switch (message.what) {
            case LOAD_USERS_DONE:
                if (memberInfoList.size() > 0){
                    personalityApprovedAdapter.setData(memberInfoList);
                    personalityApprovedAdapter.notifyDataSetChanged();
                }
                stopLoadProgress();
                break;
            case APPROVED:
                approve.setVisibility(View.VISIBLE);
                approve.setText(getResources().getString(R.string.feature_approvied));
                approve.setBackgroundColor(mContext.getResources().getColor(R.color.color_blue));
                approve.setTextColor(mContext.getResources().getColor(R.color.white));
                approve.setClickable(false);
                approve.setEnabled(false);
                break;
            case APPROVE_DONE:
                stopLoadProgress();
                approve.setVisibility(View.VISIBLE);
                approve.setText(getResources().getString(R.string.feature_approvied));
                approve.setBackgroundColor(mContext.getResources().getColor(R.color.color_blue));
                approve.setTextColor(mContext.getResources().getColor(R.color.white));
                approve.setClickable(false);
                approve.setEnabled(false);
                count += 1;
                titleText.setText(personalityName + " · " + count);

            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getTargetFragment() != null) {
            if (type == PERSONALITY){
                Intent intent = new Intent();
                intent.putExtra("type", ParseUtils.TYPE_PERSONALITY);
                intent.putExtra("status", writeDone);
                getTargetFragment().onActivityResult(REQUESTCODE, RESULT_OK, intent);
            }
        }

        closeProgressDialog();

        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }
    
    private void showProgress(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage(message);
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void stopLoadProgress(){
        if (progressImageView.getVisibility() == View.VISIBLE){
            animationDrawable.stop();
            progressImageView.setVisibility(View.GONE);
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

    static class MyHandler extends Handler {
        WeakReference<CommonUserListDialogFragment> commonUserListDialogFragmentWeakReference;

        MyHandler(CommonUserListDialogFragment personalityDialogFragment) {
            commonUserListDialogFragmentWeakReference = new WeakReference<CommonUserListDialogFragment>(personalityDialogFragment);
        }

        @Override
        public void handleMessage(Message message) {
            CommonUserListDialogFragment personalityDialogFragment = commonUserListDialogFragmentWeakReference.get();
            if (personalityDialogFragment != null) {
                personalityDialogFragment.handleMessage(message);
            }
        }
    }
}
