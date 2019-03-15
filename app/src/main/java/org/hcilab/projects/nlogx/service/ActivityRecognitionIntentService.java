package org.hcilab.projects.nlogx.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import org.hcilab.projects.nlogx.misc.Const;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.TimeZone;

import androidx.annotation.Nullable;

public class ActivityRecognitionIntentService extends IntentService {

	public ActivityRecognitionIntentService() {
		super("ActivityRecognitionIntentService");
	}

	@Override
	protected void onHandleIntent(@Nullable Intent intent) {
		if(intent == null) {
			return;
		}

		ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

		ArrayList<DetectedActivity> detectedActivities = (ArrayList<DetectedActivity>) result.getProbableActivities();

		ArrayList<String> activities = new ArrayList<>();
		ArrayList<Integer> confidences = new ArrayList<>();

		for(DetectedActivity activity : detectedActivities) {
			activities.add(getActivityString(activity.getType()));
			confidences.add(activity.getConfidence());
		}

		JSONObject json = new JSONObject();
		String str = null;
		try {
			long now = System.currentTimeMillis();
			json.put("time", now);
			json.put("offset", TimeZone.getDefault().getOffset(now));
			json.put("activities", new JSONArray(activities));
			json.put("confidences", new JSONArray(confidences));
			str = json.toString();
		} catch (JSONException e) {
			if(Const.DEBUG) e.printStackTrace();
		}

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		sp.edit().putString(Const.PREF_LAST_ACTIVITY, str).apply();
	}

	private String getActivityString(int detectedActivityType) {
		switch(detectedActivityType) {
			case DetectedActivity.IN_VEHICLE:
				return "IN_VEHICLE";
			case DetectedActivity.ON_BICYCLE:
				return "ON_BICYCLE";
			case DetectedActivity.ON_FOOT:
				return "ON_FOOT";
			case DetectedActivity.RUNNING:
				return "RUNNING";
			case DetectedActivity.STILL:
				return "STILL";
			case DetectedActivity.TILTING:
				return "TILTING";
			case DetectedActivity.UNKNOWN:
				return "UNKNOWN";
			case DetectedActivity.WALKING:
				return "WALKING";
			default:
				return detectedActivityType + "";
		}
	}

}