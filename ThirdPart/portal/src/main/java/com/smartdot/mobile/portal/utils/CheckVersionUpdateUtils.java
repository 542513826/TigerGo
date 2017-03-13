package com.smartdot.mobile.portal.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.smartdot.mobile.portal.abconstant.GloableConfig;

/** 检测版本更新的工具类 */
public class CheckVersionUpdateUtils {
	public static CheckVersionUpdateUtils updateUtils = null;

    private static Context context;

    public static CheckVersionUpdateUtils createCheckVersionUpdateUtils(Context tcontext) {
        context = tcontext;
        if (updateUtils == null)
            updateUtils = new CheckVersionUpdateUtils();
        return updateUtils;
    }

    /** 获取最新版本信息进行比较并下载 (应用每次启动专用 不会提示当前是最新版本) */
    public void getVersionInfoCompareStart(String Verion, String downloadUrl) {
        String versionName = getCurrentAppVersion(context);
        int a = compareToVersion(Verion, versionName);
        if (a == 1) {
            downloadLastedApp(downloadUrl);
        }
    }

    /** 获取最新版本信息进行比较并下载 */
    public void getVersionInfoCompare(String Verion, String downloadUrl) {
        String versionName = getCurrentAppVersion(context);
        int a = compareToVersion(Verion, versionName);
        if (a == 1) {
            downloadLastedApp(downloadUrl);
        } else {
            CustomToast.showToast(context, "当前已是最新版本!");
        }
    }

    /** 下载新的版本 */
    public void downloadLastedApp(final String downloadUrl) {
        new AlertDialog.Builder(context).setTitle("更新提醒 ").setMessage("有新版本，是否更新？")
                .setPositiveButton("确定", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DownloadManager manager = new DownloadManager(context);
                        manager.showDownloadDialog(GloableConfig.BaseUrl);
                        dialog.dismiss();
                    }
                }).setNegativeButton("取消", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                }).show();
    }

    /** 获取系统当前的版本号 */
    public String getCurrentAppVersion(Context context) {
        PackageManager manager = context.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return info.versionName;
    }

    /***
     * 版本号比较
     * 
     * @param serverVersion
     *            是从服务器返回回来的版本号
     * @param localVersion
     *            是从本地Me获取的版本号
     * @return 1表示大于；0表示相等；-1表示小于。
     */
    public int compareToVersion(String serverVersion, String localVersion) {
        try {
            float server = Float.parseFloat(serverVersion.trim());
            float local = Float.parseFloat(localVersion.trim());
            if (server > local)
                return 1;
            if (server < local)
                return -1;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
