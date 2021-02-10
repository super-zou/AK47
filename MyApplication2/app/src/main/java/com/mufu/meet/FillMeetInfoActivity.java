package com.mufu.meet;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.mufu.R;
import com.mufu.common.AddPictureActivity;
import com.mufu.common.BaseAppCompatActivity;
import com.mufu.common.MyApplication;
import com.mufu.util.CommonBean;
import com.mufu.util.CommonPickerView;
import com.mufu.util.FontManager;
import com.mufu.util.HttpUtil;
import com.mufu.util.SharedPreferencesUtils;
import com.mufu.util.Slog;
import com.mufu.util.UserProfile;

import org.angmarch.views.NiceSpinner;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FillMeetInfoActivity extends BaseAppCompatActivity {
    //private ArrayList<ArrayList<ArrayList<String>>> options3Items = new ArrayList<>();
    public static final String FILL_MEET_INFO_BROADCAST = "com.hetang.action.FILL_MEET_INFO";
    private static final String TAG = "FillMeetInfoActivity";
    private static final String FILL_MEET_INFO_URL = HttpUtil.DOMAIN + "?q=meet/look_friend";
    Resources res;
    String[] years;
    String[] constellations;
    String[] hometown;
    String[] nations;
    String[] religions;
    String[] heights;
    String[] degrees;
    String[] ages;
    SelfCondition selfCondition;
    PartnerRequirement partnerRequirement;
    boolean DEGREESELECTED = false;
    boolean BIRTHYEARSELECTED = false;
    boolean CONSTELLATIONSELECTED = false;
    boolean HOMETOWNSELECTED = false;
    boolean NATIONSELECTED = false;
    boolean RELIGIONSELECTED = false;
    boolean HEIGHTSELECTED = false;
    boolean INDUSTRYSELECTED = false;
    boolean LOWERAGESELECTED = false;
    boolean UPPERAGESELECTED = false;
    boolean REQUIREHEIGHTSELECTED = false;
    boolean REQUIREDEGREESELECTED = false;
    boolean REQUIRELIVESELECTED = false;
    String selfConditionJson;
    String partnerRequirementJson;
    int currentPageIndex = 1;
    int defaultPageCount = 2;
    ConstraintLayout fillMeetInfoLayout;
    ConstraintLayout customActionBar;
    LinearLayout setAvatar;
    LinearLayout pageIndicator;
    LinearLayout situationLayout;
    Button requireRegionSelection;
    TextView leftBack;
    TextView title;
    Button prev;
    Button next;
    boolean isHometownSet = false;
    boolean isEndPage = false;
    private int mSex = -1;
    private String selfHometown = "";
    private ArrayList<CommonBean> industryMainItems = new ArrayList<>();
    private ArrayList<ArrayList<String>> industrySubItems = new ArrayList<>();
    private ArrayList<CommonBean> provinceItems = new ArrayList<>();
    private ArrayList<ArrayList<String>> cityItems = new ArrayList<>();
    private UserProfile userProfile;
    private Thread threadCity;
        private EditText myUniversityET;
    private EditText myMajorET;
    private EditText illustrationET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fill_meet_info);
        //if avatar is set, avatarSet will be set true
        userProfile = (UserProfile) getIntent().getSerializableExtra("userProfile");

        Slog.d(TAG, "-------------------userProfile sex: " + userProfile.getSex());
        if (userProfile != null) {
            mSex = userProfile.getSex();
            selfHometown = userProfile.getHometown();
        }

        if (mSex == -1) {
            mSex = SharedPreferencesUtils.getLoginedAccountSex(MyApplication.getContext());
        }

        if (!TextUtils.isEmpty(selfHometown)) {
            isHometownSet = true;
        }

        initView();

        setSelfCondition();

        setRequirement();

        pageNavigator();


    }

    private void initView() {
        leftBack = findViewById(R.id.left_back);
        customActionBar = findViewById(R.id.custom_actionbar_id);
        title = findViewById(R.id.title);
        fillMeetInfoLayout = findViewById(R.id.fill_meet_info);

        requireRegionSelection = findViewById(R.id.require_region);

        setAvatar = findViewById(R.id.set_avatar);
        pageIndicator = findViewById(R.id.page_indicator);
        prev = findViewById(R.id.prev);
        next = findViewById(R.id.next);

        //add indicator
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        for (int i = 0; i < defaultPageCount; i++) {
            TextView textView = new TextView(this);

            if (i != 0) {
                layoutParams.setMarginStart(8);
                textView.setText(R.string.circle_o);
            } else {
                textView.setText(R.string.circle);
            }
            textView.setTextColor(getResources().getColor(R.color.blue_dark));
            textView.setLayoutParams(layoutParams);

            pageIndicator.addView(textView);
        }


        leftBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.fill_meet_info), font);
    }

    private void setSelfCondition() {

        title.setText(getResources().getText(R.string.self_info));
        res = getResources();
        years = res.getStringArray(R.array.years);
        constellations = res.getStringArray(R.array.constellations);
        hometown = res.getStringArray(R.array.hometown);
        nations = res.getStringArray(R.array.nations);
        religions = res.getStringArray(R.array.religions);
        heights = res.getStringArray(R.array.heights);
        degrees = res.getStringArray(R.array.degrees);
        ages = res.getStringArray(R.array.ages);

        selfCondition = new SelfCondition();
        partnerRequirement = new PartnerRequirement();
        
                RadioGroup mySex = findViewById(R.id.my_sex);
        RadioButton sexRadioButton;
        if (mSex != -1) {
            if (mSex == 0) {
                sexRadioButton = (RadioButton) mySex.getChildAt(0);
                selfCondition.setSelfSex(0);
            } else {
                sexRadioButton = (RadioButton) mySex.getChildAt(1);
                selfCondition.setSelfSex(1);
            }

            sexRadioButton.setChecked(true);
        }
        
                mySex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.sex_male) {
                    selfCondition.setSelfSex(0);
                } else {
                    selfCondition.setSelfSex(1);
                }
            }
        });
        
                NiceSpinner niceSpinnerDegree = findViewById(R.id.nice_spinner_degree);
        final List<String> degreeList = new LinkedList<>(Arrays.asList(degrees));
        niceSpinnerDegree.attachDataSource(degreeList);
        niceSpinnerDegree.addOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selfCondition.setDegree(String.valueOf(degreeList.get(i)));
                if (i != 0){
                    DEGREESELECTED = true;
                }
            }

        });
        
                myUniversityET = findViewById(R.id.my_university);
        if (!TextUtils.isEmpty(userProfile.getUniversity())){
            myUniversityET.setText(userProfile.getUniversity());
        }

        myMajorET = findViewById(R.id.my_major);
        if (!TextUtils.isEmpty(userProfile.getMajor())){
            myMajorET.setText(userProfile.getMajor());
        }

        NiceSpinner niceSpinnerYears = (NiceSpinner) findViewById(R.id.nice_spinner_years);
        final List<String> yearList = new LinkedList<>(Arrays.asList(years));
        niceSpinnerYears.attachDataSource(yearList);
        niceSpinnerYears.addOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                //Toast.makeText(FillMeetInfoActivity.this, "年份"+String.valueOf(yearList.get(i)), Toast.LENGTH_SHORT).show();
                selfCondition.setBirthYear(Integer.parseInt(yearList.get(i)));
                BIRTHYEARSELECTED = true;
            }

        });

        NiceSpinner niceSpinnerConstellation = findViewById(R.id.nice_spinner_constellations);
        final List<String> constellationList = new LinkedList<>(Arrays.asList(constellations));
        niceSpinnerConstellation.attachDataSource(constellationList);
        niceSpinnerConstellation.addOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                //Toast.makeText(FillMeetInfoActivity.this, String.valueOf(monthList.get(i)), Toast.LENGTH_SHORT).show();
                selfCondition.setConstellation(String.valueOf(constellationList.get(i)));
                CONSTELLATIONSELECTED = true;

            }

        });

        NiceSpinner niceSpinnerHometown = (NiceSpinner) findViewById(R.id.nice_spinner_hometown);

        final List<String> hometownList = new LinkedList<>(Arrays.asList(hometown));
        niceSpinnerHometown.attachDataSource(hometownList);
        niceSpinnerHometown.addOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                //Toast.makeText(FillMeetInfoActivity.this, String.valueOf(dayList.get(i)), Toast.LENGTH_SHORT).show();
                selfCondition.setHometown(String.valueOf(hometownList.get(i)));
                HOMETOWNSELECTED = true;
            }

        });

        if (isHometownSet == true) {
            niceSpinnerHometown.setText(selfHometown);
            selfCondition.setHometown(selfHometown);
            HOMETOWNSELECTED = true;
        }

        NiceSpinner niceSpinnerNation = (NiceSpinner) findViewById(R.id.nice_spinner_nation);
        final List<String> nationList = new LinkedList<>(Arrays.asList(nations));
        niceSpinnerNation.attachDataSource(nationList);
        selfCondition.setNation(String.valueOf(nationList.get(0)));
        niceSpinnerNation.addOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                //Toast.makeText(FillMeetInfoActivity.this, String.valueOf(dayList.get(i)), Toast.LENGTH_SHORT).show();
                selfCondition.setNation(String.valueOf(nationList.get(i)));
                NATIONSELECTED = true;
            }

        });

        NiceSpinner niceSpinnerReligion = (NiceSpinner) findViewById(R.id.nice_spinner_religion);
        final List<String> religionList = new LinkedList<>(Arrays.asList(religions));
        niceSpinnerReligion.attachDataSource(religionList);
        selfCondition.setReligion(String.valueOf(religionList.get(0)));
        niceSpinnerReligion.addOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                //Toast.makeText(FillMeetInfoActivity.this, String.valueOf(dayList.get(i)), Toast.LENGTH_SHORT).show();
                selfCondition.setReligion(String.valueOf(religionList.get(i)));
                RELIGIONSELECTED = true;
            }

        });

        NiceSpinner niceSpinnerHeight = (NiceSpinner) findViewById(R.id.nice_spinner_height);
        final List<String> heightList = new LinkedList<>(Arrays.asList(heights));
        niceSpinnerHeight.attachDataSource(heightList);
        niceSpinnerHeight.addOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                //Toast.makeText(FillMeetInfoActivity.this, String.valueOf(heightList.get(i)), Toast.LENGTH_SHORT).show();
                selfCondition.setHeight(Integer.parseInt(heightList.get(i)));
                HEIGHTSELECTED = true;
            }
        });

    }

    private void pageNavigator() {
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //customActionBar.setVisibility(View.INVISIBLE);

                if (currentPageIndex == defaultPageCount) {
                    if (checkRequiredInfo()) {
                        saveMeetInfo();
                    }
                    return;
                }

                if (currentPageIndex == defaultPageCount - 1) {
                    if (!checkSelfInfo()) {
                        return;
                    }
                }

                leftBack.setVisibility(View.INVISIBLE);

                fillMeetInfoLayout.getChildAt(currentPageIndex).setVisibility(View.GONE);
                TextView preIndicator = (TextView) pageIndicator.getChildAt(currentPageIndex - 1);
                preIndicator.setText(R.string.circle_o);
                //if (checkSelfInfo()) {
                ++currentPageIndex;

                fillMeetInfoLayout.getChildAt(currentPageIndex).setVisibility(View.VISIBLE);
                TextView indicator = (TextView) pageIndicator.getChildAt(currentPageIndex - 1);
                indicator.setText(R.string.circle);

                if (prev.getVisibility() == View.INVISIBLE) {
                    prev.setVisibility(View.VISIBLE);
                }

                if (currentPageIndex == defaultPageCount) {
                    title.setText(getResources().getText(R.string.requirement));
                    next.setText(R.string.done);
                    isEndPage = true;
                }

            }

        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fillMeetInfoLayout.getChildAt(currentPageIndex).setVisibility(View.GONE);
                TextView nextIndicator = (TextView) pageIndicator.getChildAt(currentPageIndex - 1);
                nextIndicator.setText(R.string.circle_o);
                --currentPageIndex;

                fillMeetInfoLayout.getChildAt(currentPageIndex).setVisibility(View.VISIBLE);

                TextView indicator = (TextView) pageIndicator.getChildAt(currentPageIndex - 1);
                indicator.setText(R.string.circle);

                next.setText(R.string.next);
                title.setText(getResources().getText(R.string.self_info));

                if (currentPageIndex == 1) {
                    prev.setVisibility(View.INVISIBLE);
                    if (customActionBar.getVisibility() == View.INVISIBLE) {
                        customActionBar.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    private void setRequirement() {

                illustrationET = findViewById(R.id.illustration);
        RadioGroup require_sex = findViewById(R.id.require_sex);
        RadioButton sexRadioButton;
        if (mSex != -1) {
            if (mSex == 0) {
                sexRadioButton = (RadioButton) require_sex.getChildAt(1);
                partnerRequirement.setRequirementSex(1);
            } else {
                sexRadioButton = (RadioButton) require_sex.getChildAt(0);
                partnerRequirement.setRequirementSex(0);
            }

            sexRadioButton.setChecked(true);
        }


        require_sex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.require_sex_male) {
                    //Toast.makeText(FillMeetInfoActivity.this,"male", Toast.LENGTH_SHORT).show();
                    partnerRequirement.setRequirementSex(0);
                } else {
                    //Toast.makeText(FillMeetInfoActivity.this,"female", Toast.LENGTH_SHORT).show();
                    partnerRequirement.setRequirementSex(1);
                }
            }
        });

        final List<String> ageList = new LinkedList<>(Arrays.asList(ages));
        NiceSpinner niceSpinnerAgeLower = (NiceSpinner) findViewById(R.id.nice_spinner_require_age_lower);
        niceSpinnerAgeLower.attachDataSource(ageList);
        niceSpinnerAgeLower.addOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        niceSpinnerAgeUpper.addOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        niceSpinnerHeight.addOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        niceSpinnerDegree.addOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                //Toast.makeText(FillMeetInfoActivity.this, String.valueOf(degreeList.get(i)), Toast.LENGTH_SHORT).show();
                partnerRequirement.setRequirementDegree(String.valueOf(degreeList.get(i)));
                REQUIREDEGREESELECTED = true;
            }

        });

        if (threadCity == null) {
            Slog.i(TAG, "city数据开始解析");
            threadCity = new Thread(new Runnable() {
                @Override
                public void run() {
                    initJsondata("city.json");
                }
            });
            threadCity.start();
        }

        requireRegionSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPickerView();
            }
        });
    }

    private void initJsondata(String jsonFile) {
        CommonPickerView commonPickerView = new CommonPickerView();
        provinceItems = commonPickerView.getOptionsMainItem(this, jsonFile);
        cityItems = commonPickerView.getOptionsSubItems(provinceItems);
    }

    private void showPickerView() {// 弹出行业选择器
        //条件选择器
        OptionsPickerView pvOptions = new OptionsPickerBuilder(this, new OnOptionsSelectListener() {

            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                //返回的分别是二个级别的选中位置
                String tx = provinceItems.get(options1).getPickerViewText() + " · "
                        + cityItems.get(options1).get(option2);
                requireRegionSelection.setText(tx);
                String city = cityItems.get(options1).get(option2);
                partnerRequirement.setRequirementLiving(city);
                REQUIRELIVESELECTED = true;

            }
        }).build();

        pvOptions.setPicker(provinceItems, cityItems);
        pvOptions.show();
    }


    private void saveMeetInfo() {

        if (!TextUtils.isEmpty(illustrationET.getText().toString())) {
            partnerRequirement.setIllustration(illustrationET.getText().toString());
        }

        selfConditionJson = getSelfConditionJsonObject().toString();
        Slog.d(TAG, "=====================selfConditionJson: " + selfConditionJson);
        partnerRequirementJson = getPartnerRequirementJsonObject().toString();
        showProgressDialog("正在保存");
        RequestBody requestBody = new FormBody.Builder()
                .add("self_condition", selfConditionJson)
                .add("partner_requirement", partnerRequirementJson)
                .build();

        HttpUtil.sendOkHttpRequest(FillMeetInfoActivity.this, FILL_MEET_INFO_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Intent intent = new Intent(getApplicationContext(), AddPictureActivity.class);
                intent.putExtra("uid", userProfile.getUid());
                startActivity(intent);
                dismissProgressDialog();
                sendBroadcast();
                finish();
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });

    }

    private JSONObject getSelfConditionJsonObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            if (userProfile.getSex() != selfCondition.getSelfSex()){
                jsonObject.put("sex", selfCondition.getSelfSex());
            }
            jsonObject.put("degree", selfCondition.getDegreeIndex(selfCondition.getDegree()));
            if (!userProfile.getUniversity().equals(selfCondition.getUniversity())){
                jsonObject.put("university", selfCondition.getUniversity());
            }

            jsonObject.put("major", myMajorET.getText().toString());
            
            jsonObject.put("birth_year", selfCondition.getBirthYear());
            jsonObject.put("constellation", selfCondition.getConstellation());
            jsonObject.put("height", selfCondition.getHeight());

            jsonObject.put("hometown", selfCondition.getHometown());
            jsonObject.put("nation", selfCondition.getNation());
            jsonObject.put("religion", selfCondition.getReligion());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    private JSONObject getPartnerRequirementJsonObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("sex", partnerRequirement.getRequirementSex());
            jsonObject.put("age_lower", partnerRequirement.getAgeLower());
            jsonObject.put("age_upper", partnerRequirement.getAgeUpper());
            jsonObject.put("height", partnerRequirement.getRequirementHeight());
            jsonObject.put("degree", partnerRequirement.getDegreeIndex());
            jsonObject.put("living", partnerRequirement.getRequirementLiving());
            jsonObject.put("illustration", partnerRequirement.getIllustration());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    private boolean checkSelfInfo() {
        
        if (DEGREESELECTED == false){
            Toast.makeText(FillMeetInfoActivity.this, "请选择学历", Toast.LENGTH_LONG).show();
            return false;
        }

        if (TextUtils.isEmpty(myUniversityET.getText().toString())){
            Toast.makeText(FillMeetInfoActivity.this, "请输入您的学校", Toast.LENGTH_LONG).show();
            return false;
        }

        if (TextUtils.isEmpty(myMajorET.getText().toString())){
            Toast.makeText(FillMeetInfoActivity.this, "请输入您的专业", Toast.LENGTH_LONG).show();
            return false;
        }

        if (BIRTHYEARSELECTED == false) {
            Toast.makeText(FillMeetInfoActivity.this, "请选择出生年份", Toast.LENGTH_LONG).show();
            return false;
        }

        if (CONSTELLATIONSELECTED == false) {
            Toast.makeText(FillMeetInfoActivity.this, "请选择星座", Toast.LENGTH_LONG).show();
            return false;
        }

        if (HOMETOWNSELECTED == false) {
            Toast.makeText(FillMeetInfoActivity.this, "请选择家乡", Toast.LENGTH_LONG).show();
            return false;
        }

        if (HEIGHTSELECTED == false) {
            Toast.makeText(FillMeetInfoActivity.this, "请选择身高", Toast.LENGTH_LONG).show();
            return false;
        }
        
        return true;
    }

    private boolean checkRequiredInfo() {

        if (LOWERAGESELECTED == false) {
            Toast.makeText(FillMeetInfoActivity.this, "请选择年龄下限", Toast.LENGTH_LONG).show();
            return false;
        }

        if (UPPERAGESELECTED == false) {
            Toast.makeText(FillMeetInfoActivity.this, "请选择年龄上限", Toast.LENGTH_LONG).show();
            return false;
        }

        if (REQUIREHEIGHTSELECTED == false) {
            Toast.makeText(FillMeetInfoActivity.this, "请选择身高", Toast.LENGTH_LONG).show();
            return false;
        }

        if (REQUIREDEGREESELECTED == false) {
            Toast.makeText(FillMeetInfoActivity.this, "请选择学历", Toast.LENGTH_LONG).show();
            return false;
        }

        if (REQUIRELIVESELECTED == false) {
            Toast.makeText(FillMeetInfoActivity.this, "请选择居住地", Toast.LENGTH_LONG).show();
            return false;
        }
        
        if (TextUtils.isEmpty(illustrationET.getText().toString())){
            Toast.makeText(FillMeetInfoActivity.this, "请概要介绍一下自己和交友要求", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void sendBroadcast() {
        Intent intent;
        intent = new Intent(FILL_MEET_INFO_BROADCAST);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
