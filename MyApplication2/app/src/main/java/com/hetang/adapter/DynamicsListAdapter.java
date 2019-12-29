package com.hetang.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.dynamics.Dynamic;
import com.hetang.dynamics.DynamicsInteractDetailsActivity;
import com.hetang.common.HandlerTemp;
import com.hetang.common.MyApplication;
import com.hetang.group.SubGroupActivity;
import com.hetang.group.SubGroupDetailsActivity;
import com.hetang.home.HomeFragment;
import com.hetang.meet.UserMeetInfo;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.InterActInterface;
import com.hetang.util.ParseUtils;
import com.hetang.util.RoundImageView;
import com.hetang.util.Slog;
import com.hetang.util.Utility;
import com.nex3z.flowlayout.FlowLayout;
import com.willy.ratingbar.ScaleRatingBar;

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

import static com.hetang.util.ParseUtils.ADD_SUBGROUP_ACTIVITY_ACTION;
import static com.hetang.util.ParseUtils.FOLLOW_GROUP_ACTION;
import static com.hetang.util.ParseUtils.MODIFY_GROUP_ACTION;

/**
 * Created by haichao.zou on 2017/11/20.
 */

public class DynamicsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String PRAISED_DYNAMICS_URL = HttpUtil.DOMAIN + "?q=dynamic/interact/praise/add";
    public static final int UPDATE_PRAISED_COUNT = 1;
    private static final boolean isDebug = true;
    private static final String TAG = "DynamicsListAdapter";
    private static final int UPDATE_LOVE_COUNT = 0;
    private static final int UPDATE_COMMENT = 2;
    private static Context mContext;
    InterActInterface interActInterface;
    private List<Dynamic> dynamicList;
    private String picture_url;
    private boolean isScrolling = false;
    private boolean specificUser = false;

    private Handler mHandler = new MyHandler(this);

    public DynamicsListAdapter(Context context, boolean specificUser) {
        mContext = context;
        dynamicList = new ArrayList<>();
        Resources resources = mContext.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        this.specificUser = specificUser;
    }

    public void setScrolling(boolean isScrolling) {
        this.isScrolling = isScrolling;
    }

    public void setData(List<Dynamic> dynamicList) {
        this.dynamicList = dynamicList;
    }

    @Override
    public int getItemViewType(int position) {
        Dynamic dynamic = dynamicList.get(position);
        if (dynamic != null) {
            return dynamic.getType();
        } else {
            return 0;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (isDebug) Slog.d(TAG, "===========onCreateViewHolder============== viewType: "+viewType);
        RecyclerView.ViewHolder holder = null;

        switch (viewType) {
            case ParseUtils.ADD_MEET_DYNAMIC_ACTION:
            case ParseUtils.ADD_INNER_DYNAMIC_ACTION:
            case ADD_SUBGROUP_ACTIVITY_ACTION:
                View viewDynamic = LayoutInflater.from(MyApplication.getContext())
                        .inflate(R.layout.dynamics_item, parent, false);
                holder = new DynamicViewHolder(viewDynamic);
                break;
            case ParseUtils.PRAISE_DYNAMIC_ACTION:
                View viewCommon = LayoutInflater.from(MyApplication.getContext())
                        .inflate(R.layout.dynamics_action_item, parent, false);
                holder = new CommonViewHolder(viewCommon);
                break;
            case ParseUtils.PRAISE_MEET_CONDITION_ACTION:
            case ParseUtils.PUBLISH_MEET_CONDITION_ACTION:
            case ParseUtils.APPROVE_IMPRESSION_ACTION:
            case ParseUtils.APPROVE_PERSONALITY_ACTION:
            case ParseUtils.JOIN_CHEERING_GROUP_ACTION:
            case ParseUtils.ADD_CHEERING_GROUP_MEMBER_ACTION:
            case ParseUtils.REFEREE_ACTION:
                View viewMeet = LayoutInflater.from(MyApplication.getContext())
                        .inflate(R.layout.meet_action_item, parent, false);
                holder = new MeetViewHolder(viewMeet);
                break;
            case ParseUtils.EVALUATE_ACTION:
            case ParseUtils.ADD_PERSONALITY_ACTION:
            case ParseUtils.ADD_HOBBY_ACTION:
                View viewMeetEvaluate = LayoutInflater.from(MyApplication.getContext())
                        .inflate(R.layout.meet_evaluate_action_item, parent, false);
                holder = new MeetEvaluateViewHolder(viewMeetEvaluate);
                break;
            case ParseUtils.CREATE_GROUP_ACTION:
            case ParseUtils.JOIN_GROUP_ACTION:
            case FOLLOW_GROUP_ACTION:
            case MODIFY_GROUP_ACTION:
            //case ParseUtils.INVITE_SINGLE_GROUP_MEMBER_ACTION:
                View viewSubGroup = LayoutInflater.from(MyApplication.getContext())
                        .inflate(R.layout.group_action_item, parent, false);
                holder = new SubGroupViewHolder(viewSubGroup);

                break;
            case ParseUtils.SET_AVATAR_ACTION:
            case ParseUtils.ADD_INTRODUCTION_ACTION:
            case ParseUtils.ADD_EDUCATION_ACTION:
            case ParseUtils.ADD_WORK_ACTION:
            case ParseUtils.ADD_BLOG_ACTION:
            case ParseUtils.ADD_PAPER_ACTION:
            case ParseUtils.ADD_PRIZE_ACTION:
            case ParseUtils.ADD_VOLUNTEER_ACTION:
                View viewArchive = LayoutInflater.from(MyApplication.getContext())
                        .inflate(R.layout.archive_action_item, parent, false);
                holder = new ArchiveViewHolder(viewArchive);
                break;
            default:
                break;
        }

        return holder;

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof DynamicViewHolder) {
            onDynamicBindViewHolder((DynamicViewHolder) holder, position);
        } else if (holder instanceof CommonViewHolder) {
            onCommonActionBindViewHolder((CommonViewHolder) holder, position);
        } else if (holder instanceof MeetViewHolder) {
            onMeetActionBindViewHolder((MeetViewHolder) holder, position);
        } else if (holder instanceof MeetEvaluateViewHolder) {
            onMeetEvaluateActionBindViewHolder((MeetEvaluateViewHolder) holder, position);
        } else if (holder instanceof SubGroupViewHolder) {
            onSubGroupActionBindViewHolder((SubGroupViewHolder) holder, position);
        } else if (holder instanceof ArchiveViewHolder) {
            onArchiveBindViewHolder((ArchiveViewHolder) holder, position);
        }
    }

    public void onCommonActionBindViewHolder(final CommonViewHolder holder, final int position) {

        if (isDebug) Slog.d(TAG, "-------->onBindViewHolder position: " + position);

        final Dynamic dynamic = dynamicList.get(position);
        if (dynamic != null) {
            setAuthorProfile(holder.authorProfileVH, dynamic);
            setRelatedContentView(holder, dynamic, position);
            holder.relativeContentWrapper.setBackgroundColor(mContext.getResources().getColor(R.color.color_disabled));
            holder.dynamicViewHolder.meetDynamicsViewHolder.contentMeta.setVisibility(View.GONE);
            setDynamicItemInterAct(holder.dynamicsInterActVH, dynamic, position);
            onDynamicsItemElementClick(holder.baseProfile, dynamic);
        }

        holder.relativeContentWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyApplication.getContext(), DynamicsInteractDetailsActivity.class);
                intent.putExtra("type", DynamicsInteractDetailsActivity.DYNAMIC_COMMENT);
                intent.putExtra("dynamic", dynamic.relatedContent);
                mContext.startActivity(intent);
            }
        });
    }

    private void setDynamicItemInterAct(DynamicsInterActVH holder, final Dynamic dynamic, final int position) {
        if (dynamic.getPraisedDynamics() == 1) {
            holder.dynamicsPraise.setText(mContext.getResources().getString(R.string.fa_thumbs_up));
        } else {
            holder.dynamicsPraise.setText(mContext.getResources().getString(R.string.fa_thumbs_O_up));
        }

        if (dynamic.getPraisedDynamicsCount() > 0) {
            holder.dynamicsPraiseCount.setText(String.valueOf(dynamic.getPraisedDynamicsCount()));
        } else {
            holder.dynamicsPraiseCount.setText("");
        }
        if (dynamic.getCommentCount() > 0) {
            holder.dynamicsComment.setText(mContext.getResources().getString(R.string.fa_comment_o) + " " + dynamic.getCommentCount());
        } else {
            holder.dynamicsComment.setText("");
            holder.dynamicsComment.setText(mContext.getResources().getString(R.string.fa_comment_o));
        }

        holder.dynamicsPraise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO change UI to show parised or no
                if (1 == dynamic.getPraisedDynamics()) {
                    Toast.makeText(mContext, "You have praised it!", Toast.LENGTH_SHORT).show();
                    return;
                }
                praiseDynamics(dynamic);
            }
        });

        holder.dynamicsPraiseCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                interActInterface.onPraiseClick(view, position);
            }
        });

        //when comment icon touched should show comment input dialog fragment
        holder.dynamicsComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show the comment input dialog fragment
                interActInterface.onCommentClick(v, position);
            }
        });
    }

    private void setAuthorProfile(AuthorProfileVH authorProfileVH, Dynamic dynamic) {
        authorProfileVH.authorName.setText(dynamic.getName());
        authorProfileVH.action.setText(dynamic.getAction());
        TextPaint paint = authorProfileVH.action.getPaint();
        paint.setFakeBoldText(true);
        if (dynamic.getAvatar() != null && !"".equals(dynamic.getAvatar())) {
            picture_url = HttpUtil.DOMAIN + dynamic.getAvatar();
            Glide.with(MyApplication.getContext()).load(picture_url).into(authorProfileVH.authorAvatar);
        } else {
            if (dynamic.getSex() == 0) {
                authorProfileVH.authorAvatar.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.male_default_avator));
            } else {
                authorProfileVH.authorAvatar.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.female_default_avator));
            }
        }

        String profile = "";
        if (dynamic.getSituation() != -1) {
            if (dynamic.getSituation() == 0) {
                profile = dynamic.getMajor() + "·" + dynamic.getDegreeName(dynamic.getDegree()) + "·" + dynamic.getUniversity();
            } else {
                profile = dynamic.getPosition() + "·" + dynamic.getIndustry();
            }
            authorProfileVH.authorProfile.setText(profile);
        }
    }

    private void setRelatedContentView(CommonViewHolder holder, Dynamic dynamic, int position) {
        if (dynamic != null && dynamic.relatedContent != null) {
            MeetDynamicsListAdapter.setDynamicContent(holder.dynamicViewHolder.meetDynamicsViewHolder, dynamic.relatedContent, position);
        }
    }

    public void onDynamicBindViewHolder(final DynamicViewHolder holder, final int position) {
        //if (isDebug) Slog.d(TAG, "-------->onBindViewHolder position: "+position+" dynamicsGrid: "+holder.dynamicsGrid.hashCode());
        final Dynamic dynamic = dynamicList.get(position);
        MeetDynamicsListAdapter.setDynamicContent(holder.meetDynamicsViewHolder, dynamic, position);

        holder.meetDynamicsViewHolder.dynamicsPraise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO change UI to show parised or no
                if (1 == dynamic.getPraisedDynamics()) {
                    Toast.makeText(mContext, "You have praised it!", Toast.LENGTH_SHORT).show();
                    return;
                }
                praiseDynamics(dynamic);
            }
        });

        holder.meetDynamicsViewHolder.dynamicsPraiseCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                interActInterface.onPraiseClick(view, position);
            }
        });

        //when comment icon touched should show comment input dialog fragment
        holder.meetDynamicsViewHolder.dynamicsComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show the comment input dialog fragment
                interActInterface.onCommentClick(v, position);
            }
        });

        holder.meetDynamicsViewHolder.baseProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUtils.startMeetArchiveActivity(mContext, dynamic.getUid());
            }
        });

    }

    private void onDynamicsItemElementClick(ConstraintLayout baseProfile, final Dynamic dynamic) {
        baseProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUtils.startMeetArchiveActivity(mContext, dynamic.getUid());
            }
        });
    }

    public void onMeetActionBindViewHolder(final MeetViewHolder holder, final int position) {

        final Dynamic dynamic = dynamicList.get(position);
        setAuthorProfile(holder.authorProfileVH, dynamic);
        String content = dynamic.getContent();
        if (content != null && !TextUtils.isEmpty(content) && !"null".equals(content)) {
            holder.authorContent.setVisibility(View.VISIBLE);
            if (dynamic.getType() == ParseUtils.APPROVE_IMPRESSION_ACTION || dynamic.getType() == ParseUtils.APPROVE_PERSONALITY_ACTION) {
                holder.authorContent.setTextColor(mContext.getResources().getColor(R.color.color_blue));
                holder.authorContent.setTextSize(18);

            } else {
                holder.authorContent.setTextColor(mContext.getResources().getColor(R.color.black));
            }
            holder.authorContent.setText(content);
        } else {
            holder.authorContent.setVisibility(View.GONE);
        }

        UserProfileViewHolder userProfileViewHolder = holder.userProfileViewHolder;
        setRelatedUserProfileView(userProfileViewHolder, dynamic);

        holder.relatedUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUtils.startMeetArchiveActivity(mContext, dynamic.relatedUerProfile.getUid());
            }
        });

        /*
        if (dynamic.getType() == ParseUtils.ADD_CHEERING_GROUP_MEMBER_ACTION) {
            UserProfileViewHolder userProfileViewHolder = holder.userProfileViewHolder;
            setRelatedUserProfileView(userProfileViewHolder, dynamic);

            holder.relatedUserProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ParseUtils.startMeetArchiveActivity(mContext, dynamic.relatedUerProfile.getUid());
                }
            });

        } else {
            holder.relativeContentWrapper.setVisibility(View.VISIBLE);
            holder.relativeContentWrapper.setBackgroundColor(mContext.getResources().getColor(R.color.color_disabled));
            setRelatedMeetContentView(holder.recommendViewHolder, dynamic);
        }*/

        onDynamicsItemElementClick(holder.baseProfile, dynamic);
        holder.relativeContentWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUtils.startMeetArchiveActivity(mContext, dynamic.getRelatedId());
            }
        });
    }

    public void onMeetEvaluateActionBindViewHolder(final MeetEvaluateViewHolder holder, final int position) {

        final Dynamic dynamic = dynamicList.get(position);
        setAuthorProfile(holder.authorProfileVH, dynamic);
        holder.relativeContentWrapper.setBackgroundColor(mContext.getResources().getColor(R.color.color_disabled));
        String content = dynamic.getContent();

        if (dynamic.getType() == ParseUtils.EVALUATE_ACTION) {
            if (content != null && !TextUtils.isEmpty(content)) {
                holder.scaleRatingBar.setVisibility(View.VISIBLE);
                String[] contentArr = content.split(":");
                float rating = Float.parseFloat(contentArr[0]);
                holder.ratingScore.setText(contentArr[0]);
                holder.scaleRatingBar.setRating(rating);

                if (contentArr.length > 1 && contentArr[1] != null && contentArr[1].length() > 0) {
                    String[] impressionArray = contentArr[1].split("#");
                    if (impressionArray.length > 0) {
                        FlowLayout impressionFlow = holder.impressionFL;
                        impressionFlow.removeAllViews();
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        layoutParams.setMargins((int) Utility.dpToPx(mContext, 2), (int) Utility.dpToPx(mContext, 2),
                                (int) Utility.dpToPx(mContext, 2), (int) Utility.dpToPx(mContext, 2));

                        for (int i = 0; i < impressionArray.length; i++) {
                            if (!impressionArray[i].isEmpty()) {
                                final TextView impressionView = new TextView(mContext);
                                impressionView.setPadding((int) Utility.dpToPx(mContext, 6), (int) Utility.dpToPx(mContext, 6),
                                        (int) Utility.dpToPx(mContext, 6), (int) Utility.dpToPx(mContext, 6));
                                //hobbyView.setBackground(getDrawable(R.drawable.label_btn_shape));
                                impressionView.setTextColor(mContext.getResources().getColor(R.color.color_blue));
                                impressionView.setLayoutParams(layoutParams);
                                impressionView.setText(impressionArray[i]);
                                impressionView.setTextSize(15);
                                impressionFlow.addView(impressionView);
                            }
                        }
                    }
                }
            }
        } else {
            if (content != null && content.length() > 0) {
                String[] personalityArray = content.split("#");
                if (personalityArray.length > 0) {
                    FlowLayout personalityFlow = holder.impressionFL;
                    personalityFlow.removeAllViews();
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins((int) Utility.dpToPx(mContext, 2), (int) Utility.dpToPx(mContext, 2),
                            (int) Utility.dpToPx(mContext, 2), (int) Utility.dpToPx(mContext, 2));
                    for (int i = 0; i < personalityArray.length; i++) {
                        if (!personalityArray[i].isEmpty()) {
                            final TextView personalityView = new TextView(mContext);
                            personalityView.setPadding((int) Utility.dpToPx(mContext, 6), (int) Utility.dpToPx(mContext, 6),
                                    (int) Utility.dpToPx(mContext, 6), (int) Utility.dpToPx(mContext, 6));
                            //hobbyView.setBackground(getDrawable(R.drawable.label_btn_shape));
                            personalityView.setTextColor(mContext.getResources().getColor(R.color.color_blue));
                            personalityView.setLayoutParams(layoutParams);
                            personalityView.setText(personalityArray[i]);
                            personalityView.setTextSize(15);
                            personalityFlow.addView(personalityView);
                        }
                    }
                }
            }
        }

        setRelatedMeetContentView(holder.recommendViewHolder, dynamic);
        onDynamicsItemElementClick(holder.baseProfile, dynamic);

        holder.relativeContentWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUtils.startMeetArchiveActivity(mContext, dynamic.relatedMeetContent.getUid());
            }
        });

    }

    public void onSubGroupActionBindViewHolder(final SubGroupViewHolder holder, final int position) {
        if (isDebug)
            Slog.d(TAG, "-------->onSubGroupActionBindViewHolder position: " + position);
        final Dynamic dynamic = dynamicList.get(position);
        setAuthorProfile(holder.authorProfileVH, dynamic);

        /*
        if (dynamic.getType() == ParseUtils.INVITE_SINGLE_GROUP_MEMBER_ACTION) {
            UserProfileViewHolder userProfileViewHolder = holder.userProfileViewHolder;
            setRelatedUserProfileView(userProfileViewHolder, dynamic);

            holder.relatedUserProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ParseUtils.startMeetArchiveActivity(mContext, dynamic.relatedUerProfile.getUid());
                }
            });

        }
        */

        setRelatedSingleGroupContentView(holder.subGroupViewHolder, dynamic);
        onDynamicsItemElementClick(holder.baseProfile, dynamic);

        holder.relativeContentWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, SubGroupDetailsActivity.class);
                intent.putExtra("gid", dynamic.relatedSubGroupContent.gid);
                mContext.startActivity(intent);
            }
        });


    }

    private void setRelatedUserProfileView(UserProfileViewHolder userProfileViewHolder, Dynamic dynamic) {
        userProfileViewHolder.relatedUserProfile.setVisibility(View.VISIBLE);

        if (dynamic == null || dynamic.relatedUerProfile == null) {
            return;
        }
        String avatar = dynamic.relatedUerProfile.getAvatar();

        if (avatar != null && !"".equals(avatar)) {
            picture_url = HttpUtil.DOMAIN + avatar;
            Glide.with(mContext).load(picture_url).into(userProfileViewHolder.relatedUserAvatar);
        } else {
            if (dynamic.relatedUerProfile.getSex() == 0) {
                userProfileViewHolder.relatedUserAvatar.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.male_default_avator));
            } else {
                userProfileViewHolder.relatedUserAvatar.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.female_default_avator));
            }
        }
        userProfileViewHolder.relatedUserName.setText(dynamic.relatedUerProfile.getName());

        if (dynamic.relatedUerProfile.getSituation() == 0) {
            userProfileViewHolder.relatedUserLevel.setText(dynamic.relatedUerProfile.getDegreeName(dynamic.relatedUerProfile.getDegree()));
            if (dynamic.relatedUerProfile.getMajor() != null && !TextUtils.isEmpty(dynamic.relatedUerProfile.getMajor())) {
                userProfileViewHolder.relatedUserMajor.setVisibility(View.VISIBLE);
                userProfileViewHolder.relatedUserMajor.setText(dynamic.relatedUerProfile.getMajor());
            }
            userProfileViewHolder.relatedUserOrg.setText(dynamic.relatedUerProfile.getUniversity());
        } else {
            userProfileViewHolder.relatedUserLevel.setText(dynamic.relatedUerProfile.getPosition());
            userProfileViewHolder.relatedUserOrg.setText(dynamic.relatedUerProfile.getIndustry());
        }
    }

    private void setRelatedSingleGroupContentView(SubGroupSummaryAdapter.ViewHolder holder, Dynamic dynamic) {
        //MeetSingleGroupFragment.SingleGroup relatedContent = dynamic.relatedSingleGroupContent;
        SubGroupActivity.SubGroup relatedContent = dynamic.relatedSubGroupContent;
        //MeetSingleGroupSummaryAdapter.setContentView(holder, relatedContent);
        SubGroupSummaryAdapter.setContentView(holder, relatedContent);
    }

    private void setRelatedMeetContentView(MeetRecommendListAdapter.ViewHolder holder, Dynamic dynamic) {
        UserMeetInfo relatedMeet = dynamic.relatedMeetContent;
        MeetRecommendListAdapter.setMeetRecommendContent(holder, relatedMeet);
    }

    public void onArchiveBindViewHolder(final ArchiveViewHolder holder, final int position) {

        final Dynamic dynamic = dynamicList.get(position);

        setAuthorProfile(holder.authorProfileVH, dynamic);

        if (holder.contentView.getVisibility() == View.VISIBLE) {
            holder.contentView.setVisibility(View.GONE);
        }

        if (holder.actionAvatar.getVisibility() == View.VISIBLE) {
            holder.actionAvatar.setVisibility(View.GONE);
        }

        if (holder.relatedContentLayout.getVisibility() == View.VISIBLE) {
            holder.relatedContentLayout.setVisibility(View.GONE);
        }

        switch (dynamic.getType()) {
            case ParseUtils.ADD_INTRODUCTION_ACTION:
                holder.contentView.setVisibility(View.VISIBLE);
                if (dynamic.getContent() != null && !TextUtils.isEmpty(dynamic.getContent())) {
                    holder.contentView.setText(dynamic.getContent());
                }
                break;
            case ParseUtils.SET_AVATAR_ACTION:
                holder.actionAvatar.setVisibility(View.VISIBLE);
                if (mContext != null) {
                    Glide.with(mContext).load(HttpUtil.DOMAIN + dynamic.getContent()).into(holder.actionAvatar);
                }

                break;

            case ParseUtils.ADD_EDUCATION_ACTION:
            case ParseUtils.ADD_WORK_ACTION:
            case ParseUtils.ADD_BLOG_ACTION:
            case ParseUtils.ADD_PAPER_ACTION:
            case ParseUtils.ADD_PRIZE_ACTION:
            case ParseUtils.ADD_VOLUNTEER_ACTION:
                setRelatedBackgroudView(holder, dynamic);
                break;
            default:
                break;
        }

        onDynamicsItemElementClick(holder.baseProfile, dynamic);

    }

    private void setRelatedBackgroudView(final ArchiveViewHolder holder, Dynamic dynamic) {
        holder.relatedContentLayout.setVisibility(View.VISIBLE);
        HomeFragment.BackgroundDetail backgroundDetail = dynamic.backgroundDetail;
        holder.title.setText(backgroundDetail.title);
        if (!TextUtils.isEmpty(backgroundDetail.link)) {
            holder.link.setVisibility(View.VISIBLE);
            holder.link.setTag(backgroundDetail.link);
            holder.title.setTextColor(mContext.getResources().getColor(R.color.color_blue));

            holder.titleWrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri uri;
                    String uriString = holder.link.getTag().toString();
                    if (!uriString.startsWith("http") && !uriString.startsWith("https")) {
                        uriString = "http://" + uriString;
                    }
                    uri = Uri.parse(uriString);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    mContext.startActivity(intent);
                }
            });
        }

        if (!TextUtils.isEmpty(backgroundDetail.secondaryTitle)) {
            holder.secondaryTitle.setText(backgroundDetail.secondaryTitle);
        }
        if (!TextUtils.isEmpty(backgroundDetail.lastTitle)) {
            holder.lastTitle.setText(", " + backgroundDetail.lastTitle);
        }
        if (!TextUtils.isEmpty(backgroundDetail.startTime)) {
            holder.startTime.setText(backgroundDetail.startTime);
        }

        if (backgroundDetail.now != 0 && !TextUtils.isEmpty(backgroundDetail.endTime)) {
            holder.endTime.setText("~  " + "至今");
        } else {
            if (!TextUtils.isEmpty(backgroundDetail.endTime)) {
                holder.endTime.setText("~  " + backgroundDetail.endTime);
            }
        }
        if (!TextUtils.isEmpty(backgroundDetail.description)) {
            holder.description.setText(backgroundDetail.description);
        }
    }

    @Override
    public int getItemCount() {
        return dynamicList != null ? dynamicList.size() : 0;
    }

    private void praiseDynamics(final Dynamic dynamic) {
        RequestBody requestBody = new FormBody.Builder().add("did", String.valueOf(dynamic.getDid())).build();
        HttpUtil.sendOkHttpRequest(mContext, PRAISED_DYNAMICS_URL, requestBody, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isDebug) Log.d(TAG, "praiseDynamics responseText:" + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject commentResponse = new JSONObject(responseText);
                        int status = commentResponse.optInt("status");
                        if (isDebug) Log.d(TAG, "praiseDynamics status:" + status);
                        if (1 == status) {
                            Dynamic tempInfo = getMeetDynamicsById(dynamic.getDid());
                            tempInfo.setPraisedDynamics(1);
                            tempInfo.setPraisedDynamicsCount(dynamic.getPraisedDynamicsCount() + 1);
                            sendMessage(UPDATE_PRAISED_COUNT);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }

    private Dynamic getMeetDynamicsById(long did) {
        if (null == dynamicList) {
            return null;
        }
        for (int i = 0; i < dynamicList.size(); i++) {
            if (did == dynamicList.get(i).getDid()) {
                return dynamicList.get(i);
            }
        }
        return null;
    }

    //register the interActInterface callback, add by zouhaichao 2018/9/16
    public void setOnCommentClickListener(InterActInterface commentDialogFragmentListener) {
        this.interActInterface = commentDialogFragmentListener;
    }

    private void sendMessage(int what, Object obj) {
        Message msg = mHandler.obtainMessage();
        msg.what = what;
        msg.obj = obj;
        msg.sendToTarget();
    }

    private void sendMessage(int what) {
        sendMessage(what, null);
    }

    private void handleMessage(Message msg) {
        switch (msg.what) {
            case UPDATE_LOVE_COUNT:
                notifyDataSetChanged();
                break;
            case UPDATE_PRAISED_COUNT:
                notifyDataSetChanged();
                break;
            case UPDATE_COMMENT:
                //notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    public static class AuthorProfileVH extends RecyclerView.ViewHolder {
        TextView authorName;
        TextView authorProfile;
        TextView action;
        RoundImageView authorAvatar;

        public AuthorProfileVH(View view) {

            super(view);
            action = view.findViewById(R.id.action);
            authorName = view.findViewById(R.id.author_name);
            authorAvatar = view.findViewById(R.id.author_avatar);
            authorProfile = view.findViewById(R.id.author_profile);
        }
    }

    public static class DynamicsInterActVH extends RecyclerView.ViewHolder {
        TextView dynamicsPraiseCount;
        TextView dynamicsPraise;
        TextView dynamicsComment;

        public DynamicsInterActVH(View view) {
            super(view);
            dynamicsPraiseCount = view.findViewById(R.id.dynamic_praise_count);
            dynamicsPraise = view.findViewById(R.id.dynamic_praise);
            dynamicsComment = view.findViewById(R.id.dynamic_comment);
        }
    }

    public static class DynamicViewHolder extends RecyclerView.ViewHolder {

        MeetDynamicsListAdapter.MeetDynamicsViewHolder meetDynamicsViewHolder;

        public DynamicViewHolder(View view) {
            super(view);
            meetDynamicsViewHolder = new MeetDynamicsListAdapter.MeetDynamicsViewHolder(view);
            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.dynamics_content_meta), font);
            FontManager.markAsIconContainer(view.findViewById(R.id.base_profile), font);
        }
    }

    public static class ArchiveViewHolder extends RecyclerView.ViewHolder {
        public ConstraintLayout baseProfile;
        public TextView contentView;
        public TextView createdView;
        public RoundImageView actionAvatar;
        AuthorProfileVH authorProfileVH;
        //for archive base background
        ConstraintLayout relatedContentLayout;
        LinearLayout titleWrapper;
        TextView title;
        TextView link;
        TextView secondaryTitle;
        TextView lastTitle;
        TextView startTime;
        TextView endTime;
        TextView description;

        public ArchiveViewHolder(View view) {
            super(view);
            baseProfile = view.findViewById(R.id.base_profile);
            authorProfileVH = new AuthorProfileVH(view);
            contentView = view.findViewById(R.id.content);
            actionAvatar = view.findViewById(R.id.action_avatar);
            //for archive base background
            relatedContentLayout = view.findViewById(R.id.relate_content);
            titleWrapper = view.findViewById(R.id.title_wrapper);
            title = view.findViewById(R.id.title);
            secondaryTitle = view.findViewById(R.id.secondary_title);
            lastTitle = view.findViewById(R.id.last_title);
            startTime = view.findViewById(R.id.start_time);

            endTime = view.findViewById(R.id.end_time);
            link = view.findViewById(R.id.link);
            description = view.findViewById(R.id.description);
            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.background_template), font);
            FontManager.markAsIconContainer(view.findViewById(R.id.base_profile), font);
        }
    }

    public static class CommonViewHolder extends RecyclerView.ViewHolder {

        AuthorProfileVH authorProfileVH;
        ConstraintLayout baseProfile;
        TextView authorContent;
        TextView createdView;
        DynamicsInterActVH dynamicsInterActVH;
        ConstraintLayout relativeContentWrapper;
        DynamicViewHolder dynamicViewHolder;

        public CommonViewHolder(View view) {
            super(view);
            baseProfile = view.findViewById(R.id.base_profile);
            authorProfileVH = new AuthorProfileVH(view);
            authorContent = view.findViewById(R.id.author_content);
            //createdView = (TextView) view.findViewById(R.id.dynamic_time);
            relativeContentWrapper = view.findViewById(R.id.relate_content);
            dynamicsInterActVH = new DynamicsInterActVH(view);
            //for related content
            dynamicViewHolder = new DynamicViewHolder(view);
        }
    }

    public static class MeetViewHolder extends RecyclerView.ViewHolder {

        AuthorProfileVH authorProfileVH;
        ConstraintLayout baseProfile;
        TextView authorContent;
        TextView createdView;
        DynamicsInterActVH dynamicsInterActVH;
        ConstraintLayout relativeContentWrapper;
        ConstraintLayout relatedUserProfile;
        UserProfileViewHolder userProfileViewHolder;
        MeetRecommendListAdapter.ViewHolder recommendViewHolder;

        public MeetViewHolder(View view) {
            super(view);
            baseProfile = view.findViewById(R.id.base_profile);
            authorProfileVH = new AuthorProfileVH(view);
            authorContent = view.findViewById(R.id.author_content);
            //createdView = (TextView) view.findViewById(R.id.dynamic_time);
            relativeContentWrapper = view.findViewById(R.id.relate_content);
            dynamicsInterActVH = new DynamicsInterActVH(view);
            //for add cheering group action
            relatedUserProfile = view.findViewById(R.id.related_user_profile);
            userProfileViewHolder = new UserProfileViewHolder(view);
            //for related content
            recommendViewHolder = new MeetRecommendListAdapter.ViewHolder(view, null);
            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.meet_action_item), font);
            FontManager.markAsIconContainer(view.findViewById(R.id.base_profile), font);
        }
    }

    public static class MeetEvaluateViewHolder extends RecyclerView.ViewHolder {

        AuthorProfileVH authorProfileVH;
        ConstraintLayout baseProfile;
        ScaleRatingBar scaleRatingBar;
        TextView ratingScore;
        FlowLayout impressionFL;
        TextView createdView;
        DynamicsInterActVH dynamicsInterActVH;
        ConstraintLayout relativeContentWrapper;
        MeetRecommendListAdapter.ViewHolder recommendViewHolder;

        public MeetEvaluateViewHolder(View view) {
            super(view);
            baseProfile = view.findViewById(R.id.base_profile);
            authorProfileVH = new AuthorProfileVH(view);
            scaleRatingBar = view.findViewById(R.id.charm_rating_bar);
            ratingScore = view.findViewById(R.id.rating_score);
            impressionFL = view.findViewById(R.id.impression_flow);
            //createdView = (TextView) view.findViewById(R.id.dynamic_time);
            relativeContentWrapper = view.findViewById(R.id.relate_content);
            dynamicsInterActVH = new DynamicsInterActVH(view);
            //for related content
            recommendViewHolder = new MeetRecommendListAdapter.ViewHolder(view, null);
            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.meet_evaluate_action_item), font);
            FontManager.markAsIconContainer(view.findViewById(R.id.base_profile), font);
        }
    }

    public static class SingleGroupViewHolder extends RecyclerView.ViewHolder {

        AuthorProfileVH authorProfileVH;
        ConstraintLayout baseProfile;
        ConstraintLayout relatedUserProfile;
        UserProfileViewHolder userProfileViewHolder;
        //TextView authorContent;
        TextView createdView;
        DynamicsInterActVH dynamicsInterActVH;
        ConstraintLayout relativeContentWrapper;
        MeetSingleGroupSummaryAdapter.ViewHolder singleGroupViewHolder;

        public SingleGroupViewHolder(View view) {
            super(view);
            baseProfile = view.findViewById(R.id.base_profile);
            authorProfileVH = new AuthorProfileVH(view);
            //createdView = (TextView) view.findViewById(R.id.dynamic_time);
            relativeContentWrapper = view.findViewById(R.id.relate_content);
            dynamicsInterActVH = new DynamicsInterActVH(view);
            relatedUserProfile = view.findViewById(R.id.related_user_profile);
            userProfileViewHolder = new UserProfileViewHolder(view);
            //for related content
            singleGroupViewHolder = new MeetSingleGroupSummaryAdapter.ViewHolder(view, null);
            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.single_group_action_item), font);
            FontManager.markAsIconContainer(view.findViewById(R.id.base_profile), font);
        }
    }

    public static class SubGroupViewHolder extends RecyclerView.ViewHolder {

        AuthorProfileVH authorProfileVH;
        ConstraintLayout baseProfile;
        ConstraintLayout relatedUserProfile;
        UserProfileViewHolder userProfileViewHolder;
        //TextView authorContent;
        TextView createdView;
        DynamicsInterActVH dynamicsInterActVH;
        ConstraintLayout relativeContentWrapper;
        SubGroupSummaryAdapter.ViewHolder subGroupViewHolder;

        public SubGroupViewHolder(View view) {
            super(view);
            baseProfile = view.findViewById(R.id.base_profile);
            authorProfileVH = new AuthorProfileVH(view);
            //createdView = (TextView) view.findViewById(R.id.dynamic_time);
            relativeContentWrapper = view.findViewById(R.id.relate_content);
            dynamicsInterActVH = new DynamicsInterActVH(view);
            relatedUserProfile = view.findViewById(R.id.related_user_profile);
            userProfileViewHolder = new UserProfileViewHolder(view);
            //for related content
            subGroupViewHolder = new SubGroupSummaryAdapter.ViewHolder(view, null);
            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
            //FontManager.markAsIconContainer(view.findViewById(R.id.subgroup_action_item), font);
            FontManager.markAsIconContainer(view.findViewById(R.id.base_profile), font);
        }
    }

    public static class UserProfileViewHolder extends RecyclerView.ViewHolder {
        //begin for add cheering group member
        public ConstraintLayout relatedUserProfile;
        public RoundImageView relatedUserAvatar;
        public TextView relatedUserName;
        public TextView relatedUserLevel;
        public TextView relatedUserMajor;
        public TextView relatedUserOrg;

        //end for add cheering group member
        public UserProfileViewHolder(View view) {
            super(view);
            relatedUserProfile = view.findViewById(R.id.related_user_profile);
            relatedUserAvatar = view.findViewById(R.id.related_user_avatar);
            relatedUserName = view.findViewById(R.id.related_user_name);
            relatedUserLevel = view.findViewById(R.id.level);
            relatedUserMajor = view.findViewById(R.id.major);
            relatedUserOrg = view.findViewById(R.id.orgnization);
        }
    }

    static class MyHandler extends HandlerTemp<DynamicsListAdapter> {
        public MyHandler(DynamicsListAdapter cls) {
            super(cls);
        }

        @Override
        public void handleMessage(Message message) {
            DynamicsListAdapter dynamicsListAdapter = ref.get();
            if (dynamicsListAdapter != null) {
                dynamicsListAdapter.handleMessage(message);
            }
        }
    }
}
                    
                    
        
            
