package org.tuxship.quickshare;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

import android.content.Context;
import android.util.Log;

public class WebContent {

	private Context context; 
	
	public WebContent(Context context) {
		this.context = context;
	}
	
	private String readFile(String file) {
		String data = "";
		
		try {
			InputStream iStream = context.getAssets().open(file);
			BufferedInputStream bStream = new BufferedInputStream(iStream);
			Scanner scan = new Scanner(bStream);
			
			while(scan.hasNextLine())
				data += scan.nextLine();
			
			scan.close();
		} catch (Exception e) {
			Log.e("quickshare", "Could not open html file '" + file + "'!");
		}
		
		return data;
	}
	
	public String getStyles() {
		String styles = readFile("Styles.html");
		return styles;
	}
	
	public String getHeader() {
		String header = readFile("Header.html");
		return header;
	}
	
	public String getFooter() {
		String footer = readFile("Footer.html");
		return footer;
	}
	
}
