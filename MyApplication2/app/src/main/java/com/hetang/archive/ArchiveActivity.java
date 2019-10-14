package com.hetang.archive;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import com.hetang.R;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.main.ArchiveFragment;
import com.hetang.util.FontManager;

import static com.hetang.util.ParseUtils.startMeetArchiveActivity;

public class ArchiveActivity extends BaseAppCompatActivity{
    private static final String TAG = "ArchiveActivity";
    private static final boolean isDebug = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.archive_activity);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        
         int uid = getIntent().getIntExtra("uid", -1);

        ArchiveFragment archiveFragment = new ArchiveFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("uid", uid);
        archiveFragment.setArguments(bundle);
        //1.创建Fragment的管理对象
        FragmentManager fragmentManager = getSupportFragmentManager();
        //2.获取Fragment的事务对象并且开启事务
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //3.调用事务中动态操作fragment的方法执行 add(添加到哪里，需要添加的fragment对象);
        fragmentTransaction.add(R.id.fragment_container, archiveFragment, "Archive");
        fragmentTransaction.addToBackStack(null);
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
    public void onBackPressed(){
        exit();
    }

    private void exit(){
        finish();
    }
}
