package com.blogspot.myroid.yamba;

import java.util.List;

import winterwell.jtwitter.OAuthSignpostClient;
import winterwell.jtwitter.Twitter;
import android.app.Application;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;
import winterwell.jtwitter.Status;

public class YambaApplication extends Application 
	implements OnSharedPreferenceChangeListener {
	
	public static final String OAUTH_KEY = "2w3utncaYHBtu0Qt1e8i6Q";
	public static final String OAUTH_SECRET = "4IcIBholO7R5tJFdSygyZsy2ohxQpATBRHEPHWXVX0";
	
//	private static final String ACCESS_TOKEN = "221538550-2BSqIDmwIU6n4jiuV7md9MV8d5hUM2QKBJtWzw8R";
//	private static final String ACCESS_TOKEN_SECRET = "O2JPxZXQyr8kpLtRIajFwTtFYX8tgfJJkstBbiIM";
	
	private boolean serviceRunning;
	
	private StatusData statusData;
	
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
				
		statusData = new StatusData(getApplicationContext());
		
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
	
	public void setServiceRunning(boolean serviceRunning) {
		this.serviceRunning = serviceRunning;
	}
	
	public boolean isServiceRunning() {
		return this.serviceRunning;
	}
	
	public StatusData getStatusData() {
		return this.statusData;
	}
	
	public synchronized int fetchStatusUpdates() {
		Log.d(TAG, "Fetching status updates");
		Twitter twitter = this.getTwitter();
		
		if (twitter == null) {
			Log.d(TAG, "Twitter connection info not initialized");
			return 0;
		}
		
		try {
			List<Status> statusUpdates = twitter.getHomeTimeline();
			long latestSatusCreatedAtTime = this.getStatusData()
					.getLatestStatusCreatedAtTime();
			int count = 0;
			ContentValues values = new ContentValues();
			for (Status status : statusUpdates) {
				values.put(StatusData.C_ID, status.getId().intValue());
				long createdAt = status.getCreatedAt().getTime();
				values.put(StatusData.C_CREATED_AT, createdAt);
				values.put(StatusData.C_TEXT, status.getText());
				values.put(StatusData.C_USER, status.getUser().getName());
				Log.d(TAG, "Got update with id " + 
							status.getId() + ". Saving");
				this.getStatusData().insertOrIgnore(values);
				if (latestSatusCreatedAtTime < createdAt) {
					count++;
				}
			}
			Log.d(TAG, count > 0 ? "Got " + count + " status updates" 
					: "No new status updates");
			return count;
		} catch (RuntimeException e) {
			Log.e(TAG, "Failed to fetch status updates", e);
			return 0;
		}
	}
}
