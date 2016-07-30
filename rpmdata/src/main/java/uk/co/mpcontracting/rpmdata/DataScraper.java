package uk.co.mpcontracting.rpmdata;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.mpcontracting.rpmdata.model.DataProcessor;
import uk.co.mpcontracting.rpmdata.model.page.IndexPage;

public class DataScraper {
	private static Logger log = LoggerFactory.getLogger(DataScraper.class);
	
	public static void main(String[] args) {
		String rootPage = "http://jukebox.rpmchallenge.com/byname.cgi";
		File outputFile = new File("data/rpm-data.txt");
		
		log.info("Scraping data from - " + rootPage);
		log.info("Output file - " + outputFile.getAbsolutePath());
		
		DataProcessor dataProcessor = new DataProcessor(outputFile);
		
		try {
			new IndexPage().parse(rootPage, dataProcessor);
		} catch (Exception e) {
			log.error("Exception scraping data from root page - " + rootPage, e);
		} finally {
			dataProcessor.close();
		}
		
		log.info("Scraping finished");
	}
}
