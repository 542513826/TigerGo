package com.smartdot.mobile.portal.bean;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

/**
 * app信息的bean
 * Created by zhangt on 2016/7/25.
 */
public class MyAppInfoBean implements Serializable{

    /**应用ID*/
    @Expose
    public String app_id;

    /**应用名字*/
    @Expose
    public String app_name;

    /**下载地址*/
    @Expose
    public String app_downUrl;

    /**应用启动包名*/
    @Expose
    public String app_start;

    /**图标地址*/
    @Expose
    public String app_icon;

    /**应用类别*/
    @Expose
    public String app_type;

    /**评论人数*/
    @Expose
    public String app_markNum;

    /**星数*/
    @Expose
    public double app_startNum;

    /**是否更新*/
    @Expose
    public Boolean app_update;

    /**是否强制更新*/
    @Expose
    public Boolean app_forced;

    /**是否轻应用*/
    @Expose
    public Boolean app_web;

    /**是否已安装轻应用*/
    @Expose
    public Boolean app_webSetup;

    /**是否互联网应用*/
    @Expose
    public Boolean app_internet;

}
