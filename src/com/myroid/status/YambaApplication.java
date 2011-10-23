package com.myroid.status;

import winterwell.jtwitter.OAuthSignpostClient;
import winterwell.jtwitter.Twitter;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

public class YambaApplication extends Application 
	implements OnSharedPreferenceChangeListener {
	
	private static final String OAUTH_KEY = "ylRkvXOBEG8CimACeY4Mg";
	private static final String OAUTH_SECRET = "T2M9cl5e9GmYLNW0VNaqnorEamMI4Oi81WY7XNpAoU";
	
	private static final String ACCESS_TOKEN = "221538550-2BSqIDmwIU6n4jiuV7md9MV8d5hUM2QKBJtWzw8R";
	private static final String ACCESS_TOKEN_SECRET = "O2JPxZXQyr8kpLtRIajFwTtFYX8tgfJJkstBbiIM";
	
	private OAuthSignpostClient oauthClient;
	
	// Log의 태그 설정
	private static final String TAG = 
			YambaApplication.class.getSimpleName();
	
	public Twitter twitter;
	private SharedPreferences prefs;
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		oauthClient = 
        		new OAuthSignpostClient(OAUTH_KEY, 
        				OAUTH_SECRET, 
        				ACCESS_TOKEN, 
        				ACCESS_TOKEN_SECRET);
		
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
			String username = prefs.getString("username", null);			
			twitter = new Twitter(username, oauthClient);
		}
		return twitter;
	}
}
