package com.fps;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

public class FlickrPhotoSync extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    public void loadPhotoSets(View view){
    	String imageUrl = ((EditText)findViewById(R.id.flickrUsername)).getText().toString();
    	Log.d("fps", "Downloading image: " + imageUrl);
    	
    	Bitmap image = null;
    	try {
    		image = downloadImage(imageUrl);
    	} catch (Exception e){
			//TODO add better error handling
    		Log.e("fps", "failed to download image: " + imageUrl, e);
    		
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
		ContentValues values = new ContentValues(3);
		values.put(Media.DISPLAY_NAME, "road_trip_1");
		values.put(Media.DESCRIPTION, "Day 1, trip to Los Angeles");
		values.put(Media.MIME_TYPE, "image/jpeg");

		Uri uri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
		try {
		    OutputStream outStream = getContentResolver().openOutputStream(uri);
		    sourceBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
		    outStream.close();
		} catch (Exception e) {
			//TODO add better error handling
		    Log.e("fps", "exception while writing image", e);
		}
	}

}