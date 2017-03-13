package com.smartdot.mobile.portal.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.activity.OrganizationActivity;
import com.smartdot.mobile.portal.application.App;
import com.smartdot.mobile.portal.application.MyAppContext;
import com.smartdot.mobile.portal.bean.GroupInfoBean;
import com.smartdot.mobile.portal.utils.CommonUtil;
import com.smartdot.mobile.portal.utils.CustomToast;
import com.smartdot.mobile.portal.utils.DisplayUtil;
import com.smartdot.mobile.portal.utils.NetUtils;
import com.smartdot.mobile.portal.utils.ProgressUtil;
import com.smartdot.mobile.portal.utils.RongUtil;
import com.smartdot.mobile.portal.utils.SharePreferenceUtils;
import com.smartdot.mobile.portal.utils.VolleyUtil;
import com.xys.libzxing.zxing.activity.CaptureActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;
import io.rong.message.InformationNotificationMessage;

/**
 * 消息列表 Created by zhangt on 2016/7/19.
 */
public class ConversionListFragment extends Fragment
        implements View.OnClickListener, RongIMClient.OnReceiveMessageListener {
    private Context mContext;

    private ImageView title_left_img;

    private TextView title_center_text;

    /** 添加按钮 */
    private ImageView title_right_img;

    /** 添加按钮弹出的view */
    private PopupWindow mPopupWindow;

    private View title_bar;

    private RelativeLayout search_rl;

    /** 融云的会话列表界面 */
    ConversationListFragment fragment;

    List<Conversation> mlist = new ArrayList<>();

    private RelativeLayout no_net_rl;

    private Button refresh_button;

    private String targetId;

    private String targetName = "";

    private String removeGroupId = "";

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case 1001: // 请求群组信息
                dealWithGroupInfo(msg.obj.toString());
                break;
            case 1002:// 加入群组
                dealWithAddGroup(msg.obj.toString());
                break;
            case 1003:// 被邀请进入群组
                dealWithJoinGroup(msg.obj.toString());
                break;
            case 1004:// 群组解散或被移出群组
                dealWithDestoryGroup(msg.obj.toString());
                break;
            default:
                break;
            }

        }
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_conversation_list, null);
        mContext = getActivity();
        SharePreferenceUtils.getAppConfig(mContext);
        initView(view);
        enterFragment();
        isReconnect();
        RongIM.getInstance().getConversationList(new RongIMClient.ResultCallback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                mlist = conversations;
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        RongIM.setOnReceiveMessageListener(this);
    }

    private void initView(View view) {
        title_bar = view.findViewById(R.id.title_bar);
        title_left_img = (ImageView) view.findViewById(R.id.title_left_img);
        title_center_text = (TextView) view.findViewById(R.id.title_center_text);
        title_right_img = (ImageView) view.findViewById(R.id.title_right_img);
        search_rl = (RelativeLayout) view.findViewById(R.id.search_rl);
        no_net_rl = (RelativeLayout) view.findViewById(R.id.no_net_rl);
        refresh_button = (Button) view.findViewById(R.id.refresh_button);

        if (!NetUtils.isConnected(mContext)) {
            no_net_rl.setVisibility(View.VISIBLE);
        } else {
            no_net_rl.setVisibility(View.GONE);
        }

        title_center_text.setText(R.string.message);
        title_right_img.setVisibility(View.VISIBLE);
        title_left_img.setVisibility(View.GONE);
        title_right_img.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.btn_add));

        title_left_img.setOnClickListener(this);
        title_center_text.setOnClickListener(this);
        title_right_img.setOnClickListener(this);
        search_rl.setOnClickListener(this);
        refresh_button.setOnClickListener(this);
    }

    /**
     * 加载 会话列表 ConversationListFragment
     */
    private void enterFragment() {

        fragment = new ConversationListFragment();

        Uri uri = Uri.parse("rong://" + GloableConfig.CURRENT_PKGNAME).buildUpon().appendPath("conversationlist")
                .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "false") // 设置私聊会话非聚合显示
                .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "false")// 设置群组会话非聚合显示
                .appendQueryParameter(Conversation.ConversationType.DISCUSSION.getName(), "false")// 设置讨论组会话非聚合显示
                .appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "false")// 设置系统会话非聚合显示
                .appendQueryParameter(Conversation.ConversationType.PUBLIC_SERVICE.getName(), "false")// 公共服务号
                .appendQueryParameter(Conversation.ConversationType.APP_PUBLIC_SERVICE.getName(), "false")// 订阅号
                .build();

        fragment.setUri(uri);

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.conversationlist, fragment);
        transaction.commit();
    }

    /**
     * 判断消息是否是 push 消息
     *
     */
    private void isReconnect() {
        Intent intent = getActivity().getIntent();
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

        if (mContext.getApplicationInfo().packageName.equals(App.getCurProcessName(mContext.getApplicationContext()))) {

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
    private void initPopupWindow() {
        Button startChatBtn;
        Button sendMessagesBtn;
        Button scanningBtn;
        View popView = getActivity().getLayoutInflater().inflate(R.layout.popupwindow_aboutme, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popView.setTranslationZ(10);
        }
        mPopupWindow = new PopupWindow(popView, DisplayUtil.dip2px(mContext, 140), DisplayUtil.dip2px(mContext, 150),
                true);

        startChatBtn = (Button) popView.findViewById(R.id.startChatBtn);
        sendMessagesBtn = (Button) popView.findViewById(R.id.sendMessagesBtn);
        scanningBtn = (Button) popView.findViewById(R.id.scanningBtn);
        /** 开始聊天 */
        startChatBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
                mPopupWindow = null;
                GloableConfig.addressBookType = 3;
                Intent intent = new Intent(mContext, OrganizationActivity.class);
                startActivity(intent);
            }
        });
        /** 群发消息 */
        sendMessagesBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
                mPopupWindow = null;
                // TODO: 2016/7/14 群发消息
            }
        });
        /** 扫描二维码 */
        scanningBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
                mPopupWindow = null;
                Intent openCameraIntent = new Intent(mContext, CaptureActivity.class);
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

    private void getGroupInfo(String id) {
        if (!NetUtils.isConnected(mContext)) {
            CustomToast.showToast(mContext, getString(R.string.net_error), 400);
            return;
        }
        // ProgressUtil.showPregressDialog(mContext, R.layout.custom_progress);
        VolleyUtil volleyUtil = new VolleyUtil(mContext);
        String url = String.format(GloableConfig.RongCloud.getGroupInfoUrl, id);
        volleyUtil.stringRequest(handler, Request.Method.POST, url, 1001);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 2001) {
                Bundle bundle = data.getExtras();
                String scanResult = bundle.getString("result");
                if (scanResult.contains(getString(R.string.use_mp_scan))) {
                    scanResult = scanResult.replace("]", "").replace("\n", "");
                    String[] results = scanResult.split(":");
                    targetId = results[1];
                    getGroupInfo(targetId);
                }
            }

        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.title_right_img) {
            getPopupWindow();
            // 这里是popwindow位置显示方式
            mPopupWindow.showAsDropDown(title_bar, title_bar.getWidth(), 0);
        } else if (v.getId() == R.id.search_rl) {
            // 搜索
            // Intent intent = new Intent(mContext, SearchActivity.class);
            // startActivity(intent);
        } else if (v.getId() == R.id.refresh_button) {
            // 无网刷新
            if (!NetUtils.isConnected(mContext)) {
                no_net_rl.setVisibility(View.VISIBLE);
            } else {
                no_net_rl.setVisibility(View.GONE);
            }
        }

    }

    private void addGroup(final String id, final String name, final boolean inGroup) {
        new AlertDialog.Builder(mContext).setTitle(getString(R.string.isAddGroup) + id + "?")
                .setMessage(getString(R.string.group_name) + "：" + name)
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (inGroup) {
                            dialog.dismiss();
                            CustomToast.showToast(mContext, getString(R.string.you_has_in_this_group), 400);
                            ProgressUtil.dismissProgressDialog();
                            RongUtil.startChat(mContext, Conversation.ConversationType.GROUP, id, name);
                        } else {
                            // TODO: 2016/7/14 加入群组
                            VolleyUtil volleyUtil = new VolleyUtil(mContext);
                            String url = String.format(GloableConfig.RongCloud.addGroupUrl,
                                    GloableConfig.myUserInfo.userId, id, name, "");
                            volleyUtil.stringRequest(handler, Request.Method.POST, url, 1002);
                            dialog.dismiss();
                        }
                    }
                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ProgressUtil.dismissProgressDialog();
                        dialog.dismiss();
                    }
                }).setCancelable(false).show();
    }

    @Override
    public boolean onReceived(io.rong.imlib.model.Message message, int i) {
        if (message.getContent() instanceof InformationNotificationMessage) {
            String info = ((InformationNotificationMessage) message.getContent()).getMessage();
            String id = message.getTargetId();
            System.out.println(info + "cinversionlistfragment");
            if (info.substring(0, 3).equals(getString(R.string.groupName))) {
                String newName = info.substring(7, info.length());
                Group group = new Group(id, newName, null);
                RongIM.getInstance().refreshGroupInfoCache(group);
            } else if (info.substring(0, 4).equals(getString(R.string.group_host))) {

            } else if (info.contains(getString(R.string.dissolution))) {
                if (GloableConfig.allGroupMap.containsKey(message.getTargetId())) {
                    removeGroupId = message.getTargetId();
                    VolleyUtil volleyUtil = new VolleyUtil(mContext);
                    String url = String.format(GloableConfig.RongCloud.getGroupInfoUrl, id);
                    volleyUtil.stringRequest(handler, Request.Method.POST, url, 1004);
                }
            } else if (info.contains(getString(R.string.exit))) {

            } else if (info.contains(getString(R.string.remove))) {
                if (GloableConfig.allGroupMap.containsKey(message.getTargetId())) {
                    removeGroupId = message.getTargetId();
                    VolleyUtil volleyUtil = new VolleyUtil(mContext);
                    String url = String.format(GloableConfig.RongCloud.getGroupInfoUrl, id);
                    volleyUtil.stringRequest(handler, Request.Method.POST, url, 1004);
                }
            } else if (info.contains(getString(R.string.invitation))) {
                if (GloableConfig.allGroupMap.containsKey(message.getTargetId())) {
                    // 如果用户被拉进了新的群组,获取这个群组的信息，填充到列表里去
                    VolleyUtil volleyUtil = new VolleyUtil(mContext);
                    String url = String.format(GloableConfig.RongCloud.getGroupInfoUrl, id);
                    volleyUtil.stringRequest(handler, Request.Method.POST, url, 1003);
                }
            } else if (info.contains(getString(R.string.start_group_chat))) {
                VolleyUtil volleyUtil = new VolleyUtil(mContext);
                String url = String.format(GloableConfig.RongCloud.getGroupInfoUrl, id);
                volleyUtil.stringRequest(handler, Request.Method.POST, url, 1003);
            }

        }

        return false;
    }

    /**
     * 处理请求群组信息后返回的数据
     * 
     * @param resultString
     */
    private void dealWithGroupInfo(String resultString) {
        try {
            if (resultString.contains(getString(R.string.system_error))) {
                CustomToast.showToast(mContext, mContext.getString(R.string.no_group), 400);
                return;
            }
            JSONObject jsonObject = new JSONObject(resultString);
            if (jsonObject.getString("code").equals("200")) {

                GroupInfoBean groupInfoBean = CommonUtil.gson.fromJson(jsonObject.getString("result"),
                        GroupInfoBean.class);
                Boolean inGroup = false; // 是否在群组中
                for (int i = 0; i < groupInfoBean.memberList.size(); i++) {
                    if (groupInfoBean.memberList.get(i).userId.equals(GloableConfig.myUserInfo.userId)) {
                        inGroup = true;
                    }
                }
                addGroup(groupInfoBean.id, groupInfoBean.name, inGroup);
            } else if (jsonObject.getString("code").equals("500")) {
                CustomToast.showToast(mContext, getString(R.string.you_are_not_group_member), 400);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理加群返回的数据
     * 
     * @param resultString
     */
    private void dealWithAddGroup(String resultString) {
        try {
            JSONObject jsonObject = new JSONObject(resultString);
            if (jsonObject.getString("code").equals("200")) {
                CustomToast.showToast(mContext, getString(R.string.addgroup_success), 400);
                ProgressUtil.dismissProgressDialog();
                RongUtil.startChat(mContext, Conversation.ConversationType.GROUP, targetId, targetName);
            } else {
                CustomToast.showToast(mContext, getString(R.string.group_has_destory), 400);
                ProgressUtil.dismissProgressDialog();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * 处理被邀请如群的时候的返回的数据
     */
    private void dealWithJoinGroup(String resultString) {
        try {
            JSONObject jsonObject = new JSONObject(resultString);
            if (jsonObject.getString("code").equals("200")) {
                GroupInfoBean groupInfoBean = CommonUtil.gson.fromJson(jsonObject.getString("result"),
                        GroupInfoBean.class);
                GloableConfig.allGroupMap.put(groupInfoBean.id, groupInfoBean);
                Group group;
                try {
                    group = new Group(groupInfoBean.id, groupInfoBean.name, null);
                } catch (RuntimeException e) {
                    group = new Group(groupInfoBean.id, getString(R.string.default_group_name), null);
                }
                RongIM.getInstance().refreshGroupInfoCache(group);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ProgressUtil.dismissProgressDialog();
    }

    /**
     * 处理群组解散或被移出群组的操作
     */
    private void dealWithDestoryGroup(String resultString) {
        try {
            if (resultString.contains(getString(R.string.system_error))) {
                // 清除聊天列表里的信息
                RongUtil.removeConversationListItem(removeGroupId);
                return;
            }
            JSONObject jsonObject = new JSONObject(resultString);
            if (jsonObject.getString("code").equals("200")) {
                GroupInfoBean groupInfoBean = CommonUtil.gson.fromJson(jsonObject.getString("result"),
                        GroupInfoBean.class);
                Boolean inGroup = false; // 是否在群组中
                for (int i = 0; i < groupInfoBean.memberList.size(); i++) {
                    if (groupInfoBean.memberList.get(i).userId.equals(GloableConfig.myUserInfo.userId)) {
                        inGroup = true;
                    }
                }
                if (!inGroup) {
                    // 清除聊天列表里的信息
                    RongUtil.removeConversationListItem(removeGroupId);
                }
            } else {
                // 清除聊天列表里的信息
                RongUtil.removeConversationListItem(removeGroupId);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
