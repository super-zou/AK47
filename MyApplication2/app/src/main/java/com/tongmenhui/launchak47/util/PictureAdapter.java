package com.tongmenhui.launchak47.util;

import android.content.Context;
import android.graphics.Picture;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.NetworkImageView;
import com.tongmenhui.launchak47.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by super-zou on 17-12-10.
 */

public class PictureAdapter extends BaseAdapter {
    private Context context;
    private RequestQueue requestQueue;
    private List<Picture> pictureList = new ArrayList<Picture>();
    ;

    public PictureAdapter(Context context){
        super();
        this.context = context;
    }

    public void setPictureList(String[] picures_url){
        for (int i=0; i<picures_url.length;i++){
            Picture picture = new Picture(picures_url[i]);
            pictureList.add(picture);
        }
    }
    public List<Picture> getPictureList(){
        return pictureList;
    }

    public void setRequestQueue(RequestQueue requestQueue){
        this.requestQueue = requestQueue;
    }
    public RequestQueue getRequestQueue(){
        return requestQueue;
    }

    @Override
    public int getCount(){
        if(null != pictureList){
            return pictureList.size();
        }else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position){
        return pictureList.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder viewHolder = null;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            // 获得容器
            convertView = LayoutInflater.from(this.context).inflate(R.layout.meet_dynamics_item, null);

            // 初始化组件
            viewHolder.networkImageView = new NetworkImageView(context);

            // 给converHolder附加一个对象
            convertView.setTag(viewHolder);
        } else {
            // 取得converHolder附加的对象
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // 给组件设置资源
        Picture picture = pictureList.get(position);
        viewHolder.networkImageView.setImageDrawable(context.getDrawable(R.mipmap.ic_launcher));
        HttpUtil.loadByImageLoader(requestQueue, viewHolder.networkImageView, picture.getUrl(), 110, 110);
        //viewHolder.title.setText(picture.getTitle());

        return convertView;
    }

    class ViewHolder{
        public NetworkImageView networkImageView;
    }

    class Picture{
        private String url;
        private int imageId;

        public Picture(String url){
            this.url = url;
            this.imageId = imageId;
        }

        String getUrl(){
            return url;
        }
    }
}
