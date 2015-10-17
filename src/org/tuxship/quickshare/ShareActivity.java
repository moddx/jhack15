package org.tuxship.quickshare;

//import org.json.simple.JSONObject;
import org.json.JSONObject;
import org.json.JSONException;


import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class ShareActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share);
		
      JSONObject obj = new JSONObject();
      try{
    	  obj.put("name", 1111);
    	  
      }catch(JSONException e){
    	  e.printStackTrace();
      }
      
      
      TextView text = (TextView) findViewById(R.id.hello_world);
      try{
    	  text.setText(obj.getString("name"));
      }catch(JSONException e){
    	  e.printStackTrace();
      }
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
