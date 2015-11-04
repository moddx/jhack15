package org.tuxship.quickshare;

import java.util.ArrayList;
import java.util.Collections;

import org.tuxship.quickshare.dao.DAOService;
import org.tuxship.quickshare.dao.DAOService.LocalBinder;
import org.tuxship.quickshare.dao.JsonDAO;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

public class CreateShareActivity extends Activity {

	public static final String EXTRA_FILE_LIST = "org.tuxship.quickshare.CreateShareActivity.EXTRA_FILE_LIST";
	private static final String LOGTAG = "Creating Share";
	
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
		
		Log.d(LOGTAG, action);
		Log.d(LOGTAG, type);

		ArrayList<Uri> uris = new ArrayList<Uri>();
		if(action.equals("android.intent.action.SEND")) {
			Uri receivedUri = (Uri)intent.getParcelableExtra(Intent.EXTRA_STREAM);
			
			uris.add(receivedUri);
			
			if(receivedUri == null)
				Log.d(LOGTAG, "receivedUri is null");
			else
				Log.d(LOGTAG, receivedUri.getPath());
			
		} else if (action.equals("android.intent.action.SEND_MULTIPLE")) {
			uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
		
			for(Uri uri : uris)
				Log.d(LOGTAG, uri.getPath());
		}
		
		files = convertUris(uris);		// convert uris to absolute paths 
		Collections.sort(files);		// and sort them
		
//		  // Figure out what to do based on the intent type
//	    if (intent.getType().indexOf("image/") != -1) {
//	        // Handle intents with image data ...
//	    } else if (intent.getType().equals("text/plain")) {
//	        // Handle intents with text ...
//	    }
		
		Log.d(LOGTAG, "ShareIntent finished.");
		
		
		setup();
	}

	@Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Unbind from the DAO
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
					Log.w(LOGTAG, "Could not store new share! dbBound: " + dbBound + " paths count: " + files.size());
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
		
		ArrayList<String> fileNames = new ArrayList<String>(files.size());
		for(String f : files) {
			String[] parts = f.split("/");
			fileNames.add(parts[parts.length - 1]);
		}
		
		FileListFragment fileListFragment = (FileListFragment) getFragmentManager().
				findFragmentById(R.id.fileListFragment); 
		fileListFragment.setListAdapter(new ArrayAdapter<String>(
				CreateShareActivity.this, 
				android.R.layout.simple_selectable_list_item, 
				fileNames));
	}
	
	
	/*
	 * Converts a list of Uri's to a list of Strings with the absolute path of the files.
	 * 
	 * Also retrieves absolute paths of content:// - Uris
	 * 
	 * Uses the Uri.getPath() method.
	 */
	private ArrayList<String> convertUris(ArrayList<Uri> uris) {
		ArrayList<String> paths = new ArrayList<String>();
		
		for(Uri uri : uris) {
			/*
			 * Handle content:// Uris for media files
			 */
			if(uri.toString().startsWith("content://")) {
				String[] parts = uri.getPath().split("/");		// e.g. "/external/audio/media/5552"
				
				String projection = null;
				if(parts[2].equals("audio"))
					projection = MediaStore.Audio.Media.DATA;
				else if(parts[2].equals("images"))
					projection = MediaStore.Images.Media.DATA;
				else if(parts[2].equals("video"))
					projection = MediaStore.Video.Media.DATA;
				else {
					Log.e(LOGTAG, "Could not determine projection for '" + uri.toString() +
							"' upon trying to convert it to an absolute path.\n Skipping.");
					continue;
				}
				
				paths.add(getRealPathFromURI(CreateShareActivity.this, uri, projection));
			} else
				paths.add(uri.getPath());
		}
		
		Log.d(LOGTAG, "List of files to add: " + paths);
		
		return paths;
	}
	
	
	@SuppressWarnings("static-method")
	private String getRealPathFromURI(Context context, Uri contentUri, String projection) {
		Cursor cursor = null;
		try {
			String[] projArr = { projection };
			cursor = context.getContentResolver().query(contentUri,  projArr, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(projection);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
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
