package org.hcilab.projects.nlogx.service;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import androidx.core.app.NotificationCompat;

import org.hcilab.projects.nlogx.BuildConfig;
import org.hcilab.projects.nlogx.misc.Const;
import org.hcilab.projects.nlogx.misc.Util;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

class NotificationObject {

	private final boolean LOG_TEXT;

	private Context context;
	private Notification n;

	// General
	private String packageName;
	private long postTime;
	private long systemTime;

	private boolean isClearable;
	private boolean isOngoing;

	private long when;
	private int number;
	private int flags;
	private int defaults;
	private int ledARGB;
	private int ledOff;
	private int ledOn;

	// Device
	private int ringerMode;
	private boolean isScreenOn;
	private int batteryLevel;
	private String batteryStatus;
	private boolean isConnected;
	private String connectionType;
	private String lastActivity;
	private String lastLocation;

	// Compat
	private String group;
	private boolean isGroupSummary;
	private String category;
	private int actionCount;
	private boolean isLocalOnly;

	private List people;
	private String style;

	// 16
	private int priority;

	// 18
	private int nid;
	private String tag;

	// 20
	private String key;
	private String sortKey;

	// 21
	private int visibility;
	private int color;
	private int interruptionFilter;
	private int listenerHints;
	private boolean matchesInterruptionFilter;

	// 26
	private int removeReason;

	// Text
	private String appName;
	private String tickerText;
	private String title;
	private String titleBig;
	private String text;
	private String textBig;
	private String textInfo;
	private String textSub;
	private String textSummary;
	private String textLines;

	NotificationObject(Context context, StatusBarNotification sbn, final boolean LOG_TEXT, int reason) {
		this.context = context;
		this.LOG_TEXT = LOG_TEXT;

		n           = sbn.getNotification();
		packageName = sbn.getPackageName();
		postTime    = sbn.getPostTime();
		systemTime  = System.currentTimeMillis();

		isClearable = sbn.isClearable();
		isOngoing   = sbn.isOngoing();

		nid         = sbn.getId();
		tag         = sbn.getTag();

		if(Build.VERSION.SDK_INT >= 20) {
			key     = sbn.getKey();
			sortKey = n.getSortKey();
		}

		removeReason = reason;

		extract();

		if(Const.ENABLE_ACTIVITY_RECOGNITION || Const.ENABLE_LOCATION_SERVICE) {
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
			lastActivity = sp.getString(Const.PREF_LAST_ACTIVITY, null);
			lastLocation = sp.getString(Const.PREF_LAST_LOCATION, null);
		}
	}

	private void extract()  {
		// General
		when           = n.when;
		flags          = n.flags;
		defaults       = n.defaults;
		ledARGB        = n.ledARGB;
		ledOff         = n.ledOffMS;
		ledOn          = n.ledOnMS;

		if(Build.VERSION.SDK_INT < 24) { // as of 24, this number is not shown anymore
			number = n.number;
		} else {
			number = -1;
		}

		// Device
		ringerMode     = Util.getRingerMode(context);
		isScreenOn     = Util.isScreenOn(context);
		batteryLevel   = Util.getBatteryLevel(context);
		batteryStatus  = Util.getBatteryStatus(context);
		isConnected    = Util.isNetworkAvailable(context);
		connectionType = Util.getConnectivityType(context);

		// 16
		priority = n.priority;

		// 21
		if(Build.VERSION.SDK_INT >= 21) {
			visibility = n.visibility;
			color      = n.color;

			listenerHints = NotificationListener.getListenerHints();
			interruptionFilter = NotificationListener.getInterruptionFilter();
			NotificationListenerService.Ranking ranking = new NotificationListenerService.Ranking();
			NotificationListenerService.RankingMap rankingMap = NotificationListener.getRanking();
			if(rankingMap != null && rankingMap.getRanking(key, ranking)) {
				matchesInterruptionFilter = ranking.matchesInterruptionFilter();
			}
		}

		// Compat
		group          = NotificationCompat.getGroup(n);
		isGroupSummary = NotificationCompat.isGroupSummary(n);
		category       = NotificationCompat.getCategory(n);
		actionCount    = NotificationCompat.getActionCount(n);
		isLocalOnly    = NotificationCompat.getLocalOnly(n);

		Bundle extras = NotificationCompat.getExtras(n);
		if(extras != null) {
			String[] tmp = extras.getStringArray(NotificationCompat.EXTRA_PEOPLE);
			people = tmp != null ? Arrays.asList(tmp) : null;
			style  = extras.getString(NotificationCompat.EXTRA_TEMPLATE);
		}

		// Text
		if(LOG_TEXT) {
			appName    = Util.getAppNameFromPackage(context, packageName, false);
			tickerText = Util.nullToEmptyString(n.tickerText);

			if(extras != null) {
				title       = Util.nullToEmptyString(extras.getCharSequence(NotificationCompat.EXTRA_TITLE));
				titleBig    = Util.nullToEmptyString(extras.getCharSequence(NotificationCompat.EXTRA_TITLE_BIG));
				text        = Util.nullToEmptyString(extras.getCharSequence(NotificationCompat.EXTRA_TEXT));
				textBig     = Util.nullToEmptyString(extras.getCharSequence(NotificationCompat.EXTRA_BIG_TEXT));
				textInfo    = Util.nullToEmptyString(extras.getCharSequence(NotificationCompat.EXTRA_INFO_TEXT));
				textSub     = Util.nullToEmptyString(extras.getCharSequence(NotificationCompat.EXTRA_SUB_TEXT));
				textSummary = Util.nullToEmptyString(extras.getCharSequence(NotificationCompat.EXTRA_SUMMARY_TEXT));

				CharSequence[] lines = extras.getCharSequenceArray(NotificationCompat.EXTRA_TEXT_LINES);
				if(lines != null) {
					textLines = "";
					for(CharSequence line : lines) {
						textLines += line + "\n";
					}
					textLines = textLines.trim();
				}
			}
		}
	}

