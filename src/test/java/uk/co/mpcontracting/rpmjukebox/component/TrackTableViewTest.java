package uk.co.mpcontracting.rpmjukebox.component;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createAlbumId;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createArtistId;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createTrack;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createTrackId;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.getFaker;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractGuiTest;

class TrackTableViewTest extends AbstractGuiTest {

  private TrackTableView underTest;

  @BeforeEach
  void beforeEach() {
    underTest = new TrackTableView();
  }

  @Test
  void shouldHighlightTrack() {
    ObservableList<TrackTableModel> trackTableModels = createTrackTableModels();
    int index = getFaker().number().numberBetween(0, 9);
    Track track = trackTableModels.get(index).getTrack();

    underTest.setItems(trackTableModels);
    underTest.highlightTrack(track);

    int selectedIndex = underTest.getSelectionModel().getSelectedIndex();
    int focusedIndex = underTest.getFocusModel().getFocusedIndex();

    assertThat(selectedIndex).isEqualTo(index);
    assertThat(focusedIndex).isEqualTo(index);
  }

  @Test
  void shouldNotHighlightTrackWhenTrackIsNull() {
    underTest.setItems(createTrackTableModels());
    underTest.highlightTrack(null);

    int selectedIndex = underTest.getSelectionModel().getSelectedIndex();
    int focusedIndex = underTest.getFocusModel().getFocusedIndex();

    assertThat(selectedIndex).isEqualTo(-1);
    assertThat(focusedIndex).isEqualTo(0);
  }

  @Test
  void shouldNotHighlightTrackWhenItemsAreNull() {
    underTest.setItems(null);
    underTest.highlightTrack(createTrack(getFaker().number().numberBetween(0, 9)));

    int selectedIndex = underTest.getSelectionModel().getSelectedIndex();
    int focusedIndex = underTest.getFocusModel().getFocusedIndex();

    assertThat(selectedIndex).isEqualTo(-1);
    assertThat(focusedIndex).isEqualTo(-1);
  }

  @Test
  void shouldNotHighlightTrackWhenTrackNotFound() {
    underTest.setItems(createTrackTableModels());

    Track track = createTrack(getFaker().number().numberBetween(0, 9));
    track.setArtistId(createArtistId() + "X");
    track.setAlbumId(createAlbumId() + "X");
    track.setTrackId(createTrackId() + "X");

    underTest.highlightTrack(track);

    int selectedIndex = underTest.getSelectionModel().getSelectedIndex();
    int focusedIndex = underTest.getFocusModel().getFocusedIndex();

    assertThat(selectedIndex).isEqualTo(-1);
    assertThat(focusedIndex).isEqualTo(0);
  }

  private ObservableList<TrackTableModel> createTrackTableModels() {
    ObservableList<TrackTableModel> trackTableModels = FXCollections.observableArrayList();

    for (int i = 0; i < 10; i++) {
      trackTableModels.add(new TrackTableModel(createTrack(i)));
    }

    return trackTableModels;
  }
}