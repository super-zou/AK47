package com.mufu.main;

import android.graphics.Typeface;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import com.mufu.R;
import com.mufu.common.BaseAppCompatActivity;
import com.mufu.meet.UserMeetInfo;
import com.mufu.util.CommonDialogFragmentInterface;
import com.mufu.util.FontManager;

public class MeetArchiveActivity extends BaseAppCompatActivity implements CommonDialogFragmentInterface {
    private static final String TAG = "MeetArchiveActivity";
    private static final boolean isDebug = true;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    MeetArchiveFragment meetArchiveFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.archive_activity);

        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
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

    @Override
    public void onBackFromDialog(int type, int result, boolean status){}
}


     


        
