package com.hetang.home;

//import android.app.Fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hetang.adapter.MeetRecommendListAdapter;
import com.hetang.adapter.RecommendContactsListAdapter;
import com.hetang.util.InterActInterface;
import com.hetang.common.MyApplication;
import com.hetang.util.ParseUtils;
import com.hetang.util.SetAvatarActivity;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.hetang.R;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.common.DynamicsInteractDetailsActivity;
import com.hetang.common.HandlerTemp;
import com.hetang.meet.FillMeetInfoActivity;
import com.hetang.meet.MeetArchiveActivity;
import com.hetang.meet.UserMeetInfo;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.SharedPreferencesUtils;
import com.hetang.util.Slog;
import com.hetang.util.UserProfile;

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
import static com.hetang.common.AddPictureActivity.ADD_PICTURE_BROADCAST;
import static com.hetang.common.DynamicsInteractDetailsActivity.MEET_RECOMMEND_COMMENT;
import static com.hetang.home.HomeFragment.COMMENT_UPDATE_RESULT;
import static com.hetang.meet.MeetDynamicsFragment.COMMENT_COUNT_UPDATE;

/**
 * Created by super-zou on 17-9-11.
 */

public class MoreRecommendActivity extends BaseAppCompatActivity {
    private static final boolean isDebug = true;
    private static final String TAG = "MoreRecommendActivity";
    //+Begin add by xuchunping for use XRecyclerView support loadmore
    //private RecyclerView recyclerView;
    private static final int PAGE_SIZE = 3;//page size
    private static final int DONE = 1;
    private static final int UPDATE = 2;
    private static final int GET_USER_PROFILE_DONE = 3;
    
    private List<UserMeetInfo> meetList = new ArrayList<>();
    private UserProfile userProfile;
    private XRecyclerView recyclerView;
    private int mTempSize;
    private RecommendContactsListAdapter recommendContactsListAdapter;
    private int uid;
    private boolean loaded = false;
    private Handler handler;
    Typeface font;
    int currentPosition = -1;
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

        TextView back = findViewById(R.id.left_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        
        recommendContactsListAdapter = new RecommendContactsListAdapter(MyApplication.getContext());
        //viewContent = view.inflate(R.layout.meet_recommend, container, false);

        recyclerView = findViewById(R.id.contacts_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MyApplication.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    recommendContactsListAdapter.setScrolling(false);
                    recommendContactsListAdapter.notifyDataSetChanged();
                } else {
                    recommendContactsListAdapter.setScrolling(true);
                }
                super.onScrollStateChanged(recyclerView, newState);
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
            public void onRefresh() {}

            @Override
            public void onLoadMore() {
                //getRecommendContent();
                loadData();
            }
        });
        
        recyclerView.setAdapter(recommendContactsListAdapter);

        loadData();
    }
    
    private void loadData(){
        RequestBody requestBody = new FormBody.Builder()
                .add("step", String.valueOf(DEFAULT_RECOMMEND_COUNT))
                .add("page", String.valueOf(0)).build();
        
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_HOME_RECOMMEND_PERSON_URL, requestBody, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug) Slog.d(TAG, "----------------->getRecommendUsers response : " + responseText);
                if (!TextUtils.isEmpty(responseText)){
                    processResponseText(responseText);
                }
                if(contactsList.size() > 0){
                    handler.sendEmptyMessage(GET_RECOMMEND_MEMBER_DONE);
                }else {
                    handler.sendEmptyMessage(NO_RECOMMEND_MEMBER_DONE);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }
    
    private void processResponseText(String responseText){
        try {
            JSONArray contactsArray = new JSONObject(responseText).optJSONArray("recommend_users");
            JSONObject contactsObject;
            for (int i=0; i<contactsArray.length(); i++){
                UserProfile contacts = new UserProfile();
                contactsObject = contactsArray.getJSONObject(i);
                contacts.setAvatar(contactsObject.optString("avatar"));
                contacts.setUid(contactsObject.optInt("uid"));
                contacts.setName(contactsObject.optString("name"));
                contacts.setSex(contactsObject.optInt("sex"));
                contacts.setSituation(contactsObject.optInt("situation"));
                
                if(contactsObject.optInt("situation") == 0){
                    contacts.setMajor(contactsObject.optString("major"));
                    contacts.setDegree(contactsObject.optString("degree"));
                    contacts.setUniversity(contactsObject.optString("university"));
                }else {
                    contacts.setIndustry(contactsObject.optString("industry"));
                    contacts.setPosition(contactsObject.optString("position"));
                }
                contactsList.add(contacts);
            }
            
             }catch (JSONException e){
            e.printStackTrace();
        }
    }


    public void handleMessage(Message message) {
        switch (message.what){
            case NO_RECOMMEND_MEMBER_DONE:
                recyclerView.setNoMore(true);
                recyclerView.loadMoreComplete();
                //stopLoadProgress();
                break;
                
                case GET_RECOMMEND_MEMBER_DONE:
                recommendContactsListAdapter.setData(contactsList);
                recommendContactsListAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();
                if (mTempSize < PAGE_SIZE) {
                    //loading finished
                    recyclerView.setNoMore(true);
                    //recyclerView.setLoadingMoreEnabled(false);
                }
                break;
                
                default:
                break;
        }
    }
    

    static class MyHandler extends HandlerTemp<MoreRecommendActivity> {
        public MyHandler(MoreRecommendActivity cls){
            super(cls);
        }

        @Override
        public void handleMessage(Message message) {
            MoreRecommendActivity moreRecommendActivity = ref.get();
            if (moreRecommendActivity != null) {
                moreRecommendActivity.handleMessage(message);
            }
        }
    }

}
