package com.refresh.customrefreshlistview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * Created by HuangZc on 2018/6/12.
 */

@SuppressLint("AppCompatCustomView")
public class AlphaTextView extends TextView {

    //下拉高度
    public static final int mDropDownAlphaHeight = 200;

    private static int mDefaultAlphaHeight = mDropDownAlphaHeight;

    // 255/AlphaHeight (AlphaHeight为UI定义的，Y轴的变化在0-AlphaHeight之间进行渐变）
    private static float scale = 255 / mDefaultAlphaHeight;
    //上推高度
    private int mPushUpAlphaHeight = 500;

    private Drawable background;

    private AInterpolator aInterpolator;

    public AlphaTextView(Context context) {
        super(context);
        init();
    }

    public AlphaTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AlphaTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        this.background = getBackground();
    }

    public void setInterpolator(AInterpolator aInterpolator) {
        this.aInterpolator = aInterpolator;
    }

    public void setDetailAlpha(int alpha, TextView tvBg) {
        if (alpha == 0) {
            tvBg.setVisibility(View.VISIBLE);
        } else {
            tvBg.setVisibility(View.GONE);
        }
        background.setAlpha(alpha);
    }

    public void setTitleAlpha(int y, boolean isDropDown) {
        int alpha = getAlpha(aInterpolator.getAlpha(y), getAlphaHeight(isDropDown), getScale(isDropDown));
        background.setAlpha(alpha);
    }

    private int getAlpha(int y, int alphaHeight, float scale) {
        int alpha = 0;
        //滑动距离小于一定高度时，设置title背景颜色透明度渐变
        if (y >= 0 && y <= alphaHeight) {
            alpha = (int) (scale * y);
        } else {
            //滑动到title下面设置为白色
            alpha = 255;
        }
        return alpha;
    }

    private int getAlphaHeight(boolean isDropDown) {
        int alphaHeight;
        if (isDropDown) {
            alphaHeight = mDropDownAlphaHeight;
        } else {
            alphaHeight = mPushUpAlphaHeight;
        }
        return alphaHeight;
    }

    private float getScale(boolean isDropDown) {
        float scale = 1f;
        if (isDropDown) {
            scale = (float) 255 / mDropDownAlphaHeight;
        } else {
            scale = (float) 255 / mPushUpAlphaHeight;
        }
        return scale;
    }
}
