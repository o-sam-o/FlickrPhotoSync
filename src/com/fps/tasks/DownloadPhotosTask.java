package com.fps.tasks;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore.Images.Media;
import android.util.Log;

import com.fps.FlickrPhotoSync;
import com.fps.flickr.resource.Photo;
import com.fps.flickr.resource.PhotoSet;

public class DownloadPhotosTask extends AsyncTask<PhotoSet, Integer, Integer> {

	private static final int MAX_FILENAME_LENGTH = 50;
	
	private Context context;
	private ProgressDialog dialog;
	private int photosDownloaded;
	
	public DownloadPhotosTask(Context context){
		this.context = context;
	}
	
	@Override
	protected Integer doInBackground(PhotoSet... photosets) {
		for(PhotoSet photoset : photosets){
			//TODO add error handling
			if(photoset.getPhotos() != null){
				for(Photo photo : photoset.getPhotos()){
					if(isCancelled()){
						return photosDownloaded;
					}
					
					Photo.PhotoUrl photoUrl = photo.getPhotoUrl(Photo.MEDIUM_TYPE);
					if(photoUrl == null){
						Log.e(FlickrPhotoSync.LOG_TAG, "Unable to find medium photo for photo " + photo.getTitle() + " (" + photo.getId() + ")");
						continue;
					}
					try {
						addImageToLibrary(photo, photoUrl.getUrl());
						photosDownloaded++;
					} catch (Exception e) {
						// TODO add better error handling
						Log.e(FlickrPhotoSync.LOG_TAG, "Failed to download picture: " + photo.getTitle() + " (" + photo.getId() + ")", e);
					}
				}
			}
		}
		return photosDownloaded;
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		AlertDialog alert = new AlertDialog.Builder(context).create();
		alert.setMessage("Downloaded " + result + " photos.");
		dialog.dismiss();
		alert.show();
	}
	
	@Override
	protected void onPreExecute() {
		photosDownloaded = 0;
		dialog = new ProgressDialog(context);
		dialog.setMessage("Downloading photos");
		dialog.show();
	}
	
	private Bitmap downloadImage(String imageUrl) throws Exception {
		URL myFileUrl = new URL(imageUrl);
		HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
		conn.setDoInput(true);
		conn.connect();
		InputStream is = conn.getInputStream();

		return BitmapFactory.decodeStream(is);
	}
	
	private void addImageToLibrary(Photo photo, String photoUrl) throws Exception {
		String photoSetName = photo.getPhotoSet().getTitle();
		File file = new File(getSaveDir(photoSetName), generateFileName(photo) + getPhotoExtention(photoUrl));
		
		ContentValues values = new ContentValues(6);
		values.put(Media.DISPLAY_NAME, photo.getTitle());
		values.put(Media.TITLE, photo.getTitle());
		values.put(Media.DATE_TAKEN, photo.getDateTaken().getTime());
		values.put(Media.MIME_TYPE, "image/jpeg");
		values.put(Media.BUCKET_ID, getStorageFolder().getAbsolutePath().hashCode());
		values.put(Media.BUCKET_DISPLAY_NAME, photoSetName);
		values.put("_data", file.getAbsolutePath());

		Uri uri = context.getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
		
		try {
			//TODO find a way to do this that isnt lossy
		    OutputStream outStream = context.getContentResolver().openOutputStream(uri);
		    downloadImage(photoUrl).compress(Bitmap.CompressFormat.JPEG, 100, outStream);
		    outStream.close();
		} catch (Exception e) {
			//TODO add better error handling
		    Log.e(FlickrPhotoSync.LOG_TAG, "exception while writing photo " + photo.getTitle() + " (" + photo.getId() + ")", e);
		}
	}

	private File getSaveDir(String setName) {
		File saveDir = new File(getStorageFolder(), setName);

		if (!saveDir.isDirectory() && !saveDir.mkdirs()) {
			saveDir = null;
			throw new IllegalStateException("couldn't mkdirs ");
		}
		return saveDir;
	}

	private File getStorageFolder(){
		return new File(Environment.getExternalStorageDirectory(), FlickrPhotoSync.FPS_PHOTO_DIR);
	}
	
	private String generateFileName(Photo photo){
		String photoName = (photo.getTitle() + '_' + photo.getId()).replaceAll("[^a-zA-Z0-9_]", "");
		if(photoName.length() > MAX_FILENAME_LENGTH){
			photoName = photoName.substring(0, MAX_FILENAME_LENGTH);
		}
		return photoName;
	}
	
	private String getPhotoExtention(String url){
		return url.substring(url.lastIndexOf("."), url.length());
	}
}
