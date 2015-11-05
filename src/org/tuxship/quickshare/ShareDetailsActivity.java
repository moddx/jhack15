package org.tuxship.quickshare;


import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;

import org.tuxship.quickshare.dao.DAOService;
import org.tuxship.quickshare.dao.DAOService.LocalBinder;
import org.tuxship.quickshare.dao.DAOService.TokenNotFoundException;
import org.tuxship.quickshare.dao.DAOServiceProvider;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ShareDetailsActivity extends Activity {

	public static final String EXTRA_SHARE = "extraShareName";
	public static final String EXTRA_TOKEN = "extraToken";
	
	private static final String LOGTAG = "ShareDetails";
	
	String shareName;
	String token;
	
	DAOService dbService;
	boolean dbBound = false;
	
	boolean initialized = false;
	
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
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		// Bind to Database
        Intent dbIntent = new Intent(this, DAOServiceProvider.SERVICE);
        startService(dbIntent);
        bindService(dbIntent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
	private void setupEverything() {
		if(initialized)
			return;
		
		setupTextViews();
		setupButton();
		setupFileList();
		
		if(dbBound) {
			initialized = true;
			
			unbindService(mConnection);
			dbBound = false;	
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Bind to Database
		if(!dbBound) {
	        Intent dbIntent = new Intent(this, DAOServiceProvider.SERVICE);
	        bindService(dbIntent, mConnection, Context.BIND_AUTO_CREATE);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		// Unbind from the DAO
		if(dbBound) {
			unbindService(mConnection);
			dbBound = false;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		// Unbind from the DAO
		if(dbBound) {
			unbindService(mConnection);
			dbBound = false;
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		shareName = intent.getStringExtra(EXTRA_SHARE);
		token = intent.getStringExtra(EXTRA_TOKEN);
		
		setupTextViews();
	}
	
	/*
	 * Sets up the TextViews
	 */
	private void setupTextViews() {
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
	
	/*
	 * Sets up the button
	 */
	private void setupButton() {
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
	
	/*
	 * Sets up the file list
	 */
	private void setupFileList() {
		/*
		 * Obtain file list
		 */
		ArrayList<String> files = null;
		
		if(dbBound) {
			try {
				files = new ArrayList<String>(dbService.getFiles(token));
				for(int i = 0; i < files.size(); i++) {			// only keep filenames without path
					String[] parts = files.remove(i).split("/");
					files.add(i, parts[parts.length - 1]);
				}
			} catch (TokenNotFoundException e) {
				Toast.makeText(ShareDetailsActivity.this, "Token not found in DB", Toast.LENGTH_LONG).show();;
				Log.e(LOGTAG, "Token '" + token + "' not found in DB.");
			}
		} else 
			Log.e(LOGTAG, "No database bound in ShareDetailsActivity!");
		
		if(files == null)
			files = new ArrayList<String>();
		
		/*
		 * Setup file list fragment
		 */
		FileListFragment fileListFragment = (FileListFragment) getFragmentManager().
				findFragmentById(R.id.fileListFragment);
		fileListFragment.setListAdapter(new ArrayAdapter<String>(
				ShareDetailsActivity.this, 
				android.R.layout.simple_selectable_list_item, 
				files));
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
			Log.e(LOGTAG, "Unable to get host address.");
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
	
	/** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to the TokenDatabase, cast the IBinder and get TokenDatabase instance
            LocalBinder binder = (LocalBinder) service;
            dbService = binder.getService();
            dbBound = true;
            
            setupEverything();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            dbBound = false;
        }
    };
}
