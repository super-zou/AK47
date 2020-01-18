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
import com.hetang.R;
import com.hetang.common.MyApplication;
import com.hetang.common.TalentEvaluatorDetailsActivity;
import com.hetang.meet.EvaluatorDetailsActivity;
import com.hetang.util.HttpUtil;
import com.hetang.util.ParseUtils;
import com.hetang.util.RoundImageView;
import com.nex3z.flowlayout.FlowLayout;
import com.willy.ratingbar.ScaleRatingBar;

import java.util.ArrayList;
import java.util.List;

public class TalentEvaluatorDetailsAdapter extends RecyclerView.Adapter<TalentEvaluatorDetailsAdapter.ViewHolder> {
    private static final String TAG = "TalentEvaluatorDetailsAdapter";
    private Context context;
    private List<TalentEvaluatorDetailsActivity.TalentEvaluatorDetails> mEvaluatorDetailsList;

    public TalentEvaluatorDetailsAdapter(Context context) {
        this.context = context;
        mEvaluatorDetailsList = new ArrayList<>();
    }
    
    public void setData(List<TalentEvaluatorDetailsActivity.TalentEvaluatorDetails> evaluatorDetailsList) {
        mEvaluatorDetailsList = evaluatorDetailsList;
    }

    @Override
    public TalentEvaluatorDetailsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.talent_evaluator_details_item, parent, false);
        TalentEvaluatorDetailsAdapter.ViewHolder holder = new TalentEvaluatorDetailsAdapter.ViewHolder(view);
        return holder;
    }
    
    @Override
    public void onBindViewHolder(@NonNull final TalentEvaluatorDetailsAdapter.ViewHolder holder, int position) {
        final TalentEvaluatorDetailsActivity.TalentEvaluatorDetails evaluatorDetails = mEvaluatorDetailsList.get(position);
        holder.name.setText(evaluatorDetails.nickname);
        holder.university.setText(evaluatorDetails.university);
        holder.content.setText(evaluatorDetails.content);
        holder.time.setText(evaluatorDetails.time);
    }

    @Override
    public int getItemCount() {
        return null != mEvaluatorDetailsList ? mEvaluatorDetailsList.size() : 0;
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView university;
        TextView content;
        TextView time;

        public ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.evaluator_name);
            university = view.findViewById(R.id.university);
            content = view.findViewById(R.id.content);
            time = view.findViewById(R.id.time);
        }
    }

}
