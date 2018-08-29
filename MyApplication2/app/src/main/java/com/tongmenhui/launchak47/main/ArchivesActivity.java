package com.tongmenhui.launchak47.main;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.meet.MeetMemberInfo;

public class ArchivesActivity extends BaseAppCompatActivity {
    private static final String TAG = "ArchivesActivity";

    private ImageView backLeft;
    private MeetMemberInfo mMeetMember;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.archives);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.hide();
        }

        backLeft = findViewById(R.id.left_back);
        backLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mMeetMember = (MeetMemberInfo) getIntent().getSerializableExtra("meet");
        Log.d(TAG, "onCreate mMeetMember:"+mMeetMember+" "+getIntent().getStringExtra("test"));
    }
}
