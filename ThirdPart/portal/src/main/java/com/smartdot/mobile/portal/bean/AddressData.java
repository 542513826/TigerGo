package com.smartdot.mobile.portal.bean;

import com.google.gson.annotations.Expose;

import java.util.List;

public class AddressData {

    /** 部门列表 */
    @Expose
    public List<AddressDept> deptList;

    /** 联系人列表 */
    @Expose
    public List<AddressUser> userList;

}
