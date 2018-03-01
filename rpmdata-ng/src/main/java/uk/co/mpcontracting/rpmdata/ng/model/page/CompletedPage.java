package uk.co.mpcontracting.rpmdata.ng.model.page;

import java.net.URL;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmdata.ng.model.DataProcessor;

@Slf4j
public class CompletedPage extends AbstractPage {
    
    private static final int LIMIT = 100;
    
    private BandPage bandPage;
    
    public CompletedPage() {
        bandPage = new BandPage();
    }
    
    @Override
    public void parse(String url, DataProcessor dataProcessor) throws Exception {
        try {
            //bandPage.parse("http://www.rpmchallenge.com/index.php/component/comprofiler/userprofile/mattferrara?Itemid=108", dataProcessor); // Matt Ferrara
            //bandPage.parse("http://www.rpmchallenge.com/index.php/component/comprofiler/userprofile/284-irenepenamusic?Itemid=108", dataProcessor); // Irene Pena
            //bandPage.parse("http://www.rpmchallenge.com/index.php/component/comprofiler/userprofile/alexmoody?Itemid=108", dataProcessor); // Raw Tape (Alex Moody)
            //bandPage.parse("http://www.rpmchallenge.com/index.php/component/comprofiler/userprofile/librtine?Itemid=108", dataProcessor); // Raw Tape (Alex Moody)

            //return;
            
            int start = 0;
            Integer lastStart = null;
            
            for (;;) {
                String searchUrl = url.replace("[LIMIT]", Integer.toString(LIMIT)).replace("[START]", Integer.toString(start));
                
                log.info("Parsing CompletedPage - " + searchUrl);
    
                Document document = Jsoup.parse(new URL(searchUrl), TIMEOUT_MILLIS);
                
                // If last start is null, get it from the pagination
                if (lastStart == null) {
                    Element paginationEnd = document.select("li.cbPageNav.cbPageNavEnd > a").first();
                    lastStart = Integer.parseInt(parseQueryString(paginationEnd.attr("href"), "&").get("limitstart"));
                }

                Element completedList = document.getElementById("cbUserTable");

                for (Element completed : completedList.select("div.cbUserListFieldLine.cbUserListFL_name a")) {
                    bandPage.parse(completed.attr("href"), dataProcessor);
                }

                start += LIMIT;
                
                if (start > lastStart) {
                    break;
                }
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
