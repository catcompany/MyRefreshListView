package com.refresh.customrefreshlistview.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import static android.view.View.VISIBLE;

/**
 * 状态栏工具类
 * creat by JiangQi 2018/4/26
 */
public class StatusBarUtil {
    private static int height = -1;

    /**
     * @param activity
     * @param color
     */
    public static void setStatusBarBg(Activity activity, int color) {
        Window window = activity.getWindow();
        //如果系统5.0以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
        setActivityTranslucent(activity);
    }

    /**
     * 获取状态栏的高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static void setGapHeight(Context context, View bannerGap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (height == -1) {
                height = getStatusBarHeight(context);
            }
            bannerGap.setVisibility(VISIBLE);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bannerGap.getLayoutParams();
            params.height = height;//设置当前控件布局的高度
            bannerGap.setLayoutParams(params);//将设置好的布局参数应用到控件中
        }
    }

    /**
     * 设置Activity全屏
     * 6.0以上系统才展示置顶优化，因此全屏的设置，以6.0作为判断条件
     *
     * @param activity
     */
    public static void setActivityTranslucent(Activity activity) {
        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ViewGroup decorView = (ViewGroup) window.getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public static void setStatusBarTxtColor(Activity activity, boolean isDefault) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 6.0及以上
            if (isDefault) {
                activity.getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);//白底
            } else {
                activity.getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//黑底
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { // 4.4-6.0
            setStatusBarBg(activity, Color.parseColor("#313131"));
        }
    }
}