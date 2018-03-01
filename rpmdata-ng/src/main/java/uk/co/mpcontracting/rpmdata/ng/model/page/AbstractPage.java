package uk.co.mpcontracting.rpmdata.ng.model.page;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmdata.ng.model.DataProcessor;

@Slf4j
public abstract class AbstractPage {
    public static final int TIMEOUT_MILLIS = 60000;
    
    public abstract void parse(String url, DataProcessor dataProcessor) throws Exception;
    
    protected Map<String, String> parseQueryString(String url, String token) {
        Map<String, String> queryStringMap = new HashMap<>();
        
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
    
    protected String getTextFromElementById(Document document, String id) {
        Element element = document.getElementById(id);
        
        if (element != null) {
            String text = element.text().trim();
            
            if (text.length() > 0 && !"-".equals(text)) {
                return text;
            }
        }
        
        return null;
    }
}
