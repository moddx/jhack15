package org.tuxship.quickshare;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.tuxship.quickshare.dao.TokenDatabase;
import org.tuxship.quickshare.dao.TokenDatabase.LocalBinder;
import org.tuxship.quickshare.webcontent.BetterWebContent;
import org.tuxship.quickshare.webcontent.IWebContent;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.webkit.MimeTypeMap;
import fi.iki.elonen.NanoHTTPD;


public class Httpd extends Service
{
    private WebServer server;
    
    TokenDatabase dbService;
    boolean dbBound = false;
    
    public static final String GET_FILE = "getfile";
    public static final String GET_TOKEN = "token";
    
    /** Called when the service is first created. */
    @Override
	public void onCreate()
    {
    	super.onCreate();
    	
    	Log.i("org.tuxship", "onCreate() in Httpd.java is called!");
    	
    	/*
		 * Start database server
		 */
		startService(new Intent(this, TokenDatabase.class));
    	
    	// Bind to dbService
        Intent dbIntent = new Intent(this, TokenDatabase.class);
        bindService(dbIntent, mConnection, Context.BIND_AUTO_CREATE);
    	
        server = new WebServer();

        try {
            server.start();
        } catch(IOException ioe) {
            Log.e("Httpd", "The web server could not start.");
        }
        Log.i("Httpd", "Web server initialized.");
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
        // Unbind from the dbService
        if (dbBound) {
            unbindService(mConnection);
            dbBound = false;
        }
        
        // stop the webserver
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
	    	Log.i("@string/logtag", "Httpd - Serving a Request");
	        Map<String, String> parms = session.getParms();

	        if(parms.containsKey(GET_FILE)) {
	        	int fileIndex;
	        	try {
	        		fileIndex = Integer.parseInt(parms.get(GET_FILE));
	        	} catch (NumberFormatException e) {
	        		return serveNormal(session, "Illegal file parameter");
	        	}
	        	
	        	/*
	        	 * Check token
	        	 */
	        	String token = parms.get(GET_TOKEN);
	        	if(!token.matches("[[a-f][0-9]]{" + TokenDatabase.tokenLength + "}"))
	        		return serveNormal(session, "Illegal token parameter");
	        	
	        	/*
	        	 * Get files for token
	        	 */
	        	List<String> files = null;
	        	if(dbBound) {
	        		files = dbService.getFiles(token);
	        	} else
	        		Log.e("@string/logtag", "No database connection in Httpd");
	        	
	        	/*
	        	 * Check fileIndex
	        	 */
	        	if(files == null || fileIndex > files.size() || fileIndex < 0)
	        		return serveNormal(session, "Illegal file parameter");
	        	
	        	/*
	        	 * Create file stream
	        	 */
	        	String path = files.get(fileIndex);
	        	FileInputStream fis = null;
		        try {
		            fis = new FileInputStream(path);
		        } catch (FileNotFoundException e) {
		            e.printStackTrace();
		        }

		        /*
		         * Misc stuff
		         */
		        String mimeType = getMimeType(path);
		        if(mimeType == null) {
		        	Log.w("quickshare", "Could not determine mime-type for '" + path + "'. Using application/octet-stream");
		        	mimeType = "application/octet-stream";
		        }
		        
		        String[] pathParts = path.split("/");
	        	String onlyFileName = pathParts[pathParts.length - 1];
		        
	        	Log.d("quickshare", "onlyFileName of '" + path + "' is '" + onlyFileName + "'");
	        	
		        /*
		         * Serve file Stream
		         */
		        Response chunkedResponse = newChunkedResponse(Response.Status.OK, mimeType, fis);
//		        chunkedResponse.addHeader("Content-Disposition: attachment; filename=", onlyFileName);		// this should normally work
		        chunkedResponse.addHeader("Content-Disposition: attachment; filename=\"" + onlyFileName + "\"", null);
		        return chunkedResponse;
	        }
	        
	        return serveNormal(session, "");
	    }
	    
	    /*
	     * TODO add message to the page
	     */
		private Response serveNormal(IHTTPSession session, String message) {
			Map<String, String> parms = session.getParms();
			
//	    	IWebContent content = new HackWebContent(getApplication().getApplicationContext());
	        IWebContent content = new BetterWebContent(getApplication().getApplicationContext());
	    	
	        if(dbBound) {
	        	return newFixedLengthResponse(content.generatePage(dbService, parms));
	        } else {
	        	Log.e("@string/logtag", "no dbBound in Httpd.serve()");
	        	return newFixedLengthResponse("Database error");
	        }
		}
	}
	
	private static String getMimeType(String url) {
	    String type = null;
	    String extension = MimeTypeMap.getFileExtensionFromUrl(url);
	    if (extension != null) {
	        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
	    }
	    
	    Log.d("mime", "MimeType of '" + url + "' is '" + type + "'.");
	    return type;
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