package com.fps.flickr.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.fps.FPSContants;
import com.fps.flickr.FlickrException;
import com.fps.flickr.FlickrRestResource;

@SuppressWarnings("serial")
public class PhotoSet extends FlickrRestResource {

	private String title;
	private String id;
	private int photoCount;
	private List<Photo> photos = null;
	
	public PhotoSet(){
		
	}
	
	public PhotoSet(String title, String id, int photoCount) {
		super();
		this.title = title;
		this.id = id;
		this.photoCount = photoCount;
	}

	public static List<PhotoSet> findForUser(String userId) throws FlickrException {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("user_id", userId);
		JSONObject result = getFlickrResource("flickr.photosets.getList", params);
		try {
			if(result == null || !result.getString("stat").equals(OK_STATUS)){
				Log.e(FPSContants.LOG_TAG, "Failed to photosets for user: " + userId);
				return null;
			}
			
			JSONArray jsonPhotoSets = result.getJSONObject("photosets").getJSONArray("photoset");
			List<PhotoSet> photoSets = new ArrayList<PhotoSet>();
			for (int i = 0; i < jsonPhotoSets.length(); i++) {
				JSONObject jsonPhotoSet = (JSONObject) jsonPhotoSets.get(i);
				photoSets.add(new PhotoSet(jsonPhotoSet.getJSONObject("title").getString("_content"), 
										   jsonPhotoSet.getString("id"), 
										   jsonPhotoSet.getInt("photos")));
			}
			
			return photoSets;
		} catch (JSONException e) {
			Log.e(FPSContants.LOG_TAG, "Error parsing photoset json: " + result.toString());
			throw new FlickrException("Error parsing result", e);
		}
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getPhotoCount() {
		return photoCount;
	}
	public void setPhotoCount(int photoCount) {
		this.photoCount = photoCount;
	}
	
	public List<Photo> getPhotos(){
		if(photos == null){
			loadPhotos();
		}
		return photos;
	}
	
	public void loadPhotos(){
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("photoset_id", id);
			params.put("extras", "url_sq,url_m,date_taken");
			params.put("media", "photos");
			JSONObject jsonPhotoSet = getFlickrResource("flickr.photosets.getPhotos", params);
			JSONArray jsonPhotos = jsonPhotoSet.getJSONObject("photoset").getJSONArray("photo");
			photos = new ArrayList<Photo>();
			for (int i = 0; i < jsonPhotos.length(); i++) {
				JSONObject jsonPhoto = (JSONObject) jsonPhotos.get(i);
				photos.add(new Photo(jsonPhoto, this));
			}
			Log.d(FPSContants.LOG_TAG, "Loaded info for " + photos.size() + " photos for set " + getTitle());
		} catch (FlickrException e) {
			Log.e(FPSContants.LOG_TAG, "Failed to load photos for photoset: " + getTitle(), e);
			photos = null;
		} catch (JSONException e) {
			Log.e(FPSContants.LOG_TAG, "Failed to load photos for photoset: " + getTitle(), e);
			photos = null;
		}
	}
	
}
