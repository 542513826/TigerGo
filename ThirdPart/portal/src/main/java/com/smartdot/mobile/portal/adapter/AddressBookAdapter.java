package com.smartdot.mobile.portal.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.abconstant.GlobleAddressConfig;
import com.smartdot.mobile.portal.activity.AddressBookActivity;
import com.smartdot.mobile.portal.activity.AddressBookUserInfoActivity;
import com.smartdot.mobile.portal.bean.AddressBean;
import com.smartdot.mobile.portal.port.OnChangeTitleListener;
import com.smartdot.mobile.portal.port.OnNumberChangeListener;
import com.smartdot.mobile.portal.port.OnRefreshListener;
import com.smartdot.mobile.portal.port.OnRefreshrChangeListener;
import com.smartdot.mobile.portal.port.ScrollOffsetListener;
import com.smartdot.mobile.portal.utils.AddressManagerUtils;
import com.smartdot.mobile.portal.utils.CustomToast;
import com.smartdot.mobile.portal.utils.RongUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.message.RichContentMessage;

/**
 * 通讯录列表适配器
 *
 * @author wb
 */
public class AddressBookAdapter extends MyBaseAdapter {
    private Context mContext;

    int selectedPosition = -1;

    private Boolean isMultiSelect = true;

    private Map<Integer, Boolean> selectMap = new HashMap<>();

    AddressBookActivity addressBookActivity;

    AddressManagerUtils addressUtils;

    int currentCount;

    /**
     * 刷新选中的view的显示样式
     */
    public void refreshSelectedView(int selectedPosition) {
        this.selectedPosition = selectedPosition;
        resetCount();
        notifyDataSetChanged();
    }

    public AddressBookAdapter(Context context, AddressBookActivity addressBookActivity, int viewCount,
            AddressManagerUtils addressUtils) {
        super(context);
        this.mContext = context;
        this.addressBookActivity = addressBookActivity;
        this.currentCount = viewCount;
        this.addressUtils = addressUtils;
    }

    /**
     * 获取数据
     */
    public List<AddressBean> getListData() {
        return itemList;
    }

