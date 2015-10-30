package org.tuxship.quickshare.dao;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
	public static final String FILENAME = "token_database";

	public static final int tokenLength = 6;
	
	private static final String SHARE_DB = "db";
	private static final String SHARE_TOKEN = "key";
	private static final String SHARE_NAME = "name";
	private static final String SHARE_FILES = "files";
	
	private JSONObject jsonDB = null;

	@Override
	public String addShare(String name, List<String> files) {
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
			// Empty database.
			// Return empty list.
		}		

		return shares;
	}

	@Override
	public List<String> getFiles(String token) throws TokenNotFoundException {
		List<String> files = new ArrayList<String>();
		
		if(jsonDB == null)
			jsonDB = loadJSON();
		
		try {
			JSONArray db = jsonDB.getJSONArray(SHARE_DB);
			
			for(int i = 0; i < db.length(); i++){
				JSONObject share = (JSONObject) db.get(i);
				
				if(share.get(SHARE_TOKEN).equals(token)){
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
		
		throw new TokenNotFoundException("Token '" + token + "' does not exist.");
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

		return result.substring(result.length() - tokenLength);
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
			FileOutputStream fos = getApplication().getApplicationContext().openFileOutput(FILENAME, Context.MODE_PRIVATE);
			fos.write(("{\"" + SHARE_DB + "\": []}").getBytes());
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean saveJSON(JSONObject obj){
		try {
			FileOutputStream fOut = getApplication().getApplicationContext()
					.openFileOutput(FILENAME, Context.MODE_PRIVATE);

			fOut.write(obj.toString().getBytes());
			fOut.close();
		} catch(FileNotFoundException e){
			Log.e("@string/logtagdb", "Can not store jsonDB. FileNotFoundException");
			return false;
		} catch(IOException e){
			Log.e("@string/logtagdb", "Can not store jsonDB. IOException");
			return false;
		}
		
		return true;
	}

	private JSONObject loadJSON(){
		String result = "";

		try {
			FileInputStream fIn = getApplication().getApplicationContext()
					.openFileInput(FILENAME);
			BufferedInputStream bIn = new BufferedInputStream(fIn);
			Scanner scan = new Scanner(bIn);
			
			while(scan.hasNext()) {
				result += scan.next();
			}
			
			scan.close();
		} catch(FileNotFoundException e){
			initFile();
			return loadJSON();
		}

		JSONObject jsonIn = new JSONObject();
		
		try {
			jsonIn = new JSONObject(result);
		} catch (JSONException e) {
			Log.w("dataout", "could not create jsonobject from loaded data");
			e.printStackTrace();
		}

		return jsonIn;
	}

}
