package com.smartdot.mobile.portal.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.activity.ShopActivity;
import com.smartdot.mobile.portal.adapter.ChannelAdapter;
import com.smartdot.mobile.portal.bean.AppDetailBean;
import com.smartdot.mobile.portal.drag.ItemDragHelperCallback;
import com.smartdot.mobile.portal.drag.MyLayoutManager;
import com.smartdot.mobile.portal.drag.RecycleViewDivider;
import com.smartdot.mobile.portal.drag.WrapHeightGridLayoutManager;
import com.smartdot.mobile.portal.utils.CommonUtil;
import com.smartdot.mobile.portal.utils.ThemeHelper;
import com.smartdot.mobile.portal.utils.VolleyUtil;
import com.smartdot.mobile.portal.utils.openAppManage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 底部tab导航 - 应用 增加注释
 */
public class appFragment extends Fragment implements View.OnClickListener {

    private Context mContext;

    private View view;

    private RecyclerView mRecy;

    public List<AppDetailBean> items = new ArrayList<AppDetailBean>();

    private ChannelAdapter adapter;

    /** 进度条控件 */
    private SwipeRefreshLayout mSwipeRefreshLayout;

    /** 红点 用来提示当前已安装应用有更新的 */
    private View update_point_view;

    /** 设置九宫格每排几个图标 */
    public int column = 3;

    /** 防止同时进行多次请求 是否允许发起请求 */
    private boolean isCanRequest = true;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case 1001:
                mSwipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);// 关闭刷新动画
                    }
                },1000);
                parseData(msg);
                break;
            }
        }
    };

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_app, container, false);
        mContext = getActivity();

        initView();
        getData();
        addListener();

        return view;
    }



    private void getData() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override public void run() {
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
        volleyUtil.stringRequest(handler, GloableConfig.MyAppUrl, map, 1001);
    }

    private void parseData(Message msg) {
        isCanRequest = true;
        try {
            JSONObject result = new JSONObject(msg.obj.toString());
            List<AppDetailBean> list = CommonUtil.gson.fromJson(result.getString("result"),
                    new TypeToken<List<AppDetailBean>>() {
                    }.getType());
            items.clear();
            items.addAll(list);
            adapter.notifyDataSetChanged();
            parseList(items);
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.id_swiperefreshlayout);
        mSwipeRefreshLayout.setEnabled(false);// 禁用下拉刷新手势响应
        ThemeHelper.setSwipeRefreshLayoutColor(mContext,mSwipeRefreshLayout);
        // 背景图片 用来点击让其退出编辑模式的
        TextView app_home_ad = (TextView) view.findViewById(R.id.app_home_ad);
        ImageView title_right_img = (ImageView) view.findViewById(R.id.title_right_img);
        update_point_view = view.findViewById(R.id.update_point_view);
        title_right_img.setOnClickListener(this);
        app_home_ad.setOnClickListener(this);

        // 添加布局方向并设置分割线
        MyLayoutManager layoutManager = new MyLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecy = (RecyclerView) view.findViewById(R.id.recy);
        mRecy.addItemDecoration(
                new RecycleViewDivider(mContext, layoutManager.getOrientation(), R.drawable.pic_bookmark_x));
        // 设置为一排3个图标
        WrapHeightGridLayoutManager manager = new WrapHeightGridLayoutManager(mContext, column);
        mRecy.setLayoutManager(manager);
        // 在界面中调用拖拽回调
        ItemDragHelperCallback callback = new ItemDragHelperCallback();
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(mRecy);
        // 控制布局大小
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int viewType = adapter.getItemViewType(position);
                // 三等分 这个3代表一行最少3个
                return viewType == ChannelAdapter.TYPE_MY || viewType == ChannelAdapter.TYPE_FOOTER ? 1 : 3;
            }
        });
        // 初始化应用列表布局
        adapter = new ChannelAdapter(mContext, helper, items);
        mRecy.setAdapter(adapter);
        adapter.setFooterView(true);
    }

    private void addListener() {
        // 应用列表布局中的图标点击事件
        adapter.setOnMyChannelItemClickListener(new ChannelAdapter.OnMyChannelItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                startApp(position);
            }
        });
        // 应用列表布局中的删除图标点击事件
        adapter.setOnDeleteItemItemClickListener(new ChannelAdapter.OnDeleteItemClickListener() {
            @Override
            public void onDeleteItemClick(View v, int position) {
                deleteApp(position);
            }
        });
    }

    /**
     * 启动应用
     *
     * @param position
     *            当前点击应用的position,通过 items.get(position) 即可获得当前应用实体类bean
     */
    public void startApp(int position) {
        AppDetailBean data = items.get(position);
        openAppManage manage = new openAppManage(mContext);
        manage.openApp(data);
    }

    /**
     * 删除应用
     *
     * @param position
     *            当前点击应用的position,通过 items.get(position) 即可获得当前应用实体类bean
     */
    public void deleteApp(int position) {
        AppDetailBean data = items.get(position);
        openAppManage manage = new openAppManage(mContext);
        manage.deleteApp(data);
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
        if (dataList.size() == 0) {
            update_point_view.setVisibility(View.GONE);// 无升级应用时隐藏红点
        } else {
            update_point_view.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.title_right_img) {
            /** 跳转到商店界面 */
            Intent intent = new Intent(mContext, ShopActivity.class);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.base_slide_right_in, R.anim.base_slide_right_out);
        } else if (v.getId() == R.id.app_home_ad) {
            /** 点击壁纸解除编辑模式 */
            if (adapter.isEditMode()) {
                adapter.cancelEditMode(mRecy);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.isHidden()) {
            return;
        }
        // 处于编辑模式就重置状态
        if (adapter.isEditMode()) {
            adapter.cancelEditMode(mRecy);
        }
        // 应用安装/卸载完毕后回到界面刷新数据
        if (isCanRequest) {
            getData();
        }
        // 对返回back键做监听 按返回时结束添加应用
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (adapter.isEditMode()) {
                    adapter.cancelEditMode(mRecy);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden && isCanRequest) {
            ThemeHelper.setSwipeRefreshLayoutColor(mContext,mSwipeRefreshLayout);
            getData();
        }
        super.onHiddenChanged(hidden);
    }
}
