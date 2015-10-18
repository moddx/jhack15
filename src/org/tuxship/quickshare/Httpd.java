package org.tuxship.quickshare;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.tuxship.quickshare.TokenDatabase.LocalBinder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import fi.iki.elonen.NanoHTTPD;


public class Httpd extends Service
{
	private WebServer server;

	TokenDatabase dbService;
	boolean dbBound = false;

	public Httpd() {

	}

	/** Called when the service is first created. */
	@Override
	public void onCreate()
	{
		super.onCreate();

		Log.i("org.tuxship", "onCreate() in Httpd.java is called!!!!!!!!!!!!!!!");

		server = new WebServer();

		try {
			server.start();
		} catch(IOException ioe) {
			Log.w("Httpd", "The server could not start.");
		}
		Log.w("Httpd", "Web server initialized.");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	// DON'T FORGET to stop the server
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (server != null)
			server.stop();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	private class WebServer extends NanoHTTPD {

		public WebServer()
		{
			super(8080);
		}

		@Override
		public Response serve(IHTTPSession session) {
			Log.i("quickshare", "SERVING A REQUEST");
			Map<String, String> parms = session.getParms();

			WebContent content = new WebContent(getApplication().getApplicationContext());

			String page = "<!DOCTYPE html><html><head>";
			page += content.getStyles();
			page += "</head><body>";

			/*
			 * Header
			 */
			page += content.getHeader();

			page += "<div id=\"content\">";

			/*
			 * Login Prompt // Data Listing
			 */
			if (parms.get("accessToken") == null) {
				page += "<form action='?' method='get'>\n"
						+ "<p>Your Token: <input type='text' name='accessToken'><input type='submit' value='submit'></p>\n" + "</form>\n";
			} else {
				page += "<p>Hello, here are your files (Token: " + parms.get("accessToken") + " )</p>";
				page += "<table><tr><th>Your Files:</th></tr>\n";
				if(dbBound){
					List<String> files=dbService.getFilesforToken(parms.get("accessToken"));
					for(int i =0;i<files.size();i++){
						page +="<tr><td>"+files.get(i)+"</td></tr>\n";
					}
				}
				page += "</table>\n";
			}

			page += "</div>";


			/*
			 * Footer
			 */
			page += content.getFooter();

			return newFixedLengthResponse( page + "</body></html>\n" );
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