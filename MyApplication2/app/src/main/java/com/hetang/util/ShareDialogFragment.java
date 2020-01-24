package com.hetang.util;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.hetang.BuildConfig;
import com.hetang.R;
import com.hetang.common.MyApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hetang.util.HttpUtil.GET_DOWNLOAD_QR;
import static com.hetang.util.Utility.drawable2File;

/**
 * Created by super-zou on 18-9-9.
 */

public class ShareDialogFragment extends BaseDialogFragment implements View.OnClickListener {
    private static final boolean isDebug = true;
    private static final String TAG = "ShareDialogFragment";
    private Dialog mDialog;
    private Uri imageUri;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new Dialog(getActivity(), R.style.Theme_Design_BottomSheetDialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.share);

        getDownLoadQR();
        //set dialog window

        Typeface font = Typeface.createFromAsset(MyApplication.getContext().getAssets(), "fonts/fontawesome-webfont_4.7.ttf");
        FontManager.markAsIconContainer(mDialog.findViewById(R.id.custom_actionbar), font);
        mDialog.setCanceledOnTouchOutside(true);
        Window window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.BOTTOM;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        //set component
        ImageView wechat = mDialog.findViewById(R.id.wechat);
        ImageView friendCircle = mDialog.findViewById(R.id.friend_circle);
        ImageView qq = mDialog.findViewById(R.id.qq);
        ImageView qqzone = mDialog.findViewById(R.id.qqzone);
        ImageView weibo = mDialog.findViewById(R.id.weibo);

        wechat.setOnClickListener(this);
        friendCircle.setOnClickListener(this);
        qq.setOnClickListener(this);
        qqzone.setOnClickListener(this);
        weibo.setOnClickListener(this);

        return mDialog;
    }

    private void getDownLoadQR() {
        RequestBody requestBody = new FormBody.Builder().build();
        HttpUtil.sendOkHttpRequest(getContext(), GET_DOWNLOAD_QR, requestBody, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                try {
                    final String uri = new JSONObject(responseText).optString("uri");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getDownLoadQrDrawable(uri);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }

    private void getDownLoadQrDrawable(String uri) {
        SimpleTarget<Drawable> simpleTarget = new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                File file = drawable2File(getContext(), resource, "download_qr_code.png");
                imageUri = FileProvider.getUriForFile(MyApplication.getContext(), BuildConfig.APPLICATION_ID + ".fileprovider", file);
            }
        };

        Glide.with(getContext()).load(HttpUtil.DOMAIN + uri).into(simpleTarget);
    }

    @Override
    public void onClick(View view) {
        // File file = drawable2File(MyApplication.getContext(), R.drawable.download_qr_code, "download_qr_code.png");
        //  Uri imageUri = FileProvider.getUriForFile(MyApplication.getContext(), BuildConfig.APPLICATION_ID + ".fileprovider", file);

        ComponentName comp = null;
        switch (view.getId()) {
            case R.id.wechat:
                comp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI");
                break;
            case R.id.friend_circle:
                comp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI");
                break;

            case R.id.qq:
                comp = new ComponentName("com.tencent.mobileqq", "com.tencent.mobileqq.activity.JumpActivity");
                break;
            case R.id.qqzone:
                comp = new ComponentName("com.qzone", "com.qzonex.module.operation.ui.QZonePublishMoodActivity");
                break;
            case R.id.weibo:
                comp = new ComponentName("com.sina.weibo", "com.sina.weibo.composerinde.ComposerDispatchActivity");
                break;
            default:
                break;
        }

        Intent shareIntent = new Intent();
        shareIntent.setComponent(comp);
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/*");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "分享下载二维码"));

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
