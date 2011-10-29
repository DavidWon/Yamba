package com.blogspot.myroid.yamba;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

	private static final String TAG = "DBHelper";
	
	private static final String DB_NAME = "timeline.db";
	private static final int DB_VERSION = 1;
	static final String TABLE_NAME = "timeline";
	static final String C_ID = BaseColumns._ID;
	static final String C_CREATED_AT = "create_dt";
	static final String C_SOURCE = "source";
	static final String C_TEXT = "text";
	static final String C_USER = "user";
	
	private Context context;
	
	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// CREATE TABLE timeline(_id int primary key, created_dt int, 
		// source text, user text, text text);
		String sql = "CREATE TABLE " + TABLE_NAME + " (" + 
						C_ID + " int primary key, " +
						C_CREATED_AT + " int, " +
						C_SOURCE + " text, " +
						C_USER + " text, " +
						C_TEXT + " text" +
						");";
		db.execSQL(sql);
		
		Log.d(TAG, "onCreate sql : " + sql);
	}

	// oldVersion과 newVersion이 틀릴 경우에 해당 메소드가 호출 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, 
			int newVersion) {
		db.execSQL("DROP TABLE if exists" + TABLE_NAME);
		Log.d(TAG, "onUpgrade");
		onCreate(db);
	}

}
