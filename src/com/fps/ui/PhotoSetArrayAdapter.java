package com.fps.ui;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.fps.R;
import com.fps.flickr.resource.PhotoSet;

public class PhotoSetArrayAdapter extends ArrayAdapter<PhotoSet> {
	private Activity context;
	private List<PhotoSet> photoSets;
	
	public PhotoSetArrayAdapter(Activity context, int textViewResourceId,
			List<PhotoSet> objects) {
		super(context, textViewResourceId, objects);
		photoSets = objects;
		this.context = context;
	}

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			rowView = inflater.inflate(R.layout.photo_set_label, null, true);
			holder = new ViewHolder();
			holder.textView = (TextView) rowView.findViewById(R.id.photoSetLabel);
			rowView.setTag(holder);
		} else {
			holder = (ViewHolder) rowView.getTag();
		}

		PhotoSet photoSet = photoSets.get(position);
		holder.textView.setText(photoSet.getTitle() + " (" + photoSet.getPhotoCount() + ")");
		
		return rowView;
    }
    
	private class ViewHolder {
		public TextView textView;
	}
	
}
