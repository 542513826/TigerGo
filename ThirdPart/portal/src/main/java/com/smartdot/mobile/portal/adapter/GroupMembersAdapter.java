package com.smartdot.mobile.portal.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.bean.GroupMemberBean;
import com.smartdot.mobile.portal.utils.CustomToast;
import com.smartdot.mobile.portal.utils.VolleyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 群主成员列表的adapter
 * Created by Administrator on 2016/7/24.
 */
public class GroupMembersAdapter extends CommonAdapter<GroupMemberBean> {

    private Context mContext;

    private List<GroupMemberBean> mList = new ArrayList<>();

    private Activity activity;

    /** 是否为转让群主 */
    private boolean changeHost = false;

    /** 记录删除的项 */
    private int deletePosition;

    /** 当前用户是否是群主 */
    private boolean isManager = false;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case 1001:
                try {
                    JSONObject jsonObject = new JSONObject(msg.obj.toString());
                    if (jsonObject.getString("code").equals("200")) {
                        CustomToast.showToast(mContext, mContext.getString(R.string.delete_member_success), 400);
                        mList.remove(deletePosition);
                        notifyDataSetChanged();
                    } else if (jsonObject.getString("code").equals("500")) {
                        CustomToast.showToast(mContext, R.string.you_are_not_group_member, 400);
                    } else {
                        CustomToast.showToast(mContext, R.string.getdata_failed, 400);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    };

    public GroupMembersAdapter(Context mContext, List<GroupMemberBean> mList, int itemLayoutResId) {
        super(mContext, mList, itemLayoutResId);
        this.mContext = mContext;
        this.mList = mList;
    }

    @Override
    public void convert(CommonViewHolder viewHolder, GroupMemberBean bean) {
        final int position = viewHolder.getPosition();
        TextView group_members_manager = (TextView) viewHolder.getView(R.id.group_members_manager);
        if (mList.get(position).isManager.equals("1")) {
            group_members_manager.setVisibility(View.VISIBLE);
        } else {
            group_members_manager.setVisibility(View.GONE);
        }
        String name = GloableConfig.allUserMap.get(mList.get(position).userId).userName;
        viewHolder.setText(R.id.group_members_name, name);
        if (position == mList.size() - 1) {
            viewHolder.getView(R.id.group_members_line).setVisibility(View.GONE);
        } else {
            viewHolder.getView(R.id.group_members_line).setVisibility(View.VISIBLE);
        }
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
        volleyUtil.stringRequest(handler, Request.Method.POST, url, 1001);
    }


    public void setChangeHost(boolean changeHost) {
        this.changeHost = changeHost;
    }
}
