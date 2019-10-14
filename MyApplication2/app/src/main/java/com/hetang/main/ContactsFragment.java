package com.hetang.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hetang.adapter.ContactsListAdapter;
import com.hetang.contacts.ContactsApplyListActivity;
import com.hetang.util.HttpUtil;
import com.hetang.util.Slog;
import com.hetang.util.UserProfile;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.hetang.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class ContactsFragment extends Fragment {
    private static final boolean isDebug = true;
    private static final String TAG = "ContactsFragment";
    private XRecyclerView recyclerView;
    private Handler myHandler;
    private int newApplyCount = 0;
    private int myApplyCount = 0;
    private List<UserProfile> contactsList = new ArrayList<>();
    private ContactsListAdapter contactsListAdapter;
    private static final int GET_CONTACTS_DONE = 0;
    TextView newApply;
    TextView newApplyCountView;
    TextView myApply;
    TextView myApplyCountView;
    public static final int CONTACTS_DEFAULT = 0;
    public static final int CONTACTS_NEW_APPLY = 1;
    public static final int CONTACTS_MY_APPLY = 2;
    private static final String GET_ALL_CONTACTS_URL = HttpUtil.DOMAIN + "?q=contacts/get_all_contacts";
    public static final String ACCEPT_CONTACTS_APPLY_URL = HttpUtil.DOMAIN + "?q=contacts/accept_apply";
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.contacts, container, false);

        initView(convertView);

        loadData();

        return convertView;
    }
    
    private void initView(View view) {
        myHandler = new MyHandler(this);
        recyclerView = view.findViewById(R.id.contacts_recyclerview);
        newApply = view.findViewById(R.id.new_apply_text);
        newApplyCountView = view.findViewById(R.id.new_apply_count);
        myApply = view.findViewById(R.id.my_apply);
        myApplyCountView = view.findViewById(R.id.my_apply_count);
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

        recyclerView.getDefaultFootView().setLoadingHint(getString(R.string.loading_pull_up_tip));
        recyclerView.getDefaultFootView().setNoMoreHint(getString(R.string.loading_no_more));
        final int itemLimit = 5;
        
        // When the item number of the screen number is list.size-2,we call the onLoadMore
        recyclerView.setLimitNumberToCallLoadMore(2);
        recyclerView.setRefreshProgressStyle(ProgressStyle.BallBeat);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);

        recyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {

            @Override
            public void onRefresh() { }

            @Override
            public void onLoadMore() {
                loadData();
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
    
    private void loadData() {
        RequestBody requestBody = new FormBody.Builder().build();
        HttpUtil.sendOkHttpRequest(getContext(), GET_ALL_CONTACTS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response != null){
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "loadData response : "+responseText);
                    processResponseText(responseText);
                    if (isDebug) Slog.d(TAG, "------------------->contactsList size: "+contactsList.size());
                    if(contactsList.size() > 0){
                        myHandler.sendEmptyMessage(GET_CONTACTS_DONE);
                    }else {
                        if (newApplyCount > 0 || myApplyCount >0){
                            myHandler.sendEmptyMessage(GET_CONTACTS_DONE);
                        }
                   }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure e:" + e);
            }
        });
    }
    
    private void processResponseText(String responseText){
        try {
            JSONObject response = new JSONObject(responseText);
            JSONArray contactsArray = response.optJSONArray("contacts");
            JSONObject contactsObject;
            if (contactsArray != null){
                for (int i=0; i<contactsArray.length(); i++){
                    UserProfile contacts = new UserProfile();
                    contactsObject = contactsArray.getJSONObject(i);
                    contacts.setAvatar(contactsObject.optString("avatar"));
                    contacts.setUid(contactsObject.optInt("uid"));
                    contacts.setName(contactsObject.optString("name"));
                    contacts.setSex(contactsObject.optInt("sex"));
                    contactsList.add(contacts);
                }
            }
            
            newApplyCount = response.optInt("newApplyCount");
            myApplyCount = response.optInt("myApplyCount");
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Slog.d(TAG, "===================onActivityResult requestCode: "+requestCode+" resultCode: "+resultCode);
        if (requestCode == Activity.RESULT_FIRST_USER){
            reInitView();
        }
    }

    private void reInitView(){
        contactsList.clear();
        loadData();
    }
    
    public void handleMessage(Message message) {
        switch (message.what){
            case GET_CONTACTS_DONE:
                contactsListAdapter.setData(contactsList);
                contactsListAdapter.notifyDataSetChanged();
                recyclerView.refreshComplete();

                if (newApplyCount > 0){
                    newApplyCountView.setText("+"+String.valueOf(newApplyCount));
                }else {
                    newApplyCountView.setText("");
                }
                if (myApplyCount > 0){
                    myApplyCountView.setText("+"+String.valueOf(myApplyCount));
                }
                
                break;
            default:
                break;
        }
    }

    static class MyHandler extends Handler {
        WeakReference<ContactsFragment> contactsFragmentWeakReference;

        MyHandler(ContactsFragment contactsFragment) {
            contactsFragmentWeakReference = new WeakReference<ContactsFragment>(contactsFragment);
        }
        
        @Override
        public void handleMessage(Message message) {
            ContactsFragment contactsFragment = contactsFragmentWeakReference.get();
            if (contactsFragment != null) {
                contactsFragment.handleMessage(message);
            }
        }
    }
}
