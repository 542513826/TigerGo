package com.smartdot.mobile.portal.port;

import android.widget.ListView;
/***
 * 回调接口 - 用于刷新通讯录当前adapter,向Activity重新请求数据
 */
public interface OnRefreshListener {
	public void OnRefresh(String deptId, ListView lv);
}
