package com.tongmenhui.launchak47.meet;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.util.BaseFragment;
import com.tongmenhui.launchak47.util.Slog;

public class MeetSingleGroupFragment extends BaseFragment {
    private static final boolean debug = false;
    private static final String TAG = "MeetSingleGroupFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.meet_discovery, container, false);
    }
    
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (debug) Slog.d(TAG, "=================onViewCreated===================");
        // initConentView();
    }

    @Override
    protected void initView(View view) {

    }
    
    @Override
    protected void loadData() {

    }

    @Override
    protected int getLayoutId() {
        return 0;
    }
    
 }
