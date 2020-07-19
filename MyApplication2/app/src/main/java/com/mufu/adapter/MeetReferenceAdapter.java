package com.mufu.adapter;

import android.content.Context;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import com.mufu.R;
import com.mufu.common.OnItemClickListener;
import com.mufu.meet.MeetReferenceInfo;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.common.MyApplication;
import com.mufu.util.ParseUtils;
import com.mufu.util.RoundImageView;

import java.util.List;

import static com.mufu.common.MyApplication.getContext;
import static com.mufu.util.ParseUtils.startMeetArchiveActivity;
import static com.mufu.util.SharedPreferencesUtils.getSessionUid;

/**
 * Created by super-zou on 18-9-21.
 */

public class MeetReferenceAdapter extends RecyclerView.Adapter<MeetReferenceAdapter.ReferenceViewHolder> {

    private static final String TAG = "MeetReferenceAdapter";
    private static Context mContext;
    private List<MeetReferenceInfo> mReferenceList;

    public MeetReferenceAdapter(Context context) {
        mContext = context;
    }

    public void setReferenceList(List<MeetReferenceInfo> referenceList) {
        mReferenceList = referenceList;
    }

    @Override
    public ReferenceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reference_item, parent, false);
        ReferenceViewHolder holder = new ReferenceViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MeetReferenceAdapter.ReferenceViewHolder holder, int position) {
        final MeetReferenceInfo referenceInfo = mReferenceList.get(position);
        holder.name.setText(referenceInfo.getRefereeName());
        holder.relation.setText(referenceInfo.getRelation());
        holder.refereeProfile.setText(referenceInfo.getRefereeProfile());
        holder.referenceContent.setText("“"+referenceInfo.getReferenceContent()+"”");
        //holder.createdView.setText(referenceInfo.getCreated().toString());
        String avatar = referenceInfo.getAvatar();
        if (avatar != null && !"".equals(avatar)) {
            Glide.with(mContext).load(HttpUtil.DOMAIN + avatar).into(holder.refereeHeadUri);
        } else {
            if(referenceInfo.getSex() == 0){
                holder.refereeHeadUri.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.male_default_avator));
            }else {
                holder.refereeHeadUri.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.female_default_avator));
            }
        }

        if (referenceInfo.getUid() == getSessionUid(getContext())){
            holder.edit.setVisibility(View.VISIBLE);
            holder.edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mItemClickListener.onItemClick(position, view);
                }
            });
        }
        
        holder.refereeHeadUri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUtils.startMeetArchiveActivity(mContext, referenceInfo.getUid());
            }
        });

        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUtils.startMeetArchiveActivity(mContext, referenceInfo.getUid());
            }
        });

    }

    private OnItemClickListener mItemClickListener;

    public void setOnItemClickListener(OnItemClickListener l) {
        this.mItemClickListener = l;
    }

    @Override
    public int getItemCount() {
        return mReferenceList != null ? mReferenceList.size() : 0;
    }

    public class ReferenceViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView relation;
        TextView refereeProfile;
        RoundImageView refereeHeadUri;
        TextView thumbsUpIcon;
        TextView referenceContent;
        TextView createdView;
        TextView commentIcon;
        TextView edit;

        public ReferenceViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.referee_name);
            relation = view.findViewById(R.id.relation);
            refereeHeadUri = view.findViewById(R.id.referee_head_uri);
            refereeProfile = view.findViewById(R.id.referee_profile);
            thumbsUpIcon = view.findViewById(R.id.thumbs_up_icon);
            commentIcon = view.findViewById(R.id.comment_icon);
            edit = view.findViewById(R.id.edit);
            //createdView = view.findViewById(R.id.dynamic_time);
            referenceContent = view.findViewById(R.id.reference_content);
            //commentList = (LinearLayout) view.findViewById(R.id.dynamics_comments);

            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.reference_item), font);
        }
    }
}
