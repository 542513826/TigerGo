package com.smartdot.mobile.portal.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.abconstant.GloableConfig;
import com.smartdot.mobile.portal.bean.AppDetailBean;
import com.smartdot.mobile.portal.port.OnShopAppRefreshListener;
import com.smartdot.mobile.portal.utils.CustomToast;
import com.smartdot.mobile.portal.utils.ImageLoaderUtils;
import com.smartdot.mobile.portal.utils.VolleyUtil;
import com.smartdot.mobile.portal.utils.openAppManage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商店列表adapter
 */
public class SearchAdapter extends CommonAdapter<AppDetailBean> {
    private List<AppDetailBean> items = new ArrayList<AppDetailBean>();

    private DisplayImageOptions options;

    private Context mContext;

    public SearchAdapter(Context context, List<AppDetailBean> items, int itemLayoutResId) {
        super(context, items, itemLayoutResId);
        this.items = items;
        this.mContext = context;
        options = ImageLoaderUtils.initOptions();
    }

    @Override
    public void convert(CommonViewHolder viewHolder, AppDetailBean bean) {
        final int position = viewHolder.getPosition();

        viewHolder.setText(R.id.app_name_tv, bean.app_name.trim());
        viewHolder.setText(R.id.app_type_tv, bean.app_type.trim());
        viewHolder.setText(R.id.shop_comment_number_tv, "( " + bean.app_markNum + " )");

        // 显示星级
        ((RatingBar) viewHolder.getView(R.id.ratingBar)).setRating(bean.app_startNum);

        // 加载应用图标
        ImageView imageView = (ImageView) viewHolder.getView(R.id.app_icon_iv);
        ImageLoader.getInstance().displayImage(bean.app_icon, imageView, options);

        // 根据不同的安装状态显示不同的图标
        switch (Integer.parseInt(bean.app_Setup)) {
        case 0:
            viewHolder.setText(R.id.shop_item_open_tv, "打开");
            ((ImageView) viewHolder.getView(R.id.shop_item_open_img))
                    .setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.pic_shop_open));
            break;
        case 1:
            viewHolder.setText(R.id.shop_item_open_tv, "更新");
            ((ImageView) viewHolder.getView(R.id.shop_item_open_img))
                    .setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.pic_shop_updata));
            break;
        case 2:
            viewHolder.setText(R.id.shop_item_open_tv, "下载");
            ((ImageView) viewHolder.getView(R.id.shop_item_open_img))
                    .setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.pic_shop_down));
            break;
        }

        // 点击下载/更新/打开的操作
        ((LinearLayout) viewHolder.getView(R.id.shop_item_open)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (Integer.parseInt(getItem(position).app_Setup)) {
                case 0:
                    // TODO: 打开应用逻辑 - 执行打开
                    openAppManage manage1 = new openAppManage(mContext);
                    manage1.openApp(getItem(position));
                    CustomToast.showToast(mContext, "打开应用");
                    break;
                case 1:
                case 2:
                    // TODO: 下载/更新应用逻辑 - 执行下载
                    if (getItem(position).app_web) {
                        sendMessage(mContext, getItem(position), 1);
                    } else {
                        openAppManage manage3 = new openAppManage(mContext);
                        manage3.downloadFile(getItem(position));
                    }
                    break;
                }
            }
        });
    }

    /** 向服务器发送当前应用的安装/卸载状态 */
    public void sendMessage(Context mContext, AppDetailBean data, int state) {
        VolleyUtil volleyUtil = new VolleyUtil(mContext);
        Map<String, String> map = new HashMap<>();
        map.put("userId", GloableConfig.myUserInfo.userId);
        map.put("appId", data.app_id);
        map.put("versionId", data.app_version);
        if (state == 1) {
            volleyUtil.stringRequest(handler, GloableConfig.SetupAppUrl, map, 1001);// 安装
        } else if (state == 2) {
            volleyUtil.stringRequest(handler, GloableConfig.UninstallAppUrl, map, 1001);// 卸载
        }
    }

    /** 接收服务器返回的结果 */
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1001) {
                try {
                    JSONObject result = new JSONObject(msg.obj.toString());
                    JSONObject json = result.getJSONObject("returnValueObject");
                    int resultCode = json.getInt("resultCode");
                    if (resultCode == 200) {
                        // L.v("向服务器发送安装/卸载状态成功");
                        changeListener.OnRefresh();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    OnShopAppRefreshListener changeListener;

    /** 删除轻应用后的回调监听,让外围界面刷新 */
    public void setOnShopAppRefreshListener(OnShopAppRefreshListener changeListener) {
        this.changeListener = changeListener;
    }

}
