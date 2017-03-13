package com.smartdot.mobile.portal.bean;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

/**
 * 用户评论的bean Created by zhangt on 2016/7/26.
 */
public class UserCommentBean implements Serializable {

    /** 评论标题 */
    @Expose
    public String commentTitle;

    /** 评论内容 */
    @Expose
    public String commentValue;

    /** 评论用户 */
    @Expose
    public String commentUserName;

    /** 评论星级 满星5分 格式只有[1~5]和[1~5.5] 2种 */
    @Expose
    public String commentStar;

    /** 评论日期 (x年x月x日) */
    @Expose
    public String commentDate;

}
