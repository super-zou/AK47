package com.mufu.util;


import android.view.View;

/**
 * Created by super-zou on 18-9-9.
 */

public interface InterActInterface {
     void onCommentClick(View view, int position);
     void onPraiseClick(View view, int position);
     void onOperationClick(View view, int position);
     void onDynamicPictureClick(View view, int position, String[] pictureUrlArray, int index);
}