    @Override
    public View getView(int position, View contentView, ViewGroup viewGroup) {
        // 加载或复用item界面
        ViewHolder holder = null;
        // 如果没有可重用的item界面，则加载一个新的item
        if (contentView == null) {
            contentView = inflater.inflate(R.layout.item_address_list, null);
            holder = new ViewHolder();
            holder.checkbox_address_item = (CheckBox) contentView.findViewById(R.id.checkbox_address_item);
            holder.tv_address_name = (TextView) contentView.findViewById(R.id.tv_address_name);
            holder.iv_address_pic = (ImageView) contentView.findViewById(R.id.iv_address_pic);
            holder.iayout_address_item = (LinearLayout) contentView.findViewById(R.id.iayout_address_item);

            contentView.setTag(holder);
        } else {
            holder = (ViewHolder) contentView.getTag();
        }

        if (GloableConfig.addressBookType != 1 && GloableConfig.addressBookType != 2) {
            holder.checkbox_address_item.setVisibility(View.GONE);
            holder.iv_address_pic.setPadding(15, 0, 0, 0);
        } else {
            holder.checkbox_address_item.setVisibility(View.VISIBLE);

        }

        if (position == selectedPosition) {
            holder.iayout_address_item.setBackgroundColor(ContextCompat.getColor(mContext, R.color.ultramarine));
        } else {
            holder.iayout_address_item.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
        }

        AddressBean data = (AddressBean) itemList.get(position);

        if (data.dept_id != null) {
            currentDeptId = data.dept_id;
        }

        if (data.obey_dept_id != null) {
            currentObeyDeptId = data.obey_dept_id;
        }

        if (data.isDept) {
            holder.tv_address_name.setText(data.dept_name);
            holder.iv_address_pic.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.pic_org_dept));
        } else {
            holder.tv_address_name.setText(data.user_name);
            holder.iv_address_pic.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.pic_org_user));
        }

        getGroupSize();
        getSelectedPersonSize();
        // 多选框监听
        checkListener(viewGroup, holder, position);
        // layout监听 只有不是部门的时候才能添加该监听,否则无法进入下一页
        layoutListener(holder, position);
        holder.iayout_address_item.setClickable(!data.isDept);

        return contentView;
    }

    int deptSize;// 部门总数量

    int personSize;// 联系人总数量

    int deptCount;// 选中的部门数量

    int personCount;// 选中的联系人数量

    String currentDeptId = "";// 当前的部门id

    String currentObeyDeptId = "";// 当前的联系人所属部门id

    boolean b = true;

    /**
     * 获取部门和联系人的总数量
     */
    public void getGroupSize() {
        if (b) {
            b = false;
            List<AddressBean> list = getListData();
            for (AddressBean data : list) {
                if (data.isDept) {
                    deptSize++;
                } else {
                    personSize++;
                }
            }
        }
    }

    /**
     * 获取当前adapter已选择的人的数量
     */
    public void getSelectedPersonSize() {
        personCount = 0;
        List<AddressBean> list = getListData();
        for (AddressBean data : list) {
            if (GlobleAddressConfig.selectedPersonIDs.containsKey(data.user_id)) {
                personCount++;
            }
        }
    }

    /**
     * 获取当前这列listview中的deptId
     */
    public String getCurrentObeyDeptId() {
        return currentObeyDeptId;
    }

    /**
     * 多选框点击操作
     */
    private void checkListener(final View viewGroup, final ViewHolder holder, final int position) {
        // 在设置监听器选中状态之前 先清空监听器，防止对setChecked进行干扰
        holder.checkbox_address_item.setOnCheckedChangeListener(null);

        // 判断当前的各行item的选择状态
        if (GlobleAddressConfig.selectedPersonIDs.containsKey(((AddressBean) getItem(position)).user_id)
                || GlobleAddressConfig.selectedDeptIDs.containsKey(((AddressBean) getItem(position)).dept_id)) {
            // 当前item属于被添加的联系人/部门
            holder.checkbox_address_item.setChecked(true);
        } else {
            holder.checkbox_address_item.setChecked(false);
        }

        holder.checkbox_address_item.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    /** 选中 */
                    if (((AddressBean) getItem(position)).isDept) {
                        // 选中的是部门 全选部门
                        // 做个反向判断 因为点击layout后也会让多选框处于选中的状态 防止重复操作
                        if (!GlobleAddressConfig.selectedDeptIDs
                                .containsKey(((AddressBean) getItem(position)).dept_id)) {
                            deptCount++;
                            GlobleAddressConfig.selectedDeptIDs.put(((AddressBean) getItem(position)).dept_id,
                                    ((AddressBean) getItem(position)).obey_dept_id); // 添加到全选部门中
                            // item变色
                            ListView lv = (ListView) viewGroup;
                            for (int i = 0; i < lv.getChildCount(); i++) {
                                View v = lv.getChildAt(i);
                                if (position == i + lv.getFirstVisiblePosition()) {
                                    v.setBackgroundColor(ContextCompat.getColor(mContext, R.color.ultramarine));
                                } else {
                                    v.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
                                }
                            }
                            // 执行请求下一页的操作 并全选
                            changeTitleListener.OnChangeTitle(((AddressBean) getItem(position)).dept_name);
                            allRefreshRight(((AddressBean) getItem(position)));
                        }
                    } else {
                        // 选中的是联系人
                        // 做个反向判断 因为点击layout后也会让多选框处于选中的状态 防止重复操作
                        if (!GlobleAddressConfig.selectedPersonIDs
                                .containsKey(((AddressBean) getItem(position)).user_id)) {
                            personCount++;
                            // 添加到已选联系人中
                            GlobleAddressConfig.selectedPersonIDs.put(((AddressBean) getItem(position)).user_id,
                                    ((AddressBean) getItem(position)).obey_dept_id);
                            // 刷新外围底部已添加人数状态
                            numberChangeListener.OnNumberChange();
                            // 每点击1次将取消全选集合清空
                            GlobleAddressConfig.cencelDeptIDs.clear();
                            // 判断是否全选
                            getSelectedPersonSize();
                            if (personCount == personSize) {
                                GlobleAddressConfig.selectedDeptIDs.put(((AddressBean) getItem(position)).obey_dept_id,
                                        ((AddressBean) getItem(position)).obey_dept_id);
                                allRefreshLeft();
                            }
                        }
                    }
                } else {
                    /** 取消选中 */
                    if (((AddressBean) getItem(position)).isDept) {
                        // 取消选中的是部门 取消全选部门
                        // 做个反向判断 因为点击layout后也会让多选框处于选中的状态 防止重复操作
                        if (GlobleAddressConfig.selectedDeptIDs
                                .containsKey(((AddressBean) getItem(position)).dept_id)) {
                            deptCount--;
                            GlobleAddressConfig.selectedDeptIDs.remove(((AddressBean) getItem(position)).dept_id);// 从全选部门中移除
                            // 存入取消全选集合中
                            GlobleAddressConfig.cencelDeptIDs.put(((AddressBean) getItem(position)).dept_id,
                                    ((AddressBean) getItem(position)).dept_id);
                            // item取消变色 (这部分要判断一下当前是否出在左侧列表)
                            holder.iayout_address_item
                                    .setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
                            // 执行请求下一页的操作 并取消全选
                            allRefreshRight(((AddressBean) getItem(position)));
                            addressBookActivity.changeCount();// tv上刷新显示人数
                        }
                    } else {
                        // 取消选中的是联系人
                        // 做个反向判断 因为点击layout后也会让多选框处于选中的状态 防止重复操作
                        if (GlobleAddressConfig.selectedPersonIDs
                                .containsKey(((AddressBean) getItem(position)).user_id)) {
                            personCount--;
                            GlobleAddressConfig.selectedPersonIDs.remove(((AddressBean) getItem(position)).user_id); // 从已选联系人中移除
                            // 刷新外围底部已添加人数状态
                            numberChangeListener.OnNumberChange();
                            // 判断是否不符合全选
                            getSelectedPersonSize();
                            if (personCount != personSize) {
                                GlobleAddressConfig.selectedDeptIDs
                                        .remove(((AddressBean) getItem(position)).obey_dept_id);
                                allRefreshLeft();
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * layout点击操作
     */
    private void layoutListener(final ViewHolder holder, final int position) {
        holder.iayout_address_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (GloableConfig.addressBookType == 1 || GloableConfig.addressBookType == 2) {
                    // 处于多选状态
                    if (!((AddressBean) getItem(position)).isDept) {
                        // (没有对应的点击是部门状态,已把部门的监听取消了,交由Fragment点击listview处理)
                        // 点击的是联系人 判断是否选择过
                        if (GlobleAddressConfig.selectedPersonIDs
                                .containsKey(((AddressBean) getItem(position)).user_id)) {
                            // 选择过 将添加的人删除
                            personCount--;
                            GlobleAddressConfig.selectedPersonIDs.remove(((AddressBean) getItem(position)).user_id);
                            // 每点击1次将取消全选集合清空
                            GlobleAddressConfig.cencelDeptIDs.clear();
                            // 多选框取消选中
                            holder.checkbox_address_item.setChecked(false);
                            // 刷新外围底部已添加人数状态
                            numberChangeListener.OnNumberChange();
                            // 判断是否不符合全选
                            getSelectedPersonSize();
                            if (personCount != personSize) {
                                GlobleAddressConfig.selectedDeptIDs
                                        .remove(((AddressBean) getItem(position)).obey_dept_id);
                                allRefreshLeft();
                            }
                        } else {
                            // 未选择过 将联系人添加
                            personCount++;
                            GlobleAddressConfig.selectedPersonIDs.put(((AddressBean) getItem(position)).user_id,
                                    ((AddressBean) getItem(position)).obey_dept_id);
                            // 每点击1次将取消全选集合清空
                            GlobleAddressConfig.cencelDeptIDs.clear();
                            // 多选框选中
                            holder.checkbox_address_item.setChecked(true);
                            // 刷新外围底部已添加人数状态
                            numberChangeListener.OnNumberChange();
                            // 判断是否全选
                            getSelectedPersonSize();
                            if (personCount == personSize) {
                                GlobleAddressConfig.selectedDeptIDs.put(((AddressBean) getItem(position)).obey_dept_id,
                                        ((AddressBean) getItem(position)).obey_dept_id);
                                allRefreshLeft();
                            }
                        }
                    }
                } else if (GloableConfig.addressBookType == 3) {
                    GloableConfig.addressBookType = 0;
                    RongUtil.startChat(context, Conversation.ConversationType.PRIVATE,
                            ((AddressBean) getItem(position)).user_id, ((AddressBean) getItem(position)).user_name);
                    addressBookActivity.finish();

                } else if (GloableConfig.addressBookType == 4) {
                    // TODO: 2016/8/5 发送一个群名片，然后关闭通讯录
                    String showMessage = GloableConfig.cardGroup.name + "\n" + "点击加入该群参与群组讨论";
                    final RichContentMessage groupcard = RichContentMessage.obtain("群名片", showMessage, "",
                            "group://" + GloableConfig.cardGroup.id);
                    groupcard.setExtra(GloableConfig.cardGroup.id);
                    RongIM.getInstance().getRongIMClient().sendMessage(Conversation.ConversationType.PRIVATE,
                            ((AddressBean) getItem(position)).user_id, groupcard, null, null,
                            new RongIMClient.SendMessageCallback() {

                                @Override
                                public void onError(Integer messageId, RongIMClient.ErrorCode e) {

                                }

                                @Override
                                public void onSuccess(Integer integer) {
                                    CustomToast.showToast(context, "群名片已分享", 400);
                                    addressBookActivity.finish();
                                }

                            });

                } else {
                    // (没有对应的点击是部门状态,已把部门的监听取消了,交由Fragment点击listview处理)
                    // 未处于多选状态
                    if (!((AddressBean) getItem(position)).isDept) {
                        // 点击的是联系人 进入聊天界面
                        Intent intent = new Intent(context, AddressBookUserInfoActivity.class);
                        intent.putExtra("userId", ((AddressBean) getItem(position)).user_id);
                        context.startActivity(intent);
                        addressBookActivity.overridePendingTransition(R.anim.base_slide_right_in, R.anim.base_slide_right_out);
                        // 通知外部界面记录当前scrollview偏移量
                        scrollOffsetListener.OnScrollOffset();
                    }
                }
            }
        });
    }

    /**
     * 让左侧列表刷新状态
     */
    private void allRefreshLeft() {
        List<ListView> list = addressUtils.getListViews();
        ListView lv = list.get(currentCount - 1);
        AddressBookAdapter a = (AddressBookAdapter) lv.getAdapter();
        a.notifyDataSetChanged();
    }

    /**
     * 让右侧列表刷新状态 如果当前页在右侧 会自动进入下一页
     */
    private void allRefreshRight(AddressBean data) {
        List<ListView> list = addressUtils.getListViews();
        int to = currentCount + 1;
        int viewCount = addressUtils.getViewCount();
        if (to >= viewCount) {
            // 新创建一列listview
            changeListener.OnRefreshChange(data.dept_id);
        } else {
            // 请求数据并刷新当前listview
            ListView lv = list.get(currentCount);
            refreshListener.OnRefresh(data.dept_id, lv);
            AddressBookAdapter adapter = (AddressBookAdapter) addressUtils.getListViews()
                    .get(addressUtils.getListViews().size() - 1).getAdapter();
            adapter.resetCount();
        }
    }

    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    /**
     * 重置当前全局变量状态,防止错乱
     */
    public void resetCount() {
        personCount = 0;
        deptSize = 0;
        personSize = 0;
        b = true;
    }

    OnRefreshrChangeListener changeListener;

    OnRefreshListener refreshListener;

    OnChangeTitleListener changeTitleListener;

    OnNumberChangeListener numberChangeListener;

    ScrollOffsetListener scrollOffsetListener;

    /**
     * 点击右侧listview新创建一列listview回调监听
     */
    public void setOnRefreshrChangeListener(OnRefreshrChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    /**
     * 刷新右侧listview回调监听
     */
    public void setOnRefreshListener(OnRefreshListener refreshListener) {
        this.refreshListener = refreshListener;
    }

    /**
     * 刷新标题栏回调监听(全选部门时调用)
     */
    public void setOnChangeTitleListener(OnChangeTitleListener onChangeTitleListener) {
        this.changeTitleListener = onChangeTitleListener;
    }

    /**
     * 刷新外围底部已选中人数回调监听
     */
    public void setOnNumberChangeListener(OnNumberChangeListener numberChangeListener) {
        this.numberChangeListener = numberChangeListener;
    }

    /**
     * 点击联系人详情时回调监听，用于通知外部界面记录scrollview偏移量
     */
    public void setScrollOffListener(ScrollOffsetListener scrollOffsetListener) {
        this.scrollOffsetListener = scrollOffsetListener;
    }

    class ViewHolder {
        private CheckBox checkbox_address_item;

        private TextView tv_address_name;

        private ImageView iv_address_pic;

        private LinearLayout iayout_address_item;
    }
}
