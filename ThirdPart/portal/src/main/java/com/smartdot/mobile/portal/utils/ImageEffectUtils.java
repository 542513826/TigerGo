package com.smartdot.mobile.portal.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.ImageView;

/** 图片特效工具类 */
public class ImageEffectUtils {

	/**
	 * 把ImageView设成圆角
	 * 
	 * @param roundPx
	 *            是圆角的半径
	 */
	public static void setRoundedCorner(ImageView imgv, float roundPx) {
		Bitmap obmp = drawableToBitmap(imgv.getDrawable());
		Bitmap bitmap = getRoundedCornerBitmap(obmp, roundPx);
		imgv.setImageBitmap(bitmap);
	}

	/**
	 * 把ImageView设成圆角
	 * 
	 * @param roundPx
	 *            是圆角的半径
	 * @param resId
	 *            imageview要显示的资源图片的id
	 */
	public static void setRoundedCorner(Context context, ImageView imgv, float roundPx, int resId) {
		Bitmap bitmap = drawableToBitmap(context.getResources().getDrawable(resId));
		imgv.setImageBitmap(getRoundedCornerBitmap(bitmap, roundPx));
	}

	/**
	 * 把ImageView设成圆角
	 * 
	 * @param roundPx
	 *            是圆角的半径
	 * @param drawable
	 *            imageview要显示的资源图片
	 */
	public static void setRoundedCorner(ImageView imgv, float roundPx, Drawable drawable) {
		Bitmap bitmap = drawableToBitmap(drawable);
		imgv.setImageBitmap(getRoundedCornerBitmap(bitmap, roundPx));
	}

	/**
	 * 把ImageView设成圆形
	 */
	public static void setCircleImage(final ImageView imgv) {
		ViewTreeObserver vto = imgv.getViewTreeObserver();
		vto.addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				int height = imgv.getMeasuredHeight();
				int width = imgv.getMeasuredWidth();
				Bitmap obmp = drawableToBitmap(imgv.getDrawable());
				// 将长方形图片压缩成正方形
				int min = Math.min(obmp.getWidth(), obmp.getHeight());
				Bitmap mSrc = Bitmap.createScaledBitmap(obmp, min, min, false);
				Bitmap bitmap = createCircleImage(mSrc, min);
				imgv.setImageBitmap(bitmap);
				return true;
			}
		});
	}

	/**
	 * 把ImageView设成圆形
	 * 
	 * @param resId
	 *            imageview要显示的资源图片的id
	 */
	public static void setCircleImage(final Context context, final ImageView imgv, final int resId) {
		ViewTreeObserver vto = imgv.getViewTreeObserver();
		vto.addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				int height = imgv.getMeasuredHeight();
				int width = imgv.getMeasuredWidth();
				Bitmap bitmap = drawableToBitmap(context.getResources().getDrawable(resId));
				// 将长方形图片压缩成正方形
				int min = Math.min(bitmap.getWidth(), bitmap.getHeight());
				Bitmap mSrc = Bitmap.createScaledBitmap(bitmap, min, min, false);
				imgv.setImageBitmap(createCircleImage(mSrc, min));
				return true;
			}
		});
	}

	/**
	 * 把ImageView设成圆形
	 * 
	 * @param drawable
	 *            imageview要显示的资源图片
	 */
	public static void setCircleImage(final ImageView imgv, final Drawable drawable) {
		ViewTreeObserver vto = imgv.getViewTreeObserver();
		vto.addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				int height = imgv.getMeasuredHeight();
				int width = imgv.getMeasuredWidth();
				Bitmap bitmap = drawableToBitmap(drawable);
				// 将长方形图片压缩成正方形
				int min = Math.min(bitmap.getWidth(), bitmap.getHeight());
				Bitmap mSrc = Bitmap.createScaledBitmap(bitmap, min, min, false);
				imgv.setImageBitmap(createCircleImage(mSrc, min));
				return true;
			}
		});
	}

	/** drawable转化成Bitmap */
	private static Bitmap drawableToBitmap(Drawable drawable) {
		// 取 drawable 的长宽
		int w = drawable.getIntrinsicWidth();
		int h = drawable.getIntrinsicHeight();

		// 取 drawable 的颜色格式
		Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Config.ARGB_8888 : Config.RGB_565;
		// 建立对应 bitmap
		Bitmap bitmap = Bitmap.createBitmap(w, h, config);
		// 建立对应 bitmap 的画布
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, w, h);
		// 把 drawable 内容画到画布中
		drawable.draw(canvas);
		return bitmap;
	}

	/**
	 * 根据原图绘制圆形图片
	 * 
	 * @param source
	 * @param length
	 *            图片的边长
	 * @return
	 */
	private static Bitmap createCircleImage(Bitmap source, int length) {
		final Paint paint = new Paint();
		paint.setAntiAlias(true);// 抗锯齿
		Bitmap target = Bitmap.createBitmap(length, length, Config.ARGB_8888);
		/**
		 * 产生一个同样大小的画布
		 */
		Canvas canvas = new Canvas(target);
		/**
		 * 首先绘制圆形
		 */
		canvas.drawCircle(length / 2, length / 2, length / 2, paint);
		/**
		 * 使用SRC_IN，参考上面的说明
		 */
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		/**
		 * 绘制图片
		 */
		canvas.drawBitmap(source, 0, 0, paint);
		return target;
	}

	/**
	 * 变成圆角图片 roundPx圆角的弧度
	 */
	private static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Bitmap output = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, w, h);
		final RectF rectF = new RectF(rect);
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}
}
