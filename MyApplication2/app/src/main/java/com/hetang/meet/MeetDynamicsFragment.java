package com.hetang.meet;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.hetang.R;
import com.hetang.adapter.MeetDynamicsListAdapter;
import com.hetang.dynamics.AddDynamicsActivity;
import com.hetang.dynamics.Dynamic;
import com.hetang.dynamics.DynamicsInteractDetailsActivity;
import com.hetang.common.MyApplication;
import com.hetang.common.PicturePreviewActivity;
import com.hetang.home.HomeFragment;
import com.hetang.util.BaseFragment;
import com.hetang.util.CommonUserListDialogFragment;
import com.hetang.dynamics.DynamicOperationDialogFragment;
import com.hetang.util.HttpUtil;
import com.hetang.util.InterActInterface;
import com.hetang.util.ParseUtils;
import com.hetang.util.SharedPreferencesUtils;
import com.hetang.util.Slog;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.tools.DoubleUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.hetang.home.HomeFragment.GET_MY_NEW_ADD_DONE;
import static com.hetang.home.HomeFragment.GET_MY_NEW_ADD_DYNAMICS_URL;
import static com.hetang.util.ParseUtils.ADD_SUBGROUP_ACTIVITY_ACTION;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

/**
 * Created by haichao.zou on 2017/11/20.
 */

public class MeetDynamicsFragment extends BaseFragment {

    public static final int REQUEST_CODE = 1;
    private static final boolean isDebug = true;
    private static final String TAG = "MeetDynamicsFragment";
    public static final int MEET_DYNAMICS = 0;
    private static final int PAGE_SIZE = 6;
    public static final int NO_MORE_DYNAMICS = 0;
    public static final int LOAD_DYNAMICS_DONE = 1;
    public static final int HAVE_UPDATE = 2;
    public static final int NO_UPDATE = 3;
    public static final int UPDATE_COMMENT = 4;
    public static final int COMMENT_COUNT_UPDATE = 5;
    public static final int PRAISE_UPDATE = 6;
    public static final int LOVE_UPDATE = 7;
    public static final int MY_COMMENT_COUNT_UPDATE = 8;
    public static final int DYNAMICS_DELETE = 9;
    public static final int MY_LOVE_UPDATE = 10;

    public static final String DYNAMICS_DELETE_BROADCAST = "com.hetang.action.DYNAMICS_DELETE";
    public static final String GET_DYNAMICS_URL = HttpUtil.DOMAIN + "?q=dynamic/action/get";
    public static final String GET_DYNAMICS_UPDATE_URL = HttpUtil.DOMAIN + "?q=dynamic/action/update";
    public static final String REQUEST_INTERACT_URL = HttpUtil.DOMAIN + "?q=dynamic/interact/get";
    public static final String GET_DYNAMICS_WITH_ID_URL = HttpUtil.DOMAIN + "?q=dynamic/get_with_id";


    JSONObject dynamics_response;
    JSONObject commentResponse;
    JSONArray dynamics;
    JSONArray praiseArray;
    String requstUrl = "";
    RequestBody requestBody = null;
    private View viewContent;
    private List<Dynamic> meetList = new ArrayList<>();

    private Context mContext;
    private int mTempSize;
    private XRecyclerView recyclerView;
    //-End add by xuchunping for use XRecyclerView support loadmore

    private MeetDynamicsListAdapter meetDynamicsListAdapter;
    private Handler handler;
    private DynamicsAddBroadcastReceiver mReceiver = new DynamicsAddBroadcastReceiver();
    public static final int DYNAMICS_PRAISED = 7;
    private int currentPos = 0;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;

    @Override
    protected int getLayoutId() {
        int layoutId = R.layout.meet_dynamics;
        return layoutId;
    }

