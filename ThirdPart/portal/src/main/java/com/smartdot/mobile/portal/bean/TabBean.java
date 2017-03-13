package com.smartdot.mobile.portal.bean;

import android.support.v4.app.Fragment;

/**
 * Created by Administrator on 2016/7/5.
 */
public class TabBean implements Comparable<TabBean> {
    private int index;

    private String label;

    private Fragment f;

    private String picName;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Fragment getF() {
        return f;
    }

    public void setF(Fragment f) {
        this.f = f;
    }

    public String getPicName() {
        return picName;
    }

    public void setPicName(String picName) {
        this.picName = picName;
    }

    public int compareTo(TabBean tabBean) {
        // 从大到小排序
        // return javaBean.getValue().compareTo(this.getValue());
        // 从小到大排序
        return (new Integer(this.getIndex())).compareTo(new Integer(tabBean.getIndex()));
    }
}