package org.tuxship.filebrowser;

//Heavily based on code from
//https://github.com/mburman/Android-File-Explore
//	Version of Aug 13, 2011
//Also contributed:
//  Sugan Krishnan (https://github.com/rgksugan) - Jan 2013.
//

//Project type now is Android library: 
//  http://developer.android.com/guide/developing/projects/projects-eclipse.html#ReferencingLibraryProject

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import org.tuxship.quickshare.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


public class FileBrowserActivity extends Activity {
	// Intent Action Constants
	public static final String INTENT_ACTION_SELECT_DIR = "ua.com.vassiliev.androidfilebrowser.SELECT_DIRECTORY_ACTION";
	public static final String INTENT_ACTION_SELECT_FILE = "ua.com.vassiliev.androidfilebrowser.SELECT_FILE_ACTION";
	public static final String INTENT_ACTION_SELECT_FILE_MULTIPLE = "ua.com.vassiliev.androidfilebrowser.SELECT_FILE_MULTIPLE_ACTION";

	// Intent parameters names constants
	public static final String startDirectoryParameter = "ua.com.vassiliev.androidfilebrowser.directoryPath";
	public static final String returnDirectoryParameter = "ua.com.vassiliev.androidfilebrowser.directoryPathRet";
	public static final String returnFileParameter = "ua.com.vassiliev.androidfilebrowser.filePathRet";
	public static final String returnFileListParameter = "ua.com.vassiliev.androidfilebrowser.fileListPathsRet";
	public static final String showUnreadableFilesParameter = "ua.com.vassiliev.androidfilebrowser.showCannotRead";
	public static final String showHiddenFilesParameter = "ua.com.vassiliev.androidfilebrowser.showHiddenFiles";
	public static final String filterExtension = "ua.com.vassiliev.androidfilebrowser.filterExtension";

	// Stores names of traversed directories
	ArrayList<String> pathDirsList = new ArrayList<String>();

	private static final String LOGTAG = "F_PATH";

	private ArrayList<Item> fileList = new ArrayList<Item>();
	private File path = null;
	private ArrayList<String> chosenFiles = new ArrayList<String>();
	
	private static final String EMPTY_DIRECTORY = "Directory is empty";
	
	TableLayout tlayout;
	Context context;
	
	private static final int mainColor = Color.argb(255, 173, 192, 193);
	
	private boolean showUnreadableFiles = false;
	private boolean showHiddenFiles = false;

	private boolean directoryShownIsEmpty = false;

	private String filterFileExtension = null;

