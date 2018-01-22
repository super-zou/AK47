package com.tongmenhui.launchak47.util;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

/**
 * Created by super-zou on 18-1-20.
 */

public class BaseFragment extends Fragment {
    private Activity activity;

    public Context getContext(){
        if(activity == null){
            return MyApplication.getContext();
        }
        return activity;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity = getActivity();
    }
}
