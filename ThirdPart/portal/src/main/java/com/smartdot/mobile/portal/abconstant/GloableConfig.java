package com.smartdot.mobile.portal.abconstant;

import com.smartdot.mobile.portal.bean.AppDetailBean;
import com.smartdot.mobile.portal.bean.GroupInfoBean;
import com.smartdot.mobile.portal.bean.UserInfoBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局变量 Created by Administrator on 2016/7/12.
 */
public class GloableConfig {

    // Android：OT1,iOS：OT2,Tablet：DT1,Phone：DT2

    /** osType */
    public static final String OS_TYPE = "OT1";

    /** deviceType */
    public static final String DEVICE_TYPE = "DT2";

    /** 当前的包名 */
    public static String CURRENT_PKGNAME = "com.smartdot.mobile.portal";

    /** 根url */
    public static String BaseUrl = "请前往 SmartgoThird\\app\\src\\main\\res\\values\\config.xml 下进行修改设置";

    /** 登录url */
    public static String LoginUrl = "login?_type=json";

    /** 适用场景 - 检查更新 */
    public static String CheckUpdateUrl = "checkUpdate?_type=json";

    /** 适用场景 - 已装应用/未装应用/升级应用/移除应用（appList） */
    public static String AppListUrl = "appList?_type=json";

    /** 适用场景 - 我的已装应用/未装应用/升级应用/移除应用（myAppList） */
    public static String MyAppUrl = "myAppList?_type=json";

    /** 适用场景 - 应用评论（comment） */
    public static String AppCommentUrl = "comment?_type=json";

    /** 适用场景 - 应用详情 */
    public static String AppDetailUrl = "appDetail?_type=json";

    /** 适用场景 - 通讯录 */
    public static String AddressBookUrl = "deptuser?_type=json";

    /** 适用场景 - 安装应用 */
    public static String SetupAppUrl = "setupApp?_type=json";

    /** 适用场景 - 卸载应用 */
    public static String UninstallAppUrl = "deleteApp?_type=json";

    /** 适用场景 - 个人信息 */
    public static String UserinfoUrl = "userinfo?_type=json";

    /** 适用场景 - 应用类别 */
    public static String CategoryUrl = "category?_type=json";

    /** 常用联系人列表处使用的bean，保存聊天信息等 */
    public static UserInfoBean myUserInfo = new UserInfoBean();

    /** 全局保存请求下来的用户信息 */
    public static List<UserInfoBean> allUser = new ArrayList<>();

    /** 全局保存请求下来的用户信息map */
    public static Map<String, UserInfoBean> allUserMap = new HashMap<>();

    /** 全局保存请求下来的群组列表map */
    public static Map<String, GroupInfoBean> allGroupMap = new HashMap<>();

    /** 跳转到通讯录的时候是何种类型 0:正常，1：选人进入群组 2：创建群组 3、直接跳到聊天 4、发送群名片 */
    public static int addressBookType = 0;

    /** 应用管理 */
    public static class AppManager {
        /** 适用场景 - 本地下载目录 */
        public static String LocalFolderName = "/smartdotgo_download/";

        /** 适用场景 - 准备安装的包名集合 */
        public static Map<String, AppDetailBean> Prepare_Uninstall_Package_Names = new HashMap<>();

        /** 适用场景 - 准备卸载的包名集合 */
        public static Map<String, AppDetailBean> Prepare_Install_Package_Names = new HashMap<>();
    }

    /** 跳转到群组列表的时候是何种类型 0:整除 1:选择群名片 */
    public static int groupListType = 0;

    /** 要发送的群名片 */
    public static GroupInfoBean cardGroup;

    /************************************* 以下是融云相关 ***********************************************/

    /** 融云相关 */
    public static class RongCloud {

        /** 是否使用融云 */
        public static Boolean useRong = true;

        /** 未读消息数 */
        public static int RongYunMessage;

