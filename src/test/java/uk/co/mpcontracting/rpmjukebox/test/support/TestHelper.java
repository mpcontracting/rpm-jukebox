package uk.co.mpcontracting.rpmjukebox.test.support;

import javafx.event.EventType;
import javafx.scene.input.*;
import org.springframework.core.io.ClassPathResource;
import uk.co.mpcontracting.rpmjukebox.model.Artist;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Track;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static java.util.Arrays.asList;

public abstract class TestHelper {

    private TestHelper() {}

    public static File getConfigDirectory() {
        return new File(System.getProperty("user.home") + File.separator + ".rpmjukeboxtest");
    }

    public static long getLocalDateTimeInMillis(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()).getTime();
    }

    public static long getDateTimeInMillis(int year, int month, int day, int hour, int minute) {
        return getLocalDateTimeInMillis(LocalDateTime.of(year, month, day, hour, minute));
    }

    public static File getTestResourceFile(String path) throws Exception {
        return new ClassPathResource(path).getFile();
    }

    public static String getTestResourceContent(String path) throws Exception {
        StringBuilder builder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(getTestResourceFile(path)))) {
            reader.lines().forEach(line -> {
                builder.append(line);
                builder.append("\r\n");
            });
        }

        return builder.toString();
    }

    public static KeyEvent getKeyEvent(EventType<KeyEvent> eventType, KeyCode keyCode) {
        return new KeyEvent(eventType, null, null, keyCode, false, false, false, false);
    }

    public static MouseEvent getMouseEvent(EventType<MouseEvent> eventType, MouseButton mouseButton, int numberOfClicks) {
        return new MouseEvent(eventType, 0, 0, 0, 0, mouseButton, numberOfClicks, false, false, false, false, true,
                false, false, false, false, true, null);
    }

    public static ContextMenuEvent getContextMenuEvent(Object source) {
        return new ContextMenuEvent(source, null, ContextMenuEvent.CONTEXT_MENU_REQUESTED, 0, 0, 0, 0, false, null);
    }

    public static DragEvent getDragEvent(EventType<DragEvent> eventType, Dragboard dragboard, TransferMode transferMode,
                                     Object gestureSource) {
        return new DragEvent(eventType, dragboard, 0, 0, 0, 0, transferMode, gestureSource, null, null);
    }

    public static Playlist generatePlaylist() {
        Playlist playlist = new Playlist(1, "Playlist", 10);
        for (int i = 0; i < 10; i++) {
            playlist .addTrack(generateTrack(i));
        }

        return playlist;
    }

    public static Artist generateArtist(int index) {
        return Artist.builder()
                .artistId("123" + index)
                .artistName("Artist Name " + index)
                .artistImage("Artist Image " + index)
                .biography("Biography " + index)
                .members("Members " + index)
                .build();
    }

    public static Track generateTrack(int index, String... genres) {
        return Track.builder()
                .artistId("123" + index)
                .artistName("Artist Name " + index)
                .artistImage("Artist Image " + index)
                .albumId("456" + index)
                .albumName("Album Name " + index)
                .albumImage("Album Image " + index)
                .year(2000 + index)
                .trackId("789" + index)
                .trackName("Track Name " + index)
                .number(index)
                .location("Location " + index)
                .isPreferred(true)
                .genres(genres.length < 1 ? null : asList(genres))
                .build();
    }
}
