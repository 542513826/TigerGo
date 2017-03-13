package com.smartdot.mobile.portal.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartdot.mobile.portal.R;
import com.smartdot.mobile.portal.activity.ShopActivity;
import com.smartdot.mobile.portal.bean.AppDetailBean;
import com.smartdot.mobile.portal.drag.OnDragVHListener;
import com.smartdot.mobile.portal.drag.OnItemMoveListener;
import com.smartdot.mobile.portal.utils.BitmapConvertUtil;
import com.smartdot.mobile.portal.utils.CircleDrawable;
import com.smartdot.mobile.portal.utils.ImageLoaderUtils;
import com.smartdot.mobile.portal.utils.L;
import com.smartdot.mobile.portal.utils.MyappUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 拖拽排序 + 增删 Created by YoKeyword on 15/12/28.
 */
public class ChannelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnItemMoveListener {
    // 我的频道
    public static final int TYPE_MY = 1;

    // 我的频道之前的header数量 该demo中 即标题部分 为 1
    private static final int COUNT_PRE_MY_HEADER = 1;

    // 其他频道之前的header数量 该demo中 即标题部分 为 COUNT_PRE_MY_HEADER + 1
    private static final int COUNT_PRE_OTHER_HEADER = COUNT_PRE_MY_HEADER + 1;

    private static final long ANIM_TIME = 360L;

    // touch 点击开始时间
    private long startTime;

    // touch 间隔时间 用于分辨是否是 "点击"
    private static final long SPACE_TIME = 100;

    private LayoutInflater mInflater;

    private ItemTouchHelper mItemTouchHelper;

    // 是否为 编辑 模式
    private boolean isEditMode;

    // 我的频道点击事件
    private OnMyChannelItemClickListener mChannelItemClickListener;

    // 删除图标点击事件
    private OnDeleteItemClickListener mDeleteItemClickListener;

    private List<AppDetailBean> mMyChannelItems, mOtherChannelItems;

    private AppDetailBean mLastAppDtaEntity;

    private Context mContext;

    // 未安装应用使用的Drawable
    private Drawable pic_not_installed;
    // 强制更新应用使用的Drawable
    private Drawable pic_force_update;

    public ChannelAdapter(Context context, ItemTouchHelper helper, List<AppDetailBean> myAppItems) {
        this.mInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.mItemTouchHelper = helper;
        this.mMyChannelItems = myAppItems;
        this.mOtherChannelItems = new ArrayList<AppDetailBean>();
        createErrorPic();
    }

    /**
     * 创建[未安装]和[强制更新]的圆角图片
     */
    private void createErrorPic() {
        Bitmap pic_not_installed_Bitmap = BitmapConvertUtil.drawable2BitmapFromResources(mContext, R.drawable.pic_not_installed);
        pic_not_installed = new CircleDrawable(pic_not_installed_Bitmap, -18);// 之前是-22
        // pic_not_installed.mutate().setAlpha(120);
        Bitmap pic_force_update_Bitmap = BitmapConvertUtil.drawable2BitmapFromResources(mContext,R.drawable.pic_force_update);
        pic_force_update = new CircleDrawable(pic_force_update_Bitmap, -22);
        // pic_force_update.mutate().setAlpha(120);
    }

