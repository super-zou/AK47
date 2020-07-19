package com.mufu.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.mufu.R;
import com.mufu.util.HttpUtil;
import com.mufu.common.MyApplication;
import com.mufu.util.RoundImageView;
import com.mufu.util.UserProfile;
import com.mufu.util.Utility;

import java.util.List;

public class CheeringGroupAdapter extends BaseAdapter {
    private static final String TAG = "CheeringGroupAdapter";
    private static Context mContext;
    int width;
    private List<UserProfile> mCheeringGroupList;

    public CheeringGroupAdapter(Context context, int width) {
        mContext = context;
        this.width = width - (int)Utility.dpToPx(mContext, 4f);
        //this.width = width;
    }
    
    public void setCheeringGroupList(List<UserProfile> cheeringGroupList) {
        mCheeringGroupList = cheeringGroupList;
    }
    
        @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder viewHolder;
        if (convertView == null){
            convertView = View.inflate(mContext, R.layout.cheering_group_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        final UserProfile cheeringGroup = mCheeringGroupList.get(position);
        setViewHolder(viewHolder, cheeringGroup);
        return convertView;
    }
    
        private void setViewHolder(ViewHolder holder, UserProfile cheeringGroup){
        holder.name.setText(cheeringGroup.getNickName());
        //holder.profile.setText(cheeringGroup.getBaseProfile());

        if(cheeringGroup.getSituation() == 0){
            holder.degree.setText(cheeringGroup.getDegreeName(cheeringGroup.getDegree()));
            holder.university.setText(cheeringGroup.getUniversity());
        }else {
            holder.education.setVisibility(View.GONE);
            holder.work.setVisibility(View.VISIBLE);
            holder.title.setText(cheeringGroup.getPosition());
            holder.company.setText(cheeringGroup.getIndustry());
        }
String avatar = cheeringGroup.getAvatar();
        if (avatar != null && !"".equals(avatar)) {
            Glide.with(mContext).load(HttpUtil.DOMAIN + cheeringGroup.getAvatar()).into(holder.headUri);
        } else {
            if(cheeringGroup.getSex() == 0){
                holder.headUri.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.male_default_avator));
            }else {
                holder.headUri.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.female_default_avator));
            }
        }
        ViewGroup.LayoutParams lp = holder.headUri.getLayoutParams();
        lp.width = width/2;
        lp.height = lp.width;
        holder.headUri.setLayoutParams(lp);
    }
    
        @Override
    public int getCount() {
        return mCheeringGroupList != null ? mCheeringGroupList.size() : 0;
    }

    @Override
    public Object getItem(int position){ return  mCheeringGroupList.get(position); }

    @Override
    public long getItemId(int position){ return position; }
    
    public class ViewHolder{
        TextView name;
        TextView degree;
        TextView university;
        TextView title;
        TextView company;
        LinearLayout education;
        LinearLayout work;
        RoundImageView headUri;
        
        public ViewHolder(View view) {
            name = view.findViewById(R.id.name);
            headUri = view.findViewById(R.id.head_uri);
            education = view.findViewById(R.id.education);
            degree = view.findViewById(R.id.degree);
            university = view.findViewById(R.id.university);
            work = view.findViewById(R.id.work);
            title = view.findViewById(R.id.title);
            company = view.findViewById(R.id.company);
        }
        
      }
 }
