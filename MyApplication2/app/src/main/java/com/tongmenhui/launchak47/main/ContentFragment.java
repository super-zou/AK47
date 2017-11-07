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
        MeetListAdapter meetListAdapter = new MeetListAdapter(meetList);
        recyclerView.setAdapter(meetListAdapter);
        return viewContent;
    }

    public void meet_member_init(){
        get_meet_member_info();





    }

    public void test_data(){
        Slog.d(TAG, "================test_data");
        MeetRecommend meet1 = new MeetRecommend("lilei");
        meetList.add(meet1);
        MeetRecommend meet2 = new MeetRecommend("hanmeimei");
        meetList.add(meet2);
        MeetRecommend meet3 = new MeetRecommend("lucy");
        meetList.add(meet3);
        MeetRecommend meet4 = new MeetRecommend("tom");
        meetList.add(meet4);
        MeetRecommend meet5 = new MeetRecommend("jerry");
        meetList.add(meet5);
        MeetRecommend meet6 = new MeetRecommend("alice");
        meetList.add(meet6);
        MeetRecommend meet7 = new MeetRecommend("lilei");
        meetList.add(meet7);
        MeetRecommend meet8 = new MeetRecommend("hanmeimei");
        meetList.add(meet8);
        MeetRecommend meet9 = new MeetRecommend("lucy");
        meetList.add(meet9);
        MeetRecommend meet10 = new MeetRecommend("tom");
        meetList.add(meet10);
        MeetRecommend meet11 = new MeetRecommend("jerry");
        meetList.add(meet11);
        MeetRecommend meet12 = new MeetRecommend("alice");
        meetList.add(meet12);
        MeetRecommend meet13 = new MeetRecommend("lilei");
        meetList.add(meet13);
        MeetRecommend meet21 = new MeetRecommend("hanmeimei");
        meetList.add(meet21);
        MeetRecommend meet31 = new MeetRecommend("lucy");
        meetList.add(meet31);
        MeetRecommend meet41 = new MeetRecommend("tom");
        meetList.add(meet41);
        MeetRecommend meet51 = new MeetRecommend("jerry");
        meetList.add(meet51);
        MeetRecommend meet61 = new MeetRecommend("alice");
        meetList.add(meet61);
        MeetRecommend meet19 = new MeetRecommend("lilei");
        meetList.add(meet19);
        MeetRecommend meet26 = new MeetRecommend("hanmeimei");
        meetList.add(meet26);
        MeetRecommend meet39 = new MeetRecommend("lucy");
        meetList.add(meet39);
        MeetRecommend meet40 = new MeetRecommend("tom");
        meetList.add(meet40);
        MeetRecommend meet52 = new MeetRecommend("jerry");
        meetList.add(meet52);
        MeetRecommend meet60 = new MeetRecommend("alice");
        meetList.add(meet60);

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
                /*
                if(!TextUtils.isEmpty(responseText)){
                    try {
                        recommend_response= new JSONObject(responseText);
                        recommendation = recommend_response.getJSONArray("recommendation");
                        int requirement_set = recommend_response.getInt("requirement_set");
                        Slog.d(TAG, "========requirement_set: "+requirement_set);
                        //set_meet_member_info(recommendation);


                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }*/
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
                String realname = recommender.getString("realname");
                //  uid = recommender.getInt("uid");

               // Slog.d(TAG, "==============realname: "+realname);
                meetRecommend = new MeetRecommend(realname);
                //meetRecommend.realname = recommender.getString("realname");
                //meetRecommend.setRealname(recommender.getString("realname"));

                meetRecommend.setUid(uid);
                meetRecommend.setPicture_uri(recommender.getString("picture_uri"));

                //meetRecommend.picture_uri = recommender.getString("picture_uri");
                             /*
                            meetRecommend.birth_day = recommender.getInt("birth_day");
                            meetRecommend.height = recommender.getInt("height");
                            meetRecommend.university = recommender.getString("university");
                            meetRecommend.degree = recommender.getString("degree");
                            meetRecommend.job_title = recommender.getString("job_title");
                            meetRecommend.lives = recommender.getString("lives");
                            meetRecommend.situation = recommender.getInt("situation");

                            //requirement
                            meetRecommend.age_lower = recommender.getInt("age_lower");
                            meetRecommend.age_upper = recommender.getInt("age_upper");
                            meetRecommend.requirement_height = recommender.getInt("requirement_height");
                            meetRecommend.requirement_degree = recommender.getInt("requirement_degree");
                            meetRecommend.requirement_lives = recommender.getString("requirement_lives");
                            meetRecommend.requirement_sex = recommender.getInt("requirement_sex");
                            meetRecommend.illustration = recommender.getString("illustration");
                            //meetRecommend.self = recommender.getInt("self");
                            //meetRecommend.loved_count = recommender.getInt("loved_count");
                            //meetRecommend.loved = recommender.getInt("loved");
                            //meetRecommend.praised = recommender.getInt("praised");
                            //meetRecommend.praised_count = recommender.getInt("praised_count");
                            //meetRecommend.picture_chain = recommender.getString("picture_chain");

                            //meetRecommend.requirement_set = recommender.getInt("requirement_set");
                             */

                meetList.add(meetRecommend);
            }
        }catch (JSONException e){

        }
    }

}
