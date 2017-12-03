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

/**
 * Created by haichao.zou on 2017/11/20.
 */

public class MeetDynamicsFragment extends Fragment {
    private static final String TAG = "MeetDynamicsFragment";
    private View viewContent;
    private List<MeetDynamics> meetList = new ArrayList<>();
    private MeetDynamics meetDynamics;
    private RecyclerView recyclerView;
    private MeetDynamicsListAdapter meetDynamicsListAdapter;
    JSONObject recommend_response;
    JSONArray recommendation;

    private static final String  domain = "http://www.tongmenhui.com";
    private static final String get_recommend_url = domain + "?q=meet/activity/get";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Slog.d(TAG, "=================onCreateView===================");
        initConentView();
        meetDynamicsListAdapter = new MeetDynamicsListAdapter(getContext());
        viewContent = inflater.inflate(R.layout.meet_dynamics, container, false);
        recyclerView = (RecyclerView) viewContent.findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);


        recyclerView.setAdapter(meetDynamicsListAdapter);
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

        HttpUtil.sendOkHttpRequest(null, get_recommend_url, requestBody, new Callback(){
            int check_login_user = 0;
            String user_name;

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                //Slog.d(TAG, "response : "+responseText);
                getResponseText(responseText);
                MeetDynamicsFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        meetDynamicsListAdapter.setData(meetList);
                        meetDynamicsListAdapter.notifyDataSetChanged();
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
                recommend_response= new JSONObject(responseText);
                recommendation = recommend_response.getJSONArray("recommendation");
                set_meet_member_info(recommendation);
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    public void set_meet_member_info(JSONArray recommendation){
        int length = recommendation.length();
        Slog.d(TAG, "==========set_meet_member_info==========recommendation length: "+length);
        try{
            for (int i=0; i< length; i++){
                JSONObject recommender = recommendation.getJSONObject(i);
                meetDynamics = new MeetDynamics();

                meetDynamics.setRealname(recommender.getString("realname"));
                meetDynamics.setUid(recommender.getInt("uid"));
                meetDynamics.setPictureUri(recommender.getString("picture_uri"));
                meetDynamics.setBirth_year(recommender.getInt("birth_year"));
                meetDynamics.setHeight(recommender.getInt("height"));
                meetDynamics.setUniversity(recommender.getString("university"));
                meetDynamics.setDegree(recommender.getString("degree"));
                meetDynamics.setJob_title(recommender.getString("job_title"));
                meetDynamics.setLives(recommender.getString("lives"));
                meetDynamics.setSituation(recommender.getInt("situation"));

                //requirement
                meetDynamics.setAge_lower(recommender.getInt("age_lower"));
                meetDynamics.setAge_upper(recommender.getInt("age_upper"));
                meetDynamics.setRequirement_height(recommender.getInt("requirement_height"));
                meetDynamics.setRequirement_degree(recommender.getInt("requirement_degree"));
                meetDynamics.setRequirement_lives(recommender.getString("requirement_lives"));
                meetDynamics.setRequirement_sex(recommender.getInt("requirement_sex"));
                meetDynamics.setIllustration(recommender.getString("illustration"));


                // meetMemberInfo.setSelf(recommender.getInt("self"));
                meetDynamics.setBrowse_count(recommender.getInt("browse_count"));
                meetDynamics.setLoved_count(recommender.getInt("loved_count"));
                // meetMemberInfo.setLoved(recommender.getInt("loved"));
                // meetMemberInfo.setPraised(recommender.getInt("praised"));
                meetDynamics.setPraised_count(recommender.getInt("praised_count"));
                //  meetMemberInfo.setPicture_chain(recommender.getString("picture_chain"));
                // meetMemberInfo.setRequirement_set(recommender.getInt("requirement_set"));


                meetList.add(meetDynamics);
            }
        }catch (JSONException e){

        }

    }

}
