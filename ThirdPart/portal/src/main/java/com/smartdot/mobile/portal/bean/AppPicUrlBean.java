package com.smartdot.mobile.portal.bean;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

/**
 * 应用截图的bean
 */
public class AppPicUrlBean implements Serializable {
    /** 应用截图url */
    @Expose
    public String pic_url;
}
