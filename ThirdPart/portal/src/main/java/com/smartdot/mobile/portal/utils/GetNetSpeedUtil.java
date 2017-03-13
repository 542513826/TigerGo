package com.smartdot.mobile.portal.utils;

import android.content.Context;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zhang on 2016/12/9.
 * 作用：获取应用实时网速
 */

public class GetNetSpeedUtil {

    private Context mContext;
    /**
     * 上一个时间点的时间
     */
    private long lastTimeStamp = 0;
    /**
     * 上一个时间点的总数据流量
     */
    private long lastTotalRxBytes = 0;
    /**
     * 监听间隔时间，单位毫秒
     */
    private int Gap = 1000;
    /**
     * msg.what
     */
    private int what = 1001;

    private Handler mHandler;

    public TimerTask task = new TimerTask() {
        @Override
        public void run() {
            showNetSpeed();
        }
    };

    public GetNetSpeedUtil(Context mContext, Handler mHandler, int what) {
        this.mContext = mContext;
        this.mHandler = mHandler;
        this.what = what;
        lastTotalRxBytes = getTotalRxBytes();
        lastTimeStamp = System.currentTimeMillis();
    }

    /**
     * 开启网速监听
     */
    public void startMonitorNet() {
        new Timer().schedule(task, 1000, Gap);
    }

    public void stopMonitorNet(){
        task.cancel();
    }

    /**
     * 获取当前总数据量
     *
     * @return
     */
    private long getTotalRxBytes() {
        long TotalRxBytes = TrafficStats.getUidRxBytes(mContext.getApplicationInfo().uid) == TrafficStats.UNSUPPORTED
                ? 0 : (TrafficStats.getTotalRxBytes() / 1024); // 转化为kb
        return TotalRxBytes;
    }

    /**
     * 计算实时网速
     */
    private void showNetSpeed() {
        long nowTimeStamp = System.currentTimeMillis();
        long nowTotalRxBytes = getTotalRxBytes();

        float speed = ((float) (nowTotalRxBytes - lastTotalRxBytes)) / (nowTimeStamp - lastTimeStamp) * 1000; //毫秒转为秒

        speed = (float) Math.round(speed * 100) / 100;

        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;

        Message msg = mHandler.obtainMessage();
        msg.what = what;
        msg.obj = String.valueOf(speed) + "kb/s";

        mHandler.sendMessage(msg);
    }


    /**
     * 设置监听时间间隔
     * @param gap
     */
    public void setGap(int gap) {
        Gap = gap;
    }
}
