package com.tongmenhui.launchak47.main;

//import android.app.Fragment;
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
import com.tongmenhui.launchak47.meet.Meet;
import com.tongmenhui.launchak47.meet.MeetListAdapter;
import com.tongmenhui.launchak47.util.HttpUtil;
import com.tongmenhui.launchak47.util.Slog;

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

/**
 * Created by super-zou on 17-9-11.
 */

public class ContentFragment extends Fragment {
    private View viewContent;
    private int mType = 0;
    private String mTitle;
    private List<Meet> meetList = new ArrayList<>();

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
        Meet meet1 = new Meet("lilei");
        meetList.add(meet1);
        Meet meet2 = new Meet("hanmeimei");
        meetList.add(meet2);
        Meet meet3 = new Meet("lucy");
        meetList.add(meet3);
        Meet meet4 = new Meet("tom");
        meetList.add(meet4);
        Meet meet5 = new Meet("jerry");
        meetList.add(meet5);
        Meet meet6 = new Meet("alice");
        meetList.add(meet6);
        Meet meet7 = new Meet("lilei");
        meetList.add(meet7);
        Meet meet8 = new Meet("hanmeimei");
        meetList.add(meet8);
        Meet meet9 = new Meet("lucy");
        meetList.add(meet9);
        Meet meet10 = new Meet("tom");
        meetList.add(meet10);
        Meet meet11 = new Meet("jerry");
        meetList.add(meet11);
        Meet meet12 = new Meet("alice");
        meetList.add(meet12);
        Meet meet13 = new Meet("lilei");
        meetList.add(meet13);
        Meet meet21 = new Meet("hanmeimei");
        meetList.add(meet21);
        Meet meet31 = new Meet("lucy");
        meetList.add(meet31);
        Meet meet41 = new Meet("tom");
        meetList.add(meet41);
        Meet meet51 = new Meet("jerry");
        meetList.add(meet51);
        Meet meet61 = new Meet("alice");
        meetList.add(meet61);
        Meet meet19 = new Meet("lilei");
        meetList.add(meet19);
        Meet meet26 = new Meet("hanmeimei");
        meetList.add(meet26);
        Meet meet39 = new Meet("lucy");
        meetList.add(meet39);
        Meet meet40 = new Meet("tom");
        meetList.add(meet40);
        Meet meet52 = new Meet("jerry");
        meetList.add(meet52);
        Meet meet60 = new Meet("alice");
        meetList.add(meet60);
    }

    public void get_meet_member_info(){
        /*
        RequestBody requestBody = new FormBody.Builder()
                .build();

        HttpUtil.sendOkHttpRequest(token, get_recommend_url, requestBody, new Callback(){
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
                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        closeProgressDialog();
                        Toast.makeText(Login.this, "登录失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        */
    }

}
