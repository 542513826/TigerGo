package com.smartdot.mobile.portal;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.multidex.MultiDex;

import com.bilibili.magicasakura.utils.ThemeUtils;
import com.smartdot.mobile.portal.application.MyAppContext;
import com.smartdot.mobile.portal.bean.TabBean;
import com.smartdot.mobile.portal.utils.ImageLoaderUtils;
import com.smartdot.mobile.portal.utils.ResetUrlUtil;
import com.smartdot.mobile.portal.utils.ThemeHelper;
import com.smartdot.mobile.portal.utils.XmlUtil;
import com.socks.library.KLog;

import org.dom4j.DocumentException;

import java.util.List;

import cn.jpush.android.api.JPushInterface;
import io.rong.imkit.RongIM;
import io.rong.imlib.ipc.RongExceptionHandler;

/**
 * 全局唯一的Application 此类管理各项模块的功能初始化 解析工作
 */
public class PortalApplication extends Application implements ThemeUtils.switchColor {

    static List<TabBean> tabBeans = null;

    private static PortalApplication sInstance;

    public static PortalApplication getApplication() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        sInstance = this;
        // 融云在一些情况下会多启动1-2个进程,为了不重复执行application方法做主进程判断
        if (getPackageName().equals(getCurProcessName(this))) {
            // 根部URL动态配置,重新赋值
            ResetUrlUtil.resetUrl();
            // 对功能开关的重新赋值
            ResetUrlUtil.resetConfig();
            // 配置解析
            getTabFromXml();
            // ImageLoader初始化
            ImageLoaderUtils.initConfiguration(getApplicationContext());
            // Log日志初始化
            KLog.init(BuildConfig.LOG_DEBUG, "fate");
            // 换肤-实现switchColor接口
            ThemeUtils.setSwitchColor(this);
            // IMKit SDK调用第一步 初始化 context上下文 只有两个进程需要初始化，主进程和 push 进程
            RongIM.init(this);
            MyAppContext.init(this);
            Thread.setDefaultUncaughtExceptionHandler(new RongExceptionHandler(this));
            // 极光推送
            JPushInterface.setDebugMode(BuildConfig.JPUSH_DEBUG); // 正式发版时此处应设置为fale
            JPushInterface.init(this); // 初始化极光推送
        }

        super.onCreate();
    }

    /**
     * 返回tab导航bean信息
     */
    public static List<TabBean> getTabBeans() {
        return tabBeans;
    }

    /**
     * 底部tab导航xml解析
     */
    private void getTabFromXml() {
        XmlUtil xmlUtil = new XmlUtil(this, "bottom_config.xml");
        try {
            tabBeans = xmlUtil.getTabBeans(this);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获得当前进程的名字
     */
    public static String getCurProcessName(Context context) {

        int pid = android.os.Process.myPid();

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {

            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    /**
     * 重写该方法,执行MultiDex配置防止64K方法数超限
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    /** 多彩主题换肤 - @ColorRes表示使用的颜色必须是 R.color.xxxx */
    @Override
    public int replaceColorById(Context context, @ColorRes int colorId) {
        if (ThemeHelper.isDefaultTheme(context)) {
            return context.getResources().getColor(colorId);
        }
        String theme = ThemeHelper.getThemeColorInfo(context);
        if (theme != null) {
            colorId = ThemeHelper.getThemeColorId(context, colorId, theme);
        }
        return context.getResources().getColor(colorId);
    }

    /** 多彩主题换肤 - @ColorInt 表示使用的颜色必须是 AABBCC这样的RGB整型 */
    @Override
    public int replaceColor(Context context, @ColorInt int originColor) {
        if (ThemeHelper.isDefaultTheme(context)) {
            return originColor;
        }
        String theme = ThemeHelper.getThemeColorInfo(context);
        int colorId = -1;

        if (theme != null) {
            colorId = ThemeHelper.getThemeColor(context, originColor, theme);
        }
        return colorId != -1 ? getResources().getColor(colorId) : originColor;
    }
}
