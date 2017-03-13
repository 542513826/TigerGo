package com.smartdot.mobile.portal.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.bean.GroupMemberBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/8/4.
 */
public class DeleteMemberAdapter extends CommonAdapter<GroupMemberBean> {

    private Context mContext;

    private List<GroupMemberBean> mList = new ArrayList<>();

    private Map<Integer, Boolean> selectMap = new HashMap<>();

    private List<GroupMemberBean> deleteList = new ArrayList<>();

    public DeleteMemberAdapter(Context mContext, List<GroupMemberBean> mList, int itemLayoutResId) {
        super(mContext, mList, itemLayoutResId);
        this.mContext = mContext;
        this.mList = mList;
    }

    @Override
    public void convert(CommonViewHolder viewHolder, GroupMemberBean bean) {
        final int position = viewHolder.getPosition();
        View group_members_line = viewHolder.getView(R.id.group_members_line);
        ImageView delete_ic = (ImageView) viewHolder.getView(R.id.delete_ic);
        RelativeLayout root = (RelativeLayout) viewHolder.getView(R.id.delete_members_root);
        if (position == mList.size() - 1) {
            group_members_line.setVisibility(View.INVISIBLE);
        } else {
            group_members_line.setVisibility(View.VISIBLE);
        }

        if (selectMap.containsKey(position)) {
            delete_ic.setVisibility(View.VISIBLE);
        } else {
            delete_ic.setVisibility(View.INVISIBLE);
        }

        viewHolder.setText(R.id.delete_members_name, GloableConfig.allUserMap.get(mList.get(position).userId).userName);

        addListener(root, delete_ic, position);
    }

    private void addListener(RelativeLayout root, final ImageView delete_ic, final int position) {

        // 选人
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!selectMap.containsKey(position)) {
                    // 如果之前没有选中，点击后变为选中效果
                    delete_ic.setVisibility(View.VISIBLE);
                    selectMap.put(position, true);
                    deleteList.add(mList.get(position));
                } else {
                    delete_ic.setVisibility(View.INVISIBLE);
                    selectMap.remove(position);
                    deleteList.remove(mList.get(position));
                }
            }
        });

    }


    public List<GroupMemberBean> getmList() {
        return mList;
    }

    public void setmList(List<GroupMemberBean> mList) {
        this.mList = mList;
    }

    public List<GroupMemberBean> getDeleteList() {
        return deleteList;
    }

    public void setDeleteList(List<GroupMemberBean> deleteList) {
        this.deleteList = deleteList;
    }

    public void setSelectMap(Map<Integer, Boolean> selectMap) {
        this.selectMap = selectMap;
    }
}
