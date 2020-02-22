package com.hetang.talent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hetang.R;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.common.InvitationDialogFragment;
import com.hetang.common.MyApplication;
import com.hetang.common.SetAvatarActivity;
import com.hetang.contacts.ChatActivity;
import com.hetang.group.SingleGroupActivity;
import com.hetang.group.SubGroupActivity;
import com.hetang.group.SubGroupDetailsActivity;
import com.hetang.meet.FillMeetInfoActivity;
import com.hetang.meet.UserMeetInfo;
import com.hetang.util.CommonDialogFragmentInterface;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.ParseUtils;
import com.hetang.util.RoundImageView;
import com.hetang.util.SharedPreferencesUtils;
import com.hetang.util.Slog;
import com.hetang.util.UserProfile;
import com.hetang.util.Utility;
import com.tencent.imsdk.TIMConversationType;
import com.tencent.qcloud.tim.uikit.modules.chat.base.ChatInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hetang.common.MyApplication.getContext;
import static com.hetang.group.GroupFragment.eden_group;
import static com.hetang.group.SingleGroupActivity.getSingleGroup;
import static com.hetang.group.SubGroupActivity.getTalent;
import static com.hetang.group.SubGroupDetailsActivity.AUTHENTICATING;
import static com.hetang.group.SubGroupDetailsActivity.REJECTED;
import static com.hetang.group.SubGroupDetailsActivity.SHOW_NOTICE_DIALOG;
import static com.hetang.group.SubGroupDetailsActivity.UNAUTHENTICATED;
import static com.hetang.meet.FillMeetInfoActivity.FILL_MEET_INFO_BROADCAST;
import static com.hetang.talent.RewardDialogFragment.COMMON_TALENT_REWARD_RESULT_OK;
import static com.hetang.talent.TalentEvaluateDialogFragment.SET_EVALUATE_RESULT_OK;
import static com.hetang.talent.TalentModifyDialogFragment.TALENT_MODIFY_RESULT_OK;