	// Action constants
	private static int currentAction = -1;
	private static final int SELECT_DIRECTORY = 1;
	private static final int SELECT_FILE = 2;
	private static final int SELECT_FILE_MULTIPLE = 3;

	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filebrowser_layout);

		/*
		 * Obtain action
		 */
		Intent thisInt = this.getIntent();
		currentAction = SELECT_DIRECTORY;		// default action
		
		if (thisInt.getAction().equalsIgnoreCase(INTENT_ACTION_SELECT_FILE)) {
			Log.d(LOGTAG, "SELECT ACTION - SELECT FILE");
			currentAction = SELECT_FILE;
		} else if(thisInt.getAction().equalsIgnoreCase(INTENT_ACTION_SELECT_FILE_MULTIPLE)) {
			Log.d(LOGTAG, "SELECT ACTION - SELECT FILE MULTIPLE");
			currentAction = SELECT_FILE_MULTIPLE;
		}
		
		/*
		 * Read default extra-values when available 
		 */
		showUnreadableFiles = thisInt.getBooleanExtra(
				showUnreadableFilesParameter, false);
		
		showHiddenFiles = thisInt.getBooleanExtra(
				showHiddenFilesParameter, false);

		filterFileExtension = thisInt.getStringExtra(filterExtension);

		/*
		 * Get TableLayout & Context
		 */
		context = this;
		tlayout = (TableLayout) findViewById(R.id.filesTable);
		
		/*
		 * Setup Stuff
		 */
		this.setInitialDirectory();

		this.parseDirectoryPath();
		this.loadFileList();
		this.initializeButtons();
		this.updateFileListUI();
		this.updateCurrentDirectoryTextView();
		
		Log.d(LOGTAG, path.getAbsolutePath());
	}

	/*
	 * In case of SELECT_DIRECTORY_ACTION expects directoryPath-Extra
	 * parameter to point to the start folder.
     * If empty or null, will start from SDcard root.
	 */
	private void setInitialDirectory() {
		Intent thisInt = this.getIntent();
		String requestedStartDir = thisInt
				.getStringExtra(startDirectoryParameter);

		if (requestedStartDir != null && requestedStartDir.length() > 0) {
			File tempFile = new File(requestedStartDir);
			if (tempFile.isDirectory())
				this.path = tempFile;
		}

		if (this.path == null) {
			if (Environment.getExternalStorageDirectory().isDirectory()
					&& Environment.getExternalStorageDirectory().canRead())
				path = Environment.getExternalStorageDirectory();
			else
				path = new File("/");
		}
	}

	private void parseDirectoryPath() {
		pathDirsList.clear();
		
		String pathString = path.getAbsolutePath();
		String[] parts = pathString.split("/");

		for(int i = 0; i < parts.length; i++)
			pathDirsList.add(parts[i]);
	}

	private void initializeButtons() {
		Button upDirButton = (Button) this.findViewById(R.id.upDirectoryButton);
//		upDirButton.setBackgroundColor(mainColor);
		upDirButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(LOGTAG, "onclick for upDirButton");
				loadDirectoryUp();
				loadFileList();
				updateFileListUI();
				updateCurrentDirectoryTextView();
			}
		});

		Button selectButton = (Button) this
				.findViewById(R.id.selectButton);
