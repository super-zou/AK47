package com.tongmenhui.launchak47.main;

//import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.meet.Meet;
import com.tongmenhui.launchak47.meet.MeetListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by super-zou on 17-9-11.
 */

public class ContentFragment extends Fragment {
    private View viewContent;
    private int mType = 0;
    private String mTitle;
    private List<Meet> meetList = new ArrayList<>();


    public void setType(int mType) {
        this.mType = mType;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //布局文件中只有一个居中的TextView
        class_init();
        viewContent = inflater.inflate(R.layout.fragment_content,container,false);
        RecyclerView recyclerView = (RecyclerView)viewContent.findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        MeetListAdapter meetListAdapter = new MeetListAdapter(meetList);
        recyclerView.setAdapter(meetListAdapter);
        return viewContent;
    }

    public void class_init(){
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
    }
}
