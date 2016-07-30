package uk.co.mpcontracting.rpmdata.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.mpcontracting.rpmdata.model.data.Album;
import uk.co.mpcontracting.rpmdata.model.data.Band;
import uk.co.mpcontracting.rpmdata.model.data.Track;

public class DataProcessor {
	private static Logger log = LoggerFactory.getLogger(DataProcessor.class);

	private Writer writer;

	private int bandId;
	private int albumId;
	private int trackId;
	
	private Band band;

	public DataProcessor(File outputFile) {
		try {
			if (outputFile.exists()) {
				outputFile.delete();
			}
			
			writer = new BufferedWriter(new FileWriter(outputFile));
		} catch (Exception e) {
			log.error("Enable to create output file - " + outputFile, e);
			throw new RuntimeException(e);
		}
	}

	public void processBandInfo(String name, String image, String biography, String members, String yearsCompleted, String genres) {
		if (band == null || !band.getName().equals(name)) {
			if (band != null) {
				writeBand();
			}

			band = new Band(++bandId, name, image, biography, members, genres);
		}
	}
	
	public void processAlbumInfo(String name, String image, int year, String preferredTrackName) {
		band.addAlbum(new Album(++albumId, name, image, year, preferredTrackName));
	}
	
	public void processTrackInfo(String albumName, String trackName, String location) {
		for (Album album : band.getAlbums()) {
			if (albumName.equalsIgnoreCase(album.getName())) {
				album.addTrack(new Track(++trackId, trackName, location));
			}
		}
	}
	
	private void writeBand() {
		try {
			if (band != null && band.isValid()) {
				writer.write(band.getDataRow() + "\n");

				for (Album album : band.getAlbums()) {
					if (album.getTracks().isEmpty()) {
						continue;
					}
					
					writer.write(album.getDataRow() + "\n");

					for (Track track : album.getTracks()) {
						writer.write(track.getDataRow() + "\n");
					}
				}
			}
		} catch (Exception e) {
			log.error("Error writing band : " + band.getId() + " - " + band.getName());
		}
	}
	
	public void close() {
		writeBand();
		
		if (writer != null) {
			try {
				writer.flush();
				writer.close();
			} catch (Exception e) {}
		}
	}
}
