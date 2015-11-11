package org.tuxship.quickshare.dao;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.Log;

public class JsonDAO extends DAOService {
	private static final String FILENAME = "token_database";
	private static final String BACKUPFILE = "token_database.bak";

	private static final String SHARE_DB = "db";
	private static final String SHARE_TOKEN = "key";
	private static final String SHARE_NAME = "name";
	private static final String SHARE_FILES = "files";
	
	private JSONObject jsonDB = null;

	@Override
	public String addShare(String name, List<String> files) {
		if(files.isEmpty() || name.equals("")) {
			Log.e(LOGTAG, "No sharename provided or empty file list, on adding a share.");
			return "";
		}
		
		/*
		 * jsonify and create token
		 */
		JSONArray jsonFileList = new JSONArray(files);
		String token = createKey(jsonFileList);
		
		/*
		 * persist data
		 */
		if(jsonDB == null)
			jsonDB = loadJSON();

		addtoJSON(name, token, jsonFileList);
		saveJSON(jsonDB);

//		dumpJSON();
		
		return token;
	}

	@Override
	public boolean removeShare(String name) {
		if(Build.VERSION.SDK_INT >= 19)
			return removefromJSON_api19(name);

		return removefromJSON_legacy(name);
	}

	@Override
	public List<String> getShares(){
		ArrayList<String> shares = new ArrayList<String>();

		if(jsonDB == null)
			jsonDB = loadJSON();
		
		try {
			JSONArray db = jsonDB.getJSONArray(SHARE_DB);

			for(int i = 0; i < db.length(); i++){
				shares.add(db.getJSONObject(i).get(SHARE_NAME).toString());
			}
		} catch (JSONException e) {
			Log.w(LOGTAG, "JSONException on getShares. This can happen when there are no shares yet.");
			// Empty database.
			// Return empty list.
		}		

		return shares;
	}

