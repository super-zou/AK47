package com.hetang.group;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.common.MyApplication;
import com.hetang.util.BaseFragment;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.RoundImageView;
import com.hetang.util.Slog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hetang.group.SubGroupActivity.GROUP_ADD_BROADCAST;
import static com.hetang.group.SubGroupActivity.getSubGroup;
import static com.hetang.group.SubGroupActivity.updateVisitorRecord;
import static com.hetang.group.SubGroupDetailsActivity.GET_SUBGROUP_BY_GID;
import static com.hetang.group.SubGroupDetailsActivity.JOIN_GROUP_BROADCAST;

public class GroupFragment extends BaseFragment implements View.OnClickListener {
    private static final boolean isDebug = true;
    private static final String TAG = "GroupFragment";

    public static final int association_group = 0;
    public static final int public_good_group = 1;
    public static final int fraternity_group = 2;
    public static final int hobby_group = 3;
    public static final int growUp_group = 4;
    public static final int activity_group = 5;
    public static final int foreign_friend_group = 6;
    private static final int MAX_ROOT_GROUP = 7;
    private static final int LOAD_DATA_DONE = 8;
    private static final int LOAD_MY_GROUP_DONE = 9;
    private static final int LOAD_NEW_JOINED_GROUP_DONE = 10;
    public static final int SINGLE_GROUP = 20;

    LinearLayout myGroupWrap;
    ConstraintLayout associationGroup;
    ConstraintLayout publicGoodGroup;
    ConstraintLayout fraternityGroup;
    ConstraintLayout hobbyGroup;
    ConstraintLayout growUpGroup;
    ConstraintLayout activityGroup;
    ConstraintLayout foreignFriendGroup;

    private static final String SUBGROUP_GET_ROOT_SUMMARY = HttpUtil.DOMAIN + "?q=subgroup/get_root_summary";
    private static final String SUBGROUP_GET_MY_GROUP = HttpUtil.DOMAIN + "?q=subgroup/get_my";

    private Handler handler;
    private Context mContext;
    private View mView;
    ImageView progressImageView;
    List<SubGroupActivity.SubGroup> groupList = new ArrayList<>();
    AnimationDrawable animationDrawable;
    private GroupReceiver groupReceiver = new GroupReceiver();

    List<GroupSummary> groupSummaryList = new ArrayList<>();

    @Override
    protected int getLayoutId() {
        int layoutId = R.layout.root_group;
        return layoutId;
    }

    @Override
    protected void initView(View convertView) {
        mContext = getContext();
        handler = new GroupFragment.MyHandler(this);
        mView = convertView;
        //show progressImage before loading done
        /*
        progressImageView = convertView.findViewById(R.id.animal_progress);
        animationDrawable = (AnimationDrawable)progressImageView.getDrawable();
        progressImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        },50);
         */

        myGroupWrap = convertView.findViewById(R.id.my_group);

        loadMyGroup();

        associationGroup = convertView.findViewById(R.id.association_group);
        associationGroup.setOnClickListener(this);
        publicGoodGroup = convertView.findViewById(R.id.public_good_group);
        publicGoodGroup.setOnClickListener(this);
        fraternityGroup = convertView.findViewById(R.id.fraternity_group);
        fraternityGroup.setOnClickListener(this);
        hobbyGroup = convertView.findViewById(R.id.hobby_group);
        hobbyGroup.setOnClickListener(this);
        growUpGroup = convertView.findViewById(R.id.growUp_group);
        growUpGroup.setOnClickListener(this);
        activityGroup = convertView.findViewById(R.id.activity_group);
        activityGroup.setOnClickListener(this);
        foreignFriendGroup = convertView.findViewById(R.id.foreign_friend_group);
        foreignFriendGroup.setOnClickListener(this);

        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(convertView.findViewById(R.id.root_group_wrapper), font);

        for (int type = 0; type < MAX_ROOT_GROUP; type++) {
            loadGroupData(type);
        }
        registerBroadcast();
    }

