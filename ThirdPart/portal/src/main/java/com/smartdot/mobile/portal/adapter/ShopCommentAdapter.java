package com.smartdot.mobile.portal.adapter;

import android.content.Context;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.bean.UserCommentBean;
import com.smartdot.mobile.portal.utils.ImageLoaderUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 商店应用详情评论列表adapter
 */
public class ShopCommentAdapter extends CommonAdapter<UserCommentBean> {
    private List<UserCommentBean> items = new ArrayList<UserCommentBean>();

    private DisplayImageOptions options;

    private Context mContext;

    public ShopCommentAdapter(Context context, List<UserCommentBean> items, int itemLayoutResId) {
        super(context, items, itemLayoutResId);
        this.items = items;
        this.mContext = context;
        options = ImageLoaderUtils.initOptions();
    }

    @Override
    public void convert(CommonViewHolder viewHolder, UserCommentBean bean) {

        int position = viewHolder.getPosition();
        // 评论标题
        viewHolder.setText(R.id.item_comment_tv, (position + 1) + "、" + bean.commentTitle);
        // 评论作者
        viewHolder.setText(R.id.item_commen_author_tv, bean.commentUserName);
        // 评论时间
        viewHolder.setText(R.id.item_commen_date, bean.commentDate);
        // 评论内容
        viewHolder.setText(R.id.item_commen_value, "" + bean.commentValue);
        // 评论星级
        ((RatingBar) viewHolder.getView(R.id.item_commen_rb)).setRating(Float.parseFloat(bean.commentStar));

        // 作者评论的招牌字 只有第一行显示
        if (position == 0) {
            ((TextView) viewHolder.getView(R.id.item_comment_logo)).setVisibility(View.VISIBLE);
        } else {
            ((TextView) viewHolder.getView(R.id.item_comment_logo)).setVisibility(View.GONE);
        }
        // 最后一条评论的底部圆角图
        if (position == items.size()) {
            ((TextView) viewHolder.getView(R.id.item_comment_line)).setVisibility(View.VISIBLE);
        } else {
            ((TextView) viewHolder.getView(R.id.item_comment_line)).setVisibility(View.GONE);
        }

    }
}
