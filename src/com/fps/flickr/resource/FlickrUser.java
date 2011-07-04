package com.fps.flickr.resource;

import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.util.Log;

import com.fps.FPSContants;
import com.fps.flickr.FlickrException;
import com.fps.flickr.FlickrRestResource;

@SuppressWarnings("serial")
public class FlickrUser extends FlickrRestResource {

	private String username;
	private String id;
	private List<PhotoSet> photoSets = null;
	
	public static FlickrUser findByUsername(String username, SharedPreferences prefs) throws FlickrException {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("username", username);
		JSONObject result = getFlickrResource("flickr.people.findByUsername", params, prefs);
		try {
			if(result == null || !result.getString("stat").equals(OK_STATUS)){
				Log.e(FPSContants.LOG_TAG, "Failed to get flickr user: " + username);
				return null;
			}
			return new FlickrUser(username, result.getJSONObject("user").getString("id"), prefs);
		} catch (JSONException e) {
			Log.e(FPSContants.LOG_TAG, "Error parsing find user json: " + result.toString());
			throw new FlickrException("Error parsing result", e);
		}
	}
	
	public FlickrUser(){
		
	}
	
	public FlickrUser(String username, String id, SharedPreferences prefs)  throws FlickrException {
		this.username = username;
		this.id = id;
		this.photoSets = PhotoSet.findForUser(id, prefs);
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public List<PhotoSet> getPhotoSets() {
		return photoSets;
	}
	
}
