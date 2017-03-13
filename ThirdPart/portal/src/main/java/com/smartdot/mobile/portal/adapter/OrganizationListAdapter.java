package com.smartdot.mobile.portal.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GlobleAddressConfig;
import com.smartdot.mobile.portal.activity.AddressBookUserInfoActivity;
import com.smartdot.mobile.portal.activity.OrganizationActivity;
import com.smartdot.mobile.portal.bean.UserInfoBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 组织机构列表的adapter Created by Administrator on 2016/7/15.
 */
public class OrganizationListAdapter extends CommonAdapter<UserInfoBean> {

    private Context mContext;

    private List<UserInfoBean> mList;

    private Map<Integer, Boolean> selectMap = new HashMap<>();

    private Map<String, Boolean> groupMemberMap = new HashMap<>();

    /**
     * 是否可以多选
     */
    private Boolean isMultiSelect = false;

    private OrganizationActivity organizationActivity;

    public OrganizationListAdapter(Context context, List<UserInfoBean> list, int itemLayoutResId) {
        super(context, list, itemLayoutResId);
        mContext = context;
        mList = list;
    }

    @Override
    public void convert(CommonViewHolder viewHolder, UserInfoBean bean) {
        final int position = viewHolder.getPosition();
        RelativeLayout item_container_rl = (RelativeLayout) viewHolder.getView(R.id.item_container_rl);
        ImageView item_choose_img = (ImageView) viewHolder.getView(R.id.item_choose_img);
        if (!isMultiSelect) {
            item_container_rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: 2016/8/1 点击群成员跳转
                    Intent intent = new Intent(mContext, AddressBookUserInfoActivity.class);
                    intent.putExtra("userId", mList.get(position).userId);
                    mContext.startActivity(intent);
                }
            });
        } else {
            if (selectMap.containsKey(position)) {
                item_container_rl.setBackgroundColor(ContextCompat.getColor(mContext, R.color.item_blue));
                item_choose_img.setVisibility(View.VISIBLE);
            } else {
                item_container_rl.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
                item_choose_img.setVisibility(View.INVISIBLE);
            }

            if (groupMemberMap.containsKey(mList.get(position).userId)) {
                item_container_rl.setBackgroundColor(ContextCompat.getColor(mContext, R.color.gray));
                item_choose_img.setVisibility(View.INVISIBLE);
            } else {
                addListener(item_container_rl, item_choose_img, position);
            }
        }
        viewHolder.setText(R.id.item_name_tv, mList.get(position).userName);
    }

    /**
     * 设置点击监听
     */
    private void addListener(final RelativeLayout item_container_rl, final ImageView item_choose_img,
            final int position) {

        // 选人
        item_container_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!selectMap.containsKey(position)) {
                    // 如果之前没有选中，点击后变为选中效果
                    item_container_rl.setBackgroundColor(ContextCompat.getColor(mContext, R.color.item_blue));
                    item_choose_img.setVisibility(View.VISIBLE);
                    selectMap.put(position, true);
                    GlobleAddressConfig.selectedPersonIDs.put(mList.get(position).userId,
                            mList.get(position).obey_dept_id);
                } else {
                    item_container_rl.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
                    item_choose_img.setVisibility(View.INVISIBLE);
                    selectMap.remove(position);
                    GlobleAddressConfig.selectedPersonIDs.remove(mList.get(position).userId);
                }
                organizationActivity.setMumberText();
            }
        });
    }

    public void setmList(List<UserInfoBean> mList) {
        this.mList = mList;
    }

    public void setMultiSelect(Boolean multiSelect) {
        isMultiSelect = multiSelect;
    }

    public void setOrganizationActivity(OrganizationActivity organizationActivity) {
        this.organizationActivity = organizationActivity;
    }

}
