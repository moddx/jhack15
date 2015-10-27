package org.tuxship.filebrowser;

import java.util.ArrayList;
import java.util.List;

import org.tuxship.filebrowser.FileBrowserActivity.Item;
import org.tuxship.quickshare.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class FileBrowserArrayAdapter extends ArrayAdapter<Item> {

	private ArrayList<Item> fileList;
	private Context context;
	
	public FileBrowserArrayAdapter(Context context, int textViewResourceId, List<Item> itemList) {
		super(context, textViewResourceId, itemList);
		this.context = context;
		this.fileList = new ArrayList<Item>();
		this.fileList.addAll(itemList);
	}

	private class ViewHolder {
		TextView fileNameView;
		CheckBox checkbox;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;

		if (convertView == null) {
			LayoutInflater lInflater = (LayoutInflater) context.getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			convertView = lInflater.inflate(R.layout.filebrowser_list_layout, null);

			holder = new ViewHolder();
			holder.fileNameView = (TextView) convertView.findViewById(R.id.name);
			holder.checkbox = (CheckBox) convertView.findViewById(R.id.checkbox);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		Item item = fileList.get(position);
		
		/*
		 * Determine icon and add it to the textview
		 */
		int drawableID = 0;
		if(!item.isEmpty) {
			if(item.isDirectory)
				drawableID = (item.canRead) ? R.drawable.folder_icon : R.drawable.folder_icon_light;
			else
				drawableID = R.drawable.file_icon;
		}
		holder.fileNameView.setCompoundDrawablesWithIntrinsicBounds(drawableID, 0,
				0, 0);

		holder.fileNameView.setEllipsize(null);

		// add margin between image and text
		int dp3 = (int) (3 * context.getResources().getDisplayMetrics().density + 0.5f);
	
		holder.fileNameView.setCompoundDrawablePadding(dp3);
		holder.fileNameView.setBackgroundColor(Color.LTGRAY);
		
		holder.fileNameView.setText(fileList.get(position).file);
		holder.checkbox.setChecked(false);

		return convertView;

	}

}