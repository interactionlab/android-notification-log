package org.hcilab.projects.nlogx.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.hcilab.projects.nlogx.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BrowseActivity extends AppCompatActivity {

	private RecyclerView recyclerView;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browse);

		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
		recyclerView = findViewById(R.id.list);
		recyclerView.setLayoutManager(layoutManager);
	}

	@Override
	protected void onResume() {
		super.onResume();
		update();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.browse, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_refresh:
				update();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void update() {
		BrowseAdapter adapter = new BrowseAdapter(this);
		recyclerView.setAdapter(adapter);

		if(adapter.getItemCount() == 0) {
			Toast.makeText(this, R.string.empty_log_file, Toast.LENGTH_LONG).show();
		}
	}

}