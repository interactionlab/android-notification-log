package org.hcilab.projects.nlog.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.google.android.gms.location.LocationResult;

import org.hcilab.projects.nlog.misc.Const;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.TimeZone;

public class FusedLocationIntentService extends IntentService {

	public FusedLocationIntentService() {
		super("FusedLocationIntentService");
	}

	@Override
	protected void onHandleIntent(@Nullable Intent intent) {
		if(LocationResult.hasResult(intent)) {
			LocationResult locationResult = LocationResult.extractResult(intent);
			Location location = locationResult.getLastLocation();

			JSONObject json = new JSONObject();
			String str = null;
			try {
				long now = System.currentTimeMillis();
				json.put("time", now);
				json.put("offset", TimeZone.getDefault().getOffset(now));
				json.put("age", now - location.getTime());
				json.put("longitude", location.getLongitude() + "");
				json.put("latitude", location.getLatitude() + "");
				json.put("accuracy", location.getAccuracy());
				str = json.toString();
			} catch (JSONException e) {
				if(Const.DEBUG) e.printStackTrace();
			}

			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			sp.edit().putString(Const.PREF_LAST_LOCATION, str).apply();
		}
	}

}