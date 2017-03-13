package com.smartdot.mobile.portal.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.adapter.GroupMembersAdapter;
import com.smartdot.mobile.portal.bean.GroupInfoBean;
import com.smartdot.mobile.portal.bean.GroupMemberBean;
import com.smartdot.mobile.portal.utils.CommonUtil;
import com.smartdot.mobile.portal.utils.CustomToast;
import com.smartdot.mobile.portal.utils.NetUtils;
import com.smartdot.mobile.portal.utils.RongUtil;
import com.smartdot.mobile.portal.utils.ThemeHelper;
import com.smartdot.mobile.portal.utils.VolleyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.rong.imlib.model.Conversation;

/**
 * 群成员列表界面
 */
public class GroupMembersActivity extends BaseActivity
        implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private Context mContext;

    private ImageView title_left_img;

    private TextView title_center_text;

    private ImageView title_right_img;

    private TextView title_right_text;

    private ListView mListView;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private GroupMembersAdapter mAdapter;

    private List<GroupMemberBean> mList = new ArrayList<>();

    private String GroupId;

    private GroupInfoBean groupInfoBean;

    private Boolean isManager;

    private int deletePosition = 0;

    /** 被踢出去的人的名字 */
    private String deleteName = "";

    /**
     * 是否为转让群主
     */
    private boolean changeHost = false;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case 1001: // 获取成员列表
                dealWithMembersList(msg.obj.toString());
                break;
            case 1002: // 踢出群成员
                dealWithRemoveMember(msg.obj.toString());
                break;
            default:
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_members);
        mContext = this;
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("bundle");
        changeHost = bundle.getBoolean("changeHost");
        isManager = bundle.getBoolean("isManager");
        GroupId = bundle.getString("groupId");
        initView();
        setListener();
        // 进入的时候显示刷新，必须调用以下代码
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
        GroupMembersActivity.this.onRefresh();
    }

    private void initView() {
        title_left_img = (ImageView) findViewById(R.id.title_left_img);
        title_center_text = (TextView) findViewById(R.id.title_center_text);
        title_right_img = (ImageView) findViewById(R.id.title_right_img);
        title_right_text = (TextView) findViewById(R.id.title_right_text);
        mListView = (ListView) findViewById(R.id.group_members_lv);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.group_members_swiperefreshlayout);

        title_right_img.setVisibility(View.GONE);

        if (changeHost) {
            title_center_text.setText(R.string.transfer_group);
            title_right_text.setVisibility(View.GONE);
        } else {
            title_right_text.setVisibility(View.VISIBLE);
            title_right_text.setText(R.string.new_add);
            title_center_text.setText(R.string.group_members);
        }

        title_left_img.setOnClickListener(this);
        title_right_text.setOnClickListener(this);

        mList = new ArrayList<>();

        mAdapter = new GroupMembersAdapter(mContext, mList,R.layout.item_groupmenber_layout);
        mAdapter.setChangeHost(changeHost);
        mListView.setAdapter(mAdapter);

        // 刷新时，指示器旋转后变化的颜色
        mSwipeRefreshLayout.setColorSchemeResources(R.color.blue, R.color.yellow);
        mSwipeRefreshLayout.setOnRefreshListener(this);

    }

    /**
     * 设置列表监听
     */
    private void setListener() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (changeHost) {
                    // 转让群主
                    if (mList.get(position).isManager.equals("1")) {
                        // 这个人就是群主，点了没反应就好了
                    } else {
                        Intent intent = new Intent();
                        intent.putExtra("new_host_id", mList.get(position).userId);
                        intent.putExtra("new_host_name",
                                GloableConfig.allUserMap.get(mList.get(position).userId).userName);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                } else {
                    // TODO: 2016/8/1 点击群成员跳转
                    Intent intent = new Intent(mContext, AddressBookUserInfoActivity.class);
                    intent.putExtra("userId", mList.get(position).userId);
                    startActivity(intent);
                }

            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                if (!changeHost && isManager) {
                    // 如果不是转让群主并且此人是群主才有 长按点击踢人
                    if (mList.get(position).isManager.equals("1")) {
                        // 这个人就是群主，点了没反应就好了
                    } else {
                        new AlertDialog.Builder(mContext).setTitle(getString(R.string.isDeleteMembers))
                                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        deletePosition = position;
                                        // 删除群成员
                                        deleteMembers(mList.get(deletePosition).userId);

                                    }
                                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).create().show();
                    }
                }
                return true;
            }
        });
    }

    /**
     * 删除群成员
     *
     * @param userId
     */
    private void deleteMembers(String userId) {
        VolleyUtil volleyUtil = new VolleyUtil(mContext);
        String url = String.format(GloableConfig.RongCloud.quitGroupUrl, userId, mList.get(0).groupId,
                GloableConfig.myUserInfo.userId);
        deleteName = GloableConfig.allUserMap.get(userId).userName;
        volleyUtil.stringRequest(handler, Request.Method.POST, url, 1002);
    }

    /**
     * 获取成员列表
     */
    private void getData() {
        if (!NetUtils.isConnected(mContext)) {
            CustomToast.showToast(mContext, getString(R.string.net_error), 400);
            return;
        }
        VolleyUtil volleyUtil = new VolleyUtil(mContext);
        String url = String.format(GloableConfig.RongCloud.getGroupInfoUrl, GroupId);
        volleyUtil.stringRequest(handler, Request.Method.POST, url, 1001);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.title_left_img) {
            finish();
        } else if (v.getId() == R.id.title_right_text) {
            // 新增群成员
            Intent intent = new Intent(mContext, OrganizationActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("groupInfoBean", groupInfoBean);
            bundle.putString("groupId", groupInfoBean.id);
            bundle.putBoolean("isMultiSelect", true);
            bundle.putSerializable("memberList", (Serializable) groupInfoBean.memberList);
            bundle.putSerializable("depart_ids_list", (Serializable) groupInfoBean.depart_ids_list);
            intent.putExtra("bundle", bundle);
            GloableConfig.addressBookType = 1;
            startActivityForResult(intent, 2001);
        }
    }

    @Override
    public void onRefresh() {
        // mSwipeRefreshLayout.setRefreshing(false);
        getData();
    }

    /**
     * 处理获取成员列表返回的数据
     */
    private void dealWithMembersList(String resultString){
        try {
            JSONObject jsonObject = new JSONObject(resultString);
            if (jsonObject.getString("code").equals("200")) {
                groupInfoBean = CommonUtil.gson.fromJson(jsonObject.getString("result"), GroupInfoBean.class);
                mList.addAll(groupInfoBean.memberList);
                mAdapter.notifyDataSetChanged();
            } else if (jsonObject.getString("code").equals("500")) {
                CustomToast.showToast(mContext, getString(R.string.you_are_not_group_member), 400);
            } else {
                CustomToast.showToast(mContext, getString(R.string.getdata_failed), 400);
            }
            mSwipeRefreshLayout.setRefreshing(false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理移除群成员信息的返回数据
     * @param resultString
     */
    private void dealWithRemoveMember(String resultString){
        try {
            JSONObject jsonObject = new JSONObject(resultString);
            if (jsonObject.getString("code").equals("200")) {
                RongUtil.sendInfoMessage(mContext, mList.get(deletePosition).groupId,
                        Conversation.ConversationType.GROUP,
                        GloableConfig.myUserInfo.userName + "将" + deleteName + "移出了群聊");
                mList.remove(deletePosition);
                mAdapter.notifyDataSetChanged();
                setResult(RESULT_OK);
                // TODO: 2016/8/1 发送小黑条消息
            } else if (jsonObject.getString("code").equals("500")) {
                CustomToast.showToast(mContext, getString(R.string.you_are_not_group_member), 400);
            } else {
                CustomToast.showToast(mContext, getString(R.string.getdata_failed), 400);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
            case 2001:
                getData();
                break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Intent intent = new Intent();
            intent.putExtra("new_host_id", "0");
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ThemeHelper.setSwipeRefreshLayoutColor(mContext,mSwipeRefreshLayout);
    }
}
