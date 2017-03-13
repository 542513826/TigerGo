package com.smartdot.mobile.portal.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.bean.UserInfoBean;
import com.smartdot.mobile.portal.utils.CommonUtil;
import com.smartdot.mobile.portal.utils.DisplayUtil;
import com.smartdot.mobile.portal.utils.PhotoUtils;
import com.smartdot.mobile.portal.utils.ProgressUtil;
import com.smartdot.mobile.portal.utils.VolleyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * 个人信息的Activity Created by zhangt on 2016/7/27.
 */
public class UserInfoActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;

    private TextView cancel_tv;

    /** 返回按钮 */
    private ImageView title_left_img;

    /** 标题 */
    private TextView title_center_text;

    private TextView title_right_text;

    /** 头像 */
    private ImageView userinfo_head_img;

    /** 头像右边小箭头 */
    private ImageView back_iv;

    /** 姓名 */
    private TextView userinfo_name_tv;

    /** 个性签名 */
    private TextView userinfo_signature_tv;

    /** 职务 */
    private TextView userinfo_position_tv;

    /** 部门 */
    private TextView userinfo_dept_tv;

    /** 手机 */
    private TextView userinfo_tel_tv;

    private ImageView mobil_iv;

    private TextView userinfo_phone_tv;

    private ImageView tel_iv;

    private TextView userinfo_email_tv;

    private ImageView email_iv;

    private String userId;

    private UserInfoBean userInfoBean;

    private PopupWindow mPopupWindow;

    private PhotoUtils photoUtils;

    private Uri selectUri;

    private String photo_path;

    private Bitmap scanBitmap;

    private Button selectPictureBtn;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case 1001:
                try {
                    ProgressUtil.dismissProgressDialog();
                    JSONObject jsonObject = new JSONObject(msg.obj.toString());
                    userInfoBean = CommonUtil.gson.fromJson(jsonObject.getString("user"), UserInfoBean.class);
                    userinfo_name_tv.setText(userInfoBean.userName);
                    userinfo_tel_tv.setText(userInfoBean.mobile + "");
                    userinfo_email_tv.setText(userInfoBean.email);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        mContext = this;
        userId = GloableConfig.myUserInfo.userId;
        initView();
        setPortraitChangeListener();
        getData();
    }

    private void initView() {
        cancel_tv = (TextView) findViewById(R.id.cancel_tv);
        title_left_img = (ImageView) findViewById(R.id.title_left_img);
        title_center_text = (TextView) findViewById(R.id.title_center_text);
        title_right_text = (TextView) findViewById(R.id.title_right_text);
        userinfo_head_img = (ImageView) findViewById(R.id.userinfo_head_img);
        back_iv = (ImageView) findViewById(R.id.back_iv);
        userinfo_name_tv = (TextView) findViewById(R.id.userinfo_name_tv);
        userinfo_signature_tv = (TextView) findViewById(R.id.userinfo_signature_tv);
        userinfo_position_tv = (TextView) findViewById(R.id.userinfo_position_tv);
        userinfo_dept_tv = (TextView) findViewById(R.id.userinfo_dept_tv);
        userinfo_tel_tv = (TextView) findViewById(R.id.userinfo_tel_tv);
        mobil_iv = (ImageView) findViewById(R.id.mobil_iv);
        userinfo_phone_tv = (TextView) findViewById(R.id.userinfo_phone_tv);
        tel_iv = (ImageView) findViewById(R.id.tel_iv);
        userinfo_email_tv = (TextView) findViewById(R.id.userinfo_email_tv);
        email_iv = (ImageView) findViewById(R.id.email_iv);

        title_center_text.setText(R.string.user_info);

        title_right_text.setVisibility(View.GONE);

        title_left_img.setOnClickListener(this);
        userinfo_head_img.setOnClickListener(this);
    }

    /**
     * 设置图片选取监听
     */
    private void setPortraitChangeListener() {
        photoUtils = new PhotoUtils(mContext, new PhotoUtils.OnPhotoResultListener() {
            @Override
            public void onPhotoResult(Uri uri) {
                if (uri != null && !TextUtils.isEmpty(uri.getPath())) {
                    selectUri = uri;
                    if (TextUtils.isEmpty(selectUri.toString())) {
                        System.out.println("图片为空");
                    }
                    try {
                        Bitmap tmpBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                        Bitmap circleBitmap = PhotoUtils.getRoundedCornerBitmap(tmpBitmap,18f);
                        userinfo_head_img.setImageBitmap(circleBitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onPhotoCancel() {
                System.out.println("onPhotoCancel()");
            }
        });
    }

    /**
     * 请求网络数据
     */
    private void getData() {
        ProgressUtil.showPregressDialog(mContext, R.layout.custom_progress);

        VolleyUtil volleyUtil = new VolleyUtil(mContext);
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        volleyUtil.stringRequest(handler, GloableConfig.UserinfoUrl, map, 1001);
    }

    /**
     * 初始化popupwindow
     */
    private void initPopupWindow() {
        Button takePicBtn;
        Button selectBtn;
        View popView = getLayoutInflater().inflate(R.layout.popupwindow_chooseimg, null);
        takePicBtn = (Button) popView.findViewById(R.id.pop_tackpic);
        selectBtn = (Button) popView.findViewById(R.id.pop_selectpic);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popView.setTranslationZ(10);
        }
        mPopupWindow = new PopupWindow(popView, FrameLayout.LayoutParams.MATCH_PARENT,
                DisplayUtil.dip2px(mContext, 120), true);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        // 添加pop窗口关闭事件
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1f);
            }
        });
        takePicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoUtils.takePicture(UserInfoActivity.this);
                mPopupWindow.dismiss();
            }
        });
        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoUtils.selectPicture(UserInfoActivity.this);
                mPopupWindow.dismiss();
            }
        });
    }

    /**
     * 获取PopupWindow实例
     **/
    private void getPopupWindow() {
        backgroundAlpha(0.5f);
        if (null != mPopupWindow) {
            mPopupWindow.dismiss();
            return;
        } else {
            initPopupWindow();
        }
    }

    /**
     * 设置添加屏幕的背景透明度
     * 
     * @param bgAlpha
     */
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha; // 0.0-1.0
        getWindow().setAttributes(lp);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.title_left_img) {
            finish();
        } else if (v.getId() == R.id.userinfo_head_img) {
            // TODO: 2016/8/22 修改头像
            getPopupWindow();
            // 这里是popwindow位置显示方式
            mPopupWindow.showAtLocation(findViewById(R.id.userinfo_ll), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0,
                    0); // 设置layout在PopupWindow中显示的位置

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
            case PhotoUtils.INTENT_SELECT:
            case PhotoUtils.INTENT_TAKE:
            case PhotoUtils.INTENT_CROP:
                photoUtils.onActivityResult(UserInfoActivity.this, requestCode, resultCode, data);
                break;
            default:
                break;
            }

        }
    }

}
