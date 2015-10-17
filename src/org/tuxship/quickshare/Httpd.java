package org.tuxship.quickshare;

import java.io.IOException;
import java.util.Map;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import fi.iki.elonen.NanoHTTPD;


public class Httpd extends Service
{
    private WebServer server;
    
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
	        
	        
		    /*
		     * Login Prompt // Data Listing
		     */
	        if (parms.get("username") == null) {
	            page += "<form action='?' method='get'>\n  <p>Your Token: <input type='text' name='accessToken'></p>\n" + "</form>\n";
	        } else {
	            page += "<p>Hello, " + parms.get("accessToken") + "!</p>";
	        }
	        
	        
		    /*
		     * Footer
		     */
	        page += content.getFooter();
	        
	        return newFixedLengthResponse( page + "</body></html>\n" );
	    }
	}
    
}