package uk.co.mpcontracting.rpmdata.model.page;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.mpcontracting.rpmdata.model.DataProcessor;

public abstract class AbstractPage {
	private static Logger log = LoggerFactory.getLogger(AbstractPage.class);
	
	public static final int TIMEOUT_MILLIS = 30000;
	
	public abstract void parse(String url, DataProcessor dataProcessor) throws Exception;
	
	protected Map<String, String> parseQueryString(String url, String token) {
		Map<String, String> queryStringMap = new HashMap<String, String>();
		
		try {
			if (url != null) {
				int startIndex = url.indexOf("?");
				
				if (startIndex > -1) {
					for (StringTokenizer tokens = new StringTokenizer(url.substring(startIndex + 1), token); tokens.hasMoreTokens();) {
						String[] keyValue = tokens.nextToken().split("=");

						queryStringMap.put(keyValue[0], (keyValue.length > 1) ? keyValue[1] : "");
					}
				}
			}
		}
		catch (Exception e) {
			log.warn("Unable to parse query string - " + url, e);
		}
		
		return queryStringMap;
	}
}
