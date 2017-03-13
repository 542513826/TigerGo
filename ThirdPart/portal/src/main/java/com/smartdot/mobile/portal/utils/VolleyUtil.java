package com.smartdot.mobile.portal.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.smartdot.mobile.portal.R;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * volley 请求工具类
 */
public class VolleyUtil {

    private String TAG = "fate";

    private Context mContext;

    /**
     * 请求队列
     */
    RequestQueue mQueue;

    /**
     * 超时时间
     */
    int OVERTIME_TIME = 10 * 1000;

    public VolleyUtil(Context mContext) {
        this.mContext = mContext;
        mQueue = Volley.newRequestQueue(mContext);
    }

    /**
     * post 请求
     *
     * @param handler
     * @param url
     *            请求地址
     * @param paramMap
     *            请求param
     * @param what
     *            msg.what值
     */
    public void stringRequest(final Handler handler, String url, final Map<String, String> paramMap, final int what) {
        if (!NetUtils.isConnected(mContext)) {
            CustomToast.showToast(mContext, mContext.getString(R.string.net_error), 400);
            ProgressUtil.dismissProgressDialog();
            return;
        }
        NormalPostRequest normalPostRequest = new NormalPostRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Message message = new Message();
                        message.obj = response;
                        message.what = what;
                        handler.sendMessage(message);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.getMessage(), error);
                        VolleyErrorHelper.getMessage(error, mContext);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String value = Long.toString(System.currentTimeMillis());
                String time = null;
                try {
                    time = encryptHeader.getEncryptedText("servicePotralKey", value);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("serviceOapiKey", time);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return paramMap;
            }

        };
        // 设置超时时间 现在是10s
        normalPostRequest.setRetryPolicy(new DefaultRetryPolicy(OVERTIME_TIME, 1, 1.0f));
        mQueue.add(normalPostRequest);
    }

    /**
     * volley string 请求，自己填写请求方法 这个方法没有加header ，请求融云的数据时候用·
     *
     * @param handler
     * @param method
     * @param url
     * @param what
     */
    public void stringRequest(final Handler handler, int method, String url, final int what) {
        if (!NetUtils.isConnected(mContext)) {
            CustomToast.showToast(mContext, mContext.getString(R.string.net_error), 400);
            ProgressUtil.dismissProgressDialog();
            return;
        }
        NormalPostRequest normalPostRequest = new NormalPostRequest(method, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);
                Message message = new Message();
                message.obj = response;
                message.what = what;
                handler.sendMessage(message);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage(), error);
                VolleyErrorHelper.getMessage(error, mContext);
            }
        });
        // 设置超时时间 现在是10s
        normalPostRequest.setRetryPolicy(new DefaultRetryPolicy(OVERTIME_TIME, 1, 1.0f));
        mQueue.add(normalPostRequest);
    }

    /**
     * volley jsonObjectRequest 请求方法 自定义方式
     *
     * @param handler
     * @param url
     * @param what
     */
    public void jsonObjectRequest(final Handler handler, String url, final int what) {
        if (!NetUtils.isConnected(mContext)) {
            CustomToast.showToast(mContext, mContext.getString(R.string.net_error), 400);
            ProgressUtil.dismissProgressDialog();
            return;
        }
        NormalPostJsonRequest normalPostJsonRequest = new NormalPostJsonRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("TAG", response.toString());
                        Message message = new Message();
                        message.obj = response;
                        message.what = what;
                        handler.sendMessage(message);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("TAG", error.getMessage(), error);
                        VolleyErrorHelper.getMessage(error, mContext);
                    }
                });
        normalPostJsonRequest.setRetryPolicy(new DefaultRetryPolicy(OVERTIME_TIME, 1, 1.0f));
        mQueue.add(normalPostJsonRequest);
    }

    /**
     * volley jsonObjectRequest 请求方法 自定义方式
     *
     * @param handler
     * @param method
     * @param url
     * @param what
     */
    public void jsonObjectRequest(final Handler handler, int method, String url, final int what) {
        if (!NetUtils.isConnected(mContext)) {
            CustomToast.showToast(mContext, mContext.getString(R.string.net_error), 400);
            ProgressUtil.dismissProgressDialog();
            return;
        }
        NormalPostJsonRequest normalPostJsonRequest = new NormalPostJsonRequest(method, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("TAG", response.toString());
                        Message message = new Message();
                        message.obj = response;
                        message.what = what;
                        handler.sendMessage(message);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("TAG", error.getMessage(), error);
                        VolleyErrorHelper.getMessage(error, mContext);
                    }
                });
        normalPostJsonRequest.setRetryPolicy(new DefaultRetryPolicy(OVERTIME_TIME, 1, 1.0f));
        mQueue.add(normalPostJsonRequest);
    }

    /**
     * 设置超时时间，单位毫秒
     *
     * @param OVERTIME_TIME
     */
    public void setOverTime(int OVERTIME_TIME) {
        this.OVERTIME_TIME = OVERTIME_TIME;
    }
}
