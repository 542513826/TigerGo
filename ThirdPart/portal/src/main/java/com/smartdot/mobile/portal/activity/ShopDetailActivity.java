package com.smartdot.mobile.portal.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.adapter.ShopCommentAdapter;
import com.smartdot.mobile.portal.adapter.ShopRecyclerViewAdapter;
import com.smartdot.mobile.portal.bean.AppDetailBean;
import com.smartdot.mobile.portal.bean.AppPicUrlBean;
import com.smartdot.mobile.portal.bean.UserCommentBean;
import com.smartdot.mobile.portal.utils.CommonUtil;
import com.smartdot.mobile.portal.utils.CustomToast;
import com.smartdot.mobile.portal.utils.ImageLoaderUtils;
import com.smartdot.mobile.portal.utils.NetUtils;
import com.smartdot.mobile.portal.utils.NumberUtils;
import com.smartdot.mobile.portal.utils.ProgressUtil;
import com.smartdot.mobile.portal.utils.VolleyUtil;
import com.smartdot.mobile.portal.utils.openAppManage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 应用详情界面 Created by Administrator on 2016/7/25.
 */
public class ShopDetailActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;

    private TextView cancel_tv;

    private ImageView title_left_img;

    private TextView title_center_text;

    private TextView title_right_text;

    private ImageView app_icon_iv;

    private TextView app_name_tv;

    private TextView app_type_tv;

    private RatingBar ratingBar;

    private TextView shop_score_tv;

    private LinearLayout shop_item_open;

    private TextView app_introduce_tv;

    private TextView shop_comment_number_tv;

    private RatingBar grayBar;

    private ScrollView mScrollView;

    private TextView detail_update_tv;

    private TextView detail_app_type_tv;

    private TextView detail_versions_tv;

    private TextView detail_app_size;

    private TextView detail_app_compatibility;

    private TextView detail_app_language;

    private RatingBar detail_overall_star;

    private TextView detail_overall_grade_tv;

    private TextView shop_item_open_tv;

    private ImageView shop_item_open_img;

    private ProgressBar detail_five_star_progressBar, detail_four_star_progressBar, detail_three_star_progressBar,
            detail_two_star_progressBar, detail_one_star_progressBar;

    /** 应用详情 */
    private AppDetailBean mAppDetailBean;

    private ShopCommentAdapter adapter;

    private ListView comment_lv;

    private String appId;

    private String versionId;

    /** 应用评论 */
    private List<UserCommentBean> commentList = new ArrayList<UserCommentBean>();

    /** 图片截图 */
    private List<AppPicUrlBean> picList = new ArrayList<AppPicUrlBean>();

    /** 应用id */
    private String appID;

    private RecyclerView shop_RecyclerView;

    private ShopRecyclerViewAdapter mRecyclerViewAdapter;

    private RecyclerView.LayoutManager mLayoutManager;

    private DisplayImageOptions options;

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

                    JSONObject result = new JSONObject(msg.obj.toString());
                    mAppDetailBean = CommonUtil.gson.fromJson(result.getString("result"), AppDetailBean.class);
                    // 刷新图片截图adapter
                    picList.clear();
                    picList.addAll(mAppDetailBean.app_screenshot);
                    mRecyclerViewAdapter.setPicList(picList);
                    mRecyclerViewAdapter.notifyDataSetChanged();
                    // 刷新评论列表adapter
                    commentList.clear();
                    commentList.addAll(parseCommentList(mAppDetailBean.commentList));
                    adapter.notifyDataSetChanged();
                    // 填充详情各项数据
                    parseData();
                    setListViewHeightBasedOnChildren(comment_lv);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case 1002:
                    try {
                        JSONObject result = new JSONObject(msg.obj.toString());
                        JSONObject json = result.getJSONObject("returnValueObject");
                        int resultCode = json.getInt("resultCode");
                        if (resultCode == 200) {
                            // TODO: 向服务器发送安装/卸载状态成功
                            shop_item_open_tv.setText(R.string.app_open);
                            shop_item_open_img
                                    .setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.pic_shop_open));
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
        setContentView(R.layout.activity_shop_detail);
        mContext = this;
        initView();
        getData();
    }

    private void initView() {
        cancel_tv = (TextView) findViewById(R.id.cancel_tv);
        title_left_img = (ImageView) findViewById(R.id.title_left_img);
        title_center_text = (TextView) findViewById(R.id.title_center_text);
        title_right_text = (TextView) findViewById(R.id.title_right_text);
        app_icon_iv = (ImageView) findViewById(R.id.app_icon_iv);
        app_name_tv = (TextView) findViewById(R.id.app_name_tv);
        app_type_tv = (TextView) findViewById(R.id.app_type_tv);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        shop_score_tv = (TextView) findViewById(R.id.shop_score_tv);
        shop_comment_number_tv = (TextView) findViewById(R.id.shop_comment_number_tv);
        shop_item_open = (LinearLayout) findViewById(R.id.shop_item_open);
        app_introduce_tv = (TextView) findViewById(R.id.app_introduce_tv);
        grayBar = (RatingBar) findViewById(R.id.grayBar);
        mScrollView = (ScrollView) findViewById(R.id.mScrollView);

        detail_update_tv = (TextView) findViewById(R.id.detail_update_tv);
        detail_app_type_tv = (TextView) findViewById(R.id.detail_app_type_tv);
        detail_versions_tv = (TextView) findViewById(R.id.detail_versions_tv);
        detail_app_size = (TextView) findViewById(R.id.detail_app_size);
        detail_app_compatibility = (TextView) findViewById(R.id.detail_app_compatibility);
        detail_app_language = (TextView) findViewById(R.id.detail_app_language);
        shop_item_open_tv = (TextView) findViewById(R.id.shop_item_open_tv);
        shop_item_open_img = (ImageView) findViewById(R.id.shop_item_open_img);

        detail_overall_grade_tv = (TextView) findViewById(R.id.detail_overall_grade_tv);
        detail_overall_star = (RatingBar) findViewById(R.id.detail_overall_star);
        detail_five_star_progressBar = (ProgressBar) findViewById(R.id.detail_five_star_progressBar);
        detail_four_star_progressBar = (ProgressBar) findViewById(R.id.detail_four_star_progressBar);
        detail_three_star_progressBar = (ProgressBar) findViewById(R.id.detail_three_star_progressBar);
        detail_two_star_progressBar = (ProgressBar) findViewById(R.id.detail_two_star_progressBar);
        detail_one_star_progressBar = (ProgressBar) findViewById(R.id.detail_one_star_progressBar);

        picList = new ArrayList<>();
        shop_RecyclerView = (RecyclerView) findViewById(R.id.shop_RecyclerView);
        mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerViewAdapter = new ShopRecyclerViewAdapter(mContext, picList);
        shop_RecyclerView.setAdapter(mRecyclerViewAdapter);
        shop_RecyclerView.setLayoutManager(mLayoutManager);

        adapter = new ShopCommentAdapter(ShopDetailActivity.this, commentList, R.layout.item_shop_app_comment);
        comment_lv = (ListView) findViewById(R.id.comment_lv);
        comment_lv.setAdapter(adapter);
        setListViewHeightBasedOnChildren(comment_lv);

        title_center_text.setText(R.string.shop);
        title_right_text.setText(R.string.do_evaluation);
        title_left_img.setOnClickListener(this);
        title_right_text.setOnClickListener(this);
        options = ImageLoaderUtils.initOptions();
        disableAutoScrollToBottom();
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

        Intent intent = getIntent();
        appID = intent.getStringExtra("appId");

        VolleyUtil volleyUtil = new VolleyUtil(mContext);
        Map<String, String> map = new HashMap<>();
        map.put("appId", appID);
        map.put("userId", GloableConfig.myUserInfo.userId);
        volleyUtil.stringRequest(handler, GloableConfig.AppDetailUrl, map, 1001);
    }

    /**
     * 解析数据
     */
    private void parseData() {
        appId = mAppDetailBean.app_id;
        versionId = mAppDetailBean.app_version;
        app_name_tv.setText(mAppDetailBean.app_name);
        app_type_tv.setText(mAppDetailBean.app_info.type);
        app_introduce_tv.setText(mAppDetailBean.app_introduce);
        ImageLoader.getInstance().displayImage(mAppDetailBean.app_icon, app_icon_iv, options);

        detail_update_tv.setText(mAppDetailBean.app_info.updateDate);
        detail_app_type_tv.setText(mAppDetailBean.app_info.type);
        detail_versions_tv.setText(mAppDetailBean.app_info.version);
        detail_app_size.setText(mAppDetailBean.app_info.size);
        // TODO: 兼容性和语言没有 暂时不加
        // detail_app_compatibility.setText(mAppDetailBean.app_info.compatibility);
        // detail_app_language.setText(mAppDetailBean.app_info.Language);

        ratingBar.setRating(NumberUtils.keepPrecision(mAppDetailBean.app_startNum, 2));
        shop_comment_number_tv.setText("( " + mAppDetailBean.app_markNum + " )");
        detail_overall_grade_tv.setText(mAppDetailBean.gradeInfo.grades + "份评分");
        detail_overall_star.setRating(mAppDetailBean.gradeInfo.overallRating);
        detail_five_star_progressBar.setProgress(mAppDetailBean.gradeInfo.fiveStarPercentage);
        detail_four_star_progressBar.setProgress(mAppDetailBean.gradeInfo.fourStarPercentage);
        detail_three_star_progressBar.setProgress(mAppDetailBean.gradeInfo.threeStarPercentage);
        detail_two_star_progressBar.setProgress(mAppDetailBean.gradeInfo.twoStarPercentage);
        detail_one_star_progressBar.setProgress(mAppDetailBean.gradeInfo.oneStarPercentage);

        // 根据不同的安装状态显示不同的图标
        switch (Integer.parseInt(mAppDetailBean.app_Setup)) {
        case 0:
            shop_item_open_tv.setText("打开");
            shop_item_open_img.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.pic_shop_open));
            break;
        case 1:
            shop_item_open_tv.setText("更新");
            shop_item_open_img.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.pic_shop_updata));
            break;
        case 2:
            shop_item_open_tv.setText("下载");
            shop_item_open_img.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.pic_shop_down));
            title_right_text.setVisibility(View.GONE);// 没有下载过应用不能评价
            break;
        }

        // 点击应用操作
        shop_item_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (Integer.parseInt(mAppDetailBean.app_Setup)) {
                case 0:
                    // TODO: 打开应用逻辑 - 执行打开
                    openAppManage manage = new openAppManage(mContext);
                    manage.openApp(mAppDetailBean);
                    break;
                case 1:
                case 2:
                    // TODO: 下载/更新应用逻辑 - 执行下载
                    if (mAppDetailBean.app_web) {
                        sendMessage(mContext, mAppDetailBean, 2);
                    } else {
                        openAppManage manage2 = new openAppManage(mContext);
                        manage2.downloadFile(mAppDetailBean);
                    }
                    break;
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.title_left_img) {
            finish();
            overridePendingTransition(R.anim.base_back_in, R.anim.base_back_out);// 退场动画
        } else if (v.getId() == R.id.title_right_text) {
            // 进入应用评价界面
            Intent intent = new Intent(mContext, EaluationActivity.class);
            intent.putExtra("appId", appId);
            intent.putExtra("versionId", versionId);
            startActivity(intent);
            overridePendingTransition(R.anim.base_slide_right_in, R.anim.base_slide_right_out);// 进场动画
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
     * 将commentList中的无用数据剔除
     */
    private List<UserCommentBean> parseCommentList(List<UserCommentBean> commentList) {
        if (commentList == null) {
            return new ArrayList<UserCommentBean>();
        }
        List<UserCommentBean> list = new ArrayList<UserCommentBean>();
        for (UserCommentBean bean : commentList) {
            if (bean.commentUserName != null && !bean.commentUserName.isEmpty()) {
                UserCommentBean data = new UserCommentBean();
                data.commentDate = bean.commentDate;
                data.commentTitle = bean.commentTitle;
                data.commentStar = bean.commentStar;
                data.commentUserName = bean.commentUserName;
                data.commentValue = bean.commentValue;
                list.add(data);
            }
        }
        return list;
    }

    /** 向服务器发送当前应用的安装/卸载状态 */
    public void sendMessage(Context mContext, AppDetailBean data, int state) {
        VolleyUtil volleyUtil = new VolleyUtil(mContext);
        Map<String, String> map = new HashMap<>();
        map.put("userId", GloableConfig.myUserInfo.userId);
        map.put("appId", data.app_id);
        map.put("versionId", data.app_version);
        if (state == 1) {
            volleyUtil.stringRequest(handler, GloableConfig.SetupAppUrl, map, 1002);// 安装
        } else if (state == 2) {
            volleyUtil.stringRequest(handler, GloableConfig.UninstallAppUrl, map, 1002);// 卸载
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
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1)) + 200;
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
}
