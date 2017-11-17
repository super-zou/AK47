package com.tongmenhui.launchak47.meet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.util.Slog;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * Created by haichao.zou on 2017/9/15.
 */

public class MeetListAdapter extends RecyclerView.Adapter<MeetListAdapter.ViewHolder>{
    private static final String TAG = "MeetListAdapter";
    private static final String  domain = "http://www.tongmenhui.com";
    private List<MeetRecommend> mMeetList;
    private String picture_url;
    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView realname;
        TextView selfcondition;
        TextView requirement;
        ImageView headUri;

        public ViewHolder(View view){

            super(view);
            realname = (TextView) view.findViewById(R.id.name);
            headUri = (ImageView) view.findViewById(R.id.recommend_head_uri);
            selfcondition = (TextView) view.findViewById(R.id.self_condition);
            requirement = (TextView) view.findViewById(R.id.partner_requirement);

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
    public void onBindViewHolder(final ViewHolder holder, int position){

        MeetRecommend meet = mMeetList.get(position);
        Slog.d(TAG, "get name============="+meet.getRealname());
        holder.realname.setText(meet.getRealname());

        picture_url = domain+"/"+meet.getPictureUri();
        Slog.d(TAG, "picture url==========="+picture_url);
        DownloadTask downloadTask = new DownloadTask(holder, picture_url);
        downloadTask.execute();

        holder.selfcondition.setText(meet.getSelfCondition(meet.getSituation()));
        holder.requirement.setText(meet.getRequirement());

    }

    @Override
    public int getItemCount(){

        return mMeetList.size();
    }

    private class DownloadTask extends AsyncTask<String, Object, Bitmap> {
        private ViewHolder mViewHolder;
        private String picture_url;

        public DownloadTask(ViewHolder mViewHolder, String picture_url) {
            this.mViewHolder = mViewHolder;
            this.picture_url = picture_url;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            //String url = params[0];
            Bitmap bitmap = null;
            try {
                //加载一个网络图片
                InputStream is = new URL(picture_url).openStream();
                bitmap = BitmapFactory.decodeStream(is);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            mViewHolder.headUri.setImageBitmap(result);
            //imageView.setImageBitmap(result);
        }
    }
}
