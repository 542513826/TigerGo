package com.smartdot.mobile.thirdpart.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import com.smartdot.mobile.thirdpart.R;

import cn.jpush.android.api.JPushInterface;

public class StartActivity extends Activity {

    public ImageView start_img;

    private Thread thread;

    private int time = 0;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == 0) {
                Intent intent;
                intent = new Intent(StartActivity.this, AppLoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(com.smartdot.mobile.portal.R.anim.base_slide_right_in, com.smartdot.mobile.portal.R.anim.base_slide_right_out);// 进场动画
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        initView();
        initThread();
        thread.start();
    }

    private void initView() {
        start_img = (ImageView) findViewById(R.id.start_img);
    }

    public void initThread() {
        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (time >= 0) {
                    try {
                        Thread.sleep(2000);// 停留2秒
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    time--;
                }
                handler.sendEmptyMessage(0);
            }
        });
    }

    @Override
    protected void onResume() {
        JPushInterface.onResume(this); // 极光统计
        super.onResume();
    }

    @Override
    protected void onPause() {
        JPushInterface.onPause(this); // 极光统计
        super.onPause();
    }
}
