package com.smartdot.mobile.portal.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;

import com.smartdot.mobile.portal.R;

/**
 * 加载进度条的管理类 Created by Administrator on 2015/12/8.
 */
public class ProgressUtil {

    private static ProgressDialog progressDialog;

    public ProgressUtil() {
        // TODO Auto-generated constructor stub
    }

    /**
     * 显示dialog
     * 
     * @param context
     */
    public static void showPregressDialog(Context context, int layout) {
        DialogInterface.OnKeyListener keyListener = new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_SEARCH) {
                    return true;
                }
                return false;
            }
        };

        progressDialog = new ProgressDialog(context, R.style.progressdialog);
        // progressDialog.setCancelable(false);//设置进度条是否可以按退回键取消
        progressDialog.setCanceledOnTouchOutside(false);// 设置点击不会消失
        progressDialog.show();
        // 注意此处要放在show之后 否则会报异常
        progressDialog.setContentView(layout);
    }

    /**
     * 取消进度条
     */
    public static void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