	@Override
	public List<String> getFiles(String identifier, int type) throws TokenNotFoundException, ShareNotFoundException {
		List<String> files = new ArrayList<String>();
		
		if(jsonDB == null)
			jsonDB = loadJSON();
		
		try {
			JSONArray db = jsonDB.getJSONArray(SHARE_DB);
			
			for(int i = 0; i < db.length(); i++){
				JSONObject share = (JSONObject) db.get(i);
				
				if(share.get( (type == TYPE_SHARENAME) ? SHARE_NAME : SHARE_TOKEN).equals(identifier)){
					JSONArray jsonFiles = share.getJSONArray(SHARE_FILES);
					
					for(int j = 0; j < jsonFiles.length(); j++){
						files.add(jsonFiles.getString(j));
					}
					
					return files;
				}
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		if(type == TYPE_SHARENAME)
			throw new ShareNotFoundException("Share '" + identifier + "' does not exist.");
		else
			throw new TokenNotFoundException("Token '" + identifier + "' does not exist.");
	}
	
	@Override
	public String getToken(String shareName) throws ShareNotFoundException {
		if(jsonDB == null)
			jsonDB = loadJSON();
		
		try {
			JSONArray db = jsonDB.getJSONArray(SHARE_DB);
			
			for(int i = 0; i < db.length();  i++) {
				JSONObject share = db.getJSONObject(i);
				
				if(share.getString(SHARE_NAME).equals(shareName))
					return share.getString(SHARE_TOKEN);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		throw new ShareNotFoundException("No share with the name '" + shareName + "'!");
	}

	/**
	 * Returns the count of the stored shares.
	 * 
	 * @return the share count
	 */
	@Override
	public int getShareCount() {
		if(jsonDB == null)
			jsonDB = loadJSON();
		
		JSONArray db = null;
		try {
			db = jsonDB.getJSONArray(SHARE_DB);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return db.length();
	}
	
	/**
	 * Backs up the current active database file in the backup file.
	 * <p>
	 * Only use in Unit Tests! Don't forget to restore() after testing.
	 * 
	 * @return true on success, false on errors
	 */
	public boolean backupAndClear() {
		if(!getBaseContext().getFileStreamPath(FILENAME).exists()) {
			initFile();
		}
			
		if(!copyFile(FILENAME, BACKUPFILE))
			return false;
		
		initFile();
		return true;
	}

	/**
	 * Restores the database from the backup file.
	 * <p>
	 * <b>BEWARE: POTENTIAL LOSS OF DATA</b>
	 * This replaces the current database file.
	 * <p>
	 * Only use in Unit Tests!
	 * 
	 * @return true on success, false on errors 
	 */
	public boolean restore() {
		if(!getBaseContext().getFileStreamPath(BACKUPFILE).exists()) {
			return false;
		}
		
		if(!copyFile(BACKUPFILE, FILENAME))
			return false;
		
		jsonDB = null;		// need to re-read database
		return true;
	}
	
	/**
	 * Copies a file from A to B.
	 * 
	 * @param sourcePath	The file to copy
	 * @param targetPath	The copy destination
	 * @return				true on success, false on errors
	 */
	private boolean copyFile(String sourcePath, String targetPath) {
		try {
			FileInputStream fIn = this.openFileInput(sourcePath);
			BufferedInputStream bIn = new BufferedInputStream(fIn);
			FileOutputStream fOut = this.openFileOutput(targetPath, Context.MODE_PRIVATE);
			BufferedOutputStream bOut = new BufferedOutputStream(fOut);
			
			while(true) {
				byte[] buffer = new byte[1024]; 
				int bytesRead = bIn.read(buffer);
				bOut.write(buffer);
			
				if(bytesRead == -1)
					break;
			}
			
			bIn.close();	
			bOut.close();
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public void dumpJSON() {
		if(jsonDB == null)
			jsonDB = loadJSON();

		Log.d("DatabaseDump", jsonDB.toString());
	}
	
	private static String createKey(JSONArray files) {
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

		return result.substring(result.length() - DAOService.TOKEN_LENGTH);
	}

	private void addtoJSON(String name, String key, JSONArray files){
		if(jsonDB == null)
			jsonDB = loadJSON();
		
		try {
			JSONArray db = (jsonDB.has(SHARE_DB)) ? 
					jsonDB.getJSONArray(SHARE_DB) : new JSONArray();

			JSONObject share = new JSONObject();
			share.put(SHARE_TOKEN, key);
			share.put(SHARE_NAME, name);
			share.put(SHARE_FILES, files);
			
			db.put(share);
			
			jsonDB.put(SHARE_DB, db);
		} catch(JSONException e) {
			e.printStackTrace();
		} 
	}

	/**
	 * Removes a share from the database.
	 * 
	 * This method uses JSONArray.remove() and hence is only 
	 * available on devices running Kitkat (Api level 19) or higher.
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	private boolean removefromJSON_api19(String shareName){
		if(jsonDB == null)
			jsonDB = loadJSON();
		
		try{
			JSONArray db = jsonDB.getJSONArray(SHARE_DB);
			
			for(int i = 0; i < db.length(); i++){
				JSONObject share = db.getJSONObject(i);
				
				if(share.get(SHARE_NAME).equals(shareName)){
					db.remove(i);				// introduced in Api level 19 (Kitkat)
					jsonDB.put(SHARE_DB, db);
					saveJSON(jsonDB);
					return true;
				}
			}
		}catch(JSONException e){
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * Removes a share from the database.
	 * 
	 * This method does not use JSONArray.remove() and thus
	 * supports devices running older versions of android 
	 * as well.
	 */
	private boolean removefromJSON_legacy(String shareName){
		if(jsonDB == null)
			jsonDB = loadJSON();
		
		boolean success = false;
		try{
			JSONArray db = jsonDB.getJSONArray(SHARE_DB);
			JSONArray newdb = new JSONArray();
			
			/*
			 * Instead of removing the object with the specified
			 * sharename, we have to create a new JSONArray with
			 * all values, but the one we want excluded.
			 * 
			 * This is necessary since JSONArray.remove() was 
			 * first introduced in API Level 19 (Kitkat)  and
			 * therefore is unavailable on older devices.
			 */
			for(int i = 0; i < db.length(); i++){
				JSONObject share = db.getJSONObject(i);
				
				if(share.get(SHARE_NAME).equals(shareName)) {
					success = true;
					continue;
				}
				
				newdb.put(share);
			}
			
			jsonDB.put(SHARE_DB, newdb);
			saveJSON(jsonDB);
		}catch(JSONException e){
			e.printStackTrace();
		}
		
		return success;
	}

	private void initFile() {
		try {
			FileOutputStream fOut = this.openFileOutput(FILENAME, Context.MODE_PRIVATE);
			fOut.write(("{\"" + SHARE_DB + "\": []}").getBytes());
			fOut.close();
			
			jsonDB = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean saveJSON(JSONObject obj){
		try {
			FileOutputStream fOut = this.openFileOutput(FILENAME, Context.MODE_PRIVATE);
			fOut.write(obj.toString().getBytes());
			fOut.close();
		} catch(FileNotFoundException e){
			Log.e(LOGTAG, "Can not store jsonDB. FileNotFoundException");
			return false;
		} catch(IOException e){
			Log.e(LOGTAG, "Can not store jsonDB. IOException");
			return false;
		}
		
		return true;
	}

	private JSONObject loadJSON(){
		StringBuilder data = new StringBuilder();

		try {
			FileInputStream fIn = this.openFileInput(FILENAME);
			InputStreamReader iReader = new InputStreamReader(fIn, "UTF-8");
			BufferedReader bReader = new BufferedReader(iReader);
			
			String line;
			while((line = bReader.readLine()) != null) {
				data.append(line);
			}
			
			bReader.close();
		} catch(FileNotFoundException e){
			initFile();
			return loadJSON();
		} catch (IOException e) {
			e.printStackTrace();
		}

		JSONObject jsonIn = new JSONObject();
		
		try {
			jsonIn = new JSONObject(data.toString());
		} catch (JSONException e) {
			Log.w(LOGTAG, "could not create jsonobject from loaded data");
			e.printStackTrace();
		}

		return jsonIn;
	}

}
