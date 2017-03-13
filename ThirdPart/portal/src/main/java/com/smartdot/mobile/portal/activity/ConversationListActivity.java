package com.smartdot.mobile.portal.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartdot.mobile.portal.PortalApplication;
import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.application.MyAppContext;
import com.smartdot.mobile.portal.bean.GroupInfoBean;
import com.smartdot.mobile.portal.utils.CommonUtil;
import com.smartdot.mobile.portal.utils.CustomToast;
import com.smartdot.mobile.portal.utils.DisplayUtil;
import com.smartdot.mobile.portal.utils.RongUtil;
import com.smartdot.mobile.portal.utils.SharePreferenceUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

/**
 * 会话列表/弃用弃用弃用弃用弃用弃用弃用弃用
 */
public class ConversationListActivity extends FragmentActivity implements View.OnClickListener {

    private Context mContext;

    private ImageView title_left_img;

    private TextView title_center_text;

    private ImageView title_right_img;

    private PopupWindow mPopupWindow;

    private View title_bar;

    private RelativeLayout search_rl;

    List<Conversation> mlist = new ArrayList<>();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case 1001:
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(msg.obj.toString());
                    if (jsonObject.getString("code").equals("200")) {
                        GroupInfoBean groupInfoBean = new GroupInfoBean();
                        groupInfoBean = CommonUtil.gson.fromJson(jsonObject.getString("result"), GroupInfoBean.class);

                        CustomToast.showToast(mContext, R.string.operation_success, 400);
                    } else {
                        CustomToast.showToast(mContext, R.string.operation_failed, 400);
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
        setContentView(R.layout.activity_conversation_list);
        mContext = this;
        SharePreferenceUtils.getAppConfig(mContext);
        initView();
        isReconnect();
        mlist = RongIM.getInstance().getRongIMClient().getConversationList();
    }

    private void initView() {
        title_bar = findViewById(R.id.title_bar);
        title_left_img = (ImageView) findViewById(R.id.title_left_img);
        title_center_text = (TextView) findViewById(R.id.title_center_text);
        title_right_img = (ImageView) findViewById(R.id.title_right_img);
        search_rl = (RelativeLayout) findViewById(R.id.search_rl);

        title_center_text.setText("消息");
        title_right_img.setVisibility(View.VISIBLE);
        title_right_img.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.btn_add));

        title_left_img.setOnClickListener(this);
        title_center_text.setOnClickListener(this);
        title_right_img.setOnClickListener(this);
        search_rl.setOnClickListener(this);

    }

    /**
     * 加载 会话列表 ConversationListFragment
     */
    private void enterFragment() {

        ConversationListFragment fragment = (ConversationListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.conversationlist);

        Uri uri = Uri.parse("rong://" + GloableConfig.CURRENT_PKGNAME).buildUpon().appendPath("conversationlist")
                .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "false") // 设置私聊会话非聚合显示
                .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "false")// 设置群组会话非聚合显示
                .appendQueryParameter(Conversation.ConversationType.DISCUSSION.getName(), "false")// 设置讨论组会话非聚合显示
                .appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "false")// 设置系统会话非聚合显示
                .appendQueryParameter(Conversation.ConversationType.PUBLIC_SERVICE.getName(), "false")// 公共服务号
                .appendQueryParameter(Conversation.ConversationType.APP_PUBLIC_SERVICE.getName(), "false")// 订阅号
                .build();

        fragment.setUri(uri);

    }

    /**
     * 判断消息是否是 push 消息
     */
    private void isReconnect() {

        Intent intent = getIntent();
        String token = (String) SharePreferenceUtils.getParam("TOKEN", "default");

        // push，通知或新消息过来
        if (intent != null && intent.getData() != null && intent.getData().getScheme().equals("rong")) {

            // 通过intent.getData().getQueryParameter("push") 为true，判断是否是push消息
            if (intent.getData().getQueryParameter("push") != null
                    && intent.getData().getQueryParameter("push").equals("true")) {

                reconnect(token);
            } else {
                // 程序切到后台，收到消息后点击进入,会执行这里
                if (RongIM.getInstance() == null || RongIM.getInstance().getRongIMClient() == null) {

                    reconnect(token);
                } else {
                    enterFragment();
                }
            }
        }
    }

    /**
     * 重连
     *
     * @param token
     */
    private void reconnect(String token) {

        if (getApplicationInfo().packageName.equals(PortalApplication.getCurProcessName(getApplicationContext()))) {

            RongIM.connect(token, new RongIMClient.ConnectCallback() {
                @Override
                public void onTokenIncorrect() {

                }

                @Override
                public void onSuccess(String s) {
                    MyAppContext.getInstance().setOtherListener();
                    enterFragment();
                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {

                }
            });
        }
    }

    /**
     * 初始化popupwindow
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initPopupWindow() {
        Button startChatBtn;
        Button sendMessagesBtn;
        Button scanningBtn;
        View popView = getLayoutInflater().inflate(R.layout.popupwindow_aboutme, null);
        popView.setTranslationZ(10);
        mPopupWindow = new PopupWindow(popView, DisplayUtil.dip2px(mContext, 140), DisplayUtil.dip2px(mContext, 150),
                true);

        startChatBtn = (Button) popView.findViewById(R.id.startChatBtn);
        sendMessagesBtn = (Button) popView.findViewById(R.id.sendMessagesBtn);
        scanningBtn = (Button) popView.findViewById(R.id.scanningBtn);

        startChatBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
                mPopupWindow = null;
                GloableConfig.addressBookType = 0;
                Intent intent = new Intent(mContext, OrganizationActivity.class);
                startActivity(intent);
            }
        });
        sendMessagesBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
                mPopupWindow = null;
                // TODO: 2016/7/14 群发消息
            }
        });

        scanningBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
                mPopupWindow = null;
                Intent openCameraIntent = new Intent(mContext, ZXingActivity.class);
                startActivityForResult(openCameraIntent, 2001);
            }
        });
        // 触摸屏幕关闭popupwindow
        popView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mPopupWindow != null && mPopupWindow.isShowing()) {
                    mPopupWindow.dismiss();
                    mPopupWindow = null;
                }
                return false;
            }
        });

    }

    /**
     * 获取PopupWindow实例
     **/
    private void getPopupWindow() {
        if (null != mPopupWindow) {
            mPopupWindow.dismiss();
            return;
        } else {
            initPopupWindow();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString("result");
            if (scanResult.contains("请打开移动门户来扫描本二维码")) {
                scanResult = scanResult.replace("]", "").replace("\n", "");
                String[] results = scanResult.split(":");
                addGroup(results[1]);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.title_left_img) {
            finish();
        } else if (v.getId() == R.id.title_right_img) {
            getPopupWindow();
            // 这里是位置显示方式
            mPopupWindow.showAsDropDown(title_bar, title_bar.getWidth(), 0);
        } else if (v.getId() == R.id.search_rl) {
            Intent intent = new Intent(mContext, SearchActivity.class);
            startActivity(intent);
        }

    }

    /**
     * 没有聊天的时候发起聊天
     *
     * @param view
     */
    public void startchat(View view) {
        GloableConfig.addressBookType = 0;
        Intent intent = new Intent(mContext, OrganizationActivity.class);
        startActivity(intent);
    }

    /**
     * 扫码加入群组
     * 
     * @param id
     */
    private void addGroup(final String id) {
        new AlertDialog.Builder(mContext).setTitle("是否加入群组" + id)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RongUtil.addGroup(mContext, id, handler, 1001);
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setCancelable(false).show();
    }
}
