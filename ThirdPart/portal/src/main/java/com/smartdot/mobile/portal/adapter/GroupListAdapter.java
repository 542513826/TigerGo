package com.smartdot.mobile.portal.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.bean.GroupInfoBean;
import com.smartdot.mobile.portal.utils.RongUtil;

import java.util.List;

import io.rong.imlib.model.Conversation;

/**
 * 群组列表的adapter
 * Created by Administrator on 2016/7/24.
 */
public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.ViewHolder> {

    private Context mContext;

    private List<GroupInfoBean> mList;

    /** 是否是要选择群名片*/
    private boolean isChooseGroupCard = false;

    private Activity activity;

    public GroupListAdapter(Context mContext,List<GroupInfoBean> mList){
        this.mContext = mContext;
        this.mList = mList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_grouplist_layout, null);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.group_list_name.setText(mList.get(position).name);
        if (position == mList.size() - 1){
            holder.group_list_line.setVisibility(View.GONE);
        }else {
            holder.group_list_line.setVisibility(View.VISIBLE);
        }

        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isChooseGroupCard){
                    RongUtil.startChat(mContext, Conversation.ConversationType.GROUP,mList.get(position).id,mList.get(position).name);
                }else {
                    Intent intent = new Intent();
                    intent.putExtra("groupid",mList.get(position).id);
                    intent.putExtra("groupname",mList.get(position).name);
                    intent.putExtra("groupurl","");
                    activity.setResult(Activity.RESULT_OK,intent);
                    GloableConfig.groupListType = 0;
                    activity.finish();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View rootView;

        public ImageView group_list_head;

        public TextView group_list_name;

        public View group_list_line;

        public ViewHolder(View rootView) {
            super(rootView);
            this.rootView = rootView;
            this.group_list_head = (ImageView) rootView.findViewById(R.id.group_list_head);
            this.group_list_name = (TextView) rootView.findViewById(R.id.group_list_name);
            this.group_list_line = rootView.findViewById(R.id.group_list_line);

        }
    }

    public void setmList(List<GroupInfoBean> mList) {
        this.mList = mList;
    }

    public void setChooseGroupCard(boolean chooseGroupCard) {
        isChooseGroupCard = chooseGroupCard;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }
}