//		selectButton.setBackgroundColor(mainColor);
		
		if (currentAction == SELECT_DIRECTORY) {
			selectButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.d(LOGTAG, "onclick for selectFolderButton");
					returnDirectoryFinishActivity();
				}
			});
		} else {
			selectButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.d(LOGTAG, "onclick for selectFolderButton");
					returnFileFinishActivity(chosenFiles);
				}
			});
		}
	}

	private void loadDirectoryUp() {
		// present directory removed from list
		String s = pathDirsList.remove(pathDirsList.size() - 1);
		
		// path modified to exclude present directory
		path = new File(path.toString().substring(0,
				path.toString().lastIndexOf(s)));
		
		fileList.clear();
	}

	private void updateCurrentDirectoryTextView() {
		String curDirString = "";
		for(int i = 0; i < pathDirsList.size(); i++) {
			curDirString += pathDirsList.get(i) + "/";
		}
		
		if (pathDirsList.size() == 0) {
			((Button) this.findViewById(R.id.upDirectoryButton))
					.setEnabled(false);
			curDirString = "/";
		} else
			((Button) this.findViewById(R.id.upDirectoryButton))
					.setEnabled(true);
		
		long freeSpace = getFreeSpace(curDirString);
		String formattedSpaceString = formatBytes(freeSpace);
		if (freeSpace == 0) {
			Log.d(LOGTAG, "NO FREE SPACE");
			File currentDir = new File(curDirString);
			if(!currentDir.canWrite())
				formattedSpaceString = "NON Writable";
		}

		((Button) this.findViewById(R.id.selectButton))
				.setText( 	(currentAction == SELECT_DIRECTORY)
						  	? "Select\n[" + formattedSpaceString + "]"
						  	: "Select"
						);

		((TextView) this.findViewById(R.id.currentDirectoryTextView))
				.setText("Current directory: " + curDirString);
	}

	private void showToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	private void updateFileListUI() {
		tlayout.removeAllViews();
		
		float textSize = new TextView(context).getTextSize() * 1.20f;
		
		for(final Item item : fileList) {
			/*
			 * Setup Row
			 */
			TableRow row = new TableRow(context);
			row.setBackgroundColor(mainColor);
			row.setPadding(0, 1, 0, 1);
			TableLayout.LayoutParams rowLayout = new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			rowLayout.setMargins(0, 2, 0, 1);
			row.setLayoutParams(rowLayout);

			/*
			 * Setup TextView containing an icon and the file name
			 */
			TextView fileView = new TextView(context);
			fileView.setText(item.file);
			fileView.setTextSize(textSize);
			fileView.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.9f));
			row.addView(fileView);
			
			if(!item.isDirectory) {
				/*
				 * Add checkbox for normal files
				 */
				CheckBox chckBx = new CheckBox(context);
				chckBx.setTextSize(textSize);
				chckBx.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.1f));
				chckBx.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if(isChecked) {
							chosenFiles.add(path + "/" + item.file);
						} else
							chosenFiles.remove(path + "/" + item.file);
					}
				});
	
				/*
				 * Set file icon
				 */
				fileView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.file_icon, 0,
						0, 0);
				fileView.setEllipsize(null);
				
				row.addView(chckBx);
			} else {
				/*
				 * Set folder icon
				 */
				int drawableID = (item.canRead) ? R.drawable.folder_icon : R.drawable.folder_icon_light;
				fileView.setCompoundDrawablesWithIntrinsicBounds(drawableID, 0,
						0, 0);
				fileView.setEllipsize(null);
				
				fileView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View _v) {
						TextView view = (TextView) _v;

						Item onClickItem = FileBrowserActivity.this.getItem(view.getText().toString());

						if (onClickItem.canRead && !onClickItem.isEmpty) {
							pathDirsList.add(onClickItem.file);
							path = new File(path + "/" + onClickItem.file);

							Log.d(LOGTAG, "Just reloading the list");
							loadFileList();

							updateFileListUI();
							updateCurrentDirectoryTextView();

							Log.d(LOGTAG, path.getAbsolutePath());
						}
					}
				});
			}

			/*
			 * Add row to table
			 */
			tlayout.addView(row);
		}
		
	}

	private void returnDirectoryFinishActivity() {
		Intent retIntent = new Intent();
		retIntent.putExtra(returnDirectoryParameter, path.getAbsolutePath());
		this.setResult(RESULT_OK, retIntent);
		this.finish();
	}

	private void returnFileFinishActivity(ArrayList<String> filePaths) {
		Intent retIntent = new Intent();
		retIntent.putStringArrayListExtra(returnFileListParameter, filePaths);
		this.setResult(RESULT_OK, retIntent);
		this.finish();
	}

	private void loadFileList() {
		try {
			path.mkdirs();
		} catch (SecurityException e) {
			Log.e(LOGTAG, "unable to write on the sd card ");
		}

		fileList.clear();
		chosenFiles.clear();
		
		if (path.exists() && path.canRead()) {
			
			FilenameFilter filter = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					File file = new File(dir, filename);
					
					/*
					 * Should the file be shown in general?
					 */
					boolean showFile = 
							(showUnreadableFiles || file.canRead()) &&
							(showHiddenFiles || !file.getName().startsWith("."));
			
					/*
					 * Make sure that a file is a file and a dir is a dir.
					 */
					switch(currentAction) {
						case SELECT_DIRECTORY:
							return (file.isDirectory() && showFile);
						case SELECT_FILE:
						case SELECT_FILE_MULTIPLE:
							return (file.isFile() && filterFileExtension != null)
								? (showFile && file.getName().endsWith(filterFileExtension))	// check the extension if filters are provided 
								: (showFile);
						default:
							return true;
					}
				}
			};

			String[] fList = path.list(filter);
			this.directoryShownIsEmpty = false;
			
			for (int i = 0; i < fList.length; i++) {
				// Convert into file path
				File file = new File(path, fList[i]);
				
				fileList.add(i, new Item(fList[i], file.isDirectory(), file.canRead()));
			}
			
			if (fileList.size() == 0) {
				this.directoryShownIsEmpty = true;
				fileList.add(0, new Item(EMPTY_DIRECTORY, true, true, true));
			} else {
				Collections.sort(fileList, new ItemFileNameComparator());
			}
		} else {
			Log.e(LOGTAG, "path does not exist or cannot be read");
		}
		
	}

	private Item getItem(String name) {
		int index = Arrays.binarySearch(fileList.toArray(new Item[0]), new Item(name, false, false), new ItemFileNameStringComparator());
		
		if(index < 0)
			throw new IndexOutOfBoundsException("Cannot getItem() for name '" + name + "'");
		
		return fileList.get(index);
	}

	/*
	 * Item Class
	 */
	public class Item {
		public String file;
		public boolean isDirectory;
		public boolean isEmpty;
		public boolean canRead;

		public Item(String file, boolean isDirectory, boolean canRead) {
			this.file = file;
			this.isDirectory = isDirectory;
			this.canRead = canRead;
			this.isEmpty = false;
		}

		public Item(String file, boolean isDirectory, boolean canRead, boolean isEmpty) {
			this.file = file;
			this.isDirectory = isDirectory;
			this.canRead = canRead;
			this.isEmpty = isEmpty;
		}
		
		@Override
		public String toString() {
			return file;
		}
	}

	
	private class ItemFileNameStringComparator implements Comparator<Item> {
		@SuppressLint("DefaultLocale")
		@Override
		public int compare(Item lhs, Item rhs) {
			return lhs.file.toLowerCase().compareTo(rhs.file.toLowerCase());
		}
	}
	
	/*
	 * Item Comparator
	 */
	private class ItemFileNameComparator implements Comparator<Item> {
		
		@SuppressLint("DefaultLocale")
		@Override
		public int compare(Item lhs, Item rhs) {
			if(lhs.isDirectory && !rhs.isDirectory)
				return -1;
			
			if(!lhs.isDirectory && rhs.isDirectory)
				return 1;
			
			return lhs.file.toLowerCase().compareTo(rhs.file.toLowerCase());
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Log.d(LOGTAG, "ORIENTATION_LANDSCAPE");
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			Log.d(LOGTAG, "ORIENTATION_PORTRAIT");
		}
		// Layout apparently changes itself, only have to provide good onMeasure
		// in custom components
	
		// TODO: check with keyboard
		// if(newConfig.keyboard == Configuration.KEYBOARDHIDDEN_YES)
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.filebrowser_menu, menu);

		/*
		 * Toggle visibility of hidden files
		 */
		final MenuItem showHiddenFilesItem = menu.findItem(R.id.action_show_hidden_files);
		final Switch showHiddenFilesSwitch = (Switch) showHiddenFilesItem.getActionView();
		showHiddenFilesSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Log.i("filebrowser", "Show Hidden Files - Switch toggled");
				
				showHiddenFiles = isChecked;
				loadFileList();
				updateFileListUI();
			}
			
		});
		
		return true;
	}

	public static long getFreeSpace(String path) {
		StatFs stat = new StatFs(path);
		long availSize = stat.getAvailableBlocksLong() 
				* stat.getBlockSizeLong();
		return availSize;
	}

	/*
	 * Formats the bytes as either Gigabytes, Megabytes, Kilobytes or Bytes
	 * with 3 decimal places. 
	 *  
	 * 
	 * e.g.: 3073741824 bytes will be formatted as: 
	 * 			2,862 GB
	 */
	public static String formatBytes(long bytes) {
		// TODO: add flag to which part is needed (e.g. GB, MB, KB or bytes)
		
		/*
		 * Some Gigabytes
		 */
		if (bytes > 1073741824) {		// One binary gigabyte equals 1,073,741,824 bytes.
			double gbs = bytes / (double) 1073741824;
			return String.format(Locale.getDefault(), "%.3f GB", gbs);
		}
			
		/*
		 * Some Megabytes
		 */
		if (bytes > 1048576) {			// One MB - 1048576 bytes
			double mbs = bytes / (double) 1048576;
			return String.format(Locale.getDefault(), "%.3f MB", mbs); 
		}
		
		/*
		 * Some Kilobytes
		 */
		if (bytes > 1024) {
			double kbs = bytes / (double) 1024;
			return String.format(Locale.getDefault(), "%.3f KB", kbs); 
		}

		/*
		 * Some bytes
		 */
		return String.format(Locale.getDefault(), "%d bytes", bytes);
	}

}
