package com.smartdot.mobile.portal.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.bean.UpDateBean;
import com.smartdot.mobile.portal.utils.CheckVersionUpdateUtils;
import com.smartdot.mobile.portal.utils.CommonUtils;
import com.smartdot.mobile.portal.utils.JpushUtil;
import com.smartdot.mobile.portal.utils.SharePreferenceUtils;
import com.smartdot.mobile.portal.utils.VolleyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户设置界面
 */
public class UserSetActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1002) {
                try {

                    JSONObject result = new JSONObject(msg.obj.toString());
                    UpDateBean bean = CommonUtils.GsonFromJsonToBean(result.getString("result"), UpDateBean.class);
                    CheckVersionUpdateUtils utils = CheckVersionUpdateUtils.createCheckVersionUpdateUtils(mContext);
                    utils.getVersionInfoCompare(bean.version,

                            bean.downloadURL);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_set);
        mContext = this;
        SharePreferenceUtils.getAppConfig(mContext);
        initView();
    }

    private void initView() {
        ImageView title_left_img = (ImageView) findViewById(R.id.title_left_img);
        TextView title_center_text = (TextView) findViewById(R.id.title_center_text);
        TextView title_right_text = (TextView) findViewById(R.id.title_right_text);
        Button exit_btn = (Button) findViewById(R.id.exit_btn);
        RelativeLayout check_update_layout = (RelativeLayout) findViewById(R.id.check_update_layout);

        title_center_text.setText(R.string.setting);
        title_right_text.setVisibility(View.GONE);
        title_left_img.setOnClickListener(this);
        exit_btn.setOnClickListener(this);
        check_update_layout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.title_left_img) {
            finish();
        } else if (v.getId() == R.id.exit_btn) {
            /** 退出 */
            exit();
        } else if (v.getId() == R.id.check_update_layout) {
            /** 检查更新 */
            checkUpdate();
        }
    }

    /**
     * 退出
     */
    private void exit() {
        new AlertDialog.Builder(mContext).setTitle(getString(R.string.exit_ask))
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (GloableConfig.useJpush) {
                            JpushUtil.logoutJpush(mContext);
                        }
                        logout();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
    }

    /**
     * 注销
     */
    private void logout() {
        Intent it = mContext.getPackageManager().getLaunchIntentForPackage(mContext.getPackageName());
        SharePreferenceUtils.remove(mContext, "username");
        SharePreferenceUtils.remove(mContext, "password");
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(it);
        BaseActivity._instance.exitAllAct();
    }

    /**
     * 检查更新
     */
    public void checkUpdate() {
        VolleyUtil volleyUtil = new VolleyUtil(mContext);
        Map<String, String> map = new HashMap<>();
        map.put("osType", GloableConfig.OS_TYPE);
        volleyUtil.stringRequest(handler, GloableConfig.CheckUpdateUrl, map, 1002);
    }

}
