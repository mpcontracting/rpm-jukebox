package uk.co.mpcontracting.rpmjukebox.test.util;

import static java.util.Arrays.asList;
import static java.util.Optional.of;

import com.github.javafaker.Faker;
import com.igormaznitsa.commons.version.Version;
import java.util.Optional;
import javafx.event.EventType;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Track;

public final class TestDataHelper {

  private TestDataHelper() {}

  private static final Faker FAKER = new Faker();

  public static Faker getFaker() {
    return FAKER;
  }

  public static Version createVersion() {
    return new Version(FAKER.numerify("##.##.##"));
  }

  public static int createPlaylistId() {
    return FAKER.number().numberBetween(1, 10000);
  }

  public static String createPlaylistName() {
    return FAKER.lorem().characters(10, 20);
  }

  public static Optional<Playlist> createPlaylist() {
    Playlist playlist = new Playlist(1, createPlaylistName(), 10);

    for (int i = 0; i < 10; i++) {
      playlist.addTrack(createTrack(i));
    }

    return of(playlist);
  }

  public static String createArtistId() {
    return FAKER.numerify("######");
  }

  public static String createArtistName() {
    return FAKER.lorem().characters(10, 20);
  }

  public static String createAlbumId() {
    return FAKER.numerify("######");
  }

  public static String createAlbumName() {
    return FAKER.lorem().characters(25, 50);
  }

  public static String createAlbumImage() {
    return FAKER.internet().url();
  }

  public static int createYear() {
    return FAKER.number().numberBetween(1950, 2020);
  }

  public static String createYearString() {
    return Integer.toString(createYear());
  }

  public static String createTrackId() {
    return FAKER.numerify("######");
  }

  public static String createTrackName() {
    return FAKER.lorem().characters(25, 50);
  }

  public static String createLocation() {
    return FAKER.lorem().characters(10, 20);
  }

  public static String createGenre() {
    return FAKER.music().genre();
  }

  public static Track createTrack(int index, String... genres) {
    return Track.builder()
        .artistId(createArtistId())
        .artistName(createArtistName())
        .albumId(createAlbumId())
        .albumName(createAlbumName())
        .albumImage(createAlbumImage())
        .year(createYear())
        .trackId(createTrackId())
        .trackName(createTrackName())
        .index(index)
        .location(createLocation())
        .isPreferred(FAKER.bool().bool())
        .genres(genres.length < 1 ? null : asList(genres))
        .build();
  }

  public static KeyEvent createKeyEvent(EventType<KeyEvent> eventType, KeyCode keyCode) {
    return new KeyEvent(eventType, null, null, keyCode, false, false, false, false);
  }

  public static MouseEvent createMouseEvent(EventType<MouseEvent> eventType, MouseButton mouseButton, int numberOfClicks) {
    return new MouseEvent(eventType, 0, 0, 0, 0, mouseButton, numberOfClicks, false, false, false, false, true,
        false, false, false, false, true, null);
  }

  public static ContextMenuEvent createContextMenuEvent(Object source) {
    return new ContextMenuEvent(source, null, ContextMenuEvent.CONTEXT_MENU_REQUESTED, 0, 0, 0, 0, false, null);
  }

  public static DragEvent createDragEvent(EventType<DragEvent> eventType, Dragboard dragboard, TransferMode transferMode,
      Object gestureSource) {
    return new DragEvent(eventType, dragboard, 0, 0, 0, 0, transferMode, gestureSource, null, null);
  }

}
