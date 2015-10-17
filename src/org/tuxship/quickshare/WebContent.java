package org.tuxship.quickshare;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
			FileInputStream iStream = context.openFileInput(file);
			BufferedInputStream bStream = new BufferedInputStream(iStream);
			Scanner scan = new Scanner(bStream);
			
			while(scan.hasNext())
				data += scan.next();
			
			scan.close();
		} catch (FileNotFoundException e) {
			Log.e("quickshare", "Could not open html file '" + file + "'!");
		}
		
		return data;
	}
	
	public String getStyles() {
		String styles = readFile("assets/html/Styles.html");
		return styles;
	}
	
	public String getHeader() {
		String header = readFile("assets/html/Header.html");
		return header;
	}
	
	public String getFooter() {
		String footer = readFile("assets/html/Footer.html");
		return footer;
	}
	
}
