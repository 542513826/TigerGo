package com.smartdot.mobile.portal.bean;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

/**
 * 应用更新的bean
 */
public class UpDateBean implements Serializable {
    /** 版本号 */
    @Expose
    public String version;

    /** 下载地址 */
    @Expose
    public String downloadURL;
}
