package com.hetang.util;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.hetang.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by super-zou on 18-9-9.
 */

public class PictureReviewDialogFragment extends BaseDialogFragment implements ViewSwitcher.ViewFactory{
    private static final String TAG = "PictureReviewDialogFragment";
    private Dialog mDialog;
    private ImageSwitcher imageSwitcher;
    private HorizontalScrollView horizontalScrollView;
    private LinearLayout pictureWrapper;
    private List<Drawable> drawableList= new ArrayList<>();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Slog.d(TAG, "------------->onCreateDialog");

        mDialog = new Dialog(getActivity(), android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.picture_review_dialog);
        mDialog.setCanceledOnTouchOutside(true);

        Window window = mDialog.getWindow();
        
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        initView();


        return mDialog;
    }
    
    private void initView(){
        imageSwitcher = mDialog.findViewById(R.id.image_switcher);
        imageSwitcher.setFactory(this);

        imageSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        pictureWrapper = mDialog.findViewById(R.id.picture_wrapper);

        Bundle bundle = getArguments();
        
        final int index = bundle.getInt("index");
        String[] pictureUrlArray = bundle.getStringArray("pictureUrlArray");

        for (int i=0; i<pictureUrlArray.length; i++){
            SimpleTarget<Drawable> simpleTarget = new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                    drawableList.add(resource);
                    final ImageView imageView = new ImageView(getContext());
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int)Utility.dpToPx(getContext(), 150),
                            (int)Utility.dpToPx(getContext(), 150));
                    layoutParams.setMargins(4, 4, 0, 4);
                    imageView.setLayoutParams(layoutParams);
                    pictureWrapper.addView(imageView);
                    imageView.setImageDrawable(resource);

                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Slog.d(TAG, "---------------->getDrawable: "+imageView.getDrawable());
                            imageSwitcher.setImageDrawable(imageView.getDrawable());
                        }
                    });
                    
                    if(drawableList.size() > index){
                        imageSwitcher.setImageDrawable(drawableList.get(index));
                    }
                }


            };

            Glide.with(getContext()).load(HttpUtil.DOMAIN + pictureUrlArray[i]).into(simpleTarget);
        }

    }
    
    @Override
    public View makeView(){
        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        float density = dm.density;
        int width = dm.widthPixels;
        int screenHeight = width;
        ImageView i = new ImageView(getContext());
        i.setScaleType(ImageView.ScaleType.FIT_CENTER);
        i.setLayoutParams(new ImageSwitcher.LayoutParams(width, screenHeight));

        return i ;
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
