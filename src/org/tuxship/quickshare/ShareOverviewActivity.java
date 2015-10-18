package org.tuxship.quickshare;

import java.util.ArrayList;

import org.tuxship.quickshare.TokenDatabase.LocalBinder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager.LayoutParams;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ShareOverviewActivity extends Activity {

	TableLayout tlayout;
	
	TokenDatabase dbService;
    boolean dbBound = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share_overview);
		
		tlayout = (TableLayout)findViewById(R.layout.activity_share_overview);
		
		/*
		 * Start database server
		 */
		startService(new Intent(this, TokenDatabase.class));
		
		// Bind to dbService
        Intent dbIntent = new Intent(this, TokenDatabase.class);
        bindService(dbIntent, mConnection, Context.BIND_AUTO_CREATE);
		
		/*
		 * Setup rows
		 */
		setupRows();
		
		/*
		 * Start web server
		 */
		startService(new Intent(this, Httpd.class));
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

	private void setupRows() {
		if(dbBound) {
			/*
			 * for all the shares..
			 */
			ArrayList<String> shares = (ArrayList<String>) dbService.getShares();
			
			int idIndex = 0;
			for(String share : shares) {
				TableRow row = new TableRow(getApplicationContext());
				TextView nameView = new TextView(getApplicationContext());
				nameView.setText(share);
				row.addView(nameView);
				
				CheckBox chckBx = new CheckBox(getApplicationContext());
				row.addView(chckBx);
				
				tlayout.addView(row, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			}
		} else {
			Log.w("shareintent", "overview has no dbBound");
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.share_overview, menu);
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
