package uk.co.mpcontracting.rpmjukebox.component;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.Test;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.generateTrack;

public class TrackTableViewTest extends AbstractGUITest {

    @Test
    public void shouldHighlightTrack() {
        TrackTableView<Track> underTest = new TrackTableView<>();
        underTest.setItems(getTrackTableModels());

        underTest.highlightTrack(generateTrack(5));

        int selectedIndex = underTest.getSelectionModel().getSelectedIndex();
        int focusedIndex = underTest.getFocusModel().getFocusedIndex();

        assertThat(selectedIndex).isEqualTo(5);
        assertThat(focusedIndex).isEqualTo(5);
    }

    @Test
    public void shouldNotHighlightTrackWhenTrackIsNull() {
        TrackTableView<Track> underTest = new TrackTableView<>();
        underTest.setItems(getTrackTableModels());

        underTest.highlightTrack(null);

        int selectedIndex = underTest.getSelectionModel().getSelectedIndex();
        int focusedIndex = underTest.getFocusModel().getFocusedIndex();

        assertThat(selectedIndex).isEqualTo(-1);
        assertThat(focusedIndex).isEqualTo(0);
    }

    @Test
    public void shouldNotHightlightTrackWhenItemsAreNull() {
        TrackTableView<Track> underTest = new TrackTableView<>();
        underTest.setItems(null);

        underTest.highlightTrack(generateTrack(5));

        int selectedIndex = underTest.getSelectionModel().getSelectedIndex();
        int focusedIndex = underTest.getFocusModel().getFocusedIndex();

        assertThat(selectedIndex).isEqualTo(-1);
        assertThat(focusedIndex).isEqualTo(-1);
    }

    @Test
    public void shouldNotHighlightTrackWhenTrackNotFound() {
        TrackTableView<Track> underTest = new TrackTableView<>();
        underTest.setItems(getTrackTableModels());

        Track track = generateTrack(5);
        track.setArtistId("12350");
        track.setAlbumId("45650");
        track.setTrackId("78950");

        underTest.highlightTrack(track);

        int selectedIndex = underTest.getSelectionModel().getSelectedIndex();
        int focusedIndex = underTest.getFocusModel().getFocusedIndex();

        assertThat(selectedIndex).isEqualTo(-1);
        assertThat(focusedIndex).isEqualTo(0);
    }

    private ObservableList<TrackTableModel> getTrackTableModels() {
        ObservableList<TrackTableModel> trackTableModels = FXCollections.observableArrayList();

        for (int i = 0; i < 10; i++) {
            trackTableModels.add(new TrackTableModel(generateTrack(i)));
        }

        return trackTableModels;
    }
}
