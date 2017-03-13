package com.smartdot.mobile.portal.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.bean.AppPicUrlBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Monkey on 2015/6/29.
 */
public class ShopRecyclerViewAdapter extends RecyclerView.Adapter<ShopRecyclerViewHolder> {


    public Context mContext;
    public List<AppPicUrlBean> picList = new ArrayList<>();
    DisplayImageOptions options = new DisplayImageOptions.Builder().build();

    public ShopRecyclerViewAdapter(Context mContext,List<AppPicUrlBean> picList) {
        this.mContext = mContext;
        this.picList = picList;
    }

    /**
     * 创建ViewHolder
     */
    @Override
    public ShopRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(mContext).inflate(R.layout.item_shop_recycleview, parent, false);
        ShopRecyclerViewHolder mViewHolder = new ShopRecyclerViewHolder(mView);
        return mViewHolder;
    }

    /**
     * 绑定ViewHoler，给item中的控件设置数据
     */
    @Override
    public void onBindViewHolder(final ShopRecyclerViewHolder holder, final int position) {
//        if (mOnItemClickListener != null) {
//            holder.itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mOnItemClickListener.onItemClick(holder.itemView, position);
//                }
//            });
//
//            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    mOnItemClickListener.onItemLongClick(holder.itemView, position);
//                    return true;
//                }
//            });
//
//        }
        ImageLoader.getInstance().displayImage(picList.get(position).pic_url,
                holder.mImageView, options);
    }

    @Override
    public int getItemCount() {
        return picList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    public OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setPicList(List<AppPicUrlBean> picList) {
        this.picList = picList;
    }
}
