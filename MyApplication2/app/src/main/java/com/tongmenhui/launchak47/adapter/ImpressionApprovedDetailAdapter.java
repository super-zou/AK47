package com.tongmenhui.launchak47.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.meet.ArchivesActivity;
import com.tongmenhui.launchak47.meet.MeetMemberInfo;
import com.tongmenhui.launchak47.util.HttpUtil;
import com.tongmenhui.launchak47.util.RequestQueueSingleton;
import com.tongmenhui.launchak47.util.SearchUserListAdapter;
import com.tongmenhui.launchak47.util.Slog;

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

import static com.tongmenhui.launchak47.util.ParseUtils.getMeetArchive;
import static com.tongmenhui.launchak47.util.ParseUtils.setMeetMemberInfo;

public class ImpressionApprovedDetailAdapter extends RecyclerView.Adapter<ImpressionApprovedDetailAdapter.ViewHolder> {
    private static final String TAG = "ImpressionApprovedDetailAdapter";
    private ArrayList<MeetMemberInfo> mUnfilteredData;
    private List<MeetMemberInfo> mMemberInfoList;
    private Context mContext;
    RequestQueue queue;

    public ImpressionApprovedDetailAdapter(Context context){
        mContext = context;
    }

    public void setData(List<MeetMemberInfo> memberInfoList){
        mMemberInfoList = memberInfoList;
    }
    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView headPic;
        public TextView name;
        public TextView profile;

        public ViewHolder(View view){
            super(view);
            headPic =  view.findViewById(R.id.networkImageView);
            name = view.findViewById(R.id.name);
            profile = view.findViewById(R.id.profile);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.impression_approved_user_info, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final MeetMemberInfo memberInfo = mMemberInfoList.get(position);
        Slog.d(TAG, "=============get real name: "+memberInfo.getRealname());
        holder.name.setText(memberInfo.getRealname());
        String profile = "";
        if(memberInfo.getSituation() == 0){//student
            profile = memberInfo.getUniversity()+"."+memberInfo.getDegree()+"."+memberInfo.getDegree();
        }else{
            profile = memberInfo.getJobTitle()+"."+memberInfo.getCompany();
            if(!"".equals(memberInfo.getLives())){
                profile += "."+memberInfo.getLives();
            }
        }
        holder.profile.setText(profile.replaceAll(" ",""));

        if(memberInfo.getPictureUri() != null && !"".equals(memberInfo.getPictureUri())){
            queue = RequestQueueSingleton.instance(mContext);
            holder.headPic.setTag(HttpUtil.DOMAIN+memberInfo.getPictureUri());
            HttpUtil.loadByImageLoader(queue, holder.headPic, HttpUtil.DOMAIN + memberInfo.getPictureUri(), 50, 50);
        }else{
            holder.headPic.setImageDrawable(mContext.getDrawable(R.mipmap.ic_launcher));
        }
        
        holder.headPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getMeetArchive(mContext, memberInfo.getUid());
                /*
                 String GET_MEET_ARCHIVE_URL = HttpUtil.DOMAIN + "?q=meet/get_archive";
                RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(37)).build();
                HttpUtil.sendOkHttpRequest(mContext, GET_MEET_ARCHIVE_URL, requestBody, new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if(response.body() != null){
                            String responseText = response.body().string();
                            Slog.d(TAG, "==========get archive response text : "+responseText);
                            if(responseText != null){
                                if(!TextUtils.isEmpty(responseText)){
                                    try {
                                        JSONObject jsonObject = new JSONObject(responseText).optJSONObject("archive");
                                        MeetMemberInfo meetMemberInfo = setMeetMemberInfo(jsonObject);
                                        Intent intent = new Intent(mContext, ArchivesActivity.class);
                                        // Log.d(TAG, "meet:"+meet+" uid:"+meet.getUid());
                                        intent.putExtra("meet", meetMemberInfo);
                                       // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                        Slog.d(TAG, "=====================hahahahahah");
                                       // intent.setClass(mContext, ArchivesActivity.class);
                                        mContext.startActivity(intent);

                                    }catch (JSONException e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call call, IOException e) {}
                });
                */
            }
        });
    }

    @Override
    public int getItemCount(){
          return mMemberInfoList != null ? mMemberInfoList.size():0;
    }
}
