package com.tongmenhui.launchak47.meet;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.adapter.MeetRecommendListAdapter;
import com.tongmenhui.launchak47.util.HttpUtil;
import com.tongmenhui.launchak47.util.Slog;

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

/**
 * Created by haichao.zou on 2017/11/23.
 */

public class MeetDiscoveryFragment extends Fragment {

    private static final String TAG = "MeetDynamicsFragment";
    private View viewContent;
    private int mType = 0;
    private String mTitle;
    private List<MeetMemberInfo> meetMemberList = new ArrayList<>();
    private MeetMemberInfo meetMemberInfo;
    private RecyclerView recyclerView;
    private MeetRecommendListAdapter meetListAdapter;
    // private String realname;
    private int uid;
    private static String responseText;
    JSONObject discovery_response;
    JSONArray discovery;
    private Boolean loaded = false;

    private static final String  domain = "http://www.tongmenhui.com";
    private static final String get_discovery_url = domain + "?q=meet/recommend";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Slog.d(TAG, "=================onCreateView===================");
        initConentView();
        meetListAdapter = new MeetRecommendListAdapter(getContext());
        viewContent = inflater.inflate(R.layout.meet_discovery, container, false);
        recyclerView = (RecyclerView) viewContent.findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState){
                if(newState == SCROLL_STATE_IDLE){
                    meetListAdapter.setScrolling(false);
                    meetListAdapter.notifyDataSetChanged();
                }else{
                    meetListAdapter.setScrolling(true);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        recyclerView.setAdapter(meetListAdapter);
        return viewContent;

    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Slog.d(TAG, "=================onViewCreated===================");
        // initConentView();
    }

    public void initConentView(){
        Slog.d(TAG, "===============initConentView==============");

        RequestBody requestBody = new FormBody.Builder().build();
        // SharedPreferences preferences =  getActivity().getSharedPreferences("session", MODE_PRIVATE);
        // String session = preferences.getString("session_name", "");

        //Slog.d(TAG, "=====in MeetRecommendFragment====session: "+session);

        HttpUtil.sendOkHttpRequest(null, get_discovery_url, requestBody, new Callback(){
            int check_login_user = 0;
            String user_name;

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                //Slog.d(TAG, "response : "+responseText);
                getResponseText(responseText);
                MeetDiscoveryFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        meetListAdapter.setData(meetMemberList);
                        meetListAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e){

            }
        });

        // getResponseText(responseText);

    }

    public void getResponseText(String responseText){

        Slog.d(TAG, "====================getResponseText====================");

        if(!TextUtils.isEmpty(responseText)){
            try {
                discovery_response= new JSONObject(responseText);
                discovery = discovery_response.getJSONArray("recommendation");
                set_meet_member_info(discovery);
                loaded = true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    public void set_meet_member_info(JSONArray discovery){
        int length = discovery.length();
        Slog.d(TAG, "==========set_meet_member_info==========recommendation length: "+length);
        try{
            for (int i=0; i< length; i++){
                JSONObject recommender = discovery.getJSONObject(i);
                meetMemberInfo = new MeetMemberInfo();

                meetMemberInfo.setRealname(recommender.getString("realname"));
                meetMemberInfo.setUid(recommender.getInt("uid"));
                meetMemberInfo.setPictureUri(recommender.getString("picture_uri"));
                meetMemberInfo.setBirthYear(recommender.getInt("birth_year"));
                meetMemberInfo.setHeight(recommender.getInt("height"));
                meetMemberInfo.setUniversity(recommender.getString("university"));
                meetMemberInfo.setDegree(recommender.getString("degree"));
                meetMemberInfo.setJobTitle(recommender.getString("job_title"));
                meetMemberInfo.setLives(recommender.getString("lives"));
                meetMemberInfo.setSituation(recommender.getInt("situation"));

                //requirement
                meetMemberInfo.setAgeLower(recommender.getInt("age_lower"));
                meetMemberInfo.setAgeUpper(recommender.getInt("age_upper"));
                meetMemberInfo.setRequirementHeight(recommender.getInt("requirement_height"));
                meetMemberInfo.setRequirementDegree(recommender.getString("requirement_degree"));
                meetMemberInfo.setRequirementLives(recommender.getString("requirement_lives"));
                meetMemberInfo.setRequirementSex(recommender.getInt("requirement_sex"));
                meetMemberInfo.setIllustration(recommender.getString("illustration"));


                // meetMemberInfo.setSelf(recommender.getInt("self"));
                meetMemberInfo.setBrowseCount(recommender.getInt("browse_count"));
                meetMemberInfo.setLovedCount(recommender.getInt("loved_count"));
                // meetMemberInfo.setLoved(recommender.getInt("loved"));
                // meetMemberInfo.setPraised(recommender.getInt("praised"));
                meetMemberInfo.setPraisedCount(recommender.getInt("praised_count"));
                //  meetMemberInfo.setPictureChain(recommender.getString("pictureChain"));
                // meetMemberInfo.setRequirementSet(recommender.getInt("requirementSet"));


                meetMemberList.add(meetMemberInfo);
            }
        }catch (JSONException e){

        }

    }

}
