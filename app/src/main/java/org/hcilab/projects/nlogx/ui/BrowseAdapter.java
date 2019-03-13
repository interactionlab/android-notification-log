package org.hcilab.projects.nlogx.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hcilab.projects.nlogx.R;
import org.hcilab.projects.nlogx.misc.Const;
import org.hcilab.projects.nlogx.misc.DatabaseHelper;
import org.hcilab.projects.nlogx.misc.Util;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

class BrowseAdapter extends RecyclerView.Adapter<BrowseViewHolder> {

	private Context context;
	private ArrayList<DataItem> data = new ArrayList<>();
	private HashMap<String, Drawable> iconCache = new HashMap<>();

	BrowseAdapter(Context context) {
		this.context = context;

		try {
			DatabaseHelper databaseHelper = new DatabaseHelper(context);
			SQLiteDatabase db = databaseHelper.getReadableDatabase();

			Cursor cursor = db.query(DatabaseHelper.PostedEntry.TABLE_NAME,
					new String[] {
							DatabaseHelper.PostedEntry._ID,
							DatabaseHelper.PostedEntry.COLUMN_NAME_CONTENT
					},
					null,
					null,
					null,
					null,
					DatabaseHelper.PostedEntry._ID + " DESC",
					"100");

			if(cursor != null && cursor.moveToFirst()) {
				for(int i = 0; i < cursor.getCount(); i++) {
					data.add(new DataItem(context, cursor.getLong(0), cursor.getString(1)));
					cursor.moveToNext();
				}
				cursor.close();
			}

			db.close();
			databaseHelper.close();
		} catch (Exception e) {
			if(Const.DEBUG) e.printStackTrace();
		}
	}

	@Override
	public BrowseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_browse, parent, false);
		BrowseViewHolder vh = new BrowseViewHolder(view);
		vh.item.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String id = (String) v.getTag();
				if(id != null) {
					Intent intent = new Intent(context, DetailsActivity.class);
					intent.putExtra(DetailsActivity.EXTRA_ID, id);
					context.startActivity(intent);
				}
			}
		});
		return vh;
	}

	@Override
	public void onBindViewHolder(BrowseViewHolder vh, int position) {
		DataItem item = data.get(position);

		if(iconCache.containsKey(item.getPackageName()) && iconCache.get(item.getPackageName()) != null) {
			vh.icon.setImageDrawable(iconCache.get(item.getPackageName()));
		} else {
			vh.icon.setImageResource(R.mipmap.ic_launcher);
		}

		vh.item.setTag("" + item.getId());
		vh.name.setText(item.getAppName());

		if(item.getPreview().length() == 0) {
			vh.preview.setVisibility(View.GONE);
			vh.text.setVisibility(View.VISIBLE);
			vh.text.setText(item.getText());
		} else {
			vh.text.setVisibility(View.GONE);
			vh.preview.setVisibility(View.VISIBLE);
			vh.preview.setText(item.getPreview());
		}
	}

	@Override
	public int getItemCount() {
		return data.size();
	}

	private class DataItem {

		private long id;
		private String packageName;
		private String appName;
		private String text;
		private String preview;

		DataItem(Context context, long id, String str) {
			this.id = id;
			try {
				JSONObject json = new JSONObject(str);
				packageName = json.getString("packageName");
				appName = Util.getAppNameFromPackage(context, packageName, false);
				text = str;

				String title = json.optString("title");
				String text = json.optString("text");
				preview = (title + "\n" + text).trim();

				if(!iconCache.containsKey(packageName)) {
					iconCache.put(packageName, Util.getAppIconFromPackage(context, packageName));
				}
			} catch (JSONException e) {
				if(Const.DEBUG) e.printStackTrace();
			}
		}

		public long getId() {
			return id;
		}

		String getPackageName() {
			return packageName;
		}

		String getAppName() {
			return appName;
		}

		public String getText() {
			return text;
		}

		String getPreview() {
			return preview;
		}

	}

}
