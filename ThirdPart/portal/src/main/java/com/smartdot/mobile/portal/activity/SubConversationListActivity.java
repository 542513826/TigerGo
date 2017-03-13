package com.smartdot.mobile.portal.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.adapter.SubConversationListAdapterEx;

import io.rong.imkit.RongContext;
import io.rong.imkit.fragment.SubConversationListFragment;

/**
 * 聚合回话列表界面（暂时没有用到）
 */
public class SubConversationListActivity extends FragmentActivity {

    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_conversation_list);

        initView();

        SubConversationListFragment fragment = new SubConversationListFragment();
        fragment.setAdapter(new SubConversationListAdapterEx(RongContext.getInstance()));
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.rong_content, fragment);
        transaction.commit();

        Intent intent = getIntent();
        // 聚合会话参数
        String type = intent.getData().getQueryParameter("type");

        if (type == null)
            return;

        if (type.equals("group")) {
            title.setText(R.string.de_actionbar_sub_group);
        } else if (type.equals("private")) {
            title.setText(R.string.de_actionbar_sub_private);
        } else if (type.equals("discussion")) {
            title.setText(R.string.de_actionbar_sub_discussion);
        } else if (type.equals("system")) {
            title.setText(R.string.de_actionbar_sub_system);
        } else {
            title.setText(R.string.de_actionbar_sub_defult);
        }
    }

    private void initView() {
        title = (TextView) findViewById(R.id.title_center_text);
    }
}
