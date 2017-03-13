package com.smartdot.mobile.portal.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.smartdot.mobile.portal.R;

/**
 * Created by Monkey on 2015/6/29.
 */
public class ShopRecyclerViewHolder extends RecyclerView.ViewHolder {

    public ImageView mImageView;

    public ShopRecyclerViewHolder(View itemView) {
        super(itemView);
        mImageView = (ImageView) itemView.findViewById(R.id.shop_detail_iv);
    }
}
