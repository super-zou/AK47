package com.hetang.meet;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hetang.R;
import com.hetang.common.Dynamic;
import com.hetang.common.MyApplication;
import com.hetang.util.BaseFragment;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.ParseUtils;
import com.hetang.util.Slog;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GroupFragment extends BaseFragment implements View.OnClickListener {
    private static final boolean isDebug = true;
    private static final String TAG = "GroupFragment";

    private static final int association_group = 0;
    private static final int public_good_group = 1;
    private static final int fraternity_group = 2;
    private static final int hobby_group = 3;
    private static final int growUp_group = 4;
    private static final int activity_group = 5;
    private static final int foreign_friend_group = 6;
    private static final int MAX_ROOT_GROUP = 7;
    private static final int LOAD_DATA_DONE = 8;

    ConstraintLayout associationGroup;
    ConstraintLayout publicGoodGroup;
    ConstraintLayout fraternityGroup;
    ConstraintLayout hobbyGroup;
    ConstraintLayout growUpGroup;
    ConstraintLayout activityGroup;
    ConstraintLayout foreignFriendGroup;

    private static final String SUBGROUP_GET_ROOT_SUMMARY = HttpUtil.DOMAIN + "?q=subgroup/get_root_summary";

    private Handler handler;
    private View mView;
    ImageView progressImageView;
    AnimationDrawable animationDrawable;
    GroupSummary groupSummary;

    @Override
    protected int getLayoutId() {
        int layoutId = R.layout.root_group;
        return layoutId;
    }

    @Override
    protected void initView(View convertView) {
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
        if (groupSummary == null){
            groupSummary = new GroupSummary();
        }

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

        for (int type=0; type<MAX_ROOT_GROUP; type++) {
            loadGroupData(type);
        }

    }

    private void loadGroupData(final int type){
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
                    if (responseText != null) {
                            Message message = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putInt("type", type);
                            message.setData(bundle);
                            message.what = LOAD_DATA_DONE;
                            handler.sendMessage(message);
                        }
                    }
            }
            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }

    public class GroupSummary{
        int subgroupAmount;
        int visitRecord;
        int memberAmount;
        int followAmount;
        int activityAmount;
    }

    private void setGroupSummaryView(int type){
        switch (type) {
            case association_group:
                TextView universityAmountOfAssociation = mView.findViewById(R.id.university_amount_of_association);
                TextView subgroupAmountOfAssociation = mView.findViewById(R.id.subGroup_amount_of_association);
                TextView memberAmountOfAssociation = mView.findViewById(R.id.member_amount_of_association);
                TextView visitAmountOfAssociation = mView.findViewById(R.id.visit_record_of_association);
                TextView descOfAssociation = mView.findViewById(R.id.association_group_desc);
                break;
            case public_good_group:
                TextView universityAmountOfPublicGood = mView.findViewById(R.id.university_amount_of_public_good);
                TextView subgroupAmountOfPublicGood = mView.findViewById(R.id.subGroup_amount_of_public_good);
                TextView memberAmountOfPublicGood = mView.findViewById(R.id.member_amount_of_public_good);
                TextView visitAmountOfPublicGood = mView.findViewById(R.id.visit_record_of_public_good);
                TextView descOfPublicGood = mView.findViewById(R.id.public_good_group_desc);
                break;
            case fraternity_group:
                TextView universityAmountOfFraternity = mView.findViewById(R.id.university_amount_of_fraternity);
                TextView subgroupAmountOfFraternity = mView.findViewById(R.id.subGroup_amount_of_fraternity);
                TextView memberAmountOfFraternity = mView.findViewById(R.id.member_amount_of_fraternity);
                TextView visitAmountOfFraternity = mView.findViewById(R.id.visit_record_of_fraternity);
                TextView descOfFraternity = mView.findViewById(R.id.fraternity_group_desc);
                break;
            case hobby_group:
                TextView universityAmountOfHobby = mView.findViewById(R.id.university_amount_of_hobby);
                TextView subgroupAmountOfHobby = mView.findViewById(R.id.subGroup_amount_of_hobby);
                TextView memberAmountOfHobby = mView.findViewById(R.id.member_amount_of_hobby);
                TextView visitAmountOfHobby = mView.findViewById(R.id.visit_record_of_hobby);
                TextView descOfHobby = mView.findViewById(R.id.hobby_group_desc);
                break;
            case growUp_group:
                TextView universityAmountOfGrowUp = mView.findViewById(R.id.university_amount_of_growUp);
                TextView subgroupAmountOfGrowUp = mView.findViewById(R.id.subGroup_amount_of_growUp);
                TextView memberAmountOfGrowUp = mView.findViewById(R.id.member_amount_of_growUp);
                TextView visitAmountOfGrowUp = mView.findViewById(R.id.visit_record_of_growUp);
                TextView descOfGrowUp = mView.findViewById(R.id.growUp_group_desc);
                break;
            case activity_group:
                TextView universityAmount = mView.findViewById(R.id.university_amount_of_activity);
                TextView subgroupAmount = mView.findViewById(R.id.subGroup_amount_of_activity);
                TextView memberAmount = mView.findViewById(R.id.member_amount_of_activity);
                TextView visitAmount = mView.findViewById(R.id.visit_record_of_activity);
                TextView desc = mView.findViewById(R.id.activity_group_desc);
                break;
            case foreign_friend_group:
                TextView universityAmountOfForeignFriend = mView.findViewById(R.id.university_amount_of_foreign_friend);
                TextView subgroupAmountForeignFriend = mView.findViewById(R.id.subGroup_amount_of_foreign_friend);
                TextView memberAmountForeignFriend = mView.findViewById(R.id.member_amount_of_foreign_friend);
                TextView visitAmountForeignFriend = mView.findViewById(R.id.visit_record_of_foreign_friend);
                TextView descForeignFriend = mView.findViewById(R.id.foreign_friend_desc);
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v){
        Slog.d(TAG, "--------------id: "+v.getId());
        switch (v.getId()){
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

    private void startSubGroupActivity(int type){
        Intent intent = new Intent(MyApplication.getContext(), SubGroupActivity.class);
        intent.putExtra("type", type);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(intent);
    }


    public void handleMessage(Message message) {
        Bundle bundle = message.getData();
        int type = bundle.getInt("type");
        if (message.what == LOAD_DATA_DONE){
            setGroupSummaryView(type);
        }
    }

    private void stopLoadProgress(){
        if (progressImageView.getVisibility() == View.VISIBLE){
            animationDrawable.stop();
            progressImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (isDebug) Slog.d(TAG, "===================onActivityResult requestCode: "+requestCode+" resultCode: "+resultCode);
        if (requestCode == Activity.RESULT_FIRST_USER){
            switch (resultCode){

            }
        }
    }
    

    @Override
    public void onDestroy() {
        super.onDestroy();
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
             
