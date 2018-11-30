package com.tongmenhui.launchak47.meet;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.tongmenhui.launchak47.util.FontManager;

import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.adapter.ImpressionApprovedDetailAdapter;

public class ApprovedUsersActivity extends AppCompatActivity {

    private static final String TAG = "ApprovedUsersActivity";
    ArchivesActivity.ImpressionStatistics impressionStatistics = null;
    private Context mContext;
    private Dialog mDialog;
    private View view;
    private RecyclerView mUsersDetailList;
    private ImpressionApprovedDetailAdapter approvedDetailAdapter;
    private LayoutInflater inflater;

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
        int uid = getIntent().getIntExtra("uid", -1);
        impressionStatistics = getIntent().getParcelableExtra("impressionStatistics");

        approvedDetailAdapter.setData(impressionStatistics.meetMemberList);
        approvedDetailAdapter.notifyDataSetChanged();
                TextView back = findViewById(R.id.left);
        TextView approve = findViewById(R.id.approve);
        TextView title = findViewById(R.id.title);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        
                approve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.custom_actionbar), font);
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