        /** 接收push消息是使用的全局变量 */
        public static String PushID = "";

        /** 推送的时候使用，标志是否是一个人推送的消息 */
        public static Boolean isOneSender = true;

        /** 推送的时候使用，标志是否是第一次收到推送 */
        public static Boolean isFirstTime = true;

        /** 融云服务器地址 */
        public static String RongBaseUrl = "http://101.201.76.73:9091";

        /** 获取Token */
        public static String getTokenUrl = "/smartdot-tokenserver/token/create?params={\"userID\":\"%s\",\"userName\":\"%s\",\"userPicUrl\":\"%s\"}";

        /** 查询用户信息 */
        public static String getUserInfoUrl = "/smartdot-tokenserver/user/query?params={\"userId\":\"%s\"}";

        // /** 查询群成员 */
        // public static String getGroupMembersUrl =
        // "/smartdot-tokenserver/group/query?params={\"userId\":\"\",\"groupId\":\"%s\",\"groupName\":\"\"}";

        /** 解散群组 */
        public static String destroyGroupUrl = "/smartdot-tokenserver/group/destory?params={\"userId\":\"%s\",\"groupId\":\"%s\"}";

        /** 退出群组 */
        public static String quitGroupUrl = "/smartdot-tokenserver/group/batchQuit?params={\"userIds\":\"%s\",\"groupId\":\"%s\",\"operateUserId\":\"%s\",\"operateUser\":\""
                + GloableConfig.myUserInfo.userId + "\"}";

        /** 加入群组 */
        public static String addGroupUrl = "/smartdot-tokenserver/group/batchadd?params={\"userIds\":\"%s\",\"groupId\":\"%s\",\"groupName\":\"%s\",\"deptIds\":\"%s\",\"operateUser\":\""
                + GloableConfig.myUserInfo.userId + "\"}";

        /** 创建群组 */
        public static String creatGroupUrl = "/smartdot-tokenserver/group/create?params={\"userIds\":\"%s\",\"userId\":\"%s\",\"groupId\":\"%s\",\"groupName\":\"%s\",\"deptIds\":\"%s\"}";

        /** 修改群信息 */
        public static String changeGroupInfoUrl = "/smartdot-tokenserver/group/refresh?params={\"groupId\":\"%s\",\"groupName\":\"%s\",\"groupMgrOldId\":\"%s\",\"groupMgrNewId\":\"%s\",\"operateUser\":\""
                + GloableConfig.myUserInfo.userId + "\"}";

        /** 修改群信息 */
        public static String changeGroupNameUrl = "/smartdot-tokenserver/group/refresh?params={\"groupId\":\"%s\",\"groupName\":\"%s\",\"operateUser\":\""
                + GloableConfig.myUserInfo.userId + "\"}";

        /** 查询群组信息 */
        public static String getGroupInfoUrl = "/smartdot-tokenserver/group/query?params={\"groupId\":\"%s\",\"operateUser\":\""
                + GloableConfig.myUserInfo.userId + "\"}";

        /** 查询群组列表 */
        public static String getGroupListUrl = "/smartdot-tokenserver/group/queryGroupByUser?params={\"userId\":\"%s\"}";

    }

    /************************************* 以上是融云相关 ***********************************************/

    /************************************* 以下是极光相关 ***********************************************/

    /** 是否使用极光推送 */
    public static Boolean useJpush = false;

    /** 向服务器注册推送信息 */
    public static String registerJpushInfpUrl = "http://172.20.96.95:8080/semp/thirdparty/oapi/push/registDevice";

    /** 注销极光信息*/
    public static String logoutJpush = "http://172.20.96.95:8080/semp/thirdparty/oapi/push/pendingDevice";

    /** 程序是否启动标志位,用于判断推送点击事件的跳转 */
    public static boolean isRunning = false;

    /** 极光，服务器所需的AppId */
    public static String JpushAppId;

    /************************************* 以上是极光相关 ***********************************************/
}
