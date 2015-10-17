package org.tuxship.quickshare;


import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tuxship.quickshare.TokenDatabase.LocalBinder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

public class ShareActivity extends Activity {

	public static final String EXTRA_SHARE = "extraShareName";
	public static final String EXTRA_TOKEN = "extraToken";
	
	String shareName;
	String token;
	
	TokenDatabase dbService;
	boolean dbBound = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share);
		
		Intent shareNameIntent = getIntent();
		shareName = shareNameIntent.getStringExtra(EXTRA_SHARE);
		token = shareNameIntent.getStringExtra(EXTRA_TOKEN);
		
		TextView sharenametext = (TextView)findViewById(R.id.sharename);
		sharenametext.setText(shareName);
		
		
		TextView link = (TextView)findViewById(R.id.remote_link);
		link.setText("http://" + getWifiIP() + ":8080");
		
		
		TextView tokentext = (TextView)findViewById(R.id.sharetoken);
		tokentext.setText(token);
		
		Button shares_button = (Button)findViewById(R.id.allshares);
		shares_button.setText("All shares");
		
		
//		Intent webIntent = new Intent();
//		webIntent.setAction("org.tuxship.STARTHTTPD");
//		startService(webIntent);
		
		/*
		 * Start web server
		 */
		startService(new Intent(this, Httpd.class));
		
	}
	
	private String getWifiIP(){
		WifiManager manager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = manager.getConnectionInfo();
		int ipAddress = info.getIpAddress();

		if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
			ipAddress = Integer.reverseBytes(ipAddress);
		}

		byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

		String ipAddressString;
		try {
			ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
		} catch (UnknownHostException ex) {
			Log.e("WIFIIP", "Unable to get host address.");
			ipAddressString = "can't get ip";
		}

		return ipAddressString;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to the TokenDatabase, cast the IBinder and get TokenDatabase instance
            LocalBinder binder = (LocalBinder) service;
            dbService = binder.getService();
            dbBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            dbBound = false;
        }
    };
}
