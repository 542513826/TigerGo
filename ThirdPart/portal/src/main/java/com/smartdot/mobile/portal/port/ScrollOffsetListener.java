package com.smartdot.mobile.portal.port;
/***
 * 回调接口 - 用于在adapter中进入联系人详情时通知外层界面记录当前scrollView的偏移量，用来在关闭详情时进行修正
 */
public interface ScrollOffsetListener {
	/**用于进入联系人详情时通知外层界面记录当前scrollView的偏移量，用来在关闭时进行修正*/
	public void OnScrollOffset();
}