    @Override
    protected void initView(View convertView) {
        mContext = getContext();
        handler = new MyHandler(this);
        recyclerView = convertView.findViewById(R.id.recyclerview);
        meetDynamicsListAdapter = new MeetDynamicsListAdapter(getActivity(), getFragmentManager(), false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    meetDynamicsListAdapter.setScrolling(false);
                    meetDynamicsListAdapter.notifyDataSetChanged();
                } else {
                    meetDynamicsListAdapter.setScrolling(true);
                }

                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        //+Begin added by xuchunping
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setRefreshProgressStyle(BallSpinFadeLoader);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallRotate);
        //mRecyclerView.setArrowImageView(R.drawable.);

        recyclerView.getDefaultRefreshHeaderView().setRefreshTimeVisible(true);

        recyclerView.getDefaultFootView().setLoadingHint(getString(R.string.loading_pull_up_tip));
        recyclerView.getDefaultFootView().setNoMoreHint(getString(R.string.loading_no_more));
        final int itemLimit = 5;

        // When the item number of the screen number is list.size-2,we call the onLoadMore
        recyclerView.setLimitNumberToCallLoadMore(PAGE_SIZE - 2);
        recyclerView.setRefreshProgressStyle(ProgressStyle.BallBeat);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);

        recyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {

            @Override
            public void onRefresh() {
                requestData(false);
            }

            @Override
            public void onLoadMore() {
                requestData(true);
            }
        });

        recyclerView.scrollToPosition(0);

        //callback from meetDynamicsListAdapter, when comment icon touched, will show comment input dialog
        meetDynamicsListAdapter.setOnCommentClickListener(new InterActInterface() {
            @Override
            public void onCommentClick(View view, int position) {
                currentPos = position;
                //createCommentDetails(meetList.get(position).getDid(), meetList.get(position).getCommentCount());
                createCommentDetails(meetList.get(position));

            }

            @Override
            public void onPraiseClick(View view, int position) {

                Bundle bundle = new Bundle();
                bundle.putInt("type", DYNAMICS_PRAISED);
                bundle.putLong("did", meetList.get(position).getDid());
                bundle.putString("title", "赞了该动态");
                CommonUserListDialogFragment commonUserListDialogFragment = new CommonUserListDialogFragment();
                commonUserListDialogFragment.setArguments(bundle);
                commonUserListDialogFragment.show(getFragmentManager(), "CommonUserListDialogFragment");

            }

            @Override
            public void onDynamicPictureClick(View view, int position, String[] pictureUrlArray, int index) {
                startPicturePreview(index, pictureUrlArray);
            }

            @Override
            public void onOperationClick(View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putLong("did", meetList.get(position).getDid());
                currentPos = position;
                DynamicOperationDialogFragment dynamicOperationDialogFragment = new DynamicOperationDialogFragment();
                dynamicOperationDialogFragment.setArguments(bundle);
                dynamicOperationDialogFragment.show(getFragmentManager(), "DynamicOperationDialogFragment");
            }

        });

        recyclerView.setAdapter(meetDynamicsListAdapter);

        final FloatingActionButton dynamicCreate = convertView.findViewById(R.id.dynamic_create);

        dynamicCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyApplication.getContext(), AddDynamicsActivity.class);
                intent.putExtra("type", ParseUtils.ADD_MEET_DYNAMIC_ACTION);
                startActivityForResult(intent, Activity.RESULT_FIRST_USER);
            }
        });

        registerLoginBroadcast();

        //show progressImage before loading done
        progressImageView = convertView.findViewById(R.id.animal_progress);
        animationDrawable = (AnimationDrawable) progressImageView.getDrawable();
        progressImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        }, 50);
    }

    public void startPicturePreview(int position, String[] pictureUrlArray){
        List<LocalMedia> localMediaList = new ArrayList<>();
        for (int i=0; i<pictureUrlArray.length; i++){
            LocalMedia localMedia = new LocalMedia();
            localMedia.setPath(HttpUtil.getDomain()+pictureUrlArray[i]);
            localMediaList.add(localMedia);
        }

        //PictureSelector.create(MeetDynamicsFragment.this).externalPicturePreview(index, localMediaList);
        if (!DoubleUtils.isFastDoubleClick()) {
            Intent intent = new Intent(getContext(), PicturePreviewActivity.class);
            intent.putExtra(PictureConfig.EXTRA_PREVIEW_SELECT_LIST, (Serializable) localMediaList);
            intent.putExtra(PictureConfig.EXTRA_POSITION, position);
            getContext().startActivity(intent);
            //getContext().overridePendingTransition(R.anim.a5, 0);
        }
    }

    @Override
    protected void loadData() {
        requestData(true);
    }

    public List<Dynamic> getDynamicsResponse(String responseText, boolean isUpdate, Handler handler) {

        if (!TextUtils.isEmpty(responseText)) {
            try {
                dynamics_response = new JSONObject(responseText);
                //dynamics = dynamics_response.getJSONArray("activity");
                if (isUpdate == true) {
                    int current = dynamics_response.optInt("current");
                    if (isDebug) Slog.d(TAG, "----------------->current: " + current);
                    SharedPreferencesUtils.setDynamicsLast(getContext(), String.valueOf(current));
                }
                dynamics = dynamics_response.optJSONArray("dynamic");
                if (dynamics != null && dynamics.length() > 0) {
                    return setDynamicInfo(dynamics, handler);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public List<Dynamic> setDynamicInfo(JSONArray dynamicsArray, Handler handler) {
        List<Dynamic> tempList = new ArrayList<Dynamic>();
        int length = dynamicsArray.length();
        if (isDebug) Slog.d(TAG, "==========setDynamicInfo==========dynamics length: " + length);

        for (int i = 0; i < length; i++) {
            JSONObject dynamicJSONObject = dynamicsArray.optJSONObject(i);
            if (dynamicJSONObject != null) {
                Dynamic dynamic = setMeetDynamics(dynamicJSONObject);
                setDynamicsInteract(dynamic, handler);
                tempList.add(dynamic);
            }

        }

        return tempList;

    }

    public Dynamic setMeetDynamics(JSONObject dynamicJSONObject) {
        Dynamic dynamic = new Dynamic();

        if (isDebug) Slog.d(TAG, "==========dynamicsArray.getJSONObject: " + dynamicJSONObject);

        dynamic.setNickName(dynamicJSONObject.optString("nickname"));
        dynamic.setUid(dynamicJSONObject.optInt("uid"));

        if(dynamicJSONObject.optInt("type") == ADD_SUBGROUP_ACTIVITY_ACTION){
            dynamic.setPid(dynamicJSONObject.optInt("pid"));
        }

        dynamic.setAvatar(dynamicJSONObject.optString("avatar"));

        int authorUid = SharedPreferencesUtils.getSessionUid(MyApplication.getContext());
        if (authorUid == dynamicJSONObject.optInt("uid")) {
            dynamic.setAuthorSelf(true);
        }

        if (!dynamicJSONObject.isNull("sex")) {
            dynamic.setSex(dynamicJSONObject.optInt("sex"));
        }

        if (!dynamicJSONObject.isNull("situation")) {
            dynamic.setSituation(dynamicJSONObject.optInt("situation"));
        }
        if (!dynamicJSONObject.isNull("major")) {
            dynamic.setMajor(dynamicJSONObject.optString("major"));
        }
        if (!dynamicJSONObject.isNull("degree")) {
            dynamic.setDegree(dynamicJSONObject.optString("degree"));
        }
        if (!dynamicJSONObject.isNull("university")) {
            dynamic.setUniversity(dynamicJSONObject.optString("university"));
        }

        if (!dynamicJSONObject.isNull("position")) {
            dynamic.setPosition(dynamicJSONObject.optString("position"));
        }

        if (!dynamicJSONObject.isNull("industry")) {
            dynamic.setIndustry(dynamicJSONObject.optString("industry"));
        }

        if (!dynamicJSONObject.isNull("living")) {
            dynamic.setLiving(dynamicJSONObject.optString("living"));
        }

        //dynamics content
        dynamic.setDid(dynamicJSONObject.optLong("did"));
        dynamic.setType(dynamicJSONObject.optInt("type"));
        dynamic.setAction(dynamicJSONObject.optString("action"));
        dynamic.setRelatedId(dynamicJSONObject.optInt("related_id"));
        if (!dynamicJSONObject.isNull("created")) {
            dynamic.setCreatedString(dynamicJSONObject.optLong("created"));
        }

        String content = dynamicJSONObject.optString("content");
        if (content != null && content.length() != 0) {
            dynamic.setContent(content);
        }
        if (!dynamicJSONObject.isNull("dynamic_picture")) {
            String dynamics_pictures = dynamicJSONObject.optString("dynamic_picture");
            if (!"".equals(dynamics_pictures)) {
                dynamic.setDynamicPicture(dynamics_pictures);
            }
        }
        if (!dynamicJSONObject.isNull("cid")) {
            dynamic.setCid(dynamicJSONObject.optInt("cid"));
        }

        return dynamic;
    }

    public void setDynamicsInteract(final Dynamic dynamic, final Handler handler) {
        if (dynamic == null) {
            return;
        }
        RequestBody requestBody = new FormBody.Builder().add("did", String.valueOf(dynamic.getDid())).build();
        HttpUtil.sendOkHttpRequest(getContext(), REQUEST_INTERACT_URL, requestBody, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "-------> setDynamicsInteract response: " + responseText);
                int commentCount = 0;
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        commentResponse = new JSONObject(responseText);
                        commentCount = commentResponse.getInt("comment_count");
                        praiseArray = commentResponse.getJSONArray("praise");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (commentCount > 0) {
                        dynamic.setCommentCount(commentCount);
                    }
                    dynamic.setPraisedDynamics(commentResponse.optInt("praised"));

                    if (null != praiseArray && praiseArray.length() > 0) {

                        dynamic.setPraisedDynamicsCount(praiseArray.length());
                        //Log.d(TAG, "setDynamicsInteract +++++++++++++++ praiseArray.length(): " + praiseArray.length());
                    }
                    handler.sendEmptyMessage(UPDATE_COMMENT);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    public void setDynamicsInteract(final Dynamic dynamic) {
        RequestBody requestBody = new FormBody.Builder().add("did", String.valueOf(dynamic.getDid())).build();
        HttpUtil.sendOkHttpRequest(getContext(), REQUEST_INTERACT_URL, requestBody, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "-------> setDynamicsInteract response: " + responseText);
                int commentCount = 0;

                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        commentResponse = new JSONObject(responseText);
                        commentCount = commentResponse.getInt("comment_count");
                        praiseArray = commentResponse.getJSONArray("praise");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (commentCount > 0) {
                        dynamic.setCommentCount(commentCount);
                    }
                    dynamic.setPraisedDynamics(commentResponse.optInt("praised"));

                    if (null != praiseArray && praiseArray.length() > 0) {
                        dynamic.setPraisedDynamicsCount(praiseArray.length());
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void stopLoadProgress() {
        if (progressImageView.getVisibility() == View.VISIBLE) {
            animationDrawable.stop();
            progressImageView.setVisibility(View.GONE);

        }
    }

    public void handleMessage(Message message) {
        switch (message.what) {
            case NO_MORE_DYNAMICS:
                recyclerView.setNoMore(true);
                recyclerView.loadMoreComplete();
                stopLoadProgress();
                break;
            case LOAD_DYNAMICS_DONE:
                meetDynamicsListAdapter.setData(meetList);
                meetDynamicsListAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();
                if (mTempSize < PAGE_SIZE) {
                    //loading finished
                    recyclerView.loadMoreComplete();
                    recyclerView.setNoMore(true);
                }
                stopLoadProgress();
                break;
            case HAVE_UPDATE:
                //meetDynamicsListAdapter.setScrolling(false);
                meetDynamicsListAdapter.setData(meetList);
                meetDynamicsListAdapter.notifyItemRangeInserted(0, mTempSize);
                meetDynamicsListAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();
                mTempSize = 0;
                break;

            case NO_UPDATE:
                mTempSize = 0;
                recyclerView.refreshComplete();
                break;
            case UPDATE_COMMENT:
                meetDynamicsListAdapter.setData(meetList);
                meetDynamicsListAdapter.notifyDataSetChanged();
                break;
            case COMMENT_COUNT_UPDATE:
                Bundle bundle = message.getData();
                int commentCount = bundle.getInt("commentCount");
                if (isDebug)
                    Slog.d(TAG, "------------------>COMMENT_COUNT_UPDATE: position: " + currentPos + " commentCount: " + commentCount);
                meetList.get(currentPos).setCommentCount(commentCount);
                meetDynamicsListAdapter.setData(meetList);
                meetDynamicsListAdapter.notifyDataSetChanged();
                break;

            case PRAISE_UPDATE:
                meetList.get(currentPos).setPraisedDynamicsCount(meetList.get(currentPos).getPraisedDynamicsCount() + 1);
                meetList.get(currentPos).setPraisedDynamics(1);
                meetDynamicsListAdapter.setData(meetList);
                meetDynamicsListAdapter.notifyDataSetChanged();
                break;
            case GET_MY_NEW_ADD_DONE:
                meetDynamicsListAdapter.setData(meetList);
                meetDynamicsListAdapter.notifyItemRangeInserted(0, 1);
                meetDynamicsListAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();
                break;
            case DYNAMICS_DELETE:
                meetList.remove(currentPos);
                meetDynamicsListAdapter.setData(meetList);
                meetDynamicsListAdapter.notifyItemRemoved(currentPos);
                meetDynamicsListAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    public void createCommentDetails(Dynamic dynamic) {
        Intent intent = new Intent(MyApplication.getContext(), DynamicsInteractDetailsActivity.class);
        intent.putExtra("type", DynamicsInteractDetailsActivity.DYNAMIC_COMMENT);
        intent.putExtra("dynamic", dynamic);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivityForResult(intent, Activity.RESULT_FIRST_USER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Activity.RESULT_FIRST_USER) {
            switch (resultCode) {
                case HomeFragment.COMMENT_UPDATE_RESULT:
                    int commentCount = data.getIntExtra("commentCount", 0);
                    if (isDebug) Slog.d(TAG, "==========commentCount: " + commentCount);
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putInt("commentCount", commentCount);
                    msg.setData(bundle);
                    msg.what = COMMENT_COUNT_UPDATE;
                    handler.sendMessage(msg);
                    break;

                case HomeFragment.PRAISE_UPDATE_RESULT:
                    handler.sendEmptyMessage(PRAISE_UPDATE);
                    break;
                case HomeFragment.DYNAMICS_UPDATE_RESULT:
                    getMyNewActivity();
                    break;
                default:
                    break;
            }
        }
    }

    private void getMyNewActivity() {
        requestBody = new FormBody.Builder()
                .add("type", String.valueOf(ParseUtils.ADD_MEET_DYNAMIC_ACTION))
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(0))
                .build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_MY_NEW_ADD_DYNAMICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    JSONObject dynamicJSONObject = null;
                    Slog.d(TAG, "==========updateData response text : " + responseText);
                    if (responseText != null) {
                        //save last update timemills
                        try {
                            dynamicJSONObject = new JSONObject(responseText).optJSONObject("dynamic");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (dynamicJSONObject != null) {
                            Dynamic dynamic = setMeetDynamics(dynamicJSONObject);
                            if (null != dynamic) {
                                //dynamicList.clear();
                                meetList.add(0, dynamic);
                                handler.sendEmptyMessage(GET_MY_NEW_ADD_DONE);
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });

        recyclerView.scrollToPosition(0);
    }

    //register local broadcast to receive DYNAMICS_ADD_BROADCAST
    private void registerLoginBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AddDynamicsActivity.DYNAMICS_ADD_BROADCAST);
        intentFilter.addAction(DYNAMICS_DELETE_BROADCAST);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, intentFilter);
    }

    //unregister local broadcast
    private void unRegisterLoginBroadcast() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
    }


    static class MyHandler extends Handler {
        WeakReference<MeetDynamicsFragment> meetDynamicsFragmentWeakReference;

        MyHandler(MeetDynamicsFragment meetDynamicsFragment) {
            meetDynamicsFragmentWeakReference = new WeakReference<MeetDynamicsFragment>(meetDynamicsFragment);
        }

        @Override
        public void handleMessage(Message message) {
            MeetDynamicsFragment mMeetDynamicsFragment = meetDynamicsFragmentWeakReference.get();
            if (mMeetDynamicsFragment != null) {
                mMeetDynamicsFragment.handleMessage(message);
            }
        }
    }

    private class DynamicsAddBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case AddDynamicsActivity.DYNAMICS_ADD_BROADCAST:
                    if (isDebug) Slog.d(TAG, "==========DYNAMICS_ADD_BROADCAST");
                    requestData(false);
                    break;
                case DYNAMICS_DELETE_BROADCAST:
                    handler.sendEmptyMessage(DYNAMICS_DELETE);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterLoginBroadcast();

        if (recyclerView != null) {
            recyclerView.destroy();
            recyclerView = null;
        }
    }

    /******* added by chunping.xu for load data opt, 2019/12/15 ***********/
    private void requestData(final boolean isLoadMore) {
        RequestBody requestBody = null;

        int page = 0;
        if (isLoadMore) {
            page = meetList.size() / PAGE_SIZE;
            requestBody = new FormBody.Builder()
                    .add("type", String.valueOf(ParseUtils.ADD_MEET_DYNAMIC_ACTION))
                    .add("step", String.valueOf(PAGE_SIZE))
                    .add("page", String.valueOf(page))
                    .build();
        } else {
            String last = SharedPreferencesUtils.getDynamicsLast(getContext());
            if (isDebug) Slog.d(TAG, "requestData last:" + last);
            requestBody = new FormBody.Builder()
                    .add("last", last)
                    .add("type", String.valueOf(ParseUtils.ADD_MEET_DYNAMIC_ACTION))
                    .add("step", String.valueOf(PAGE_SIZE))
                    .add("page", String.valueOf(0))
                    .build();
        }

        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_DYNAMICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response == null || response.body() == null) {
                    return;
                }
                parseResponse(response.body().string(), isLoadMore);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure e:" + e);
            }
        });
    }

    private void parseResponse(String responseText, final boolean isLoadMore) {
        if (isDebug) Slog.d(TAG, "parseResponse response text : " + responseText);
        if (null == responseText) {
            return;
        }
        List<Dynamic> tempList = getDynamicsResponse(responseText, isLoadMore ? false : true , handler);
        if (null == tempList || tempList.size() == 0) {
            return;
        }
        int loadSize = tempList.size();
        mTempSize = loadSize;
        if (loadSize > 0) {
            if (!isLoadMore) {
                meetList.clear();
            }
            meetList.addAll(tempList);
            handler.sendEmptyMessage(isLoadMore ? LOAD_DYNAMICS_DONE : HAVE_UPDATE);
        } else {
            handler.sendEmptyMessage(isLoadMore ? NO_MORE_DYNAMICS : NO_UPDATE);
        }
    }
}