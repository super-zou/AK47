package com.tongmenhui.launchak47.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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
import com.tongmenhui.launchak47.util.HttpUtil;
import com.tongmenhui.launchak47.util.RequestQueueSingleton;
import com.willy.ratingbar.ScaleRatingBar;
import com.tongmenhui.launchak47.util.Slog;

import java.util.ArrayList;
import java.util.List;

public class EvaluatorDetailsAdapter extends RecyclerView.Adapter<EvaluatorDetailsAdapter.ViewHolder>{
    private static final String TAG = "EvaluatorDetailsAdapter";
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
    public void onBindViewHolder(@NonNull EvaluatorDetailsAdapter.ViewHolder holder, int position) {
        final EvaluatorDetailsActivity.EvaluatorDetails evaluatorDetails = mEvaluatorDetailsList.get(position);
        holder.headUri.setTag(HttpUtil.DOMAIN+"/"+evaluatorDetails.getPictureUri());
        queueEvaluator = requestQueueSingleton.instance(context);
        HttpUtil.loadByImageLoader(queueEvaluator, holder.headUri, HttpUtil.DOMAIN+"/"+evaluatorDetails.getPictureUri(), 50, 50);

        holder.name.setText(evaluatorDetails.getName());
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
    }
    
        @Override
    public int getItemCount() {
        return  null != mEvaluatorDetailsList? mEvaluatorDetailsList.size():0;
    }
    
        public static class ViewHolder extends RecyclerView.ViewHolder{
        NetworkImageView headUri;
        TextView name;
        ScaleRatingBar scaleRatingBar;
        FlowLayout features;
        TextView rating;

        public ViewHolder(View view){
            super(view);
            headUri = view.findViewById(R.id.evaluator_picture);
            name = view.findViewById(R.id.evaluator_name);
            scaleRatingBar = view.findViewById(R.id.charm_rating);
            rating = view.findViewById(R.id.rating);
            features = view.findViewById(R.id.features);
        }
    }
    
    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

}
