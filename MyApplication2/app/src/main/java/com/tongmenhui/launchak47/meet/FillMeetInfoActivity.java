package com.tongmenhui.launchak47.meet;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.GridView;
import android.widget.TextView;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.permissions.RxPermissions;
import com.luck.picture.lib.tools.PictureFileUtils;
import com.tongmenhui.launchak47.adapter.GridImageAdapter;
import com.tongmenhui.launchak47.main.AddDynamicsActivity;
import com.tongmenhui.launchak47.util.FontManager;
import com.tongmenhui.launchak47.util.Slog;

import java.io.File;
import java.util.ArrayList;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import android.content.Intent;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tongmenhui.launchak47.R;
import com.tongmenhui.launchak47.region.activity.RegionSelectionActivity;
import com.tongmenhui.launchak47.util.HttpUtil;

import org.angmarch.views.NiceSpinner;
import org.json.JSONArray;
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

//import com.google.gson.JsonSerializer;

public class FillMeetInfoActivity extends AppCompatActivity {
        private static final String TAG = "FillMeetInfoActivity";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final static int SELFREQUEST = 1;
    private final static int REQUIREREQUEST = 2;
    private static final String fillMeetInfoUrl = HttpUtil.DOMAIN +"?q=meet/look_friend";
    Resources res;
    String[] years;
    String[] months;
    String[] days;
    String[] heights;
    String[] degrees;
    String[] ages;
    MeetMemberInfo meetMemberInfo;
    SelfCondition selfCondition;
    PartnerRequirement partnerRequirement;
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
    String selfConditionJson;
    String partnerRequirementJson;
    
        LinearLayout uploadPicture;
    LinearLayout pageIndicator;
    GridView morePicture;
    Button addAvatar;
    TextView addMorePicture;
    TextView pageLeft;
    TextView pageMiddle;
    TextView pageRight;
    private int themeId;
    private Context mContext;
    
