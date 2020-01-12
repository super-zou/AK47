package com.hetang.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hetang.common.MyApplication;
import com.nex3z.flowlayout.FlowLayout;
import com.hetang.R;
import com.hetang.meet.EvaluatorDetailsActivity;
import com.hetang.util.HttpUtil;
import com.hetang.util.ParseUtils;
import com.hetang.util.RoundImageView;
import com.willy.ratingbar.ScaleRatingBar;

import java.util.ArrayList;
import java.util.List;

import static com.hetang.util.ParseUtils.startMeetArchiveActivity;

public class EvaluatorDetailsAdapter extends RecyclerView.Adapter<EvaluatorDetailsAdapter.ViewHolder> {
    private static final String TAG = "EvaluatorDetailsAdapter";
    private Context context;
    private List<EvaluatorDetailsActivity.EvaluatorDetails> mEvaluatorDetailsList;

    public EvaluatorDetailsAdapter(Context context) {
        this.context = context;
        mEvaluatorDetailsList = new ArrayList<>();
    }

    public void setData(List<EvaluatorDetailsActivity.EvaluatorDetails> evaluatorDetailsList) {
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
        String avatar = evaluatorDetails.getAvatar();
        if (avatar != null && !"".equals(avatar)) {
            Glide.with(context).load(HttpUtil.DOMAIN + avatar).into(holder.avatar);
        } else {
            if(evaluatorDetails.getSex() == 0){
                holder.avatar.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.male_default_avator));
            }else {
                holder.avatar.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.female_default_avator));
            }
        }
        
        holder.name.setText(evaluatorDetails.getNickName());
        holder.uid.setText(String.valueOf(evaluatorDetails.getEvaluatorUid()));
        holder.scaleRatingBar.setRating((float) evaluatorDetails.getRating());
        holder.rating.setText(evaluatorDetails.getRating() + "分");
        //Slog.d(TAG, "=====================getImpression: " + evaluatorDetails.getFeatures());

        String features = evaluatorDetails.getFeatures();
        
        if (!"".equals(features) && !"null".equals(features)) {
            if(holder.features.getTag() == null){
                setFeatureView(holder.features, evaluatorDetails, features);
            }else {
                if(!features.equals(holder.features.getTag())){
                    holder.features.removeAllViews();
                    setFeatureView(holder.features, evaluatorDetails, features);
                }
            }
        }else {
            if (holder.features.getChildCount() > 0){
                holder.features.removeAllViews();
            }
        }
        
        holder.avatar.setOnClickListener(new View.OnClickListener() {
            int uid = Integer.parseInt(holder.uid.getText().toString());
            @Override
            public void onClick(View view) {
                ParseUtils.startMeetArchiveActivity(context, uid);
            }
        });
    }
    
    private void setFeatureView(FlowLayout featureView, EvaluatorDetailsActivity.EvaluatorDetails evaluatorDetails, String features){
        String[] featureArray = features.split("#");

        for (int i = 0; i < featureArray.length; i++) {
            TextView diyTextView = new TextView(context);
            diyTextView.setPadding((int) dpToPx(8), (int) dpToPx(8), (int) dpToPx(8), (int) dpToPx(8));
            diyTextView.setText(featureArray[i]);
            diyTextView.setGravity(Gravity.CENTER);
            diyTextView.setBackground(context.getDrawable(R.drawable.label_bg));
            featureView.addView(diyTextView);
        }
        featureView.setTag(evaluatorDetails);
    }

    @Override
    public int getItemCount() {
        return null != mEvaluatorDetailsList ? mEvaluatorDetailsList.size() : 0;
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RoundImageView avatar;
        TextView name;
        TextView uid;
        ScaleRatingBar scaleRatingBar;
        FlowLayout features;
        TextView rating;

        public ViewHolder(View view) {
            super(view);
            avatar = view.findViewById(R.id.evaluator_picture);
            name = view.findViewById(R.id.evaluator_name);
            uid = view.findViewById(R.id.uid);
            scaleRatingBar = view.findViewById(R.id.charm_rating);
            rating = view.findViewById(R.id.rating);
            features = view.findViewById(R.id.features);
        }
    }

}
