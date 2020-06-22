package uk.co.mpcontracting.rpmjukebox.test.support;

import javafx.event.EventType;
import javafx.scene.input.*;
import lombok.SneakyThrows;
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
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.of;
import static org.springframework.test.util.ReflectionTestUtils.getField;

public abstract class TestHelper {

    private TestHelper() {
    }

    public static File getConfigDirectory() {
        return new File(System.getProperty("user.home") + File.separator + ".rpmjukeboxtest");
    }

    public static long getLocalDateTimeInMillis(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()).getTime();
    }

    public static long getDateTimeInMillis(int year, int month, int day, int hour, int minute) {
        return getLocalDateTimeInMillis(LocalDateTime.of(year, month, day, hour, minute));
    }

    @SneakyThrows
    public static File getTestResourceFile(String path) {
        return new ClassPathResource(path).getFile();
    }

    @SneakyThrows
    public static String getTestResourceContent(String path) {
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

    public static Optional<Playlist> generatePlaylist() {
        Playlist playlist = new Playlist(1, "Playlist", 10);
        for (int i = 0; i < 10; i++) {
            playlist.addTrack(generateTrack(i));
        }

        return of(playlist);
    }

    public static Object getNonNullField(Object object, String field) {
        return requireNonNull(getField(object, field));
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
                .albumId("456" + index)
                .albumName("Album Name " + index)
                .albumImage("Album Image " + index)
                .year(2000 + index)
                .trackId("789" + index)
                .trackName("Track Name " + index)
                .index(index)
                .location("Location " + index)
                .isPreferred(true)
                .genres(genres.length < 1 ? null : asList(genres))
                .build();
    }
}
