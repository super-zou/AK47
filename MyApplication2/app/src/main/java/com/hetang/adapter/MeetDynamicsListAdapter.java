package com.hetang.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.hetang.R;
import com.hetang.archive.ArchiveActivity;
import com.hetang.common.Dynamic;
import com.hetang.common.HandlerTemp;
import com.hetang.common.MyApplication;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.InterActInterface;
import com.hetang.util.RoundImageView;
import com.hetang.util.Slog;
import com.hetang.util.Utility;

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

/**
 * Created by haichao.zou on 2017/11/20.
 */

public class MeetDynamicsListAdapter extends RecyclerView.Adapter<MeetDynamicsListAdapter.MeetDynamicsViewHolder> {

    private static final boolean isDebug = false;
    private static final String TAG = "MeetDynamicsListAdapter";

    //+ added by xuchunping
    public static final String PRAISED_DYNAMICS_URL = HttpUtil.DOMAIN + "?q=dynamic/interact/praise/add";

    private static final int UPDATE_LOVE_COUNT = 0;
    public static final int UPDATE_PRAISED_COUNT = 1;
    private static final int UPDATE_COMMENT = 2;

    private FragmentManager fragmentManager;

    private static final int TYPE_COMMENT = 0;
    private static final int TYPE_REPLY = 1;


    private static Context mContext;

    static InterActInterface interActInterface;
    private List<Dynamic> mMeetList;

    private List<ImageView> imageViewList = new ArrayList<>();
    private boolean isScrolling = false;
    private MeetDynamicsViewHolder mMyViewHolder;
    private DisplayMetrics outMetrics;
    private static int width = 0;
    private static int height = 0;
    private static int innerWidth = 0;
    private boolean specificUser = false;
    private Handler mHandler = new MyHandler(this);

    public MeetDynamicsListAdapter(Context context, FragmentManager fragmentManager, boolean specificUser) {
        //Slog.d(TAG, "==============MeetRecommendListAdapter init=================");
        mContext = context;
        if (mContext == null) {
            mContext = MyApplication.getContext();
        }
        mMeetList = new ArrayList<Dynamic>();
        Resources resources = mContext.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        innerWidth = dm.widthPixels - (int) Utility.dpToPx(mContext, 32f);
        this.specificUser = specificUser;
        this.fragmentManager = fragmentManager;

    }

    public void setScrolling(boolean isScrolling) {
        this.isScrolling = isScrolling;
    }

    public void setData(List<Dynamic> meetList) {
        mMeetList = meetList;
    }

    @Override
    public int getItemViewType(int position) {
        //Slog.d(TAG, "----------------->position: "+position+" type: "+mMeetList.get(position).getType()+" action: "+mMeetList.get(position).getAction());
        return mMeetList.get(position).getType();
    }

