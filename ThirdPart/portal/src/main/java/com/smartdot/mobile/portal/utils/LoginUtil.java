package com.smartdot.mobile.portal.utils;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.reflect.TypeToken;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.bean.GroupInfoBean;
import com.smartdot.mobile.portal.bean.UserInfoBean;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imlib.model.Group;


/**
 * Created by Administrator on 2016/9/19.
 */
public class LoginUtil {

    /**
     * 处理登录返回数据
     * @param mContext
     * @param resultString
     * @return
     */
    public static boolean dealWithLogin(final Context mContext, String resultString) {
        try {
            JSONObject result = new JSONObject(resultString);
            if (!StringUtils.isAsNull(result.getString("userInfo"))) {

                JSONObject jsonObject = result.getJSONObject("userInfo");
                if (jsonObject.getInt("resultCode") == 200) {
                    doAsyncTaskForUserlist(jsonObject);
                    return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * 处理登录中返回的用户列表
     * @param jsonObject
     */
    private static void doAsyncTaskForUserlist(final JSONObject jsonObject) {
        AsyncTask asyncTask = new AsyncTask() {

            @Override
            protected void onPostExecute(Object o) {
            }

            @Override
            protected Object doInBackground(Object[] params) {
                List<UserInfoBean> userList = new ArrayList<>();
                try {
                    GloableConfig.myUserInfo.userId = jsonObject.getString("user_id");
                    GloableConfig.myUserInfo.userName = jsonObject.getString("user_name");
                    GloableConfig.myUserInfo.obey_dept_id = jsonObject.getString("obey_dept_id");
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
                return 1;// 这个值没有特殊含义
            }
        };
        asyncTask.execute();
    }


    /**
     * 处理获取token的数据
     * @param mContext
     * @param resultString
     * @return
     */
    public static String dealWithToken(Context mContext,String resultString) {
        String TOKEN = null;
        try {
            JSONObject jsonObject = new JSONObject(resultString);
            if (jsonObject.getString("errorCode").equals("0")) {
                JSONObject infoObject = jsonObject.getJSONObject("info");
                TOKEN = infoObject.getString("token");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TOKEN;
    }

    /**
     * 获取群组列表后的操作
     * @param mContext
     * @param resultString
     * @return
     */
    public static boolean dealWithUserList(Context mContext,String resultString) {
        try {
            final JSONObject jsonObject = new JSONObject(resultString);

            if (jsonObject.getString("code").equals("200")) {
                doAsyncForGrouplist(jsonObject);
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
           return false;
        }
        return false;
    }

    /**处理群组列表的异步操作
     * @param jsonObject
     */
    private static void doAsyncForGrouplist(final JSONObject jsonObject) {
        AsyncTask asyncTask = new AsyncTask() {

            @Override
            protected void onPostExecute(Object o) {

            }

            @Override
            protected Object doInBackground(Object[] params) {
                List<GroupInfoBean> groupList = new ArrayList<>();
                try {
                    groupList = CommonUtil.gson.fromJson(jsonObject.getString("result"),
                            new TypeToken<List<GroupInfoBean>>() {
                            }.getType());
                    for (int i = 0; i < groupList.size(); i++) {
                        GloableConfig.allGroupMap.put(groupList.get(i).id, groupList.get(i));

                        Group group;
                        GroupInfoBean groupInfoBean = groupList.get(i);
                        try {
                            group = new Group(groupInfoBean.id, groupInfoBean.name, null);
                        } catch (RuntimeException e) {
                            group = new Group(groupInfoBean.id, "默认群组名", null);
                        }
                        RongIM.getInstance().refreshGroupInfoCache(group);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return 1;
            }
        };
        asyncTask.execute();
    }


}
