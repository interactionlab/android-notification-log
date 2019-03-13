package org.hcilab.projects.nlogx.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import org.hcilab.projects.nlogx.BuildConfig;
import org.hcilab.projects.nlogx.R;
import org.hcilab.projects.nlogx.misc.Const;
import org.hcilab.projects.nlogx.misc.DatabaseHelper;
import org.hcilab.projects.nlogx.misc.Util;
import org.hcilab.projects.nlogx.service.NotificationHandler;

public class SettingsFragment extends PreferenceFragmentCompat {

	public static final String TAG = SettingsFragment.class.getName();

	private DatabaseHelper dbHelper;
	private BroadcastReceiver updateReceiver;

	private Preference prefStatus;
	private Preference prefText;
	private Preference prefOngoing;
	private Preference prefEntries;

	@Override
	public void onCreatePreferences(Bundle bundle, String s) {
		addPreferencesFromResource(R.xml.preferences);

		PreferenceManager pm = getPreferenceManager();

		prefStatus = pm.findPreference(Const.PREF_STATUS);
		prefStatus.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
				return true;
			}
		});

		pm.findPreference(Const.PREF_BROWSE).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(getActivity(), BrowseActivity.class));
				return true;
			}
		});

		prefText = pm.findPreference(Const.PREF_TEXT);
		prefOngoing = pm.findPreference(Const.PREF_ONGOING);
		prefEntries = pm.findPreference(Const.PREF_ENTRIES);

		pm.findPreference(Const.PREF_VERSION).setSummary(BuildConfig.VERSION_NAME + (Const.DEBUG ? " dev" : ""));
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			dbHelper = new DatabaseHelper(getActivity());
		} catch (Exception e) {
			if(Const.DEBUG) e.printStackTrace();
		}

		updateReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				update();
			}
		};
	}

	@Override
	public void onResume() {
		super.onResume();

		if(Util.isNotificationAccessEnabled(getActivity())) {
			prefStatus.setSummary(R.string.settings_notification_access_enabled);
			prefText.setEnabled(true);
			prefOngoing.setEnabled(true);
		} else {
			prefStatus.setSummary(R.string.settings_notification_access_disabled);
			prefText.setEnabled(false);
			prefOngoing.setEnabled(false);
		}

		IntentFilter filter = new IntentFilter();
		filter.addAction(NotificationHandler.BROADCAST);
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(updateReceiver, filter);

		update();
	}

	@Override
	public void onPause() {
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(updateReceiver);
		super.onPause();
	}

	private void update() {
		try {
			SQLiteDatabase db   = dbHelper.getReadableDatabase();
			long numRowsPosted  = DatabaseUtils.queryNumEntries(db, DatabaseHelper.PostedEntry.TABLE_NAME);
			prefEntries.setSummary("" + numRowsPosted);
		} catch (Exception e) {
			if(Const.DEBUG) e.printStackTrace();
		}
	}

}