    @Override
    public MeetDynamicsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (isDebug) Slog.d(TAG, "===========onCreateViewHolder==============");
        View viewDynamic = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.meet_dynamics_item, parent, false);
        MeetDynamicsViewHolder holder = new MeetDynamicsViewHolder(viewDynamic);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MeetDynamicsViewHolder holder, final int position) {
        if (isDebug)
            Slog.d(TAG, "-------->onBindViewHolder position: " + position + " dynamicsGrid: " + holder.dynamicsGrid.hashCode());

        final Dynamic dynamic = mMeetList.get(position);

        if (specificUser) {
            holder.baseProfile.setVisibility(View.GONE);
        }

        if (null != dynamic) {
            setDynamicContent(holder, dynamic, position);
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

        holder.baseProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ArchiveActivity.class);
                intent.putExtra("uid", dynamic.getUid());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                mContext.startActivity(intent);
            }
        });

        if (dynamic.getAuthorSelf() == true) {
            holder.operation.setVisibility(View.VISIBLE);
            holder.operation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    interActInterface.onOperationClick(view, position);
                }
            });
        } else {
            holder.operation.setVisibility(View.GONE);
        }
    }

    public static void setDynamicContent(MeetDynamicsViewHolder holder, Dynamic dynamic, int position) {
        if (dynamic == null) {
            return;
        }
        holder.name.setText(dynamic.getName());
        holder.living.setText(dynamic.getLiving());

        if (dynamic.getAvatar() != null && !"".equals(dynamic.getAvatar())) {
            String picture_url = HttpUtil.DOMAIN + dynamic.getAvatar();
            Glide.with(mContext).load(picture_url).into(holder.avatar);
        } else {
            if (dynamic.getSex() == 0) {
                holder.avatar.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.male_default_avator));
            } else {
                holder.avatar.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.female_default_avator));
            }
        }

        String profile = "";
        if (dynamic.getSituation() != -1) {
            if (dynamic.getSituation() == 0) {
                profile = dynamic.getMajor() + "·" + dynamic.getDegreeName(dynamic.getDegree()) + "·" + dynamic.getUniversity();
            } else {
                profile = dynamic.getPosition() + "·" + dynamic.getIndustry();
            }
            holder.profile.setText(profile);
        }

        holder.contentView.setText(dynamic.getContent());

        String pictures = dynamic.getDynamicPicture();

        if (!"".equals(pictures)) {
            if (holder.dynamicsGrid.getTag() == null) {
                setContentView(holder, pictures, dynamic, position);
            } else {
                if (!dynamic.equals(holder.dynamicsGrid.getTag())) {
                    holder.dynamicsGrid.removeAllViews();
                    setContentView(holder, pictures, dynamic, position);
                }
            }
        } else {
            if (holder.dynamicsGrid.getChildCount() > 0) {
                holder.dynamicsGrid.removeAllViews();
            }
        }

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
            holder.dynamicsComment.setText(mContext.getResources().getString(R.string.fa_comment_o) + " " + String.valueOf(dynamic.getCommentCount()));
        } else {
            holder.dynamicsComment.setText("");
            holder.dynamicsComment.setText(mContext.getResources().getString(R.string.fa_comment_o));
        }
    }

    private static void setContentView(final MeetDynamicsViewHolder holder, String pictures, Dynamic dynamic, final int position) {
        final String[] picture_array = pictures.split(":");
        final int length = picture_array.length;
        if (length > 0) {
            if (length != 4) {
                if (length < 4) {
                    holder.dynamicsGrid.setColumnCount(length);
                    if (length == 1) {
                        width = innerWidth / 2;
                        height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    } else {
                        width = (innerWidth - (int) Utility.dpToPx(mContext, 2f) * (length - 1)) / length;
                        height = width;
                    }

                } else {
                    holder.dynamicsGrid.setColumnCount(3);
                    width = (innerWidth - (int) Utility.dpToPx(mContext, 2f) * 2) / 3;
                    height = width;
                }
            } else {
                holder.dynamicsGrid.setColumnCount(2);
                width = (innerWidth) / 2;
                height = width;
            }

            final RequestOptions requestOptions = new RequestOptions()
                    .placeholder(mContext.getDrawable(R.mipmap.hetang_icon))
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE);
            final List<Drawable> drawableList = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                if (picture_array[i] != null) {
                    //LinearLayout linearLayout = new LinearLayout(mContext);
                    final RoundImageView picture = new RoundImageView(mContext);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
                    layoutParams.setMargins(0, 0, 2, 4);
                    //将以上的属性赋给LinearLayout
                    picture.setLayoutParams(layoutParams);
                    picture.setAdjustViewBounds(true);
                    if (length == 1) {
                        picture.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    } else {
                        picture.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }
                    picture.setMaxHeight(2 * width);

                    holder.dynamicsGrid.addView(picture);
                    Glide.with(mContext).load(HttpUtil.DOMAIN + picture_array[i]).apply(requestOptions).into(picture);

                    picture.setId(i);
                    picture.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            interActInterface.onDynamicPictureClick(view, position, picture_array, picture.getId());
                        }


                    });

                }
            }

            holder.dynamicsGrid.setTag(dynamic);
        }
    }

    @Override
    public int getItemCount() {
        return mMeetList != null ? mMeetList.size() : 0;
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
        if (null == mMeetList) {
            return null;
        }
        for (int i = 0; i < mMeetList.size(); i++) {
            if (did == mMeetList.get(i).getDid()) {
                return mMeetList.get(i);
            }
        }
        return null;
    }

    //register the interActInterface callback, add by zouhaichao 2018/9/16
    public void setOnCommentClickListener(InterActInterface commentDialogFragmentListener) {
        this.interActInterface = commentDialogFragmentListener;
    }

    private void handleMessage(Message message) {
        switch (message.what) {
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

    public static class MeetDynamicsViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView living;
        TextView profile;
        RoundImageView avatar;
        ConstraintLayout baseProfile;

        TextView contentView;
        TextView createdView;
        TextView dynamicsPraiseCount;
        TextView dynamicsPraise;
        TextView dynamicsComment;
        TextView operation;
        GridLayout dynamicsGrid;
        LinearLayout commentList;
        LinearLayout contentMeta;

        public MeetDynamicsViewHolder(View view) {
            super(view);
            baseProfile = view.findViewById(R.id.base_profile);
            name = (TextView) view.findViewById(R.id.name);
            living = (TextView) view.findViewById(R.id.living);
            avatar = view.findViewById(R.id.avatar);
            profile = (TextView) view.findViewById(R.id.profile);
            contentView = (TextView) view.findViewById(R.id.dynamics_content);
            dynamicsGrid = (GridLayout) view.findViewById(R.id.dynamics_picture_grid);
            //createdView = (TextView) view.findViewById(R.id.dynamic_time);
            dynamicsPraiseCount = (TextView) view.findViewById(R.id.dynamic_praise_count);
            dynamicsPraise = (TextView) view.findViewById(R.id.dynamic_praise);
            dynamicsComment = (TextView) view.findViewById(R.id.dynamic_comment);
            contentMeta = view.findViewById(R.id.dynamics_content_meta);
            operation = view.findViewById(R.id.operation);

            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
            FontManager.markAsIconContainer(view.findViewById(R.id.meet_dynamics_item), font);
        }
    }

    static class MyHandler extends HandlerTemp<MeetDynamicsListAdapter> {
        public MyHandler(MeetDynamicsListAdapter cls) {
            super(cls);
        }

        @Override
        public void handleMessage(Message message) {
            MeetDynamicsListAdapter meetDynamicsListAdapter = ref.get();
            if (meetDynamicsListAdapter != null) {
                meetDynamicsListAdapter.handleMessage(message);
            }
        }
    }

}
