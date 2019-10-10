package com.hetang.meet;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.hetang.util.FontManager;

import com.hetang.launchak47.R;
import com.hetang.adapter.ImpressionApprovedDetailAdapter;
import com.hetang.util.HttpUtil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApprovedUsersActivity extends AppCompatActivity {

    private static final String TAG = "ApprovedUsersActivity";
    MeetArchivesActivity.ImpressionStatistics impressionStatistics = null;
    private Context mContext;
    private Dialog mDialog;
    private View view;
    private RecyclerView mUsersDetailList;
    private ImpressionApprovedDetailAdapter approvedDetailAdapter;
    private LayoutInflater inflater;
    private static final String IMPRESSION_APPROVE_URL = HttpUtil.DOMAIN + "?q=meet/impression/approve";

    /*
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.impression_approved_users_detail);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        
        mUsersDetailList = findViewById(R.id.users_detail);
        
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mUsersDetailList.setLayoutManager(linearLayoutManager);
        approvedDetailAdapter = new ImpressionApprovedDetailAdapter(mContext);
        mUsersDetailList.setAdapter(approvedDetailAdapter);
        final int uid = getIntent().getIntExtra("uid", -1);
        impressionStatistics = getIntent().getParcelableExtra("impressionStatistics");

        approvedDetailAdapter.setData(impressionStatistics.meetMemberList);
        approvedDetailAdapter.notifyDataSetChanged();
        TextView back = findViewById(R.id.left);
        TextView approve = findViewById(R.id.approve);
        TextView title = findViewById(R.id.title);
        title.setText(impressionStatistics.impression+" · "+impressionStatistics.impressionCount);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        
        approve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                set_impression_approve(uid);
                finish();
            }
        });

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.custom_actionbar), font);
    }

    private void set_impression_approve(int uid){
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .add("features", impressionStatistics.impression).build();
        HttpUtil.sendOkHttpRequest(ApprovedUsersActivity.this, IMPRESSION_APPROVE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                /*
                if(response.body() != null){
                    String responseText = response.body().string();
                    Slog.d(TAG, "==========loadReferences response text : "+responseText);
                    if(responseText != null){
                        List<MeetReferenceInfo> meetReferenceInfoList = ParseUtils.getMeetReferenceList(responseText);
                        if(meetReferenceInfoList != null && meetReferenceInfoList.size() > 0){
                            mReferenceList.addAll(meetReferenceInfoList);
                        }
                        handler.sendEmptyMessage(LOAD_REFERENCE_DONE);
                    }
                }
                */
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });

    }
    /*
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

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
        layoutParams.gravity = Gravity.BOTTOM;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
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
    */
}
