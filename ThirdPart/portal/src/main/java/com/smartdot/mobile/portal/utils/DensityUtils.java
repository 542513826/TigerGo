package com.smartdot.mobile.portal.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;

/**
 * 屏幕相关辅助类
 */
public class DensityUtils {
    // ******************************************************************************************
    // 单位转换
    // ******************************************************************************************
    /** 像素密度 */
    public static float getDisplayMetrics(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    /** dp 转成为 px */
    public static int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue,
                context.getResources().getDisplayMetrics());
    }

    /** px 转成为 dp */
    public static int px2dp(Context context, float pxValue) {
        return (int) (pxValue / getDisplayMetrics(context) + 0.5f);
    }

    /** sp转px */
    public static int sp2px(Context context, float spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spVal,
                context.getResources().getDisplayMetrics());
    }

    /** px转sp */
    public static float px2sp(Context context, float pxVal) {
        return (pxVal / context.getResources().getDisplayMetrics().scaledDensity);
    }

    // ******************************************************************************************
    // 屏幕宽高
    // ******************************************************************************************
    /** 获取屏幕宽 */
    public static int getScreenWidth(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metric);
        return metric.widthPixels;
    }

    /** 获取屏幕高，包含状态栏，但不包含某些手机最下面的【HOME键那一栏】，如1920屏幕只有1794 */
    public static int getScreenHeight(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metric);
        return metric.heightPixels;
    }

    /** 获取屏幕宽 */
    public static int getScreenWidth2(Context context) {
        Point point = new Point();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(point);
        return point.x;
    }

    /** 获取屏幕高，包含状态栏，但不包含某些手机最下面的【HOME键那一栏】，如1920屏幕只有1794 */
    public static int getScreenHeight2(Context context) {
        Point point = new Point();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(point);
        return point.y;
    }

    // ******************************************************************************************
    // 状态栏、标题栏
    // ******************************************************************************************
    /** 状态栏高度，单位px，一般为25dp */
    public static int getStatusBarHeight(Context context) {
        int height = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            height = context.getResources().getDimensionPixelSize(resourceId);
        }
        return height;
    }

    /** 状态栏高度，单位px，【注意】要在onWindowFocusChanged中获取才可以，不建议采用这种方式获取 */
    public static int getStatusBarHeight2(Activity activity) {
        Rect rect = new Rect();
        // DecorView是Window中的最顶层view，可以从DecorView获取到程序显示的区域，包括标题栏，但不包括状态栏。所以状态栏的高度即为显示区域的top坐标值
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect.top;
    }

    /** 标题栏的高度，【注意】要在onWindowFocusChanged中获取才可以，不建议采用这种方式获取 */
    public static int getTitleBarHeight(Activity activity) {
        int contentTop = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
        return contentTop - getStatusBarHeight(activity);
    }

    // ******************************************************************************************
    // 屏幕截图
    // ******************************************************************************************
    /** 获取当前屏幕截图，包含状态栏，需要 root 权限，且可能会被360【报毒】 */
    public static Bitmap snapShotWithStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        int width = getScreenWidth(activity);
        int height = getScreenHeight(activity);
        Bitmap bp = null;
        bp = Bitmap.createBitmap(bmp, 0, 0, width, height);
        view.destroyDrawingCache();
        return bp;
    }

    /** 获取当前屏幕截图，不包含状态栏，需要 root 权限，且可能会被360【报毒】 */
    public static Bitmap snapShotWithoutStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        int width = getScreenWidth(activity);
        int height = getScreenHeight(activity);
        Bitmap bp = null;
        bp = Bitmap.createBitmap(bmp, 0, statusBarHeight, width, height - statusBarHeight);
        view.destroyDrawingCache();
        return bp;
    }

    /** 保存bitmap为图片 */
    public static void saveBitmap2Pic(Bitmap bitmap, String fileName) {
        File file = new File(fileName);
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
