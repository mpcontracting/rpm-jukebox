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
        TrackTableView<Track> trackTableView = new TrackTableView<>();
        trackTableView.setItems(getTrackTableModels());

        trackTableView.highlightTrack(generateTrack(5));

        int selectedIndex = trackTableView.getSelectionModel().getSelectedIndex();
        int focussedIndex = trackTableView.getFocusModel().getFocusedIndex();

        assertThat(selectedIndex).isEqualTo(5);
        assertThat(focussedIndex).isEqualTo(5);
    }

    @Test
    public void shouldNotHighlightTrackWhenTrackIsNull() {
        TrackTableView<Track> trackTableView = new TrackTableView<>();
        trackTableView.setItems(getTrackTableModels());

        trackTableView.highlightTrack(null);

        int selectedIndex = trackTableView.getSelectionModel().getSelectedIndex();
        int focussedIndex = trackTableView.getFocusModel().getFocusedIndex();

        assertThat(selectedIndex).isEqualTo(-1);
        assertThat(focussedIndex).isEqualTo(0);
    }

    @Test
    public void shouldNotHightlightTrackWhenItemsAreNull() {
        TrackTableView<Track> trackTableView = new TrackTableView<>();
        trackTableView.setItems(null);

        trackTableView.highlightTrack(generateTrack(5));

        int selectedIndex = trackTableView.getSelectionModel().getSelectedIndex();
        int focussedIndex = trackTableView.getFocusModel().getFocusedIndex();

        assertThat(selectedIndex).isEqualTo(-1);
        assertThat(focussedIndex).isEqualTo(-1);
    }

    @Test
    public void shouldNotHighlightTrackWhenTrackNotFound() {
        TrackTableView<Track> trackTableView = new TrackTableView<>();
        trackTableView.setItems(getTrackTableModels());

        Track track = generateTrack(5);
        track.setArtistId("12350");
        track.setAlbumId("45650");
        track.setTrackId("78950");

        trackTableView.highlightTrack(track);

        int selectedIndex = trackTableView.getSelectionModel().getSelectedIndex();
        int focussedIndex = trackTableView.getFocusModel().getFocusedIndex();

        assertThat(selectedIndex).isEqualTo(-1);
        assertThat(focussedIndex).isEqualTo(0);
    }

    private ObservableList<TrackTableModel> getTrackTableModels() {
        ObservableList<TrackTableModel> trackTableModels = FXCollections.observableArrayList();

        for (int i = 0; i < 10; i++) {
            trackTableModels.add(new TrackTableModel(generateTrack(i)));
        }

        return trackTableModels;
    }
}