public class TalentDetailsActivity extends BaseAppCompatActivity implements CommonDialogFragmentInterface{
    private static final String TAG = "TalentDetailsActivity";
    private static final boolean isDebug = true;
    private static final String GET_EVALUATION_STATISTICS_URL = HttpUtil.DOMAIN + "?q=talent/evaluation/get_statistics";
    public static final String GET_TALENT_byID = HttpUtil.DOMAIN + "?q=talent/get_by_id";
    private static final int LOAD_TALENT_DONE = 1;
    private static final int GET_EVALUATION_DONE = 3;
    private static final int nonMEMBER = -1;
    private static final int INVITTED = 1;
    private static boolean isReward = false;
    SingleGroupActivity.SingleGroup singleGroup;
    private Context mContext;
    private Handler handler = null;
    private int aid = -1;
    private SubGroupActivity.Talent talent;
    private Bundle savedInstanceState;
    private GridLayout gridLayout;
    private JSONObject memberJSONObject = new JSONObject();
    private SubGroupDetailsActivity subGroupDetailsActivity;
    private SingleGroupReceiver mReceiver = new SingleGroupReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        this.savedInstanceState = savedInstanceState;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.talent_details);
        handler = new MyHandler(this);
        aid = getIntent().getIntExtra("aid", 0);
        getTalentDetails();
        /*
        talent = (SubGroupActivity.Talent) getIntent().getSerializableExtra("talent");
        if (talent == null){
            aid = getIntent().getIntExtra("aid", 0);
            getTalentDetails();
        }else {
            setTalentDetailsView();
        }

         */
    }

    private void getTalentDetails(){
        RequestBody requestBody = new FormBody.Builder()
                .add("aid", String.valueOf(aid))
                .build();

        HttpUtil.sendOkHttpRequest(this, GET_TALENT_byID, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (isDebug) Slog.d(TAG, "==========response body : " + response.body());

                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        JSONObject talentResponse = null;
                        try {
                            talentResponse = new JSONObject(responseText);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (talentResponse != null) {
                            JSONObject talentObject = talentResponse.optJSONObject("talent");
                            talent = getTalent(talentObject);
                            handler.sendEmptyMessage(LOAD_TALENT_DONE);
                        }

                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void setTalentDetailsView() {

        TextView back = findViewById(R.id.left_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        int authorUid = SharedPreferencesUtils.getSessionUid(getContext());
        Slog.d(TAG, "---------------------->authorUid: "+authorUid+" talent uid: "+talent.profile.getUid());
        if (talent.profile.getUid() == authorUid){
            TextView modify = findViewById(R.id.save);
            modify.setText(getContext().getResources().getString(R.string.edit));
            modify.setVisibility(View.VISIBLE);
            modify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("talent", talent);
                    TalentModifyDialogFragment talentModifyDialogFragment = new TalentModifyDialogFragment();
                    talentModifyDialogFragment.setArguments(bundle);
                    talentModifyDialogFragment.show(getSupportFragmentManager(), "TalentModifyDialogFragment");
                }
            });
        }

        RoundImageView leaderHead = findViewById(R.id.avatar);
        Glide.with(MyApplication.getContext()).load(HttpUtil.DOMAIN + talent.profile.getAvatar()).into(leaderHead);
        TextView name = findViewById(R.id.name);
        name.setText(talent.profile.getNickName());
        TextView university = findViewById(R.id.university);
        TextView major = findViewById(R.id.major);
        TextView degree = findViewById(R.id.degree);

        if (talent.profile.getSituation() == 0){
            university.setText(talent.profile.getUniversity());
            major.setText(talent.profile.getMajor());
            degree.setText(talent.profile.getDegreeName(talent.profile.getDegree()));
        }else {
            university.setText(talent.profile.getIndustry());
            //major.setText(talent.profile.getMajor());
            major.setVisibility(View.GONE);
            degree.setText(talent.profile.getPosition());
        }


        TextView introduction = findViewById(R.id.introduction);
        introduction.setText(talent.introduction);

        TextView chargeDesc = findViewById(R.id.charge_desc);
        chargeDesc.setText(talent.desc);

        LinearLayout materialLL = findViewById(R.id.material);
        gridLayout = findViewById(R.id.material_pictures);
        if (talent.materialArray != null && talent.materialArray.length > 0){
            DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
            int innerWidth = dm.widthPixels;
            int wrapperWidth = innerWidth/2;
            int avatarWidth = wrapperWidth;

            materialLL.setVisibility(View.VISIBLE);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(avatarWidth, avatarWidth);
            layoutParams.setMargins(0, 0, 0, 6);
            for (int i=0; i<talent.materialArray.length; i++){
                RoundImageView materialView = new RoundImageView(this);
                materialView.setLayoutParams(layoutParams);
                materialView.setAdjustViewBounds(true);
                gridLayout.addView(materialView);
                Glide.with(MyApplication.getContext()).load(HttpUtil.DOMAIN + talent.materialArray[i]).into(materialView);
            }
        }

        TextView consultate = findViewById(R.id.consultation);
        consultate.setText(talent.charge+"元/"+"咨询");
        consultate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contactTalentDialog();
            }
        });

        Button evaluate = findViewById(R.id.evaluate_matchmaker);
        evaluate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TalentEvaluateDialogFragment talentEvaluateDialogFragment = new TalentEvaluateDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("uid", talent.profile.getUid());
                bundle.putInt("aid", talent.aid);
                bundle.putInt("type", talent.type);
                talentEvaluateDialogFragment.setArguments(bundle);
                talentEvaluateDialogFragment.show(getSupportFragmentManager(), "TalentEvaluateDialogFragment");
            }
        });

        getEvaluationStatistics();

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.talent_details_layout), font);
    }

    private void getEvaluationStatistics() {
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(talent.profile.uid))
                .add("type", String.valueOf(talent.type))
                .add("aid", String.valueOf(talent.aid))
                .build();

        HttpUtil.sendOkHttpRequest(this, GET_EVALUATION_STATISTICS_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (isDebug) Slog.d(TAG, "==========response body : " + response.body());

                if (response.body() != null) {
                    String responseText = response.body().string();
                    if (isDebug) Slog.d(TAG, "==========response text : " + responseText);
                    if (responseText != null && !TextUtils.isEmpty(responseText)) {
                        processEvaluationResponse(responseText);
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private void processEvaluationResponse(String response) {
        try {
            JSONObject evaluateObject = new JSONObject(response);
            double scores = evaluateObject.optDouble("scores");
            int count = evaluateObject.optInt("count");
            float score = 0;
            if (count != 0) {
                float scoreFloat = (float) scores / count;
                score = (float) (Math.round(scoreFloat * 10)) / 10;
            }
            Slog.d(TAG, "------------------>processEvaluationResponse: scores" + scores + "  score: " + score + " count " + count);
            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putFloat("score", score);
            bundle.putInt("count", count);
            message.setData(bundle);
            message.what = GET_EVALUATION_DONE;
            handler.sendMessage(message);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setEvaluationView(final float score, int count) {
        TextView evaluateTV = findViewById(R.id.evaluate);
        String textContent = "";
        if (count == 0) {
            textContent = getResources().getString(R.string.no_evaluate);
        } else {
            textContent = score + "分" + getResources().getString(R.string.dot) + count + "条评论";
        }
        evaluateTV.setText(textContent);

        evaluateTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TalentDetailsActivity.this, TalentEvaluatorDetailsActivity.class);
                intent.putExtra("uid", talent.profile.getUid());
                intent.putExtra("type", talent.type);
                intent.putExtra("scores", score);
                intent.putExtra("aid", talent.aid);
                startActivity(intent);
            }
        });

    }

    private void contactTalentDialog() {
        if (isReward){
            contactTalent();
        }else {
            RewardDialogFragment rewardDialogFragment = new RewardDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("uid", talent.profile.uid);
            bundle.putString("name", talent.profile.getNickName());
            bundle.putInt("type", talent.type);
            bundle.putString("qr_code", talent.payeeQRCode);
            bundle.putInt("aid", talent.aid);

            rewardDialogFragment.setArguments(bundle);
            rewardDialogFragment.show(getSupportFragmentManager(), "RewardDialogFragment");
        }
    }

    private void contactTalent() {
        ChatInfo chatInfo = new ChatInfo();
        chatInfo.setType(TIMConversationType.C2C);
        chatInfo.setId(String.valueOf(talent.profile.uid));
        chatInfo.setChatName(talent.profile.getNickName());

        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("CHAT_INFO", chatInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onBackFromDialog(int type, int result, boolean status) {
        Slog.d(TAG, "------------------------onBackFromDialog isReward: "+isReward);
        switch (type) {
            case COMMON_TALENT_REWARD_RESULT_OK://For EvaluateDialogFragment back
                if (status == true) {
                    isReward = true;
                }
                break;
            case TALENT_MODIFY_RESULT_OK:
                if (status == true){
                    if (gridLayout.getChildCount() > 0){
                        gridLayout.removeAllViews();
                    }
                    getTalentDetails();
                }
                break;
            default:
                break;
        }
    }

    public void handleMessage(Message message) {
        switch (message.what) {
            case GET_EVALUATION_DONE:
                Bundle bundle = message.getData();
                setEvaluationView(bundle.getFloat("score"), bundle.getInt("count"));
                break;
            case LOAD_TALENT_DONE:
                setTalentDetailsView();
                break;
            default:
                break;
        }
    }

    private void registerBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FILL_MEET_INFO_BROADCAST);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, intentFilter);
    }

    //unregister local broadcast
    private void unRegisterBroadcast() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterBroadcast();
    }

    static class MyHandler extends Handler {
        WeakReference<TalentDetailsActivity> talentDetailsActivityWeakReference;
        MyHandler(TalentDetailsActivity talentDetailsActivity) {
            talentDetailsActivityWeakReference = new WeakReference<TalentDetailsActivity>(talentDetailsActivity);
        }

        @Override
        public void handleMessage(Message message) {
            TalentDetailsActivity talentDetailsActivity = talentDetailsActivityWeakReference.get();
            if (talentDetailsActivity != null) {
                talentDetailsActivity.handleMessage(message);
            }
        }
    }

    private class SingleGroupReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case FILL_MEET_INFO_BROADCAST:
                    if (singleGroup.authorStatus == nonMEMBER) {
                        Toast.makeText(mContext, "申请已发出，待达人审核", Toast.LENGTH_LONG).show();
                    } else if (singleGroup.authorStatus == INVITTED) {
                        Toast.makeText(mContext, "已加入牵线团", Toast.LENGTH_LONG).show();
                    }
                    break;
            }

        }
    }
}


                
