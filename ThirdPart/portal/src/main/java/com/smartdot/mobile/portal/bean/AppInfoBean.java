package com.smartdot.mobile.portal.bean;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

/**
 * 应用信息的bean-用于应用详情
 * Created by zhangt on 2016/7/26.
 */
public class AppInfoBean implements Serializable{

    /** 应用类别*/
    @Expose
    public String type;

    /** 更新日期 (x年x月x日)*/
    @Expose
    public String updateDate;

    /** 应用版本*/
    @Expose
    public String version;

    /** 应用大小*/
    @Expose
    public String size;

    /** 兼容性*/
    @Expose
    public String compatibility;

    /** 应用语言*/
    @Expose
    public String language;


}
