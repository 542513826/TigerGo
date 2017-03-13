package com.smartdot.mobile.portal.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.adapter.DeleteMemberAdapter;
import com.smartdot.mobile.portal.bean.GroupMemberBean;
import com.smartdot.mobile.portal.utils.CustomToast;
import com.smartdot.mobile.portal.utils.ProgressUtil;
import com.smartdot.mobile.portal.utils.RongUtil;
import com.smartdot.mobile.portal.utils.VolleyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.rong.imlib.model.Conversation;

/**
 * 删除群成员界面
 */
public class DeleteMembersActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;

    private TextView title_left_text;

    private ImageView title_left_img;

    private TextView title_center_text;

    private ImageView title_right_img;

    private TextView title_right_text;

    private ListView delete_members_lv;

    private List<GroupMemberBean> mList = new ArrayList<>();

    DeleteMemberAdapter mAdapter;

    private List<GroupMemberBean> deleteList = new ArrayList<>();

    private String groupId = "";

    private String userIds = "";

    private String userNames = "";

    private String toastString = "";

    /** 是否删除过群成员 */
    private Boolean hasDelete = false;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1001) {
                try {
                    JSONObject jsonObject = new JSONObject(msg.obj.toString());
                    if (jsonObject.getString("code").equals("200")) {

                        CustomToast.showToast(mContext,getString(R.string.operation_success), 400);
                        RongUtil.sendInfoMessage(mContext, groupId, Conversation.ConversationType.GROUP, toastString);

                        for (int i = 0; i < deleteList.size(); i++) {
                            if (mList.contains(deleteList.get(i))) {
                                mList.remove(deleteList.get(i));
                            }
                        }

                        userIds = "";
                        userNames = "";
                        mAdapter.setmList(mList);
                        mAdapter.setSelectMap(new HashMap<Integer, Boolean>());
                        mAdapter.notifyDataSetChanged();
                        hasDelete = true;
                    }
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
        setContentView(R.layout.activity_delete_members);
        mContext = this;
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("bundle");
        mList = (List<GroupMemberBean>) bundle.getSerializable("memberList");
        groupId = bundle.getString("groupId");
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).isManager.equals("1")) {
                mList.remove(i);
            }
        }
        initView();
    }

    private void initView() {
        title_left_text = (TextView) findViewById(R.id.title_left_text);
        title_left_img = (ImageView) findViewById(R.id.title_left_img);
        title_center_text = (TextView) findViewById(R.id.title_center_text);
        title_right_img = (ImageView) findViewById(R.id.title_right_img);
        title_right_text = (TextView) findViewById(R.id.title_right_text);
        delete_members_lv = (ListView) findViewById(R.id.delete_members_lv);

        title_right_text.setText(R.string.confirm);
        title_center_text.setText(R.string.remove_member);
        title_right_text.setVisibility(View.VISIBLE);
        title_right_img.setVisibility(View.GONE);

        title_left_img.setOnClickListener(this);
        title_right_text.setOnClickListener(this);

        mAdapter = new DeleteMemberAdapter(mContext, mList,R.layout.item_delete_members);
        delete_members_lv.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.title_left_img) {
            if (hasDelete) {
                setResult(RESULT_OK);
            }
            finish();
        }
        if (v.getId() == R.id.title_right_text) {
            // TODO: 2016/8/4 批量移除群成员
            new AlertDialog.Builder(mContext).setMessage(R.string.isRemoveMember)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteMembers();
                            dialog.dismiss();
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
        }
    }

    /**
     * 移除群成员
     */
    private void deleteMembers() {
        deleteList = mAdapter.getDeleteList();
        if (deleteList.size() == 0) {
            CustomToast.showToast(mContext, getString(R.string.choose_no_one), 400);
            return;
        } else if (deleteList.size() < 4) {
            for (int i = 0; i < deleteList.size(); i++) {
                userIds = userIds + "," + deleteList.get(i).userId;
                userNames = userNames + "," + GloableConfig.allUserMap.get(deleteList.get(i).userId).userName;
            }
            userIds = userIds.substring(1, userIds.length());
            userNames = userNames.substring(1, userNames.length());
            toastString = GloableConfig.myUserInfo.userName + "将" + userNames + "移除了群组";
        } else {
            for (int i = 0; i < deleteList.size(); i++) {
                userIds = userIds + "," + deleteList.get(i).userId;

            }
            for (int i = 0; i < deleteList.size(); i++) {
                if (i > 2) {
                    break;
                }
                userNames = userNames + "," + GloableConfig.allUserMap.get(deleteList.get(i).userId).userName;
            }
            userIds = userIds.substring(1, userIds.length());
            userNames = userNames.substring(1, userNames.length());
            toastString = GloableConfig.myUserInfo.userName + "将" + userNames + "等" + deleteList.size() + "人移除了群组";
        }
        VolleyUtil volleyUtil = new VolleyUtil(mContext);
        String url = String.format(GloableConfig.RongCloud.quitGroupUrl, userIds, groupId,
                GloableConfig.myUserInfo.userId);
        ProgressUtil.showPregressDialog(mContext, R.layout.custom_progress);
        volleyUtil.stringRequest(handler, Request.Method.POST, url, 1001);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (hasDelete) {
                setResult(RESULT_OK);
            }
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
