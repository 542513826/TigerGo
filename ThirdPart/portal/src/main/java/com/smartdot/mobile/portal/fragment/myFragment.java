package com.smartdot.mobile.portal.fragment;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bilibili.magicasakura.utils.ThemeUtils;
import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.activity.UserInfoActivity;
import com.smartdot.mobile.portal.activity.UserSetActivity;
import com.smartdot.mobile.portal.bean.UserInfoBean;
import com.smartdot.mobile.portal.utils.CommonUtils;
import com.smartdot.mobile.portal.utils.ThemeHelper;
import com.smartdot.mobile.portal.utils.VolleyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 底部tab导航 - 我的
 */
public class myFragment extends Fragment implements View.OnClickListener, ThemeHelper.ClickListener {

    private Context mContext;

    private View view;

    /** 个性签名 */
    public TextView myfragment_signature;

    /** 文件 */
    private RelativeLayout file_rl;

    /** 收藏 */
    private RelativeLayout collect_rl;

    /** 主题 */
    private RelativeLayout theme_rl;

    /** 设置的小红点 */
    private ImageView set_point;

    /** 设置 */
    private RelativeLayout set_rl;

    /** 头像 */
    private ImageView head_img;

    /** 名字 */
    private TextView title_tv;

    /** 职务 */
    private TextView position_tv;

    /** 职务后面的小箭头 */
    private LinearLayout my_user_set_layout;

    /** 进度条控件 */
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private UserInfoBean userInfoBean;

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
                    JSONObject jsonObject = new JSONObject(msg.obj.toString());
                    userInfoBean = CommonUtils.GsonFromJsonToBean(jsonObject.getString("user"), UserInfoBean.class);
                    title_tv.setText(userInfoBean.userName);
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    };

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my, container, false);
        mContext = getActivity();
        initView(view);
        getData();
        return view;
    }

    public void initView(View view) {
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.id_swiperefreshlayout);
        mSwipeRefreshLayout.setEnabled(false);// 禁用下拉刷新手势响应
        ThemeHelper.setSwipeRefreshLayoutColor(mContext, mSwipeRefreshLayout);
        myfragment_signature = (TextView) view.findViewById(R.id.myfragment_signature);
        file_rl = (RelativeLayout) view.findViewById(R.id.file_rl);
        collect_rl = (RelativeLayout) view.findViewById(R.id.collect_rl);
        theme_rl = (RelativeLayout) view.findViewById(R.id.theme_rl);
        set_point = (ImageView) view.findViewById(R.id.set_point);
        set_rl = (RelativeLayout) view.findViewById(R.id.set_rl);
        head_img = (ImageView) view.findViewById(R.id.myfragment_head_img);
        title_tv = (TextView) view.findViewById(R.id.myfragment_title_tv);
        position_tv = (TextView) view.findViewById(R.id.myfragment_position_tv);
        my_user_set_layout = (LinearLayout) view.findViewById(R.id.my_user_set_layout);

        myfragment_signature.setOnClickListener(this);
        file_rl.setOnClickListener(this);
        collect_rl.setOnClickListener(this);
        theme_rl.setOnClickListener(this);
        set_rl.setOnClickListener(this);
        head_img.setOnClickListener(this);
        my_user_set_layout.setOnClickListener(this);
    }

    /**
     * 获取数据
     */
    private void getData() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);// 显示刷新动画
            }
        });
        VolleyUtil volleyUtil = new VolleyUtil(mContext);
        Map<String, String> map = new HashMap<>();
        map.put("userId", GloableConfig.myUserInfo.userId);
        volleyUtil.stringRequest(handler, GloableConfig.UserinfoUrl, map, 1001);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.myfragment_signature) {
            // TODO: 2016/7/27 点击个性签名
        } else if (v.getId() == R.id.file_rl) {
            // TODO: 2016/7/27 我的文件
        } else if (v.getId() == R.id.collect_rl) {
            // TODO: 2016/7/27 收藏
        } else if (v.getId() == R.id.theme_rl) {
            // TODO: 2016/7/27 主题
            changeTheme();
        } else if (v.getId() == R.id.set_rl) {
            // TODO: 2016/7/27 设置
            Intent intent = new Intent(mContext, UserSetActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.myfragment_head_img) {
            // TODO: 2016/7/27 点击了头像
        } else if (v.getId() == R.id.my_user_set_layout) {
            Intent intent = new Intent(mContext, UserInfoActivity.class);
            startActivity(intent);
        }
        ((Activity) mContext).overridePendingTransition(R.anim.base_slide_right_in, R.anim.base_slide_right_out);// 进场动画
    }


    /** 换肤*/
    public void changeTheme() {
        ThemeHelper.showThemeDialog(mContext, this);
    }

    /** 点击主题后的监听 */
    @Override
    public void onConfirm(int currentTheme) {
        if (ThemeHelper.getTheme(mContext) != currentTheme) {
            // 切换主题
            ThemeHelper.setTheme(mContext, currentTheme);
            // 刷新UI
            ThemeUtils.refreshUI(mContext, new ThemeUtils.ExtraRefreshable() {
                @Override
                public void refreshGlobal(Activity activity) {
                    if (Build.VERSION.SDK_INT >= 21) {
                        ActivityManager.TaskDescription description = new ActivityManager.TaskDescription(null, null,
                                ThemeUtils.getThemeAttrColor(mContext, android.R.attr.colorPrimary));
                        getActivity().setTaskDescription(description);
                        getActivity().getWindow().setStatusBarColor(ThemeUtils.getColorById(mContext, R.color.theme_color_primary_dark));
                    }
                }

                @Override
                public void refreshSpecificView(View view) {

                }
            });
        }
    }
}
