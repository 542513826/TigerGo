package com.smartdot.mobile.portal.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.smartdot.mobile.portal.R;

import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerActivity;
import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.QRCodeDecoder;

public class ZXingActivity extends AppCompatActivity implements QRCodeView.Delegate {

    private static final String TAG = "fate";

    private Context context;

    private static final int REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY = 666;

    private QRCodeView mQRCodeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zxing);
        context = this;
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        mQRCodeView = (QRCodeView) findViewById(R.id.zxingview);
        mQRCodeView.setDelegate(this);

        mQRCodeView.startSpotDelay(100); // 开始识别
    }

    @Override
    protected void onStart() {
        super.onStart();
        mQRCodeView.startCamera(); // 打开相机
        mQRCodeView.showScanRect(); // 显示扫描框
    }

    @Override
    protected void onStop() {
        mQRCodeView.stopCamera();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mQRCodeView.onDestroy();
        super.onDestroy();
    }

    /**
     * 震动
     */
    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        // 数据扫描成功
        Log.i(TAG, "result:" + result);

        vibrate();
        if (result.contains("请打开移动门户来扫描本二维码")) {
            // 如果扫描出来是群名片，就直接关闭页面，交由移动门户处理
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("result", result);
            resultIntent.putExtras(bundle);
            this.setResult(RESULT_OK, resultIntent);
            ZXingActivity.this.finish();
        } else {
            Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
        }
        mQRCodeView.startSpot(); // 默认延迟1.5s后开始识别
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Toast.makeText(context, "无法打开相机", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "打开相机出错");
    }

    public void onClick(View v) {
        if (v.getId() == R.id.start_spot) {
            mQRCodeView.startSpotDelay(100); // 开始识别
        } else if (v.getId() == R.id.stop_spot) {
            mQRCodeView.stopSpot(); // 暂停识别
        } else if (v.getId() == R.id.start_spot_showrect) {
            mQRCodeView.startSpotAndShowRect(); // 显示扫描框并扫描
        } else if (v.getId() == R.id.stop_spot_hiddenrect) {
            mQRCodeView.stopSpotAndHiddenRect(); // 隐藏扫描框并暂停扫描
        } else if (v.getId() == R.id.show_rect) {
            mQRCodeView.showScanRect(); // 显示扫描框
        } else if (v.getId() == R.id.hidden_rect) {
            mQRCodeView.hiddenScanRect(); // 隐藏扫描框
        } else if (v.getId() == R.id.start_preview) {
            mQRCodeView.startCamera(); // 开启相机预览
        } else if (v.getId() == R.id.stop_preview) {
            mQRCodeView.stopCamera(); // 关闭相机预览
        } else if (v.getId() == R.id.open_flashlight) {
            mQRCodeView.openFlashlight(); // 打开闪光灯
        } else if (v.getId() == R.id.close_flashlight) {
            mQRCodeView.closeFlashlight(); // 关闭闪光灯
        } else if (v.getId() == R.id.scan_barcode) {
            mQRCodeView.changeToScanBarcodeStyle(); // 转为扫描条码格式
        } else if (v.getId() == R.id.scan_qrcode) {
            mQRCodeView.changeToScanQRCodeStyle(); // 转为扫描二维码格式
        } else if (v.getId() == R.id.choose_qrcde_from_gallery) {
            /*
             * 从相册选取二维码图片，这里为了方便演示，使用的是
             * https://github.com/bingoogolapple/BGAPhotoPicker-Android
             * 这个库来从图库中选择二维码图片，这个库不是必须的，你也可以通过自己的方式从图库中选择图片
             */
            startActivityForResult(BGAPhotoPickerActivity.newIntent(context, null, 1, null, false),
                    REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mQRCodeView.showScanRect();
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY) {
            final String picPath = BGAPhotoPickerActivity.getSelectedImages(data).get(0);

            new AsyncTask<Void, Void, String>() {

                @Override
                protected String doInBackground(Void... voids) {
                    return QRCodeDecoder.syncDecodeQRCode(picPath);
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    if (TextUtils.isEmpty(s)) {
                        Toast.makeText(context, "未发现二维码", Toast.LENGTH_SHORT).show();
                    } else {
                        if (s.contains("请打开移动门户来扫描本二维码")) {
                            // 如果扫描出来是群名片，就直接关闭页面，交由移动门户处理
                            Intent resultIntent = new Intent();
                            Bundle bundle = new Bundle();
                            bundle.putString("result", s);
                            resultIntent.putExtras(bundle);
                            ZXingActivity.this.setResult(RESULT_OK, resultIntent);
                            ZXingActivity.this.finish();
                        } else {
                            Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }.execute();
        }
    }

}
