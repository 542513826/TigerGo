package com.smartdot.mobile.portal.bean;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 群组信息
 * Created by zhangt on 2016/7/25.
 */
public class GroupInfoBean implements Serializable{

    @Expose
    public String id;

    @Expose
    public String name;

    @Expose
    public String portrait;

    @Expose
    public String host_user_id;

    @Expose
    public String number;

    @Expose
    public String create_datetime;

    @Expose
    public List<GroupMemberBean> memberList;

    @Expose
    public String depart_ids = "";

    @Expose
    public List<String> depart_ids_list = new ArrayList<>();

    public void setDepart_ids_list(String depart_ids) {
        depart_ids_list = new ArrayList<>();
        String[] depart = depart_ids.split(",");
        for (int i = 0; i < depart.length;i++){
            depart_ids_list.add(depart[i]);
        }
    }
}
