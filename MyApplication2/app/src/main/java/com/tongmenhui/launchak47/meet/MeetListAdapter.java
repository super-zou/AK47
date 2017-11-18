package com.tongmenhui.launchak47.meet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.res.AssetManager;

import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.util.FontManager;
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
    private static Context mContext;

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView realname;
        TextView lives;
        TextView selfcondition;
        TextView requirement;
        ImageView headUri;
        TextView illustration;
        TextView eyeView;
        TextView lovedView;
        TextView thumbsView;
        TextView photosView;

        public ViewHolder(View view){

            super(view);
            realname = (TextView) view.findViewById(R.id.name);
            lives = (TextView) view.findViewById(R.id.lives);
            headUri = (ImageView) view.findViewById(R.id.recommend_head_uri);
            selfcondition = (TextView) view.findViewById(R.id.self_condition);
            requirement = (TextView) view.findViewById(R.id.partner_requirement);
            illustration = (TextView) view.findViewById(R.id.illustration);
            eyeView = (TextView)view.findViewById(R.id.eye_statistics);
            lovedView = (TextView)view.findViewById(R.id.loved_statistics);
            thumbsView = (TextView)view.findViewById(R.id.thumbs_up_statistics);
            photosView = (TextView)view.findViewById(R.id.photos_statistics);
            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.behavior_statistics), font);

        }
    }

    public MeetListAdapter(Context context, List<MeetRecommend> meetList){
        mContext = context;
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
        holder.lives.setText(meet.getLives());

        picture_url = domain+"/"+meet.getPictureUri();
        Slog.d(TAG, "picture url==========="+picture_url);
        DownloadTask downloadTask = new DownloadTask(holder, picture_url);
        downloadTask.execute();

        holder.selfcondition.setText(meet.getSelfCondition(meet.getSituation()));
        holder.requirement.setText(meet.getRequirement());

        holder.eyeView.setText(String.valueOf(meet.getBrowse_count()));
        holder.lovedView.setText(String.valueOf(meet.getLoved_count()));
        holder.thumbsView.setText(String.valueOf(meet.getPraised_count()));
        holder.illustration.setText(meet.getIllustration());


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
