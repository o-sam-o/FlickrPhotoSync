package com.fps;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.fps.flickr.FlickrUser;
import com.fps.tasks.LoadPhotoSetsTask;

public class FlickrPhotoSync extends Activity {
	public static final String LOG_TAG = "fps";
	public static final String FLICKR_USER_EXTRA = "FlickrUserExtra";
	
	private static final String FPS_PHOTO_DIR = "flickr";
	
	
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
    
    public void loadImageFromUrl(View view){
    	String imageUrl = getUsername();
    	Log.d(LOG_TAG, "Downloading image: " + imageUrl);
    	
    	Bitmap image = null;
    	try {
    		image = downloadImage(imageUrl);
    	} catch (Exception e){
			//TODO add better error handling
    		Log.e(LOG_TAG, "failed to download image: " + imageUrl, e);
    		
        	AlertDialog dialog  = new AlertDialog.Builder(this).create();
        	dialog.setMessage("Error downloading image: " + e.getMessage());
        	dialog.show();
    	}
    	
    	ImageView img = (ImageView) findViewById(R.id.img);
        img.setImageBitmap(image);
    	
        // Insert image into Android Gallery
        addImageToLibrary(image);
    }
    
	private Bitmap downloadImage(String imageUrl) throws Exception {
		URL myFileUrl = new URL(imageUrl);
		HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
		conn.setDoInput(true);
		conn.connect();
		InputStream is = conn.getInputStream();

		return BitmapFactory.decodeStream(is);
	}
	
	private void addImageToLibrary(Bitmap sourceBitmap){
		String imageName = "road_trip_1";
		
		File file = new File(getSaveDir(), imageName + ".jpg");
		
		ContentValues values = new ContentValues(7);
		values.put(Media.DISPLAY_NAME, imageName);
		values.put(Media.TITLE, "Road Trip Title");
		values.put(Media.DESCRIPTION, "Day 1, trip to Los Angeles");
		values.put(Media.MIME_TYPE, "image/jpeg");
		values.put(Media.BUCKET_ID, Environment.getExternalStorageDirectory().hashCode());
		values.put(Media.BUCKET_DISPLAY_NAME, FPS_PHOTO_DIR);
		values.put("_data", file.getAbsolutePath());

		Uri uri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
		
		try {
			//TODO find a way to do this that isnt lossy
		    OutputStream outStream = getContentResolver().openOutputStream(uri);
		    sourceBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
		    outStream.close();
		} catch (Exception e) {
			//TODO add better error handling
		    Log.e(LOG_TAG, "exception while writing image", e);
		}
	}

	private File getSaveDir() {
		File saveDir = new File(Environment.getExternalStorageDirectory(),
				FPS_PHOTO_DIR);
		Log.d(LOG_TAG, "saveDir: " + saveDir);

		if (!saveDir.isDirectory() && !saveDir.mkdirs()) {
			saveDir = null;
			throw new IllegalStateException("couldn't mkdirs ");
		}
		return saveDir;
	}

}