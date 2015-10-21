package org.tuxship.quickshare.webcontent;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
		final String token = parms.get("token");

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
			String prompt = readAssetFile("prompt.html");
			
			Log.w("prompt", prompt);
			
			template.replace("_REPLACE_WITH_TABLE_OR_PROMPT_", prompt);

		} else {
			/*
			 * Generate table with files
			 */
			List<String> files = dbService.getFilesforToken(token);

			StringBuilder table = new StringBuilder();
			table.append("<table>\n");

			for (String f : files) {
				table.append("<tr><td><a href='" + token + "/" + f + "'>" + f + "</a></td></tr>\n");
			}
			
			table.append("</table>\n");
			
			template = template.replace("_REPLACE_WITH_TABLE_OR_PROMPT_", table.toString());
		}

		return template;
	}

}
