package org.tuxship.quickshare.webcontent;

import java.util.Map;

import org.tuxship.quickshare.dao.DAOService;

/**
 * An Interface that generates web content.
 */
public interface IWebContent {

	public String generatePage(DAOService dbService, Map<String, String> parms);
	
}
