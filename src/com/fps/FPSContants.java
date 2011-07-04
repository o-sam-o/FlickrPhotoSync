package com.fps;

public class FPSContants {

	public static final String FLICKR_KEY = "ef9ad4ef689af505cde45ec1dc31120f";
	public static final String FLICKR_SECRET = "87f2991454bbb9a0";
	
	public static final String	OAUTH_CALLBACK_SCHEME	= "x-oauthflow";
	public static final String	OAUTH_CALLBACK_HOST		= "callback";
	public static final String	OAUTH_CALLBACK_URL		= OAUTH_CALLBACK_SCHEME + "://" + OAUTH_CALLBACK_HOST;
	
	public static final String FLICKR_REQUEST_TOKEN_URL = "http://www.flickr.com/services/oauth/request_token";
	public static final String FLICKR_AUTHORISE_URL = "http://www.flickr.com/services/oauth/authorize";
	public static final String FLICKR_OAUTH_ACCESS_URL = "http://www.flickr.com/services/oauth/access_token";

	
	public static final String LOG_TAG = "FlickrPhotoSyncApp";
	public static final String FLICKR_USER_EXTRA = "FlickrUserExtra";
	public static final String FPS_PHOTO_DIR = "flickr";
	public static final int MAX_FILENAME_LENGTH = 50;
	public static String BASE_FLICKR_URL = "http://api.flickr.com/services/rest/";
	
}
