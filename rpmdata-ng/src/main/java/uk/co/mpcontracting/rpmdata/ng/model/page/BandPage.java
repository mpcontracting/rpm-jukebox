package uk.co.mpcontracting.rpmdata.ng.model.page;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmdata.ng.model.DataProcessor;
import uk.co.mpcontracting.rpmdata.ng.model.json.JsonTrack;

@Slf4j
public class BandPage extends AbstractPage {
    
    private static final int TRACK_PREFIX_LENGTH = "http://www.rpmchallenge.com/media/com_myplayer/tracks/".length();
    private static final String TRACK_PREFIX = "http://www.rpmchallenge.com/music/2018/";
    private static final String COVER_PREFIX = "http://www.rpmchallenge.com/music/2018/covers/";

    @Override
    public void parse(String url, DataProcessor dataProcessor, Map<Integer, List<JsonTrack>> artistTracksMap) throws Exception {
        log.info("Parsing ArtistPage - " + url);
        
        try {
            Document document = Jsoup.parse(new URL(url), TIMEOUT_MILLIS);
            
            Element bandImageElement = document.select("div#cbfv_29 > img").first();

            Integer bandId = parseBandId(document);
            String bandName = getTextFromElementById(document, "cbfv_41");
            String bandImage = bandImageElement != null ? bandImageElement.attr("src") : null;
            String bandBiography = getTextFromElementById(document, "cbfv_61");
            String bandMembers = getTextFromElementById(document, "cbfv_62");
            
            if (bandImage.contains("nophoto")) {
                bandImage = null;
            }

            List<JsonTrack> tracks = artistTracksMap.get(bandId);
            
            if (tracks == null || tracks.isEmpty()) {
                log.info("No finished tracks found");
                
                return;
            }
            
            JsonTrack firstTrack = tracks.iterator().next();
            String bandGenre = firstTrack.getGenre();
            
            if (bandGenre != null && bandGenre.trim().isEmpty()) {
                bandGenre = null;
            }
            
            /*log.info("Band =============================");
            log.info("ID - " + bandId);
            log.info("Name - " + bandName);
            log.info("Image - " + bandImage);
            log.info("Biography - " + bandBiography);
            log.info("Members - " + bandMembers);
            log.info("Genre - " + bandGenre);*/

            dataProcessor.processBandInfo(bandName, bandImage, bandBiography, bandMembers, null, bandGenre);
            
            int year = 2018;
            String albumId = bandId + "_" + year;
            String albumName = getTextFromElementById(document, "cbfv_66");
            String albumImage = firstTrack.getCover();
            
            if (albumImage != null && albumImage.trim().isEmpty()) {
                albumImage = "";
            } else if (albumImage != null) {
                albumImage = COVER_PREFIX + bandId + "/" + albumImage;
            }

            String preferredTrackName = getTextFromElementById(document, "cbfv_68");
            
            if (albumImage.isEmpty()) {
                for (Element element : document.select("a.galleryItemName.galleryModalToggle")) {
                    String imageText = element.text();
                    
                    if (imageText.contains(Integer.toString(year))) {
                        albumImage = element.attr("data-cbgallery-preload");
                        break;
                    }
                }
            }

            /*log.info("Album ============================");
            log.info("Name - " + albumName);
            log.info("Image - " + albumImage);
            log.info("Year - " + year);
            log.info("Preferred track - " + preferredTrackName);*/
            
            dataProcessor.processAlbumInfo(albumId, albumName, albumImage, year, preferredTrackName);
            
            Element playlistWrapper = document.getElementById("cb_tabid_27");
            
            if (playlistWrapper != null) {
                for (Element element : playlistWrapper.select("div.sm2-playlist-target > ul.sm2-playlist-bd > li > a")) {
                    String trackName = formatSongName(element.ownText());
                    String location = element.attr("href");
                    
                    int trackSlash = location.lastIndexOf('/') + 1;
                    location = TRACK_PREFIX + bandId + "/" + location.substring(trackSlash);
                    
                    /*log.info("Track ============================");
                    log.info("Track name - " + trackName);
                    log.info("Location - " + location);*/
                    
                    dataProcessor.processTrackInfo(Integer.toString(bandId), albumId, albumName, trackName, location);
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
    
    private Integer parseBandId(Document document) {
        Element playlistWrapper = document.getElementById("cb_tabid_27");
        
        if (playlistWrapper != null) {
            Element firstSong = playlistWrapper.select("div.sm2-playlist-target > ul.sm2-playlist-bd > li > a").first();
            
            if (firstSong != null) {
                String url = firstSong.attr("href");
                String bandId = url.substring(TRACK_PREFIX_LENGTH, url.indexOf("/", TRACK_PREFIX_LENGTH));
                
                //log.info("URL - " + url);
                //log.info("Band ID - " + bandId);
                
                return Integer.valueOf(bandId);
            }
        }
        
        return null;
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
