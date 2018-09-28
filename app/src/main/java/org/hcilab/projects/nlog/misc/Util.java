package org.hcilab.projects.nlog.misc;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.LocaleList;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;

import org.hcilab.projects.nlog.BuildConfig;

import java.util.ArrayList;
import java.util.List;

public class Util {

	public static String getAppNameFromPackage(Context context, String packageName, boolean returnNull) {
		final PackageManager pm = context.getApplicationContext().getPackageManager();
		ApplicationInfo ai;
		try {
			ai = pm.getApplicationInfo(packageName, 0);
		} catch(final PackageManager.NameNotFoundException e) {
			ai = null;
		}
		if(returnNull) {
			return ai == null ? null : pm.getApplicationLabel(ai).toString();
		}
		return (String) (ai != null ? pm.getApplicationLabel(ai) : packageName);
	}

	public static Drawable getAppIconFromPackage(Context context, String packageName) {
		PackageManager pm = context.getApplicationContext().getPackageManager();
		Drawable drawable = null;
		try {
			ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
			if(ai != null) {
				drawable = pm.getApplicationIcon(ai);
			}
		} catch (Exception e) {
			if(Const.DEBUG) e.printStackTrace();
		}
		return drawable;
	}

	public static String nullToEmptyString(CharSequence charsequence) {
		if(charsequence == null) {
			return "";
		} else {
			return charsequence.toString();
		}
	}

	public static boolean isNotificationAccessEnabled(Context context) {
		try {
			ContentResolver contentResolver = context.getContentResolver();
			String listeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
			return !(listeners == null || !listeners.contains(BuildConfig.APPLICATION_ID));
		} catch(Exception e) {
			if(Const.DEBUG) e.printStackTrace();
		}
		return false;
	}

	public static int getRingerMode(Context context) {
		AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		if(am == null) {
			return -1;
		}
		return am.getRingerMode();
	}

	public static boolean isScreenOn(Context context) {
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		if(pm == null) {
			return false;
		}
		if(Build.VERSION.SDK_INT >= 20) {
			return pm.isInteractive();
		} else {
			return pm.isScreenOn();
		}
	}

	public static String getLocale(Context context) {
		if(Build.VERSION.SDK_INT >= 24) {
			LocaleList localeList = context.getResources().getConfiguration().getLocales();
			return localeList.toString();
		} else {
			return context.getResources().getConfiguration().locale.toString();
		}
	}

	public static boolean hasPermission(Context context, String permission) {
		return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
	}

	public static String[] getAllInstalledApps(Context context) {
		PackageManager pm = context.getPackageManager();
		List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		ArrayList<String> list = new ArrayList<>();
		for(ApplicationInfo packageInfo : packages) {
			list.add(packageInfo.packageName);
		}
		return list.toArray(new String[0]);
	}

	public static int getBatteryLevel(Context context) {
		int batteryLevel = -1;
		if(Build.VERSION.SDK_INT >= 21) {
			BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
			if (bm != null) {
				batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
			}
		}
		return batteryLevel;
	}

	public static String getBatteryStatus(Context context) {
		if(Build.VERSION.SDK_INT < 26) {
			return "not supported";
		}
		BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
		if(bm != null) {
			int status = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS);
			switch (status) {
				case BatteryManager.BATTERY_STATUS_CHARGING: return "charging";
				case BatteryManager.BATTERY_STATUS_DISCHARGING: return "discharging";
				case BatteryManager.BATTERY_STATUS_FULL: return "full";
				case BatteryManager.BATTERY_STATUS_NOT_CHARGING: return "not charging";
				case BatteryManager.BATTERY_STATUS_UNKNOWN: return "unknown";
				default: return ""+status;
			}
		}
		return "undefined";
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(cm == null) {
			return false;
		}
		NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	public static String getConnectivityType(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(cm != null) {
			NetworkInfo networkInfo = cm.getActiveNetworkInfo();
			if(networkInfo != null) {
				int type = networkInfo.getType();
				switch (type) {
					case ConnectivityManager.TYPE_BLUETOOTH: return "bluetooth";
					case ConnectivityManager.TYPE_DUMMY: return "dummy";
					case ConnectivityManager.TYPE_ETHERNET: return "ethernet";
					case ConnectivityManager.TYPE_MOBILE: return "mobile";
					case ConnectivityManager.TYPE_MOBILE_DUN: return "mobile dun";
					case ConnectivityManager.TYPE_VPN: return "vpn";
					case ConnectivityManager.TYPE_WIFI: return "wifi";
					case ConnectivityManager.TYPE_WIMAX: return "wimax";
					default: return ""+type;
				}
			} else {
				return "none";
			}
		}
		return "undefined";
	}

}