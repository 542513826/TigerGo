package com.smartdot.mobile.portal.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.abconstant.GlobleAddressConfig;
import com.smartdot.mobile.portal.adapter.AddressBookAdapter;
import com.smartdot.mobile.portal.bean.AddressBean;
import com.smartdot.mobile.portal.bean.AddressData;
import com.smartdot.mobile.portal.bean.AddressDept;
import com.smartdot.mobile.portal.bean.AddressUser;
import com.smartdot.mobile.portal.bean.GroupInfoBean;
import com.smartdot.mobile.portal.port.OnChangeTitleListener;
import com.smartdot.mobile.portal.port.OnNumberChangeListener;
import com.smartdot.mobile.portal.port.OnRefreshListener;
import com.smartdot.mobile.portal.port.OnRefreshrChangeListener;
import com.smartdot.mobile.portal.port.ScrollOffsetListener;
import com.smartdot.mobile.portal.utils.AddressManagerUtils;
import com.smartdot.mobile.portal.utils.CommonUtil;
import com.smartdot.mobile.portal.utils.CustomToast;
import com.smartdot.mobile.portal.utils.NetUtils;
import com.smartdot.mobile.portal.utils.ProgressUtil;
import com.smartdot.mobile.portal.utils.RongUtil;
import com.smartdot.mobile.portal.utils.StringUtils;
import com.smartdot.mobile.portal.utils.VolleyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;

/**
 * 新 - 通讯录 通过网络即时请求
 */
