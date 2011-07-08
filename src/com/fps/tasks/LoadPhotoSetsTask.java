package com.fps.tasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.fps.FPSContants;
import com.fps.FlickrPhotoSync;
import com.fps.flickr.FlickrException;
import com.fps.flickr.resource.FlickrUser;

public class LoadPhotoSetsTask extends AsyncTask<String, Integer, FlickrUser> {

	private Exception processException = null;
	private FlickrPhotoSync fps;
	ProgressDialog dialog;
	
	public LoadPhotoSetsTask(FlickrPhotoSync fps){
		this.fps = fps;
	}
	
	@Override
	protected FlickrUser doInBackground(String... usernames) {
		try {
			FlickrUser flickrUser = FlickrUser.findByUsername(usernames[0], PreferenceManager.getDefaultSharedPreferences(fps));
			Log.i(FPSContants.LOG_TAG, flickrUser.getId());
			Log.i(FPSContants.LOG_TAG, "Photosets: " + flickrUser.getPhotoSets().size());
			return flickrUser;
		} catch (FlickrException e) {
			Log.e(FPSContants.LOG_TAG, "Failed to load flickr user: " + usernames[0], e);
			processException = e;
			return null;
		}
	}

	@Override
	protected void onPostExecute(FlickrUser flickrUser) {
		dialog.dismiss();
		//FIXME displaying dialog from here doesnt seem to work
		if(processException != null){
			fps.setMessage("Error: " + processException.getMessage());
		}else if(flickrUser == null){
			fps.setMessage("Unable to find user");
		}else{
			fps.displayUsersPhotoSets(flickrUser);
		}
	}

	@Override
	protected void onPreExecute() {
		dialog = ProgressDialog.show(fps, "Getting photo info", "please wait...");
	}

}
