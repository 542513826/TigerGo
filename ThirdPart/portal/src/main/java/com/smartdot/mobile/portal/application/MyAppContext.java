package com.smartdot.mobile.portal.application;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.View;

import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.activity.AMAPLocationActivity;
import com.smartdot.mobile.portal.activity.BaseActivity;
import com.smartdot.mobile.portal.bean.UserInfoBean;
import com.smartdot.mobile.portal.provider.ContactsProvider;
import com.smartdot.mobile.portal.provider.GroupProvider;
import com.smartdot.mobile.portal.utils.CustomToast;
import com.smartdot.mobile.portal.utils.RongUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.RongIM.ConversationListBehaviorListener;
import io.rong.imkit.model.GroupUserInfo;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.widget.provider.CameraInputProvider;
import io.rong.imkit.widget.provider.ImageInputProvider;
import io.rong.imkit.widget.provider.InputProvider;
import io.rong.imkit.widget.provider.LocationInputProvider;
import io.rong.imkit.widget.provider.TextInputProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;

/**
 * 融云相关监听，事件整合类 Created by Administrator on 2016/7/11.
 */
public class MyAppContext implements ConversationListBehaviorListener, RongIMClient.OnReceiveMessageListener,
        RongIM.UserInfoProvider, RongIM.GroupInfoProvider, RongIM.GroupUserInfoProvider,
        RongIMClient.ConnectionStatusListener, RongIM.LocationProvider, RongIM.ConversationBehaviorListener {

    public static final String UPDATEFRIEND = "updatefriend";

    public static final String UPDATEREDDOT = "updatereddot";

    public static String NETUPDATEGROUP = "netupdategroup";

    private Context mContext;

    private static MyAppContext mRongCloudInstance;

    private LocationCallback mLastLocationCallback;

    private Stack<Map<String, Activity>> mActivityStack;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case 1:
                CustomToast.showToast(mContext, mContext.getString(R.string.force_tape_out));
                Intent it = mContext.getPackageManager()
                        .getLaunchIntentForPackage(mContext.getPackageName());
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(it);
                BaseActivity._instance.exitAllAct();
                break;

            default:
                break;
            }
        };
    };

    public MyAppContext(Context mContext) {
        this.mContext = mContext;
        initListener();
        mActivityStack = new Stack<>();
    }

    /**
     * 初始化 RongCloud.
     *
     * @param context
     *            上下文。
     */
    public static void init(Context context) {

        if (mRongCloudInstance == null) {

            synchronized (MyAppContext.class) {

                if (mRongCloudInstance == null) {
                    mRongCloudInstance = new MyAppContext(context);
                }
            }
        }

    }

    public boolean pushActivity(Conversation.ConversationType conversationType, String targetId, Activity activity) {
        if (conversationType == null || targetId == null || activity == null)
            return false;

        String key = conversationType.getName() + targetId;
        Map<String, Activity> map = new HashMap<>();
        map.put(key, activity);
        mActivityStack.push(map);
        return true;
    }

    public boolean popActivity(Conversation.ConversationType conversationType, String targetId) {
        if (conversationType == null || targetId == null)
            return false;

        String key = conversationType.getName() + targetId;
        Map<String, Activity> map = mActivityStack.peek();
        if (map.containsKey(key)) {
            mActivityStack.pop();
            return true;
        }
        return false;
    }

    public boolean containsInQue(Conversation.ConversationType conversationType, String targetId) {
        if (conversationType == null || targetId == null)
            return false;

        String key = conversationType.getName() + targetId;
        Map<String, Activity> map = mActivityStack.peek();
        return map.containsKey(key);
    }

    /**
     * 获取RongCloud 实例。
     *
     * @return RongCloud。
     */
    public static MyAppContext getInstance() {
        return mRongCloudInstance;
    }

    /**
     * init 后就能设置的监听
     */
    private void initListener() {
        RongIM.setConversationBehaviorListener(this);// 设置会话界面操作的监听器。
        RongIM.setConversationListBehaviorListener(this);
        RongIM.setUserInfoProvider(this, true);
        RongIM.setGroupInfoProvider(this, true);
        RongIM.setLocationProvider(this);// 设置地理位置提供者,不用位置的同学可以注掉此行代码

        RongIM.setGroupUserInfoProvider(this, true);
        // RongIM.getInstance().setMessageAttachedUserInfo(true);// 消息体内是否有
        // userinfo 这个属性
        // RongIM.getInstance().enableNewComingMessageIcon(true);// 显示新消息提醒
        // RongIM.getInstance().enableUnreadMessageIcon(true);// 显示未读消息数目

        setInputProvider();
        // setUserInfoEngineListener();
    }

    private void setInputProvider() {

        RongIM.setOnReceiveMessageListener(this);
        RongIM.setConnectionStatusListener(this);

        // 扩展功能自定义

        InputProvider.ExtendProvider[] singleProvider = { new ImageInputProvider(RongContext.getInstance()), // 图片
                new CameraInputProvider(RongContext.getInstance()), // 相机
                // new VoIPInputProvider(RongContext.getInstance()),// 语音通话
                // new ContactsProvider(RongContext.getInstance()), // 通讯录
                new LocationInputProvider(RongContext.getInstance()), // 地理位置
                new GroupProvider(RongContext.getInstance()),// 群名片

        };

        InputProvider.ExtendProvider[] muiltiProvider = { new ImageInputProvider(RongContext.getInstance()), // 图片
                new CameraInputProvider(RongContext.getInstance()), // 相机
                // new VoIPInputProvider(RongContext.getInstance()),// 语音通话
                // new ContactsProvider(RongContext.getInstance()), // 通讯录
                new LocationInputProvider(RongContext.getInstance()), // 地理位置
                new GroupProvider(RongContext.getInstance()),// 群名片
        };

        RongIM.resetInputExtensionProvider(Conversation.ConversationType.PRIVATE, singleProvider);
        RongIM.resetInputExtensionProvider(Conversation.ConversationType.DISCUSSION, muiltiProvider);
        RongIM.resetInputExtensionProvider(Conversation.ConversationType.CUSTOMER_SERVICE, muiltiProvider);
        RongIM.resetInputExtensionProvider(Conversation.ConversationType.GROUP, muiltiProvider);
    }

    /**
     * 连接成功注册。
     * <p>
     * 在RongIM-connect-onSuc cess后调用。
     */
    public void setOtherListener() {

        RongIM.setOnReceiveMessageListener(this);
        RongIM.setConnectionStatusListener(this);

        TextInputProvider textInputProvider = new TextInputProvider(RongContext.getInstance());
        RongIM.setPrimaryInputProvider(textInputProvider);

        InputProvider.ExtendProvider[] singleProvider = { new ImageInputProvider(RongContext.getInstance()), // 图片
                new CameraInputProvider(RongContext.getInstance()), // 相机
                // new VoIPInputProvider(RongContext.getInstance()),// 语音通话
                new ContactsProvider(RongContext.getInstance()), // 通讯录
                new LocationInputProvider(RongContext.getInstance()),// 地理位置

        };

        InputProvider.ExtendProvider[] muiltiProvider = { new ImageInputProvider(RongContext.getInstance()), // 图片
                new CameraInputProvider(RongContext.getInstance()), // 相机
                // new VoIPInputProvider(RongContext.getInstance()),// 语音通话
                new ContactsProvider(RongContext.getInstance()), // 通讯录
                new LocationInputProvider(RongContext.getInstance()),// 地理位置
        };

        RongIM.resetInputExtensionProvider(Conversation.ConversationType.PRIVATE, singleProvider);
        RongIM.resetInputExtensionProvider(Conversation.ConversationType.DISCUSSION, muiltiProvider);
        RongIM.resetInputExtensionProvider(Conversation.ConversationType.GROUP, muiltiProvider);
        RongIM.resetInputExtensionProvider(Conversation.ConversationType.CUSTOMER_SERVICE, muiltiProvider);
        RongIM.resetInputExtensionProvider(Conversation.ConversationType.CHATROOM, muiltiProvider);
    }

    @Override
    public boolean onConversationPortraitClick(Context context, Conversation.ConversationType conversationType,
            String s) {
        return false;
    }

    @Override
    public boolean onConversationPortraitLongClick(Context context, Conversation.ConversationType conversationType,
            String s) {
        return false;
    }

    @Override
    public boolean onConversationLongClick(Context context, View view, UIConversation uiConversation) {
        return false;
    }

    @Override
    public boolean onConversationClick(Context context, View view, UIConversation uiConversation) {
        RongUtil.startChat(context, uiConversation.getConversationType(), uiConversation.getConversationTargetId(),
                uiConversation.getUIConversationTitle());
        return true;
    }

    @Override
    public GroupUserInfo getGroupUserInfo(String groupId, String userId) {
        // return GroupUserInfoEngine.getInstance(mContext).startEngine(groupId,
        // userId);
        return null;
    }

    @Override
    public void onChanged(ConnectionStatus connectionStatus) {
        switch (connectionStatus) {

        case CONNECTED:// 连接成功。

            break;
        case DISCONNECTED:// 断开连接。

            break;
        case CONNECTING:// 连接中。

            break;
        case NETWORK_UNAVAILABLE:// 网络不可用。

            break;
        case KICKED_OFFLINE_BY_OTHER_CLIENT:// 用户账户在其他设备登录，本机会被踢掉线
            handler.sendEmptyMessage(1);
            break;
        }
    }

    @Override
    public void onStartLocation(Context context, LocationCallback locationCallback) {
        /**
         * demo 代码 开发者需替换成自己的代码。
         */
        // TODO: 2016/7/11 开始定位
        System.out.println("------------ 开始定位 -------------");
        MyAppContext.getInstance().setLastLocationCallback(locationCallback);
        Intent intent = new Intent(context, AMAPLocationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

    }

    /**
     * 用户头像的点击事件
     *
     * @param context
     * @param conversationType
     * @param userInfo
     * @return
     */
    @Override
    public boolean onUserPortraitClick(Context context, Conversation.ConversationType conversationType,
            UserInfo userInfo) {
        // if (userInfo != null) {
        // Intent intent = new Intent(context, ChatUserInfoActivity.class);
        // intent.putExtra("conversationType", conversationType.getValue());
        // intent.putExtra("userinfo", userInfo);
        // context.startActivity(intent);
        // }
        System.out.println("点击了头像");
        return true;
    }

    @Override
    public boolean onUserPortraitLongClick(Context context, Conversation.ConversationType conversationType,
            UserInfo userInfo) {
        return false;
    }

    /**
     * 聊天消息的点击事件
     *
     * @param context
     * @param view
     * @param message
     * @return
     */
    @Override
    public boolean onMessageClick(final Context context, final View view, final Message message) {

        // /**
        // * demo 代码 开发者需替换成自己的代码。
        // */
        // if (message.getContent() instanceof LocationMessage) {
        // System.out.println("点击了地理位置");
        // Intent intent = new Intent(context, AMAPLocationActivity.class);
        // intent.putExtra("location", message.getContent());
        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // context.startActivity(intent);
        // } else if (message.getContent() instanceof ImageMessage) {
        // // Intent intent = new Intent(context, PhotoActivity.class);
        // // intent.putExtra("message", message);
        // // context.startActivity(intent);
        // }

        return false;
    }

    @Override
    public boolean onMessageLinkClick(Context context, String s) {
        return false;
    }

    @Override
    public boolean onMessageLongClick(Context context, View view, Message message) {
        return false;
    }

    public LocationCallback getLastLocationCallback() {
        return mLastLocationCallback;
    }

    public void setLastLocationCallback(LocationCallback lastLocationCallback) {
        this.mLastLocationCallback = lastLocationCallback;
    }

    @Override
    public Group getGroupInfo(String s) {
        return null;
    }

    @Override
    public boolean onReceived(Message message, int i) {
        return false;
    }

    @Override
    public UserInfo getUserInfo(String s) {
        UserInfoBean userInfoBean = GloableConfig.allUserMap.get(s);
        if (userInfoBean != null) {
            UserInfo userInfo = new UserInfo(userInfoBean.userId, userInfoBean.userName,
                    Uri.parse(userInfoBean.portraitUri));
            return userInfo;
        } else {
            return null;
        }

    }

}
