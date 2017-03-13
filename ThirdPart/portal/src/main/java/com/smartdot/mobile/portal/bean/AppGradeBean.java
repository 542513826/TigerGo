package com.smartdot.mobile.portal.bean;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

/**
 * 应用评分的bean Created by zhangt on 2016/7/26.
 */
public class AppGradeBean implements Serializable {

    /** 有多少份评分 */
    @Expose
    public double grades;

    /** 总评分 满星5分 格式只有[1~5]和[x.5] 2种 */
    @Expose
    public float overallRating;

    /** 5星 */
    @Expose
    public int fiveStarPercentage;

    /** 4星 */
    @Expose
    public int fourStarPercentage;

    /** 3星 */
    @Expose
    public int threeStarPercentage;

    /** 2星 */
    @Expose
    public int twoStarPercentage;

    /** 1星 */
    @Expose
    public int oneStarPercentage;

}
