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
        
        Log.d(TAG, "onCreate ȣ��!!!");
        
        // �� ��ü���� ã�Ƽ� ��������� ã�� �䰴ü���� �������� �����Ѵ�.
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
			// 1�� �ڿ� ������ �� �� �ִ� Activity�� �̵��Ͽ� ������ �Ѵ�
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					// OAuthActivity ȭ������ �̵��Ѵ�
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
	
	// �񵿱������� twitter ���񽺿� ���� ���¸� �ø��� ���� �׽�Ƽ�� Ŭ����
    class PostToTwitter extends AsyncTask<String, Integer, String> {
    	// background���� ����Ǵ� �۾��� ó�� �����Ҷ� ȣ��Ǵ� �޼���
    	// �۾��� �ʱ� ó���� �����ϸ� �ȴ�.
		@Override
		protected String doInBackground(String... statuses) {
					
			Log.d(TAG, "doInBackground ȣ��!!!");
			
			YambaApplication app = 
					(YambaApplication)getApplication();
			winterwell.jtwitter.Status status = 
					app.getTwitter().updateStatus(statuses[0]);
			
			return status.text;
		}

		// backgroud���� ����Ǵ� �۾��� ���� ���� ȣ���� �ȴ�.
		@Override
		protected void onPostExecute(String result) {
			Log.d(TAG, "onPostExecute ȣ��!!!");
			Toast.makeText(
					StatusActivity.this, 
					result, 
					Toast.LENGTH_LONG).show();
		}

		// background���� ����Ǵ� �۾��� ���������� ����ؼ� ȣ��Ǵ� �޼���
		@Override
		protected void onProgressUpdate(Integer... values) {
			Log.d(TAG, "onProgressUpdate ȣ��!!!");
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