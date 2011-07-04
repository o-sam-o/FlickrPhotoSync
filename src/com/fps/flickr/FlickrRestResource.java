package com.fps.flickr;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.SharedPreferences;
import android.provider.SyncStateContract.Constants;
import android.util.Log;

import com.fps.FPSContants;
import com.fps.FlickrPhotoSync;

@SuppressWarnings("serial")
public abstract class FlickrRestResource implements Serializable {

	protected static String OK_STATUS = "ok";
	
	protected static JSONObject getFlickrResource(String method, Map<String, String> params) throws FlickrException {
		try {
			HttpGet request = new HttpGet();
			String requestUrl = addParamsToUrl(method, params); 
			Log.d(FPSContants.LOG_TAG, requestUrl);
			request.setURI(new URI(requestUrl));
			
			String response = httpResponseString(request);
			if (response == null){
				return null;
			}
			Log.d(FPSContants.LOG_TAG, response);
			
			return (JSONObject) new JSONTokener(httpResponseString(request)).nextValue();
		} catch (ClientProtocolException e) {
			throw new FlickrException("Failed to get flickr resource for method: " + method + " params: " + params, e);
		} catch (URISyntaxException e) {
			throw new FlickrException("Failed to get flickr resource for method: " + method + " params: " + params, e);
		} catch (IOException e) {
			throw new FlickrException("Failed to get flickr resource for method: " + method + " params: " + params, e);
		} catch (JSONException e) {
			throw new FlickrException("Failed to get flickr resource for method: " + method + " params: " + params, e);
		}
	}

	private static String httpResponseString(HttpUriRequest request) throws ClientProtocolException, IOException {
		
		HttpClient client = new DefaultHttpClient();
		HttpEntity entity = client.execute(request).getEntity();
		if (entity != null) {
			return EntityUtils.toString(entity);
		} else {
			return null;
		}
	}
	
	private static String addParamsToUrl(String method,
			Map<String, String> params) {
		StringWriter writer = new StringWriter();
		writer.append(FPSContants.BASE_FLICKR_URL);
		writer.append("?method=").append(method);
		writer.append("&format=json");
		writer.append("&nojsoncallback=1");
		writer.append("&api_key=").append(FPSContants.FLICKR_KEY);
		
        for(Map.Entry<String, String> param : params.entrySet()){
        	writer.append("&" + param.getKey() + "=").append(param.getValue());
        }
		return writer.toString();
	}
	
	private OAuthConsumer getConsumer(SharedPreferences prefs) {
		String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
		String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
		OAuthConsumer consumer = new CommonsHttpOAuthConsumer(FPSContants.FLICKR_KEY, FPSContants.FLICKR_SECRET);
		consumer.setTokenWithSecret(token, secret);
		return consumer;
	}
	
}