	@Override
	public String toString() {
		try {
			JSONObject json = new JSONObject();

			// General
			json.put("packageName",    packageName);
			json.put("postTime",       postTime);
			json.put("systemTime",     systemTime);
			json.put("offset",         TimeZone.getDefault().getOffset(systemTime));
			json.put("version",        BuildConfig.VERSION_CODE);
			json.put("sdk",            android.os.Build.VERSION.SDK_INT);

			json.put("isOngoing",      isOngoing);
			json.put("isClearable",    isClearable);

			json.put("when",           when);
			json.put("number",         number);
			json.put("flags",          flags);
			json.put("defaults",       defaults);
			json.put("ledARGB",        ledARGB);
			json.put("ledOn",          ledOn);
			json.put("ledOff",         ledOff);

			// Device
			json.put("ringerMode",     ringerMode);
			json.put("isScreenOn",     isScreenOn);
			json.put("batteryLevel",   batteryLevel);
			json.put("batteryStatus",  batteryStatus);
			json.put("isConnected",    isConnected);
			json.put("connectionType", connectionType);

			// Compat
			json.put("group",          group);
			json.put("isGroupSummary", isGroupSummary);
			json.put("category",       category);
			json.put("actionCount",    actionCount);
			json.put("isLocalOnly",    isLocalOnly);

			json.put("people",         people == null ? 0 : people.size());
			json.put("style",          style);
			//json.put("displayName",    displayName);

			// Text
			if(LOG_TEXT) {
				json.put("tickerText",        tickerText);
				json.put("title",             title);
				json.put("titleBig",          titleBig);
				json.put("text",              text);
				json.put("textBig",           textBig);
				json.put("textInfo",          textInfo);
				json.put("textSub",           textSub);
				json.put("textSummary",       textSummary);
				json.put("textLines",         textLines);
			}

			json.put("appName", appName);

			// 16
			json.put("priority", priority);

			// 18
			json.put("nid", nid);
			json.put("tag", tag);

			// 20
			if(Build.VERSION.SDK_INT >= 20) {
				json.put("key",     key);
				json.put("sortKey", sortKey);
			}

			// 21
			if(Build.VERSION.SDK_INT >= 21) {
				json.put("visibility",                visibility);
				json.put("color",                     color);
				json.put("interruptionFilter",        interruptionFilter);
				json.put("listenerHints",             listenerHints);
				json.put("matchesInterruptionFilter", matchesInterruptionFilter);
			}

			// 26
			if(Build.VERSION.SDK_INT >= 26 && removeReason != -1) {
				json.put("removeReason", removeReason);
			}

			// Activity
			if(Const.ENABLE_ACTIVITY_RECOGNITION && lastActivity != null) {
				json.put("lastActivity", new JSONObject(lastActivity));
			}

			// Location
			if(Const.ENABLE_LOCATION_SERVICE && lastLocation != null) {
				json.put("lastLocation", new JSONObject(lastLocation));
			}

			return json.toString();
		} catch (Exception e) {
			if(Const.DEBUG) e.printStackTrace();
			return null;
		}
	}

}
