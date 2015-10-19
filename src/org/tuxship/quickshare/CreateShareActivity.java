package org.tuxship.quickshare;

import java.util.ArrayList;

import org.tuxship.quickshare.TokenDatabase.LocalBinder;

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
	
	Button submitBtn;
	EditText shareNameInput; 
	
	TokenDatabase dbService;
	boolean dbBound = false;
	
	ArrayList<Uri> files;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_share);
		
        // Bind to Database
        Intent dbIntent = new Intent(this, TokenDatabase.class);
        bindService(dbIntent, mConnection, Context.BIND_AUTO_CREATE);
		
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		
		Log.i("shareintent", action);
		Log.i("shareintent", type);
		
		if(action.equals("android.intent.action.SEND")) {
			files = new ArrayList<Uri>();
			Uri receivedUri = (Uri)intent.getParcelableExtra(Intent.EXTRA_STREAM);
			
			files.add(receivedUri);
			
			if(receivedUri == null)
				Log.i("shareintent", "receivedUri is null");
			else
				Log.i("shareintent", receivedUri.getPath());
			
		} else if (action.equals("android.intent.action.SEND_MULTIPLE")) {
			files = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
		
			for(Uri f : files)
				Log.i("shareintent", f.getPath());
		}
		
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
				
				ArrayList<String> paths = convertUris(files);
				
				/*
				 * Store in database
				 */
				String token = "";
				if(dbBound && paths != null) {
					token = dbService.addShare(shareName, paths);
				} else {
					Log.w("shareintent", "Could not store new share! dbBound: " + dbBound + " paths count: " + paths.size());
				}
				
				
				/*  
				 *  Launch ShareActivity
				 */
				
				Intent intent = new Intent(CreateShareActivity.this, ShareDetailsActivity.class);
				intent.putExtra(ShareDetailsActivity.EXTRA_SHARE, shareName);
				intent.putExtra(ShareDetailsActivity.EXTRA_TOKEN, token);
				startActivity(intent);
			}
		});
	}
	
	private ArrayList<String> convertUris(ArrayList<Uri> uris) {
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
