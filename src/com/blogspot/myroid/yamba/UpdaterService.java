package com.blogspot.myroid.yamba;

import java.math.BigInteger;
import java.util.List;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;
import winterwell.jtwitter.Status;
import winterwell.jtwitter.TwitterException;

public class UpdaterService extends Service {

	private static final String TAG = "UpdaterService";

	// Thread의 지연 값을 위한 변수
	private static final int DELAY = 60000;
	// Thread의 루프의 탈출 조건을 처리
	private boolean runFlag = false;
	// Thread class
	private Updater updater;
	// Yamba Applicaiton class
	private YambaApplication yamba;
	
	DBHelper dbHelper;
	SQLiteDatabase db;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		yamba = (YambaApplication)getApplication();
		updater = new Updater();
		
		dbHelper = new DBHelper(this);
		
		Log.d(TAG, "OnCreated");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		runFlag = false;		
		updater.interrupt();
		updater = null;
		
		yamba.setServiceRunning(false);
		
		Log.d(TAG, "onDestroyed");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		
		if (!runFlag) {
			runFlag = true;
			updater.start();
			
			yamba.setServiceRunning(true);
			
			Log.d(TAG, "onStarted");
		}

		return START_STICKY;
	}
	
	/**
	 * online twitter 서비스로 부터 실제 update 처리를 하는 Thread
	 */
	class Updater extends Thread {
		List<Status> timeLine;
		
		public Updater() {
			super("updater-thread");
		}

		@Override
		public void run() {
			super.run();
		
			UpdaterService updaterService = UpdaterService.this;
			
			while (updaterService.runFlag) {
				Log.d(TAG, "updater running");
				try {
					
					// Twitter 서비스로 부터 timeline을 가져 온다.
					try {
						timeLine = yamba.getTwitter().getHomeTimeline();
					} catch (TwitterException e) {
						Log.e(TAG, "Failed to connect to twitter service", e);
					}
					
					// database 데이터
					db = dbHelper.getWritableDatabase();
					
					// Loop over the timeline and print it out
					ContentValues values = new ContentValues();
					for (Status s : timeLine) {
						// Insert into database
						values.clear();
						values.put(DBHelper.C_ID, s.id.intValue());
						values.put(DBHelper.C_CREATED_AT, 
								s.createdAt.getTime());
						values.put(DBHelper.C_SOURCE, s.source);
						values.put(DBHelper.C_TEXT, s.text);
						values.put(DBHelper.C_USER, s.user.name);
						
						// constraints _id primary key 때문에 exception이 발생하게 됨
						// insert 시에 발생하는 exception은 무시하도록 처리한다.
						try {
						db.insertOrThrow(DBHelper.TABLE_NAME, null, values);
						} catch (SQLException e) {
							// exception을 무시한다. 
						}
						
						Log.d(TAG, String.format("%s: %s", 
								s.user.name, s.text));
					}
					
					db.close();
					
					Log.d(TAG, "updater ran");
					sleep(DELAY);
				} catch (InterruptedException e) {
					runFlag = false;
				}
			}
		}
	}
}
