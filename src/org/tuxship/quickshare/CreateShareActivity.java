package org.tuxship.quickshare;

import java.util.ArrayList;

import org.tuxship.quickshare.dao.DAOService;
import org.tuxship.quickshare.dao.DAOService.LocalBinder;
import org.tuxship.quickshare.dao.JsonDAO;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class CreateShareActivity extends Activity {

	public static final String EXTRA_FILE_LIST = "org.tuxship.quickshare.CreateShareActivity.EXTRA_FILE_LIST";
	
	Button submitBtn;
	EditText shareNameInput; 
	
	DAOService dbService;
	boolean dbBound = false;
	
	ArrayList<String> files;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_share);
		
        // Bind to Database
        Intent dbIntent = new Intent(this, JsonDAO.class);
        bindService(dbIntent, mConnection, Context.BIND_AUTO_CREATE);
		
		Intent intent = getIntent();
		
		/*
		 * Try to receive file list via extra
		 */
		files = intent.getStringArrayListExtra(EXTRA_FILE_LIST);
		if(files != null) {
			setup();
			return;
		}
		
		/*
		 * Otherwise receive files via SEND action
		 */
		String action = intent.getAction();
		String type = intent.getType();
		
		Log.i("shareintent", action);
		Log.i("shareintent", type);

		ArrayList<Uri> uris = new ArrayList<Uri>();
		if(action.equals("android.intent.action.SEND")) {
			Uri receivedUri = (Uri)intent.getParcelableExtra(Intent.EXTRA_STREAM);
			
			uris.add(receivedUri);
			
			if(receivedUri == null)
				Log.i("shareintent", "receivedUri is null");
			else
				Log.i("shareintent", receivedUri.getPath());
			
		} else if (action.equals("android.intent.action.SEND_MULTIPLE")) {
			uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
		
			for(Uri uri : uris)
				Log.i("shareintent", uri.getPath());
		}
		
		files = convertUris(uris);
		
//		  // Figure out what to do based on the intent type
//	    if (intent.getType().indexOf("image/") != -1) {
//	        // Handle intents with image data ...
//	    } else if (intent.getType().equals("text/plain")) {
//	        // Handle intents with text ...
//	    }
		
		Log.i("shareintent", "ShareIntent finished.");
		
		
		setup();
	}

	@Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        if (dbBound) {
            unbindService(mConnection);
            dbBound = false;
        }
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_share, menu);
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
	
	private void setup() {
		submitBtn = (Button)findViewById(R.id.submitBtn);
		shareNameInput = (EditText)findViewById(R.id.shareNameInput);
		
		submitBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String shareName = shareNameInput.getText().toString();
				
				if(shareName.equals(""))
					return;
				
				/*
				 * Store in database
				 */
				String token = "";
				if(dbBound && files != null) {
					token = dbService.addShare(shareName, files);
				} else {
					Log.w("shareintent", "Could not store new share! dbBound: " + dbBound + " paths count: " + files.size());
				}
				
				
				/*  
				 *  Launch ShareActivity
				 */
				Intent intent = new Intent(CreateShareActivity.this, ShareDetailsActivity.class);
				intent.putExtra(ShareDetailsActivity.EXTRA_SHARE, shareName);
				intent.putExtra(ShareDetailsActivity.EXTRA_TOKEN, token);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});
	}
	
	/*
	 * Converts a list of Uri's to a list of Strings with the absolute path of the files.
	 * 
	 * Uses the Uri.getPath() method.
	 */
	private static ArrayList<String> convertUris(ArrayList<Uri> uris) {
		ArrayList<String> paths = new ArrayList<String>();
		
		for(Uri uri : uris) {
			paths.add(uri.getPath());
		}
		
		return paths;
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
