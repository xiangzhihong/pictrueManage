package com.xzh.picturesmanager.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.xzh.picturesmanager.base.YmatouApplication;

public final class Utils {

	public static String makeSafe(String s) {
		return (s == null) ? "" : s;
	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static int getScreenWidth(Context context) {
		if (context == null){
			return 0;
		}
		Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int w = display.getWidth();
		return w;
	}

	public static int getScreenHeight(Context context) {
		if (context == null){
			return 800;
		}
		Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int h = display.getHeight();
		return h;
	}

	/**
	 * 隐藏键盘
	 * @param context
	 */
	public static void hideKeyBoard(Activity context) {
		if (context != null && context.getCurrentFocus() != null) {
			((InputMethodManager) context
					.getSystemService(Context.INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(context.getCurrentFocus()
									.getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	/**
	 * 显示键盘
	 * @param context
	 */
	public static void showInput(Context context) {
		InputMethodManager inputMethodManager = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.toggleSoftInput(0,
				InputMethodManager.HIDE_NOT_ALWAYS);

	}

	public static void shortToast(CharSequence text) {
		toast(YmatouApplication.getInstance(), text, Toast.LENGTH_SHORT);
	}

	public static void toast(Context context, CharSequence text, int duration) {
		Toast toast = new Toast(context);
		toast.setDuration(duration);
		TextView v = new TextView(context);
		v.setBackgroundColor(0x88000000);
		v.setTextColor(Color.WHITE);
		v.setText(text);
		v.setSingleLine(false);
		v.setPadding(20, 10, 20, 10);
		v.setGravity(Gravity.CENTER);
		toast.setView(v);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
}
