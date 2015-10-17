package org.tuxship.quickshare;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class TokenDatabase {
	public String FILENAME = "token_database";

	private Context context;
	
	public TokenDatabase(Context context) {
		this.context = context;
	}

	public String ip_wifi(){

		WifiManager manager=(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info=manager.getConnectionInfo();
		int ipAddress=info.getIpAddress();

		if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
			ipAddress = Integer.reverseBytes(ipAddress);
		}

		byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

		String ipAddressString;
		try {
			ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
		} catch (UnknownHostException ex) {
			Log.e("WIFIIP", "Unable to get host address.");
			ipAddressString = null;
		}

		return ipAddressString;
	}

	public void addtoJSON(JSONObject obj,String sname,String sdata){
		try{
			obj.put(sname, sdata);
			
		}catch(JSONException e){
			e.printStackTrace();
		}
	}
	
	public void removefromJSON(JSONObject obj,String sname){
//		try{
			obj.remove(sname);
			
//		}catch(JSONException e){
//			e.printStackTrace();
//		}
	}
	
	
	public void saveJSON(JSONObject obj){

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

	public JSONObject loadJSON(){
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
