package com.fps;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.fps.tasks.OAuthRequestTokenTask;
import com.fps.tasks.RetrieveAccessTokenTask;

/**
 * Source: https://github.com/ddewaele/AndroidOAuthFlowSample/blob/master/src/com/ecs/android/oauth/PrepareRequestTokenActivity.java
 */
public class PrepareRequestTokenActivity extends Activity {
	
    private OAuthConsumer consumer; 
    private OAuthProvider provider;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			System.setProperty("debug", "true");
			this.consumer = new CommonsHttpOAuthConsumer(
					FPSContants.FLICKR_KEY, FPSContants.FLICKR_SECRET);
			provider = new CommonsHttpOAuthProvider(
					FPSContants.FLICKR_REQUEST_TOKEN_URL,
					FPSContants.FLICKR_OAUTH_ACCESS_URL,
					FPSContants.FLICKR_AUTHORISE_URL);
			provider.setOAuth10a(true);
		} catch (Exception e) {
			// TODO better error handling
			Log.e(FPSContants.LOG_TAG, "Error creating consumer / provider", e);
		}

		Log.i(FPSContants.LOG_TAG, "Starting task to retrieve request token.");
		new OAuthRequestTokenTask(this, consumer, provider).execute();
	}
	
	/**
	 * Called when the OAuthRequestTokenTask finishes (user has authorized the request token).
	 * The callback URL will be intercepted here.
	 */
	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent); 
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final Uri uri = intent.getData();
		if (uri != null && uri.getScheme().equals(FPSContants.OAUTH_CALLBACK_SCHEME)) {
			Log.i(FPSContants.LOG_TAG, "Callback received : " + uri);
			Log.i(FPSContants.LOG_TAG, "Retrieving Access Token");
			new RetrieveAccessTokenTask(this, consumer, this.provider, prefs).execute(uri);
			finish();	
		}
	}
	
}
