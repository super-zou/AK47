package com.tongmenhui.launchak47.meet;

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

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by haichao.zou on 2017/11/20.
 */

public class MeetActivityFragment extends Fragment {
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


        viewContent = inflater.inflate(R.layout.fragment_meet_activity_item,container,false);
        RecyclerView recyclerView = (RecyclerView)viewContent.findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
       // MeetListAdapter meetListAdapter = new MeetListAdapter(getContext(),meetList);
       // recyclerView.setAdapter(meetListAdapter);
        return viewContent;
    }

}
