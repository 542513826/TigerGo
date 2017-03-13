package com.smartdot.mobile.portal.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.view.View;

import com.bilibili.magicasakura.utils.ThemeUtils;
import com.smartdot.mobile.portal.R;

/**
 * 更换主题 工具类
 */
public class ChangeThemeUtils {

    public static  void ChangeTheme(final Context mContext, Activity activity){
        ThemeHelper.setTheme(mContext,ThemeHelper.getTheme(mContext));
        ThemeUtils.refreshUI(mContext, new ThemeUtils.ExtraRefreshable() {
            @Override
            public void refreshGlobal(Activity activity) {
                if (Build.VERSION.SDK_INT >=21){
                    ActivityManager.TaskDescription description = new ActivityManager.TaskDescription(null, null, ThemeUtils.getThemeAttrColor(mContext, android.R.attr.colorPrimary));
                    activity.setTaskDescription(description);
                    activity.getWindow().setStatusBarColor(ThemeUtils.getColorById(mContext, R.color.theme_color_primary_dark));
                }

            }

            @Override
            public void refreshSpecificView(View view) {

            }
        });
    }
}
