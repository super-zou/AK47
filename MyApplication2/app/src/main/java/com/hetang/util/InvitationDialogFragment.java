package com.hetang.util;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
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
import android.widget.TextView;

import com.hetang.R;
//import com.hetang.meet.MeetMemberInfo;

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

import static com.hetang.archive.ArchiveFragment.REQUESTCODE;
import static com.hetang.main.MeetArchiveFragment.RESULT_OK;

public class InvitationDialogFragment extends DialogFragment implements View.OnClickListener {
    private static final String TAG = "InvitationDialogFragment";
    private static final int GET_UIDS_DONE = 0;
    private static final int CLEAR_SEARCH_RESULTS = 1;
    private static final int QUERY_USER_DONE = 2;
    private static final int GET_CONTACTS_DONE = 3;
    private static final int PAGE_SIZE = 20;
    private static final String SEARCH_USER_URL = HttpUtil.DOMAIN + "?q=contacts/search";
    private static final String GET_CONTACTS_URL = HttpUtil.DOMAIN + "?q=contacts/get_all_contacts";
    private static final String GET_REFERENCE_UIDS_URL = HttpUtil.DOMAIN + "?q=meet/reference/get_uids";
    private static final String GET_CHEERING_GROUP_UIDS_URL = HttpUtil.DOMAIN + "?q=meet/cheering_group/get_uids";
    private static final String GET_SINGLE_GROUP_UIDS_URL = HttpUtil.DOMAIN + "?q=single_group/get_uids";
    private Dialog mDialog;
    private int uid = -1;
    private int type = -1;
    private int[] uidArray;
    private int gid;
    private RecyclerView searchResultsView;
    //private ArrayAdapter<String> adapter;
    private SearchUserListAdapter adapter;
    private List<UserProfile> mMemberInfoList = new ArrayList<>();
    private List<UserProfile> mContactsList = new ArrayList<>();
    private Context mContext;
    private SearchView mSearchView;
    private Handler handler = new MyHandler(this);
    private UserProfile userProfile;
    private CommonDialogFragmentInterface commonDialogFragmentInterface;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        try {
            //evaluateDialogFragmentListener = (EvaluateDialogFragmentListener) context;
            commonDialogFragmentInterface = (CommonDialogFragmentInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement commonDialogFragmentInterface");
        }
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
            uid = bundle.getInt("uid", 0);
            type = bundle.getInt("type", 0);
            if(type == ParseUtils.TYPE_SINGLE_GROUP){
                gid = bundle.getInt("gid", -1);
            }
        }
        
