package com.hetang.main;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import com.hetang.R;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.main.MeetArchiveFragment;
import com.hetang.meet.UserMeetInfo;
import com.hetang.util.FontManager;

public class MeetArchiveActivity extends BaseAppCompatActivity {
    private static final String TAG = "MeetArchiveActivity";
    private static final boolean isDebug = true;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    MeetArchiveFragment meetArchiveFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.archive_activity);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        int uid = getIntent().getIntExtra("uid", -1);
        UserMeetInfo userMeetInfo = (UserMeetInfo) getIntent().getSerializableExtra("meet");
        if (meetArchiveFragment == null){
            meetArchiveFragment = new MeetArchiveFragment();
        }
        Bundle bundle = new Bundle();
        if (uid != -1){
            bundle.putInt("uid", uid);
        }

        if (userMeetInfo != null){
            bundle.putSerializable("user_meet_info", userMeetInfo);
        }
        meetArchiveFragment.setArguments(bundle);
        //1.创建Fragment的管理对象
        fragmentManager = getSupportFragmentManager();
        //2.获取Fragment的事务对象并且开启事务
        fragmentTransaction = fragmentManager.beginTransaction();
        //3.调用事务中动态操作fragment的方法执行 add(添加到哪里，需要添加的fragment对象);
        fragmentTransaction.add(R.id.fragment_container, meetArchiveFragment, "MeetArchive");
        fragmentTransaction.addToBackStack("MeetArchive");
        //4.提交事务
        fragmentTransaction.commit();

        TextView backLeft = findViewById(R.id.left_back);
        backLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.custom_actionbar), font);
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    private void exit() {
        finish();
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }


}


     


        