package uk.co.mpcontracting.rpmjukebox.component;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class TrackTableViewTest extends AbstractTest {

    @Test
    public void shouldHighlightTrack() {
        TrackTableView<Track> trackTableView = new TrackTableView<>();
        trackTableView.setItems(getTrackTableModels());

        trackTableView.highlightTrack(new Track("1235", null, null, "4565", null, null, 2005, "7895", null, 5, null, false, null));

        int selectedIndex = trackTableView.getSelectionModel().getSelectedIndex();
        int focussedIndex = trackTableView.getFocusModel().getFocusedIndex();
        
        assertThat("Selected index should be 5", selectedIndex, equalTo(5));
        assertThat("Focussed index should be 5", focussedIndex, equalTo(5));
    }
    
    @Test
    public void shouldNotHighlightTrackWhenTrackIsNull() {
        TrackTableView<Track> trackTableView = new TrackTableView<>();
        trackTableView.setItems(getTrackTableModels());

        trackTableView.highlightTrack(null);

        int selectedIndex = trackTableView.getSelectionModel().getSelectedIndex();
        int focussedIndex = trackTableView.getFocusModel().getFocusedIndex();
        
        assertThat("Selected index should be -1", selectedIndex, equalTo(-1));
        assertThat("Focussed index should be 0", focussedIndex, equalTo(0));
    }
    
    @Test
    public void shouldNotHightlightTrackWhenItemsAreNull() {
        TrackTableView<Track> trackTableView = new TrackTableView<>();
        trackTableView.setItems(null);

        trackTableView.highlightTrack(new Track("1235", null, null, "4565", null, null, 2005, "7895", null, 5, null, false, null));

        int selectedIndex = trackTableView.getSelectionModel().getSelectedIndex();
        int focussedIndex = trackTableView.getFocusModel().getFocusedIndex();
        
        assertThat("Selected index should be -1", selectedIndex, equalTo(-1));
        assertThat("Focussed index should be -1", focussedIndex, equalTo(-1));
    }
    
    @Test
    public void shouldNotHighlightTrackWhenTrackNotFound() {
        TrackTableView<Track> trackTableView = new TrackTableView<>();
        trackTableView.setItems(getTrackTableModels());

        trackTableView.highlightTrack(new Track("12350", null, null, "45650", null, null, 2005, "78950", null, 5, null, false, null));

        int selectedIndex = trackTableView.getSelectionModel().getSelectedIndex();
        int focussedIndex = trackTableView.getFocusModel().getFocusedIndex();
        
        assertThat("Selected index should be -1", selectedIndex, equalTo(-1));
        assertThat("Focussed index should be 0", focussedIndex, equalTo(0));
    }

    private ObservableList<TrackTableModel> getTrackTableModels() {
        ObservableList<TrackTableModel> trackTableModels = FXCollections.observableArrayList();
        
        for (int i = 0; i < 10; i++) {
            trackTableModels.add(new TrackTableModel(new Track("123" + i, null, null, "456" + i, null, null, 2000 + i, "789" + i, null, i, null, false, null)));
        }
        
        return trackTableModels;
    }
}
