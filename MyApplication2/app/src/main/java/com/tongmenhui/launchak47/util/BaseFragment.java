package com.tongmenhui.launchak47.util;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by super-zou on 18-1-20.
 */

public abstract class BaseFragment extends Fragment {
    private static final String TAG = "BaseFragment";
    private Activity activity;
    private boolean isVisible = false;//当前Fragment是否可见
    private boolean isInitView = false;//是否与View建立起映射关系
    private boolean isFirstLoad = true;//是否是第一次加载数据

    private View convertView;
    private SparseArray<View> mViews;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Slog.d(TAG, "   " + this.getClass().getSimpleName()+"====onCreateView========================================");
        convertView = inflater.inflate(getLayoutId(), container, false);
        mViews = new SparseArray<>();
        initView(convertView);
        //isInitView = true;
        //lazyLoadData();
        return convertView;
    }

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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        Slog.d(TAG, "isVisibleToUser " + isVisibleToUser + "   " + this.getClass().getSimpleName());
        if (isVisibleToUser) {
            isVisible = true;
            lazyLoadData();

        } else {
            isVisible = false;
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    /**
     * 加载页面布局文件
     * @return
     */
    protected abstract int getLayoutId();

    protected abstract void initView(View view);

    /*
    ** If first load data, init set true.
     */
    protected abstract void loadData(boolean init);

    private void lazyLoadData() {
        if (isFirstLoad) {
            Slog.d(TAG, "第一次加载 " + "  isVisible  " + isVisible + "   " + this.getClass().getSimpleName());
        } else {
            Slog.d(TAG, "不是第一次加载" + "  isVisible  " + isVisible + "   " + this.getClass().getSimpleName());
        }

        if (isVisible) {
            if(isFirstLoad){
                Slog.d(TAG, "========完成数据第一次加载");
                loadData(true);
                isFirstLoad = false;
            }else {
                Slog.d(TAG, "========完成数据更新");
                loadData(false);
            }
        }else{
            Slog.d(TAG, "=======不加载" + "   " + this.getClass().getSimpleName());
            return;
        }

    }

    /**
     * fragment中可以通过这个方法直接找到需要的view，而不需要进行类型强转
     * @param viewId
     * @param <E>
     * @return
     */
    protected <E extends View> E findView(int viewId) {
        if (convertView != null) {
            E view = (E) mViews.get(viewId);
            if (view == null) {
                view = (E) convertView.findViewById(viewId);
                mViews.put(viewId, view);
            }
            return view;
        }
        return null;
    }
}
