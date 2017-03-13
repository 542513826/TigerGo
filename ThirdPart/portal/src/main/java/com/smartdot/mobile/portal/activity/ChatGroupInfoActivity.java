package com.smartdot.mobile.portal.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.Request;
import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.bean.GroupInfoBean;
import com.smartdot.mobile.portal.utils.CommonUtil;
import com.smartdot.mobile.portal.utils.CustomToast;
import com.smartdot.mobile.portal.utils.NetUtils;
import com.smartdot.mobile.portal.utils.ProgressUtil;
import com.smartdot.mobile.portal.utils.RongUtil;
import com.smartdot.mobile.portal.utils.StringUtils;
import com.smartdot.mobile.portal.utils.VolleyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;

/**
 * 群信息的界面
 */
public class ChatGroupInfoActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;

    private ImageView title_left_img;

    private TextView title_center_text;

    private ImageView title_right_img;

    private ImageView head_img;

    private ImageView group_name_arrow;

    private TextView group_name_tv;

    private ImageView group_Qrcode_arrow;

    private ImageView group_Qrcode_img;

    private RelativeLayout share_group_card;

    private ImageView group_members_arrow;

    private TextView group_members_tv;

    private ImageView group_members_img_1;

    private TextView group_members_name_1;

    private ImageView group_members_img_2;

    private TextView group_members_name_2;

    private ImageView group_members_img_3;

    private TextView group_members_name_3;

    private ImageView group_members_img_4;

    private TextView group_members_name_4;

    private ImageView group_members_img_5;

    private TextView group_members_name_5;

    private RelativeLayout group_members_rl_1;
    private RelativeLayout group_members_rl_2;
    private RelativeLayout group_members_rl_3;
    private RelativeLayout group_members_rl_4;
    private RelativeLayout group_members_rl_5;

    private RelativeLayout group_members_rl;

    private ToggleButton ontop_tb;

    private ToggleButton nodisturb_tb;

    private ToggleButton fav_contact_tb;

    private RelativeLayout transfer_group_rl;

    private RelativeLayout chat_file_rl;

    private RelativeLayout clear_chat_data_rl;

    private Button group_exit_btn;

    private Conversation.ConversationType conversationType;

    private String targetId;

    private GroupInfoBean groupInfoBean;

    /**
     * 是否是群主
     */
    private boolean isManager = false;

    private String GroupName;

    /**
     * 是否置顶
     */
    private Boolean isTop;

    /**
     * 是否消息提醒
     */
    private Boolean isNotifation = true;

    /** 新的群主id */
    String new_host_id = "";

    /** 新的群主姓名 */
    String new_host_name = "";

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case 1001:// 请求群组信息
                dealWithGroupInfoResult(msg.obj.toString());
                break;
            case 1002:// 解散群组
                dealWithDestoryResult(msg.obj.toString());
                break;
            case 1003:// 退出群组
                dealWithExitResult(msg.obj.toString());
                break;
            case 1004:// 修改群名称
                dealWithChangeNameResult(msg.obj.toString());
                break;
            case 1005:// 转让群主
                dealWithChangeHostResult(msg.obj.toString());
                break;
            default:
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_group_info);
        mContext = this;
        initData();
        initView();
        getData();
    }

    private void initData() {
        Intent intent = getIntent();
        conversationType = (Conversation.ConversationType) intent.getSerializableExtra("conversationType");
        targetId = intent.getStringExtra("targetId");
        try {
            isTop = RongIM.getInstance().getRongIMClient().getConversation(conversationType, targetId).isTop();
            int notifationValue = RongIM.getInstance().getRongIMClient().getConversation(conversationType, targetId)
                    .getNotificationStatus().getValue();
            if (notifationValue == 0) {
                isNotifation = false;
            } else {
                isNotifation = true;
            }
        } catch (NullPointerException e) {
            isTop = false;
            isNotifation = true;
        }

    }

    private void initView() {
        title_left_img = (ImageView) findViewById(R.id.title_left_img);
        title_center_text = (TextView) findViewById(R.id.title_center_text);
        title_right_img = (ImageView) findViewById(R.id.title_right_img);
        head_img = (ImageView) findViewById(R.id.head_img);
        group_name_arrow = (ImageView) findViewById(R.id.group_name_arrow);
        group_name_tv = (TextView) findViewById(R.id.group_name_tv);
        group_Qrcode_arrow = (ImageView) findViewById(R.id.group_Qrcode_arrow);
        group_Qrcode_img = (ImageView) findViewById(R.id.group_Qrcode_img);
        share_group_card = (RelativeLayout) findViewById(R.id.share_group_card);
        group_members_arrow = (ImageView) findViewById(R.id.group_members_arrow);
        group_members_tv = (TextView) findViewById(R.id.group_members_tv);
        group_members_img_1 = (ImageView) findViewById(R.id.group_members_img_1);
        group_members_name_1 = (TextView) findViewById(R.id.group_members_name_1);
        group_members_img_2 = (ImageView) findViewById(R.id.group_members_img_2);
        group_members_name_2 = (TextView) findViewById(R.id.group_members_name_2);
        group_members_img_3 = (ImageView) findViewById(R.id.group_members_img_3);
        group_members_name_3 = (TextView) findViewById(R.id.group_members_name_3);
        group_members_img_4 = (ImageView) findViewById(R.id.group_members_img_4);
        group_members_name_4 = (TextView) findViewById(R.id.group_members_name_4);
        group_members_img_5 = (ImageView) findViewById(R.id.group_members_img_5);
        group_members_name_5 = (TextView) findViewById(R.id.group_members_name_5);
        group_members_rl_1 = (RelativeLayout) findViewById(R.id.group_members_rl_1);
        group_members_rl_2 = (RelativeLayout) findViewById(R.id.group_members_rl_2);
        group_members_rl_3 = (RelativeLayout) findViewById(R.id.group_members_rl_3);
        group_members_rl_4 = (RelativeLayout) findViewById(R.id.group_members_rl_4);
        group_members_rl_5 = (RelativeLayout) findViewById(R.id.group_members_rl_5);
        group_members_rl = (RelativeLayout) findViewById(R.id.group_members_rl);
        ontop_tb = (ToggleButton) findViewById(R.id.ontop_tb);
        nodisturb_tb = (ToggleButton) findViewById(R.id.nodisturb_tb);
        fav_contact_tb = (ToggleButton) findViewById(R.id.fav_contact_tb);
        transfer_group_rl = (RelativeLayout) findViewById(R.id.transfer_group_rl);
        chat_file_rl = (RelativeLayout) findViewById(R.id.chat_file_rl);
        clear_chat_data_rl = (RelativeLayout) findViewById(R.id.clear_chat_data_rl);
        group_exit_btn = (Button) findViewById(R.id.group_exit_btn);

        title_left_img.setVisibility(View.VISIBLE);
        title_center_text.setText(R.string.group_setting);
        title_right_img.setVisibility(View.INVISIBLE);

        title_left_img.setOnClickListener(this);
        group_name_arrow.setOnClickListener(this);
        group_name_tv.setOnClickListener(this);
        group_Qrcode_arrow.setOnClickListener(this);
        group_Qrcode_img.setOnClickListener(this);
        share_group_card.setOnClickListener(this);
        group_members_rl.setOnClickListener(this);
        ontop_tb.setOnClickListener(this);
        nodisturb_tb.setOnClickListener(this);
        fav_contact_tb.setOnClickListener(this);
        transfer_group_rl.setOnClickListener(this);
        chat_file_rl.setOnClickListener(this);
        clear_chat_data_rl.setOnClickListener(this);
        group_exit_btn.setOnClickListener(this);
        group_members_img_5.setOnClickListener(this);
        group_members_img_4.setOnClickListener(this);

        ontop_tb.setChecked(isTop);
        nodisturb_tb.setChecked(!isNotifation);

        ontop_tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setOnTop(isChecked);
            }
        });

        nodisturb_tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setNotificationStatus(isChecked);

            }
        });

        fav_contact_tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // TODO: 2016/7/13 设为常用联系人
                } else {
                    // TODO: 2016/7/13 取消设为常用联系人
                }
            }
        });
    }

    /**
     * 获取群组信息
     */
    private void getData() {
        if (!NetUtils.isConnected(mContext)) {
            CustomToast.showToast(mContext, getString(R.string.net_error), 400);
            return;
        }
        ProgressUtil.showPregressDialog(this, R.layout.custom_progress);
        VolleyUtil volleyUtil = new VolleyUtil(mContext);
        String url = String.format(GloableConfig.RongCloud.getGroupInfoUrl, targetId);
        volleyUtil.stringRequest(handler, Request.Method.POST, url, 1001);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.title_left_img) {
            Intent intent = new Intent();
            intent.putExtra("newName", group_name_tv.getText().toString());
            setResult(3001, intent);
            finish();
        } else if (v.getId() == R.id.group_name_arrow || v.getId() == R.id.group_name_tv) {
            // 修改群名称
            if (groupInfoBean.host_user_id.equals(GloableConfig.myUserInfo.userId)){
                changeName();
            }
        } else if (v.getId() == R.id.group_Qrcode_arrow || v.getId() == R.id.group_Qrcode_img) {
            // 显示二维码
            Intent QRintent = new Intent(mContext, QrActivity.class);
            QRintent.putExtra("targetId", targetId);
            startActivity(QRintent);
        } else if (v.getId() == R.id.share_group_card) {
            // TODO: 2016/7/13 分享群名片
            GloableConfig.addressBookType = 4;
            Intent intent = new Intent(mContext, OrganizationActivity.class);
            GloableConfig.cardGroup = new GroupInfoBean();
            GloableConfig.cardGroup.id = groupInfoBean.id;
            GloableConfig.cardGroup.name = groupInfoBean.name;
            startActivity(intent);
        } else if (v.getId() == R.id.group_members_rl) {
            // 显示群成员
            Intent GroupMemberIntent = new Intent(mContext, GroupMembersActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("groupMembers", (Serializable) groupInfoBean.memberList);
            bundle.putString("groupId", groupInfoBean.id);
            bundle.putBoolean("changeHost", false);
            bundle.putBoolean("isManager", isManager);
            GroupMemberIntent.putExtra("bundle", bundle);
            startActivityForResult(GroupMemberIntent, 2003);
        } else if (v.getId() == R.id.transfer_group_rl) {
            // 转让群主
            Intent GroupMemberIntent = new Intent(mContext, GroupMembersActivity.class);
            Bundle bundle = new Bundle();
            bundle.putBoolean("changeHost", true);
            bundle.putSerializable("groupMembers", (Serializable) groupInfoBean.memberList);
            bundle.putString("groupId", groupInfoBean.id);
            bundle.putBoolean("changeHost", true);
            bundle.putBoolean("isManager", isManager);
            GroupMemberIntent.putExtra("bundle", bundle);
            startActivityForResult(GroupMemberIntent, 2002);
        } else if (v.getId() == R.id.chat_file_rl) {
            // TODO: 2016/7/13 聊天文件
        } else if (v.getId() == R.id.clear_chat_data_rl) {
            // 删除聊天记录
            clearData();
        } else if (v.getId() == R.id.group_exit_btn) {
            if (isManager) {
                destoryGroup();
            } else {
                quitGroup();
            }
        } else if (v.getId() == R.id.group_members_img_5) {
            // 加人进群
            // groupInfoBean.memberList; 群成员
            // depart_ids_list; //群里的部门id
            Intent intent = new Intent(mContext, OrganizationActivity.class);
            Bundle bundle = new Bundle();
            System.out.println("groupInfoBean.id-------------------" + groupInfoBean.id);
            bundle.putSerializable("groupInfoBean", groupInfoBean);
            bundle.putString("groupId", groupInfoBean.id);
            bundle.putBoolean("isMultiSelect", true);
            bundle.putSerializable("memberList", (Serializable) groupInfoBean.memberList);
            bundle.putSerializable("depart_ids_list", (Serializable) groupInfoBean.depart_ids_list);
            intent.putExtra("bundle", bundle);
            GloableConfig.addressBookType = 1;
            startActivityForResult(intent, 2001);
        } else if (v.getId() == R.id.group_members_img_4) {
            // TODO: 2016/8/4 跳到删除人员界面
            if (groupInfoBean.host_user_id.equals(GloableConfig.myUserInfo.userId)){
                Intent intent = new Intent(mContext, DeleteMembersActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("groupId",groupInfoBean.id);
                bundle.putSerializable("memberList", (Serializable) groupInfoBean.memberList);
                intent.putExtra("bundle", bundle);
                startActivityForResult(intent, 2004);
            }else {
                CustomToast.showToast(mContext,"您没有权限移除群成员",400);
            }

        }
    }

    /**
     * 解散群组
     */
    private void destoryGroup() {
        new AlertDialog.Builder(mContext).setTitle(R.string.confirm_dismiss_group)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!NetUtils.isConnected(mContext)) {
                            CustomToast.showToast(mContext, getString(R.string.net_error), 400);
                            return;
                        }
                        ProgressUtil.showPregressDialog(mContext, R.layout.custom_progress);
                        RongUtil.sendInfoMessage(mContext, groupInfoBean.id, Conversation.ConversationType.GROUP,
                                GloableConfig.myUserInfo.userName + getString(R.string.destory_group));
                        VolleyUtil volleyUtil = new VolleyUtil(mContext);
                        String url = String.format(GloableConfig.RongCloud.destroyGroupUrl,
                                GloableConfig.myUserInfo.userId, groupInfoBean.id);
                        volleyUtil.stringRequest(handler, Request.Method.POST, url, 1002);
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();

    }

    /**
     * 退出群组
     */
    private void quitGroup() {
        new AlertDialog.Builder(mContext).setTitle(R.string.confirm_quit_group)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!NetUtils.isConnected(mContext)) {
                            CustomToast.showToast(mContext, getString(R.string.net_error), 400);
                            return;
                        }
                        ProgressUtil.showPregressDialog(mContext, R.layout.custom_progress);
                        RongUtil.sendInfoMessage(mContext, groupInfoBean.id, Conversation.ConversationType.GROUP,
                                GloableConfig.myUserInfo.userName + R.string.exit_group);
                        VolleyUtil volleyUtil = new VolleyUtil(mContext);
                        String url = String.format(GloableConfig.RongCloud.quitGroupUrl,
                                GloableConfig.myUserInfo.userId, groupInfoBean.id, GloableConfig.myUserInfo.userId);
                        volleyUtil.stringRequest(handler, Request.Method.POST, url, 1003);
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();

    }

    /**
     * 修改群名
     */
    private void changeName() {
        final EditText editText = new EditText(mContext);
        editText.setId(R.id.alertdialog_edit);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
        new AlertDialog.Builder(mContext).setTitle(R.string.input_new_groupname).setView(editText)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = editText.getText().toString().trim();
                        if (name.equals(groupInfoBean.name)) {
                            CustomToast.showToast(mContext, getString(R.string.new_old_groupname_cannot_same));
                            return;
                        } else if (!StringUtils.isUserName(name)) {
                            CustomToast.showToast(mContext, getString(R.string.groupname_cannot_contain_sp));
                            return;
                        } else {
                            // 修改群名称
                            GroupName = name;
                            if (!NetUtils.isConnected(mContext)) {
                                CustomToast.showToast(mContext, getString(R.string.net_error), 400);
                                return;
                            }
                            ProgressUtil.showPregressDialog(mContext, R.layout.custom_progress);
                            VolleyUtil volleyUtil = new VolleyUtil(mContext);
                            String url = String.format(GloableConfig.RongCloud.changeGroupNameUrl, groupInfoBean.id,
                                    name);
                            volleyUtil.stringRequest(handler, Request.Method.POST, url, 1004);

                        }
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
    }

    /**
     * 设置是否置顶
     *
     * @param onTop
     */
    private void setOnTop(Boolean onTop) {
        if (onTop) {
            // 保存会话置顶状态
            RongIM.getInstance().setConversationToTop(conversationType, targetId, true,
                    new RongIMClient.ResultCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean aBoolean) {
                            Toast.makeText(mContext, R.string.onTop, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {
                            Toast.makeText(mContext, R.string.operation_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // 保存会话置顶状态
            RongIM.getInstance().setConversationToTop(conversationType, targetId, false,
                    new RongIMClient.ResultCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean aBoolean) {
                            Toast.makeText(mContext, R.string.onTop_cancel, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {
                            Toast.makeText(mContext, R.string.operation_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    /**
     * 设置会话是否提醒
     *
     * @param noNotification
     */
    private void setNotificationStatus(Boolean noNotification) {
        if (noNotification) {
            // 保存会话提醒状态
            RongIM.getInstance().setConversationNotificationStatus(conversationType, targetId,
                    Conversation.ConversationNotificationStatus.DO_NOT_DISTURB,
                    new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {
                        @Override
                        public void onSuccess(
                                Conversation.ConversationNotificationStatus conversationNotificationStatus) {
                            Toast.makeText(mContext, R.string.ignore_remind, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {
                            Toast.makeText(mContext, R.string.operation_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // 保存会话提醒状态
            RongIM.getInstance().setConversationNotificationStatus(conversationType, targetId,
                    Conversation.ConversationNotificationStatus.NOTIFY,
                    new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {
                        @Override
                        public void onSuccess(
                                Conversation.ConversationNotificationStatus conversationNotificationStatus) {
                            Toast.makeText(mContext, R.string.ignore_remind_cancel, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {
                            Toast.makeText(mContext, R.string.operation_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    /**
     * 转让群主
     */
    private void changerManager(String new_host_id) {
        if (!NetUtils.isConnected(mContext)) {
            CustomToast.showToast(mContext, getString(R.string.net_error), 400);
            return;
        }
        ProgressUtil.showPregressDialog(mContext, R.layout.custom_progress);
        VolleyUtil volleyUtil = new VolleyUtil(mContext);
        String url = String.format(GloableConfig.RongCloud.changeGroupInfoUrl, groupInfoBean.id, groupInfoBean.name,
                groupInfoBean.host_user_id, new_host_id);
        volleyUtil.stringRequest(handler, Request.Method.POST, url, 1005);
    }

    /**
     * 删除聊天记录
     */
    private void clearData() {
        new AlertDialog.Builder(mContext).setMessage(R.string.whether_delete_record).setTitle(R.string.cannotRecover)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 清除聊天列表里的信息
                        RongUtil.removeConversationListItem(targetId);
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
            case 2001:
                getData();
                break;
            case 2002:
                try {
                    new_host_id = data.getStringExtra("new_host_id");
                    new_host_name = data.getStringExtra("new_host_name");
                    if (!StringUtils.isNull(new_host_id) && !new_host_name.equals("0")) {
                        changerManager(new_host_id);
                    }

                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
                break;
            case 2003:
                getData();
                break;
            case 2004:
                getData();
                break;
            default:
                break;
            }

        }
    }

    /***
     * 先清除聊天记录，再清除聊天列表里的信息
     */
    private void deleteChatList(final Conversation.ConversationType conversationType, final String targetId){
        // 清除聊天列表里的信息
        RongUtil.removeConversationListItem(targetId);
    }

    /**
     * 处理请求群组信息后返回的数据
     * @param resultString
     */
    private void dealWithGroupInfoResult(String resultString){
        if (resultString.contains(getString(R.string.system_error))) {
            CustomToast.showToast(mContext, getString(R.string.group_has_destory), 400);
            finish();
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(resultString);
            if (jsonObject.getString("code").equals("200")) {

                groupInfoBean = CommonUtil.gson.fromJson(jsonObject.getString("result"), GroupInfoBean.class);
                groupInfoBean.setDepart_ids_list(groupInfoBean.depart_ids);
                group_name_tv.setText(groupInfoBean.name);
                group_members_tv.setText(groupInfoBean.number + getString(R.string.person));
                group_members_name_1.setText("");
                group_members_name_2.setText("");
                group_members_name_3.setText("");
                group_members_rl_1.setVisibility(View.GONE);
                group_members_rl_2.setVisibility(View.GONE);
                group_members_rl_3.setVisibility(View.GONE);
                group_members_rl_4.setVisibility(View.GONE);
                for (int i = 0; i < groupInfoBean.memberList.size(); i++) {
                    if (i == 0) {
                        group_members_name_1.setText(
                                GloableConfig.allUserMap.get(groupInfoBean.memberList.get(0).userId).userName);
                        group_members_img_1.setVisibility(View.VISIBLE);
                        group_members_rl_1.setVisibility(View.VISIBLE);
                    } else if (i == 1) {
                        group_members_name_2.setText(
                                GloableConfig.allUserMap.get(groupInfoBean.memberList.get(1).userId).userName);
                        group_members_img_2.setVisibility(View.VISIBLE);
                        group_members_rl_2.setVisibility(View.VISIBLE);
                    } else if (i == 2) {
                        group_members_name_3.setText(
                                GloableConfig.allUserMap.get(groupInfoBean.memberList.get(2).userId).userName);
                        group_members_img_3.setVisibility(View.VISIBLE);
                        group_members_rl_3.setVisibility(View.VISIBLE);
                    }
                }
                if (groupInfoBean.host_user_id.equals(GloableConfig.myUserInfo.userId)) {
                    transfer_group_rl.setVisibility(View.VISIBLE);
                    group_exit_btn.setText(R.string.destroy_group);
                    group_members_rl_4.setVisibility(View.VISIBLE);
                    isManager = true;
                } else {
                    transfer_group_rl.setVisibility(View.GONE);
                    group_members_rl_4.setVisibility(View.GONE);
                    group_exit_btn.setText(R.string.exit_group);
                    isManager = false;
                }

            } else if (jsonObject.getString("code").equals("500")) {
                deleteChatList(conversationType,targetId);
                CustomToast.showToast(mContext, getString(R.string.you_are_not_group_member), 400);
                finish();
            } else {
                CustomToast.showToast(mContext, getString(R.string.getdata_failed), 400);
                finish();
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ProgressUtil.dismissProgressDialog();
    }

    /**
     * 处理解散群组后返回的数据
     */
    private void dealWithDestoryResult(String resultString){
        try {
            JSONObject jsonObject = new JSONObject(resultString);
            if (jsonObject.getString("code").equals("200")) {
                CustomToast.showToast(mContext, R.string.dismiss_groups, 400);
                setResult(3003);
//                        RongIM.getInstance().getRongIMClient().clearMessages(conversationType, targetId);
                // 清除聊天列表里的信息
                RongUtil.removeConversationListItem(groupInfoBean.id);
                finish();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ProgressUtil.dismissProgressDialog();
    }

    /**
     * 处理退出群组后返回的数据
     * @param resultString
     */
    private void dealWithExitResult(String resultString){
        try {
            JSONObject jsonObject = new JSONObject(resultString);
            if (jsonObject.getString("code").equals("200")) {
                CustomToast.showToast(mContext, R.string.quit_groups, 400);
                setResult(3002);
                // 清除聊天列表里的信息
                RongUtil.removeConversationListItem(groupInfoBean.id);
                finish();
            } else if (jsonObject.getString("code").equals("500")) {
                deleteChatList(conversationType,groupInfoBean.id);
                CustomToast.showToast(mContext, getString(R.string.you_are_not_group_member), 400);
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ProgressUtil.dismissProgressDialog();
    }

    /**
     * 处理改群名后返回的数据
     * @param resultString
     */
    private void dealWithChangeNameResult(String resultString){
        try {
            JSONObject jsonObject = new JSONObject(resultString);
            if (jsonObject.getString("code").equals("200")) {
                CustomToast.showToast(mContext, getString(R.string.change_groupname_success), 400);
                group_name_tv.setText(GroupName);
                groupInfoBean.name = GroupName;
                Group group = new Group(groupInfoBean.id, GroupName, null);
                RongIM.getInstance().refreshGroupInfoCache(group);
                RongUtil.sendInfoMessage(mContext, groupInfoBean.id, Conversation.ConversationType.GROUP,
                        getString(R.string.group_name_change_to) + GroupName);
            } else if (jsonObject.getString("code").equals("500")) {
                deleteChatList(conversationType,targetId);
                CustomToast.showToast(mContext, getString(R.string.you_are_not_group_member), 400);
                finish();
            } else {
                CustomToast.showToast(mContext, getString(R.string.getdata_failed), 400);
                finish();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ProgressUtil.dismissProgressDialog();
    }

    /**
     * 处理修改群主后返回的数据
     * @param resultString
     */
    private void dealWithChangeHostResult(String resultString){
        try {
            JSONObject jsonObject = new JSONObject(resultString);
            if (jsonObject.getString("code").equals("200")) {
                CustomToast.showToast(mContext, getString(R.string.group_host_change_to) + new_host_name, 400);
                RongUtil.sendInfoMessage(mContext, groupInfoBean.id, Conversation.ConversationType.GROUP,
                        getString(R.string.group_host_change_to) + new_host_name);
                isManager = false;
                group_members_rl_4.setVisibility(View.GONE);
                transfer_group_rl.setVisibility(View.GONE);
                group_exit_btn.setText(R.string.exit_group);
            } else if (jsonObject.getString("code").equals("500")) {
                deleteChatList(conversationType,targetId);
                CustomToast.showToast(mContext, getString(R.string.you_are_not_group_member), 400);
                finish();
            } else {
                CustomToast.showToast(mContext,getString(R.string.getdata_failed), 400);
                finish();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ProgressUtil.dismissProgressDialog();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Intent intent = new Intent();
            intent.putExtra("newName", group_name_tv.getText().toString());
            setResult(3001, intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
