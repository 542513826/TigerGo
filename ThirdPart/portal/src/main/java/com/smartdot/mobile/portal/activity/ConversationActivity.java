package com.smartdot.mobile.portal.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.application.MyAppContext;
import com.smartdot.mobile.portal.bean.GroupInfoBean;
import com.smartdot.mobile.portal.utils.CommonUtil;
import com.smartdot.mobile.portal.utils.CustomToast;
import com.smartdot.mobile.portal.utils.L;
import com.smartdot.mobile.portal.utils.NetUtils;
import com.smartdot.mobile.portal.utils.ProgressUtil;
import com.smartdot.mobile.portal.utils.RongUtil;
import com.smartdot.mobile.portal.utils.SharePreferenceUtils;
import com.smartdot.mobile.portal.utils.VolleyUtil;
import com.smartdot.mobile.portal.widget.LoadingDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imkit.widget.InputView;
import io.rong.imkit.widget.provider.InputProvider;
import io.rong.imkit.widget.provider.TextInputProvider;
import io.rong.imlib.MessageTag;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.TypingMessage.TypingStatus;
import io.rong.imlib.location.RealTimeLocationConstant;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;
import io.rong.message.InformationNotificationMessage;
import io.rong.message.LocationMessage;
import io.rong.message.RichContentMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;

/**
 * 会话界面
 */
public class ConversationActivity extends FragmentActivity implements RongIMClient.OnReceiveMessageListener {

    private ConversationFragment fragment;

    private Context mContext;

    /** 标题 */
    private TextView titleTextView;

    /** 返回按钮 */
    private ImageView backImageView;

    /** 聊天信息的按钮 */
    private ImageView chatinfobutton;

    /** 对方id */
    private String mTargetId;

    /** 会话类型 */
    private Conversation.ConversationType mConversationType;

    /** title */
    private String title;

    /** 是否在讨论组内，如果不在讨论组内，则进入不到讨论组设置页面 */
    private boolean isDiscussion = false;

    private boolean isFromPush = false;

    private RelativeLayout mRealTimeBar;// real-time bar

    private RealTimeLocationConstant.RealTimeLocationStatus currentLocationStatus;

    private LoadingDialog mDialog;

    private SharedPreferences sp;

    private String TextTypingTitle;

    private String VoiceTypingTitle;

    public static final int SET_TEXT_TYPING_TITLE = 1;

    public static final int SET_VOICE_TYPING_TITLE = 2;

    public static final int SET_TARGETID_TITLE = 0;

    private UserInfo userInfo;

    private RelativeLayout nonet_rl;

    private Button refreshBtn;

