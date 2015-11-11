package org.tuxship.quickshare.dao;

import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * This abstract class aims to provide a base for different 
 * database providers, which can store shares.
 * Other components should bind to this Service using an 
 * android.content.ServiceConnection.
 * 
 * <p>It supports:
 * <ul>
 *  <li> adding and removing shares, </li>
 *  <li> obtaining a list of all share names, </li>
 *  <li> obtaining the files of a share and </li>
 *  <li> obtaining the access token of a share. </li>
 * </ul>
 *  </p>
 * 
 * @author Matthias Ervens, 2015
 *
 */
public abstract class DAOService extends Service {
	
	public static final int TOKEN_LENGTH = 6;
	public static final int TYPE_TOKEN = 0;
	public static final int TYPE_SHARENAME = 1;
	
	protected static final String LOGTAG = "DAOService";
	
	// Binder given to clients
    protected final IBinder binder = new LocalBinder();

    @Override
    public final void onCreate() {
    	Log.i(LOGTAG, "Creating DAOService..");
    	super.onCreate();
    }
    
    
    @Override
    public final int onStartCommand(Intent intent, int flags, int startId) {
    	super.onStartCommand(intent, flags, startId);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    
	@Override
	public final IBinder onBind(Intent intent) {
		return binder;
	}

	
	/**
	 * Binder implementation that allows to bind to this service.
	 */
	public final class LocalBinder extends Binder {
		public DAOService getService() {
			return DAOService.this;
		}
	}
    
	
    /**
     * Adds a share to the database
     * <p>
     * Returns a token that allows to access the share from the webservice
     * or other future frontends.
     * <p>
     * The share name and the file list both may not be empty.
     * 
     * @param name 	the name of the share
     * @param files the files to share
     * @return the access token
     */
	public abstract String addShare(String name, List<String> files);
	
	
	/**
	 * Removes a share from the database.
	 * 
	 * Returns the success of removal.
	 * 
	 * @param name	the name of the share to remove
	 * @return	success of removal
	 */
	public abstract boolean removeShare(String name);

	
	/**
	 * Returns the names of all shares stored in the database.
	 * 
	 * @return	names of all shares
	 */
	public abstract List<String> getShares();

	
	/**
	 * Returns the count of the stored shares.
	 * 
	 * @return the share count
	 */
	public abstract int getShareCount();
	
	/**
	 * Returns a list of files that correspond to an identifier.
	 * <p>
	 * The identifier can either be an access token or the name of a share.
	 * The type of the identifier needs to be passed as well.
	 * This can either be {@code DAOService.TYPE_TOKEN} or {@code DAOService.TYPE_SHARENAME}.
	 * 
	 * @param identifier 	can either be an access token or the name of a share
	 * @param type 			the type of the identifier
	 * @return	the files that are stored with this token 
	 */
	public abstract List<String> getFiles(String identifier, int type) throws TokenNotFoundException, ShareNotFoundException;

	
	/**
	 * Returns the token that grants access to a share.
	 * <p>
	 * Each share has a unique token. It may be derived
	 * partly or fully from the files of a share. 
	 * 
	 * @param share	the name of the share
	 * @return	the access token of the share
	 */
	public abstract String getToken(String share) throws ShareNotFoundException;
	
	
	/**
	 * Thrown when a share name, passed as a parameter, does not exist.
	 */
	public final class ShareNotFoundException extends Exception {
		/**
		 * Generated serialVersionUID
		 */
		private static final long serialVersionUID = -6940677985750749370L;
		
		public ShareNotFoundException() {}
		
		public ShareNotFoundException(String message) {
			super(message);
		}
	}
	
	/**
	 * Thrown when a token, passed as a parameter, does not exist.
	 */
	public final class TokenNotFoundException extends Exception {
		/**
		 * Generated serialVersionUID
		 */
		private static final long serialVersionUID = 3753580994362735631L;
		
		public TokenNotFoundException() {}
		
		public TokenNotFoundException(String message) {
			super(message);
		}
	}
	
}
