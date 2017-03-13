package com.smartdot.mobile.portal.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.utils.CustomToast;
import com.smartdot.mobile.portal.utils.NetUtils;
import com.smartdot.mobile.portal.utils.ProgressUtil;
import com.smartdot.mobile.portal.utils.StringUtils;
import com.smartdot.mobile.portal.utils.VolleyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 评论activity Created by Administrator on 2016/7/26.
 */
public class EaluationActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;

    private TextView cancel_tv;

    private ImageView title_left_img;

    private TextView title_center_text;

    private TextView title_right_text;

    private RatingBar ratingBar;

    /**
     * app id
     */
    private String app_id;

    /**
     * 评论标题
     */
    private String commentTitle;

    /**
     * 评价内容
     */
    private String comment;

    /**
     * 评价星级
     */
    private String gradeValue;

    private EditText comment_title_ed;

    private EditText comment_valu_ed;

    private String comment_title;

    private String comment_value;

    private String appId;

    private String versionId;

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
        setContentView(R.layout.activity_shop_evaluation);

        getData();
        initView();
    }

    private void getData() {
        Intent intent = getIntent();
        appId = intent.getStringExtra("appId");
        versionId = intent.getStringExtra("versionId");
    }

    private void initView() {
        mContext = this;
        cancel_tv = (TextView) findViewById(R.id.cancel_tv);
        title_left_img = (ImageView) findViewById(R.id.title_left_img);
        title_center_text = (TextView) findViewById(R.id.title_center_text);
        title_right_text = (TextView) findViewById(R.id.title_right_text);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        comment_title_ed = (EditText) findViewById(R.id.comment_title_ed);
        comment_valu_ed = (EditText) findViewById(R.id.comment_valu_ed);

        title_center_text.setText(R.string.evaluation_title);
        title_right_text.setText(R.string.submit);

        title_left_img.setVisibility(View.GONE);
        cancel_tv.setVisibility(View.VISIBLE);
        title_right_text.setOnClickListener(this);
        cancel_tv.setOnClickListener(this);
    }

    /**
     * 提交评价
     */
    private void submit() {
        VolleyUtil volleyUtil = new VolleyUtil(mContext);
        Map<String, String> map = new HashMap<>();
        map.put("appId", appId);
        map.put("versionId", versionId);
        map.put("userId", GloableConfig.myUserInfo.userId);
        map.put("userName", GloableConfig.myUserInfo.userName);
        map.put("deptCode", GloableConfig.myUserInfo.obey_dept_id);
        map.put("commentTitle", comment_title_ed.getText().toString().trim());
        map.put("comment", comment_valu_ed.getText().toString().trim());
        map.put("gradeValue", ratingBar.getRating() + "");
        volleyUtil.stringRequest(handler, GloableConfig.AppCommentUrl, map, 1001);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.cancel_tv) {
            finish();
            overridePendingTransition(R.anim.base_back_in, R.anim.base_back_out);// 退场动画
        } else if (v.getId() == R.id.title_right_text) {
            // TODO: 2016/7/26 提交评价

            if (ratingBar.getRating() == 0.0) {
                CustomToast.showToast(EaluationActivity.this, "请您评论应用星级");
                return;
            } else if (StringUtils.isNull(comment_title_ed.getText().toString())) {
                CustomToast.showToast(EaluationActivity.this, "请您编写评价标题");
                return;
            } else if (!StringUtils.isUserName(comment_title_ed.getText().toString())) {
                CustomToast.showToast(EaluationActivity.this, "您的标题中包含特殊字符请重新输入");
                return;
            } else if (!StringUtils.isNull(comment_valu_ed.getText().toString())) {
                if (StringUtils.isUserName(comment_valu_ed.getText().toString())) {
                    CustomToast.showToast(EaluationActivity.this, "您的内容中包含特殊字符请重新输入");
                    return;
                }
                return;
            }

            if (NetUtils.isConnected(mContext)) {
                ProgressUtil.showPregressDialog(mContext, R.layout.custom_progress);
                submit();
            } else {
                CustomToast.showToast(mContext, getString(R.string.net_error));
            }
        }
    }
}
