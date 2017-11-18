package com.tongmenhui.launchak47.main;

//import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tongmenhui.launchak47.Login;
import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.meet.MeetRecommend;
import com.tongmenhui.launchak47.meet.MeetListAdapter;
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

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by super-zou on 17-9-11.
 */

public class ContentFragment extends Fragment {
    private static final String TAG = "ContentFragment";
    private View viewContent;
    private int mType = 0;
    private String mTitle;
    private List<MeetRecommend> meetList = new ArrayList<>();
    private MeetRecommend meetRecommend;
   // private String realname;
    private int uid;
    private static String responseText;
    JSONObject recommend_response;
    JSONArray recommendation;

    private static final String  domain = "http://www.tongmenhui.com";
    private static final String get_recommend_url = domain + "?q=meet/recommend";

    public void setType(int mType) {
        this.mType = mType;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        meet_member_init();
        viewContent = inflater.inflate(R.layout.fragment_content,container,false);
        RecyclerView recyclerView = (RecyclerView)viewContent.findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        MeetListAdapter meetListAdapter = new MeetListAdapter(getContext(),meetList);
        recyclerView.setAdapter(meetListAdapter);
        return viewContent;
    }

    public void meet_member_init(){
        get_meet_member_info();
    }

    public void get_meet_member_info(){


        RequestBody requestBody = new FormBody.Builder().build();
        SharedPreferences preferences =  getActivity().getSharedPreferences("session", MODE_PRIVATE);
        String session = preferences.getString("session_name", "");

        //Slog.d(TAG, "=====in ContentFragment====session: "+session);

        HttpUtil.getOkHttpRequestAsync(get_recommend_url, session, new Callback(){
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                responseText = response.body().string();
                //Slog.d(TAG, "response : "+responseText);
            }

            @Override
            public void onFailure(Call call, IOException e){

            }
        });

        HttpUtil.sendOkHttpRequest(null, get_recommend_url, requestBody, new Callback(){
            int check_login_user = 0;
            String user_name;

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "response : "+responseText);
                if(!TextUtils.isEmpty(responseText)){
                    try {
                        JSONObject login_response= new JSONObject(responseText);

                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e){

            }
        });
       // Slog.d(TAG, "=============response : "+responseText);
        if(!TextUtils.isEmpty(responseText)){
            try {
                recommend_response= new JSONObject(responseText);
                recommendation = recommend_response.getJSONArray("recommendation");
                int requirement_set = recommend_response.getInt("requirement_set");
                Slog.d(TAG, "========requirement_set: "+requirement_set);
                set_meet_member_info(recommendation);


            }catch (JSONException e){
                e.printStackTrace();
            }
        }

        //set_meet_member_info(recommendation);
        //test_data();

    }

    public void set_meet_member_info(JSONArray recommendation){
        int length = recommendation.length();
        Slog.d(TAG, "==========set_meet_member_info==========recommendation length: "+length);
        try{
            for (int i=0; i< length; i++){
                JSONObject recommender = recommendation.getJSONObject(i);
                meetRecommend = new MeetRecommend();

                meetRecommend.setRealname(recommender.getString("realname"));
                meetRecommend.setUid(recommender.getInt("uid"));
                meetRecommend.setPictureUri(recommender.getString("picture_uri"));
                meetRecommend.setBirth_year(recommender.getInt("birth_year"));
                meetRecommend.setHeight(recommender.getInt("height"));
                meetRecommend.setUniversity(recommender.getString("university"));
                meetRecommend.setDegree(recommender.getString("degree"));
                meetRecommend.setJob_title(recommender.getString("job_title"));
                meetRecommend.setLives(recommender.getString("lives"));
                meetRecommend.setSituation(recommender.getInt("situation"));

                //requirement
                meetRecommend.setAge_lower(recommender.getInt("age_lower"));
                meetRecommend.setAge_upper(recommender.getInt("age_upper"));
                meetRecommend.setRequirement_height(recommender.getInt("requirement_height"));
                meetRecommend.setRequirement_degree(recommender.getInt("requirement_degree"));
                meetRecommend.setRequirement_lives(recommender.getString("requirement_lives"));
                meetRecommend.setRequirement_sex(recommender.getInt("requirement_sex"));
                meetRecommend.setIllustration(recommender.getString("illustration"));


               // meetRecommend.setSelf(recommender.getInt("self"));
                meetRecommend.setBrowse_count(recommender.getInt("browse_count"));
                meetRecommend.setLoved_count(recommender.getInt("loved_count"));
               // meetRecommend.setLoved(recommender.getInt("loved"));
               // meetRecommend.setPraised(recommender.getInt("praised"));
               // meetRecommend.setPraised_count(recommender.getInt("praised_count"));
              //  meetRecommend.setPicture_chain(recommender.getString("picture_chain"));
               // meetRecommend.setRequirement_set(recommender.getInt("requirement_set"));


                meetList.add(meetRecommend);
            }
        }catch (JSONException e){

        }
    }

}
