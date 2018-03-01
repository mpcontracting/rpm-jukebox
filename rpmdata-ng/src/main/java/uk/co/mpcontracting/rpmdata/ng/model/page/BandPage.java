package uk.co.mpcontracting.rpmdata.ng.model.page;

import java.net.URL;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmdata.ng.model.DataProcessor;

@Slf4j
public class BandPage extends AbstractPage {

    @Override
    public void parse(String url, DataProcessor dataProcessor) throws Exception {
        log.info("Parsing ArtistPage - " + url);
        
        try {
            Document document = Jsoup.parse(new URL(url), TIMEOUT_MILLIS);
            
            Element bandImageElement = document.select("div#cbfv_29 > img").first();

            String bandId = parseBandId(url);
            String bandName = getTextFromElementById(document, "cbfv_41");
            String bandImage = bandImageElement != null ? bandImageElement.attr("src") : null;
            String bandBiography = getTextFromElementById(document, "cbfv_61");
            String bandMembers = getTextFromElementById(document, "cbfv_62");

            /*log.info("Band =============================");
            log.info("ID - " + bandId);
            log.info("Name - " + bandName);
            log.info("Image - " + bandImage);
            log.info("Biography - " + bandBiography);
            log.info("Members - " + bandMembers);*/
            
            dataProcessor.processBandInfo(bandName, bandImage, bandBiography, bandMembers, null, null);
            
            String albumName = getTextFromElementById(document, "cbfv_66");
            String albumImage = null;
            int year = 2018;
            String preferredTrackName = getTextFromElementById(document, "cbfv_68");
            
            for (Element element : document.select("a.galleryItemName.galleryModalToggle")) {
                String imageText = element.text();
                
                if (imageText.contains(Integer.toString(year))) {
                    albumImage = element.attr("data-cbgallery-preload");
                    break;
                }
            }

            /*log.info("Album ============================");
            log.info("Name - " + albumName);
            log.info("Image - " + albumImage);
            log.info("Year - " + year);
            log.info("Preferred track - " + preferredTrackName);*/
            
            dataProcessor.processAlbumInfo(null, albumName, albumImage, year, preferredTrackName);
            
            Element playlistWrapper = document.getElementById("cb_tabid_27");
            
            if (playlistWrapper != null) {
                for (Element element : playlistWrapper.select("div.sm2-playlist-target > ul.sm2-playlist-bd > li > a")) {
                    String trackName = formatSongName(element.ownText());
                    String location = element.attr("href");
                    
                    /*log.info("Track ============================");
                    log.info("Track name - " + trackName);
                    log.info("Location - " + location);*/
                    
                    dataProcessor.processTrackInfo(bandId, albumName, trackName, location);
                }
            }
            
            // Write the band
            dataProcessor.writeBand();
        } catch (Exception e) {
            if (e instanceof HttpStatusException && ((HttpStatusException)e).getStatusCode() >= 500) {
                log.warn("Unable to fetch url - " + url + " - " + ((HttpStatusException)e).getStatusCode());
                
                throw e;
            } else {
                log.warn("Unable to fetch url - " + url + " - " + e.getMessage(), e);
            }
        }
    }
    
    private String parseBandId(String url) {
        int lastSlash = url.lastIndexOf('/');
        int query = url.lastIndexOf('?');
        
        return url.substring(lastSlash + 1, query);
    }
    
    private String formatSongName(String songName) {
        if (songName != null) {
            songName = songName.trim();
            
            if (songName.length() > 3) {
                songName = songName.substring(0, songName.length() - 3);
            }
        }
        
        return songName;
    }
}
