package org.tuxship.quickshare;

import java.util.ArrayList;

import org.tuxship.quickshare.TokenDatabase.LocalBinder;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ShareOverviewActivity extends Activity {

	TokenDatabase dbService;
    boolean dbBound = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share_overview);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		/*
		 * Start web server
		 */
		startService(new Intent(this, Httpd.class));
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

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public class PlaceholderFragment extends Fragment {

		TableLayout tlayout;
		
		public PlaceholderFragment() {
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
				}
			}
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_share_overview, container, false);

			tlayout = (TableLayout)findViewById(R.layout.fragment_share_overview);
			
			/*
			 * Setup rows
			 */
			setupRows();
			
			return rootView;
		}
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
