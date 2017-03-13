package com.smartdot.mobile.portal.bean;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

/**
 * 群组成员 Created by Administrator on 2016/7/28.
 */
public class GroupMemberBean implements Serializable {

    @Expose
    public String groupId;

    @Expose
    public String groupName;

    @Expose
    public String userId;

    @Expose
    public String userName;

    @Expose
    public String isManager;

    @Override
    public boolean equals(Object obj) {
        if(obj==null)
            return false;
        if(this == obj){
            return true;
        }
        if (obj instanceof GroupMemberBean) {
            GroupMemberBean other = (GroupMemberBean) obj;
            return  (other.userId).equals(this.userId);
        }
        return false;
    }
}
