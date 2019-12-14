package com.hetang.home;

//import android.app.Fragment;

import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hetang.R;
import com.hetang.adapter.CommonContactsListAdapter;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.common.HandlerTemp;
import com.hetang.common.MyApplication;
import com.hetang.meet.UserMeetInfo;
import com.hetang.util.DynamicOperationDialogFragment;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.InterActInterface;
import com.hetang.util.Slog;
import com.hetang.util.UserProfile;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.hetang.home.HomeFragment.DEFAULT_RECOMMEND_COUNT;
import static com.hetang.home.HomeFragment.GET_HOME_RECOMMEND_PERSON_URL;
import static com.hetang.home.HomeFragment.GET_RECOMMEND_MEMBER_DONE;
import static com.hetang.home.HomeFragment.NO_RECOMMEND_MEMBER_DONE;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

/**
 * Created by super-zou on 17-9-11.
 */

public class CommonContactsActivity extends BaseAppCompatActivity {
    private static final boolean isDebug = true;
    private static final String TAG = "CommonContactsActivity";
    //+Begin add by xuchunping for use XRecyclerView support loadmore
    //private RecyclerView recyclerView;
    private int type;
    private int gid;
    private boolean isLeader;
    private int mLoadSize = 0;
    public static final int GROUP_MEMBER = 2;
    private static final int PAGE_SIZE = 12;//page size
    private static final int GET_ALL_DONE = 1;
    private static final int GET_ALL_END = 2;
    private static final int NO_MORE = 3;
    public static final String GET_ALL_MEMBERS_URL = HttpUtil.DOMAIN + "?q=subgroup/get_all_members";

