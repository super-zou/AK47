package com.tongmenhui.launchak47.util;

import android.content.Context;
import android.graphics.Picture;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.NetworkImageView;
import com.tongmenhui.launchak47.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by super-zou on 17-12-10.
 */

public class PictureAdapter extends BaseAdapter {
    private static final String TAG = "PictureAdapter";
    private Context context;
    private RequestQueue requestQueue;
    GridView gridView;
    LinearLayout.LayoutParams lp;
    private List<Picture> pictureList = new ArrayList<Picture>();


    public PictureAdapter(Context context){
        super();
        this.context = context;
        lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    }

    public void setPictureList(String[] picures_url){
        Slog.d(TAG, "==============setPictureList=========="+picures_url.length);

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

    public void setGridView(GridView gridView){
        this.gridView = gridView;
    }

    @Override
    public int getCount(){
        Slog.d(TAG, "==============getCount=========="+pictureList.size());
        if(null != pictureList){
            return pictureList.size();
        }else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position){
        Slog.d(TAG, "==============getItem==========");
        return pictureList.get(position);
    }

    @Override
    public long getItemId(int position){
        Slog.d(TAG, "==============getItemId==========");

        return position;
    }

    @Override

    public View getView(int position, View convertView, ViewGroup parent){
        Slog.d(TAG, "*******************************************************************getView==========");
        //ViewHolder viewHolder = null;
        NetworkImageView networkImageView;
        //if (convertView == null) {

            //viewHolder = new ViewHolder();
            // 获得容器
            //convertView = LayoutInflater.from(this.context).inflate(R.layout.dynamics_item, null);
            View view = View.inflate(this.context, R.layout.dynamics_item, null);
            //networkImageView = ViewHolder.get(convertView, R.id.dynamics_item_picture);
            networkImageView = (NetworkImageView)view.findViewById(R.id.dynamics_item_picture);

            // 初始化组件
            /*
            ViewHolder.networkImageView = new NetworkImageView(context);
            ViewHolder.networkImageView.setLayoutParams(lp);
            gridView.addView(ViewHolder.networkImageView);
            */
            // 给converHolder附加一个对象
           // convertView.setTag(viewHolder);
        //} else {
            // 取得converHolder附加的对象
            //viewHolder = (ViewHolder) convertView.getTag();
        //}

        // 给组件设置资源
        Picture picture = getPictureList().get(position);
        networkImageView.setImageDrawable(context.getDrawable(R.mipmap.ic_launcher));
        HttpUtil.loadByImageLoader(getRequestQueue(), networkImageView, picture.getUrl(), 110, 110);
        //viewHolder.title.setText(picture.getTitle());

        return convertView;
    }

    static class ViewHolder{
        public NetworkImageView networkImageView;
        /*
        public static  <T extends View> T get(View view, int id) {
            SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
            if (viewHolder == null) {
                viewHolder = new SparseArray<View>();
                view.setTag(viewHolder);
            }
            View childView = viewHolder.get(id);
            if (childView == null) {
                childView = view.findViewById(id);
                viewHolder.put(id, childView);
            }
            return (T) childView;
        }*/
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
