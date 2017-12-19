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
import android.widget.LinearLayout;

import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.adapter.MeetDynamicsListAdapter;
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
 * Created by haichao.zou on 2017/11/20.
 */

public class MeetDynamicsFragment extends Fragment {
    private static final String TAG = "MeetDynamicsFragment";
    private View viewContent;
    private List<MeetDynamics> meetList = new ArrayList<>();

    private MeetDynamics meetDynamics;
    private RecyclerView recyclerView;
    private DynamicsComment dynamicsComment;
    private MeetDynamicsListAdapter meetDynamicsListAdapter;
    JSONObject dynamics_response;
    JSONObject commentResponse;
    JSONArray dynamics;
    JSONArray commentArray;


    private static final String  domain = "http://www.tongmenhui.com";
    private static final String dynamics_url = domain + "?q=meet/activity/get";

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

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState){
                if(newState == SCROLL_STATE_IDLE){
                    meetDynamicsListAdapter.setScrolling(false);
                    meetDynamicsListAdapter.notifyDataSetChanged();
                }else{
                    meetDynamicsListAdapter.setScrolling(true);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });


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

        HttpUtil.sendOkHttpRequest(null, dynamics_url, requestBody, new Callback(){
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

    }

    public void getResponseText(String responseText){

        Slog.d(TAG, "====================getResponseText====================");

        if(!TextUtils.isEmpty(responseText)){
            try {
                dynamics_response= new JSONObject(responseText);
                dynamics = dynamics_response.getJSONArray("activity");
                if(dynamics.length() > 0){
                    set_meet_member_info(dynamics);
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    public void set_meet_member_info(JSONArray recommendation){
        int length = recommendation.length();
        //Slog.d(TAG, "==========set_meet_member_info==========recommendation length: "+length);
        try{
            for (int i=0; i< length; i++){
                JSONObject dynamics = recommendation.getJSONObject(i);
                Slog.d(TAG, "==========recommendation.getJSONObject: "+dynamics);
                meetDynamics = new MeetDynamics();

                meetDynamics.setRealname(dynamics.getString("realname"));
                meetDynamics.setUid(dynamics.getInt("uid"));
                meetDynamics.setPictureUri(dynamics.getString("picture_uri"));
                meetDynamics.setBirth_year(dynamics.getInt("birth_year"));
                meetDynamics.setHeight(dynamics.getInt("height"));
                meetDynamics.setUniversity(dynamics.getString("university"));
                meetDynamics.setDegree(dynamics.getString("degree"));
                meetDynamics.setJob_title(dynamics.getString("job_title"));
                meetDynamics.setLives(dynamics.getString("lives"));
                meetDynamics.setSituation(dynamics.getInt("situation"));

                //requirement
                meetDynamics.setAge_lower(dynamics.getInt("age_lower"));
                meetDynamics.setAge_upper(dynamics.getInt("age_upper"));
                meetDynamics.setRequirement_height(dynamics.getInt("requirement_height"));
                meetDynamics.setRequirement_degree(dynamics.getInt("requirement_degree"));
                meetDynamics.setRequirement_lives(dynamics.getString("requirement_lives"));
                meetDynamics.setRequirement_sex(dynamics.getInt("requirement_sex"));
                meetDynamics.setIllustration(dynamics.getString("illustration"));

                //interact count
                meetDynamics.setBrowse_count(dynamics.getInt("browse_count"));
                meetDynamics.setLoved_count(dynamics.getInt("loved_count"));
                meetDynamics.setPraised_count(dynamics.getInt("praised_count"));

                //dynamics content
                String content = dynamics.getString("content");
                if(content != null && content.length() != 0){
                    meetDynamics.setContent(content);
                }
                if(!dynamics.isNull("activity_picture")){
                    String dynamics_pictures = dynamics.getString("activity_picture");
                    if(!"".equals(dynamics_pictures)){
                        meetDynamics.setActivityPicture(dynamics_pictures);
                    }
                }

                meetDynamics.setAid(dynamics.getLong("aid"));

                getDynamicsComment(dynamics.getLong("aid"));

                meetList.add(meetDynamics);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public void getDynamicsComment(Long aid){
        String request_comment_url = "?q=meet/activity/interact/get";
        RequestBody requestBody = new FormBody.Builder().add("aid",aid.toString()).build();

        HttpUtil.sendOkHttpRequest(null, domain+request_comment_url, requestBody, new Callback(){

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "######################comment: "+responseText);
                if(!TextUtils.isEmpty(responseText)){
                    try {
                        commentResponse= new JSONObject(responseText);
                        commentArray = commentResponse.getJSONArray("comment");
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                    if(commentArray.length() > 0){
                        setDynamicsComment(commentArray);
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e){

            }
        });

    }
    public void setDynamicsComment(JSONArray commentArray){
        JSONObject comment;
        for (int i=0; i<commentArray.length(); i++){
            try {
                comment = commentArray.getJSONObject(i);
                dynamicsComment = new DynamicsComment();
                dynamicsComment.setCid(comment.getInt("cid"));
                dynamicsComment.setAid(comment.getInt("aid"));
                dynamicsComment.setPictureUrl(comment.getString("picture_uri"));
                if(!comment.isNull("author_uid")){
                    dynamicsComment.setAuthorUid(comment.getLong("author_uid"));
                }
                if(!comment.isNull("author_name")){
                    dynamicsComment.setAuthorName(comment.getString("author_name"));
                }
                dynamicsComment.setCommenterName(comment.getString("commenter_name"));
                dynamicsComment.setCommenterUid(comment.getLong("commenter_uid"));
                dynamicsComment.setContent(comment.getString("content"));

            }catch (JSONException e){
                e.printStackTrace();
            }

            meetDynamics.addComment(dynamicsComment);


        }
    }

}
