package org.tuxship.quickshare.dao;

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
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

public class TokenDatabase extends DAOService {
	public String FILENAME = "token_database";

	public static final int tokenLength = 6;

	@Override
	public String addShare(String name, List<String> files) {
		JSONArray jarray = new JSONArray(files);
		JSONObject jobj=loadJSON();
		String token=createKey(jarray);
		addtoJSON(jobj, name, token, jarray);
		saveJSON(jobj);

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
		ArrayList<String> list = new ArrayList<String>();

		JSONObject obj = loadJSON();
		
		try {
			JSONArray db = obj.getJSONArray("db");

			for(int i = 0; i < db.length(); i++){
				list.add(db.getJSONObject(i).get("name").toString());
			}
		} catch (JSONException e) {
			/*
			 *  no values in db!
			 *  return empty list
			 */
		}		

		return list;
	}

	@Override
	public List<String> getFiles(String token) throws TokenNotFoundException {
		List<String> files = new ArrayList<String>();
		
		try {
			JSONArray db = loadJSON().getJSONArray("db");
			
			Log.i("dataout", "files in db for token: " + db.length());
			for(int i = 0; i < db.length(); i++){
				JSONObject curobj = (JSONObject) db.get(i);
				
				if(curobj.get("key").equals(token)){
					JSONArray curfiles = curobj.getJSONArray("files");
					for(int j = 0; j < curfiles.length(); j++){
						files.add(curfiles.getString(j));
					}
				}
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		if(files.size() == 0)
			throw new TokenNotFoundException("Token '" + token + "' does not exist.");
		
		return files;
	}
	
	@Override
	public String getToken(String share) throws ShareNotFoundException {
		try {
			JSONArray db = loadJSON().getJSONArray("db");
			
			for(int i = 0; i < db.length();  i++) {
				JSONObject curShare = db.getJSONObject(i);
				
				if(curShare.getString("name").equals(share))
					return curShare.getString("key");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		throw new ShareNotFoundException("No share with the name '" + share + "'!");
	}

	private static String createKey(JSONArray files) {
		String str=files.toString();

		StringBuffer result = new StringBuffer();
		try {
			byte[] bytesOfMessage = str.getBytes("UTF-8");

			MessageDigest md;
			md = MessageDigest.getInstance("SHA1");

			byte[] thedigest = md.digest(bytesOfMessage);

			for(byte b : thedigest) {
				if((0xff & b) < 0x10) {
					result.append("0"
							+ Integer.toHexString(0xff & b));
				} else {
					result.append(Integer.toHexString(0xff & b));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result.substring(result.length() - tokenLength);
	}

	private static void addtoJSON(JSONObject obj, String name, String key, JSONArray files){
		try{
			JSONArray db;
			if(!obj.has("db")){//check if top level array exists
				db=new JSONArray();
				obj.put("db", db);
			} else {
				db=obj.getJSONArray("db");
			}

			JSONObject input=new JSONObject();
			input.put("key",key);
			input.put("name", name);
			input.put("files",files);
			db.put(input);
			obj.put("db", db);			

		}catch(JSONException e){
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
	private boolean removefromJSON_api19(String sname){
		JSONObject in = loadJSON();
		try{
			JSONArray db = in.getJSONArray("db");
			
			for(int i = 0; i < db.length(); i++){
				JSONObject obj = db.getJSONObject(i);
				
				if(obj.get("name").equals(sname)){
					db.remove(i);				// introduced in Api level 19 (Kitkat)
					in.put("db", db);
					saveJSON(in);
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
	private boolean removefromJSON_legacy(String sname){
		JSONObject in = loadJSON();
		
		boolean success = false;
		try{
			JSONArray db = in.getJSONArray("db");
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
				JSONObject obj = db.getJSONObject(i);
				
				if(obj.get("name").equals(sname)) {
					success = true;
					continue;
				}
				
				newdb.put(obj);
			}
			
			in.put("db", newdb);
			saveJSON(in);
		}catch(JSONException e){
			e.printStackTrace();
		}
		
		return success;
	}

	private void initFile() {
		try {
			FileOutputStream fos = getApplication().getApplicationContext().openFileOutput(FILENAME, Context.MODE_PRIVATE);
			fos.write("{\"db\": []}".getBytes());
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void saveJSON(JSONObject obj){

		try{
			FileOutputStream fos = getApplication().getApplicationContext().openFileOutput(FILENAME, Context.MODE_PRIVATE);
			try{

				fos.write(obj.toString().getBytes());
				fos.close();

			} catch(IOException ioex){
				ioex.printStackTrace();
			}
		} catch(FileNotFoundException fnf){

			fnf.printStackTrace();
		}

	}

	private JSONObject loadJSON(){
		String result = "";
		JSONObject out=new JSONObject();
		try{
			FileInputStream fis = getApplication().getApplicationContext().openFileInput(FILENAME);
			Scanner scan = new Scanner(fis);
			while(scan.hasNext()) {
				result += scan.next();
			}
			scan.close();
		} catch(FileNotFoundException e){
			initFile();
			return loadJSON();
		}

		try {
			out=new JSONObject(result);
		} catch (JSONException e) {
			e.printStackTrace();
			Log.w("dataout", "could not create jsonobject from loaded data");
		}

		return out;
	}

}
