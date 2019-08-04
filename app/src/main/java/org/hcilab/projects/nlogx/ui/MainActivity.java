package org.hcilab.projects.nlogx.ui;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.hcilab.projects.nlogx.R;
import org.hcilab.projects.nlogx.misc.Const;
import org.hcilab.projects.nlogx.misc.DatabaseHelper;
import org.hcilab.projects.nlogx.misc.ExportTask;
import org.hcilab.projects.nlogx.service.NotificationHandler;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_delete:
				confirm();
				return true;
			case R.id.menu_export:
				export();
				return true;

		}
		return super.onOptionsItemSelected(item);
	}

	private void confirm() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
		builder.setTitle(R.string.dialog_delete_header);
		builder.setMessage(R.string.dialog_delete_text);
		builder.setNegativeButton(R.string.dialog_delete_no, (dialogInterface, i) -> {});
		builder.setPositiveButton(R.string.dialog_delete_yes, (dialogInterface, i) -> truncate());
		builder.show();
	}

	private void truncate() {
		try {
			DatabaseHelper dbHelper = new DatabaseHelper(this);
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.execSQL(DatabaseHelper.SQL_DELETE_ENTRIES_POSTED);
			db.execSQL(DatabaseHelper.SQL_CREATE_ENTRIES_POSTED);
			db.execSQL(DatabaseHelper.SQL_DELETE_ENTRIES_REMOVED);
			db.execSQL(DatabaseHelper.SQL_CREATE_ENTRIES_REMOVED);
			Intent local = new Intent();
			local.setAction(NotificationHandler.BROADCAST);
			LocalBroadcastManager.getInstance(this).sendBroadcast(local);
		} catch (Exception e) {
			if(Const.DEBUG) e.printStackTrace();
		}
	}

	private void export() {
		if(!ExportTask.exporting) {
			ExportTask exportTask = new ExportTask(this, findViewById(android.R.id.content));
			exportTask.execute();
		}
	}

}