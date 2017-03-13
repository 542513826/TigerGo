package com.smartdot.mobile.portal.abconstant;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局的选择记录
 */
public class GlobleAddressConfig {

    /** 取消全选的map 内含部门id */
    public static Map<String, String> cencelDeptIDs = new HashMap<>();

    /** 当前是否处于多选状态 */
    public static boolean isSelected = true;

    /** 选中的人员 id */
    public static Map<String, String> selectedPersonIDs = new HashMap<>();

    /** 全选的部门 id */
    public static Map<String, String> selectedDeptIDs = new HashMap<>();

    /** 群组中原有人员 id */
    public static Map<String, String> groupPersonIDs = new HashMap<>();


}
