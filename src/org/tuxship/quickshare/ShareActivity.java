package org.tuxship.quickshare;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class ShareActivity extends Activity {




	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share);

		//		Intent webIntent = new Intent();
		//		webIntent.setAction("org.tuxship.STARTHTTPD");
		//		
		//		startService(webIntent);

		/*
		 * Start web server
		 */
		startService(new Intent(this, Httpd.class));

		TokenDatabase tdb=new TokenDatabase(getApplicationContext());

		TextView  link= (TextView) findViewById(R.id.remote_link);
		link.setText("http://"+tdb.ip_wifi()+":8080");

		JSONObject a=new JSONObject();
		JSONArray a1=new JSONArray();
		a1.put("file1");
		a1.put("file2");
		a1.put("file3");
		a1.put("file4");

		JSONArray a2=new JSONArray();
		a2.put("file38921");
		a2.put("file32131");
		a2.put("file321321");
		a2.put("file31213");

		tdb.addtoJSON(a, "key1","name1", a1);
		tdb.addtoJSON(a, "key31","name2", a2);

		TextView  list= (TextView) findViewById(R.id.filelist);
		list.setText(tdb.getShares(a).toString());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
}
