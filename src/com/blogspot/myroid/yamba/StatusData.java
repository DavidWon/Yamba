package com.blogspot.myroid.yamba;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class StatusData {

	private static final String TAG = StatusData.class.getSimpleName();
	
	static final int VERSION = 2;
	static final String DATABASE = "timeline.db";
	static final String TABLE = "timeline";
	
	public static final String C_ID = "_id";
	public static final String C_CREATED_AT = "create_dt";
	public static final String C_TEXT = "text";
	public static final String C_USER = "user";
	
	private static final String GET_ALL_ORDER_BY = C_CREATED_AT + " DESC";
	
	private static final String[] MAX_CREATED_AT_COLUMNS = {
		"max(" + StatusData.C_CREATED_AT + ")" };

	private static final String[] DB_TEXT_COLUMNS = { C_TEXT };
	
	class DbHelper extends SQLiteOpenHelper {

		public DbHelper(Context context) {
			super(context, DATABASE, null, VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.i(TAG, "Creating database: " + DATABASE);
			db.execSQL("CREATE TABLE " + TABLE + " (" + 
					C_ID + " int primary key, " +
					C_CREATED_AT + " int, " +
					C_USER + " text, " +
					C_TEXT + " text" +
					");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, 
				int oldVersion, int newVersion) {
			db.execSQL("drop table " + TABLE);
			this.onCreate(db);
		}
	}
	
	private final DBHelper dbHelper;
	
	public StatusData(Context context) {
		this.dbHelper = new DBHelper(context);
		Log.i(TAG, "Initialized data");
	}
	
	public void close() {
		this.dbHelper.close();
	}
	
	public void insertOrIgnore(ContentValues values) {
		Log.d(TAG, "insertOrIgnore on " + values);
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		try {
			db.insertWithOnConflict(TABLE, null, values, 
					SQLiteDatabase.CONFLICT_IGNORE);
		} finally {
			db.close();
		}
	}
	
	/**
	 * 
	 * @return Cursor 컬럼은 _id, created_at, user, txt
	 */
	public Cursor getStatusUpdates() {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		return db.query(TABLE, null, null, null, null, null, 
				GET_ALL_ORDER_BY);
	}
	
	/**
	 * 
	 * @return 가장 최근에 저장된 status의 timestamp
	 */
	public long getLatestStatusCreatedAtTime() {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		try {
			Cursor cursor = db.query(TABLE, MAX_CREATED_AT_COLUMNS, 
					null, null, null, null, null);
			try {
				return cursor.moveToNext() ? 
						cursor.getLong(0) : Long.MAX_VALUE;
			} finally {
				cursor.close();
			}
		} finally {
			db.close();
		}
	}
	
	/**
	 * 
	 * @param 테이블에서 찾을 id
	 * @return id에 맞는 status의 텍스트
	 */
	public String getStatusTextById(long id) {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		try {
			Cursor cursor = db.query(TABLE, DB_TEXT_COLUMNS, C_ID + "=" + id, 
					null, null, null, null);
			try {
				return cursor.moveToNext() ? cursor.getString(0) : null;
			} finally {
				cursor.close();
			}
		} finally {
			db.close();
		}
	}
}
