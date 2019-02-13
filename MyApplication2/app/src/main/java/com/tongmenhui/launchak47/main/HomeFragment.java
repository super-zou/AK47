package com.tongmenhui.launchak47.main;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.adapter.ArchivesListAdapter;
import com.tongmenhui.launchak47.adapter.HomePageAdapter;
import com.tongmenhui.launchak47.util.BaseFragment;
import com.tongmenhui.launchak47.util.FontManager;
import com.tongmenhui.launchak47.util.MyApplication;

import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class HomeFragment extends BaseFragment {
    private static final String TAG = "MeetRecommendFragment";
    private static final boolean isDebug = true;
    private Context mContext;
    private View homeRecommend;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = MyApplication.getContext();
        View viewContent = inflater.inflate(R.layout.home_page, container, false);
        initView(viewContent);

        return viewContent;
    }
    
    @Override
    protected void initView(View view) {
                Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(view.findViewById(R.id.home_page), font);
        final HomePageAdapter homePageAdapter = new HomePageAdapter(mContext);
        XRecyclerView xRecyclerView = view.findViewById(R.id.home_page_recycler_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(linearLayoutManager);
        xRecyclerView.setRefreshProgressStyle(BallSpinFadeLoader);
        xRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallRotate);

        xRecyclerView.getDefaultRefreshHeaderView().setRefreshTimeVisible(true);
        xRecyclerView.setPullRefreshEnabled(false);

        homeRecommend = LayoutInflater.from(mContext).inflate(R.layout.home_page_recommend, (ViewGroup) view.findViewById(android.R.id.content), false);
        xRecyclerView.addHeaderView(homeRecommend);
        
                xRecyclerView.getDefaultFootView().setLoadingHint(getString(R.string.loading_pull_up_tip));
        xRecyclerView.getDefaultFootView().setNoMoreHint(getString(R.string.loading_no_more));
        final int itemLimit = 5;
        // When the item number of the screen number is list.size-2,we call the onLoadMore
        xRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                homePageAdapter.notifyDataSetChanged();
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        xRecyclerView.setLimitNumberToCallLoadMore(itemLimit);
        xRecyclerView.setRefreshProgressStyle(ProgressStyle.BallBeat);
        xRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);
        
        xRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
//                updateData();
            }

            @Override
            public void onLoadMore() {
               // loadDynamicsData(mMeetMember.getUid());
            }
        });

        xRecyclerView.setAdapter(homePageAdapter);

    }
    
    @Override
    protected void loadData() {

    }

    @Override
    protected int getLayoutId() {
        return 0;
    }
}
