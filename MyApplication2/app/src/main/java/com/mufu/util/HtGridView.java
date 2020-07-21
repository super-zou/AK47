package com.mufu.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class HtGridView extends GridView{
    public HtGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HtGridView(Context context) {
        super(context);
    }

    public HtGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);

        super.onMeasure(widthMeasureSpec, expandSpec);
    }

}
