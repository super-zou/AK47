package com.tongmenhui.launchak47.meet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

//import com.google.gson.JsonSerializer;
import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.region.activity.RegionSelectionActivity;
import com.tongmenhui.launchak47.util.HttpUtil;

import org.angmarch.views.NiceSpinner;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FillMeetInfoActivity extends AppCompatActivity {
    Resources res;
    String[] years;
    String[] months;
    String[] days;
    String[] heights;
    String[] degrees;
    String[] ages;
    private final static int SELFREQUEST = 1;
    private final static int REQUIREREQUEST = 2;
    MeetMemberInfo meetMemberInfo;
    SelfCondition selfCondition;
    PartnerRequirement partnerRequirement;
    JSONObject selfConditionJson;
    JSONObject partnerRequirementJson;
    boolean SELFSEXCHECKED = false;
    boolean BIRTHYEARSELECTED = false;
    boolean BIRTHMONTHSELECTED = false;
    boolean BIRTHDAYSELECTED = false;
    boolean HEIGHTSELECTED = false;
    boolean DEGREESELECTED = false;
    boolean SELFLIVESELECTED = false;
    boolean REQUIRESEXCHECKED = false;
    boolean LOWERAGESELECTED = false;
    boolean UPPERAGESELECTED = false;
    boolean REQUIREHEIGHTSELECTED = false;
    boolean REQUIREDEGREESELECTED = false;
    boolean REQUIRELIVESELECTED = false;
    private static final String fillMeetInfoUrl = "http://112.126.83.127:88/?q=meet/look_friend";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_meet_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        res =getResources();
        years = res.getStringArray(R.array.years);
        months = res.getStringArray(R.array.months);
        days = res.getStringArray(R.array.days);
        heights = res.getStringArray(R.array.heights);
        degrees = res.getStringArray(R.array.degrees);
        ages = res.getStringArray(R.array.ages);

        meetMemberInfo = new MeetMemberInfo();
        selfCondition = new SelfCondition();
        partnerRequirement = new PartnerRequirement();

        final LinearLayout selfLayout = (LinearLayout)findViewById(R.id.self);
        final LinearLayout requireLayout = (LinearLayout)findViewById(R.id.requirement);

        RadioGroup self_sex = (RadioGroup)findViewById(R.id.self_sex);
        self_sex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.self_sex_male){
                    //Toast.makeText(FillMeetInfoActivity.this,"male", Toast.LENGTH_SHORT).show();
                    //meetMemberInfo.setSelfSex(0);
                    selfCondition.setSelfSex(0);
                }else{
                    //Toast.makeText(FillMeetInfoActivity.this,"female", Toast.LENGTH_SHORT).show();
                    //meetMemberInfo.setSelfSex(1);
                    selfCondition.setSelfSex(1);
                }
                SELFSEXCHECKED = true;
            }
        });

        NiceSpinner niceSpinnerYears = (NiceSpinner) findViewById(R.id.nice_spinner_years);
        final List<String> yearList = new LinkedList<>(Arrays.asList(years));
        niceSpinnerYears.attachDataSource(yearList);
        niceSpinnerYears.addOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                //Toast.makeText(FillMeetInfoActivity.this, "年份"+String.valueOf(yearList.get(i)), Toast.LENGTH_SHORT).show();
                selfCondition.setBirthYear(Integer.parseInt(yearList.get(i)));
                BIRTHYEARSELECTED = true;
            }

        });

        NiceSpinner niceSpinnerMonths = (NiceSpinner) findViewById(R.id.nice_spinner_months);
        final List<String> monthList = new LinkedList<>(Arrays.asList(months));
        niceSpinnerMonths.attachDataSource(monthList);
        niceSpinnerMonths.addOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                //Toast.makeText(FillMeetInfoActivity.this, String.valueOf(monthList.get(i)), Toast.LENGTH_SHORT).show();
                selfCondition.setBirthMonth(Integer.parseInt(monthList.get(i)));
                BIRTHMONTHSELECTED = true;
            }

        });

        NiceSpinner niceSpinnerDays = (NiceSpinner) findViewById(R.id.nice_spinner_days);
        final List<String> dayList = new LinkedList<>(Arrays.asList(days));
        niceSpinnerDays.attachDataSource(dayList);
        niceSpinnerDays.addOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                //Toast.makeText(FillMeetInfoActivity.this, String.valueOf(dayList.get(i)), Toast.LENGTH_SHORT).show();
                selfCondition.setBirthDay(Integer.parseInt(dayList.get(i)));
                BIRTHDAYSELECTED = true;
            }

        });

        NiceSpinner niceSpinnerHeight = (NiceSpinner) findViewById(R.id.nice_spinner_height);
        final List<String> heightList = new LinkedList<>(Arrays.asList(heights));
        niceSpinnerHeight.attachDataSource(heightList);
        niceSpinnerHeight.addOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                //Toast.makeText(FillMeetInfoActivity.this, String.valueOf(heightList.get(i)), Toast.LENGTH_SHORT).show();
                selfCondition.setHeight(Integer.parseInt(heightList.get(i)));
                HEIGHTSELECTED = true;
            }

        });

        NiceSpinner niceSpinnerDegree = (NiceSpinner) findViewById(R.id.nice_spinner_degree);
        final List<String> degreeList = new LinkedList<>(Arrays.asList(degrees));
        niceSpinnerDegree.attachDataSource(degreeList);
        niceSpinnerDegree.addOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                //Toast.makeText(FillMeetInfoActivity.this, String.valueOf(degreeList.get(i)), Toast.LENGTH_SHORT).show();
                selfCondition.setDegree(String.valueOf(degreeList.get(i)));
                DEGREESELECTED = true;
            }

        });
        Button selfRegionSelection = (Button)findViewById(R.id.self_region);
        selfRegionSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FillMeetInfoActivity.this, RegionSelectionActivity.class);
                startActivityForResult(intent, SELFREQUEST);
            }
        });

        final Button preButton = (Button)findViewById(R.id.prev);
        final Button nextButton = (Button) findViewById(R.id.next);
        final Button doneButton = (Button)findViewById(R.id.done);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkSelfInfo()){
                    selfLayout.setVisibility(View.GONE);
                    requireLayout.setVisibility(View.VISIBLE);
                    createRequiredView();
                    v.setVisibility(View.GONE);
                    preButton.setVisibility(View.VISIBLE);
                    doneButton.setVisibility(View.VISIBLE);
                    selfConditionJson = getSelfConditionJsonObject();

                }
            }
        });

        preButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selfLayout.setVisibility(View.VISIBLE);
                requireLayout.setVisibility(View.INVISIBLE);
                v.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
                doneButton.setVisibility(View.GONE);
            }
        });
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText)findViewById(R.id.illustration);
                partnerRequirement.setIllustration(editText.getText().toString());
                partnerRequirementJson = getPartnerRequirementJsonObject();
                if(checkRequiredInfo()){
                    RequestBody requestBody = new FormBody.Builder()
                                                          .add("self_condition", selfConditionJson.toString())
                                                          .add("partner_requirement", partnerRequirementJson.toString())
                                                          .build();

                    HttpUtil.sendOkHttpRequest(FillMeetInfoActivity.this, fillMeetInfoUrl, requestBody, new Callback(){
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String responseText = response.body().string();
                            //Slog.d(TAG, "response : "+responseText);
                            //getResponseText(responseText);
                            finish();
                        }

                        @Override
                        public void onFailure(Call call, IOException e){

                        }
                    });
                }
            }
        });

    }

    private JSONObject getSelfConditionJsonObject(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("sex", selfCondition.getSelfSex());
            jsonObject.put("birth_year", selfCondition.getBirthYear());
            jsonObject.put("birth_month", selfCondition.getBirthMonth());
            jsonObject.put("birth_day", selfCondition.getBirthDay());
            jsonObject.put("height", selfCondition.getHeight());
            jsonObject.put("degree", selfCondition.getDegreeIndex());
            jsonObject.put("lives", selfCondition.getLives());
        }catch (JSONException e){
            e.printStackTrace();
        }


        return jsonObject;
    }

    private JSONObject getPartnerRequirementJsonObject(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("sex", partnerRequirement.getRequirementSex());
            jsonObject.put("age_lower", partnerRequirement.getAgeLower());
            jsonObject.put("age_upper", partnerRequirement.getAgeUpper());
            jsonObject.put("height", partnerRequirement.getRequirementHeight());
            jsonObject.put("degree", partnerRequirement.getDegreeIndex());
            jsonObject.put("lives", partnerRequirement.getRequirementLives());
            jsonObject.put("illustration", partnerRequirement.getIllustration());
        }catch (JSONException e){
            e.printStackTrace();
        }


        return jsonObject;
    }

    private boolean checkSelfInfo(){
        if(SELFSEXCHECKED == false){
            Toast.makeText(FillMeetInfoActivity.this, "请选择性别", Toast.LENGTH_LONG).show();
            return false;
        }

        if(BIRTHYEARSELECTED == false){
            Toast.makeText(FillMeetInfoActivity.this, "请选择出生年份", Toast.LENGTH_LONG).show();
            return false;
        }

        if(BIRTHMONTHSELECTED == false){
            Toast.makeText(FillMeetInfoActivity.this, "请选择出生月份", Toast.LENGTH_LONG).show();
            return false;
        }

        if(BIRTHDAYSELECTED == false){
            Toast.makeText(FillMeetInfoActivity.this, "请选择出生日", Toast.LENGTH_LONG).show();
            return false;
        }

        if(HEIGHTSELECTED == false){
            Toast.makeText(FillMeetInfoActivity.this, "请选择身高", Toast.LENGTH_LONG).show();
            return false;
        }

        if(DEGREESELECTED == false){
            Toast.makeText(FillMeetInfoActivity.this, "请选择学历", Toast.LENGTH_LONG).show();
            return false;
        }

        if(SELFLIVESELECTED == false){
            Toast.makeText(FillMeetInfoActivity.this, "请选择居住地", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private boolean checkRequiredInfo(){

        if(REQUIRESEXCHECKED == false){
            Toast.makeText(FillMeetInfoActivity.this, "请选择性别", Toast.LENGTH_LONG).show();
            return false;
        }

        if(LOWERAGESELECTED == false){
            Toast.makeText(FillMeetInfoActivity.this, "请选择年龄下限", Toast.LENGTH_LONG).show();
            return false;
        }

        if(UPPERAGESELECTED == false){
            Toast.makeText(FillMeetInfoActivity.this, "请选择年龄上限", Toast.LENGTH_LONG).show();
            return false;
        }

        if(REQUIREHEIGHTSELECTED == false){
            Toast.makeText(FillMeetInfoActivity.this, "请选择身高", Toast.LENGTH_LONG).show();
            return false;
        }

        if(REQUIREDEGREESELECTED == false){
            Toast.makeText(FillMeetInfoActivity.this, "请选择学历", Toast.LENGTH_LONG).show();
            return false;
        }

        if(REQUIRELIVESELECTED == false){
            Toast.makeText(FillMeetInfoActivity.this, "请选择居住地", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }
    private void createRequiredView(){

        RadioGroup require_sex = (RadioGroup)findViewById(R.id.require_sex);
        require_sex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.require_sex_male){
                    //Toast.makeText(FillMeetInfoActivity.this,"male", Toast.LENGTH_SHORT).show();
                    partnerRequirement.setRequirementSex(0);
                }else{
                    //Toast.makeText(FillMeetInfoActivity.this,"female", Toast.LENGTH_SHORT).show();
                    partnerRequirement.setRequirementSex(1);
                }
                REQUIRESEXCHECKED = true;
            }
        });

        final List<String> ageList = new LinkedList<>(Arrays.asList(ages));
        NiceSpinner niceSpinnerAgeLower = (NiceSpinner) findViewById(R.id.nice_spinner_require_age_lower);
        niceSpinnerAgeLower.attachDataSource(ageList);
        niceSpinnerAgeLower.addOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                //Toast.makeText(FillMeetInfoActivity.this, String.valueOf(ageList.get(i)), Toast.LENGTH_SHORT).show();
                partnerRequirement.setAgeLower(Integer.parseInt(ageList.get(i)));
                LOWERAGESELECTED = true;
            }

        });

        NiceSpinner niceSpinnerAgeUpper = (NiceSpinner) findViewById(R.id.nice_spinner_require_age_upper);
        niceSpinnerAgeUpper.attachDataSource(ageList);
        niceSpinnerAgeUpper.addOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                //Toast.makeText(FillMeetInfoActivity.this, String.valueOf(ageList.get(i)), Toast.LENGTH_SHORT).show();
                partnerRequirement.setAgeUpper(Integer.parseInt(ageList.get(i)));
                UPPERAGESELECTED = true;
            }

        });

        NiceSpinner niceSpinnerHeight = (NiceSpinner) findViewById(R.id.nice_spinner_require_height);
        final List<String> heightList = new LinkedList<>(Arrays.asList(heights));
        niceSpinnerHeight.attachDataSource(heightList);
        niceSpinnerHeight.addOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                //Toast.makeText(FillMeetInfoActivity.this, String.valueOf(heightList.get(i)), Toast.LENGTH_SHORT).show();
                partnerRequirement.setRequirementHeight(Integer.parseInt(heightList.get(i)));
                REQUIREHEIGHTSELECTED = true;
            }

        });

        NiceSpinner niceSpinnerDegree = (NiceSpinner) findViewById(R.id.nice_spinner_require_degree);
        final List<String> degreeList = new LinkedList<>(Arrays.asList(degrees));
        niceSpinnerDegree.attachDataSource(degreeList);
        niceSpinnerDegree.addOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                //Toast.makeText(FillMeetInfoActivity.this, String.valueOf(degreeList.get(i)), Toast.LENGTH_SHORT).show();
                partnerRequirement.setRequirementDegree(String.valueOf(degreeList.get(i)));
                REQUIREDEGREESELECTED = true;
            }

        });

        Button requireRegionSelection = (Button)findViewById(R.id.require_region);
        requireRegionSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FillMeetInfoActivity.this, RegionSelectionActivity.class);
                startActivityForResult(intent, REQUIREREQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         // RESULT_OK，判断另外一个activity已经结束数据输入功能，Standard activity result:
         // operation succeeded. 默认值是-1
         if (resultCode == 2) {
             String SelectedResult = data.getStringExtra("SelectedResult");
                 if (requestCode == SELFREQUEST) {
                     //设置结果显示框的显示数值
                     Button button = (Button)findViewById(R.id.self_region);
                     button.setText(SelectedResult);
                     selfCondition.setLives(SelectedResult);
                     SELFLIVESELECTED = true;

                 }else{
                     //设置结果显示框的显示数值
                     Button button = (Button)findViewById(R.id.require_region);
                     button.setText(SelectedResult);
                     partnerRequirement.setRequirementLives(SelectedResult);
                     REQUIRELIVESELECTED = true;
                 }
         }
    }


}