        private List<LocalMedia> selectList = new ArrayList<>();
    private List<LocalMedia> avatarSelectList = new ArrayList<>();
    private GridImageAdapter adapter;
    private List<File> selectFileList = new ArrayList<>();
    private List<File> avatarSelectFileList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fill_meet_info);
        mContext = this;
        boolean avatarSet = getIntent().getBooleanExtra("avatarSet", false);

        initView();

        uploadPicture(avatarSet);   
    }
    
    private void  initView(){
        addAvatar = findViewById(R.id.add_avatar);
        addMorePicture = findViewById(R.id.add_more_picture);
        morePicture = findViewById(R.id.more_picture);
        pageLeft = findViewById(R.id.page_left);
        pageMiddle = findViewById(R.id.page_middle);
        pageRight = findViewById(R.id.page_right);
        uploadPicture = findViewById(R.id.upload_picture);
        pageIndicator = findViewById(R.id.page_indicator);

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(findViewById(R.id.fill_meet_info), font);
    }
    
        private void uploadPicture(boolean avatarSet){
        if(avatarSet == false){

            RxPermissions permissions = new RxPermissions(this);
            permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Observer<Boolean>() {
                @Override
                public void onSubscribe(Disposable d) {
                }
                @Override
                public void onNext(Boolean aBoolean) {
                    if (aBoolean) {
                        PictureFileUtils.deleteCacheDirFile(mContext);
                    } else {
                        Toast.makeText(mContext,
                                getString(R.string.picture_jurisdiction), Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onError(Throwable e) {
                }

                @Override
                public void onComplete() {
                }
            });
            
            if(uploadPicture.getVisibility() == View.GONE){
                uploadPicture.setVisibility(View.VISIBLE);
            }

            if(pageMiddle.getVisibility() == View.GONE){
                pageMiddle.setVisibility(View.VISIBLE);
            }
            
            themeId = R.style.picture_default_style;

            addAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PictureSelector.create(FillMeetInfoActivity.this)
                            .openGallery(PictureMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                            .theme(themeId)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style
                            .maxSelectNum(1)// 最大图片选择数量
                            .minSelectNum(1)// 最小选择数量
                            .selectionMode(PictureConfig.SINGLE)// 多选 or 单选
                            .previewImage(true)// 是否可预览图片
                            .isCamera(true)// 是否显示拍照按钮
                            .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                        .enableCrop(true)// 是否裁剪
                            .compress(true)// 是否压缩
                            .synOrAsy(true)//同步true或异步false 压缩 默认同步
                            .glideOverride(180, 180)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
                            .withAspectRatio(1, 1)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                        .isGif(true)// 是否显示gif图片
                            .freeStyleCropEnabled(false)// 裁剪框是否可拖拽
                            .showCropFrame(false)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
                            .showCropGrid(true)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
                            .openClickSound(true)// 是否开启点击声音
                            .cropCompressQuality(90)// 裁剪压缩质量 默认100
                            .minimumCompressSize(100)// 小于100kb的图片不压缩
                            .cropWH(160, 160)// 裁剪宽高比，设置如果大于图片本身宽高则无效
                            .rotateEnabled(true) // 裁剪是否可旋转图片
                            .scaleEnabled(true)// 裁剪是否可放大缩小图片
                            .forResult(PictureConfig.SINGLE);//结果回调onActivityResult code
                    }
            });
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Slog.d(TAG, "=======uri: "+data.getData());
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.SINGLE:
                    // 图片选择结果回调
                    avatarSelectList = PictureSelector.obtainMultipleResult(data);
                    // 例如 LocalMedia 里面返回三种path
                    // 1.media.getPath(); 为原图path
                    // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                    // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                    // 如果裁剪并压缩了，已取压缩路径为准，因为是先裁剪后压缩的
                    Slog.d(TAG, "Selected pictures: " + avatarSelectList.size());
                    //activity_picture_array = new String[selectList.size()];
                    for (LocalMedia media : avatarSelectList) {

                        Log.i("图片-----》", media.getPath());
                        Log.d("压缩图片------->>", media.getCompressPath());
                        Slog.d(TAG, "===========num: " + media.getNum());
                        //activity_picture_array[media.getNum() - 1] = media.getCompressPath();
                        avatarSelectFileList.add(new File(media.getCompressPath()));

                        Bitmap bitmap = BitmapFactory.decodeFile(media.getCompressPath());
                        Drawable drawable = new BitmapDrawable(getResources(),bitmap);
                        addAvatar.setBackground(drawable);
                    }

                    break;
                    case PictureConfig.CHOOSE_REQUEST:
                    // 图片选择结果回调
                    selectList = PictureSelector.obtainMultipleResult(data);
                    // 例如 LocalMedia 里面返回三种path
                    // 1.media.getPath(); 为原图path
                    // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                    // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                    // 如果裁剪并压缩了，已取压缩路径为准，因为是先裁剪后压缩的
                    Slog.d(TAG, "Selected pictures: " + selectList.size());
                    //activity_picture_array = new String[selectList.size()];
                    for (LocalMedia media : selectList) {

                        Log.i("图片-----》", media.getPath());
                        Log.d("压缩图片------->>", media.getCompressPath());
                        Slog.d(TAG, "===========num: " + media.getNum());
                        //activity_picture_array[media.getNum() - 1] = media.getCompressPath();
                        selectFileList.add(new File(media.getCompressPath()));

                        /*
                        Bitmap bitmap = BitmapFactory.decodeFile(media.getCompressPath());
                        Drawable drawable = new BitmapDrawable(getResources(),bitmap);
                        addAvatar.setBackground(drawable);
                        */
                    }
                    if(false){
                        adapter.setList(selectList);
                        adapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
        
        if (resultCode == 2) {
            String SelectedResult = data.getStringExtra("SelectedResult");
            if (requestCode == SELFREQUEST) {
                //设置结果显示框的显示数值
                Button button = (Button) findViewById(R.id.self_region);
                button.setText(SelectedResult);
                selfCondition.setLives(SelectedResult);
                SELFLIVESELECTED = true;

            } else {
                //设置结果显示框的显示数值
                Button button = (Button) findViewById(R.id.require_region);
                button.setText(SelectedResult);
                partnerRequirement.setRequirementLives(SelectedResult);
                REQUIRELIVESELECTED = true;
            }
        }
    }
    
    
        private void fillMeetInfo(){

        res = getResources();
        years = res.getStringArray(R.array.years);
        months = res.getStringArray(R.array.months);
        days = res.getStringArray(R.array.days);
        heights = res.getStringArray(R.array.heights);
        degrees = res.getStringArray(R.array.degrees);
        ages = res.getStringArray(R.array.ages);

        meetMemberInfo = new MeetMemberInfo();
        selfCondition = new SelfCondition();
        partnerRequirement = new PartnerRequirement();

        final LinearLayout selfLayout = (LinearLayout) findViewById(R.id.self);
        final LinearLayout requireLayout = (LinearLayout) findViewById(R.id.requirement);

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

        NiceSpinner niceSpinnerMonths = (NiceSpinner) findViewById(R.id.nice_spinner_months);
        final List<String> monthList = new LinkedList<>(Arrays.asList(months));
        niceSpinnerMonths.attachDataSource(monthList);
        niceSpinnerMonths.addOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        niceSpinnerDays.addOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        niceSpinnerHeight.addOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        niceSpinnerDegree.addOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Slog.e("什么数据",String.valueOf(yearList.get(i)));
                //Toast.makeText(FillMeetInfoActivity.this, String.valueOf(degreeList.get(i)), Toast.LENGTH_SHORT).show();
                selfCondition.setDegree(String.valueOf(degreeList.get(i)));
                DEGREESELECTED = true;
            }

        });
        Button selfRegionSelection = (Button) findViewById(R.id.self_region);
        selfRegionSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FillMeetInfoActivity.this, RegionSelectionActivity.class);
                startActivityForResult(intent, SELFREQUEST);
            }
        });

        final Button preButton = (Button) findViewById(R.id.prev);
        final Button nextButton = (Button) findViewById(R.id.next);
        final Button doneButton = (Button) findViewById(R.id.done);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSelfInfo()) {
                    selfLayout.setVisibility(View.GONE);
                    requireLayout.setVisibility(View.VISIBLE);
                    createRequiredView();
                    v.setVisibility(View.GONE);
                    preButton.setVisibility(View.VISIBLE);
                    doneButton.setVisibility(View.VISIBLE);
                    selfConditionJson = getSelfConditionJsonObject().toString();

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
                EditText editText = (EditText) findViewById(R.id.illustration);
                partnerRequirement.setIllustration(editText.getText().toString());
                partnerRequirementJson = getPartnerRequirementJsonObject().toString();
                ObjectMapper mapper = new ObjectMapper();
                if (checkRequiredInfo()) {
                    RequestBody requestBody = new FormBody.Builder()
                            .add("self_condition", selfConditionJson)
                            .add("partner_requirement", partnerRequirementJson)
                            .build();

                    HttpUtil.sendOkHttpRequest(FillMeetInfoActivity.this, fillMeetInfoUrl, requestBody, new Callback() {
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String responseText = response.body().string();
                            //Slog.d(TAG, "response : "+responseText);
                            //getResponseText(responseText);
                            finish();
                        }

                        @Override
                        public void onFailure(Call call, IOException e) {

                        }
                    });
                }
            }
        });

    }

    private JSONObject getSelfConditionJsonObject() {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            jsonObject.put("sex", selfCondition.getSelfSex());
            jsonObject.put("birth_year", selfCondition.getBirthYear());
            jsonObject.put("birth_month", selfCondition.getBirthMonth());
            jsonObject.put("birth_day", selfCondition.getBirthDay());
            jsonObject.put("height", selfCondition.getHeight());
            jsonObject.put("degree", selfCondition.getDegreeIndex());
            jsonObject.put("lives", selfCondition.getLives());

            jsonObject.put("sex", selfCondition.getSelfSex());
            jsonObject.put("birth_year", selfCondition.getBirthYear());
            jsonObject.put("birth_month", selfCondition.getBirthMonth());
            jsonObject.put("birth_day", selfCondition.getBirthDay());
            jsonObject.put("height", selfCondition.getHeight());
            jsonObject.put("degree", selfCondition.getDegreeIndex());
            jsonObject.put("lives", selfCondition.getLives());
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
            jsonObject.put("lives", partnerRequirement.getRequirementLives());
            jsonObject.put("illustration", partnerRequirement.getIllustration());
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return jsonObject;
    }

    private boolean checkSelfInfo() {

        if (BIRTHYEARSELECTED == false) {
            Toast.makeText(FillMeetInfoActivity.this, "请选择出生年份", Toast.LENGTH_LONG).show();
            return false;
        }

        if (BIRTHMONTHSELECTED == false) {
            Toast.makeText(FillMeetInfoActivity.this, "请选择出生月份", Toast.LENGTH_LONG).show();
            return false;
        }

        if (BIRTHDAYSELECTED == false) {
            Toast.makeText(FillMeetInfoActivity.this, "请选择出生日", Toast.LENGTH_LONG).show();
            return false;
        }

        if (HEIGHTSELECTED == false) {
            Toast.makeText(FillMeetInfoActivity.this, "请选择身高", Toast.LENGTH_LONG).show();
            return false;
        }

        if (DEGREESELECTED == false) {
            Toast.makeText(FillMeetInfoActivity.this, "请选择学历", Toast.LENGTH_LONG).show();
            return false;
        }

        if (SELFLIVESELECTED == false) {
            Toast.makeText(FillMeetInfoActivity.this, "请选择居住地", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private boolean checkRequiredInfo() {

        if (REQUIRESEXCHECKED == false) {
            Toast.makeText(FillMeetInfoActivity.this, "请选择性别", Toast.LENGTH_LONG).show();
            return false;
        }

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

        return true;
    }

    private void createRequiredView() {

        RadioGroup require_sex = (RadioGroup) findViewById(R.id.require_sex);
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
                REQUIRESEXCHECKED = true;
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

        Button requireRegionSelection = (Button) findViewById(R.id.require_region);
        requireRegionSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FillMeetInfoActivity.this, RegionSelectionActivity.class);
                startActivityForResult(intent, REQUIREREQUEST);
            }
        });
    }

}
