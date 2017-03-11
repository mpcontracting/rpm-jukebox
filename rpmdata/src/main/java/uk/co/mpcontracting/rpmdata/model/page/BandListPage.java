package uk.co.mpcontracting.rpmdata.model.page;

import java.net.URL;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.mpcontracting.rpmdata.model.DataProcessor;

public class BandListPage extends AbstractPage {
	private static Logger log = LoggerFactory.getLogger(BandListPage.class);
	
	private AbstractPage bandPage;
	
	public BandListPage() {
		bandPage = new BandPage();
	}
	
	@Override
	public void parse(String url, DataProcessor dataProcessor) throws Exception {
		log.info("Parsing BandListPage - " + url);
		
		try {
			Document document = Jsoup.parse(new URL(url), TIMEOUT_MILLIS);

			for (Element element : document.select("div#mainarea > a")) {
				bandPage.parse(element.attr("href"), dataProcessor);
			}
		} catch (Exception e) {
			if (e instanceof HttpStatusException) {
				log.warn("Unable to fetch url - " + url + " - " + ((HttpStatusException)e).getStatusCode());
				
				throw e;
			} else {
				log.warn("Unable to fetch url - " + url + " - " + e.getMessage());
			}
		}
	}
}