    private XRecyclerView recyclerView;
    private int mTempSize;
    private CommonContactsListAdapter commonContactsListAdapter;
    private Handler handler;
    Typeface font;
    int currentPosition = -1;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    private List<UserProfile> contactsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recommend_contacts);
        font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.custom_actionbar), font);
        initView();
    }

    protected void initView() {
        handler = new MyHandler(this);
        type = getIntent().getIntExtra("type", -1);
        if (type == GROUP_MEMBER){
            gid = getIntent().getIntExtra("gid", -1);
            isLeader = getIntent().getBooleanExtra("isLeader", false);
        }
        TextView back = findViewById(R.id.left_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        commonContactsListAdapter = new CommonContactsListAdapter(MyApplication.getContext(), type);
        //viewContent = view.inflate(R.layout.meet_recommend, container, false);

        recyclerView = findViewById(R.id.contacts_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MyApplication.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    commonContactsListAdapter.setScrolling(false);
                    commonContactsListAdapter.notifyDataSetChanged();
                } else {
                    commonContactsListAdapter.setScrolling(true);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        commonContactsListAdapter.setOnItemClickListener(new InterActInterface() {
            @Override
            public void onCommentClick(View view, int position) {}

            @Override
            public void onPraiseClick(View view, int position) {}

            @Override
            public void onDynamicPictureClick(View view, int position, String[] pictureUrlArray, int index) {}

            @Override
            public void onOperationClick(View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putInt("uid", contactsList.get(position).getUid());
                //currentPos = position;
                DynamicOperationDialogFragment dynamicOperationDialogFragment = new DynamicOperationDialogFragment();
                dynamicOperationDialogFragment.setArguments(bundle);
                dynamicOperationDialogFragment.show(getSupportFragmentManager(), "DynamicOperationDialogFragment");
            }

        });

        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setRefreshProgressStyle(BallSpinFadeLoader);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallRotate);
        //mRecyclerView.setArrowImageView(R.drawable.);

        recyclerView.getDefaultRefreshHeaderView().setRefreshTimeVisible(false);
        recyclerView.setPullRefreshEnabled(false);

        recyclerView.getDefaultFootView().setLoadingHint(getString(R.string.loading_pull_up_tip));
        recyclerView.getDefaultFootView().setNoMoreHint(getString(R.string.no_more));
        //recyclerView.setArrowImageView(R.drawable.iconfont_downgrey);//TODO set pull down icon
        final int itemLimit = 6;

        // When the item number of the screen number is list.size-2,we call the onLoadMore
        // recyclerView.setLimitNumberToCallLoadMore(itemLimit);
        recyclerView.setRefreshProgressStyle(ProgressStyle.BallBeat);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);

        recyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
            }

            @Override
            public void onLoadMore() {
                //getRecommendContent();
                loadData();
            }
        });

        recyclerView.setAdapter(commonContactsListAdapter);

        //show progressImage before loading done
        progressImageView = findViewById(R.id.animal_progress);
        animationDrawable = (AnimationDrawable) progressImageView.getDrawable();
        progressImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        }, 50);

        loadData();
    }

    private void loadData() {
        final int page = contactsList.size() / PAGE_SIZE;
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page));
        String uri = GET_HOME_RECOMMEND_PERSON_URL;
        if (type == GROUP_MEMBER){
            builder.add("gid", String.valueOf(gid));
            uri = GET_ALL_MEMBERS_URL;
        }
        RequestBody requestBody = builder.build();

        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), uri, requestBody, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug)
                    Slog.d(TAG, "----------------->loadData response : " + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    mLoadSize = processResponseText(responseText);
                }
                if (mLoadSize == PAGE_SIZE) {
                    handler.sendEmptyMessage(GET_ALL_DONE);
                } else {
                    if (mLoadSize != 0) {
                        handler.sendEmptyMessage(GET_ALL_END);
                    } else {
                        handler.sendEmptyMessage(NO_MORE);
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }

    private int processResponseText(String responseText) {
        JSONArray contactsArray = new JSONArray();
        try {
            contactsArray = new JSONObject(responseText).optJSONArray("members");
            if (contactsArray == null || contactsArray.length() == 0){
                return 0;
            }
            JSONObject contactsObject;
            for (int i = 0; i < contactsArray.length(); i++) {
                UserProfile contacts = new UserProfile();
                contactsObject = contactsArray.getJSONObject(i);
                contacts.setAvatar(contactsObject.optString("avatar"));
                contacts.setUid(contactsObject.optInt("uid"));
                contacts.setName(contactsObject.optString("name"));
                contacts.setSex(contactsObject.optInt("sex"));
                contacts.setSituation(contactsObject.optInt("situation"));
                if (type == GROUP_MEMBER){
                    contacts.setLeader(isLeader);
                }

                if (contactsObject.optInt("situation") == 0) {
                    contacts.setMajor(contactsObject.optString("major"));
                    contacts.setDegree(contactsObject.optString("degree"));
                    contacts.setUniversity(contactsObject.optString("university"));
                } else {
                    contacts.setIndustry(contactsObject.optString("industry"));
                    contacts.setPosition(contactsObject.optString("position"));
                }
                contactsList.add(contacts);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return contactsArray.length();
    }


    public void handleMessage(Message message) {
        switch (message.what) {
            case GET_ALL_DONE:
                Slog.d(TAG, "-------------->GET_ALL_DONE");
                commonContactsListAdapter.setData(contactsList);
                commonContactsListAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();
                // recyclerView.loadMoreComplete();
                stopLoadProgress();
                break;
            case GET_ALL_END:
                Slog.d(TAG, "-------------->GET_ALL_END");
                commonContactsListAdapter.setData(contactsList);
                commonContactsListAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();
                recyclerView.loadMoreComplete();
                recyclerView.setNoMore(true);
                stopLoadProgress();
                break;
            case NO_MORE:
                recyclerView.setNoMore(true);
                recyclerView.loadMoreComplete();
                stopLoadProgress();
                break;

            default:
                break;
        }
    }

    private void stopLoadProgress() {
        if (progressImageView.getVisibility() == View.VISIBLE) {
            animationDrawable.stop();
            progressImageView.setVisibility(View.GONE);
        }
    }


    static class MyHandler extends HandlerTemp<CommonContactsActivity> {
        public MyHandler(CommonContactsActivity cls) {
            super(cls);
        }

        @Override
        public void handleMessage(Message message) {
            CommonContactsActivity commonContactsActivity = ref.get();
            if (commonContactsActivity != null) {
                commonContactsActivity.handleMessage(message);
            }
        }
    }

}
