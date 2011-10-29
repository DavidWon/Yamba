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

	// Thread�� ���� ���� ���� ����
	private static final int DELAY = 60000;
	// Thread�� ������ Ż�� ������ ó��
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
	 * online twitter ���񽺷� ���� ���� update ó���� �ϴ� Thread
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
					
					// Twitter ���񽺷� ���� timeline�� ���� �´�.
					try {
						timeLine = yamba.getTwitter().getHomeTimeline();
					} catch (TwitterException e) {
						Log.e(TAG, "Failed to connect to twitter service", e);
					}
					
					// database ������
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
						
						// constraints _id primary key ������ exception�� �߻��ϰ� ��
						// insert �ÿ� �߻��ϴ� exception�� �����ϵ��� ó���Ѵ�.
						try {
						db.insertOrThrow(DBHelper.TABLE_NAME, null, values);
						} catch (SQLException e) {
							// exception�� �����Ѵ�. 
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
