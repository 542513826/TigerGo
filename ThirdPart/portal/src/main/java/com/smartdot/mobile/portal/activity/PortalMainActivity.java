package com.smartdot.mobile.portal.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.smartdot.mobile.portal.PortalApplication;
import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.bean.TabBean;
import com.smartdot.mobile.portal.utils.CommonUtils;
import com.smartdot.mobile.portal.utils.DisplayUtil;
import com.smartdot.mobile.portal.utils.JpushUtil;
import com.smartdot.mobile.portal.widget.BadgeView;
import com.smartdot.mobile.portal.widget.CircleBadgeView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

/**
 * 主Activity 在此类加载4个导航模块
 */
public class PortalMainActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {

    private Context mContext;

    /** 消息图标所处的位置（通过config.xml读取） */
    private int messageIndex;

    private ViewPager mViewPager;

    private FragmentPagerAdapter mAdapter;

    private List<Fragment> mFragments;

    private SparseArray<Fragment> fragmentMap;

    private RadioGroup rg;

    private RadioButton rb;

    private int screenWidth;

    private List<TabBean> tabBeanList;

    private LinearLayout badgeContainer;

    List<CircleBadgeView> badges = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.portal_main);
        mContext = this;

        registerServer();
        initData();
        initViews();
    }

    /** 注册第三方SDK服务 */
    private void registerServer() {
        /** 融云通讯 */
        if (GloableConfig.RongCloud.useRong) {
            registerRongChangedListerner();
            // 当前融云未连接。点击通知的时候如果程序没有启动，则跳转到启动界面
            if (RongIM.getInstance().getCurrentConnectionStatus()
                    .equals(RongIMClient.ConnectionStatusListener.ConnectionStatus.DISCONNECTED)) {
                Intent intent1 = mContext.getPackageManager().getLaunchIntentForPackage(mContext.getPackageName());
                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(intent1);
                finish();
            }
        }
        /** 极光推送 */
        if (GloableConfig.useJpush) {
            JpushUtil.registerJpush(mContext);
            // 在此处把程序启动标志位设置为true
            GloableConfig.isRunning = true;
        }
    }

    /** 初始化数据 */
    private void initData() {
        messageIndex = mContext.getResources().getInteger(R.integer.messageIndex); // 读取消息图标所在位置
        tabBeanList = PortalApplication.getTabBeans();
        TabBean tabBean;
        fragmentMap = new SparseArray<>();
        for (int i = 0; i < tabBeanList.size(); i++) {
            tabBean = tabBeanList.get(i);
            fragmentMap.put(tabBean.getIndex(), tabBean.getF());
        }
    }

    private void initViews() {
        badgeContainer = (LinearLayout) findViewById(R.id.badge_container);
        rg = (RadioGroup) findViewById(R.id.tab_menu);
        // 导航栏顶部阴影
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            rg.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
        }
        rg.setOnCheckedChangeListener(this);

        RadioButton rb;
        Collections.sort(tabBeanList); // 排序
        screenWidth = CommonUtils.getScreenWidth(mContext);
        // 通过读取配置文件动态创建底部Tab导航栏中的RadioButton
        for (int i = 0; i < tabBeanList.size(); i++) {
            rb = new RadioButton(this);
            rb.setId(tabBeanList.get(i).getIndex());
            rb.setLayoutParams(new LinearLayout.LayoutParams(screenWidth / tabBeanList.size(),ViewGroup.LayoutParams.WRAP_CONTENT));
            rb.setTextColor(ContextCompat.getColor(mContext, R.drawable.btn_color));
            rb.setTextSize(12);
            rb.setMaxLines(1);
            rb.setCompoundDrawablePadding(15);
            rb.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            rb.setPadding(3, DisplayUtil.dip2px(mContext, 5), 3, 3);
            int resID = getResources().getIdentifier(tabBeanList.get(i).getPicName(), "drawable",
                    mContext.getApplicationInfo().packageName);
            Drawable drawable = ContextCompat.getDrawable(mContext, resID);
            rb.setBackground(null);
            rb.setButtonDrawable(android.R.color.transparent);
            rb.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
            rb.setText(tabBeanList.get(i).getLabel());
            rg.addView(rb);
            if (i == 0) {
                rb.setChecked(true);
            }
        }
        // 通过读取配置文件动态创建底部tab导航栏中对应的红点
        for (int i = 0; i < tabBeanList.size(); i++) {
            RelativeLayout badgeRl = new RelativeLayout(mContext);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(screenWidth / tabBeanList.size(),
                    DisplayUtil.dip2px(mContext, 50));
            badgeRl.setGravity(Gravity.CENTER);
            badgeRl.setLayoutParams(lp);
            Button button = new Button(mContext);
            LinearLayout.LayoutParams buttonlp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(mContext, 50),
                    DisplayUtil.dip2px(mContext, 50));
            button.setLayoutParams(buttonlp);
            button.setGravity(Gravity.CENTER);
            button.setBackground(null);
            button.setClickable(false);
            badgeRl.addView(button);
            badgeContainer.addView(badgeRl);
            CircleBadgeView badge = new CircleBadgeView(this, button);
            badge.setBackgroundColor(Color.RED);// 设置背景颜色
            badge.setTextSize(8);// 设置文字大小
            badge.setGravity(Gravity.CENTER);// 设置文字居中
            badges.add(badge);
        }
    }

    /**
     * 注册融云未读消息数监听
     */
    private void registerRongChangedListerner() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                RongIM.getInstance().setOnReceiveUnreadCountChangedListener(mCountListener, conversationTypes);
            }
        }, 500);
    }

    /**
     * showTag为要显示的fragment对应的标签
     */
    private void switchFragment(int showTag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        for (int i = 0; i < fragmentMap.size(); i++) {
            Fragment f = fragmentMap.get(i + 1);
            if (f == fragmentMap.get(showTag)) {
                if (!f.isAdded()) {// 未被添加
                    transaction.add(R.id.fragment_view, fragmentMap.get(showTag));
                } else if (f.isHidden()) {// 添加了但被隐藏
                    transaction.show(f);
                }
            } else if (f.isAdded() && !f.isHidden()) {// 被添加但未被隐藏
                transaction.hide(f);
            }
        }
        transaction.commit();
    }

    /**
     * 没有聊天的时候发起聊天
     */
    public void startchat(View view) {
        GloableConfig.addressBookType = 3;
        Intent intent = new Intent(mContext, OrganizationActivity.class);
        startActivity(intent);
    }

    /**
     * 角标监听
     */
    RongIM.OnReceiveUnreadCountChangedListener mCountListener = new RongIM.OnReceiveUnreadCountChangedListener() {
        @Override
        public void onMessageIncreased(int count) {
            // TODO: 2016/7/26 改变角标
            changeBadge(messageIndex, count);
        }
    };

    /**
     * 改变角标
     *
     * @param index
     *            位置(角标实际位置需要-1)
     * @param count
     *            角标显示的数值
     */
    public void changeBadge(int index, int count) {
        CircleBadgeView badge = badges.get(messageIndex - 1);
        badge.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
        if (count > 0) {
            if (badge.isShown()) {
                badge.setText(count + "");
            } else {
                badge.setText(count + "");
                badge.show();
            }
        } else {
            badge.hide();
        }
        GloableConfig.RongCloud.RongYunMessage = count;
    }

    /**
     * 来信息时的角标
     */
    Conversation.ConversationType[] conversationTypes = { Conversation.ConversationType.PRIVATE,
            Conversation.ConversationType.DISCUSSION, Conversation.ConversationType.GROUP,
            Conversation.ConversationType.SYSTEM, Conversation.ConversationType.PUBLIC_SERVICE,
            Conversation.ConversationType.APP_PUBLIC_SERVICE };

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        removeAllFragmentsFromMap();
    }

    /**
     * 移除所有的fragment
     */
    private void removeAllFragmentsFromMap() {
        fragmentMap.clear();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switchFragment(checkedId);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        /** 从Activity返回到主Activity后重定向到[消息]模块 */
        rb = (RadioButton) findViewById(messageIndex);
        rb.setChecked(true);// 会主动回调onCheckedChanged函数的
    }

}
