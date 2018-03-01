package uk.co.mpcontracting.rpmdata.model.page;

import java.net.URL;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.mpcontracting.rpmdata.model.DataProcessor;

public class IndexPage extends AbstractPage {
	private static Logger log = LoggerFactory.getLogger(IndexPage.class);
	
	private AbstractPage bandListPage;
	
	public IndexPage() {
		bandListPage = new BandListPage();
	}
	
	@Override
	public void parse(String url, DataProcessor dataProcessor) throws Exception {
		log.info("Parsing IndexPage - " + url);
		
		try {
			Document document = Jsoup.parse(new URL(url), TIMEOUT_MILLIS);
			
			for (Element element : document.select("div#menu > a")) {
				bandListPage.parse(url + "?letter=" + parseQueryString(element.attr("href"), "&").get("letter"), dataProcessor);
			}
		} catch (Exception e) {
			if (e instanceof HttpStatusException && ((HttpStatusException)e).getStatusCode() >= 500) {
				log.warn("Unable to fetch url - " + url + " - " + ((HttpStatusException)e).getStatusCode());
				
				throw e;
			} else {
				log.warn("Unable to fetch url - " + url + " - " + e.getMessage());
			}
		}
	}
}
