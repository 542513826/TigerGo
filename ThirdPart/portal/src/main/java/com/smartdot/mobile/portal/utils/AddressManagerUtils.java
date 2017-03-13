package com.smartdot.mobile.portal.utils;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.smartdot.mobile.portal.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/** 通讯录管理工具类 */
public class AddressManagerUtils {

	private Activity context;
	private HorizontalScrollView scrollView;
	private LinearLayout homeLl;// scrollView里默认的LinearLayout
	private List<ListView> listviews;// 所有listView的集合

	/** 设置左右ListView的显示比例 */
	private final int leftScale = 2;// 左边比例
	private final int rightScale = 3;// 右边比例

	private int width = 0;// 屏幕的宽度
	private int height = 0;// 屏幕的高度

	/** 由这两个因素决定滑动速度totaltime（滑动所需要的总时间） = time*num; */
	final long time = 30;// 滑动时间间隔
	final int num = 10;// 滑动次数

	/** 存放将要滑动到的绝对位置（以屏幕左边0为基准的相对距离） */
	private int scrollDistance;
	
	/**listView的数量*/
	private int listviewSize = 0;

	/**两列中间的阴影分割线*/
	private View v;

	/** 左滑时，最后一个ListView被绘制完成的监听 */
	OnGlobalLayoutListener leftListener = new OnGlobalLayoutListener() {
		@Override
		public void onGlobalLayout() {
			// 移动(注意：必须在控件绘制完成以后，才可以移动，否则控件正在绘制的过程中，执行移动无效。)
			scrollLeftWithSpeed(time, num, scrollDistance);
		}
	};
	/** 右滑时，最后一个ListView被绘制完成的监听 */
	OnGlobalLayoutListener rightListener = new OnGlobalLayoutListener() {
		@Override
		public void onGlobalLayout() {
			// 移动(注意：必须在控件绘制完成以后，才可以移动，否则控件正在绘制的过程中，执行移动无效。)
			scrollRightWithSpeed(time, num, scrollDistance);
		}
	};

	/**
	 * @param act
	 *            当前所在的activity
	 * @param scrollView
	 *            滑动控件
	 * @param homeLl
	 * @param v
	 */
	public AddressManagerUtils(Activity act, HorizontalScrollView scrollView, LinearLayout homeLl, View v) {

		this.context = act;
		this.scrollView = scrollView;
		this.homeLl = homeLl;
		this.v = v;
		initWidgets();
	}

	/*************************************** 用户调用的方法start *******************************************/
	/** 获取ListView控件的数量 */
	public int getViewCount() {
		return listviewSize;
	}
	/**获取所有listview的集合*/
    public List<ListView> getListViews(){
    	return listviews;
    }
	/**
	 * 向左滑动一个 (即添加操作)
	 * 
	 * @param adapter
	 *            是新添加的listview对应的adapter
	 * @return 返回新添加的listview对象
	 */
	public ListView scrollLeft(BaseAdapter adapter) {
		listviewSize++;
		// 添加
		addOneListView(adapter);
		int listviewSize = listviews.size();
		if (listviewSize <= 2) {
			ListView lv=listviews.get(listviews.size() - 1);
			if(listviewSize==1)
				lv.setBackgroundColor(context.getResources().getColor(R.color.white));
			return lv;
		}
		// 变形
		int scale = leftScale + rightScale;
		ListView leftView = listviews.get(listviewSize - 1 - 1);
		LinearLayout.LayoutParams leftParams = (LinearLayout.LayoutParams) leftView.getLayoutParams();
		leftParams.width = width / scale * leftScale;
		leftView.setLayoutParams(leftParams);
		scrollDistance = (listviewSize - 2) * (width / scale * leftScale);
		leftView.setBackgroundColor(context.getResources().getColor(R.color.white));
		// view加载完成时回调
		listviews.get(listviews.size() - 1).getViewTreeObserver().addOnGlobalLayoutListener(leftListener);
		return listviews.get(listviews.size() - 1);
	}

	/** 向右滑动一个(即删除操作) */
	public void scrollRight() {
		listviewSize--;
		final int listviewSize = listviews.size();
		if (listviewSize <= 2)
			return;
		// 变形
		int scale = leftScale + rightScale;
		ListView rightView = listviews.get(listviewSize - 1 - 1);
		LinearLayout.LayoutParams rightParams = (LinearLayout.LayoutParams) rightView.getLayoutParams();
		rightParams.width = width / scale * rightScale;
		rightView.setLayoutParams(rightParams);
		rightView.setBackgroundColor(context.getResources().getColor(R.color.white));
		rightView.setDivider(null);
		// 滑动
		scrollDistance = (listviewSize - 3) * width / scale * leftScale;
		listviews.get(listviewSize - 1).getViewTreeObserver().addOnGlobalLayoutListener(rightListener);
	}

