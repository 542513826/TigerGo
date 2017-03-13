package com.smartdot.mobile.portal.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.adapter.ShopClassifyAdapter;
import com.smartdot.mobile.portal.bean.AppDetailBean;
import com.smartdot.mobile.portal.port.OnShopAppRefreshListener;
import com.smartdot.mobile.portal.utils.CommonUtil;
import com.smartdot.mobile.portal.utils.CustomToast;
import com.smartdot.mobile.portal.utils.NetUtils;
import com.smartdot.mobile.portal.utils.ProgressUtil;
import com.smartdot.mobile.portal.utils.VolleyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 应用分类界面,根据点击的不同类别,加载不同的列表界面 对应已装/未装/升级/移除应用
 */
public class ShopClassifyActivity extends BaseActivity implements View.OnClickListener, OnShopAppRefreshListener {

    private Context mContext;

    private TextView cancel_tv;

    private ImageView title_left_img;

    private TextView title_center_text;

    private TextView title_right_text;

    private ListView shop_lv;

    private List<AppDetailBean> mList = new ArrayList<>();

    private ShopClassifyAdapter adapter;

    private String appType;

    private RelativeLayout no_app_rl;

    /** 防止同时进行多次请求 是否允许发起请求 */
    private boolean isCanRequest = true;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case 1001:
                try {
                    ProgressUtil.dismissProgressDialog();
                    isCanRequest = true;
                    /** 接收到返回的全部应用列表进行刷新 */
                    JSONObject result = new JSONObject(msg.obj.toString());
                    List<AppDetailBean> list = new ArrayList<>();
                    list = CommonUtil.gson.fromJson(result.getString("result"), new TypeToken<List<AppDetailBean>>() {
                    }.getType());
                    mList.clear();
                    mList.addAll(classifyList(list));
                    adapter.notifyDataSetChanged();
                    if (mList.size() == 0) {
                        no_app_rl.setVisibility(View.VISIBLE);// 显示无应用界面
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classify);
        mContext = this;
        getData();
        initView();
    }

    private void initView() {
        Intent intent = getIntent();
        appType = intent.getStringExtra("appType");

        cancel_tv = (TextView) findViewById(R.id.cancel_tv);
        title_left_img = (ImageView) findViewById(R.id.title_left_img);
        title_right_text = (TextView) findViewById(R.id.title_right_text);
        title_center_text = (TextView) findViewById(R.id.title_center_text);
        no_app_rl = (RelativeLayout) findViewById(R.id.no_app_rl);
        shop_lv = (ListView) findViewById(R.id.shop_lv);
        adapter = new ShopClassifyAdapter(ShopClassifyActivity.this, mList,appType, R.layout.item_shop_updata);
        adapter.setOnShopAppRefreshListener(this);
        shop_lv.setAdapter(adapter);
        title_right_text.setVisibility(View.GONE);
        title_center_text.setText(appType);
        title_left_img.setOnClickListener(this);
    }

    /**
     * 获取数据
     */
    private void getData() {
        if (!NetUtils.isConnected(mContext)) {
            CustomToast.showToast(mContext, getString(R.string.net_error));
            return;
        }
        ProgressUtil.showPregressDialog(this, R.layout.custom_progress);
        isCanRequest = false;

        VolleyUtil volleyUtil = new VolleyUtil(mContext);
        Map<String, String> map = new HashMap<>();
        map.put("userId", GloableConfig.myUserInfo.userId);
        map.put("deptId", GloableConfig.myUserInfo.obey_dept_id);
        map.put("osType", GloableConfig.OS_TYPE);
        map.put("deviceType", GloableConfig.DEVICE_TYPE);
        volleyUtil.stringRequest(handler, GloableConfig.AppListUrl, map, 1001);
    }

    private List<AppDetailBean> classifyList(List<AppDetailBean> list) {
        List<AppDetailBean> dataList = new ArrayList<>();
        switch (appType) {
        case "已装应用":
            for (AppDetailBean bean : list) {
                if (bean.app_Setup.equals("0")) {
                    dataList.add(bean);
                }
            }
            break;
        case "未装应用":
            for (AppDetailBean bean : list) {
                if (bean.app_Setup.equals("2")) {
                    dataList.add(bean);
                }
            }
            break;
        case "升级应用":
            for (AppDetailBean bean : list) {
                if (bean.app_Setup.equals("1")) {
                    dataList.add(bean);
                }
            }
            break;
        case "移除应用":
            for (AppDetailBean bean : list) {
                if (bean.app_Setup.equals("0")) {
                    dataList.add(bean);
                }
            }
            break;
        }
        return dataList;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.title_left_img) {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isCanRequest) {
            getData();
        }
    }

    /**
     * 接收在adapter卸载轻应用后的回调,重新请求数据刷新界面
     */
    @Override
    public void OnRefresh() {
        if (isCanRequest) {
            getData();
        }
    }
}
