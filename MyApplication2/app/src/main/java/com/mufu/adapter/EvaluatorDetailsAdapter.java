package com.mufu.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mufu.common.OnItemClickListener;
import com.mufu.util.FontManager;
import com.nex3z.flowlayout.FlowLayout;
import com.mufu.R;
import com.mufu.meet.EvaluatorDetailsActivity;
import com.mufu.util.HttpUtil;
import com.mufu.util.ParseUtils;
import com.mufu.util.RoundImageView;
import com.willy.ratingbar.ScaleRatingBar;

import java.util.ArrayList;
import java.util.List;

import static com.mufu.common.MyApplication.getContext;
import static com.mufu.util.ParseUtils.startMeetArchiveActivity;
import static com.mufu.util.SharedPreferencesUtils.getSessionUid;

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
                holder.avatar.setImageDrawable(getContext().getDrawable(R.drawable.male_default_avator));
            }else {
                holder.avatar.setImageDrawable(getContext().getDrawable(R.drawable.female_default_avator));
            }
        }
        
        holder.name.setText(evaluatorDetails.getNickName());
        holder.uid.setText(String.valueOf(evaluatorDetails.getEvaluatorUid()));
        holder.scaleRatingBar.setRating((float) evaluatorDetails.getRating());
        holder.rating.setText(evaluatorDetails.getRating() + "分");
        //Slog.d(TAG, "=====================getImpression: " + evaluatorDetails.getFeatures());

        String features = evaluatorDetails.getFeatures();

        if (evaluatorDetails.getEvaluatorUid() == getSessionUid(getContext())){
            holder.edit.setVisibility(View.VISIBLE);
            holder.edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mItemClickListener.onItemClick(position, view);
                }
            });
        }
        
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
            diyTextView.setTextColor(getContext().getResources().getColor(R.color.white));
            diyTextView.setBackground(context.getDrawable(R.drawable.btn_big_radius_primary));
            featureView.addView(diyTextView);
        }

        featureView.setTag(evaluatorDetails);
    }

    @Override
    public int getItemCount() {
        return null != mEvaluatorDetailsList ? mEvaluatorDetailsList.size() : 0;
    }

    private OnItemClickListener mItemClickListener;

    public void setOnItemClickListener(OnItemClickListener l) {
        this.mItemClickListener = l;
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
        TextView edit;

        public ViewHolder(View view) {
            super(view);
            avatar = view.findViewById(R.id.evaluator_picture);
            name = view.findViewById(R.id.evaluator_name);
            uid = view.findViewById(R.id.uid);
            scaleRatingBar = view.findViewById(R.id.charm_rating);
            rating = view.findViewById(R.id.rating);
            features = view.findViewById(R.id.features);
            edit = view.findViewById(R.id.edit);

            Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.edit), font);
        }
    }

}
