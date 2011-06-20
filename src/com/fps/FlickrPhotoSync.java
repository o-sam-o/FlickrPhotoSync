package com.fps;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.fps.flickr.resource.FlickrUser;
import com.fps.tasks.LoadPhotoSetsTask;

public class FlickrPhotoSync extends Activity {
	public static final String LOG_TAG = "fps";
	public static final String FLICKR_USER_EXTRA = "FlickrUserExtra";
	
	public static final String FPS_PHOTO_DIR = "flickr";
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    public void loadPhotoSets(View view){
    	new LoadPhotoSetsTask(this).execute(getUsername());
    }
    
    private String getUsername(){
    	return ((EditText)findViewById(R.id.flickrUsername)).getText().toString();
    }
    
    public void setMessage(String message){
    	((TextView)findViewById(R.id.flickrId)).setText(message);
    }
    
    public void displayUsersPhotoSets(FlickrUser flickrUser){
    	Intent intent = new Intent(this, PhotoSetsActivity.class);
    	intent.putExtra(FLICKR_USER_EXTRA, flickrUser);
    	startActivityForResult(intent, 0);
    }

}