package com.smartdot.mobile.portal.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.adapter.ShopListAdapter;
import com.smartdot.mobile.portal.bean.AppDetailBean;
import com.smartdot.mobile.portal.port.OnShopAppRefreshListener;
import com.smartdot.mobile.portal.utils.CommonUtil;
import com.smartdot.mobile.portal.utils.ThemeHelper;
import com.smartdot.mobile.portal.utils.VolleyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商店的activity
 */
public class ShopActivity extends BaseActivity implements View.OnClickListener, OnShopAppRefreshListener {

    private Context mContext;

    private TextView title_center_text;

    private Button shop_updata_size_bt;

    private ScrollView mScrollView;

    private ListView shop_lv;

    private List<AppDetailBean> mList = new ArrayList<>();

    private ShopListAdapter adapter;

    private TextView not_value_tv;

    private String type_id = "";

    /**
     * 进度条控件
     */
    private SwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * 防止同时进行多次请求 是否允许发起请求
     */
    private boolean isCanRequest = true;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case 1001:
                try {
                    mSwipeRefreshLayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mSwipeRefreshLayout.setRefreshing(false);// 关闭刷新动画
                        }
                    }, 1000);
                    isCanRequest = true;
                    /** 接收到返回的全部应用列表进行刷新 */
                    JSONObject result = new JSONObject(msg.obj.toString());
                    List<AppDetailBean> list = CommonUtil.gson.fromJson(result.getString("result"),
                            new TypeToken<List<AppDetailBean>>() {
                            }.getType());
                    mList.clear();
                    mList.addAll(list);
                    adapter.notifyDataSetChanged();
                    parseList(mList);
                    setListViewHeightBasedOnChildren(shop_lv);
                    if (mList.size() == 0) {
                        not_value_tv.setVisibility(View.VISIBLE);
                    } else {
                        not_value_tv.setVisibility(View.GONE);
                    }
                } catch (NullPointerException | JSONException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        mContext = this;

        initView();
        addListener();
        getData("");
    }

    /**
     * 获取数据
     */
    private void getData(String type_id) {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);// 显示刷新动画
            }
        });
        isCanRequest = false;

        VolleyUtil volleyUtil = new VolleyUtil(mContext);
        Map<String, String> map = new HashMap<>();
        map.put("userId", GloableConfig.myUserInfo.userId);
        map.put("deptId", GloableConfig.myUserInfo.obey_dept_id);
        map.put("osType", GloableConfig.OS_TYPE);
        map.put("deviceType", GloableConfig.DEVICE_TYPE);
        if (!type_id.equals("")) {
            map.put("category", type_id);
        }
        volleyUtil.stringRequest(handler, GloableConfig.AppListUrl, map, 1001);
    }

    private void initView() {
        ImageView title_left_img = (ImageView) findViewById(R.id.title_left_img);
        TextView title_right_text = (TextView) findViewById(R.id.title_right_text);
        LinearLayout installiert_layout = (LinearLayout) findViewById(R.id.installiert_layout);
        LinearLayout not_installed_layout = (LinearLayout) findViewById(R.id.not_installed_layout);
        LinearLayout update_layout = (LinearLayout) findViewById(R.id.update_layout);
        LinearLayout remove_layout = (LinearLayout) findViewById(R.id.remove_layout);
        LinearLayout shop_search_layout = (LinearLayout) findViewById(R.id.shop_search_layout);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.id_swiperefreshlayout);
        mSwipeRefreshLayout.setEnabled(false);// 禁用下拉刷新手势响应
        ThemeHelper.setSwipeRefreshLayoutColor(mContext, mSwipeRefreshLayout);// 刷新转动颜色
        shop_updata_size_bt = (Button) findViewById(R.id.shop_updata_size_bt);
        not_value_tv = (TextView) findViewById(R.id.not_value_tv);
        title_center_text = (TextView) findViewById(R.id.title_center_text);
        mScrollView = (ScrollView) findViewById(R.id.mScrollView);
        shop_lv = (ListView) findViewById(R.id.shop_lv);
        adapter = new ShopListAdapter(ShopActivity.this, mList, R.layout.item_shop_header);
        adapter.setOnShopAppRefreshListener(this);
        shop_lv.setAdapter(adapter);
        setListViewHeightBasedOnChildren(shop_lv);
        title_right_text.setText(R.string.all_app_type);

        title_left_img.setOnClickListener(this);
        title_right_text.setOnClickListener(this);
        installiert_layout.setOnClickListener(this);
        not_installed_layout.setOnClickListener(this);
        update_layout.setOnClickListener(this);
        remove_layout.setOnClickListener(this);
        shop_search_layout.setOnClickListener(this);
        disableAutoScrollToBottom();
    }

    private void addListener() {
        shop_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AppDetailBean bean = mList.get(position);
                Intent intent = new Intent(mContext, ShopDetailActivity.class);
                intent.putExtra("appId", bean.app_id);
                mContext.startActivity(intent);
                overridePendingTransition(R.anim.base_slide_right_in, R.anim.base_slide_right_out);// 进场动画
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData(type_id);
            }
        });
    }

    /**
     * 将需要升级的应用数量筛选出来并显示在升级应用的红点上
     */
    private void parseList(List<AppDetailBean> list) {
        List<AppDetailBean> dataList = new ArrayList<>();
        for (AppDetailBean bean : list) {
            if (bean.app_Setup.equals("1")) {
                dataList.add(bean);
            }
        }
        shop_updata_size_bt.setText("" + dataList.size());
        if (dataList.size() == 0) {
            shop_updata_size_bt.setVisibility(View.INVISIBLE);// 无升级应用时隐藏红点
        } else {
            shop_updata_size_bt.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.title_left_img) {
            finish();
            overridePendingTransition(R.anim.base_back_in, R.anim.base_back_out);// 退场动画
            return;
        } else if (v.getId() == R.id.title_right_text) {// 应用类别
            Intent intent = new Intent(mContext, AppTypeActivity.class);
            startActivityForResult(intent, 1);
        } else if (v.getId() == R.id.installiert_layout) {
            Intent intent = new Intent(mContext, ShopClassifyActivity.class);
            intent.putExtra("appType", "已装应用");
            mContext.startActivity(intent);
        } else if (v.getId() == R.id.not_installed_layout) {
            Intent intent = new Intent(mContext, ShopClassifyActivity.class);
            intent.putExtra("appType", "未装应用");
            mContext.startActivity(intent);
        } else if (v.getId() == R.id.update_layout) {
            Intent intent = new Intent(mContext, ShopClassifyActivity.class);
            intent.putExtra("appType", "升级应用");
            mContext.startActivity(intent);
        } else if (v.getId() == R.id.remove_layout) {
            Intent intent = new Intent(mContext, ShopClassifyActivity.class);
            intent.putExtra("appType", "移除应用");
            mContext.startActivity(intent);
        } else if (v.getId() == R.id.shop_search_layout) {// 搜索应用
            Intent intent = new Intent(mContext, SearchActivity.class);
            mContext.startActivity(intent);
            return;
        }
        overridePendingTransition(R.anim.base_slide_right_in, R.anim.base_slide_right_out);// 进场动画
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isCanRequest) {
            getData("");
            title_center_text.setText("商店");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                // 根据类别重新获取应用列表
                type_id = data.getStringExtra("appType");
                String type_name = data.getStringExtra("appTypeName");
                title_center_text.setText(type_name);
                getData(type_id);
            }
        }
    }

    /**
     * 动态设置ListView的高度
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        if (listView == null)
            return;
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    /**
     * 禁止ScrollView的childview自动滑动到底部
     */
    private void disableAutoScrollToBottom() {
        mScrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        mScrollView.setFocusable(true);
        mScrollView.setFocusableInTouchMode(true);
        mScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.requestFocusFromTouch();
                return false;
            }
        });
    }

    /**
     * 接收在adapter卸载轻应用后的回调,重新请求数据刷新界面
     */
    @Override
    public void OnRefresh() {
        if (isCanRequest) {
            getData("");
        }
    }

}
