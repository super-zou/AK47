package com.hetang.meet;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hetang.R;
import com.hetang.adapter.DynamicsListAdapter;
import com.hetang.common.BaseAppCompatActivity;
import com.hetang.common.Dynamic;
import com.hetang.common.DynamicsInteractDetailsActivity;
import com.hetang.common.HandlerTemp;
import com.hetang.common.MyApplication;
import com.hetang.home.HomeFragment;
import com.hetang.main.MeetArchiveFragment;
import com.hetang.util.CommonDialogFragmentInterface;
import com.hetang.util.CommonUserListDialogFragment;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.InterActInterface;
import com.hetang.util.Slog;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.jcodecraeer.xrecyclerview.ProgressStyle.BallSpinFadeLoader;

public class SpecificUserDynamicsActivity extends BaseAppCompatActivity{
    private static final String TAG = "SpecificUserDynamics";
    private static final boolean isDebug = true;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    HomeFragment homeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.specific_user_dynamics_activity);

        int uid = getIntent().getIntExtra("uid", -1);

        if (homeFragment == null){
            homeFragment = new HomeFragment();
        }
        Bundle bundle = new Bundle();
        if (uid != -1){
            bundle.putInt("uid", uid);
            bundle.putBoolean("specific", true);
        }
        homeFragment.setArguments(bundle);
        //1.创建Fragment的管理对象
        fragmentManager = getSupportFragmentManager();
        //2.获取Fragment的事务对象并且开启事务
        fragmentTransaction = fragmentManager.beginTransaction();
        //3.调用事务中动态操作fragment的方法执行 add(添加到哪里，需要添加的fragment对象);
        fragmentTransaction.add(R.id.fragment_container, homeFragment, "MeetDynamic");
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

                
