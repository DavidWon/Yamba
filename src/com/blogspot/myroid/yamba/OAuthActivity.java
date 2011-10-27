package com.blogspot.myroid.yamba;

import com.myroid.status.R;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import winterwell.jtwitter.OAuthSignpostClient;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

// OAuth 인증 처리가 끝난 이후에 preference에 사용자 키와 비밀키를 저장하고, 
// YambaApplication 클래스에 실제 twitter 인스턴스를 생성할때 여기서 저장한
// SharedPreference 값을 이용한다
public class OAuthActivity extends Activity {
	  private static final String TAG = "OAuthActivity";

	  private static final String OAUTH_CALLBACK_SCHEME = "x-myroid-oauth";
	  private static final String OAUTH_CALLBACK_URL = OAUTH_CALLBACK_SCHEME + "://callback";
	  
	  private OAuthSignpostClient oauthClient;
	  private OAuthConsumer mConsumer;
	  private OAuthProvider mProvider;
	  
	  SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.oauth);
		
		mConsumer = new DefaultOAuthConsumer(
				YambaApplication.OAUTH_KEY, YambaApplication.OAUTH_SECRET);
		mProvider = new DefaultOAuthProvider("https://api.twitter.com/oauth/request_token",
				"https://api.twitter.com/oauth/access_token",
				"https://api.twitter.com/oauth/authorize");
	
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
	}
	
	public void onClickAuthorize(View view) {
		new OAuthAuthorizeTask().execute();
	}
	
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.d(TAG, "Intent: " + intent);
		
		Uri uri = intent.getData();
		if (uri != null && uri.getScheme().equals(OAUTH_CALLBACK_SCHEME)) {
			Log.d(TAG, "callback: " + uri.getPath());
			
			String verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
			Log.d(TAG, "verifier: " + verifier);
			
			new RetrieveAccessTokenTask().execute(verifier);
		}
	}

	class OAuthAuthorizeTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			String authUrl;
			String message = null;
			try {
				// 내장 브라우저에서 인증을 실시한다. 한마디로 브라우저를 띄우고 설정할 수 있는 twitter 화면이 나온다.
				authUrl = mProvider.retrieveRequestToken(mConsumer, OAUTH_CALLBACK_URL);
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
				startActivity(intent);
			} catch (OAuthMessageSignerException e) {
				message = "OAuthMessageSignerException";
				e.printStackTrace();
			} catch (OAuthNotAuthorizedException e) {
				message = "OAuthNotAuthorizedException";
				e.printStackTrace();
			} catch (OAuthExpectationFailedException e) {
				message = "OAuthExpectationFailedException";
				e.printStackTrace();
			} catch (OAuthCommunicationException e) {
				message = "OAuthCommunicationException";
				e.printStackTrace();
			}
			
			return message;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result != null) {
				Toast.makeText(OAuthActivity.this, result, Toast.LENGTH_LONG).show();
			}
		}
	}
	
	class RetrieveAccessTokenTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			
			String verifier = params[0];
			String message = null;
			
	        try {
	        	// Get the token
		        Log.d(TAG, "mConsumer: "+mConsumer);
		        Log.d(TAG, "mProvider: "+mProvider);
		        
	        	mProvider.retrieveAccessToken(mConsumer, verifier);
		        String token = mConsumer.getToken();
		        String tokenSecret = mConsumer.getTokenSecret();
		        mConsumer.setTokenWithSecret(token, tokenSecret);

		        Log.d(TAG, String.format("verifier: %s, token: %s, tokenSecret: %s", verifier,
		            token, tokenSecret));

		        // Store token in prefs
		        prefs.edit().putString("token", token).putString("tokenSecret", tokenSecret)
		            .commit();
	        	
		        // 인증화면을 종료한다.
		        finish();
		        
			} catch (OAuthMessageSignerException e) {
				message = "OAuthMessageSignerException";
				e.printStackTrace();
			} catch (OAuthNotAuthorizedException e) {
				message = "OAuthNotAuthorizedException";
				e.printStackTrace();
			} catch (OAuthExpectationFailedException e) {
				message = "OAuthExpectationFailedException";
				e.printStackTrace();
			} catch (OAuthCommunicationException e) {
				message = "OAuthCommunicationException";
				e.printStackTrace();
			}
	        
			return message;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result != null) {
				Toast.makeText(OAuthActivity.this, result, Toast.LENGTH_LONG).show();
			}
		}
	}
}
