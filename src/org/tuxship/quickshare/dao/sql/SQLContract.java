package org.tuxship.quickshare.dao.sql;

import android.provider.BaseColumns;

public class SQLContract {

	public SQLContract() {}

	/* Inner class that defines the table contents */
	public static abstract class ShareTable implements BaseColumns {
		public static final String TABLE_NAME = "sharetable";
		public static final String COLUMN_SHARE_NAME = "name";
		public static final String COLUMN_SHARE_TOKEN = "token";
		
		public static final String BACKUP_TABLE_NAME = "sharetablebak";
	}
	
	/* Inner class that defines the table contents */
	public static abstract class FilesTable implements BaseColumns {
		public static final String TABLE_NAME = "filestable";
		public static final String COLUMN_SHARE_NAME = "share_id";
		public static final String COLUMN_FILE = "file";
		
		public static final String BACKUP_TABLE_NAME = "filestablebak";
	}
	
}
