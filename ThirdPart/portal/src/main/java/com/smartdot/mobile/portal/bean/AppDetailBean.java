package com.smartdot.mobile.portal.bean;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.List;

/**
 * 应用详情的bean Created by Administrator on 2016/7/26.
 */
public class AppDetailBean implements Serializable {
    /** 是否可拖拽 */
    public boolean isDraggable;

    /** 是否能删除 [true 能、false 不能]*/
    @Expose
    public boolean isDelete;

    /** 安装状态 [0 已安装、1 待更新、2 未安装] */
    @Expose
    public String app_Setup;

    /**是否强制更新*/
    @Expose
    public Boolean app_forced;

    /** 应用ID */
    @Expose
    public String app_id;

    /** 应用名字 */
    @Expose
    public String app_name;

    /** 下载地址 */
    @Expose
    public String app_downUrl;

    /** 应用启动包名 */
    @Expose
    public String app_start;

    /** 图标地址 */
    @Expose
    public String app_icon;

    /** 应用类别 */
    @Expose
    public String app_type;

    /** 评论人数 */
    @Expose
    public int app_markNum;

    /** 星数 */
    @Expose
    public float app_startNum;

    /** 是否更新 */
    @Expose
    public Boolean app_update;

    /** 是否轻应用 */
    @Expose
    public Boolean app_web = false;

    /** 是否已安装轻应用 */
    @Expose
    public Boolean app_webSetup;

    /** 是否互联网应用 */
    @Expose
    public Boolean app_internet;

    /** 应用介绍 */
    @Expose
    public String app_introduce;

    /** 用户评论*/
    @Expose
    public List<UserCommentBean> commentList;

    /** 应用评分*/
    @Expose
    public AppGradeBean gradeInfo;

    /** 应用信息*/
    @Expose
    public AppInfoBean app_info;

    /** 应用版本*/
    @Expose
    public String app_version;

    /** 应用截图*/
    @Expose
    public List<AppPicUrlBean> app_screenshot;
}
