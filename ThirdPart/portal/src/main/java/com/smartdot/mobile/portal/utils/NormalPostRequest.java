package com.smartdot.mobile.portal.utils;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import java.io.UnsupportedEncodingException;

/**
 * 解决volley中文乱码问题
 */
public class NormalPostRequest extends StringRequest {

    public NormalPostRequest(int method,String url, Response.Listener<String> listener,
                           Response.ErrorListener errorListener) {
        super(method,url, listener, errorListener);
    }

    /**
     * 重写以解决乱码问题
     */
    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String str = null;
        try {
            str = new String(response.data, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Response.success(str,
                HttpHeaderParser.parseCacheHeaders(response));
    }

}