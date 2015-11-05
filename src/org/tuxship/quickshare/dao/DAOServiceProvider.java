package org.tuxship.quickshare.dao;

import org.tuxship.quickshare.dao.sql.SQLiteDAO;

public class DAOServiceProvider {
	
	public static final Class<SQLiteDAO> SERVICE = SQLiteDAO.class;
	
}
