package org.tuxship.quickshare.webcontent;

import java.util.Map;

import org.tuxship.quickshare.TokenDatabase;

/**
 * An Interface that generates web content.
 */
public interface IWebContent {

	public StringBuilder generatePage(TokenDatabase dbService, Map<String, String> parms);
	
}
