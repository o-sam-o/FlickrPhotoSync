package com.fps;

import oauth.signpost.OAuth;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.fps.flickr.resource.FlickrUser;
import com.fps.tasks.LoadPhotoSetsTask;

public class FlickrPhotoSync extends Activity {
	private static final int DO_OAUTH_REQUEST_CODE = 101;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if(loggedIn()){
        	setToggleAsLoggedIn();
        }
    }
    
    public void loadPhotoSets(View view){
    	new LoadPhotoSetsTask(this).execute(getUsername());
    }
    
    public void toggleLogin(View view){
    	if(loggedIn()){
    		clearCredentials();
    		getLoginToggleButton().setChecked(false);
    	}else{
    		getLoginToggleButton().setTextOn("Login ...");    	
    		startActivityForResult(new Intent().setClass(this, PrepareRequestTokenActivity.class), DO_OAUTH_REQUEST_CODE);
    	}
    }
    
    private void setToggleAsLoggedIn(){
    	Log.d(FPSContants.LOG_TAG, "Logged in as " + getLoggedInUserName());
    	getLoginToggleButton().setTextOn("Logged in as " + getLoggedInUserName());    	
    	getLoginToggleButton().setChecked(true);
    	((EditText)findViewById(R.id.flickrUsername)).setText(getLoggedInUserName());
    }
    
    private void clearCredentials() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final Editor edit = prefs.edit();
		edit.remove(OAuth.OAUTH_TOKEN);
		edit.remove(OAuth.OAUTH_TOKEN_SECRET);
		edit.remove(FPSContants.OAUTH_USERNAME);
		edit.commit();
	}
    
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
    	Log.d(FPSContants.LOG_TAG, "Handling oauth activity response");
		super.onActivityResult(reqCode, resultCode, data);
		if(reqCode == DO_OAUTH_REQUEST_CODE && loggedIn()){
			setToggleAsLoggedIn();
			new LoadPhotoSetsTask(this).execute(getUsername());
		}
    }
    
    private String getUsername(){
    	return ((EditText)findViewById(R.id.flickrUsername)).getText().toString();
    }
    
    public void setMessage(String message){
    	((TextView)findViewById(R.id.flickrId)).setText(message);
    }
    
    private ToggleButton getLoginToggleButton(){
    	return (ToggleButton)findViewById(R.id.loginToggleButton);
    }
    
    public void displayUsersPhotoSets(FlickrUser flickrUser){
    	Intent intent = new Intent(this, PhotoSetsActivity.class);
    	intent.putExtra(FPSContants.FLICKR_USER_EXTRA, flickrUser);
    	startActivityForResult(intent, 0);
    }
    
    private boolean loggedIn(){
    	return getLoggedInUserName().length() != 0;
    }
    
    private String getLoggedInUserName(){
    	return PreferenceManager.getDefaultSharedPreferences(this).getString(FPSContants.OAUTH_USERNAME, "");
    }
}