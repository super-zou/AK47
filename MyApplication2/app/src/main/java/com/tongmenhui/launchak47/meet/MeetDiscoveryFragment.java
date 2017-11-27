package com.tongmenhui.launchak47.meet;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tongmenhui.launchak47.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by haichao.zou on 2017/11/23.
 */

public class MeetDiscoveryFragment extends Fragment {

    private static final String TAG = "MeetActivityFragment";
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
    private static final String get_activity_url = domain + "?q=meet/activity/get";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        viewContent = inflater.inflate(R.layout.meet_discovery_item,container,false);
        RecyclerView recyclerView = (RecyclerView)viewContent.findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        MeetListAdapter meetListAdapter = new MeetListAdapter(getContext(),meetList);
        recyclerView.setAdapter(meetListAdapter);
        return viewContent;
    }

}
