package uk.co.mpcontracting.rpmdata.ng;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmdata.ng.model.DataProcessor;
import uk.co.mpcontracting.rpmdata.ng.model.json.JsonTrack;
import uk.co.mpcontracting.rpmdata.ng.model.page.CompletedPage;

@Slf4j
public class DataScraper {
    public static void main(String[] args) {
        String rootPage = "http://www.rpmchallenge.com/index.php?option=com_comprofiler&view=userslist&listid=7&searchmode=0&Itemid=108&limit=[LIMIT]&limitstart=[START]";
        File tracksJson = new File("data/myplayer_tracks.json");
        File newFile = new File("data/rpm-data-ng.gz");
        File oldFile = new File("data/rpm-data-old.gz");
        File mergeFile = new File("data/rpm-data.gz");
        
        log.info("Scraping data from - " + rootPage);
        log.info("Tracks json - " + tracksJson.getAbsolutePath());
        log.info("New file - " + newFile.getAbsolutePath());
        log.info("Old file - " + oldFile.getAbsolutePath());
        log.info("Merge file - " + mergeFile.getAbsolutePath());
        
        Type jsonTrackType = new TypeToken<ArrayList<JsonTrack>>(){}.getType();
        List<JsonTrack> jsonTracks = null;
        
        try (FileReader trackReader = new FileReader(tracksJson)) {
            jsonTracks = new Gson().fromJson(trackReader, jsonTrackType);
        } catch (Exception e) {
            log.error("Unable to read json", e);
            
            System.exit(1);
        }
        
        Map<Integer, List<JsonTrack>> artistTracksMap = new HashMap<>();
        
        for (JsonTrack track : jsonTracks) {
            List<JsonTrack> tracks = artistTracksMap.get(track.getCreatedBy());
            
            if (tracks == null) {
                tracks = new ArrayList<>();
                artistTracksMap.put(track.getCreatedBy(), tracks);
            }
            
            tracks.add(track);
        }
        
        log.info("Artists - " + artistTracksMap.keySet().size());
        log.info("Tracks - " + jsonTracks.size());
        
        DataProcessor dataProcessor = new DataProcessor(newFile, true);
        
        try {
            new CompletedPage().parse(rootPage, dataProcessor, artistTracksMap);
        } catch (Exception e) {
            log.error("Exception scraping data from root page - " + rootPage, e);
        } finally {
            dataProcessor.close();
        }
        
        log.info("Scraping finished");
        
        log.info("Merging data files");
        
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(mergeFile, false)), "UTF-8"))) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(oldFile)), "UTF-8"))) {
                reader.lines().forEach(line -> {
                    try {
                        writer.write(line);
                        writer.newLine();
                    } catch (IOException e) {
                        log.error("Unable to write line", e);
                    }
                });
            }
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(newFile)), "UTF-8"))) {
                reader.lines().forEach(line -> {
                    try {
                        writer.write(line);
                        writer.newLine();
                    } catch (IOException e) {
                        log.error("Unable to write line", e);
                    }
                });
            }
            
            writer.flush();
        } catch (Exception e) {
            log.error("Unable to write merge file", e);
        }
        
        log.info("Data files merged");
    }
}
