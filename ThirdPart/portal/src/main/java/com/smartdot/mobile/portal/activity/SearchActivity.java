package com.smartdot.mobile.portal.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.adapter.SearchAdapter;
import com.smartdot.mobile.portal.bean.AppDetailBean;
import com.smartdot.mobile.portal.port.OnShopAppRefreshListener;
import com.smartdot.mobile.portal.utils.CommonUtil;
import com.smartdot.mobile.portal.utils.CommonUtils;
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
 * 搜索界面
 */
public class SearchActivity extends BaseActivity implements View.OnClickListener, OnShopAppRefreshListener {

    private Context mContext;

    private ImageView title_left_img;

    private TextView title_center_text;

    private ImageView title_right_img;

    private TextView cancel_tv;

    private RelativeLayout search_rl;

    private List<AppDetailBean> mList;

    private ListView search_lv;

    private SearchAdapter adapter;

    private EditText search_et;

    private TextView not_value_tv;

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

                    List<AppDetailBean> list = new ArrayList<AppDetailBean>();
                    JSONObject result = new JSONObject(msg.obj.toString());
                    list = CommonUtil.gson.fromJson(result.getString("result"), new TypeToken<List<AppDetailBean>>() {
                    }.getType());
                    mList.clear();
                    mList.addAll(list);
                    adapter.notifyDataSetChanged();
                    if (mList.size() == 0) {
                        not_value_tv.setVisibility(View.VISIBLE);
                    } else {
                        not_value_tv.setVisibility(View.GONE);
                    }
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
        setContentView(R.layout.activity_search);
        mContext = this;

        initView();
        addListener();
    }

    private void addListener() {
        search_et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchData();
                    return true;
                }
                return false;
            }
        });
    }

    private void searchData() {
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
        map.put("searchAppName", search_et.getText().toString().trim());
        volleyUtil.stringRequest(handler, GloableConfig.AppListUrl, map, 1001);
    }

    private void initView() {
        title_left_img = (ImageView) findViewById(R.id.title_left_img);
        title_center_text = (TextView) findViewById(R.id.title_center_text);
        not_value_tv = (TextView) findViewById(R.id.not_value_tv);
        title_right_img = (ImageView) findViewById(R.id.title_right_img);
        cancel_tv = (TextView) findViewById(R.id.cancel_tv);
        search_rl = (RelativeLayout) findViewById(R.id.search_rl);
        search_et = (EditText) findViewById(R.id.search_et);

        title_center_text.setText(R.string.shop);
        title_right_img.setVisibility(View.GONE);

        mList = new ArrayList<>();
        adapter = new SearchAdapter(SearchActivity.this, mList, R.layout.item_shop_search);
        adapter.setOnShopAppRefreshListener(this);
        search_lv = (ListView) findViewById(R.id.search_lv);
        search_lv.setAdapter(adapter);

        title_left_img.setOnClickListener(this);
        cancel_tv.setOnClickListener(this);
        search_rl.setOnClickListener(this);
        CommonUtils.showKeyboard(SearchActivity.this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.title_left_img) {
            CommonUtils.hideKeyboard(SearchActivity.this);
            finish();
        } else if (v.getId() == R.id.cancel_tv) {
            CommonUtils.hideKeyboard(SearchActivity.this);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isCanRequest) {
            searchData();
        }
    }

    /**
     * 接收在adapter卸载轻应用后的回调,重新请求数据刷新界面
     */
    @Override
    public void OnRefresh() {
        if (isCanRequest) {
            searchData();
        }
    }
}
