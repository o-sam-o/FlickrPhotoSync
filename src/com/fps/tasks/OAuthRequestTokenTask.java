package com.fps.tasks;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.fps.FPSContants;

/**
 * Source: https://github.com/ddewaele/AndroidOAuthFlowSample/blob/master/src/com/ecs/android/oauth/OAuthRequestTokenTask.java
 */
public class OAuthRequestTokenTask extends AsyncTask<Void, Void, Void> {

	private Context	context;
	private OAuthConsumer consumer;
	private OAuthProvider provider;
	
	/**
	 * 
	 * We pass the OAuth consumer and provider.
	 * 
	 * @param 	context
	 * 			Required to be able to start the intent to launch the browser.
	 * @param 	provider
	 * 			The OAuthProvider object
	 * @param 	consumer
	 * 			The OAuthConsumer object
	 */
	public OAuthRequestTokenTask(Context context, OAuthConsumer consumer, OAuthProvider provider) {
		this.context = context;
		this.consumer = consumer;
		this.provider = provider;
	}

	/**
	 * 
	 * Retrieve the OAuth Request Token and present a browser to the user to authorize the token.
	 * 
	 */
	@Override
	protected Void doInBackground(Void... params) {

		try {
			Log.i(FPSContants.LOG_TAG, "Retrieving request token from Flickr servers");
			
			final String url = provider.retrieveRequestToken(consumer, FPSContants.OAUTH_CALLBACK_URL);
			
			Log.i(FPSContants.LOG_TAG, "Popping a browser with the authorize URL : " + url);
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url)).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_FROM_BACKGROUND);
			context.startActivity(intent);

		} catch (Exception e) {
			Log.e(FPSContants.LOG_TAG, "Error during OAuth retrieve request token", e);
		}

		return null;
	}

}