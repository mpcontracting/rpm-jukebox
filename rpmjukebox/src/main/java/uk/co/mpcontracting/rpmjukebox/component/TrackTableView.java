package uk.co.mpcontracting.rpmjukebox.component;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.model.Track;

@Slf4j
public class TrackTableView<T> extends TableView<TrackTableModel> {

    public void highlightTrack(Track track) {
        log.debug("Highlighting track - {}", track);

        if (track != null) {
            ObservableList<TrackTableModel> observableTracks = getItems();

            if (observableTracks != null) {
                for (int i = 0; i < observableTracks.size(); i++) {
                    TrackTableModel trackTableModel = observableTracks.get(i);

                    if (trackTableModel.getTrack() != null && trackTableModel.getTrack().equals(track)) {
                        getSelectionModel().select(i);
                        getFocusModel().focus(i);

                        break;
                    }
                }
            }
        }
    }
}
