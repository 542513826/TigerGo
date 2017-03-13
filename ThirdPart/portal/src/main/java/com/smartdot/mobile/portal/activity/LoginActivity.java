package com.smartdot.mobile.portal.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.google.gson.reflect.TypeToken;
import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.bean.GroupInfoBean;
import com.smartdot.mobile.portal.bean.UserInfoBean;
import com.smartdot.mobile.portal.utils.CommonUtil;
import com.smartdot.mobile.portal.utils.CommonUtils;
import com.smartdot.mobile.portal.utils.CustomToast;
import com.smartdot.mobile.portal.utils.JpushUtil;
import com.smartdot.mobile.portal.utils.NetUtils;
import com.smartdot.mobile.portal.utils.ProgressUtil;
import com.smartdot.mobile.portal.utils.SharePreferenceUtils;
import com.smartdot.mobile.portal.utils.StringUtils;
import com.smartdot.mobile.portal.utils.VolleyUtil;
import com.smartdot.mobile.portal.widget.ClearWriteEditText;
import com.socks.library.KLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.UserInfo;

/**
 * 登录界面
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;

    private LinearLayout login_container;

    private Button login_button;

    private Button english_btn;

    private Button chinese_btn;

    private ClearWriteEditText username_et;

    private ClearWriteEditText password_et;

    private String username;

    private String password;

    List<UserInfoBean> userList = new ArrayList<>();

    List<GroupInfoBean> groupList = new ArrayList<>();

    /**
     * 按键最小时间间隔
     */
    public static final int MIN_CLICK_DELAY_TIME = 1000;

    private long lastClickTime = 0;

    private String TOKEN;

    private String beforeUserId;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case 1001: // 登录
                dealWithLogin(msg.obj.toString());
                break;
            case 1002: // 获取token返回后的操作
                dealWithToken(msg.obj.toString());
                break;
            case 1003: // 获取群组列表后的操作
                dealWithUserList(msg.obj.toString());
                break;
            case 1004: // 注销前一个账号在服务器注册的极光信息，然后进入首页
                dealWithLogcat(msg);
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mContext = this;
        SharePreferenceUtils.getAppConfig(mContext);
        initView();
    }

    private void initView() {
        login_container = (LinearLayout) findViewById(R.id.login_container);
        login_button = (Button) findViewById(R.id.login_button);
        english_btn = (Button) findViewById(R.id.english_btn);
        chinese_btn = (Button) findViewById(R.id.chinese_btn);
        username_et = (ClearWriteEditText) findViewById(R.id.username_et);
        password_et = (ClearWriteEditText) findViewById(R.id.password_et);

        String spUsername = (String) SharePreferenceUtils.getParam("username", "");
        String spPassword = (String) SharePreferenceUtils.getParam("password", "");

        if (!StringUtils.isNull(spUsername) && !StringUtils.isNull(spPassword)) {
            username_et.setText(spUsername);
            password_et.setText(spPassword);
        }

        login_button.setOnClickListener(this);
        english_btn.setOnClickListener(this);
        chinese_btn.setOnClickListener(this);

        beforeUserId = (String) SharePreferenceUtils.getParam("userid", "");
    }

    /**
     * 获取token
     */
    private void getToken() {
        // 获取token
        VolleyUtil volleyUtil = new VolleyUtil(mContext);
        String url = String.format(GloableConfig.RongCloud.getTokenUrl, GloableConfig.myUserInfo.userId,
                GloableConfig.myUserInfo.userName, GloableConfig.myUserInfo.portraitUri);
        volleyUtil.stringRequest(handler, Request.Method.POST, url, 1002);
    }

    /**
     * 连接融云服务器
     */
    private void RongConnect(String token) {
        RongIM.connect(token, new RongIMClient.ConnectCallback() {
            @Override
            public void onTokenIncorrect() {
                KLog.v("连接融云服务器失败，token失效");
                ProgressUtil.dismissProgressDialog();
            }

            @Override
            public void onSuccess(String s) {
                KLog.v("连接融云服务器成功");
                KLog.v("融云返回的id--------" + s);
                // 设置当前用户信息
                if (RongIM.getInstance() != null) {
                    RongIM.getInstance().setCurrentUserInfo(new UserInfo(s, GloableConfig.myUserInfo.userName, null));
                    // 设置消息体内是否携带用户信息
                    RongIM.getInstance().setMessageAttachedUserInfo(true);

                    for (int i = 0; i < groupList.size(); i++) {
                        Group group;
                        GroupInfoBean groupInfoBean = groupList.get(i);
                        try {
                            group = new Group(groupInfoBean.id, groupInfoBean.name, null);
                        } catch (RuntimeException e) {
                            group = new Group(groupInfoBean.id, "默认群组名", null);
                        }
                        RongIM.getInstance().refreshGroupInfoCache(group);
                    }

                }
                /** 登录成功 进入应用首页 **/
                ProgressUtil.dismissProgressDialog();
                Intent intent = new Intent(mContext, PortalMainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.base_slide_right_in, R.anim.base_slide_right_out);// 进场动画
                finish();
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                KLog.v("onError errorcode:" + errorCode.getValue());
            }
        });
    }

    /**
     * 登录
     */
    public void login() {
        if (!NetUtils.isConnected(mContext)) {
            CustomToast.showToast(mContext, getString(R.string.net_error), 400);
            return;
        }
        ProgressUtil.showPregressDialog(this, R.layout.custom_progress);
        VolleyUtil volleyUtil = new VolleyUtil(mContext);
        Map<String, String> map = new HashMap<>();
        map.put("userId", username);
        map.put("password", password);
        map.put("osType", GloableConfig.OS_TYPE);
        map.put("deviceType", GloableConfig.DEVICE_TYPE);
        volleyUtil.stringRequest(handler, GloableConfig.LoginUrl, map, 1001);
    }

    /**
     * 获取群组列表
     */
    private void getGroupList() {
        VolleyUtil volleyUtil = new VolleyUtil(mContext);
        String url = String.format(GloableConfig.RongCloud.getGroupListUrl, GloableConfig.myUserInfo.userId);
        volleyUtil.stringRequest(handler, Request.Method.POST, url, 1003);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.login_button) {
            // 登录
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
                lastClickTime = currentTime;
                username = username_et.getText().toString().trim();
                password = password_et.getText().toString().trim();
                if (StringUtils.isNull(username) || StringUtils.isNull(password)) {
                    CustomToast.showToast(mContext, getString(R.string.cannotEmpty), 400);
                    return;
                } else if (!StringUtils.isUserName(username) || !StringUtils.isUserName(password)) {
                    CustomToast.showToast(mContext, getString(R.string.cannotSpecialCharacters), 400);
                    return;
                }
                login();
            }
        } else if (v.getId() == R.id.english_btn) {
            // 英语
            english_btn.setVisibility(View.GONE);
            chinese_btn.setVisibility(View.VISIBLE);
        } else if (v.getId() == R.id.chinese_btn) {
            // 中文
            english_btn.setVisibility(View.VISIBLE);
            chinese_btn.setVisibility(View.GONE);
        }
    }

    /**
     * 处理登录返回数据
     */
    private void dealWithLogin(String resultString) {
        try {
            JSONObject result = new JSONObject(resultString);
            if (StringUtils.isAsNull(result.getString("userInfo"))) {
                CustomToast.showToast(mContext, getString(R.string.login_error), 400);
                ProgressUtil.dismissProgressDialog();
                return;
            }

            final JSONObject jsonObject = result.getJSONObject("userInfo");
            if (jsonObject.getInt("resultCode") == 200) {
                SharePreferenceUtils.setParam("username", username);
                SharePreferenceUtils.setParam("password", password);

                doAsyncTask(jsonObject);
            } else {
                ProgressUtil.dismissProgressDialog();
                CustomToast.showToast(mContext, getString(R.string.login_error), 400);
            }
        } catch (JSONException e) {
            CustomToast.showToast(mContext, getString(R.string.login_failure), 400);
            ProgressUtil.dismissProgressDialog();
            e.printStackTrace();
        }
    }

    /**
     * 新启线程处理登录过程中返回的用户数据
     *
     * @param jsonObject
     */
    private void doAsyncTask(final JSONObject jsonObject) {
        AsyncTask asyncTask = new AsyncTask() {

            @Override
            protected void onPostExecute(Object o) {
                if (SharePreferenceUtils.contains("userid") && !beforeUserId.equals(GloableConfig.myUserInfo.userId)) {
                    JpushUtil.logoutJpush(mContext, handler, 1004);
                } else {
                    if (GloableConfig.RongCloud.useRong) {
                        getToken();
                    } else {
                        ProgressUtil.dismissProgressDialog();
                        Intent intent = new Intent(mContext, PortalMainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            protected Object doInBackground(Object[] params) {
                try {
                    GloableConfig.myUserInfo.userId = jsonObject.getString("user_id");
                    GloableConfig.myUserInfo.userName = jsonObject.getString("user_name");
                    GloableConfig.myUserInfo.obey_dept_id = jsonObject.getString("obey_dept_id");

                    SharePreferenceUtils.setParam("userid", GloableConfig.myUserInfo.userId);

                    userList = CommonUtils.gson.fromJson(jsonObject.getString("userList"),
                            new TypeToken<List<UserInfoBean>>() {
                            }.getType());
                    GloableConfig.allUser = userList;
                    for (int i = 0; i < userList.size(); i++) {
                        GloableConfig.allUserMap.put(userList.get(i).userId, userList.get(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return 1;
            }
        };
        asyncTask.execute();
    }

    /**
     * 处理获取token的数据
     */
    private void dealWithToken(String resultString) {
        try {
            JSONObject jsonObject = new JSONObject(resultString);
            if (jsonObject.getString("errorCode").equals("0")) {
                JSONObject infoObject = jsonObject.getJSONObject("info");
                TOKEN = infoObject.getString("token");
                getGroupList();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            CustomToast.showToast(mContext, getString(R.string.login_failure), 400);
            ProgressUtil.dismissProgressDialog();
        }
    }

    /**
     * 获取群组列表后的操作
     */
    private void dealWithUserList(String resultString) {
        try {
            final JSONObject jsonObject = new JSONObject(resultString);

            if (jsonObject.getString("code").equals("200")) {
                AsyncTask asyncTask = new AsyncTask() {

                    @Override
                    protected void onPostExecute(Object o) {
                        RongConnect(TOKEN);
                    }

                    @Override
                    protected Object doInBackground(Object[] params) {
                        try {
                            groupList = CommonUtil.gson.fromJson(jsonObject.getString("result"),
                                    new TypeToken<List<GroupInfoBean>>() {
                                    }.getType());
                            for (int i = 0; i < groupList.size(); i++) {
                                GloableConfig.allGroupMap.put(groupList.get(i).id, groupList.get(i));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return 1;
                    }
                };
                asyncTask.execute();
            } else {
                CustomToast.showToast(mContext, getString(R.string.login_failure), 400);
                ProgressUtil.dismissProgressDialog();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            CustomToast.showToast(mContext, getString(R.string.login_failure), 400);
            ProgressUtil.dismissProgressDialog();
        }

    }

    /**
     * 处理极光推送的注销后返回的数据，然后进入主界面
     *
     * @param msg
     */
    private void dealWithLogcat(Message msg) {
        try {
            JSONObject jsonObject = new JSONObject(msg.obj.toString());
            if (msg.obj.toString().contains("success")) {
                if (GloableConfig.RongCloud.useRong) {
                    getToken();
                } else {
                    ProgressUtil.dismissProgressDialog();
                    Intent intent = new Intent(mContext, PortalMainActivity.class);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.base_slide_right_in, R.anim.base_slide_right_out);// 进场动画
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 触摸空白区域关闭软键盘
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                return imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        }
        return false;
    }

}
