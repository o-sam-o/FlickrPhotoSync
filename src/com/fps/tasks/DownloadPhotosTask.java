package com.fps.tasks;

import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Media;
import android.util.Log;

import com.fps.FlickrPhotoSync;
import com.fps.flickr.resource.Photo;
import com.fps.flickr.resource.PhotoSet;

public class DownloadPhotosTask extends AsyncTask<PhotoSet, Integer, Integer> {

	private static final int MAX_FILENAME_LENGTH = 50;
	
	private Activity context;
	private ProgressDialog dialog;
	private int photosDownloaded;
	
	public DownloadPhotosTask(Activity context){
		this.context = context;
	}
	
	@Override
	protected Integer doInBackground(PhotoSet... photosets) {
		int totalPhotoCount = 0;
		for(PhotoSet photoset : photosets){
			totalPhotoCount += photoset.getPhotoCount();
		}
		dialog.setMax(totalPhotoCount);
		
		for(PhotoSet photoset : photosets){
			Map<String, ExistingFlickrPhotoRef> alreadyDownloaded = findFlickrImages(photoset.getTitle());
			//TODO add error handling
			if(photoset.getPhotos() != null){
				for(Photo photo : photoset.getPhotos()){
					if(isCancelled()){
						return photosDownloaded;
					}
					ExistingFlickrPhotoRef existingPhoto = alreadyDownloaded.get(photo.getId());
					//TODO add support for updating
					if(existingPhoto != null){
						Log.i(FlickrPhotoSync.LOG_TAG, "Skipping " + photo.getLogName() + " as already downloaded");
						continue;
					}
					
					Photo.PhotoUrl photoUrl = photo.getPhotoUrl(Photo.MEDIUM_TYPE);
					if(photoUrl == null){
						Log.e(FlickrPhotoSync.LOG_TAG, "Unable to find medium photo for photo " + photo.getLogName());
						continue;
					}
					try {
						addImageToLibrary(photo, photoUrl.getUrl());
						photosDownloaded++;
						dialog.setProgress(photosDownloaded);
					} catch (Exception e) {
						// TODO add better error handling
						Log.e(FlickrPhotoSync.LOG_TAG, "Failed to download picture: " + photo.getLogName(), e);
					}
				}
			}
		}
		return photosDownloaded;
	}

	private Map<String, ExistingFlickrPhotoRef> findFlickrImages(String setName){
		Log.d(FlickrPhotoSync.LOG_TAG, "Finding existing flickr photos for set: "+ setName);
		Cursor cur = context.managedQuery(Images.Media.EXTERNAL_CONTENT_URI, null, Media.BUCKET_DISPLAY_NAME + " = '" + setName + "'", null, null);
		Map<String, ExistingFlickrPhotoRef>  result = new HashMap<String, ExistingFlickrPhotoRef>();
	    if (cur.moveToFirst()) {

	        int nameColumn = cur.getColumnIndex(Media.DISPLAY_NAME); 
	        int idColumn = cur.getColumnIndex(Media._ID); 
	        int dataColumn = cur.getColumnIndex("_data"); 
	    
	        do {
	            String name = cur.getString(nameColumn);
	            String dataPath = cur.getString(dataColumn);
	            String flickrId = getFlickrIdFromFilePath(dataPath);
	            String contentId = cur.getString(idColumn);
	            
	            Log.d(FlickrPhotoSync.LOG_TAG, "Existing Photo: " + name);
	            Log.d(FlickrPhotoSync.LOG_TAG, "Content Id: " + contentId);
	            Log.d(FlickrPhotoSync.LOG_TAG, "Flickr Id: " + flickrId);
	            
	            result.put(flickrId, new ExistingFlickrPhotoRef(contentId, dataPath));
	        } while (cur.moveToNext());

	    }
		return result;
	}
	
	private String getFlickrIdFromFilePath(String filePath){
		return filePath.substring(filePath.lastIndexOf("_") + 1, filePath.lastIndexOf("."));
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
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setCancelable(false);
		dialog.show();
	}
	
	private void addImageToLibrary(Photo photo, String photoUrl) throws Exception {
		String photoSetName = photo.getPhotoSet().getTitle();
		File file = new File(getSaveDir(photoSetName), generateFileName(photo) + getPhotoExtention(photoUrl));
		
		ContentValues values = new ContentValues(6);
		values.put(Media.DISPLAY_NAME, photo.getTitle());
		values.put(Media.TITLE, photo.getTitle());
		values.put(Media.DATE_TAKEN, photo.getDateTaken().getTime());
		// TODO add support for other content types
		values.put(Media.MIME_TYPE, "image/jpeg");
		// TODO add geo
		values.put(Media.BUCKET_ID, getBucketId());
		values.put(Media.BUCKET_DISPLAY_NAME, photoSetName);
		values.put("_data", file.getAbsolutePath());
		
		OutputStream outStream = null;
		Uri uri = context.getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
		Log.d(FlickrPhotoSync.LOG_TAG, "Uri: " + uri.toString());
		try {
			Log.d(FlickrPhotoSync.LOG_TAG, "Downloading image: " + photo.getLogName());
			outStream = context.getContentResolver().openOutputStream(uri);
			new DefaultHttpClient().execute(new HttpGet(photoUrl)).getEntity().writeTo(outStream);
		} catch (Exception e) {
			//TODO add better error handling
		    Log.e(FlickrPhotoSync.LOG_TAG, "exception while writing photo " + photo.getLogName(), e);
		}finally{
			if(outStream != null){
				outStream.close();
			}
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
	
	private String getBucketId() {
		return ((Integer)getStorageFolder().getAbsolutePath().hashCode()).toString();
	}
	
	private class ExistingFlickrPhotoRef {
		private String contentId;
		private String filePath;
		public ExistingFlickrPhotoRef(String contentId, String filePath) {
			super();
			this.contentId = contentId;
			this.filePath = filePath;
		}
		public String getContentId() {
			return contentId;
		}
		public String getFilePath() {
			return filePath;
		}
	}
}
