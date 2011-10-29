package com.blogspot.myroid.yamba;

import winterwell.jtwitter.Twitter;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class StatusActivity extends Activity implements OnClickListener,
	TextWatcher {//
	private	static final String TAG ="StatusActivity";
		
	EditText editText;
	Button updateButton;
	Twitter twitter;
	TextView textCount;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status);
        
        Log.d(TAG, "onCreate 호출!!!");
        
        // 뷰 객체들을 찾아서 멤버변수에 찾은 뷰객체들의 참조값을 저장한다.
        editText = (EditText)findViewById(R.id.editText);
        updateButton = (Button)findViewById(R.id.buttonUpdate);
        updateButton.setOnClickListener(this);

        textCount = (TextView)findViewById(R.id.textCount);
        textCount.setText(Integer.toString(140));

        editText.addTextChangedListener(this);    
        
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String token = prefs.getString("token", null);
		String tokenSecret = prefs.getString("tokenSecret", null);
		if (token == null || tokenSecret == null) {
			// 1초 뒤에 인즈을 할 수 있는 Activity로 이동하여 인증을 한다
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					// OAuthActivity 화면으로 이동한다
					startActivity(new Intent(StatusActivity.this, OAuthActivity.class));
				}
			}, 1000);
		}
   	}

	@Override
	public void onClick(View arg0) {
		String status = editText.getText().toString();
		new PostToTwitter().execute(status);
		Log.d(TAG, "onClicked");
	}
	
	// 비동기적으로 twitter 서비스에 나의 상태를 올리기 위한 네스티드 클래스
    class PostToTwitter extends AsyncTask<String, Integer, String> {
    	// background에서 수행되는 작업을 처음 시작할때 호출되는 메서드
    	// 작업의 초기 처리를 수행하면 된다.
		@Override
		protected String doInBackground(String... statuses) {
					
			Log.d(TAG, "doInBackground 호출!!!");
			
			YambaApplication app = 
					(YambaApplication)getApplication();
			winterwell.jtwitter.Status status = 
					app.getTwitter().updateStatus(statuses[0]);
			
			return status.text;
		}

		// backgroud에서 수행되는 작업이 끝이 나면 호출이 된다.
		@Override
		protected void onPostExecute(String result) {
			Log.d(TAG, "onPostExecute 호출!!!");
			Toast.makeText(
					StatusActivity.this, 
					result, 
					Toast.LENGTH_LONG).show();
		}

		// background에서 수행되는 작업의 진행정도를 계속해서 호출되는 메서드
		@Override
		protected void onProgressUpdate(Integer... values) {
			Log.d(TAG, "onProgressUpdate 호출!!!");
			super.onProgressUpdate(values);
		}
    }

	@Override
	public void afterTextChanged(Editable statusText) {
		int count = 140 - statusText.length();
		textCount.setText(Integer.toString(count));
		textCount.setTextColor(Color.GREEN);
		if (count < 10) {
			textCount.setTextColor(Color.YELLOW);
		}
		if (count < 0) {
			textCount.setTextColor(Color.RED);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		
	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemServiceStart:
			startService(new Intent(this, UpdaterService.class));
			break;
		case R.id.itemStopService:
			stopService(new Intent(this, UpdaterService.class));
			break;
		case R.id.itemPrefs:
			Intent i = new Intent(this, PrefsActivity.class);
			startActivity(i);
			break;
		}
		return true;
	}
}