    @Override
    public int getItemViewType(int position) {
        // if (mFooterView == null) return TYPE_MY;
        if (!hasFooter)
            return TYPE_MY;
        if (position == getItemCount() - 1) {
            return TYPE_FOOTER;
        } else {
            return TYPE_MY;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        final View view;
        switch (viewType) {
        case TYPE_FOOTER:
            view = mInflater.inflate(R.layout.item_app_home_add, null);
            FooterViewHolder footerViewHolder = new FooterViewHolder(view);
            return footerViewHolder;
        case TYPE_MY:
        default:
            view = mInflater.inflate(R.layout.item_app_home, parent, false);
            final MyViewHolder myHolder = new MyViewHolder(view);

            // 长按图标监听
            myHolder.appIcon.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
                    String name = myHolder.name.getText() + "";

                    if (!isEditMode) {
                        if (!name.equals("添加应用")) {
                            RecyclerView recyclerView = ((RecyclerView) parent);
                            startEditMode(recyclerView);
                            mItemTouchHelper.startDrag(myHolder);
                        }
                    }

                    return true;
                }
            });
            // 点击图标监听
            myHolder.appIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    int position = myHolder.getAdapterPosition();
                    if (isEditMode) {
                        // 图标删除
                        // iconMove(position, (RecyclerView) parent, myHolder);
                    } else {
                        // 图标点击
                        mChannelItemClickListener.onItemClick(v, position);
                    }
                }
            });
            // 删除图标监听
            myHolder.imgEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    int position = myHolder.getAdapterPosition();
                    if (isEditMode) {
                        // 图标删除
                        mDeleteItemClickListener.onDeleteItemClick(v, position);
                        iconMove(position, (RecyclerView) parent, myHolder);
                    }
                }
            });
            myHolder.appIcon.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (isEditMode) {
                        switch (MotionEventCompat.getActionMasked(event)) {
                        case MotionEvent.ACTION_DOWN:
                            startTime = System.currentTimeMillis();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (System.currentTimeMillis() - startTime > SPACE_TIME) {
                                mItemTouchHelper.startDrag(myHolder);
                            }
                            break;
                        case MotionEvent.ACTION_CANCEL:
                        case MotionEvent.ACTION_UP:
                            startTime = 0;
                            break;
                        }

                    }
                    return false;
                }
            });
            return myHolder;
        }
    }

    /**
     * 图片移除操作
     */
    private void iconMove(int position, RecyclerView parent, MyViewHolder myHolder) {
        RecyclerView recyclerView = parent;
        View targetView = recyclerView.getLayoutManager().findViewByPosition(mMyChannelItems.size());
        View currentView = recyclerView.getLayoutManager().findViewByPosition(position);
        // 如果targetView不在屏幕内,则indexOfChild为-1
        // 此时不需要添加动画,因为此时notifyItemMoved自带一个向目标移动的动画
        // 如果在屏幕内,则添加一个位移动画
        if (recyclerView.indexOfChild(targetView) >= 0) {
            int targetX, targetY;

            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            int spanCount = ((GridLayoutManager) manager).getSpanCount();

            // 移动后 高度将变化 (我的频道Grid 最后一个item在新的一行第一个)
            if ((mMyChannelItems.size() - COUNT_PRE_MY_HEADER) % spanCount == 0) {
                View preTargetView = recyclerView.getLayoutManager()
                        .findViewByPosition(mMyChannelItems.size() + COUNT_PRE_OTHER_HEADER - 1);
                targetX = preTargetView.getLeft();
                targetY = preTargetView.getTop();
            } else {
                targetX = targetView.getLeft();
                targetY = targetView.getTop();
            }

            moveMyToOther(myHolder, recyclerView);
            startAnimation(recyclerView, currentView, targetX, targetY);

        } else {
            moveMyToOther(myHolder, recyclerView);
        }
    }

    @Override
    public int getItemCount() {
        return hasFooter ? mMyChannelItems.size() + 1 : mMyChannelItems.size();
    }

    /**
     * 开始增删动画
     */
    private void startAnimation(RecyclerView recyclerView, final View currentView, float targetX, float targetY) {
        final ViewGroup viewGroup = (ViewGroup) recyclerView.getParent();
        final ImageView mirrorView = addMirrorView(viewGroup, recyclerView, currentView);

        Animation animation = getTranslateAnimator(targetX - currentView.getLeft(), targetY - currentView.getTop());
        currentView.setVisibility(View.INVISIBLE);
        mirrorView.startAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                viewGroup.removeView(mirrorView);
                if (currentView.getVisibility() == View.INVISIBLE) {
                    currentView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 我的频道 移动到 其他频道
     *
     * @param myHolder
     * @param recyclerView
     */
    private void moveMyToOther(MyViewHolder myHolder, RecyclerView recyclerView) {
        int position = myHolder.getAdapterPosition();
        int startPosition = position;
        mMyChannelItems.remove(startPosition);
        cancelEditMode(recyclerView);
    }

    /**
     * 其他频道 移动到 我的频道
     *
     * @param otherHolder
     */
    private void moveOtherToMy(OtherViewHolder otherHolder) {
        int position = processItemRemoveAdd(otherHolder);
        if (position == -1) {
            return;
        }
        notifyItemMoved(position, mMyChannelItems.size() - 1 + COUNT_PRE_MY_HEADER);
    }

    /**
     * 其他频道 移动到 我的频道 伴随延迟
     *
     * @param otherHolder
     */
    private void moveOtherToMyWithDelay(OtherViewHolder otherHolder) {
        final int position = processItemRemoveAdd(otherHolder);
        if (position == -1) {
            return;
        }
        delayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                notifyItemMoved(position, mMyChannelItems.size() - 1 + COUNT_PRE_MY_HEADER);
            }
        }, ANIM_TIME);
    }

    private Handler delayHandler = new Handler();

    /**
     * 获取当前正在拖动的图标的position
     */
    private int processItemRemoveAdd(OtherViewHolder otherHolder) {
        int position = otherHolder.getAdapterPosition();

        int startPosition = position - mMyChannelItems.size() - COUNT_PRE_OTHER_HEADER;
        if (startPosition > mOtherChannelItems.size() - 1) {
            return -1;
        }
        AppDetailBean item = mOtherChannelItems.get(startPosition);
        mOtherChannelItems.remove(startPosition);
        mMyChannelItems.add(item);
        return position;
    }

    /**
     * 添加需要移动的 镜像View
     */
    private ImageView addMirrorView(ViewGroup parent, RecyclerView recyclerView, View view) {
        /**
         * 我们要获取cache首先要通过setDrawingCacheEnable方法开启cache，
         * 然后再调用getDrawingCache方法就可以获得view的cache图片了。
         * buildDrawingCache方法可以不用调用，因为调用getDrawingCache方法时，若果cache没有建立，
         * 系统会自动调用buildDrawingCache方法生成cache。 若想更新cache,
         * 必须要调用destoryDrawingCache方法把旧的cache销毁，才能建立新的。
         * 当调用setDrawingCacheEnabled方法设置为false, 系统也会自动把原来的cache销毁。
         */
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(true);
        final ImageView mirrorView = new ImageView(recyclerView.getContext());
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        mirrorView.setImageBitmap(bitmap);
        view.setDrawingCacheEnabled(false);
        int[] locations = new int[2];
        view.getLocationOnScreen(locations);
        int[] parenLocations = new int[2];
        recyclerView.getLocationOnScreen(parenLocations);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(bitmap.getWidth(), bitmap.getHeight());
        params.setMargins(locations[0], locations[1] - parenLocations[1], 0, 0);
        parent.addView(mirrorView, params);

        return mirrorView;
    }

    /**
     * item移动过程
     *
     * @param fromPosition
     *            起始点
     * @param toPosition
     *            到达点
     */
    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        L.v("当前的移动坐标起点-->" + fromPosition);
        L.v("当前的移动坐标终点-->" + toPosition);
        try {
            AppDetailBean item = mMyChannelItems.get(fromPosition);
            mMyChannelItems.remove(fromPosition);
            mMyChannelItems.add(toPosition, item);
            notifyItemMoved(fromPosition, toPosition);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            notifyDataSetChanged();
        }
    }

    /**
     * 开启编辑模式
     *
     * @param parent
     */
    public void startEditMode(RecyclerView parent) {
        isEditMode = true;
        // 把[添加应用]从集合中删掉
        removeFooterView();

        int visibleChildCount = parent.getChildCount();
        for (int i = 0; i < visibleChildCount - 1; i++) {
            View view = parent.getChildAt(i);
            ImageView imgEdit = (ImageView) view.findViewById(R.id.img_edit);
            RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.app_home_layout);
            if (imgEdit != null) {
                imgEdit.setVisibility(View.VISIBLE);
            }
            // 对[不可删除]的应用做隐藏编辑图标处理
            AppDetailBean data = mMyChannelItems.get(i);
            if (data.isDelete) {
                imgEdit.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 完成编辑模式
     *
     * @param parent
     */
    public void cancelEditMode(RecyclerView parent) {
        if (isEditMode) {
            isEditMode = false;
            int visibleChildCount = parent.getChildCount();
            for (int i = 0; i < visibleChildCount; i++) {
                View view = parent.getChildAt(i);
                ImageView imgEdit = (ImageView) view.findViewById(R.id.img_edit);
                if (imgEdit != null) {
                    imgEdit.setVisibility(View.INVISIBLE);
                }
            }
            setFooterView();
            notifyDataSetChanged();// 改变布局后重新刷新数据
        }
    }

    /**
     * 是否处于编辑模式
     *
     * @return true为处于
     */
    public boolean isEditMode() {
        return isEditMode;
    }

    // 给[添加]做的footerView
    public static final int TYPE_FOOTER = 4;

    private boolean hasFooter;

    private void setFooterView() {
        if (!hasFooter) {
            hasFooter = true;
            notifyItemInserted(getItemCount() - 1);
        }
    }

    /** 设置页脚 用来add[添加]的item */
    public void setFooterView(boolean hasFooter) {
        // this.mFooterView = footerView;
        this.hasFooter = hasFooter;
        if (hasFooter) {
            notifyItemInserted(getItemCount() - 1);
        }
    }

    /** 删除页脚 在进入编辑模式时删除 */
    private void removeFooterView() {
        if (hasFooter) {
            notifyItemRemoved(getItemCount() - 1);
            hasFooter = false;
        }
    }

    /** 初始化[添加应用]的view */
    class FooterViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ImageView appIcon;

        public FooterViewHolder(View itemView) {
            super(itemView);
            appIcon = (ImageView) itemView.findViewById(R.id.iv);
            appIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.btn_home_add));
            textView = (TextView) itemView.findViewById(R.id.tv);
            textView.setText("添加应用");
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ShopActivity.class);
                    mContext.startActivity(intent);
                    ((Activity)mContext).overridePendingTransition(R.anim.base_slide_right_in, R.anim.base_slide_right_out);
                }
            });
        }
    }

    /**
     * 获取位移动画
     */
    private TranslateAnimation getTranslateAnimator(float targetX, float targetY) {
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,
                Animation.ABSOLUTE, targetX, Animation.RELATIVE_TO_SELF, 0f, Animation.ABSOLUTE, targetY);
        // RecyclerView默认移动动画250ms 这里设置360ms 是为了防止在位移动画结束后 remove(view)过早
        // 导致闪烁
        translateAnimation.setDuration(ANIM_TIME);
        translateAnimation.setFillAfter(true);
        return translateAnimation;
    }

    public interface OnMyChannelItemClickListener {
        void onItemClick(View v, int position);
    }

    public void setOnMyChannelItemClickListener(OnMyChannelItemClickListener listener) {
        this.mChannelItemClickListener = listener;
    }

    public interface OnDeleteItemClickListener {
        void onDeleteItemClick(View v, int position);
    }

    public void setOnDeleteItemItemClickListener(OnDeleteItemClickListener listener) {
        this.mDeleteItemClickListener = listener;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FooterViewHolder) {
        }
        if (holder instanceof MyViewHolder) {
            // 填充数据 如果想要不同类型的布局 参考demo
            MyViewHolder myHolder = (MyViewHolder) holder;
            AppDetailBean data = mMyChannelItems.get(position);
            myHolder.name.setText(data.app_name);
            // 判断是否处于编辑状态
            if (isEditMode) {
                myHolder.imgEdit.setVisibility(View.VISIBLE);
            } else {
                myHolder.imgEdit.setVisibility(View.INVISIBLE);
            }
            // 判断是否属于添加图标
            if (data.app_icon != null) {
                ImageLoaderUtils.loadImage(data.app_icon, myHolder.appIcon);
            } else {
                myHolder.appIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.btn_home_add));
            }
            // 判断应用是否安装
            if (!MyappUtil.hasAppInstalled(mContext, data.app_start)) {
                /** 当应用并未在本机安装就会显示未安装的标识 */
                myHolder.appIcon_null_installation.setImageDrawable(pic_not_installed);
                myHolder.appIcon_null_installation.setVisibility(View.VISIBLE);
                // 如果是轻应用就不用加了
                if (data.app_web) {
                    myHolder.appIcon_null_installation.setVisibility(View.GONE);
                }
            } else if (data.app_forced) {// 判断应用是否强制更新
                /** 当应用需要强制更新就会显示未安装的标识 */
                myHolder.appIcon_null_installation.setImageDrawable(pic_force_update);
                myHolder.appIcon_null_installation.setVisibility(View.VISIBLE);
                // 如果是轻应用就不用加了
                if (data.app_web) {
                    myHolder.appIcon_null_installation.setVisibility(View.GONE);
                }
            } else {
                myHolder.appIcon_null_installation.setVisibility(View.GONE);
            }

        }

    }

    class MyViewHolder extends RecyclerView.ViewHolder implements OnDragVHListener {
        private TextView name;

        private ImageView appIcon;

        private ImageView appIcon_null_installation;

        private ImageView imgEdit;

        private RelativeLayout app_home_layout;

        public MyViewHolder(View itemView) {
            super(itemView);
            appIcon = (ImageView) itemView.findViewById(R.id.iv);
            name = (TextView) itemView.findViewById(R.id.tv);
            imgEdit = (ImageView) itemView.findViewById(R.id.img_edit);
            appIcon_null_installation = (ImageView) itemView.findViewById(R.id.iv_null_installation);
            app_home_layout = (RelativeLayout) itemView.findViewById(R.id.app_home_layout);
        }

        /**
         * item 被选中时
         */
        @Override
        public void onItemSelected() {
            app_home_layout.setBackgroundResource(R.drawable.bg_drag_icon_pressed);
        }

        /**
         * item 取消选中时
         */
        @Override
        public void onItemFinish() {
            app_home_layout.setBackgroundResource(R.drawable.bg_drag_icon_normal);
        }
    }

    /**
     * 其他频道
     */
    class OtherViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;

        public OtherViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.tv);
        }
    }

    /**
     * 我的频道 标题部分
     */
    class MyChannelHeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvBtnEdit;

        public MyChannelHeaderViewHolder(View itemView) {
            super(itemView);
        }
    }
}
