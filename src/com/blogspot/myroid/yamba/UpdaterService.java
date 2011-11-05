package com.blogspot.myroid.yamba;

import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;
import winterwell.jtwitter.Status;

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
					YambaApplication ymba = 
							(YambaApplication)getApplication();
					int newUpdates = ymba.fetchStatusUpdates();
					if (newUpdates > 0) {
						Log.d(TAG, "We have a new status");
					}
					sleep(DELAY);
				} catch (InterruptedException e) {
					runFlag = false;
				}
			}
		}
	}
}
