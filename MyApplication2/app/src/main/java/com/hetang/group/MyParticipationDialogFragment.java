package com.hetang.group;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.common.MyApplication;
import com.hetang.talent.TalentDetailsActivity;
import com.hetang.util.BaseDialogFragment;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.RoundImageView;
import com.hetang.util.Slog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hetang.group.GroupFragment.GET_MY_TALENTS;
import static com.hetang.group.GroupFragment.LOAD_MY_GROUP_DONE;
import static com.hetang.group.GroupFragment.LOAD_MY_TALENTS_DONE;
import static com.hetang.group.GroupFragment.SUBGROUP_GET_MY_GROUP;
import static com.hetang.group.SubGroupActivity.getSubGroup;
import static com.hetang.group.SubGroupActivity.getTalent;
import static com.hetang.group.SubGroupActivity.updateVisitorRecord;
import static com.hetang.main.MeetArchiveFragment.GET_EXPERIENCE_STATISTICS_URL;

public class MyParticipationDialogFragment extends BaseDialogFragment {
    private Dialog mDialog;
    private static final boolean isDebug = true;
    private MyHandler myHandler;
    private static final String TAG = "MyParticipationDialogFragment";
    public static final int MY_EXPERIENCE = 0;
    public static final int MY_TALENT = 1;
    public static final int MY_GUIDE = 2;
    LinearLayout myParticipationWrapper;
    List<SubGroupActivity.Talent> talentList = new ArrayList<>();
    List<SubGroupActivity.SubGroup> groupList = new ArrayList<>();
    
