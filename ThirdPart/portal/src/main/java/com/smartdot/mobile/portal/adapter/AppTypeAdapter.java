package com.smartdot.mobile.portal.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.bean.AppTypeBean;

import java.util.List;

/**
 * 应用类别的adapter
 * Created by Administrator on 2016/7/28.
 */
public class AppTypeAdapter extends BaseAdapter {

    private Context mContext;

    private List<AppTypeBean> mList;

    public AppTypeAdapter(Context mContext, List<AppTypeBean> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_apptype_layout, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.item_type_name.setText(mList.get(position).type_name);

        return convertView;
    }

    public static class ViewHolder {
        public View rootView;

        public ImageView item_type_img;

        public TextView item_type_name;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.item_type_img = (ImageView) rootView.findViewById(R.id.item_type_img);
            this.item_type_name = (TextView) rootView.findViewById(R.id.item_type_name);
        }

    }

    public void setmList(List<AppTypeBean> mList) {
        this.mList = mList;
    }
}
