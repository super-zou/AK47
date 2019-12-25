package com.hetang.contacts;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.hetang.common.BaseAppCompatActivity;
import com.hetang.main.MeetArchiveFragment;
import com.hetang.util.HttpUtil;
import com.hetang.util.UserProfile;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.hetang.R;
import com.hetang.adapter.ContactsListAdapter;
import com.hetang.util.FontManager;
import com.hetang.util.Slog;

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

import static com.hetang.main.ContactsFragment.CONTACTS_DEFAULT;
import static com.hetang.main.ContactsFragment.CONTACTS_NEW_APPLY;

public class ContactsApplyListActivity extends BaseAppCompatActivity {

    private static final String TAG = "ContactsApplyListActivity";
    MeetArchiveFragment.ImpressionStatistics impressionStatistics = null;
    private Context mContext;
    private MyHandler myHandler;
    public static final int GET_CONTACTS_DONE = 0;
    private XRecyclerView mContactsApplyList;
    private ContactsListAdapter contactsListAdapter;
    private List<Contacts> contactsList = new ArrayList<>();

    private static final String GET_CONTACTS_NEW_APPLY_URL = HttpUtil.DOMAIN + "?q=contacts/get_new_apply";
    private static final String GET_CONTACTS_MY_APPLY_URL = HttpUtil.DOMAIN + "?q=contacts/get_my_apply";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_apply_list);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        myHandler = new MyHandler(this);

        int type = getIntent().getIntExtra("type", CONTACTS_DEFAULT);
        Slog.d(TAG, "---------->type: "+type);
        
        mContactsApplyList = findViewById(R.id.contacts_apply_list);
        
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mContactsApplyList.setLayoutManager(linearLayoutManager);

        contactsListAdapter = new ContactsListAdapter(mContext, type);
        mContactsApplyList.setAdapter(contactsListAdapter);



        TextView back = findViewById(R.id.left_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        
        TextView title = findViewById(R.id.title);
        if (type == CONTACTS_NEW_APPLY){
            title.setText(getResources().getString(R.string.contacts_apply));
        }else {
            title.setText(getResources().getString(R.string.waiting_accept));
        }

        loadData(type);

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.custom_actionbar), font);
    }
    
    private void loadData(int type){
        RequestBody requestBody = new FormBody.Builder().build();
        String address = "";

        if (type == CONTACTS_NEW_APPLY){
            address = GET_CONTACTS_NEW_APPLY_URL;
        } else {
            address = GET_CONTACTS_MY_APPLY_URL;
        }
HttpUtil.sendOkHttpRequest(ContactsApplyListActivity.this, address, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if(response.body() != null){
                    String responseText = response.body().string();
                    Slog.d(TAG, "==========loadData response text : "+responseText);
                    if(responseText != null){
                        if (processResponse(responseText) > 0){
                            myHandler.sendEmptyMessage(GET_CONTACTS_DONE);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) { }
        });
    }
    
    public static class Contacts extends UserProfile{
        private String content;
        public String getContent(){ return content; }
        public void setContent(String content) { this.content = content; }
    }

    private int processResponse(String responseText){
        try {
            JSONObject response = new JSONObject(responseText);
            JSONArray contactsArray = response.optJSONArray("contacts");
            
            if (contactsArray != null && contactsArray.length() > 0){
                for (int i=0; i<contactsArray.length(); i++){
                    Contacts contacts = new Contacts();
                    JSONObject contactsObject = contactsArray.optJSONObject(i);
                    contacts.setAvatar(contactsObject.optString("avatar"));
                    contacts.setUid(contactsObject.optInt("uid"));
                    contacts.setName(contactsObject.optString("name"));
                    contacts.setSex(contactsObject.optInt("sex"));
                    contacts.setContent(contactsObject.optString("apply_content"));
                    contactsList.add(contacts);
                }

                return contactsList.size();
            }
        }catch (JSONException e){
            e.printStackTrace();
        }

        return 0;
    }
    
    public void handleMessage(Message message) {
        switch (message.what){
            case GET_CONTACTS_DONE:
                Slog.d(TAG, "--------------------->contactsList size: "+contactsList.size());
                contactsListAdapter.setData(contactsList);
                contactsListAdapter.notifyDataSetChanged();
                //mContactsApplyList.refreshComplete();
                break;
            default:
                break;
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
    }

    static class MyHandler extends Handler {
        WeakReference<ContactsApplyListActivity> contactsApplyListActivityWeakReference;

        MyHandler(ContactsApplyListActivity contactsApplyListActivity) {
            contactsApplyListActivityWeakReference = new WeakReference<>(contactsApplyListActivity);
        }
        
        @Override
        public void handleMessage(Message message) {
            ContactsApplyListActivity contactsApplyListActivity = contactsApplyListActivityWeakReference.get();
            if (contactsApplyListActivity != null) {
                contactsApplyListActivity.handleMessage(message);
            }
        }
    }
}
