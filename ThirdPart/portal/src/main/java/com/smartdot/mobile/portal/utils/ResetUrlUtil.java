package com.smartdot.mobile.portal.utils;

import com.smartdot.mobile.portal.PortalApplication;
import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;

/**
 * 此类是对当前应用的所有接口进行重新赋值的工具类,因为根url是动态添加的
 */
public class ResetUrlUtil {

    public static void resetUrl() {
        // 读取配置文件中的根url接口
        GloableConfig.BaseUrl = PortalApplication.getApplication().getApplicationContext().getResources()
                .getString(R.string.baseUrl);

        // 读取配置文件中的融云根url接口
        GloableConfig.RongCloud.RongBaseUrl = PortalApplication.getApplication().getApplicationContext().getResources()
                .getString(R.string.rongUrl);

        /** 登录url */
        GloableConfig.LoginUrl = GloableConfig.BaseUrl + GloableConfig.LoginUrl;

        /** 登录url */
        GloableConfig.CheckUpdateUrl = GloableConfig.BaseUrl + GloableConfig.CheckUpdateUrl;

        /** 适用场景 - 已装应用/未装应用/升级应用/移除应用（appList） */
        GloableConfig.AppListUrl = GloableConfig.BaseUrl + GloableConfig.AppListUrl;

        /** 适用场景 - 我的已装应用/未装应用/升级应用/移除应用（myAppList） */
        GloableConfig.MyAppUrl = GloableConfig.BaseUrl + GloableConfig.MyAppUrl;

        /** 适用场景 - 应用评论（comment） */
        GloableConfig.AppCommentUrl = GloableConfig.BaseUrl + GloableConfig.AppCommentUrl;

        /** 适用场景 - 应用详情 */
        GloableConfig.AppDetailUrl = GloableConfig.BaseUrl + GloableConfig.AppDetailUrl;

        /** 适用场景 - 通讯录 */
        GloableConfig.AddressBookUrl = GloableConfig.BaseUrl + GloableConfig.AddressBookUrl;

        /** 适用场景 - 安装应用 */
        GloableConfig.SetupAppUrl = GloableConfig.BaseUrl + GloableConfig.SetupAppUrl;

        /** 适用场景 - 卸载应用 */
        GloableConfig.UninstallAppUrl = GloableConfig.BaseUrl + GloableConfig.UninstallAppUrl;

        /** 个人信息 */
        GloableConfig.UserinfoUrl = GloableConfig.BaseUrl + GloableConfig.UserinfoUrl;

        /** 应用类别 */
        GloableConfig.CategoryUrl = GloableConfig.BaseUrl + GloableConfig.CategoryUrl;

        /***** 融云相关 ****/

        /** 获取Token */
        GloableConfig.RongCloud.getTokenUrl = GloableConfig.RongCloud.RongBaseUrl + GloableConfig.RongCloud.getTokenUrl;

        /** 查询用户信息 */
        GloableConfig.RongCloud.getUserInfoUrl = GloableConfig.RongCloud.RongBaseUrl
                + GloableConfig.RongCloud.getUserInfoUrl;

        // /** 查询群成员 */
        // GloableConfig.RongCloud.getGroupMembersUrl
        // =GloableConfig.RongCloud.RongBaseUrl +
        // GloableConfig.RongCloud.getGroupMembersUrl;

        /** 解散群组 */
        GloableConfig.RongCloud.destroyGroupUrl = GloableConfig.RongCloud.RongBaseUrl
                + GloableConfig.RongCloud.destroyGroupUrl;

        /** 退出群组 */
        GloableConfig.RongCloud.quitGroupUrl = GloableConfig.RongCloud.RongBaseUrl
                + GloableConfig.RongCloud.quitGroupUrl;

        /** 加入群组 */
        GloableConfig.RongCloud.addGroupUrl = GloableConfig.RongCloud.RongBaseUrl + GloableConfig.RongCloud.addGroupUrl;

        /** 创建群组 */
        GloableConfig.RongCloud.creatGroupUrl = GloableConfig.RongCloud.RongBaseUrl
                + GloableConfig.RongCloud.creatGroupUrl;

        /** 修改群信息 */
        GloableConfig.RongCloud.changeGroupInfoUrl = GloableConfig.RongCloud.RongBaseUrl
                + GloableConfig.RongCloud.changeGroupInfoUrl;

        /** 修改群信息 */
        GloableConfig.RongCloud.changeGroupNameUrl = GloableConfig.RongCloud.RongBaseUrl
                + GloableConfig.RongCloud.changeGroupNameUrl;

        /** 查询群组信息 */
        GloableConfig.RongCloud.getGroupInfoUrl = GloableConfig.RongCloud.RongBaseUrl
                + GloableConfig.RongCloud.getGroupInfoUrl;

        /** 查询群组列表 */
        GloableConfig.RongCloud.getGroupListUrl = GloableConfig.RongCloud.RongBaseUrl
                + GloableConfig.RongCloud.getGroupListUrl;

        /** 极光推送向服务器注册时使用的地址 */
        GloableConfig.registerJpushInfpUrl = PortalApplication.getApplication().getApplicationContext().getResources()
                .getString(R.string.jpushUrl);

        /** 极光推送向服务器注销时使用的地址 */
        GloableConfig.logoutJpush = PortalApplication.getApplication().getApplicationContext().getResources()
                .getString(R.string.logoutjpushUrl);

    }

    /** 对一些功能开关的重新赋值 */
    public static void resetConfig() {

        /** 是否使用极光推送 */
        GloableConfig.useJpush = PortalApplication.getApplication().getApplicationContext().getResources()
                .getBoolean(R.bool.useJpush);

        /** 是否使用融云 */
        GloableConfig.RongCloud.useRong = PortalApplication.getApplication().getApplicationContext().getResources()
                .getBoolean(R.bool.useRong);
    }
}
