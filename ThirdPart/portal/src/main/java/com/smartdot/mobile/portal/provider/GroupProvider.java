package com.smartdot.mobile.portal.provider;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;

import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.activity.GroupListActivity;

import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.widget.provider.InputProvider;
import io.rong.imlib.RongIMClient;
import io.rong.message.RichContentMessage;

/**
 * Created by Administrator on 2016/7/14.
 */
public class GroupProvider extends InputProvider.ExtendProvider {
    HandlerThread mWorkThread;

    Handler mUploadHandler;

    private int REQUEST_CONTACT = 20;

    private RongContext mContext;

    public GroupProvider(RongContext context) {
        super(context);
        mWorkThread = new HandlerThread("RongDemo");
        mWorkThread.start();
        mUploadHandler = new Handler(mWorkThread.getLooper());
        mContext = context;
    }

    /**
     * 设置展示的图标
     *
     * @param context
     * @return
     */
    @Override
    public Drawable obtainPluginDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.de_contacts);
    }

    /**
     * 设置图标下的title
     *
     * @param context
     * @return
     */
    @Override
    public CharSequence obtainPluginTitle(Context context) {
        return context.getString(R.string.add_card);
    }

    /**
     * click 事件
     *
     * @param view
     */
    @Override
    public void onPluginClick(View view) {
        // TODO: 2016/7/14 点击事件。点击后应跳转到群列表界面
        Intent intent = new Intent(mContext, GroupListActivity.class);
        GloableConfig.groupListType = 1;
        startActivityForResult(intent, REQUEST_CONTACT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK)
            return;

        String groupid = data.getStringExtra("groupid");
        String groupname = data.getStringExtra("groupname");
        String groupurl = data.getStringExtra("groupurl");

        mUploadHandler.post(new MyRunnable(groupid, groupname, groupurl));

        super.onActivityResult(requestCode, resultCode, data);
    }

    class MyRunnable implements Runnable {

        String groupid;

        String groupname;

        String groupimageurl;

        public MyRunnable(String groupid, String groupname, String groupimageurl) {
            this.groupid = groupid;
            this.groupname = groupname;
            this.groupimageurl = groupimageurl;
        }

        @Override
        public void run() {

            String showMessage = groupname + "\n" + "点击加入该群参与群组讨论";
            final RichContentMessage groupcard = RichContentMessage.obtain("群名片", showMessage, groupimageurl,"group://" + groupid);
            groupcard.setExtra(groupid);
            RongIM.getInstance().getRongIMClient().sendMessage(getCurrentConversation().getConversationType(),
                    getCurrentConversation().getTargetId(), groupcard, null, null,
                    new RongIMClient.SendMessageCallback() {

                        @Override

                        public void onError(Integer messageId, RongIMClient.ErrorCode e) {

                        }

                        @Override

                        public void onSuccess(Integer integer) {

                        }

                    });
        }
    }
}
