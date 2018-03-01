package uk.co.mpcontracting.rpmdata.ng;

import java.io.File;

import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmdata.ng.model.DataProcessor;
import uk.co.mpcontracting.rpmdata.ng.model.page.CompletedPage;

@Slf4j
public class DataScraper {
    public static void main(String[] args) {
        String rootPage = "http://www.rpmchallenge.com/index.php?option=com_comprofiler&view=userslist&listid=7&searchmode=0&Itemid=108&limit=[LIMIT]&limitstart=[START]";
        File outputFile = new File("data/rpm-data.gz");
        
        log.info("Scraping data from - " + rootPage);
        log.info("Output file - " + outputFile.getAbsolutePath());
        
        DataProcessor dataProcessor = new DataProcessor(outputFile, true);
        
        try {
            new CompletedPage().parse(rootPage, dataProcessor);
        } catch (Exception e) {
            log.error("Exception scraping data from root page - " + rootPage, e);
        } finally {
            dataProcessor.close();
        }
        
        log.info("Scraping finished");
    }
}
