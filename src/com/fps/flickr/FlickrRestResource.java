package com.fps.flickr;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

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

import android.util.Log;

public abstract class FlickrRestResource {

	protected static String OK_STATUS = "ok";
	
	protected static String BASE_FLICKR_URL = "http://api.flickr.com/services/rest/";
	protected static String FLICKR_API_KEY = "ef04ab7614a3d621695285b69ba87a2f";
	
	protected static JSONObject getFlickrResource(String method, Map<String, String> params) throws FlickrException {
		try {
			HttpGet request = new HttpGet();
			request.setURI(new URI(addParamsToUrl(method, params)));
			
			String response = httpResponseString(request);
			if (response == null){
				return null;
			}
			Log.i("test", response);
			
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
		writer.append(BASE_FLICKR_URL);
		writer.append("?method=").append(method);
		writer.append("&format=json");
		writer.append("&nojsoncallback=1");
		writer.append("&api_key=").append(FLICKR_API_KEY);
		
        for(Map.Entry<String, String> param : params.entrySet()){
        	writer.append("&" + param.getKey() + "=").append(param.getValue());
        }
		return writer.toString();
	}
	
}
