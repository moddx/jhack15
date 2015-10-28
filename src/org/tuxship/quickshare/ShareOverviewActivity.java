package org.tuxship.quickshare;

import java.util.ArrayList;

import org.tuxship.filebrowser.FileBrowserActivity;
import org.tuxship.quickshare.dao.DAOService;
import org.tuxship.quickshare.dao.DAOService.LocalBinder;
import org.tuxship.quickshare.dao.TokenDatabase;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
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

/**
 * The main activity that lists all existing shares.
 * 
 * Clicking on a share opens a ShareDetailsActivity with more information.
 * Shares can also be checked and deleted via a button.
 * 
 * Furthermore you can add new shares from within the ShareOverviewActivity,
 * which starts a simple filebrowser.
 */
public class ShareOverviewActivity extends Activity {

	Context context;
	TableLayout tlayout;
	Button deleteButton;
	
	DAOService dbService;
    boolean dbBound = false;

    private final int REQUEST_CODE_PICK_FILES = 1;
    
    Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            updateRows();
        }
    };
	
    /*
     * Sets up some class variables and starts the database server and webserver.
     * Both servers are marked as sticky (they should keep running and launch 
     * again after restart).
     * 
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
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
	
	/*
	 * Ensures database connection on start / resume of the activity
	 * and updates the share list, just in case (needed when 
	 * transitioning from CreateShareActivity to ShareOverviewActivity).
	 * 
	 * (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		
		/*
		 * Bind to database
		 */
        Intent dbIntent = new Intent(this, TokenDatabase.class);
        bindService(dbIntent, mConnection, Context.BIND_AUTO_CREATE);
        
        updateRunnable.run();	// ensure that list is really updated
        // (may actually fail, but then the other call in onServiceConnected() will succeed)
	}
	
	/*
	 * Disconnects from the database when pausing.
	 * 
	 * (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
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
	
	/*
	 * Disconnects from the database on exit.
	 * 
	 * (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
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

    /**
     * Do not call directly, but using the updateRunnable instead
     * 
     * e.g.: updateRunnable.run();
     */
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
	
	/**
	 * A placeholder that is shown instead of the sharelist,
	 * in case no shares exist.
	 */
	private void noSharesPlaceholder() {
		TableRow row = new TableRow(context);
		
		TextView noShares = new TextView(context);
		noShares.setText("No Shares yet. Sorry.");
		noShares.setTextColor(Color.LTGRAY);
		noShares.setTextSize(noShares.getTextSize() * 1.5f);
		
		row.addView(noShares, LayoutParams.MATCH_PARENT);
		tlayout.addView(row);
	}
	
	/**
	 * Deletes all shares whose corresponding checkboxes are checked.
	 * 
	 * @param _v unused parameter; needed because the function is 'called' from an xml layout file.
	 */
	public void deleteShares(View _v) {
		for(int i = 0; i < tlayout.getChildCount();) {
			TableRow row = (TableRow) tlayout.getChildAt(i);
			
			if(row.getChildCount() >= 2 &&
					dbBound &&
					((CheckBox) row.getChildAt(1)).isChecked()) {
				
				String shareName = ((TextView) row.getChildAt(0)).getText().toString();
				
				if(dbService.removeShare(shareName))
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
		
		/*
		 * Display placeholder if all shares have been deleted.
		 */
		if(tlayout.getChildCount() == 0)
			noSharesPlaceholder();
		
		deleteButton.setEnabled(tlayout.getChildCount() > 0);
	}
	
	/**
	 * Opens a simple filebrowser to select files for a new share.
	 * 
	 * The file paths are provided in the onActivityResult() method.
	 */
	private void addShare() {
		Intent selectFileIntent = new Intent(
				FileBrowserActivity.INTENT_ACTION_SELECT_FILE_MULTIPLE,
				null,
				context,
				FileBrowserActivity.class);
		
		selectFileIntent.putExtra(
				FileBrowserActivity.startDirectoryParameter, 
				Environment.getExternalStorageDirectory().getAbsolutePath());		// TODO fix this for devices without sd card
		
		selectFileIntent.putExtra(
				FileBrowserActivity.showHiddenFilesParameter, false);
		
		try {
			startActivityForResult(selectFileIntent, REQUEST_CODE_PICK_FILES);
		} catch ( ActivityNotFoundException e) {
		    e.printStackTrace();
		}
		
		
	}
	
	/*
	 * Handles results of startActivityForResult() calls.
	 * 
	 * (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == REQUEST_CODE_PICK_FILES) {
	            if(resultCode == Activity.RESULT_OK) {
	            	ArrayList<String> selectedFiles = data.getStringArrayListExtra(
	                        FileBrowserActivity.returnFileListParameter);
	                
	            	Toast.makeText(
	                    this, 
	                    "Adding:" + selectedFiles, 
	                    Toast.LENGTH_LONG
	                ).show();
	            	
	            	Intent createIntent = new Intent(context, CreateShareActivity.class);
	            	createIntent.putStringArrayListExtra(CreateShareActivity.EXTRA_FILE_LIST, selectedFiles);
					
	            	startActivity(createIntent);
	            } else {
	                Toast.makeText(
	                    this, 
	                    "Received NO valid result from file browser",
	                    Toast.LENGTH_LONG)
	                .show(); 
	            }
	        }
	        super.onActivityResult(requestCode, resultCode, data);
	}
	
	/*
	 * Creates the options menu, based on an xml layout file.
	 * 
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.share_overview, menu);
		
		return true;
	}

	/*
	 * Specifies the actions to be done, when a menu item is selected.
	 * 
	 * (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_refresh) {
			updateRunnable.run();
			return true;
		}
		if(id == R.id.action_add_share) {
			addShare();
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
     		 * Setup/Update rows every time we connect to the database again
     		 */
            updateRunnable.run();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            dbBound = false;
        }
    };
}
