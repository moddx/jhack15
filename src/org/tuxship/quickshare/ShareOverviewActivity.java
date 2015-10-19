package org.tuxship.quickshare;

import java.util.ArrayList;

import org.tuxship.quickshare.TokenDatabase.LocalBinder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class ShareOverviewActivity extends Activity {

	Context context;
	TableLayout tlayout;
	Button deleteButton;
	
	TokenDatabase dbService;
    boolean dbBound = false;

    boolean initialised = false;
    
    Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            updateRows();
        }
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share_overview);

		context = this;
		tlayout = (TableLayout) findViewById(R.id.table);
		deleteButton = (Button) findViewById(R.id.deleteButton);
		
		/*
		 * Start database server
		 */
		startService(new Intent(this, TokenDatabase.class));
		
		/*
		 * Start web server
		 */
		startService(new Intent(this, Httpd.class));
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		/*
		 * Bind to database
		 */
        Intent dbIntent = new Intent(this, TokenDatabase.class);
        bindService(dbIntent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		/*
         * Unbind from the service
         */
		if (dbBound) {
            unbindService(mConnection);
            dbBound = false;
        }
	}
	
    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*
         * Unbind from the service
         */
        if (dbBound) {
            unbindService(mConnection);
            dbBound = false;
        }
    }

	private void updateRows() {
		if(dbBound) {
			/*
			 * for all the shares..
			 */
			ArrayList<String> shares = (ArrayList<String>) dbService.getShares();

			tlayout.removeAllViews();
			
			if(shares.size() == 0) {
				noSharesPlaceholder();
				return;
			}

			for(final String share : shares) {
				TableRow row = new TableRow(context);
				TextView nameView = new TextView(context);
				nameView.setText(share);
				nameView.setTextSize(nameView.getTextSize() * 1.25f);
				nameView.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.9f));
				row.addView(nameView);
				
				CheckBox chckBx = new CheckBox(context);
				chckBx.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.1f));
				row.addView(chckBx);

				tlayout.addView(row, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				
				/*
				 * Open a ShareDetailsActivity when clicking on a row.
				 */
				row.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent openDetailsIntent = new Intent(context, ShareDetailsActivity.class);
						openDetailsIntent.putExtra(ShareDetailsActivity.EXTRA_SHARE, share);
						
						try {
							openDetailsIntent.putExtra(ShareDetailsActivity.EXTRA_TOKEN, dbService.getToken(share));
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						startActivity(openDetailsIntent);
					}
				});
			}
			
			deleteButton.setEnabled(tlayout.getChildCount() > 0);
		} else {
			Log.e("shareintent", "overview has no dbBound");
		}
	}
	
	private void noSharesPlaceholder() {
		TableRow row = new TableRow(context);
		
		TextView noShares = new TextView(context);
		noShares.setText("No Shares yet. Sorry.");
		noShares.setTextColor(Color.LTGRAY);
		noShares.setTextSize(noShares.getTextSize() * 1.5f);
		
		row.addView(noShares, LayoutParams.MATCH_PARENT);
		tlayout.addView(row);
	}
	
	public void deleteShares(View v) {
		for(int i = 0; i < tlayout.getChildCount();) {
			TableRow row = (TableRow) tlayout.getChildAt(i);
			
			if(row.getChildCount() >= 2 &&
					dbBound &&
					((CheckBox) row.getChildAt(1)).isChecked()) {
				
				String shareName = ((TextView) row.getChildAt(0)).getText().toString();
				
				if(dbService.deleteShare(shareName))
					tlayout.removeViewAt(i);
				else {
					Toast toast = Toast.makeText(context, "Could not remove " + shareName + " from database.", Toast.LENGTH_LONG);
					toast.show();
					i++;
				}
			} else {
				i++;
			}
		}
		
		deleteButton.setEnabled(tlayout.getChildCount() > 0);
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
		if (id == R.id.action_refresh) {
			updateRunnable.run();
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
            
            /*
     		 * Setup rows initially
     		 */
            if(!initialised) {
            	updateRunnable.run();
            	initialised = true;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            dbBound = false;
        }
    };
}
