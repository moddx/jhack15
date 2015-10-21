package org.tuxship.quickshare.webcontent;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.tuxship.quickshare.TokenDatabase;

import android.content.Context;
import android.util.Log;

/**
 * The WebContent implementation used during the hackathon.
 * 
 *  It reads stuff for the different sections of an HTML file 
 *  from different files and mixes everything together into one file.
 */
public class HackWebContent implements IWebContent {

	private Context context; 
	
	public HackWebContent(Context context) {
		this.context = context;
	}
	
	private String readAssetFile(String file) {
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
	
	private String getStyles() {
		String styles = readAssetFile("hackcontent/Styles.html");
		return styles;
	}
	
	private String getHeader() {
		String header = readAssetFile("hackcontent/Header.html");
		return header;
	}
	
	private String getFooter() {
		String footer = readAssetFile("hackcontent/Footer.html");
		return footer;
	}
	
	@Override
	public String generatePage(TokenDatabase dbService, Map<String, String> parms) {
		StringBuilder page = new StringBuilder();

		page.append("<!DOCTYPE html><html><head>");
        page.append(getStyles());
        page.append("</head><body>");
        
        /*
	     * Header
	     */
        page.append(getHeader());
        
        page.append("<div id=\"content\">");
        
	    /*
	     * Login Prompt // Data Listing
	     */
        if (parms.get("accessToken") == null) {
            page.append("<form action='?' method='get'>\n"
            		+ "<p>Your Token: <input type='text' name='accessToken'><input type='submit' value='submit'></p>\n" + "</form>\n");
        } else {
            page.append("<p>Hello, here are your files (Token: " + parms.get("accessToken") + " )</p>");
            page.append("<table><tr><th>Your Files:</th></tr>\n");
            
        	List<String> files=dbService.getFilesforToken(parms.get("accessToken"));
        	for(int i =0;i<files.size();i++){
        		File f=new File(files.get(i));
        			            		
        		page.append("<tr><td>"+files.get(i)+"</td></tr>\n");
        		page.append("<tr><td>"+f.getAbsolutePath()+"</td></tr>\n");
        	}
        	
            page.append("</table>\n");
        }
        
        page.append("</div>");

        
	    /*
	     * Footer
	     */
        page.append(getFooter());
        
        page.append("</body></html>\n");

        return page.toString();
	}
	
}
