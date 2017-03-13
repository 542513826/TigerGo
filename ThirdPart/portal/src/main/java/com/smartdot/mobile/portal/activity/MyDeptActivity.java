package com.smartdot.mobile.portal.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.utils.CustomToast;
import com.smartdot.mobile.portal.utils.ProgressUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 我的部门 界面
 */
public class MyDeptActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;


    private ImageView title_left_img;

    private TextView title_center_text;

    private TextView title_right_text;

    private ListView dept_lv;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1001) {
                try {
                    ProgressUtil.dismissProgressDialog();
                    JSONObject result = new JSONObject(msg.obj.toString());
                    JSONObject json = result.getJSONObject("returnValueObject");
                    int resultCode = json.getInt("resultCode");
                    if (resultCode == 200) {
                        CustomToast.showToast(mContext, "发布评论成功!");
                        finish();
                        // 此处应该会做ActivityForResult操作
                    } else {
                        CustomToast.showToast(mContext, "您已发布过评论!");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_dept);

        getData();
        initView();
    }

    private void getData() {

    }

    private void initView() {
        mContext = this;
        title_left_img = (ImageView) findViewById(R.id.title_left_img);
        title_center_text = (TextView) findViewById(R.id.title_center_text);
        title_right_text = (TextView) findViewById(R.id.title_right_text);
        dept_lv = (ListView) findViewById(R.id.dept_lv);

        title_center_text.setText("我的部门");

        title_left_img.setVisibility(View.GONE);
        title_right_text.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.cancel_tv) {
            finish();
        } else if (v.getId() == R.id.title_right_text) {

        }
    }
}
