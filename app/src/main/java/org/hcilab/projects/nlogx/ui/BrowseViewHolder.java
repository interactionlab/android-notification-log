package org.hcilab.projects.nlogx.ui;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.hcilab.projects.nlogx.R;

class BrowseViewHolder extends RecyclerView.ViewHolder {

	public LinearLayout item;
	public ImageView icon;
	public TextView name;
	public TextView preview;
	public TextView text;
	public TextView date;

	BrowseViewHolder(View view) {
		super(view);
		item = view.findViewById(R.id.item);
		icon = view.findViewById(R.id.icon);
		name = view.findViewById(R.id.name);
		preview = view.findViewById(R.id.preview);
		text = view.findViewById(R.id.text);
		date = view.findViewById(R.id.date);
	}

}
