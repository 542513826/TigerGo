package com.smartdot.mobile.portal.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.activity.LightAppActivity;
import com.smartdot.mobile.portal.bean.AppDetailBean;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 打开app做的对应操作
 */
public class openAppManage {

    private Context context;

    public openAppManage(Context context) {
        this.context = context;
    }

    /**
     * 打开app操作
     */
    public void openApp(AppDetailBean data) {
        if (data.app_web) {
            // TODO: 打开轻应用
            startWebApp(data);
        } else {
            // TODO: 打开本地应用
            if (!data.app_forced) {
                startLocalApp(data);
            } else {
                // TODO: 强制更新逻辑 (这块需要做个dialog提醒)
                downloadFile(data);
            }
        }
    }

    /**
     * 启动WebApp操作
     */
    public void startWebApp(AppDetailBean data) {
        Intent lightIntent = new Intent(context, LightAppActivity.class);
        lightIntent.putExtra("url", data.app_start);
        lightIntent.putExtra("title", data.app_name);
        context.startActivity(lightIntent);
    }

    /**
     * 启动本地App操作
     */
    public void startLocalApp(AppDetailBean data) {
        if (MyappUtil.hasAppInstalled(context, data.app_start)) {
            // 打开应用
            MyappUtil.startAnotherApp(context, data.app_start);
        } else {
            // TODO: 应用未安装逻辑
            downloadFile(data);
            CustomToast.showToast(context, data.app_name + " 没有安装在本机,将自动下载");
        }
    }

    /**
     * 删除app操作
     */
    public void deleteApp(AppDetailBean data) {
        if (data.app_web) {
            // TODO: 卸载轻应用 直接发已删除消息给服务器
            sendUninstallMessage(context, data);
        } else {
            // TODO: 卸载本地应用
            if (MyappUtil.hasAppInstalled(context, data.app_start)) {
                // 卸载应用
                GloableConfig.AppManager.Prepare_Uninstall_Package_Names.put(data.app_start, data);
                MyappUtil.removeApp(context, data.app_start);
            } else {
                // TODO: 应用未安装逻辑 直接发已删除消息给服务器
                sendUninstallMessage(context, data);
            }
        }
    }

    /** 向服务器发送当前应用的安装/卸载状态 */
    private void sendUninstallMessage(Context mContext, AppDetailBean data) {
        VolleyUtil volleyUtil = new VolleyUtil(context);
        Map<String, String> map = new HashMap<>();
        map.put("userId", GloableConfig.myUserInfo.userId);
        map.put("appId", data.app_id);
        map.put("versionId", data.app_version);
        volleyUtil.stringRequest(handler, GloableConfig.UninstallAppUrl, map, 1001);// 卸载
    }

    /**
     * 下载文件
     */
    public void downloadFile(AppDetailBean data) {
        DownloadManager manager = new DownloadManager(context);
        manager.showDownloadDialog(data.app_downUrl);
        // 将其填充到准备安装的应用map中
        GloableConfig.AppManager.Prepare_Install_Package_Names.put(data.app_start, data);
    }

    /**
     * 打开已经下载下来的应用的安装界面
     */
    public void openApkFile(File f) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        String type = "application/vnd.android.package-archive";
        intent.setDataAndType(Uri.fromFile(f), type);
        context.startActivity(intent);
    }

    /**
     * 初始化，保存的文件信息
     */
    private String initFile(String file_name) {
        File file = null;
        File parent = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // 存在SD卡 //sdcard/
            parent = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/"
                    + GloableConfig.AppManager.LocalFolderName);
        } else {
            // 不存在
            parent = new File(
                    Environment.getDataDirectory().getAbsolutePath() + "/" + GloableConfig.AppManager.LocalFolderName);
        }
        if (!parent.exists()) {
            parent.mkdirs();
        }
        try {
            file = new File(parent, file_name);
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.getPath();
    }

    /** 接收服务器返回的结果 */
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1001) {
                try {
                    JSONObject result = new JSONObject(msg.obj.toString());
                    JSONObject json = result.getJSONObject("returnValueObject");
                    int resultCode = json.getInt("resultCode");
                    if (resultCode == 200) {
                        L.v("向服务器发送安装/卸载状态成功");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

}
