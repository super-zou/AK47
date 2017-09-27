package com.tongmenhui.launchak47.meet;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.util.HttpUtil;

import java.util.List;

/**
 * Created by haichao.zou on 2017/9/15.
 */

public class MeetListAdapter extends RecyclerView.Adapter<MeetListAdapter.ViewHolder>{
    private List<MeetRecommend> mMeetList;
    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView realname;
        ImageView headUri;

        public ViewHolder(View view){

            super(view);
            realname = (TextView) view.findViewById(R.id.name);
            headUri = (ImageView) view.findViewById(R.id.recommend_head_uri);
        }
    }

    public MeetListAdapter(List<MeetRecommend> meetList){
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
        MeetRecommend meet = mMeetList.get(position);
        Log.d("zouhaichao", "meet get name============="+meet.getRealnameName());
        holder.realname.setText(meet.getRealnameName());
        //Bitmap bitmap = HttpUtil.getHttpBitmap(meet.picture_uri);
       // holder.headUri.setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount(){

        return mMeetList.size();
    }
}
