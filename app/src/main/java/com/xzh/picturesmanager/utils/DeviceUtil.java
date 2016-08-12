package com.xzh.picturesmanager.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Rect;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Method;

public class DeviceUtil {

	public static String getDeviceType() {
        return "android";
    }
	
	public static String getDeviceModel() {
        return Utils.makeSafe(Build.MODEL);
    }

	/**
	 * 获得设备制造商
	 * @return
	 */
	public static String getManufacturer() {
		return Utils.makeSafe(Build.MANUFACTURER);
	}
	
	

	/**
	 * 获得设备的固件版本号
	 */
	public static String getReleaseVersion() {
		return Utils.makeSafe(Build.VERSION.RELEASE);
	}
	
	/**
	 * 获得国际移动设备身份码
	 * @param context
	 * @return
	 */
	public static String getIMEI(Context context) {
        return ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
    }
	
	/**
	 * 获得国际移动用户识别码
	 * @param context
	 * @return
	 */
	public static String getIMSI(Context context) {
        return ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE)).getSubscriberId();
    }
	
	/**
	 * 获得设备屏幕矩形区域范围
	 * @param context
	 * @return
	 */
	public static Rect getScreenRect(Context context) {
		if (context == null){
			return new Rect(0, 0, 0, 0);
		}
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int w = display.getWidth();
        int h = display.getHeight();
        return new Rect(0, 0, w, h);
    }
	
	public static int getScreenHeight(Context context) {
			if (context == null){
				return 800;
			}
		Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int h = display.getHeight();
        return h;
    }

	public static int getScreenHeightNotHaveTitle(Context context){
		Rect outRect = new Rect();
		((Activity)context).getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);
        return outRect.height();
	}
	public static int getScreenWidth(Context context) {
		if (context == null){
			return 0;
		}
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int w = display.getWidth();
        return w;
    }
	
	/**
	 * 获得设备屏幕密度
	 */
	public static float getScreenDensity(Context context) {
		DisplayMetrics metrics = context.getApplicationContext().getResources().getDisplayMetrics();
		return metrics.density;
	}

	public static int getScreenDensityDpi(Context context) {
		DisplayMetrics metrics = context.getApplicationContext().getResources().getDisplayMetrics();
		return (int)(metrics.density * 160);
	}

	public static boolean isExistSDCard() {
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			return true;
		} else
			return false;
	}

	/**
	 * 获得deviceId
	 * @param context
	 * @return
	 */
	public static String getDeviceId(Context context) {
		return getIMEI(context);
	}

	/**
	 * 获得屏幕尺寸
	 * @param context
	 * @return
	 */
	public static String getResolution(Context context) {
		Rect rect = getScreenRect(context);
		return rect.right + "x" + rect.bottom;
	}

	public static String getSerialNumber() {
		String serialNumber = "";

		try {
			Class<?> c = Class.forName("android.os.SystemProperties");
			Method get = c.getMethod("get", String.class);
			serialNumber = (String) get.invoke(c, "ro.serialno");

			if(serialNumber.equals("")) {
				serialNumber = "?";
			}
		} catch (Exception e) {
			if(serialNumber.equals("")) {
				serialNumber = "?";
			}
		}

		return serialNumber;
	}
	
	public static PackageInfo getPackageInfo(Context context){
		try {
			return context.getPackageManager().getPackageInfo(context.getClass().getPackage().getName(), PackageManager.GET_CONFIGURATIONS);
		} catch (NameNotFoundException e) {
		}
		return null;
	}
	
	public static boolean isOpenGPS(Context context) {
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

		return locationManager != null
				&& locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

	}
	
	/**
	 * Get height of status bar 
	 * @param activity
	 * @return
	 */
	public static int getStatusBarHeight(Activity activity){
		Rect outRect = new Rect();
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);
		if(outRect.top > 0)
			return outRect.top;
		
		int dpi = getScreenDensityDpi(activity);
		if (dpi == DisplayMetrics.DENSITY_LOW) {
			return 24;
		} else if (dpi == DisplayMetrics.DENSITY_MEDIUM) {
			return 32;
		} else {
			return 48;
		}
	}


	/**
	 * 获取当前ip地址
	 *
	 * @param context
	 * @return
	 */
	public static String getLocalIpAddress(Context context) {
		try {
			WifiManager wifiManager = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			int i = wifiInfo.getIpAddress();
			return int2ip(i);
		} catch (Exception ex) {
			return " 获取IP出错鸟!!!!请保证是WIFI,或者请重新打开网络!\n" + ex.getMessage();
		}
	}

	public static String int2ip(int ipInt) {
		StringBuilder sb = new StringBuilder();
		sb.append(ipInt & 0xFF).append(".");
		sb.append((ipInt >> 8) & 0xFF).append(".");
		sb.append((ipInt >> 16) & 0xFF).append(".");
		sb.append((ipInt >> 24) & 0xFF);
		return sb.toString();
	}

	public static double getScreenRate(Context context) {
		return (getScreenHeight(context) + 0.00f) / (getScreenWidth(context) + 0.00f);
	}
}