        public static MyParticipationDialogFragment newInstance(int type, int uid) {
        MyParticipationDialogFragment myParticipationDialogFragment = new MyParticipationDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        bundle.putInt("uid", uid);

        myParticipationDialogFragment.setArguments(bundle);

        return myParticipationDialogFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_Design_BottomSheetDialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.my_participation);
        
         Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.create_subgroup), font);
        mDialog.setCanceledOnTouchOutside(true);
        Window window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        myParticipationWrapper = mDialog.findViewById(R.id.my_participation_wrapper);

        myHandler = new MyHandler(this);
        Bundle bundle = getArguments();
        int uid = 0;
        if (bundle != null){
            int type = bundle.getInt("type");
            uid = bundle.getInt("uid");
            if (type == MY_EXPERIENCE){
                loadMyExperiences(uid);
            }else {
                loadMyTalents(uid);
            }
        }
        
         TextView back = mDialog.findViewById(R.id.left_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });

        return mDialog;
    }

    private void loadMyTalents(int uid){
                RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_MY_TALENTS,requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
            if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========loadMyTalents response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject talentResponse = null;
                        try {
                            talentResponse = new JSONObject(responseText);
                            if (talentResponse != null) {
                                processTalentResponse(talentResponse);
                                if (talentList.size() > 0){
                                    myHandler.sendEmptyMessage(LOAD_MY_TALENTS_DONE);
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

    public int processTalentResponse(JSONObject talentResponse) {

        int talentSize = 0;
        JSONArray talentArray = null;

        if (talentResponse != null) {
            talentArray = talentResponse.optJSONArray("talents");
        }
        
        if (talentArray != null) {
            talentSize = talentArray.length();
            if (talentSize > 0) {
                for (int i = 0; i < talentArray.length(); i++) {
                    JSONObject talentObject = talentArray.optJSONObject(i);
                    if (talentObject != null) {
                        SubGroupActivity.Talent talent = getTalent(talentObject);
                        talentList.add(talent);
                    }
                }
            }
        }

        return talentSize;
    }
    
     private void setMyTalentViewData(){
        int size = talentList.size();
        if (size > 1){
            for (int i=0; i<size; i++){
                SubGroupActivity.Talent talent = talentList.get(i);
                View talentView = setMyTalentItem(talent);
                myParticipationWrapper.addView(talentView);
            }
        }else {
            SubGroupActivity.Talent talent = talentList.get(0);
            Intent intent = new Intent(getContext(), TalentDetailsActivity.class);
            //intent.putExtra("talent", talent);
            intent.putExtra("tid", talent.tid);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
            mDialog.dismiss();
        }
    }

    private View setMyTalentItem(SubGroupActivity.Talent talent) {
        final View myTalentView = LayoutInflater.from(getContext()).inflate(R.layout.talent_summary_item, (ViewGroup) mDialog.findViewById(android.R.id.content), false);
        TextView name = myTalentView.findViewById(R.id.leader_name);
        name.setText(talent.profile.getNickName().trim());
        
        RoundImageView avatar = myTalentView.findViewById(R.id.leader_avatar);
        if (talent.profile.getAvatar() != null && !"".equals(talent.profile.getAvatar())) {
            Glide.with(getContext()).load(HttpUtil.DOMAIN + talent.profile.getAvatar()).into(avatar);
        } else {
            if (talent.profile.getSex() == 0){
                avatar.setImageDrawable(getContext().getDrawable(R.drawable.male_default_avator));
            }else {
                avatar.setImageDrawable(getContext().getDrawable(R.drawable.female_default_avator));
            }
        }
        
        TextView university = myTalentView.findViewById(R.id.university);
        TextView degree = myTalentView.findViewById(R.id.degree);
        TextView majorTV = myTalentView.findViewById(R.id.major);
        TextView subject = myTalentView.findViewById(R.id.subject);
        TextView introduction = myTalentView.findViewById(R.id.introduction);
        TextView star = myTalentView.findViewById(R.id.star);
        TextView evaluateCount = myTalentView.findViewById(R.id.evaluate_count);
        if (talent.profile.getSituation() == 0){
            university.setText(talent.profile.getUniversity().trim());
            degree.setText(talent.profile.getDegreeName(talent.profile.getDegree()));
            majorTV.setText(talent.profile.getMajor());
        }else {
            university.setText(talent.profile.getIndustry());
            degree.setText(talent.profile.getPosition());
        }
        
        TextView titleTV = myTalentView.findViewById(R.id.talent_title);
        titleTV.setText(talent.title);
        subject.setText(talent.subject);
        introduction.setText(talent.introduction);
        //holder.maleCount.setText(mContext.getResources().getString(R.string.male)+" "+singleGroup.maleCount);
        //holder.femaleCount.setText(mContext.getResources().getString(R.string.female)+" "+singleGroup.femaleCount);
        if (talent.evaluateCount > 0){
            float scoreFloat = talent.evaluateScores/talent.evaluateCount;
            float score = (float)(Math.round(scoreFloat*10))/10;
            star.setVisibility(View.VISIBLE);
            evaluateCount.setText(score+"("+talent.evaluateCount+")");
        }
        
                if (talent.answerCount > 0){
            TextView answerCountTV = myTalentView.findViewById(R.id.answer_count);
            if(talent.answerCount > 0){
                answerCountTV.setText(getResources().getString(R.string.dot) + "解答"+talent.answerCount);
            }else {
                answerCountTV.setText("解答"+talent.answerCount);
            }
        }
        
        ConstraintLayout talentItem = myTalentView.findViewById(R.id.talent_summary_item);
        talentItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), TalentDetailsActivity.class);
                //intent.putExtra("talent", talent);
                intent.putExtra("tid", talent.tid);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });
        
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(myTalentView.findViewById(R.id.cny), font);
        FontManager.markAsIconContainer(myTalentView.findViewById(R.id.star), font);

        return myTalentView;
    }


    private void loadMyExperiences(int uid) {
                RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), GET_EXPERIENCE_STATISTICS_URL, requestBody, new Callback() {
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
                                    myHandler.sendEmptyMessage(LOAD_MY_GROUP_DONE);
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
    
    private void setMyGroupViewData() {
        int size = groupList.size();
        for (int i = 0; i < size; i++) {
            SubGroupActivity.SubGroup subGroup = groupList.get(i);
            View subView = setMyGroupItem(subGroup);
            myParticipationWrapper.addView(subView);
        }
    }
    
    private View setMyGroupItem(SubGroupActivity.SubGroup subGroup) {
        final View myGroupView = LayoutInflater.from(getContext()).inflate(R.layout.my_group_summary, (ViewGroup) mDialog.findViewById(android.R.id.content), false);
        RoundImageView groupLogo = myGroupView.findViewById(R.id.group_logo);
        TextView groupName = myGroupView.findViewById(R.id.group_name);
        TextView groupDesc = myGroupView.findViewById(R.id.group_desc);
        myGroupView.setId(subGroup.gid);
        groupName.setText(subGroup.groupName);
        groupDesc.setText(subGroup.groupProfile);
        if (subGroup.groupLogoUri != null && !"".equals(subGroup.groupLogoUri)) {
            Glide.with(getContext()).load(HttpUtil.DOMAIN + subGroup.groupLogoUri).into(groupLogo);
        } else {
            groupLogo.setImageDrawable(getContext().getDrawable(R.drawable.icon));
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
    
     private void handleMessage(Message message){
        switch (message.what){
            case LOAD_MY_TALENTS_DONE:
                setMyTalentViewData();
                break;
            case LOAD_MY_GROUP_DONE:
                setMyGroupViewData();
                break;
                default:
                    break;
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }
    
     @Override
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
    }
    
    static class MyHandler extends Handler {
        WeakReference<MyParticipationDialogFragment> myParticipationDialogFragmentWeakReference;
        MyHandler(MyParticipationDialogFragment myParticipationDialogFragment) {
            myParticipationDialogFragmentWeakReference = new WeakReference<>(myParticipationDialogFragment);
        }

        @Override
        public void handleMessage(Message message) {
            MyParticipationDialogFragment myParticipationDialogFragment = myParticipationDialogFragmentWeakReference.get();
            if (myParticipationDialogFragment != null) {
                myParticipationDialogFragment.handleMessage(message);
            }
        }
    }


}
