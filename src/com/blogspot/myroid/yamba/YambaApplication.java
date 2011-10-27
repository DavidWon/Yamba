package com.blogspot.myroid.yamba;

import winterwell.jtwitter.OAuthSignpostClient;
import winterwell.jtwitter.Twitter;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

public class YambaApplication extends Application 
	implements OnSharedPreferenceChangeListener {
	
	public static final String OAUTH_KEY = "2w3utncaYHBtu0Qt1e8i6Q";
	public static final String OAUTH_SECRET = "4IcIBholO7R5tJFdSygyZsy2ohxQpATBRHEPHWXVX0";
	
//	private static final String ACCESS_TOKEN = "221538550-2BSqIDmwIU6n4jiuV7md9MV8d5hUM2QKBJtWzw8R";
//	private static final String ACCESS_TOKEN_SECRET = "O2JPxZXQyr8kpLtRIajFwTtFYX8tgfJJkstBbiIM";
	
	// Log의 태그 설정
	private static final String TAG = 
			YambaApplication.class.getSimpleName();
	
	public Twitter twitter;
	private SharedPreferences prefs;
	
	private OAuthSignpostClient oauthClient;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		prefs = 
			PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
				
		Log.i(TAG, "onCreate");
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		Log.i(TAG, "onTerminate");
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.i(TAG, "onSharedPreferenceChanged");
	}
	
	public synchronized Twitter getTwitter() {
		if (twitter == null) {
			String token = prefs.getString("token", null);
			String tokenSecret = prefs.getString("tokenSecret", null);
			if (token != null && tokenSecret != null) {
				oauthClient = 
		        		new OAuthSignpostClient(OAUTH_KEY, 
		        				OAUTH_SECRET, 
		        				token, 
		        				tokenSecret);
			}
			
			String username = prefs.getString("username", null);			
			twitter = new Twitter(username, oauthClient);
		}
		return twitter;
	}
}
