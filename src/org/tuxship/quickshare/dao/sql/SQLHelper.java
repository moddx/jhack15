package org.tuxship.quickshare.dao.sql;

import org.tuxship.quickshare.dao.sql.SQLContract.FilesTable;
import org.tuxship.quickshare.dao.sql.SQLContract.ShareTable;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.provider.BaseColumns;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLHelper extends SQLiteOpenHelper {

	private static final String TEXT_TYPE = " TEXT";
	private static final String COMMA_SEP = ",";
	
	/*
	 * SQL for regular tables
	 */
	private static final String SQL_CREATE_SHARE_TABLE =
			"CREATE TABLE " + ShareTable.TABLE_NAME + " (" +
					ShareTable._ID + " INTEGER PRIMARY KEY," +
					ShareTable.COLUMN_SHARE_NAME + TEXT_TYPE + COMMA_SEP +
					ShareTable.COLUMN_SHARE_TOKEN + TEXT_TYPE +
					" )";
	
	private static final String SQL_CREATE_FILES_TABLE =
			"CREATE TABLE " + FilesTable.TABLE_NAME + " (" +
					FilesTable._ID + " INTEGER PRIMARY KEY," +
					FilesTable.COLUMN_SHARE_NAME + TEXT_TYPE + COMMA_SEP +
					FilesTable.COLUMN_FILE + TEXT_TYPE +
					" )";

	/*
	 * SQL for backup tables
	 */
	private static final String SQL_CREATE_SHARE_BACKUP_TABLE =
			"CREATE TABLE " + ShareTable.BACKUP_TABLE_NAME + " (" +
					ShareTable._ID + " INTEGER PRIMARY KEY," +
					ShareTable.COLUMN_SHARE_NAME + TEXT_TYPE + COMMA_SEP +
					ShareTable.COLUMN_SHARE_TOKEN + TEXT_TYPE +
					" )";
	
	private static final String SQL_CREATE_FILES_BACKUP_TABLE =
			"CREATE TABLE " + FilesTable.BACKUP_TABLE_NAME + " (" +
					FilesTable._ID + " INTEGER PRIMARY KEY," +
					FilesTable.COLUMN_SHARE_NAME + TEXT_TYPE + COMMA_SEP +
					FilesTable.COLUMN_FILE + TEXT_TYPE +
					" )";
	
	
	
	
	// If you change the database schema, you must increment the database version.
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "Shares.db";
	
	public SQLHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_SHARE_TABLE);
		db.execSQL(SQL_CREATE_FILES_TABLE);
		
//		db.execSQL(SQL_CREATE_SHARE_BACKUP_TABLE);
//		db.execSQL(SQL_CREATE_FILES_BACKUP_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

	
	public void createCleanBackupTables(SQLiteDatabase db) {
		dropTable(db, ShareTable.BACKUP_TABLE_NAME);
		dropTable(db, FilesTable.BACKUP_TABLE_NAME);
		db.execSQL(SQL_CREATE_SHARE_BACKUP_TABLE);
		db.execSQL(SQL_CREATE_FILES_BACKUP_TABLE);
	}
	
	private void dropTable(SQLiteDatabase db, String table) {
		String sql = "DROP TABLE IF EXISTS " + table;
		db.execSQL(sql);
	}
}
