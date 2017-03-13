package com.smartdot.mobile.portal.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.google.gson.reflect.TypeToken;
import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.adapter.GroupListAdapter;
import com.smartdot.mobile.portal.bean.GroupInfoBean;
import com.smartdot.mobile.portal.utils.CommonUtil;
import com.smartdot.mobile.portal.utils.CustomToast;
import com.smartdot.mobile.portal.utils.NetUtils;
import com.smartdot.mobile.portal.utils.ThemeHelper;
import com.smartdot.mobile.portal.utils.VolleyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 群组列表界面
 */
public class GroupListActivity extends BaseActivity
        implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private Context mContext;

    private ImageView title_left_img;

    private TextView title_center_text;

    private ImageView title_right_img;

    private TextView title_right_text;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private RecyclerView mRecyclerView;

    private GroupListAdapter mAdapter;

    List<GroupInfoBean> groupList = new ArrayList<>();

    private boolean isChooseGroupCard = false;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case 1001:
                try {
                    JSONObject jsonObject = new JSONObject(msg.obj.toString());
                    if (jsonObject.getString("code").equals("200")) {
                        groupList = CommonUtil.gson.fromJson(jsonObject.getString("result"),
                                new TypeToken<List<GroupInfoBean>>() {
                                }.getType());
                        mAdapter.setmList(groupList);
                        mAdapter.notifyDataSetChanged();
                    } else {
                        CustomToast.showToast(mContext, R.string.operation_failed, 400);
                    }
                    mSwipeRefreshLayout.setRefreshing(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            default:

                break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);
        mContext = this;
        initView();
        // 进入的时候显示刷新，必须调用以下代码
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        GroupListActivity.this.onRefresh();
        ThemeHelper.setSwipeRefreshLayoutColor(mContext,mSwipeRefreshLayout);
    }

    private void initView() {
        title_left_img = (ImageView) findViewById(R.id.title_left_img);
        title_center_text = (TextView) findViewById(R.id.title_center_text);
        title_right_img = (ImageView) findViewById(R.id.title_right_img);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.group_list_swiperefreshlayout);
        mRecyclerView = (RecyclerView) findViewById(R.id.group_list_lv);
        title_right_text = (TextView) findViewById(R.id.title_right_text);

        title_right_text.setText(R.string.add_select_group);

        title_left_img.setOnClickListener(this);
        title_center_text.setOnClickListener(this);
        title_right_text.setOnClickListener(this);
        title_right_text.setVisibility(View.VISIBLE);

        title_center_text.setText(R.string.my_groups);
        title_right_img.setVisibility(View.GONE);
        title_center_text.setVisibility(View.VISIBLE);

        mAdapter = new GroupListAdapter(mContext, groupList);
        mAdapter.setActivity(GroupListActivity.this);
        mAdapter.setChooseGroupCard(GloableConfig.groupListType == 1);
        mRecyclerView.setAdapter(mAdapter);
        RecyclerView.LayoutManager lm = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(lm);

        // 刷新时，指示器旋转后变化的颜色
        mSwipeRefreshLayout.setColorSchemeResources(R.color.blue, R.color.yellow);
        mSwipeRefreshLayout.setOnRefreshListener(this);

//        // 设置RecyclerView的分割线
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.title_left_img) {
            finish();
        } else if (v.getId() == R.id.title_center_text) {
            getData();
        }else if (v.getId() == R.id.title_right_text) {
            // TODO: 2016/8/4 创建群组
            Intent intent = new Intent(mContext, OrganizationActivity.class);
            GloableConfig.addressBookType = 2;
            startActivity(intent);
        }
    }


    /**
     * 获取网络数据
     */
    private void getData() {
        if (!NetUtils.isConnected(mContext)){
            CustomToast.showToast(mContext,getString(R.string.net_error),400);
            return;
        }
        VolleyUtil volleyUtil = new VolleyUtil(mContext);
        String url = String.format(GloableConfig.RongCloud.getGroupListUrl, GloableConfig.myUserInfo.userId);
        volleyUtil.stringRequest(handler, Request.Method.POST, url, 1001);
    }

    @Override
    public void onRefresh() {
        getData();
    }
}
