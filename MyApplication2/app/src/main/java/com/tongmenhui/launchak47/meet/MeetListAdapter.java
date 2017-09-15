package com.tongmenhui.launchak47.meet;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tongmenhui.launchak47.R;

import java.util.List;

/**
 * Created by haichao.zou on 2017/9/15.
 */

public class MeetListAdapter extends RecyclerView.Adapter<MeetListAdapter.ViewHolder>{
    private List<Meet> mMeetList;
    static class ViewHolder extends RecyclerView.ViewHolder{
        public ViewHolder(View view){
            super(view);
        }
    }

    public MeetListAdapter(List<Meet> meetList){
        mMeetList = meetList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.meet_item, parent, false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        Meet meet = mMeetList.get(position);
    }

    @Override
    public int getItemCount(){
        return mMeetList.size();
    }
}
