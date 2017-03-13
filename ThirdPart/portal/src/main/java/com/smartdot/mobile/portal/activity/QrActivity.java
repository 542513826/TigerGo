package com.smartdot.mobile.portal.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.utils.DisplayUtil;
import com.smartdot.mobile.portal.utils.StringUtils;
import com.xys.libzxing.zxing.activity.CaptureActivity;
import com.xys.libzxing.zxing.encoding.EncodingUtils;

/**
 * 显示群二维码的界面
 */
public class QrActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;

    private ImageView title_left_img;

    private TextView title_center_text;

    private ImageView title_right_img;

    private ImageView qr_image;

    private TextView qr_scan_result_tv;

    private String targetId;

    PopupWindow mPopupWindow;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API. See
     * https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        mContext = this;
        targetId = getIntent().getStringExtra("targetId");
        initView();

        createQRcode();

    }

    private void initView() {

        title_left_img = (ImageView) findViewById(R.id.title_left_img);
        title_center_text = (TextView) findViewById(R.id.title_center_text);
        title_right_img = (ImageView) findViewById(R.id.title_right_img);
        qr_image = (ImageView) findViewById(R.id.qr_image);
        qr_scan_result_tv = (TextView) findViewById(R.id.qr_scan_result_tv);

        title_center_text.setText(R.string.groupQrcode);
        title_right_img.setVisibility(View.VISIBLE);
        title_right_img.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.btn_setting_point));

        title_right_img.setVisibility(View.GONE);

        title_left_img.setOnClickListener(this);
        title_right_img.setOnClickListener(this);
        qr_scan_result_tv.setOnClickListener(this);
    }

    /**
     * 生成二维码
     */
    private void createQRcode() {
        // 根据字符串生成二维码图片并显示在界面上，第二个参数为图片的大小（350*350）
        Bitmap qrCodeBitmap = EncodingUtils.createQRCode(
                String.format(getResources().getString(R.string.qrcode_value), targetId), 500, 500,
                BitmapFactory.decodeResource(getResources(), R.drawable.btn_userinfo_head));
        qr_image.setImageBitmap(qrCodeBitmap);
    }

    /**
     * 初始化popupwindow
     */
    private void initPopupWindow() {
        Button startChatBtn;
        Button sendMessagesBtn;
        Button scanningBtn;
        View popView = getLayoutInflater().inflate(R.layout.popupwindow_aboutme, null);
        mPopupWindow = new PopupWindow(popView, DisplayUtil.dip2px(mContext, 140), DisplayUtil.dip2px(mContext, 150),
                true);

        startChatBtn = (Button) popView.findViewById(R.id.startChatBtn);
        sendMessagesBtn = (Button) popView.findViewById(R.id.sendMessagesBtn);
        scanningBtn = (Button) popView.findViewById(R.id.scanningBtn);

        startChatBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
                mPopupWindow = null;
                // TODO: 2016/7/14 发起聊天
            }
        });
        sendMessagesBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
                mPopupWindow = null;
                // TODO: 2016/7/14 群发消息
            }
        });

        scanningBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
                mPopupWindow = null;
                Intent openCameraIntent = new Intent(mContext, CaptureActivity.class);
                startActivityForResult(openCameraIntent, 0);
            }
        });
        // 触摸屏幕关闭popupwindow
        popView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mPopupWindow != null && mPopupWindow.isShowing()) {
                    mPopupWindow.dismiss();
                    mPopupWindow = null;
                }
                return false;
            }
        });

    }

    /**
     * 获取PopupWindow实例
     **/
    private void getPopupWindow() {
        if (null != mPopupWindow) {
            mPopupWindow.dismiss();
            return;
        } else {
            initPopupWindow();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.title_left_img) {
            finish();
        } else if (v.getId() == R.id.title_right_img) {
            getPopupWindow();
            // 这里是位置显示方式
            mPopupWindow.showAsDropDown(title_right_img, title_right_img.getWidth(), 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString("result");
            if (scanResult.contains(getString(R.string.use_mp_scan))) {
                scanResult = scanResult.replace("]", "").replace("\n", "");
                String[] results = scanResult.split(":");
                addGroup(results[1]);
            } else {
                qr_image.setVisibility(View.GONE);
                qr_scan_result_tv.setVisibility(View.VISIBLE);
                if (StringUtils.isUrl(scanResult)) {
                    qr_scan_result_tv.setText(Html.fromHtml("<u>" + scanResult + "</u>"));
                    final String finalScanResult = scanResult;
                    qr_scan_result_tv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.setAction("android.intent.action.VIEW");
                            Uri content_url = Uri.parse(finalScanResult);
                            intent.setData(content_url);
                            startActivity(intent);
                        }
                    });
                } else {
                    qr_scan_result_tv.setText(scanResult);
                }
            }
        }
    }

    private void addGroup(String id) {
        new AlertDialog.Builder(mContext).setTitle(getString(R.string.isAddGroup) + id)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO: 2016/7/14 加入群组
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.can_not_add_yourself, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setCancelable(false).show();
    }
}