        switch (type){
            case ParseUtils.TYPE_REFERENCE:
                getRefereeUids();
                break;
            case ParseUtils.TYPE_CHEERING_GROUP:
                getCheeringGroupUids();
                break;
            case ParseUtils.TYPE_SINGLE_GROUP:
                getSingleGroupUids();
                break;
            case ParseUtils.TYPE_COMMON_SEARCH:
                getAllContacts();
                searchContactsByName();
                break;
                default:
                    break;
        }
        
        Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.invite_reference), font);

        TextView cancel = mDialog.findViewById(R.id.cancel);
        
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        TextView shareIcon = mDialog.findViewById(R.id.share_icon);
        
        shareIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareDialogFragment shareDialogFragment = new ShareDialogFragment();
                shareDialogFragment.show(getFragmentManager(), "shareDialogFragment");
            }
        });

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
                        try {
                            JSONArray memberInfoArray = new JSONObject(responseText).optJSONArray("contacts");
                            if (memberInfoArray != null && memberInfoArray.length() > 0){
                                //mMemberInfoList.clear();
                                for (int i=0; i<memberInfoArray.length(); i++){
                                    JSONObject jsonObject = memberInfoArray.getJSONObject(i);
                                    Slog.d(TAG, "==============user jsonObject: "+jsonObject);
                                    userProfile = ParseUtils.getUserProfileFromJSONObject(jsonObject);
                                    if(userProfile != null){
                                        mContactsList.add(userProfile);
                                        Slog.d(TAG, "getResponseText mContactsList.size:" + mContactsList.size());
                                        handler.sendEmptyMessage(GET_CONTACTS_DONE);
                                    }
                                }
                            }

                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                        }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
    private void getSingleGroupUids(){
        FormBody requestBody = new FormBody.Builder().add("gid", String.valueOf(gid)).build();
        HttpUtil.sendOkHttpRequest(mContext, GET_SINGLE_GROUP_UIDS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    Slog.d(TAG, "==========getSingleGroupUids  response text : " + responseText);

                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        try {
                            JSONArray uidJsonArray = new JSONObject(responseText).optJSONArray("response");
                            Slog.d(TAG, "==========uidJsonArray : " + uidJsonArray);
                            if (uidJsonArray != null && uidJsonArray.length() > 0){
                                uidArray = new int[uidJsonArray.length()];
                                for (int i=0; i<uidJsonArray.length(); i++){
                                    uidArray[i] = uidJsonArray.getInt(i);
                                }
                            }

                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                    
                    handler.sendEmptyMessage(GET_UIDS_DONE);

                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    private void getRefereeUids(){
        FormBody requestBody = new FormBody.Builder().build();
        HttpUtil.sendOkHttpRequest(mContext, GET_REFERENCE_UIDS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    Slog.d(TAG, "==========getRefereeUids  response text : " + responseText);

                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        try {
                            JSONArray uidJsonArray = new JSONObject(responseText).optJSONArray("response");
                            Slog.d(TAG, "==========uidJsonArray : " + uidJsonArray);
                            if (uidJsonArray != null && uidJsonArray.length() > 0){
                                uidArray = new int[uidJsonArray.length()];
                                for (int i=0; i<uidJsonArray.length(); i++){
                                    uidArray[i] = uidJsonArray.getInt(i);
                                }
                            }

                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                    
                    handler.sendEmptyMessage(GET_UIDS_DONE);

                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    
    private void getCheeringGroupUids(){
        FormBody requestBody = new FormBody.Builder().build();
        HttpUtil.sendOkHttpRequest(mContext, GET_CHEERING_GROUP_UIDS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    Slog.d(TAG, "==========getCheeringGroupUids  response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        try {
                            JSONArray uidJsonArray = new JSONObject(responseText).optJSONArray("response");
                            Slog.d(TAG, "==========uidJsonArray : " + uidJsonArray);
                            if (uidJsonArray != null && uidJsonArray.length() > 0){
                                uidArray = new int[uidJsonArray.length()];
                                for (int i=0; i<uidJsonArray.length(); i++){
                                    uidArray[i] = uidJsonArray.getInt(i);
                                }
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                    
                    handler.sendEmptyMessage(GET_UIDS_DONE);
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
                    clearSearchResults();
                    searchUserResults(newText, true);
                } else {
                    clearSearchResults();
                    getAllContacts();
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
                        try {
                            //JSONArray memberInfoArray = new JSONObject(responseText).optJSONArray("contacts");
                            JSONArray memberInfoArray = new JSONObject(responseText).optJSONArray("users");
                            if (memberInfoArray != null && memberInfoArray.length() > 0){
                                mMemberInfoList.clear();
                                for (int i=0; i<memberInfoArray.length(); i++){
                                    JSONObject jsonObject = memberInfoArray.getJSONObject(i);
                                    Slog.d(TAG, "==============user jsonObject: "+jsonObject);
                                    userProfile = ParseUtils.getUserProfileFromJSONObject(jsonObject);
                                    if(userProfile != null){
                                        mMemberInfoList.add(userProfile);
                                        Slog.d(TAG, "getResponseText list.size:" + mMemberInfoList.size());
                                        handler.sendEmptyMessage(QUERY_USER_DONE);
                                    }
                                }
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }

                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }


    private void clearSearchResults() {
        mMemberInfoList.clear();
        handler.sendEmptyMessage(CLEAR_SEARCH_RESULTS);
    }

    public void handleMessage(Message message) {
        switch (message.what) {
            case GET_UIDS_DONE:
                getAllContacts();
                searchContactsByName();
                break;
            case QUERY_USER_DONE:
                if(type == ParseUtils.TYPE_SINGLE_GROUP){
                    adapter.setData(mMemberInfoList, type, uidArray, gid);
                }else {
                    adapter.setData(mMemberInfoList, type, uidArray);
                }
                adapter.notifyDataSetChanged();
                break;
            case CLEAR_SEARCH_RESULTS:
                mMemberInfoList.clear();
                mContactsList.clear();
                searchResultsView.removeAllViews();
                adapter.setData(mMemberInfoList, type, uidArray);
                adapter.notifyDataSetChanged();
                break;
            case GET_CONTACTS_DONE:
               if (type == ParseUtils.TYPE_SINGLE_GROUP){
                    adapter.setData(mMemberInfoList, type, uidArray, gid);
                }else {
                    adapter.setData(mContactsList, type, uidArray);
                }
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
    public void onDestroy() {
        super.onDestroy();

        if (getTargetFragment() != null){
            if (type == ParseUtils.TYPE_CHEERING_GROUP){
                Intent intent = new Intent();
                intent.putExtra("type", ParseUtils.TYPE_CHEERING_GROUP);
                intent.putExtra("status", true);
                getTargetFragment().onActivityResult(REQUESTCODE, RESULT_OK, intent);
            }
        }
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
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
