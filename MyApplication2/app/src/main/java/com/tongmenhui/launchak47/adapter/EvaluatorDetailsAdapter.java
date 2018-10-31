package com.tongmenhui.launchak47.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.NetworkImageView;
import com.nex3z.flowlayout.FlowLayout;
import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.meet.EvaluatorDetailsActivity;
import com.tongmenhui.launchak47.meet.MeetMemberInfo;
import com.tongmenhui.launchak47.util.HttpUtil;
import com.tongmenhui.launchak47.util.ParseUtils;
import com.tongmenhui.launchak47.util.RequestQueueSingleton;
import com.willy.ratingbar.ScaleRatingBar;
import com.tongmenhui.launchak47.util.Slog;
import com.tongmenhui.launchak47.meet.ArchivesActivity;

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

import static com.tongmenhui.launchak47.util.ParseUtils.setMeetMemberInfo;

public class EvaluatorDetailsAdapter extends RecyclerView.Adapter<EvaluatorDetailsAdapter.ViewHolder>{
    private static final String TAG = "EvaluatorDetailsAdapter";
    private static final String GET_MEET_ARCHIVE_URL = HttpUtil.DOMAIN + "?q=meet/get_archive";
    private Context context;
    RequestQueue queueEvaluator;
    RequestQueueSingleton requestQueueSingleton;
    private List<EvaluatorDetailsActivity.EvaluatorDetails> mEvaluatorDetailsList;

    public EvaluatorDetailsAdapter(Context context){
        this.context = context;
        mEvaluatorDetailsList = new ArrayList<>();
        requestQueueSingleton = new RequestQueueSingleton();
    }
    
        public void setData(List<EvaluatorDetailsActivity.EvaluatorDetails> evaluatorDetailsList){
        mEvaluatorDetailsList = evaluatorDetailsList;
    }

    @Override
    public EvaluatorDetailsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.evaluator_details_item, parent, false);
        EvaluatorDetailsAdapter.ViewHolder holder = new EvaluatorDetailsAdapter.ViewHolder(view);
        return holder;
    }
    
        @Override
    public void onBindViewHolder(@NonNull final EvaluatorDetailsAdapter.ViewHolder holder, int position) {
        final EvaluatorDetailsActivity.EvaluatorDetails evaluatorDetails = mEvaluatorDetailsList.get(position);
        holder.headUri.setTag(HttpUtil.DOMAIN+"/"+evaluatorDetails.getPictureUri());
        queueEvaluator = requestQueueSingleton.instance(context);
        HttpUtil.loadByImageLoader(queueEvaluator, holder.headUri, HttpUtil.DOMAIN+"/"+evaluatorDetails.getPictureUri(), 50, 50);

        holder.name.setText(evaluatorDetails.getName());
        holder.uid.setText(String.valueOf(evaluatorDetails.getEvaluatorUid()));
        holder.scaleRatingBar.setRating((float) evaluatorDetails.getRating());
        holder.rating.setText(evaluatorDetails.getRating()+"分");
        Slog.d(TAG, "=====================getImpression: "+evaluatorDetails.getFeatures());
        if(evaluatorDetails.getFeatures() != null && !"".equals(evaluatorDetails.getFeatures())){
            String[] featureArray = evaluatorDetails.getFeatures().split("#");
            holder.features.removeAllViews();
            for (int i=0; i<featureArray.length; i++){
                TextView diyTextView = new TextView(context);
                diyTextView.setPadding((int) dpToPx(8), (int) dpToPx(8), (int) dpToPx(8), (int) dpToPx(8));
                diyTextView.setText(featureArray[i]);
                diyTextView.setGravity(Gravity.CENTER);
                diyTextView.setBackground(context.getDrawable(R.drawable.label_bg));
                holder.features.addView(diyTextView);
            }
        }
        holder.headUri.setOnClickListener(new View.OnClickListener() {
            int uid = Integer.parseInt(holder.uid.getText().toString());
            @Override
            public void onClick(View view) {
                getMeetArchive(context, uid);
            }
        });
    }

    public static void getMeetArchive(final Context context, int uid){
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(context, GET_MEET_ARCHIVE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.body() != null){
                    String responseText = response.body().string();
                    Slog.d(TAG, "==========get archive response text : "+responseText);
                    if(responseText != null){
                        if(!TextUtils.isEmpty(responseText)){
                            try {
                                JSONObject jsonObject = new JSONObject(responseText);
                                MeetMemberInfo meetMemberInfo = setMeetMemberInfo(jsonObject);
                                Intent intent = new Intent(context, ArchivesActivity.class);
                                // Log.d(TAG, "meet:"+meet+" uid:"+meet.getUid());
                                intent.putExtra("meet", meetMemberInfo);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                context.startActivity(intent);
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
    }
    
        @Override
    public int getItemCount() {
        return  null != mEvaluatorDetailsList? mEvaluatorDetailsList.size():0;
    }
    
        public static class ViewHolder extends RecyclerView.ViewHolder{
        NetworkImageView headUri;
        TextView name;
        TextView uid;
        ScaleRatingBar scaleRatingBar;
        FlowLayout features;
        TextView rating;

        public ViewHolder(View view){
            super(view);
            headUri = view.findViewById(R.id.evaluator_picture);
            name = view.findViewById(R.id.evaluator_name);
            uid = view.findViewById(R.id.uid);
            scaleRatingBar = view.findViewById(R.id.charm_rating);
            rating = view.findViewById(R.id.rating);
            features = view.findViewById(R.id.features);
        }
    }
    
    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

}
