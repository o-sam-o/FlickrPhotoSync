package com.fps.flickr.resource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.fps.FlickrPhotoSync;

public class Photo {
	
	public static String SQUARE_TYPE = "sq";
	public static String MEDIUM_TYPE = "m";
	
	private String id;
	private String title;
	private Date dateTaken; 
	private PhotoSet photoSet;
	private List<PhotoUrl> photoUrls;

	public Photo(JSONObject json, PhotoSet photoSet) throws JSONException {
		this.id = json.getString("id");
		this.title = json.getString("title");
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			this.dateTaken = formatter.parse(json.getString("datetaken"));
		} catch (ParseException e) {
			Log.e(FlickrPhotoSync.LOG_TAG, "Failed to parse datetaken: " + json.getString("datetaken"), e);
		}
		this.photoUrls = new ArrayList<PhotoUrl>();
		this.photoUrls.add(new PhotoUrl(SQUARE_TYPE, json));
		this.photoUrls.add(new PhotoUrl(MEDIUM_TYPE, json));
		this.photoSet = photoSet;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getDateTaken() {
		return dateTaken;
	}

	public void setDateTaken(Date dateTaken) {
		this.dateTaken = dateTaken;
	}

	public List<PhotoUrl> getPhotoUrls() {
		return photoUrls;
	}

	public void setPhotoUrls(List<PhotoUrl> photoUrls) {
		this.photoUrls = photoUrls;
	}

	public PhotoSet getPhotoSet() {
		return photoSet;
	}

	public void setPhotoSet(PhotoSet photoSet) {
		this.photoSet = photoSet;
	}

	public PhotoUrl getPhotoUrl(String type){
		for(PhotoUrl url : photoUrls){
			if(url.getType() == type){
				return url;
			}
		}
		return null;
	}
	
	public String getLogName() {
		return getTitle() + " (" + getId() + ")";
	}	
	
	public class PhotoUrl {
		
		private String url;
		private int height;
		private int width;
		private String type;
		
		public PhotoUrl(String type, JSONObject json) throws JSONException {
			this.type = type;
			this.url = json.getString("url_" + type);
			this.width = json.getInt("width_" + type);
			this.height = json.getInt("height_" + type);
		}
		
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		public int getHeight() {
			return height;
		}
		public void setHeight(int height) {
			this.height = height;
		}
		public int getWidth() {
			return width;
		}
		public void setWidth(int width) {
			this.width = width;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		
		
	}

}
