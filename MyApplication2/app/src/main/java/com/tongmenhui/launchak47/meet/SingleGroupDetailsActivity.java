package com.tongmenhui.launchak47.meet;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.tongmenhui.launchak47.R;

public class SingleGroupDetailsActivity extends AppCompatActivity {
    private static final String TAG = "SingleGroupDetailsActivity";
    private static final boolean isDebug = true;
    private Context mContext;
    private static final String SINGLE_GROUP_GET_BY_GID = HttpUtil.DOMAIN + "?q=single_group/get_by_gid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_group_details);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        final int gid = getIntent().getIntExtra("gid", -1);

    }
}
