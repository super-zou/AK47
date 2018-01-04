package com.tongmenhui.launchak47.meet;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.util.Slog;

import org.angmarch.views.NiceSpinner;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class FillMeetInfoActivity extends AppCompatActivity {
    Resources res;
    String[] years;
    String[] months;
    String[] days;
    String[] heights;
    String[] degrees;
    String[] regions;
    String[] provinces;
    String[] cities;
    String[] ages;

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
        regions = res.getStringArray(R.array.regions);
        provinces = res.getStringArray(R.array.provinces);
        cities = res.getStringArray(R.array.cities);
        ages = res.getStringArray(R.array.ages);

        final LinearLayout selfLayout = (LinearLayout)findViewById(R.id.self);
        final LinearLayout requireLayout = (LinearLayout)findViewById(R.id.requirement);


        NiceSpinner niceSpinnerYears = (NiceSpinner) findViewById(R.id.nice_spinner_years);
        final List<String> yearList = new LinkedList<>(Arrays.asList(years));
        niceSpinnerYears.attachDataSource(yearList);
        niceSpinnerYears.addOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                Toast.makeText(FillMeetInfoActivity.this, String.valueOf(yearList.get(i)), Toast.LENGTH_SHORT).show();
            }

        });

        NiceSpinner niceSpinnerMonths = (NiceSpinner) findViewById(R.id.nice_spinner_months);
        List<String> monthList = new LinkedList<>(Arrays.asList(months));
        niceSpinnerMonths.attachDataSource(monthList);
        niceSpinnerYears.addOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                Toast.makeText(FillMeetInfoActivity.this, String.valueOf(yearList.get(i)), Toast.LENGTH_SHORT).show();
            }

        });

        NiceSpinner niceSpinnerDays = (NiceSpinner) findViewById(R.id.nice_spinner_days);
        List<String> dayList = new LinkedList<>(Arrays.asList(days));
        niceSpinnerDays.attachDataSource(dayList);
        niceSpinnerYears.addOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                Toast.makeText(FillMeetInfoActivity.this, String.valueOf(yearList.get(i)), Toast.LENGTH_SHORT).show();
            }

        });

        NiceSpinner niceSpinnerHeight = (NiceSpinner) findViewById(R.id.nice_spinner_height);
        List<String> heightList = new LinkedList<>(Arrays.asList(heights));
        niceSpinnerHeight.attachDataSource(heightList);
        niceSpinnerYears.addOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                Toast.makeText(FillMeetInfoActivity.this, String.valueOf(yearList.get(i)), Toast.LENGTH_SHORT).show();
            }

        });

        NiceSpinner niceSpinnerDegree = (NiceSpinner) findViewById(R.id.nice_spinner_degree);
        List<String> degreeList = new LinkedList<>(Arrays.asList(degrees));
        niceSpinnerDegree.attachDataSource(degreeList);
        niceSpinnerYears.addOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                Toast.makeText(FillMeetInfoActivity.this, String.valueOf(yearList.get(i)), Toast.LENGTH_SHORT).show();
            }

        });
        /*
        NiceSpinner niceSpinnerRegion = (NiceSpinner) findViewById(R.id.nice_spinner_region);
        List<String> regionList = new LinkedList<>(Arrays.asList(regions));
        niceSpinnerRegion.attachDataSource(regionList);
        niceSpinnerYears.addOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                Toast.makeText(FillMeetInfoActivity.this, String.valueOf(yearList.get(i)), Toast.LENGTH_SHORT).show();
            }

        });

        NiceSpinner niceSpinnerProvince = (NiceSpinner) findViewById(R.id.nice_spinner_province);
        List<String> provinceList = new LinkedList<>(Arrays.asList(provinces));
        niceSpinnerProvince.attachDataSource(provinceList);
        niceSpinnerYears.addOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                Toast.makeText(FillMeetInfoActivity.this, String.valueOf(yearList.get(i)), Toast.LENGTH_SHORT).show();
            }

        });

        NiceSpinner niceSpinnerCity = (NiceSpinner) findViewById(R.id.nice_spinner_city);
        List<String> cityList = new LinkedList<>(Arrays.asList(cities));
        niceSpinnerCity.attachDataSource(cityList);
        niceSpinnerYears.addOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                Toast.makeText(FillMeetInfoActivity.this, String.valueOf(yearList.get(i)), Toast.LENGTH_SHORT).show();
            }

        });
        */

        final Button preButton = (Button)findViewById(R.id.prev);
        final Button nextButton = (Button) findViewById(R.id.next);
        final Button doneButton = (Button)findViewById(R.id.done);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selfLayout.setVisibility(View.GONE);
                requireLayout.setVisibility(View.VISIBLE);
                createRequireView();
                v.setVisibility(View.GONE);
                preButton.setVisibility(View.VISIBLE);
                doneButton.setVisibility(View.VISIBLE);
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

        RadioGroup self_sex = (RadioGroup)findViewById(R.id.self_sex);
        self_sex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.self_sex_male){
                    Toast.makeText(FillMeetInfoActivity.this,"male", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(FillMeetInfoActivity.this,"female", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void createRequireView(){

        final List<String> ageList = new LinkedList<>(Arrays.asList(ages));
        NiceSpinner niceSpinnerAgeLower = (NiceSpinner) findViewById(R.id.nice_spinner_require_age_lower);
        niceSpinnerAgeLower.attachDataSource(ageList);
        niceSpinnerAgeLower.addOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                Toast.makeText(FillMeetInfoActivity.this, String.valueOf(ageList.get(i)), Toast.LENGTH_SHORT).show();
            }

        });

        NiceSpinner niceSpinnerAgeupper = (NiceSpinner) findViewById(R.id.nice_spinner_require_age_upper);
        niceSpinnerAgeupper.attachDataSource(ageList);
        niceSpinnerAgeupper.addOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                Toast.makeText(FillMeetInfoActivity.this, String.valueOf(ageList.get(i)), Toast.LENGTH_SHORT).show();
            }

        });

        NiceSpinner niceSpinnerHeight = (NiceSpinner) findViewById(R.id.nice_spinner_require_height);
        final List<String> heightList = new LinkedList<>(Arrays.asList(heights));
        niceSpinnerHeight.attachDataSource(heightList);
        niceSpinnerHeight.addOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                Toast.makeText(FillMeetInfoActivity.this, String.valueOf(heightList.get(i)), Toast.LENGTH_SHORT).show();
            }

        });

        NiceSpinner niceSpinnerDegree = (NiceSpinner) findViewById(R.id.nice_spinner_require_degree);
        final List<String> degreeList = new LinkedList<>(Arrays.asList(degrees));
        niceSpinnerDegree.attachDataSource(degreeList);
        niceSpinnerDegree.addOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                Toast.makeText(FillMeetInfoActivity.this, String.valueOf(degreeList.get(i)), Toast.LENGTH_SHORT).show();
            }

        });

