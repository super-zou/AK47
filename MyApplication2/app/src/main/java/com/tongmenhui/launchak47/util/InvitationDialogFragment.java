package com.tongmenhui.launchak47.util;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SearchView;

import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.meet.MeetMemberInfo;

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
import okhttp3.Response;

public class InvitationDialogFragment extends DialogFragment implements View.OnClickListener {
    private static final String TAG = "InvitationDialogFragment";
    private static final int CLEAR_SEARCH_RESULTS = 0;
    private static final int QUERY_USER_DONE = 1;
    private static final int GET_CONTACTS_DONE = 2;
    private static final int PAGE_SIZE = 20;
    private static final String SEARCH_USER_URL = HttpUtil.DOMAIN + "?q=contacts/search";
    private static final String GET_CONTACTS_URL = HttpUtil.DOMAIN + "?q=contacts/get_all_contacts";
    private Dialog mDialog;
    private int uid = -1;
    private RecyclerView searchResultsView;
    //private ArrayAdapter<String> adapter;
    private SearchUserListAdapter adapter;
    private List<MeetMemberInfo> mMemberInfoList = new ArrayList<>();
    private List<MeetMemberInfo> mContactsList = new ArrayList<>();
    private Context mContext;
    private SearchView mSearchView;
    private Handler handler = new MyHandler(this);

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.invite_reference);
        mDialog.setCanceledOnTouchOutside(true);
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        //layoutParams.alpha = 0.9f;
        layoutParams.gravity = Gravity.TOP;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        //multiAutoCompleteTextView = mDialog.findViewById(R.id.multiAutoCompleteTextView);
        //adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_dropdown_item_1line);
        //adapter = new SearchUserListAdapter(mContext);

        mSearchView = mDialog.findViewById(R.id.searchUserView);
        searchResultsView = mDialog.findViewById(R.id.searchResultsView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        searchResultsView.setLayoutManager(linearLayoutManager);

        //adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_dropdown_item_1line);
        adapter = new SearchUserListAdapter(mContext);
        searchResultsView.setAdapter(adapter);

        Bundle bundle = getArguments();
        if (bundle != null) {
            uid = bundle.getInt("uid");
        }

        getAllContacts();
        searchContactsByName();

        return mDialog;
    }

    public void getAllContacts() {
        int page = mContactsList.size() / PAGE_SIZE;
        FormBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                // .add("init_recommend_page", String.valueOf(PAGE_SIZE))
                // .add("contacts_index", String.valueOf(page))
                .build();
        HttpUtil.sendOkHttpRequest(mContext, GET_CONTACTS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    Slog.d(TAG, "==========getAllContacts  response text : " + responseText);

                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        List<MeetMemberInfo> memberInfos = parseUserInfo(responseText);
                        if (null != memberInfos) {
                            mContactsList.addAll(memberInfos);
                            Slog.d(TAG, "getResponseText list.size:" + memberInfos.size());
                            handler.sendEmptyMessage(GET_CONTACTS_DONE);
                        }

                    }

                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    public void searchContactsByName() {
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.equals("")) {
                    Slog.d(TAG, "===========submit text: " + query);
                    searchUserResults(query, false);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() >= 1) {
                    Slog.d(TAG, "===========search text:" + newText);
                    //mMemberInfoList.clear();
                    clearSearchResults();
                    searchUserResults(newText, true);
                } else {
                    clearSearchResults();
                    getAllContacts();
                    /*
                    if(mMemberInfoList.size() > 0){
                        clearSearchResults();
                    }else{
                        getAllContacts();
                    }*/
                }
                return false;
            }
        });
    }

    public void searchUserResults(String word, boolean autocomplete) {
        int page = mMemberInfoList.size() / PAGE_SIZE;
        FormBody requestBody = new FormBody.Builder()
                .add("name", word)
                .add("step", String.valueOf(PAGE_SIZE))
                .add("page", String.valueOf(page))
                .build();
        HttpUtil.sendOkHttpRequest(mContext, SEARCH_USER_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    Slog.d(TAG, "==========searchUserResults response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        List<MeetMemberInfo> memberInfos = parseUserInfo(responseText);
                        if (null != memberInfos) {
                            mMemberInfoList.clear();
                            mMemberInfoList.addAll(memberInfos);
                            Slog.d(TAG, "getResponseText list.size:" + memberInfos.size());
                            handler.sendEmptyMessage(QUERY_USER_DONE);
                        }

                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private List<MeetMemberInfo> parseUserInfo(String response) {
        List<MeetMemberInfo> memberInfoList = new ArrayList<MeetMemberInfo>();
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONObject memberInfosResponse = new JSONObject(response);
                JSONArray memberInfoArray = memberInfosResponse.optJSONArray("contacts");
                if (memberInfoArray != null && memberInfoArray.length() > 0) {
                    for (int i = 0; i < memberInfoArray.length(); i++) {
                        MeetMemberInfo memberInfo = new MeetMemberInfo();
                        JSONObject member = memberInfoArray.getJSONObject(i);
                        memberInfo.setUid(member.getInt("uid"));
                        memberInfo.setRealname(member.getString("realname"));
                        memberInfo.setSituation(member.getInt("situation"));
                        if (member.getInt("situation") == 0) {
                            memberInfo.setUniversity(member.getString("university"));
                            memberInfo.setDegree(member.getString("degree"));
                            memberInfo.setMajor(member.getString("major"));
                        } else {
                            memberInfo.setCompany(member.getString("company"));
                            memberInfo.setJobTitle(member.getString("job_title"));
                        }
                        //memberInfo.setProfile(profile);
                        memberInfo.setPictureUri(member.getString("picture_uri"));

                        memberInfoList.add(memberInfo);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return memberInfoList;
    }

    private void clearSearchResults() {
        mMemberInfoList.clear();
        handler.sendEmptyMessage(CLEAR_SEARCH_RESULTS);
    }

    public void handleMessage(Message message) {
        switch (message.what) {
            case QUERY_USER_DONE:
                Slog.d(TAG, "=======handle message: " + QUERY_USER_DONE);
                adapter.setData(mMemberInfoList);
                adapter.notifyDataSetChanged();
                break;
            case CLEAR_SEARCH_RESULTS:
                mMemberInfoList.clear();
                mContactsList.clear();
                searchResultsView.removeAllViews();
                adapter.setData(mMemberInfoList);
                adapter.notifyDataSetChanged();
                break;
            case GET_CONTACTS_DONE:
                adapter.setData(mContactsList);
                adapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
    }

    static class MyHandler extends Handler {
        WeakReference<InvitationDialogFragment> invitationDialogFragmentWeakReference;

        MyHandler(InvitationDialogFragment invitationDialogFragment) {
            invitationDialogFragmentWeakReference = new WeakReference<InvitationDialogFragment>(invitationDialogFragment);
        }

        @Override
        public void handleMessage(Message message) {
            InvitationDialogFragment invitationDialogFragment = invitationDialogFragmentWeakReference.get();
            if (invitationDialogFragment != null) {
                invitationDialogFragment.handleMessage(message);
            }
        }
    }
}
