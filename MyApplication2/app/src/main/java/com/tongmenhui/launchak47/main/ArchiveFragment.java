package com.tongmenhui.launchak47.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;

import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.util.BaseFragment;

/**
 * Created by super-zou on 17-9-11.
 */

public class ArchiveFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_archive, container, false);
    }

    @Override
    protected void initView(View view){

    }

    @Override
    protected void loadData(boolean firstInit){

    }

    @Override
    protected int getLayoutId(){
        return 0;
    }
}
