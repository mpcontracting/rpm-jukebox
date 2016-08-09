package uk.co.mpcontracting.rpmjukebox.support;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.manager.SearchManager;
import uk.co.mpcontracting.rpmjukebox.model.Artist;
import uk.co.mpcontracting.rpmjukebox.model.Track;

@Slf4j
public abstract class DataParser implements Constants {

	private DataParser() {}
	
	public static void parse(SearchManager searchManager, URL dataFile, List<String> genreList, List<String> yearList) throws Exception {
		genreList.add(UNSPECIFIED_GENRE);

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(dataFile.openStream())))) {
			log.info("Loading data from - " + dataFile);

			// Ignore first line as its a record count
			String nextLine = null;
			ParserModelArtist currentArtist = null;
			ParserModelAlbum currentAlbum = null;
			int trackNumber = 1;

			while ((nextLine = reader.readLine()) != null) {
				try {
					// Split the string into row data
					String[] rowData = nextLine.split("\\|@\\|");

					// B = Band, A = Album, T = Track
					if ("B".equals(getRowData(rowData, 0))) {
						currentArtist = parseArtist(rowData);

						for (String genre : currentArtist.getGenres()) {
							if (!genreList.contains(genre)) {
								genreList.add(genre);
							}
						}

						searchManager.addArtist(new Artist(
							currentArtist.getArtistId(),
							currentArtist.getArtistName(),
							currentArtist.getArtistImage(),
							currentArtist.getBiography(), 
							currentArtist.getMembers()));
					} else if ("A".equals(getRowData(rowData, 0))) {
						currentAlbum = parseAlbum(rowData);
						trackNumber = 1;

						String year = Integer.toString(currentAlbum.getYear());

						if (!yearList.contains(year)) {
							yearList.add(year);
						}
					} else if ("T".equals(getRowData(rowData, 0))) {
						ParserModelTrack currentTrack = parseTrack(rowData);

						searchManager.addTrack(new Track(
							currentArtist.getArtistId(), 
							currentArtist.getArtistName(),
							currentArtist.getArtistImage(),
							currentAlbum.getAlbumId(), 
							currentAlbum.getAlbumName(),
							currentAlbum.getAlbumImage(),
							currentAlbum.getYear(), 
							currentTrack.getTrackId(),
							currentTrack.getTrackName(), 
							trackNumber++,
							currentTrack.getLocation(), 
							currentTrack.isPreferred(), 
							currentArtist.getGenres()));
					}
				} catch (Exception e) {
					log.warn("Error parsing line record - " + e.getMessage() + " - ignoring", e);
					log.warn("Record - " + nextLine);
				}
			}

			Collections.sort(genreList);
			Collections.sort(yearList);
		}
	}
	
	private static ParserModelArtist parseArtist(String[] rowData) {
		ParserModelArtist artist = new ParserModelArtist(
			getRowData(rowData, 1),
			getRowData(rowData, 2), 
			getRowData(rowData, 3), 
			getRowData(rowData, 4),
			getRowData(rowData, 5));

		String genres = getRowData(rowData, 6);

		if (genres != null) {
			for (String genre : genres.split(",")) {
				if (genre.trim().length() > 0) {
					artist.addGenre(cleanGenre(genre.trim()));
				}
			}
		} else {
			artist.addGenre(UNSPECIFIED_GENRE);
		}

		return artist;
	}

	private static ParserModelAlbum parseAlbum(String[] rowData) {
		return new ParserModelAlbum(
			getRowData(rowData, 1),
			getRowData(rowData, 2),
			getRowData(rowData, 3),
			Integer.parseInt(getRowData(rowData, 4)));
	}

	private static ParserModelTrack parseTrack(String[] rowData) {
		return new ParserModelTrack(
			getRowData(rowData, 1),
			getRowData(rowData, 2), 
			getRowData(rowData, 3),
			Boolean.valueOf(getRowData(rowData, 4)));
	}
	
	private static String getRowData(String[] rowData, int index) {
		if (index > (rowData.length - 1)) {
			return null;
		}

		return rowData[index].trim();
	}

	private static String cleanGenre(String genre) {
		if (genre == null) {
			return UNSPECIFIED_GENRE;
		}

		if (genre.equalsIgnoreCase("Unknown") || genre.equalsIgnoreCase("None") || genre.equalsIgnoreCase("Other")) {
			return UNSPECIFIED_GENRE;
		}

		if (genre.equalsIgnoreCase("rpm")) {
			return "RPM";
		}

		if (genre.startsWith("Children")) {
			return "Children's Music";
		}

		try {
			return toTitleCase(genre.replaceAll("&amp;", "&"));
		} catch (Exception e) {
			return UNSPECIFIED_GENRE;
		}
	}

	private static String toTitleCase(String string) {
		StringBuilder builder = new StringBuilder();
		StringTokenizer tokens = new StringTokenizer(string.toLowerCase());

		while (tokens.hasMoreTokens()) {
			String token = tokens.nextToken();
			builder.append(token.replaceFirst(token.substring(0, 1), token.substring(0, 1).toUpperCase()) + " ");
		}

		return builder.toString().trim();
	}
	
	@AllArgsConstructor
	@ToString(includeFieldNames = true)
	@EqualsAndHashCode(of = { "trackId" })
	private static class ParserModelTrack {
		@Getter private String trackId;
		@Getter private String trackName;
		@Getter private String location;
		@Getter private boolean isPreferred;
	}

	@AllArgsConstructor
	@ToString(includeFieldNames = true)
	@EqualsAndHashCode(of = { "albumId" })
	private static class ParserModelAlbum {
		@Getter private String albumId;
		@Getter private String albumName;
		@Getter private String albumImage;
		@Getter private int year;
	}

	@ToString(includeFieldNames = true)
	@EqualsAndHashCode(of = { "artistId" })
	private static class ParserModelArtist {
		@Getter private String artistId;
		@Getter private String artistName;
		@Getter private String artistImage;
		@Getter private String biography;
		@Getter private String members;
		@Getter private List<String> genres;

		private ParserModelArtist(String artistId, String artistName, String artistImage, String biography, String members) {
			this.artistId = artistId;
			this.artistName = artistName;
			this.artistImage = artistImage;
			this.biography = biography;
			this.members = members;

			genres = new ArrayList<String>(1);
		}

		public void addGenre(String genre) {
			genres.add(genre);
		}
	}
}
