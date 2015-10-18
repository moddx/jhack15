package org.tuxship.quickshare;

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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class TokenDatabase extends Service {
	public String FILENAME = "token_database";

	private final int tokenLength = 6;

	// Binder given to clients
    private final IBinder binder = new LocalBinder();
    
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

	public String addShare(String name, List<String> files) {
		JSONArray jarray = new JSONArray(files);
		JSONObject jobj=loadJSON();
		String token=createKey(jarray);
		addtoJSON(jobj, name, token, jarray);
		saveJSON(jobj);

		return token;
	}
	public String printdatabase(){
		JSONObject db = loadJSON();
		
		if(db == null) 
			return "Database is empty";
		
		String ret="";
		try {
			ret = db.toString(8);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ret;
		
	}
	public boolean deleteShare(String name) {
		if(removefromJSON(name)){
			return true;
		} else {
			return false;
		}
	}

	public List<String> getShares(){
		JSONObject obj=loadJSON();
		ArrayList<String> list = new ArrayList<String>();

		try {
			JSONArray db=obj.getJSONArray("db");
			JSONObject curobj;

			for(int i = 0; i < db.length(); i++){
				curobj=(JSONObject) db.get(i);
				list.add((String) curobj.get("name"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}		

		return list;
	}

	public List<String> getFilesforToken(String token){

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
			
			if(files.size() == 0){
				files.add("No files for your token");
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return files;
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

	private void addtoJSON(JSONObject obj,String name,String key,JSONArray files){
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

	private boolean removefromJSON(String sname){
		JSONObject in=loadJSON();
		try{
			JSONArray db=in.getJSONArray("db");
			for(int i=0; i<db.length();i++){
				JSONObject obj=db.getJSONObject(i);
				if(obj.get("name")==sname){
					db.remove(i);
					in.put("db",db);
					saveJSON(in);
					return true;
				}
			}

		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}

	private void initFile() {
		try {
			FileOutputStream fos = getApplication().getApplicationContext().openFileOutput(FILENAME, Context.MODE_PRIVATE);
			fos.write("{}".getBytes());
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

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}


	public class LocalBinder extends Binder {
		TokenDatabase getService() {
			return TokenDatabase.this;
		}
	}
}
