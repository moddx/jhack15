package org.tuxship.quickshare.webcontent;

import java.util.Map;

import org.tuxship.quickshare.dao.TokenDatabase;

/**
 * An Interface that generates web content.
 */
public interface IWebContent {

	public String generatePage(TokenDatabase dbService, Map<String, String> parms);
	
}
