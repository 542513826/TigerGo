/*
 * Copyright (C) 2016 Bilibili
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smartdot.mobile.portal.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;

import com.smartdot.mobile.portal.R;

/**
 * 换肤工具类
 */
public class ThemeHelper {
    /**
     * 当前主题
     */
    private static final String CURRENT_THEME = "theme_current";

    /**
     * 当前主题色
     */
    private static final String CURRENT_COLOR = "color_current";

    /**
     * 各主题对应的int值
     */
    public static final int CARD_SAKURA = 0x1;// pink

    public static final int CARD_HOPE = 0x2;// purple

    public static final int CARD_STORM = 0x3;// blue

    public static final int CARD_WOOD = 0x4;// green

    public static final int CARD_LIGHT = 0x5;// green_light

    public static final int CARD_THUNDER = 0x6;// yellow

    public static final int CARD_SAND = 0x7;// orange

    public static final int CARD_FIREY = 0x8;// red

    public static int color = R.color.default_swipe_color;

    public static SharedPreferences getSharePreference(Context context) {
        return context.getSharedPreferences("multiple_theme", Context.MODE_PRIVATE);
    }

    // 设置主题
    public static void setTheme(Context context, int themeId) {
        // 换肤--设置有换肤属性的控件颜色
        getSharePreference(context).edit().putInt(CURRENT_THEME, themeId).apply();

        // 设置swipe刷新控件箭头的颜色
        switch (themeId) {
        case CARD_SAKURA:// pink
            color = R.color.pink;
            break;
        case CARD_HOPE:// purple
            color = R.color.purple;
            break;
        case CARD_STORM:// blue
            color = R.color.blue;
            break;
        case CARD_WOOD:// green
            color = R.color.green;
            break;
        case CARD_LIGHT:// green_light
            color = R.color.green_light;
            break;
        case CARD_THUNDER:// yellow
            color = R.color.yellow;
            break;
        case CARD_SAND:// orange
            color = R.color.orange;
            break;
        case CARD_FIREY:// red
            color = R.color.red;
            break;
        }

        setColor(context, color);
    }

    // 获取当前主题
    public static int getTheme(Context context) {
        return getSharePreference(context).getInt(CURRENT_THEME, CARD_STORM);
    }

    // 设置当前主题对应的主色
    public static void setColor(Context context, int color) {
        getSharePreference(context).edit().putInt(CURRENT_COLOR, color).commit();
    }

    // 获取当前主题的主色
    public static int getColor(Context context) {
        return getSharePreference(context).getInt(CURRENT_COLOR, color);
    }

    // 是否是默认主题
    public static boolean isDefaultTheme(Context context) {
        return getTheme(context) == CARD_STORM;
    }

    /**
     * 设置swipeRefreshLayout的箭头转动颜色
     * 
     * @param layout
     *            swipeRefreshLayout控件
     */
    public static void setSwipeRefreshLayoutColor(Context context, SwipeRefreshLayout layout) {
        if (context.getResources().getBoolean(R.bool.useChangeTheme)) {
            layout.setColorSchemeColors(ContextCompat.getColor(context, getColor(context)));
        } else {
            layout.setColorSchemeColors(ContextCompat.getColor(context, R.color.default_swipe_color));
        }
    }

    /**
     * 弹出换肤窗口 单选列表dialog
     */
    private static int mCurrentTheme;// 当前主题

    /**
     * 各主题
     */
    public static String[] theme = new String[] { "粉", "紫", "蓝", "绿", "浅绿", "黄", "橘", "红" };

    public static void showThemeDialog(Context context, final ClickListener listener) {
        mCurrentTheme = getTheme(context);
        int selectedItem = mCurrentTheme - 1;// 当前的主题
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("请选择主题颜色");
        builder.setSingleChoiceItems(theme, selectedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {// KLog.d("pink");
                    mCurrentTheme = CARD_SAKURA;
                } else if (which == 1) {// KLog.d("purple");
                    mCurrentTheme = CARD_HOPE;
                } else if (which == 2) {// KLog.d("blue");
                    mCurrentTheme = CARD_STORM;
                } else if (which == 3) {// KLog.d("green");
                    mCurrentTheme = CARD_WOOD;
                } else if (which == 4) {// KLog.d("green_light");
                    mCurrentTheme = CARD_LIGHT;
                } else if (which == 5) {// KLog.d("yellow");
                    mCurrentTheme = CARD_THUNDER;
                } else if (which == 6) {// KLog.d("orange");
                    mCurrentTheme = CARD_SAND;
                } else if (which == 7) {// KLog.d("red");
                    mCurrentTheme = CARD_FIREY;
                }
            }
        });
        builder.setPositiveButton(context.getResources().getString(R.string.confirm),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onConfirm(mCurrentTheme);
                    }
                });
        builder.setNegativeButton(context.getResources().getString(R.string.cancel), null);
        builder.create().show();
    }

    public interface ClickListener {
        void onConfirm(int currentTheme);
    }

    /**
     * 获取各主题代表的颜色
     */
    public static String getThemeColorInfo(Context context) {
        if (getTheme(context) == CARD_SAKURA) {
            return "pink";
        } else if (getTheme(context) == CARD_STORM) {
            return "blue";
        } else if (getTheme(context) == CARD_HOPE) {
            return "purple";
        } else if (getTheme(context) == CARD_WOOD) {
            return "green";
        } else if (getTheme(context) == CARD_LIGHT) {
            return "green_light";
        } else if (getTheme(context) == CARD_THUNDER) {
            return "yellow";
        } else if (getTheme(context) == CARD_SAND) {
            return "orange";
        } else if (getTheme(context) == CARD_FIREY) {
            return "red";
        }
        return null;
    }

    /**
     * 根据颜色id获取应用中各主题对应的颜色资源id
     */
    public static @ColorRes int getThemeColorId(Context context, int colorId, String theme) {
        if (colorId == R.color.theme_color_primary) {
            return context.getResources().getIdentifier(theme, "color", context.getPackageName());// 获取应用包下指定的id
        } else if (colorId == R.color.theme_color_primary_dark) {
            return context.getResources().getIdentifier(theme + "_dark", "color", context.getPackageName());
        } else if (colorId == R.color.theme_color_primary_trans) {
            return context.getResources().getIdentifier(theme + "_trans", "color", context.getPackageName());
        }

        return colorId;
    }

    /**
     * 根据颜色值获取应用中各主题对应的颜色资源id
     */
    public static @ColorRes int getThemeColor(Context context, int color, String theme) {
        switch (color) {
        case 0xfffb7299:
            return context.getResources().getIdentifier(theme, "color", context.getPackageName());
        case 0xffb85671:
            return context.getResources().getIdentifier(theme + "_dark", "color", context.getPackageName());
        case 0x99f0486c:
            return context.getResources().getIdentifier(theme + "_trans", "color", context.getPackageName());
        }
        return -1;
    }
}