/*
        NiceSpinner niceSpinnerRegion = (NiceSpinner) findViewById(R.id.nice_spinner_require_region);
        final List<String> regionList = new LinkedList<>(Arrays.asList(regions));
        niceSpinnerRegion.attachDataSource(regionList);
        niceSpinnerRegion.addOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                Toast.makeText(FillMeetInfoActivity.this, String.valueOf(regionList.get(i)), Toast.LENGTH_SHORT).show();
            }

        });

        NiceSpinner niceSpinnerProvince = (NiceSpinner) findViewById(R.id.nice_spinner_require_province);
        final List<String> provinceList = new LinkedList<>(Arrays.asList(provinces));
        niceSpinnerProvince.attachDataSource(provinceList);
        niceSpinnerProvince.addOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                Toast.makeText(FillMeetInfoActivity.this, String.valueOf(provinceList.get(i)), Toast.LENGTH_SHORT).show();
            }

        });

        NiceSpinner niceSpinnerCity = (NiceSpinner) findViewById(R.id.nice_spinner_require_city);
        final List<String> cityList = new LinkedList<>(Arrays.asList(cities));
        niceSpinnerCity.attachDataSource(cityList);
        niceSpinnerCity.addOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                Toast.makeText(FillMeetInfoActivity.this, String.valueOf(cityList.get(i)), Toast.LENGTH_SHORT).show();
            }

        });
        */

        RadioGroup require_sex = (RadioGroup)findViewById(R.id.require_sex);
        require_sex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.require_sex_male){
                    Toast.makeText(FillMeetInfoActivity.this,"male", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(FillMeetInfoActivity.this,"female", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
