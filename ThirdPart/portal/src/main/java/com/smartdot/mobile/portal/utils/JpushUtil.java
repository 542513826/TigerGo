package com.smartdot.mobile.portal.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.smartdot.mobile.portal.PortalApplication;
import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.jpush.android.api.JPushInterface;

public class JpushUtil {
    public static final String PREFS_NAME = "JPUSH_EXAMPLE";

    public static final String PREFS_DAYS = "JPUSH_EXAMPLE_DAYS";

    public static final String PREFS_START_TIME = "PREFS_START_TIME";

    public static final String PREFS_END_TIME = "PREFS_END_TIME";

    public static final String KEY_APP_KEY = "JPUSH_APPKEY";

    public static boolean isEmpty(String s) {
        if (null == s)
            return true;
        if (s.length() == 0)
            return true;
        if (s.trim().length() == 0)
            return true;
        return false;
    }

    // 校验Tag Alias 只能是数字,英文字母和中文
    public static boolean isValidTagAndAlias(String s) {
        Pattern p = Pattern.compile("^[\u4E00-\u9FA50-9a-zA-Z_!@#$&*+=.|￥¥]+$");
        Matcher m = p.matcher(s);
        return m.matches();
    }

    // 取得AppKey
    public static String getAppKey(Context context) {
        Bundle metaData = null;
        String appKey = null;
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            if (null != ai)
                metaData = ai.metaData;
            if (null != metaData) {
                appKey = metaData.getString(KEY_APP_KEY);
                if ((null == appKey) || appKey.length() != 24) {
                    appKey = null;
                }
            }
        } catch (NameNotFoundException e) {

        }
        return appKey;
    }

    // 取得版本号
    public static String GetVersion(Context context) {
        try {
            PackageInfo manager = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return manager.versionName;
        } catch (NameNotFoundException e) {
            return "Unknown";
        }
    }

    /**
     * 获取设备表示码
     *
     * @param context
     * @return
     */
    public static String getDeviceId(Context context) {
        String deviceId = JPushInterface.getUdid(context);

        return deviceId;
    }

    /**
     * 显示Toast
     *
     * @param toast
     * @param context
     */
    public static void showToast(final String toast, final Context context) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }).start();
    }

    /**
     * 判断极光是否连接
     *
     * @param context
     * @return
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = conn.getActiveNetworkInfo();
        return (info != null && info.isConnected());
    }

    /**
     * 获取Imei值
     *
     * @param context
     * @param imei
     * @return
     */
    public static String getImei(Context context, String imei) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            imei = telephonyManager.getDeviceId();
        } catch (Exception e) {
            Log.e(JpushUtil.class.getSimpleName(), e.getMessage());
        }
        return imei;
    }

    /**
     * 向服务器注册极光推送信息
     *
     * @param mContext
     */
    public static void registerJpush(Context mContext) {
        registerJpush(mContext, new Handler(), 10000);
    }

    /**
     * 向服务器注册极光推送信息 默认不提示[当前无网络]，如果需要，可自行在外层判断
     *
     * @param mContext
     * @param handler  处理服务器返回的信息及注册成功或失败之后的相关操作
     * @param what
     */
    public static void registerJpush(Context mContext, Handler handler, int what) {
        if (!NetUtils.isConnected(mContext)) {
            // 在首页中注册极光推送，如果无网络也不给出任何提示
            return;
        }
        String deviceToken = JPushInterface.getRegistrationID(mContext);
        if (!StringUtils.isNull(deviceToken)) {
            GloableConfig.JpushAppId = PortalApplication.getApplication().getApplicationContext().getResources()
                    .getString(R.string.jpushAppId);
            VolleyUtil volleyUtil = new VolleyUtil(mContext);
            Map<String, String> map = new HashMap<>();
            map.put("deviceToken", deviceToken);
            map.put("userId", GloableConfig.myUserInfo.userId);
            map.put("appId", GloableConfig.JpushAppId);
            volleyUtil.stringRequest(handler, GloableConfig.registerJpushInfpUrl, map, what);
        }
    }

    /**
     * 注销极光操作，只发出请求，不判断请求是否成功
     *
     * @param context
     */
    public static void logoutJpush(Context context) {
        logoutJpush(context, new Handler(), 10000);
        ProgressUtil.dismissProgressDialog();
    }

    /**
     * 注销极光推送
     *
     * @param mContext
     * @param handler  在handler中需要做的操作 1、隐藏进度条 2、移除SharePreference中的信息 3、退出程序并跳转到登录页面
     * @param what
     */
    public static void logoutJpush(Context mContext, Handler handler, int what) {
        if (!NetUtils.isConnected(mContext)) {
            CustomToast.showToast(mContext, mContext.getString(R.string.no_internet), 400);
            return;
        }
        ProgressUtil.showPregressDialog(mContext, R.layout.custom_progress);
        String deviceToken = JPushInterface.getRegistrationID(mContext);
        GloableConfig.JpushAppId = PortalApplication.getApplication().getApplicationContext().getResources()
                .getString(R.string.jpushAppId);
        VolleyUtil volleyUtil = new VolleyUtil(mContext);
        Map<String, String> map = new HashMap<>();
        map.put("deviceToken", deviceToken);
        map.put("userId", GloableConfig.myUserInfo.userId);
        map.put("appId", GloableConfig.JpushAppId);
        volleyUtil.stringRequest(handler, GloableConfig.logoutJpush, map, what);
    }
}
