package com.smartdot.mobile.portal.bean;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

import io.rong.imlib.model.Conversation;

/**
 * 常用联系人列表处使用的bean，保存聊天信息等
 * Created by Administrator on 2016/7/15.
 */
public class UserInfoBean implements Serializable{

    @Expose
    public String userId;

    /** 姓名*/
    @Expose
    public String userName;

    /** 头像*/
    @Expose
    public String portraitUri = "";

    /** 部门id*/
    @Expose
    public String obey_dept_id;

    /** 会话类型*/
    @Expose
    public Conversation.ConversationType conversationType;

    /** 是否为群主*/
    @Expose
    public boolean isManager;

    //以下为服务器登录的时候返回的额外数据

    @Expose
    public int companyCode;

    @Expose
    public String created;

    @Expose
    public String email;

    @Expose
    public String empNo;

    @Expose
    public int enabled;

    @Expose
    public String isBlue;

    @Expose
    public int limit;

    @Expose
    public String mobile;

    @Expose
    public int orgCode;

    @Expose
    public int passwdStatus;

    @Expose
    public String password;

    @Expose
    public int start;

    @Expose
    public int userType;

}
