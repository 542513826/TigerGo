package com.smartdot.mobile.portal.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.adapter.AppTypeAdapter;
import com.smartdot.mobile.portal.bean.AppTypeBean;
import com.smartdot.mobile.portal.utils.CommonUtil;
import com.smartdot.mobile.portal.utils.ProgressUtil;
import com.smartdot.mobile.portal.utils.VolleyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * app的全部分类界面
 */
public class AppTypeActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;

    private GridView shop_type_gridview;

    private List<AppTypeBean> mList;

    private AppTypeAdapter mAdapter;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case 1001:
                try {
                    ProgressUtil.dismissProgressDialog();
                    JSONObject result = new JSONObject(msg.obj.toString());
                    mList = CommonUtil.gson.fromJson(result.getString("result"), new TypeToken<List<AppTypeBean>>() {
                    }.getType());
                    AppTypeBean allTypeBean = new AppTypeBean();
                    allTypeBean.type_id = "";
                    allTypeBean.type_name = "所有类别";
                    allTypeBean.type_pic = "";
                    mList.add(allTypeBean);
                    mAdapter.setmList(mList);
                    mAdapter.notifyDataSetChanged();
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
        setContentView(R.layout.activity_shop_type);
        mContext = this;
        initView();
        setListener();
        getData();
    }

    private void initView() {
        TextView cancel_tv = (TextView) findViewById(R.id.cancel_tv);
        ImageView title_left_img = (ImageView) findViewById(R.id.title_left_img);
        TextView title_center_text = (TextView) findViewById(R.id.title_center_text);
        TextView title_right_text = (TextView) findViewById(R.id.title_right_text);
        shop_type_gridview = (GridView) findViewById(R.id.shop_type_gridview);

        cancel_tv.setText(R.string.cancel);
        title_center_text.setText(R.string.shop);

        cancel_tv.setOnClickListener(this);
        cancel_tv.setVisibility(View.VISIBLE);
        title_left_img.setVisibility(View.GONE);
        title_right_text.setVisibility(View.GONE);

        mList = new ArrayList<>();
        mAdapter = new AppTypeAdapter(mContext, mList);
        shop_type_gridview.setAdapter(mAdapter);
    }

    private void setListener() {
        shop_type_gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("appType", mList.get(position).type_id);
                intent.putExtra("appTypeName", mList.get(position).type_name);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    /**
     * 获取应用类别
     */
    private void getData() {
        ProgressUtil.showPregressDialog(this, R.layout.custom_progress);
        VolleyUtil volleyUtil = new VolleyUtil(mContext);
        Map<String, String> map = new HashMap<>();
        volleyUtil.stringRequest(handler, GloableConfig.CategoryUrl, map, 1001);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.cancel_tv) {
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
            overridePendingTransition(R.anim.base_back_in, R.anim.base_back_out);// 退场动画
        }
    }
}
