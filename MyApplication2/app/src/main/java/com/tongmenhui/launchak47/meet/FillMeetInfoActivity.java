package com.tongmenhui.launchak47.meet;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.tongmenhui.launchak47.R;

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
        List<String> yearList = new LinkedList<>(Arrays.asList(years));
        niceSpinnerYears.attachDataSource(yearList);

        NiceSpinner niceSpinnerMonths = (NiceSpinner) findViewById(R.id.nice_spinner_months);
        List<String> monthList = new LinkedList<>(Arrays.asList(months));
        niceSpinnerMonths.attachDataSource(monthList);

        NiceSpinner niceSpinnerDays = (NiceSpinner) findViewById(R.id.nice_spinner_days);
        List<String> dayList = new LinkedList<>(Arrays.asList(days));
        niceSpinnerDays.attachDataSource(dayList);

        NiceSpinner niceSpinnerHeight = (NiceSpinner) findViewById(R.id.nice_spinner_height);
        List<String> heightList = new LinkedList<>(Arrays.asList(heights));
        niceSpinnerHeight.attachDataSource(heightList);

        NiceSpinner niceSpinnerDegree = (NiceSpinner) findViewById(R.id.nice_spinner_degree);
        List<String> degreeList = new LinkedList<>(Arrays.asList(degrees));
        niceSpinnerDegree.attachDataSource(degreeList);

        NiceSpinner niceSpinnerRegion = (NiceSpinner) findViewById(R.id.nice_spinner_region);
        List<String> regionList = new LinkedList<>(Arrays.asList(regions));
        niceSpinnerRegion.attachDataSource(regionList);

        NiceSpinner niceSpinnerProvince = (NiceSpinner) findViewById(R.id.nice_spinner_province);
        List<String> provinceList = new LinkedList<>(Arrays.asList(provinces));
        niceSpinnerProvince.attachDataSource(provinceList);

        NiceSpinner niceSpinnerCity = (NiceSpinner) findViewById(R.id.nice_spinner_city);
        List<String> cityList = new LinkedList<>(Arrays.asList(cities));
        niceSpinnerCity.attachDataSource(cityList);

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
    }
    private void createRequireView(){

        List<String> ageList = new LinkedList<>(Arrays.asList(ages));
        NiceSpinner niceSpinnerAgeLower = (NiceSpinner) findViewById(R.id.nice_spinner_require_age_lower);
        niceSpinnerAgeLower.attachDataSource(ageList);

        NiceSpinner niceSpinnerAgeupper = (NiceSpinner) findViewById(R.id.nice_spinner_require_age_upper);
        niceSpinnerAgeupper.attachDataSource(ageList);

        NiceSpinner niceSpinnerHeight = (NiceSpinner) findViewById(R.id.nice_spinner_require_height);
        List<String> heightList = new LinkedList<>(Arrays.asList(heights));
        niceSpinnerHeight.attachDataSource(heightList);

        NiceSpinner niceSpinnerDegree = (NiceSpinner) findViewById(R.id.nice_spinner_require_degree);
        List<String> degreeList = new LinkedList<>(Arrays.asList(degrees));
        niceSpinnerDegree.attachDataSource(degreeList);

        NiceSpinner niceSpinnerRegion = (NiceSpinner) findViewById(R.id.nice_spinner_require_region);
        List<String> regionList = new LinkedList<>(Arrays.asList(regions));
        niceSpinnerRegion.attachDataSource(regionList);

        NiceSpinner niceSpinnerProvince = (NiceSpinner) findViewById(R.id.nice_spinner_require_province);
        List<String> provinceList = new LinkedList<>(Arrays.asList(provinces));
        niceSpinnerProvince.attachDataSource(provinceList);

        NiceSpinner niceSpinnerCity = (NiceSpinner) findViewById(R.id.nice_spinner_require_city);
        List<String> cityList = new LinkedList<>(Arrays.asList(cities));
        niceSpinnerCity.attachDataSource(cityList);
    }

}
