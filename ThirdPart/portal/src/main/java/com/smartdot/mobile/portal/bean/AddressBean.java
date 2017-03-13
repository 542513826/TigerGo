package com.smartdot.mobile.portal.bean;

import com.google.gson.annotations.Expose;

public class AddressBean {
    /** 当前是否处于选择状态*/
    @Expose
    public boolean isClick;

    /** 是否部门 */
    @Expose
    public boolean isDept;

    /** 所属部门ID */
    @Expose
    public String obey_dept_id;

    /** 用户ID */
    @Expose
    public String user_id;

    /** 用户姓名 */
    @Expose
    public String user_name;

    /** 用户头像地址 */
    @Expose
    public String user_portrait;

    /** 部门ID */
    @Expose
    public String dept_id;

    /** 部门名字 */
    @Expose
    public String dept_name;

    /** 部门头像地址 */
    @Expose
    public String dept_pic;

}
