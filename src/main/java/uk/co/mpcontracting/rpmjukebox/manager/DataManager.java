package uk.co.mpcontracting.rpmjukebox.manager;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.mpcontracting.rpmjukebox.configuration.AppProperties;
import uk.co.mpcontracting.rpmjukebox.model.Artist;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.HashGenerator;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataManager implements Constants {

    private final AppProperties appProperties;
    private final HashGenerator hashGenerator;

    private SearchManager searchManager;
    private InternetManager internetManager;

    private Map<String, Integer> artistIndexMap;
    private AtomicInteger artistIndex;

    private Map<String, Integer> albumIndexMap;
    private AtomicInteger albumIndex;

    private AtomicInteger trackIndex;

    @Autowired
    public void wireSearchManager(SearchManager searchManager) {
        this.searchManager = searchManager;
    }

    @Autowired
    public void wireInternetManager(InternetManager internetManager) {
        this.internetManager = internetManager;
    }

    @PostConstruct
    public void initialise() {
        artistIndexMap = new HashMap<>();
        artistIndex = new AtomicInteger(1);

        albumIndexMap = new HashMap<>();
        albumIndex = new AtomicInteger(1);

        trackIndex = new AtomicInteger(1);
    }

    void parse(URL dataFile) {
        log.info("Loading data from - {}", dataFile);

        try {
            ParserModelData parserModelData = new ParserModelData();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(
                    internetManager.openConnection(dataFile).getInputStream()), Charset.forName("UTF-8")))) {
                reader.lines().forEach(line -> {
                    try {
                        // Split the string into row data
                        String[] rowData = line.split("\\|@\\|");

                        // B = Band, A = Album, T = Track
                        if ("B".equals(getRowData(rowData, 0))) {
                            ParserModelArtist parserModelArtist = parseArtist(rowData);
                            parserModelData.setArtist(parserModelArtist);
                            trackIndex.set(1);

                            searchManager.addArtist(Artist.builder()
                                    .artistId(Integer.toString(parserModelArtist.getArtistId()))
                                    .artistName(parserModelArtist.getArtistName())
                                    .artistImage(parserModelArtist.getArtistImage())
                                    .biography(parserModelArtist.getBiography())
                                    .members(parserModelArtist.getMembers())
                                    .build());
                        } else if ("A".equals(getRowData(rowData, 0))) {
                            parserModelData.setAlbum(parseAlbum(rowData));
                            trackIndex.set(1);
                        } else if ("T".equals(getRowData(rowData, 0))) {
                            ParserModelArtist parserModelArtist = parserModelData.getArtist();
                            ParserModelAlbum parserModelAlbum = parserModelData.getAlbum();
                            ParserModelTrack parserModelTrack = parseTrack(rowData);
                            String trackKey = getTrackKey(parserModelArtist, parserModelAlbum, parserModelTrack);

                            searchManager.addTrack(new Track(Integer.toString(parserModelArtist.getArtistId()),
                                    parserModelArtist.getArtistName(), parserModelArtist.getArtistImage(),
                                    Integer.toString(parserModelAlbum.getAlbumId()), parserModelAlbum.getAlbumName(),
                                    parserModelAlbum.getAlbumImage(), parserModelAlbum.getYear(),
                                    hashGenerator.generateHash(trackKey),
                                    parserModelTrack.getTrackName(),
                                    parserModelTrack.getNumber(), appProperties.getS3BucketUrl() + trackKey,
                                    parserModelTrack.isPreferred(), parserModelArtist.getGenres()));
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

    private String getTrackKey(ParserModelArtist parserModelArtist, ParserModelAlbum parserModelAlbum,
                               ParserModelTrack parserModelTrack) {
        return "music/" +
                parserModelAlbum.getYear() + "/" +
                parserModelArtist.getArtistId() + "/" +
                parserModelAlbum.getAlbumId() + "/" +
                formatNumber(parserModelTrack.getNumber());
    }

    private String formatNumber(int index) {
        String string = "000" + index;

        return string.substring(string.length() - 3);
    }

    private ParserModelArtist parseArtist(String[] rowData) {
        return ParserModelArtist.builder()
                .artistId(ofNullable(artistIndexMap.get(getRowData(rowData, 1))).orElseGet(() -> {
                    int nextIndex = artistIndex.getAndIncrement();
                    artistIndexMap.put(getRowData(rowData, 1), nextIndex);

                    return nextIndex;
                }))
                .artistName(getRowData(rowData, 2))
                //.artistImage(getRowData(rowData, 3))
                .biography(getRowData(rowData, 4))
                .members(getRowData(rowData, 5))
                .genres(ofNullable(getRowData(rowData, 6))
                        .map(genres -> stream(genres.split(","))
                                .filter(genre -> genre.trim().length() > 0)
                                .map(this::cleanGenre)
                                .collect(toList())
                        )
                        .orElse(singletonList(UNSPECIFIED_GENRE)))
                .build();
    }

    private ParserModelAlbum parseAlbum(String[] rowData) {
        return ParserModelAlbum.builder()
                .albumId(ofNullable(albumIndexMap.get(getRowData(rowData, 1))).orElseGet(() -> {
                    int nextIndex = albumIndex.getAndIncrement();
                    albumIndexMap.put(getRowData(rowData, 1), nextIndex);

                    return nextIndex;
                }))
                .albumName(getRowData(rowData, 2))
                //.albumImage(getRowData(rowData, 3))
                .year(ofNullable(getRowData(rowData, 4)).map(Integer::valueOf).orElse(null))
                .build();
    }

    private ParserModelTrack parseTrack(String[] rowData) {
        return ParserModelTrack.builder()
                .number(trackIndex.getAndIncrement())
                .trackName(getRowData(rowData, 2))
                .isPreferred(Boolean.valueOf(getRowData(rowData, 4)))
                .build();
    }

    private String getRowData(String[] rowData, int index) {
        if (index > (rowData.length - 1)) {
            return null;
        }

        return rowData[index].trim();
    }

    private String cleanGenre(String genre) {
        if (genre == null) {
            return UNSPECIFIED_GENRE;
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
            return toTitleCase(genre.replaceAll("&amp;", "&"));
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
    private static class ParserModelAlbum {
        private final int albumId;
        private final String albumName;
        private final String albumImage;
        private final Integer year;
    }

    @Value
    @Builder
    private static class ParserModelArtist {
        private final int artistId;
        private final String artistName;
        private final String artistImage;
        private final String biography;
        private final String members;
        private final List<String> genres;
    }

    @Value
    @Builder
    private static class ParserModelTrack {
        private final int number;
        private final String trackName;
        private final boolean isPreferred;
    }
}
