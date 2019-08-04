package org.hcilab.projects.nlogx.misc;

import org.hcilab.projects.nlogx.BuildConfig;

public class Const {

	public static final boolean DEBUG = BuildConfig.DEBUG;
	public static final long VERSION  = BuildConfig.VERSION_CODE;

	// Feature flags
	public static final boolean ENABLE_ACTIVITY_RECOGNITION = true;
	public static final boolean ENABLE_LOCATION_SERVICE     = true;

	// Preferences shown in the UI
	public static final String PREF_STATUS  = "pref_status";
	public static final String PREF_BROWSE  = "pref_browse";
	public static final String PREF_TEXT    = "pref_text";
	public static final String PREF_ONGOING = "pref_ongoing";
	public static final String PREF_ABOUT   = "pref_about";
	public static final String PREF_VERSION = "pref_version";

	// Preferences not shown in the UI
	public static final String PREF_LAST_ACTIVITY  = "pref_last_activity";
	public static final String PREF_LAST_LOCATION  = "pref_last_location";

}