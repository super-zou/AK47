package com.hetang.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hetang.adapter.ContactsListAdapter;
import com.hetang.common.MyApplication;
import com.hetang.contacts.ContactsApplyListActivity;
import com.hetang.util.BaseFragment;
import com.hetang.util.HttpUtil;
import com.hetang.util.ParseUtils;
import com.hetang.util.RoundImageView;
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

public class ContactsFragment extends BaseFragment {
    private static final boolean isDebug = true;
    private static final String TAG = "ContactsFragment";
    private XRecyclerView recyclerView;
    private Handler myHandler;
    private int newApplyCount = 0;
    private int myApplyCount = 0;
    private List<UserProfile> contactsList = new ArrayList<>();
    private List<ContactsApplyListActivity.Contacts> requestList = new ArrayList<>();
    private ContactsListAdapter contactsListAdapter;
    private static final int GET_CONTACTS_DONE = 0;
    TextView newApply;
    TextView newApplyCountView;
    TextView myApply;
    TextView myApplyCountView;
    View mView;
    View mRequestHeaderView;
    public static final int CONTACTS_DEFAULT = 0;
    public static final int CONTACTS_NEW_APPLY = 1;
    public static final int CONTACTS_MY_APPLY = 2;
    private static final String GET_ALL_CONTACTS_URL = HttpUtil.DOMAIN + "?q=contacts/get_all_contacts";
    public static final String ACCEPT_CONTACTS_APPLY_URL = HttpUtil.DOMAIN + "?q=contacts/accept_apply";
    public static final String CONTACTS_DISMISS_URL = HttpUtil.DOMAIN + "?q=contacts/dismiss";
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.contacts, container, false);

        mView = convertView;

        initContentView(convertView);

        requestData();

        return convertView;
    }
    
    private void initContentView(View view) {
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
    
    private void requestData() {
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
            
                        JSONArray requestArray = response.optJSONArray("request");
            JSONObject requestObject;
            if (requestArray != null && requestArray.length() > 0){
                for (int i=0; i<requestArray.length(); i++){
                    ContactsApplyListActivity.Contacts contacts = new ContactsApplyListActivity.Contacts();
                    requestObject = requestArray.getJSONObject(i);
                    contacts.setAvatar(requestObject.optString("avatar"));
                    contacts.setUid(requestObject.optInt("uid"));
                    contacts.setName(requestObject.optString("name"));
                    contacts.setSex(requestObject.optInt("sex"));
                    contacts.setContent(requestObject.optString("apply_content"));
                    requestList.add(contacts);
                }
            }
            
            newApplyCount = response.optInt("newApplyCount");
            myApplyCount = response.optInt("myApplyCount");
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
    
    private void setRequestPeopleView(){
        mRequestHeaderView = LayoutInflater.from(getContext()).inflate(R.layout.contacts_requesting, (ViewGroup) mView.findViewById(android.R.id.content), false);
        recyclerView.addHeaderView(mRequestHeaderView);
        final LinearLayout requestWrapper = mRequestHeaderView.findViewById(R.id.contacts_requesting);
        TextView more = mRequestHeaderView.findViewById(R.id.more);
        for (int i=0; i<requestList.size(); i++){
            View view = LayoutInflater.from(getContext()).inflate(R.layout.contacts_item, null);
            requestWrapper.addView(view);
            RoundImageView avatar = view.findViewById(R.id.avatar);
            TextView name = view.findViewById(R.id.name);
            TextView content = view.findViewById(R.id.apply_content);
            final Button dismissBtn = view.findViewById(R.id.dismiss);
            final Button acceptBtn = view.findViewById(R.id.accept);
            final ContactsApplyListActivity.Contacts request = requestList.get(i);
            String avatarUrl = request.getAvatar();
            if (avatarUrl != null && !"".equals(avatarUrl)) {
                Glide.with(getContext()).load(HttpUtil.DOMAIN + avatarUrl).into(avatar);
            } else {
                if(request.getSex() == 0){
                    avatar.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.male_default_avator));
                }else {
                    avatar.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.female_default_avator));
                }
            }
            
            name.setText(request.getName());

            if (!TextUtils.isEmpty(request.getContent())){
                content.setText(request.getContent());
            }

            name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ParseUtils.startMeetArchiveActivity(getContext(), request.getUid());
                }
            });
            avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ParseUtils.startMeetArchiveActivity(getContext(), request.getUid());
                }
            });

            dismissBtn.setTag(i);
            dismissBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showProgressDialog("ÕýÔÚºöÂÔ");
                    dismiss(request.getUid());
                    requestWrapper.removeViewAt((int)dismissBtn.getTag());
                }
            });
            
             acceptBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    accept(acceptBtn, request.getUid());
                }
            });

        }
        
        if (newApplyCount > requestList.size()){
            more.setVisibility(View.VISIBLE);
            more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), ContactsApplyListActivity.class);
                    intent.putExtra("type", CONTACTS_NEW_APPLY);
                    startActivityForResult(intent, Activity.RESULT_FIRST_USER);
                }
            });
        }
    }
    
     public static void accept(Button acceptBtn, int uid){
        acceptBtn.setText(MyApplication.getContext().getResources().getString(R.string.acceptted));
        acceptBtn.setClickable(false);
        acceptBtn.setBackground(MyApplication.getContext().getDrawable(R.drawable.btn_disable));
        acceptContactsApply(uid);
    }
    
    public static void acceptContactsApply(int uid){
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), ACCEPT_CONTACTS_APPLY_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response != null){
                    String responseText = response.body().string();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure e:" + e);
            }
        });
    }
    
    private void dismiss(int uid){
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), CONTACTS_DISMISS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response != null){
                    String responseText = response.body().string();
                    dismissProgressDialog();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure e:" + e);
            }
        });
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
                    setRequestPeopleView();
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
    
        @Override
    protected int getLayoutId(){ return  0; }

    @Override
    protected void initView(View view){
        return;
    }

    @Override
    protected void loadData(){
        return;
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