    private void loadMyGroup() {

        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), SUBGROUP_GET_MY_GROUP, new FormBody.Builder().build(), new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject subGroupResponse = null;
                        try {
                            subGroupResponse = new JSONObject(responseText);
                            if (subGroupResponse != null) {
                                processResponse(subGroupResponse);
                                if (groupList.size() > 0){
                                    handler.sendEmptyMessage(LOAD_MY_GROUP_DONE);
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }

    private int processResponse(JSONObject subGroupResponse) {

        int subGroupSize = 0;
        JSONArray subGroupArray = null;

        if (subGroupResponse != null) {
            subGroupArray = subGroupResponse.optJSONArray("subgroup");
        }
        if (subGroupArray != null) {
            subGroupSize = subGroupArray.length();
            if (subGroupSize > 0) {
                for (int i = 0; i < subGroupArray.length(); i++) {
                    JSONObject group = subGroupArray.optJSONObject(i);
                    if (group != null) {
                        SubGroupActivity.SubGroup subGroup = getSubGroup(group);
                        groupList.add(subGroup);
                    }
                }
            }
        }

        return subGroupSize;
    }


    private void loadGroupData(final int type) {
        RequestBody requestBody = new FormBody.Builder()
                .add("type", String.valueOf(type))
                .build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), SUBGROUP_GET_ROOT_SUMMARY, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    //Slog.d(TAG, "==========response : "+response.body());
                    if (isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    try {
                        GroupSummary groupSummary = new GroupSummary();
                        JSONObject summaryObject = new JSONObject(responseText).optJSONObject("result");
                        groupSummary.subgroupAmount = summaryObject.optInt("subgroup_count");
                        groupSummary.memberAmount = summaryObject.optInt("member_count");
                        groupSummary.visitRecord = summaryObject.optInt("visit_record");
                        groupSummary.followAmount = summaryObject.optInt("follow_count");
                        groupSummary.activityAmount = summaryObject.optInt("activity_count");

                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putInt("type", type);
                        bundle.putSerializable("summary", groupSummary);
                        message.setData(bundle);
                        message.what = LOAD_DATA_DONE;
                        handler.sendMessage(message);

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

    public class GroupSummary implements Serializable {
        int subgroupAmount;
        int visitRecord;
        int memberAmount;
        int followAmount;
        int activityAmount;
    }

    private void getMyNewJoinedGroupData(int gid) {
        getSubGroupByGid(gid);
    }

    private void getSubGroupByGid(int gid) {
        RequestBody requestBody = new FormBody.Builder()
                .add("gid", String.valueOf(gid))
                .build();
        HttpUtil.sendOkHttpRequest(getContext(), GET_SUBGROUP_BY_GID, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (isDebug) Slog.d(TAG, "==========response body : " + response.body());

                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        processNewAddSubGroupResponse(responseText);
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void processNewAddSubGroupResponse(String response) {
        JSONObject subGroupResponse = null;

        try {
            subGroupResponse = new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (subGroupResponse != null) {
            SubGroupActivity.SubGroup subGroup;
            JSONObject group = subGroupResponse.optJSONObject("group");
            subGroup = getSubGroup(group);
            groupList.add(0, subGroup);

            handler.sendEmptyMessage(LOAD_NEW_JOINED_GROUP_DONE);
        }

    }


    private void setMyGroupViewData() {
        TextView myGroupLabel = mView.findViewById(R.id.my_group_label);
        myGroupLabel.setVisibility(View.VISIBLE);
        myGroupWrap.setVisibility(View.VISIBLE);
        int size = groupList.size();
        for (int i = 0; i < size; i++) {
            SubGroupActivity.SubGroup subGroup = groupList.get(i);
            View subView = setMyGroupItem(subGroup);
            myGroupWrap.addView(subView);
        }
    }

    private void setNewJoinedGroupView() {
        SubGroupActivity.SubGroup subGroup = groupList.get(0);
        View subView = setMyGroupItem(subGroup);
        myGroupWrap.addView(subView, 0);
    }

    private View setMyGroupItem(SubGroupActivity.SubGroup subGroup) {
        final View myGroupView = LayoutInflater.from(getContext()).inflate(R.layout.my_group_summary, (ViewGroup) mView.findViewById(android.R.id.content), false);
        RoundImageView groupLogo = myGroupView.findViewById(R.id.group_logo);
        TextView groupName = myGroupView.findViewById(R.id.group_name);
        TextView groupDesc = myGroupView.findViewById(R.id.group_desc);
        /*
        TextView visitRecord = myGroupView.findViewById(R.id.visit_record);
        TextView activityCount = myGroupView.findViewById(R.id.activity_count);
        TextView followCount = myGroupView.findViewById(R.id.follow_count);
        TextView memberCount = myGroupView.findViewById(R.id.member_count);
        */
        myGroupView.setId(subGroup.gid);
        groupName.setText(subGroup.groupName);
        groupDesc.setText(subGroup.groupProfile);
        /*
        visitRecord.setText(mContext.getResources().getString(R.string.visit) + " " + subGroup.visitRecord);
        followCount.setText(mContext.getResources().getString(R.string.follow) + " " + subGroup.followCount);
        activityCount.setText(mContext.getResources().getString(R.string.dynamics) + " " + subGroup.activityCount);
        memberCount.setText(mContext.getResources().getString(R.string.member) + " " + subGroup.memberCount);
        */

        if (subGroup.groupLogoUri != null && !"".equals(subGroup.groupLogoUri)) {
            Glide.with(mContext).load(HttpUtil.DOMAIN + subGroup.groupLogoUri).into(groupLogo);
        } else {
            groupLogo.setImageDrawable(mContext.getDrawable(R.drawable.icon));
        }

        myGroupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int gid = myGroupView.getId();
                updateVisitorRecord(gid);
                Intent intent = new Intent(getContext(), SubGroupDetailsActivity.class);
                intent.putExtra("gid", gid);
                startActivity(intent);
            }
        });

        return myGroupView;
    }


    private void setGroupSummaryView(int type, GroupSummary groupSummary) {
        switch (type) {
            case association_group:
                //TextView universityAmountOfAssociation = mView.findViewById(R.id.university_amount_of_association);
                TextView subgroupAmountOfAssociation = mView.findViewById(R.id.subGroup_amount_of_association);
                subgroupAmountOfAssociation.setText("社团 " + groupSummary.subgroupAmount);
                TextView memberAmountOfAssociation = mView.findViewById(R.id.member_amount_of_association);
                memberAmountOfAssociation.setText("成员 " + groupSummary.memberAmount);
                TextView visitAmountOfAssociation = mView.findViewById(R.id.visit_record_of_association);
                visitAmountOfAssociation.setText("访问 " + groupSummary.visitRecord);
                TextView descOfAssociation = mView.findViewById(R.id.association_group_desc);

                break;
            case public_good_group:
                //TextView universityAmountOfPublicGood = mView.findViewById(R.id.university_amount_of_public_good);
                TextView subgroupAmountOfPublicGood = mView.findViewById(R.id.subGroup_amount_of_public_good);
                subgroupAmountOfPublicGood.setText("组织 " + groupSummary.subgroupAmount);
                TextView memberAmountOfPublicGood = mView.findViewById(R.id.member_amount_of_public_good);
                memberAmountOfPublicGood.setText("成员 " + groupSummary.memberAmount);
                TextView visitAmountOfPublicGood = mView.findViewById(R.id.visit_record_of_public_good);
                visitAmountOfPublicGood.setText("访问 " + groupSummary.visitRecord);
                TextView descOfPublicGood = mView.findViewById(R.id.public_good_group_desc);
                break;
            case fraternity_group:
                //TextView universityAmountOfFraternity = mView.findViewById(R.id.university_amount_of_fraternity);
                TextView subgroupAmountOfFraternity = mView.findViewById(R.id.subGroup_amount_of_fraternity);
                subgroupAmountOfFraternity.setText("群组 " + groupSummary.subgroupAmount);
                TextView memberAmountOfFraternity = mView.findViewById(R.id.member_amount_of_fraternity);
                memberAmountOfFraternity.setText("成员 " + groupSummary.memberAmount);
                TextView visitAmountOfFraternity = mView.findViewById(R.id.visit_record_of_fraternity);
                visitAmountOfFraternity.setText("访问 " + groupSummary.visitRecord);
                TextView descOfFraternity = mView.findViewById(R.id.fraternity_group_desc);
                break;
            case hobby_group:
                //TextView universityAmountOfHobby = mView.findViewById(R.id.university_amount_of_hobby);
                TextView subgroupAmountOfHobby = mView.findViewById(R.id.subGroup_amount_of_hobby);
                subgroupAmountOfHobby.setText("群组 " + String.valueOf(groupSummary.subgroupAmount));
                TextView memberAmountOfHobby = mView.findViewById(R.id.member_amount_of_hobby);
                memberAmountOfHobby.setText("成员 " + String.valueOf(groupSummary.memberAmount));
                TextView visitAmountOfHobby = mView.findViewById(R.id.visit_record_of_hobby);
                visitAmountOfHobby.setText("访问 " + String.valueOf(groupSummary.visitRecord));
                TextView descOfHobby = mView.findViewById(R.id.hobby_group_desc);
                break;
            case growUp_group:
                //TextView universityAmountOfGrowUp = mView.findViewById(R.id.university_amount_of_growUp);
                TextView subgroupAmountOfGrowUp = mView.findViewById(R.id.subGroup_amount_of_growUp);
                subgroupAmountOfGrowUp.setText("群组 " + String.valueOf(groupSummary.subgroupAmount));
                TextView memberAmountOfGrowUp = mView.findViewById(R.id.member_amount_of_growUp);
                memberAmountOfGrowUp.setText("成员 " + String.valueOf(groupSummary.memberAmount));
                TextView visitAmountOfGrowUp = mView.findViewById(R.id.visit_record_of_growUp);
                visitAmountOfGrowUp.setText("访问 " + String.valueOf(groupSummary.visitRecord));
                TextView descOfGrowUp = mView.findViewById(R.id.growUp_group_desc);
                break;
            case activity_group:
                //TextView universityAmount = mView.findViewById(R.id.university_amount_of_activity);
                TextView subgroupAmount = mView.findViewById(R.id.subGroup_amount_of_activity);
                subgroupAmount.setText("群组 " + String.valueOf(groupSummary.subgroupAmount));
                TextView memberAmount = mView.findViewById(R.id.member_amount_of_activity);
                memberAmount.setText("成员 " + String.valueOf(groupSummary.memberAmount));
                TextView visitAmount = mView.findViewById(R.id.visit_record_of_activity);
                visitAmount.setText("访问 " + String.valueOf(groupSummary.visitRecord));
                TextView desc = mView.findViewById(R.id.activity_group_desc);
                break;
            case foreign_friend_group:
                //TextView universityAmountOfForeignFriend = mView.findViewById(R.id.university_amount_of_foreign_friend);
                TextView subgroupAmountForeignFriend = mView.findViewById(R.id.subGroup_amount_of_foreign_friend);
                subgroupAmountForeignFriend.setText("群组 " + String.valueOf(groupSummary.subgroupAmount));
                TextView memberAmountForeignFriend = mView.findViewById(R.id.member_amount_of_foreign_friend);
                memberAmountForeignFriend.setText("成员 " + String.valueOf(groupSummary.memberAmount));
                TextView visitAmountForeignFriend = mView.findViewById(R.id.visit_record_of_foreign_friend);
                visitAmountForeignFriend.setText("访问 " + String.valueOf(groupSummary.visitRecord));
                TextView descForeignFriend = mView.findViewById(R.id.foreign_friend_desc);
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        Slog.d(TAG, "--------------id: " + v.getId());
        switch (v.getId()) {
            case R.id.association_group:
                startSubGroupActivity(association_group);
                break;
            case R.id.public_good_group:
                startSubGroupActivity(public_good_group);
                break;
            case R.id.fraternity_group:
                startSubGroupActivity(fraternity_group);
                break;
            case R.id.hobby_group:
                startSubGroupActivity(hobby_group);
                break;
            case R.id.growUp_group:
                startSubGroupActivity(growUp_group);
                break;
            case R.id.activity_group:
                startSubGroupActivity(activity_group);
                break;
            case R.id.foreign_friend_group:
                startSubGroupActivity(foreign_friend_group);
                break;
            default:
                break;

        }
    }

    private void startSubGroupActivity(int type) {
        if (type < activity_group){
            Intent intent = new Intent(MyApplication.getContext(), SubGroupActivity.class);
            intent.putExtra("type", type);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
        }else {
            PreparationDialogFragment preparationDialogFragment = new PreparationDialogFragment();
            //preparationDialogFragment.setTargetFragment(MeetArchiveFragment.this, REQUESTCODE);
            Bundle bundle = new Bundle();
            bundle.putInt("type", type);
            preparationDialogFragment.setArguments(bundle);
            preparationDialogFragment.show(getFragmentManager(), "PreparationDialogFragment");
        }

    }


    public void handleMessage(Message message) {
        Bundle bundle = message.getData();
        switch (message.what) {
            case LOAD_MY_GROUP_DONE:
                setMyGroupViewData();
                break;
            case LOAD_DATA_DONE:
                int type = bundle.getInt("type");
                GroupSummary groupSummary = (GroupSummary) bundle.getSerializable("summary");
                setGroupSummaryView(type, groupSummary);
                break;
            case LOAD_NEW_JOINED_GROUP_DONE:
                setNewJoinedGroupView();
                break;
            default:
                break;
        }
    }

    private void stopLoadProgress() {
        if (progressImageView.getVisibility() == View.VISIBLE) {
            animationDrawable.stop();
            progressImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (isDebug)
            Slog.d(TAG, "===================onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode);
        if (requestCode == Activity.RESULT_FIRST_USER) {
            switch (resultCode) {

            }
        }
    }

    private class GroupReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case JOIN_GROUP_BROADCAST:
                case GROUP_ADD_BROADCAST:
                    int gid = intent.getIntExtra("gid", -1);
                    if (gid > 0) {
                        getMyNewJoinedGroupData(gid);
                    }
                    break;
            }

        }
    }

    private void registerBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(JOIN_GROUP_BROADCAST);
        intentFilter.addAction(GROUP_ADD_BROADCAST);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(groupReceiver, intentFilter);
    }

    //unregister local broadcast
    private void unRegisterLoginBroadcast() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(groupReceiver);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterLoginBroadcast();
    }

    @Override
    protected void loadData() {

    }

    static class MyHandler extends Handler {
        WeakReference<GroupFragment> groupFragmentWeakReference;

        MyHandler(GroupFragment groupFragment) {
            groupFragmentWeakReference = new WeakReference<GroupFragment>(groupFragment);
        }

        @Override
        public void handleMessage(Message message) {
            GroupFragment groupFragment = groupFragmentWeakReference.get();
            if (groupFragment != null) {
                groupFragment.handleMessage(message);
            }
        }
    }

}
             
