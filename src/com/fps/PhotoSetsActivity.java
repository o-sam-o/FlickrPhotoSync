package com.fps;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.fps.flickr.resource.FlickrUser;
import com.fps.flickr.resource.PhotoSet;
import com.fps.tasks.DownloadPhotosTask;
import com.fps.ui.PhotoSetArrayAdapter;

public class PhotoSetsActivity extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setListAdapter(new PhotoSetArrayAdapter(this, R.layout.photo_set_label, getFlickrUser().getPhotoSets()));

        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
    }
	
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		PhotoSet photoSet = (PhotoSet) this.getListAdapter().getItem(position);
		new DownloadPhotosTask(this).execute(photoSet);
	}

    private FlickrUser getFlickrUser(){
    	return (FlickrUser) getIntent().getExtras().get(FlickrPhotoSync.FLICKR_USER_EXTRA);
    }

}
