package com.fps;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.fps.flickr.resource.FlickrUser;
import com.fps.flickr.resource.PhotoSet;
import com.fps.tasks.DownloadPhotosTask;

public class PhotoSetsActivity extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        FlickrUser flickrUser = getFlickrUser();
        String[] photoSets = new String[flickrUser.getPhotoSets().size()];
        for (int i = 0; i < flickrUser.getPhotoSets().size(); i++) {
			photoSets[i] = flickrUser.getPhotoSets().get(i).getTitle();
		}
        setListAdapter(new ArrayAdapter<String>(this, R.layout.photo_set_label, photoSets));

        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
    }
	
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		// FIXME create list adapter that contains a PhotoSet
		Object o = this.getListAdapter().getItem(position);
		String setName = o.toString();
		Log.i(FlickrPhotoSync.LOG_TAG, "Selected set: " + setName);
		for(PhotoSet photoSet : getFlickrUser().getPhotoSets()){
			if(photoSet.getTitle().equals(setName)){
				new DownloadPhotosTask(this).execute(photoSet);
			}
		}
	}

    private FlickrUser getFlickrUser(){
    	return (FlickrUser) getIntent().getExtras().get(FlickrPhotoSync.FLICKR_USER_EXTRA);
    }
}
