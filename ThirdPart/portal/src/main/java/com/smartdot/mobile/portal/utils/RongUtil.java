package com.smartdot.mobile.portal.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.abconstant.GlobleAddressConfig;
import com.smartdot.mobile.portal.activity.PortalMainActivity;
import com.smartdot.mobile.portal.bean.GroupInfoBean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;
import io.rong.message.InformationNotificationMessage;

/**
 * Created by Administrator on 2016/7/27.
 */
public class RongUtil {

    List<GroupInfoBean> groupList = new ArrayList<>();

    /**
     * 连接融云服务器
     */
    public static void RongConnect(final Context mContext, String token) {
        RongIM.connect(token, new RongIMClient.ConnectCallback() {
            @Override
            public void onTokenIncorrect() {
                L.v("连接融云服务器失败，token失效");
                ProgressUtil.dismissProgressDialog();
            }

            @Override
            public void onSuccess(String s) {
                L.v("连接融云服务器成功，融云返回的id--------" + s);
                // 设置当前用户信息
                if (RongIM.getInstance() != null) {
                    RongIM.getInstance().setCurrentUserInfo(new UserInfo(s, GloableConfig.myUserInfo.userName, null));
                    // 设置消息体内是否携带用户信息
                    RongIM.getInstance().setMessageAttachedUserInfo(true);
                }
                Intent intent = new Intent(mContext, PortalMainActivity.class);
                mContext.startActivity(intent);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                L.v("onError errorcode:" + errorCode.getValue());
            }
        });
    }

    /**
     * 发起聊天
     * @param mContext
     * @param type
     * @param targetId
     * @param title
     */
    public static void startChat(Context mContext, Conversation.ConversationType type, String targetId, String title) {
        if (mContext != null && !TextUtils.isEmpty(targetId)) {
            if (RongContext.getInstance() == null) {
                throw new ExceptionInInitializerError("RongCloud SDK not init");
            } else {
                Uri uri = Uri.parse("rong://" + GloableConfig.CURRENT_PKGNAME).buildUpon().appendPath("conversation")
                        .appendPath(type.getName().toLowerCase()).appendQueryParameter("targetId", targetId)
                        .appendQueryParameter("title", title).build();
                mContext.startActivity(new Intent("android.intent.action.VIEW", uri));
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * 创建个群组
     *
     * @param groupName
     *            群名称
     */
    public static void createGroup(Context mContext, String groupName, Handler handler, int what) {
        // 创建人
        String userId = GloableConfig.myUserInfo.userId;
        // 人员
        String userIds = "";
        for (Map.Entry<String, String> entry : GlobleAddressConfig.selectedPersonIDs.entrySet()) {
            userIds = userIds + "," + entry.getKey();
        }
        userIds = userId + userIds;

        // 群组id
        Calendar calendar = Calendar.getInstance();
        String groupId = calendar.get(Calendar.YEAR) + "" + calendar.get(Calendar.MONTH) + ""
                + calendar.get(Calendar.DAY_OF_MONTH) + "" + calendar.get(Calendar.HOUR_OF_DAY) + ""
                + calendar.get(Calendar.MINUTE) + "" + calendar.get(Calendar.SECOND) + "" + (int) (Math.random() * 10);

        // 全选部门id
        String deptIds = "";
        for (Map.Entry<String, String> entry : GlobleAddressConfig.selectedDeptIDs.entrySet()) {
            deptIds = deptIds + "," + entry.getKey();
        }
        if (deptIds.length() > 1) {
            deptIds = deptIds.substring(1, deptIds.length());
        }

        VolleyUtil volleyUtil = new VolleyUtil(mContext);
        String url = String.format(GloableConfig.RongCloud.creatGroupUrl, userIds, userId, groupId, groupName, deptIds);
        volleyUtil.stringRequest(handler, Request.Method.POST, url, what);
    }

    /**
     * 自己加群操作-是否在群组中由服务器判断
     * 
     * @param mContext
     * @param GroupId
     *            群id
     * @param handler
     * @param what
     */
    public static void addGroup(Context mContext, String GroupId, Handler handler, int what) {
        // 自己
        String userId = GloableConfig.myUserInfo.userId;

        // 人员也是自己
        String userIds = GloableConfig.myUserInfo.userId;

        // 全选部门id为空
        String deptIds = "";

        VolleyUtil volleyUtil = new VolleyUtil(mContext);
        String url = String.format(GloableConfig.RongCloud.addGroupUrl, userIds, GroupId, "", deptIds);
        volleyUtil.stringRequest(handler, Request.Method.POST, url, what);
    }

    /**
     * 拉人进群
     * 
     * @param mContext
     * @param GroupId
     *            群id
     * @param handler
     * @param what
     */
    public static void addGroupMember(Context mContext, String GroupId, Handler handler, int what) {
        // 创建人
        String userId = GloableConfig.myUserInfo.userId;

        // 人员
        String userIds = "";
        for (Map.Entry<String, String> entry : GlobleAddressConfig.selectedPersonIDs.entrySet()) {
            userIds = userIds + "," + entry.getKey();
        }
        userIds = userIds.substring(1, userIds.length());

        // 全选部门id
        String deptIds = "";
        for (Map.Entry<String, String> entry : GlobleAddressConfig.selectedDeptIDs.entrySet()) {
            deptIds = deptIds + "," + entry.getKey();
        }
        if (deptIds.length() > 1) {
            deptIds = deptIds.substring(1, deptIds.length());
        }

        VolleyUtil volleyUtil = new VolleyUtil(mContext);
        String url = String.format(GloableConfig.RongCloud.addGroupUrl, userIds, GroupId, "", deptIds);
        volleyUtil.stringRequest(handler, Request.Method.POST, url, what);
    }

    /**
     * 发送小黑条消息
     * 
     * @param mContext
     * @param targetId
     * @param type
     * @param showMessage
     */
    public static void sendInfoMessage(Context mContext, String targetId, Conversation.ConversationType type,
            String showMessage) {
        final InformationNotificationMessage infoMsg = InformationNotificationMessage.obtain(showMessage);
        if (RongIM.getInstance() != null) {
            Message message = RongIM.getInstance().sendMessage(type, targetId, infoMsg, null, null,
                    new RongIMClient.SendMessageCallback() {
                        @Override
                        public void onError(Integer integer, RongIMClient.ErrorCode errorCode) {
                            Log.d("ExtendProvider", "onError--" + errorCode);
                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            Log.d("ExtendProvider", "onSuccess--" + integer);
                        }
                    });
        }
    }

    /**
     * 删除聊天列表里的群聊会话
     *
     * @param targetId
     */
    public static void removeConversationListItem(String targetId) {
        // 清除聊天列表里的信息
        RongIM.getInstance().removeConversation(Conversation.ConversationType.GROUP, targetId,
                new RongIMClient.ResultCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {

                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode errorCode) {

                    }
                });
    }

}