public class AddressBookActivity extends BaseActivity implements OnClickListener, OnRefreshrChangeListener,
        OnRefreshListener, OnChangeTitleListener, OnNumberChangeListener,ScrollOffsetListener {

    Activity context;

    HorizontalScrollView scrollView;

    LinearLayout homeLl;

    AddressManagerUtils addressUtils;

    TextView titleTv;

    ImageView backBtn;

    ImageView searchBttn;

    Button selectBtn;

    public String currentTitleName = "组织";// 存放当前点击的title

    /** 当前正在点击的listview */
    ListView currentLv = null;

    /** 当前选中的人数 */
    private TextView countTextView;

    /** 部门ID (此变量只在从 [我的部门] 中进入时有值) 其余都为无值状态 */
    private String deptId = "";

    private String groupID = "";

    /** 位于两个左右listview之间的阴影分割线 */
    private View v;

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case 1001:
                // TODO: 通讯录逻辑 每次请求列表数据走入该分支
                loadAddress(msg);
                break;
            case 1002:
                // TODO: 融云逻辑 - 加人逻辑
                rongAddPerson(msg);
                break;
            case 1003:
                // TODO: 融云逻辑 - 创建群
                rongCreateGroup(msg);
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_address);

        context = AddressBookActivity.this;
        initData();
        initViews();
        getData(deptId);// 获取通讯录第一个listview对应的数据
    }

    /** 初始化从上一个界面获取到的数据 */
    private void initData() {
        Intent intent = getIntent();
        try {
            Bundle bundle = intent.getBundleExtra("bundle");
            groupID = bundle.getString("groupId", "");
            deptId = bundle.getString("deptId", "");
            if (!StringUtils.isNull(deptId)) {
                currentTitleName = getString(R.string.mydept);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /** 初始化界面 */
    private void initViews() {
        titleTv = (TextView) findViewById(R.id.address_name_tv);
        backBtn = (ImageView) findViewById(R.id.address_back_imgbtn);
        searchBttn = (ImageView) findViewById(R.id.address_search_iv);
        selectBtn = (Button) findViewById(R.id.select_mumber_confirm_btn);
        scrollView = (HorizontalScrollView) findViewById(R.id.address_horizontalscrollview);
        homeLl = (LinearLayout) findViewById(R.id.address_home_ll);
        countTextView = (TextView) findViewById(R.id.count_mumbers_tv);
        RelativeLayout bottom_layout = (RelativeLayout) findViewById(R.id.bottom_layout);// 常驻于底部选人窗口
        v = findViewById(R.id.address_divider_view);
        addressUtils = new AddressManagerUtils(context, scrollView, homeLl, v);
        backBtn.setOnClickListener(this);
        selectBtn.setOnClickListener(this);
        // 底部选人窗口
        if (GloableConfig.addressBookType != 1 && GloableConfig.addressBookType != 2) {
            bottom_layout.setVisibility(View.GONE);
        } else {
            bottom_layout.setVisibility(View.VISIBLE);
        }

    }

    // 按键消抖
    long currentTime = 0;// 当前时间

    long lastTime = 0;// 上一次点击时间

    /** view操作 */
    private void listViewHanlde(final AddressBookAdapter adapter) {

        final ListView lv = addressUtils.scrollLeft(adapter);
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {

                lastTime = currentTime;
                currentTime = System.currentTimeMillis();
                if (lastTime != 0 && (currentTime - lastTime) < 1000)// 点击过于频繁
                    return;

                currentLv = lv;

                List<AddressBean> itemList = adapter.getListData();
                AddressBean bean = itemList.get(position);
                if (bean.isDept) {
                    if (!bean.dept_id.equals("0")) {
                        currentTitleName = bean.dept_name.trim();
                        for (int i = 0; i < lv.getChildCount(); i++) {
                            View v = arg0.getChildAt(i);
                            if (position == i + lv.getFirstVisiblePosition()) {
                                v.setBackgroundColor(
                                        ContextCompat.getColor(AddressBookActivity.this, R.color.ultramarine));
                            } else {
                                v.setBackgroundColor(ContextCompat.getColor(AddressBookActivity.this, R.color.white));
                            }
                        }
                        getData(bean.dept_id);
                    }
                    adapter.refreshSelectedView(position);
                }
            }
        });
    }

    /** 请求数据 */
    public void getData(String id) {
        ProgressUtil.showPregressDialog(this, R.layout.custom_progress);
        VolleyUtil volleyUtil = new VolleyUtil(this);
        Map<String, String> map = new HashMap<>();
        if (!id.equals("")) {
            map.put("deptId", id);
        }
        volleyUtil.stringRequest(handler, GloableConfig.AddressBookUrl, map, 1001);
    }

    /** 数据解析 */
    private List<AddressBean> parseData(JSONObject result) {
        if (result == null) {
            return new ArrayList<AddressBean>();
        }
        List<AddressBean> list = new ArrayList<AddressBean>();
        AddressData base = null;
        try {
            String a = result.getString("result");
            base = CommonUtil.gson.fromJson(a, AddressData.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // 解析联系人
        if (base != null && base.userList != null) {
            List<AddressUser> users = base.userList;
            for (AddressUser user : users) {
                if (user.user_id.equals("")) {
                    continue;
                }
                AddressBean data = new AddressBean();
                data.obey_dept_id = user.obey_dept_id;
                data.user_id = user.user_id;
                data.user_name = user.user_name;
                data.user_portrait = user.user_portrait;
                data.isDept = false;
                list.add(data);
            }
        }
        // 解析部门
        if (base != null && base.deptList != null) {
            List<AddressDept> depts = base.deptList;
            for (AddressDept dept : depts) {
                if (dept.dept_id.equals("")) {
                    continue;
                }
                AddressBean data = new AddressBean();
                data.obey_dept_id = dept.obey_dept_id;
                data.dept_id = dept.dept_id;
                data.dept_name = dept.dept_name;
                data.dept_pic = dept.dept_pic;
                data.isDept = true;
                list.add(data);
            }
        }
        return list;
    }

    /** 全选/取消多选操作 */
    public void allPersonIDPut(List<AddressBean> list) {
        // 获取当前数据的所属部门id
        String currentObeyDeptId = list.get(0).obey_dept_id;
        // 判断全选部门id的map中是否包含当前列的所属部门id
        if (GlobleAddressConfig.selectedDeptIDs.containsKey(currentObeyDeptId)) {
            // 包含则属于全选情况 判断是否添加过后以此将人员id填进去并刷新状态
            for (AddressBean data : list) {
                if (!GlobleAddressConfig.selectedPersonIDs.containsKey(data.user_id)) {
                    if (!data.isDept) {
                        // 添加到已选联系人中
                        GlobleAddressConfig.selectedPersonIDs.put(data.user_id, data.obey_dept_id);
                        OnNumberChange();
                    }
                }
            }
        }
        // 判断取消全选部门id的map中是否包含当前列的所属部门id
        if (GlobleAddressConfig.cencelDeptIDs.containsKey(currentObeyDeptId)) {
            // 包含则属于取消全选情况 判断是否添加过后以此将人员id删除并刷新状态
            for (AddressBean data : list) {
                if (!data.isDept) {
                    if (GlobleAddressConfig.selectedPersonIDs.containsKey(data.user_id)) {
                        // 从已选联系人中移除
                        GlobleAddressConfig.selectedPersonIDs.remove(data.user_id);
                        OnNumberChange();
                    }
                }
            }
        }
    }

    /** 通讯录加载逻辑 每次请求列表数据走入该分支 */
    private void loadAddress(Message msg) {
        ProgressUtil.dismissProgressDialog();
        JSONObject result = null;

        try {
            result = new JSONObject(msg.obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        List<AddressBean> list = parseData(result);

        if (list == null || list.size() == 0) {
            CustomToast.showToast(AddressBookActivity.this, getString(R.string.part_has_no_person));
            return;
        }

        allPersonIDPut(list);

        titleTv.setText(currentTitleName.trim());

        ListView tLv = null;

        if (addressUtils.getListViews().size() >= 2) {
            tLv = addressUtils.getListViews().get(addressUtils.getListViews().size() - 2);
            // 判断是不是当前点击的listview 如果是 则刷新右侧listview 不新建
            if (currentLv == tLv) {
                AddressBookAdapter adapter = (AddressBookAdapter) addressUtils.getListViews()
                        .get(addressUtils.getListViews().size() - 1).getAdapter();
                adapter.changeData(list);
                currentLv = null;

                return;
            }
        }

        // 新建一列listview
        AddressBookAdapter adapter = new AddressBookAdapter(context, AddressBookActivity.this,
                addressUtils.getViewCount(), addressUtils);
        adapter.setOnRefreshrChangeListener(AddressBookActivity.this);
        adapter.setOnRefreshListener(AddressBookActivity.this);
        adapter.setOnChangeTitleListener(AddressBookActivity.this);
        adapter.setOnNumberChangeListener(AddressBookActivity.this);
        adapter.setScrollOffListener(AddressBookActivity.this);

        /** 第一次进来默认加载第一个界面和第二个界面，并且第一个界面的第一项被选中。 */
        if (addressUtils.getListViews().size() == 0) {
            AddressBean bean = list.get(0);
            // 公司
            if (bean.obey_dept_id.equals("")) {
                currentTitleName = bean.dept_name.trim();
                getData(bean.dept_id);
            }
            adapter.refreshSelectedView(0);
        }

        adapter.changeData(list);
        listViewHanlde(adapter);
        currentLv = null;
    }

    /** 融云请求返回逻辑 - 创建群 */
    private void rongCreateGroup(Message msg) {
        try {
            JSONObject jsonObject = new JSONObject(msg.obj.toString());
            if (jsonObject.getString("code").equals("200")) {
                GroupInfoBean groupInfoBean = CommonUtil.gson.fromJson(jsonObject.getString("result"),
                        GroupInfoBean.class);
                RongUtil.sendInfoMessage(context, groupInfoBean.id, Conversation.ConversationType.GROUP,
                        GloableConfig.myUserInfo.userName + getString(R.string.start_group_chat));
                RongUtil.startChat(context, Conversation.ConversationType.GROUP, groupInfoBean.id, groupInfoBean.name);
                GloableConfig.allGroupMap.put(groupInfoBean.id, groupInfoBean);
                Group group;
                try {
                    group = new Group(groupInfoBean.id, groupInfoBean.name, null);
                } catch (RuntimeException e) {
                    group = new Group(groupInfoBean.id, getString(R.string.default_group_name), null);
                }
                RongIM.getInstance().refreshGroupInfoCache(group);
                exit();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ProgressUtil.dismissProgressDialog();
    }

    /** 融云请求返回逻辑 - 加人 */
    private void rongAddPerson(Message msg) {
        try {
            JSONObject jsonObject = new JSONObject(msg.obj.toString());
            if (jsonObject.getString("code").equals("200")) {
                CustomToast.showToast(context, R.string.operation_success, 400);
                setResult(RESULT_OK);
                GloableConfig.addressBookType = 0; // 通讯录标志位重置为正常
                // TODO: 2016/8/1 加人小黑条
                // 人员
                String userIds = "";
                int i = 0;
                for (Map.Entry<String, String> entry : GlobleAddressConfig.selectedPersonIDs.entrySet()) {
                    if (i > 2) {
                        break;
                    }
                    userIds = userIds + "," + GloableConfig.allUserMap.get(entry.getKey()).userName;
                    i++;
                }
                userIds = userIds.substring(1, userIds.length());
                if (GlobleAddressConfig.selectedPersonIDs.size() > 3) {
                    RongUtil.sendInfoMessage(context, groupID, Conversation.ConversationType.GROUP,
                            GloableConfig.myUserInfo.userName + "邀请了" + userIds + "等"
                                    + GlobleAddressConfig.selectedPersonIDs.size() + "人加入了群聊");
                } else {
                    RongUtil.sendInfoMessage(context, groupID, Conversation.ConversationType.GROUP,
                            GloableConfig.myUserInfo.userName + "邀请了" + userIds + "加入了群聊");
                }
                exit();
            } else {
                CustomToast.showToast(context, R.string.operation_failed, 400);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ProgressUtil.dismissProgressDialog();
    }

    /** 融云添加人员 */
    private void addRongPerson() {
        if (GloableConfig.addressBookType == 1) {
            if (!NetUtils.isConnected(context)) {
                CustomToast.showToast(context, getString(R.string.net_error), 400);
                return;
            }

            // TODO: 2016/8/1 选人进入群组
            for (Map.Entry<String, String> entry : GlobleAddressConfig.groupPersonIDs.entrySet()) {
                GlobleAddressConfig.selectedPersonIDs.remove(entry.getKey());
            }
            if (GlobleAddressConfig.selectedPersonIDs.size() == 0) {
                CustomToast.showToast(context, getString(R.string.choose_none), 400);
            } else {
                RongUtil.addGroupMember(context, groupID, handler, 1002);
                ProgressUtil.showPregressDialog(this, R.layout.custom_progress);
            }

        } else if (GloableConfig.addressBookType == 2) {
            // TODO: 2016/8/1 建群
            if (GlobleAddressConfig.selectedPersonIDs.size() > 1) {
                if (!NetUtils.isConnected(context)) {
                    CustomToast.showToast(context, getString(R.string.net_error), 400);
                    return;
                }
                String userIds = "";
                int i = 0;
                for (Map.Entry<String, String> entry : GlobleAddressConfig.selectedPersonIDs.entrySet()) {
                    if (i > 2) {
                        break;
                    }
                    userIds = userIds + "," + GloableConfig.allUserMap.get(entry.getKey()).userName;
                    i++;
                }
                userIds = userIds.substring(1, userIds.length());

                RongUtil.createGroup(context, userIds, handler, 1003);
                ProgressUtil.showPregressDialog(context, R.layout.custom_progress);

            } else {
                CustomToast.showToast(context, getString(R.string.Atleast2), 400);
            }
        }
    }

    /** 设置选中的人数 */
    public void changeCount() {
        String countString = GlobleAddressConfig.selectedPersonIDs.size() + "";
        countTextView.setText(countString);
    }

    /** 回调接口 - 用于adapter点击通讯录右侧listview新创建一列listview的回调监听 */
    @Override
    public void OnRefreshChange(String deptId) {
        getData(deptId);
    }

    /** 回调接口 - 用于刷新通讯录当前adapter,向Activity重新请求数据 */
    @Override
    public void OnRefresh(String deptId, ListView lv) {
        currentLv = lv;
        getData(deptId);
    }

    /** 回调接口 - 在adapter点击后用于更新通讯录当前点击的部门名称 */
    @Override
    public void OnChangeTitle(String title) {
        currentTitleName = title;
        titleTv.setText(title);
    }

    /** 回调接口 - 在adapter用于更新通讯录当前已添加的人员数量 */
    @Override
    public void OnNumberChange() {
        changeCount();
    }


    /** 回调接口 - 用于在adapter中进入联系人详情时通知外层界面记录当前scrollView的偏移量，用来在关闭详情时进行修正 */
    @Override
    public void OnScrollOffset() {
        // 隐藏前记录偏移量
        mScrollOffset = scrollView.getScrollX();
    }

    @Override
    public void onClick(View v) {
        {
            if (v.getId() == R.id.select_mumber_confirm_btn) {
                // 确认添加人员 在这里加接口逻辑
                addRongPerson();
            } else if (v.getId() == R.id.address_search_iv) {
                // TODO: 2016/8/18 搜索
            } else if (v.getId() == R.id.address_back_imgbtn) {
                // 返回键
                if (addressUtils.getViewCount() <= 2) {
                    exit();
                } else {
                    addressUtils.scrollRight();
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /** 关闭当前界面并清空数据 */
    private void exit() {
        GlobleAddressConfig.cencelDeptIDs.clear();
        GlobleAddressConfig.selectedDeptIDs.clear();
        GlobleAddressConfig.selectedPersonIDs.clear();
        deptId = "";
        finish();
        overridePendingTransition(R.anim.base_back_in, R.anim.base_back_out);// 退场动画
    }

    // 保存位移
    private int mScrollOffset;

    @Override
    public void onResume() {
        super.onResume();
        // 再次显示时读取偏移量进行修正
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollView.scrollTo(mScrollOffset,0);
            }
        },10);
    }
}
