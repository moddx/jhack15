package org.tuxship.quickshare;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public class TokenDatabase {
	public String FILENAME = "token_database";

	private final int tokenLength = 6;
	Context context;
	
	public TokenDatabase(Context context) {
		this.context = context;
	}
	
	public String addShare(String name, ArrayList<String> files) {
		/*
		 * Create token
		 */
		String token = "";
		
		/*
		 * Store files and sharename with token
		 */
		
		return token;
	}
	
	public boolean deleteShare(String name) {
		/*
		 * Delete json stuff and return success
		 */
		
		
		return false;
	}
	
	
	private String createKey(JSONArray files) {
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
	
	private void addtoJSON(JSONObject obj,String key,JSONArray files){
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
			input.put("files",files);
			db.put(input);
			obj.put("db", db);			
			
		}catch(JSONException e){
			e.printStackTrace();
		} 
	}
	
	private void removefromJSON(JSONObject obj,String sname){
//		try{
			obj.remove(sname);
			
//		}catch(JSONException e){
//			e.printStackTrace();
//		}
	}
	
	
	private void saveJSON(JSONObject obj){

		try{
			FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
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
			FileInputStream fis = context.openFileInput(FILENAME);
			Scanner scan = new Scanner(fis);
			while(scan.hasNext()) {
				result += scan.next();
			}
			scan.close();
		} catch(IOException e){
			e.printStackTrace();
		}

		try {
			out=new JSONObject(result);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return out;
	}

}
