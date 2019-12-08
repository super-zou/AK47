package com.hetang.meet;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hetang.R;
import com.hetang.common.MyApplication;
import com.hetang.util.BaseFragment;
import com.hetang.util.FontManager;
import com.hetang.util.Slog;

import java.lang.ref.WeakReference;

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

    private Handler handler;

    ImageView progressImageView;
    AnimationDrawable animationDrawable;

    @Override
    protected int getLayoutId() {
        int layoutId = R.layout.root_group;
        return layoutId;
    }

    @Override
    protected void initView(View convertView) {
        handler = new GroupFragment.MyHandler(this);

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
        LinearLayout rootGroupWrapper = convertView.findViewById(R.id.root_group_wrapper);
        ConstraintLayout associationGroup = convertView.findViewById(R.id.association_group);
        associationGroup.setOnClickListener(this);
        ConstraintLayout publicGoodGroup = convertView.findViewById(R.id.public_good_group);
        publicGoodGroup.setOnClickListener(this);
        ConstraintLayout fraternityGroup = convertView.findViewById(R.id.fraternity_group);
        fraternityGroup.setOnClickListener(this);
        ConstraintLayout hobbyGroup = convertView.findViewById(R.id.hobby_group);
        hobbyGroup.setOnClickListener(this);
        ConstraintLayout growUpGroup = convertView.findViewById(R.id.growUp_group);
        growUpGroup.setOnClickListener(this);
        ConstraintLayout activityGroup = convertView.findViewById(R.id.activity_group);
        activityGroup.setOnClickListener(this);
        ConstraintLayout foreignFriendGroup = convertView.findViewById(R.id.foreign_friend_group);
        foreignFriendGroup.setOnClickListener(this);

        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(convertView.findViewById(R.id.root_group_wrapper), font);
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
    
    @Override
    protected void loadData() {

    }
    
    private void getMyGroup(){

    }

    public void handleMessage(Message message) {
        switch (message.what) {
            default:
                break;
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
             