	/*************************************** 用户调用的方法end *******************************************/

	/** 初始化控件 */
	private void initWidgets() {
		listviews = new ArrayList<ListView>();
		// 禁止手滑
		scrollView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				return true;
			}
		});
		getScreenWH();
	}

	/** 添加一个ListView (添加一个左ListView或右ListView,自动判断是左还是右，用户无需判断) */
	private void addOneListView(BaseAdapter adapter) {
		ListView lv = new ListView(context);
		lv.setBackgroundColor(context.getResources().getColor(R.color.white));
		lv.setDivider(null);
		lv.setAdapter(adapter);
		LinearLayout.LayoutParams lp;
		int scale = leftScale + rightScale;
		int listviewSize = listviews.size();
		if (listviewSize == 0) {// 添加左边布局:添加第一个ListView时，为添加左ListView
			lp = new LinearLayout.LayoutParams(width / scale * leftScale, LinearLayout.LayoutParams.FILL_PARENT);
			// 根据不同的屏幕宽度对分割阴影条做不同的Margin
			(((RelativeLayout.LayoutParams)v.getLayoutParams())).setMargins((width / scale * leftScale)-25, 0, 0, 0);
		} else {// 添加右边布局：其余均为添加右ListView
			lp = new LinearLayout.LayoutParams(width / scale * rightScale, LinearLayout.LayoutParams.FILL_PARENT);
		}
		lv.setLayoutParams(lp);
		homeLl.addView(lv);
		listviews.add(lv);
	}

	/** 获取屏幕的宽高 */
	private void getScreenWH() {
		WindowManager wm = context.getWindowManager();
		width = wm.getDefaultDisplay().getWidth();
		height = wm.getDefaultDisplay().getHeight();
	}

	/** 绘制速度 */
	private void scrollLeftWithSpeed(long totalTime, final int num, final int distance) {
		listviews.get(listviews.size() - 1).getViewTreeObserver().removeGlobalOnLayoutListener(leftListener);
		final int oneLeftViewLenth = width / (leftScale + rightScale) * leftScale;
		final Timer timer = new Timer();
		// 中间的阴影分割线 隐藏
		Message msg = lineHandler.obtainMessage(1);
		msg.sendToTarget();
		timer.schedule(new TimerTask() {
			int timeNum = 0;

			@Override
			public void run() {
				if (timeNum >= num) {
					scrollView.scrollTo(distance, 0); // 可能会存在不精确的情况 校准一下
					timer.cancel();
					// 中间的阴影分割线 显示
					Message msg = lineHandler.obtainMessage(2);
					msg.sendToTarget();
				} else {
					timeNum++;
					scrollView.scrollBy(oneLeftViewLenth / num, 0); // 更新位置
				}
			}
		}, 0, time);
	}

	/** 绘制速度 */
	private void scrollRightWithSpeed(final long totalTime, final int num, final int distance) {
		listviews.get(listviews.size() - 1).getViewTreeObserver().removeGlobalOnLayoutListener(rightListener);
		final int oneLeftViewLenth = width / (leftScale + rightScale) * leftScale;
		final Timer timer = new Timer();
		// 中间的阴影分割线 隐藏
		Message msg = lineHandler.obtainMessage(1);
		msg.sendToTarget();
		timer.schedule(new TimerTask() {
			int timeNum = 0;

			@Override
			public void run() {
				if (timeNum >= num) {
					scrollView.scrollTo(distance, 0); // 可能会存在不精确的情况 校准一下
					listviews.get(listviews.size() - 1).getViewTreeObserver().removeGlobalOnLayoutListener(rightListener);
					rightHandler.sendEmptyMessage(0);
					timer.cancel();
					// 中间的阴影分割线 显示
					Message msg = lineHandler.obtainMessage(2);
					msg.sendToTarget();
				} else {
					timeNum++;
					scrollView.scrollBy(-oneLeftViewLenth / num, 0); // 更新位置
				}
			}
		}, 0, time);
	}

	/** 当右滑完成时，发送信号到handler去移除最后一个ListView视图。 */
	Handler rightHandler = new Handler() {
		public void handleMessage(Message msg) {
			// 移除
			int listviewsSize = listviews.size();
			homeLl.removeView(listviews.get(listviewsSize - 1));
			listviews.remove(listviewsSize - 1);
		};
	};

	/** 当滑动开始或者结束时 发送信号隐藏中间的阴影竖线 */
	Handler lineHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what){
				case 1:
					// 中间的阴影分割线 隐藏
					v.setVisibility(View.GONE);
					break;
				case 2:
					// 中间的阴影分割线 显示
					v.setVisibility(View.VISIBLE);
					break;
			}
		};
	};

}
