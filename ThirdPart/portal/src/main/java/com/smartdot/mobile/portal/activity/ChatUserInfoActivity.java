package com.smartdot.mobile.portal.activity;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.abconstant.GlobleAddressConfig;
import com.smartdot.mobile.portal.bean.UserInfoBean;
import com.smartdot.mobile.portal.utils.RongUtil;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

/**
 * 聊天界面查询聊天信息会跳到此界面 显示聊天内容或加人使用
 */
public class ChatUserInfoActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;

    private ImageView title_left_img;

    private TextView title_center_text;

    private ImageView title_right_img;

    private ImageView userinfo_head_img;

    private TextView userinfo_name_tv;

    private ImageView userinfo_add_img;

    private ToggleButton ontop_tb;

    private ToggleButton nodisturb_tb;

    private ToggleButton fav_contact_tb;

    private RelativeLayout chat_file_rl;

    private RelativeLayout clear_chat_data_rl;

    private Conversation.ConversationType conversationType;

    private String targetId;

    private UserInfoBean userInfoBean;

    /** 是否置顶 */
    private Boolean isTop = false;

    /** 是否提醒的value*/
    int notifationValue;

    /** 是否消息提醒 */
    private Boolean isNotifation = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_user_info);
        mContext = this;
        initData();
        initView();
    }

    private void initData() {
        Intent intent = getIntent();
        conversationType = (Conversation.ConversationType) intent.getSerializableExtra("conversationType");
        targetId = intent.getStringExtra("targetId");
        userInfoBean = GloableConfig.allUserMap.get(targetId);
        try {
            RongIM.getInstance().getConversation(conversationType, targetId, new RongIMClient.ResultCallback<Conversation>() {
                @Override
                public void onSuccess(Conversation conversation) {
                    isTop = conversation.isTop();
                    ontop_tb.setChecked(isTop);
                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {

                }
            });
            RongIM.getInstance().getConversation(conversationType, targetId, new RongIMClient.ResultCallback<Conversation>() {
                @Override
                public void onSuccess(Conversation conversation) {
                    notifationValue = conversation.getNotificationStatus().getValue();
                    nodisturb_tb.setChecked(notifationValue == 0 ? true : false);
                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {

                }
            });
            if (notifationValue == 0){
                isNotifation = false;
            }else{
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
        userinfo_head_img = (ImageView) findViewById(R.id.userinfo_head_img);
        userinfo_name_tv = (TextView) findViewById(R.id.userinfo_name_tv);
        userinfo_add_img = (ImageView) findViewById(R.id.userinfo_add_img);
        ontop_tb = (ToggleButton) findViewById(R.id.ontop_tb);
        nodisturb_tb = (ToggleButton) findViewById(R.id.nodisturb_tb);
        fav_contact_tb = (ToggleButton) findViewById(R.id.fav_contact_tb);
        chat_file_rl = (RelativeLayout) findViewById(R.id.chat_file_rl);
        clear_chat_data_rl = (RelativeLayout) findViewById(R.id.clear_chat_data_rl);

        title_center_text.setText(R.string.privateChatInfo);
        userinfo_name_tv.setText(userInfoBean.userName);
        title_right_img.setVisibility(View.GONE);

        title_left_img.setOnClickListener(this);
        userinfo_add_img.setOnClickListener(this);
        ontop_tb.setOnClickListener(this);
        nodisturb_tb.setOnClickListener(this);
        fav_contact_tb.setOnClickListener(this);
        chat_file_rl.setOnClickListener(this);
        clear_chat_data_rl.setOnClickListener(this);

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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.title_left_img){
            finish();
        }else if(v.getId() == R.id.userinfo_add_img){
            // TODO: 2016/7/13 拉人加入聊天
            GloableConfig.addressBookType = 2;
            Intent intent = new Intent(mContext, OrganizationActivity.class);
            GlobleAddressConfig.selectedPersonIDs.put(targetId,GloableConfig.allUserMap.get(targetId).obey_dept_id);
            GloableConfig.addressBookType = 2;
            startActivity(intent);
        }else if(v.getId() == R.id.chat_file_rl){
            // TODO: 2016/7/13 查询聊天文件
        }else if(v.getId() == R.id.clear_chat_data_rl){
            clearData();
        }
    }

    /**
     * 设置是否置顶
     * 
     * @param onTop
     */
    private void setOnTop(Boolean onTop) {
        if (onTop) {
            //保存会话置顶状态

            RongIM.getInstance().setConversationToTop(conversationType, targetId, true,
                    new RongIMClient.ResultCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean aBoolean) {
//                            Toast.makeText(mContext, R.string.onTop, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {
                            Toast.makeText(mContext, R.string.operation_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            //保存会话置顶状态
            RongIM.getInstance().setConversationToTop(conversationType, targetId, false,
                    new RongIMClient.ResultCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean aBoolean) {
//                            Toast.makeText(mContext, R.string.onTop_cancel, Toast.LENGTH_SHORT).show();
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
            //保存会话提醒状态
            RongIM.getInstance().setConversationNotificationStatus(conversationType, targetId,
                    Conversation.ConversationNotificationStatus.DO_NOT_DISTURB,
                    new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {
                        @Override
                        public void onSuccess(
                                Conversation.ConversationNotificationStatus conversationNotificationStatus) {
//                            Toast.makeText(mContext, R.string.ignore_remind, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {
                            Toast.makeText(mContext, R.string.operation_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            //保存会话提醒状态
            RongIM.getInstance().setConversationNotificationStatus(conversationType, targetId,
                    Conversation.ConversationNotificationStatus.NOTIFY,
                    new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {
                        @Override
                        public void onSuccess(
                                Conversation.ConversationNotificationStatus conversationNotificationStatus) {
//                            Toast.makeText(mContext, R.string.ignore_remind_cancel, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {
                            Toast.makeText(mContext, R.string.operation_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
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
}
