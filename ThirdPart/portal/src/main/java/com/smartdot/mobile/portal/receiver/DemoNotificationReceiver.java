package com.smartdot.mobile.portal.receiver;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.activity.PortalMainActivity;

import io.rong.push.notification.PushMessageReceiver;
import io.rong.push.notification.PushNotificationMessage;

/**
 * 处理融云接收推送的广播
 */
public class DemoNotificationReceiver extends PushMessageReceiver {

    @Override
    public boolean onNotificationMessageArrived(Context context, PushNotificationMessage message) {
        if (GloableConfig.RongCloud.isOneSender) {
            if (GloableConfig.RongCloud.isFirstTime) {
                GloableConfig.RongCloud.PushID = message.getTargetId();
                GloableConfig.RongCloud.isFirstTime = false;
            } else {
                if (!GloableConfig.RongCloud.PushID.equals(message.getTargetId())) {
                    GloableConfig.RongCloud.isOneSender = false;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onNotificationMessageClicked(Context context, PushNotificationMessage message) {

        /** 收到推送消息中包含的内容 */
        // System.out.println("1" + message.getConversationType().getName());
        // System.out.println("2" + message.getExtra());
        // System.out.println("3" + message.getObjectName());
        // System.out.println("4" + message.getPushContent());
        // System.out.println("7" + message.getTargetId());
        // System.out.println("8" + message.getTargetUserName());
        // System.out.println("9" + message.getSenderId());
        // System.out.println("10" + message.getSenderName());
        // System.out.println("11" + message.getSenderPortrait());

        if (message.getConversationType().getName().equals("push_service")) {
            // TODO: 2016/7/18 这是服务器推送过来的东西
            return false;
        }

        if (GloableConfig.RongCloud.isOneSender) {
            // 单个的会话
            System.out.println("收到了单条推送");
            Intent intent = context.getPackageManager()
                    .getLaunchIntentForPackage(context.getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Intent intent1 = new Intent(context, PortalMainActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = Uri.parse("rong://" + GloableConfig.CURRENT_PKGNAME).buildUpon()
                    .appendPath("conversation")
                    .appendPath(message.getConversationType().getName().toLowerCase())
                    .appendQueryParameter("targetId", message.getTargetId())
                    .appendQueryParameter("title", message.getTargetUserName())
                    // .appendQueryParameter("isFromPush","true")
                    .build();
            Intent intent2 = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivities(new Intent[] { intent, intent1, intent2 });
            return true;
        } else {
            // 多人多会话推送
            System.out.println("收到了多条推送");
            Intent intent1 = new Intent(context, PortalMainActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(intent1);
            return true;
        }

    }

}
