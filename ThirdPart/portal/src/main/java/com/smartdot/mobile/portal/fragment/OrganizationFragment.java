package com.smartdot.mobile.portal.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.activity.AddressBookActivity;
import com.smartdot.mobile.portal.activity.GroupListActivity;
import com.smartdot.mobile.portal.activity.OrganizationActivity;
import com.smartdot.mobile.portal.activity.SearchActivity;
import com.smartdot.mobile.portal.adapter.OrganizationListAdapter;
import com.smartdot.mobile.portal.bean.UserInfoBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 底部tab导航 - 消息
 */
public class OrganizationFragment extends Fragment implements View.OnClickListener {

    private Context mContext;

    OrganizationListAdapter mAdapter;

    private ListView common_contant_lv;

    private List<UserInfoBean> mList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organization, null);
        mContext = getActivity();
        initView(view);
        return view;
    }

    private void initView(View view) {
        ImageView title_left_img = (ImageView) view.findViewById(R.id.title_left_img);
        TextView title_center_text = (TextView) view.findViewById(R.id.title_center_text);
        RelativeLayout search_rl = (RelativeLayout) view.findViewById(R.id.search_rl);
        ImageView title_right_img = (ImageView) view.findViewById(R.id.title_right_img);
        TextView title_right_text = (TextView) view.findViewById(R.id.title_right_text);
        RelativeLayout public_rl = (RelativeLayout) view.findViewById(R.id.public_rl);
        ImageView public_img = (ImageView) view.findViewById(R.id.public_img);
        ImageView mydept_img = (ImageView) view.findViewById(R.id.mydept_img);
        RelativeLayout mydept_rl = (RelativeLayout) view.findViewById(R.id.mydept_rl);
        ImageView mygroup_img = (ImageView) view.findViewById(R.id.mygroup_img);
        TextView mygroup_tv = (TextView) view.findViewById(R.id.mygroup_tv);
        RelativeLayout mygroup_rl = (RelativeLayout) view.findViewById(R.id.mygroup_rl);
        ImageView ic_organization_img = (ImageView) view.findViewById(R.id.ic_organization_img);
        RelativeLayout organization_rl = (RelativeLayout) view.findViewById(R.id.organization_rl);
        ImageView ic_creategroup_img = (ImageView) view.findViewById(R.id.ic_creategroup_img);
        RelativeLayout creategroup_rl = (RelativeLayout) view.findViewById(R.id.creategroup_rl);
        common_contant_lv = (ListView) view.findViewById(R.id.common_contant_lv);

        title_center_text.setText(R.string.addressbook);
        title_left_img.setVisibility(View.GONE);
        title_right_img.setVisibility(View.GONE);

        mList = new ArrayList<>();
        mAdapter = new OrganizationListAdapter(getActivity(), mList, R.layout.item_common_contact);
        common_contant_lv.setAdapter(mAdapter);

        title_left_img.setOnClickListener(this);
        title_right_img.setOnClickListener(this);
        search_rl.setOnClickListener(this);
        mydept_rl.setOnClickListener(this);
        mygroup_rl.setOnClickListener(this);
        public_rl.setOnClickListener(this);
        organization_rl.setOnClickListener(this);
        creategroup_rl.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.search_rl) {
            // 搜索
            Intent intent = new Intent(mContext, SearchActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.mydept_rl) {
            // TODO: 2016/7/15 我的部门
            GloableConfig.addressBookType = 0;
            Intent intent1 = new Intent(mContext, AddressBookActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("deptId", GloableConfig.myUserInfo.obey_dept_id);
            intent1.putExtra("bundle", bundle);
            startActivity(intent1);
        } else if (v.getId() == R.id.mygroup_rl) {
            // TODO: 2016/7/15 我的群组
            Intent intent = new Intent(mContext, GroupListActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.public_rl) {
            // TODO: 2016/7/15 应用号
        } else if (v.getId() == R.id.organization_rl) {
            // TODO: 2016/7/15 组织机构
            Intent intent = new Intent(mContext, AddressBookActivity.class);
            GloableConfig.addressBookType = 0;
            startActivity(intent);
        } else if (v.getId() == R.id.creategroup_rl) {
            // TODO: 2016/7/15 创建群组
            Intent intent = new Intent(mContext, OrganizationActivity.class);
            GloableConfig.addressBookType = 2;
            startActivity(intent);
        }
        ((Activity) mContext).overridePendingTransition(R.anim.base_slide_right_in, R.anim.base_slide_right_out);// 进场动画
    }
}
