package org.hcilab.projects.nlogx.misc;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;

import androidx.core.content.FileProvider;

import com.google.android.material.snackbar.Snackbar;

import org.hcilab.projects.nlogx.BuildConfig;
import org.hcilab.projects.nlogx.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ExportTask extends AsyncTask<Void, Void, Void> {

	public static boolean exporting = false;

	private static final String EXPORT_FILE_NAME = "notification_export_%s.json";

	private Context context;
	private View view;
	private Snackbar snackbar;

	public ExportTask(Context context, View view) {
		this.context  = context;
		this.view     = view;
	}

	@Override
	protected void onPreExecute() {
		exporting = true;
		snackbar = Snackbar.make(view, R.string.snackbar_export, Snackbar.LENGTH_INDEFINITE);
		snackbar.show();
	}

	@Override
	protected Void doInBackground(Void... params) {
		// Create share folder
		File exportPath = new File(context.getCacheDir(), "share");
		if(!exportPath.exists()) {
			boolean mkdirsResult = exportPath.mkdirs();
			if(Const.DEBUG) {
				System.out.println("Share directory created: " + mkdirsResult);
			}
		}

		// Clean up old exports
		File[] oldFiles = exportPath.listFiles();
		if(oldFiles != null) {
			for(File oldFile : oldFiles) {
				if(oldFile.isFile() && oldFile.getName().startsWith("notification_export")) {
					boolean deleteResult = oldFile.delete();
					if(Const.DEBUG) {
						System.out.println("Existing cache file deleted: " + deleteResult);
					}
				}
			}
		}

		// Generate a file name
		long currentTime = System.currentTimeMillis();
		String exportDate = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(new Date(currentTime));
		String exportFileName = String.format(EXPORT_FILE_NAME, exportDate);

		// Create a new file
		File newFile = new File(exportPath, exportFileName);

		try {
			// Start writing
			FileOutputStream outputStream = new FileOutputStream(newFile);
			outputStream.write("{\n\n".getBytes());

			// Device info
			outputStream.write("\"device\": ".getBytes());

			JSONObject json = new JSONObject();
			try {
				long time = System.currentTimeMillis();
				json.put("version",      BuildConfig.VERSION_CODE);
				json.put("locale",       Util.getLocale(context));
				json.put("model",        android.os.Build.MODEL);
				json.put("device",       android.os.Build.DEVICE);
				json.put("product",      android.os.Build.PRODUCT);
				json.put("manufacturer", android.os.Build.MANUFACTURER);
				json.put("sdk",          android.os.Build.VERSION.SDK_INT);
				json.put("timezone",     TimeZone.getDefault().getID());
				json.put("offset",       TimeZone.getDefault().getOffset(time));
				json.put("exportTime",   time);

				PackageManager pm = context.getPackageManager();
				List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
				JSONArray array = new JSONArray();
				for(ApplicationInfo packageInfo : packages) {
					JSONObject obj = new JSONObject();
					boolean isSystemApp = (packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
					obj.put("appName",     Util.getAppNameFromPackage(context, packageInfo.packageName, false));
					obj.put("packageName", packageInfo.packageName);
					obj.put("enabled",     packageInfo.enabled);
					obj.put("system",      isSystemApp);
					array.put(obj);
				}
				json.put("apps", array);

			} catch(JSONException e) {
				if(Const.DEBUG) e.printStackTrace();
			}

			outputStream.write(json.toString().getBytes());
			outputStream.write(",\n\n".getBytes());

			// Posted and removed notifications
			DatabaseHelper dbHelper = new DatabaseHelper(context);
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Cursor c;

			String[] projectionPosted = {
					DatabaseHelper.PostedEntry._ID,
					DatabaseHelper.PostedEntry.COLUMN_NAME_CONTENT
			};

			String sortOrderPosted =
					DatabaseHelper.PostedEntry._ID + " ASC";

			String[] projectionRemoved = {
					DatabaseHelper.RemovedEntry._ID,
					DatabaseHelper.RemovedEntry.COLUMN_NAME_CONTENT
			};

			String sortOrderRemoved =
					DatabaseHelper.RemovedEntry._ID + " ASC";

			outputStream.write("\"posted\": [\n".getBytes());

			c = db.query(DatabaseHelper.PostedEntry.TABLE_NAME,
					projectionPosted,
					null,
					null,
					null,
					null,
					sortOrderPosted);

			if(c != null) {
				c.moveToFirst();
				for(int i = 0; i < c.getCount(); i++) {
					String content = c.getString(c.getColumnIndex(DatabaseHelper.PostedEntry.COLUMN_NAME_CONTENT));
					outputStream.write("\t".getBytes());
					outputStream.write(content.getBytes());

					if(i == c.getCount() - 1) {
						outputStream.write("\n".getBytes());
					} else {
						outputStream.write(",\n".getBytes());
					}

					c.moveToNext();
				}
				c.close();
			}

			outputStream.write("],\n\n".getBytes());
			outputStream.write("\"removed\": [\n".getBytes());

			c = db.query(DatabaseHelper.RemovedEntry.TABLE_NAME,
					projectionRemoved,
					null,
					null,
					null,
					null,
					sortOrderRemoved);

			if(c != null) {
				c.moveToFirst();
				for(int i = 0; i < c.getCount(); i++) {
					String content = c.getString(c.getColumnIndex(DatabaseHelper.RemovedEntry.COLUMN_NAME_CONTENT));
					outputStream.write("\t".getBytes());
					outputStream.write(content.getBytes());

					if(i == c.getCount() - 1) {
						outputStream.write("\n".getBytes());
					} else {
						outputStream.write(",\n".getBytes());
					}

					c.moveToNext();
				}
				c.close();
			}

			outputStream.write("]\n\n}".getBytes());

			outputStream.close();

		} catch (Exception e) {
			if(Const.DEBUG) e.printStackTrace();
		}

		// Get the content provider URI
		Uri contentUri = FileProvider.getUriForFile(context, "org.hcilab.projects.nlogx.fileprovider", newFile);

		// Open the share dialog
		Intent sharingIntent = new Intent(Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		sharingIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
		context.startActivity(Intent.createChooser(sharingIntent, context.getResources().getString(R.string.menu_export)));

		return null;
	}

	@Override
	protected void onPostExecute(Void aVoid) {
		if(snackbar != null && snackbar.isShownOrQueued()) {
			snackbar.dismiss();
		}
		exporting = false;
	}

}