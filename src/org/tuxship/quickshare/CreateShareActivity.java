package org.tuxship.quickshare;

import java.util.ArrayList;
import java.util.Arrays;

import org.tuxship.quickshare.TokenDatabase.LocalBinder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class CreateShareActivity extends Activity {
	
	public static final String EXTRA_FILES = "filesToShare";

	Button submitBtn;
	EditText shareNameInput; 
	
	TokenDatabase dbService;
	boolean dbBound = false;
	
	String[] files;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_share);
		
		Intent shareIntent = getIntent();
		files = shareIntent.getStringArrayExtra(EXTRA_FILES);
		
		setup();
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
					token = dbService.addShare(shareName, Arrays.asList(files));
				}
				
				
				/*  
				 *  Launch ShareActivity
				 */
				
				Intent intent = new Intent(getParent(), ShareActivity.class);
				intent.putExtra("sharename", shareName);
				intent.putExtra("sharetoken", token);
				startActivity(intent);
			}
		});
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
