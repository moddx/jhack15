package org.tuxship.quickshare.webcontent;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.tuxship.quickshare.Httpd;
import org.tuxship.quickshare.TokenDatabase;

import android.content.Context;
import android.util.Log;

public class BetterWebContent implements IWebContent {

	private Context context;

	public BetterWebContent(Context context) {
		this.context = context;
	}

	private String readAssetFile(String file) {
		StringBuilder data = new StringBuilder();
		;

		try {
			InputStream iStream = context.getAssets().open(file);
			BufferedInputStream bStream = new BufferedInputStream(iStream);
			Scanner scan = new Scanner(bStream);

			while (scan.hasNextLine()) {
				data.append(scan.nextLine());
				data.append("\n");
			}

			scan.close();
		} catch (Exception e) {
			Log.e("@string/logtag", "Could not open asset file '" + file + "'!");
		}

		return data.toString();
	}

	@Override
	public String generatePage(TokenDatabase dbService, Map<String, String> parms) {
		final String token = parms.get(Httpd.GET_TOKEN);

		/*
		 * Read index.html template
		 */
		String template = readAssetFile("index.html");

		/*
		 * Insert style-sheets
		 */
		template = template.replace("_REPLACE_WITH_CSS_RULES_", readAssetFile("index.css"));

		if (token == null) {
			/*
			 * Prompt for user token
			 */
			template = template.replace("_REPLACE_WITH_FILES_OR_PROMPT_", readAssetFile("prompt.html"));

		} else {
			/*
			 * Generate file list
			 */
			StringBuilder list = new StringBuilder();
			list.append("<div id='token'>" + token + "</div>");
			list.append("<p>Here are your files:</p>");
			
			
			list.append("<ul id='files'>\n");
			
			List<String> files = dbService.getFilesforToken(token);
			for (String f : files) {
				String[] parts = f.split("/");				// obtain file name from path
				String fname = parts[parts.length - 1];
				
				list.append("<li><a href='" + token + "/" + f + "'>" + fname + "</a></li>\n");
			}
			
			list.append("</ul>\n");
			
			template = template.replace("_REPLACE_WITH_FILES_OR_PROMPT_", list.toString());
		}

		return template;
	}

}
