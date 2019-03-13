package org.hcilab.projects.nlogx.misc;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "notifications.db";

	// Posted notifications

	public static abstract class PostedEntry implements BaseColumns {
		public static final String TABLE_NAME = "notifications_posted";
		public static final String COLUMN_NAME_CONTENT = "content";
	}

	public static final String SQL_CREATE_ENTRIES_POSTED =
			"CREATE TABLE " + PostedEntry.TABLE_NAME + " (" +
					PostedEntry._ID + " INTEGER PRIMARY KEY," +
					PostedEntry.COLUMN_NAME_CONTENT + " TEXT)";

	public static final String SQL_DELETE_ENTRIES_POSTED =
			"DROP TABLE IF EXISTS " + PostedEntry.TABLE_NAME;

	// Removed notifications

	public static abstract class RemovedEntry implements BaseColumns {
		public static final String TABLE_NAME = "notifications_removed";
		public static final String COLUMN_NAME_CONTENT = "content";
	}

	public static final String SQL_CREATE_ENTRIES_REMOVED =
			"CREATE TABLE " + RemovedEntry.TABLE_NAME + " (" +
					RemovedEntry._ID + " INTEGER PRIMARY KEY," +
					RemovedEntry.COLUMN_NAME_CONTENT + " TEXT)";

	public static final String SQL_DELETE_ENTRIES_REMOVED =
			"DROP TABLE IF EXISTS " + RemovedEntry.TABLE_NAME;

	// Implementation

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ENTRIES_POSTED);
		db.execSQL(SQL_CREATE_ENTRIES_REMOVED);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(SQL_DELETE_ENTRIES_POSTED);
		db.execSQL(SQL_DELETE_ENTRIES_REMOVED);
		onCreate(db);
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}

}