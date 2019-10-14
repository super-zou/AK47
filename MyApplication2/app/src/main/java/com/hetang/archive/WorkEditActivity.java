package com.hetang.archive;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.hetang.R;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.util.CommonBean;
import com.hetang.util.CommonPickerView;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.Slog;
import com.hetang.common.MyApplication;

import org.angmarch.views.NiceSpinner;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hetang.main.ArchiveFragment.SET_WORK_RESULT_OK;

public class WorkEditActivity extends BaseAppCompatActivity {
    private static final String TAG = "WorkEditActivity";
    private Context mContext;
    private TextView title;
    private TextView save;
    private boolean entranceSlected = false;
    private boolean leaveSlected = false;
    private boolean stillNow = false;
    private Thread threadIndustry = null;
    private WorkExperience workExperience;
    private ArrayList<CommonBean> industryMainItems = new ArrayList<>();
    private ArrayList<ArrayList<String>> industrySubItems = new ArrayList<>();
    private static final String CREATE_WORK_EXPERIENCE_URL = HttpUtil.DOMAIN + "?q=personal_archive/work_experience/create";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.work_experience_edit);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        initView();
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.work_experience_edit), font);
    }
    
    private void initView() {

        save = findViewById(R.id.save);
        save.setVisibility(View.VISIBLE);
        TextView back = findViewById(R.id.left_back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        initIndustryJsondata();
        
        final TextInputEditText industryEdit = findViewById(R.id.industry_edit);
        final TextInputEditText companyEdit = findViewById(R.id.company_edit);
        final TextInputEditText positionEdit = findViewById(R.id.position_edit);
        CheckBox now = findViewById(R.id.now);
        workExperience = new WorkExperience();
        String[] entranceYears = getResources().getStringArray(R.array.entrance_years);
        String[] leaveYears = getResources().getStringArray(R.array.graduate_years);
        industryEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b == true){
                    InputMethodManager imm = (InputMethodManager) MyApplication.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(industryEdit.getWindowToken(),0);
                    ShowPickerView(industryEdit);
                }
                }
        });
        final List<String> entranceYearList = new LinkedList<>(Arrays.asList(entranceYears));
        NiceSpinner niceSpinnerEntranceYears = (NiceSpinner) findViewById(R.id.entrance_year);
        niceSpinnerEntranceYears.attachDataSource(entranceYearList);
        niceSpinnerEntranceYears.addOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                workExperience.entranceYear = Integer.parseInt(entranceYearList.get(i));
                entranceSlected = true;
            }

        });
        
        final List<String> leaveYearList = new LinkedList<>(Arrays.asList(leaveYears));
        NiceSpinner niceSpinnerGraduateYears = (NiceSpinner) findViewById(R.id.leave_year);
        niceSpinnerGraduateYears.attachDataSource(leaveYearList);
        niceSpinnerGraduateYears.addOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                workExperience.leaveYear = Integer.parseInt(leaveYearList.get(i));
                leaveSlected = true;
            }

        });
        
        now.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    workExperience.now = 1;
                    stillNow = true;
                }else {
                    workExperience.now = 0;
                    stillNow = false;
                }
            }
        });
        
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(industryEdit.getText().toString())){
                    workExperience.industry = industryEdit.getText().toString();
                }else {
                    Toast.makeText(MyApplication.getContext(), "请输入行业", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!TextUtils.isEmpty(companyEdit.getText().toString())){
                    workExperience.company = companyEdit.getText().toString();
                }else {
                    Toast.makeText(MyApplication.getContext(), "请输入公司", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!TextUtils.isEmpty(positionEdit.getText().toString())){
                    workExperience.position = positionEdit.getText().toString();
                }else {
                    Toast.makeText(MyApplication.getContext(), "请输入职位", Toast.LENGTH_LONG).show();
                    return;
                }

                if(!entranceSlected){
                    Toast.makeText(MyApplication.getContext(), "请选择入职时间", Toast.LENGTH_LONG).show();
                    return;
                }
                if(!stillNow && !leaveSlected){
                    Toast.makeText(MyApplication.getContext(), "请选择离职时间", Toast.LENGTH_LONG).show();
                    return;
                }

                String workExperienceString = getWorkExperienceJsonObject(workExperience).toString();
                uploadToServer(workExperienceString);
            }
        });
    }
    
    private void uploadToServer(String workExperienceString) {
        Slog.d(TAG, "----------------------->workExperienceString: "+workExperienceString);
        showProgressDialog(getString(R.string.saving_progress));

        RequestBody requestBody = new FormBody.Builder()
                .add("workExperience", workExperienceString).build();
        HttpUtil.sendOkHttpRequest(MyApplication.getContext(), CREATE_WORK_EXPERIENCE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "================uploadToServer response:" + responseText);
                if(!TextUtils.isEmpty(responseText)){
                    Intent intent = getIntent();
                    setResult(SET_WORK_RESULT_OK, intent);
                    finish();
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {  }
        });

    }

    private void initIndustryJsondata(){

        final CommonPickerView commonPickerView = new CommonPickerView();
        if (threadIndustry == null){
            Slog.i(TAG,"行业数据开始解析");
            threadIndustry = new Thread(new Runnable() {
                @Override
                public void run() {
                    industryMainItems = commonPickerView.getOptionsMainItem(MyApplication.getContext(), "industry.json");
                    industrySubItems = commonPickerView.getOptionsSubItems(industryMainItems);
                }
            });
            threadIndustry.start();
        }
    }

    private void ShowPickerView(final TextInputEditText industryEdit) {// 弹出行业选择器
    
    //条件选择器
        OptionsPickerView pvOptions;

        pvOptions= new OptionsPickerBuilder(this, new OnOptionsSelectListener() {

            @Override
            public void onOptionsSelect(int options1, int option2, int options3 ,View v) {
                //返回的分别是二个级别的选中位置
                String tx = industryMainItems.get(options1).getPickerViewText()
                        + industrySubItems.get(options1).get(option2);
                industryEdit.setText(industrySubItems.get(options1).get(option2));
                workExperience.industry = industrySubItems.get(options1).get(option2);

            }
        }).build();

        pvOptions.setPicker(industryMainItems, industrySubItems);


        pvOptions.show();

    }
    
     private JSONObject getWorkExperienceJsonObject(WorkExperience workExperience) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("industry", workExperience.industry);
            jsonObject.put("company", workExperience.company);
            jsonObject.put("position", workExperience.position);
            jsonObject.put("entrance_year", workExperience.entranceYear);
            jsonObject.put("leave_year", workExperience.leaveYear);
            jsonObject.put("now", workExperience.now);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
    
    class WorkExperience{
        String industry;
        String company;
        String position;
        int entranceYear;
        int leaveYear;
        int now;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();
    }

}
