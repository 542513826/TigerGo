package com.smartdot.mobile.portal.application;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import com.smartdot.mobile.portal.utils.ImageLoaderUtils;

import io.rong.imkit.RongIM;
import io.rong.imlib.ipc.RongExceptionHandler;

/**
 * Created by Administrator on 2016/7/11.
 */
public class App extends Application{
    @Override
    public void onCreate() {

        super.onCreate();

        /**
         * 注意：
         *
         * IMKit SDK调用第一步 初始化
         *
         * context上下文
         *
         * 只有两个进程需要初始化，主进程和 push 进程
         */
        RongIM.init(this);
        MyAppContext.init(this);
        Thread.setDefaultUncaughtExceptionHandler(new RongExceptionHandler(this));
        ImageLoaderUtils.initConfiguration(getApplicationContext());


    }

    /**
     * 获得当前进程的名字
     *
     * @param context
     * @return
     */
    @SuppressLint("NewApi")
    public static String getCurProcessName(Context context) {

        int pid = android.os.Process.myPid();

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {

            if (appProcess.pid == pid) {

                return appProcess.processName;
            }
        }
        return null;
    }
}
