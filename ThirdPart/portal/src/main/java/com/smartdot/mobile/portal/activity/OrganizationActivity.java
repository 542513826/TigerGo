package com.smartdot.mobile.portal.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.abconstant.GlobleAddressConfig;
import com.smartdot.mobile.portal.adapter.OrganizationListAdapter;
import com.smartdot.mobile.portal.bean.GroupInfoBean;
import com.smartdot.mobile.portal.bean.GroupMemberBean;
import com.smartdot.mobile.portal.bean.UserInfoBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 组织界面
 */
public class OrganizationActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;

    private ImageView title_left_img;

    private TextView title_left_text;

    private TextView title_center_text;

    private ImageView title_right_img;

    private RelativeLayout search_rl;

    private ImageView mydept_img;

    private RelativeLayout mydept_rl;

    private ImageView mygroup_img;

    private TextView mygroup_tv;

    private RelativeLayout mygroup_rl;

    private ImageView public_img;

    private RelativeLayout public_rl;

    private ImageView ic_organization_img;

    private RelativeLayout organization_rl;

    private ImageView ic_creategroup_img;

    private RelativeLayout creategroup_rl;

    private ListView common_contant_lv;

    private TextView count_mumbers_tv;

    private Button select_mumber_confirm_btn;

    OrganizationListAdapter mAdapter;

    private List<UserInfoBean> mList;

    private String groupID = "";

    private List<GroupMemberBean> memberList = new ArrayList<>();

    private List<String> depart_ids = new ArrayList<>();

    /**
     * 是否可以多选
     */
    private Boolean isMultiSelect = false;

    private GroupInfoBean groupInfoBean = new GroupInfoBean();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization);
        mContext = this;
        Intent intent = getIntent();
        try {
            Bundle bundle = intent.getBundleExtra("bundle");
            groupInfoBean = (GroupInfoBean) bundle.getSerializable("groupInfoBean");
            groupID = bundle.getString("groupId", "");
            isMultiSelect = bundle.getBoolean("isMultiSelect", false);
            memberList = (List<GroupMemberBean>) bundle.getSerializable("memberList");
            depart_ids = (List<String>) bundle.getSerializable("depart_ids_list");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        initView();
    }

    private void initView() {
        title_left_img = (ImageView) findViewById(R.id.title_left_img);
        title_left_text = (TextView) findViewById(R.id.title_left_text);
        title_center_text = (TextView) findViewById(R.id.title_center_text);
        title_right_img = (ImageView) findViewById(R.id.title_right_img);
        search_rl = (RelativeLayout) findViewById(R.id.search_rl);
        mydept_img = (ImageView) findViewById(R.id.mydept_img);
        mydept_rl = (RelativeLayout) findViewById(R.id.mydept_rl);
        mygroup_img = (ImageView) findViewById(R.id.mygroup_img);
        mygroup_tv = (TextView) findViewById(R.id.mygroup_tv);
        mygroup_rl = (RelativeLayout) findViewById(R.id.mygroup_rl);
        public_img = (ImageView) findViewById(R.id.public_img);
        public_rl = (RelativeLayout) findViewById(R.id.public_rl);
        ic_organization_img = (ImageView) findViewById(R.id.ic_organization_img);
        organization_rl = (RelativeLayout) findViewById(R.id.organization_rl);
        ic_creategroup_img = (ImageView) findViewById(R.id.ic_creategroup_img);
        creategroup_rl = (RelativeLayout) findViewById(R.id.creategroup_rl);
        common_contant_lv = (ListView) findViewById(R.id.common_contant_lv);
        count_mumbers_tv = (TextView) findViewById(R.id.count_mumbers_tv);
        select_mumber_confirm_btn = (Button) findViewById(R.id.select_mumber_confirm_btn);

        title_center_text.setText(R.string.choose_contact);
        title_left_img.setVisibility(View.GONE);
        title_right_img.setVisibility(View.GONE);
        title_left_text.setVisibility(View.VISIBLE);

        if (GloableConfig.addressBookType != 1 && GloableConfig.addressBookType != 2) {
            findViewById(R.id.organization_bottom).setVisibility(View.GONE);
        }

        title_left_text.setOnClickListener(this);
        search_rl.setOnClickListener(this);
        mydept_rl.setOnClickListener(this);
        mygroup_rl.setOnClickListener(this);
        public_rl.setOnClickListener(this);
        organization_rl.setOnClickListener(this);
        creategroup_rl.setOnClickListener(this);
        select_mumber_confirm_btn.setOnClickListener(this);

        mList = new ArrayList<>();

        mAdapter = new OrganizationListAdapter(this, mList,R.layout.item_common_contact);
        mAdapter.setMultiSelect(isMultiSelect);
        mAdapter.setOrganizationActivity(OrganizationActivity.this);
        common_contant_lv.setAdapter(mAdapter);

    }

    public void setMumberText() {
        String count = GlobleAddressConfig.selectedPersonIDs.size() + "";
        count_mumbers_tv.setText(count);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.search_rl) {
            // 搜索
            Intent intent = new Intent(mContext, SearchActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.title_left_text) {
            finish();
        } else if (v.getId() == R.id.creategroup_rl) {
            // TODO: 2016/7/15 我的部门
            switch (GloableConfig.addressBookType) {
            case 0:
            case 3:
            case 4:
                Intent intent = new Intent(mContext, AddressBookActivity.class);
                Bundle bundle1 = new Bundle();
                bundle1.putString("deptId", GloableConfig.myUserInfo.obey_dept_id);
                intent.putExtra("bundle", bundle1);
                startActivity(intent);
                break;
            case 1:
                // TODO: 2016/7/31 加人
                GlobleAddressConfig.selectedPersonIDs.clear();
                GlobleAddressConfig.groupPersonIDs.clear();
                // 将当前群组的已选人员和已全选部门id放到全局map中,让通讯录界面识别
                for (GroupMemberBean bean : groupInfoBean.memberList) {
                    GlobleAddressConfig.selectedPersonIDs.put(bean.userId, "");// 添加到已选联系人中
                }

                for (GroupMemberBean bean : groupInfoBean.memberList) {
                    GlobleAddressConfig.groupPersonIDs.put(bean.userId, "");// 记录群组人员
                }

                for (String bean : groupInfoBean.depart_ids_list) {
                    GlobleAddressConfig.selectedDeptIDs.put(bean, ""); // 添加到全选部门中
                }

                Intent intent1 = new Intent(mContext, AddressBookActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("deptId", GloableConfig.myUserInfo.obey_dept_id);
                bundle.putString("groupId", groupInfoBean.id);
                bundle.putBoolean("isMultiSelect", true);
                intent1.putExtra("bundle", bundle);
                GloableConfig.addressBookType = 1;
                startActivityForResult(intent1, 2001);
                break;
            case 2:
                // TODO: 2016/7/31 创建群组
                Intent intent2 = new Intent(mContext, AddressBookActivity.class);
                Bundle bundle2 = new Bundle();
                bundle2.putString("deptId", GloableConfig.myUserInfo.obey_dept_id);
                intent2.putExtra("bundle", bundle2);
                startActivity(intent2);
                finish();
                break;

            }

        } else if (v.getId() == R.id.mygroup_rl) {
            // TODO: 2016/7/15 我的群组
        } else if (v.getId() == R.id.public_rl) {
            // TODO: 2016/7/15 应用号
        } else if (v.getId() == R.id.organization_rl) {
            // TODO: 2016/7/15 在线通讯录
            switch (GloableConfig.addressBookType) {
            case 0:
            case 3:
            case 4:
                Intent intent = new Intent(mContext, AddressBookActivity.class);
                startActivity(intent);
                finish();
                break;
            case 1:
                // TODO: 2016/7/31 加人
                GlobleAddressConfig.selectedPersonIDs.clear();
                GlobleAddressConfig.groupPersonIDs.clear();
                // 将当前群组的已选人员和已全选部门id放到全局map中,让通讯录界面识别
                for (GroupMemberBean bean : groupInfoBean.memberList) {
                    GlobleAddressConfig.selectedPersonIDs.put(bean.userId, "");// 添加到已选联系人中
                }

                for (GroupMemberBean bean : groupInfoBean.memberList) {
                    GlobleAddressConfig.groupPersonIDs.put(bean.userId, "");// 记录群组人员
                }

                for (String bean : groupInfoBean.depart_ids_list) {
                    GlobleAddressConfig.selectedDeptIDs.put(bean, ""); // 添加到全选部门中
                }

                Intent intent1 = new Intent(mContext, AddressBookActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("groupId", groupInfoBean.id);
                bundle.putBoolean("isMultiSelect", true);
                intent1.putExtra("bundle", bundle);
                GloableConfig.addressBookType = 1;
                startActivityForResult(intent1, 2001);
                break;
            case 2:
                // TODO: 2016/7/31 创建群组
                Intent intent2 = new Intent(mContext, AddressBookActivity.class);
                startActivity(intent2);
                finish();
                break;

            }

        } else if (v.getId() == R.id.creategroup_rl) {
            // TODO: 2016/7/15 创建群组
        } else if (v.getId() == R.id.select_mumber_confirm_btn) {
            switch (GloableConfig.addressBookType) {
            case 1:
                // TODO: 2016/7/31 加人
                break;
            case 2:
                // TODO: 2016/7/31 创建群组
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
            case 2001:
                setResult(RESULT_OK);
                finish();
                break;
            }
        }
    }
}
