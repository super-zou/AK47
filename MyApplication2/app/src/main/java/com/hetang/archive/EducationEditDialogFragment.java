import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.hetang.R;
import com.hetang.util.BaseDialogFragment;
import com.hetang.util.CommonDialogFragmentInterface;
import com.hetang.util.FontManager;
import com.hetang.util.HttpUtil;
import com.hetang.util.Slog;

import org.angmarch.views.NiceSpinner;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hetang.main.ArchiveFragment.REQUESTCODE;
import static com.hetang.main.ArchiveFragment.SET_EDUCATION_RESULT_OK;

public class EducationEditDialogFragment extends BaseDialogFragment {
    private static final String TAG = "EducationEditDialogFragment";
    private Context mContext;
    private Dialog mDialog;
    private View view;
    private LayoutInflater inflater;
    private TextView title;
    private TextView save;
    private TextView cancel;
    private CommonDialogFragmentInterface commonDialogFragmentInterface;
    private boolean degreeSlected = false;
    private boolean entranceSlected = false;
    private boolean graduateSlected = false;
    private static final String CREATE_EDUCATION_BACKGROUND_URL = HttpUtil.DOMAIN + "?q=personal_archive/education_background/create";
    
     @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        try {
            commonDialogFragmentInterface = (CommonDialogFragmentInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement commonDialogFragmentInterface");
        }
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        inflater = LayoutInflater.from(mContext);
        mDialog = new Dialog(mContext, android.R.style.Theme_Light_NoTitleBar);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        view = inflater.inflate(R.layout.education_background_edit, null);
        //mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(view);
        mDialog.setCanceledOnTouchOutside(true);
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        
        //layoutParams.alpha = 0.9f;
        layoutParams.gravity = Gravity.TOP;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        //window.setDimAmount(0.8f);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        window.setAttributes(layoutParams);

        initView();

        Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.cancel), font);

        return mDialog;
    }
    
    private void initView() {

        save = mDialog.findViewById(R.id.save);
        cancel = mDialog.findViewById(R.id.cancel);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });
        
        final TextInputEditText universityEdit = mDialog.findViewById(R.id.university_edit);
        final TextInputEditText majorEdit = mDialog.findViewById(R.id.major_edit);

        final EducationBackground educationBackground = new EducationBackground();

        String[] degrees = getResources().getStringArray(R.array.degrees);
        String[] entranceYears = getResources().getStringArray(R.array.entrance_years);
        String[] graduateYears = getResources().getStringArray(R.array.graduate_years);
        
        NiceSpinner niceSpinnerDegree = (NiceSpinner) mDialog.findViewById(R.id.nice_spinner_degree);
        final List<String> degreeList = new LinkedList<>(Arrays.asList(degrees));
        niceSpinnerDegree.attachDataSource(degreeList);
        niceSpinnerDegree.addOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                educationBackground.degree = String.valueOf(degreeList.get(i));
                degreeSlected = true;
            }

        });
        
        final List<String> entranceYearList = new LinkedList<>(Arrays.asList(entranceYears));
        NiceSpinner niceSpinnerEntranceYears = (NiceSpinner) mDialog.findViewById(R.id.entrance_year);
        niceSpinnerEntranceYears.attachDataSource(entranceYearList);
        niceSpinnerEntranceYears.addOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                educationBackground.entranceYear = Integer.parseInt(entranceYearList.get(i));
                entranceSlected = true;
            }

        });
        
        final List<String> graduateYearList = new LinkedList<>(Arrays.asList(graduateYears));
        NiceSpinner niceSpinnerGraduateYears = (NiceSpinner) mDialog.findViewById(R.id.graduate_year);
        niceSpinnerGraduateYears.attachDataSource(graduateYearList);
        niceSpinnerGraduateYears.addOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                educationBackground.graduateYear = Integer.parseInt(graduateYearList.get(i));
                graduateSlected = true;
            }

        });
        
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(universityEdit.getText().toString())){
                    educationBackground.university = universityEdit.getText().toString();
                }else {
                    Toast.makeText(getContext(), "请输入学校", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!TextUtils.isEmpty(majorEdit.getText().toString())){
                    educationBackground.major = majorEdit.getText().toString();
                }else {
                    Toast.makeText(getContext(), "请输入专业", Toast.LENGTH_LONG).show();
                    return;
                }
                
                if(!degreeSlected){
                    Toast.makeText(getContext(), "请选择学历", Toast.LENGTH_LONG).show();
                    return;
                }
                if(!entranceSlected){
                    Toast.makeText(getContext(), "请选择入学时间", Toast.LENGTH_LONG).show();
                    return;
                }
                if(!graduateSlected){
                    Toast.makeText(getContext(), "请选择毕业时间", Toast.LENGTH_LONG).show();
                    return;
                }
                
                String educationBackGroundString = getEducationBackgroundJsonObject(educationBackground).toString();
                uploadToServer(educationBackGroundString);
            }
        });
    }

    private void uploadToServer(String educationBackground) {
    Slog.d(TAG, "----------------------->educationBackground: "+educationBackground);
        showProgressDialog(getString(R.string.saving_progress));

        RequestBody requestBody = new FormBody.Builder()
                .add("educationBackground", educationBackground).build();
        HttpUtil.sendOkHttpRequest(getContext(), CREATE_EDUCATION_BACKGROUND_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Slog.d(TAG, "================uploadToServer response:" + responseText);
                if(!TextUtils.isEmpty(responseText)){
                    dismissProgressDialog();
                    mDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {  }
        });

    }
    
    private JSONObject getEducationBackgroundJsonObject(EducationBackground educationBackground) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("university", educationBackground.university);
            jsonObject.put("major", educationBackground.major);
            jsonObject.put("degree", educationBackground.degree);
            jsonObject.put("entrance_year", educationBackground.entranceYear);
            jsonObject.put("graduate_year", educationBackground.graduateYear);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
    
    class EducationBackground{
        String university;
        String major;
        String degree;
        int entranceYear;
        int graduateYear;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        if (getTargetFragment() != null){
            Intent intent = new Intent();
            getTargetFragment().onActivityResult(REQUESTCODE, SET_EDUCATION_RESULT_OK, intent);
        }

        dismissProgressDialog();

        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
    }
    
    @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
    }

}
