package org.tuxship.quickshare;


import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ShareDetailsActivity extends Activity {

	public static final String EXTRA_SHARE = "extraShareName";
	public static final String EXTRA_TOKEN = "extraToken";
	
	String shareName;
	String token;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share_details);
		
		/*
		 * Receive intent extras.
		 */
		Intent inputIntent = getIntent();
		shareName = inputIntent.getStringExtra(EXTRA_SHARE);
		token = inputIntent.getStringExtra(EXTRA_TOKEN);
		
		setupTextViews();
		
		Button allSharesBtn = (Button)findViewById(R.id.buttonAllShare);

		/*
		 * Open the ShareOverviewActivity on click.
		 */
		allSharesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
			public void onClick(View v) {
				Intent intent = new Intent(ShareDetailsActivity.this, ShareOverviewActivity.class);
				startActivity(intent);
            }
        });
		
	}
	
	private void setupTextViews() {
		/*
		 * Setup TextViews.
		 */
		TextView shareText = (TextView)findViewById(R.id.shareText);
		shareText.setText(shareName);
		
		setShareUrl();
		
		TextView tokenText = (TextView)findViewById(R.id.tokenText);
		tokenText.setText(token);
	}
	
	private void setShareUrl() {
		TextView linkText = (TextView)findViewById(R.id.linkText);
		String ip;
		if( (ip = getWifiIP()).equals("") ) {
			linkText.setText("No Wifi available!");
			linkText.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark));
		} else {
			linkText.setText("http://" + ip + ":8080");
			linkText.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.primary_text_dark));
		}
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
			Log.e("@string/logtag", "Unable to get host address.");
			ipAddressString = "";
		}

		return ipAddressString;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.share_details, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_refresh) {
			setShareUrl();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
