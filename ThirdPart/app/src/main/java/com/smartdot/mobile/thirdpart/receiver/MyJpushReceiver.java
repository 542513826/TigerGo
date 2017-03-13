package com.smartdot.mobile.thirdpart.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.abconstant.JPushConstants;
import com.smartdot.mobile.portal.activity.PortalMainActivity;
import com.smartdot.mobile.portal.utils.JpushUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import cn.jpush.android.api.JPushInterface;

/**
 * 自定义接收器
 *
 * 如果不定义这个 Receiver，则： 1) 默认用户会打开主界面 2) 接收不到自定义消息
 */
public class MyJpushReceiver extends BroadcastReceiver {
    private static final String TAG = "fate";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Log.i(TAG, "[MyJpushRe ceiver] onReceive - " + intent.getAction() + ", extras: " + printBundle(bundle));

        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            // 接收到Registration Id，此处有时会返回Null，如果需要使用Registration Id，
            // 请在使用时调用JPushInterface.getRegistrationID(mContext)方法获取；
            // send the Registration Id to your server...

        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            // 接收到推送下来的自定义消息
            // processCustomMessage(context, bundle);

        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            // 接收到推送下来的通知"
            int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);

        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            // 用户点击打开了通知"
            openActivity(context, bundle);

        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
            // 用户收到到富文本消息
            // 在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity，
            // 打开一个网页等..

        } else if (JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
            boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
            Log.w(TAG, "[MyJpushReceiver]" + intent.getAction() + " connected state change to " + connected);
        } else {
            Log.i(TAG, "[MyJpushReceiver] Unhandled intent - " + intent.getAction());
        }
    }

    /**
     * 点击通知打开界面
     * 
     * @param context
     * @param bundle
     */
    private void openActivity(Context context, Bundle bundle) {
        // TODO: 2016/8/31 用户点击打开了通知，打开自定义的Activity
        if (GloableConfig.isRunning) {
            Intent i = new Intent(context, PortalMainActivity.class);
            i.putExtras(bundle);
            i.putExtra("from", "jpush");
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(i);
        } else {
            Intent i = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(i);
        }
    }

    /**
     * 打印所有的 intent extra 数据
     * 
     * @param bundle
     * @return
     */
    private static String printBundle(Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) {
            if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
            } else if (key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
            } else if (key.equals(JPushInterface.EXTRA_EXTRA)) {
                if (bundle.getString(JPushInterface.EXTRA_EXTRA).isEmpty()) {
                    Log.i(TAG, "This message has no Extra data");
                    continue;
                }

                try {
                    JSONObject json = new JSONObject(bundle.getString(JPushInterface.EXTRA_EXTRA));
                    Iterator<String> it = json.keys();

                    while (it.hasNext()) {
                        String myKey = it.next().toString();
                        sb.append("\nkey:" + key + ", value: [" + myKey + " - " + json.optString(myKey) + "]");
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Get message extra JSON error!");
                }

            } else {
                sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
            }
        }
        return sb.toString();
    }

    /**
     * send msg to MainActivity
     * 
     * @param context
     * @param bundle
     */
    private void processCustomMessage(Context context, Bundle bundle) {
        if (JPushConstants.isForeground) {
            String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
            String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
            Intent msgIntent = new Intent(JPushConstants.MESSAGE_RECEIVED_ACTION);
            msgIntent.putExtra(JPushConstants.KEY_MESSAGE, message);
            if (!JpushUtil.isEmpty(extras)) {
                try {
                    JSONObject extraJson = new JSONObject(extras);
                    if (null != extraJson && extraJson.length() > 0) {
                        msgIntent.putExtra(JPushConstants.KEY_EXTRAS, extras);
                    }
                } catch (JSONException e) {

                }
            }
            context.sendBroadcast(msgIntent);
        }
    }
}
