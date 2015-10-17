package org.tuxship.quickshare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class CreateShareActivity extends Activity {

	Button submitBtn;
	EditText shareNameInput; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_share);
		
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
				String input = shareNameInput.getText().toString();
				
				if(input.equals(""))
					return;
				
				/*
				 * TODO Save in database and launch ShareActivity
				 */
				
				Intent intent = new Intent(getParent(), ShareActivity.class);
				intent.putExtra("sharename", input);
				startActivity(intent);
			}
		});
	}
}
