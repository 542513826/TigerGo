package com.smartdot.mobile.portal.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.bean.UserInfoBean;
import com.smartdot.mobile.portal.utils.CommonUtil;
import com.smartdot.mobile.portal.utils.CommonUtils;
import com.smartdot.mobile.portal.utils.ProgressUtil;
import com.smartdot.mobile.portal.utils.RongUtil;
import com.smartdot.mobile.portal.utils.VolleyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.rong.imlib.model.Conversation;

/**
 * 通讯录个人详情
 */
public class AddressBookUserInfoActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;

    private TextView signature;

    private TextView myfragment_signature;

    private TextView duty;

    private TextView duty_content;

    private TextView obey;

    private TextView obey_content;

    private TextView phone;

    private TextView phone_content;

    private ImageView message_iv;

    private ImageView mobil_iv;

    private TextView tel;

    private TextView tel_content;

    private ImageView tel_iv;

    private TextView mail;

    private TextView mail_content;

    private ImageView iv_adderss_back;

    private ImageView iv_adderss_collect;

    private ImageView iv_adderss_user_pic;

    private TextView tv_address_name;

    private LinearLayout sendMessagesBtn;

    private LinearLayout saveTelBtn;

    private String userId;

    UserInfoBean userInfoBean;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1001) {
                try {
                    JSONObject jsonObject = new JSONObject(msg.obj.toString());
                    userInfoBean = CommonUtil.gson.fromJson(jsonObject.getString("user"), UserInfoBean.class);
                    tv_address_name.setText(userInfoBean.userName);
                    mail_content.setText(userInfoBean.email);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ProgressUtil.dismissProgressDialog();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_user_info);
        mContext = this;
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        initView();
        getData();
    }

    private void initView() {
        signature = (TextView) findViewById(R.id.signature);
        myfragment_signature = (TextView) findViewById(R.id.myfragment_signature);
        duty = (TextView) findViewById(R.id.duty);
        duty_content = (TextView) findViewById(R.id.duty_content);
        obey = (TextView) findViewById(R.id.obey);
        obey_content = (TextView) findViewById(R.id.obey_content);
        phone = (TextView) findViewById(R.id.phone);
        phone_content = (TextView) findViewById(R.id.phone_content);
        message_iv = (ImageView) findViewById(R.id.message_iv);
        mobil_iv = (ImageView) findViewById(R.id.mobil_iv);
        tel = (TextView) findViewById(R.id.tel);
        tel_content = (TextView) findViewById(R.id.tel_content);
        tel_iv = (ImageView) findViewById(R.id.tel_iv);
        mail = (TextView) findViewById(R.id.mail);
        mail_content = (TextView) findViewById(R.id.mail_content);
        iv_adderss_back = (ImageView) findViewById(R.id.iv_adderss_back);
        iv_adderss_collect = (ImageView) findViewById(R.id.iv_adderss_collect);
        iv_adderss_user_pic = (ImageView) findViewById(R.id.iv_adderss_user_pic);
        tv_address_name = (TextView) findViewById(R.id.tv_address_name);
        sendMessagesBtn = (LinearLayout) findViewById(R.id.sendMessagesBtn);
        saveTelBtn = (LinearLayout) findViewById(R.id.saveTelBtn);

        if (userId.equals(GloableConfig.myUserInfo.userId)) {
            sendMessagesBtn.setVisibility(View.GONE);
            saveTelBtn.setVisibility(View.GONE);
        } else {
            sendMessagesBtn.setVisibility(View.VISIBLE);
            saveTelBtn.setVisibility(View.VISIBLE);
        }

        myfragment_signature.setOnClickListener(this);
        iv_adderss_back.setOnClickListener(this);
        iv_adderss_collect.setOnClickListener(this);
        iv_adderss_user_pic.setOnClickListener(this);
        sendMessagesBtn.setOnClickListener(this);
        saveTelBtn.setOnClickListener(this);
        message_iv.setOnClickListener(this);
        mobil_iv.setOnClickListener(this);
        tel_iv.setOnClickListener(this);
    }

    /**
     * 获取数据
     */
    private void getData() {
        ProgressUtil.showPregressDialog(this, R.layout.custom_progress);
        VolleyUtil volleyUtil = new VolleyUtil(mContext);
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        volleyUtil.stringRequest(handler, GloableConfig.UserinfoUrl, map, 1001);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.myfragment_signature) {
            // TODO: 2016/8/2 个性签名
        } else if (v.getId() == R.id.iv_adderss_back) {
            // TODO: 2016/8/2 返回
            finish();
        } else if (v.getId() == R.id.iv_adderss_collect) {
            // TODO: 2016/8/2 收藏
        } else if (v.getId() == R.id.sendMessagesBtn) {
            // TODO: 2016/8/2 发送消息
            RongUtil.startChat(mContext, Conversation.ConversationType.PRIVATE, userId, userInfoBean.userName);
        } else if (v.getId() == R.id.saveTelBtn) {
            // TODO: 2016/8/2 保存到通讯录
            CommonUtils.saveTel(mContext, userInfoBean.userName, userInfoBean.email, phone_content.getText().toString(),
                    tel_content.getText().toString());
        } else if (v.getId() == R.id.message_iv) {
            CommonUtils.sendSms(mContext, phone_content.getText().toString());
        } else if (v.getId() == R.id.mobil_iv) {
            CommonUtils.dialPhoneNumber(mContext, phone_content.getText().toString());
        } else if (v.getId() == R.id.tel_iv) {
            CommonUtils.dialPhoneNumber(mContext, tel_content.getText().toString());
        }
    }
}
