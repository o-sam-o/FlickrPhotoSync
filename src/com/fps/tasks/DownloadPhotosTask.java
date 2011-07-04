package com.fps.tasks;

import java.io.File;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.R;
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

import com.fps.FPSContants;
import com.fps.flickr.resource.Photo;
import com.fps.flickr.resource.PhotoSet;

public class DownloadPhotosTask extends AsyncTask<PhotoSet, Integer, Integer> {

	private Activity context;
	private ProgressDialog dialog;
	private int photosDownloaded = 0;
	private int alreadyExistsCount = 0;
	private int errorCount = 0;
	
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
			List<Photo> photos = photoset.getPhotos(context);
			if(photos == null){
				Log.e(FPSContants.LOG_TAG, "Failed to download photos info for photoset : " + photoset.getTitle());
				errorCount += photoset.getPhotoCount();
				updateProgressIndicator();
				continue;
			}
			
			for(Photo photo : photos){
				if(isCancelled()){
					return getImagesProcessed();
				}
				ExistingFlickrPhotoRef existingPhoto = alreadyDownloaded.get(photo.getId());
				//TODO add support for updating
				if(existingPhoto != null){
					Log.i(FPSContants.LOG_TAG, "Skipping " + photo.getLogName() + " as already downloaded");
					alreadyExistsCount++;
					updateProgressIndicator();
					continue;
				}
				
				Photo.PhotoUrl photoUrl = photo.getPhotoUrl(Photo.MEDIUM_TYPE);
				if(photoUrl == null){
					Log.e(FPSContants.LOG_TAG, "Unable to find medium photo for photo " + photo.getLogName());
					continue;
				}
				
				try {
					addImageToLibrary(photo, photoUrl.getUrl());
					photosDownloaded++;
					updateProgressIndicator();
				} catch (Exception e) {
					errorCount++;
					Log.e(FPSContants.LOG_TAG, "Failed to download picture: " + photo.getLogName(), e);
				}
			}
			
		}
		return getImagesProcessed();
	}

	private void updateProgressIndicator() {
		dialog.setProgress(getImagesProcessed());
	}
	
	private int getImagesProcessed(){
		return photosDownloaded + errorCount + alreadyExistsCount;	
	}

	private Map<String, ExistingFlickrPhotoRef> findFlickrImages(String setName){
		Log.d(FPSContants.LOG_TAG, "Finding existing flickr photos for set: "+ setName);
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
	            
	            Log.d(FPSContants.LOG_TAG, "Existing Photo: " + name);
	            Log.d(FPSContants.LOG_TAG, "Content Id: " + contentId);
	            Log.d(FPSContants.LOG_TAG, "Flickr Id: " + flickrId);
	            
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
		alert.setIcon(R.drawable.ic_dialog_info);
		alert.setTitle("Download Complete");	
		alert.setMessage(getCompletionMessage());
		dialog.dismiss();
		alert.show();
	}
	
	private String getCompletionMessage(){
		StringWriter message = new StringWriter();
		
		message.append("Downloaded " + photosDownloaded + " " + puralize("photo", photosDownloaded));
		if(alreadyExistsCount != 0){
			message.append(", skipped " + alreadyExistsCount + " existing " + puralize("photo", alreadyExistsCount));
		}
		if(errorCount != 0){
			message.append(", failed to download " + errorCount + " " + puralize("photo", errorCount));
		}
		message.append(".");
		
		return message.toString();
	}
	
	private String puralize(String text, int count){
		if(count == 1){
			return text;
		}else{
			return text + "s";
		}
	}
	
	@Override
	protected void onPreExecute() {
		photosDownloaded = 0;
		dialog = new ProgressDialog(context);
		dialog.setTitle("Downloading photos");
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
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
		Log.d(FPSContants.LOG_TAG, "Uri: " + uri.toString());
		try {
			Log.d(FPSContants.LOG_TAG, "Downloading image: " + photo.getLogName());
			outStream = context.getContentResolver().openOutputStream(uri);
			new DefaultHttpClient().execute(new HttpGet(photoUrl)).getEntity().writeTo(outStream);
		} catch (Exception e) {
			//FIXME do we need to delete the entry in the content resolver?
		    Log.e(FPSContants.LOG_TAG, "exception while writing photo " + photo.getLogName(), e);
		    throw e;
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
		return new File(Environment.getExternalStorageDirectory(), FPSContants.FPS_PHOTO_DIR);
	}
	
	private String generateFileName(Photo photo){
		String photoName = (photo.getTitle() + '_' + photo.getId()).replaceAll("[^a-zA-Z0-9_]", "");
		if(photoName.length() > FPSContants.MAX_FILENAME_LENGTH){
			photoName = photoName.substring(0, FPSContants.MAX_FILENAME_LENGTH);
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
		@SuppressWarnings("unused")
		public String getContentId() {
			return contentId;
		}
		@SuppressWarnings("unused")
		public String getFilePath() {
			return filePath;
		}
	}
}
