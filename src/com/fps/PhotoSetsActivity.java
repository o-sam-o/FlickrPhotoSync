package com.fps;

import com.fps.flickr.FlickrUser;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class PhotoSetsActivity extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        FlickrUser flickrUser = (FlickrUser) getIntent().getExtras().get(FlickrPhotoSync.FLICKR_USER_EXTRA);
        String[] photoSets = new String[flickrUser.getPhotoSets().size()];
        for (int i = 0; i < flickrUser.getPhotoSets().size(); i++) {
			photoSets[i] = flickrUser.getPhotoSets().get(i).getTitle();
		}
        setListAdapter(new ArrayAdapter<String>(this, R.layout.photo_set_label, photoSets));

        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
    }
	
}
