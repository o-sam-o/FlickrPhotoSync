package com.fps;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
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
    		Log.e("fps", "failed to download image: " + imageUrl, e);
    		
        	AlertDialog dialog  = new AlertDialog.Builder(this).create();
        	dialog.setMessage("Error downloading image: " + e.getMessage());
        	dialog.show();
    	}
    	
    	ImageView img = (ImageView) findViewById(R.id.img);
        img.setImageBitmap(image);
    	
    	//MediaStore.Images.Media.insertImage(arg0, arg1, arg2, arg3);
    }
    
	private Bitmap downloadImage(String imageUrl) throws Exception {
		URL myFileUrl = new URL(imageUrl);
		HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
		conn.setDoInput(true);
		conn.connect();
		InputStream is = conn.getInputStream();

		return BitmapFactory.decodeStream(is);
	}

}