package com.smartdot.mobile.portal.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;

import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.utils.ChangeThemeUtils;
import com.smartdot.mobile.portal.utils.CustomToast;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.RongIM;

public abstract class BaseActivity extends FragmentActivity {
    public static List<Activity> actList = null;

    private Context mContext;

    private long exitTime = 0;

    /** 生成自己的实例 可以让外部类进行调用 目前用于退出整个应用 */
    public static BaseActivity _instance = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        // setVolumeControlStream(AudioManager.STREAM_MUSIC);// 使得音量键控制媒体声音
        _instance = this;

        if (actList == null)
            actList = new ArrayList<Activity>();
        actList.add(this);
    }

    /** 退出所有的activity */
    public void exitAllAct() {
        for (Activity act : actList)
            act.finish();
    }

    @Override
    protected void onDestroy() {
        actList.remove(this);
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                /** 2秒内点击返回键流程，如果在首页将会进行误操作提示，否则走正常关闭界面流程 */
                String strClassName = this.getClass().getName();
                if (strClassName.equals("com.smartdot.mobile.portal.activity.PortalMainActivity")) {
                    CustomToast.showToast(this, getString(R.string.Again_according_to_exit_the_program));
                    exitTime = System.currentTimeMillis();
                } else {
                    this.finish();
                    overridePendingTransition(R.anim.base_back_in, R.anim.base_back_out);// 退场动画
                    return true;
                }

            } else {
                /** 应用正常退出流程 */
                RongIM.getInstance().disconnect();
                exitAllAct();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ChangeThemeUtils.ChangeTheme(mContext, _instance);
    }
}
