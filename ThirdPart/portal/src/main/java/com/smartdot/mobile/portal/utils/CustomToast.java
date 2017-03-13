package com.smartdot.mobile.portal.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * 此类是Toast的工具类
 */
public class CustomToast {

    private static Toast mToast;

    private static Handler mHandler = new Handler();

    private static Runnable r = new Runnable() {
        public void run() {
            mToast.cancel();
        }
    };

    /**
     * 
     * @param mContext
     *            上下文对象
     * @param text
     *            显示的文本
     * @param duration
     *            持续时间 单位:毫秒
     */
    public static void showToast(Context mContext, String text, int duration) {
        mHandler.removeCallbacks(r);
        if (mToast != null) {
            mToast.setText(text);
        } else {
            mToast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
        }
        mHandler.postDelayed(r, duration);

        mToast.show();
    }

    /**
     * 
     * @param mContext
     *            上下文对象
     * @param text
     *            显示的文本
     */
    public static void showToast(Context mContext, String text) {
        mHandler.removeCallbacks(r);
        if (mToast != null) {
            try {
                mToast.setText(text);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        } else {
            mToast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
        }
        mHandler.postDelayed(r, 3000);
        mToast.show();
    }

//    /** 显示Snackbar */
//    public static void showSnackbar(View view, String text) {
//        Snackbar.make(view, text, Snackbar.LENGTH_LONG).setAction("关闭", new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            }
//        }).show();
//    }

    /** 在非UI线程中显示Toast */
    public static void showThreadToast(Context Context, String text) {
        Looper.prepare();
        Toast.makeText(Context, text, Toast.LENGTH_SHORT).show();
        Looper.loop();
    }

    /** 如果未来把提示信息放在strings.xml 可以调用此方法 */
    public static void showToast(Context mContext, int resId, int duration) {
        showToast(mContext, mContext.getResources().getString(resId), duration);
    }

}
