package com.smartdot.mobile.portal.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.bean.AppDetailBean;
import com.smartdot.mobile.portal.utils.L;
import com.smartdot.mobile.portal.utils.VolleyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 监听应用的安装、删除等等
 */
public class HandleAppReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // 接收安装广播
        if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
            String packageName = intent.getDataString().split(":")[1];
            if (GloableConfig.AppManager.Prepare_Install_Package_Names.containsKey(packageName)) {
                AppDetailBean data = GloableConfig.AppManager.Prepare_Install_Package_Names.get(packageName);
                L.v("安装的应用ID:" + data.app_id);
                L.v("安装的应用版本:" + data.app_version);
                L.v("安装的应用名字:" + data.app_name);
                L.v("安装的用户ID:" + GloableConfig.myUserInfo.userId);
                sendMessage(context, data, 1);
            }
            L.v("安装了:" + packageName + "包名的程序");
        }
        // 接收卸载广播
        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
            String packageName = intent.getDataString().split(":")[1];
            if (GloableConfig.AppManager.Prepare_Uninstall_Package_Names.containsKey(packageName)) {
                AppDetailBean data = GloableConfig.AppManager.Prepare_Uninstall_Package_Names.get(packageName);
                L.v("卸载的应用ID:" + data.app_id);
                L.v("卸载的应用版本:" + data.app_version);
                L.v("卸载的应用名字:" + data.app_name);
                L.v("卸载的用户ID:" + GloableConfig.myUserInfo.userId);
                sendMessage(context, data, 2);
            }
            L.v("卸载了:" + packageName + "包名的程序");
        }
    }

    /** 向服务器发送当前应用的安装/卸载状态 */
    public void sendMessage(Context mContext, AppDetailBean data, int state) {
        VolleyUtil volleyUtil = new VolleyUtil(mContext);
        Map<String, String> map = new HashMap<>();
        map.put("userId", GloableConfig.myUserInfo.userId);
        map.put("appId", data.app_id);
        map.put("versionId", data.app_version);
        if (state == 1) {
            volleyUtil.stringRequest(handler, GloableConfig.SetupAppUrl, map, 1001);// 安装
        } else if (state == 2) {
            volleyUtil.stringRequest(handler, GloableConfig.UninstallAppUrl, map, 1001);// 卸载
        }
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
