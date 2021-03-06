package com.tongmenhui.launchak47.meet;

//import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.adapter.MeetRecommendListAdapter;
import com.tongmenhui.launchak47.util.BaseFragment;
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

import static android.content.Context.MODE_PRIVATE;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

/**
 * Created by super-zou on 17-9-11.
 */

public class MeetRecommendFragment extends BaseFragment {
    private static final boolean debug = false;
    private static final String TAG = "MeetRecommendFragment";
    private View viewContent;
    private int mType = 0;
    private String mTitle;
    private List<MeetMemberInfo> meetList = new ArrayList<>();
    private MeetMemberInfo meetMemberInfo;
    private RecyclerView recyclerView;
    private MeetRecommendListAdapter meetRecommendListAdapter;
   // private String realname;
    private int uid;
    private static String responseText;
    JSONObject recommend_response;
    JSONArray recommendation;
    private Boolean loaded = false;
    private Handler handler;
    private static final int DONE = 1;

    private static final String  domain = "http://112.126.83.127:88/";
    private static final String get_recommend_url = domain + "?q=meet/recommend";

    @Override
    protected void initView(View view){

    }

    @Override
    protected void loadData(boolean firstInit){

    }

    @Override
    protected int getLayoutId(){
        return 0;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(debug) Slog.d(TAG, "=================onCreateView===================");
        initContentView();
        meetRecommendListAdapter = new MeetRecommendListAdapter(getContext());
        viewContent = inflater.inflate(R.layout.meet_recommend, container, false);
        TextView addMeetInfo = (TextView)viewContent.findViewById(R.id.meet_info_add);
        addMeetInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FillMeetInfoActivity.class);
                startActivity(intent);
            }
        });
        recyclerView = (RecyclerView) viewContent.findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState){
                if(newState == SCROLL_STATE_IDLE){
                    meetRecommendListAdapter.setScrolling(false);
                    meetRecommendListAdapter.notifyDataSetChanged();
                }else{
                    meetRecommendListAdapter.setScrolling(true);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        recyclerView.setAdapter(meetRecommendListAdapter);
        return viewContent;

    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(debug) Slog.d(TAG, "=================onViewCreated===================");
       // initConentView();
    }

    public void initContentView(){
        if(debug) Slog.d(TAG, "===============initConentView==============");

        RequestBody requestBody = new FormBody.Builder().build();
        HttpUtil.sendOkHttpRequest(getContext(), get_recommend_url, requestBody, new Callback(){
            int check_login_user = 0;
            String user_name;

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                //Slog.d(TAG, "response : "+responseText);
                getResponseText(responseText);
            }

            @Override
            public void onFailure(Call call, IOException e){

            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(Message message){
                if(message.what == DONE){
                    meetRecommendListAdapter.setData(meetList);
                    meetRecommendListAdapter.notifyDataSetChanged();
                }
            }
        };

       // getResponseText(responseText);

    }

    public void getResponseText(String responseText){

        if(debug) Slog.d(TAG, "====================getResponseText: "+responseText);

        if(!TextUtils.isEmpty(responseText)){
            try {
                recommend_response= new JSONObject(responseText);
                recommendation = recommend_response.getJSONArray("recommendation");
                set_meet_member_info(recommendation);
                loaded = true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    public void set_meet_member_info(JSONArray recommendation){
        int length = recommendation.length();
        if(debug) Slog.d(TAG, "==========set_meet_member_info==========recommendation length: "+length);
        try{
            for (int i=0; i< length; i++){
                JSONObject recommender = recommendation.getJSONObject(i);
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


                meetList.add(meetMemberInfo);
            }

            handler.sendEmptyMessage(DONE);
        }catch (JSONException e){
            e.printStackTrace();
        }

    }

}
