package uk.co.mpcontracting.rpmjukebox.service;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.UNSPECIFIED_GENRE;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.co.mpcontracting.rpmjukebox.config.ApplicationProperties;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.util.HashGenerator;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataService {

  private static final int ARTIST_ID = 1;
  private static final int ARTIST_NAME = 2;
  private static final int ARTIST_BIOGRAPHY = 4;
  private static final int ARTIST_MEMBERS = 5;
  private static final int ARTIST_GENRES = 6;

  private static final int ALBUM_ID = 1;
  private static final int ALBUM_NAME = 2;
  private static final int ALBUM_YEAR = 4;

  private static final int TRACK_NAME = 2;
  private static final int TRACK_PREFERRED = 4;
  private static final int TRACK_INDEX = 5;
  private static final int TRACK_GENRE = 6;

  private final ApplicationProperties applicationProperties;
  private final HashGenerator hashGenerator;

  private final InternetService internetService;
  private final SearchService searchService;

  void parse(URL dataFile) {
    log.info("Loading data from - {}", dataFile);

    try {
      ParserModelData parserModelData = new ParserModelData();

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(
          internetService.openConnection(dataFile).getInputStream()), UTF_8))) {
        reader.lines().forEach(line -> {
          try {
            // Split the string into row data
            String[] rowData = line.split("\\|@\\|");

            // B = Band, A = Album, T = Track
            if ("B".equals(getRowData(rowData, 0))) {
              ParserModelArtist parserModelArtist = parseArtist(rowData);
              parserModelData.setArtist(parserModelArtist);
            } else if ("A".equals(getRowData(rowData, 0))) {
              parserModelData.setAlbum(parseAlbum(rowData));
            } else if ("T".equals(getRowData(rowData, 0))) {
              ParserModelArtist parserModelArtist = parserModelData.getArtist();
              ParserModelAlbum parserModelAlbum = parserModelData.getAlbum();
              ParserModelTrack parserModelTrack = parseTrack(rowData);
              String trackKey = getTrackKey(parserModelArtist, parserModelAlbum, parserModelTrack);
              String albumName = parserModelAlbum.getAlbumName().isEmpty() ? parserModelArtist.getArtistName() : parserModelAlbum.getAlbumName();

              searchService.addTrack(Track.builder()
                  .artistId(parserModelArtist.getArtistId())
                  .artistName(parserModelArtist.getArtistName())
                  .albumId(parserModelAlbum.getAlbumId())
                  .albumName(albumName)
                  .albumImage(applicationProperties.getS3BucketUrl() + getAlbumImageKey(parserModelArtist, parserModelAlbum))
                  .year(parserModelAlbum.getYear())
                  .trackId(hashGenerator.generateHash(trackKey))
                  .trackName(parserModelTrack.getTrackName())
                  .index(parserModelTrack.getIndex())
                  .location(applicationProperties.getS3BucketUrl() + trackKey)
                  .isPreferred(parserModelTrack.isPreferred())
                  .genres(ofNullable(parserModelTrack.getGenre())
                      .map(Collections::singletonList)
                      .orElse(parserModelArtist.getGenres())
                  )
                  .build());
            }
          } catch (Exception e) {
            log.warn("Error parsing line record - {} - ignoring", e.getMessage(), e);
            log.warn("Record - {}", line);
          }
        });
      }
    } catch (Exception e) {
      log.error("Unable to open connection to data file {}", dataFile, e);
    }
  }

  private String getAlbumImageKey(ParserModelArtist parserModelArtist, ParserModelAlbum parserModelAlbum) {
    return "image/album/" +
        parserModelAlbum.getYear() + "/" +
        parserModelArtist.getArtistId() + "/" +
        parserModelAlbum.getAlbumId();
  }

  private String getTrackKey(ParserModelArtist parserModelArtist, ParserModelAlbum parserModelAlbum,
      ParserModelTrack parserModelTrack) {
    return "music/" +
        parserModelAlbum.getYear() + "/" +
        parserModelArtist.getArtistId() + "/" +
        parserModelAlbum.getAlbumId() + "/" +
        formatNumber(parserModelTrack.getIndex());
  }

  private String formatNumber(int index) {
    String string = "000" + index;

    return string.substring(string.length() - 3);
  }

  private ParserModelArtist parseArtist(String[] rowData) {
    return ParserModelArtist.builder()
        .artistId(hashGenerator.generateHash(getRowData(rowData, ARTIST_ID)))
        .artistName(getRowData(rowData, ARTIST_NAME))
        .biography(getRowData(rowData, ARTIST_BIOGRAPHY))
        .members(getRowData(rowData, ARTIST_MEMBERS))
        .genres(ofNullable(getRowData(rowData, ARTIST_GENRES))
            .map(genres -> stream(genres.split(","))
                .filter(genre -> !genre.trim().isEmpty())
                .map(genre -> cleanGenre(genre, true))
                .collect(toList())
            )
            .orElse(singletonList(UNSPECIFIED_GENRE)))
        .build();
  }

  private ParserModelAlbum parseAlbum(String[] rowData) {
    return ParserModelAlbum.builder()
        .albumId(hashGenerator.generateHash(getRowData(rowData, ALBUM_ID)))
        .albumName(getRowData(rowData, ALBUM_NAME))
        .year(ofNullable(getRowData(rowData, ALBUM_YEAR)).map(Integer::valueOf).orElse(null))
        .build();
  }

  private ParserModelTrack parseTrack(String[] rowData) {
    return ParserModelTrack.builder()
        .index(ofNullable(getRowData(rowData, TRACK_INDEX)).map(Integer::parseInt).orElse(-1))
        .trackName(getRowData(rowData, TRACK_NAME))
        .isPreferred(Boolean.parseBoolean(getRowData(rowData, TRACK_PREFERRED)))
        .genre(cleanGenre(getRowData(rowData, TRACK_GENRE), false))
        .build();
  }

  private String getRowData(String[] rowData, int index) {
    if (index > (rowData.length - 1)) {
      return null;
    }

    return rowData[index].trim();
  }

  private String cleanGenre(String genre, boolean blankIsUnspecified) {
    if (isNull(genre) || genre.isEmpty()) {
      return blankIsUnspecified ? UNSPECIFIED_GENRE : null;
    }

    if (genre.equalsIgnoreCase("Unknown") || genre.equalsIgnoreCase("None") || genre.equalsIgnoreCase("Other")
        || genre.equalsIgnoreCase("0")) {
      return UNSPECIFIED_GENRE;
    }

    if (genre.equalsIgnoreCase("rpm")) {
      return "RPM";
    }

    if (genre.startsWith("Children")) {
      return "Children's Music";
    }

    try {
      return toTitleCase(genre.replaceAll("&amp;", "&")).trim();
    } catch (Exception e) {
      return UNSPECIFIED_GENRE;
    }
  }

  private String toTitleCase(String string) {
    StringBuilder builder = new StringBuilder();
    Scanner scanner = new Scanner(string).useDelimiter(" ");
    scanner.forEachRemaining(token -> builder.append(token.replaceFirst(token.substring(0, 1),
        token.substring(0, 1).toUpperCase())).append(' '));

    return builder.toString();
  }

  @Data
  private static class ParserModelData {
    public ParserModelArtist artist;
    public ParserModelAlbum album;
  }

  @Value
  @Builder
  private static class ParserModelArtist {
    String artistId;
    String artistName;
    String biography;
    String members;
    List<String> genres;
  }

  @Value
  @Builder
  private static class ParserModelAlbum {
    String albumId;
    String albumName;
    Integer year;
  }

  @Value
  @Builder
  private static class ParserModelTrack {
    int index;
    String trackName;
    boolean isPreferred;
    String genre;
  }
}
