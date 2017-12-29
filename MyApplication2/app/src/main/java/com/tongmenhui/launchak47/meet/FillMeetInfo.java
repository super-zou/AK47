package com.tongmenhui.launchak47.meet;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.tongmenhui.launchak47.R;

import org.angmarch.views.NiceSpinner;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class FillMeetInfo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_meet_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Resources res =getResources();
        String[] years = res.getStringArray(R.array.years);
        String[] months = res.getStringArray(R.array.months);
        String[] days = res.getStringArray(R.array.days);
        String[] heights = res.getStringArray(R.array.heights);
        String[] degrees = res.getStringArray(R.array.degrees);
        String[] regions = res.getStringArray(R.array.regions);
        String[] provinces = res.getStringArray(R.array.provinces);
        String[] cities = res.getStringArray(R.array.cities);

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
    }

}