    private String titleString = "";

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case SET_TEXT_TYPING_TITLE:
                titleTextView.setText(TextTypingTitle);
                break;
            case SET_VOICE_TYPING_TITLE:
                titleTextView.setText(VoiceTypingTitle);
                break;
            case SET_TARGETID_TITLE:
                titleTextView.setText(titleString);
                break;
            case 1001:
                // 加群
                dealWithAddGroup(msg.obj.toString());
                break;
            case 1002: // 被拉进了群聊
                dealWithJoinGroup(msg.obj.toString());
                break;
            default:
                break;
            }
            return true;
        }

    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        mDialog = new LoadingDialog(this);
        mContext = this;
        SharePreferenceUtils.getAppConfig(mContext);
        Intent intent = getIntent();
        sp = getSharedPreferences("config", MODE_PRIVATE);
        initView();
        getChatInfo();
        addListener();
        checkTextInputEditTextChanged(); // 讨论组 @ 消息
        isPushMessage(intent);
        getPermissions();
        setTypingStatusListener();
        RongIM.setOnReceiveMessageListener(this);
    }

    private void initView() {
        TextTypingTitle = getString(R.string.The_other_party_is_inputting);
        VoiceTypingTitle = getString(R.string.The_other_is_speaking);
        titleTextView = (TextView) findViewById(R.id.title_center_text);
        backImageView = (ImageView) findViewById(R.id.title_left_img);
        chatinfobutton = (ImageView) findViewById(R.id.title_right_img);
        nonet_rl = (RelativeLayout) findViewById(R.id.no_net_rl);
        refreshBtn = (Button) findViewById(R.id.refresh_button);

        if (NetUtils.isConnected(mContext)) {
            nonet_rl.setVisibility(View.GONE);
        } else {
            nonet_rl.setVisibility(View.VISIBLE);
        }

        chatinfobutton.setVisibility(View.VISIBLE);
        backImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 关闭会话界面后回调到主Activity再跳转到[消息]模块
                Intent intent = new Intent(mContext, PortalMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        // 跳转到 群/个人 详情界面
        chatinfobutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (mConversationType.getValue() == 1) {
                    intent = new Intent(mContext, ChatUserInfoActivity.class);
                } else {
                    intent = new Intent(mContext, ChatGroupInfoActivity.class);
                }
                intent.putExtra("conversationType", mConversationType);
                intent.putExtra("targetId", mTargetId);
                startActivityForResult(intent, 2001);
            }
        });

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetUtils.isConnected(mContext)) {
                    nonet_rl.setVisibility(View.GONE);
                } else {
                    nonet_rl.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void getChatInfo() {
        Intent intent = getIntent();
        if (intent == null || intent.getData() == null)
            return;
        // titleString =
        // String.format(getResources().getString(R.string.talk_with), title);
        mTargetId = intent.getData().getQueryParameter("targetId");
        mConversationType = Conversation.ConversationType
                .valueOf(intent.getData().getLastPathSegment().toUpperCase(Locale.getDefault()));
        title = intent.getData().getQueryParameter("title");
        titleString = title;
        titleTextView.setText(title);
        // 为群组聊天时,判断最后一条消息是否为[已不在该群组]
        if (mConversationType.getValue() == 3) {
            chatinfobutton.setImageResource(R.drawable.btn_setting_point);
            try {
                MessageContent messageContent = RongIM.getInstance()
                        .getConversation(Conversation.ConversationType.GROUP, mTargetId).getLatestMessage();
                if (messageContent instanceof InformationNotificationMessage) {
                    String info = ((InformationNotificationMessage) messageContent).getMessage();
                    if (info.contains(getString(R.string.you_are_not))) {
                        chatinfobutton.setVisibility(View.GONE);
                        // 清除聊天列表里的信息
                        RongUtil.removeConversationListItem(mTargetId);
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 监听输入信息
     */
    private void checkTextInputEditTextChanged() {

        InputProvider.MainInputProvider provider = RongContext.getInstance().getPrimaryInputProvider();
        if (provider instanceof TextInputProvider) {
            TextInputProvider textInputProvider = (TextInputProvider) provider;
            textInputProvider.setEditTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    if (mConversationType.equals(Conversation.ConversationType.DISCUSSION)) {

                        if (s.length() > 0) {
                            String str = s.toString().substring(s.toString().length() - 1, s.toString().length());
                            // 此处是@信息的处理逻辑
                            // if (str.equals("@")) {
                            //
                            // Intent intent = new
                            // Intent(ConversationActivity.this,
                            // NewTextMessageActivity.class);
                            // intent.putExtra("DEMO_REPLY_CONVERSATIONTYPE",
                            // mConversationType.toString());
                            //
                            //// if (mTargetIds != null) {
                            //// UriFragment fragment = (UriFragment)
                            // getSupportFragmentManager().getFragments().get(0);
                            //// //得到讨论组的 targetId
                            //// mTargetId =
                            // fragment.getUri().getQueryParameter("targetId");
                            //// }
                            // intent.putExtra("DEMO_REPLY_TARGETID",
                            // mTargetId);
                            // startActivityForResult(intent, 29);
                            //
                            // mEditText = s.toString();
                            // }
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }

    /**
     * 判断是否是 Push 消息，判断是否需要做 connect 操作
     *
     * @param intent
     */
    private void isPushMessage(Intent intent) {

        if (intent == null || intent.getData() == null)
            return;

        // push
        if (intent.getData().getScheme().equals("rong") && intent.getData().getQueryParameter("isFromPush") != null) {

            // 通过intent.getData().getQueryParameter("push") 为true，判断是否是push消息
            if (intent.getData().getQueryParameter("isFromPush").equals("true")) {
                // 只有收到系统消息和不落地 push 消息的时候，pushId 不为 null。而且这两种消息只能通过 server
                // 来发送，客户端发送不了。
                String id = intent.getData().getQueryParameter("pushId");
                // RongIM.getInstance().getRongIMClient().recordNotificationEvent(userId);
                if (mDialog != null && !mDialog.isShowing()) {
                    mDialog.show();
                }
                isFromPush = true;
                enterActivity();
            } else if (RongIM.getInstance().getCurrentConnectionStatus()
                    .equals(RongIMClient.ConnectionStatusListener.ConnectionStatus.DISCONNECTED)) {
                if (mDialog != null && !mDialog.isShowing()) {
                    mDialog.show();
                }
                enterActivity();
            } else {
                enterFragment(mConversationType, mTargetId);
            }

        } else {
            if (RongIM.getInstance().getCurrentConnectionStatus()
                    .equals(RongIMClient.ConnectionStatusListener.ConnectionStatus.DISCONNECTED)) {
                if (mDialog != null && !mDialog.isShowing()) {
                    mDialog.show();
                }
                enterActivity();
            } else {
                enterFragment(mConversationType, mTargetId);
            }
        }
    }

    /**
     * 收到 push 消息后，选择进入哪个 Activity 如果程序缓存未被清理，进入 MainActivity 程序缓存被清理，进入
     * LoginActivity，重新获取token
     * <p/>
     * 作用：由于在 manifest 中 intent-filter 是配置在 ConversationActivity
     * 下面，所以收到消息后点击notifacition 会跳转到 DemoActivity。 以跳到 MainActivity 为例： 在
     * ConversationActivity 收到消息后，选择进入 MainActivity，这样就把 MainActivity
     * 激活了，当你读完收到的消息点击 返回键 时，程序会退到 MainActivity 页面，而不是直接退回到 桌面。
     */
    private void enterActivity() {

        String token = (String) SharePreferenceUtils.getParam("TOKEN", "default");
        assert token != null;
        if (token.equals("default")) {
            Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(mContext.getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);
            finish();
        } else {
            reconnect(token);
        }
    }

    /**
     * 加载会话页面 ConversationFragment
     *
     * @param mConversationType
     * @param mTargetId
     */
    private void enterFragment(Conversation.ConversationType mConversationType, String mTargetId) {

        fragment = new ConversationFragment();

        Uri uri = Uri.parse("rong://" + GloableConfig.CURRENT_PKGNAME).buildUpon()
                .appendPath("conversation").appendPath(mConversationType.getName().toLowerCase())
                .appendQueryParameter("targetId", mTargetId).build();

        fragment.setUri(uri);
        fragment.setInputBoardListener(new InputView.IInputBoardListener() {
            @Override
            public void onBoardExpanded(int height) {
            }

            @Override
            public void onBoardCollapsed() {
            }
        });

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.rong_content, fragment);
        transaction.commitAllowingStateLoss();
    }

    /** 重连 */
    private void reconnect(String token) {

        RongIM.connect(token, new RongIMClient.ConnectCallback() {
            @Override
            public void onTokenIncorrect() {

                Log.e("fate", "---onTokenIncorrect--");
            }

            @Override
            public void onSuccess(String s) {
                Log.i("fate", "---onSuccess--" + s);

                if (mDialog != null) {
                    mDialog.dismiss();
                }
                MyAppContext.getInstance().setOtherListener();
                enterFragment(mConversationType, mTargetId);
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {
                Log.e("fate", "---onError--" + e);
                if (mDialog != null) {
                    mDialog.dismiss();
                }
                enterFragment(mConversationType, mTargetId);
            }
        });

    }

    /**
     * 聊天界面点击事件监听
     */
    private void addListener() {
        if (RongIM.getInstance() != null) {
            RongIM.getInstance().setConversationBehaviorListener(new RongIM.ConversationBehaviorListener() {
                @Override
                public boolean onUserPortraitClick(Context context, Conversation.ConversationType conversationType,
                        UserInfo userInfo) {
                    return false;
                }

                @Override
                public boolean onUserPortraitLongClick(Context context, Conversation.ConversationType conversationType,
                        UserInfo userInfo) {
                    return false;
                }

                @Override
                public boolean onMessageClick(Context context, View view, final Message message) {
                    if (message.getContent() instanceof RichContentMessage) {
                        if (((RichContentMessage) message.getContent()).getUrl().contains("group://")) {
                            // 匹配为群名片
                            new AlertDialog.Builder(context).setTitle(R.string.whether_addgroup)
                                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (!NetUtils.isConnected(mContext)) {
                                                CustomToast.showToast(mContext, getString(R.string.net_error), 400);
                                                return;
                                            }

                                            String url = ((RichContentMessage) message.getContent()).getUrl();
                                            String groupId = url.substring(8, url.length());
                                            if (GloableConfig.allGroupMap.containsKey(groupId)) {
                                                dialog.dismiss();
                                                CustomToast.showToast(mContext,
                                                        getString(R.string.you_has_in_this_group), 400);
                                                finish();
                                                RongUtil.startChat(mContext, Conversation.ConversationType.GROUP,
                                                        groupId, GloableConfig.allGroupMap.get(groupId).name);
                                            } else {
                                                ProgressUtil.showPregressDialog(mContext, R.layout.custom_progress);
                                                RongUtil.addGroup(mContext, groupId, handler, 1001);
                                                dialog.dismiss();
                                            }
                                        }
                                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                            return true;
                        }

                    } else if (message.getContent() instanceof LocationMessage) {
                        L.v("点击了地理位置");
                        Intent intent = new Intent(context, AMAPLocationActivity.class);
                        intent.putExtra("location", message.getContent());
                        intent.putExtra("showLocation", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                    return false;

                }

                @Override
                public boolean onMessageLinkClick(Context context, String s) {
                    return false;
                }

                @Override
                public boolean onMessageLongClick(Context context, View view, Message message) {
                    return false;
                }
            });
        }
    }

    private void setTypingStatusListener() {
        RongIMClient.setTypingStatusListener(new RongIMClient.TypingStatusListener() {
            @Override
            public void onTypingStatusChanged(Conversation.ConversationType type, String targetId,
                    Collection<TypingStatus> typingStatusSet) {
                // 当输入状态的会话类型和targetID与当前会话一致时，才需要显示
                if (type.equals(mConversationType) && targetId.equals(mTargetId)) {
                    int count = typingStatusSet.size();
                    // count表示当前会话中正在输入的用户数量，目前只支持单聊，所以判断大于0就可以给予显示了
                    if (count > 0) {
                        Iterator iterator = typingStatusSet.iterator();
                        TypingStatus status = (TypingStatus) iterator.next();
                        String objectName = status.getTypingContentType();

                        MessageTag textTag = TextMessage.class.getAnnotation(MessageTag.class);
                        MessageTag voiceTag = VoiceMessage.class.getAnnotation(MessageTag.class);
                        // 匹配对方正在输入的是文本消息还是语音消息
                        if (objectName.equals(textTag.value())) {
                            handler.sendEmptyMessage(SET_TEXT_TYPING_TITLE);
                        } else if (objectName.equals(voiceTag.value())) {
                            handler.sendEmptyMessage(SET_VOICE_TYPING_TITLE);
                        }
                    } else {// 当前会话没有用户正在输入，标题栏仍显示原来标题
                        handler.sendEmptyMessage(SET_TARGETID_TITLE);
                    }
                }
            }
        });
    }

    /**
     * android 6.0 以上版本，监听SDK权限请求，弹出对应请求框。 if (Build.VERSION.SDK_INT >= 23) {
     */
    private void getPermissions() {
        RongIM.getInstance().setRequestPermissionListener(new RongIM.RequestPermissionsListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onPermissionRequest(String[] permissions, final int requestCode) {
                for (final String permission : permissions) {
                    if (shouldShowRequestPermissionRationale(permission)) {
                        requestPermissions(new String[] { permission }, requestCode);
                    } else {
                        int isPermissionGranted = checkSelfPermission(permission);
                        if (isPermissionGranted != PackageManager.PERMISSION_GRANTED) {
                            new AlertDialog.Builder(ConversationActivity.this)
                                    .setMessage(getString(R.string.requestPermission) + permission)
                                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            requestPermissions(new String[] { permission }, requestCode);
                                        }
                                    }).setNegativeButton(R.string.cancel, null).create().show();
                        }
                        return;
                    }
                }
            }
        });
    }

    /**
     * 处理加群之后返回的数据
     * 
     * @param resultString
     */
    private void dealWithAddGroup(String resultString) {
        try {

            if (resultString.contains("不存在该群组")) {
                CustomToast.showToast(mContext, getString(R.string.group_has_destory), 400);
            } else {
                JSONObject jsonObject = new JSONObject(resultString);
                if (jsonObject.getString("code").equals("200")) {

                    CustomToast.showToast(mContext, getString(R.string.addgroup_success), 400);
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
                    finish();
                    RongUtil.startChat(mContext, Conversation.ConversationType.GROUP, groupInfoBean.id,
                            groupInfoBean.name);
                } else if (jsonObject.getString("code").equals("500")) {
                    CustomToast.showToast(mContext, getString(R.string.group_has_destory), 400);
                } else {
                    CustomToast.showToast(mContext, getString(R.string.addgroup_failed), 400);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        ProgressUtil.dismissProgressDialog();
    }

    /**
     * 处理被拉进群聊返回的数据
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
        case 3001:// 群组改名
            String name = data.getStringExtra("newName");
            titleTextView.setText(name);
            break;
        case 3002:
            finish();
            break;
        case 3003:
            finish();
            break;
        default:
            break;

        }
    }

    @Override
    public boolean onReceived(Message message, int i) {
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
                RongIM.getInstance().getRongIMClient().clearMessages(Conversation.ConversationType.GROUP, id);
                RongIM.getInstance().getRongIMClient().removeConversation(Conversation.ConversationType.GROUP, id);
            } else if (info.contains(getString(R.string.exit))) {

            } else if (info.contains(getString(R.string.remove))) {

            } else if (info.contains(getString(R.string.invitation))) {
                if (GloableConfig.allGroupMap.containsKey(message.getTargetId())) {
                    // 如果用户被拉进了新的群组,获取这个群组的信息，填充到列表里去
                    VolleyUtil volleyUtil = new VolleyUtil(mContext);
                    String url = String.format(GloableConfig.RongCloud.getGroupInfoUrl, id);
                    volleyUtil.stringRequest(handler, Request.Method.POST, url, 1002);
                }
            } else if (info.contains(getString(R.string.start_group_chat))) {
                VolleyUtil volleyUtil = new VolleyUtil(mContext);
                String url = String.format(GloableConfig.RongCloud.getGroupInfoUrl, id);
                volleyUtil.stringRequest(handler, Request.Method.POST, url, 1002);
            }

        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            Intent intent = new Intent(mContext,PortalMainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



}
