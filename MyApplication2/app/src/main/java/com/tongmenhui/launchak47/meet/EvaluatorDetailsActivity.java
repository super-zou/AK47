package com.tongmenhui.launchak47.meet;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.adapter.EvaluatorDetailsAdapter;
import com.tongmenhui.launchak47.main.BaseAppCompatActivity;
import com.tongmenhui.launchak47.util.HttpUtil;
import com.tongmenhui.launchak47.util.Slog;

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

import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class EvaluatorDetailsActivity extends BaseAppCompatActivity {
    private static final String TAG = "EvaluatorDetailsActivity";
    //private XRecyclerView mXRecyclerView;
    //private JSONObject impressionObj;
    private Handler handler;
    private static final int LOAD_EVALUATOR_DONE = 0;
    private EvaluatorDetailsAdapter mEvaluatorDetailsAdapter;
    List<EvaluatorDetails> mEvaluatorDetailsList;
    XRecyclerView mEvaluatorDetailsListRV;
    private static final String GET_IMPRESSION_DETAIL_URL = HttpUtil.DOMAIN + "?q=meet/impression/get_detail";
    
        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.evaluator_details);
        int uid =  (int)getIntent().getIntExtra("uid", -1);
        handler = new EvaluatorDetailsActivity.MyHandler(this);
        mEvaluatorDetailsList = new ArrayList<>();

        mEvaluatorDetailsAdapter = new EvaluatorDetailsAdapter(this);
        mEvaluatorDetailsListRV = findViewById(R.id.evaluator_details_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        mEvaluatorDetailsListRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                mEvaluatorDetailsAdapter.notifyDataSetChanged();
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
                mEvaluatorDetailsListRV.setLayoutManager(linearLayoutManager);
        mEvaluatorDetailsListRV.setRefreshProgressStyle(BallSpinFadeLoader);
        mEvaluatorDetailsListRV.setLoadingMoreProgressStyle(ProgressStyle.BallRotate);
        mEvaluatorDetailsListRV.getDefaultRefreshHeaderView().setRefreshTimeVisible(true);
        mEvaluatorDetailsListRV.setPullRefreshEnabled(false);
        mEvaluatorDetailsListRV.getDefaultRefreshHeaderView().setRefreshTimeVisible(true);

        mEvaluatorDetailsListRV.getDefaultFootView().setLoadingHint(getString(R.string.loading_pull_up_tip));
        mEvaluatorDetailsListRV.getDefaultFootView().setNoMoreHint(getString(R.string.loading_no_more));
        final int itemLimit = 5;
        
                // When the item number of the screen number is list.size-2,we call the onLoadMore
        mEvaluatorDetailsListRV.setLimitNumberToCallLoadMore(4);
        mEvaluatorDetailsListRV.setRefreshProgressStyle(ProgressStyle.BallBeat);
        mEvaluatorDetailsListRV.setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);

        mEvaluatorDetailsListRV.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                //updateData();
            }

            @Override
            public void onLoadMore() {
                //loadDynamicsData(mMeetMember.getUid());
            }
        });
        
                mEvaluatorDetailsListRV.setAdapter(mEvaluatorDetailsAdapter);

        getEvaluatorDetails(uid);
    }
    
        private void getEvaluatorDetails(int uid){
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(this, GET_IMPRESSION_DETAIL_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.body() != null){
                    String responseText = response.body().string();
                    Slog.d(TAG, "==========getEvaluatorDetails : "+responseText);
                    if(responseText != null){
                        if(!TextUtils.isEmpty(responseText)){
                            Message msg = handler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putString("response", responseText);
                            msg.setData(bundle);
                            msg.what = LOAD_EVALUATOR_DONE;
                            handler.sendMessage(msg);
                        }
                    }
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {}
        });
    }
    
     private void setEvaluatorDetails(String response){
        JSONObject evaluatorDetailsObjWraper = null;
        try {
            evaluatorDetailsObjWraper = new JSONObject(response);
        }catch (JSONException e){
            e.printStackTrace();
        }
        
                JSONArray impressionArray = evaluatorDetailsObjWraper.optJSONArray("impression");
        EvaluatorDetails evaluatorDetails = new EvaluatorDetails();
        if(impressionArray != null && impressionArray.length() > 0){
            for (int i=0; i<impressionArray.length(); i++){
                JSONObject evaluatorDetailsObj = impressionArray.optJSONObject(i);
                evaluatorDetails.setEvaluatorUid(evaluatorDetailsObj.optInt("evaluator_uid"));
                evaluatorDetails.setRating(evaluatorDetailsObj.optDouble("rating"));
                evaluatorDetails.setImpression(evaluatorDetailsObj.optString("impression"));
                evaluatorDetails.setName(evaluatorDetailsObj.optString("name"));
                evaluatorDetails.setPictureUri(evaluatorDetailsObj.optString("picture_uri"));
                
                                mEvaluatorDetailsList.add(evaluatorDetails);
            }

            mEvaluatorDetailsAdapter.setData(mEvaluatorDetailsList);
            mEvaluatorDetailsAdapter.notifyDataSetChanged();
           // mEvaluatorDetailsListRV.refreshComplete();
        }

    }
        static class MyHandler extends Handler {
        WeakReference<EvaluatorDetailsActivity> evaluatorDetailsActivityWeakReference;

        MyHandler(EvaluatorDetailsActivity evaluatorDetailsActivity) {
            evaluatorDetailsActivityWeakReference = new WeakReference<EvaluatorDetailsActivity>(evaluatorDetailsActivity);
        }

        @Override
        public void handleMessage(Message message) {
            EvaluatorDetailsActivity evaluatorDetailsActivity = evaluatorDetailsActivityWeakReference.get();
            if(evaluatorDetailsActivity != null){
                evaluatorDetailsActivity.handleMessage(message);
            }
        }
    }
    
        public void handleMessage(Message message){
        switch (message.what){
            case LOAD_EVALUATOR_DONE:
                Bundle bundle = message.getData();
                String response = bundle.getString("response");
                Slog.d(TAG, "==========================get response: "+response);
                setEvaluatorDetails(response);
                break;
            default:
                break;
        }
    }
    
        public class EvaluatorDetails{
        private int uid;
        private String name;
        private String pictureUri;
        private int evaluatorUid;
        private int visitorUid;
        private double rating;
        private String impression;
        private MeetMemberInfo meetMemberInfo;

        public int getUid(){
            return uid;
        }
                public void setUid(int uid){
            this.uid = uid;
        }

        public int getEvaluatorUid() {
            return evaluatorUid;
        }

        public void setEvaluatorUid(int evaluator_uid) {
            this.evaluatorUid = evaluator_uid;
        }

        public double getRating() {
            return rating;
        }
        
                public void setRating(double rating) {
            this.rating = rating;
        }

        public String getImpression() {
            return impression;
        }

        public void setImpression(String impression) {
            this.impression = impression;
        }

        public void setName(String name) {
            this.name = name;
        }
        
         public String getName() {
            return name;
        }

        public void setPictureUri(String pictureUri) {
            this.pictureUri = pictureUri;
        }

        public String getPictureUri() {
            return pictureUri;
        }
    }
}
