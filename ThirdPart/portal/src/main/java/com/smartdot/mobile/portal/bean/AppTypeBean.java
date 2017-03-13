package com.smartdot.mobile.portal.bean;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

/**
 * 应用类别bean
 * Created by zhangt on 2016/7/28.
 */
public class AppTypeBean implements Serializable {

    /** 类别id */
    @Expose
    public String type_id;

    /** 类别名称 */
    @Expose
    public String type_name;

    /** 类别图标 */
    @Expose
    public String type_pic;

}
