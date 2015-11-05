package org.tuxship.quickshare.dao.sql;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import org.tuxship.quickshare.dao.DAOService;
import org.tuxship.quickshare.dao.JsonDAO;
import org.tuxship.quickshare.dao.sql.SQLContract.FilesTable;
import org.tuxship.quickshare.dao.sql.SQLContract.ShareTable;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SQLiteDAO extends DAOService  {

	SQLHelper sqlHelper = new SQLHelper(SQLiteDAO.this);

	@Override
	public String addShare(String name, List<String> files) {
		SQLiteDatabase db = sqlHelper.getWritableDatabase();

		String token = createToken(files);

		/*
		 * Store Share Information
		 */
		ContentValues shareValues = new ContentValues();
		shareValues.put(ShareTable.COLUMN_SHARE_NAME, name);
		shareValues.put(ShareTable.COLUMN_SHARE_TOKEN, token);

		db.insert(ShareTable.TABLE_NAME, null, shareValues);

		/*
		 * Store files that belong to the share
		 */
		for(String file : files) {
			ContentValues fileValues = new ContentValues();
			fileValues.put(FilesTable.COLUMN_SHARE_NAME, name);
			fileValues.put(FilesTable.COLUMN_FILE, file);

			db.insert(FilesTable.TABLE_NAME, null, fileValues);
		}

		db.close();

		return token;
	}

	@Override
	public boolean removeShare(String share) {
		SQLiteDatabase db = sqlHelper.getWritableDatabase();
		
		boolean success = true;
		
		/*
		 * Delete entry from ShareTable
		 */
		{
			String selection = "? LIKE ?";
			String[] selectionArgs = {
					ShareTable.COLUMN_SHARE_NAME,
					share
			};

			int deleted = db.delete(ShareTable.TABLE_NAME, selection, selectionArgs);

			if(deleted < 1) success = false;
		}
		
		
		/*
		 * Delete files belonging to the share
		 */
		{
			String selection = "? LIKE ?";
			String[] selectionArgs = {
					FilesTable.COLUMN_SHARE_NAME,
					share
			};

			int deleted = db.delete(FilesTable.TABLE_NAME, selection, selectionArgs);
		
			if(deleted < 1) success = false;
		}
		
		
		db.close();
		
		return success;
	}

	@Override
	public List<String> getShares() {
		SQLiteDatabase db = sqlHelper.getReadableDatabase();

		String[] projection = { ShareTable.COLUMN_SHARE_NAME };

		Cursor c = db.query(
				ShareTable.TABLE_NAME,
				projection,
				null,
				null,
				null,
				null,
				null);

		ArrayList<String> shares = new ArrayList<String>();

		c.moveToFirst();

		while(!c.isAfterLast()) {
			shares.add(c.getString(
					c.getColumnIndex(projection[0])));
			c.moveToNext();
		}

		c.close();
		db.close();

		return shares;
	}

	@Override
	public List<String> getFiles(String token) throws TokenNotFoundException {
		final String share = getShareName(token);
		
		SQLiteDatabase db = sqlHelper.getReadableDatabase();

		String[] projection = { FilesTable.COLUMN_FILE };

		Cursor c = db.query(
				FilesTable.TABLE_NAME,
				projection,
				"? LIKE ?",
				new String[] { FilesTable.COLUMN_SHARE_NAME, share },
				null,
				null,
				null);
		
		ArrayList<String> files = new ArrayList<String>(c.getCount());
		
		while(!c.isAfterLast()) {
			files.add(c.getString(
					c.getColumnIndex(projection[0])));
			c.moveToNext();
		}
		
		c.close();
		db.close();
		
		return files;
	}

	@Override
	public String getToken(String share) throws ShareNotFoundException {
		SQLiteDatabase db = sqlHelper.getReadableDatabase();

		String[] projection = { ShareTable.COLUMN_SHARE_TOKEN };

		Cursor c = db.query(
				ShareTable.TABLE_NAME,
				projection,
				"? = ?",
				new String[] { ShareTable.COLUMN_SHARE_NAME, share },
				null,
				null,
				null);
		
		if(c.getCount() > 1)
			Log.w(LOGTAG, "Multiple rows with share name " + share);
		else if(c.getCount() < 1) {
			Log.e(LOGTAG, "No rows with share name " + share);
			throw new ShareNotFoundException();
		}
		
		c.close();
		db.close();
		
		return c.getString(c.getColumnIndex(projection[0]));
	}

	private String getShareName(String token) throws TokenNotFoundException {
		SQLiteDatabase db = sqlHelper.getReadableDatabase();

		String[] projection = { ShareTable.COLUMN_SHARE_NAME };

		Cursor c = db.query(
				ShareTable.TABLE_NAME,
				projection,
				"? = ?",
				new String[] { ShareTable.COLUMN_SHARE_TOKEN, token },
				null,
				null,
				null);
		
		if(c.getCount() > 1)
			Log.w(LOGTAG, "Multiple rows with token  " + token);
		else if(c.getCount() < 1) {
			Log.e(LOGTAG, "No rows with token " + token);
			throw new TokenNotFoundException();
		}
		
		c.close();
		db.close();
		
		return c.getString(c.getColumnIndex(projection[0]));
	}
	
	private static String createToken(List<String> files) {
		StringBuffer result = new StringBuffer();

		try {
			byte[] bytesOfMessage = files.toString().getBytes("UTF-8");

			MessageDigest md;
			md = MessageDigest.getInstance("SHA1");

			byte[] thedigest = md.digest(bytesOfMessage);

			for(byte b : thedigest) {
				if((0xff & b) < 0x10) {
					result.append("0" + Integer.toHexString(0xff & b));
				} else {
					result.append(Integer.toHexString(0xff & b));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result.substring(result.length() - JsonDAO.tokenLength);
	}

}
