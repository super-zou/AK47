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
}
