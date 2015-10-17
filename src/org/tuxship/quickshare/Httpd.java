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
	        String msg = "<html><body><h1>Hello server</h1>\n";
	        Map<String, String> parms = session.getParms();
	        if (parms.get("username") == null) {
	            msg += "<form action='?' method='get'>\n  <p>Your name: <input type='text' name='username'></p>\n" + "</form>\n";
	        } else {
	            msg += "<p>Hello, " + parms.get("username") + "!</p>";
	        }
	        return newFixedLengthResponse( msg + "</body></html>\n" );
	    }
	    
//	    @Override
//	    public Response serve(String uri, Method method, 
//	                          Map<String, String> header,
//	                          Map<String, String> parameters,
//	                          Map<String, String> files) {
//	    	
//	    	long totalBytes = 0;
//	    	FileInputStream stream;
//	        
//	    	try {
//	            // Open file from SD Card
//	            File root = Environment.getExternalStorageDirectory();
//	            String fileName = root.getAbsolutePath() + "/www/index.html";
//	            
//	            stream = new FileInputStream(fileName);
//	            totalBytes = new File(fileName).length();
//	        } catch(IOException ioe) {
//	            Log.w("Httpd", ioe.toString());
//	        }
	//
//	        
//	        NanoHTTPD.Response res = new NanoHTTPD.Response(Response.Status.OK, 
//	        		"text/html", stream, totalBytes);
//	        res.addHeader("Content-Disposition: attachment; filename=", fileName); 
//	        return res;
//	    }


	}
    
}