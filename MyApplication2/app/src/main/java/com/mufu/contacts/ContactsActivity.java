package com.mufu.contacts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.mufu.R;
import com.mufu.adapter.ContactsListAdapter;
import com.mufu.common.BaseAppCompatActivity;
import com.mufu.common.ReminderManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.Slog;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.mufu.common.MyApplication.getContext;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class ContactsActivity extends BaseAppCompatActivity {
    public static final int CONTACTS_DEFAULT = 0;
    public static final int CONTACTS_NEW_APPLY = 1;
    public static final int CONTACTS_MY_APPLY = 2;
    public static final int PAGE_SIZE = 8;
    public static final String ACCEPT_CONTACTS_APPLY_URL = HttpUtil.DOMAIN + "?q=contacts/accept_apply";
    public static final String CONTACTS_DISMISS_URL = HttpUtil.DOMAIN + "?q=contacts/dismiss";
    public static final String GET_APPLY_AND_REQUEST_COUNT = HttpUtil.DOMAIN + "?q=contacts/get_apply_add_request_count";
    private static final boolean isDebug = true;
    private static final String TAG = "ContactsActivity";
    private static final int GET_CONTACTS_DONE = 0;
    private static final int GET_CONTACTS_END = 1;
    private static final int NO_CONTACTS = 2;
    public static final int HAS_REQUEST_OR_APPLY = 3;
    private static final String GET_ALL_CONTACTS_URL = HttpUtil.DOMAIN + "?q=contacts/get_all_contacts";
    TextView newApply;
    TextView newApplyCountView;
    TextView myApply;
    TextView myApplyCountView;
    View mView;
    View mRequestHeaderView;
    private XRecyclerView recyclerView;
    private Handler myHandler;
    private int newApplyCount = 0;
    private int myApplyCount = 0;
    private List<ContactsApplyListActivity.Contacts> contactsList = new ArrayList<>();
    private List<ContactsApplyListActivity.Contacts> requestList = new ArrayList<>();
    private ContactsListAdapter contactsListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts);
        initContentView();

        getApplyAndRequestCount();

        requestData();
    }
    
    private void initContentView() {
        myHandler = new MyHandler(this);
        recyclerView = findViewById(R.id.contacts_recyclerview);
        newApply = findViewById(R.id.new_apply_text);
        newApplyCountView = findViewById(R.id.new_apply_count);
        myApply = findViewById(R.id.my_apply);
        myApplyCountView = findViewById(R.id.my_apply_count);
        contactsListAdapter = new ContactsListAdapter(getContext(), CONTACTS_DEFAULT);
        
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    contactsListAdapter.setScrolling(false);
                    contactsListAdapter.notifyDataSetChanged();
                } else {
                    contactsListAdapter.setScrolling(true);
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

        recyclerView.getDefaultRefreshHeaderView().setRefreshTimeVisible(false);
        recyclerView.setPullRefreshEnabled(false);

        //recyclerView.getDefaultFootView().setLoadingHint(getString(R.string.loading_pull_up_tip));
        //recyclerView.getDefaultFootView().setNoMoreHint(getString(R.string.loading_no_more));
        final int itemLimit = 5;
        
        // When the item number of the screen number is list.size-2,we call the onLoadMore
        recyclerView.setLimitNumberToCallLoadMore(2);
        recyclerView.setRefreshProgressStyle(ProgressStyle.BallBeat);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);

        recyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {

            @Override
            public void onRefresh() {
            }

            @Override
            public void onLoadMore() {
                requestData();
            }
        });
        
        recyclerView.setAdapter(contactsListAdapter);

        newApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ContactsApplyListActivity.class);
                intent.putExtra("type", CONTACTS_NEW_APPLY);
                startActivityForResult(intent, Activity.RESULT_FIRST_USER);
            }
        });

        myApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ContactsApplyListActivity.class);
                intent.putExtra("type", CONTACTS_MY_APPLY);
                startActivity(intent);
            }
        });
    }
    
    private void getApplyAndRequestCount(){
        RequestBody requestBody = new FormBody.Builder().build();

        HttpUtil.sendOkHttpRequest(getContext(), GET_APPLY_AND_REQUEST_COUNT, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null) {
                    String responseText = response.body().string();
                    try {
                        JSONObject responseObject = new JSONObject(responseText);
                        newApplyCount = responseObject.optInt("newApplyCount");
                        myApplyCount = responseObject.optInt("myApplyCount");

                        if (newApplyCount > 0 || myApplyCount > 0){
                            myHandler.sendEmptyMessage(HAS_REQUEST_OR_APPLY);
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }
    
    private void requestData() {
        int page = contactsList.size() / PAGE_SIZE;
        RequestBody requestBody = new FormBody.Builder()
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();
        HttpUtil.sendOkHttpRequest(getContext(), GET_ALL_CONTACTS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null) {
                    String responseText = response.body().string();
                    int size = processResponseText(responseText);
                    if (isDebug)
                        Slog.d(TAG, "------------------->contactsList size: " + contactsList.size());
                    if (size > 0) {
                        if (size == PAGE_SIZE){
                            myHandler.sendEmptyMessage(GET_CONTACTS_DONE);
                        }else {
                            myHandler.sendEmptyMessage(GET_CONTACTS_END);
                        }
                    } else {
                        myHandler.sendEmptyMessage(NO_CONTACTS);
                    }
                }else {
                    myHandler.sendEmptyMessage(NO_CONTACTS);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure e:" + e);
            }
        });
    }
    
    private int processResponseText(String responseText) {
        int size = 0;
        try {
            JSONObject response = new JSONObject(responseText);
            JSONArray contactsArray = response.optJSONArray("contacts");
            JSONObject contactsObject;
            if (contactsArray != null) {
                size = contactsArray.length();
                for (int i = 0; i < contactsArray.length(); i++) {
                    ContactsApplyListActivity.Contacts contacts = new ContactsApplyListActivity.Contacts();
                    contactsObject = contactsArray.getJSONObject(i);
                    contacts.setAvatar(contactsObject.optString("avatar"));
                    contacts.setUid(contactsObject.optInt("uid"));
                    contacts.setNickName(contactsObject.optString("nickname"));
                    contacts.setSex(contactsObject.optInt("sex"));
                    contactsList.add(contacts);
                }
            }
            newApplyCount = response.optInt("newApplyCount");
            myApplyCount = response.optInt("myApplyCount");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return size;

    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Activity.RESULT_FIRST_USER) {
            reInitView();
        }
    }

    private void reInitView() {
        Slog.d(TAG, "----------------------------->reInitView");
        contactsList.clear();
        requestData();
    }
    
    public void handleMessage(Message message) {
        switch (message.what) {
            case GET_CONTACTS_DONE:
                contactsListAdapter.setData(contactsList);
                contactsListAdapter.notifyDataSetChanged();
                recyclerView.loadMoreComplete();
                break;
            case NO_CONTACTS:
                ReminderManager.getInstance().updateNewContactsApplied(newApplyCount);
                recyclerView.loadMoreComplete();
                recyclerView.setNoMore(true);
                break;
           case HAS_REQUEST_OR_APPLY:
                if (newApplyCount > 0) {
                    newApplyCountView.setText("+" + String.valueOf(newApplyCount));
                } else {
                    newApplyCountView.setText("");
                }
                if (myApplyCount > 0) {
                    myApplyCountView.setText("+" + String.valueOf(myApplyCount));
                }
                ReminderManager.getInstance().updateNewContactsApplied(newApplyCount);
                break;
            default:
                break;
        }
    }
    
    static class MyHandler extends Handler {
        WeakReference<ContactsActivity> contactsActivityWeakReference;

        MyHandler(ContactsActivity contactsActivity) {
            contactsActivityWeakReference = new WeakReference<>(contactsActivity);
        }

        @Override
        public void handleMessage(Message message) {
            ContactsActivity contactsActivity = contactsActivityWeakReference.get();
            if (contactsActivity != null) {
                contactsActivity.handleMessage(message);
            }
        }
    }
}